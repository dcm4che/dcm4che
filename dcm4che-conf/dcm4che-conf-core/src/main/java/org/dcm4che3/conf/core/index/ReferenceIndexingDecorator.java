package org.dcm4che3.conf.core.index;

import org.dcm4che3.conf.core.DelegatingConfiguration;
import org.dcm4che3.conf.core.Nodes;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser;
import org.dcm4che3.conf.core.util.PathPattern;
import org.dcm4che3.conf.core.util.PathTrackingConfigNodeFilter;
import org.slf4j.Logger;

import java.util.*;

/**
 * Keeps an index of objects referable by uuids to allow fast lookup by uuid
 *
 * @author Roman K
 */
public class ReferenceIndexingDecorator extends DelegatingConfiguration {

    static PathPattern referencePattern = new PathPattern(Configuration.REFERENCE_BY_UUID_PATTERN);

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ReferenceIndexingDecorator.class);

    protected HashMap<String, Path> uuidToReferableIndex;

    public ReferenceIndexingDecorator(Configuration delegate, HashMap<String, Path> uuidToSimplePathCache) {
        super(delegate);
        uuidToReferableIndex = uuidToSimplePathCache;
    }

    private void removeOldReferablesFromIndex(Object oldConfigurationNode) {
        if (oldConfigurationNode instanceof Map) {
            ConfigNodeTraverser.traverseMapNode(oldConfigurationNode, new ConfigNodeTraverser.AConfigNodeFilter() {
                @Override
                public void onPrimitiveNodeElement(Map<String, Object> containerNode, String key, Object value) {
                    if (Configuration.UUID_KEY.equals(key)) uuidToReferableIndex.remove(value);
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
     * @return true if no duplicates detected, false otherwise
     */
    protected boolean addReferablesToIndex(List<String> pathItems, Object configNode) {
        final boolean[] noDuplicates = new boolean[]{true};
        if (configNode instanceof Map) {
            ConfigNodeTraverser.traverseMapNode(configNode, new PathTrackingConfigNodeFilter(pathItems) {
                @Override
                public void onPrimitiveNodeElement(Map<String, Object> containerNode, String key, Object value) {
                    if (Configuration.UUID_KEY.equals(key)) {
                        Object last = path.pop();
                        Path put = uuidToReferableIndex.put((String) value, new Path(path.descendingIterator()));

                        if (put != null) {
                            log.warn("Detected duplicate UUID in configuration: " + value);
                            noDuplicates[0] = false;
                        }

                        path.push(last);
                    }
                }
            });
        } else
            throw new ConfigurationException("Unexpected node type:" + configNode);

        return noDuplicates[0];

        // TODO add proper handling for lists
    }

    @Override
    public Object getConfigurationNode(String path, Class configurableClass) throws ConfigurationException {
        PathPattern.PathParser pathParser = referencePattern.parseIfMatches(path);
        if (pathParser != null) {

            Path pathFromIndex = uuidToReferableIndex.get(pathParser.getParam("uuid"));

            // not found
            if (pathFromIndex == null) {
                return null;
            }

            return super.getConfigurationNode(pathFromIndex.toSimpleEscapedXPath(), configurableClass);
        }

        return super.getConfigurationNode(path, configurableClass);
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
        if (pathItems==null) {
            throw new ConfigurationException("Unexpected path '"+path+"'");
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
            return uuidToReferableIndex.containsKey(pathParser.getParam("uuid"));
        }

        return super.nodeExists(path);
    }

    @Override
    public Iterator search(String liteXPathExpression) throws IllegalArgumentException, ConfigurationException {
        PathPattern.PathParser pathParser = referencePattern.parseIfMatches(liteXPathExpression);
        if (pathParser != null) {

            Path pathFromIndex = uuidToReferableIndex.get(pathParser.getParam("uuid"));

            // not found
            if (pathFromIndex == null) {
                return Collections.emptyList().iterator();
            }

            return super.search(pathFromIndex.toSimpleEscapedXPath());
        }

        return super.search(liteXPathExpression);
    }
}
