/*
 * **** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2014
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */
package org.dcm4che3.conf.core.adapters;

import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.ConfigurationUnserializableException;
import org.dcm4che3.conf.core.api.internal.AnnotatedConfigurableProperty;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;
import org.dcm4che3.conf.core.api.internal.ConfigTypeAdapter;
import org.dcm4che3.conf.core.validation.ValidationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * @author Roman K
 */
public class DefaultConfigTypeAdapters {

    /**
     * Gets a child node using the name of the provided property, and then looks up the proper adapter and runs it against this child node
     *
     * @param configNode
     * @param property
     * @param vitalizer
     * @param parent
     * @return
     * @throws org.dcm4che3.conf.core.api.ConfigurationException
     */
    public static Object delegateGetChildFromConfigNode(Map<String, Object> configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer, Object parent) throws ConfigurationException {

        Object node;
        if (property.isOlockHash()) {
            // olock prop name is constant
            node = configNode.get(Configuration.OLOCK_HASH_KEY);
        } else
        if (property.isUuid()){
            // uuid prop name is constant
            node = configNode.get(Configuration.UUID_KEY);
        } else {
            // determine node name and get the property
            String nodeName = property.getAnnotatedName();
            node = configNode.get(nodeName);
        }

        // lookup adapter and run it on the property
        ConfigTypeAdapter adapter = vitalizer.lookupTypeAdapter(property);

        // normalize
        node = adapter.normalize(node, property, vitalizer);
        return adapter.fromConfigNode(node, property, vitalizer, parent);
    }

    public static void delegateChildToConfigNode(Object object, Map<String, Object> parentNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
        String nodeName;
        if (property.isOlockHash()) {
            // special case - olock prop name is constant
            nodeName = Configuration.OLOCK_HASH_KEY;
        } else if (property.isUuid()) {
            // special case - uuid prop name is constant
            nodeName = Configuration.UUID_KEY;
        } else {
            nodeName = property.getAnnotatedName();
        }

        ConfigTypeAdapter adapter = vitalizer.lookupTypeAdapter(property);
        Object value = adapter.toConfigNode(object, property, vitalizer);

        // filter out nulls, except olocks
        if (value != null || property.isOlockHash())
            parentNode.put(nodeName, value);
    }

    /**
     * Common Read/Write methods for primitives that have same serialized and deserialized representation, and same
     * write method
     */
    public static class PrimitiveTypeAdapter<T> implements ConfigTypeAdapter<T, T> {
        private final Map<String, Object> metadata = new HashMap<String, Object>();

        /**
         * Assign the type for metadata
         *
         * @param type
         */
        public PrimitiveTypeAdapter(String type) {
            metadata.put("type", type);
        }

        public PrimitiveTypeAdapter() {
        }

        @Override
        public T fromConfigNode(T configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer, Object parent) throws ConfigurationException {
            return configNode;
        }

