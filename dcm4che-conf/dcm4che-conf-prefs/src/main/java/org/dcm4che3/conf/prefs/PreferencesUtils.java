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
package org.dcm4che3.conf.prefs;

import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.net.Connection;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class PreferencesUtils {

    public static void removeKeys(Preferences prefs, String key, int from, int to) {
        for (int i = from; i < to;) 
            prefs.remove(key + '.' + (++i));
        if (from == 0)
            prefs.remove(key + ".#");
    }

    public static void storeDiff(Preferences prefs, String key, int prev, int val, int defVal) {
        if (prev != val)
            if (val == defVal)
                prefs.remove(key);
            else
                prefs.putInt(key, val);
     }

    public static void storeDiff(Preferences prefs, String key,
            boolean prev, boolean val, boolean defVal) {
        if (prev != val)
            if (val == defVal)
                prefs.remove(key);
            else
                prefs.putBoolean(key, val);
    }

    public static <T> void storeDiff(Preferences prefs, String key, T prev, T val) {
        if (val == null) {
            if (prev != null)
                prefs.remove(key);
        } else if (!val.equals(prev))
            prefs.put(key, val.toString());
    }

    public static <T> void storeDiff(Preferences prefs, String key, T[] prevs, T[] vals) {
        for (int i = 0; i < vals.length; i++)
            if (i >= prevs.length || !vals[i].equals(prevs[i]))
                prefs.put(key + '.' + (i+1), vals[i].toString());
        for (int i = vals.length; i < prevs.length;)
            prefs.remove(key + '.' + (++i));
        if (vals.length != prevs.length)
            if (vals.length == 0)
                prefs.remove(key + ".#");
            else
                prefs.putInt(key + ".#", vals.length);
    }

    public static <T> void storeDiff(Preferences prefs, String key, int[] prevs, int[] vals) {
        for (int i = 0; i < vals.length; i++)
            if (i >= prevs.length || vals[i] != prevs[i])
                prefs.putInt(key + '.' + (i+1), vals[i]);
        for (int i = vals.length; i < prevs.length;)
            prefs.remove(key + '.' + (++i));
        if (vals.length != prevs.length)
            if (vals.length == 0)
                prefs.remove(key + ".#");
            else
                prefs.putInt(key + ".#", vals.length);
    }

    public static <T> void storeNotEmpty(Preferences prefs, String key, T[] values) {
        if (values != null && values.length != 0) {
            int count = 0;
            for (T value : values)
                prefs.put(key + '.' + (++count), value.toString());
            prefs.putInt(key + ".#", count);
        }
    }

    public static void storeNotEmpty(Preferences prefs, String key, int[] values) {
        if (values != null && values.length != 0) {
            int count = 0;
            for (int value : values)
                prefs.putInt(key + '.' + (++count), value);
            prefs.putInt(key + ".#", count);
        }
    }

    public static void storeNotNull(Preferences prefs, String key, Object value) {
        if (value != null)
            prefs.put(key, value.toString());
    }

    public static void storeNotDef(Preferences prefs, String key, boolean val, boolean defVal) {
        if (val != defVal)
            prefs.putBoolean(key, val);
    }

    public static void storeNotDef(Preferences prefs, String key, int value, int defVal) {
        if (value != defVal)
            prefs.putInt(key, value);
    }

    public static String[] stringArray(Preferences prefs, String key)  {
        int n = prefs.getInt(key + ".#", 0);
        if (n == 0)
            return StringUtils.EMPTY_STRING;
        
        String[] ss = new String[n];
        for (int i = 0; i < n; i++)
            ss[i] = prefs.get(key + '.' + (i+1), null);
        return ss;
    }

    public static int[] intArray(Preferences prefs, String key)  {
        int n = prefs.getInt(key + ".#", 0);
        if (n == 0)
            return ByteUtils.EMPTY_INTS;
        
        int[] a = new int[n];
        for (int i = 0; i < n; i++)
            a[i] = prefs.getInt(key + '.' + (i+1), 0);
        return a;
    }

    public static Boolean booleanValue(String s) {
        return s != null ? Boolean.valueOf(s) : null;
    }

    public static void storeDiffConnRefs(Preferences prefs,
            List<Connection> prevConnRefs, List<Connection> prevDevConns,
            List<Connection> connRefs, List<Connection> devConns) {
        int prevSize = prevConnRefs.size();
        int size = connRefs.size();
        removeKeys(prefs, "dicomNetworkConnectionReference", size, prevSize);
        for (int i = 0; i < size; i++) {
            int ref = devConns.indexOf(connRefs.get(i));
            if (i >= prevSize || ref != prevDevConns.indexOf(prevConnRefs.get(i)))
                prefs.putInt("dicomNetworkConnectionReference." + (i + 1), ref + 1);
        }
        if (prevSize != size && size != 0)
            prefs.putInt("dicomNetworkConnectionReference.#", size);
    }

    public static void storeConnRefs(Preferences prefs, List<Connection> connRefs,
            List<Connection> devConns) {
        int refCount = 0;
        for (Connection conn : connRefs) {
            prefs.putInt("dicomNetworkConnectionReference." + (++refCount), 
                    devConns.indexOf(conn) + 1);
        }
        prefs.putInt("dicomNetworkConnectionReference.#", refCount);
    }

    public static boolean nodeExists(Preferences prefs, String pathName)
            throws ConfigurationException {
        try {
            return prefs.nodeExists(pathName);
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    public static void removeNode(Preferences prefs, String pathName)
            throws ConfigurationException {
        try {
            if (prefs.nodeExists(pathName)) {
                Preferences node = prefs.node(pathName);
                node.removeNode();
                node.flush();
            }
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }
}
