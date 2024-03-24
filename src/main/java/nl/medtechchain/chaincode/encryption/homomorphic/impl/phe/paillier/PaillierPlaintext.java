package nl.medtechchain.chaincode.encryption.homomorphic.impl.phe.paillier;

import nl.medtechchain.chaincode.encryption.homomorphic.Plaintext;

public class PaillierPlaintext implements Plaintext<PaillierEncryptionKey, PaillierPlaintext, PaillierCiphertext> {
    private final int plaintext;

    public PaillierPlaintext(int plaintext) {
        this.plaintext = plaintext;
    }

    public int getPlaintext() {
        return plaintext;
    }

    @Override
    public String toString() {
        return plaintext + "";
    }
}
