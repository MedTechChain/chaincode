package nl.medtechchain.chaincode.contract;

import com.google.protobuf.InvalidProtocolBufferException;
import nl.medtechchain.chaincode.config.ConfigOps;
import nl.medtechchain.proto.config.NetworkConfig;
import nl.medtechchain.proto.config.PlatformConfig;
import nl.medtechchain.proto.config.UpdateNetworkConfig;
import nl.medtechchain.proto.config.UpdatePlatformConfig;
import nl.medtechchain.proto.devicedata.DeviceDataAsset;
import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;

import java.util.stream.Collectors;

import static nl.medtechchain.chaincode.config.ConfigDefaults.NetworkDefaults;
import static nl.medtechchain.chaincode.config.ConfigDefaults.PlatformConfigDefaults;
import static nl.medtechchain.chaincode.util.Base64EncodingOps.decode64;
import static nl.medtechchain.chaincode.util.Base64EncodingOps.encode64;
import static nl.medtechchain.chaincode.util.ChaincodeResponseUtil.invalidTransaction;
import static nl.medtechchain.chaincode.util.ChaincodeResponseUtil.successResponse;

@Contract(name = "platformconfig", info = @Info(title = "Platform Config Contract", license = @License(name = "Apache 2.0 License", url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
public final class PlatformConfigContract implements ContractInterface {

    private static final Logger logger = Logger.getLogger(PlatformConfigContract.class);

    private static final String CURRENT_NETWORK_CONFIG_KEY = "CURRENT_NETWORK_CONFIG";
    private static final String CURRENT_PLATFORM_CONFIG_KEY = "CURRENT_PLATFORM_CONFIG";

    public static NetworkConfig currentNetworkConfig(Context ctx) throws InvalidProtocolBufferException {
        return decode64(ctx.getStub().getStringState(CURRENT_NETWORK_CONFIG_KEY), NetworkConfig::parseFrom);
    }

    public static PlatformConfig currentPlatformConfig(Context ctx) throws InvalidProtocolBufferException {
        return decode64(ctx.getStub().getStringState(CURRENT_PLATFORM_CONFIG_KEY), PlatformConfig::parseFrom);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void Init(Context ctx) {
        var initNetworkConfig = NetworkDefaults.defaultNetworkConfig();
        storeNetworkConfig(ctx, initNetworkConfig);

        var initPlatformConfig = ConfigOps.PlatformConfigOps.create(PlatformConfigDefaults.defaultPlatformConfigs());
        storePlatformConfig(ctx, initPlatformConfig);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String UpdateNetworkConfig(Context ctx, String transaction) {
        try {
            var update = decode64(transaction, UpdateNetworkConfig::parseFrom);
            var current = currentNetworkConfig(ctx);
            storeNetworkConfig(ctx, ConfigOps.NetworkConfigOps.update(current, update.getName(), update.getMapList()));
            logger.info("Updated network config: " + update);
            return encode64(successResponse(transaction));
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse NetworkConfig: " + e.getMessage());
            return encode64(invalidTransaction("Failed to parse NetworkConfig: " + e.getMessage()));
        }
    }

    public static void main(String[] args) {
        System.out.println(DeviceDataAsset.DeviceData.getDescriptor().getFields().stream().map(d -> d.getMessageType().getFullName()).collect(Collectors.toList()));
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String UpdatePlatformConfig(Context ctx, String transaction) {
        try {
            var update = decode64(transaction, UpdatePlatformConfig::parseFrom);
            var current = currentPlatformConfig(ctx);
            storePlatformConfig(ctx, ConfigOps.PlatformConfigOps.update(current, update.getMapList()));
            logger.info("Updated platform config: " + update);
            return encode64(successResponse(transaction));
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse PlatformConfig: " + e.getMessage());
            return encode64(invalidTransaction("Failed to parse PlatformConfig: " + e.getMessage()));
        }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetNetworkConfig(Context ctx) {
        try {
            var platformConfig = currentNetworkConfig(ctx);
            return encode64(platformConfig);
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse NetworkConfig: " + e.getMessage());
            return encode64(invalidTransaction("Failed to parse NetworkConfig: " + e.getMessage()));
        }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetNetworkConfig(Context ctx, String id) {
        try {
            var platformConfig = decode64(ctx.getStub().getStringState(TXType.NETWORK_CONFIG.compositeKey(id).toString()), NetworkConfig::parseFrom);
            return encode64(platformConfig);
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse NetworkConfig: " + e.getMessage());
            return encode64(invalidTransaction("Failed to parse NetworkConfig: " + e.getMessage()));
        }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetPlatformConfig(Context ctx) {
        try {
            var platformConfig = currentPlatformConfig(ctx);
            return encode64(platformConfig);
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse PlatformConfig: " + e.getMessage());
            return encode64(invalidTransaction("Failed to parse PlatformConfig: " + e.getMessage()));
        }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetPlatformConfig(Context ctx, String id) {
        try {
            var platformConfig = decode64(ctx.getStub().getStringState(TXType.PLATFORM_CONFIG.compositeKey(id).toString()), PlatformConfig::parseFrom);
            return encode64(platformConfig);
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse PlatformConfig: " + e.getMessage());
            return encode64(invalidTransaction("Failed to parse PlatformConfig: " + e.getMessage()));
        }
    }

    private void storeNetworkConfig(Context ctx, NetworkConfig networkConfig) {
        ctx.getStub().putStringState(CURRENT_NETWORK_CONFIG_KEY, encode64(networkConfig));
        ctx.getStub().putStringState(TXType.NETWORK_CONFIG.compositeKey(networkConfig.getId()).toString(), encode64(networkConfig));
    }

    private void storePlatformConfig(Context ctx, PlatformConfig platformConfig) {
        ctx.getStub().putStringState(CURRENT_PLATFORM_CONFIG_KEY, encode64(platformConfig));
        ctx.getStub().putStringState(TXType.PLATFORM_CONFIG.compositeKey(platformConfig.getId()).toString(), encode64(platformConfig));
    }

}