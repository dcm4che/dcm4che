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

import org.dcm4che3.conf.core.DefaultBeanVitalizer;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.internal.AnnotatedConfigurableProperty;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.DelegatingConfiguration;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser.ConfigNodeTypesafeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 */
@SuppressWarnings("unchecked")
public class DefaultsAndNullFilterDecorator extends DelegatingConfiguration {

    public static final Logger log = LoggerFactory.getLogger(DefaultsAndNullFilterDecorator.class);


    protected List<Class> allExtensionClasses;
    private BeanVitalizer dummyVitalizer = new DefaultBeanVitalizer();

    public DefaultsAndNullFilterDecorator(Configuration delegate, List<Class> allExtensionClasses) {
        super(delegate);
        this.allExtensionClasses = allExtensionClasses;
    }

    @Override
    public void persistNode(String path, Map<String, Object> configNode, Class configurableClass) throws ConfigurationException {

        ConfigNodeTypesafeFilter filterDefaults = new ConfigNodeTypesafeFilter() {
            @Override
            public boolean beforeNode(Map<String, Object> containerNode, AnnotatedConfigurableProperty property) throws ConfigurationException {

                boolean doDelete = false;

                // if the value for a property equals to default, filter it out
                if (property.getDefaultValue().equals(String.valueOf(containerNode.get(property.getAnnotatedName())))
                        || containerNode.get(property.getAnnotatedName()) == null) {
                    doDelete = true;
                } // if that is an empty extension map or map
                else if ((property.isExtensionsProperty() || property.isMap())
                        && containerNode.get(property.getAnnotatedName()) != null
                        && ((Map) containerNode.get(property.getAnnotatedName())).size() == 0) {
                    doDelete = true;
                } // if that is an empty collection or array
                else if ((property.isCollection() || property.isArray())
                        && containerNode.get(property.getAnnotatedName()) != null
                        && ((Collection) containerNode.get(property.getAnnotatedName())).size() == 0) {
                    doDelete = true;
                }

                if (doDelete)
                    containerNode.remove(property.getAnnotatedName());

                return doDelete;
            }
        };

        // filter out defaults
        if (configurableClass != null)
            ConfigNodeTraverser.traverseNodeTypesafe(configNode, configurableClass, filterDefaults, allExtensionClasses);

        super.persistNode(path, configNode, configurableClass);
    }


    @Override
    public Object getConfigurationNode(String path, Class configurableClass) throws ConfigurationException {

        ConfigNodeTypesafeFilter applyDefaults = new ConfigNodeTypesafeFilter() {
            @Override
            public boolean beforeNode(Map<String, Object> containerNode, AnnotatedConfigurableProperty property) throws ConfigurationException {

                String defaultValue = property.getAnnotation(ConfigurableProperty.class).defaultValue();
                // if no value for this property, see if there is default and set it
                if (!containerNode.containsKey(property.getAnnotatedName()) && !defaultValue.equals(ConfigurableProperty.NO_DEFAULT_VALUE)) {
                    Object normalized = dummyVitalizer.lookupDefaultTypeAdapter(property.getRawClass()).normalize(defaultValue, property, dummyVitalizer);
                    containerNode.put(property.getAnnotatedName(), normalized);
                    return true;
                }
                // for null map & extension map - create empty obj
                else if ((property.isExtensionsProperty() || property.isMap()) &&
                        containerNode.get(property.getAnnotatedName()) == null) {
                    containerNode.put(property.getAnnotatedName(), new TreeMap());
                    return true;
                }
                // for null arrays/collections map - create empty arr
                else if ((property.isCollection() || property.isArray())
                        && containerNode.get(property.getAnnotatedName()) == null) {
                    containerNode.put(property.getAnnotatedName(), new ArrayList());
                    return true;
                }

                return false;
            }
        };

        // fill in default values for properties that are null and have defaults
        Map<String, Object> node = (Map<String, Object>) super.getConfigurationNode(path, configurableClass);
        if (configurableClass != null && node != null)
            ConfigNodeTraverser.traverseNodeTypesafe(node, configurableClass, applyDefaults, allExtensionClasses);
        return node;
    }


    @Override
    public Iterator search(String liteXPathExpression) throws IllegalArgumentException, ConfigurationException {
        return super.search(liteXPathExpression);
    }
}
