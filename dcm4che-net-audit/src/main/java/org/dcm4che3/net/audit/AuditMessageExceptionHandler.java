package org.dcm4che3.net.audit;

import java.io.IOException;

import org.dcm4che3.audit.AuditMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.ExceptionHandler;

public class AuditMessageExceptionHandler implements ExceptionHandler<AuditMessageEvent> {
    private static final Logger logger = LoggerFactory.getLogger(AuditMessageExceptionHandler.class);
    public static final String AUDIT_LOGGER_LOG4J_LOGCATEGORY = AuditMessageExceptionHandler.class.getName() + ".auditmessage";
    private static final Logger loggerAuditMessages = LoggerFactory.getLogger(AUDIT_LOGGER_LOG4J_LOGCATEGORY);

    @Override
    public void handleEventException(Throwable ex, long sequence, AuditMessageEvent event) {
        try {
            loggerAuditMessages.warn(AuditMessages.toXML(event.getMessage()));
        } catch (IOException e) {
            logger.error("Failed to writeAuditMessageToLogCategory '{}': {}", AUDIT_LOGGER_LOG4J_LOGCATEGORY, e.getMessage(), e);
        }
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        logger.warn("Inside handleOnStartException(Throwable ex):\n" +
            "exception message: " + ex.getMessage(), ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        logger.warn("Inside handleOnShutdownException(Throwable ex):\n" +
        "exception message: " + ex.getMessage(), ex);
    }
}
