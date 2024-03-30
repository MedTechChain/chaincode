package nl.medtechchain.chaincode.encryption.scheme;

import nl.medtechchain.chaincode.encryption.scheme.phe.paillier.Paillier;
import nl.medtechchain.protos.encryption.EncryptionSchemeType;

import java.io.IOException;
import java.util.Optional;

class HomomorphicEncryptionInterfaceProvider {

    public static Optional<? extends EncryptionScheme> apply(EncryptionSchemeType scheme) throws IOException {
        switch (scheme) {
            case PAILLIER_2048:
                return providePaillier(BitLength.BL_2048);
            case PAILLIER_3072:
                return providePaillier(BitLength.BL_3072);
            default:
                return Optional.empty();
        }
    }


    private static Optional<Paillier> providePaillier(BitLength bl) throws IOException {
        return null;
    }
}