package org.dcm4che3.conf.core.tests.adapters;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.junit.jupiter.api.Test;

class ReflectiveAdapterTest {
    
    private static final String STRING_PROPERTY_NAME = "stringSetting";
    private static final String INTEGER_PROPERTY_NAME = "integerSetting";
    private static final String BOOLEAN_PROPERTY_NAME = "booleanSetting";
    
    private final Map<String, Object> configNode = new HashMap<>();
    
    private final DefaultBeanVitalizer beanVitalizer = new DefaultBeanVitalizer();
    private final LoadingContext loadingContext = new LoadingContext(beanVitalizer);
    private final SavingContext savingContext = new SavingContext(beanVitalizer);
    
    /**
     * System Under Test (SUT).
     */
    private ReflectiveAdapter<Object> reflectiveAdapter = new ReflectiveAdapter<Object>();

    @Test
    void fromConfigNode_ReturnsCorrectlyDeserializedObject_GivenValidConfigNodeAndPropertyForConfigurableClass() {
        
        final String stringSetting = "C";
        final Integer integerSetting = 88;
        
        configNode.put("stringSetting", stringSetting);
        configNode.put("integerSetting", integerSetting);
        configNode.put("booleanSetting", true);
        configNode.put("extraValue", "boom");
        
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(TestConfigurableClass.class);
        
        final Object actualObject = reflectiveAdapter.fromConfigNode(configNode, property, loadingContext, null);
        
        assertThat(actualObject).as("Returned object").isExactlyInstanceOf(TestConfigurableClass.class);
        
        TestConfigurableClass testConfigurableClass = (TestConfigurableClass) actualObject;
        
        assertThat(testConfigurableClass.getStringSetting()).as("String setting").isEqualTo(stringSetting);
        assertThat(testConfigurableClass.getIntegerSetting()).as("Integer setting").isEqualTo(integerSetting);
        assertThat(testConfigurableClass.getBooleanSetting()).as("Boolean setting").isTrue();
    }
    
    @Test
    void fromConfigNode_PopulatesStorageVersionAndReturnsDeserializedObject_GivenValidConfigNodeAndPropertyForClassExtendingStorageVersionedConfigurableClass() {
        
        final long version = 7L;
        final String someConfig = "A";
         
        configNode.put(AStorageVersionedConfigurableClass.PROPERTY_NAME, someConfig);
        configNode.put(Configuration.VERSION_KEY, version);
        
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(AStorageVersionedConfigurableClass.class);
        
        final Object actualObject = reflectiveAdapter.fromConfigNode(configNode, property, loadingContext, null);
        
        assertThat(actualObject).as("Returned object").isExactlyInstanceOf(AStorageVersionedConfigurableClass.class);
        
        AStorageVersionedConfigurableClass storageVersionedConfigurableClass
                = (AStorageVersionedConfigurableClass) actualObject;
        
        assertThat(storageVersionedConfigurableClass.getStorageVersion()).as("Storage version").isEqualTo(version);
        assertThat(storageVersionedConfigurableClass.getSomeConfig()).as("Some config").isEqualTo(someConfig);
    }
    
    @Test
    void fromConfigNode_PopulatesStorageVersionAndReturnsDeserializedObject_GivenValidConfigNodeWithIntegerVersionAndPropertyForClassExtendingStorageVersionedConfigurableClass() {
        
        final long version = 8L;
        final String someConfig = "B";
         
        configNode.put(AStorageVersionedConfigurableClass.PROPERTY_NAME, someConfig);
        configNode.put(Configuration.VERSION_KEY, (int) version);
        
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(AStorageVersionedConfigurableClass.class);
        
        final Object actualObject = reflectiveAdapter.fromConfigNode(configNode, property, loadingContext, null);
        
        assertThat(actualObject).as("Returned object").isExactlyInstanceOf(AStorageVersionedConfigurableClass.class);
        
        AStorageVersionedConfigurableClass storageVersionedConfigurableClass
                = (AStorageVersionedConfigurableClass) actualObject;
        
        assertThat(storageVersionedConfigurableClass.getStorageVersion()).as("Storage version").isEqualTo(version);
        assertThat(storageVersionedConfigurableClass.getSomeConfig()).as("Some config").isEqualTo(someConfig);
    }
    
    @Test
    void fromConfigNode_DoesNotPopulateStorageVersionAndReturnsDeserializedObject_GivenConfigNodeWithoutVersionAndPropertyForClassExtendingStorageVersionedConfigurableClass() {
        
        final String someConfig = "C";
        
        configNode.put(AStorageVersionedConfigurableClass.PROPERTY_NAME, someConfig);
        
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(AStorageVersionedConfigurableClass.class);
        
        final Object actualObject = reflectiveAdapter.fromConfigNode(configNode, property, loadingContext, null);
        
        assertThat(actualObject).as("Returned object").isExactlyInstanceOf(AStorageVersionedConfigurableClass.class);
        
        AStorageVersionedConfigurableClass storageVersionedConfigurableClass
                = (AStorageVersionedConfigurableClass) actualObject;
        
        assertThat(storageVersionedConfigurableClass.getStorageVersion()).as("Storage version").isZero();
        assertThat(storageVersionedConfigurableClass.getSomeConfig()).as("Some config").isEqualTo(someConfig);
    }

