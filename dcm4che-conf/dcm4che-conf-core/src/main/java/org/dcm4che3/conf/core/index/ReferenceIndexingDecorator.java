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

package org.dcm4che3.conf.core.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.slf4j.LoggerFactory;

/**
 * Keeps an index of objects referable by UUIDs to allow fast lookup by UUID.
 * <p>
 * IMPORTANT:
 * Some cache implementations have issues with isolation, so the base impl treats
 * both absence of objects in the cache as well as empty paths the same way - i.e. like that entry does not exist
 * </p>
 *
 * @author Roman K
 */
public class ReferenceIndexingDecorator extends DelegatingConfiguration {

	private static final Logger log = LoggerFactory.getLogger(ReferenceIndexingDecorator.class);
	
    static PathPattern referencePattern = new PathPattern(Configuration.REFERENCE_BY_UUID_PATTERN);

    protected Map<String, Path> uuidToReferableIndex;

    public ReferenceIndexingDecorator() {
    }
    
    public ReferenceIndexingDecorator(Configuration delegate, Map<String, Path> uuidToSimplePathCache) {
    	
        super(delegate);
        
        uuidToReferableIndex = uuidToSimplePathCache;
    }

    @Override
    public Path getPathByUUID(String uuid) {

        Path pathFromIndex = uuidToReferableIndex.get(uuid);

        if ((pathFromIndex == null || pathFromIndex.getPathItems().isEmpty())) {
            return null;
        }

        return pathFromIndex;
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
            for (Object item : ((List<?>) oldConfigurationNode)) {
                removeOldReferablesFromIndex(item);
            }
        } else if (!Nodes.isPrimitive(oldConfigurationNode))
            throw new RuntimeException("Unexpected node type:" + oldConfigurationNode);

    }

    /**
     * @return list of duplicate uuid exceptions, list is empty if no duplicates detected
     */
    protected List<DuplicateUUIDException> addReferablesToIndex(List<Object> pathItems, Object configNode) {

        final ArrayList<DuplicateUUIDException> uuidDuplicateErrors = new ArrayList<>();

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
                        if (!(oldPath == null || oldPath.getPathItems().isEmpty())) {

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


    protected Object getNodeByUUID(Class<?> configurableClass, String uuid) throws ConfIndexOutOfSyncException {
        Path pathFromIndex = uuidToReferableIndex.get(uuid);

        // see the comment on top
        if ((pathFromIndex == null || pathFromIndex.getPathItems().isEmpty())) {
            return null;
        }

        return super.getConfigurationNode(pathFromIndex, configurableClass);
    }

    @Override
    public void persistNode(Path path, final Map<String, Object> configNode, Class<?> configurableClass) throws ConfigurationException {

        // remove the overwritten referables from index
        removeOldReferablesFromIndex(super.getConfigurationNode(path, null));

        addReferablesToIndex(path.getPathItems(), configNode);

        super.persistNode(path, configNode, configurableClass);
    }

    @Override
    public void refreshNode(Path path) throws ConfigurationException {
        removeOldReferablesFromIndex(super.getConfigurationNode(path, null));
        super.refreshNode(path);
        Object node = super.getConfigurationNode(path, null);
        if (node != null) {
            addReferablesToIndex(path.getPathItems(), node);
        }
    }

    @Override
    public void removeNode(Path path) throws ConfigurationException {
        removeOldReferablesFromIndex(super.getConfigurationNode(path, null));
        super.removeNode(path);
    }

    @Override
    public Iterator<?> search(String liteXPathExpression) throws IllegalArgumentException, ConfigurationException {
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

    protected void removeFromCache(String uuid) {
        uuidToReferableIndex.remove(uuid);
    }
}
