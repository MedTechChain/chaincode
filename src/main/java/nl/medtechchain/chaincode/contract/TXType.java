package nl.medtechchain.chaincode.contract;

import org.hyperledger.fabric.shim.ledger.CompositeKey;

public enum TXType {
    DEVICE_DATA_ASSET,
    QUERY,
    PLATFORM_CONFIG,
    NETWORK_CONFIG;

    public CompositeKey partialKey() {
        return new CompositeKey(this.name());
    }

    public CompositeKey compositeKey(String id) {
        return new CompositeKey(this.name(), id);
    }
}
