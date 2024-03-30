package nl.medtechchain.chaincode.encryption.ttp;

import nl.medtechchain.chaincode.encryption.scheme.Ciphertext;
import nl.medtechchain.chaincode.encryption.scheme.EncryptionKey;
import nl.medtechchain.chaincode.encryption.scheme.Plaintext;
import nl.medtechchain.protos.encryption.EncryptionKeyMetadata;

import java.io.IOException;

public interface TTPInterface {
    EncryptionKey getEncryptionKey(EncryptionKeyMetadata encryptionKeyMetadata) throws IOException;

    Plaintext decrypt(Ciphertext ciphertext) throws IOException;
}
