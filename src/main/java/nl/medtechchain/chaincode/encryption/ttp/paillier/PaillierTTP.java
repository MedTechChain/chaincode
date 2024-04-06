package nl.medtechchain.chaincode.encryption.ttp.paillier;

import nl.medtechchain.chaincode.encryption.scheme.BitLength;
import nl.medtechchain.chaincode.encryption.scheme.Ciphertext;
import nl.medtechchain.chaincode.encryption.scheme.EncryptionKey;
import nl.medtechchain.chaincode.encryption.scheme.Plaintext;
import nl.medtechchain.chaincode.encryption.ttp.TTPInterface;
import nl.medtechchain.protos.encryption.EncryptionKeyMetadata;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

public class PaillierTTP implements TTPInterface {

    private BitLength bl;

    private Duration timeout;
    private HttpClient httpClient;

    private URI getEncryptionKeyURI;
    private URI decryptURI;

    PaillierTTP(BitLength bl, Duration timeout, HttpClient httpClient, URI getEncryptionKeyURI, URI decrypURI) {
        this.timeout = timeout;
        this.httpClient = httpClient;
        this.getEncryptionKeyURI = getEncryptionKeyURI;
        this.decryptURI = decrypURI;
    }

    @Override
    public EncryptionKey getEncryptionKey(EncryptionKeyMetadata encryptionKeyMetadata) throws IOException {
        // TODO: Implement communication with TTP to retrieve encryption key
        return null;
    }

    @Override
    public Plaintext decrypt(Ciphertext ciphertext) throws IOException {
        // TODO: Implement communication with TTP to decrypt data
        return null;
    }
}
