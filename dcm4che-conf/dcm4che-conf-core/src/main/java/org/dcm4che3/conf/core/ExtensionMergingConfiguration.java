package org.dcm4che3.conf.core;

import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.internal.AnnotatedConfigurableProperty;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser;

import java.util.Map;

/**
 * Ensures that extensions are not deleted, even if they are unknown to the caller of persistNode
 */
public class ExtensionMergingConfiguration extends DelegatingConfiguration {
    public ExtensionMergingConfiguration(Configuration delegate) {
        super(delegate);
    }


    @Override
    public void persistNode(String path, Map<String, Object> configNode, Class configurableClass) throws ConfigurationException {

        // make sure we don't delete the extensions we are not aware of

        Object configurationNode = super.getConfigurationNode(path, configurableClass);

        ConfigNodeTraverser.traverseNodeTypesafe(configNode, new AnnotatedConfigurableProperty(), null, new ConfigNodeTraverser.ConfigNodeTypesafeFilter() {
            @Override
            public boolean beforeNode(Map<String, Object> containerNode, Class containerNodeClass, AnnotatedConfigurableProperty property) throws ConfigurationException {

                if (property.isExtensionsProperty()) ;
                    //...

                return false;
            }
        });

        super.persistNode(path, configNode, configurableClass);
    }
}
