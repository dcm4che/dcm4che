package org.dcm4che3.conf.core.api;

import org.dcm4che3.conf.core.api.ConfigurationException;

/**
 * Thrown when the serialized configuration representation cannot be retrieved from a configured POJO.
 */
public class ConfigurationUnserializableException extends ConfigurationException {

    private static final long serialVersionUID = 9078943756654391743L;

    public ConfigurationUnserializableException() {
    }

    public ConfigurationUnserializableException(String message) {
        super(message);
    }

    public ConfigurationUnserializableException(Throwable cause) {
        super(cause);
    }

    public ConfigurationUnserializableException(String message, Throwable cause) {
        super(message, cause);
    }
}
