package org.dcm4che3.conf.core.api;

/**
 * @author Roman K
 */
public class OptimisticLockException extends ConfigurationException {

    String path;

    public OptimisticLockException(String path) {
        super("Concurrent modification at path '" + path + "'");
        this.path = path;
    }

    public String getPath() {
        return path;
    }

}
