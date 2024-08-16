package nl.medtechchain.chaincode.contract.util;

import java.util.UUID;

import static nl.medtechchain.chaincode.contract.PlatformConfigContract.getPlatformConfig;

public class ConfigUtil {
    public static String encryptionVersion() {
        return UUID.nameUUIDFromBytes(getPlatformConfig().getFeatureConfig().getQueryConfig().getEncryptionConfig().toByteArray()).toString();
    }

    public static boolean encryptionEnabled() {
        return getPlatformConfig().getFeatureConfig().getQueryConfig().getEncryptionConfig().getEnabled();
    }

    public static boolean differentialPrivacyEnabled() {
        return getPlatformConfig().getFeatureConfig().getQueryConfig().getDifferentialPrivacyConfig().getEnabled();
    }

    public static Integer epsilon() {
        return getPlatformConfig().getFeatureConfig().getQueryConfig().getDifferentialPrivacyConfig().getEpsilon();
    }
}
