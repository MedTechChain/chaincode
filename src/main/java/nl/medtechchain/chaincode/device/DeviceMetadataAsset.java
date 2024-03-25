package nl.medtechchain.chaincode.device;

import com.google.protobuf.CodedOutputStream;
import nl.medtechchain.chaincode.encryption.HomomorphicEncryptionType;
import nl.medtechchain.protos.devicemetadata.BedsideMonitorEncrypted;
import nl.medtechchain.protos.devicemetadata.WearableDeviceEncrypted;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@DataType(namespace = "device")
public class DeviceMetadataAsset {
    @Property()
    private final HomomorphicEncryptionType encryptionType;

    @Property()
    private final DeviceType deviceType;

    @Property()
    private final byte[] data;

    public DeviceMetadataAsset(final HomomorphicEncryptionType encryptionType, final WearableDeviceEncrypted data) throws IOException {
        this.data = new byte[0];
        data.writeTo(CodedOutputStream.newInstance(this.data));
        this.encryptionType = encryptionType;
        this.deviceType = DeviceType.WEARABLE;
    }

    public DeviceMetadataAsset(final HomomorphicEncryptionType encryptionType, final BedsideMonitorEncrypted data) throws IOException {
        this.data = new byte[0];
        data.writeTo(CodedOutputStream.newInstance(this.data));
        this.encryptionType = encryptionType;
        this.deviceType = DeviceType.BEDSIDE_MONITOR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceMetadataAsset that = (DeviceMetadataAsset) o;
        return encryptionType == that.encryptionType && deviceType == that.deviceType && Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(encryptionType, deviceType);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}
