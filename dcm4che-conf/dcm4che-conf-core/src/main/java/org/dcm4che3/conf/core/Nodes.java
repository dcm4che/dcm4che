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
import org.dcm4che3.conf.core.util.XNodeUtil;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Nodes {


    private static Pattern itemPattern = Pattern.compile("/(?<item>(\\\\/|[^/])*)");
    private static Pattern simplePathPattern = Pattern.compile("(" + itemPattern + ")*");

    public static String concat(String path1, String path2) {
        String res = path1 + "/" + path2;
        return res.replace("///", "/").replace("//", "/");
    }

    public static void replaceNode(Object rootConfigNode, String path, Object replacementConfigNode) {

        JXPathContext jxPathContext = JXPathContext.newContext(rootConfigNode);
        jxPathContext.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
                if (parent instanceof Map) {
                    ((Map<String, Object>) parent).put(name, new TreeMap<String, Object>());
                    return true;
                }
                return false;
            }

            @Override
            public boolean declareVariable(JXPathContext context, String name) {
                return super.declareVariable(context, name);
            }
        });
        jxPathContext.createPathAndSetValue(path, replacementConfigNode);
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
     * @param node
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Object deepCloneNode(Object node) {


        if (isPrimitive(node)) return node;

        if (node instanceof Collection) {
            ArrayList<Object> newList = new ArrayList<Object>();
            for (Object o : (Collection) node) newList.add(deepCloneNode(o));
            return newList;
        }

        if (node instanceof Map) {
            Map newMap = new TreeMap();

            for (Entry e : (Set<Entry>) ((Map) node).entrySet()) {
                newMap.put(e.getKey(), deepCloneNode(e.getValue()));
            }

            return newMap;
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
    public static List<String> getPathItems(String path) {
        List<Map<String, Object>> refItems = XNodeUtil.parseReference(path);
        List<String> names = new ArrayList<String>();

        for (Map<String, Object> refItem : refItems) {
            names.add((String) refItem.get("$name"));
            if (refItem.containsKey("@name"))
                names.add((String) refItem.get("@name"));
        }

        return names;
    }

    public static String toSimpleEscapedPath(Iterator<String> items) {
        ArrayList<String> strings = new ArrayList<String>();
        while (items.hasNext())
            strings.add(items.next());
        return toSimpleEscapedPath(strings);
    }

    public static String toSimpleEscapedPath(Iterable<String> items) {
        String s = "";
        for (String item : items) s += "/" + item.replace("/", "\\/");
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

    public static Map<String, Object> replaceNode(Map<String, Object> map, Map replacement, List<String> pathItems) {
        Map<String, Object> m = map;
        Map<String, Object> subm = m;
        String name = null;

        if (pathItems.isEmpty())
            return replacement;

        for (String pathItem : pathItems) {
            name = pathItem;
            m = subm;
            subm = (Map<String, Object>) m.get(name);
            if (subm == null) {
                subm = new HashMap<String, Object>();
                m.put(name, subm);
            }
        }

        m.put(name, replacement);
        return map;
    }

    public static void removeNode(Map<String, Object> map, List<String> pathItems) {
        Map<String, Object> m = map;
        Map<String, Object> subm = m;
        String name = null;

        for (String pathItem : pathItems) {
            name = pathItem;
            m = subm;
            subm = (Map<String, Object>) m.get(name);
            if (subm == null) return;
        }

        // remove leaf
        m.remove(name);
    }

    public static boolean nodeExists(Map<String, Object> map, List<String> pathItems) {
        Map<String, Object> m = map;
        Map<String, Object> subm;

        for (String pathItem : pathItems) {

            subm = (Map<String, Object>) m.get(pathItem);
            if (subm == null) return false;
            m = subm;
        }
        return true;
    }
}
