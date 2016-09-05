package org.dcm4che3.conf.core.olock;

import org.dcm4che3.conf.core.DelegatingConfiguration;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.api.internal.ConfigReflection;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser.ConfigNodeTypesafeFilter;
import org.dcm4che3.conf.core.Nodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 *
 *
 * Caution: persistNode should be called in a transaction to ensure consistent comparison between data from backend/data being persisted,
 * otherwise it's possible that another writer modifies something before this class obtains a lock and the changes by that writer will be lost
 *
 * Edge cases:
 * <ul>
 * <li>
 *  If one deletes a node that is a root of olock, and another changes that node, it is NOT considered a conflict, regardless of operation order the node will be just deleted.
 *  this holds for case of a subnode, a map entry, or a collection element that has a uuid.
 *  </li><li>
 *  For any collections whose elements don't have uuids, a collection is always fully overwritten without trying
 *  to merge in any modified contents from the backend.
 *  </li>
 * </ul>
 *
 *
 * @author Roman K
 */
@SuppressWarnings("unchecked")
public class HashBasedOptimisticLockingConfiguration extends DelegatingConfiguration {

    private static final Logger log = LoggerFactory.getLogger(HashBasedOptimisticLockingConfiguration.class);

    public static final String NOT_CALCULATED_YET = "not-calculated-yet";
    public static final String OLD_OLOCK_HASH_KEY = "#old_hash";

    private List<Class> allExtensionClasses;

    /**
     * @param delegate
     * @param allExtensionClasses
     */
    public HashBasedOptimisticLockingConfiguration(Configuration delegate, List<Class> allExtensionClasses) {
        super(delegate);

        this.allExtensionClasses = allExtensionClasses;
    }

    @Override
    public void persistNode(final Path path, final Map<String, Object> configNode, final Class configurableClass) throws ConfigurationException {
        Map<String, Object> nodeBeingPersisted = (Map<String, Object>) Nodes.deepCloneNode(configNode);

        // get existing node from storage
        Map<String, Object> nodeInStorage = (Map<String, Object>) getConfigurationNode(path, configurableClass);

        // if there is nothing in storage - just persist and leave
        if (nodeInStorage == null) {
            if (nodeBeingPersisted != null) {
                ConfigNodeTraverser.traverseMapNode(nodeBeingPersisted, new CleanupFilter(Configuration.OLOCK_HASH_KEY));
            }
            delegate.persistNode(path, nodeBeingPersisted, configurableClass);
            return;
        }

        // save old hashes in node being persisted
        ConfigNodeTraverser.traverseMapNode(nodeBeingPersisted, new OLockCopyFilter(OLD_OLOCK_HASH_KEY));

        // calculate current hashes in node being persisted
        ConfigNodeTraverser.traverseMapNode(nodeBeingPersisted, new OLockHashCalcFilter(OLD_OLOCK_HASH_KEY));

        ////// merge the object /////
        ConfigNodeTraverser.dualTraverseMapNodes(nodeInStorage, nodeBeingPersisted, new OLockMergeDualFilter());

        // filter the #hash clutter out
        ConfigNodeTraverser.traverseMapNode(nodeBeingPersisted, new CleanupFilter(OLD_OLOCK_HASH_KEY, Configuration.OLOCK_HASH_KEY));

        delegate.persistNode(path, nodeBeingPersisted, configurableClass);
    }


    @Override
    public Object getConfigurationNode(Path path, Class configurableClass) throws ConfigurationException {

        Object configurationNode = super.getConfigurationNode(path, configurableClass);

        //  calculate olock hashes if called with configurableClass
        if (configurableClass != null && configurationNode != null) {

            ConfigNodeTraverser.traverseNodeTypesafe(
                    configurationNode,
                    ConfigReflection.getDummyPropertyForClass(configurableClass),
                    allExtensionClasses,
                    new HashMarkingTypesafeNodeFilter()
            );

            ConfigNodeTraverser.traverseMapNode(configurationNode, new OLockHashCalcFilter());
        }

        return configurationNode;
    }

    /**
     * Inserts #hash props into node according to @ConfigurableProperty annotations
     */
    public static class HashMarkingTypesafeNodeFilter implements ConfigNodeTypesafeFilter {

        @Override
        public boolean beforeNode(Map<String, Object> containerNode, Class containerNodeClass, ConfigProperty property) throws ConfigurationException {

            if (property.isOlockHash()) {
                containerNode.put(OLOCK_HASH_KEY, NOT_CALCULATED_YET);
                return true;
            }

            return false;
        }
    }

}
