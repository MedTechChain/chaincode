package nl.medtechchain.chaincode.service.encryption.paillier;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.medtechchain.chaincode.service.encryption.paillier.dto.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface PaillierTTPAPI {

    PaillierEncryptionKeyResponse encryptionKey(int bitLength) throws IOException, InterruptedException;

    PaillierEncryptResponse encrypt(PaillierEncryptRequest encryptRequest) throws IOException, InterruptedException;

    PaillierDecryptResponse decrypt(PaillierDecryptRequest decryptRequest) throws IOException, InterruptedException;

    static PaillierTTPAPI getInstance(String ttpAddress) {
        return new PaillierTTPAPI() {
            private final ObjectMapper om = new ObjectMapper();
            private final HttpClient httpClient = HttpClient.newHttpClient();

            @Override
            public PaillierEncryptionKeyResponse encryptionKey(int bitLength) throws IOException, InterruptedException {
                var request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create("http://" + ttpAddress + "/api/paillier/key?bitLength=" + bitLength))
                        .build();

                var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return om.readValue(response.body(), PaillierEncryptionKeyResponse.class);
            }

            @Override
            public PaillierEncryptResponse encrypt(PaillierEncryptRequest encryptRequest) throws IOException, InterruptedException {
                var httpRequest = HttpRequest.newBuilder()
                        .header("Content-Type", "application/ json")
                        .POST(HttpRequest.BodyPublishers.ofString(om.writeValueAsString(encryptRequest)))
                        .uri(URI.create("http://" + ttpAddress + "/api/paillier/encrypt"))
                        .build();

                var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                return om.readValue(response.body(), PaillierEncryptResponse.class);
            }

            @Override
            public PaillierDecryptResponse decrypt(PaillierDecryptRequest decryptRequest) throws IOException, InterruptedException {
                var httpRequest = HttpRequest.newBuilder()
                        .header("Content-Type", "application/ json")
                        .POST(HttpRequest.BodyPublishers.ofString(om.writeValueAsString(decryptRequest)))
                        .uri(URI.create("http://" + ttpAddress + "/api/paillier/decrypt"))
                        .build();

                var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                return om.readValue(response.body(), PaillierDecryptResponse.class);
            }
        };
    }
}
