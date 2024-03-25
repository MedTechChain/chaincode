package nl.medtechchain.chaincode.device;

import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.Chaincode;

@Contract(name = "device", info = @Info(
        title = "Device Metadata",
        license = @License(
                name = "Apache 2.0 License",
                url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
@Default
public final class DeviceMetadataContract implements ContractInterface {

    private final Genson genson = new Genson();

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Chaincode.Response CreateDeviceMetadataAsset(final Context ctx, DeviceType type, byte[] data) {
        // TODO

        // Just generate a random UUID as key cause we never care to access assets by key individually

        return null;
    }


    // TODO: Implement the contract(s) for queries

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Chaincode.Response DummyQuery(final Context ctx, final String lowBoundFirmwareVersion) {
        return null;
    }
}