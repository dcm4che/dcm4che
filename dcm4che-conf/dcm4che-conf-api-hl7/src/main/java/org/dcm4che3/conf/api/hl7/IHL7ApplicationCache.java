package org.dcm4che3.conf.api.hl7;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.net.hl7.HL7Application;

public interface IHL7ApplicationCache {

    int getStaleTimeout();

    void setStaleTimeout(int staleTimeout);

    void clear();

    /**
     * Looks-up the HL7-Application with the specified name from the cache.
     * @param name Name of the HL7-Application
     * @return Returns the HL7-Application, if no HL7-Application with the given name exists then <code>null</code> is returned
     * @throws ConfigurationException Thrown if some error happens while looking-up the application.
     */
    HL7Application get(String name) throws ConfigurationException;

    HL7Application findHL7Application(String name)
            throws ConfigurationException;

}