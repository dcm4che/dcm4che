package org.dcm4che3.conf.json.imageio;

import org.dcm4che3.conf.json.JsonConfigurationExtension;
import org.dcm4che3.conf.json.JsonWriter;
import org.dcm4che3.imageio.codec.ImageReaderFactory;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.imageio.ImageReaderExtension;

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
            //TODO
            writer.writeEnd();
        }

        writer.writeEnd();
    }
}
