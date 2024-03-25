package nl.medtechchain.chaincode.encryption.scheme.phe;

import nl.medtechchain.chaincode.encryption.scheme.EncKey;
import nl.medtechchain.chaincode.encryption.scheme.HEInterface;

public abstract class AbstractPHE<K extends EncKey> implements HEInterface {
    protected final K encryptionKey;

    public AbstractPHE(K encryptionKey) {
        this.encryptionKey = encryptionKey;
    }
}
