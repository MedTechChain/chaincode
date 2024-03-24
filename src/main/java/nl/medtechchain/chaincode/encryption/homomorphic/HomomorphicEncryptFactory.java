package nl.medtechchain.chaincode.encryption.homomorphic;


import java.io.IOException;

public interface HomomorphicEncryptFactory<K extends EncryptionKey<K, P, C>, P extends Plaintext<K, P, C>, C extends HomomorphicCiphertext<K, P, C>> {
    HomomorphicEncryptInterface<K, P, C> encryptInterface() throws IOException;

    P parsePlaintext(String s);

    C parseCiphertext(String s);
}
