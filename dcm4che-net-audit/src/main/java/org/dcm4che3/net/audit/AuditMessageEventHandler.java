package org.dcm4che3.net.audit;

import com.lmax.disruptor.EventHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Created by Umberto Cappellini on 4/12/16.
 */
public class AuditMessageEventHandler implements EventHandler<AuditMessageEvent> {

    private static Logger LOG = LoggerFactory.getLogger(AuditMessageEventHandler.class);

    public void onEvent(AuditMessageEvent event, long sequence, boolean endOfBatch) throws Exception
    {
        try {
            event.getLogger().write(event.getLogger().timeStamp(),event.getMessage());
        } catch (Throwable e) {
            LOG.warn("could not send audit",e);
        }
    }
}
