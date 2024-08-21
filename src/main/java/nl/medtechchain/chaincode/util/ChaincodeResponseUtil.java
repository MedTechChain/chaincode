package nl.medtechchain.chaincode.util;

import nl.medtechchain.proto.common.ChaincodeError;
import nl.medtechchain.proto.common.ChaincodeError.ErrorCode;
import nl.medtechchain.proto.common.ChaincodeResponse;
import nl.medtechchain.proto.common.ChaincodeSuccess;

public class ChaincodeResponseUtil {
    public static ChaincodeResponse successResponse(String message) {
        return ChaincodeResponse
                .newBuilder()
                .setSuccess(ChaincodeSuccess
                        .newBuilder()
                        .setMessage(message)
                        .build())
                .build();
    }


    public static ChaincodeResponse errorResponse(ChaincodeError error) {
        return ChaincodeResponse.newBuilder().setError(error).build();
    }

    public static ChaincodeResponse errorResponse(ErrorCode code, String message, String details) {
        return ChaincodeResponse
                .newBuilder()
                .setError(ChaincodeError
                        .newBuilder()
                        .setCode(code)
                        .setMessage(message)
                        .setDetails(details)
                        .build())
                .build();
    }

    public static ChaincodeResponse errorResponse(ErrorCode code, String message) {
        return errorResponse(code, message, "");
    }

    public static ChaincodeResponse invalidTransaction(String message, String details) {
        return errorResponse(ErrorCode.ERROR_CODE_INVALID_TRANSACTION, message, details);
    }

    public static ChaincodeResponse invalidTransaction(String message) {
        return errorResponse(ErrorCode.ERROR_CODE_INVALID_TRANSACTION, message, "");
    }
}