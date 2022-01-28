/*
 * *** BEGIN LICENSE BLOCK *****
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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2013
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.net.hl7;

import org.dcm4che3.hl7.HL7Segment;

import java.io.Serializable;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jun 2016
 */
public class UnparsedHL7Message implements Serializable {
    private static final AtomicInteger prevSerialNo = new AtomicInteger();
    private final int serialNo;
    private final byte[] data;
    private transient volatile byte[] unescapeXdddd;
    private transient volatile HL7Segment msh;
    private transient volatile int mshLength;

    public UnparsedHL7Message(byte[] data) {
        this.serialNo = prevSerialNo.incrementAndGet();
        this.data = data;
    }

    public HL7Segment msh() {
        init();
        return msh;
    }

    public int getSerialNo() {
        return serialNo;
    }

    private void init() {
        if (msh == null) {
            ParsePosition pos = new ParsePosition(0);
            msh = HL7Segment.parseMSH(data, data.length, pos);
            mshLength = pos.getIndex();
        }
    }

    public byte[] data() {
        return data;
    }

    @Override
    public String toString() {
        if (mshLength == 0) {
            int mshlen = 0;
            while (mshlen < data.length && data[mshlen] != '\r')
                mshlen++;
            mshLength = mshlen;
        }
        return new String(data, 0, mshLength);
    }

    /**
     * Return HL7 message with unescaped hexdata from \Xdddd\ escape sequences.
     * Does not unescape \Xdddd\ escape sequences which contains a field separator,
     * component separator, subcomponent separator, repetition separator or escape character.
     *
     * @return HL7 message with unescaped hexdata from \Xdddd\ escape sequences
     */
    public byte[] unescapeXdddd() {
        if (unescapeXdddd == null)
            unescapeXdddd = unescapeXdddd(data);
        return unescapeXdddd;
    }

    private static byte[] unescapeXdddd(byte[] data) {
        int[] pos = findEscapeXdddd(data);
        return pos.length == 0 ? data : replaceXdddd(data, pos);
    }

    private static byte[] replaceXdddd(byte[] src, int[] pos) {
        byte[] dest = new byte[src.length - calcLengthDecrement(pos)];
        int srcPos = 0;
        int destPos = 0;
        int i = 0;
        do {
            int length = pos[i] - srcPos - 2;
            System.arraycopy(src, srcPos, dest, destPos, length);
            srcPos += length;
            length = replaceXdddd(src, pos[i], pos[++i], dest, destPos += length);
            srcPos += 3 + length;
            destPos += length / 2;
        } while (++i < pos.length);
        System.arraycopy(src, srcPos, dest, destPos, src.length - srcPos);
        return dest;
    }

    private static int replaceXdddd(byte[] src, int beginIndex, int endIndex, byte[] dest, int destPos) {
        for (int i = beginIndex; i < endIndex;) {
            dest[destPos++] = (byte) parseHex(src[i++], src[i++]);
        }
        return endIndex - beginIndex;
    }

    private static int calcLengthDecrement(int[] pos) {
        int i = pos.length;
        int l = 0;
        do {
            l += pos[--i];
            l -= pos[--i];
        } while (i > 0);
        return (l + pos.length * 3) / 2;
    }

    private static int[] findEscapeXdddd(byte[] data) {
        int[] pos = {};
        int x = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0x58) { // == X
                if (i > 0 && data[i-1] == data[6]) {
                    x = i + 1;
                }
            } else if (x > 0 && data[i] == data[6]) {
                if (validHexAndNoSeparator(data, x, i)) {
                    pos = Arrays.copyOf(pos, pos.length + 2);
                    pos[pos.length-2] = x;
                    pos[pos.length-1] = i;
                }
                x = 0;
            }
        }
        return pos;
    }

    private static boolean validHexAndNoSeparator(byte[] data, int beginIndex, int endIndex) {
        if (((endIndex - beginIndex) & 1) != 0) return false;
        int d;
        for (int i = beginIndex; i < endIndex;) {
            if ((d = parseHex(data[i++], data[i++])) < 0
                    || d == data[3]   // field separator
                    || d == data[4]   // component separator
                    || d == data[5]   // repetition separator
                    || d == data[6]   // escape character
                    || d == data[7]   // subcomponent separator
            ) {
                return false;
            }
        }
        return true;
    }

    private static int parseHex(int ch1, int ch2) {
        return (parseHex(ch1) << 4) | parseHex(ch2);
    }

    private static int parseHex(int ch) {
        int d = ch - 0x30;
        if (d > 9) {
            d = ch - 0x41;
            if (d > 5) {
                d = ch - 0x61;
                if (d > 5) return -1;
            }
            if (d >= 0)
                d += 10;
        }
        return d;
    }
}
