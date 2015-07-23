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

package org.dcm4che3.data;

import java.util.Date;
import java.util.TimeZone;

import org.dcm4che3.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
enum StringValueType implements ValueType {
    ASCII("\\", null),
    STRING("\\", null){

        @Override
        public boolean useSpecificCharacterSet() {
            return true;
        }

        @Override
        protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
            return cs;
        }
    },
    TEXT("\t\n\f\r", null) {

        @Override
        public boolean useSpecificCharacterSet() {
            return true;
        }

        @Override
        protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
            return cs;
        }

        @Override
        protected Object splitAndTrim(String s, SpecificCharacterSet cs) {
            return cs.toText(StringUtils.trimTrailing(s));
        }
    },
    UR(null, null) {

        @Override
        protected Object splitAndTrim(String s, SpecificCharacterSet cs) {
            return StringUtils.trimTrailing(s);
        }
    },
    DA("\\", TemporalType.DA),
    DT("\\", TemporalType.DT),
    TM("\\", TemporalType.TM),
    PN("^=\\", null){

        @Override
        public boolean useSpecificCharacterSet() {
            return true;
        }

        @Override
        protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
            return cs;
        }
    },
    DS("\\", null) {

        @Override
        public byte[] toBytes(Object val, SpecificCharacterSet cs) {

            if (val instanceof double[])
                val = toStrings((double[]) val);
            return super.toBytes(val, cs);
        } 

        @Override
        public String toString(Object val, boolean bigEndian, int valueIndex,
                String defVal) {

            if (val instanceof double[]) {
                double[] ds = (double[]) val;
                return (valueIndex < ds.length
                        && !Double.isNaN(ds[valueIndex]))
                                ? StringUtils.formatDS(ds[valueIndex])
                                : defVal;
            }
            return super.toString(val, bigEndian, valueIndex, defVal);
        } 

        @Override
        public Object toStrings(Object val, boolean bigEndian,
                SpecificCharacterSet cs) {

            return (val instanceof double[])
                    ? toStrings((double[]) val)
                    : super.toStrings(val, bigEndian, cs);
        }

        private Object toStrings(double[] ds) {
            if (ds.length == 1)
                return StringUtils.formatDS(ds[0]);

            String[] ss = new String[ds.length];
            for (int i = 0; i < ds.length; i++)
                ss[i] = !Double.isNaN(ds[i]) ? StringUtils.formatDS(ds[i]) : "";

            return ss;
        }

        @Override
        public float toFloat(Object val, boolean bigEndian, int valueIndex,
                float defVal) {
            double[] ds = (double[]) val;
            return valueIndex < ds.length && !Double.isNaN(ds[valueIndex])
                    ? (float) ds[valueIndex]
                    : defVal;
        } 

        @Override
        public float[] toFloats(Object val, boolean bigEndian) {
            double[] ds = (double[]) val;
            float[] fs = new float[ds.length];
            for (int i = 0; i < fs.length; i++)
                fs[i] = (float) ds[i];
            return fs;
        }

        @Override
        public double toDouble(Object val, boolean bigEndian, int valueIndex,
                double defVal) {
            double[] ds = (double[]) val;
            return valueIndex < ds.length && !Double.isNaN(ds[valueIndex])
                    ? ds[valueIndex]
                    : defVal;
        } 

        @Override
        public double[] toDoubles(Object val, boolean bigEndian) {
            return (double[]) val;
        }

        @Override
        public Object toValue(float[] fs, boolean bigEndian) {
            if (fs == null || fs.length == 0)
                return Value.NULL;

            if (fs.length == 1)
                return StringUtils.formatDS(fs[0]);
            
            String[] ss = new String[fs.length];
            for (int i = 0; i < fs.length; i++)
                ss[i] = StringUtils.formatDS(fs[i]);
            return ss;
        } 

        @Override
        public Object toValue(double[] ds, boolean bigEndian) {
            if (ds == null || ds.length == 0)
                return Value.NULL;

            return ds;
        } 

        @Override
        public boolean prompt(Object val, boolean bigEndian,
                SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
            if (val instanceof double[])
                val = toStrings((double[]) val);
            return super.prompt(val, bigEndian, cs, maxChars, sb);
        }
    },
    IS("\\", null) {

        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toBytes(Object val, SpecificCharacterSet cs) {

            if (val instanceof int[])
                val = toStrings((int[]) val);
            return super.toBytes(val, cs);
        } 

        @Override
        public String toString(Object val, boolean bigEndian, int valueIndex,
                String defVal) {

            if (val instanceof int[]) {
                int[] is = (int[]) val;
                return (valueIndex < is.length
                        && is[valueIndex] != Integer.MIN_VALUE)
                                ? Integer.toString(is[valueIndex])
                                : defVal;
            }
            return super.toString(val, bigEndian, valueIndex, defVal);
        } 

        @Override
        public Object toStrings(Object val, boolean bigEndian,
                SpecificCharacterSet cs) {

            return (val instanceof int[])
                    ? toStrings((int[]) val)
                    : super.toStrings(val, bigEndian, cs);
        }

        private Object toStrings(int[] is) {
            if (is.length == 1)
                return Integer.toString(is[0]);

            String[] ss = new String[is.length];
            for (int i = 0; i < is.length; i++)
                ss[i] = is[i] != Integer.MIN_VALUE ? Integer.toString(is[i]) : "";

            return ss;
        }

        @Override
        public int toInt(Object val, boolean bigEndian, int valueIndex,
                int defVal) {
            int[] is = (int[]) val;
            return valueIndex < is.length && is[valueIndex] != Integer.MIN_VALUE
                    ? is[valueIndex]
                    : defVal;
        } 

        @Override
        public int[] toInts(Object val, boolean bigEndian) {
            return (int[]) val;
        }

        @Override
        public Object toValue(int[] is, boolean bigEndian) {
            if (is == null || is.length == 0)
                return Value.NULL;

            return is;
        } 

        @Override
        public boolean prompt(Object val, boolean bigEndian,
                SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
            if (val instanceof int[])
                val = toStrings((int[]) val);
            return super.prompt(val, bigEndian, cs, maxChars, sb);
        }
    };

    final String delimiters;
    final TemporalType temporalType; 

    StringValueType(String delimiters, TemporalType temperalType) {
        this.delimiters = delimiters;
        this.temporalType = temperalType;
   }

    @Override
    public boolean isStringValue() {
        return true;
    }

    @Override
    public boolean isIntValue() {
        return false;
    }

    @Override
    public boolean isTemporalType() {
        return temporalType != null;
    }

    @Override
    public int numEndianBytes() {
        return 1;
    }

    @Override
    public byte[] toggleEndian(byte[] b, boolean preserve) {
        return b;
    }

    @Override
    public boolean useSpecificCharacterSet() {
        return false;
    }

    protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
        return SpecificCharacterSet.ASCII;
    }

    @Override
    public byte[] toBytes(Object val, SpecificCharacterSet cs) {

        if (val instanceof byte[])
            return (byte[]) val;

        if (val instanceof String)
            return cs(cs).encode((String) val, delimiters);

        if (val instanceof String[])
            return cs(cs).encode(
                    StringUtils.concat((String[]) val, '\\'), delimiters);

        throw new UnsupportedOperationException();
    } 

    @Override
    public String toString(Object val, boolean bigEndian, int valueIndex,
            String defVal) {

        if (val instanceof String)
            return (String) (valueIndex == 0 ? val : defVal);

        if (val instanceof String[]) {
            String[] ss = (String[]) val;
            return (valueIndex < ss.length && ss[valueIndex] != null && !ss[valueIndex].isEmpty())
                    ? ss[valueIndex]
                    : defVal;
        }

        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toStrings(Object val, boolean bigEndian,
            SpecificCharacterSet cs) {

        if (val instanceof byte[]) {
            return splitAndTrim(cs(cs).decode((byte[]) val), cs);
        }

        if (val instanceof String
                || val instanceof String[])
            return val;

        throw new UnsupportedOperationException();
    } 

    protected Object splitAndTrim(String s, SpecificCharacterSet cs) {
        return StringUtils.splitAndTrim(s, '\\');
    }

    @Override
    public int toInt(Object val, boolean bigEndian, int valueIndex,
            int defVal) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public int[] toInts(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public float toFloat(Object val, boolean bigEndian, int valueIndex,
            float defVal) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public float[] toFloats(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public double toDouble(Object val, boolean bigEndian, int valueIndex,
            double defVal) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public double[] toDoubles(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 


    @Override
    public Date toDate(Object val, TimeZone tz, int valueIndex, boolean ceil,
            Date defVal, DatePrecision precision) {
        if (temporalType == null)
            throw new UnsupportedOperationException();

        if (val instanceof String) {
            return valueIndex == 0
                ? temporalType.parse(tz, (String) val, ceil, precision)
                : defVal;
        }
        if (val instanceof String[]) {
            String[] ss = (String[]) val;
            return (valueIndex < ss.length && ss[valueIndex] != null)
                ? temporalType.parse(tz, ss[valueIndex], ceil, precision)
                : defVal;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Date[] toDate(Object val, TimeZone tz, boolean ceil,
            DatePrecisions precisions) {
        if (temporalType == null)
            throw new UnsupportedOperationException();

        if (val instanceof String) {
            precisions.precisions = new DatePrecision[1];
            return new Date[] { temporalType.parse(tz, (String) val, ceil,
                    precisions.precisions[0] = new DatePrecision()) };
        }
        if (val instanceof String[]) {
            String[] ss = (String[]) val;
            Date[] is = new Date[ss.length];
            precisions.precisions = new DatePrecision[ss.length];
            for (int i = 0; i < is.length; i++) {
                if (ss[i] != null) {
                    is[i] = temporalType.parse(tz, ss[i], ceil, 
                            precisions.precisions[i] = new DatePrecision());
                }
            }
            return is;
        }
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

        return s;
    } 

    @Override
    public Object toValue(String[] ss, boolean bigEndian) {
        if (ss == null || ss.length == 0)
            return Value.NULL;

        if (ss.length == 1)
            return toValue(ss[0], bigEndian);

        return ss;
    } 

    @Override
    public Object toValue(int[] is, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toValue(float[] fs, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toValue(double[] ds, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toValue(Date[] ds, TimeZone tz, DatePrecision precision) {
        if (temporalType == null)
            throw new UnsupportedOperationException();

        if (ds == null || ds.length == 0)
            return Value.NULL;

        if (ds.length == 1)
            return temporalType.format(tz, ds[0], precision);

        String[] ss = new String[ds.length];
        for (int i = 0; i < ss.length; i++) {
            ss[i] = temporalType.format(tz, ds[i], precision);
        }
        return ss;
    }

    @Override
    public boolean prompt(Object val, boolean bigEndian,
            SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        if (val instanceof byte[])
            return prompt(cs(cs).decode((byte[]) val), maxChars, sb);

        if (val instanceof String)
            return prompt((String) val, maxChars, sb);

        if (val instanceof String[])
            return prompt((String[]) val, maxChars, sb);

        return prompt(val.toString(), maxChars, sb);
    }

    static boolean prompt(String s, int maxChars, StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        sb.append(s.trim());
        if (sb.length() > maxLength) {
            sb.setLength(maxLength+1);
            return false;
        }
        return true;
    }

    static boolean prompt(String[] ss, int maxChars,
            StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        for (String s : ss) {
            if (s != null)
                sb.append(s);
            if (sb.length() > maxLength) {
                sb.setLength(maxLength+1);
                return false;
            }
            sb.append('\\');
        }
        sb.setLength(sb.length()-1);
        return true;
    }

    @Override
    public int vmOf(Object val) {
        if (val instanceof String)
            return 1;

        if (val instanceof String[]) {
            String[] ss = (String[]) val;
            return ss.length;
        }

        throw new UnsupportedOperationException();
    }
}
