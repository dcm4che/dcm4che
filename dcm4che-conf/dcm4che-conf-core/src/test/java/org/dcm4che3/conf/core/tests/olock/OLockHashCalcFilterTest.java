package org.dcm4che3.conf.core.tests.olock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.olock.OLockHashCalcFilter;
import org.junit.Test;

public class OLockHashCalcFilterTest {
    
    /**
     * System Under Test (SUT).
     */
    private OLockHashCalcFilter filter = new OLockHashCalcFilter();
    
    @Test
    public void defaultContructor_SetsIgnoredKeysToEmpty_WhenCalled() {
        
        // n.b. the default constructor is actually called at the test class level.
        
        assertThat("Ignored keys are not empty", filter.getIgnoredKeys(), empty());
    }
    
    @Test
    public void parameterizedContructor_SetsIgnoredKeysCorrectly_GivenOneKeyToIgnore() {
        
        filter = new OLockHashCalcFilter(Configuration.VERSION_KEY);
        
        assertThat("Ignored keys", filter.getIgnoredKeys(), equalTo(Arrays.asList(Configuration.VERSION_KEY)));
    }
    
    @Test
    public void parameterizedContructor_SetsIgnoredKeysCorrectly_GivenTwoKeysToIgnore() {
        
        filter = new OLockHashCalcFilter("ignoreMe", "andMe");
        
        assertThat("Ignored keys", filter.getIgnoredKeys(), equalTo(Arrays.asList("ignoreMe", "andMe")));
    }
    
    @Test
    public void parameterizedContructor_SetsIgnoredKeysToEmpty_GivenNull() {
        
        filter = new OLockHashCalcFilter((String[]) null);
        
        assertThat("Ignored keys are not empty", filter.getIgnoredKeys(), empty());
    }
}
