package org.dcm4che3.conf.core.tests.adapters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.dcm4che3.conf.core.DefaultBeanVitalizer;
import org.dcm4che3.conf.core.adapters.ReflectiveAdapter;
import org.dcm4che3.conf.core.adapters.ReflectiveAdapter.PropertySchema;
import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.ConfigurableProperty.ConfigurablePropertyType;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.StorageVersionedConfigurableClass;
import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.api.internal.ConfigReflection;
import org.dcm4che3.conf.core.context.LoadingContext;
import org.dcm4che3.conf.core.context.SavingContext;
import org.junit.Test;

public class ReflectiveAdapterTest {
    
    private final Map<String, Object> configNode = new HashMap<>();
    
    private final DefaultBeanVitalizer beanVitalizer = new DefaultBeanVitalizer();
    private final LoadingContext loadingContext = new LoadingContext(beanVitalizer);
    private final SavingContext savingContext = new SavingContext(beanVitalizer);
    
    /**
     * System Under Test (SUT).
     */
    private ReflectiveAdapter<Object> reflectiveAdapter = new ReflectiveAdapter<Object>();

    @Test
    public void fromConfigNode_ReturnsCorrectlyDeserializedObject_GivenValidConfigNodeAndPropertyForConfigurableClass() {
        
        final String expectedStringSetting = "C";
        final Integer expectedIntegerSetting = 88;
        final Boolean expectedBooleanSetting = true;
        
        configNode.put("stringSetting", expectedStringSetting);
        configNode.put("integerSetting", expectedIntegerSetting);
        configNode.put("booleanSetting", expectedBooleanSetting);
        configNode.put("extraValue", "boom");
        
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(TestConfigurableClass.class);
        
        Object actualObject = reflectiveAdapter.fromConfigNode(configNode, property, loadingContext, null);
        
        assertThat("Returned object", actualObject, notNullValue());
        assertThat("Object is wrong type", actualObject, instanceOf(TestConfigurableClass.class));
        
        TestConfigurableClass testConfigurableClass = (TestConfigurableClass) actualObject;
        
        assertThat("String setting", testConfigurableClass.getStringSetting(), equalTo(expectedStringSetting));
        assertThat("Integer setting", testConfigurableClass.getIntegerSetting(), equalTo(expectedIntegerSetting));
        assertThat("Boolean setting", testConfigurableClass.getBooleanSetting(), equalTo(expectedBooleanSetting));
    }
    
    @Test
    public void fromConfigNode_PopulatesStorageVersionAndReturnsDeserializedObject_GivenValidConfigNodeAndPropertyForClassExtendingStorageVersionedConfigurableClass() {
        
        final long expectedVersion = 7L;
        final String expectedSomeConfig = "A";
         
        configNode.put(AStorageVersionedConfigurableClass.PROPERTY_NAME, expectedSomeConfig);
        configNode.put(Configuration.VERSION_KEY, expectedVersion);
        
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(AStorageVersionedConfigurableClass.class);
        
        Object actualObject = reflectiveAdapter.fromConfigNode(configNode, property, loadingContext, null);
        
        assertThat("Returned object", actualObject, notNullValue());
        assertThat("Object is wrong type", actualObject, instanceOf(AStorageVersionedConfigurableClass.class));
        
        AStorageVersionedConfigurableClass storageVersionedConfigurableClass
                = (AStorageVersionedConfigurableClass) actualObject;
        
        assertThat("Wrong version", storageVersionedConfigurableClass.getStorageVersion(), equalTo(expectedVersion));
        assertThat("Some config", storageVersionedConfigurableClass.getSomeConfig(), equalTo(expectedSomeConfig));
    }
    
