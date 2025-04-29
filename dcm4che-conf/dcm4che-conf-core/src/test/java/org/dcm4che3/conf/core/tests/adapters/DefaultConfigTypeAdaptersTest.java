package org.dcm4che3.conf.core.tests.adapters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che3.conf.core.adapters.DefaultConfigTypeAdapters.EnumTypeAdapter;
import org.dcm4che3.conf.core.adapters.ReflectiveAdapter.PropertySchema;
import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.api.internal.ConfigReflection;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DefaultConfigTypeAdaptersTest {

	@Nested
	class EnumTypeAdapterTests {
	
		/**
	     * System Under Test (SUT).
	     */
	    private EnumTypeAdapter adapter = new EnumTypeAdapter();
	    
		@Test
		void getSchema_ReturnsCorrectlyPopulatedMap_GivenValidEnumConfigProperty() {

			List<ConfigProperty> configurableFields = ConfigReflection.getAllConfigurableFields(TestEnumClass.class);

			final Map<String, Object> actualSchema = adapter.getSchema(configurableFields.get(0), null);
			
	        Map<String, Object> expectedSchema = new HashMap<>();
	        expectedSchema.put(PropertySchema.TYPE_KEY, Arrays.asList("enum", "null"));
	        expectedSchema.put(PropertySchema.CLASS_KEY, "TestEnum");
	        expectedSchema.put("enum", Arrays.asList("ONE", "TWO"));
	        expectedSchema.put("enumRepresentation", ConfigurableProperty.EnumRepresentation.STRING.toString());
	        
			assertThat(actualSchema).isEqualTo(expectedSchema);
		}
		
		@Test
		void getSchema_ReturnsCorrectlyPopulatedMap_GivenRequiredEnumConfigProperty() {

			List<ConfigProperty> configurableFields = ConfigReflection.getAllConfigurableFields(RequiredEnumClass.class);

			final Map<String, Object> actualSchema = adapter.getSchema(configurableFields.get(0), null);
			
	        Map<String, Object> expectedSchema = new HashMap<>();
	        expectedSchema.put(PropertySchema.TYPE_KEY, "enum");
	        expectedSchema.put(PropertySchema.CLASS_KEY, "TestEnum");
	        expectedSchema.put("enum", Arrays.asList("ONE", "TWO"));
	        expectedSchema.put("enumRepresentation", ConfigurableProperty.EnumRepresentation.STRING.toString());
	        
			assertThat(actualSchema).isEqualTo(expectedSchema);
		}
		
		@Test
		void getSchema_ReturnsCorrectlyPopulatedMap_GivenEnumConfigPropertyWithOrdinalRepresentation() {

			List<ConfigProperty> configurableFields = ConfigReflection.getAllConfigurableFields(OrdinalEnumClass.class);

			final Map<String, Object> actualSchema = adapter.getSchema(configurableFields.get(0), null);
			
	        Map<String, Object> expectedSchema = new HashMap<>();
	        expectedSchema.put(PropertySchema.TYPE_KEY, Arrays.asList("enum", "null"));
	        expectedSchema.put(PropertySchema.CLASS_KEY, "TestEnum");
	        expectedSchema.put("enum", Arrays.asList(0, 1));
	        expectedSchema.put("enumStrValues", Arrays.asList("ONE", "TWO"));
	        expectedSchema.put("enumRepresentation", ConfigurableProperty.EnumRepresentation.ORDINAL.toString());
	        
			assertThat(actualSchema).isEqualTo(expectedSchema);
		}
		
		@Test
		void getSchema_ThrowsConfigurationException_WhenRuntimeExceptionIsThrown() {

			ConfigProperty property = new ConfigProperty(TestEnumClass.class);
			
			assertThatThrownBy(() -> adapter.getSchema(property, null))
					.isExactlyInstanceOf(ConfigurationException.class)
					.hasMessage("Schema export for enum property 'dummy' failed.")
					.hasCauseExactlyInstanceOf(NullPointerException.class);
		}
		
		@ConfigurableClass
		public final class TestEnumClass {
			
			@ConfigurableProperty
			private TestEnum enumProperty;
		}
		
		@ConfigurableClass
		public final class RequiredEnumClass {
			
			@ConfigurableProperty(required = true)
			private TestEnum enumProperty;
		}
		
		@ConfigurableClass
		public final class OrdinalEnumClass {
			
			@ConfigurableProperty(enumRepresentation = ConfigurableProperty.EnumRepresentation.ORDINAL)
			private TestEnum enumProperty;
		}
	}
	
	public enum TestEnum {
		
		ONE, TWO;
	}
}
