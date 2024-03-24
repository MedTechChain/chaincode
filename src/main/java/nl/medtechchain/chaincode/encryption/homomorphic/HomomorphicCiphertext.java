package nl.medtechchain.chaincode.encryption.homomorphic;

import java.io.IOException;

public interface HomomorphicCiphertext<K extends EncryptionKey<K, P, C>, P extends Plaintext<K, P, C>, C extends HomomorphicCiphertext<K, P, C>> {
    C add(C other) throws IOException;

    C mul(P constant) throws IOException;

    C mul(C ciphertext2) throws IOException;

    String toString();
}
