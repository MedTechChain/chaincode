package nl.medtechchain.chaincode.encryption.scheme;

import nl.medtechchain.chaincode.encryption.exception.IllegalOperationException;

import java.io.IOException;

public interface HEInterface {
    Plaintext plaintext(String representation) throws IllegalOperationException;

    Ciphertext ciphertext(String representation) throws IllegalOperationException;

    Ciphertext encrypt(Plaintext plaintext) throws IllegalOperationException, IOException;
}
