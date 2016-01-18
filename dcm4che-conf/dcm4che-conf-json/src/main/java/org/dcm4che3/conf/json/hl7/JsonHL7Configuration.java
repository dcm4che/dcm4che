package org.dcm4che3.conf.json.hl7;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.json.ConfigurationDelegate;
import org.dcm4che3.conf.json.JsonConfigurationExtension;
import org.dcm4che3.conf.json.JsonReader;
import org.dcm4che3.conf.json.JsonWriter;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.hl7.HL7Application;
import org.dcm4che3.net.hl7.HL7DeviceExtension;

import javax.json.stream.JsonParser;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Jan 2016
 */
public class JsonHL7Configuration extends JsonConfigurationExtension {

    private final List<JsonHL7ConfigurationExtension> extensions = new ArrayList<>();

    public void addHL7ConfigurationExtension(JsonHL7ConfigurationExtension ext) {
        extensions.add(ext);
    }

    public boolean removeHL7ConfigurationExtension(JsonHL7ConfigurationExtension ext) {
        if (!extensions.remove(ext))
            return false;

        return true;
    }

    @Override
    protected void storeTo(Device device, JsonWriter writer) {
        HL7DeviceExtension ext = device.getDeviceExtension(HL7DeviceExtension.class);
        if (ext == null)
            return;

        writer.writeStartArray("hl7Application");
        for (HL7Application hl7App : ext.getHL7Applications())
            writeTo(device, hl7App, writer);

        writer.writeEnd();
    }

    @Override
    public boolean loadDeviceExtension(Device device, JsonReader reader, ConfigurationDelegate config)
            throws ConfigurationException {
        if (!reader.getString().equals("hl7Application"))
            return false;

        HL7DeviceExtension ext = new HL7DeviceExtension();
        loadFrom(ext, reader, device);
        device.addDeviceExtension(ext);
        return true;
    }

    private void writeTo(Device device, HL7Application hl7App, JsonWriter writer) {
        writer.writeStartObject();
        writer.writeNotNull("hl7ApplicationName", hl7App.getApplicationName());
        writer.writeNotNull("dicomInstalled", hl7App.getInstalled());
        writer.writeConnRefs(device.listConnections(), hl7App.getConnections());
        writer.writeNotEmpty("hl7AcceptedSendingApplication", hl7App.getAcceptedSendingApplications());
        writer.writeNotEmpty("hl7AcceptedMessageType", hl7App.getAcceptedMessageTypes());
        writer.writeNotNull("hl7DefaultCharacterSet", hl7App.getHL7DefaultCharacterSet());
        for (JsonHL7ConfigurationExtension ext : extensions)
            ext.storeTo(hl7App, device, writer);
        writer.writeEnd();
    }

    private void loadFrom(HL7DeviceExtension ext, JsonReader reader, Device device) {
        List<Connection> conns = device.listConnections();
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            HL7Application hl7App = new HL7Application();
            loadFrom(hl7App, reader, device, conns);
            reader.expect(JsonParser.Event.END_OBJECT);
            ext.addHL7Application(hl7App);
        }
        reader.expect(JsonParser.Event.END_ARRAY);
    }

    private void loadFrom(HL7Application hl7App, JsonReader reader, Device device, List<Connection> conns) {
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
                case "hl7ApplicationName":
                    hl7App.setApplicationName(reader.stringValue());
                    break;
                case "dicomInstalled":
                    hl7App.setInstalled(reader.booleanValue());
                    break;
                case "dicomNetworkConnectionReference":
                    for (String connRef : reader.stringArray())
                        hl7App.addConnection(conns.get(JsonReader.toConnectionIndex(connRef)));
                    break;
                case "hl7AcceptedSendingApplication":
                    hl7App.setAcceptedSendingApplications(reader.stringArray());
                    break;
                case "hl7AcceptedMessageType":
                    hl7App.setAcceptedMessageTypes(reader.stringArray());
                    break;
                case "hl7DefaultCharacterSet":
                    hl7App.setHL7DefaultCharacterSet(reader.stringValue());
                    break;
                default:
                    if (!loadHL7ApplicationExtension(device, hl7App, reader))
                        reader.skipUnknownProperty();
            }
        }
    }

    private boolean loadHL7ApplicationExtension(Device device, HL7Application hl7App, JsonReader reader) {
        for (JsonHL7ConfigurationExtension ext : extensions)
            if (ext.loadHL7ApplicationExtension(device, hl7App, reader))
                return true;
        return false;
    }

}
