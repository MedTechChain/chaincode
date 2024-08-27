package nl.medtechchain.chaincode.contract;

import com.google.protobuf.InvalidProtocolBufferException;
import nl.medtechchain.chaincode.service.encryption.PlatformEncryptionInterface;
import nl.medtechchain.chaincode.service.query.FilterService;
import nl.medtechchain.chaincode.service.query.QueryService;
import nl.medtechchain.proto.config.PlatformConfig;
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
import org.hyperledger.fabric.shim.ledger.KeyValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static nl.medtechchain.chaincode.util.Base64EncodingOps.decode64;
import static nl.medtechchain.chaincode.util.Base64EncodingOps.encode64;
import static nl.medtechchain.chaincode.util.ChaincodeResponseUtil.*;
import static nl.medtechchain.chaincode.util.MeasureExecTimeUtil.monitorTime;

@Contract(name = "devicedata", info = @Info(title = "Device Data Contract", license = @License(name = "Apache 2.0 License", url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
public final class DeviceDataContract implements ContractInterface {

    private static final Logger logger = Logger.getLogger(DeviceDataContract.class);

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String StoreDeviceData(Context ctx, String id, String transaction) {
        try {
            decode64(transaction, DeviceDataAsset::parseFrom);
            var key = TXType.DEVICE_DATA_ASSET.compositeKey(id);
            ctx.getStub().putStringState(key.toString(), transaction);
            logger.debug("Stored device data asset: " + key);
            return encode64(successResponse("Device data asset stored successfully"));
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse DeviceDataAsset: " + e.getMessage());
            return encode64(invalidTransaction("Failed to parse DeviceDataAsset: " + e.getMessage()));
        }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String Query(Context ctx, String transaction) {
        try {
            var platformConfig = ConfigContract.currentPlatformConfig(ctx);
            var query = decode64(transaction, Query::parseFrom);
            var queryService = new QueryService(platformConfig);

            var error = queryService.validateQuery(query);
            if (error.isPresent())
                return encode64(errorResponse(error.get()));

            logger.info("Performing: " + query);

            var result = monitorTime(() -> {
                var data = getFilteredData(ctx, query, platformConfig);
                var r = QueryResult.newBuilder().setError(invalidTransaction("Unknown query type").getError()).build();
                try {
                    switch (query.getQueryType()) {
                        case COUNT:
                            r = queryService.count(query, data);
                            break;
                        case GROUPED_COUNT:
                            r = queryService.groupedCount(query, data);
                            break;
                        case AVERAGE:
                            r = queryService.average(query, data);
                            break;
                    }
                } catch (Exception t) {
                    logger.log(Level.WARNING, "Query error", t);
                    return QueryResult.newBuilder().setError(internalError("Error running query", t.toString()).getError()).build();
                }

                return r;
            });

            var asset = QueryAsset.newBuilder().setQuery(query).setResult(result).build();
            var key = TXType.QUERY.compositeKey(UUID.nameUUIDFromBytes(asset.toByteArray()).toString());
            ctx.getStub().putStringState(key.toString(), encode64(asset));

            return encode64(result);
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse query transaction: " + e.getMessage());
            return encode64(invalidTransaction("Error parsing query transaction", e.toString()));
        }
    }

    private List<DeviceDataAsset> getFilteredData(Context ctx, Query tx, PlatformConfig platformConfig) {
        var filteredDeviceData = new ArrayList<DeviceDataAsset>();

        var iterator = ctx.getStub().getStateByPartialCompositeKey(TXType.DEVICE_DATA_ASSET.partialKey());

        var filterService = PlatformEncryptionInterface.Factory.getInstance(platformConfig).map(FilterService::new).orElse(new FilterService());

        for (KeyValue kv : iterator) {
            try {
                DeviceDataAsset asset = decode64(kv.getStringValue(), DeviceDataAsset::parseFrom);

                boolean valid = asset.getConfigId().equals(platformConfig.getId()) &&
                        tx.getFiltersList().stream().allMatch(filter -> filterService.checkFilter(asset, filter));

                if (valid)
                    filteredDeviceData.add(asset);

            } catch (InvalidProtocolBufferException e) {
                logger.warning("Error parsing device data transaction from ledger: " + e.getMessage() + "\n" + kv.getKey() + "\n" + Arrays.toString(kv.getValue()));
            }
        }

        return filteredDeviceData;
    }
}