package nl.medtechchain.chaincode.encryption.homomorphic.impl.phe.paillier;

import nl.medtechchain.chaincode.encryption.homomorphic.HomomorphicEncryptInterface;
import nl.medtechchain.chaincode.encryption.homomorphic.impl.phe.paillier.ttp.PaillierTTP;
import nl.medtechchain.chaincode.subprocess.SubprocessCall;

import java.io.IOException;
import java.math.BigInteger;

public final class PaillierEncrypt implements HomomorphicEncryptInterface<PaillierEncryptionKey, PaillierPlaintext, PaillierCiphertext> {
    private static final String BINARY_PATH = "/usr/lib/encrypt";

    private final PaillierEncryptionKey encryptionKey;

    public PaillierEncrypt(PaillierBitLength bl) throws IOException {
        this.encryptionKey = PaillierTTP.getEncryptionKey(bl);
    }

    @Override
    public PaillierCiphertext encrypt(PaillierPlaintext plaintext) throws IOException {
        String command = String.format("%s encrypt --plaintext %s --ek '%s'", BINARY_PATH, plaintext, encryptionKey);
        return new PaillierCiphertext(new BigInteger(SubprocessCall.execute(command)));
    }
}