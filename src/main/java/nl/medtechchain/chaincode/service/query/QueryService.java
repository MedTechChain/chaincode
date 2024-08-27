package nl.medtechchain.chaincode.service.query;

import com.google.privacy.differentialprivacy.LaplaceNoise;
import com.google.protobuf.Descriptors;
import nl.medtechchain.chaincode.config.ConfigOps;
import nl.medtechchain.chaincode.service.differentialprivacy.MechanismType;
import nl.medtechchain.chaincode.service.encryption.PlatformEncryptionInterface;
import nl.medtechchain.chaincode.service.query.average.Average;
import nl.medtechchain.chaincode.service.query.groupedcount.GroupedCount;
import nl.medtechchain.proto.common.ChaincodeError;
import nl.medtechchain.proto.config.PlatformConfig;
import nl.medtechchain.proto.devicedata.DeviceCategory;
import nl.medtechchain.proto.devicedata.DeviceDataAsset;
import nl.medtechchain.proto.devicedata.MedicalSpeciality;
import nl.medtechchain.proto.query.Filter;
import nl.medtechchain.proto.query.Query;
import nl.medtechchain.proto.query.QueryResult;

import java.util.*;
import java.util.logging.Logger;

import static nl.medtechchain.chaincode.config.ConfigOps.PlatformConfigOps.get;
import static nl.medtechchain.chaincode.config.ConfigOps.PlatformConfigOps.getUnsafe;
import static nl.medtechchain.proto.config.PlatformConfig.Config.*;

public class QueryService {

    private static final Logger logger = Logger.getLogger(QueryService.class.getName());

    private final PlatformEncryptionInterface encryptionInterface;
    private final PlatformConfig platformConfig;
    private final MechanismType mechanismType;

    public QueryService(PlatformConfig platformConfig) {
        this.encryptionInterface = PlatformEncryptionInterface.Factory.getInstance(platformConfig).orElse(null);
        this.platformConfig = platformConfig;
        String differentialPrivacyProp = get(platformConfig, CONFIG_FEATURE_QUERY_DIFFERENTIAL_PRIVACY).orElse("NONE");
        MechanismType type;
        try {
            type = MechanismType.valueOf(differentialPrivacyProp);
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid differential privacy mechanism: " + differentialPrivacyProp + ", defaulting to none");
            type = MechanismType.NONE;
        }
        this.mechanismType = type;
    }

    public Optional<ChaincodeError> validateQuery(Query query) {
        String validFields = "";
        switch (query.getQueryType()) {
            case COUNT:
                validFields = ConfigOps.PlatformConfigOps.get(platformConfig, CONFIG_FEATURE_QUERY_INTERFACE_COUNT_FIELDS).orElse("");
                break;
            case GROUPED_COUNT:
                validFields = ConfigOps.PlatformConfigOps.get(platformConfig, CONFIG_FEATURE_QUERY_INTERFACE_GROUPED_COUNT_FIELDS).orElse("");
                break;
            case AVERAGE:
                validFields = ConfigOps.PlatformConfigOps.get(platformConfig, CONFIG_FEATURE_QUERY_INTERFACE_AVERAGE_FIELDS).orElse("");
                break;
        }

        if (!validFields.contains(query.getTargetField()))
            return Optional.of(invalidQueryError("Target field not among valid fields for query type " + query.getTargetField() + " not in " + validFields + " for " + query.getQueryType().name()));

        if (deviceDataDescriptorByName(query.getTargetField()).isEmpty())
            return Optional.of(invalidQueryError("Unknown target field: " + query.getTargetField()));


        var fieldType = DeviceDataFieldType.fromFieldName(query.getTargetField());

        for (Filter filter : query.getFiltersList()) {
            if (filter.getField().equals(query.getTargetField()))
                return Optional.of(invalidQueryError("Target field specified as filter: " + query.getTargetField()));

            if (deviceDataDescriptorByName(filter.getField()).isEmpty())
                return Optional.of(invalidQueryError("Unknown filter field: " + filter.getField()));

            var invalidFilter = Optional.of(invalidQueryError("Invalid filter type: " + filter.getField() + " " + fieldType + "!=" + filter.getComparatorCase()));

            switch (filter.getComparatorCase()) {
                case ENUM_FILTER:
                    if (fieldType == DeviceDataFieldType.DEVICE_CATEGORY)
                        try {
                            DeviceCategory.valueOf(filter.getEnumFilter().getValue());
                        } catch (Throwable t) {
                            return invalidFilter;
                        }

                    if (fieldType == DeviceDataFieldType.MEDICAL_SPECIALITY)
                        try {
                            MedicalSpeciality.valueOf(filter.getEnumFilter().getValue());
                        } catch (Throwable t) {
                            return invalidFilter;
                        }
                case STRING_FILTER:
                    if (fieldType != DeviceDataFieldType.STRING)
                        return invalidFilter;
                    break;
                case INTEGER_FILTER:
                    if (fieldType != DeviceDataFieldType.INTEGER)
                        return invalidFilter;
                    break;
                case BOOL_FILTER:
                    if (fieldType != DeviceDataFieldType.BOOL)
                        return invalidFilter;
                    break;
                case TIMESTAMP_FILTER:
                    if (fieldType != DeviceDataFieldType.TIMESTAMP)
                        return invalidFilter;
                    break;
                case COMPARATOR_NOT_SET:
                    return Optional.of(invalidQueryError("Comparator for filter not set: " + filter.getField()));
            }
        }

        return Optional.empty();
    }

