package nl.medtechchain.chaincode.encryption.homomorphic.impl.phe.paillier;

import nl.medtechchain.chaincode.encryption.homomorphic.HomomorphicEncryptFactory;
import nl.medtechchain.chaincode.encryption.homomorphic.HomomorphicEncryptInterface;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class PaillierFacotry implements HomomorphicEncryptFactory<PaillierEncryptionKey, PaillierPlaintext, PaillierCiphertext> {
    private static final Map<PaillierBitLength, PaillierEncrypt> interfaceInstances = new HashMap<>();

    private final PaillierBitLength bl;

    public PaillierFacotry(PaillierBitLength bl) {
        this.bl = bl;
    }

    @Override
    public HomomorphicEncryptInterface<PaillierEncryptionKey, PaillierPlaintext, PaillierCiphertext> encryptInterface() throws IOException {
        if (interfaceInstances.containsKey(bl))
            return interfaceInstances.get(bl);

        interfaceInstances.put(bl, new PaillierEncrypt(bl));
        return interfaceInstances.get(bl);
    }

    @Override
    public PaillierPlaintext parsePlaintext(String s) {
        return new PaillierPlaintext(Integer.parseInt(s));
    }

    @Override
    public PaillierCiphertext parseCiphertext(String s) {
        return new PaillierCiphertext(new BigInteger(s));
    }
}
