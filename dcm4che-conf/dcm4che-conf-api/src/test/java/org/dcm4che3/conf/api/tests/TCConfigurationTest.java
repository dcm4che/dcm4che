package org.dcm4che3.conf.api.tests;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dcm4che3.conf.api.TCConfiguration;
import org.dcm4che3.conf.core.api.StorageVersionedConfigurableClass;
import org.junit.Test;

public class TCConfigurationTest {

    @Test
    public void tcConfiguration_ExtendsStorageVersionedConfigurableClass_BecauseItIsRootConfigurationClass() {
        
        assertThat(
            "TCConfiguration is root class and should be extending StorageVersionedConfigurableClass",
            StorageVersionedConfigurableClass.class.isAssignableFrom(TCConfiguration.class),
            equalTo(true));
    }
}
