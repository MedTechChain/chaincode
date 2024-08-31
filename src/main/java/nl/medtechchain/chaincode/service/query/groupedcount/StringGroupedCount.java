package nl.medtechchain.chaincode.service.query.groupedcount;

import com.google.protobuf.Descriptors;
import nl.medtechchain.chaincode.service.encryption.PlatformEncryptionInterface;
import nl.medtechchain.proto.devicedata.DeviceDataAsset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringGroupedCount implements GroupedCount {

    StringGroupedCount() {
    }

    @Override
    public Map<String, Long> groupedCount(PlatformEncryptionInterface encryptionInterface, Descriptors.FieldDescriptor descriptor, List<DeviceDataAsset> assets) {
        var result = new HashMap<String, Long>();

        for (DeviceDataAsset asset : assets) {
            var fieldValue = (DeviceDataAsset.StringField) asset.getDeviceData().getField(descriptor);
            String key = null;
            switch (fieldValue.getFieldCase()) {
                case PLAIN:
                    key = fieldValue.getPlain();
                    break;
                case ENCRYPTED:
                    if (encryptionInterface == null)
                        throw new IllegalStateException("Field " + descriptor.getName() + " is encrypted, but the platform is not properly configured to use encryption.");
                    key = encryptionInterface.decryptString(fieldValue.getEncrypted());
                    break;
            }
            if (key != null)
                result.put(key, result.getOrDefault(key, 0L) + 1);
        }
        return result;
    }
}
