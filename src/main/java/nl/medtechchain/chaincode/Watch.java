package nl.medtechchain.chaincode;

import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;


//TODO : Remove this
@DataType()
public final class Watch {

    @Property()
    private final String watchID;

    public String getWatchID() {
        return watchID;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    @Property()
    private final String firmwareVersion;

    public Watch(@JsonProperty("watchID") final String watchID, @JsonProperty("firmwareVersion") final String firmwareVersion) {
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
                new String[]{watchID.toString(), firmwareVersion},
                new String[]{other.watchID.toString(), firmwareVersion});
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