    @Test
    public void fromConfigNode_DoesNotPopulateStorageVersionAndReturnsDeserializedObject_GivenConfigNodeWithoutVersionAndPropertyForClassExtendingStorageVersionedConfigurableClass() {
        
        final String expectedSomeConfig = "B";
        
        configNode.put(AStorageVersionedConfigurableClass.PROPERTY_NAME, expectedSomeConfig);
        
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(AStorageVersionedConfigurableClass.class);
        
        Object actualObject = reflectiveAdapter.fromConfigNode(configNode, property, loadingContext, null);
        
        assertThat("Returned object", actualObject, notNullValue());
        assertThat("Object is wrong type", actualObject, instanceOf(AStorageVersionedConfigurableClass.class));
        
        AStorageVersionedConfigurableClass storageVersionedConfigurableClass
                = (AStorageVersionedConfigurableClass) actualObject;
        
        assertThat("Wrong version", storageVersionedConfigurableClass.getStorageVersion(), equalTo(0L));
        assertThat("Some config", storageVersionedConfigurableClass.getSomeConfig(), equalTo(expectedSomeConfig));
    }

    @Test
    public void toConfigNode_ReturnsCorrectlySerializedMap_GivenValidObjectAndPropertyForConfigurableClass() {
        
        final String expectedStringSetting = "stringy";
        final Integer expectedIntegerSetting = 999;
        final Boolean expectedBooleanSetting = true;
        
        Map<String, Object> expectedConfigNode = new HashMap<>();
        expectedConfigNode.put(TestConfigurableClass.STRING_PROPERTY_NAME, expectedStringSetting);
        expectedConfigNode.put(TestConfigurableClass.INTEGER_PROPERTY_NAME, expectedIntegerSetting);
        expectedConfigNode.put(TestConfigurableClass.BOOLESN_PROPERTY_NAME, expectedBooleanSetting);
          
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(TestConfigurableClass.class);
        
        TestConfigurableClass objectToSerialize = new TestConfigurableClass();
        objectToSerialize.setStringSetting(expectedStringSetting);
        objectToSerialize.setIntegerSetting(expectedIntegerSetting);
        objectToSerialize.setBooleanSetting(expectedBooleanSetting);
        
        Map<String, Object> actualConfigNode = reflectiveAdapter
                .toConfigNode(objectToSerialize, property, savingContext);
        
        assertThat("Returned map", actualConfigNode, equalTo(expectedConfigNode));
    }
    
    @Test
    public void toConfigNode_PopulateStorageVersionAndReturnsSerializedMap_GivenValidObjectAndPropertyForClassExtendingStorageVersionedConfigurableClass() {
        
        final String expectedSomeConfig = "versionedClass";
        final long expectedStorageVersion = 451;
        
        Map<String, Object> expectedConfigNode = new HashMap<>();
        expectedConfigNode.put(AStorageVersionedConfigurableClass.PROPERTY_NAME, expectedSomeConfig);
        expectedConfigNode.put(Configuration.VERSION_KEY, expectedStorageVersion);
        
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(AStorageVersionedConfigurableClass.class);
        
        AStorageVersionedConfigurableClass objectToSerialize = new AStorageVersionedConfigurableClass();
        objectToSerialize.setSomeConfig(expectedSomeConfig);
        objectToSerialize.setStorageVersion(expectedStorageVersion);
        
        Map<String, Object> actualConfigNode = reflectiveAdapter
            .toConfigNode(objectToSerialize, property, savingContext);
        
        assertThat("Returned map", actualConfigNode, equalTo(expectedConfigNode));
    }
    
