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

package org.dcm4che3.conf.core;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.api.internal.ConfigReflection;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ensures that extensions are not deleted, even if they are unknown to the caller of persistNode
 */
@SuppressWarnings("rawtypes")
public class ExtensionMergingConfiguration extends DelegatingConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ExtensionMergingConfiguration.class);

    protected List<Class> allExtensionClasses;

    public ExtensionMergingConfiguration(Configuration delegate, List<Class> allExtensionClasses) {
        
        super(delegate);
        
        this.allExtensionClasses = allExtensionClasses;
    }

    @Override
    public void persistNode(Path path, Map<String, Object> configNode, Class<?> configurableClass) throws ConfigurationException {

        // make sure we don't delete the extensions we are not aware of
        if (configurableClass != null) {
            Object currentConfigurationNode = super.getConfigurationNode(path, configurableClass);

            ConfigProperty dummyPropertyForClass = ConfigReflection.getDummyPropertyForClass(configurableClass);
            
            log.debug("About to merge extensions for {} between (new) config node {} "
                            + "and existing (current) config node {}.",
                    configurableClass, configNode, currentConfigurationNode);
            
            // configNode (new values) = node1 and currentConfigurationNode (existing) = node2
            ConfigNodeTraverser.dualTraverseNodeTypesafe(configNode, currentConfigurationNode,
                    dummyPropertyForClass, allExtensionClasses, new ExtensionMergingTypesafeFilter());
        }
        
        super.persistNode(path, configNode, configurableClass);
    }
    
    private final class ExtensionMergingTypesafeFilter implements ConfigNodeTraverser.ConfigNodesTypesafeFilter {
        
        @SuppressWarnings("unchecked")
        @Override
        public void beforeNodes(
                Map<String, Object> containerNode1,
                Map<String, Object> containerNode2,
                Class containerNodeClass,
                ConfigProperty property) throws ConfigurationException {

            if (property.isExtensionsProperty()) {
                //Preserve each key of the current configuration that do not belong to the new configuration
                Map<String, Object> extensionsMap1 = (Map<String, Object>) containerNode1.get(property.getAnnotatedName());
                Map<String, Object> extensionsMap2 = (Map<String, Object>) containerNode2.get(property.getAnnotatedName());

                if (extensionsMap1 == null && extensionsMap2 == null) {
                    log.debug("Both extension maps are null, nothing to do.");
                    return;
                }

                if (extensionsMap1 == null) {
                    log.debug("Adding existing extension map {} to the (new) config node.", extensionsMap2);
                    
                    containerNode1.put(property.getAnnotatedName(), extensionsMap2);
                    return;
                }

                if (extensionsMap2 == null) {
                    log.debug("Existing config node has no extensions, nothing to do.");
                    return;
                }
                
                log.debug("Merging extension map {} into {}.", extensionsMap2, extensionsMap1);

                for (Entry<String, Object> entry : extensionsMap2.entrySet()) {
                    extensionsMap1.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }
        }
    }
}
