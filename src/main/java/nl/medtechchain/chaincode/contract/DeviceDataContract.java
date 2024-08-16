package nl.medtechchain.chaincode.contract;

import com.google.privacy.differentialprivacy.LaplaceNoise;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import nl.medtechchain.chaincode.contract.util.FilterChecker;
import nl.medtechchain.chaincode.contract.util.TXType;
import nl.medtechchain.proto.common.ChaincodeError.ErrorCode;
import nl.medtechchain.proto.devicedata.DeviceDataTransaction;
import nl.medtechchain.proto.query.QueryResult;
import nl.medtechchain.proto.query.QueryTransaction;
import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static nl.medtechchain.chaincode.contract.util.ConfigUtil.differentialPrivacyEnabled;
import static nl.medtechchain.chaincode.contract.util.ConfigUtil.epsilon;
import static nl.medtechchain.chaincode.contract.util.ResponseUtil.*;

@Contract(name = "devicedata", info = @Info(title = "Device Data Contract", license = @License(name = "Apache 2.0 License", url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
public final class DeviceDataContract implements ContractInterface {

    private static final Logger logger = Logger.getLogger(DeviceDataContract.class);

    private static final String INDEX = "TX_ID";

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String StoreDeviceData(Context ctx, String transaction) {
        try {
            var tx = DeviceDataTransaction.parseFrom(ByteString.copyFromUtf8(transaction));
            var key = compositeKey(ctx, TXType.DEVICE_DATA_ASSET, UUID.nameUUIDFromBytes(tx.toByteArray()).toString());
            ctx.getStub().putState(key.toString(), tx.toByteArray());
            logger.debug("Stored device data asset: " + key);
            return successResponse("Device data asset stored successfully");
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse device data asset: " + e.getMessage());
            return errorResponse(ErrorCode.ERROR_CODE_INVALID_ARGUMENT, "Error parsing device data transaction", e.toString());
        }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String Query(Context ctx, String transaction) {
        try {
            var tx = QueryTransaction.parseFrom(ByteString.copyFromUtf8(transaction));

            // Store transaction
            var key = compositeKey(ctx, TXType.QUERY, UUID.nameUUIDFromBytes(tx.toByteArray()).toString());
            ctx.getStub().putState(key.toString(), tx.toByteArray());

            var result = unrecognizedQueryResponse();

            logger.info("Performing: " + tx);
            var startTime = System.nanoTime();

            switch (tx.getQueryType()) {
                case QUERY_TYPE_COUNT:
                    result = count(ctx, tx);
                    break;
                case QUERY_TYPE_GROUPED_COUNT:
                    result = groupedCount(ctx, tx);
                    break;
                case QUERY_TYPE_AVERAGE:
                    result = average(ctx, tx);
                    break;
            }

            var endTime = System.nanoTime();
            var executionTime = endTime - startTime;
            logger.info("Query execution time: " + executionTime);

            return result.toByteString().toStringUtf8();
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse query transaction: " + e.getMessage());
            return errorResponse(ErrorCode.ERROR_CODE_INVALID_ARGUMENT, "Error parsing query transaction", e.toString());
        }
    }

    // TODO: Handle encryption
    private QueryResult count(Context ctx, QueryTransaction tx) {
        var assets = getFilteredData(ctx, tx);
        var resultCount = assets.size();
        if (differentialPrivacyEnabled()) {
            var noise = new LaplaceNoise();
            resultCount = Math.abs((int) noise.addNoise(resultCount, computeL1Sensitivity(tx), epsilon(), 0));
        }
        return QueryResult.newBuilder().setCountResult(resultCount).build();
    }

    // TODO: Handle encryption
    private QueryResult groupedCount(Context ctx, QueryTransaction tx) {
        return null;
    }

    // TODO: Handle encryption
    private QueryResult average(Context ctx, QueryTransaction tx) {
        return null;
    }

    // Sensitivity depends on the queried type and queried data
    // For now, its value will be defaulted to 1
    private long computeL1Sensitivity(QueryTransaction tx) {
        return 1;
    }

    private List<DeviceDataTransaction> getFilteredData(Context ctx, QueryTransaction tx) {
        var filteredDeviceData = new ArrayList<DeviceDataTransaction>();

        var iterator = ctx.getStub().getStateByPartialCompositeKey(ctx.getStub().createCompositeKey(INDEX, TXType.DEVICE_DATA_ASSET.name()));

        for (KeyValue kv : iterator) {
            try {
                DeviceDataTransaction asset = DeviceDataTransaction.parseFrom(kv.getValue());

                boolean valid = FilterChecker.checkEncryption(asset) &&
                        tx.getFiltersList().stream().allMatch(filter -> FilterChecker.checkFilter(asset, filter));

                if (valid)
                    filteredDeviceData.add(asset);

            } catch (InvalidProtocolBufferException e) {
                logger.warning("Error parsing device data transaction from ledger: " + e.getMessage() + "\n" + kv.getKey() + "\n" + Arrays.toString(kv.getValue()));
            }
        }

        return filteredDeviceData;
    }

    private CompositeKey compositeKey(Context ctx, TXType txType, String id) {
        return ctx.getStub().createCompositeKey(INDEX, txType.name(), id);
    }
}