package nl.medtechchain.chaincode.contract;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;

import java.time.Instant;

@Contract(name = "devicedata", info = @Info(title = "Device Data Contract", license = @License(name = "Apache 2.0 License", url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
public final class DeviceDataContract implements ContractInterface {

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public boolean CreateDeviceMetadataAsset(Context ctx) {
        ctx.getStub().putStringState("test", Instant.now().toString());
        return true;
    }

//    private static final String INDEX_NAME = "UDI_HOSPITAL_DTYPE_TIMESTAMP";
//
//    private static final Logger logger = Logger.getLogger(DeviceMetadataContract.class);
//
//    private static boolean DP = false;

//    @Transaction(intent = Transaction.TYPE.SUBMIT)
//    public boolean CreateDeviceMetadataAsset(Context ctx, String udi, String deviceType, String jsonString) {
//        Hospital hospital = getHospitalFromCtx(ctx);
//        if (hospital == Hospital.UNRECOGNIZED) return false;
//
//        EncryptedDeviceMetadata.Builder queryBuilder = EncryptedDeviceMetadata.newBuilder();
//        try {
//            JsonFormat.parser().merge(jsonString, queryBuilder);
//        } catch (InvalidProtocolBufferException e) {
//            return false;
//        }
//        EncryptedDeviceMetadata md = queryBuilder.build();
//
//        CompositeKey key = compositeKey(ctx, udi, hospital, DeviceType.valueOf(deviceType), Instant.now().getEpochSecond());
//        ctx.getStub().putState(key.toString(), md.toByteArray());
//        return true;
//    }
//
//    @Transaction(intent = Transaction.TYPE.EVALUATE)
//    public String Query(Context ctx, String jsonString) {
//        try {
//            Query.Builder queryBuilder = Query.newBuilder();
//            JsonFormat.parser().merge(jsonString, queryBuilder);
//            Query query = queryBuilder.build();
//
//            logger.info("Performing the " + query.getQueryType().name() + " query");
//            long startTime = System.nanoTime();
//            String result = ResponseUtil.error("Unrecognized query");
//            switch (query.getQueryType()) {
//                case COUNT:
//                    result = count(ctx, query);
//                    break;
//                case COUNT_ALL:
//                    result = countAll(ctx, query);
//                    break;
//                case AVERAGE:
//                    result = average(ctx, query);
//                    break;
//            }
//            DP = !DP;
//            long endTime = System.nanoTime();
//            long executionTime = endTime - startTime;
//            logger.info(DP + " " + executionTime);
//            executionTime = 0;
//            for (int i = 0; i < 5000; i++) {
//                var noise = new LaplaceNoise();
//                var rr = 1;
//                startTime = System.nanoTime();
//                rr = Math.abs((int) noise.addNoise(rr, 1, 0.25, 0));
//                endTime = System.nanoTime();
//                executionTime += endTime - startTime;
//            }
//            logger.info("DP: " + (executionTime * 1.0) / 5000);
//            return result;
//        } catch (InvalidProtocolBufferException e) {
//            return ResponseUtil.error(e.getMessage());
//        }
//    }
//
//    private String count(Context ctx, Query query) {
//        if (!query.hasStartTime() || !query.hasStopTime())
//            return ResponseUtil.error("Malformed query: Time period required!");
//
//        if (!query.getField().isEmpty())
//            return ResponseUtil.error("Malformed query: Field specified... should use filters for counting: " + query.getField());
//
//        var typeFilter = deviceTypeListFilter(query);
//        var hospitalFilter = hospitalListFilter(query);
//
//        var assets = retrieveData(ctx, query.getStartTime(), query.getStopTime(), typeFilter, hospitalFilter);
//
//        int result = 0;
//
//        logger.info("Assets present, counting for " + assets.size() + " assets");
//        for (EncryptedDeviceMetadata asset : assets) {
//            try {
//                switch (asset.getType()) {
//                    case WEARABLE_DEVICE:
//                        EncryptedWearableDeviceMetadata mdw = EncryptedWearableDeviceMetadata.parseFrom(asset.getRawBytes());
//                        if (filter(query.getFilterList(), mdw)) continue;
//                        result++;
//                        break;
//                    case PORTABLE_DEVICE:
//                        EncryptedPortableDeviceMetadata mdp = EncryptedPortableDeviceMetadata.parseFrom(asset.getRawBytes());
//                        if (filter(query.getFilterList(), mdp)) continue;
//                        result++;
//                        break;
//                }
//            } catch (Throwable t) {
//                logger.error("Error: " + t.getMessage());
//            }
//        }
//
//
//        if(DP) {
//            var noise = new LaplaceNoise();
//            result = Math.abs((int) noise.addNoise(result, 1, 0.25, 0));
//        }
//
//        try {
//            return JsonFormat.printer().includingDefaultValueFields().print(CountResult.newBuilder().setResult(result).build());
//        } catch (InvalidProtocolBufferException e) {
//            return ResponseUtil.error("Error: Could not serialize data");
//        }
//    }
//
//    private String countAll(Context ctx, Query query) {
//        if (!query.hasStartTime() || !query.hasStopTime())
//            return ResponseUtil.error("Malformed query: Time period required!");
//
//        var validFields = List.of("medical_speciality", "manufacturer_name", "operating_system");
//        if (query.getField().isEmpty() || !validFields.contains(query.getField()))
//            return ResponseUtil.error("Malformed query: Invalid field: " + query.getField());
//
//        if (query.hasValue()) return ResponseUtil.error("Malformed query: Value specified!");
//
//        var typeFilter = deviceTypeListFilter(query);
//        var hospitalFilter = hospitalListFilter(query);
//
//        var assets = retrieveData(ctx, query.getStartTime(), query.getStopTime(), typeFilter, hospitalFilter);
//        Map<String, Integer> result = new HashMap<>();
//
//        logger.info("Assets present, counting all for " + assets.size() + " assets");
//        for (EncryptedDeviceMetadata asset : assets) {
//            try {
//                Descriptors.FieldDescriptor fieldDescriptor;
//                String fieldValue;
//                switch (asset.getType()) {
//                    case WEARABLE_DEVICE:
//                        EncryptedWearableDeviceMetadata mdw = EncryptedWearableDeviceMetadata.parseFrom(asset.getRawBytes());
//                        fieldDescriptor = mdw.getDescriptorForType().findFieldByName(query.getField());
//                        fieldValue = (String) mdw.getField(fieldDescriptor);
//                        if (filter(query.getFilterList(), mdw)) continue;
//                        result.put(fieldValue, result.getOrDefault(fieldValue, 0) + 1);
//                        break;
//                    case PORTABLE_DEVICE:
//                        EncryptedPortableDeviceMetadata mdp = EncryptedPortableDeviceMetadata.parseFrom(asset.getRawBytes());
//                        fieldDescriptor = mdp.getDescriptorForType().findFieldByName(query.getField());
//                        fieldValue = (String) mdp.getField(fieldDescriptor);
//                        if (filter(query.getFilterList(), mdp)) continue;
//                        result.put(fieldValue, result.getOrDefault(fieldValue, 0) + 1);
//                        break;
//                }
//            } catch (Throwable t) {
//                logger.error("Error: " + t.getMessage());
//            }
//        }
//
//        if(DP) {
//            var noise = new LaplaceNoise();
//            result.replaceAll((k, v) -> Math.abs((int) noise.addNoise(v, 1, 0.4, 0)));
//        }
//
//        try {
//            return JsonFormat.printer().includingDefaultValueFields().print(CountAllResult.newBuilder().putAllResult(result).build());
//        } catch (InvalidProtocolBufferException e) {
//            return ResponseUtil.error("Error: Could not serialize data");
//        }
//    }
//
//    private String average(Context ctx, Query query) {
//        if (!query.hasStartTime() || !query.hasStopTime())
//            return ResponseUtil.error("Malformed query: Time period required!");
//
//        var validFields = List.of("aquired_price", "rental_price");
//        if (query.getField().isEmpty() || !validFields.contains(query.getField()))
//            return ResponseUtil.error("Malformed query: Invalid field: " + query.getField());
//
//        if (query.hasValue()) return ResponseUtil.error("Malformed query: Value specified!");
//
//        var typeFilter = deviceTypeListFilter(query);
//        var hospitalFilter = hospitalListFilter(query);
//
//        if (typeFilter.size() > 1)
//            return ResponseUtil.error("Malformed query: Averaging a field makes sense only for a single type of device!");
//
//        var assets = retrieveData(ctx, query.getStartTime(), query.getStopTime(), typeFilter, hospitalFilter);
//
//        double result = 0;
//        int count = 0;
//
//        double aux = 0;
//        logger.info("Assets present, computing average for " + assets.size() + " assets");
//        for (EncryptedDeviceMetadata asset : assets) {
//            try {
//                Descriptors.FieldDescriptor fieldDescriptor;
//                String fieldValue = "0";
//                switch (asset.getType()) {
//                    case WEARABLE_DEVICE:
//                        EncryptedWearableDeviceMetadata mdw = EncryptedWearableDeviceMetadata.parseFrom(asset.getRawBytes());
//                        fieldDescriptor = mdw.getDescriptorForType().findFieldByName(query.getField());
//                        fieldValue = (String) mdw.getField(fieldDescriptor);
//                        if (filter(query.getFilterList(), mdw)) continue;
//                        break;
//                    case PORTABLE_DEVICE:
//                        EncryptedPortableDeviceMetadata mdp = EncryptedPortableDeviceMetadata.parseFrom(asset.getRawBytes());
//                        fieldDescriptor = mdp.getDescriptorForType().findFieldByName(query.getField());
//                        fieldValue = (String) mdp.getField(fieldDescriptor);
//                        if (filter(query.getFilterList(), mdp)) continue;
//                        break;
//                }
//
//                try {
//                    result += Double.parseDouble(fieldValue);
//                    count++;
//                } catch (Throwable ignored) {
//                }
//            } catch (Throwable t) {
//                logger.error("Error: " + t.getMessage());
//            }
//        }
//
//        if(DP) {
//            var noise = new LaplaceNoise();
//            result = Math.abs((int) noise.addNoise(result, 1, 0.1, 0));
//        }
//
//        if (count == 0) result = 0;
//        else result /= count;
//
//        try {
//            return JsonFormat.printer().includingDefaultValueFields().print(AverageResult.newBuilder().setResult(result).build());
//        } catch (InvalidProtocolBufferException e) {
//            return ResponseUtil.error("Error: Could not serialize data");
//        }
//    }
//
//    private <T extends GeneratedMessageV3> boolean filter(FilterList fl, T obj) {
//        for (Filter f : fl.getFiltersList()) {
//            Descriptors.FieldDescriptor fieldDescriptor = obj.getDescriptorForType().findFieldByName(f.getField());
//            String fieldValue = (String) obj.getField(fieldDescriptor);
//            if (!Objects.equals(fieldValue, f.getValue())) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private List<DeviceType> deviceTypeListFilter(Query query) {
//        if (query.hasDeviceType()) return List.of(query.getDeviceType());
//        return Arrays.asList(DeviceType.values());
//    }
//
//    private List<Hospital> hospitalListFilter(Query query) {
//        if (query.hasHospitalList()) return query.getHospitalList().getHospitalsList();
//        return List.of(Hospital.values());
//    }
//
//    private List<EncryptedDeviceMetadata> retrieveData(Context ctx, Timestamp startTime, Timestamp stopTime, List<DeviceType> types, List<Hospital> hospitals) {
//        ChaincodeStub stub = ctx.getStub();
//
//        QueryResultsIterator<KeyValue> iterator = stub.getStateByPartialCompositeKey(ctx.getStub().createCompositeKey(INDEX_NAME));
//
//        logger.info("Is iterator empty: " + !iterator.iterator().hasNext());
//
//        List<EncryptedDeviceMetadata> result = new ArrayList<>();
//
//        for (KeyValue kv : iterator) {
//            try {
//                CompositeKey compositeKey = ctx.getStub().splitCompositeKey(kv.getKey());
//
//                var assetHospital = Hospital.valueOf(compositeKey.getAttributes().get(1));
//                var assetDeviceType = DeviceType.valueOf(compositeKey.getAttributes().get(2));
//                var assetTimestamp = Long.parseLong(compositeKey.getAttributes().get(3));
//
//                EncryptedDeviceMetadata asset = EncryptedDeviceMetadata.parseFrom(kv.getValue());
//
//                if (startTime.getSeconds() < assetTimestamp && assetTimestamp < stopTime.getSeconds() && types.contains(assetDeviceType) && hospitals.contains(assetHospital)) {
//                    result.add(asset);
//                }
//            } catch (InvalidProtocolBufferException e) {
//                logger.error("Error: " + e.getMessage());
//            }
//        }
//
//        return result;
//    }
//
//    private Hospital getHospitalFromCtx(Context ctx) {
//        String mspID = ctx.getClientIdentity().getMSPID().toUpperCase();
//        for (Hospital hospital : Hospital.values()) {
//            if (mspID.contains(hospital.name())) return hospital;
//        }
//        return Hospital.UNRECOGNIZED;
//    }
//
//    private CompositeKey compositeKey(Context ctx, String udi, Hospital hospital, DeviceType deviceType, long epochSeconds) {
//        return ctx.getStub().createCompositeKey(INDEX_NAME, udi, hospital.name(), deviceType.name(), epochSeconds + "");
//    }
}