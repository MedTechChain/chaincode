package nl.medtechchain.chaincode.encryption.scheme.phe.paillier;

import nl.medtechchain.chaincode.encryption.scheme.Plaintext;

public class PaillierPlaintext implements Plaintext {
    private final PaillierBitLength bl;
    private final int plaintext;

    PaillierPlaintext(PaillierBitLength bl, int plaintext) {
        this.bl = bl;
        this.plaintext = plaintext;
    }

    public int getPlaintext() {
        return plaintext;
    }

    public PaillierBitLength getBitLength() {
        return bl;
    }

    @Override
    public String toString() {
        return plaintext + "";
    }
}
