package nl.medtechchain.chaincode.encryption.scheme.phe.paillier;

import nl.medtechchain.chaincode.encryption.exception.IllegalOperationException;
import nl.medtechchain.chaincode.encryption.scheme.Plaintext;
import nl.medtechchain.chaincode.encryption.scheme.phe.AbstractPHE;

import java.math.BigInteger;

public final class Paillier extends AbstractPHE<PaillierEncryptionKey> {
    private static final String BINARY_PATH = "/usr/lib/encrypt";

    public Paillier(PaillierEncryptionKey key) {
        super(key);
    }

    @Override
    public boolean isHomomorphic() {
        return true;
    }

    @Override
    public PaillierPlaintext plaintext(String representation) throws IllegalOperationException {
        int plaintext;
        try {
            plaintext = Integer.parseInt(representation);
        } catch (NumberFormatException e) {
            throw new IllegalOperationException("Could not parse representation: " + representation, e);
        }

        return new PaillierPlaintext(encryptionKey.getBitLength(), plaintext);
    }

    @Override
    public PaillierCiphertext ciphertext(String representation) throws IllegalOperationException {
        BigInteger ciphertext;
        try {
            ciphertext = new BigInteger(representation);
        } catch (NumberFormatException e) {
            throw new IllegalOperationException("Could not parse representation: " + representation, e);
        }
        return new PaillierCiphertext(encryptionKey, ciphertext);
    }

    @Override
    public PaillierCiphertext encrypt(Plaintext plaintext) {
        return null;
    }
}