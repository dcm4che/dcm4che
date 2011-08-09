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
import java.util.Calendar;
import java.util.Date;

import org.dcm4che.data.Attributes;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class FilePathFormat {

    private final String[] strs;
    private final int[] tags;
    private final boolean[] hash;
    private final int dateTag;

    public FilePathFormat(String pattern) {
        int tagEnd = -1;
        if (pattern.startsWith("{{")) {
            tagEnd = pattern.indexOf("}}");
            if (tagEnd == -1)
                throw new IllegalArgumentException(pattern);
            try {
                dateTag = (int) Long.parseLong(pattern.substring(2, tagEnd), 16);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(pattern);
            }
            tagEnd++;
        } else {
            dateTag = 0;
        }
        int tagStart;
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
            if (hash[i] = strs[i].endsWith("#")) {
                strs[i] = strs[i].substring(0, strs[i].length() - 1);
            }
            String s = tokens.get(index++);
            if (s.length() < 5) {
                if (s.equals("yyyy")) {
                    tags[i] = Calendar.YEAR;
                    continue;
                }
                if (s.equals("MM")) {
                    tags[i] = Calendar.MONTH;
                    continue;
                }
                if (s.equals("dd")) {
                    tags[i] = Calendar.DATE;
                    continue;
                }
                if (s.equals("HH")) {
                    tags[i] = Calendar.HOUR_OF_DAY;
                    continue;
                }
            }
            try {
                tags[i] = (int) Long.parseLong(s, 16);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(pattern);
            }
        }
        strs[n] = tokens.get(index);
    }

    @SuppressWarnings("deprecation")
    public String format(Attributes attrs) {
        StringBuilder sb = new StringBuilder(64);
        Date date = dateTag > 0 ? attrs.getDate(dateTag, null) : new Date();
        final int n = tags.length;
        for (int i = 0; i < n; i++) {
            sb.append(strs[i]);
            switch (tags[i]) {
            case Calendar.YEAR:
                appendNNNN(date != null ? (date.getYear() + 1900) : 0, sb);
                continue;
            case Calendar.MONTH:
                appendNN(date != null ? (date.getMonth() + 1) : 0, sb);
                continue;
            case Calendar.DATE:
                appendNN(date != null ? date.getDate() : 0, sb);
                continue;
            case Calendar.HOUR_OF_DAY:
                appendNN(date != null ? date.getHours() : 0, sb);
                continue;
            }
            String s = attrs.getString(tags[i], null);
            if (hash[i]) {
                if (s == null)
                    sb.append("00000000");
                else {
                    sb.append(TagUtils.toHexString(s.hashCode()));
                }
            } else {
                sb.append(s == null ? "null" : s);
            }
        }
        sb.append(strs[n]);
        return sb.toString();
    }

    private void appendNNNN(int i, StringBuilder sb) {
        if (i < 1000) {
            sb.append('0');
            if (i < 100) {
                sb.append('0');
                if (i < 10) {
                    sb.append('0');
                }
            }
        }
        sb.append(i);
    }

    private void appendNN(int i, StringBuilder sb) {
        if (i < 10)
            sb.append('0');
        sb.append(i);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (dateTag > 0)
            sb.append("{{").append(TagUtils.toHexString(dateTag)).append("}}");
        int n = tags.length;
        for (int i = 0; i < n; i++) {
            sb.append(strs[i]);
            if (hash[i])
                sb.append('#');
            sb.append('{');
            switch (tags[i]) {
            case Calendar.YEAR:
                sb.append("yyyy");
                break;
            case Calendar.MONTH:
                sb.append("MM");
                break;
            case Calendar.DATE:
                sb.append("dd");
                break;
            case Calendar.HOUR_OF_DAY:
                sb.append("HH");
                break;
            default:
                sb.append(TagUtils.toHexString(tags[i]));
            }
            sb.append('}');
        }
        sb.append(strs[n]);
        return sb.toString();
    }
}
