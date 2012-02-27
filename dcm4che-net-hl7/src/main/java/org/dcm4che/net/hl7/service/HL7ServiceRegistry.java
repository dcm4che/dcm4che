package org.dcm4che.net.hl7.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.dcm4che.hl7.HL7Exception;
import org.dcm4che.net.Connection;
import org.dcm4che.net.hl7.HL7MessageListener;

public class HL7ServiceRegistry extends HL7MessageListener {

    private final ArrayList<HL7Service> services = new ArrayList<HL7Service>();
    private final HashMap<String,HL7MessageListener> listeners =
            new HashMap<String,HL7MessageListener>();

    public synchronized void addHL7Service(HL7Service service) {
        services.add(service);
        for (String messageType : service.getMessageTypes())
            listeners.put(messageType, service);
    }

    public synchronized boolean removeHL7Service(HL7Service service) {
        if (!services.remove(service))
            return false;
        
        for (String messageType : service.getMessageTypes())
            listeners.remove(messageType);

        return true;
    }

    @Override
    public byte[] onMessage(String[] msh, byte[] msg, int off, int len, Connection conn)
            throws HL7Exception {
        return service(msh[8]).onMessage(msh, msg, 0, 0, conn);
    }

    private HL7MessageListener service(String messageType)
            throws HL7Exception {
        HL7MessageListener ret = listeners.get(messageType);
        if (ret == null)
            ret = listeners.get("*");

        return ret != null ? ret : this;
    }
 
}
