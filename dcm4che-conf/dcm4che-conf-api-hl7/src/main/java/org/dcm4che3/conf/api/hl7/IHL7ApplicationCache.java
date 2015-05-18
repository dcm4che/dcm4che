package org.dcm4che3.conf.api.hl7;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.net.hl7.HL7Application;

public interface IHL7ApplicationCache {

    int getStaleTimeout();

    void setStaleTimeout(int staleTimeout);

    void clear();

    HL7Application get(String name) throws ConfigurationException;

    HL7Application findHL7Application(String name)
            throws ConfigurationException;

}