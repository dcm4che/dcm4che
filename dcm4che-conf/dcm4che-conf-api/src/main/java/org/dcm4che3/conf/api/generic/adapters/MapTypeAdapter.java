package org.dcm4che3.conf.api.generic.adapters;

import java.lang.reflect.Field;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.NamingException;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.generic.ConfigField;
import org.dcm4che3.conf.api.generic.ReflectiveConfig;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigReader;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigTypeAdapter;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigWriter;
import org.dcm4che3.net.TransferCapability;

/**
 * Map<br>
 * 
 * Key type must have String as serialized representation and must not use field when serializing/deserializing!
 * 
 */

public class MapTypeAdapter<K, V> implements ConfigTypeAdapter<Map<K, V>, Map<String,Object>> {

    @Override
    public boolean isWritingChildren(Field field) {
        return true;
    }

    private ConfigTypeAdapter<V, ?> getValueAdapter(Field field, ReflectiveConfig config) {
        ParameterizedType pt = (ParameterizedType) field.getGenericType();

        Type[] ptypes = pt.getActualTypeArguments();

        // there must be only 2 parameterized types
        if (ptypes.length != 2)
            throw new MalformedParameterizedTypeException();

        // figure out the classes of declared generic parameters and
        // get value adapter
        return config.lookupTypeAdapter((Class<V>) ptypes[1]);

    }

