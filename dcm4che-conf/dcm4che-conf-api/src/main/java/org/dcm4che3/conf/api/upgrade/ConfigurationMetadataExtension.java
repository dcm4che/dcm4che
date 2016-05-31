package org.dcm4che3.conf.api.upgrade;

import org.dcm4che3.conf.core.api.ConfigurableClassExtension;

public class ConfigurationMetadataExtension extends ConfigurableClassExtension<ConfigurationMetadataExtension>{

    @Override
    public void reconfigure(ConfigurationMetadataExtension from) {

    }

    @Override
    public Class<ConfigurationMetadataExtension> getBaseClass() {
        return ConfigurationMetadataExtension.class;
    }
}
