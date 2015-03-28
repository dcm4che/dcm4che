package org.dcm4che3.conf.api.extensions;

import org.dcm4che3.net.DeviceExtension;

/**
 * This class shall be extended by vendors/contributors to store extra configuration for a device.
 * @author Roman K
 */
public class CommonDeviceExtension extends DeviceExtension {

    /**
     * Iterates over all configurable fields and transfers the values using getter/setters from 'from' to this
     * @param from The extension with new configuration
     * @param clazz Class of the extension
     */
    public void reconfigureReflectively(DeviceExtension from, Class<? extends DeviceExtension> clazz) {
        CommonIterator.reconfigure(this, from, clazz);
    }

    public void reconfigure(DeviceExtension from) {
        // fallback to default reflective if the extension did not override the method
        reconfigureReflectively(from,from.getClass());
    }
}
