package nl.medtechchain.chaincode.encryption.provider;

import nl.medtechchain.chaincode.encryption.scheme.HEInterface;
import nl.medtechchain.chaincode.encryption.scheme.HEType;
import nl.medtechchain.chaincode.encryption.scheme.phe.paillier.Paillier;
import nl.medtechchain.chaincode.encryption.scheme.phe.paillier.PaillierBitLength;
import nl.medtechchain.chaincode.encryption.scheme.phe.paillier.PaillierEncKey;
import nl.medtechchain.chaincode.encryption.ttp.paillier.PaillierTTPEncKeyResponse;
import nl.medtechchain.chaincode.encryption.ttp.paillier.PaillierTTPInterface;

import java.io.IOException;
import java.math.BigInteger;

public interface HEInterfaceProvider {
    void setHEType(HEType type);

    HEInterface provide() throws IOException;

    static HEInterfaceProvider instance() {
        return new HEInterfaceProviderImpl();
    }
}

class HEInterfaceProviderImpl implements HEInterfaceProvider {

    private HEType type;

    private Paillier paillier2048 = null;
    private Paillier paillier4096 = null;

    HEInterfaceProviderImpl() {
        type = HEType.PAILLIER_2048;
    }

    @Override
    public void setHEType(HEType type) {
        this.type = type;
    }

    @Override
    public HEInterface provide() throws IOException {
        switch (type) {
            case PAILLIER_2048:
                if (paillier2048 == null)
                    paillier2048 = providePaillier(PaillierBitLength.BL_2048);
                return paillier2048;
            case PAILLIER_4096:
                if (paillier4096 == null)
                    paillier4096 = providePaillier(PaillierBitLength.BL_4096);
                return paillier4096;
        }
        return null;
    }

    private Paillier providePaillier(PaillierBitLength bl) throws IOException {
        int bitLength = 2048;
        switch (bl) {
            case BL_2048:
                break;
            case BL_4096:
                bitLength = 4096;
                break;
        }

        PaillierTTPEncKeyResponse response = PaillierTTPInterface.instance().getEncryptionKey(bitLength);
        return new Paillier(new PaillierEncKey(bl, new BigInteger(response.getN())));
    }
}