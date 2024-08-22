package nl.medtechchain.chaincode.service.query;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;
import lombok.SneakyThrows;
import nl.medtechchain.chaincode.service.query.util.FieldUtil;
import nl.medtechchain.chaincode.util.ConfigUtil;
import nl.medtechchain.proto.devicedata.DeviceDataAsset;
import nl.medtechchain.proto.query.Filter;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static nl.medtechchain.chaincode.util.ConfigUtil.encryptionEnabled;
import static nl.medtechchain.chaincode.util.EncodingUtil.bigIntegerToString;

public class FilterService {

    private static final Logger logger = Logger.getLogger(FilterService.class.getName());

    private final DecryptionService decryptionService = new DecryptionService();
    private final FieldUtil fieldUtil = new FieldUtil();

    public boolean checkEncryptionConfig(DeviceDataAsset asset) {
        var currentEncryptionVersion = ConfigUtil.encryptionVersion();
        boolean encryptionEnabled = currentEncryptionVersion.isPresent();
        return !(!asset.hasEncryptionVersion() && encryptionEnabled ||
                asset.hasEncryptionVersion() && !encryptionEnabled ||
                asset.hasEncryptionVersion() && !asset.getEncryptionVersion().equals(currentEncryptionVersion.get()));
    }


    public boolean checkFilter(DeviceDataAsset asset, Filter filter) {
        try {
            var fieldName = filter.getField();

            if (fieldUtil.isPlainField(fieldName))
                return checkPlainField(asset, filter);
            else if (fieldUtil.isSensitiveField(fieldName))
                if (encryptionEnabled())
                    return checkSensitiveEncryptedField(asset, filter);
                else
                    return checkSensitiveStringField(asset, filter);
            else
                throw new IllegalStateException("Field " + fieldName + " is not present in asset " + asset);


        } catch (Throwable t) {
            logger.severe("Error checking filter: " + filter + ". " + t);
            return false;
        }
    }

    private boolean checkPlainField(DeviceDataAsset asset, Filter filter) {
        Object fieldValue = fieldUtil.extractPlainFieldValue(asset, filter.getField());
        return applyFilter(fieldValue, filter);
    }

    private boolean checkSensitiveStringField(DeviceDataAsset asset, Filter filter) {
        String value = fieldUtil.extractSensitiveFieldValue(asset, filter.getField());
        return applyFilter(parseSensitiveStringValue(value, filter), filter);
    }


    private boolean checkSensitiveEncryptedField(DeviceDataAsset asset, Filter filter) {
        String encryptedValue = fieldUtil.extractSensitiveFieldValue(asset, filter.getField());
        Optional<String> plaintext = decryptionService.decrypt(encryptedValue);
        return plaintext.map(value -> applyFilter(parseSensitiveEncryptedValue(value, filter), filter)).orElse(false);
    }

    @SneakyThrows
    private Object parseSensitiveStringValue(String value, Filter filter) {
        switch (filter.getComparatorCase()) {
            case INT_FILTER:
                return Long.parseLong(value);
            case STRING_FILTER:
                return value;
            case BOOL_FILTER:
                return Boolean.parseBoolean(value);
            case TIMESTAMP_FILTER:
                var builder = Timestamp.newBuilder();
                JsonFormat.parser().merge(value, builder);
                return check(builder.build(), filter.getTimestampFilter());
            default:
                throw new IllegalArgumentException("Unsupported filter type");
        }
    }

    private Object parseSensitiveEncryptedValue(String value, Filter filter) {
        switch (filter.getComparatorCase()) {
            case INT_FILTER:
                return Long.parseLong(value);
            case STRING_FILTER:
                return bigIntegerToString(new BigInteger(value));
            case BOOL_FILTER:
                return Long.parseLong(value) != 0;
            case TIMESTAMP_FILTER:
                return Timestamp.newBuilder().setSeconds(Long.parseLong(value)).build();
            default:
                throw new IllegalArgumentException("Unsupported filter type");
        }
    }

    private boolean applyFilter(Object fieldValue, Filter filter) {
        Predicate<Object> predicate = getFilterPredicate(filter);
        return predicate.test(fieldValue);
    }

    private Predicate<Object> getFilterPredicate(Filter filter) {
        switch (filter.getComparatorCase()) {
            case INT_FILTER:
                return value -> check((long) value, filter.getIntFilter());
            case STRING_FILTER:
                return value -> check((String) value, filter.getStringFilter());
            case BOOL_FILTER:
                return value -> check((boolean) value, filter.getBoolFilter());
            case TIMESTAMP_FILTER:
                return value -> check((Timestamp) value, filter.getTimestampFilter());
            default:
                throw new IllegalArgumentException("Unsupported filter type");
        }
    }

    private boolean check(long value, Filter.IntFilter intFilter) {
        switch (intFilter.getOperator()) {
            case INT_OPERATOR_GREATER_THAN_OR_EQUAL:
                return value >= intFilter.getValue();
            case INT_OPERATOR_EQUALS:
                return value == intFilter.getValue();
            case INT_OPERATOR_LESS_THAN:
                return value < intFilter.getValue();
            case INT_OPERATOR_GREATER_THAN:
                return value > intFilter.getValue();
            case INT_OPERATOR_LESS_THAN_OR_EQUAL:
                return value <= intFilter.getValue();
        }
        return false;
    }

    private boolean check(String value, Filter.StringFilter stringFilter) {
        switch (stringFilter.getOperator()) {
            case STRING_OPERATOR_CONTAINS:
                return value.contains(stringFilter.getValue());
            case STRING_OPERATOR_ENDS_WITH:
                return value.endsWith(stringFilter.getValue());
            case STRING_OPERATOR_EQUALS:
                return value.equals(stringFilter.getValue());
            case STRING_OPERATOR_STARTS_WITH:
                return value.startsWith(stringFilter.getValue());
        }
        return false;
    }

    private boolean check(boolean value, Filter.BoolFilter boolFilter) {
        if (boolFilter.getOperator() == Filter.BoolFilter.BoolOperator.BOOL_OPERATOR_EQUALS) {
            return value == boolFilter.getValue();
        }
        return false;
    }

    private boolean check(Timestamp value, Filter.TimestampFilter timestampFilter) {
        switch (timestampFilter.getOperator()) {
            case TIMESTAMP_OPERATOR_AFTER:
                return value.getNanos() > timestampFilter.getValue().getNanos();
            case TIMESTAMP_OPERATOR_BEFORE:
                return value.getNanos() < timestampFilter.getValue().getNanos();
            case TIMESTAMP_OPERATOR_EQUALS:
                return value.getNanos() == timestampFilter.getValue().getNanos();
        }
        return false;
    }
}
