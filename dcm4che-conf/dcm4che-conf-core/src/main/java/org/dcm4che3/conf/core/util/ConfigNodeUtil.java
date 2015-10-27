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

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.apache.commons.jxpath.Pointer;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigNodeUtil {


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

    public static boolean validatePath(String path) {
        return true;
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

//        // clone
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            return objectMapper.treeToValue(objectMapper.valueToTree(node), node.getClass());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    public static boolean isPrimitive(Object value) {
        return value == null ||
                value instanceof Number ||
                value instanceof String ||
                value instanceof Boolean;
    }

    public String[] split(String path) {
        // TODO: support slash as a symbol with escaping?
        return path.split("/");
    }

    public static String escape(String str) {
        // TODO: implement validation, as we do not allow slashes, commas quotes etc
        // TODO: implement escaping
        return str;
    }

    private final static String IDENTIFIER = "@?[a-zA-Z\\d_]+";
    private static final String VALUE = "(('.+?')|(\\-?[\\d]+)|true|false)";
    private final static String IDENTIFIER_NAMED = "(?<identifier>" + IDENTIFIER + ")";
    private static final String VALUE_NAMED = "(('(?<strvalue>.+?)')|(?<intvalue>\\-?[\\d]+)|(?<boolvalue>true|false))";
    private static final String AND = " and ";
    private static final String APOS = "&apos;";


    private final static String XPREDICATE = "(" + IDENTIFIER + "=" + VALUE + ")";
    private final static String XPREDICATENAMED = "(" + IDENTIFIER_NAMED + "=" + VALUE_NAMED + ")";

    private final static String XPATHNODE = "/(?<nodename>" + IDENTIFIER + "|\\*)" + "(\\[(?<predicates>" + XPREDICATE + "( and " + XPREDICATE + ")*)\\])?";
    private final static String XPATH = "(" + XPATHNODE + ")*";

    public final static Pattern xPathPattern = Pattern.compile(XPATH);
    public final static Pattern xPathNodePattern = Pattern.compile(XPATHNODE);
    private final static Pattern xPredicatePattern = Pattern.compile(XPREDICATE);
    private final static Pattern xNamedPredicatePattern = Pattern.compile(XPREDICATENAMED);
    private final static Pattern xAndPattern = Pattern.compile(AND);
    private final static Pattern aposPattern = Pattern.compile(APOS);

    /**
     * Returns list of path elements.
     * $name - name of the node (not @name, which is a predicate)
     * other entries are just key - value
     *
     * @param s
     * @return
     */
    public static List<Map<String, Object>> parseReference(String s) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        //special case
        if (s.equals("/")) return list;

        if (!xPathPattern.matcher(s).matches()) {
            throw new IllegalArgumentException("Failed to parse provided reference (" + s + ")");
        }


        Matcher nodeMatcher = xPathNodePattern.matcher(s);
        while (nodeMatcher.find()) {

            Map<String, Object> propMap = new HashMap<String, Object>();
            list.add(propMap);

            String node = nodeMatcher.group();

            // nodename $name
            String nodeName = nodeMatcher.group("nodename");
            propMap.put("$name", nodeName);

            // now key-value
            String predicatesStr = nodeMatcher.group("predicates");
            if (predicatesStr != null) {
                String[] predicates = xAndPattern.split(predicatesStr);

                for (String p : predicates) {
                    Matcher matcher = xNamedPredicatePattern.matcher(p);
                    if (!matcher.find()) throw new RuntimeException("Unexpected error");


                    String boolvalue = matcher.group("boolvalue");
                    String intvalue = matcher.group("intvalue");
                    String strvalue = matcher.group("strvalue");

                    Object value;
                    if (boolvalue != null)
                        value = Boolean.parseBoolean(boolvalue);
                    else if (intvalue != null)
                        value = Integer.parseInt(intvalue);
                    else if (strvalue != null)
                        value = strvalue.replace(APOS, "'");
                    else throw new RuntimeException("Unexpected error: no value");


                    String identifier = matcher.group("identifier");
                    propMap.put(identifier, value);

                }

            }
        }

        return list;
    }

    public static String escapeApos(String name) {
        return name.replace("'", "&apos;");
    }

    public static String unescapeApos(String value) {
        return value.replace("&apos;", "'");
    }
}
