package nl.medtechchain.chaincode.contract.util;

import nl.medtechchain.proto.common.ChaincodeError;
import nl.medtechchain.proto.common.ChaincodeError.ErrorCode;
import nl.medtechchain.proto.common.ChaincodeResponse;
import nl.medtechchain.proto.common.ChaincodeSuccess;
import nl.medtechchain.proto.query.QueryResult;
import org.hyperledger.fabric.contract.ContractInterface;

public final class ResponseUtil implements ContractInterface {
    public static String successResponse(String message) {
        return ChaincodeResponse
                .newBuilder()
                .setSuccess(ChaincodeSuccess
                        .newBuilder()
                        .setMessage(message)
                        .build())
                .build()
                .toByteString()
                .toStringUtf8();
    }


    public static String errorResponse(ErrorCode code, String message, String details) {
        return ChaincodeResponse
                .newBuilder()
                .setError(ChaincodeError
                        .newBuilder()
                        .setCode(code)
                        .setMessage(message)
                        .setDetails(details)
                        .build())
                .build()
                .toByteString()
                .toStringUtf8();
    }

    public static QueryResult unrecognizedQueryResponse() {
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