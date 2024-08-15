//package nl.medtechchain.chaincode.encryption.scheme;
//
//import nl.medtechchain.chaincode.encryption.exception.IllegalOperationException;
//import nl.medtechchain.protos.encryption.EncryptionSchemeType;
//
//import java.io.IOException;
//import java.util.Optional;
//
//public interface EncryptionScheme {
//    boolean isHomomorphic();
//
//    Plaintext plaintext(String representation) throws IllegalOperationException;
//
//    Ciphertext ciphertext(String representation) throws IllegalOperationException;
//
//    Ciphertext encrypt(Plaintext plaintext) throws IllegalOperationException, IOException;
//
//    static Optional<? extends EncryptionScheme> get(EncryptionSchemeType scheme) throws IOException {
//        return HomomorphicEncryptionInterfaceProvider.apply(scheme);
//    }
//}
//
