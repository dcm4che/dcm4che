package org.dcm4che3.conf.core.olock;

import java.util.Map;

import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser.AConfigNodeFilter;

/**
 * This class is responsible for merging configuration nodes using hash-based optimistic locking
 * approach. This is accomplished by traversing the nodes and using {@link AConfigNodeFilter}s to
 * do the actual work.
 * <p>
 * Edge cases:
 * <ul>
 *  <li>
 *      If one deletes a node that is a root of olock, and another changes that node, it is NOT considered a conflict,
 *      regardless of operation order the node will be just deleted. This holds for case of a subnode, a map entry,
 *      or a collection element that has a uuid.
 *  </li>
 *  <li>
 *      For any collections whose elements don't have uuids, a collection is always fully overwritten without trying
 *      to merge in any modified contents from the backend.
 *  </li>
 * </ul> 
 * 
 * @author Roman K
 * @author Maciek Siemczyk (maciek.siemczyk@agfa.com)
 * 
 * @see OLockHashCalcFilter
 * @see OLockMergeDualFilter
 */
public class OLockNodeMerger {
    
    public static final String OLD_OLOCK_HASH_KEY = "#old_hash";

    /**
     * Private constructor to prevent instantiation.
     */
    private OLockNodeMerger() { }
    
    /**
     * Merges two configuration nodes together using the optimistic locking hashes for comparison.
     * 
     * @param nodeBeingPersisted Config node with "new" data.
     * @param nodeInStorage Config node as it is currently in the storage.
     * 
     * @throws OLockMergeException if nodes cannot be merged.
     */
    public static void merge(
            Map<String, Object> nodeBeingPersisted,
            Map<String, Object> nodeInStorage) throws OLockMergeException {

        // save old hashes in node being persisted
        ConfigNodeTraverser.traverseMapNode(nodeBeingPersisted, new OLockCopyFilter(OLD_OLOCK_HASH_KEY));

        // calculate current hashes in node being persisted and set them in OLOCK_HASH_KEY property,
        // ensure to ignore OLD_OLOCK_HASH_KEY property in calculation
        ConfigNodeTraverser.traverseMapNode(
            nodeBeingPersisted,
            new OLockHashCalcFilter(OLD_OLOCK_HASH_KEY, Configuration.VERSION_KEY));

        ////// merge the object /////
        ConfigNodeTraverser.dualTraverseMapNodes(nodeInStorage, nodeBeingPersisted, new OLockMergeDualFilter());

        // filter all the set #hash properties
        ConfigNodeTraverser.traverseMapNode(
            nodeBeingPersisted,
            new CleanupFilter(OLD_OLOCK_HASH_KEY, Configuration.OLOCK_HASH_KEY));
    }
}
