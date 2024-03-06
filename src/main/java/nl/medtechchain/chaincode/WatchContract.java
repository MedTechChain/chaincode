package nl.medtechchain.chaincode;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Contract(name = "medtechchain", info = @Info(title = "Watch Contract", version = "0.0.1"))
@Default
public final class WatchContract implements ContractInterface {

    private final Genson genson = new Genson();

    private enum WatchErrors {
        ASSET_NOT_FOUND,
        ASSET_ALREADY_EXISTS,
        INVALID_FIRMWARE_VERSION_FORMAT
    }

    /**
     * Creates a new watch on the ledger.
     *
     * @param ctx             the transaction context
     * @param watchID         the ID of the new watch
     * @param firmwareVersion the color of the new watch
     * @return the created watch
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Watch CreateWatch(final Context ctx, final String watchID, final String firmwareVersion) {
        ChaincodeStub stub = ctx.getStub();

        if (WatchExists(ctx, watchID)) {
            String errorMessage = String.format("Watch %s already exists", watchID);
            throw new ChaincodeException(errorMessage, WatchErrors.ASSET_ALREADY_EXISTS.toString());
        }

        validateInput(firmwareVersion);

        Watch watch = new Watch(watchID, firmwareVersion);
        String sortedJson = genson.serialize(watch);
        stub.putStringState(watchID, sortedJson);

        return watch;
    }

    /**
     * Retrieves a watch with the specified ID from the ledger.
     *
     * @param ctx     the transaction context
     * @param watchID the ID of the watch
     * @return the watch found on the ledger if there was one
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Watch ReadWatch(final Context ctx, final String watchID) {
        ChaincodeStub stub = ctx.getStub();
        String watchJSON = stub.getStringState(watchID);

        if (watchJSON == null || watchJSON.isEmpty()) {
            String errorMessage = String.format("Watch %s does not exist", watchID);
            throw new ChaincodeException(errorMessage, WatchErrors.ASSET_NOT_FOUND.toString());
        }

        return genson.deserialize(watchJSON, Watch.class);
    }

    /**
     * Updates the properties of an watch on the ledger.
     *
     * @param ctx             the transaction context
     * @param watchID         the ID of the new watch
     * @param firmwareVersion the color of the new watch
     * @return the created watch
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Watch UpdateWatch(final Context ctx, final String watchID, final String firmwareVersion) {
        ChaincodeStub stub = ctx.getStub();

        if (!WatchExists(ctx, watchID)) {
            String errorMessage = String.format("Watch %s does not exist", watchID);
            throw new ChaincodeException(errorMessage, WatchErrors.ASSET_NOT_FOUND.toString());
        }

        validateInput(firmwareVersion);

        Watch newWatch = new Watch(watchID, firmwareVersion);
        String sortedJson = genson.serialize(newWatch);
        stub.putStringState(watchID, sortedJson);
        return newWatch;
    }

    /**
     * Deletes watch on the ledger.
     *
     * @param ctx     the transaction context
     * @param watchID the ID of the new watch
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteWatch(final Context ctx, final String watchID) {
        ChaincodeStub stub = ctx.getStub();

        if (!WatchExists(ctx, watchID)) {
            String errorMessage = String.format("Watch %s does not exist", watchID);
            throw new ChaincodeException(errorMessage, WatchErrors.ASSET_NOT_FOUND.toString());
        }

        stub.delState(watchID);
    }

    /**
     * Checks the existence of the watch on the ledger
     *
     * @param ctx     the transaction context
     * @param watchID the ID of the new watch
     * @return boolean indicating the existence of the boolean
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean WatchExists(final Context ctx, final String watchID) {
        ChaincodeStub stub = ctx.getStub();
        String watchJSON = stub.getStringState(watchID);

        return (watchJSON != null && !watchJSON.isEmpty());
    }

    /**
     * Count how many watches have their firmware version greater than the lower bound
     *
     * @param ctx                     the transaction context
     * @param lowBoundFirmwareVersion lower bound firmware version
     * @return int indicating how many watches have their firmware version greater than the lower bound
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public int CountFirmwareVersionGreaterEqualThan(final Context ctx, final String lowBoundFirmwareVersion) {
        ChaincodeStub stub = ctx.getStub();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        int count = 0;
        for (KeyValue result : results) {
            Watch watch = genson.deserialize(result.getStringValue(), Watch.class);
            if (watch.getFirmwareVersion().compareTo(lowBoundFirmwareVersion) >= 0)
                count++;
        }

        return count;
    }

    private void validateInput(String firmwareVersion) {
        if (!isValidVersion(firmwareVersion)) {
            String errorMessage = String.format("Firmware version %s invalid format", firmwareVersion);
            throw new ChaincodeException(errorMessage, WatchErrors.INVALID_FIRMWARE_VERSION_FORMAT.toString());
        }
    }

    private boolean isValidVersion(String version) {
        String regex = "^v\\d+\\.\\d+\\.\\d+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(version);

        return matcher.matches();
    }
}