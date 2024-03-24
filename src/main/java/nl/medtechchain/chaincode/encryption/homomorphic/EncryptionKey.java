package nl.medtechchain.chaincode.encryption.homomorphic;

public interface EncryptionKey<K extends EncryptionKey<K, P, C>, P extends Plaintext<K, P, C>, C extends HomomorphicCiphertext<K, P, C>> {
    String toString();
}
