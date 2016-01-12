package org.dcm4che3.conf.json.imageio;

import org.dcm4che3.conf.json.JsonConfigurationExtension;
import org.dcm4che3.conf.json.JsonWriter;
import org.dcm4che3.imageio.codec.ImageReaderFactory;
import org.dcm4che3.imageio.codec.ImageWriterFactory;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.imageio.ImageReaderExtension;
import org.dcm4che3.net.imageio.ImageWriterExtension;

import javax.imageio.ImageWriter;
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

        writer.writeStartArray("dcmImageReader");
        for (Map.Entry<String, ImageWriterFactory.ImageWriterParam> entry : ext.getImageWriterFactory().getEntries()) {
            writer.writeStartObject();
            String tsuid = entry.getKey();
            ImageWriterFactory.ImageWriterParam param = entry.getValue();
            //TODO
            writer.writeEnd();
        }

        writer.writeEnd();
    }
}
