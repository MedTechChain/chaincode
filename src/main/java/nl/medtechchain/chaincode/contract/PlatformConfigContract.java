package nl.medtechchain.chaincode.contract;

import com.google.protobuf.InvalidProtocolBufferException;
import nl.medtechchain.chaincode.service.PaillierTTPService;
import nl.medtechchain.chaincode.util.ConfigUtil;
import nl.medtechchain.proto.common.ChaincodeError.ErrorCode;
import nl.medtechchain.proto.config.EncryptionConfig;
import nl.medtechchain.proto.config.PlatformConfig;
import nl.medtechchain.proto.config.ReadPlatformConfigResponse;
import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.Base64;

import static nl.medtechchain.chaincode.util.ConfigUtil.*;
import static nl.medtechchain.chaincode.util.ResponseUtil.errorResponse;
import static nl.medtechchain.chaincode.util.ResponseUtil.successResponse;

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
    public void Init(Context ctx) {
        var platformConfig = defaultPlatformConfig();

        var ttpService = new PaillierTTPService(ConfigUtil.EncryptionConstants.TTP_ADDRESS);
        var keyOpt = ttpService.getEncryptionKey(ConfigUtil.EncryptionConstants.BIT_LENGTH);

        if (keyOpt.isPresent()) {
            var paillierConfig = EncryptionConfig.Paillier.newBuilder()
                    .setBiTLength(ConfigUtil.EncryptionConstants.BIT_LENGTH)
                    .setPublicKey(keyOpt.get())
                    .setTrustedThirdPartyAddress(ConfigUtil.EncryptionConstants.TTP_ADDRESS)
                    .build();

            platformConfig = setPaillier(platformConfig, paillierConfig);
        }

        var ignored = SetPlatformConfig(ctx, Base64.getEncoder().encodeToString(platformConfig.toByteArray()));
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String SetPlatformConfig(Context ctx, String transaction) {
        try {
            var tx = PlatformConfig.parseFrom(Base64.getDecoder().decode(transaction));
            ctx.getStub().putStringState(PLATFORM_CONFIG_KEY, transaction);
            platformConfig = tx;
            logger.info("Updated platform config: " + tx);
            return successResponse("Platform config updated successfully");
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse write platform config transaction: " + e.getMessage());
            return errorResponse(ErrorCode.ERROR_CODE_INVALID_ARGUMENT, "Error parsing write platform config transaction", e.toString());
        }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetPlatformConfig(Context ctx) {
        try {
            var platformConfig = PlatformConfig.parseFrom(Base64.getDecoder().decode(ctx.getStub().getStringState(PLATFORM_CONFIG_KEY)));
            PlatformConfigContract.platformConfig = platformConfig;
            var response = ReadPlatformConfigResponse
                    .newBuilder()
                    .setPlatformConfig(platformConfig)
                    .build();

            if (encryptionVersion().isPresent())
                response = response.toBuilder().setEncryptionVersion(encryptionVersion().get()).build();

            return Base64.getEncoder().encodeToString(response.toByteArray());
        } catch (InvalidProtocolBufferException e) {
            logger.warning("Failed to parse write platform config transaction: " + e.getMessage());
            return errorResponse(ErrorCode.ERROR_CODE_INVALID_ARGUMENT, "Error parsing read platform config transaction", e.toString());
        }
    }

    public static PlatformConfig platformConfig() {
        return platformConfig;
    }
}