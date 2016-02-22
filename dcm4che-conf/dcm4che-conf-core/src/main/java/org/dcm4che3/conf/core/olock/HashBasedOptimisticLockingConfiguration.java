package org.dcm4che3.conf.core.olock;

import org.dcm4che3.conf.core.DelegatingConfiguration;
import org.dcm4che3.conf.core.api.BatchRunner;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.internal.AnnotatedConfigurableProperty;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser.ADualNodeFilter;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser.ConfigNodeTypesafeFilter;
import org.dcm4che3.conf.core.Nodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
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
    private BatchRunner mergeBatchRunner;
    private List<Class> allExtensionClasses;

    /**
     * @param delegate
     * @param allExtensionClasses
     * @param mergeBatchRunner    a batchrunner that is used by persistNode to execute read/write op to ensure consistency
     */
    public HashBasedOptimisticLockingConfiguration(Configuration delegate, List<Class> allExtensionClasses, BatchRunner mergeBatchRunner) {
        super(delegate);

        this.mergeBatchRunner = mergeBatchRunner;
        this.allExtensionClasses = allExtensionClasses;
    }

    @Override
    public void persistNode(final String path, final Map<String, Object> configNode, final Class configurableClass) throws ConfigurationException {
        mergeBatchRunner.runBatch(new Batch() {
            public void run() {


                Map<String, Object> nodeBeingPersisted = (Map<String, Object>) Nodes.deepCloneNode(configNode);

                // get existing node from storage
                Map<String, Object> nodeInStorage = (Map<String, Object>) getConfigurationNode(path, configurableClass);

                // if there is nothing in storage - just persist and leave
                if (nodeInStorage == null) {
                    if (nodeBeingPersisted != null)
                        ConfigNodeTraverser.traverseMapNode(nodeBeingPersisted, new CleanupFilter(Configuration.OLOCK_HASH_KEY));
                    delegate.persistNode(path, nodeBeingPersisted, configurableClass);
                    return;
                }

                // save old hashes in node being persisted
                ConfigNodeTraverser.traverseMapNode(nodeBeingPersisted, new OLockCopyFilter("#old_hash"));

                // calculate current hashes in node being persisted
                ConfigNodeTraverser.traverseMapNode(nodeBeingPersisted, new OLockHashCalcFilter("#old_hash"));

                ////// merge the object /////
                ConfigNodeTraverser.dualTraverseMapNodes(nodeInStorage, nodeBeingPersisted, new OLockMergeDualFilter());

                // filter the #hash clutter out
                ConfigNodeTraverser.traverseMapNode(nodeBeingPersisted, new CleanupFilter("#old_hash", Configuration.OLOCK_HASH_KEY));

                delegate.persistNode(path, nodeBeingPersisted, configurableClass);
            }
        });
    }


    @Override
    public Object getConfigurationNode(String path, Class configurableClass) throws ConfigurationException {

        // calculate olock hashes
        Object configurationNode = super.getConfigurationNode(path, configurableClass);

        // if called with no configurableClass, omit hashes
        if (configurableClass != null && configurationNode != null) {
            ConfigNodeTraverser.traverseNodeTypesafe(configurationNode, new AnnotatedConfigurableProperty(configurableClass), allExtensionClasses, new HashMarkingTypesafeNodeFilter());
            ConfigNodeTraverser.traverseMapNode(configurationNode, new OLockHashCalcFilter());
        }

        return configurationNode;
    }

    /**
     * Copies #hash props from node1 to node2, sets their value to constant
     */
    public static class HashMarkingCopyFilter extends ADualNodeFilter {
        @Override
        public void beforeNode(Map<String, Object> node1, Map<String, Object> node2) {
            if (node1.containsKey(OLOCK_HASH_KEY))
                node2.put(OLOCK_HASH_KEY, NOT_CALCULATED_YET);

        }
    }

    /**
     * Inserts #hash props into node according to @ConfigurableProperty annotations
     */
    public static class HashMarkingTypesafeNodeFilter implements ConfigNodeTypesafeFilter {

        @Override
        public boolean beforeNode(Map<String, Object> containerNode, Class containerNodeClass, AnnotatedConfigurableProperty property) throws ConfigurationException {

            if (property.isOlockHash()) {
                containerNode.put(OLOCK_HASH_KEY, NOT_CALCULATED_YET);
                return true;
            }

            return false;
        }
    }

}
