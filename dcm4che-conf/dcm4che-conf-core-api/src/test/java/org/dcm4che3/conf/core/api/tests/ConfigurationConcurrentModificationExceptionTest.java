package org.dcm4che3.conf.core.api.tests;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ejb.ApplicationException;

import org.assertj.core.api.Condition;
import org.dcm4che3.conf.core.api.ConfigurationConcurrentModificationException;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.junit.Test;

public class ConfigurationConcurrentModificationExceptionTest {

    /**
     * System Under Test (SUT).
     */
    private ConfigurationConcurrentModificationException exception;
    
    @Test
    public void configurationConcurrentModificationException_IsAnnotatedWithApplicationException_AtClassLevel() {
    
        assertThat(ConfigurationConcurrentModificationException.class)
                .hasAnnotation(ApplicationException.class)
                .has(new Condition<>(
                    clazz -> clazz.getDeclaredAnnotation(ApplicationException.class).rollback(),
                    "annotation with rollback set to true"));
    }
    
    @Test
    public void contructor_SetsMembersCorrecly_GivenValidMessageAndNullCause() {
        
        exception = new ConfigurationConcurrentModificationException("Some message", null);
        
        assertThat(exception).hasMessage("Some message").hasNoCause();
    }

    @Test
    public void contructor_SetsMembersCorrecly_GivenValidMessageAndCause() {
        
        final ConfigurationException expectedCause = new ConfigurationException();
        
        exception = new ConfigurationConcurrentModificationException("bonjour", expectedCause);
        
        assertThat(exception).hasMessage("bonjour").hasCause(expectedCause);
    }
}
