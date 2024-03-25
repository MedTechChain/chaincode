package nl.medtechchain.chaincode.encryption.ttp.paillier;

import java.io.IOException;

public interface PaillierTTPInterface {
    PaillierTTPEncKeyResponse getEncryptionKey(int bitLength) throws IOException;

    PaiilierTTPDecryptResponse decrypt(PaillierTTPDecryptRequest ciphertext) throws IOException;

    static PaillierTTPInterface instance() {
        return new PaillierTTPInterfaceImpl();
    }
}

class PaillierTTPInterfaceImpl implements PaillierTTPInterface {

    PaillierTTPInterfaceImpl() {
        // TODO: create HTTP client
    }

    @Override
    public PaillierTTPEncKeyResponse getEncryptionKey(int bitLength) throws IOException {
        return null;
    }

    @Override
    // TODO:
    public PaiilierTTPDecryptResponse decrypt(PaillierTTPDecryptRequest ciphertext) throws IOException {
        return null;
    }
}
