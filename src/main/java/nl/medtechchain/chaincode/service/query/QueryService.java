package nl.medtechchain.chaincode.service.query;

import com.google.privacy.differentialprivacy.LaplaceNoise;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Timestamp;
import nl.medtechchain.chaincode.service.query.util.FieldUtil;
import nl.medtechchain.proto.common.ChaincodeError;
import nl.medtechchain.proto.devicedata.DeviceDataAsset;
import nl.medtechchain.proto.query.Filter;
import nl.medtechchain.proto.query.Query;
import nl.medtechchain.proto.query.QueryResult;

import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static nl.medtechchain.chaincode.util.ConfigUtil.differentialPrivacyConfig;
import static nl.medtechchain.chaincode.util.ConfigUtil.encryptionEnabled;
import static nl.medtechchain.chaincode.util.EncodingUtil.bigIntegerToString;

public class QueryService {

    private final FieldUtil fieldUtil = new FieldUtil();
    private final DecryptionService decryptionService = new DecryptionService();

    public Optional<ChaincodeError> validateQuery(Query tx) {
        for (Filter filter : tx.getFiltersList()) {
            if (filter.getField().equals(tx.getField()))
                return Optional.of(error("Target field specified as filter: " + tx.getField()));

            var descriptor = fieldUtil.fieldDescriptor(filter.getField());

            if (descriptor.isEmpty())
                return Optional.of(error("Unknown filter field: " + filter.getField()));

            var invalidFilter = Optional.of(error("Invalid filter - comparator did not match field type: " + filter.getField()));

            switch (filter.getComparatorCase()) {
                case STRING_FILTER:
                    if (descriptor.get().getType() != Descriptors.FieldDescriptor.Type.STRING && descriptor.get().getType() != Descriptors.FieldDescriptor.Type.ENUM)
                        return invalidFilter;
                    break;
                case INT_FILTER:
                    if (descriptor.get().getType() != Descriptors.FieldDescriptor.Type.INT32 && descriptor.get().getType() != Descriptors.FieldDescriptor.Type.INT64)
                        return invalidFilter;
                    break;
                case BOOL_FILTER:
                    if (descriptor.get().getType() != Descriptors.FieldDescriptor.Type.BOOL)
                        return invalidFilter;
                    break;
                case TIMESTAMP_FILTER:
                    if (descriptor.get().getType() != Descriptors.FieldDescriptor.Type.MESSAGE)
                        return invalidFilter;

                    if (!descriptor.get().getMessageType().getFullName().equals("google.protobuf.Timestamp"))
                        return invalidFilter;
                    break;
                case COMPARATOR_NOT_SET:
                    return Optional.of(error("Comparator for filter not set: " + filter.getField()));
            }
        }

        return Optional.empty();
    }

    public QueryResult count(Query query, List<DeviceDataAsset> assets) {
        var result = groupedCountRaw(query, assets).size();

        var differentialPrivacyConfig = differentialPrivacyConfig();
        switch (differentialPrivacyConfig.getMechanismCase()) {
            case LAPLACE:
                double epsilon = differentialPrivacyConfig.getLaplace().getEpsilon();
                result = (int) new LaplaceNoise().addNoise(result, 1, epsilon, 0.);
        }

        return QueryResult.newBuilder().setCountResult(result).build();
    }

    public QueryResult groupedCount(Query query, List<DeviceDataAsset> assets) {
        var result = groupedCountRaw(query, assets);

        var differentialPrivacyConfig = differentialPrivacyConfig();
        switch (differentialPrivacyConfig.getMechanismCase()) {
            case LAPLACE:
                var noise = new LaplaceNoise();
                double epsilon = differentialPrivacyConfig.getLaplace().getEpsilon();
                result.replaceAll((key, value) -> (int) noise.addNoise((int) value, 1, epsilon, 0.));

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

        if (fieldUtil.isPlainField(query.getField()))
            for (DeviceDataAsset asset : assets) {
                var key = fieldUtil.extractPlainFieldValue(asset, query.getField()).toString();
                result.put(key, result.getOrDefault(key, 0) + 1);
            }
        else if (fieldUtil.isSensitiveField(query.getField()))
            if (encryptionEnabled())
                for (DeviceDataAsset asset : assets) {
                    var keyEncrypted = fieldUtil.extractSensitiveFieldValue(asset, query.getField());
                    var descriptor = fieldUtil.fieldDescriptor(query.getField());
                    if (descriptor.isEmpty())
                        throw new IllegalStateException("Unknown target field");

                    var decrypted = decryptionService.decrypt(keyEncrypted);
                    if (decrypted.isEmpty())
                        throw new IllegalStateException("Could not decrypt value: " + keyEncrypted);

                    var key = parseEncryptedValue(decrypted.get(), descriptor.get());

                    result.put(key, result.getOrDefault(key, 0) + 1);
                }
            else
                for (DeviceDataAsset asset : assets) {
                    var key = fieldUtil.extractSensitiveFieldValue(asset, query.getField());
                    result.put(key, result.getOrDefault(key, 0) + 1);
                }
        else
            throw new IllegalStateException("Field " + query.getField() + " is not present in asset");

        return result;
    }

    private String parseEncryptedValue(String value, Descriptors.FieldDescriptor descriptor) {
        switch (descriptor.getType()) {
            case INT32:
            case INT64:
                return "" + Long.parseLong(value);
            case STRING:
                return bigIntegerToString(new BigInteger(value));
            case BOOL:
                return "" + (Long.parseLong(value) != 0);
            case MESSAGE:
                if (descriptor.getMessageType().getFullName().equals("google.protobuf.Timestamp"))
                    return Instant.ofEpochSecond(Long.parseLong(value)).toString();
            default:
                throw new IllegalArgumentException("Unhandled field type: " + descriptor.getType());
        }
    }

    private ChaincodeError error(String details) {
        return ChaincodeError.newBuilder().setCode(ChaincodeError.ErrorCode.ERROR_CODE_INVALID_TRANSACTION).setMessage("Bad query").setDetails(details).build();
    }
}
