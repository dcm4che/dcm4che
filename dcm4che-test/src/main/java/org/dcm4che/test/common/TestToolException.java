package org.dcm4che.test.common;

/**
 * @author Roman K
 */
public class TestToolException extends RuntimeException {

    public TestToolException(String message) {
        super(message);
    }

    public TestToolException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestToolException(Throwable cause) {
        super(cause);
    }
}
