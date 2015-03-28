package org.dcm4che3.conf.api.extensions;

import org.dcm4che3.net.AEExtension;

/**
 * @author Roman K
 */
public class CommonAEExtension extends AEExtension {
    /**
     * Iterates over all configurable fields and transfers the values using getter/setters from 'from' to this
     * @param from The extension with new configuration
     * @param clazz Class of the extension
     */
    public void reconfigureReflectively(AEExtension from, Class<? extends AEExtension> clazz) {
        CommonIterator.reconfigure(this, from, clazz);
    }

    public void reconfigure(AEExtension from) {
        // fallback to default reflective if the extension did not override the method
        reconfigureReflectively(from,from.getClass());
    }
}
