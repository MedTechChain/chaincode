package nl.medtechchain.chaincode.util;

import nl.medtechchain.proto.common.ChaincodeError;
import nl.medtechchain.proto.common.ChaincodeError.ErrorCode;
import nl.medtechchain.proto.common.ChaincodeResponse;
import nl.medtechchain.proto.common.ChaincodeSuccess;
import nl.medtechchain.proto.query.QueryResult;

import java.util.Base64;

public class ResponseUtil {
    public static String successResponse(String message) {
        return Base64.getEncoder().encodeToString(
                ChaincodeResponse
                        .newBuilder()
                        .setSuccess(ChaincodeSuccess
                                .newBuilder()
                                .setMessage(message)
                                .build())
                        .build()
                        .toByteArray()
        );
    }


    public static String errorResponse(ErrorCode code, String message, String details) {
        return Base64.getEncoder().encodeToString(
                ChaincodeResponse
                        .newBuilder()
                        .setError(ChaincodeError
                                .newBuilder()
                                .setCode(code)
                                .setMessage(message)
                                .setDetails(details)
                                .build())
                        .build()
                        .toByteArray()
        );
    }

    public static QueryResult unrecognizedQueryResult() {
        return QueryResult.newBuilder().setError(
                ChaincodeError
                        .newBuilder()
                        .setCode(ErrorCode.ERROR_CODE_INVALID_TRANSACTION)
                        .setMessage("Unrecognized query type")
                        .setDetails("")
                        .build()
        ).build();
    }
}