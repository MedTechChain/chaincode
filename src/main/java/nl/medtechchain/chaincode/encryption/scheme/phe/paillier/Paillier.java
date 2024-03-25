package nl.medtechchain.chaincode.encryption.scheme.phe.paillier;

import nl.medtechchain.chaincode.encryption.exception.IllegalOperationException;
import nl.medtechchain.chaincode.encryption.scheme.Plaintext;
import nl.medtechchain.chaincode.encryption.scheme.phe.AbstractPHE;
import nl.medtechchain.chaincode.subprocess.SubprocessCall;

import java.io.IOException;
import java.math.BigInteger;

public final class Paillier extends AbstractPHE<PaillierEncKey> {
    private static final String BINARY_PATH = "/usr/lib/encrypt";

    public Paillier(PaillierEncKey key) {
        super(key);
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
    public PaillierCiphertext encrypt(Plaintext plaintext) throws IOException, IllegalOperationException {
        String command = String.format("%s encrypt --plaintext %s --ek '%s'", BINARY_PATH, plaintext, encryptionKey);
        PaillierPlaintext plaintext1;
        try {
            plaintext1 = (PaillierPlaintext) plaintext;
            assert plaintext1.getBitLength() == encryptionKey.getBitLength();
            return new PaillierCiphertext(encryptionKey, new BigInteger(SubprocessCall.execute(command)));
        } catch (ClassCastException | AssertionError e) {
            throw new IllegalOperationException("Could not encrypt plaintext: " + plaintext, e);
        }
    }
}