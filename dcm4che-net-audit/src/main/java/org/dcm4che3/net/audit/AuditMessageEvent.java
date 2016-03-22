package org.dcm4che3.net.audit;

import org.dcm4che3.audit.AuditMessage;

/**
 * Created by Umberto Cappellini on 3/21/16.
 */
public class AuditMessageEvent {

    private AuditLogger logger;
    private AuditMessage message;

    public AuditLogger getLogger() {
        return logger;
    }

    public void setLogger(AuditLogger logger) {
        this.logger = logger;
    }

    public AuditMessage getMessage() {
        return message;
    }

    public void setMessage(AuditMessage message) {
        this.message = message;
    }
}
