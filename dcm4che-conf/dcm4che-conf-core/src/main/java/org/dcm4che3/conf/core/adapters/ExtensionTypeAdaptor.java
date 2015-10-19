/*
 * *** BEGIN LICENSE BLOCK *****
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
 *  Portions created by the Initial Developer are Copyright (C) 2015
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
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.SetParentIntoField;
import org.dcm4che3.conf.core.api.internal.*;
import org.dcm4che3.conf.core.util.Extensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Roman K
 */
public class ExtensionTypeAdaptor implements ConfigTypeAdapter<Map<Class<?>, Object>, Map<String, Object>> {

    public static final Logger log = LoggerFactory.getLogger(ExtensionTypeAdaptor.class);

    @Override
    public Map<Class<?>, Object> fromConfigNode(Map<String, Object> configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer, Object parent) throws ConfigurationException {

        // figure out base extension class
        Class<Object> extensionBaseClass = null;
        try {
            extensionBaseClass = (Class<Object>) property.getTypeForGenericsParameter(1);
        } catch (ClassCastException e) {
            throw new ConfigurationException("Incorrectly annotated extensions field, parameter 1 must be an extension class", e);
        }

        Map<Class<?>, Object> extensionsMap = new HashMap<Class<?>, Object>();

        for (Map.Entry<String, Object> entry : configNode.entrySet()) {
            try {

                // figure out current extension class
                List<Class<?>> extensionClasses = vitalizer.getContext(ConfigurationManager.class).getExtensionClassesByBaseClass(extensionBaseClass);
                Class<?> extensionClass = Extensions.getExtensionClassBySimpleName(entry.getKey(), extensionClasses);

                // create empty extension bean
                Object extension = vitalizer.newInstance(extensionClass);

                // set parent so this field is accessible for use in extension bean's setters
                SetParentIntoField setParentIntoField = extensionBaseClass.getAnnotation(SetParentIntoField.class);
                if (setParentIntoField != null)
                    try {
                        PropertyUtils.setSimpleProperty(extension, setParentIntoField.value(), parent);
                    } catch (Exception e) {
                        throw new ConfigurationException(
                                "Could not 'inject' parent object into field specified by 'SetParentIntoField' annotation. Field '" + setParentIntoField.value() + "'", e);
                    }

                // proceed with deserialization
                vitalizer.configureInstance(extension, (Map<String, Object>) entry.getValue(), extensionClass);

                extensionsMap.put(extensionClass, extension);

            } catch (ClassNotFoundException e) {
                // noop
                log.warn("Extension class {} not found", entry.getKey());
            }
        }

        return extensionsMap;
    }

    @Override
    public Map<String, Object> toConfigNode(Map<Class<?>, Object> object, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {

        Map<String, Object> extensionsMapNode = new TreeMap<String, Object>();

        for (Map.Entry<Class<?>, Object> classObjectEntry : object.entrySet()) {
            Object extensionNode = vitalizer.createConfigNodeFromInstance(classObjectEntry.getValue(), classObjectEntry.getKey());
            extensionsMapNode.put(classObjectEntry.getKey().getSimpleName(), extensionNode);
        }

        return extensionsMapNode;
    }

    @Override
    public Map<String, Object> getSchema(AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("type", "extensionMap");
        return metadata;
    }

    @Override
    public Map<String, Object> normalize(Object configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
        if (configNode == null)
            return new HashMap<String, Object>();
        return (Map<String, Object>) configNode;
    }
}
