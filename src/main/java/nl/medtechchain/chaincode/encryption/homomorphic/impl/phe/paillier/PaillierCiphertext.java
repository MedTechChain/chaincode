package nl.medtechchain.chaincode.encryption.homomorphic.impl.phe.paillier;

import nl.medtechchain.chaincode.encryption.homomorphic.HomomorphicCiphertext;

import java.io.IOException;
import java.math.BigInteger;

public class PaillierCiphertext implements HomomorphicCiphertext<PaillierEncryptionKey, PaillierPlaintext, PaillierCiphertext> {
    private final BigInteger ciphertext;

    public PaillierCiphertext(BigInteger ciphertext) {
        this.ciphertext = ciphertext;
    }

    @Override
    public PaillierCiphertext add(PaillierCiphertext other) {
        return new PaillierCiphertext(ciphertext.multiply(other.ciphertext));
    }

    @Override
    public PaillierCiphertext mul(PaillierPlaintext constant) {
        return new PaillierCiphertext(ciphertext.pow(constant.getPlaintext()));
    }

    @Override
    public PaillierCiphertext mul(PaillierCiphertext ciphertext2) throws IOException {
        // TODO: Implement multiplication protocol
        return null;
    }

    @Override
    public String toString() {
        return ciphertext.toString();
    }
}
