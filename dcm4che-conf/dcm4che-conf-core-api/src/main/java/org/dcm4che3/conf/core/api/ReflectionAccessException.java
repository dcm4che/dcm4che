package org.dcm4che3.conf.core.api;

/**
 * @author rawmahn
 */
public class ReflectionAccessException extends RuntimeException {
    public ReflectionAccessException() {
    }

    public ReflectionAccessException(String message) {
        super(message);
    }

    public ReflectionAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
