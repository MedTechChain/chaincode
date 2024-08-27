package nl.medtechchain.chaincode.service.encryption;

public interface HomomorphicEncryptionScheme {
    String add(String c1, String c2);

    String mulCt(String ct, String c);

    String mul(String c1, String c2);
}

