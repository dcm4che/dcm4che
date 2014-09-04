/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2014
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4che3.conf.api.generic.adapters;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.generic.ConfigField;
import org.dcm4che3.conf.api.generic.ReflectiveConfig;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigReader;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigTypeAdapter;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigWriter;
import org.dcm4che3.data.Code;
import org.dcm4che3.net.Device;
import org.dcm4che3.util.AttributesFormat;

/**
 * 
 * @author Roman K
 * 
 */
public class DefaultConfigTypeAdapters {

    /**
     * Common Read/Write methods for primitives that have same serialized and deserialized representation, and same
     * write method
     */
    public abstract static class PrimitiveAbstractTypeAdapter<T> implements ConfigTypeAdapter<T, T> {

        Map<String, Object> metadata =  new HashMap<String, Object>();

        @Override
        public boolean isWritingChildren(Field field) {
            return false;
        }

        @Override
        public T deserialize(T serialized, ReflectiveConfig config, Field field) throws ConfigurationException {
            return serialized;
        }

        @Override
        public T serialize(T obj, ReflectiveConfig config, Field field) throws ConfigurationException {
            return obj;
        }

        @Override
        public void write(T serialized, ReflectiveConfig config, ConfigWriter writer, Field field) throws ConfigurationException {
            ConfigField fieldAnno = field.getAnnotation(ConfigField.class);

            if (!fieldAnno.def().equals("N/A"))
                writer.storeNotDef(fieldAnno.name(), serialized, fieldAnno.def());
            else
                writer.storeNotNull(fieldAnno.name(), serialized);
        }

        @Override
        public void merge(T prev, T curr, ReflectiveConfig config, ConfigWriter diffwriter, Field field) throws ConfigurationException {
            ConfigField fieldAnno = field.getAnnotation(ConfigField.class);

            T prevSerialized = serialize(prev, config, field);
            T currSerialized = serialize(curr, config, field);

            diffwriter.storeDiff(fieldAnno.name(), prevSerialized, currSerialized);
        }

        /**
         * Constant metadata defined in constructors
         */
        @Override
        public Map<String, Object> getMetadata(ReflectiveConfig config, Field field) throws ConfigurationException {
            return metadata;
        }


    }

    /**
     * Common Read/Write methods for String representation
     */
    public abstract static class CommonAbstractTypeAdapter<T> implements ConfigTypeAdapter<T, String> {

        Map<String, Object> metadata =  new HashMap<String, Object>();

        @Override
        public boolean isWritingChildren(Field field) {
            return false;
        }

        @Override
        public String read(ReflectiveConfig config, ConfigReader reader, Field field) throws ConfigurationException {
            ConfigField fieldAnno = field.getAnnotation(ConfigField.class);
            return reader.asString(fieldAnno.name(), null);
        }

        @Override
        public void write(String serialized, ReflectiveConfig config, ConfigWriter writer, Field field) {
            ConfigField fieldAnno = field.getAnnotation(ConfigField.class);
            writer.storeNotNull(fieldAnno.name(), serialized);
        }

        @Override
        public void merge(T prev, T curr, ReflectiveConfig config, ConfigWriter diffwriter, Field field) throws ConfigurationException {
            ConfigField fieldAnno = field.getAnnotation(ConfigField.class);

            String prevSerialized = serialize(prev, config, field);
            String currSerialized = serialize(curr, config, field);

            diffwriter.storeDiff(fieldAnno.name(), prevSerialized, currSerialized);
        }

        @Override
        public Map<String, Object> getMetadata(ReflectiveConfig config, Field field) throws ConfigurationException {
            return metadata;
        }
    }

    /**
     * String
     */
    public static class StringTypeAdapter extends PrimitiveAbstractTypeAdapter<String> {

        public StringTypeAdapter() {
            metadata.put("type", "String");
        }

        @Override
        public String read(ReflectiveConfig config, ConfigReader reader, Field field) throws ConfigurationException {
            ConfigField fieldAnno = field.getAnnotation(ConfigField.class);
            return reader.asString(fieldAnno.name(), (fieldAnno.def().equals("N/A") ? null : fieldAnno.def()));
        }

    }

    /**
     * Integer
     */
    public static class IntegerTypeAdapter extends PrimitiveAbstractTypeAdapter<Integer> {

        public IntegerTypeAdapter() {
            metadata.put("type", "Integer");
        }

        @Override
        public Integer read(ReflectiveConfig config, ConfigReader reader, Field field) throws ConfigurationException {
            ConfigField fieldAnno = field.getAnnotation(ConfigField.class);
            return reader.asInt(fieldAnno.name(), (fieldAnno.def().equals("N/A") ? "0" : fieldAnno.def()));
        }
    }

    /**
     * Boolean
     */
    public static class BooleanTypeAdapter extends PrimitiveAbstractTypeAdapter<Boolean> {

        public BooleanTypeAdapter() {
            metadata.put("type", "Boolean");
        }

        @Override
        public Boolean read(ReflectiveConfig config, ConfigReader reader, Field field) throws ConfigurationException {
            ConfigField fieldAnno = field.getAnnotation(ConfigField.class);
            return reader.asBoolean(fieldAnno.name(), (fieldAnno.def().equals("N/A") ? "false" : fieldAnno.def()));
        }
    }

    /**
     * Array
     */
    public static class ArrayTypeAdapter extends PrimitiveAbstractTypeAdapter<Object> {

