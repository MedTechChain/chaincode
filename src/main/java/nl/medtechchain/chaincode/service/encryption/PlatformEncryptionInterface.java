package nl.medtechchain.chaincode.service.encryption;

import nl.medtechchain.proto.config.PlatformConfig;

import java.math.BigInteger;
import java.util.Optional;
import java.util.logging.Logger;

import static nl.medtechchain.chaincode.config.ConfigOps.PlatformConfigOps.get;
import static nl.medtechchain.chaincode.config.ConfigOps.PlatformConfigOps.getUnsafe;
import static nl.medtechchain.proto.config.PlatformConfig.Config.*;

public interface PlatformEncryptionInterface {
    String encryptString(String plaintext);

    String encryptLong(long plaintext);

    String encryptBool(boolean plaintext);

    String decryptString(String ciphertext);

    long decryptLong(String ciphertext);

    boolean decryptBool(String ciphertext);

    class Factory {
        private enum SchemeType {
            NONE,
            PAILLIER
        }

        public static Optional<PlatformEncryptionInterface> getInstance(PlatformConfig platformConfig) {
            var logger = Logger.getLogger(PlatformEncryptionInterface.class.getName());

            var schemeProp = get(platformConfig, CONFIG_FEATURE_QUERY_ENCRYPTION_SCHEME).orElse("NONE");
            SchemeType schemeType = SchemeType.NONE;

            try {
                schemeType = SchemeType.valueOf(schemeProp.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid encryption scheme: " + schemeProp + ", defaulting to none");
            }

            Optional<PlatformEncryptionInterface> scheme = Optional.empty();
            try {
                switch (schemeType) {
                    case PAILLIER:
                        var bitLength = Integer.parseInt(getUnsafe(platformConfig, CONFIG_FEATURE_QUERY_ENCRYPTION_PAILLIER_BIT_LENGTH));
                        var publicKey = new BigInteger(getUnsafe(platformConfig, CONFIG_FEATURE_QUERY_ENCRYPTION_PAILLIER_PUBLIC_KEY));
                        var ttpAddress = getUnsafe(platformConfig, CONFIG_FEATURE_QUERY_ENCRYPTION_PAILLIER_TTP_ADRRESS);
                        scheme = Optional.of(new PlatformPaillierEncryption(publicKey, ttpAddress));
                        break;
                }
            } catch (Throwable t) {
                logger.warning("Error while getting scheme: " + t.getMessage());
                scheme = Optional.empty();
            }

            return scheme;
        }
    }
}

