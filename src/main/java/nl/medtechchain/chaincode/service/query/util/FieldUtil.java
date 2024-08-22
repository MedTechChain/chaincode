package nl.medtechchain.chaincode.service.query.util;

import com.google.protobuf.Descriptors;
import nl.medtechchain.proto.devicedata.DeviceDataAsset;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FieldUtil {
    public Optional<Descriptors.FieldDescriptor> fieldDescriptor(String fieldName) {
        return fieldDescriptors().stream().filter(f -> fieldName.equals(f.getName())).findAny();
    }

    public List<Descriptors.FieldDescriptor> fieldDescriptors() {
        var fields = new ArrayList<Descriptors.FieldDescriptor>();
        fields.addAll(plainFieldsDescriptors());
        fields.addAll(sensitiveFieldsDescriptors());
        return fields;
    }

    public List<Descriptors.FieldDescriptor> plainFieldsDescriptors() {
        return DeviceDataAsset.PlainDeviceData.getDescriptor().getFields();
    }

    public List<Descriptors.FieldDescriptor> sensitiveFieldsDescriptors() {
        return DeviceDataAsset.SensitiveDeviceData.getDescriptor().getFields();
    }

    public String extractSensitiveFieldValue(DeviceDataAsset asset, String field) {
        var descriptor = DeviceDataAsset.SensitiveDeviceData.getDescriptor().findFieldByName(field);
        return (String) asset.getSensitiveDeviceData().getField(descriptor);
    }

    public <T> T extractPlainFieldValue(DeviceDataAsset asset, String field) {
        var descriptor = DeviceDataAsset.PlainDeviceData.getDescriptor().findFieldByName(field);
        return (T) asset.getPlainDeviceData().getField(descriptor);
    }

    public boolean isPlainField(String fieldName) {
        return DeviceDataAsset.PlainDeviceData.getDescriptor().getFields().stream().anyMatch(f -> f.getName().equals(fieldName));
    }

    public boolean isSensitiveField(String fieldName) {
        return DeviceDataAsset.SensitiveDeviceData.getDescriptor().getFields().stream().anyMatch(f -> f.getName().equals(fieldName));
    }
}
