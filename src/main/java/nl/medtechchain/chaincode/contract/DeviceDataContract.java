package nl.medtechchain.chaincode.contract;

import com.google.privacy.differentialprivacy.LaplaceNoise;
import com.google.protobuf.InvalidProtocolBufferException;
import nl.medtechchain.chaincode.service.FilterChecker;
import nl.medtechchain.proto.common.ChaincodeError.ErrorCode;
import nl.medtechchain.proto.devicedata.DeviceDataAsset;
import nl.medtechchain.proto.query.Query;
import nl.medtechchain.proto.query.QueryAsset;
import nl.medtechchain.proto.query.QueryResult;
import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;

import java.util.*;

import static nl.medtechchain.chaincode.util.ConfigUtil.differentialPrivacyConfig;
import static nl.medtechchain.chaincode.util.ResponseUtil.*;

@Contract(name = "devicedata", info = @Info(title = "Device Data Contract", license = @License(name = "Apache 2.0 License", url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
public final class DeviceDataContract implements ContractInterface {

    private static final Logger logger = Logger.getLogger(DeviceDataContract.class);

    private static final String INDEX = "TX_ID_";

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String StoreDeviceData(Context ctx, String id, String transaction) {
        try {
            DeviceDataAsset.parseFrom(Base64.getDecoder().decode(transaction));
            var key = compositeKey(ctx, TXType.DEVICE_DATA_ASSET, id);
            ctx.getStub().putStringState(key.toString(), transaction);
            logger.debug("Stored device data asset: " + key);
            return successResponse("Device data asset stored successfully");
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse device data asset: " + e.getMessage());
            return errorResponse(ErrorCode.ERROR_CODE_INVALID_ARGUMENT, "Error parsing device data transaction", e.toString());
        }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public void Test(Context ctx) {
        var iterator = ctx.getStub().getStateByPartialCompositeKey(ctx.getStub().createCompositeKey(INDEX, TXType.DEVICE_DATA_ASSET.name()));
        DeviceDataAsset asset = DeviceDataAsset.newBuilder().build();
        for (KeyValue kv : iterator) {
            try {
                asset = DeviceDataAsset.parseFrom(Base64.getDecoder().decode(kv.getValue()));
                logger.info(asset.toString());
            } catch (InvalidProtocolBufferException e) {
                logger.warning("Error parsing device data transaction from ledger: " + e.getMessage() + "\n" + kv.getKey() + "\n" + Arrays.toString(kv.getValue()));
            }
        }
        logger.info(asset.toString());
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String Query(Context ctx, String transaction) {
        try {
            var tx = Query.parseFrom(Base64.getDecoder().decode(transaction));

            var result = unrecognizedQueryResult();

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

            var asset = QueryAsset.newBuilder().setQuery(tx).setResult(result).build();
            var key = compositeKey(ctx, TXType.QUERY, UUID.nameUUIDFromBytes(asset.toByteArray()).toString());
            ctx.getStub().putStringState(key.toString(), Base64.getEncoder().encodeToString(asset.toByteArray()));

            return Base64.getEncoder().encodeToString(result.toByteArray());
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse query transaction: " + e.getMessage());
            return errorResponse(ErrorCode.ERROR_CODE_INVALID_ARGUMENT, "Error parsing query transaction", e.toString());
        }
    }

    // TODO: Handle encryption
    private QueryResult count(Context ctx, Query tx) {
        var assets = getFilteredData(ctx, tx);
        var resultCount = assets.size();
        var differentialPrivacyConfig = differentialPrivacyConfig();
        switch (differentialPrivacyConfig.getMechanismCase()) {
            case LAPLACE:
                var laplaceConfig = differentialPrivacyConfig.getLaplace();
                resultCount = Math.abs((int) new LaplaceNoise().addNoise(resultCount, computeL1Sensitivity(tx), laplaceConfig.getEpsilon(), 0));
                break;
        }
        return QueryResult.newBuilder().setCountResult(resultCount).build();
    }

    // TODO: Handle encryption
    private QueryResult groupedCount(Context ctx, Query tx) {
        return null;
    }

    // TODO: Handle encryption
    private QueryResult average(Context ctx, Query tx) {
        return null;
    }

    // Sensitivity depends on the queried type and queried data
    // For now, its value will be defaulted to 1
    private long computeL1Sensitivity(Query tx) {
        return 1;
    }

    private List<DeviceDataAsset> getFilteredData(Context ctx, Query tx) {
        var filteredDeviceData = new ArrayList<DeviceDataAsset>();

        var iterator = ctx.getStub().getStateByPartialCompositeKey(ctx.getStub().createCompositeKey(INDEX, TXType.DEVICE_DATA_ASSET.name()));

        for (KeyValue kv : iterator) {
            try {
                DeviceDataAsset asset = DeviceDataAsset.parseFrom(Base64.getDecoder().decode(kv.getValue()));

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