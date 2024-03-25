package nl.medtechchain.chaincode.encryption.scheme.phe.paillier;

import nl.medtechchain.chaincode.encryption.scheme.EncKey;

import java.math.BigInteger;

public class PaillierEncKey implements EncKey {
    private final PaillierBitLength bl;
    private final BigInteger N;

    public PaillierEncKey(PaillierBitLength bl, BigInteger N) {
        this.bl = bl;
        this.N = N;
    }

    @Override
    public String toString() {
        return N.toString();
    }

    public PaillierBitLength getBitLength() {
        return bl;
    }
}