    @Test
    public void getSchema_ReturnsCorrectlyPopulatedMap_GivenPropertyForConfigurableClass() {
        
        Map<String, Object> expectedProperties = new HashMap<>();
        addExpectedConfigurableProperty(expectedProperties, TestConfigurableClass.STRING_PROPERTY_NAME, "string");
        addExpectedConfigurableProperty(expectedProperties, TestConfigurableClass.INTEGER_PROPERTY_NAME, "integer");
        addExpectedConfigurableProperty(expectedProperties, TestConfigurableClass.BOOLESN_PROPERTY_NAME, "boolean");
        
        Map<String, Object> expectedSchema = new HashMap<>();
        expectedSchema.put(PropertySchema.TYPE_KEY, "object");
        expectedSchema.put(PropertySchema.CLASS_KEY, "TestConfigurableClass");
        expectedSchema.put(PropertySchema.PROPERTIES_KEY, expectedProperties);
          
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(TestConfigurableClass.class);
        
        Map<String, Object> actualSchema = reflectiveAdapter.getSchema(property, loadingContext);
        
        assertThat("Returned map", actualSchema, equalTo(expectedSchema));
    }
    
    @Test
    public void getSchema_ReturnsCorrectlyPopulatedMap_GivenPropertyForConfigurableClassWithOlockHash() {
        
        Map<String, Object> expectedProperties = new HashMap<>();
        addExpectedConfigurableProperty(expectedProperties, Configuration.OLOCK_HASH_KEY, "string", true);
        
        Map<String, Object> expectedSchema = new HashMap<>();
        expectedSchema.put(PropertySchema.TYPE_KEY, "object");
        expectedSchema.put(PropertySchema.CLASS_KEY, "OlockHashConfigurableClass");
        expectedSchema.put(PropertySchema.PROPERTIES_KEY, expectedProperties);
          
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(OlockHashConfigurableClass.class);
        
        Map<String, Object> actualSchema = reflectiveAdapter.getSchema(property, loadingContext);
        
        assertThat("Returned map", actualSchema, equalTo(expectedSchema));
    }
    
    @Test
    public void getSchema_ReturnsCorrectlyPopulatedMap_GivenPropertyForConfigurableClassWithOrderedProperties() {
        
        Map<String, Object> orderOnePropertyExtraValues = new HashMap<>();
        orderOnePropertyExtraValues.put(PropertySchema.UI_ORDER_KEY, 1);
        orderOnePropertyExtraValues.put(PropertySchema.UI_GROUP_KEY, "Fun group");
        
        Map<String, Object> expectedProperties = new HashMap<>();
        addExpectedConfigurableProperty(expectedProperties, "orderOne", "string").putAll(orderOnePropertyExtraValues);
        addExpectedConfigurableProperty(expectedProperties, "orderTwo", "string").put(PropertySchema.UI_ORDER_KEY, 2);
        
        Map<String, Object> expectedSchema = new HashMap<>();
        expectedSchema.put(PropertySchema.TYPE_KEY, "object");
        expectedSchema.put(PropertySchema.CLASS_KEY, "GuiOrderedConfigurableClass");
        expectedSchema.put(PropertySchema.PROPERTIES_KEY, expectedProperties);
          
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(GuiOrderedConfigurableClass.class);
        
        Map<String, Object> actualSchema = reflectiveAdapter.getSchema(property, loadingContext);
        
        assertThat("Returned map", actualSchema, equalTo(expectedSchema));
    }
    
    @Test
    public void getSchema_ReturnsCorrectlyPopulatedMap_GivenPropertyForClassExtendingStorageVersionedConfigurableClass() {
        
        Map<String, Object> expectedProperties = new HashMap<>();
        addExpectedConfigurableProperty(
            expectedProperties, AStorageVersionedConfigurableClass.PROPERTY_NAME, "string");
        addExpectedConfigurableProperty(expectedProperties, Configuration.VERSION_KEY, "integer", true);
        
        Map<String, Object> expectedSchema = new HashMap<>();
        expectedSchema.put(PropertySchema.TYPE_KEY, "object");
        expectedSchema.put(PropertySchema.CLASS_KEY, "AStorageVersionedConfigurableClass");
        expectedSchema.put(PropertySchema.PROPERTIES_KEY, expectedProperties);
          
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(AStorageVersionedConfigurableClass.class);
        
        Map<String, Object> actualSchema = reflectiveAdapter.getSchema(property, loadingContext);
        
        assertThat("Returned map", actualSchema, equalTo(expectedSchema));
    }
    
