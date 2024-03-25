package nl.medtechchain.chaincode.encryption.scheme.phe.protocol;

import nl.medtechchain.chaincode.encryption.scheme.Ciphertext;

import java.io.IOException;

public interface SMP {
    Ciphertext mul(Ciphertext c1, Ciphertext c2) throws IOException;

    static SMP instance() {
        return new SMPImpl();
    }
}

class SMPImpl implements SMP {

    @Override
    public Ciphertext mul(Ciphertext c1, Ciphertext c2) {
        // TODO: use ttp and homomorphic properties
        return null;
    }
}