    @Test
    void toConfigNode_ReturnsCorrectlySerializedMap_GivenValidObjectAndPropertyForConfigurableClass() {
        
        final String expectedStringSetting = "stringy";
        final Integer expectedIntegerSetting = 999;
        final Boolean expectedBooleanSetting = true;
          
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(TestConfigurableClass.class);
        
        TestConfigurableClass objectToSerialize = new TestConfigurableClass();
        objectToSerialize.setStringSetting(expectedStringSetting);
        objectToSerialize.setIntegerSetting(expectedIntegerSetting);
        objectToSerialize.setBooleanSetting(expectedBooleanSetting);
        
        final Map<String, Object> actualConfigNode = reflectiveAdapter
                .toConfigNode(objectToSerialize, property, savingContext);
        
        Map<String, Object> expectedConfigNode = new HashMap<>();
        expectedConfigNode.put(STRING_PROPERTY_NAME, expectedStringSetting);
        expectedConfigNode.put(INTEGER_PROPERTY_NAME, expectedIntegerSetting);
        expectedConfigNode.put(BOOLEAN_PROPERTY_NAME, expectedBooleanSetting);
        
        assertThat(actualConfigNode).isEqualTo(expectedConfigNode);
    }
    
    @Test
    void toConfigNode_PopulateStorageVersionAndReturnsSerializedMap_GivenValidObjectAndPropertyForClassExtendingStorageVersionedConfigurableClass() {
        
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
        
        assertThat(actualConfigNode).isEqualTo(expectedConfigNode);
    }

    @Test
    void getSchema_ReturnsCorrectlyPopulatedMap_GivenPropertyForConfigurableClass() {
          
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(TestConfigurableClass.class);
        
        final Map<String, Object> actualSchema = reflectiveAdapter.getSchema(property, loadingContext);
        
        Map<String, Object> expectedProperties = new HashMap<>();
        addPropertySchema(expectedProperties, schemaBuilder(STRING_PROPERTY_NAME, "string"));
        addPropertySchema(expectedProperties, schemaBuilder(INTEGER_PROPERTY_NAME, "integer"));
        addPropertySchema(expectedProperties, schemaBuilder(BOOLEAN_PROPERTY_NAME, "boolean"));
        
        Map<String, Object> expectedSchema = new HashMap<>();
        expectedSchema.put(PropertySchema.TYPE_KEY, "object");
        expectedSchema.put(PropertySchema.CLASS_KEY, "TestConfigurableClass");
        expectedSchema.put(PropertySchema.PROPERTIES_KEY, expectedProperties);
        
        assertThat(actualSchema).isEqualTo(expectedSchema);
    }
    
    @Test
    void getSchema_ReturnsCorrectlyPopulatedMap_GivenPropertyForConfigurableClassWithRequiredProperties() {
          
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(RequiredPropertiesConfigurableClass.class);
        
        final Map<String, Object> actualSchema = reflectiveAdapter.getSchema(property, loadingContext);
        
        Map<String, Object> expectedProperties = new HashMap<>();
        addPropertySchema(expectedProperties, schemaBuilder(STRING_PROPERTY_NAME, "string").withRequired(true));
        addPropertySchema(expectedProperties, schemaBuilder(INTEGER_PROPERTY_NAME, "integer").withRequired(true));
        addPropertySchema(expectedProperties, schemaBuilder(BOOLEAN_PROPERTY_NAME, "boolean").withRequired(true));
        
        Map<String, Object> stringsItems = new HashMap<>();
        stringsItems.put(PropertySchema.TYPE_KEY, "string");
        
        addPropertySchema(expectedProperties, schemaBuilder(
                RequiredPropertiesConfigurableClass.STRINGS_PROPERTY_NAME, "array")
                    .withRequired(true)
                    .with("items", stringsItems));
        
        Map<String, Object> expectedSchema = new HashMap<>();
        expectedSchema.put(PropertySchema.TYPE_KEY, "object");
        expectedSchema.put(PropertySchema.CLASS_KEY, "RequiredPropertiesConfigurableClass");
        expectedSchema.put(PropertySchema.PROPERTIES_KEY, expectedProperties);
        
        assertThat(actualSchema).isEqualTo(expectedSchema);
    }
    