    private Map<String, Object> addExpectedConfigurableProperty(
            final Map<String, Object> expectedProperties,
            final String propertyName,
            final String propertyType) {
    
        return addExpectedConfigurableProperty(expectedProperties, propertyName, propertyType, propertyName, null);
    }
    
    private Map<String, Object> addExpectedConfigurableProperty(
            final Map<String, Object> expectedProperties,
            final String propertyName,
            final String propertyType,
            final boolean propertyReadOnly) {
    
        return addExpectedConfigurableProperty(expectedProperties, propertyName, propertyType, null, propertyReadOnly);
    }
    
    private Map<String, Object> addExpectedConfigurableProperty(
            final Map<String, Object> expectedProperties,
            final String propertyName,
            final String propertyType,
            final String propertyTitle,
            final Boolean propertyReadOnly) {

        Map<String, Object> propertySchema = new HashMap<>();
        
        propertySchema.put(PropertySchema.TYPE_KEY, propertyType);
        propertySchema.put(PropertySchema.UI_GROUP_KEY, "Other");
        
        if (propertyTitle != null) {
            propertySchema.put(PropertySchema.TITLE_KEY, propertyTitle);
        }
        
        if (propertyReadOnly != null) {
            propertySchema.put(PropertySchema.READONLY_KEY, propertyReadOnly);    
        }
        
        expectedProperties.put(propertyName, propertySchema);
        
        return propertySchema;
    }

    @ConfigurableClass
    public static final class TestConfigurableClass {
        
        public static final String STRING_PROPERTY_NAME = "stringSetting";
        public static final String INTEGER_PROPERTY_NAME = "integerSetting";
        public static final String BOOLESN_PROPERTY_NAME = "booleanSetting";
        
        @ConfigurableProperty
        private String stringSetting;

        @ConfigurableProperty
        private Integer integerSetting;
        
        @ConfigurableProperty
        private Boolean booleanSetting;

        public String getStringSetting() {

            return stringSetting;
        }

        public void setStringSetting(String stringSetting) {

            this.stringSetting = stringSetting;
        }

        public Integer getIntegerSetting() {

            return integerSetting;
        }

        public void setIntegerSetting(Integer integerSetting) {

            this.integerSetting = integerSetting;
        }

        public Boolean getBooleanSetting() {

            return booleanSetting;
        }

        public void setBooleanSetting(Boolean booleanSetting) {

            this.booleanSetting = booleanSetting;
        }
    }
    
    @ConfigurableClass
    public static final class OlockHashConfigurableClass {
        
        @ConfigurableProperty(type = ConfigurablePropertyType.OptimisticLockingHash)
        private String olockHash;

        public String getOlockHash() {

            return olockHash;
        }

        public void setOl(String olockHash) {

            this.olockHash = olockHash;
        }
    }
    
    @ConfigurableClass
    public static final class GuiOrderedConfigurableClass {
        
        @ConfigurableProperty(order = 2)
        private String orderTwo;
        
        @ConfigurableProperty(order = 1, group = "Fun group")
        private String orderOne;
        
        public String getOrderTwo() {

            return orderTwo;
        }

        public void setOrderTwo(String orderTwo) {

            this.orderTwo = orderTwo;
        }

        public String getOrderOne() {

            return orderOne;
        }

        public void setOrderOne(String orderOne) {

            this.orderOne = orderOne;
        }
    }
    
    @ConfigurableClass
    public static final class AStorageVersionedConfigurableClass extends StorageVersionedConfigurableClass {
        
        public static final String PROPERTY_NAME = "someConfig";
        
        @ConfigurableProperty
        private String someConfig;

        public String getSomeConfig() {

            return someConfig;
        }

        public void setSomeConfig(String someConfig) {

            this.someConfig = someConfig;
        }
    }
}
