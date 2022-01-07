package org.dcm4che3.conf.core.api.tests;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.dcm4che3.conf.core.api.ConfigurationConcurrentModificationException;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.junit.Test;

public class ConfigurationConcurrentModificationExceptionTest {

    /**
     * System Under Test (SUT).
     */
    private ConfigurationConcurrentModificationException exception;
    
    @Test
    public void contructor_SetsMembersCorrecly_GivenValidMessageAndNullCause() {
        
        exception = new ConfigurationConcurrentModificationException("Some message", null);
        
        assertThat("Message", exception.getMessage(), equalTo("Some message"));
        assertThat("Cause", exception.getCause(), nullValue());
    }

    @Test
    public void contructor_SetsMembersCorrecly_GivenValidMessageAndCause() {
        
        final ConfigurationException expectedCause = new ConfigurationException();
        
        exception = new ConfigurationConcurrentModificationException("bonjour", expectedCause);
        
        assertThat("Message", exception.getMessage(), equalTo("bonjour"));
        assertThat("Cause", exception.getCause(), equalTo(expectedCause));
    }
}
