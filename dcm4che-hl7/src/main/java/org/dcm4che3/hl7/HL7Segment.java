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

package org.dcm4che3.hl7;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class HL7Segment implements Serializable {

    private static final long serialVersionUID = 2268883954083242976L;
    private static final AtomicInteger nextMessageControlID =
            new AtomicInteger(new Random().nextInt());

    private final char fieldSeparator;
    private final String encodingCharacters;
    private String[] fields;

    public HL7Segment(int size, char fieldSeparator, String encodingCharacters) {
        if (size <= 0)
            throw new IllegalArgumentException("size: " + size);
        this.fieldSeparator = fieldSeparator;
        this.encodingCharacters = encodingCharacters;
        this.fields = new String[size];
    }

    public HL7Segment(int size) {
        this(size, '|', "^~\\&");
    }

    public HL7Segment(String s, char fieldSeparator, String encodingCharacters) {
        this.fieldSeparator = fieldSeparator;
        this.encodingCharacters = encodingCharacters;
        this.fields = split(s, fieldSeparator);
    }

    public final char getFieldSeparator() {
        return fieldSeparator;
    }

    public final char getComponentSeparator() {
        return encodingCharacters.charAt(0);
    }

    public final char getRepetitionSeparator() {
        return encodingCharacters.charAt(1);
    }

    public final char getEscapeCharacter() {
        return encodingCharacters.charAt(2);
    }

    public final char getSubcomponentSeparator() {
        return encodingCharacters.charAt(3);
    }

    public final String getEncodingCharacters() {
        return encodingCharacters;
    }

    public void setField(int index, String value) {
        if (index >= fields.length)
            fields = Arrays.copyOf(fields, index+1);
        fields[index] = value;
    }

    public String getField(int index, String defVal) {
        String val = index < fields.length ? fields[index] : null;
        return val != null && !val.isEmpty() ? val : defVal;
    }

    public int size() {
        return fields.length;
    }

    public String getSendingApplicationWithFacility() {
        return getField(2, "") + '|' + getField(3, "");
    }

    public void setSendingApplicationWithFacility(String s) {
        String[] ss = split(s, '|');
        setField(2, ss[0]);
        if (ss.length > 1)
            setField(3, ss[1]);
    }

    public String getReceivingApplicationWithFacility() {
        return getField(4, "") + '|' + getField(5, "");
    }

    public void setReceivingApplicationWithFacility(String s) {
        String[] ss = split(s, '|');
        setField(4, ss[0]);
        if (ss.length > 1)
            setField(5, ss[1]);
    }

    public String getMessageType() {
        String s = getField(8, "").replace(getComponentSeparator(), '^');
        int end = s.indexOf('^', s.indexOf('^') + 1);
        return end > 0 ? s.substring(0, end) : s;
    }

    public String getMessageControlID() {
        return getField(9, null);
    }

    public String toString() {
        return concat(fields, fieldSeparator);
    }

    public static String concat(String[] ss, char delim) {
        int n = ss.length;
        if (n == 0)
            return "";
        if (n == 1) {
            String s = ss[0];
            return s != null ? s : "";
        }
        int len = n - 1;
        for (String s : ss)
            if (s != null)
                len += s.length();
        char[] cs = new char[len];
        for (int i = 0, off = 0; i < n; ++i) {
            if (i != 0)
                cs[off++] = delim;
            String s = ss[i];
            if (s != null) {
                int l = s.length();
                s.getChars(0, l, cs, off);
                off += l;
            }
        }
        return new String(cs);
    }

    public static String[] split(String s, char delim) {
        int count = 1;
        int delimPos = -1;
        while ((delimPos = s.indexOf(delim, delimPos+1)) >= 0)
            count++;

        if (count == 1)
            return new String[] { s };

        String[] ss = new String[count];
        int delimPos2 = s.length();
        while (--count >= 0) {
            delimPos = s.lastIndexOf(delim, delimPos2-1);
            ss[count] = s.substring(delimPos+1, delimPos2);
            delimPos2 = delimPos;
        }
        return ss;
    }

    public static HL7Segment parseMSH(byte[] b, int size) {
        return parseMSH(b, size, new ParsePosition(0));
    }

    public static HL7Segment parseMSH(byte[] b, int size, ParsePosition pos) {
        String s = parse(b, size, pos, null);
        if (s.length() < 8)
            throw new IllegalArgumentException("Invalid MSH Segment: " + s);
        return new HL7Segment(s, s.charAt(3), s.substring(4,8));
    }

    static HL7Segment parse(byte[] b, int size, ParsePosition pos,
            char fieldSeparator, String encodingCharacters, String charsetName) {
        String s = parse(b, size, pos, charsetName);
        return s != null
                ? new HL7Segment(s, fieldSeparator, encodingCharacters)
                : null;
    }

    private static String parse(byte[] b, int size, ParsePosition pos,
            String charsetName) {
        int off = pos.getIndex();
        int end = off;
        while (end < size && b[end] != '\r' && b[end] != '\n')
            end++;

        int len = end - off;
        if (len == 0)
            return null;

        if (++end < size && (b[end] == '\r' || b[end] == '\n'))
            end++;

        pos.setIndex(end);
        try {
            return charsetName != null 
                    ? new String(b, off, len, charsetName)
                    : new String(b, off, len);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("charsetName: " + charsetName);
        }
    }

    public static String nextMessageControlID() {
        return Integer.toString(
                nextMessageControlID.getAndIncrement() & 0x7FFFFFFF);
    }

    public static String timeStamp(Date date) {
        return new SimpleDateFormat("yyyyMMddHHmmss.SSS").format(date);
    }

    public static HL7Segment makeMSH() {
        return makeMSH(21, '|', "^~\\&");
    }

    public static HL7Segment makeMSH(int size, char fieldSeparator, String encodingCharacters) {
        HL7Segment msh = new HL7Segment(size, fieldSeparator, encodingCharacters);
        msh.setField(0, "MSH");
        msh.setField(1, encodingCharacters);
        msh.setField(6, timeStamp(new Date()));
        msh.setField(9, nextMessageControlID());
        msh.setField(10, "P");
        msh.setField(11, "2.5");
        return msh;
    }
}
