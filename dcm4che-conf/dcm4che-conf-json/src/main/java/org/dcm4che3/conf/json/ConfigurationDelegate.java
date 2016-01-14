package org.dcm4che3.conf.json;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.net.Device;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jan 2016
 */
public interface ConfigurationDelegate {
    Device findDevice(String name) throws ConfigurationException;
}
