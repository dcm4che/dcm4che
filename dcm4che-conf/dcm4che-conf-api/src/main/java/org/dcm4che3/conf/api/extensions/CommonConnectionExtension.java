package org.dcm4che3.conf.api.extensions;

import org.dcm4che3.net.ConnectionExtension;

/**
 * This class shall be extended by vendors/contributors to store extra configuration for a Connection
 * @author Roman K
 */
public class CommonConnectionExtension extends ConnectionExtension {
    /**
     * Iterates over all configurable fields and transfers the values using getter/setters from 'from' to this
     * @param from The extension with new configuration
     * @param clazz Class of the extension
     */
    public void reconfigureReflectively(ConnectionExtension from, Class<? extends ConnectionExtension> clazz) {
        ReconfiguringIterator.reconfigure(from, this, clazz);
    }

    public void reconfigure(ConnectionExtension from) {
        // fallback to default reflective if the extension did not override the method
        reconfigureReflectively(from,from.getClass());
    }
}
