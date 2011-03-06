package org.dcm4che.data;

import org.dcm4che.util.StringUtils;

enum StringValueType implements ValueType {
    ASCII("\\") {

        @Override
        protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
            return SpecificCharacterSet.DEFAULT;
        }
    },
    STRING("\\"),
    TEXT("\n\f\r") {

        @Override
        protected Object splitAndTrim(String s) {
            return StringUtils.trimTrailing(s);
        }
    },
    PN("^=\\"),
    DS("\\") {

        @Override
        protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
            return SpecificCharacterSet.DEFAULT;
        }

        @Override
        public float toFloat(Object val, boolean bigEndian, int valueIndex,
                float defVal) {
            if (val instanceof String) {
                return valueIndex == 0
                    ? Float.parseFloat((String) val)
                    : defVal;
            }
            if (val instanceof String[]) {
                String[] ss = (String[]) val;
                return (valueIndex < ss.length && ss[valueIndex] != null)
                    ? Float.parseFloat(ss[valueIndex])
                    : defVal;
            }
            throw new UnsupportedOperationException();
        } 

        @Override
        public float[] toFloats(Object val, boolean bigEndian) {
            if (val instanceof String) {
                return new float[] { Float.parseFloat((String) val) };
            }
            if (val instanceof String[]) {
                String[] ss = (String[]) val;
                float[] fs = new float[ss.length];
                for (int i = 0; i < fs.length; i++) {
                    if (ss[i] != null)
                        fs[i] = Float.parseFloat(ss[i]);
                }
                return fs;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public double toDouble(Object val, boolean bigEndian, int valueIndex,
                double defVal) {
            if (val instanceof String) {
                return valueIndex == 0
                    ? Double.parseDouble((String) val)
                    : defVal;
            }
            if (val instanceof String[]) {
                String[] ss = (String[]) val;
                return (valueIndex < ss.length && ss[valueIndex] != null)
                    ? Double.parseDouble(ss[valueIndex])
                    : defVal;
            }
            throw new UnsupportedOperationException();
        } 

        @Override
        public double[] toDoubles(Object val, boolean bigEndian) {
            if (val instanceof String) {
                return new double[] { Double.parseDouble((String) val) };
            }
            if (val instanceof String[]) {
                String[] ss = (String[]) val;
                double[] ds = new double[ss.length];
                for (int i = 0; i < ds.length; i++) {
                    if (ss[i] != null)
                        ds[i] = Double.parseDouble(ss[i]);
                }
                return ds;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public Object toValue(float[] fs, boolean bigEndian) {
            if (fs == null || fs.length == 0)
                return Value.NULL;

            if (fs.length == 1)
                return StringUtils.formatDS(fs[0]);

            String[] ss = new String[fs.length];
            for (int i = 0; i < ss.length; i++) {
                ss[i] = StringUtils.formatDS(fs[i]);
            }
            return ss;
        } 

        @Override
        public Object toValue(double[] ds, boolean bigEndian) {
            if (ds == null || ds.length == 0)
                return Value.NULL;

            if (ds.length == 1)
                return StringUtils.formatDS(ds[0]);

            String[] ss = new String[ds.length];
            for (int i = 0; i < ss.length; i++) {
                ss[i] = StringUtils.formatDS(ds[i]);
            }
            return ss;
        } 
    },
    IS("\\") {

        @Override
        protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
            return SpecificCharacterSet.DEFAULT;
        }

        @Override
        public int toInt(Object val, boolean bigEndian, int valueIndex,
                int defVal) {
            if (val instanceof String) {
                return valueIndex == 0
                    ? StringUtils.parseIS((String) val)
                    : defVal;
            }
            if (val instanceof String[]) {
                String[] ss = (String[]) val;
                return (valueIndex < ss.length && ss[valueIndex] != null)
                    ? StringUtils.parseIS(ss[valueIndex])
                    : defVal;
            }
            throw new UnsupportedOperationException();
        } 

        @Override
        public int[] toInts(Object val, boolean bigEndian) {
            if (val instanceof String) {
                return new int[] { StringUtils.parseIS((String) val) };
            }
            if (val instanceof String[]) {
                String[] ss = (String[]) val;
                int[] is = new int[ss.length];
                for (int i = 0; i < is.length; i++) {
                    if (ss[i] != null)
                        is[i] = StringUtils.parseIS(ss[i]);
                }
                return is;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public Object toValue(int[] is, boolean bigEndian) {
            if (is == null || is.length == 0)
                return Value.NULL;

            if (is.length == 1)
                return Integer.toString(is[0]);

            String[] ss = new String[is.length];
            for (int i = 0; i < ss.length; i++) {
                ss[i] = Integer.toString(is[i]);
            }
            return ss;
        } 
    };

    final String delimiters; 

    StringValueType(String delimiters) {
        this.delimiters = delimiters;
    }

    @Override
    public int numEndianBytes() {
        return 1;
    }

    @Override
    public byte[] toggleEndian(byte[] b, boolean preserve) {
        return b;
    }

    protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
        return cs;
    }

    @Override
    public byte[] toBytes(Object val, SpecificCharacterSet cs) {

        if (val instanceof byte[])
            return (byte[]) val;

        if (val instanceof String)
            return cs(cs).encode((String) val, delimiters);

        if (val instanceof String[])
            return cs(cs).encode(
                    StringUtils.join((String[]) val, '\\'), delimiters);

        throw new UnsupportedOperationException();
    } 

    @Override
    public String toString(Object val, boolean bigEndian, int valueIndex,
            String defVal) {

        if (val instanceof String)
            return (String) (valueIndex == 0 ? val : null);

        if (val instanceof String[]) {
            String[] ss = (String[]) val;
            return valueIndex < ss.length ? ss[valueIndex] : null;
        }

        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toStrings(Object val, boolean bigEndian,
            SpecificCharacterSet cs) {

        if (val instanceof byte[]) {
            return splitAndTrim(cs(cs).decode((byte[]) val));
        }

        if (val instanceof String
                || val instanceof String[])
            return val;

        throw new UnsupportedOperationException();
    } 

    protected Object splitAndTrim(String s) {
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
        for (int i = 0; i < ss.length; i++) {
            if (i > 0)
                sb.append('\\');
            sb.append(ss[i]);
            if (sb.length() > maxLength) {
                sb.setLength(maxLength+1);
                return false;
            }
        }
        return true;
    }
}
