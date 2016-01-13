package org.dcm4che3.conf.json.audit;

import org.dcm4che3.conf.json.JsonConfigurationExtension;
import org.dcm4che3.conf.json.JsonReader;
import org.dcm4che3.conf.json.JsonWriter;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditRecordRepository;

import javax.json.stream.JsonParser;
import java.util.List;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jan 2016
 */
public class JsonAuditRecordRepositoryConfiguration extends JsonConfigurationExtension {
    @Override
    protected void storeTo(Device device, JsonWriter writer) {
        AuditRecordRepository arr = device.getDeviceExtension(AuditRecordRepository.class);
        if (arr == null)
            return;

        writer.writeStartObject("dcmAuditRecordRepository");
        writeConnRefs(arr, device.listConnections(), writer);
        writer.writeNotNull("dicomInstalled", arr.getInstalled());
        writer.writeEnd();
    }

    private void writeConnRefs(AuditRecordRepository arr, List<Connection> conns, JsonWriter writer) {
        writer.writeStartArray("dicomNetworkConnectionReference");
        for (Connection conn : arr.getConnections())
            writer.write("/dicomNetworkConnection/" + conns.indexOf(conn));
        writer.writeEnd();
    }

    @Override
    public boolean loadDeviceExtension(Device device, JsonReader reader) {
        if (!reader.getString().equals("dcmAuditRecordRepository"))
            return false;

        reader.next();
        reader.expect(JsonParser.Event.START_OBJECT);
        AuditRecordRepository arr = new AuditRecordRepository();
        loadFrom(arr, reader, device.listConnections());
        device.addDeviceExtension(arr);
        reader.expect(JsonParser.Event.END_OBJECT);
        return true;
    }

    private void loadFrom(AuditRecordRepository arr, JsonReader reader, List<Connection> conns) {
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
                case "dicomInstalled":
                    arr.setInstalled(reader.booleanValue());
                    break;
                case "dicomNetworkConnectionReference":
                    for (String connRef : reader.stringArray())
                        arr.addConnection(conns.get(JsonReader.toConnectionIndex(connRef)));
                    break;
                default:
                    reader.skipUnknownProperty();
            }
        }
    }
}
