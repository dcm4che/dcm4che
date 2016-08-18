package org.dcm4che3.conf.api;

import org.dcm4che3.conf.core.api.ConfigurationException;

public interface TCGroupsProvider {

    TCConfiguration getTCGroups() throws ConfigurationException;
}
