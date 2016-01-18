package org.dcm4che3.conf.json.hl7;

import org.dcm4che3.conf.json.JsonReader;
import org.dcm4che3.conf.json.JsonWriter;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.hl7.HL7Application;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jan 2016
 */
public interface JsonHL7ConfigurationExtension {
    void storeTo(HL7Application hl7App, Device device, JsonWriter writer);

    boolean loadHL7ApplicationExtension(Device device, HL7Application hl7App, JsonReader reader);
}
