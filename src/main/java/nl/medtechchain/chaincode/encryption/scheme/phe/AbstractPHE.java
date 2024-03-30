package nl.medtechchain.chaincode.encryption.scheme.phe;

import nl.medtechchain.chaincode.encryption.scheme.EncryptionKey;
import nl.medtechchain.chaincode.encryption.scheme.EncryptionScheme;

public abstract class AbstractPHE<K extends EncryptionKey> implements EncryptionScheme {
    protected final K encryptionKey;

    public AbstractPHE(K encryptionKey) {
        this.encryptionKey = encryptionKey;
    }
}
