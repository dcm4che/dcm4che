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

package org.dcm4che3.conf.api.generic;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.commons.beanutils.PropertyUtils;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigReader;
import org.dcm4che3.conf.api.generic.adapters.DefaultConfigTypeAdapters;
import org.dcm4che3.conf.api.generic.adapters.ReflectiveAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic helper class that provides reflective traversing of classes annotated
 * with Config annotations, and storing/reading config from/to such classes
 * 
 * @author Roman K
 * 
 */
@SuppressWarnings("rawtypes")
public class ReflectiveConfig {

    public static final Logger log = LoggerFactory.getLogger(ReflectiveConfig.class);

    /**
     * Type adapter that handles configuration read/write/serialize/deserialize
     * for a specific config java class.
     * 
     * @author Roman K
     * 
     * @param <T>
     *            Java class
     * @param <ST>
     *            Serialized representation - this intermediate format is used by UIs and storeDiffs.
     */
    public interface ConfigTypeAdapter<T, ST> {

        /**
         * Writes the serialized representation to the configuration backend.
         * 
         * @param obj
         * @param config
         *            ReflectiveConfig that can be used e.g. to retrieve
         *            DicomConfiguration <b>Can be <i>null</i>!</b>
         * @param writer
         *            ConfigWriter to use
         * @param field
         *            Config field. Can be used to read additional annotations,
         *            check type, etc.
         * @return
         */
        void write(ST serialized, ReflectiveConfig config, ConfigWriter writer, Field field) throws ConfigurationException;

        /**
         * Constructs a serialized representation of an object
         * 
         * @param obj
         * @param config
         *            ReflectiveConfig that can be used e.g. to retrieve
         *            DicomConfiguration <b>Can be <i>null</i>!</b>
         * @param field
         *            Config field. Can be used to read additional annotations,
         *            check type, etc.
         * @return
         * @throws ConfigurationException
         */
        ST serialize(T obj, ReflectiveConfig config, Field field) throws ConfigurationException;

        /**
         * Reads an attribute from configuration into its serialized form
         * 
         * @param str
         * @param config
         *            ReflectiveConfig that can be used e.g. to retrieve
         *            DicomConfiguration <b>Can be <i>null</i>!</b>
         * @param reader
         *            ConfigReader to use
         * @param field
         *            Config field. Can be used to read additional annotations,
         *            check type, etc.
         * @return
         * @throws ConfigurationException
         * @throws NamingException
         */
        ST read(ReflectiveConfig config, ConfigReader reader, Field field) throws ConfigurationException;

        /**
         * Constructs an object from its serialized form
         * 
         * @param serialized
         * @param config
         *            ReflectiveConfig that can be used e.g. to retrieve
         *            DicomConfiguration <b>Can be <i>null</i>!</b>
         * @param field
         *            Config field. Can be used to read additional annotations,
         *            check type, etc.
         * @return
         * @throws ConfigurationException
         */
        T deserialize(ST serialized, ReflectiveConfig config, Field field) throws ConfigurationException;

        void merge(T prev, T curr, ReflectiveConfig config, ConfigWriter diffwriter, Field field) throws ConfigurationException;

        /**
         * Should indicate whether the specified field will generate a child when writing configuration
         * @param field
         * @return
         */
        boolean isWritingChildren(Field field);

        Map<String, Object> getMetadata(ReflectiveConfig config, Field classField) throws ConfigurationException;

    }

    @Deprecated 
    public interface DiffWriter extends ConfigWriter {
    }

    /**
     * Used by reflective config writer, should implement storage type-specific
     * methods
     * 
     * @author Roman K
     */
    public interface ConfigWriter {
        void storeNotDef(String propName, Object value, String def);

        void storeNotEmpty(String propName, Object value);

        void storeNotNull(String propName, Object value);

        // collections

        /**
         * 
         * @param keyName
         * @param keyValue
         * @param field
         *            Field with the declared collection/map.
         * @return
         * @throws ConfigurationException
         */
        ConfigWriter getCollectionElementWriter(String keyName, String keyValue, Field field) throws ConfigurationException;

        public ConfigWriter createCollectionChild(String propName, Field field) throws ConfigurationException;

        void flushWriter() throws ConfigurationException;

        void storeDiff(String propName, Object prev, Object curr);

        void flushDiffs() throws ConfigurationException;

        void removeCollectionElement(String keyName, String keyValue) throws ConfigurationException;
        
        void removeCurrentNode() throws ConfigurationException;

        ConfigWriter getCollectionElementDiffWriter(String keyName, String keyValue);

        ConfigWriter getChildWriter(String propName, Field field) throws ConfigurationException;

    }

    /**
     * Used by reflective config reader, should implement storage type-specific
     * methods
     * 
     * @author Roman K
     */
    public interface ConfigReader {

        String[] asStringArray(String propName) throws ConfigurationException;

        int[] asIntArray(String propName) throws ConfigurationException;

        int asInt(String propName, String def) throws ConfigurationException;

        String asString(String propName, String def) throws ConfigurationException;

        boolean asBoolean(String propName, String def) throws ConfigurationException;

        // collections

        Map<String, ConfigReader> readCollection(String keyName) throws ConfigurationException;

        ConfigReader getChildReader(String propName) throws ConfigurationException;

    }

