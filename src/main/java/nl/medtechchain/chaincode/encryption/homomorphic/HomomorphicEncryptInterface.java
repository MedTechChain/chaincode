package nl.medtechchain.chaincode.encryption.homomorphic;

import java.io.IOException;

public interface HomomorphicEncryptInterface<K extends EncryptionKey<K, P, C>, P extends Plaintext<K, P, C>, C extends HomomorphicCiphertext<K, P, C>> {
    C encrypt(P plaintext) throws IOException;
}
