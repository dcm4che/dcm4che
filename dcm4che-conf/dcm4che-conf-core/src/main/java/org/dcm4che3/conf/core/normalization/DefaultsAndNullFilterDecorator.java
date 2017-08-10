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
package org.dcm4che3.conf.core.normalization;

import org.dcm4che3.conf.core.DelegatingConfiguration;
import org.dcm4che3.conf.core.adapters.ArrayTypeAdapter;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;
import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.api.internal.ConfigReflection;
import org.dcm4che3.conf.core.context.ContextFactory;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser.ConfigNodeTypesafeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 *
 */
@SuppressWarnings("unchecked")
public class DefaultsAndNullFilterDecorator extends DelegatingConfiguration {

    public static final Logger log = LoggerFactory.getLogger(DefaultsAndNullFilterDecorator.class);


    private final List<Class> allExtensionClasses;
    private final BeanVitalizer vitalizer;
    private final ContextFactory contextFactory;

    public DefaultsAndNullFilterDecorator(Configuration delegate, List<Class> allExtensionClasses, BeanVitalizer vitalizer) {
        super(delegate);
        this.allExtensionClasses = allExtensionClasses;
        this.vitalizer = vitalizer;
        contextFactory = new ContextFactory(vitalizer);
    }

    @Override
    public void persistNode(Path path, final Map<String, Object> configNode, final Class configurableClass) throws ConfigurationException {

        ConfigNodeTypesafeFilter filterDefaults = new ConfigNodeTypesafeFilter() {
            @Override
            public boolean beforeNode(Map<String, Object> containerNode, Class containerNodeClass, ConfigProperty property) throws ConfigurationException {

                boolean doDelete = false;

                String name = property.getAnnotatedName();
                if (property.isOlockHash())
                    name = Configuration.OLOCK_HASH_KEY;
                if (property.isUuid())
                    name = Configuration.CONF_STORAGE_SYSTEM_PROP;

                Object value = containerNode.get(name);

                // if the value for a property equals to default, filter it out
                if (property.getDefaultValue().equals(String.valueOf(value))
                        || value == null) {
                    doDelete = true;
                }
                // if that is an empty extension map or map
                else if ((property.isExtensionsProperty()
                        || property.isMap())
                        && ((Map) value).size() == 0) {
                    doDelete = true;
                }
                // if that is an empty collection
                else if ((property.isCollection() && ((Collection) value).size() == 0)) {
                    doDelete = true;

                } // if that is an array
                else if (property.isArray()) {

                    // it equals the default value, then filter it out
                    Object defaultNode = getDefaultValueFromClass(containerNodeClass, property);
                    if (value.equals(defaultNode)) {
                        doDelete = true;
                    }
                    // if the default is null (i.e. no default) but the array is empty, then also remove it
                    else if (defaultNode == null && ((Collection) value).size() == 0) {
                        doDelete = true;
                    }
                }

                if (doDelete)
                    containerNode.remove(name);

                return doDelete;
            }
        };

        if (configurableClass != null) {
            // filter out defaults
            ConfigNodeTraverser.traverseNodeTypesafe(configNode, ConfigReflection.getDummyPropertyForClass(configurableClass), allExtensionClasses, filterDefaults);

            // generate missing UUIDs
            ConfigNodeTraverser.traverseNodeTypesafe(configNode, ConfigReflection.getDummyPropertyForClass(configurableClass), allExtensionClasses, new ConfigNodeTypesafeFilter() {
                @Override
                public boolean beforeNode(Map<String, Object> containerNode, Class containerNodeClass, ConfigProperty property) throws ConfigurationException {
                    if (property.isUuid() && (containerNode.get(Configuration.UUID_KEY) == null || "".equals(containerNode.get(Configuration.UUID_KEY)))) {
                        String newUUID = UUID.randomUUID().toString();
                        log.warn("Adding a missing UUID [" + newUUID + "] to a " + containerNodeClass.getSimpleName());
                        containerNode.put(Configuration.UUID_KEY, newUUID);
                    }
                    return false;
                }
            });


        }

        super.persistNode(path, configNode, configurableClass);
    }


    @Override
    public Object getConfigurationNode(Path path, Class configurableClass) throws ConfigurationException {

        ConfigNodeTypesafeFilter applyDefaults = new ConfigNodeTypesafeFilter() {
            @Override
            public boolean beforeNode(Map<String, Object> containerNode, Class containerNodeClass, ConfigProperty property) throws ConfigurationException {

                // if no value for this property, see if there is default and set it
                if (!containerNode.containsKey(property.getAnnotatedName())) {
                    String defaultValue = property.getDefaultValue();
                    if (!defaultValue.equals(ConfigurableProperty.NO_DEFAULT_VALUE)) {
                        Object normalized = vitalizer.lookupDefaultTypeAdapter(property.getRawClass()).normalize(defaultValue, property, contextFactory.newProcessingContext());
                        containerNode.put(property.getAnnotatedName(), normalized);
                        return true;
                    }
                    // for null map & extension map - create empty obj
                    else if ((property.isExtensionsProperty() || property.isMap())) {
                        containerNode.put(property.getAnnotatedName(), new TreeMap());
                        return true;
                    }
                    // for null collections - create empty list
                    else if ((property.isCollection())) {
                        containerNode.put(property.getAnnotatedName(), new ArrayList());
                        return true;
                    }
                    // for null array - set to default value
                    else if (property.isArray()) {
                        Object defaultNode = getDefaultValueFromClass(containerNodeClass, property);
                        if(defaultNode == null) {
                            containerNode.put( property.getAnnotatedName(), new ArrayList() );
                        }else {
                            containerNode.put( property.getAnnotatedName(), defaultNode );
                        }
                        return true;
                    }
                }

                return false;
            }
        };

        // fill in default values for properties that are null and have defaults
        Map<String, Object> node = (Map<String, Object>) super.getConfigurationNode(path, configurableClass);
        if (configurableClass != null && node != null)
            ConfigNodeTraverser.traverseNodeTypesafe(node, ConfigReflection.getDummyPropertyForClass(configurableClass), allExtensionClasses, applyDefaults);
        return node;
    }

    private Object getDefaultValueFromClass(Class containerNodeClass, ConfigProperty property) {
        Object defaultValueFromClass;
        defaultValueFromClass = ConfigReflection.getProperty(vitalizer.newInstance(containerNodeClass), property);
        return new ArrayTypeAdapter().toConfigNode(defaultValueFromClass, property, contextFactory.newSavingContext());
    }


    @Override
    public Iterator search(String liteXPathExpression) throws IllegalArgumentException, ConfigurationException {
        return super.search(liteXPathExpression);
    }
}
