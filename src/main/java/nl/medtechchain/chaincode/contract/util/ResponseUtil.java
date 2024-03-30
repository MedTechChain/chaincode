package nl.medtechchain.chaincode.contract.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import nl.medtechchain.protos.common.Error;

public class ResponseUtil {
    public static String error(String message) {
        try {
            return JsonFormat.printer().print(Error.newBuilder().setMessage(message).build());
        } catch (InvalidProtocolBufferException e) {
            return String.format("{\"message\": \"%s\"}", message);
        }
    }
}
