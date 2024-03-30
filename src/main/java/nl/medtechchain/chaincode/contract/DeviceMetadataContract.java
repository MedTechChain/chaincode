package nl.medtechchain.chaincode.contract;

import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;
import nl.medtechchain.chaincode.contract.util.ResponseUtil;
import nl.medtechchain.protos.devicemetadata.DeviceType;
import nl.medtechchain.protos.devicemetadata.EncryptedDeviceMetadata;
import nl.medtechchain.protos.devicemetadata.EncryptedPortableDeviceMetadata;
import nl.medtechchain.protos.devicemetadata.EncryptedWearableDeviceMetadata;
import nl.medtechchain.protos.query.*;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.time.Instant;
import java.util.*;

@Contract(name = "devicemetadata", info = @Info(title = "Device Metadata Contract", license = @License(name = "Apache 2.0 License", url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
public final class DeviceMetadataContract implements ContractInterface {

    private static final String INDEX_NAME = "UDI_HOSPITAL_DTYPE_TIMESTAMP";

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Chaincode.Response CreateDeviceMetadataAsset(Context ctx, String udi, DeviceType deviceType, String jsonString) {
        Hospital hospital = getHospitalFromCtx(ctx);
        if (hospital == Hospital.UNRECOGNIZED) return ResponseUtil.error("Hospital not recognized");

        EncryptedDeviceMetadata.Builder queryBuilder = EncryptedDeviceMetadata.newBuilder();
        try {
            JsonFormat.parser().merge(jsonString, queryBuilder);
        } catch (InvalidProtocolBufferException e) {
            return ResponseUtil.error("Could not deserialize");
        }
        EncryptedDeviceMetadata md = queryBuilder.build();

        CompositeKey key = compositeKey(ctx, udi, hospital, deviceType, Instant.now().getEpochSecond());
        ctx.getStub().putState(key.toString(), md.getRawBytes().toByteArray());
        return ResponseUtil.success();
    }


    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Chaincode.Response Query(Context ctx, String jsonString) {
        try {
            Query.Builder queryBuilder = Query.newBuilder();
            JsonFormat.parser().merge(jsonString, queryBuilder);
            Query query = queryBuilder.build();

            switch (query.getQueryType()) {
                case COUNT:
                    return count(ctx, query);
                case COUNT_ALL:
                    return countAll(ctx, query);
                case AVERAGE:
                    return average(ctx, query);
                case UNRECOGNIZED:
                    return ResponseUtil.error("");
            }
            return ResponseUtils.newSuccessResponse();
        } catch (InvalidProtocolBufferException e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    private Chaincode.Response count(Context ctx, Query query) {
        if (!query.hasStartTime() || !query.hasStopTime())
            return ResponseUtil.error("Malformed query: Time period required!");

        var validFields = List.of("medical_speciality", "manufacturer_name", "operating_system");
        if (query.getField().isEmpty() || !validFields.contains(query.getField()))
            return ResponseUtil.error("Malformed query: Invalid field: " + query.getField());

        if (query.hasValue()) return ResponseUtil.error("Malformed query: Value specified!");

        var typeFilter = deviceTypeListFilter(query);
        var hospitalFilter = hospitalListFilter(query);

        var assets = retrieveData(ctx, query.getStartTime(), query.getStopTime(), typeFilter, hospitalFilter);

        if (assets.isPresent()) {
            int result = 0;

            for (EncryptedDeviceMetadata asset : assets.get()) {
                try {
                    Descriptors.FieldDescriptor fieldDescriptor;
                    String fieldValue;
                    switch (asset.getType()) {
                        case WEARABLE_DEVICE:
                            EncryptedWearableDeviceMetadata mdw = EncryptedWearableDeviceMetadata.parseFrom(asset.getRawBytes());
                            fieldDescriptor = mdw.getDescriptorForType().findFieldByName(query.getField());
                            fieldValue = (String) asset.getField(fieldDescriptor);
                            if (!filter(query.getFilterList(), mdw) && Objects.equals(fieldValue, query.getValue()))
                                result++;
                            break;
                        case PORTABLE_DEVICE:
                            EncryptedPortableDeviceMetadata mdp = EncryptedPortableDeviceMetadata.parseFrom(asset.getRawBytes());
                            fieldDescriptor = mdp.getDescriptorForType().findFieldByName(query.getField());
                            fieldValue = (String) asset.getField(fieldDescriptor);
                            if (!filter(query.getFilterList(), mdp) && Objects.equals(fieldValue, query.getValue()))
                                result++;
                            break;
                    }
                } catch (Throwable t) {
                    System.out.println(t.getMessage());
                }
            }

            return ResponseUtil.success(QueryResultType.COUNT_RESULT.name(), CountResult.newBuilder().setResult(result).build().toByteArray());
        } else return ResponseUtil.error("Error: Could not deserialize data");
    }

    private Chaincode.Response countAll(Context ctx, Query query) {
        if (!query.hasStartTime() || !query.hasStopTime())
            return ResponseUtil.error("Malformed query: Time period required!");

        var validFields = List.of("medical_speciality", "manufacturer_name", "operating_system");
        if (query.getField().isEmpty() || !validFields.contains(query.getField()))
            return ResponseUtil.error("Malformed query: Invalid field: " + query.getField());

        if (query.hasValue()) return ResponseUtil.error("Malformed query: Value specified!");

        var typeFilter = deviceTypeListFilter(query);
        var hospitalFilter = hospitalListFilter(query);

        var assets = retrieveData(ctx, query.getStartTime(), query.getStopTime(), typeFilter, hospitalFilter);

        if (assets.isPresent()) {
            Map<String, Integer> result = new HashMap<>();

            for (EncryptedDeviceMetadata asset : assets.get()) {
                try {
                    Descriptors.FieldDescriptor fieldDescriptor;
                    String fieldValue;
                    switch (asset.getType()) {
                        case WEARABLE_DEVICE:
                            EncryptedWearableDeviceMetadata mdw = EncryptedWearableDeviceMetadata.parseFrom(asset.getRawBytes());
                            fieldDescriptor = mdw.getDescriptorForType().findFieldByName(query.getField());
                            fieldValue = (String) asset.getField(fieldDescriptor);
                            if (filter(query.getFilterList(), mdw)) continue;

                            if (!result.containsKey(fieldValue)) result.put(fieldValue, 0);
                            break;
                        case PORTABLE_DEVICE:
                            EncryptedPortableDeviceMetadata mdp = EncryptedPortableDeviceMetadata.parseFrom(asset.getRawBytes());
                            fieldDescriptor = mdp.getDescriptorForType().findFieldByName(query.getField());
                            fieldValue = (String) asset.getField(fieldDescriptor);
                            if (filter(query.getFilterList(), mdp)) continue;

                            if (!result.containsKey(fieldValue)) result.put(fieldValue, 0);
                            break;
                    }
                } catch (Throwable t) {
                    System.out.println(t.getMessage());
                }
            }

            return ResponseUtil.success(QueryResultType.COUNT_RESULT.name(), CountAllResult.newBuilder().putAllResult(result).build().toByteArray());
        } else return ResponseUtil.error("Error: Could not deserialize data");
    }

    private boolean filter(FilterList fl, EncryptedWearableDeviceMetadata mdw) {
        for (Filter f : fl.getFiltersList()) {
            Descriptors.FieldDescriptor fieldDescriptor = mdw.getDescriptorForType().findFieldByName(f.getField());
            String fieldValue = (String) mdw.getField(fieldDescriptor);
            if (!Objects.equals(fieldValue, f.getValue())) {
                return true;
            }
        }
        return false;
    }

    private boolean filter(FilterList fl, EncryptedPortableDeviceMetadata mdp) {
        for (Filter f : fl.getFiltersList()) {
            Descriptors.FieldDescriptor fieldDescriptor = mdp.getDescriptorForType().findFieldByName(f.getField());
            String fieldValue = (String) mdp.getField(fieldDescriptor);
            if (!Objects.equals(fieldValue, f.getValue())) {
                return true;
            }
        }
        return false;
    }

    private Chaincode.Response average(Context ctx, Query query) {
        if (!query.hasStartTime() || !query.hasStopTime())
            return ResponseUtil.error("Malformed query: Time period required!");

        var validFields = List.of("aquired_price", "rental_price");
        if (query.getField().isEmpty() || !validFields.contains(query.getField()))
            return ResponseUtil.error("Malformed query: Invalid field: " + query.getField());

        var typeFilter = deviceTypeListFilter(query);
        var hospitalFilter = hospitalListFilter(query);

        if (typeFilter.size() > 1)
            return ResponseUtil.error("Malformed query: Averaging a field makes sense only for a single type of device!");


        var assets = retrieveData(ctx, query.getStartTime(), query.getStopTime(), typeFilter, hospitalFilter);

        if (assets.isPresent()) {
            double result = 0;
            int count = 0;

            double aux = 0;
            for (EncryptedDeviceMetadata asset : assets.get()) {
                try {
                    Descriptors.FieldDescriptor fieldDescriptor;
                    String fieldValue;
                    switch (asset.getType()) {
                        case WEARABLE_DEVICE:
                            EncryptedWearableDeviceMetadata mdw = EncryptedWearableDeviceMetadata.parseFrom(asset.getRawBytes());
                            fieldDescriptor = mdw.getDescriptorForType().findFieldByName(query.getField());
                            fieldValue = (String) asset.getField(fieldDescriptor);
                            if (filter(query.getFilterList(), mdw)) continue;

                            try {
                                aux = Double.parseDouble(fieldValue);
                                count++;
                            } catch (Throwable t) {
                                aux = 0;
                            }
                            break;
                        case PORTABLE_DEVICE:
                            EncryptedPortableDeviceMetadata mdp = EncryptedPortableDeviceMetadata.parseFrom(asset.getRawBytes());
                            fieldDescriptor = mdp.getDescriptorForType().findFieldByName(query.getField());
                            fieldValue = (String) asset.getField(fieldDescriptor);
                            if (filter(query.getFilterList(), mdp)) continue;

                            try {
                                aux = Double.parseDouble(fieldValue);
                                count++;
                            } catch (Throwable t) {
                                aux = 0;
                            }
                            break;
                    }

                    result += aux;
                } catch (Throwable t) {
                    System.out.println(t.getMessage());
                }
            }

            if (count == 0)
                return ResponseUtil.success(QueryResultType.COUNT_RESULT.name(), AverageResult.newBuilder().setResult(0).build().toByteArray());

            return ResponseUtil.success(QueryResultType.COUNT_RESULT.name(), AverageResult.newBuilder().setResult(result / count).build().toByteArray());
        } else return ResponseUtil.error("Error: Could not deserialize data");

    }

    private List<DeviceType> deviceTypeListFilter(Query query) {
        if (query.hasDeviceType()) return List.of(query.getDeviceType());
        return Arrays.asList(DeviceType.values());
    }

    private List<Hospital> hospitalListFilter(Query query) {
        if (query.hasHospitalList()) return query.getHospitalList().getHospitalsList();
        return List.of(Hospital.values());
    }

    private Optional<List<EncryptedDeviceMetadata>> retrieveData(Context ctx, Timestamp startTime, Timestamp stopTime, List<DeviceType> types, List<Hospital> hospitals) {
        ChaincodeStub stub = ctx.getStub();

        QueryResultsIterator<KeyValue> iterator = stub.getStateByRange("", "");

        List<EncryptedDeviceMetadata> result = new ArrayList<>();

        for (KeyValue kv : iterator) {
            EncryptedDeviceMetadata asset;
            try {
                asset = EncryptedDeviceMetadata.parseFrom(kv.getValue());

                CompositeKey compositeKey = ctx.getStub().splitCompositeKey(kv.getKey());

                var assetHospital = Hospital.valueOf(compositeKey.getAttributes().get(1));
                var assetDeviceType = DeviceType.valueOf(compositeKey.getAttributes().get(2));
                var assetTimestamp = Long.getLong(compositeKey.getAttributes().get(3));

                if (startTime.getSeconds() < assetTimestamp && assetTimestamp < stopTime.getSeconds() && types.contains(assetDeviceType) && hospitals.contains(assetHospital))
                    result.add(asset);

            } catch (InvalidProtocolBufferException e) {
                System.out.println(e.getMessage());
                return Optional.empty();
            }
        }

        return Optional.of(result);
    }

    private Hospital getHospitalFromCtx(Context ctx) {
        String mspID = ctx.getClientIdentity().getMSPID().toUpperCase();
        for (Hospital hospital : Hospital.values()) {
            if (mspID.contains(hospital.name())) return hospital;
        }
        return Hospital.UNRECOGNIZED;
    }

    private CompositeKey compositeKey(Context ctx, String udi, Hospital hospital, DeviceType deviceType, long epochSeconds) {
        return ctx.getStub().createCompositeKey(INDEX_NAME, udi, hospital.name(), deviceType.name(), epochSeconds + "");
    }
}