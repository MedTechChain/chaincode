package nl.medtechchain.chaincode;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public final class WatchTest {

    @Nested
    class Equality {

        @Test
        public void isReflexive() {
            Watch watch = new Watch(UUID.randomUUID(), "v0.0.1");

            assertThat(watch).isEqualTo(watch);
        }

        @Test
        public void isSymmetric() {
            UUID uuid = UUID.randomUUID();
            String version = "v0.0.1";
            Watch watchA = new Watch(uuid, version);
            Watch watchB = new Watch(uuid, version);

            assertThat(watchA).isEqualTo(watchB);
            assertThat(watchB).isEqualTo(watchA);
        }

        @Test
        public void isTransitive() {
            UUID uuid = UUID.randomUUID();
            String version = "v0.0.1";

            Watch watchA = new Watch(uuid, version);
            Watch watchB = new Watch(uuid, version);
            Watch watchC = new Watch(uuid, version);

            assertThat(watchA).isEqualTo(watchB);
            assertThat(watchB).isEqualTo(watchC);
            assertThat(watchA).isEqualTo(watchC);
        }

        @Test
        public void handlesInequality() {
            Watch watchA = new Watch(UUID.randomUUID(), "v0.0.1");
            Watch watchB = new Watch(UUID.randomUUID(), "v0.0.1");

            assertThat(watchA).isNotEqualTo(watchB);
        }

        @Test
        public void handlesOtherObjects() {
            Watch watchA = new Watch(UUID.randomUUID(), "v0.0.1");
            String watchB = "not a watch";

            assertThat(watchA).isNotEqualTo(watchB);
        }

        @Test
        public void handlesNull() {
            Watch watch = new Watch(UUID.randomUUID(), "v0.0.1");

            assertThat(watch).isNotEqualTo(null);
        }
    }
}
