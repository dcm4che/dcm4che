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

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class ByteUtils {

    public static final byte[] EMPTY_BYTES = {};
    public static final int[] EMPTY_INTS = {};
    public static final float[] EMPTY_FLOATS = {};
    public static final double[] EMPTY_DOUBLES = {};

    public static int bytesToVR(byte[] bytes, int off) {
        return bytesToUShortBE(bytes, off);
    }

    public static int bytesToUShort(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToUShortBE(bytes, off)
                         : bytesToUShortLE(bytes, off);
    }

    public static int bytesToUShortBE(byte[] bytes, int off) {
        return ((bytes[off] & 255) << 8) + (bytes[off + 1] & 255);
    }

    public static int bytesToUShortLE(byte[] bytes, int off) {
        return ((bytes[off + 1] & 255) << 8) + (bytes[off] & 255);
    }

    public static int bytesToShort(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToShortBE(bytes, off)
                         : bytesToShortLE(bytes, off);
    }

    public static int bytesToShortBE(byte[] bytes, int off) {
        return (bytes[off] << 8) + (bytes[off + 1] & 255);
    }

    public static int bytesToShortLE(byte[] bytes, int off) {
        return (bytes[off + 1] << 8)  + (bytes[off] & 255);
    }

    public static int bytesToInt(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToIntBE(bytes, off) : bytesToIntLE(bytes, off);
    }

    public static int bytesToIntBE(byte[] bytes, int off) {
        return (bytes[off] << 24) + ((bytes[off + 1] & 255) << 16)
                + ((bytes[off + 2] & 255) << 8) + (bytes[off + 3] & 255);
    }

    public static int bytesToIntLE(byte[] bytes, int off) {
        return (bytes[off + 3] << 24) + ((bytes[off + 2] & 255) << 16)
                + ((bytes[off + 1] & 255) << 8) + (bytes[off] & 255);
    }

    public static int bytesToTag(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToTagBE(bytes, off) : bytesToTagLE(bytes, off);
    }

    public static int bytesToTagBE(byte[] bytes, int off) {
        return bytesToIntBE(bytes, off);
    }

    public static int bytesToTagLE(byte[] bytes, int off) {
        return (bytes[off + 1] << 24) + ((bytes[off] & 255) << 16)
                + ((bytes[off + 3] & 255) << 8) + (bytes[off + 2] & 255);
    }

    public static float bytesToFloat(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToFloatBE(bytes, off)
                         : bytesToFloatLE(bytes, off);
    }

    public static float bytesToFloatBE(byte[] bytes, int off) {
        return Float.intBitsToFloat(bytesToIntBE(bytes, off));
    }

    public static float bytesToFloatLE(byte[] bytes, int off) {
        return Float.intBitsToFloat(bytesToIntLE(bytes, off));
    }

    public static long bytesToLong(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToLongBE(bytes, off)
                         : bytesToLongLE(bytes, off);
    }

    public static long bytesToLongBE(byte[] bytes, int off) {
        return ((long) bytes[off] << 56)
                + ((long) (bytes[off + 1] & 255) << 48)
                + ((long) (bytes[off + 2] & 255) << 40)
                + ((long) (bytes[off + 3] & 255) << 32)
                + ((long) (bytes[off + 4] & 255) << 24)
                + ((bytes[off + 5] & 255) << 16)
                + ((bytes[off + 6] & 255) << 8)
                + (bytes[off + 7] & 255);
     }

    public static long bytesToLongLE(byte[] bytes, int off) {
        return ((long) bytes[off + 7] << 56)
                + ((long) (bytes[off + 6] & 255) << 48)
                + ((long) (bytes[off + 5] & 255) << 40)
                + ((long) (bytes[off + 4] & 255) << 32)
                + ((long) (bytes[off + 3] & 255) << 24)
                + ((bytes[off + 2] & 255) << 16)
                + ((bytes[off + 1] & 255) << 8)
                + (bytes[off] & 255);
    }

    public static double bytesToDouble(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToDoubleBE(bytes, off)
                         : bytesToDoubleLE(bytes, off);
    }

    public static double bytesToDoubleBE(byte[] bytes, int off) {
        return Double.longBitsToDouble(bytesToLongBE(bytes, off));
    }

    public static double bytesToDoubleLE(byte[] bytes, int off) {
        return Double.longBitsToDouble(bytesToLongLE(bytes, off));
    }

    public static byte[] shortToBytes(int i, byte[] bytes, int off,
            boolean bigEndian) {
        return bigEndian ? shortToBytesBE(i, bytes, off)
                         : shortToBytesLE(i, bytes, off);
    }

    public static byte[] shortToBytesBE(int i, byte[] bytes, int off) {
        bytes[off] = (byte) (i >> 8);
        bytes[off + 1] = (byte) i;
        return bytes;
    }

    public static byte[] shortToBytesLE(int i, byte[] bytes, int off) {
        bytes[off + 1] = (byte) (i >> 8);
        bytes[off] = (byte) i;
        return bytes;
    }

    public static byte[] intToBytes(int i, byte[] bytes, int off,
            boolean bigEndian) {
        return bigEndian ? intToBytesBE(i, bytes, off)
                         : intToBytesLE(i, bytes, off);
    }

    public static byte[] intToBytesBE(int i, byte[] bytes, int off) {
        bytes[off] = (byte) (i >> 24);
        bytes[off + 1] = (byte) (i >> 16);
        bytes[off + 2] = (byte) (i >> 8);
        bytes[off + 3] = (byte) i;
        return bytes;
    }

    public static byte[] intToBytesLE(int i, byte[] bytes, int off) {
        bytes[off + 3] = (byte) (i >> 24);
        bytes[off + 2] = (byte) (i >> 16);
        bytes[off + 1] = (byte) (i >> 8);
        bytes[off] = (byte) i;
        return bytes;
    }

    public static byte[] tagToBytes(int i, byte[] bytes, int off,
            boolean bigEndian) {
        return bigEndian ? tagToBytesBE(i, bytes, off)
                         : tagToBytesLE(i, bytes, off);
    }

    public static byte[] tagToBytesBE(int i, byte[] bytes, int off) {
        return intToBytesBE(i, bytes, off);
    }

    public static byte[] tagToBytesLE(int i, byte[] bytes, int off) {
        bytes[off + 1] = (byte) (i >> 24);
        bytes[off] = (byte) (i >> 16);
        bytes[off + 3] = (byte) (i >> 8);
        bytes[off + 2] = (byte) i;
        return bytes;
    }

    public static byte[] floatToBytes(float f, byte[] bytes, int off,
            boolean bigEndian) {
        return bigEndian ? floatToBytesBE(f, bytes, off)
                         : floatToBytesLE(f, bytes, off);
    }

   public static byte[] floatToBytesBE(float f, byte[] bytes, int off) {
        return intToBytesBE(Float.floatToIntBits(f), bytes, off);
    }

    public static byte[] floatToBytesLE(float f, byte[] bytes, int off) {
        return intToBytesLE(Float.floatToIntBits(f), bytes, off);
    }

    public static byte[] doubleToBytes(double d, byte[] bytes, int off,
            boolean bigEndian) {
        return bigEndian ? doubleToBytesBE(d, bytes, off)
                         : doubleToBytesLE(d, bytes, off);
    }

    public static byte[] doubleToBytesBE(double d, byte[] bytes, int off) {
        return longToBytesBE(Double.doubleToLongBits(d), bytes, off);
    }

    public static byte[] doubleToBytesLE(double d, byte[] bytes, int off) {
        return longToBytesLE(Double.doubleToLongBits(d), bytes, off);
    }

    public static byte[] longToBytes(long l, byte[] bytes, int off,
            boolean bigEndian) {
        return bigEndian ? longToBytesBE(l, bytes, off)
                         : longToBytesLE(l, bytes, off);
    }

    public static byte[] longToBytesBE(long l, byte[] bytes, int off) {
        bytes[off] = (byte) (l >> 56);
        bytes[off + 1] = (byte) (l >> 48);
        bytes[off + 2] = (byte) (l >> 40);
        bytes[off + 3] = (byte) (l >> 32);
        bytes[off + 4] = (byte) (l >> 24);
        bytes[off + 5] = (byte) (l >> 16);
        bytes[off + 6] = (byte) (l >> 8);
        bytes[off + 7] = (byte) l;
        return bytes;
    }

    public static byte[] longToBytesLE(long l, byte[] bytes, int off) {
        bytes[off + 7] = (byte) (l >> 56);
        bytes[off + 6] = (byte) (l >> 48);
        bytes[off + 5] = (byte) (l >> 40);
        bytes[off + 4] = (byte) (l >> 32);
        bytes[off + 3] = (byte) (l >> 24);
        bytes[off + 2] = (byte) (l >> 16);
        bytes[off + 1] = (byte) (l >> 8);
        bytes[off] = (byte) l;
        return bytes;
    }

    public static byte[] swapShorts(byte b[], int off, int len) {
        checkLength(len, 2);
        for (int i = off, n = off + len; i < n; i += 2)
            swap(b, i, i+1);
        return b;
    }

    public static byte[] swapInts(byte b[], int off, int len) {
        checkLength(len, 4);
        for (int i = off, n = off + len; i < n; i += 4) {
            swap(b, i, i+3);
            swap(b, i+1, i+2);
        }
        return b;
    }

    public static byte[] swapLongs(byte b[], int off, int len) {
        checkLength(len, 8);
        for (int i = off, n = off + len; i < n; i += 8) {
            swap(b, i, i+7);
            swap(b, i+1, i+6);
            swap(b, i+2, i+5);
            swap(b, i+3, i+4);
        }
        return b;
    }

    private static void checkLength(int len, int numBytes) {
        if (len < 0 || (len % numBytes) != 0)
            throw new IllegalArgumentException("length: " + len);
    }

    private static void swap(byte[] bytes, int a, int b) {
        byte t = bytes[a];
        bytes[a] = bytes[b];
        bytes[b] = t;
    }

}
