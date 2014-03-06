package org.dcm4che3.net.audit.impl;

import org.dcm4che3.net.audit.AuditAnswer;

public class DefaultAuditAnswer implements AuditAnswer {

    private AuditStatus status;
    private String      message;
    private Exception   e;

    private DefaultAuditAnswer(AuditStatus status, String message, Exception e) {
        this.status = status;
        this.message = message;
        this.e = e;
    }

    /**
     * This factory method creates a success answer with the given message.
     * 
     * @param message
     *        the audit message
     * @return
     */
    public static AuditAnswer createSuccessAnswer(String message) {
        return new DefaultAuditAnswer(AuditStatus.SUCCESS, message, null);
    }

    /**
     * This factory method creates a failure answer with the given message and exception.
     * 
     * @param message
     *        the audit message
     * @param e
     *        an optional exception
     * @return
     */
    public static AuditAnswer createFailureAnswer(String message, Exception e) {
        return new DefaultAuditAnswer(AuditStatus.FAILURE, message, e);
    }

    @Override
    public AuditStatus getAuditStatus() {
        return status;
    }

    @Override
    public Exception getException() {
        return e;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
