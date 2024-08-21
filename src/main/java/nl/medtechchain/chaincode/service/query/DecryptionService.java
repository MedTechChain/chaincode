package nl.medtechchain.chaincode.service.query;

import nl.medtechchain.chaincode.service.ttp.PaillierTTPService;
import nl.medtechchain.chaincode.service.ttp.dto.PaillierDecryptRequest;
import nl.medtechchain.chaincode.util.ConfigUtil;

import java.util.Optional;

import static nl.medtechchain.chaincode.util.ConfigUtil.encryptionConfig;

public class DecryptionService {

    public Optional<String> decrypt(String value) {
        switch (encryptionConfig().getSchemeCase()) {
            case PAILLIER:
                return decryptPaillier(value);
            default:
                return Optional.empty();
        }
    }

    private Optional<String> decryptPaillier(String value) {
        var ttp = new PaillierTTPService(ConfigUtil.EncryptionConstants.TTP_ADDRESS);
        return ttp.decrypt(new PaillierDecryptRequest(encryptionConfig().getPaillier().getPublicKey(), value));
    }

}
