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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4che.hl7;

import java.util.Arrays;
import java.util.List;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Segment {

    public static final int SegmentID = 0;

    private final char fieldSeparator;
    private String[] fields;

    public Segment(int size, char fieldSeparator) {
        if (size <= 0)
            throw new IllegalArgumentException("size: " + size);
        this.fieldSeparator = fieldSeparator;
        this.fields = new String[size];
    }

    public Segment(String s, char fieldDelimiter) {
        this(count(s, fieldDelimiter), fieldDelimiter);
        String[] ss = fields;
        int count = ss.length;
        int begin, end = s.length();
        while (--count >= 0) {
            begin = s.lastIndexOf(fieldDelimiter, end-1);
            ss[count] = s.substring(begin+1, end);
            end = begin;
        }
    }

    private static int count(String s, char delim) {
        int count = 1;
        int pos = -1;
        while ((pos = s.indexOf(delim, pos+1)) >= 0)
            count++;

        return count;
    }

    public final char getFieldSeparator() {
        return fieldSeparator;
    }

    public void setField(int index, String value) {
        if (index >= fields.length)
            fields = Arrays.copyOf(fields, index+1);
        fields[index] = value;
    }

    public String getField(int index, String defVal) {
        String val = index < fields.length ? fields[index] : null;
        return val != null ? val : defVal;
    }

    public int size() {
        return fields.length;
    }

    public String toString() {
        String[] ss = fields;
        int n = ss.length;
        int len = n - 1;
        for (String s : ss)
            len += s != null ? s.length() : 0;

        char[] cs = new char[len];
        for (int i = 0, off = 0; i < n; ++i) {
            if (i != 0)
                cs[off++] = fieldSeparator;
            String s = ss[i];
            if (s != null) {
                int l = s.length();
                s.getChars(0, l, cs, off);
                off += l;
            }
        }
        return new String(cs);
    }

    public static String toString(List<Segment> msg) {
        int len = msg.size();
        for (Segment seg : msg) {
            String[] ss = seg.fields;
            len += ss.length - 1;
            for (String s : ss)
                len += s != null ? s.length() : 0;
        }
        char[] cs = new char[len];
        int off = 0;
        for (Segment seg : msg) {
            char delim = seg.getFieldSeparator();
            String[] ss = seg.fields;
            for (String s : ss) {
                if (s != null) {
                    int l = s.length();
                    s.getChars(0, l, cs, off);
                    off += l;
                }
                cs[off++] = delim;
            }
            cs[off-1] = '\r';
        }
        return new String(cs);
    }

    public static int endOfSegment(byte[] b, int begin, int end) {
        for (int i = begin; i < end; i++)
            if (b[i] == '\r')
                return i;
        return end;
    }
}
