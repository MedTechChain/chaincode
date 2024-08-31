package nl.medtechchain.chaincode.service.query.average;

import com.google.protobuf.Descriptors;
import nl.medtechchain.chaincode.service.encryption.PlatformEncryptionInterface;
import nl.medtechchain.proto.devicedata.DeviceDataAsset;
import nl.medtechchain.proto.devicedata.DeviceDataFieldType;

import java.util.List;

public interface Average {

    Pair<Long, Long> average(PlatformEncryptionInterface encryptionInterface, Descriptors.FieldDescriptor descriptor, List<DeviceDataAsset> assets);

    class Factory {
        public static Average getInstance(DeviceDataFieldType fieldType) {
            switch (fieldType) {
                case INTEGER:
                    return new IntegerAverage();
                case TIMESTAMP:
                    return new TimestampAverage();
                default:
                    return (encryptionInterface, descriptor, assets) -> new Pair<>(0L, 1L);
            }
        }
    }
}
