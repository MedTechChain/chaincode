package nl.medtechchain.chaincode.contract;

import com.google.protobuf.InvalidProtocolBufferException;
import nl.medtechchain.chaincode.contract.util.ResponseUtil;
import nl.medtechchain.protos.query.Hospital;
import nl.medtechchain.protos.query.Query;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;
import org.hyperledger.fabric.shim.ledger.CompositeKey;

import java.time.Instant;

@Contract(name = "devicemetadata", info = @Info(
        title = "Device Metadata Contract",
        license = @License(
                name = "Apache 2.0 License",
                url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
public final class DeviceMetadataContract implements ContractInterface {

    private static final String INDEX_NAME = "UDI_HOSPITAL_TIMESTAMP";

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Chaincode.Response CreateDeviceMetadataAsset(Context ctx, String udi, byte[] bytes) {
        Hospital hospital = getHospitalFromCtx(ctx);
        if (hospital == Hospital.UNRECOGNIZED)
            return ResponseUtil.error("Hospital not recognized");

        CompositeKey key = compositeKey(ctx, udi, hospital, Instant.now());
        ctx.getStub().putState(key.toString(), bytes);
        return ResponseUtils.newSuccessResponse();
    }


    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Chaincode.Response Query(Context ctx, byte[] bytes) {
        ChaincodeStub stub = ctx.getStub();
        try {
            Query query = Query.parseFrom(bytes);

            // TODO: Implement Queries
            return ResponseUtils.newSuccessResponse();
        } catch (InvalidProtocolBufferException e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    private Hospital getHospitalFromCtx(Context ctx) {
        String mspID = ctx.getClientIdentity().getMSPID().toUpperCase();
        for (Hospital hospital : Hospital.values()) {
            if (mspID.contains(hospital.name()))
                return hospital;
        }
        return Hospital.UNRECOGNIZED;
    }

    private CompositeKey compositeKey(Context ctx, String udi, Hospital hospital, Instant timestamp) {
        return ctx.getStub().createCompositeKey(INDEX_NAME, udi, hospital.name(), timestamp.toString());
    }
}