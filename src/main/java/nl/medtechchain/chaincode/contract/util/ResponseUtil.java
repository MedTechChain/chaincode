package nl.medtechchain.chaincode.contract.util;

import nl.medtechchain.protos.common.Error;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ResponseUtils;

public class ResponseUtil {
    private static final String ERROR_TYPE = "ERROR";

    public static Chaincode.Response success() {
        return ResponseUtils.newSuccessResponse();
    }

    public static Chaincode.Response success(String message, byte[] payload) {
        return ResponseUtils.newSuccessResponse(message, payload);
    }

    public static Chaincode.Response error(String message) {
        return error(Chaincode.Response.Status.INTERNAL_SERVER_ERROR, message);
    }

    public static Chaincode.Response forbidden(String message) {
        return error(Chaincode.Response.Status.forCode(403), message);
    }

    private static Chaincode.Response error(Chaincode.Response.Status status, String message) {
        Error e = Error.newBuilder().setMessage(message).build();
        return new Chaincode.Response(status, ERROR_TYPE, e.toByteArray());
    }
}
