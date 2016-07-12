package org.dcm4che3.conf.core.index;

import org.dcm4che3.conf.core.DelegatingConfiguration;
import org.dcm4che3.conf.core.Nodes;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.DuplicateUUIDException;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser;
import org.dcm4che3.conf.core.util.PathPattern;
import org.dcm4che3.conf.core.util.PathTrackingConfigNodeFilter;
import org.slf4j.Logger;

import java.util.*;

/**
 * Keeps an index of objects referable by uuids to allow fast lookup by uuid
 * <p>
 * IMPORTANT:
 * Some cache implementations have issues with isolation, so the base impl treats
 * both absence of objects in the cache as well as empty paths the same way - i.e. like that entry does not exist
 * </p>
 *
 * @author Roman K
 */
public class ReferenceIndexingDecorator extends DelegatingConfiguration implements ReferablesIndex {

    static PathPattern referencePattern = new PathPattern(Configuration.REFERENCE_BY_UUID_PATTERN);

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ReferenceIndexingDecorator.class);

    protected Map<String, Path> uuidToReferableIndex;

    public ReferenceIndexingDecorator() {
    }

    public ReferenceIndexingDecorator(Configuration delegate, Map<String, Path> uuidToSimplePathCache) {
        super(delegate);
        uuidToReferableIndex = uuidToSimplePathCache;
    }

    @Override
    public Path getPathByUUID(String uuid) {
        return uuidToReferableIndex.get(uuid);
    }

    protected void removeOldReferablesFromIndex(Object oldConfigurationNode) {
        if (oldConfigurationNode instanceof Map) {
            ConfigNodeTraverser.traverseMapNode(oldConfigurationNode, new ConfigNodeTraverser.AConfigNodeFilter() {
                @Override
                public void onPrimitiveNodeElement(Map<String, Object> containerNode, String key, Object value) {
                    if (Configuration.UUID_KEY.equals(key)) {
                        String uuid = (String) value;

                        removeFromCache(uuid);
                    }
                }
            });
        } else if (oldConfigurationNode instanceof List) {
            for (Object item : ((List) oldConfigurationNode)) {
                removeOldReferablesFromIndex(item);
            }
        } else if (!Nodes.isPrimitive(oldConfigurationNode))
            throw new RuntimeException("Unexpected node type:" + oldConfigurationNode);

    }

    /**
     * @return list of duplicate uuid exceptions, list is empty if no duplicates detected
     */
    protected List<DuplicateUUIDException> addReferablesToIndex(List<String> pathItems, Object configNode) {

        final ArrayList<DuplicateUUIDException> uuidDuplicateErrors = new ArrayList<DuplicateUUIDException>();

        if (configNode instanceof Map) {
            ConfigNodeTraverser.traverseMapNode(configNode, new PathTrackingConfigNodeFilter(pathItems) {
                @Override
                public void onPrimitiveNodeElement(Map<String, Object> containerNode, String key, Object value) {
                    if (Configuration.UUID_KEY.equals(key)) {
                        Object last = path.pop();
                        String uuid;
                        try {
                            uuid = (String) value;
                        } catch (ClassCastException e) {
                            throw new IllegalArgumentException("UUID must be a string, got " + value);
                        }

                        Path newPath = new Path(this.path.descendingIterator());

                        // using return value of put does not seem to work as expected with infinispan (maybe some issues with isolation...)
                        Path oldPath = uuidToReferableIndex.get(uuid);
                        uuidToReferableIndex.put(uuid, newPath);

                        // see the comment on top
                        if (!(oldPath == null || oldPath.getPathItems().size() == 0)) {

                            DuplicateUUIDException duplicateUUIDException = new DuplicateUUIDException(uuid, oldPath, newPath);

                            log.warn("Duplicate UUID found while adding references to index", duplicateUUIDException);
                            uuidDuplicateErrors.add(duplicateUUIDException);
                        }

                        this.path.push(last);
                    }
                }
            });
        } else
            throw new ConfigurationException("Unexpected node type:" + configNode);

        return uuidDuplicateErrors;

        // TODO add proper handling for lists
    }

    @Override
    public Object getConfigurationNode(String path, Class configurableClass) throws ConfigurationException {
        PathPattern.PathParser pathParser = referencePattern.parseIfMatches(path);
        if (pathParser != null) {

            String uuid = pathParser.getParam("uuid");
            try {
                return getNodeByUUID(configurableClass, uuid);
            } catch (ConfIndexOutOfSyncException e) {
                return super.getConfigurationNode(path, configurableClass);
            }
        }

        return super.getConfigurationNode(path, configurableClass);
    }

    /**
     * A bit of resilience for the time being.
     * Returns a node if it is successfully fetched from the cache by indexed path and validated against the uuid
     */
    protected Object getNodeByUUID(Class configurableClass, String uuid) throws ConfIndexOutOfSyncException {
        Path pathFromIndex = uuidToReferableIndex.get(uuid);

        // see the comment on top
        if (!(pathFromIndex == null || pathFromIndex.getPathItems().size() == 0)) {
            Object configurationNode = super.getConfigurationNode(pathFromIndex.toSimpleEscapedXPath(), configurableClass);

            // verify correct uuid
            try {
                Object detectedUUID = ((Map<String, Object>) configurationNode).get(Configuration.UUID_KEY);
                if (!uuid.equals(detectedUUID))
                    throw new ConfIndexOutOfSyncException("Index for UUID (" + uuid + ") points to an object with UUID (" + detectedUUID + ")");

                return configurationNode;
            } catch (ConfIndexOutOfSyncException e) {
                throw e;
            } catch (Exception e) {

                log.error("Configuration UUID index is out of sync - index for uuid " + uuid
                        + " points to a non-existing node or a node with unexpected type (node = " + configurationNode + ")", e);

                throw new ConfIndexOutOfSyncException();
            }


        } else {

            // not found, try to fall back in case index got outdated for some reason
            Object node = super.getConfigurationNode(referencePattern.set("uuid", uuid).path(), configurableClass);
            if (node != null) {
                log.error("Configuration UUID index is out of sync: uuid '" + uuid + "' not indexed but was found");
                throw new ConfIndexOutOfSyncException();
            } else {
                log.warn("Looked up UUID of non-existing configuration object: '" + uuid);
                return null;
            }
        }
    }

    @Override
    public void persistNode(String path, final Map<String, Object> configNode, Class configurableClass) throws ConfigurationException {

        // remove the overwritten referables from index
        removeOldReferablesFromIndex(super.getConfigurationNode(path, null));

        // add newcomer referables to index
        List<String> pathItems = getPathItemsOrFail(path);

        addReferablesToIndex(pathItems, configNode);

        super.persistNode(path, configNode, configurableClass);
    }

    @Override
    public void refreshNode(String path) throws ConfigurationException {
        removeOldReferablesFromIndex(super.getConfigurationNode(path, null));
        super.refreshNode(path);
        List<String> pathItems = getPathItemsOrFail(path);

        addReferablesToIndex(pathItems, super.getConfigurationNode(path, null));
    }

    private List<String> getPathItemsOrFail(String path) {
        List<String> pathItems = Nodes.simpleOrPersistablePathToPathItemsOrNull(path);
        if (pathItems == null) {
            throw new ConfigurationException("Unexpected path '" + path + "'");
        }
        return pathItems;
    }

    @Override
    public void removeNode(String path) throws ConfigurationException {
        removeOldReferablesFromIndex(super.getConfigurationNode(path, null));
        super.removeNode(path);
    }

    @Override
    public boolean nodeExists(String path) throws ConfigurationException {
        PathPattern.PathParser pathParser = referencePattern.parseIfMatches(path);
        if (pathParser != null) {

            Path uuid = uuidToReferableIndex.get(pathParser.getParam("uuid"));

            // see the comment on top
            return !(uuid == null || uuid.getPathItems().size() == 0);
        }

        return super.nodeExists(path);
    }

    @Override
    public Iterator search(String liteXPathExpression) throws IllegalArgumentException, ConfigurationException {
        PathPattern.PathParser pathParser = referencePattern.parseIfMatches(liteXPathExpression);
        if (pathParser != null) {

            String uuid = pathParser.getParam("uuid");
            Object nodeByUUID = null;
            try {
                nodeByUUID = getNodeByUUID(null, uuid);
            } catch (ConfIndexOutOfSyncException e) {
                return super.search(liteXPathExpression);
            }

            // not found
            if (nodeByUUID == null) {
                return Collections.emptyList().iterator();
            } else {
                return Collections.singleton(nodeByUUID).iterator();
            }
        }

        return super.search(liteXPathExpression);
    }

    protected Path getPathByUUIDFromIndex(String uuid) {
        return uuidToReferableIndex.get(uuid);
    }

    protected void removeFromCache(String uuid) {
        uuidToReferableIndex.remove(uuid);
    }

}
