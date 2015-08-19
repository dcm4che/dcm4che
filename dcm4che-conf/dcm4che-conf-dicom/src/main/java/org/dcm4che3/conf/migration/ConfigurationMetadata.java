package org.dcm4che3.conf.migration;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;

/**
 * @author Roman K
 */
@ConfigurableClass
public class ConfigurationMetadata {

    @ConfigurableProperty
    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}


