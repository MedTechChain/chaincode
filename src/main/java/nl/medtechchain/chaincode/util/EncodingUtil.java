package nl.medtechchain.chaincode.util;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncodingUtil {
    public static <T extends GeneratedMessageV3> String encode64(T m) {
        return Base64.getEncoder().encodeToString(m.toByteArray());
    }

    public static <T extends GeneratedMessageV3> T decode64(String encoded, ProtobufParse<T> parse) throws InvalidProtocolBufferException {
        return parse.parseFrom(Base64.getDecoder().decode(encoded));
    }

    public interface ProtobufParse<T> {
        T parseFrom(byte[] data) throws InvalidProtocolBufferException;
    }

    public static BigInteger stringToBigInteger(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        return new BigInteger(1, bytes);
    }

    public static String bigIntegerToString(BigInteger bigInt) {
        byte[] bytes = bigInt.toByteArray();

        if (bytes[0] == 0) {
            byte[] temp = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, temp, 0, temp.length);
            bytes = temp;
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }
}
