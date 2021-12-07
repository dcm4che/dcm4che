package org.dcm4che3.conf.api.tests;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dcm4che3.conf.api.upgrade.ConfigurationMetadata;
import org.dcm4che3.conf.core.api.StorageVersionedConfigurableClass;
import org.junit.Test;

public class ConfigurationMetadataTest {

    @Test
    public void configurationMetadata_ExtendsStorageVersionedConfigurableClass_BecauseItIsRootConfigurationClass() {
        
        assertThat(
            "ConfigurationMetadata is root class and should be extending StorageVersionedConfigurableClass",
            StorageVersionedConfigurableClass.class.isAssignableFrom(ConfigurationMetadata.class),
            equalTo(true));
    }
}
