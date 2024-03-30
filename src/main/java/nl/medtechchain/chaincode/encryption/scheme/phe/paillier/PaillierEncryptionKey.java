package nl.medtechchain.chaincode.encryption.scheme.phe.paillier;

import nl.medtechchain.chaincode.encryption.scheme.BitLength;
import nl.medtechchain.chaincode.encryption.scheme.EncryptionKey;

import java.math.BigInteger;

public class PaillierEncryptionKey implements EncryptionKey {
    private final BitLength bl;
    private final BigInteger N;

    public PaillierEncryptionKey(BitLength bl, BigInteger N) {
        this.bl = bl;
        this.N = N;
    }

    @Override
    public String toString() {
        return N.toString();
    }

    public BitLength getBitLength() {
        return bl;
    }
}
