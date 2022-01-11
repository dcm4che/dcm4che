package org.dcm4che3.conf.core.tests.olock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.ConfigurableProperty.ConfigurablePropertyType;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.olock.HashBasedOptimisticLockingConfigurationDecorator;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class HashBasedOptimisticLockingConfigurationDecoratorTest extends EasyMockSupport {
    
    private Configuration mockConfiguration = createMock(Configuration.class);
    
    @SuppressWarnings("rawtypes")
    private HashBasedOptimisticLockingConfigurationDecorator decorator =
            new HashBasedOptimisticLockingConfigurationDecorator(mockConfiguration , new ArrayList<Class>());
    
    @Test
    public void getConfigurationNode_DoesNotCalculateHashesAndReturnsValidNode_GivenRoothPathAndNullConfigurableClass() {

        final Map<String, Object> expectedConfigurationNode = new HashMap<>();
        
        EasyMock.expect(
                mockConfiguration.getConfigurationNode(Path.ROOT, null))
            .andReturn(new HashMap<String, Object>());
        
        replayAll();
        
        Map<String, Object> actualConfigurationNode =
            (Map<String, Object>) decorator.getConfigurationNode(Path.ROOT, null);
        
        assertThat("Actual configuration node", actualConfigurationNode, equalTo(expectedConfigurationNode));
        verifyAll();
    }
    
    @Test
    public void getConfigurationNode_DoesNotCalculateHashesAndReturnsNull_GivenValidPathAndConfigurableClassButStorageReturnsNull() {

        final Path path = new Path("some", "path");
        
        EasyMock.expect(mockConfiguration.getConfigurationNode(path, SimpleConfigurableClass.class)).andReturn(null);
        replayAll();
        
        Map<String, Object> actualConfigurationNode =
            (Map<String, Object>) decorator.getConfigurationNode(path, SimpleConfigurableClass.class);
        
        assertThat("Actual configuration node", actualConfigurationNode, nullValue());
        verifyAll();
    }
    
    @Test
    public void getConfigurationNode_DoesNotCalculateHashesAndReturnsValidNode_GivenValidPathAndConfigurableClassWithoutOlockProperty() {

        final Path path = new Path("A", "B", "C");
        
        final Map<String, Object> expectedConfigurationNode = new HashMap<>();
        expectedConfigurationNode.put("setting", "OK");
        
        EasyMock.expect(
                mockConfiguration.getConfigurationNode(path, SimpleConfigurableClass.class))
            .andReturn(new HashMap<String, Object>(expectedConfigurationNode));
        
        replayAll();
        
        Map<String, Object> actualConfigurationNode =
            (Map<String, Object>) decorator.getConfigurationNode(path, SimpleConfigurableClass.class);
        
        assertThat("Actual configuration node", actualConfigurationNode, equalTo(expectedConfigurationNode));
        verifyAll();
    }
    
    @Test
    public void getConfigurationNode_CalculatesCorrectHashAndReturnsValidNode_GivenValidPathAndConfigurableClassWithOlockProperty() {

        final Path path = new Path("path");
        
        final Map<String, Object> expectedConfigurationNode = new HashMap<>();
        expectedConfigurationNode.put("setting", "olock");
        
        EasyMock.expect(
                mockConfiguration.getConfigurationNode(path, OLockedConfigurableClass.class))
            .andReturn(new HashMap<String, Object>(expectedConfigurationNode));
        
        replayAll();
        
        Map<String, Object> actualConfigurationNode =
            (Map<String, Object>) decorator.getConfigurationNode(path, OLockedConfigurableClass.class);
        
        // Set the correct expectations.
        expectedConfigurationNode.put(Configuration.OLOCK_HASH_KEY, "W+Hx4/orY42R3zzSYnHd/BKjirY=");
        
        assertThat("Actual configuration node", actualConfigurationNode, equalTo(expectedConfigurationNode));
        verifyAll();
    }

    @Test
    public void getConfigurationNode_CalculatesCorrectHashIgnoringVersionAndReturnsValidNode_GivenValidPathAndConfigurableClassWithOlockProperty() {

        final Path path = new Path("path");
        
        final Map<String, Object> expectedConfigurationNode = new HashMap<>();
        expectedConfigurationNode.put("setting", "olock");
        expectedConfigurationNode.put(Configuration.VERSION_KEY, 5);
        
        EasyMock.expect(
                mockConfiguration.getConfigurationNode(path, OLockedConfigurableClass.class))
            .andReturn(new HashMap<String, Object>(expectedConfigurationNode));
        
        replayAll();
        
        Map<String, Object> actualConfigurationNode =
            (Map<String, Object>) decorator.getConfigurationNode(path, OLockedConfigurableClass.class);
        
        // Set the correct expectations, which should be exactly same hash as previous test.
        expectedConfigurationNode.put(Configuration.OLOCK_HASH_KEY, "W+Hx4/orY42R3zzSYnHd/BKjirY=");
        
        assertThat("Actual configuration node", actualConfigurationNode, equalTo(expectedConfigurationNode));
        verifyAll();
    }
    
    @Test
    public void getConfigurationNode_IsTheOnlyMethodImplementedDirectly_OnTheDecorator() {
        
        Method[] declaredMethods = HashBasedOptimisticLockingConfigurationDecorator.class.getDeclaredMethods();
        
        assertThat("There should be only one declarted method", declaredMethods.length, equalTo(1));
        assertThat("Wrong method declared", declaredMethods[0].getName(), equalTo("getConfigurationNode"));
    }
    
    @ConfigurableClass
    public static class SimpleConfigurableClass {
        
        @ConfigurableProperty
        public String setting;
    }
    
    @ConfigurableClass
    public static final class OLockedConfigurableClass extends SimpleConfigurableClass {
        
        @ConfigurableProperty(type = ConfigurablePropertyType.OptimisticLockingHash)
        public String olockHash;
    }
}
