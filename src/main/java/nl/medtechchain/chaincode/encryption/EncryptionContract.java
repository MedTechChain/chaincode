package nl.medtechchain.chaincode.encryption;

import com.google.protobuf.CodedOutputStream;
import nl.medtechchain.chaincode.device.DeviceType;
import nl.medtechchain.chaincode.encryption.provider.HEInterfaceProvider;
import nl.medtechchain.chaincode.encryption.scheme.HEInterface;
import nl.medtechchain.protos.devicemetadata.BedsideMonitor;
import nl.medtechchain.protos.devicemetadata.BedsideMonitorEncrypted;
import nl.medtechchain.protos.devicemetadata.WearableDevice;
import nl.medtechchain.protos.devicemetadata.WearableDeviceEncrypted;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.Chaincode;

import java.io.IOException;

@Contract(name = "encryption", info = @Info(
        title = "Privacy-Preserving Encryption",
        license = @License(
                name = "Apache 2.0 License",
                url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
public class EncryptionContract {

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Chaincode.Response Encrypt(final DeviceType type, final byte[] data) {
        switch (type) {
            case WEARABLE:
                try {
                    WearableDevice wd = WearableDevice.parseFrom(data);
                    byte[] result = new byte[0];
                    encrypt(wd).writeTo(CodedOutputStream.newInstance(result));
                    new Chaincode.Response(Chaincode.Response.Status.SUCCESS, "ENCRYPTED_DATA", result);
                } catch (Exception e) {
                    return new Chaincode.Response(Chaincode.Response.Status.INTERNAL_SERVER_ERROR, e.getMessage(), new byte[0]);
                }
            case BEDSIDE_MONITOR:
                try {
                    BedsideMonitor bm = BedsideMonitor.parseFrom(data);
                    byte[] result = new byte[0];
                    encrypt(bm).writeTo(CodedOutputStream.newInstance(result));
                    new Chaincode.Response(Chaincode.Response.Status.SUCCESS, "ENCRYPTED_DATA", result);
                } catch (Exception e) {
                    return new Chaincode.Response(Chaincode.Response.Status.INTERNAL_SERVER_ERROR, e.getMessage(), new byte[0]);
                }
        }

        return new Chaincode.Response(Chaincode.Response.Status.INTERNAL_SERVER_ERROR, "UNKNOWN_DEVICE_TYPE", new byte[0]);
    }

    private WearableDeviceEncrypted encrypt(WearableDevice wd) throws IOException {
        HEInterface he = HEInterfaceProvider.instance().provide();
        // TODO: Properly encrypt

        // From Real Types (e.g., boolean, string, integers) -> map to mathematical object string representation for ciphertext and plaintext (for Paillier, all types need to be mapped to BigInt) -> use interface to parse into Plaintext / Ciphertext objects

        return WearableDeviceEncrypted.newBuilder().build();
    }

    private BedsideMonitorEncrypted encrypt(BedsideMonitor bm) throws IOException {
        HEInterface he = HEInterfaceProvider.instance().provide();
        // TODO: Properly encrypt

        // From Real Types (e.g., boolean, string, integers) -> map to mathematical object string representation for ciphertext and plaintext (for Paillier, all types need to be mapped to int / BigInt) -> use interface to parse into Plaintext / Ciphertext objects

        return BedsideMonitorEncrypted.newBuilder().build();
    }

}
