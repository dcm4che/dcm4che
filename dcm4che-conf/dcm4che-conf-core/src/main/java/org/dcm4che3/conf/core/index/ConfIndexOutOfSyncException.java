package org.dcm4che3.conf.core.index;

/**
 * @author rawmahn
 */
public class ConfIndexOutOfSyncException extends RuntimeException {
    public ConfIndexOutOfSyncException() {
    }

    public ConfIndexOutOfSyncException(String s) {
        super(s);
    }
}
