package nl.medtechchain.chaincode.service.query.groupedcount;

import com.google.protobuf.Descriptors;
import nl.medtechchain.chaincode.service.encryption.HomomorphicEncryptionScheme;
import nl.medtechchain.chaincode.service.encryption.PlatformEncryptionInterface;
import nl.medtechchain.chaincode.service.solver.ILPSolver;
import nl.medtechchain.proto.devicedata.DeviceCategory;
import nl.medtechchain.proto.devicedata.DeviceDataAsset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeviceCateogryGroupedCount implements GroupedCount {

    DeviceCateogryGroupedCount() {
    }

    @Override
    public Map<String, Integer> groupedCount(PlatformEncryptionInterface encryptionInterface, Descriptors.FieldDescriptor descriptor, List<DeviceDataAsset> assets) {
        var result = new HashMap<String, Integer>();

        String key;

        String homomorphicSum = null;
        int homomorphicOperations = 0;

        for (DeviceDataAsset asset : assets) {
            var fieldValue = (DeviceDataAsset.DeviceCategoryField) asset.getDeviceData().getField(descriptor);
            switch (fieldValue.getFieldCase()) {
                case PLAIN:
                    key = fieldValue.getPlain().name();
                    result.put(key, result.getOrDefault(key, 0) + 1);
                    break;
                case ENCRYPTED:
                    if (encryptionInterface == null)
                        throw new IllegalStateException("Field " + descriptor.getName() + " is encrypted, but the platform is not properly configured to use encryption.");

                    if (encryptionInterface.isHomomorphic()) {
                        homomorphicOperations++;
                        if (homomorphicSum == null)
                            homomorphicSum = fieldValue.getEncrypted();
                        else
                            homomorphicSum = ((HomomorphicEncryptionScheme) encryptionInterface).add(homomorphicSum, fieldValue.getEncrypted());


                    } else {
                        key = DeviceCategory.forNumber((int) encryptionInterface.decryptLong(fieldValue.getEncrypted())).name();
                        result.put(key, result.getOrDefault(key, 0) + 1);
                    }

                    break;
            }

        }

        Map<String, Integer> partialHomomorphicResult = new HashMap<>();

        if (homomorphicSum != null) {
            var decryptedSum = encryptionInterface.decryptLong(homomorphicSum);
            var values = new ArrayList<>(List.of(DeviceCategory.values()));
            values.remove(DeviceCategory.UNRECOGNIZED);
            values.remove(DeviceCategory.DEVICE_CATEGORY_UNSPECIFIED);

            partialHomomorphicResult = new ILPSolver().solveSystem(values.stream().map(Enum::name).collect(Collectors.toList()), values.stream().map(DeviceCategory::getNumber).collect(Collectors.toList()), decryptedSum, homomorphicOperations).orElse(new HashMap<>());
        }

        for (Map.Entry<String, Integer> entry : partialHomomorphicResult.entrySet())
            result.put(entry.getKey(), result.getOrDefault(entry.getKey(), 0) + entry.getValue());

        return result;
    }
}
