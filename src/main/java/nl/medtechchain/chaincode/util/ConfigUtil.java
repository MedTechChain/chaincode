package nl.medtechchain.chaincode.util;

import nl.medtechchain.proto.config.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static nl.medtechchain.chaincode.contract.PlatformConfigContract.platformConfig;

public class ConfigUtil {
    public static class EncryptionConstants {
        public static final String TTP_ADDRESS = "ttp.medtechchain.nl:6000";
        public static final int BIT_LENGTH = 2048;
    }

    public static class HospitalConstants {
        public static List<ParticipantConfig.HospitalConfig> hospitalConfigs() {
            var healpoint = ParticipantConfig.HospitalConfig.newBuilder()
                    .setName("HealPoint")
                    .setApplicationServerAddress("http://hospital.healpoint.nl:8080")
                    .build();

            var lifecare = ParticipantConfig.HospitalConfig.newBuilder()
                    .setName("LifeCare")
                    .setApplicationServerAddress("http://hospital.lifecare.nl:8080")
                    .build();

            return List.of(healpoint, lifecare);
        }
    }


    public static Optional<String> encryptionVersion() {
        if (encryptionEnabled())
            return Optional.of(UUID.nameUUIDFromBytes(encryptionConfig().toByteArray()).toString());

        return Optional.empty();
    }

    public static boolean encryptionEnabled() {
        return encryptionConfig().getSchemeCase() != EncryptionConfig.SchemeCase.SCHEME_NOT_SET;
    }

    public static EncryptionConfig encryptionConfig() {
        return platformConfig().getFeatureConfig().getQueryConfig().getEncryptionConfig();
    }

    public static DifferentialPrivacyConfig differentialPrivacyConfig() {
        return platformConfig().getFeatureConfig().getQueryConfig().getDifferentialPrivacyConfig();
    }

    public static PlatformConfig setPaillier(PlatformConfig platformConfig, EncryptionConfig.Paillier paillier) {
        var featureConfig = platformConfig.getFeatureConfig();
        var queryConfig = featureConfig.getQueryConfig();
        var updatedQueryConfig = queryConfig.toBuilder().setEncryptionConfig(EncryptionConfig.newBuilder().setPaillier(paillier).build());
        var updatedFeatureConfig = featureConfig.toBuilder().setQueryConfig(updatedQueryConfig).build();
        return platformConfig.toBuilder().setFeatureConfig(updatedFeatureConfig).build();
    }

    public static PlatformConfig defaultPlatformConfig() {
        var inputConfig = InputConfig
                .newBuilder()
                .addAllValidAverageFields(List.of(

                ))
                .addAllValidCountFields(List.of(
                ))
                .addAllValidGroupedCountFields(List.of(

                ))
                .build();

        var differentialPrivacyConfig = DifferentialPrivacyConfig
                .newBuilder()
                .setLaplace(DifferentialPrivacyConfig.Laplace
                        .newBuilder()
                        .setEpsilon(1)
                        .build())
                .build();

        var encryptionConfig = EncryptionConfig
                .newBuilder()
                .build();

        var queryConfig = QueryConfig
                .newBuilder()
                .setInputConfig(inputConfig)
                .setDifferentialPrivacyConfig(differentialPrivacyConfig)
                .setEncryptionConfig(encryptionConfig)
                .build();

        var auditableKeyExchangeConfig = AuditableKeyExchangeConfig
                .newBuilder()
                .setEnabled(false)
                .build();

        var featureConfig = FeatureConfig
                .newBuilder()
                .setQueryConfig(queryConfig)
                .setAuditableKeyExchangeConfig(auditableKeyExchangeConfig)
                .build();

        var participantConfig = ParticipantConfig
                .newBuilder()
                .addAllHospitalsConfig(HospitalConstants.hospitalConfigs())
                .build();

        return PlatformConfig
                .newBuilder()
                .setFeatureConfig(featureConfig)
                .setParticipantConfig(participantConfig)
                .build();
    }
}