    public QueryResult count(Query query, List<DeviceDataAsset> assets) {
        var result = groupedCountRaw(query, assets).size();

        if (Objects.requireNonNull(mechanismType) == MechanismType.LAPLACE) {
            result = (int) new LaplaceNoise().addNoise(result, 1, getEpsilon(), 0.);
        }

        return QueryResult.newBuilder().setCountResult(result).build();
    }

    public QueryResult groupedCount(Query query, List<DeviceDataAsset> assets) {
        var result = groupedCountRaw(query, assets);

        switch (mechanismType) {
            case LAPLACE:
                var noise = new LaplaceNoise();
                result.replaceAll((key, value) -> (int) noise.addNoise((int) value, 1, getEpsilon(), 0.));
        }

        return QueryResult.newBuilder().setGroupedCountResult(QueryResult.GroupedCount.newBuilder().putAllMap(result).build()).build();
    }

    public QueryResult average(Query query, List<DeviceDataAsset> assets) {
        var descriptor = deviceDataDescriptorByName(query.getTargetField());
        var fieldType = DeviceDataFieldType.fromFieldName(query.getTargetField());
        assert descriptor.isPresent();

        Average avg = Average.Factory.getInstance(fieldType);

        switch (fieldType) {
            case INTEGER:
            case TIMESTAMP:
                break;
            default:
                throw new IllegalStateException("Cannot average field type: " + fieldType);
        }

        var result = avg.average(encryptionInterface, descriptor.get(), assets);
        var sum = result.get_1();
        var count = result.get_2();

        switch (mechanismType) {
            case LAPLACE:
                var noise = new LaplaceNoise();
                sum += noise.addNoise(sum, (long) ((double) sum) / count, getEpsilon(), 0);
        }

        return QueryResult.newBuilder().setAverageResult(((double) sum) / count).build();
    }

    private Optional<Long> diffMaxMin(List<Long> list) {
        return list.stream().max(Comparator.naturalOrder()).flatMap(max -> list.stream().min(Comparator.naturalOrder()).map(min -> max - min));
    }

    private Map<String, Integer> groupedCountRaw(Query query, List<DeviceDataAsset> assets) {
        var descriptor = deviceDataDescriptorByName(query.getTargetField());
        var fieldType = DeviceDataFieldType.fromFieldName(query.getTargetField());
        assert descriptor.isPresent();

        return GroupedCount.Factory.getInstance(fieldType).groupedCount(encryptionInterface, descriptor.get(), assets);
    }

    private double getEpsilon() {
        return Double.parseDouble(getUnsafe(platformConfig, CONFIG_FEATURE_QUERY_DIFFERENTIAL_PRIVACY));
    }

    private ChaincodeError invalidQueryError(String details) {
        return ChaincodeError.newBuilder().setCode(ChaincodeError.ErrorCode.INVALID_TRANSACTION).setMessage("Bad query").setDetails(details).build();
    }

    private Optional<Descriptors.FieldDescriptor> deviceDataDescriptorByName(String name) {
        return Optional.ofNullable(DeviceDataAsset.DeviceData.getDescriptor().findFieldByName(name));
    }
}
