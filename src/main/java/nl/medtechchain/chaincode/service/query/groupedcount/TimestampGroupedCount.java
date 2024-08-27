package nl.medtechchain.chaincode.service.query.groupedcount;

import com.google.protobuf.Descriptors;
import nl.medtechchain.chaincode.service.encryption.PlatformEncryptionInterface;
import nl.medtechchain.proto.devicedata.DeviceDataAsset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimestampGroupedCount implements GroupedCount {

    TimestampGroupedCount() {
    }

    @Override
    public Map<String, Integer> groupedCount(PlatformEncryptionInterface encryptionInterface, Descriptors.FieldDescriptor descriptor, List<DeviceDataAsset> assets) {
        var result = new HashMap<String, Integer>();

        for (DeviceDataAsset asset : assets) {
            var fieldValue = (DeviceDataAsset.TimestampField) asset.getDeviceData().getField(descriptor);
            String key = null;
            switch (fieldValue.getFieldCase()) {
                case PLAIN:
                    key = fieldValue.getPlain().getSeconds() + "";
                    break;
                case ENCRYPTED:
                    if (encryptionInterface == null)
                        throw new IllegalStateException("Field " + descriptor.getName() + " is encrypted, but the platform is not properly configured to use encryption.");
                    key = encryptionInterface.decryptLong(fieldValue.getEncrypted()) + "";
                    break;
            }
            if (key != null)
                result.put(key, result.getOrDefault(key, 0) + 1);
        }
        return result;
    }
}
