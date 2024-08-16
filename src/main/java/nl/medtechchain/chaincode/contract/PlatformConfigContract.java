package nl.medtechchain.chaincode.contract;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import nl.medtechchain.proto.common.ChaincodeError.ErrorCode;
import nl.medtechchain.proto.config.*;
import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import static nl.medtechchain.chaincode.contract.util.ConfigUtil.encryptionVersion;
import static nl.medtechchain.chaincode.contract.util.ResponseUtil.errorResponse;
import static nl.medtechchain.chaincode.contract.util.ResponseUtil.successResponse;

@Contract(name = "platformconfig", info = @Info(title = "Platform Config Contract", license = @License(name = "Apache 2.0 License", url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
public final class PlatformConfigContract implements ContractInterface {

    private static final Logger logger = Logger.getLogger(PlatformConfigContract.class);

    private static final String PLATFORM_CONFIG_KEY = "PLATFORM_CONFIG_KEY";

    private static PlatformConfig platformConfig = defaultPlatformConfig();

    @Override
    public Context createContext(ChaincodeStub stub) {
        try {
            platformConfig = PlatformConfig.parseFrom(stub.getState(PLATFORM_CONFIG_KEY));
        } catch (InvalidProtocolBufferException e) {
            platformConfig = defaultPlatformConfig();
        }
        return ContractInterface.super.createContext(stub);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(Context ctx) {
        WritePlatformConfigTransaction writePlatformConfigTransaction = WritePlatformConfigTransaction
                .newBuilder()
                .setPlatformConfig(defaultPlatformConfig())
                .build();

        var tx = writePlatformConfigTransaction.toByteString().toStringUtf8();
        var ignored = SetPlatformConfig(ctx, tx);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String SetPlatformConfig(Context ctx, String transaction) {
        try {
            var tx = WritePlatformConfigTransaction.parseFrom(ByteString.copyFromUtf8(transaction));
            ctx.getStub().putState(PLATFORM_CONFIG_KEY, tx.getPlatformConfig().toByteArray());
            platformConfig = tx.getPlatformConfig();
            logger.debug("Updated platform config: " + tx.getPlatformConfig());
            return successResponse("Platform config updated successfully");
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse write platform config transaction: " + e.getMessage());
            return errorResponse(ErrorCode.ERROR_CODE_INVALID_ARGUMENT, "Error parsing write platform config transaction", e.toString());
        }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetPlatformConfig(Context ctx, String transaction) {
        try {
            ReadPlatformConfigTransaction.parseFrom(ByteString.copyFromUtf8(transaction));
            var platformConfig = PlatformConfig.parseFrom(ctx.getStub().getState(PLATFORM_CONFIG_KEY));
            PlatformConfigContract.platformConfig = platformConfig;
            var response = ReadPlatformConfigResponse
                    .newBuilder()
                    .setPlatformConfig(platformConfig)
                    .setEncryptionVersion(encryptionVersion())
                    .build();
            return response.toByteString().toStringUtf8();
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse write platform config transaction: " + e.getMessage());
            return errorResponse(ErrorCode.ERROR_CODE_INVALID_ARGUMENT, "Error parsing read platform config transaction", e.toString());
        }
    }

    public static PlatformConfig getPlatformConfig() {
        return platformConfig;
    }

    private static PlatformConfig defaultPlatformConfig() {
        var inputConfig = InputConfig
                .newBuilder()
                .build();

        var differentialPrivacyConfig = DifferentialPrivacyConfig
                .newBuilder()
                .setEnabled(true)
                .setEpsilon(1)
                .build();

        var encryptionConfig = EncryptionConfig
                .newBuilder()
                .setEnabled(false)
                .build();

        var queryConfig = QueryConfig
                .newBuilder()
                .setInputConfig(inputConfig)
                .setDifferentialPrivacyConfig(differentialPrivacyConfig)
                .setEncryptionConfig(encryptionConfig)
                .build();

        var auditableKeyExchangeConfig = AuditableKeyExchangeConfig
                .newBuilder()
                .setEnabled(false)
                .build();

        var featureConfig = FeatureConfig
                .newBuilder()
                .setQueryConfig(queryConfig)
                .setAuditableKeyExchangeConfig(auditableKeyExchangeConfig)
                .build();


        var participantConfig = ParticipantConfig
                .newBuilder()
                .build();

        return PlatformConfig
                .newBuilder()
                .setFeatureConfig(featureConfig)
                .setParticipantConfig(participantConfig)
                .build();
    }
}