        @Override
        public T toConfigNode(T object, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationUnserializableException {
            return object;
        }

        /**
         * Constant metadata
         */
        @Override
        public Map<String, Object> getSchema(AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
            return metadata;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T normalize(Object configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
            try {
                if (metadata.get("type").equals("integer")) {
                    return normalizeInt(configNode, property);
                } else if (metadata.get("type").equals("boolean")) {
                   return normalizeBoolean(configNode, property);
                } else if (metadata.get("type").equals("number")) {
                   return normalizeNumber(configNode, property);
                } else return (T) configNode;
            } catch (ConfigurationException ce) {
                throw ce;
            } catch (Exception e) {
                throw new ConfigurationException("Cannot parse node " + configNode, e);
            }
        }
        
        protected T normalizeInt(Object configNode, AnnotatedConfigurableProperty property) throws ConfigurationException {
            if (configNode == null) {
                throw new ConfigurationException("No value found for primitive integer property without default");
            }
            if (configNode.getClass().equals(String.class)) {
                return (T) Integer.valueOf((String) configNode);
            }
            else if (configNode.getClass().equals(Integer.class)) {
                return (T) configNode;
            }
            else {
                throw new ClassCastException(format("Cannot convert config node type %s to primitive integer", configNode.getClass().getName()));
            }
        }
        
        protected T normalizeBoolean(Object configNode, AnnotatedConfigurableProperty property) throws ConfigurationException {
            if (configNode == null && property.getType().equals(boolean.class)) {
                throw new ConfigurationException("No value found for primitive boolean property without default");
            }
            
            // special handling for Boolean's null
            if (configNode == null || configNode.equals("null")) {
                return null;
            }

            if (configNode.getClass().equals(String.class)) {
                return (T) Boolean.valueOf((String) configNode);
            }
            else if (configNode.getClass().equals(Boolean.class)) {
                return (T) configNode;
            }
            else {
                throw new ClassCastException(format("Cannot convert config node type %s to primitive boolean", configNode.getClass().getName()));
            }
        }
        
        protected T normalizeNumber(Object configNode, AnnotatedConfigurableProperty property) throws ConfigurationException {
            if (configNode == null) {
                throw new ConfigurationException("No value found for number property without default");
            }
            
            if (configNode.getClass().equals(String.class)) {
                return (T) Double.valueOf((String) configNode);
            }
            else if (configNode.getClass().equals(Double.class) ||
                    configNode.getClass().equals(Float.class)) {
                return (T) configNode;
            }
            else {
                throw new ClassCastException(format("Cannot convert config node type %s to number", configNode.getClass().getName()));
            }
        }
        
    }
    
    /**
     * Extension of primitive type adapter that can handle missing config values for primitive types.
     * It replaces missing values with either:
     * <ol>
     *   <li>Default values found in the annotation
     *   <li>Java default value for primitive types
     * </ol>
     * 
     * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
     */
    public static class NullablePrimitiveTypeAdapter<T> extends PrimitiveTypeAdapter<T> {

        public NullablePrimitiveTypeAdapter(String type) {
            super(type);
        }

        @Override
        public T fromConfigNode(T configNode, AnnotatedConfigurableProperty property,
                                BeanVitalizer vitalizer, Object parent) throws ConfigurationException {
            return super.fromConfigNode(configNode, property, vitalizer, parent);
        }

        @Override
        protected T normalizeInt(Object configNode, AnnotatedConfigurableProperty property) throws ConfigurationException {
            // Java default value for int: 0
            Object checkedConfigNode = checkIfNoValueAndSetDefaultOrFallback(configNode, property, new Integer(0));
            return super.normalizeInt(checkedConfigNode, property);
        }

        @Override
        protected T normalizeBoolean(Object configNode, AnnotatedConfigurableProperty property) throws ConfigurationException {
            // Java default value for boolean: false
            Object checkedConfigNode = checkIfNoValueAndSetDefaultOrFallback(configNode, property, Boolean.FALSE);
            return super.normalizeBoolean(checkedConfigNode, property);
        }

        @Override
        protected T normalizeNumber(Object configNode, AnnotatedConfigurableProperty property) throws ConfigurationException {
            // use 0.0 as default for other number types
            Object checkedConfigNode = checkIfNoValueAndSetDefaultOrFallback(configNode, property, new Double(0.0));
            return super.normalizeNumber(checkedConfigNode, property);
        }
        
        private Object checkIfNoValueAndSetDefaultOrFallback(Object configNode, AnnotatedConfigurableProperty property, Object fallbackDefValue) {
            if(configNode == null) {
                String defaultValue = property.getAnnotation(ConfigurableProperty.class).defaultValue();
                if (!defaultValue.equals(ConfigurableProperty.NO_DEFAULT_VALUE)) {
                    return defaultValue;
                } else {
                   return fallbackDefValue; 
                }
            }
            
            return configNode;
        }
        
    }


    /**
     * Common Read/Write methods for String representation
     */
    public abstract static class CommonAbstractTypeAdapter<T> implements ConfigTypeAdapter<T, String> {
        protected Map<String, Object> metadata = new HashMap<String, Object>();

        public CommonAbstractTypeAdapter(String type) {
            metadata.put("type", type);
        }


        @Override
        public Map<String, Object> getSchema(AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
            return metadata;
        }

        @Override
        public String normalize(Object configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
            return (String) configNode;
        }


    }

    private static Object getDefaultIfNull(Object configNode, AnnotatedConfigurableProperty property) throws ConfigurationException {
        if (configNode == null) {
            configNode = property.getDefaultValue();
            if (configNode.equals(""))
                throw new ValidationException("Property " + property.getAnnotatedName() + " must have a value");
        }
        return configNode;
    }


    /**
     * Enum - string
     */
    public static class EnumTypeAdapter implements ConfigTypeAdapter<Enum<?>, Object> {

        @Override
        public Enum<?> fromConfigNode(Object configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer, Object parent) throws ConfigurationException {

            try {
                ConfigurableProperty.EnumRepresentation howToRepresent = getEnumRepresentation(property);
                switch (howToRepresent) {
                    case ORDINAL:
                        Enum[] vals = getEnumValues(property);
                        return vals[(Integer) configNode];
                    default:
                    case STRING:
                        Method valueOfMethod = ((Class) property.getType()).getMethod("valueOf", String.class);
                        return (Enum<?>) valueOfMethod.invoke(null, configNode);

                }
            } catch (Exception x) {
                throw new ConfigurationException("Deserialization of Enum failed! field:" + property.getName() + " of type " + property.getType(), x);
            }
        }

        private ConfigurableProperty.EnumRepresentation getEnumRepresentation(AnnotatedConfigurableProperty property) {
            ConfigurableProperty anno = property.getAnnotation(ConfigurableProperty.class);
            return anno == null ? ConfigurableProperty.EnumRepresentation.STRING : anno.enumRepresentation();
        }

        private Enum[] getEnumValues(AnnotatedConfigurableProperty property) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            Method valuesMethod = ((Class) property.getType()).getMethod("values");
            return (Enum[]) valuesMethod.invoke(null);
        }

        @Override
        public Object toConfigNode(Enum<?> object, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationUnserializableException {

            ConfigurableProperty.EnumRepresentation howToRepresent = getEnumRepresentation(property);

            switch (howToRepresent) {
                case ORDINAL:
                    return object.ordinal();
                default:
                case STRING:
                    return object.name();
            }
        }

        @Override
        public Map<String, Object> getSchema(AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
            try {
                Map<String, Object> metadata = new HashMap<String, Object>();

                // if there is no default, then this enum supports null
                if (property.getAnnotation(ConfigurableProperty.class).defaultValue().equals(ConfigurableProperty.NO_DEFAULT_VALUE)) {
                    ArrayList<String> types = new ArrayList<String>();
                    types.add("enum");
                    types.add("null");
                    metadata.put("type", types);
                } else
                    metadata.put("type", "enum");



                metadata.put("class", property.getRawClass().getSimpleName());

                ConfigurableProperty.EnumRepresentation howToRepresent = getEnumRepresentation(property);
                List<String> enumStringValues = new ArrayList<String>();

                for (Enum anEnum : getEnumValues(property)) enumStringValues.add(anEnum.toString());

                if (howToRepresent.equals(ConfigurableProperty.EnumRepresentation.STRING)) {
                    metadata.put("enum", enumStringValues);
                } else if (howToRepresent.equals(ConfigurableProperty.EnumRepresentation.ORDINAL)) {
                    // for ordinal representation - create array of ints with appropriate length, and add a clarifying array with names
                    List<Integer> vals = new ArrayList<Integer>();

                    for (int i = 0; i<getEnumValues(property).length;i++) vals.add(i);
                    metadata.put("enum", vals);
                    metadata.put("enumStrValues", enumStringValues);
                }

                metadata.put("enumRepresentation", howToRepresent.toString());

                return metadata;
            } catch (Exception e) {
                throw new ConfigurationException("Schema export for enum property " + property.getAnnotatedName() + " failed");
            }
        }

        @Override
        public Object normalize(Object configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {

            if (configNode == null) return null;
            switch (property.getAnnotation(ConfigurableProperty.class).enumRepresentation()) {
                case ORDINAL:
                    try {
                        if (configNode.getClass().equals(String.class))
                            return Integer.valueOf((String) configNode);
                        else if (configNode.getClass().equals(Integer.class))
                            return configNode;
                        else
                            throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        throw new ConfigurationException("Expected int ordinal value for enum, got " + configNode.getClass().getName(), e);
                    }
                default:
                case STRING:

                    return configNode;

            }
        }
    }


    public static Map<Class, ConfigTypeAdapter> defaultTypeAdapters;

    static {
        defaultTypeAdapters = new HashMap<Class, ConfigTypeAdapter>();

        defaultTypeAdapters.put(String.class, new PrimitiveTypeAdapter("string"));

        ConfigTypeAdapter integerAdapter = new PrimitiveTypeAdapter("integer");
        defaultTypeAdapters.put(int.class, integerAdapter);
        defaultTypeAdapters.put(Integer.class, integerAdapter);

        ConfigTypeAdapter booleanAdapter = new PrimitiveTypeAdapter("boolean");
        defaultTypeAdapters.put(Boolean.class, booleanAdapter);
        defaultTypeAdapters.put(boolean.class, booleanAdapter);

        ConfigTypeAdapter doubleAdapter = new PrimitiveTypeAdapter("number");
        defaultTypeAdapters.put(double.class, doubleAdapter);
        defaultTypeAdapters.put(float.class, doubleAdapter);
        defaultTypeAdapters.put(Double.class, doubleAdapter);
        defaultTypeAdapters.put(Float.class, doubleAdapter);

        defaultTypeAdapters.put(Map.class, new NullToNullDecorator(new MapTypeAdapter()));
        defaultTypeAdapters.put(Set.class, new NullToNullDecorator(new CollectionTypeAdapter<Set>(LinkedHashSet.class)));
        defaultTypeAdapters.put(EnumSet.class, new NullToNullDecorator(new CollectionTypeAdapter<Set>(LinkedHashSet.class)));
        defaultTypeAdapters.put(List.class, new NullToNullDecorator(new CollectionTypeAdapter<List>(ArrayList.class)));
        defaultTypeAdapters.put(Collection.class, new NullToNullDecorator(new CollectionTypeAdapter<List>(ArrayList.class)));
        defaultTypeAdapters.put(Enum.class, new NullToNullDecorator(new EnumTypeAdapter()));

        defaultTypeAdapters.put(TimeZone.class, new NullToNullDecorator(new TimeZoneTypeAdapter()));
        defaultTypeAdapters.put(TimeUnit.class, new NullToNullDecorator(new TimeUnitTypeAdapter()));

    }

    public static ConfigTypeAdapter get(Class clazz) {
        return defaultTypeAdapters.get(clazz);
    }

}
