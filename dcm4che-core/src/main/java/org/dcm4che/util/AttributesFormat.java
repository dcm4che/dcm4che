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

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dcm4che.data.Attributes;


/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class AttributesFormat extends Format {

    private static final long serialVersionUID = 1901510733531643054L;

    private static final Pattern pattern = Pattern.compile(
            "\\(\\p{XDigit}{4},\\p{XDigit}{4}\\)(\\[\\d+\\])?");

    private final String[] strs;
    private final int[] tags;
    private final int[] index;
    private final boolean[] hash;
    private final SimpleDateFormat[] dateFormat;

    public AttributesFormat(String s) {
        Matcher m = pattern.matcher(s);
        ArrayList<String> tokens = new ArrayList<String>();
        int tagStart, tagEnd = 0;
        while (m.find()) {
            tagStart = m.start();
            tokens.add(s.substring(tagEnd, tagStart));
            tokens.add(m.group());
            tagEnd = m.end();
        }
        tokens.add(s.substring(tagEnd));
        final int n = tokens.size() / 2;
        strs = new String[n + 1];
        tags = new int[n];
        index = new int[n];
        hash = new boolean[n];
        dateFormat = new SimpleDateFormat[n];
        int j = 0;
        for (int i = 0; i < n; i++) {
            String str = tokens.get(j++);
            String tagStr = tokens.get(j++);
            int tagStrLen = tagStr.length();
            tags[i] = TagUtils.toTag(
                    Integer.parseInt(tagStr.substring(1,5), 16), 
                    Integer.parseInt(tagStr.substring(6,10), 16));
            if (tagStrLen > 13)
                index[i] = Integer.parseInt(tagStr.substring(12, tagStrLen-1));
            if (str.endsWith("#")) {
                hash[i] = true;
                str = str.substring(0, str.length()-1);
            } else {
                int datePos = str.lastIndexOf("date:");
                if (datePos != -1)
                    try {
                        dateFormat[i] = new SimpleDateFormat(str.substring(datePos+5));
                        str = str.substring(0, datePos);
                    } catch (IllegalArgumentException e) {}
            }
            strs[i] = str;
        }
        strs[n] = tokens.get(j);
    }

    public static AttributesFormat valueOf(String s) {
        return s != null ? new AttributesFormat(s) : null;
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        Attributes attrs = (Attributes) obj;
        final int n = tags.length;
        for (int i = 0; i < n; i++) {
            toAppendTo.append(strs[i]);
            if (dateFormat[i] != null) {
                Date d = tags[i] != 0 ? attrs.getDate(tags[i], index[i]) : new Date();
                if (d == null)
                    toAppendTo.append(dateFormat[i].toPattern());
                else
                    dateFormat[i].format(d, toAppendTo, pos);
            } else {
                String s = attrs.getString(tags[i], index[i]);
                if (hash[i]) {
                    if (s == null)
                        toAppendTo.append("00000000");
                    else
                        toAppendTo.append(TagUtils.toHexString(s.hashCode()));
                } else {
                    toAppendTo.append(s);
                }
            }
        }
        toAppendTo.append(strs[n]);
        return toAppendTo;
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int n = tags.length;
        for (int i = 0; i < n; i++) {
            sb.append(strs[i]);
            if (hash[i])
                sb.append('#');
            else if (dateFormat[i] != null)
                sb.append("date:").append(dateFormat[i].toPattern());
            sb.append(TagUtils.toString(tags[i]));
            if (index[i] != 0)
                sb.append('[').append(index[i]).append(']');
        }
        sb.append(strs[n]);
        return sb.toString();
    }

}