    private ConfigTypeAdapter<V, ?> getKeyAdapter(Field field, ReflectiveConfig config) {
        ParameterizedType pt = (ParameterizedType) field.getGenericType();

        Type[] ptypes = pt.getActualTypeArguments();

        // there must be only 2 parameterized types
        if (ptypes.length != 2)
            throw new MalformedParameterizedTypeException();

        // figure out the classes of declared generic parameters and
        // get key adapter
        return config.lookupTypeAdapter((Class<V>) ptypes[0]);

    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String,Object> read(ReflectiveConfig config, ConfigReader reader, Field field) throws ConfigurationException {
        ConfigField fieldAnno = field.getAnnotation(ConfigField.class);

        Map<String,Object> cnode = new HashMap<String,Object>();

        // read collection
        ConfigReader collectionReader = reader.getChildReader(getCollectionName(fieldAnno));
        Map<String, ConfigReader> map = collectionReader.readCollection(fieldAnno.mapKey());

        ConfigTypeAdapter<V, Object> valueAdapter = (ConfigTypeAdapter<V, Object>) getValueAdapter(field, config);

        // for each element, read it using the value adapter
        for (Entry<String, ConfigReader> e : map.entrySet()) {
            cnode.put(e.getKey(), valueAdapter.read(config, e.getValue(), field));
        }
        return cnode;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<K, V> deserialize(Map<String,Object> serialized, ReflectiveConfig config, Field field) throws ConfigurationException {

        if (serialized == null) return null;
        
        ConfigTypeAdapter<V, Object> valueAdapter = (ConfigTypeAdapter<V, Object>) getValueAdapter(field, config);
        ConfigTypeAdapter<K, String> keyAdapter = (ConfigTypeAdapter<K, String>) getKeyAdapter(field, config);

        // deserialize entries
        Map<K, V> map = new HashMap<K, V>();
        for (Entry<String, Object> e : serialized.entrySet()) {
            map.put(keyAdapter.deserialize(e.getKey(), config, null), valueAdapter.deserialize(e.getValue(), config, field));
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String,Object> serialize(Map<K, V> obj, ReflectiveConfig config, Field field) throws ConfigurationException {

        if (obj == null) return null;
        
        Map<String,Object> cnode = new HashMap<String,Object>();

        ConfigTypeAdapter<V, Object> valueAdapter = (ConfigTypeAdapter<V, Object>) getValueAdapter(field, config);
        ConfigTypeAdapter<K, String> keyAdapter = (ConfigTypeAdapter<K, String>) getKeyAdapter(field, config);

        for (Entry<K, V> e : obj.entrySet()) {
            cnode.put(keyAdapter.serialize(e.getKey(), config, null), valueAdapter.serialize(e.getValue(), config, field));
        }

        return cnode;
    }

    private String getCollectionName(ConfigField fieldAnno) {
        return (fieldAnno.mapName().equals("N/A") ? fieldAnno.name() : fieldAnno.mapName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(Map<String,Object> serialized, ReflectiveConfig config, ConfigWriter writer, Field field) throws ConfigurationException {
        ConfigField fieldAnno = field.getAnnotation(ConfigField.class);

        // getValueAdapter
        ConfigTypeAdapter<V, Object> valueAdapter = (ConfigTypeAdapter<V, Object>) getValueAdapter(field, config);

        ConfigWriter collectionWriter = writer.createCollectionChild(getCollectionName(fieldAnno), field);

        if (serialized == null) return;
        
        for (Entry<String, Object> e : serialized.entrySet()) {

            ConfigWriter elementWriter = collectionWriter.getCollectionElementWriter(fieldAnno.mapKey(), e.getKey(), field);
            valueAdapter.write(e.getValue(), config, elementWriter, field);
            elementWriter.flushWriter();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void merge(Map<K, V> prev, Map<K, V> curr, ReflectiveConfig config, ConfigWriter diffwriter, Field field) throws ConfigurationException {
        ConfigField fieldAnno = field.getAnnotation(ConfigField.class);

        ConfigTypeAdapter<V, Object> valueAdapter = (ConfigTypeAdapter<V, Object>) getValueAdapter(field, config);
        ConfigTypeAdapter<K, String> keyAdapter = (ConfigTypeAdapter<K, String>) getKeyAdapter(field, config);

        ConfigWriter collectionWriter = diffwriter.getChildWriter(getCollectionName(fieldAnno), field);

        // remove nodes that were deleted since prev
        for (Entry<K, V> e : prev.entrySet())
            if (curr.get(e.getKey()) == null)
                collectionWriter.removeCollectionElement(fieldAnno.mapKey(), keyAdapter.serialize(e.getKey(), config, null));

        // add new nodes and merge existing
        for (Entry<K, V> e : curr.entrySet()) {

            // serialize key
            String serializedKey = keyAdapter.serialize(e.getKey(), config, null);

            // if new node
            if (prev.get(e.getKey()) == null) {
                ConfigWriter elementWriter = collectionWriter.getCollectionElementWriter(fieldAnno.mapKey(), serializedKey, field);
                // serialize
                Object serialized = valueAdapter.serialize(e.getValue(), config, field);
                valueAdapter.write(serialized, config, elementWriter, field);
                elementWriter.flushWriter();
            }
            // existing node
            else {
                ConfigWriter elementWriter = collectionWriter.getCollectionElementDiffWriter(fieldAnno.mapKey(), serializedKey);
                valueAdapter.merge(prev.get(e.getKey()), e.getValue(), config, elementWriter, field);
                elementWriter.flushDiffs();
            }
        }
    }
    
    @Override
    public Map<String, Object> getMetadata(ReflectiveConfig config, Field field) throws ConfigurationException {
        ConfigField fieldAnno = field.getAnnotation(ConfigField.class);

        Map<String, Object> metadata =  new HashMap<String, Object>();
        Map<String, Object> keyMetadata =  new HashMap<String, Object>();
        Map<String, Object> valueMetadata =  new HashMap<String, Object>();
        
        metadata.put("type", "Map");
        
        // get adapters
        ConfigTypeAdapter<V, Object> valueAdapter = (ConfigTypeAdapter<V, Object>) getValueAdapter(field, config);
        ConfigTypeAdapter<K, String> keyAdapter = (ConfigTypeAdapter<K, String>) getKeyAdapter(field, config);

        // fill in key and value metadata
        keyMetadata.putAll(keyAdapter.getMetadata(config, field));
        metadata.put("keyMetadata", keyMetadata);
        
        valueMetadata.putAll(valueAdapter.getMetadata(config, field));
        metadata.put("valueMetadata", valueMetadata);

        return metadata;
    }
}