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
package org.dcm4che3.conf.core;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.apache.commons.jxpath.Pointer;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.util.XNodeUtil;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Nodes {


    private static Pattern itemPattern = Pattern.compile("/(?<item>(\\\\/|[^/\\[\\]@\\*])*)");
    private static Pattern simplePathPattern = Pattern.compile("(" + itemPattern + ")*");

    public static String concat(String path1, String path2) {
        String res = path1 + "/" + path2;
        return res.replace("///", "/").replace("//", "/");
    }

    public static Object getNode(Object rootConfigNode, String path) {
        try {
            return JXPathContext.newContext(rootConfigNode).getValue(path);
        } catch (JXPathNotFoundException e) {
            return null;
        }
    }

    public static boolean nodeExists(Map<String, Object> rootConfigNode, String path) {
        return getNode(rootConfigNode, path) != null;
    }

    public static void removeNodes(Map<String, Object> configurationRoot, String path) {
        JXPathContext.newContext(configurationRoot).removeAll(path);
    }

    public static Iterator search(Map<String, Object> configurationRoot, String liteXPathExpression) throws IllegalArgumentException {
        return JXPathContext.newContext(configurationRoot).iterate(liteXPathExpression);

    }

    /**
     * Clones structure but re-uses primitives
     *
     * @param node
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Object deepCloneNode(Object node) {


        if (isPrimitive(node)) return node;

        if (node instanceof Collection) {
            Collection givenCollection = (Collection) node;
            ArrayList<Object> newCollection = new ArrayList<Object>(givenCollection.size());
            for (Object o : givenCollection) newCollection.add(deepCloneNode(o));
            return newCollection;
        }

        if (node instanceof Map) {
            Map givenMapNode = (Map) node;
            Map newMapNode = new HashMap(givenMapNode.size());
            for (Entry e : (Set<Entry>) givenMapNode.entrySet()) {
                newMapNode.put(e.getKey(), deepCloneNode(e.getValue()));
            }

            return newMapNode;
        }

        throw new IllegalArgumentException("Unexpected node type " + node.getClass());
    }

    public static boolean isPrimitive(Object value) {
        return value == null ||
                value instanceof Number ||
                value instanceof String ||
                value instanceof Boolean;
    }


    /**
     * traverses nodenames and also @name predicates, so something like this
     * /dicomConfigurationRoot/dicomDevicesRoot[@name='deviceName']/deviceExtensions
     * will return
     * dicomConfigurationRoot,dicomDevicesRoot,deviceName,deviceExtensions
     *
     * @param path
     * @return
     */
    public static List<Object> getPathItems(String path) {
        List<Map<String, Object>> refItems = XNodeUtil.parseReference(path);
        List<Object> names = new ArrayList<Object>();

        for (Map<String, Object> refItem : refItems) {
            names.add((String) refItem.get("$name"));
            if (refItem.containsKey("@name"))
                names.add((String) refItem.get("@name"));
        }

        return names;
    }

    /**
     * @param path path str to parse
     * @return list of path items in case the provided path
     * <ul>
     * <li>is simple (e.g. "/dicomConfigurationRoot/globalConfiguration/dcmTransferCapabilities" )</li>
     * <li>is persistable (e.g. "/dicomConfigurationRoot/dicomDevicesRoot[@name='someName']")</li>
     * </ul>
     * <p/>
     * otherwise <b>null</b>
     */
    public static List<Object> simpleOrPersistablePathToPathItemsOrNull(String path) {

        List<Map<String, Object>> refItems;
        try {
            refItems = XNodeUtil.parseReference(path);
        } catch (IllegalArgumentException e) {
            return null;
        }

        List<Object> pathItems = new ArrayList<Object>();

        for (Map<String, Object> refItem : refItems) {

            String name = null;
            String attrName = null;

            for (Entry<String, Object> stringObjectEntry : refItem.entrySet()) {

                if (stringObjectEntry.getKey().equals("$name"))
                    name = ((String) stringObjectEntry.getValue());
                else if (stringObjectEntry.getKey().equals("@name"))
                    attrName = ((String) stringObjectEntry.getValue());
                else {
                    // this path is neither simple nor persistable
                    return null;
                }

                if (((String) stringObjectEntry.getValue()).contains("*"))
                    return null;
            }

            // this path is neither simple nor persistable
            if (name == null)
                return null;

            pathItems.add(name);

            if (attrName != null)
                pathItems.add(attrName);

        }

        return pathItems;
    }

    public static String toSimpleEscapedPath(Iterator<Object> items) {
        ArrayList<Object> strings = new ArrayList<Object>();
        while (items.hasNext())
            strings.add(items.next());
        return toSimpleEscapedPath(strings);
    }

    public static String toSimpleEscapedPath(Iterable<Object> items) {
        String s = "";
        for (Object item : items) s += "/" + item.toString().replace("/", "\\/");
        return s;
    }

    public static List<String> fromSimpleEscapedPath(String path) {
        List<String> strings = fromSimpleEscapedPathOrNull(path);
        if (strings == null)
            throw new IllegalArgumentException("Simple path " + path + " is invalid");
        return strings;
    }

    public static List<String> fromSimpleEscapedPathOrNull(String path) {

        if (!simplePathPattern.matcher(path).matches())
            return null;

        Matcher matcher = itemPattern.matcher(path);

        List<String> list = new ArrayList<String>();
        while (matcher.find()) {
            list.add(matcher.group("item").replace("\\/", "/"));
        }

        return list;
    }

    public static void replacePrimitive(Map<String, Object> map, Object replacement, List<Object> pathItems) {

        if (pathItems.isEmpty())
            throw new IllegalArgumentException("Cannot replace root with a primitive");

        replaceObject(map, replacement, pathItems);

    }

    public static Map<String, Object> replaceNode(Map<String, Object> map, Map<String, Object> replacement, List<Object> pathItems) {
        return (Map<String, Object>) replaceObject(map, replacement, pathItems);
    }

    private static Object replaceObject(Map<String, Object> map, Object replacement, List<Object> pathItems) {
        Object node = map;
        Object subNode = node;
        Object name = null;

        if (pathItems.isEmpty())
            return replacement;

        for (Object pathItem : pathItems) {
            name = pathItem;

            node = subNode;
            subNode = getElement(node, pathItem);

            if (subNode == null) {

                if (!(name instanceof String))
                    throw new IllegalStateException("Cannot create lists items with replace, path " + pathItems + " , item '" + name + "' , node " + node);

                subNode = Configuration.NodeFactory.emptyNode();
                ((Map)node).put(name, subNode);
            }
        }

        ((Map)node).put(name, replacement);
        return map;
    }


    public static void removeNode(Map<String, Object> map, List<Object> pathItems) {
        Object node = map;
        Object subNode = node;
        Object name = null;

        for (Object pathItem : pathItems) {

            name = pathItem;

            node = subNode;
            subNode = getElement(node, pathItem);

            if (subNode == null) return;
        }

        // remove leaf
        if (node instanceof List) {
            ((List) node).remove(name);
        } else if (node instanceof Map) {
            ((Map) node).remove(name);
        }
    }

    private static Object getElement(Object node, Object pathItem) {
        Object subNode;
        if (node instanceof List && pathItem instanceof Integer) {
            subNode = ((List) node).get((Integer) pathItem);
        } else if (node instanceof Map && pathItem instanceof String) {
            subNode = ((Map) node).get(pathItem);
        } else
            throw new IllegalArgumentException("Unexpected node/path: node " + node + " , path item " + pathItem);
        return subNode;
    }

    public static boolean nodeExists(Object node, List<Object> pathItems) {
        return getNode(node, pathItems) != null;
    }

    public static Object getNode(Object node, List<Object> pathItems) {
        for (Object pathItem : pathItems) {
            if (node == null) {
                return null;
            } else {
                node = getElement(node, pathItem);
            }
        }

        return node;
    }


}
