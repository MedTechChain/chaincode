package nl.medtechchain.chaincode.encryption;

import com.owlike.genson.Genson;
import nl.medtechchain.chaincode.encryption.homomorphic.HomomorphicEncryptFactory;
import nl.medtechchain.chaincode.encryption.homomorphic.impl.phe.paillier.PaillierBitLength;
import nl.medtechchain.chaincode.encryption.homomorphic.impl.phe.paillier.PaillierFacotry;
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
public class EncryptionManagementContract {

    private static final String HOM_ENC_TYPE = "HOM_ENC_TYPE";

    @SuppressWarnings({"rawtypes"})
    private static HomomorphicEncryptFactory factory;

    private final Genson genson = new Genson();

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Chaincode.Response InitLedger(final Context ctx) {
        if (!checkAccess(ctx))
            return new Chaincode.Response(Chaincode.Response.Status.forCode(403), "FORBIDDEN", new byte[0]);

        SetEncryptionType(ctx, HomomorphicEncryptionType.PAILLIER_2048);
        setFactory(HomomorphicEncryptionType.PAILLIER_2048);
        return new Chaincode.Response(Chaincode.Response.Status.SUCCESS, "SUCCESS", new byte[0]);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Chaincode.Response SetEncryptionType(final Context ctx, final HomomorphicEncryptionType type) {
        if (!checkAccess(ctx))
            return new Chaincode.Response(Chaincode.Response.Status.forCode(403), "FORBIDDEN", new byte[0]);

        ChaincodeStub stub = ctx.getStub();
        stub.putStringState(HOM_ENC_TYPE, type.name());
        setFactory(type);
        return new Chaincode.Response(Chaincode.Response.Status.SUCCESS, "SUCCESS", new byte[0]);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Chaincode.Response GetEncryptionType(final Context ctx) {
        if (!checkAccess(ctx))
            return new Chaincode.Response(Chaincode.Response.Status.forCode(403), "FORBIDDEN", new byte[0]);

        ChaincodeStub stub = ctx.getStub();
        return new Chaincode.Response(Chaincode.Response.Status.SUCCESS, "ENCRYPTION_TYPE", genson.serialize(HomomorphicEncryptionType.valueOf(stub.getStringState(HOM_ENC_TYPE)).name()).getBytes());
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Chaincode.Response GetEncryptionTypes(final Context ctx) {
        if (!checkAccess(ctx))
            return new Chaincode.Response(Chaincode.Response.Status.forCode(403), "FORBIDDEN", new byte[0]);

        return new Chaincode.Response(Chaincode.Response.Status.SUCCESS, "ENCRYPTION_TYPE_LIST", genson.serialize(Arrays.asList(HomomorphicEncryptionType.values())).getBytes());
    }

    @SuppressWarnings({"rawtypes"})
    public static HomomorphicEncryptFactory getFactory() {
        return factory;
    }

    private void setFactory(final HomomorphicEncryptionType type) {
        switch (type) {
            case PAILLIER_2048:
                factory = new PaillierFacotry(PaillierBitLength.BL_2048);
                break;
            case PAILLIER_4096:
                factory = new PaillierFacotry(PaillierBitLength.BL_4096);
                break;
        }
    }

    // TODO: Perform Access control checks
    private boolean checkAccess(final Context ctx) {
        return true;
    }
}
