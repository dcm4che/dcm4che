package org.dcm4che3.conf.core;

import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.api.internal.ConfigReflection;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Ensures that extensions are not deleted, even if they are unknown to the caller of persistNode
 */
public class ExtensionMergingConfiguration extends DelegatingConfiguration {

    private static Logger log = LoggerFactory.getLogger(ExtensionMergingConfiguration.class);

    protected List<Class> allExtensionClasses;

    public ExtensionMergingConfiguration(Configuration delegate, List<Class> allExtensionClasses) {
        super(delegate);
        this.allExtensionClasses = allExtensionClasses;
    }

    @Override
    public void persistNode(Path path, Map<String, Object> configNode, Class configurableClass) throws ConfigurationException {

        // make sure we don't delete the extensions we are not aware of

        if (configurableClass != null) {
            Object currentConfigurationNode = super.getConfigurationNode(path, configurableClass);

            ConfigNodeTraverser.dualTraverseNodeTypesafe(configNode, currentConfigurationNode, ConfigReflection.getDummyPropertyForClass(configurableClass), allExtensionClasses, new ConfigNodeTraverser.ConfigNodesTypesafeFilter() {
                @Override
                public void beforeNodes(Map<String, Object> containerNode1, Map<String, Object> containerNode2, Class containerNodeClass, ConfigProperty property) throws ConfigurationException {

                if (property.isExtensionsProperty()) {
                        //Preserve each key of the current configuration that do not belong to the new configuration
                        Map<String, Object> extensionsMap1 = (Map<String, Object>) containerNode1.get(property.getAnnotatedName());
                        Map<String, Object> extensionsMap2 = (Map<String, Object>) containerNode2.get(property.getAnnotatedName());

                        if(extensionsMap1 == null && extensionsMap2 == null) return;

                        if(extensionsMap1 == null){
                            containerNode1.put(property.getAnnotatedName(), extensionsMap2);
                            return;
                        }

                        if(extensionsMap2 == null) return;

                        for (String key : extensionsMap2.keySet()) {
                            if(!extensionsMap1.containsKey(key)){
                                extensionsMap1.put(key,extensionsMap2.get(key));
                            }
                        }
                    }
                }
            });
        }
        super.persistNode(path, configNode, configurableClass);
    }
}
