package nl.medtechchain.chaincode.encryption.scheme;

public enum BitLength {
    BL_2048(2048),
    BL_3072(3072);

    private final int value;

    BitLength(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
