package nl.medtechchain.chaincode.service.query.groupedcount;

import com.google.protobuf.Descriptors;
import nl.medtechchain.chaincode.service.encryption.PlatformEncryptionInterface;
import nl.medtechchain.proto.devicedata.DeviceDataAsset;
import nl.medtechchain.proto.devicedata.DeviceDataFieldType;

import java.util.List;
import java.util.Map;

public interface GroupedCount {

    Map<String, Long> groupedCount(PlatformEncryptionInterface encryptionInterface, Descriptors.FieldDescriptor descriptor, List<DeviceDataAsset> assets);

    class Factory {
        public static GroupedCount getInstance(DeviceDataFieldType fieldType) {
            switch (fieldType) {
                case STRING:
                    return new StringGroupedCount();
                case INTEGER:
                    return new IntegerGroupedCount();
                case TIMESTAMP:
                    return new TimestampGroupedCount();
                case BOOL:
                    return new BoolGroupedCount();
                case DEVICE_CATEGORY:
                    return new DeviceCateogryGroupedCount();
                case MEDICAL_SPECIALITY:
                    return new MedicalSpecialityGroupedCount();
                default:
                    return (encryptionInterface, descriptor, assets) -> Map.of();
            }
        }
    }
}
