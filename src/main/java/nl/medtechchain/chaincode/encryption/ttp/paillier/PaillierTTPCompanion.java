package nl.medtechchain.chaincode.encryption.ttp.paillier;

import nl.medtechchain.chaincode.encryption.scheme.BitLength;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

public class PaillierTTPCompanion {
    public PaillierTTP apply(BitLength bl) {
        return new PaillierTTP(
                bl,
                readEnvRequestTimeout(),
                HttpClient.newHttpClient(),
                readEnvGetEncryptionKeyURI(),
                readEnvDecryptURI()
        );
    }


    private Duration readEnvRequestTimeout() {
        String timeoutRaw = System.getenv("TTP_REQUEST_TIMEOUT");
        if (timeoutRaw != null && !timeoutRaw.isEmpty()) {
            try {
                return Duration.parse(timeoutRaw);
            } catch (Throwable ignored) {
            }
        }
        return Duration.ofMinutes(1);
    }

    private URI readEnvGetEncryptionKeyURI() {
        return URI.create(System.getenv("TTP_GET_ENCRYPTION_KEY_URI"));
    }

    private URI readEnvDecryptURI() {
        return URI.create(System.getenv("TTP_DECRYPT_URI"));
    }
}
