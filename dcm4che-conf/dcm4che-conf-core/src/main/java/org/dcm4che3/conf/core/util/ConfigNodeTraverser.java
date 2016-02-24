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
package org.dcm4che3.conf.core.util;

import org.dcm4che3.conf.core.Nodes;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.internal.AnnotatedConfigurableProperty;
import org.dcm4che3.conf.core.api.internal.ConfigIterators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("unchecked")
public class ConfigNodeTraverser {

    private static Logger log = LoggerFactory.getLogger(ConfigNodeTraverser.class);

    public interface ConfigNodeTypesafeFilter {
        boolean beforeNode(Map<String, Object> containerNode, Class containerNodeClass, AnnotatedConfigurableProperty property) throws ConfigurationException;
    }

    public static class AConfigNodeFilter {
        public void beforeNodeElement(Map<String, Object> containerNode, String key, Object value) {
        }

        public void afterNodeElement(Map<String, Object> containerNode, String key, Object value) {
        }

        public void beforeNode(Map<String, Object> node) {
        }

        public void afterNode(Map<String, Object> node) {
        }

        public void beforeList(Collection list) {
        }

        public void beforeListElement(Collection list, Object element) {
        }

        public void afterListElement(Collection list, Object element) {
        }

        public void afterList(Collection list) {
        }

        /**
         * Fired for Boolean,String,Number,null
         */
        public void onPrimitiveNodeElement(Map<String, Object> containerNode, String key, Object value) {
        }

        /**
         * Fired for Boolean,String,Number,null
         */
        public void onPrimitiveListElement(Collection list, Object element) {
        }
    }

    public static class ADualNodeFilter {
        public void beforeNode(Map<String, Object> node1, Map<String, Object> node2) {

        }

        public void afterNode(Map<String, Object> node1, Map<String, Object> node2) {

        }

        public void afterNodeProperty(String key) {

        }

        public void beforeNodeProperty(String key) {

        }

        public void beforeListElement(int index1, int index2) {

        }

        public void afterListElement(int index1, int index2) {

        }

        /**
         * @param node1
         * @param node2
         * @return false if the traverser should skip this list, true if it should proceed normally
         */
        public boolean beforeList(List node1, List node2) {
            return true;
        }

        public void afterList(List node1, List node2) {

        }
    }

    public static void traverseNodeTypesafe(Object node, AnnotatedConfigurableProperty containerProperty, List<Class> allExtensionClasses, ConfigNodeTypesafeFilter filter) throws ConfigurationException {

        // if because of any reason this is not a map (e.g. a reference or a custom adapter for a configurableclass),
        // we don't go deeper
        if (!(node instanceof Map)) return;

        // if that's a reference, don't traverse deeper
        if (containerProperty.isReference()) return;

        Map<String, Object> containerNode = (Map<String, Object>) node;

        List<AnnotatedConfigurableProperty> properties = ConfigIterators.getAllConfigurableFieldsAndSetterParameters(containerProperty.getRawClass());
        for (AnnotatedConfigurableProperty property : properties) {
            Object childNode = containerNode.get(property.getAnnotatedName());

            if (filter.beforeNode(containerNode, containerProperty.getRawClass(), property)) continue;

            if (childNode == null) continue;

            // if the property is a configclass
            if (property.isConfObject()) {
                traverseNodeTypesafe(childNode, property, allExtensionClasses, filter);
                continue;
            }

            // collection, where a generics parameter is a configurable class or it is an array with comp type of configurableClass
            if (property.isCollectionOfConfObjects() || property.isArrayOfConfObjects()) {

                Collection collection = (Collection) childNode;

                for (Object object : collection) {
                    traverseNodeTypesafe(object, property.getPseudoPropertyForConfigClassCollectionElement(), allExtensionClasses, filter);
                }

                continue;
            }

            // map, where a value generics parameter is a configurable class
            if (property.isMapOfConfObjects()) {

                try {
                    Map<String, Object> collection = (Map<String, Object>) childNode;

                    for (Object object : collection.values())
                        traverseNodeTypesafe(object, property.getPseudoPropertyForConfigClassCollectionElement(), allExtensionClasses, filter);

                } catch (ClassCastException e) {
                    log.warn("Map is malformed", e);
                }

                continue;
            }

            // extensions map
            if (property.isExtensionsProperty()) {

                try {
                    Map<String, Object> extensionsMap = (Map<String, Object>) childNode;

                    for (Entry<String, Object> entry : extensionsMap.entrySet()) {
                        try {
                            traverseNodeTypesafe(entry.getValue(), new AnnotatedConfigurableProperty(Extensions.getExtensionClassBySimpleName(entry.getKey(), allExtensionClasses)), allExtensionClasses, filter);
                        } catch (ClassNotFoundException e) {
                            // noop
                            log.debug("Extension class {} not found, parent node class {} ", entry.getKey(), containerProperty.getRawClass().getName());
                        }
                    }

                } catch (ClassCastException e) {
                    log.warn("Extensions are malformed", e);
                }

            }


        }
    }


