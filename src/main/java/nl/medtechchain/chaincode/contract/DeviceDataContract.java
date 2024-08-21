package nl.medtechchain.chaincode.contract;

import com.google.protobuf.InvalidProtocolBufferException;
import nl.medtechchain.chaincode.service.query.FilterService;
import nl.medtechchain.chaincode.service.query.InputValidatorService;
import nl.medtechchain.chaincode.service.query.QueryService;
import nl.medtechchain.proto.devicedata.DeviceDataAsset;
import nl.medtechchain.proto.query.Query;
import nl.medtechchain.proto.query.QueryAsset;
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

import static nl.medtechchain.chaincode.util.ChaincodeResponseUtil.*;
import static nl.medtechchain.chaincode.util.EncodingUtil.decode64;
import static nl.medtechchain.chaincode.util.EncodingUtil.encode64;
import static nl.medtechchain.chaincode.util.MeasureExecTimeUtil.monitorTime;

@Contract(name = "devicedata", info = @Info(title = "Device Data Contract", license = @License(name = "Apache 2.0 License", url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
public final class DeviceDataContract implements ContractInterface {

    private static final Logger logger = Logger.getLogger(DeviceDataContract.class);

    private static final String INDEX = "TX_ID_";

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String StoreDeviceData(Context ctx, String id, String transaction) {
        try {
            decode64(transaction, DeviceDataAsset::parseFrom);
            var key = compositeKey(ctx, TXType.DEVICE_DATA_ASSET, id);
            ctx.getStub().putStringState(key.toString(), transaction);
            logger.debug("Stored device data asset: " + key);
            return encode64(successResponse("Device data asset stored successfully"));
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse device data asset: " + e.getMessage());
            return encode64(invalidTransaction("Error parsing device data transaction", e.toString()));
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
            var tx = decode64(transaction, Query::parseFrom);

            var error = InputValidatorService.validateQueryFilters(tx);
            if (error.isPresent())
                return encode64(errorResponse(error.get()));

            logger.info("Performing: " + tx);

            var result = monitorTime(() -> QueryService.executeQuery(tx));

            var asset = QueryAsset.newBuilder().setQuery(tx).setResult(result).build();
            var key = compositeKey(ctx, TXType.QUERY, UUID.nameUUIDFromBytes(asset.toByteArray()).toString());
            ctx.getStub().putStringState(key.toString(), encode64(asset));

            return encode64(result);
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse query transaction: " + e.getMessage());
            return encode64(invalidTransaction("Error parsing query transaction", e.toString()));
        }
    }


    private List<DeviceDataAsset> getFilteredData(Context ctx, Query tx) {
        var filteredDeviceData = new ArrayList<DeviceDataAsset>();

        var iterator = ctx.getStub().getStateByPartialCompositeKey(ctx.getStub().createCompositeKey(INDEX, TXType.DEVICE_DATA_ASSET.name()));

        for (KeyValue kv : iterator) {
            try {
                DeviceDataAsset asset = decode64(kv.getStringValue(), DeviceDataAsset::parseFrom);

                boolean valid = FilterService.checkEncryption(asset) &&
                        tx.getFiltersList().stream().allMatch(filter -> FilterService.checkFilter(asset, filter));

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