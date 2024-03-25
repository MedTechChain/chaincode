package nl.medtechchain.chaincode.device;

import org.hyperledger.fabric.contract.annotation.DataType;

@DataType(namespace = "device")
public enum DeviceType {
    WEARABLE,
    BEDSIDE_MONITOR
}
