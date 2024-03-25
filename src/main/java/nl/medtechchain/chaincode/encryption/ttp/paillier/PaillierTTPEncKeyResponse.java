package nl.medtechchain.chaincode.encryption.ttp.paillier;

public class PaillierTTPEncKeyResponse {
    private String N;

    public PaillierTTPEncKeyResponse(String N) {
        this.N = N;
    }

    public String getN() {
        return N;
    }

    public void setN(String N) {
        this.N = N;
    }
}
