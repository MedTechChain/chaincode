package nl.medtechchain.chaincode.service.encryption.paillier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaillierEncryptResponse {
    private String ciphertext;
}
