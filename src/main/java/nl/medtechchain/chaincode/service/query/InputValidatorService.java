package nl.medtechchain.chaincode.service.query;

import com.google.protobuf.Descriptors;
import nl.medtechchain.chaincode.util.ConfigUtil;
import nl.medtechchain.proto.common.ChaincodeError;
import nl.medtechchain.proto.devicedata.DeviceDataAsset;
import nl.medtechchain.proto.query.Filter;
import nl.medtechchain.proto.query.Query;

import java.util.ArrayList;
import java.util.Optional;

public class InputValidatorService {

    public Optional<ChaincodeError> validateQueryFilters(Query tx) {
        var fields = new ArrayList<Descriptors.FieldDescriptor>();
        fields.addAll(DeviceDataAsset.PlainDeviceData.getDescriptor().getFields());
        fields.addAll(DeviceDataAsset.SensitiveDeviceData.getDescriptor().getFields());

        for (Filter filter : tx.getFiltersList()) {
            var descriptor = fields.stream().filter(f -> filter.getField().equals(f.getName())).findAny();

            if (descriptor.isEmpty())
                return error("Bad query", "Unknown filter field: " + filter.getField());

            var invalidFilter = error("Bad query", "Invalid filter - comparator did not match field type: " + filter.getField());

            switch (filter.getComparatorCase()) {
                case STRING_FILTER:
                    if (descriptor.get().getType() != Descriptors.FieldDescriptor.Type.STRING && descriptor.get().getType() != Descriptors.FieldDescriptor.Type.ENUM)
                        return invalidFilter;
                    break;
                case INT_FILTER:
                    if (descriptor.get().getType() != Descriptors.FieldDescriptor.Type.INT32 && descriptor.get().getType() != Descriptors.FieldDescriptor.Type.INT64)
                        return invalidFilter;
                    break;
                case BOOL_FILTER:
                    if (descriptor.get().getType() != Descriptors.FieldDescriptor.Type.BOOL)
                        return invalidFilter;
                    break;
                case TIMESTAMP_FILTER:
                    if (descriptor.get().getType() != Descriptors.FieldDescriptor.Type.MESSAGE)
                        return invalidFilter;

                    if (!descriptor.get().getMessageType().getFullName().equals("google.protobuf.Timestamp"))
                        return invalidFilter;
                    break;
                case COMPARATOR_NOT_SET:
                    return error("Bad query", "Comparator for filter not set: " + filter.getField());
            }
        }

        return Optional.empty();
    }

    public boolean validateEncyptionConfig(DeviceDataAsset asset) {
        var currentEncryptionVersion = ConfigUtil.encryptionVersion();
        boolean encryptionEnabled = currentEncryptionVersion.isPresent();
        return !(!asset.hasEncryptionVersion() && encryptionEnabled ||
                asset.hasEncryptionVersion() && !encryptionEnabled ||
                asset.hasEncryptionVersion() && !asset.getEncryptionVersion().equals(currentEncryptionVersion.get()));
    }

    private Optional<ChaincodeError> error(String message, String details) {
        return Optional.of(ChaincodeError.newBuilder().setCode(ChaincodeError.ErrorCode.ERROR_CODE_INVALID_TRANSACTION).setMessage(message).setDetails(details).build());
    }
}
