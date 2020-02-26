package org.dcm4che3.net.audit;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Created by Umberto Cappellini on 4/12/16.
 */
public class AuditMessageEventHandler implements WorkHandler<AuditMessageEvent> {

    private static Logger LOG = LoggerFactory.getLogger(AuditMessageEventHandler.class);

    private String clientName;

    public AuditMessageEventHandler(String clientName) {
        this.clientName = clientName;
    }

    public void onEvent(AuditMessageEvent event) throws Exception
    {
        try {
            event.getLogger().write(event.getLogger().timeStamp(),event.getMessage(),clientName);
        } catch (Throwable e) {
            LOG.warn("could not send audit",e);
            throw new IOException(event.getMessage().toString(), e);
        }
    }
}
