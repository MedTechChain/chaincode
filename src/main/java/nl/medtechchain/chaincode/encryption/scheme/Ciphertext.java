package nl.medtechchain.chaincode.encryption.scheme;

import nl.medtechchain.chaincode.encryption.exception.IllegalOperationException;

import java.io.IOException;

public interface Ciphertext {
    Ciphertext add(Ciphertext other) throws IllegalOperationException, IOException;

    Ciphertext mul(Plaintext constant) throws IllegalOperationException, IOException;

    Ciphertext mul(Ciphertext other) throws IllegalOperationException, IOException;

    String toString();
}
