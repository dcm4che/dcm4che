package org.dcm4che3.conf.dicom.decorators;

import org.dcm4che3.conf.api.internal.DicomConfigurationManager;
import org.dcm4che3.conf.core.DelegatingConfiguration;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.dicom.DicomPath;
import org.dcm4che3.net.Device;

import java.util.Map;

/**
 * Performs validation of modifications and ensures referential integrity of configuration
 *
 * Current impl is a quick solution - persist/remove the node and try to reload all devices, if fails, revert the persisted node back
 * @author Roman K
 */
public class DicomValidatingConfigurationDecorator extends DelegatingConfiguration {

    private DicomConfigurationManager configurationManager;

    public DicomValidatingConfigurationDecorator(Configuration delegate, DicomConfigurationManager configurationManager) {
        super(delegate);
        this.configurationManager = configurationManager;
    }

    @Override
    public void persistNode(String path, Map<String, Object> configNode, Class configurableClass) throws ConfigurationException {
        Object oldConfig = delegate.getConfigurationNode(path, configurableClass);

        delegate.persistNode(path, configNode, configurableClass);

        try {
            for (String deviceName : configurationManager.listDeviceNames())
                configurationManager.findDevice(deviceName);
        } catch (ConfigurationException e) {
            // validation failed, replace the node back
            delegate.persistNode(path, (Map<String, Object>) oldConfig, Device.class);
            throw e;
        }
    }

    @Override
    public void removeNode(String path) throws ConfigurationException {
        Object oldConfig = delegate.getConfigurationNode(path, Device.class);

        delegate.removeNode(path);

        try {
            for (String deviceName : configurationManager.listDeviceNames())
                configurationManager.findDevice(deviceName);
        } catch (ConfigurationException e) {
            // validation failed, replace the node back
            delegate.persistNode(path, (Map<String, Object>) oldConfig, Device.class);
            throw e;
        }
    }
}
