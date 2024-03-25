package nl.medtechchain.chaincode.encryption.scheme.phe.paillier;

import nl.medtechchain.chaincode.encryption.exception.IllegalOperationException;
import nl.medtechchain.chaincode.encryption.scheme.phe.protocol.SMP;
import nl.medtechchain.chaincode.encryption.scheme.Ciphertext;
import nl.medtechchain.chaincode.encryption.scheme.Plaintext;

import java.io.IOException;
import java.math.BigInteger;

public class PaillierCiphertext implements Ciphertext {
    private final PaillierEncKey encryptionKey;
    private final BigInteger ciphertext;

    PaillierCiphertext(PaillierEncKey encryptionKey, BigInteger ciphertext) {
        this.encryptionKey = encryptionKey;
        this.ciphertext = ciphertext;
    }

    @Override
    public PaillierCiphertext add(Ciphertext other) throws IllegalOperationException {
        PaillierCiphertext other1;
        try {
            other1 = (PaillierCiphertext) other;
            assert encryptionKey.equals(other1.encryptionKey);
        } catch (ClassCastException | AssertionError e) {
            throw new IllegalOperationException("Cannot perform operation", e);
        }
        return new PaillierCiphertext(encryptionKey, ciphertext.multiply(other1.ciphertext));
    }

    @Override
    public PaillierCiphertext mul(Plaintext constant) throws IllegalOperationException {
        PaillierPlaintext constant1;
        try {
            constant1 = (PaillierPlaintext) constant;
            assert constant1.getBitLength() == encryptionKey.getBitLength();
        } catch (ClassCastException | AssertionError e) {
            throw new IllegalOperationException("Cannot perform operation", e);
        }
        return new PaillierCiphertext(encryptionKey, ciphertext.pow(constant1.getPlaintext()));
    }

    @Override
    public PaillierCiphertext mul(Ciphertext other) throws IOException, IllegalOperationException {
        PaillierCiphertext other1;
        try {
            other1 = (PaillierCiphertext) other;
            assert encryptionKey.equals(other1.encryptionKey);
            Ciphertext result =  SMP.instance().mul(this, other1);
            assert result instanceof PaillierCiphertext;
            return (PaillierCiphertext) result;
        } catch (ClassCastException | AssertionError e) {
            throw new IllegalOperationException("Cannot perform operation", e);
        }
    }

    @Override
    public String toString() {
        return ciphertext.toString();
    }
}
