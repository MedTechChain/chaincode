package nl.medtechchain.chaincode.service.query;

import com.google.protobuf.Timestamp;
import nl.medtechchain.chaincode.service.encryption.PlatformEncryptionInterface;
import nl.medtechchain.proto.devicedata.DeviceCategory;
import nl.medtechchain.proto.devicedata.DeviceDataAsset;
import nl.medtechchain.proto.devicedata.MedicalSpeciality;
import nl.medtechchain.proto.query.Filter;

import java.util.Optional;
import java.util.logging.Logger;


public class FilterService {

    private static final Logger logger = Logger.getLogger(FilterService.class.getName());

    private final PlatformEncryptionInterface encryptionInterface;

    public FilterService() {
        this.encryptionInterface = null;
    }

    public FilterService(PlatformEncryptionInterface encryptionInterface) {
        this.encryptionInterface = encryptionInterface;
    }

    public boolean checkFilter(DeviceDataAsset asset, Filter filter) {
        try {
            var descriptor = Optional.ofNullable(DeviceDataAsset.DeviceData.getDescriptor().findFieldByName(filter.getField()));
            if (descriptor.isEmpty())
                throw new IllegalStateException("Field " + filter.getField() + " is not present in asset " + asset);

            var value = asset.getDeviceData().getField(descriptor.get());

            switch (DeviceDataFieldType.fromFieldName(filter.getField())) {
                case STRING:
                    assert filter.getComparatorCase() == Filter.ComparatorCase.STRING_FILTER;
                    return check(filter.getField(), (DeviceDataAsset.StringField) value, filter.getStringFilter());
                case INTEGER:
                    assert filter.getComparatorCase() == Filter.ComparatorCase.INTEGER_FILTER;
                    return check(filter.getField(), (DeviceDataAsset.IntegerField) value, filter.getIntegerFilter());
                case TIMESTAMP:
                    assert filter.getComparatorCase() == Filter.ComparatorCase.TIMESTAMP_FILTER;
                    return check(filter.getField(), (DeviceDataAsset.TimestampField) value, filter.getTimestampFilter());
                case BOOL:
                    assert filter.getComparatorCase() == Filter.ComparatorCase.BOOL_FILTER;
                    return check(filter.getField(), (DeviceDataAsset.BoolField) value, filter.getBoolFilter());
                case DEVICE_CATEGORY:
                    assert filter.getComparatorCase() == Filter.ComparatorCase.ENUM_FILTER;
                    return check(filter.getField(), (DeviceDataAsset.DeviceCategoryField) value, filter.getEnumFilter());
                case MEDICAL_SPECIALITY:
                    assert filter.getComparatorCase() == Filter.ComparatorCase.ENUM_FILTER;
                    return check(filter.getField(), (DeviceDataAsset.MedicalSpecialityField) value, filter.getEnumFilter());
            }

            return false;
        } catch (Throwable t) {
            logger.warning("Error checking filter: " + filter + ". " + t);
            return false;
        }
    }

    private boolean check(String name, DeviceDataAsset.StringField field, Filter.StringFilter filter) {
        String value;
        switch (field.getFieldCase()) {
            case PLAIN:
                value = field.getPlain();
                break;
            case ENCRYPTED:
                if (encryptionInterface == null)
                    throw new IllegalStateException("Field " + name + " is encrypted, but the platform is not properly configured to use encryption.");

                value = encryptionInterface.decryptString(field.getEncrypted());
                break;
            default:
                return false;
        }

        switch (filter.getOperator()) {
            case CONTAINS:
                return value.contains(filter.getValue());
            case ENDS_WITH:
                return value.endsWith(filter.getValue());
            case EQUALS:
                return value.equals(filter.getValue());
            case STARTS_WITH:
                return value.startsWith(filter.getValue());
        }

        return false;
    }

    private boolean check(String name, DeviceDataAsset.IntegerField field, Filter.IntegerFilter filter) {
        long value;
        switch (field.getFieldCase()) {
            case PLAIN:
                value = field.getPlain();
                break;
            case ENCRYPTED:
                if (encryptionInterface == null)
                    throw new IllegalStateException("Field " + name + " is encrypted, but the platform is not properly configured to use encryption.");

                value = encryptionInterface.decryptLong(field.getEncrypted());
                break;
            default:
                return false;
        }

        switch (filter.getOperator()) {
            case GREATER_THAN_OR_EQUAL:
                return value >= filter.getValue();
            case EQUALS:
                return value == filter.getValue();
            case LESS_THAN:
                return value < filter.getValue();
            case GREATER_THAN:
                return value > filter.getValue();
            case LESS_THAN_OR_EQUAL:
                return value <= filter.getValue();
        }

        return false;
    }

    private boolean check(String name, DeviceDataAsset.TimestampField field, Filter.TimestampFilter filter) {
        Timestamp value;
        switch (field.getFieldCase()) {
            case PLAIN:
                value = field.getPlain();
                break;
            case ENCRYPTED:
                if (encryptionInterface == null)
                    throw new IllegalStateException("Field " + name + " is encrypted, but the platform is not properly configured to use encryption.");

                value = Timestamp.newBuilder().setSeconds(encryptionInterface.decryptLong(field.getEncrypted())).build();
                break;
            default:
                return false;
        }

        switch (filter.getOperator()) {
            case AFTER:
                return value.getSeconds() > filter.getValue().getSeconds();
            case BEFORE:
                return value.getSeconds() < filter.getValue().getSeconds();
            case EQUALS:
                return value.getSeconds() == filter.getValue().getSeconds();
        }

        return false;
    }

    private boolean check(String name, DeviceDataAsset.BoolField field, Filter.BoolFilter filter) {
        boolean value;
        switch (field.getFieldCase()) {
            case PLAIN:
                value = field.getPlain();
                break;
            case ENCRYPTED:
                if (encryptionInterface == null)
                    throw new IllegalStateException("Field " + name + " is encrypted, but the platform is not properly configured to use encryption.");

                value = encryptionInterface.decryptBool(field.getEncrypted());
                break;
            default:
                return false;
        }

        if (filter.getOperator() == Filter.BoolFilter.BoolOperator.EQUALS) {
            return value == filter.getValue();
        }

        return false;
    }

    private boolean check(String name, DeviceDataAsset.MedicalSpecialityField field, Filter.EnumFilter filter) {
        MedicalSpeciality value;
        switch (field.getFieldCase()) {
            case PLAIN:
                value = field.getPlain();
                break;
            case ENCRYPTED:
                if (encryptionInterface == null)
                    throw new IllegalStateException("Field " + name + " is encrypted, but the platform is not properly configured to use encryption.");
                value = MedicalSpeciality.forNumber((int) encryptionInterface.decryptLong(field.getEncrypted()));
                break;
            default:
                return false;
        }

        return value == MedicalSpeciality.valueOf(filter.getValue());
    }

    private boolean check(String name, DeviceDataAsset.DeviceCategoryField field, Filter.EnumFilter filter) {
        DeviceCategory value;
        switch (field.getFieldCase()) {
            case PLAIN:
                value = field.getPlain();
                break;
            case ENCRYPTED:
                if (encryptionInterface == null)
                    throw new IllegalStateException("Field " + name + " is encrypted, but the platform is not properly configured to use encryption.");
                value = DeviceCategory.forNumber((int) encryptionInterface.decryptLong(field.getEncrypted()));
                break;
            default:
                return false;
        }

        return value == DeviceCategory.valueOf(filter.getValue());
    }

}
