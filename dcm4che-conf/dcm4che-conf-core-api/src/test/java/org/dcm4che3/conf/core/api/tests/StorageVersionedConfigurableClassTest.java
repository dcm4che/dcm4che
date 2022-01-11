package org.dcm4che3.conf.core.api.tests;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dcm4che3.conf.core.api.StorageVersionedConfigurableClass;
import org.junit.Test;

public class StorageVersionedConfigurableClassTest {

    /**
     * System Under Test (SUT).
     */
    private StorageVersionedConfigurableClass storageVersionedConfigurableClass
        = new TestableStorageVersionedConfigurableClass();
    
    @Test
    public void contructor_SetsMemberCorrecly_WhenCalled() {
        
        assertThat("Wrong default version", storageVersionedConfigurableClass.getStorageVersion(), equalTo(0L));
    }

    @Test
    public void setStorageVersion_SetsMemberCorrecly_WhenCalled() {
        
        storageVersionedConfigurableClass.setStorageVersion(Integer.MAX_VALUE + 1L);
        
        assertThat("Wrong version", storageVersionedConfigurableClass.getStorageVersion(), equalTo(2147483648L));
    }

    @SuppressWarnings("serial")
    private static final class TestableStorageVersionedConfigurableClass extends StorageVersionedConfigurableClass { }
}
