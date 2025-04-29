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

import java.lang.reflect.Field;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.api.TypeSafeConfiguration;
import org.dcm4che3.conf.core.api.StorageVersionedConfigurableClass;
import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.api.internal.ConfigReflection;
import org.dcm4che3.conf.core.api.internal.ConfigTypeAdapter;
import org.dcm4che3.conf.core.context.LoadingContext;
import org.dcm4che3.conf.core.context.ProcessingContext;
import org.dcm4che3.conf.core.context.Referable;
import org.dcm4che3.conf.core.context.SavingContext;
import org.dcm4che3.conf.core.util.PathFollower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;

/**
 * Reflective adapter that handles classes with ConfigurableClass annotations.<br/>
 * <br/>
 * <p/>
 * User has to use the special constructor and initialize providedConfObj when the
 * already created conf object should be used instead of instantiating one
 * 
 * @author Roman K
 * @author Maciek Siemczyk (maciek.siemczyk@agfa.com)
 */
@SuppressWarnings("unchecked")
public class ReflectiveAdapter<T> implements ConfigTypeAdapter<T, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(ReflectiveAdapter.class);

    private T providedConfObj;

    /**
     * stateless
     */
    public ReflectiveAdapter() {
    }

    /**
     * stateful
     */
    public ReflectiveAdapter(T providedConfigurationObjectInstance) {
        this.providedConfObj = providedConfigurationObjectInstance;
    }

    @Override
    public T fromConfigNode(Map<String, Object> configNode, ConfigProperty property, LoadingContext ctx, Object parent) throws ConfigurationException {

        if (configNode == null) return null;

        Class<T> clazz = (Class<T>) property.getType();

        if (!Map.class.isAssignableFrom(configNode.getClass()))
            throw new ConfigurationException("Provided configuration node is not a map (type " + clazz.getName() + ")");


        // figure out UUID
        String uuid;
        try {
            uuid = (String) configNode.get(Configuration.UUID_KEY);
        } catch (RuntimeException e) {
            throw new ConfigurationException("UUID is malformed: " + configNode.get(Configuration.UUID_KEY));
        }


        // if the object is provided - just populate and return
        if (providedConfObj != null) {
            populate(configNode, ctx, clazz, providedConfObj, parent, uuid);
            return providedConfObj;
        }


        // if uuid not present - simply create new instance
        if (uuid == null) {
            T confObj = ctx.getVitalizer().newInstance(clazz);
            populate(configNode, ctx, clazz, confObj, parent, uuid);
            return confObj;
        }

        //// uuid present - need to coordinate with the context

        // first check the context
        Referable existingReferable = ctx.getReferable(uuid);
        if (existingReferable != null) {
            // TODO: proper cast!
            return (T) existingReferable.getConfObject();
        }

        SettableFuture<Object> confObjFuture = SettableFuture.create();
        T confObj = ctx.getVitalizer().newInstance(clazz);
        Referable createdReferable = new Referable(confObjFuture, confObj);

        // cover non-atomicity above
        Referable suddenlyExistingReferable = ctx.registerReferableIfAbsent(uuid, createdReferable);
        if (suddenlyExistingReferable != null) {
            // TODO: proper cast!
            return (T) suddenlyExistingReferable.getConfObject();
        }

        // now it's for sure me who is responsible for loading this object
        try {
            populate(configNode, ctx, clazz, confObj, parent, uuid);
            confObjFuture.set(confObj);
            return confObj;
        } catch (RuntimeException e) {
            confObjFuture.setException(e);
            throw e;
        } catch (Error e) {
            confObjFuture.setException(e);
            throw e;
        }
    }

    private void populate(Map<String, Object> configNode, LoadingContext ctx, Class<T> clazz, T confObj, Object parent, String uuid) {

        // this differentiation is needed for historical reasons .. two examples is Device and the addApplicationEntity method, another example is HL7DeviceExtension....

        if (ConfigReflection.getUUIDPropertyForClass(clazz) != null) {
            // if class has uuid => it's 'standalone' conf class => initialize fields before parent

            populateFields(configNode, ctx, clazz, confObj);
            injectParent(ctx, clazz, confObj, parent, uuid);
        } else {
            // if class has no uuid => it's either an extension or a simple conf class => initialize parent before fields

            injectParent(ctx, clazz, confObj, parent, uuid);
            populateFields(configNode, ctx, clazz, confObj);
        }

        // Set the storage version if needed. 
        if (StorageVersionedConfigurableClass.class.isAssignableFrom(clazz)) {
            Number version = (Number) configNode.get(Configuration.VERSION_KEY);
            
            if (version != null) {
                ((StorageVersionedConfigurableClass) confObj).setStorageVersion(version.longValue());
            }
        }
    }

    private void populateFields(Map<String, Object> configNode, LoadingContext ctx, Class<T> clazz, T confObj) {
        for (ConfigProperty fieldProperty : ConfigReflection.getAllConfigurableFields(clazz))
            try {
                Object fieldValue = DefaultConfigTypeAdapters.delegateGetChildFromConfigNode(configNode, fieldProperty, ctx, confObj);
                ConfigReflection.setProperty(confObj, fieldProperty, fieldValue);
            } catch (RuntimeException e) {
                throw new ConfigurationException("Error while reading configuration property '" + fieldProperty.getAnnotatedName() + "' (field " + fieldProperty.getName() + ") in class " + clazz.getSimpleName(), e);
            }
    }

    private void injectParent(LoadingContext ctx, Class<T> clazz, T confObj, Object parent, String uuid) {

        Field parentField = ConfigReflection.getParentPropertyForClass(clazz);
        if (parentField == null) return;

        // if parent was provided - use it
        if (parent != null) {
            try {
                ConfigReflection.setProperty(confObj, parentField.getName(), parent);
                return;
            } catch (RuntimeException e) {
                throw new ConfigurationException("Could not 'inject' parent object into the @Parent field (class " + clazz.getName() + ")", e);
            }
        }

        // if no provided parent and no uuid - we cannot really find the parent, so just leave it null
        if (uuid == null) return;


        // TODO: replace with proxy

        // if no config context - leave the parent unset
        TypeSafeConfiguration<?> typeSafeConfig = ctx.getTypeSafeConfiguration();
        if (typeSafeConfig == null) return;

        // Get path of this object in the storage
        Path pathByUUID = typeSafeConfig.getLowLevelAccess().getPathByUUID(uuid);
        if (pathByUUID == null) return;
        Deque<ConfigProperty> configProperties = PathFollower.traceProperties(typeSafeConfig.getRootClass(), pathByUUID);

        // parent is either the first or the second in the path (otherwise cannot really get the parent)

        if (configProperties.isEmpty()) {
            return;
        }
        
        configProperties.removeLast();
        int nodesAbove = 1;

        // this can be still a map/collection, try one level above
        if (!configProperties.peekLast().isConfObject()) {
            configProperties.removeLast();
            nodesAbove++;
            if (configProperties.isEmpty()) {
                return;
            }
            
            if (!configProperties.peekLast().isConfObject()) {
                return;
            }
        }

        // now we are looking at the parent
        ConfigProperty parentProp = configProperties.peekLast();

        if (!parentField.getType().isAssignableFrom(parentProp.getRawClass())) {
            log.warn(
                    "Parent type mismatch: config structure denotes {}, but the class"
                        + "has a field of type {} config object uuid={}, config class {}.",
                    parentProp.getRawClass(), parentField.getType(), uuid, clazz);
            return;
        }

        Path parentPath = pathByUUID.subPath(0, pathByUUID.getPathItems().size() - nodesAbove);

        // load parent
        Object loadedParent = typeSafeConfig.load(parentPath, parentProp.getRawClass(), ctx);
        ConfigReflection.setProperty(confObj, parentField.getName(), loadedParent);
    }


    @Override
    public Map<String, Object> toConfigNode(T object, ConfigProperty property, SavingContext ctx) throws ConfigurationException {

        if (object == null) return null;

        Class<T> clazz = (Class<T>) object.getClass();

        Map<String, Object> configNode = new TreeMap<>();

        // get data from all the configurable fields
        for (ConfigProperty fieldProperty : ConfigReflection.getAllConfigurableFields(clazz)) {
            try {
                Object value = ConfigReflection.getProperty(object, fieldProperty);
                DefaultConfigTypeAdapters.delegateChildToConfigNode(value, configNode, fieldProperty, ctx);
            } catch (Exception e) {
                throw new ConfigurationException("Error while serializing configuration field '" + fieldProperty.getName() + "' in class " + clazz.getSimpleName(), e);
            }
        }
        
        // Set the storage version if needed. 
        if (StorageVersionedConfigurableClass.class.isAssignableFrom(clazz)) {
            configNode.put(Configuration.VERSION_KEY, ((StorageVersionedConfigurableClass) object).getStorageVersion());
        }

        return configNode;
    }

    @Override
    public Map<String, Object> getSchema(ConfigProperty property, ProcessingContext ctx) throws ConfigurationException {

        Class<T> clazz = (Class<T>) property.getType();

        Map<String, Object> classMetaDataWrapper = new HashMap<>();
        Map<String, Object> classMetaData = new HashMap<>();
        classMetaDataWrapper.put(PropertySchema.PROPERTIES_KEY, classMetaData);
        classMetaDataWrapper.put(PropertySchema.TYPE_KEY, "object");
        classMetaDataWrapper.put(PropertySchema.CLASS_KEY, clazz.getSimpleName());

        // find out if we need to include uiOrder metadata
        boolean includeOrder = ConfigReflection.getAllConfigurableFields(clazz).stream()
            .map(configurableProperty -> configurableProperty.getAnnotation(ConfigurableProperty.class))
            .anyMatch(annotation -> annotation.order() != 0);
        
        // populate properties
        for (ConfigProperty prop : ConfigReflection.getAllConfigurableFields(clazz)) {

            ConfigTypeAdapter<?, ?> childAdapter = ctx.getVitalizer().lookupTypeAdapter(prop);
            Map<String, Object> childPropertyMetadata = new LinkedHashMap<>();
            classMetaData.put(prop.getAnnotatedName(), childPropertyMetadata);

            setPropertyAttributeIfPresent(childPropertyMetadata, prop.getLabel(), PropertySchema.TITLE_KEY);
            setPropertyAttributeIfPresent(childPropertyMetadata, prop.getDescription(), "description");

            try {
                if (!prop.getDefaultValue().equals(ConfigurableProperty.NO_DEFAULT_VALUE)) {
                    childPropertyMetadata.put("default", childAdapter.normalize(prop.getDefaultValue(), prop, ctx));
                }
            } catch (ClassCastException e) {
                childPropertyMetadata.put("default", 0);
            }
            
            if (!prop.getTags().isEmpty()) {
                childPropertyMetadata.put("tags", prop.getTags());
            }

            if (includeOrder) {
                childPropertyMetadata.put(PropertySchema.UI_ORDER_KEY, prop.getOrder());
            }

            childPropertyMetadata.put(PropertySchema.UI_GROUP_KEY, prop.getGroup());
            
            // Make optimistic lock hashes read-only so people don't mess with them.
            if (prop.isOlockHash()) {
                childPropertyMetadata.put(PropertySchema.READONLY_KEY, true);
            } else {
            	childPropertyMetadata.put(PropertySchema.REQUIRED_KEY, prop.isRequired());
            }

            // also merge in the metadata from this child itself
            Map<String, Object> childMetaData = childAdapter.getSchema(prop, ctx);
            if (childMetaData != null) childPropertyMetadata.putAll(childMetaData);
        }
        
        if (StorageVersionedConfigurableClass.class.isAssignableFrom(clazz)) {
            addStorageVersionSchemaProperty(classMetaData);
        }

        return classMetaDataWrapper;
    }

    @Override
    public Map<String, Object> normalize(Object configNode, ConfigProperty property, ProcessingContext ctx) throws ConfigurationException {
        return (Map<String, Object>) configNode;
    }
    
    private void setPropertyAttributeIfPresent(
            final Map<String, Object> childPropertyMetadata,
            final String propertyAttributeValue,
            final String propertyAttributeKey) {
        
        if (StringUtils.isNotEmpty(propertyAttributeValue)) {
            childPropertyMetadata.put(propertyAttributeKey, propertyAttributeValue);
        }
    }
        
    private void addStorageVersionSchemaProperty(Map<String, Object> classMetaData) {

        Map<String, Object> versionProperty = new HashMap<>();
        versionProperty.put(PropertySchema.TYPE_KEY, "integer");
        versionProperty.put(PropertySchema.UI_GROUP_KEY, "Other");
        versionProperty.put(PropertySchema.READONLY_KEY, true);
        
        classMetaData.put(Configuration.VERSION_KEY, versionProperty);
    }
    
    public static final class PropertySchema {
        
        /**
         * Private constructor to prevent instantiation.
         */
        private PropertySchema() { }
        
        public static final String CLASS_KEY = "class";
        public static final String TYPE_KEY = "type";
        public static final String PROPERTIES_KEY = "properties";
        
        public static final String TITLE_KEY = "title";
        public static final String UI_ORDER_KEY = "uiOrder";
        public static final String UI_GROUP_KEY = "uiGroup";
        public static final String READONLY_KEY = "readonly";
        public static final String REQUIRED_KEY = "required";
    }
}
