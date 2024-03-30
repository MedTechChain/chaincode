package nl.medtechchain.chaincode.encryption.scheme.phe.paillier;

import nl.medtechchain.chaincode.encryption.scheme.BitLength;
import nl.medtechchain.chaincode.encryption.scheme.Plaintext;

public class PaillierPlaintext implements Plaintext {
    private final BitLength bl;
    private final int plaintext;

    PaillierPlaintext(BitLength bl, int plaintext) {
        this.bl = bl;
        this.plaintext = plaintext;
    }

    public int getPlaintext() {
        return plaintext;
    }

    public BitLength getBitLength() {
        return bl;
    }

    @Override
    public String toString() {
        return plaintext + "";
    }
}
