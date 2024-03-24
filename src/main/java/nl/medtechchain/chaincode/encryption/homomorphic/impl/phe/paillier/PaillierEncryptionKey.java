package nl.medtechchain.chaincode.encryption.homomorphic.impl.phe.paillier;

import nl.medtechchain.chaincode.encryption.homomorphic.EncryptionKey;

import java.math.BigInteger;

public class PaillierEncryptionKey implements EncryptionKey<PaillierEncryptionKey, PaillierPlaintext, PaillierCiphertext> {
    private final BigInteger N;

    public PaillierEncryptionKey(BigInteger N) {
        this.N = N;
    }

    @Override
    public String toString() {
        return N.toString();
    }
}
