package nl.medtechchain.chaincode.service.ttp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaillierDecryptRequest {
    private String encryptionKey;
    private String ciphertext;
}