        @Override
        public Object deserialize(Object serialized, ReflectiveConfig config, Field field) throws ConfigurationException {
            // Support for Sets/Lists - needed for JSON deserialization
            // Creates an array with proper component type
            if (Collection.class.isAssignableFrom(serialized.getClass())) {
                Collection l = ((Collection) serialized);
                Object arr = Array.newInstance(field.getType().getComponentType(), l.size());
                int i=0;
                for (Object el : l) 
                    Array.set(arr, i++, el);
                return arr;
            }
            return super.deserialize(serialized, config, field);
        }

        @Override
        public boolean isWritingChildren(Field field) {
            return false;
        }

        @Override
        public Object read(ReflectiveConfig config, ConfigReader reader, Field field) throws ConfigurationException {
            ConfigField fieldAnno = field.getAnnotation(ConfigField.class);

            if (String.class.isAssignableFrom(field.getType().getComponentType()))
                return reader.asStringArray(fieldAnno.name());
            else if (int.class.isAssignableFrom(field.getType().getComponentType()))
                return reader.asIntArray(fieldAnno.name());
            else if (Code.class.isAssignableFrom(field.getType().getComponentType()))
                return reader.asCodeArray(fieldAnno.name());
            else
                return null;
        }

        @Override
        public void write(Object serialized, ReflectiveConfig config, ConfigWriter writer, Field field) throws ConfigurationException {
            ConfigField fieldAnno = field.getAnnotation(ConfigField.class);
            writer.storeNotEmpty(fieldAnno.name(), serialized);
        }

        @Override
        public Map<String, Object> getMetadata(ReflectiveConfig config, Field field) throws ConfigurationException {

            Map<String, Object> metadata =  new HashMap<String, Object>();
            Map<String, Object> elementMetadata =  new HashMap<String, Object>();

            metadata.put("type", "Array");
            metadata.put("elementMetadata", elementMetadata);

            if (String.class.isAssignableFrom(field.getType().getComponentType()))
                elementMetadata.put("type", "String");
            else if (int.class.isAssignableFrom(field.getType().getComponentType()))
                elementMetadata.put("type", "Integer");

            return metadata;
        }

    }

    /**
     * AttributesFormat
     */
    public static class AttributeFormatTypeAdapter extends CommonAbstractTypeAdapter<AttributesFormat> {

        public AttributeFormatTypeAdapter() {
            metadata.put("type", "AttributesFormat");
        }

        @Override
        public AttributesFormat deserialize(String serialized, ReflectiveConfig config, Field field) throws ConfigurationException {
            return AttributesFormat.valueOf(serialized);
        }

        @Override
        public String serialize(AttributesFormat obj, ReflectiveConfig config, Field field) {
            return (obj == null ? null : obj.toString());
        }

    }

    /**
     * Device by name
     */
    public static class DeviceTypeAdapter extends CommonAbstractTypeAdapter<Device> {

        public DeviceTypeAdapter() {
            metadata.put("type", "Device");
        }

        @Override
        public Device deserialize(String serialized, ReflectiveConfig config, Field field) throws ConfigurationException {
            return ((config == null || serialized == null) ? null : config.getDicomConfiguration().findDevice(serialized));
        }

        @Override
        public String serialize(Device obj, ReflectiveConfig config, Field field) throws ConfigurationException {
            return (obj == null ? null : obj.getDeviceName());
        }

    }

    /**
     * AttributesFormat
     */
    public static class EnumTypeAdapter extends CommonAbstractTypeAdapter<Enum<?>> {

        public EnumTypeAdapter() {
            metadata.put("type", "Enum");
        }

        @Override
        public Enum<?> deserialize(String serialized, ReflectiveConfig config, Field field) throws ConfigurationException {
            if (serialized == null) 
                return null;
            try {
                Method method = field.getType().getMethod("valueOf", String.class);
                return (Enum<?>) method.invoke(null, serialized);
            } catch (Exception x) {
                throw new ConfigurationException("Deserialization of Enum failed! field:"+field, x);
            }
        }

        @Override
        public String serialize(Enum<?> obj, ReflectiveConfig config, Field field) {
            return (obj == null ? null : obj.name());
        }

    }

    public static Map<Class, ConfigTypeAdapter> defaultTypeAdapters;

    static {
        defaultTypeAdapters = new HashMap<Class, ConfigTypeAdapter>();

        defaultTypeAdapters.put(String.class, new StringTypeAdapter());

        defaultTypeAdapters.put(int.class, new IntegerTypeAdapter());
        defaultTypeAdapters.put(Integer.class, new IntegerTypeAdapter());

        defaultTypeAdapters.put(Boolean.class, new BooleanTypeAdapter());
        defaultTypeAdapters.put(boolean.class, new BooleanTypeAdapter());

        defaultTypeAdapters.put(AttributesFormat.class, new AttributeFormatTypeAdapter());
        defaultTypeAdapters.put(Device.class, new DeviceTypeAdapter());

        defaultTypeAdapters.put(Map.class, new MapTypeAdapter());
        defaultTypeAdapters.put(Set.class, new SetTypeAdapter());

        defaultTypeAdapters.put(Enum.class, new EnumTypeAdapter());
    }

    public static Map<Class, ConfigTypeAdapter> get() {
        return defaultTypeAdapters;
    }

}
