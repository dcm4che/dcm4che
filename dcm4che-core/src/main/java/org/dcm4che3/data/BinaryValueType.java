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

package org.dcm4che3.data;

import java.util.Date;
import java.util.TimeZone;

import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
enum BinaryValueType implements ValueType {
    BYTE(1, 1) {

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return b;
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return b[off];
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            b[off] = (byte) i;
            return b;
        }
    },
    SHORT(2, 2) {

        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapShorts(preserve ? b.clone() : b, 0, b.length);
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToShort(b, off, bigEndian);
        }

        @Override
        protected long toLong(byte[] b, int off, boolean bigEndian) {
            return toInt(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.shortToBytes(i, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(long l, byte[] b, int off, boolean bigEndian) {
            return toBytes((int) l, b, off, bigEndian);
        }
    },
    USHORT(2, 2) {

        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapShorts(preserve ? b.clone() : b, 0, b.length);
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToUShort(b, off, bigEndian);
        }

        @Override
        protected long toLong(byte[] b, int off, boolean bigEndian) {
            return toInt(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.shortToBytes(i, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(long l, byte[] b, int off, boolean bigEndian) {
            return toBytes((int) l, b, off, bigEndian);
        }
    },
    INT(4, 4) {

        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapInts(preserve ? b.clone() : b, 0, b.length);
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToInt(b, off, bigEndian);
        }

        @Override
        protected long toLong(byte[] b, int off, boolean bigEndian) {
            return toInt(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.intToBytes(i, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(long l, byte[] b, int off, boolean bigEndian) {
            return toBytes((int) l, b, off, bigEndian);
        }
    },
    UINT(4, 4) {

        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapInts(preserve ? b.clone() : b, 0, b.length);
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToInt(b, off, bigEndian);
        }

        @Override
        protected long toLong(byte[] b, int off, boolean bigEndian) {
            return toInt(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.intToBytes(i, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(long l, byte[] b, int off, boolean bigEndian) {
            return toBytes((int) l, b, off, bigEndian);
        }

        @Override
        protected String toString(byte[] b, int off, boolean bigEndian) {
            return Integer.toUnsignedString(toInt(b, off, bigEndian));
        }
    },
    TAG(4, 2) {

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapShorts(preserve ? b.clone() : b, 0, b.length);
        }

        @Override
        protected String toString(byte[] b, int off, boolean bigEndian) {
            return TagUtils.toHexString(toInt(b, off, bigEndian));
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToTag(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(String s, byte[] b, int off, boolean bigEndian) {
            return toBytes(Integer.parseInt(s, 16), b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.tagToBytes(i, b, off, bigEndian);
        }
    },
    LONG(8, 8) {

        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapLongs(preserve ? b.clone() : b, 0, b.length);
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return (int) toLong(b, off, bigEndian);
        }

        @Override
        protected long toLong(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToLong(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return toBytes((long) i, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(long l, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.longToBytes(l, b, off, bigEndian);
        }
    },
    ULONG(8, 8) {

        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapLongs(preserve ? b.clone() : b, 0, b.length);
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return (int) toLong(b, off, bigEndian);
        }

        @Override
        protected long toLong(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToLong(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(String s, byte[] b, int off, boolean bigEndian) {
            return toBytes(StringUtils.parseUV(s), b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return toBytes((long) i, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(long l, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.longToBytes(l, b, off, bigEndian);
        }

        @Override
        protected String toString(byte[] b, int off, boolean bigEndian) {
            return Long.toUnsignedString(toLong(b, off, bigEndian));
        }
    },
    FLOAT(4, 4) {

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapInts(preserve ? b.clone() : b, 0, b.length);
        }

        @Override
        protected String toString(byte[] b, int off, boolean bigEndian) {
            return StringUtils
                    .formatDS(ByteUtils.bytesToFloat(b, off, bigEndian));
        }

        @Override
        protected float toFloat(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToFloat(b, off, bigEndian);
        }

        @Override
        protected double toDouble(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToFloat(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(String s, byte[] b, int off, boolean bigEndian) {
            return toBytes(Float.parseFloat(s), b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(float f, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.floatToBytes(f, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(double d, byte[] b, int off,
                boolean bigEndian) {
            return ByteUtils.floatToBytes((float) d, b, off, bigEndian);
        }
    },
    DOUBLE(8, 8) {

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapLongs(preserve ? b.clone() : b, 0, b.length);
        }

        @Override
        protected String toString(byte[] b, int off, boolean bigEndian) {
            return StringUtils
                    .formatDS(ByteUtils.bytesToDouble(b, off, bigEndian));
        }

        @Override
        protected float toFloat(byte[] b, int off, boolean bigEndian) {
            return (float) ByteUtils.bytesToDouble(b, off, bigEndian);
        }

        @Override
        protected double toDouble(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToDouble(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(String s, byte[] b, int off,
                boolean bigEndian) {
            return toBytes(Double.parseDouble(s), b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(float f, byte[] b, int off,
                boolean bigEndian) {
            return ByteUtils.doubleToBytes(f, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(double d, byte[] b, int off,
                boolean bigEndian) {
            return ByteUtils.doubleToBytes(d, b, off, bigEndian);
        }
     };

    final int numBytes;
    final int numEndianBytes;

    BinaryValueType(int numBytes, int numEndianBytes) {
        this.numBytes = numBytes;
        this.numEndianBytes = numEndianBytes;
    }

    @Override
    public boolean isIntValue() {
        return false;
    }

    @Override
    public boolean isStringValue() {
        return false;
    }

    @Override
    public boolean useSpecificCharacterSet() {
        return false;
    }

    @Override
    public boolean isTemporalType() {
        return false;
    }

    @Override
    public int numEndianBytes() {
        return numEndianBytes;
    }

    protected String toString(byte[] b, int off, boolean bigEndian) {
        return Long.toString(toLong(b, off, bigEndian));
    }

    protected int toInt(byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    protected long toLong(byte[] b, int off, boolean bigEndian) {
        return toInt(b, off, bigEndian);
    }

    protected float toFloat(byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    protected double toDouble(byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    protected byte[] toBytes(String s, byte[] b, int off, boolean bigEndian) {
        return toBytes(StringUtils.parseIS(s), b, off, bigEndian);
    }

    protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    protected byte[] toBytes(long l, byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    protected byte[] toBytes(float f, byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    protected byte[] toBytes(double d, byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] toBytes(Object val, SpecificCharacterSet cs) {
        if (val instanceof byte[])
            return (byte[]) val;

        throw new UnsupportedOperationException();
    } 

    @Override
    public String toString(Object val, boolean bigEndian, int valueIndex,
            String defVal) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        int off = valueIndex * numBytes;
        return off + numBytes <= len
                ? toString(b, off, bigEndian)
                : defVal;
    } 

    private void checkLength(int len) {
        if (len % numBytes != 0)
            throw new IllegalArgumentException("length: " + len);
    }

    @Override
    public Object toStrings(Object val, boolean bigEndian,
            SpecificCharacterSet cs) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        checkLength(len);
        if (len == numBytes)
            return toString(b, 0, bigEndian);

        String[] ss = new String[len / numBytes];
        for (int i = 0, off = 0; i < ss.length; i++, off += numBytes)
            ss[i] = toString(b, off, bigEndian);
        return ss;
    } 

    @Override
    public int toInt(Object val, boolean bigEndian, int valueIndex,
            int defVal) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        int off = valueIndex * numBytes;
        return off + numBytes <= len
                ? toInt(b, off, bigEndian)
                : defVal;
    } 

    @Override
    public int[] toInts(Object val, boolean bigEndian) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        checkLength(len);
        int[] is = new int[len / numBytes];
        for (int i = 0, off = 0; i < is.length; i++, off += numBytes)
            is[i] = toInt(b, off, bigEndian);
        return is;
    } 

    @Override
    public long toLong(Object val, boolean bigEndian, int valueIndex, long defVal) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        int off = valueIndex * numBytes;
        return off + numBytes <= len
                ? toLong(b, off, bigEndian)
                : defVal;
    }

    @Override
    public long[] toLongs(Object val, boolean bigEndian) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        checkLength(len);
        long[] ls = new long[len / numBytes];
        for (int i = 0, off = 0; i < ls.length; i++, off += numBytes)
            ls[i] = toLong(b, off, bigEndian);
        return ls;
    }

    @Override
    public float toFloat(Object val, boolean bigEndian, int valueIndex,
            float defVal) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        int off = valueIndex * numBytes;
        return off + numBytes <= len
                ? toFloat(b, off, bigEndian)
                : defVal;
    } 

    @Override
    public float[] toFloats(Object val, boolean bigEndian) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        checkLength(len);
        float[] fs = new float[len / numBytes];
        for (int i = 0, off = 0; i < fs.length; i++, off += numBytes)
            fs[i] = toFloat(b, off, bigEndian);
        return fs;
    } 

    @Override
    public double toDouble(Object val, boolean bigEndian, int valueIndex,
            double defVal) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        int off = valueIndex * numBytes;
        return off + numBytes <= len
                ? toDouble(b, off, bigEndian)
                : defVal;
    } 

    @Override
    public double[] toDoubles(Object val, boolean bigEndian) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        checkLength(len);
        double[] ds = new double[len / numBytes];
        for (int i = 0, off = 0; i < ds.length; i++, off += numBytes)
            ds[i] = toDouble(b, off, bigEndian);
        return ds;
    } 

    @Override
    public Date toDate(Object val, TimeZone tz, int valueIndex, boolean ceil,
            Date defVal, DatePrecision precision) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date[] toDate(Object val, TimeZone tz, boolean ceil,
            DatePrecisions precisions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toValue(byte[] b) {
        return b != null && b.length > 0 ? b : Value.NULL;
    } 

    @Override
    public Object toValue(String s, boolean bigEndian) {
        if (s == null || s.isEmpty())
            return Value.NULL;

        return toBytes(s, new byte[numBytes], 0, bigEndian);
    } 

    @Override
    public Object toValue(String[] ss, boolean bigEndian) {
        if (ss == null || ss.length == 0)
            return Value.NULL;

        if (ss.length == 1)
            return toValue(ss[0], bigEndian);

        byte[] b = new byte[ss.length * numBytes];
        for (int i = 0, off = 0; i < ss.length; i++, off += numBytes)
            toBytes(ss[i], b, off, bigEndian);

        return b;
    } 

    @Override
    public Object toValue(int[] is, boolean bigEndian) {
        if (is == null || is.length == 0)
            return Value.NULL;

        byte[] b = new byte[is.length * numBytes];
        for (int i = 0, off = 0; i < is.length; i++, off += numBytes)
            toBytes(is[i], b, off, bigEndian);

        return b;
    } 

    @Override
    public Object toValue(long[] ls, boolean bigEndian) {
        if (ls == null || ls.length == 0)
            return Value.NULL;

        byte[] b = new byte[ls.length * numBytes];
        for (int i = 0, off = 0; i < ls.length; i++, off += numBytes)
            toBytes(ls[i], b, off, bigEndian);

        return b;
    }

    @Override
    public Object toValue(float[] fs, boolean bigEndian) {
        if (fs == null || fs.length == 0)
            return Value.NULL;

        byte[] b = new byte[fs.length * numBytes];
        for (int i = 0, off = 0; i < fs.length; i++, off += numBytes)
            toBytes(fs[i], b, off, bigEndian);

        return b;
    } 

    @Override
    public Object toValue(double[] ds, boolean bigEndian) {
        if (ds == null || ds.length == 0)
            return Value.NULL;

        byte[] b = new byte[ds.length * numBytes];
        for (int i = 0, off = 0; i < ds.length; i++, off += numBytes)
            toBytes(ds[i], b, off, bigEndian);

        return b;
    } 

    @Override
    public Object toValue(Date[] ds, TimeZone tz, DatePrecision precision) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean prompt(Object val, boolean bigEndian,
            SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        if (val instanceof byte[])
            return prompt((byte[]) val, bigEndian, maxChars, sb);

        return StringValueType.prompt(val.toString(), maxChars, sb);
   }

    private boolean prompt(byte[] b, boolean bigEndian, int maxChars,
            StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        for (int i = b.length / numBytes, off = 0; i-- > 0; off += numBytes) {
            sb.append(toString(b, off, bigEndian));
            if (sb.length() > maxLength) {
                sb.setLength(maxLength+1);
                return false;
            }
            if (i > 0)
                sb.append('\\');
        }
        return true;
    }

    @Override
    public int vmOf(Object val) {
        if (val instanceof byte[]) {
            return ((byte[]) val).length / numBytes;
        }
        throw new UnsupportedOperationException();
    }

}
