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

package org.dcm4che.util;

import java.util.ArrayList;

import org.dcm4che.data.Attributes;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class FilePathFormat {

    private static final char[] HEX = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private final String[] strs;
    private final int[] tags;
    private final boolean[] hash;

    public FilePathFormat(String pattern) {
        int tagStart;
        int tagEnd = -1;
        ArrayList<String> tokens = new ArrayList<String>();
        while ((tagStart = pattern.indexOf('{', tagEnd + 1)) != -1) {
            tokens.add(pattern.substring(tagEnd + 1, tagStart));
            tagEnd = pattern.indexOf('}', tagStart + 1);
            if (tagEnd == -1)
                throw new IllegalArgumentException(pattern);
            tokens.add(pattern.substring(tagStart + 1, tagEnd));
        }
        tokens.add(pattern.substring(tagEnd + 1));
        final int n = tokens.size() / 2;
        strs = new String[n + 1];
        tags = new int[n];
        hash = new boolean[n];
        int index = 0;
        for (int i = 0; i < n; i++) {
            strs[i] = tokens.get(index++);
            try {
                tags[i] = (int) Long.parseLong(tokens.get(index++), 16);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(pattern);
            }
            if (hash[i] = strs[i].endsWith("#")) {
                strs[i] = strs[i].substring(0, strs[i].length() - 1);
            }
        }
        strs[n] = tokens.get(index);
    }

    public String format(Attributes attrs) {
        StringBuilder sb = new StringBuilder(64);
        final int n = tags.length;
        for (int i = 0; i < n; i++) {
            sb.append(strs[i]);
            String s = attrs.getString(tags[i], null);
            if (hash[i]) {
                if (s == null)
                    sb.append("00000000");
                else {
                    appendHex(s.hashCode(), sb);
                }
            } else {
                sb.append(s == null ? "null" : s);
            }
        }
        sb.append(strs[n]);
        return sb.toString();
    }

    private static void appendHex(int i, StringBuilder sb) {
        sb.append(HEX[(i >>> 28) & 15])
          .append(HEX[(i >>> 24) & 15])
          .append(HEX[(i >>> 20) & 15])
          .append(HEX[(i >>> 16) & 15])
          .append(HEX[(i >>> 12) & 15])
          .append(HEX[(i >>> 8) & 15])
          .append(HEX[(i >>> 4) & 15])
          .append(HEX[i & 15]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int n = tags.length;
        for (int i = 0; i < n; i++) {
            sb.append(strs[i]);
            if (hash[i])
                sb.append('#');
            sb.append('{');
            appendHex(tags[i], sb);
            sb.append('}');
        }
        sb.append(strs[n]);
        return sb.toString();
    }
}
