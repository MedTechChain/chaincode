package nl.medtechchain.chaincode.contract;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import nl.medtechchain.chaincode.contract.util.ResponseUtil;
import nl.medtechchain.protos.encryption.EncryptionSchemeType;
import nl.medtechchain.protos.encryption.EncryptionSchemeTypeList;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Contract(name = "encryption", info = @Info(
        title = "Encryption Scheme Contract",
        license = @License(
                name = "Apache 2.0 License",
                url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
public class EncryptionSchemeTypeContract {
    private static final String HOM_ENC_TYPE = "HOM_ENC_TYPE";

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public boolean InitLedger(final Context ctx) {
        return SetEncryptionSchemeType(ctx, EncryptionSchemeType.PAILLIER_2048);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public boolean SetEncryptionSchemeType(final Context ctx, final EncryptionSchemeType scheme) {
        if (!checkAccess(ctx))
            return false;

        ChaincodeStub stub = ctx.getStub();
        stub.putStringState(HOM_ENC_TYPE, scheme.name());
        return true;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetEncryptionSchemeTypes(final Context ctx) {
        if (!checkAccess(ctx))
            return ResponseUtil.error("Unauthorized to perform this operation!");

        ChaincodeStub stub = ctx.getStub();
        EncryptionSchemeType current = EncryptionSchemeType.valueOf(stub.getStringState(HOM_ENC_TYPE));

        if (current == EncryptionSchemeType.UNRECOGNIZED)
            return ResponseUtil.error("Unrecognized encryption scheme");

        List<EncryptionSchemeType> encryptionSchemes = Stream.of(EncryptionSchemeType.values())
                .filter(s -> s != EncryptionSchemeType.UNRECOGNIZED)
                .collect(Collectors.toList());

        EncryptionSchemeTypeList encryptionSchemeList = EncryptionSchemeTypeList.newBuilder()
                .setCurrent(current)
                .addAllEncryptionSchemeTypeList(encryptionSchemes)
                .build();

        try {
            return JsonFormat.printer().print(encryptionSchemeList);
        } catch (InvalidProtocolBufferException e) {
            return ResponseUtil.error("Could not serialize data");
        }
    }

    // TODO: Perform Access control checks
    private boolean checkAccess(final Context ctx) {
        return true;
    }

}
