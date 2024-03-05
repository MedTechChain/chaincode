package nl.medtechchain.chaincode;

import java.util.Objects;
import java.util.UUID;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public record Watch(@Property() UUID watchID, @Property() String firmwareVersion) {

    public Watch(@JsonProperty("watchID") final UUID watchID, @JsonProperty("firmwareVersion") final String firmwareVersion) {
        this.watchID = watchID;
        this.firmwareVersion = firmwareVersion;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Watch other = (Watch) obj;

        return Objects.deepEquals(
                new String[]{watchID().toString(), firmwareVersion()},
                new String[]{other.watchID().toString(), firmwareVersion()});
    }

    @Override
    public int hashCode() {
        return Objects.hash(watchID, firmwareVersion);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [watchID=" + watchID + ", firmwareVersion=" + firmwareVersion + "]";
    }
}
