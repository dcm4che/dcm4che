package org.dcm4che3.net.audit;

import com.lmax.disruptor.EventFactory;

/**
 * Created by Umberto Cappellini on 4/12/16.
 */
public class AuditMessageEventFactory implements EventFactory<AuditMessageEvent> {

    public AuditMessageEvent newInstance() {
        return new AuditMessageEvent();
    }
}
