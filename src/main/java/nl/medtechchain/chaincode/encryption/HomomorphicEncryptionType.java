package nl.medtechchain.chaincode.encryption;

import org.hyperledger.fabric.contract.annotation.DataType;

@DataType(namespace = "encrypt")
public enum HomomorphicEncryptionType {
    PAILLIER_2048,
    PAILLIER_4096,
}
