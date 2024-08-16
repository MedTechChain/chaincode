package nl.medtechchain.chaincode.contract.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import nl.medtechchain.chaincode.contract.PlatformConfigContract;
import nl.medtechchain.proto.devicedata.DeviceDataTransaction;
import nl.medtechchain.proto.query.Filter;

import static nl.medtechchain.chaincode.contract.PlatformConfigContract.encryptionEnabled;

public class FilterChecker {
    public static boolean checkFilter(DeviceDataTransaction asset, Filter filter) {
        switch (filter.getFilterTypeCase()) {
            case INT_FILTER:
                return checkInt(asset, filter.getIntFilter());
            case STRING_FILTER:
                return checkString(asset, filter.getStringFilter());
            case BOOL_FILTER:
                return checkBool(asset, filter.getBoolFilter());
            case TIMESTAMP_FILTER:
                return checkTimestamp(asset, filter.getTimestampFilter());

        }
        return false;
    }

    public static boolean checkEncryption(DeviceDataTransaction asset) {
        String currentEncryptionVersion = PlatformConfigContract.getEncryptionVersion();
        boolean encryptionEnabled = encryptionEnabled();
        return !(!asset.hasEncryptionVersion() && encryptionEnabled ||
                asset.hasEncryptionVersion() && !encryptionEnabled ||
                asset.hasEncryptionVersion() && !asset.getEncryptionVersion().equals(currentEncryptionVersion));
    }

    private static boolean checkInt(DeviceDataTransaction asset, Filter.IntFilter intFilter) {
        String value = fieldValue(asset, intFilter.getField());

        if (encryptionEnabled()) {
            // TODO
            return false;
        } else {
            int intValue;
            try {
                intValue = Integer.parseInt(value);
            } catch (Throwable ignored) {
                return false;
            }
            switch (intFilter.getOperator()) {
                case INT_OPERATOR_GREATER_THAN_OR_EQUAL:
                    return intValue >= intFilter.getValue();
                case INT_OPERATOR_EQUALS:
                    return intValue == intFilter.getValue();
                case INT_OPERATOR_LESS_THAN:
                    return intValue < intFilter.getValue();
                case INT_OPERATOR_GREATER_THAN:
                    return intValue > intFilter.getValue();
                case INT_OPERATOR_LESS_THAN_OR_EQUAL:
                    return intValue <= intFilter.getValue();
            }
            return false;
        }
    }

    private static boolean checkString(DeviceDataTransaction asset, Filter.StringFilter stringFilter) {
        String value = fieldValue(asset, stringFilter.getField());

        if (encryptionEnabled()) {
            // TODO
            return false;
        } else {
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
    }

    private static boolean checkBool(DeviceDataTransaction asset, Filter.BoolFilter boolFilter) {
        String value = fieldValue(asset, boolFilter.getField());

        if (encryptionEnabled()) {
            // TODO
            return false;
        } else {
            boolean boolValue;
            try {
                boolValue = Boolean.parseBoolean(value);
            } catch (Throwable ignored) {
                return false;
            }
            if (boolFilter.getOperator() == Filter.BoolFilter.BoolOperator.BOOL_OPERATOR_EQUALS) {
                return boolValue == boolFilter.getValue();
            }
            return false;
        }
    }

    private static boolean checkTimestamp(DeviceDataTransaction asset, Filter.TimestampFilter timestampFilter) {
        String value = fieldValue(asset, timestampFilter.getField());

        if (encryptionEnabled()) {
            // TODO
            return false;
        } else {
            Timestamp timestampValue;
            try {
                timestampValue = Timestamp.parseFrom(ByteString.copyFromUtf8(value));
            } catch (Throwable ignored) {
                return false;
            }
            switch (timestampFilter.getOperator()) {
                case TIMESTAMP_OPERATOR_AFTER:
                    return timestampValue.getNanos() > timestampFilter.getValue().getNanos();
                case TIMESTAMP_OPERATOR_BEFORE:
                    return timestampValue.getNanos() < timestampFilter.getValue().getNanos();
                case TIMESTAMP_OPERATOR_EQUALS:
                    return timestampValue.getNanos() == timestampFilter.getValue().getNanos();
            }
            return false;
        }
    }

    private static String fieldValue(DeviceDataTransaction asset, String fieldName) {
        return (String) asset.getField(DeviceDataTransaction.getDescriptor().findFieldByName(fieldName));
    }
}