    @Test
    void getSchema_ReturnsCorrectlyPopulatedMap_GivenPropertyForConfigurableClassWithOlockHash() {
          
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(OlockHashConfigurableClass.class);
        
        final Map<String, Object> actualSchema = reflectiveAdapter.getSchema(property, loadingContext);
        
        Map<String, Object> expectedProperties = new HashMap<>();
        addPropertySchema(expectedProperties, schemaBuilder(Configuration.OLOCK_HASH_KEY, "string")
                .withoutTitle()
                .withoutRequired()
                .withReadOnly(true));
        
        Map<String, Object> expectedSchema = new HashMap<>();
        expectedSchema.put(PropertySchema.TYPE_KEY, "object");
        expectedSchema.put(PropertySchema.CLASS_KEY, "OlockHashConfigurableClass");
        expectedSchema.put(PropertySchema.PROPERTIES_KEY, expectedProperties);
        
        assertThat(actualSchema).isEqualTo(expectedSchema);
    }
    
    @Test
    void getSchema_ReturnsCorrectlyPopulatedMap_GivenPropertyForConfigurableClassWithOrderedProperties() {
          
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(GuiOrderedConfigurableClass.class);
        
        final Map<String, Object> actualSchema = reflectiveAdapter.getSchema(property, loadingContext);
        
        Map<String, Object> orderOnePropertyExtraValues = new HashMap<>();
        orderOnePropertyExtraValues.put(PropertySchema.UI_ORDER_KEY, 1);
        orderOnePropertyExtraValues.put(PropertySchema.UI_GROUP_KEY, "Fun group");
        
        Map<String, Object> expectedProperties = new HashMap<>();
        addPropertySchema(expectedProperties, schemaBuilder("orderOne", "string").with(orderOnePropertyExtraValues));
        addPropertySchema(expectedProperties, schemaBuilder("orderTwo", "string").withOrder(2));
        
        Map<String, Object> expectedSchema = new HashMap<>();
        expectedSchema.put(PropertySchema.TYPE_KEY, "object");
        expectedSchema.put(PropertySchema.CLASS_KEY, "GuiOrderedConfigurableClass");
        expectedSchema.put(PropertySchema.PROPERTIES_KEY, expectedProperties);
        
        assertThat(actualSchema).isEqualTo(expectedSchema);
    }
    
    @Test
    void getSchema_ReturnsCorrectlyPopulatedMap_GivenPropertyForClassExtendingStorageVersionedConfigurableClass() {
          
        ConfigProperty property = ConfigReflection.getDummyPropertyForClass(AStorageVersionedConfigurableClass.class);
        
        final Map<String, Object> actualSchema = reflectiveAdapter.getSchema(property, loadingContext);
        
        Map<String, Object> expectedProperties = new HashMap<>();
        addPropertySchema(expectedProperties, schemaBuilder(
                AStorageVersionedConfigurableClass.PROPERTY_NAME, "string"));
        addPropertySchema(expectedProperties, schemaBuilder(Configuration.VERSION_KEY, "integer")
                .withoutTitle()
                .withoutRequired()
                .withReadOnly(true));
        
        Map<String, Object> expectedSchema = new HashMap<>();
        expectedSchema.put(PropertySchema.TYPE_KEY, "object");
        expectedSchema.put(PropertySchema.CLASS_KEY, "AStorageVersionedConfigurableClass");
        expectedSchema.put(PropertySchema.PROPERTIES_KEY, expectedProperties);
        
        assertThat(actualSchema).isEqualTo(expectedSchema);
    }
    
    private static void addPropertySchema(
            Map<String, Object> expectedProperties,
            PropertySchemaBuilder builder) {

        expectedProperties.put(builder.getPropertyName(), builder.build());
    }
    
    private static PropertySchemaBuilder schemaBuilder(String propertyName, String type) {
        
        return new PropertySchemaBuilder(propertyName, type);
    }

    @ConfigurableClass
    public static final class TestConfigurableClass {
        
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
    public static final class AStorageVersionedConfigurableClass extends StorageVersionedConfigurableClass {
        
        private static final long serialVersionUID = 1L;

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
    
    @ConfigurableClass
    public static final class RequiredPropertiesConfigurableClass {
        
        public static final String STRINGS_PROPERTY_NAME = "stringsSetting";
        
        @ConfigurableProperty(required = true)
        private String stringSetting;

        @ConfigurableProperty(required = true)
        private Integer integerSetting;
        
        @ConfigurableProperty(required = true)
        private Boolean booleanSetting;
        
        @ConfigurableProperty(required = true)
        private String[] stringsSetting;
    }
    
    @ConfigurableClass
    public static final class OlockHashConfigurableClass {
        
    	/**
    	 * The required attribute should be ignored and set to false in the schema.
    	 */
        @ConfigurableProperty(type = ConfigurablePropertyType.OptimisticLockingHash, required = true)
        private String olockHash;
    }
    
    @ConfigurableClass
    public static final class GuiOrderedConfigurableClass {
        
        @ConfigurableProperty(order = 2)
        private String orderTwo;
        
        @ConfigurableProperty(order = 1, group = "Fun group")
        private String orderOne;
    }
}
