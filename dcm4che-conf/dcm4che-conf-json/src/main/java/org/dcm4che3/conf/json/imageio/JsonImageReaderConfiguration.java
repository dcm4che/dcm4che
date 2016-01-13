package org.dcm4che3.conf.json.imageio;

import org.dcm4che3.conf.json.JsonConfigurationExtension;
import org.dcm4che3.conf.json.JsonReader;
import org.dcm4che3.conf.json.JsonWriter;
import org.dcm4che3.imageio.codec.ImageReaderFactory;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.net.imageio.ImageReaderExtension;

import javax.json.stream.JsonParser;
import java.util.Map;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jan 2016
 */
public class JsonImageReaderConfiguration extends JsonConfigurationExtension {
    @Override
    protected void storeTo(Device device, JsonWriter writer) {
        ImageReaderExtension ext =  device.getDeviceExtension(ImageReaderExtension.class);
        if (ext == null)
            return;

        writer.writeStartArray("dcmImageReader");
        for (Map.Entry<String, ImageReaderFactory.ImageReaderParam> entry : ext.getImageReaderFactory().getEntries()) {
            writer.writeStartObject();
            String tsuid = entry.getKey();
            ImageReaderFactory.ImageReaderParam param = entry.getValue();
            writer.writeNotNull("dicomTransferSyntax", tsuid);
            writer.writeNotNull("dcmIIOFormatName", param.formatName);
            writer.writeNotNull("dcmJavaClassName", param.className);
            writer.writeNotNull("dcmPatchJPEGLS", param.patchJPEGLS);
            writer.writeEnd();
        }

        writer.writeEnd();
    }

    @Override
    public boolean loadDeviceExtension(Device device, JsonReader reader) {
        if (!reader.getString().equals("dcmImageReader"))
            return false;

        ImageReaderFactory factory = new ImageReaderFactory();
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            String tsuid = null;
            String formatName = null;
            String className = null;
            String patchJPEGLS = null;
            while (reader.next() == JsonParser.Event.KEY_NAME) {
                switch (reader.getString()) {
                    case "dicomTransferSyntax":
                        tsuid = reader.stringValue();
                        break;
                    case "dcmIIOFormatName":
                        formatName = reader.stringValue();
                        break;
                    case "dcmJavaClassName":
                        className = reader.stringValue();
                        break;
                    case "dcmPatchJPEGLS":
                        patchJPEGLS = reader.stringValue();
                        break;
                    default:
                        reader.skipUnknownProperty();
                }
            }
            reader.expect(JsonParser.Event.END_OBJECT);
            factory.put(tsuid, new ImageReaderFactory.ImageReaderParam(formatName, className, patchJPEGLS));
        }
        device.addDeviceExtension(new ImageReaderExtension(factory));
        return true;
    }
}
