/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4che3.conf.ldap;

import org.dcm4che3.conf.api.ConfigurationChanges;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.DatePrecision;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.DateUtils;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapUtils {

    private static final Code[] EMPTY_CODES = {};

    @SuppressWarnings("unchecked")
    public static boolean hasObjectClass(Attributes attrs, String objectClass)
           throws NamingException {
       NamingEnumeration<String> ne =
           (NamingEnumeration<String>) attrs.get("objectclass").getAll();
       try {
           while (ne.hasMore())
               if (objectClass.equals(ne.next()))
                   return true;
       } finally {
           LdapUtils.safeClose(ne);
       }
       return false;
    }

    public static <T> void safeClose(NamingEnumeration<T> enumeration) {
        if (enumeration != null)
            try {
                enumeration.close();
            } catch (NamingException e) {
            }
    }

    public static void storeConnRefs(ConfigurationChanges.ModifiedObject ldapObj, Attributes attrs, Collection<Connection> conns,
                                     String deviceDN) {
        if (!conns.isEmpty()) {
            attrs.put(LdapUtils.connRefs(conns, deviceDN));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute("dicomNetworkConnectionReference");
                for (Connection conn : conns)
                    attribute.addValue(LdapUtils.dnOf(conn, deviceDN));
                ldapObj.add(attribute);
            }
        }
    }

    private static Attribute connRefs(Collection<Connection> conns,
            String deviceDN) {
        Attribute attr = new BasicAttribute("dicomNetworkConnectionReference");
        for (Connection conn : conns)
            attr.add(LdapUtils.dnOf(conn, deviceDN));
        return attr;
    }

    public static <T> void storeNotEmpty(ConfigurationChanges.ModifiedObject ldapObj, Attributes attrs, String attrID, T[] vals, T... defVals) {
        if (vals.length > 0 && !LdapUtils.equals(vals, defVals)) {
            attrs.put(LdapUtils.attr(attrID, vals));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                for (T val : vals)
                    attribute.addValue(val);
                ldapObj.add(attribute);
            }
        }
    }

    public static <T> void storeNotEmpty(ConfigurationChanges.ModifiedObject ldapObj, Attributes attrs, String attrID,
            Map<String, T> map) {
        storeNotEmpty(ldapObj, attrs, attrID, toStrings(map));
    }

    public static <T> String[] toStrings(Map<String, T> map) {
        String[] ss = new String[map.size()];
        int i = 0;
        for (Map.Entry<String, T> entry : map.entrySet())
            ss[i++] = entry.getKey() + '=' + entry.getValue();
        return ss;
    }

    public static <T> void storeNotEmpty(Attributes attrs, String attrID, T[] vals, T... defVals) {
        if (vals.length > 0 && !LdapUtils.equals(vals, defVals))
            attrs.put(LdapUtils.attr(attrID, vals));
    }

    public static <T> Attribute attr(String attrID, Map<String, T> map) {
        return attr(attrID, toStrings(map));
    }

    public static <T> Attribute attr(String attrID, T... vals) {
        Attribute attr = new BasicAttribute(attrID);
        for (T val : vals)
            attr.add(val.toString());
        return attr;
    }

    public static void storeNotEmpty(ConfigurationChanges.ModifiedObject ldapObj, Attributes attrs, String attrID, int... vals) {
        if (vals != null && vals.length > 0) {
            attrs.put(LdapUtils.attr(attrID, vals));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                for (int val : vals)
                    attribute.addValue(val);
                ldapObj.add(attribute);
            }
        }
    }

    public static void storeNotEmpty(Attributes attrs, String attrID, int... vals) {
        if (vals != null && vals.length > 0)
            attrs.put(LdapUtils.attr(attrID, vals));
    }

    public static Attribute attr(String attrID, int... vals) {
        Attribute attr = new BasicAttribute(attrID);
        for (int val : vals)
            attr.add(Integer.toString(val));
        return attr;
    }

    public static <T> void storeNotNullOrDef(ConfigurationChanges.ModifiedObject ldapObj, Attributes attrs, String attrID, T val, T defVal) {
        if (val != null && !val.equals(defVal)) {
            attrs.put(attrID, LdapUtils.toString(val));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                attribute.addValue(val);
                ldapObj.add(attribute);
            }
        }
    }

    public static <T> void storeNotNullOrDef(Attributes attrs, String attrID, T val, T defVal) {
        if (val != null && !val.equals(defVal))
            attrs.put(attrID, LdapUtils.toString(val));
    }

    public static void storeNotNull(ConfigurationChanges.ModifiedObject ldapObj, Attributes attrs, String attrID, Integer val) {
        if (val != null) {
            LdapUtils.storeInt(attrs, attrID, val);
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                attribute.addValue(val);
                ldapObj.add(attribute);
            }
        }
    }

    public static void storeNotDef(ConfigurationChanges.ModifiedObject ldapObj, Attributes attrs, String attrID, int val, int defVal) {
        if (val != defVal) {
            LdapUtils.storeInt(attrs, attrID, val);
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                attribute.addValue(val);
                ldapObj.add(attribute);
            }
        }
    }

    public static void storeNotDef(Attributes attrs, String attrID, int val, int defVal) {
        if (val != defVal)
            LdapUtils.storeInt(attrs, attrID, val);
    }

    public static void storeNotDef(
            ConfigurationChanges.ModifiedObject ldapObj, Attributes attrs, String attrID, long val, long defVal) {
        if (val != defVal) {
            LdapUtils.storeLong(attrs, attrID, val);
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                attribute.addValue(val);
                ldapObj.add(attribute);
            }
        }
    }

    public static void storeNotDef(ConfigurationChanges.ModifiedObject ldapObj, Attributes attrs, String attrID, boolean val, boolean defVal) {
        if (val != defVal) {
            LdapUtils.storeBoolean(attrs, attrID, val);
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                attribute.addValue(val);
                ldapObj.add(attribute);
            }
        }
    }

    public static Attribute storeBoolean(Attributes attrs, String attrID, boolean val) {
        return attrs.put(attrID, LdapUtils.toString(val));
    }

    public static Attribute storeBoolean(ConfigurationChanges.ModifiedObject ldapObj, Attributes attrs, String attrID, boolean val) {
        if (ldapObj != null) {
            ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
            attribute.addValue(val);
            ldapObj.add(attribute);
        }
        return attrs.put(attrID, LdapUtils.toString(val));
    }

    public static Attribute storeInt(ConfigurationChanges.ModifiedObject ldapObj, Attributes attrs, String attrID, int val) {
        if (ldapObj != null) {
            ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
            attribute.addValue(val);
            ldapObj.add(attribute);
        }
        return attrs.put(attrID, Integer.toString(val));
    }

    public static Attribute storeInt(Attributes attrs, String attrID, int val) {
        return attrs.put(attrID, Integer.toString(val));
    }

    public static Attribute storeLong(Attributes attrs, String attrID, long val) {
        return attrs.put(attrID, Long.toString(val));
    }

    public static String dnOf(Connection conn, String deviceDN) {
        String cn = conn.getCommonName();
        return (cn != null)
            ? LdapUtils.dnOf("cn", cn , deviceDN)
            : (conn.isServer()
                    ? LdapUtils.dnOf("dicomHostname", conn.getHostname(),
                           "dicomPort", Integer.toString(conn.getPort()),
                            deviceDN)
                    : LdapUtils.dnOf("dicomHostname", conn.getHostname(), deviceDN));
    }

    public static String dnOf(String attrID, String attrValue, String parentDN) {
        return attrID + '=' + attrValue + ',' + parentDN;
    }

    public static String cutAttrValueFromDN(String dn, String attrID) {
        int beginIndex = dn.indexOf(attrID + '=');
        if (beginIndex < 0)
            return null;

        beginIndex += attrID.length() + 1;
        int endIndex = dn.indexOf(',', beginIndex);
        return endIndex >= 0 ? dn.substring(beginIndex, endIndex) : dn.substring(beginIndex);
    }

    public static String dnOf(String attrID1, String attrValue1,
            String attrID2, String attrValue2, String baseDN) {
        return attrID1 + '=' + attrValue1
                + '+' + attrID2 + '=' + attrValue2
                + ','  + baseDN;
    }

    public static void storeDiff(ConfigurationChanges.ModifiedObject ldapObj, List<ModificationItem> mods,
                                 String attrId, boolean prev, boolean val, boolean defVal) {
        if (val != prev) {
            mods.add((val == defVal)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            new BasicAttribute(attrId, LdapUtils.toString(val))));
            if (ldapObj != null)
                ldapObj.add(new ConfigurationChanges.ModifiedAttribute(attrId, prev, val));
        }
    }

    public static void storeDiff(ConfigurationChanges.ModifiedObject ldapObj, List<ModificationItem> mods,
                                 String attrId, int prev, int val, int defVal) {
        if (val != prev) {
            mods.add((val == defVal)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            new BasicAttribute(attrId, Integer.toString(val))));
            if (ldapObj != null)
                ldapObj.add(new ConfigurationChanges.ModifiedAttribute(attrId, prev, val));
        }
    }

    public static void storeDiff(ConfigurationChanges.ModifiedObject ldapObj, List<ModificationItem> mods,
                                 String attrId, long prev, long val, long defVal) {
        if (val != prev) {
            mods.add((val == defVal)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                    new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    new BasicAttribute(attrId, Long.toString(val))));
            if (ldapObj != null)
                ldapObj.add(new ConfigurationChanges.ModifiedAttribute(attrId, prev, val));
        }
    }

    public static <T> void storeDiffObject(ConfigurationChanges.ModifiedObject ldapObj, List<ModificationItem> mods,
                                           String attrId, T prev, T val, T defVal) {
        if (val == null || val.equals(defVal)) {
            if (prev != null && !prev.equals(defVal)) {
                mods.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                        new BasicAttribute(attrId)));
                if (ldapObj != null)
                    ldapObj.add(new ConfigurationChanges.ModifiedAttribute(attrId,
                            LdapUtils.toString(prev), LdapUtils.toString(val)));
            }
        } else if (!val.equals(prev)) {
            mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    new BasicAttribute(attrId, LdapUtils.toString(val))));
            if (ldapObj != null)
                ldapObj.add(new ConfigurationChanges.ModifiedAttribute(attrId,
                        LdapUtils.toString(prev), LdapUtils.toString(val)));
        }
    }

    public static <T> void storeDiffProperties(ConfigurationChanges.ModifiedObject ldapObj, List<ModificationItem> mods,
            String attrID, Map<String, T> prevs, Map<String, T> props) {
        if (!equalsProperties(prevs, props)) {
            mods.add(props.size() == 0
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(attrID))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE, LdapUtils.attr(attrID, props)));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                for (String val : LdapUtils.toStrings(props))
                    attribute.addValue(val);
                for (String prev : LdapUtils.toStrings(prevs))
                    attribute.removeValue(prev);
                ldapObj.add(attribute);
            }
        }
    }

    private static <T> boolean equalsProperties(Map<String, T> prevs, Map<String, T> props) {
        if (prevs == props)
            return true;

        if (prevs.size() != props.size())
            return false;

        for (Map.Entry<String, T> prop : props.entrySet()) {
            Object value = prop.getValue();
            Object prevValue = prevs.get(prop.getKey());
            if (!(value == null
                    ? prevValue == null && prevs.containsKey(prop.getKey())
                    : prevValue != null && prevValue.toString().equals(value.toString())))
                return false;
        }
        return true;
    }

    public static <T> void storeDiff(ConfigurationChanges.ModifiedObject ldapObj, List<ModificationItem> mods,
                                     String attrId, int[] prevs, int[] vals, int... defVals) {
        storeDiff(ldapObj, mods, attrId,
                Arrays.stream(prevs).boxed().toArray(Integer[]::new),
                Arrays.stream(vals).boxed().toArray(Integer[]::new),
                Arrays.stream(defVals).boxed().toArray(Integer[]::new));
    }

    public static <T> void storeDiff(ConfigurationChanges.ModifiedObject ldapObj, List<ModificationItem> mods,
                                     String attrId, T[] prevs, T[] vals, T... defVals) {
        if (!LdapUtils.equals(prevs, vals)) {
            mods.add((vals.length == 0 || LdapUtils.equals(defVals, vals))
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            attr(attrId, vals)));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrId);
                for (T val : vals)
                    attribute.addValue(val);
                for (T prev : prevs)
                    attribute.removeValue(prev);
                ldapObj.add(attribute);
            }
        }
    }

    public static void storeDiffWithOrdinalPrefix(ConfigurationChanges.ModifiedObject ldapObj,
                                                  List<ModificationItem> mods,
                                                  String attrId, String[] prevs, String[] vals) {
        if (!Arrays.equals(prevs, vals)) {
            String[] valsWithOrdinalPrefix = addOrdinalPrefix(vals);
            mods.add((vals.length == 0)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            attr(attrId, valsWithOrdinalPrefix)));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrId);
                for (String val : valsWithOrdinalPrefix)
                    attribute.addValue(val);
                for (String prev : addOrdinalPrefix(prevs))
                    attribute.removeValue(prev);
                ldapObj.add(attribute);
            }
        }
    }

    public static void storeDiff(ConfigurationChanges.ModifiedObject ldapObj, List<ModificationItem> mods,
                                 String attrId, List<Connection> prevs, List<Connection> conns, String deviceDN) {
        if (!LdapUtils.equalsConnRefs(prevs, conns, deviceDN)) {
            mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    connRefs(conns, deviceDN)));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrId);
                for (Connection conn : conns)
                    attribute.addValue(LdapUtils.dnOf(conn, deviceDN));
                for (Connection conn  : prevs)
                    attribute.removeValue(LdapUtils.dnOf(conn, deviceDN));
                ldapObj.add(attribute);
            }
        }
    }

    private static boolean equalsConnRefs(List<Connection> conns1,
           List<Connection> conns2, String deviceDN) {
        if (conns1.size() != conns2.size())
            return false;
        for (Connection conn1 : conns1)
            if (LdapUtils.findByDN(deviceDN, conns2, dnOf(conn1, deviceDN)) == null)
                return false;
        return true;
    }

    public static <T> boolean equals(T[] a, T[] a2) {
        int length = a.length;
        if (a2.length != length)
            return false;
    
        outer:
        for (Object o1 : a) {
            for (Object o2 : a2)
                if (o1.equals(o2))
                    continue outer;
            return false;
        }
        return true;
    }

    public static Connection findByDN(String deviceDN, 
            List<Connection> conns, String dn) {
        for (Connection conn : conns)
            if (dn.equals(dnOf(conn, deviceDN)))
                return conn;
        return null;
    }

    public static Boolean booleanValue(Attribute attr, Boolean defVal)
            throws NamingException {
        return attr != null ? Boolean.valueOf((String) attr.get()) : defVal;
    }

    public static boolean booleanValue(Attribute attr, boolean defVal)
            throws NamingException {
        return attr != null ? Boolean.parseBoolean((String) attr.get()) : defVal;
    }

    public static String stringValue(Attribute attr, String defVal) throws NamingException {
        return attr != null ? stringValue(attr.get()) : defVal;
    }

    private static String stringValue(Object o) {
        return o instanceof byte[] ? new String((byte[]) o, StandardCharsets.UTF_8) : (String) o;
    }

    public static TimeZone timeZoneValue(Attribute attr, TimeZone defVal) throws NamingException {
        return attr != null ? TimeZone.getTimeZone((String) attr.get()) : defVal;
    }

    public static Date dateTimeValue(Attribute attr) throws NamingException {
        return attr != null ? DateUtils.parseDT(null, (String) attr.get(), new DatePrecision()) : null;
    }

    public static <T extends Enum<T>> T enumValue(Class<T> enumType, Attribute attr, T defVal) throws NamingException {
        return attr != null ? Enum.valueOf(enumType, (String) attr.get()) : defVal;
    }

    public static <T extends Enum<T>> T[] enumArray(Class<T> enumType, Attribute attr) throws NamingException {
        T[] a = (T[]) Array.newInstance(enumType, attr != null ? attr.size() : 0);
        for (int i = 0; i < a.length; i++)
            a[i] = Enum.valueOf(enumType, (String) attr.get(i));

        return a;
    }

    public static String[] stringArray(Attribute attr, String... defVals) throws NamingException {
        if (attr == null)
            return defVals;
    
        String[] ss = new String[attr.size()];
        for (int i = 0; i < ss.length; i++)
            ss[i] = (String) attr.get(i);
    
        return ss;
    }

    public static long longValue(Attribute attr, long defVal) throws NamingException {
        return attr != null ? Long.parseLong((String) attr.get()) : defVal;
    }

    public static int intValue(Attribute attr, int defVal) throws NamingException {
        return attr != null ? Integer.parseInt((String) attr.get()) : defVal;
    }

    public static Long longValue(Attribute attr, Long defVal) throws NamingException {
        return attr != null ? Long.valueOf((String) attr.get()) : defVal;
    }

    public static Integer intValue(Attribute attr, Integer defVal) throws NamingException {
        return attr != null ? Integer.valueOf((String) attr.get()) : defVal;
    }

    public static Code codeValue(Attribute attr) throws NamingException {
        return attr != null ? new Code((String) attr.get()) : null;
    }

    public static Issuer issuerValue(Attribute attr) throws NamingException {
        return attr != null ? new Issuer((String) attr.get()) : null;
    }

    public static int[] intArray(Attribute attr) throws NamingException {
        if (attr == null)
            return ByteUtils.EMPTY_INTS;
    
        int[] a = new int[attr.size()];
        for (int i = 0; i < a.length; i++)
            a[i] = Integer.parseInt((String) attr.get(i));
    
        return a;
    }

    public static Code[] codeArray(Attribute attr) throws NamingException {
        if (attr == null)
            return EMPTY_CODES;

        Code[] codes = new Code[attr.size()];
        for (int i = 0; i < codes.length; i++)
            codes[i] = new Code((String) attr.get(i));

        return codes;
    }


    public static Connection findConnection(String connDN, String deviceDN, Device device)
            throws NameNotFoundException {
        for (Connection conn : device.listConnections())
            if (dnOf(conn, deviceDN).equalsIgnoreCase(connDN))
                return conn;
    
        throw new NameNotFoundException(connDN);
    }

    public static String toString(boolean val) {
        return val ? "TRUE" : "FALSE";
    }

    public static String toString(Object o) {
        return (o instanceof Boolean) ? toString(((Boolean) o).booleanValue())
                : (o instanceof TimeZone) ? ((TimeZone) o).getID()
                : (o instanceof Date) ? DateUtils.formatDT(null, (Date) o)
                : o != null ? o.toString() : null;
    }

    public static Attributes attrs(String objectclass, String attrID, String attrVal) {
        Attributes attrs = new BasicAttributes(true); // case-ignore
        attrs.put("objectclass", objectclass);
        storeNotNullOrDef(attrs, attrID, attrVal, null);
        return attrs;
    }

    public static String[] addOrdinalPrefix(String[] vals) {
        String[] result = new String[vals.length];
        for (int i = 0; i < result.length; i++) {
            String val = vals[i];
            int vallen = val.length();
            char[] cs = new char[3 + vallen];
            cs[0] = '{';
            cs[1] = DIGITS[i];
            cs[2] = '}';
            val.getChars(0, vallen, cs, 3);
            result[i] = new String(cs);
        }
        return result;
    }

    public static String[] removeOrdinalPrefix(String[] vals) {
        Arrays.sort(vals);
        String[] result = new String[vals.length];
        for (int i = 0; i < result.length; i++)
            result[i] = vals[i].substring(3);
        return result;
    }

    private final static char[] DIGITS = {
            '0' , '1' , '2' , '3' , '4' , '5' ,
            '6' , '7' , '8' , '9' , 'A' , 'B' ,
            'C' , 'D' , 'E' , 'F' , 'G' , 'H' ,
            'I' , 'J' , 'K' , 'L' , 'M' , 'N' ,
            'O' , 'P' , 'Q' , 'R' , 'S' , 'T' ,
            'U' , 'V' , 'W' , 'X' , 'Y' , 'Z'
    };

    public static String cutDeviceName(String name) {
        int start = name.indexOf("dicomDeviceName=");
        if (start < 0)
            return null;

        start += 16;
        int end = name.indexOf(',', start);
        return end < 0 ? name.substring(start) : name.substring(start, end);
    }
}