    public static void traverseMapNode(Object node, AConfigNodeFilter filter) {

        if (node instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) node;

            filter.beforeNode(map);
            for (Entry<String, Object> stringObjectEntry : map.entrySet()) {

                String key = stringObjectEntry.getKey();
                Object value = stringObjectEntry.getValue();

                filter.beforeNodeElement(map, key, value);

                if (Nodes.isPrimitive(value))
                    filter.onPrimitiveNodeElement(map, key, value);
                else if (value instanceof Map) traverseMapNode(value, filter);
                else if (value instanceof Collection) {
                    Collection collection = (Collection) value;

                    filter.beforeList(collection);
                    for (Object o : collection) {
                        filter.beforeListElement(collection, o);

                        if (Nodes.isPrimitive(o))
                            filter.onPrimitiveListElement(collection, o);
                        else if (o instanceof Map) traverseMapNode(o, filter);
                        else throw new IllegalArgumentException("List of lists is not allowed");

                        filter.afterListElement(collection, o);
                    }
                    filter.afterList(collection);
                }
                filter.afterNodeElement(map, key, value);
            }
            filter.afterNode(map);

        } else
            throw new IllegalArgumentException("A composite config node must be a Map<String,Object>");
    }


    /**
     * Traverses with in-depth search and applies dual filter.
     *
     * @param node1
     * @param node2
     */
    public static void dualTraverseMapNodes(Map<String, Object> node1, Map<String, Object> node2, ADualNodeFilter filter) {

        filter.beforeNode(node1, node2);

        if (node1 != null && node2 != null)
            for (Map.Entry<String, Object> objectEntry : node1.entrySet())
                if (node2.containsKey(objectEntry.getKey())) {

                    filter.beforeNodeProperty(objectEntry.getKey());

                    Object node1El = objectEntry.getValue();
                    Object node2El = node2.get(objectEntry.getKey());
                    dualTraverseProperty(node1El, node2El, filter);

                    filter.afterNodeProperty(objectEntry.getKey());
                }

        filter.afterNode(node1, node2);
    }

    private static void dualTraverseProperty(Object node1El, Object node2El, ADualNodeFilter filter) {
        if (node2El == null && node1El == null) return;

        if (node1El instanceof List) {

            if (!(node2El instanceof List)) return;

            List list1 = (List) node1El;
            List list2 = (List) node2El;

            // if any of elements don't have #uuid defined, don't go deeper into the collection
            // because there is no guarantee that elements will match

            boolean allUuids = allElementsAreNodesAndHaveUuids(list1)
                    && allElementsAreNodesAndHaveUuids(list2)
                    && filter.beforeList(list1, list2);

            if (allUuids) {

                // we are sure by now
                List<Map<String, Object>> nodeList1 = list1;
                List<Map<String, Object>> nodeList2 = list2;

                // match on #uuid

                // extract Map uuid -> index
                Map<String, Integer> index1 = new HashMap<String, Integer>();

                int i = 0;
                for (Map<String, Object> node : nodeList1) {
                    index1.put((String) node.get(Configuration.UUID_KEY), i);
                    i++;
                }

                i = 0;
                for (Map<String, Object> node : nodeList2) {

                    Object uuid = node.get(Configuration.UUID_KEY);
                    Integer elem1Ind = index1.get(uuid);

                    if (elem1Ind != null) {
                        // found match
                        filter.beforeListElement(elem1Ind, i);
                        dualTraverseMapNodes(nodeList1.get(elem1Ind), node, filter);
                        filter.afterListElement(elem1Ind, i);
                    }

                    i++;
                }

            }

            filter.afterList(list1, list2);
        }

        if (node1El instanceof Map) {

            if (!(node2El instanceof Map)) return;

            dualTraverseMapNodes((Map) node1El, (Map) node2El, filter);
        }

    }

    private static boolean allElementsAreNodesAndHaveUuids(List list) {
        for (Object o : list) {
            if (!(o instanceof Map)) return false;
            if (!(((Map) o).get(Configuration.UUID_KEY) instanceof String)) return false;
        }
        return true;
    }

}
