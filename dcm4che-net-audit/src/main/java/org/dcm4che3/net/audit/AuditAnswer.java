package org.dcm4che3.net.audit;

/**
 * This interface provides methods to query the status of an audit logging, the message itself
 * (formatted as a string, for e.g. local logging) and an exception that may have occurred. You can
 * and must not rely on an exception being present in all cases of failure though, it may be null.
 * The message may also be null if there was an error creating the string but in this case the
 * exception has to return a value.
 */
public interface AuditAnswer {

    /**
     * The status of the audit logging process
     */
    public enum AuditStatus {
        SUCCESS, FAILURE;
    }

    /**
     * @return the audit status
     */
    public abstract AuditStatus getAuditStatus();

    /**
     * @return null if the status is {@link AuditStatus#SUCCESS}, may contain an exception otherwise
     */
    public abstract Exception getException();

    /**
     * @return the audited message as String
     */
    public abstract String getMessage();
}
