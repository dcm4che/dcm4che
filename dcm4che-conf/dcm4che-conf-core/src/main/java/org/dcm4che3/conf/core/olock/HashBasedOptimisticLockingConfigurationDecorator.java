package org.dcm4che3.conf.core.olock;

import java.util.List;
import java.util.Map;

import org.dcm4che3.conf.core.DelegatingConfiguration;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.api.internal.ConfigReflection;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser.ConfigNodeTypesafeFilter;

/**
 * This decorator makes it possible to use hash-based optimistic lock merge algorithm to compare
 * and merge configuration records when storage version does not match during updates.
 * 
 * @author Roman K
 * @author Maciek Siemczyk (maciek.siemczyk@agfa.com)
 */
@SuppressWarnings("rawtypes")
public class HashBasedOptimisticLockingConfigurationDecorator extends DelegatingConfiguration {
    
    private static final String NOT_CALCULATED_YET = "not-calculated-yet";

    private final List<Class> allExtensionClasses;

    /**
     * Default constructor.
     * 
     * @param delegate
     * @param allExtensionClasses
     */
    public HashBasedOptimisticLockingConfigurationDecorator(
            Configuration delegate,
            List<Class> allExtensionClasses) {
        
        super(delegate);
        
        this.allExtensionClasses = allExtensionClasses;
    }

    @Override
    public Object getConfigurationNode(Path path, Class configurableClass) throws ConfigurationException {

        // load node from (storage) backend
        Object configurationNode = super.getConfigurationNode(path, configurableClass);

        //  calculate olock hashes if called with configurableClass
        if (configurableClass != null && configurationNode != null) {

            ConfigNodeTraverser.traverseNodeTypesafe(
                    configurationNode,
                    ConfigReflection.getDummyPropertyForClass(configurableClass),
                    allExtensionClasses,
                    new HashMarkingTypesafeNodeFilter()
            );

            // calculate hashes and set them in OLOCK_HASH_KEY property, ignoring storage version.
            ConfigNodeTraverser.traverseMapNode(configurationNode, new OLockHashCalcFilter(Configuration.VERSION_KEY));
        }

        return configurationNode;
    }

    /**
     * Inserts #hash props into node according to @ConfigurableProperty annotations
     */
    public static final class HashMarkingTypesafeNodeFilter implements ConfigNodeTypesafeFilter {

        @Override
        public boolean beforeNode(
                Map<String, Object> containerNode,
                Class containerNodeClass,
                ConfigProperty property) throws ConfigurationException {

            if (property.isOlockHash()) {
                containerNode.put(OLOCK_HASH_KEY, NOT_CALCULATED_YET);
                return true;
            }

            return false;
        }
    }
}
