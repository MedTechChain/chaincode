package nl.medtechchain.chaincode.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.medtechchain.chaincode.service.dto.PaillierDecryptRequest;
import nl.medtechchain.chaincode.service.dto.PaillierDecryptResponse;
import nl.medtechchain.chaincode.service.dto.PaillierEncryptionKeyResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.logging.Logger;

public class PaillierTTPService {
    private static final Logger logger = Logger.getLogger(PaillierTTPService.class.getName());

    private final ObjectMapper om = new ObjectMapper();
    private final String ttpAddress;

    public PaillierTTPService(String ttpAddress) {
        this.ttpAddress = ttpAddress;
    }

    public Optional<String> getEncryptionKey(int bitLength) {
        try {

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI("http://" + ttpAddress + "/api/paillier/key?bitLength=" + bitLength))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                PaillierEncryptionKeyResponse keyResponse = om.readValue(response.body(), PaillierEncryptionKeyResponse.class);
                return Optional.of(keyResponse.getEncryptionKey());
            }

            logger.warning("Failed to fetch encryption key. HTTP code: " + response.statusCode());
            return Optional.empty();
        } catch (Throwable e) {
            logger.warning("Failed to fetch encryption key: " + e);
            return Optional.empty();
        }
    }

    public Optional<String> decrypt(PaillierDecryptRequest request) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-Type", "application/ json")
                    .POST(HttpRequest.BodyPublishers.ofString(om.writeValueAsString(request)))
                    .uri(new URI("http://" + ttpAddress + "/api/paillier/decrypt"))
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                PaillierDecryptResponse decryptResponse = om.readValue(response.body(), PaillierDecryptResponse.class);
                return Optional.of(decryptResponse.getPlaintext());
            }

            logger.warning("Failed to decrypt data. HTTP code: " + response.statusCode());
            return Optional.empty();
        } catch (Throwable e) {
            logger.warning("Failed to decrypt data: " + e);
            return Optional.empty();
        }
    }
}
