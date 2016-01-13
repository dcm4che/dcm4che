package org.dcm4che3.conf.json.imageio;

import org.dcm4che3.conf.json.JsonConfigurationExtension;
import org.dcm4che3.conf.json.JsonWriter;
import org.dcm4che3.conf.json.JsonReader;
import org.dcm4che3.imageio.codec.ImageReaderFactory;
import org.dcm4che3.imageio.codec.ImageWriterFactory;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.imageio.ImageReaderExtension;
import org.dcm4che3.net.imageio.ImageWriterExtension;

import javax.json.stream.JsonParser;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jan 2016
 */
public class JsonImageWriterConfiguration extends JsonConfigurationExtension {
    @Override
    protected void storeTo(Device device, JsonWriter writer) {
        ImageWriterExtension ext =  device.getDeviceExtension(ImageWriterExtension.class);
        if (ext == null)
            return;

        writer.writeStartArray("dcmImageWriter");
        for (Map.Entry<String, ImageWriterFactory.ImageWriterParam> entry : ext.getImageWriterFactory().getEntries()) {
            writer.writeStartObject();
            String tsuid = entry.getKey();
            ImageWriterFactory.ImageWriterParam param = entry.getValue();
            //TODO
            writer.writeNotNull("dicomTransferSyntax", tsuid);
            writer.writeNotNull("dcmIIOFormatName", param.formatName);
            writer.writeNotNull("dcmJavaClassName", param.className);
            writer.writeNotNull("dcmPatchJPEGLS", param.patchJPEGLS);
            writer.writeNotEmpty("dcmImageWriteParam", param.imageWriteParams);

            writer.writeEnd();
        }

        writer.writeEnd();
    }

    @Override
    public boolean loadDeviceExtension(Device device, JsonReader reader) {
        if (!reader.getString().equals("dcmImageWriter"))
            return false;

        ImageWriterFactory factory = new ImageWriterFactory();
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            String tsuid = null;
            String formatName = null;
            String className = null;
            String patchJPEGLS = null;
            String[] imageWriteParam = new String[0];
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
                    case "dcmImageWriteParam":
                        imageWriteParam = reader.stringArray();
                        break;
                    default:
                        reader.skipUnknownProperty();
                }
            }
            reader.expect(JsonParser.Event.END_OBJECT);
            factory.put(tsuid, new ImageWriterFactory.ImageWriterParam(formatName, className, patchJPEGLS, imageWriteParam));
        }
        device.addDeviceExtension(new ImageWriterExtension(factory));
        return true;
    }
}
