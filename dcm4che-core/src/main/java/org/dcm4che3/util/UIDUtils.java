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

package org.dcm4che3.util;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Value;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class UIDUtils {

    /**
     * UID root for UUIDs (Universally Unique Identifiers) generated in
     * accordance with Rec. ITU-T X.667 | ISO/IEC 9834-8.
     * @see &lt;a href="http://www.oid-info.com/get/2.25">OID repository {joint-iso-itu-t(2) uuid(25)}$lt;/a>
     */
    private static final String UUID_ROOT = "2.25";

    private static final Pattern PATTERN =
            Pattern.compile("[012]((\\.0)|(\\.[1-9]\\d*))+");

    private static String root = UUID_ROOT;

    public static final String getRoot() {
        return root;
    }

    public static final void setRoot(String root) {
        checkRoot(root);
        UIDUtils.root = root;
    }

    private static void checkRoot(String root) {
        if (root.length() > 24)
            throw new IllegalArgumentException("root length > 24");
        if (!isValid(root))
            throw new IllegalArgumentException(root);
    }

    public static boolean isValid(String uid) {
        return uid.length() <= 64 && PATTERN.matcher(uid).matches();
    }

    public static String createUID() {
        return randomUID(root);
    }

    public static String createNameBasedUID(byte[] name) {
        return nameBasedUID(name, root);
    }

    public static String createNameUID(byte[] name, String root) {
        checkRoot(root);
        return nameBasedUID(name, root);
    }

    public static String createUID(String root) {
        checkRoot(root);
        return randomUID(root);
    }

    public static String createUIDIfNull(String uid) {
        return uid == null ? randomUID(root) : uid;
    }

    public static String createUIDIfNull(String uid, String root) {
        checkRoot(root);
        return uid == null ? randomUID(root) : uid;
    }

    private static String randomUID(String root) {
        return toUID(root, UUID.randomUUID());
    }

    private static String nameBasedUID(byte[] name, String root) {
        return toUID(root, UUID.nameUUIDFromBytes(name));
    }

    private static String toUID(String root, UUID uuid) {
        byte[] b17 = new byte[17];
        ByteUtils.longToBytesBE(uuid.getMostSignificantBits(), b17, 1);
        ByteUtils.longToBytesBE(uuid.getLeastSignificantBits(), b17, 9);
        String uuidStr = new BigInteger(b17).toString();
        int rootlen = root.length();
        int uuidlen = uuidStr.length();
        char[] cs = new char[rootlen + uuidlen + 1];
        root.getChars(0, rootlen, cs, 0);
        cs[rootlen] = '.';
        uuidStr.getChars(0, uuidlen, cs, rootlen + 1);
        return new String(cs);
    }

    public static StringBuilder promptTo(String uid, StringBuilder sb) {
        return sb.append(uid).append(" - ").append(UID.nameOf(uid));
    }

    public static String[] findUIDs(String regex) {
        Pattern p = Pattern.compile(regex);
        Field[] fields = UID.class.getFields();
        String[] uids = new String[fields.length];
        int j = 0;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (p.matcher(field.getName()).matches())
                try {
                    uids[j++] = (String) field.get(null);
                } catch (Exception ignore) { }
        }
        return Arrays.copyOf(uids, j);
    }

    public static int remapUIDs(Attributes attrs, Map<String,String> uidMap) {
        UIDUtils.Visitor visitor = new UIDUtils.Visitor(uidMap);
        try {
            attrs.accept(visitor, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return visitor.replaced;
    }

    private static class Visitor implements Attributes.Visitor {
        private final Map<String, String> uidMap;
        private int replaced;

        public Visitor(Map<String, String> uidMap) {
            this.uidMap = uidMap;
        }

        @Override
        public boolean visit(Attributes attrs, int tag, VR vr, Object val) {
            if (vr != VR.UI || val == Value.NULL)
                return true;

            String[] ss;
            if (val instanceof byte[]) {
                ss = attrs.getStrings(tag);
                val = ss.length == 1 ? ss[0] : ss;
            }
            if (val instanceof String[]) {
                ss = (String[]) val;
                for (int i = 0; i < ss.length; i++) {
                    String uid = uidMap.get(ss[i]);
                    if (uid != null) {
                        ss[i] = uid;
                        replaced++;
                    }
                }
            } else {
                String uid = uidMap.get(val);
                if (uid != null) {
                    attrs.setString(tag, VR.UI, uid);
                    replaced++;
                }
            }
            return true;
        }
    }
}