    /*
     * Static members
     */

    /**
     * Default singleton instance for simplified usage
     */
    private static final ReflectiveConfig singleton = new ReflectiveConfig(null, null);

    /**
     * Writes the configuration from the properties of the specified
     * configuration object into the config storage using the provided writer.
     * 
     * @param confObj
     *            Configuration object
     * @param writer
     *            Configuration writer
     * @throws ConfigurationException
     */
    public static <T> void store(T confObj, ConfigWriter writer) throws ConfigurationException {
        singleton.storeConfig(confObj, writer);
    }

    /**
     * Reads the configuration into the properties of the specified
     * configuration object using the provided reader.
     * 
     * @param confObj
     * @param reader
     * @throws ConfigurationException
     */
    public static <T> void read(T confObj, ConfigReader reader) throws ConfigurationException {
        singleton.readConfig(confObj, reader);
    }

    /**
     * Walk through the <b>from</b> object and for each field annotated with
     * 
     * @ConfigField, copy the value by using getter/setter to the <b>to</b>
     *               object.
     * 
     * @param from
     * @param to
     */
    public static <T> void reconfigure(T from, T to) {

        // look through all fields of the config class, not including
        // superclass fields
        for (Field field : from.getClass().getDeclaredFields()) {

            // if field is not annotated, skip it
            ConfigField fieldAnno = (ConfigField) field.getAnnotation(ConfigField.class);
            if (fieldAnno == null)
                continue;

            try {

                PropertyUtils.setSimpleProperty(to, field.getName(), PropertyUtils.getSimpleProperty(from, field.getName()));

            } catch (Exception e) {
                throw new RuntimeException("Unable to reconfigure a device!", e);
            }

        }

    }

    /**
     * Calls store diff methods for all pairs of the annotated fields of
     * prevConfObj and confObj
     * 
     * @param prevConfObj
     * @param confObj
     * @param diffWriter
     * @throws ConfigurationException
     */
    public static <T> void storeAllDiffs(T prevConfObj, T confObj, ConfigWriter diffWriter) throws ConfigurationException {
        singleton.storeConfigDiffs(prevConfObj, confObj, diffWriter);
    }

    /*
     * Non-static class members
     */

    private Map<Class, ConfigTypeAdapter> customRepresentations;
    private DicomConfiguration dicomConfiguration;

    /**
     * Creates an instance of ReflectiveConfig that will use the specified
     * config context and custom representations
     * 
     * @param customRepresentations
     *            Null can be provided. class-representation map for types that
     *            should be treated in a special way when reading/writing the
     *            configuration.
     * @param configCtx
     *            Null can be provided. DicomCofiguration that will be forwarded
     *            to type adapters as a config context.
     */
    public ReflectiveConfig(Map<Class, ConfigTypeAdapter> customRepresentations, DicomConfiguration configCtx) {
        super();
        this.customRepresentations = customRepresentations;
        this.dicomConfiguration = configCtx;
    }

    @SuppressWarnings("unchecked")
    public <T> void readConfig(T confObj, ConfigReader reader) throws ConfigurationException {

        ReflectiveAdapter<T> adapter = new ReflectiveAdapter<T>((Class<T>) confObj.getClass(), confObj);
        adapter.deserialize(adapter.read(this, reader, null), this, null);
    }

    @SuppressWarnings("unchecked")
    public <T> void storeConfig(T confObj, ConfigWriter writer) throws ConfigurationException {

        ReflectiveAdapter<T> adapter = new ReflectiveAdapter<T>((Class<T>) confObj.getClass());
        adapter.write(adapter.serialize(confObj, this, null), this, writer, null);

    }

    @SuppressWarnings("unchecked")
    public <T> void storeConfigDiffs(T prevConfObj, T confObj, ConfigWriter ldapDiffWriter) throws ConfigurationException {

        ReflectiveAdapter<T> adapter = new ReflectiveAdapter<T>((Class<T>) confObj.getClass());
        adapter.merge(prevConfObj, confObj, this, ldapDiffWriter, null);
    }

    @SuppressWarnings("unchecked")
    public <T> ConfigTypeAdapter<T, ?> lookupTypeAdapter(Class<T> clazz) {

        ConfigTypeAdapter<T, ?> adapter = null;

        Map<Class, ConfigTypeAdapter> def = DefaultConfigTypeAdapters.get();

        // if it is a config class, use reflective adapter
        if (clazz.getAnnotation(ConfigClass.class) != null)
            adapter = new ReflectiveAdapter(clazz);
        else if (clazz.isArray())
            // if array
            adapter = (ConfigTypeAdapter<T, ?>) new DefaultConfigTypeAdapters.ArrayTypeAdapter();
        else
            // try find in defaults
            adapter = def.get(clazz);

        // if still not found, try custom
        if (adapter == null && customRepresentations != null)
            adapter = customRepresentations.get(clazz);

        return adapter;

    }

    public DicomConfiguration getDicomConfiguration() {
        return dicomConfiguration;
    }

    public void setDicomConfiguration(DicomConfiguration dicomConfiguration) {
        this.dicomConfiguration = dicomConfiguration;
    }

    public void setCustomRepresentations(Map<Class, ConfigTypeAdapter> customRepresentations) {
        this.customRepresentations = customRepresentations;
    }

}
