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

import org.apache.commons.beanutils.PropertyUtils;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.ConfigurationUnserializableException;
import org.dcm4che3.conf.core.AnnotatedConfigurableProperty;
import org.dcm4che3.conf.core.BeanVitalizer;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.util.ConfigIterators;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reflective adapter that handles classes with ConfigurableClass annotations.<br/>
 * <br/>
 * <p/>
 * User has to use the special constructor and initialize providedConfObj when the
 * already created conf object should be used instead of instantiating one
 */
@SuppressWarnings("unchecked")
public class ReflectiveAdapter<T> implements ConfigTypeAdapter<T, Map<String, Object>> {

    private T providedConfObj;

    public ReflectiveAdapter() {
    }

    public ReflectiveAdapter(T providedConfigurationObjectInstance) {
        this.providedConfObj = providedConfigurationObjectInstance;
    }


    @Override
    public T fromConfigNode(Map<String, Object> configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {

        if (configNode == null) return null;
        Class<T> clazz = (Class<T>) property.getType();

        if (!Map.class.isAssignableFrom(configNode.getClass()))
            throw new ConfigurationException("Provided configuration node is not a map (type " + clazz.getName() + ")");


        T confObj;

        // create instance or use provided when it was created for us
        if (providedConfObj == null) {
            try {
                confObj = vitalizer.newInstance(clazz);
            } catch (Exception e) {
                throw new ConfigurationException("Error while instantiating config class " + clazz.getSimpleName()
                        + ". Check whether null-arg constructor exists.", e);
            }
        } else
            confObj = providedConfObj;

        // iterate and populate annotated fields
        for (AnnotatedConfigurableProperty fieldProperty : ConfigIterators.getAllConfigurableFields(clazz))
            try {
                Object fieldValue = DefaultConfigTypeAdapters.delegateGetChildFromConfigNode(configNode, fieldProperty, vitalizer);
                PropertyUtils.setSimpleProperty(confObj, fieldProperty.getName(), fieldValue);
            } catch (Exception e) {
                throw new ConfigurationException("Error while reading configuration property '" + fieldProperty.getAnnotatedName() + "' (field "+fieldProperty.getName()+") in class " + clazz.getSimpleName(), e);
            }

        // iterate over setters
        for (ConfigIterators.AnnotatedSetter setter : ConfigIterators.getAllConfigurableSetters(clazz)) {
            try {
                // populate parameters for the setter
                Object[] args = new Object[setter.getParameters().size()];
                int i = 0;
                for (AnnotatedConfigurableProperty paramProperty : setter.getParameters())
                    args[i++] = DefaultConfigTypeAdapters.delegateGetChildFromConfigNode(configNode, paramProperty, vitalizer);

                // invoke setter
                setter.getMethod().invoke(confObj, args);
            } catch (Exception e) {
                throw new ConfigurationException("Error while trying to initialize the object with method '" + setter.getMethod().getName()+"'", e);
            }
        }

        return confObj;
    }


    @Override
    public Map<String, Object> toConfigNode(T object, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {

        if (object == null) return null;

        Class<T> clazz = (Class<T>) object.getClass();

        Map<String, Object> configNode = new HashMap<String, Object>();

        // get data from all the configurable fields
        for (AnnotatedConfigurableProperty fieldProperty : ConfigIterators.getAllConfigurableFields(clazz)) {
            try {
                Object value = PropertyUtils.getSimpleProperty(object, fieldProperty.getName());
                DefaultConfigTypeAdapters.delegateChildToConfigNode(value, configNode, fieldProperty, vitalizer);
            } catch (Exception e) {
                throw new ConfigurationException("Error while serializing configuration field '" + fieldProperty.getName() + "' in class " + clazz.getSimpleName(), e);
            }
        }

        // there must be no setters
        for (ConfigIterators.AnnotatedSetter setter : ConfigIterators.getAllConfigurableSetters(clazz))
            throw new ConfigurationUnserializableException("Cannot infer properties which are setter parameters. This object has a setter (" + setter.getMethod().getName() + ")");

        return configNode;
    }


    @Override
    public Map<String, Object> getSchema(AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {

        Class<T> clazz = (Class<T>) property.getType();

        Map<String, Object> classMetaDataWrapper = new HashMap<String, Object>();
        Map<String, Object> classMetaData = new HashMap<String, Object>();
        classMetaDataWrapper.put("properties", classMetaData);
        classMetaDataWrapper.put("type", "object");
        classMetaDataWrapper.put("class", clazz.getSimpleName());

        // find out if we need to include uiOrder metadata
        boolean includeOrder = false;
        for (AnnotatedConfigurableProperty configurableChildProperty : ConfigIterators.getAllConfigurableFieldsAndSetterParameters(clazz))
            if (configurableChildProperty.getAnnotation(ConfigurableProperty.class).order() != 0) includeOrder = true;


        // populate properties
        for (AnnotatedConfigurableProperty configurableChildProperty : ConfigIterators.getAllConfigurableFieldsAndSetterParameters(clazz)) {

            ConfigurableProperty propertyAnnotation = configurableChildProperty.getAnnotation(ConfigurableProperty.class);

            Map<String, Object> childPropertyMetadata = new LinkedHashMap<String, Object>();
            classMetaData.put(configurableChildProperty.getAnnotatedName(), childPropertyMetadata);

            if (!propertyAnnotation.label().equals(""))
                childPropertyMetadata.put("title", propertyAnnotation.label());

            if (!propertyAnnotation.description().equals(""))
                childPropertyMetadata.put("description", propertyAnnotation.description());

            if (!propertyAnnotation.defaultValue().equals(ConfigurableProperty.NO_DEFAULT_VALUE))
                childPropertyMetadata.put("default", propertyAnnotation.defaultValue());

            if (!configurableChildProperty.getTags().isEmpty())
                childPropertyMetadata.put("tags", configurableChildProperty.getTags());

            if (includeOrder)
                childPropertyMetadata.put("uiOrder",propertyAnnotation.order());

            childPropertyMetadata.put("uiGroup",propertyAnnotation.group());

            // also merge in the metadata from this child itself
            ConfigTypeAdapter adapter = vitalizer.lookupTypeAdapter(configurableChildProperty);
            Map<String, Object> childMetaData = adapter.getSchema(configurableChildProperty, vitalizer);
            if (childMetaData != null) childPropertyMetadata.putAll(childMetaData);
        }

        return classMetaDataWrapper;
    }

    @Override
    public Map<String, Object> normalize(Object configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
        return (Map<String, Object>) configNode;
    }
}