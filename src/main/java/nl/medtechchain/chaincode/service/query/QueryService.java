package nl.medtechchain.chaincode.service.query;

import com.google.privacy.differentialprivacy.LaplaceNoise;
import com.google.protobuf.Descriptors;
import nl.medtechchain.chaincode.config.ConfigOps;
import nl.medtechchain.chaincode.service.differentialprivacy.MechanismType;
import nl.medtechchain.chaincode.service.encryption.PlatformEncryptionInterface;
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

    // TODO
    public QueryResult average(Query query, List<DeviceDataAsset> assets) {
        return null;
//        var descriptor = fieldUtil.fieldDescriptor(query.getField());
//        if (descriptor.isEmpty())
//            throw new IllegalStateException("Unknown target field");
//
//        if (fieldUtil.isPlainField(query.getField())) {
//            var sensitivity = l1sensitivity(assets, descriptor.get());
//
//        } else
//            // when data is encrypted computing the sensitivity automatically cannot be performed
//            // since it would require decrypting data
//            // additional logic is needed to perform differential privacy in this case
//            // currently, no average is allowed on sensitive fields
//            if (fieldUtil.isSensitiveField(query.getField()))
//            if (encryptionEnabled())
//
//        else
//
//        else
//        throw new IllegalStateException("Field " + query.getField() + " is not present in asset");


    }

//    private long l1sensitivity(List<DeviceDataAsset> assets, Descriptors.FieldDescriptor plainFieldDescriptor) {
//
//        switch (plainFieldDescriptor.getType()) {
//            case INT32:
//            case INT64:
//                var intValues = assets.stream()
//                        .map(asset -> (long) fieldUtil.extractPlainFieldValue(asset, plainFieldDescriptor.getName()))
//                        .collect(Collectors.toList());
//                return diffMaxMin(intValues).filter(v -> v != 0).orElse((long) 1);
//            case BOOL:
//                return 1;
//            case MESSAGE:
//                if (plainFieldDescriptor.getMessageType().getFullName().equals("google.protobuf.Timestamp")) {
//                    var timestampValues = assets.stream()
//                            .map(asset -> (Timestamp) fieldUtil.extractPlainFieldValue(asset, plainFieldDescriptor.getName()))
//                            .map(Timestamp::getSeconds)
//                            .collect(Collectors.toList());
//                    return diffMaxMin(timestampValues).filter(v -> v != 0).orElse((long) 1);
//                }
//            default:
//                throw new IllegalArgumentException("Unhandled field type: " + plainFieldDescriptor.getType());
//        }
//    }

    private Optional<Long> diffMaxMin(List<Long> list) {
        return list.stream().max(Comparator.naturalOrder()).flatMap(max -> list.stream().min(Comparator.naturalOrder()).map(min -> max - min));
    }

    private Map<String, Integer> groupedCountRaw(Query query, List<DeviceDataAsset> assets) {
        var result = new HashMap<String, Integer>();

        var descriptor = deviceDataDescriptorByName(query.getTargetField());
        var fieldType = DeviceDataFieldType.fromFieldName(query.getTargetField());
        assert descriptor.isPresent();

        for (DeviceDataAsset asset : assets) {
            String key = null;
            switch (fieldType) {
                case STRING:
                    var stringFieldValue = (DeviceDataAsset.StringField) asset.getField(descriptor.get());

                    switch (stringFieldValue.getFieldCase()) {
                        case PLAIN:
                            key = stringFieldValue.getPlain();
                            break;
                        case ENCRYPTED:
                            if (encryptionInterface == null)
                                throw new IllegalStateException("Field " + query.getTargetField() + " is encrypted, but the platform is not properly configured to use encryption.");
                            key = encryptionInterface.decryptString(stringFieldValue.getEncrypted());
                            break;
                    }

                    break;
                case INTEGER:
                    var integerFieldValue = (DeviceDataAsset.IntegerField) asset.getField(descriptor.get());
                    switch (integerFieldValue.getFieldCase()) {
                        case PLAIN:
                            key = integerFieldValue.getPlain() + "";
                            break;
                        case ENCRYPTED:
                            if (encryptionInterface == null)
                                throw new IllegalStateException("Field " + query.getTargetField() + " is encrypted, but the platform is not properly configured to use encryption.");
                            key = encryptionInterface.decryptLong(integerFieldValue.getEncrypted()) + "";
                            break;
                    }
                    break;
                case BOOL:
                    var boolFieldValue = (DeviceDataAsset.BoolField) asset.getField(descriptor.get());
                    switch (boolFieldValue.getFieldCase()) {
                        case PLAIN:
                            key = boolFieldValue.getPlain() + "";
                            break;
                        case ENCRYPTED:
                            if (encryptionInterface == null)
                                throw new IllegalStateException("Field " + query.getTargetField() + " is encrypted, but the platform is not properly configured to use encryption.");
                            key = encryptionInterface.decryptBool(boolFieldValue.getEncrypted()) + "";
                            break;
                    }
                    break;
                case TIMESTAMP:
                    var timestampFieldValue = (DeviceDataAsset.TimestampField) asset.getField(descriptor.get());
                    switch (timestampFieldValue.getFieldCase()) {
                        case PLAIN:
                            key = timestampFieldValue.getPlain().getSeconds() + "";
                            break;
                        case ENCRYPTED:
                            if (encryptionInterface == null)
                                throw new IllegalStateException("Field " + query.getTargetField() + " is encrypted, but the platform is not properly configured to use encryption.");
                            key = encryptionInterface.decryptLong(timestampFieldValue.getEncrypted()) + "";
                            break;
                    }
                    break;
                case MEDICAL_SPECIALITY:
                    var medicalSpecialityFieldValue = (DeviceDataAsset.MedicalSpecialityField) asset.getField(descriptor.get());
                    switch (medicalSpecialityFieldValue.getFieldCase()) {
                        case PLAIN:
                            key = medicalSpecialityFieldValue.getPlain().name();
                            break;
                        case ENCRYPTED:
                            if (encryptionInterface == null)
                                throw new IllegalStateException("Field " + query.getTargetField() + " is encrypted, but the platform is not properly configured to use encryption.");
                            key = MedicalSpeciality.forNumber((int) encryptionInterface.decryptLong(medicalSpecialityFieldValue.getEncrypted())).name();
                            break;
                    }
                    break;

                case DEVICE_CATEGORY:
                    var deviceCategoryFieldValue = (DeviceDataAsset.DeviceCategoryField) asset.getField(descriptor.get());
                    switch (deviceCategoryFieldValue.getFieldCase()) {
                        case PLAIN:
                            key = deviceCategoryFieldValue.getPlain().name();
                            break;
                        case ENCRYPTED:
                            if (encryptionInterface == null)
                                throw new IllegalStateException("Field " + query.getTargetField() + " is encrypted, but the platform is not properly configured to use encryption.");
                            key = DeviceCategory.forNumber((int) encryptionInterface.decryptLong(deviceCategoryFieldValue.getEncrypted())).name();
                    }
                    break;
            }
            if (key != null)
                result.put(key, result.getOrDefault(key, 0) + 1);
        }

        return result;
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
