package org.dcm4che3.conf.core.api;

import javax.ejb.ApplicationException;

/**
 * This exception is thrown to indicate that configuration framework has detected concurrent
 * modification of the same node when such modification is not permissible.

 * @author Maciek Siemczyk (maciek.siemczyk@agfa.com)
 * 
 * @see ConfigurationException
 */
@ApplicationException(rollback = true)
public class ConfigurationConcurrentModificationException extends ConfigurationException {

    private static final long serialVersionUID = 2L;

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message 
     * @param cause the cause or null if nonexistent or unknown.
     */
    public ConfigurationConcurrentModificationException(
            final String message,
            final ConfigurationException cause) {
        
        super(message, cause);
    }
}
