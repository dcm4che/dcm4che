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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
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

import java.util.Collection;
import java.util.List;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapUtils {

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

    public static void storeConnRefs(Attributes attrs, Collection<Connection> conns,
            String deviceDN) {
        if (!conns.isEmpty())
            attrs.put(LdapUtils.connRefs(conns, deviceDN));
    }

    private static Attribute connRefs(Collection<Connection> conns,
            String deviceDN) {
        Attribute attr = new BasicAttribute("dicomNetworkConnectionReference");
        for (Connection conn : conns)
            attr.add(LdapUtils.dnOf(conn, deviceDN));
        return attr;
    }

    public static <T> void storeNotEmpty(Attributes attrs, String attrID, T... vals) {
        if (vals != null && vals.length > 0)
            attrs.put(LdapUtils.attr(attrID, vals));
    }

    public static <T> Attribute attr(String attrID, T... vals) {
        Attribute attr = new BasicAttribute(attrID);
        for (T val : vals)
            attr.add(val.toString());
        return attr;
    }

    public static void storeNotEmpty(Attributes attrs, String attrID, int... vals) {
        if (vals != null && vals.length > 0)
            attrs.put(LdapUtils.attr(attrID, vals));
    }

    public static Attribute attr(String attrID, int... vals) {
        Attribute attr = new BasicAttribute(attrID);
        for (int val : vals)
            attr.add("" + val);
        return attr;
    }

    public static void storeNotNull(Attributes attrs, String attrID, Object val) {
        if (val != null)
            attrs.put(attrID, LdapUtils.toString(val));
    }

    public static void storeNotDef(Attributes attrs, String attrID, int val, int defVal) {
        if (val != defVal)
            LdapUtils.storeInt(attrs, attrID, val);
    }

    public static void storeNotDef(Attributes attrs, String attrID, boolean val, boolean defVal) {
        if (val != defVal)
            LdapUtils.storeBoolean(attrs, attrID, val);
    }

    public static Attribute storeBoolean(Attributes attrs, String attrID, boolean val) {
        return attrs.put(attrID, LdapUtils.toString(val));
    }

    public static Attribute storeInt(Attributes attrs, String attrID, int val) {
        return attrs.put(attrID, "" + val);
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

    public static String dnOf(String attrID1, String attrValue1,
            String attrID2, String attrValue2, String baseDN) {
        return attrID1 + '=' + attrValue1
                + '+' + attrID2 + '=' + attrValue2
                + ','  + baseDN;
    }

    public static void storeDiff(List<ModificationItem> mods,
            String attrId, boolean prev, boolean val, boolean defVal) {
        if (val != prev)
            mods.add((val == defVal)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            new BasicAttribute(attrId, LdapUtils.toString(val))));
    }

    public static void storeDiff(List<ModificationItem> mods,
            String attrId, int prev, int val, int defVal) {
        if (val != prev)
            mods.add((val == defVal)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            new BasicAttribute(attrId, "" + val)));
    }

    public static void storeDiff(List<ModificationItem> mods, String attrId,
            Object prev, Object val) {
        if (val == null) {
            if (prev != null)
                mods.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                        new BasicAttribute(attrId)));
        } else if (!val.equals(prev))
            mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    new BasicAttribute(attrId, LdapUtils.toString(val))));
    }

    public static <T> void storeDiff(List<ModificationItem> mods, String attrId,
            T[] prevs, T[] vals) {
        if (!LdapUtils.equals(prevs, vals))
            mods.add((vals != null && vals.length == 0)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            attr(attrId, vals)));
    }

    public static void storeDiff(List<ModificationItem> mods, String attrId,
            List<Connection> prevs, List<Connection> conns, String deviceDN) {
        if (!LdapUtils.equalsConnRefs(prevs, conns, deviceDN))
            mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    connRefs(conns, deviceDN)));
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
        return attr != null ? (String) attr.get() : defVal;
    }

    public static String[] stringArray(Attribute attr) throws NamingException {
        if (attr == null)
            return StringUtils.EMPTY_STRING;
    
        String[] ss = new String[attr.size()];
        for (int i = 0; i < ss.length; i++)
            ss[i] = (String) attr.get(i);
    
        return ss;
    }

    public static int intValue(Attribute attr, int defVal) throws NamingException {
        return attr != null ? Integer.parseInt((String) attr.get()) : defVal;
    }

    public static int[] intArray(Attribute attr) throws NamingException {
        if (attr == null)
            return ByteUtils.EMPTY_INTS;
    
        int[] a = new int[attr.size()];
        for (int i = 0; i < a.length; i++)
            a[i] = Integer.parseInt((String) attr.get(i));
    
        return a;
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
        return (o instanceof Boolean)
                ? toString(((Boolean) o).booleanValue())
                : o != null ? o.toString() : null;
    }

    public static Attributes attrs(String objectclass, String attrID, String attrVal) {
        Attributes attrs = new BasicAttributes(true); // case-ignore
        attrs.put("objectclass", objectclass);
        storeNotNull(attrs, attrID, attrVal);
        return attrs;
    }

}
