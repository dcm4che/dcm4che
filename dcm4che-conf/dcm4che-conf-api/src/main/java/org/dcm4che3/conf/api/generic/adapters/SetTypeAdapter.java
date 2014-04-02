package org.dcm4che3.conf.api.generic.adapters;

import java.lang.reflect.Field;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.generic.ConfigField;
import org.dcm4che3.conf.api.generic.ReflectiveConfig;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigReader;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigTypeAdapter;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigWriter;

/**
 * For now only supports String[] serialized representation, so no ConfigClass'ed classes as elements
 * @author Roman K
 *
 * @param <T>
 */
public class SetTypeAdapter<T> implements ConfigTypeAdapter<Set<T>, String[]> {

    @Override
    public void write(String[] serialized, ReflectiveConfig config, ConfigWriter writer, Field field) throws ConfigurationException {
        ConfigField fieldAnno = field.getAnnotation(ConfigField.class);
        writer.storeNotEmpty(fieldAnno.name(), serialized);
    }
    
    private ConfigTypeAdapter<T, String> getElementAdapter(Field field, ReflectiveConfig config) {
        ParameterizedType pt = (ParameterizedType) field.getGenericType();

        Type[] ptypes = pt.getActualTypeArguments();

        // there must be only 1 parameterized type
        if (ptypes.length != 1)
            throw new MalformedParameterizedTypeException();

        // figure out the classes of declared generic parameter
        return (ConfigTypeAdapter<T, String>) config.lookupTypeAdapter((Class<T>) ptypes[0]);

    }

    @SuppressWarnings("unchecked")
    @Override
    public String[] serialize(Set<T> obj, ReflectiveConfig config, Field field) throws ConfigurationException {
        if (obj == null) return null;

        ConfigTypeAdapter<T,String> ta = getElementAdapter(field,config);
        
        String[] serialized = new String[obj.size()];
        int i=0;
        for (Object o : obj) 
            serialized[i++] = ta.serialize((T) o, config, field);
        return serialized;
    }

    @Override
    public String[] read(ReflectiveConfig config, ConfigReader reader, Field field) throws ConfigurationException {
        ConfigField fieldAnno = field.getAnnotation(ConfigField.class);
        return reader.asStringArray(fieldAnno.name());
    }

    @Override
    public Set<T> deserialize(String[] serialized, ReflectiveConfig config, Field field) throws ConfigurationException {
        
        ConfigTypeAdapter<T,String> ta = getElementAdapter(field,config);
        
        Set<T> set = new HashSet<T>(); 
        for (int i=0;i<serialized.length;i++) 
            set.add(ta.deserialize(serialized[i], config, field));
        return set;
    }

    @Override
    public void merge(Set<T> prev, Set<T> curr, ReflectiveConfig config, ConfigWriter diffwriter, Field field) throws ConfigurationException {
        // regular merge
        ConfigField fieldAnno = field.getAnnotation(ConfigField.class);

        String[] prevSerialized = serialize(prev, config, field);
        String[] currSerialized = serialize(curr, config, field);

        diffwriter.storeDiff(fieldAnno.name(), prevSerialized, currSerialized);
    }

    @Override
    public boolean isWritingChildren(Field field) {
        return false;
    }

}
