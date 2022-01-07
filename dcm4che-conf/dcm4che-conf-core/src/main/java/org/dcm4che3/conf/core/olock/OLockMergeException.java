package org.dcm4che3.conf.core.olock;

import org.dcm4che3.conf.core.api.ConfigurationException;

/**
 * This is an <b>internal</b> exception indicating that hash-based optimistic lock merge failed.
 * 
 * @author Maciek Siemczyk (maciek.siemczyk@agfa.com)
 * 
 * @see ConfigurationException
 */
public class OLockMergeException extends ConfigurationException {

    private static final long serialVersionUID = 1L;

    /** 
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public OLockMergeException(final String message) {

        super(message);
    }
}
