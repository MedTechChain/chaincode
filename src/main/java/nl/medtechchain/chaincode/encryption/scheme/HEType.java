package nl.medtechchain.chaincode.encryption.scheme;

import org.hyperledger.fabric.contract.annotation.DataType;

@DataType(namespace = "encrypt")
public enum HEType {
    PAILLIER_2048,
    PAILLIER_4096,
}
