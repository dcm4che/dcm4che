package org.dcm4che3.conf.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Refrain from using, will most likely be removed in near future
 */
@Deprecated
public class XNodeUtil {
    private final static String IDENTIFIER = "@?[a-zA-Z\\d_]+";
    private final static String IDENTIFIER_NAMED = "(?<identifier>" + IDENTIFIER + ")";
    private static final String VALUE = "(('.+?')|(\\-?[\\d]+)|true|false)";
    private final static String XPREDICATE = "(" + IDENTIFIER + "=" + VALUE + ")";
    private final static Pattern xPredicatePattern = Pattern.compile(XPREDICATE);
    private final static String XPATHNODE = "/(?<nodename>" + IDENTIFIER + "|\\*)" + "(\\[(?<predicates>" + XPREDICATE + "( and " + XPREDICATE + ")*)\\])?";
    public final static Pattern xPathNodePattern = Pattern.compile(XPATHNODE);
    private final static String XPATH = "(" + XPATHNODE + ")*";
    public final static Pattern xPathPattern = Pattern.compile(XPATH);
    private static final String VALUE_NAMED = "(('(?<strvalue>.+?)')|(?<intvalue>\\-?[\\d]+)|(?<boolvalue>true|false))";
    private final static String XPREDICATENAMED = "(" + IDENTIFIER_NAMED + "=" + VALUE_NAMED + ")";
    private final static Pattern xNamedPredicatePattern = Pattern.compile(XPREDICATENAMED);
    private static final String AND = " and ";
    private final static Pattern xAndPattern = Pattern.compile(AND);
    private static final String APOS = "&apos;";
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
}
