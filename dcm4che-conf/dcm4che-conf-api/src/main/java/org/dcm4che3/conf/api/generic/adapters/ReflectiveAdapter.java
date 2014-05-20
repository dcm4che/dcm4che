package org.dcm4che3.conf.api.generic.adapters;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.commons.beanutils.PropertyUtils;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.generic.ConfigField;
import org.dcm4che3.conf.api.generic.ReflectiveConfig;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigReader;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigTypeAdapter;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigWriter;

/**
 * Reflective adapter that handles classes with ConfigClass annotations.<br/>
 * <br/>
 * 
 * <b>field</b> argument is not actually used in the methods, the class must be
 * set in the constructor.
 * 
 * User has to use 2 arg constructor and initialize providedConfObj when the
 * already created conf object should be used instead of instantiating one in
 * deserialize method, as, e.g. in ReflectiveConfig.readConfig
 * 
 */
public class ReflectiveAdapter<T> implements ConfigTypeAdapter<T, Map<String,Object>> {

    private Class<T> clazz;

    /**
     * Initialized only when doing first level parsing, e.g. in
     * ReflectiveConfig.readConfig
     */
    private T providedConfObj;

    public ReflectiveAdapter(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    public ReflectiveAdapter(Class<T> clazz, T providedConfObj) {
        super();
        this.clazz = clazz;
        this.providedConfObj = providedConfObj;
    }

    @Override
    public boolean isWritingChildren(Field field) {
        // if this object is a property, create a child
        return (field != null && field.getType().equals(clazz));

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void write(Map<String,Object> serialized, ReflectiveConfig config, ConfigWriter writer, Field field) throws ConfigurationException {

        if (serialized == null) {
            return;
        }
        
        // if this object is a property, create a child
        if (field != null && field.getType().equals(clazz)) {
            ConfigField fieldAnno = (ConfigField) field.getAnnotation(ConfigField.class);
            writer = writer.getChildWriter(fieldAnno.name(), field);
        }

        for (Field classField : clazz.getDeclaredFields()) {

            // if field is not annotated, skip it
            ConfigField fieldAnno = (ConfigField) classField.getAnnotation(ConfigField.class);
            if (fieldAnno == null)
                continue;

            // find typeadapter
            ConfigTypeAdapter customRep = config.lookupTypeAdapter(classField.getType());

            if (customRep != null) {

                // this is done in the next phase after flush
                if (!customRep.isWritingChildren(classField))
                    customRep.write(serialized.get(fieldAnno.name()), config, writer, classField);
            } else {
                throw new ConfigurationException("Corresponding 'writer' was not found for field" + fieldAnno.name());
            }
        }

        // do actual store
        writer.flushWriter();

        // now when we have a node generated, store children
        for (Field classField : clazz.getDeclaredFields()) {

            // if field is not annotated, skip it
            ConfigField fieldAnno = (ConfigField) classField.getAnnotation(ConfigField.class);
            if (fieldAnno == null)
                continue;

            // find typeadapter
            ConfigTypeAdapter customRep = config.lookupTypeAdapter(classField.getType());

            if (customRep.isWritingChildren(classField))
                customRep.write(serialized.get(fieldAnno.name()), config, writer, classField);

        }

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Map<String,Object> serialize(T obj, ReflectiveConfig config, Field field) throws ConfigurationException {

        if (obj == null) return null;

        
        Map<String,Object> cnode = new HashMap<String,Object>();
        for (Field classField : clazz.getDeclaredFields()) {

            // if field is not annotated, skip it
            ConfigField fieldAnno = (ConfigField) classField.getAnnotation(ConfigField.class);
            if (fieldAnno == null)
                continue;

            // read a configuration value using its getter
            Object value;

            try {
                value = PropertyUtils.getSimpleProperty(obj, classField.getName());
            } catch (Exception e) {
                throw new ConfigurationException("Error while writing configuration field " + fieldAnno.name(), e);
            }

            // find typeadapter
            ConfigTypeAdapter customRep = config.lookupTypeAdapter(classField.getType());

            if (customRep != null) {
                Object serialized = customRep.serialize(value, config, classField);
                cnode.put(fieldAnno.name(), serialized);
            } else {
                throw new ConfigurationException("Corresponding 'writer' was not found for field " + fieldAnno.name());
            }

        }
        return cnode;

    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map<String,Object> read(ReflectiveConfig config, ConfigReader reader, Field field) throws ConfigurationException {

        // if this object is a property, get a child
        if (field != null && field.getType().equals(clazz)) {
            ConfigField fieldAnno = (ConfigField) field.getAnnotation(ConfigField.class);
            try {
                reader = reader.getChildReader(fieldAnno.name());
            } catch (ConfigurationException e) {
                // if there is any default specified, load as null
                if (fieldAnno.def().equals("null"))
                    return null;
                else
                    throw e;
            }
        }

        Map<String,Object> cnode = new HashMap<String,Object>();
        for (Field classField : clazz.getDeclaredFields()) {

            // if field is not annotated, skip it
            ConfigField fieldAnno = (ConfigField) classField.getAnnotation(ConfigField.class);
            if (fieldAnno == null)
                continue;

            // find typeadapter
            ConfigTypeAdapter customRep = config.lookupTypeAdapter(classField.getType());

            if (customRep != null) {

                Object value = customRep.read(config, reader, classField);
                cnode.put(fieldAnno.name(), value);

            } else
                throw new ConfigurationException("Corresponding 'reader' was not found for field " + fieldAnno.name());

        }

        return cnode;

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public T deserialize(Map<String,Object> serialized, ReflectiveConfig config, Field field) throws ConfigurationException {

        if (serialized == null) return null;
        
        T confObj;

        // create instance or use provided when it was created earlier,
        // e.g., in other config extensions
        if (providedConfObj == null) {
            try {
                confObj = (T) clazz.newInstance();
            } catch (Exception e) {
                throw new ConfigurationException("Error while instantiating config class " + clazz.getSimpleName()
                        + ". Check whether null-arg constructor exists.", e);
            }
        } else
            confObj = providedConfObj;

        for (Field classField : clazz.getDeclaredFields()) {

            // if field is not annotated, skip it
            ConfigField fieldAnno = (ConfigField) classField.getAnnotation(ConfigField.class);
            if (fieldAnno == null)
                continue;

            // find typeadapter
            ConfigTypeAdapter customRep = config.lookupTypeAdapter(classField.getType());

            if (customRep != null) {
                try {
                    Object value = customRep.deserialize(serialized.get(fieldAnno.name()), config, classField);

                    // set using a setter
                    PropertyUtils.setSimpleProperty(confObj, classField.getName(), value);
                } catch (Exception e) {
                    throw new ConfigurationException("Error while reading configuration field " + fieldAnno.name(), e);
                }

            } else
                throw new ConfigurationException("Corresponding 'reader' was not found for field " + fieldAnno.name());

        }

        return confObj;

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void merge(T prev, T curr, ReflectiveConfig config, ConfigWriter diffwriter, Field field) throws ConfigurationException {

        // if this object is a property, get a child
        if (field != null && field.getType().equals(clazz)) {
            ConfigField fieldAnno = (ConfigField) field.getAnnotation(ConfigField.class);
            
            if (prev == null) {
                write(serialize(curr, config, field), config, diffwriter, field);
                return;
            } else
            if (curr == null) {
                diffwriter.getChildWriter(fieldAnno.name(), field).removeCurrentNode();
                return;
            }

            diffwriter = diffwriter.getChildWriter(fieldAnno.name(), field);

        }

        // look through all fields of the config class, not including
        // superclass fields
        for (Field classField : clazz.getDeclaredFields()) {

            // if field is not annotated, skip it
            ConfigField fieldAnno = (ConfigField) classField.getAnnotation(ConfigField.class);
            if (fieldAnno == null)
                continue;

            try {

                Object prevProp = PropertyUtils.getSimpleProperty(prev, classField.getName());
                Object currProp = PropertyUtils.getSimpleProperty(curr, classField.getName());

                // find adapter
                ConfigTypeAdapter customRep = config.lookupTypeAdapter(classField.getType());

                customRep.merge(prevProp, currProp, config, diffwriter, classField);

            } catch (Exception e) {
                throw new ConfigurationException("Cannot store diff for field " + fieldAnno.name(), e);
            }

        }

        // do actual merge
        diffwriter.flushDiffs();

    }
    
    @SuppressWarnings("rawtypes")
    public Map<String,Object> getMetadata(ReflectiveConfig config, Field field) throws ConfigurationException {

        Map<String,Object> classMetaData = new HashMap<String,Object>();
        // go through all annotated fields
        for (Field classField : clazz.getDeclaredFields()) {

            // if field is not annotated, skip it
            ConfigField fieldAnno = (ConfigField) classField.getAnnotation(ConfigField.class);
            if (fieldAnno == null)
                continue;

            // save metadata
            Map<String, Object> fieldMetaData = new HashMap<String, Object>();
            classMetaData.put(fieldAnno.name(), fieldMetaData);

            fieldMetaData.put("label", fieldAnno.label());
            fieldMetaData.put("description", fieldAnno.description());
            fieldMetaData.put("optional", fieldAnno.optional());
            
            //TODO: add type, default?
            
            // find typeadapter
            ConfigTypeAdapter customRep = config.lookupTypeAdapter(classField.getType());

            if (customRep != null) {
                Map<String, Object> childrenMetaData = customRep.getMetadata(config, classField);
                if (childrenMetaData != null)
                    fieldMetaData.put("children", childrenMetaData);
            };

        }

        return classMetaData;

    }

}