package org.dcm4che3.conf.json.audit;

import org.dcm4che3.conf.json.JsonConfigurationExtension;
import org.dcm4che3.conf.json.JsonWriter;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditRecordRepository;

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
}
