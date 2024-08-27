package nl.medtechchain.chaincode.service.encryption;

import autovalue.shaded.kotlin.NotImplementedError;
import lombok.SneakyThrows;
import nl.medtechchain.chaincode.service.encryption.paillier.PaillierTTPAPI;
import nl.medtechchain.chaincode.service.encryption.paillier.dto.PaillierDecryptRequest;
import nl.medtechchain.chaincode.service.encryption.paillier.dto.PaillierEncryptRequest;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class PlatformPaillierEncryption implements PlatformEncryptionInterface, HomomorphicEncryptionScheme {

    private final BigInteger encryptionKey;
    private final PaillierTTPAPI api;

    PlatformPaillierEncryption(BigInteger encryptionKey, String ttpAddress) {
        this.encryptionKey = encryptionKey;
        this.api = PaillierTTPAPI.getInstance(ttpAddress);
    }

    @Override
    public String encryptString(String plaintext) {
        return encrypt(stringToBigInteger(plaintext)).toString();
    }

    @Override
    public String encryptLong(long plaintext) {
        return encrypt(BigInteger.valueOf(plaintext)).toString();
    }

    @Override
    public String encryptBool(boolean plaintext) {
        if (plaintext)
            return encrypt(BigInteger.ONE).toString();
        return encrypt(BigInteger.ZERO).toString();
    }

    @Override
    public String decryptString(String ciphertext) {
        return bigIntegerToString(decrypt(new BigInteger(ciphertext)));
    }

    @Override
    public long decryptLong(String ciphertext) {
        return decrypt(new BigInteger(ciphertext)).longValue();
    }

    @Override
    public boolean decryptBool(String ciphertext) {
        return decrypt(new BigInteger(ciphertext)).equals(BigInteger.ONE);
    }

    @Override
    public String add(String c1, String c2) {
        return new BigInteger(c1).multiply(new BigInteger(c2)).toString();
    }

    @Override
    public String mulCt(String ct, String c) {
        return new BigInteger(c).pow(Integer.parseInt(ct)).toString();
    }

    @Override
    public String mul(String c1, String c2) {
        throw new NotImplementedError("SMP not implemented currently since it is not needed.");
    }

    @SneakyThrows
    private BigInteger encrypt(BigInteger value) {
        return new BigInteger(api.encrypt(new PaillierEncryptRequest(encryptionKey.toString(), value.toString())).getCiphertext());
    }

    @SneakyThrows
    private BigInteger decrypt(BigInteger value) {
        return new BigInteger(api.decrypt(new PaillierDecryptRequest(encryptionKey.toString(), value.toString())).getPlaintext());
    }

    private BigInteger stringToBigInteger(String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        return new BigInteger(1, bytes);  // 1 indicates positive number
    }

    private String bigIntegerToString(BigInteger bigInt) {
        byte[] bytes = bigInt.toByteArray();

        if (bytes[0] == 0) {
            byte[] temp = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, temp, 0, temp.length);
            bytes = temp;
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }
}
