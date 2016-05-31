package org.dcm4che3.conf.core.api;

public class ConfigurationUpgradeException extends RuntimeException {

    public ConfigurationUpgradeException() {
    }

    public ConfigurationUpgradeException(String message) {
        super(message);
    }

    public ConfigurationUpgradeException(String message, Throwable cause) {
        super(message, cause);
    }
}
