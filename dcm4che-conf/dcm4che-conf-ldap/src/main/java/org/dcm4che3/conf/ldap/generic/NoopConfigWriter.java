package org.dcm4che3.conf.ldap.generic;

import java.lang.reflect.Field;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.generic.ReflectiveConfig;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigWriter;

/**
 * Writes nothing but allows the reflective config to run without exceptions
 * @author Roman K
 *
 */
public class NoopConfigWriter implements ConfigWriter {

    @Override
    public void storeNotDef(String propName, Object value, String def) {
    }

    @Override
    public void storeNotEmpty(String propName, Object value) {
    }

    @Override
    public void storeNotNull(String propName, Object value) {
    }

    @Override
    public ConfigWriter getCollectionElementWriter(String keyName, String keyValue, Field field) throws ConfigurationException {
        return this;
    }

    @Override
    public ConfigWriter createCollectionChild(String propName, Field field) throws ConfigurationException {
        return this;
    }

    @Override
    public void flushWriter() throws ConfigurationException {
    }

    @Override
    public void storeDiff(String propName, Object prev, Object curr) {
    }

    @Override
    public void flushDiffs() throws ConfigurationException {
    }

    @Override
    public void removeCollectionElement(String keyName, String keyValue) throws ConfigurationException {
    }

    @Override
    public void removeCurrentNode() throws ConfigurationException {
    }

    @Override
    public ConfigWriter getCollectionElementDiffWriter(String keyName, String keyValue) {
        return this;
    }

    @Override
    public ConfigWriter getChildWriter(String propName, Field field) throws ConfigurationException {
        return this;
    }
    
    private static ConfigWriter instance = new NoopConfigWriter();
    
    public static ConfigWriter getInstance() {
        return instance;
    }
}
