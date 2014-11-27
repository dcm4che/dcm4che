package org.dcm4che3.conf.api;

/**
 * Thrown when the serialized configuration representation cannot be retrieved from a configured POJO.
 */
public class ConfigurationUnserializableException extends ConfigurationException {
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
