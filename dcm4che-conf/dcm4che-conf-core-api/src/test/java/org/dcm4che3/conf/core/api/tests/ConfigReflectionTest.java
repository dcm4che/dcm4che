package org.dcm4che3.conf.core.api.tests;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.ConfigurableProperty.ConfigurablePropertyType;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.api.internal.ConfigReflection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ConfigReflectionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Test
    public void getOlockHashPropertyForClass_ThrowsIllegalArgumentException_GivenNonConfigurableClass() {

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Class 'org.dcm4che3.conf.core.api.tests.ConfigReflectionTest' is"
                + " not a configurable class. Make sure the a dependency to org.dcm4che.conf.core-api exists.");
        
        ConfigReflection.getOlockHashPropertyForClass(getClass());
    }
    
    @Test
    public void getOlockHashPropertyForClass_ReturnsNull_GivenConfigurableClassWithoutOptimisticLockingHashProperty() {

        assertThat(
            "Property",
            ConfigReflection.getOlockHashPropertyForClass(NonOlockedConfigurableClass.class),
            nullValue());
    }
    
    @Test
    public void getOlockHashPropertyForClass_ReturnsCorrectProperty_GivenConfigurableClassWithOptimisticLockingHashProperty() {

        ConfigProperty actualProperty = ConfigReflection.getOlockHashPropertyForClass(OlockedConfigurableClass.class);
        
        assertThat("Property", actualProperty, notNullValue());
        assertThat("Propety name", actualProperty.getName(), equalTo("olockHash"));
        assertThat("Propety annotated name", actualProperty.getAnnotatedName(), equalTo(Configuration.OLOCK_HASH_KEY));
        assertThat("Propety raw type", actualProperty.getRawClass(), equalTo(String.class));
    }
    
    @ConfigurableClass
    protected static class NonOlockedConfigurableClass {
        
        @ConfigurableProperty
        public String string;
    }
    
    @ConfigurableClass
    protected static final class OlockedConfigurableClass extends NonOlockedConfigurableClass {
        
        @ConfigurableProperty(type = ConfigurablePropertyType.OptimisticLockingHash)
        public String olockHash;
    }
}
