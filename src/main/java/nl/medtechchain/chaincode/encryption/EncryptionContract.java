package nl.medtechchain.chaincode.encryption;

import nl.medtechchain.chaincode.encryption.homomorphic.HomomorphicEncryptFactory;
import nl.medtechchain.chaincode.encryption.homomorphic.impl.phe.paillier.PaillierBitLength;
import nl.medtechchain.chaincode.encryption.homomorphic.impl.phe.paillier.PaillierFacotry;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.Arrays;
import java.util.List;

@Contract(name = "encryption", info = @Info(
        title = "Privacy-Preserving Encryption",
        license = @License(
                name = "Apache 2.0 License",
                url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
@SuppressWarnings({"rawtypes", "unchecked"})
public class EncryptionContract {

    private static final String HOM_ENC_TYPE = "HOM_ENC_TYPE";

    private HomomorphicEncryptFactory factory;

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public boolean InitLedger(final Context ctx) {
        if (!checkAccess(ctx))
            return false;

        SetEncryptionType(ctx, HomomorphicEncryptionType.PAILLIER_2048);
        return true;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void SetEncryptionType(final Context ctx, HomomorphicEncryptionType type) {
        if (checkAccess(ctx)) {
            ChaincodeStub stub = ctx.getStub();
            stub.putStringState(HOM_ENC_TYPE, type.name());
            setFactory(type);
        }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public HomomorphicEncryptionType GetEncryptionType(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        return HomomorphicEncryptionType.valueOf(stub.getStringState(HOM_ENC_TYPE));
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public List<HomomorphicEncryptionType> GetEncryptionTypes(final Context ctx) {
        return Arrays.asList(HomomorphicEncryptionType.values());
    }

    // TODO
    private boolean checkAccess(final Context ctx) {
        return true;
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
}
