package nl.medtechchain.chaincode.encryption;

import com.owlike.genson.Genson;
import nl.medtechchain.chaincode.encryption.provider.HEInterfaceProvider;
import nl.medtechchain.chaincode.encryption.scheme.HEType;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.Arrays;

@Contract(name = "encryption-management", info = @Info(
        title = "Encryption Scheme Management",
        license = @License(
                name = "Apache 2.0 License",
                url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
public class ManagementContract {

    private static final String HOM_ENC_TYPE = "HOM_ENC_TYPE";

    private final Genson genson = new Genson();

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Chaincode.Response InitLedger(final Context ctx) {
        if (!checkAccess(ctx))
            return new Chaincode.Response(Chaincode.Response.Status.forCode(403), "FORBIDDEN", new byte[0]);

        SetEncryptionType(ctx, HEType.PAILLIER_2048);
        return new Chaincode.Response(Chaincode.Response.Status.SUCCESS, "SUCCESS", new byte[0]);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Chaincode.Response SetEncryptionType(final Context ctx, final HEType type) {
        if (!checkAccess(ctx))
            return new Chaincode.Response(Chaincode.Response.Status.forCode(403), "FORBIDDEN", new byte[0]);

        ChaincodeStub stub = ctx.getStub();
        stub.putStringState(HOM_ENC_TYPE, type.name());
        HEInterfaceProvider.instance().setHEType(type);
        return new Chaincode.Response(Chaincode.Response.Status.SUCCESS, "SUCCESS", new byte[0]);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Chaincode.Response GetEncryptionType(final Context ctx) {
        if (!checkAccess(ctx))
            return new Chaincode.Response(Chaincode.Response.Status.forCode(403), "FORBIDDEN", new byte[0]);

        ChaincodeStub stub = ctx.getStub();
        return new Chaincode.Response(Chaincode.Response.Status.SUCCESS, "ENCRYPTION_TYPE", genson.serialize(getEncryptionTypes(ctx).name()).getBytes());
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Chaincode.Response GetEncryptionTypes(final Context ctx) {
        if (!checkAccess(ctx))
            return new Chaincode.Response(Chaincode.Response.Status.forCode(403), "FORBIDDEN", new byte[0]);

        return new Chaincode.Response(Chaincode.Response.Status.SUCCESS, "ENCRYPTION_TYPE_LIST", genson.serialize(Arrays.asList(HEType.values())).getBytes());
    }

    HEType getEncryptionTypes(final Context ctx) {
        return HEType.valueOf(ctx.getStub().getStringState(HOM_ENC_TYPE));
    }


    // TODO: Perform Access control checks
    private boolean checkAccess(final Context ctx) {
        return true;
    }
}
