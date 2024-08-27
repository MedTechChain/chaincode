package nl.medtechchain.chaincode.service.query.average;

import com.google.protobuf.Descriptors;
import nl.medtechchain.chaincode.service.encryption.HomomorphicEncryptionScheme;
import nl.medtechchain.chaincode.service.encryption.PlatformEncryptionInterface;
import nl.medtechchain.proto.devicedata.DeviceDataAsset;

import java.util.List;

public class IntegerAverage implements Average {
    IntegerAverage() {
    }

    @Override
    public Pair<Long, Long> average(PlatformEncryptionInterface encryptionInterface, Descriptors.FieldDescriptor descriptor, List<DeviceDataAsset> assets) {
        long sum = 0;

        String homomorphicSum = null;

        long count = 0;

        for (DeviceDataAsset asset : assets) {
            var fieldValue = (DeviceDataAsset.IntegerField) asset.getDeviceData().getField(descriptor);
            switch (fieldValue.getFieldCase()) {
                case PLAIN:
                    sum += fieldValue.getPlain();
                    count++;
                    break;
                case ENCRYPTED:
                    if (encryptionInterface == null)
                        throw new IllegalStateException("Field " + descriptor.getName() + " is encrypted, but the platform is not properly configured to use encryption.");

                    if (encryptionInterface.isHomomorphic()) {
                        if (homomorphicSum == null)
                            homomorphicSum = fieldValue.getEncrypted();
                        else
                            homomorphicSum = ((HomomorphicEncryptionScheme) encryptionInterface).add(homomorphicSum, fieldValue.getEncrypted());
                    } else
                        sum += encryptionInterface.decryptLong(fieldValue.getEncrypted());

                    count++;

                    break;
            }
        }

        if (homomorphicSum != null)
            sum += encryptionInterface.decryptLong(homomorphicSum);

        return new Pair<>(sum, count);
    }
}
