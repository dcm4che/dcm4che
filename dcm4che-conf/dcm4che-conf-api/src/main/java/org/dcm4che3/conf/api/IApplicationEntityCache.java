package org.dcm4che3.conf.api;

import org.dcm4che3.net.ApplicationEntity;

public interface IApplicationEntityCache {

    int getStaleTimeout();

    void setStaleTimeout(int staleTimeout);

    void clear();

    ApplicationEntity get(String aet) throws ConfigurationException;

    ApplicationEntity findApplicationEntity(String aet)
            throws ConfigurationException;

}