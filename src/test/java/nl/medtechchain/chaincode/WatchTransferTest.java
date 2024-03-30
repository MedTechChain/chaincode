//package nl.medtechchain.chaincode;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.ThrowableAssert.catchThrowable;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verifyNoInteractions;
//import static org.mockito.Mockito.when;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.UUID;
//
//import org.hyperledger.fabric.contract.Context;
//import org.hyperledger.fabric.shim.ChaincodeException;
//import org.hyperledger.fabric.shim.ChaincodeStub;
//import org.hyperledger.fabric.shim.ledger.KeyValue;
//import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//
//public final class WatchTransferTest {
//
//    public static class MockKeyValue implements KeyValue {
//        private final String key;
//        private final String value;
//
//        public MockKeyValue(String key, String value) {
//            this.key = key;
//            this.value = value;
//        }
//
//        @Override
//        public String getKey() {
//            return key;
//        }
//
//        @Override
//        public String getStringValue() {
//            return value;
//        }
//
//        @Override
//        public byte[] getValue() {
//            return value.getBytes();
//        }
//    }
//
//    private static final class MockAssetResultsIterator implements QueryResultsIterator<KeyValue> {
//
//        private final List<KeyValue> assetList;
//
//        MockAssetResultsIterator() {
//            super();
//
//            assetList = new ArrayList<>();
//
//            UUID uuid = UUID.randomUUID();
//            assetList.add(new MockKeyValue(uuid.toString(), String.format("{ \"watchID\": \"%s\", \"firmwareVersion\": \"%s\" }", uuid, "v0.0.1")));
//
//            uuid = UUID.randomUUID();
//            assetList.add(new MockKeyValue(uuid.toString(), String.format("{ \"watchID\": \"%s\", \"firmwareVersion\": \"%s\" }", uuid, "v0.0.1")));
//
//            uuid = UUID.randomUUID();
//            assetList.add(new MockKeyValue(uuid.toString(), String.format("{ \"watchID\": \"%s\", \"firmwareVersion\": \"%s\" }", uuid, "v0.0.2")));
//
//            uuid = UUID.randomUUID();
//            assetList.add(new MockKeyValue(uuid.toString(), String.format("{ \"watchID\": \"%s\", \"firmwareVersion\": \"%s\" }", uuid, "v0.0.2")));
//
//            uuid = UUID.randomUUID();
//            assetList.add(new MockKeyValue(uuid.toString(), String.format("{ \"watchID\": \"%s\", \"firmwareVersion\": \"%s\" }", uuid, "v0.0.2")));
//
//            uuid = UUID.randomUUID();
//            assetList.add(new MockKeyValue(uuid.toString(), String.format("{ \"watchID\": \"%s\", \"firmwareVersion\": \"%s\" }", uuid, "v0.0.3")));
//
//            uuid = UUID.randomUUID();
//            assetList.add(new MockKeyValue(uuid.toString(), String.format("{ \"watchID\": \"%s\", \"firmwareVersion\": \"%s\" }", uuid, "v0.0.4")));
//
//            uuid = UUID.randomUUID();
//            assetList.add(new MockKeyValue(uuid.toString(), String.format("{ \"watchID\": \"%s\", \"firmwareVersion\": \"%s\" }", uuid, "v0.0.4")));
//        }
//
//        @Override
//        public Iterator<KeyValue> iterator() {
//            return assetList.iterator();
//        }
//
//        @Override
//        public void close() {
//            // do nothing
//        }
//
//    }
//
//    @Test
//    public void invokeUnknownTransaction() {
//        WatchContract contract = new WatchContract();
//        Context ctx = mock(Context.class);
//
//        Throwable thrown = catchThrowable(() -> contract.unknownTransaction(ctx));
//
//        assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause().hasMessage("Undefined contract method called");
//        assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo(null);
//
//        verifyNoInteractions(ctx);
//    }
//
//    @Nested
//    class InvokeReadWatchTransaction {
//
//        private final String uuid = UUID.randomUUID().toString();
//
//        @Test
//        public void whenAssetExists() {
//            WatchContract contract = new WatchContract();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState(uuid)).thenReturn(String.format("{ \"watchID\": \"%s\", \"firmwareVersion\": \"%s\" }", uuid, "V0.0.1"));
//
//            Watch watch = contract.ReadWatch(ctx, uuid);
//
//            assertThat(watch).isEqualTo(new Watch(uuid, "v0.0.1"));
//        }
//
//        @Test
//        public void whenAssetDoesNotExist() {
//            WatchContract contract = new WatchContract();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState(uuid)).thenReturn("");
//
//            Throwable thrown = catchThrowable(() -> contract.ReadWatch(ctx, uuid));
//
//            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause().hasMessage(String.format("Watch %s does not exist", uuid));
//            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
//        }
//    }
//
//    @Nested
//    class InvokeCreateWatchTransaction {
//
//        private final String uuid = UUID.randomUUID().toString();
//
//        @Test
//        public void whenAssetExists() {
//            WatchContract contract = new WatchContract();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState(uuid)).thenReturn(String.format("{ \"watchID\": \"%s\", \"firmwareVersion\": \"%s\" }", uuid, "V0.0.1"));
//
//            Throwable thrown = catchThrowable(() -> contract.CreateWatch(ctx, uuid, "v0.0.1"));
//
//            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause().hasMessage(String.format("Watch %s already exists", uuid));
//            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_ALREADY_EXISTS".getBytes());
//        }
//
//        @Test
//        public void whenAssetDoesNotExist() {
//            WatchContract contract = new WatchContract();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState(uuid)).thenReturn("");
//
//            Watch watch = contract.CreateWatch(ctx, uuid, "v0.0.1");
//
//            assertThat(watch).isEqualTo(new Watch(uuid, "v0.0.1"));
//        }
//
//        @Test
//        public void whenFirmwareVersionFormatIsInvalid() {
//            WatchContract contract = new WatchContract();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState(uuid)).thenReturn("");
//
//            Throwable thrown = catchThrowable(() -> contract.CreateWatch(ctx, uuid, "not a valid version"));
//
//            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause().hasMessage(String.format("Firmware version %s invalid format", "not a valid version"));
//            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("INVALID_FIRMWARE_VERSION_FORMAT".getBytes());
//        }
//    }
//
//    @Test
//    void invokeCountFirmwareVersionGreaterEqualThanTransaction() {
//        WatchContract contract = new WatchContract();
//        Context ctx = mock(Context.class);
//        ChaincodeStub stub = mock(ChaincodeStub.class);
//        when(ctx.getStub()).thenReturn(stub);
//        when(stub.getStateByRange("", "")).thenReturn(new MockAssetResultsIterator());
//
//        int watchesCount = contract.CountFirmwareVersionGreaterEqualThan(ctx, "v0.0.3");
//
//        assertThat(watchesCount).isEqualTo(3);
//    }
//
//
//    @Nested
//    class UpdateWatchTransaction {
//
//        private final String uuid = UUID.randomUUID().toString();
//
//        @Test
//        public void whenAssetExists() {
//            WatchContract contract = new WatchContract();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState(uuid)).thenReturn(String.format("{ \"watchID\": \"%s\", \"firmwareVersion\": \"%s\" }", uuid, "V0.0.1"));
//
//            Watch watch = contract.UpdateWatch(ctx, uuid, "v0.0.2");
//
//            assertThat(watch).isEqualTo(new Watch(uuid, "v0.0.2"));
//        }
//
//        @Test
//        public void whenFirmwareVersionFormatIsInvalid() {
//            WatchContract contract = new WatchContract();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState(uuid)).thenReturn(String.format("{ \"watchID\": \"%s\", \"firmwareVersion\": \"%s\" }", uuid, "V0.0.2"));
//
//            Throwable thrown = catchThrowable(() -> contract.UpdateWatch(ctx, uuid, "not a valid version"));
//
//            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause().hasMessage(String.format("Firmware version %s invalid format", "not a valid version"));
//            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("INVALID_FIRMWARE_VERSION_FORMAT".getBytes());
//        }
//    }
//
//    @Nested
//    class DeleteWatchTransaction {
//
//        private final String uuid = UUID.randomUUID().toString();
//
//        @Test
//        public void whenAssetDoesNotExist() {
//            WatchContract contract = new WatchContract();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState(uuid)).thenReturn("");
//
//            Throwable thrown = catchThrowable(() -> contract.DeleteWatch(ctx, uuid));
//
//            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause().hasMessage(String.format("Watch %s does not exist", uuid));
//            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
//        }
//    }
//}
