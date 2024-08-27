package nl.medtechchain.chaincode.config;

import com.google.protobuf.Timestamp;
import nl.medtechchain.proto.config.NetworkConfig;
import nl.medtechchain.proto.config.PlatformConfig;
import nl.medtechchain.proto.config.UpdatePlatformConfig;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ConfigOps {

    public static class NetworkConfigOps {
        public static NetworkConfig create(List<NetworkConfig.HospitalConfig> configs) {
            return NetworkConfig.newBuilder()
                    .setTimestamp(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                    .setId(computeNetworkConfigId(configs))
                    .addAllList(configs)
                    .build();
        }

        public static NetworkConfig update(NetworkConfig networkConfig, String name, List<NetworkConfig.HospitalConfig.Entry> update) {
            var configs = new ArrayList<>(networkConfig.getListList());
            var hospitalConfig = configs.stream().filter(h -> h.getName().equals(name)).findAny();
            if (hospitalConfig.isPresent()) {
                configs.removeIf(c -> c.getName().equals(name));

                var list = new ArrayList<>(hospitalConfig.get().getMapList());
                for (NetworkConfig.HospitalConfig.Entry entry : update) {
                    list.removeIf(e -> e.getKey() == entry.getKey());
                    list.add(entry);
                }
                configs.add(NetworkConfig.HospitalConfig.newBuilder().setName(name).addAllMap(list).build());

            } else
                configs.add(NetworkConfig.HospitalConfig.newBuilder().setName(name).addAllMap(update).build());

            return create(configs);
        }

        private static String computeNetworkConfigId(List<NetworkConfig.HospitalConfig> configs) {
            return UUID.nameUUIDFromBytes(NetworkConfig.newBuilder().addAllList(configs).build().toByteArray()).toString();
        }
    }

    public static class PlatformConfigOps {
        public static Optional<String> get(PlatformConfig platformConfig, nl.medtechchain.proto.config.PlatformConfig.Config property) {
            return platformConfig.getMapList().stream().filter(e -> e.getKey().equals(property)).map(PlatformConfig.Entry::getValue).findFirst();
        }

        public static String getUnsafe(PlatformConfig platformConfig, nl.medtechchain.proto.config.PlatformConfig.Config property) {
            var o = get(platformConfig, property);
            if (o.isPresent())
                return o.get();

            throw new ConfigException("Config property not set: " + property.name());
        }

        public static PlatformConfig create(List<PlatformConfig.Entry> configs) {
            return PlatformConfig.newBuilder()
                    .setTimestamp(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                    .setId(computePlatformConfigId(configs))
                    .addAllMap(configs)
                    .build();
        }

        public static PlatformConfig update(PlatformConfig platformConfig, List<PlatformConfig.Entry> update) {
            var list = new ArrayList<>(platformConfig.getMapList());
            for (PlatformConfig.Entry entry : update) {
                list.removeIf(e -> e.getKey() == entry.getKey());
                list.add(entry);
            }
            return create(list);
        }

        private static String computePlatformConfigId(List<PlatformConfig.Entry> configs) {
            return UUID.nameUUIDFromBytes(UpdatePlatformConfig.newBuilder().addAllMap(configs).build().toByteArray()).toString();
        }
    }
}
