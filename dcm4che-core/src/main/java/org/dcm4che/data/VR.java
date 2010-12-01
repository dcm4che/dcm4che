package org.dcm4che.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public enum VR {
    AE(0x4145, 8, ' ', null, StringType.ASCII),
    AS(0x4153, 8, ' ', null, StringType.ASCII),
    AT(0x4154, 8, 0, BinaryType.TAG, null),
    CS(0x4353, 8, ' ', null, StringType.ASCII),
    DA(0x4441, 8, ' ', null, StringType.ASCII),
    DS(0x4453, 8, ' ', null, StringType.ASCII) {

        @Override
        void checkSupportFloats() { }

        @Override
        float toFloat(Object val, boolean bigEndian, int valueIndex,
                float defVal) {
            if (val instanceof String)
                return valueIndex == 0 ? Float.parseFloat((String) val)
                        : defVal;
            String[] ss = (String[]) val;
            return valueIndex < ss.length
                    ? Float.parseFloat(ss[valueIndex])
                    : defVal; 
        }

        @Override
        float[] toFloats(Object val, boolean bigEndian) {
            if (val instanceof String)
                return new float[] { Float.parseFloat((String) val) };
            String[] ss = (String[]) val;
            float[] floats = new float[ss.length];
            for (int i = 0; i < ss.length; i++)
                floats[i] = Float.parseFloat(ss[i]);
            return floats;
        }

        @Override
        double toDouble(Object val, boolean bigEndian, int valueIndex,
                double defVal) {
            if (val instanceof String)
                return valueIndex == 0 ? Double.parseDouble((String) val)
                        : defVal;
            String[] ss = (String[]) val;
            return valueIndex < ss.length
                    ? Double.parseDouble(ss[valueIndex])
                    : defVal; 
        }

        @Override
        double[] toDoubles(Object val, boolean bigEndian) {
            if (val instanceof String)
                return new double[] { Double.parseDouble((String) val) };
            String[] ss = (String[]) val;
            double[] doubles = new double[ss.length];
            for (int i = 0; i < ss.length; i++)
                doubles[i] = Double.parseDouble(ss[i]);
            return doubles;
        }

        @Override
        Object toValue(float[] floats, boolean bigEndian) {
            String s = formatDS(floats[0]);
            if (floats.length == 1)
                return s;
            String[] ss = new String[floats.length];
            ss[0] = s;
            for (int i = 1; i < floats.length; i++)
                ss[i] = formatDS(floats[i]);
            return ss;
        }

        @Override
        Object toValue(double[] doubles, boolean bigEndian) {
            String s = formatDS(doubles[0]);
            if (doubles.length == 1)
                return s;
            String[] ss = new String[doubles.length];
            ss[0] = s;
            for (int i = 1; i < doubles.length; i++)
                ss[i] = formatDS(doubles[i]);
            return ss;
        }
    },
    DT(0x4454, 8, ' ', null, StringType.ASCII),
    FD(0x4644, 8, 0, BinaryType.DOUBLE, null) {

        @Override
        void checkSupportInts() {
            throw unsupported();
        }

        @Override
        void checkSupportFloats() {}
    },
    FL(0x464c, 8, 0, BinaryType.FLOAT, null) {

        @Override
        void checkSupportInts() {
            throw unsupported();
        }

        @Override
        void checkSupportFloats() {}
    },
    IS(0x4953, 8, ' ', null, StringType.ASCII) {

        @Override
        void checkSupportInts() { }

        @Override
        int toInt(Object val, boolean bigEndian, int valueIndex, int defVal) {
            if (val instanceof String)
                return valueIndex == 0 ? parseIS((String) val) : defVal;

            String[] ss = (String[]) val;
            return valueIndex < ss.length ? parseIS(ss[valueIndex]) : defVal; 
        }

        @Override
        int[] toInts(Object val, boolean bigEndian) {
            if (val instanceof String)
                return new int[] { parseIS((String) val) };

            String[] ss = (String[]) val;
            int[] floats = new int[ss.length];
            for (int i = 0; i < ss.length; i++)
                floats[i] = parseIS(ss[i]);

            return floats;
        }

        @Override
        Object toValue(int[] ints, boolean bigEndian) {
            String s = Integer.toString(ints[0]);
            if (ints.length == 1)
                return s;
            String[] ss = new String[ints.length];
            ss[0] = s;
            for (int i = 1; i < ints.length; i++)
                ss[i] = Integer.toString(ints[i]);
            return ss;
        }
    },
    LO(0x4c4f, 8, ' ', null, StringType.STRING),
    LT(0x4c54, 8, ' ', null, StringType.TEXT) {

        @Override
        void checkSupportStrings() {
            throw unsupported();
        }
    },
    OB(0x4f42, 12, 0, BinaryType.BYTE, null),
    OF(0x4f46, 12, 0, BinaryType.FLOAT, null) {

        @Override
        void checkSupportInts() {
            throw unsupported();
        }

        @Override
        void checkSupportFloats() {}
    },
    OW(0x4f57, 12, 0, BinaryType.SHORT, null),
    PN(0x504e, 8, ' ', null, StringType.PN),
    SH(0x5348, 8, ' ', null, StringType.STRING),
    SL(0x534c, 8, 0, BinaryType.INT, null),
    SQ(0x5351, 12, 0, null, null){

        @Override
        void checkSupportBytes() {
            throw unsupported();
        }

        @Override
        void checkSupportString() {
            throw unsupported();
        }

        @Override
        void checkSupportStrings() {
            throw unsupported();
        }
    },
    SS(0x5353, 8, 0, BinaryType.SHORT,null),
    ST(0x5354, 8, ' ', null, StringType.TEXT) {

        @Override
        void checkSupportStrings() {
            throw unsupported();
        }
    },
    TM(0x544d, 8, ' ', null, StringType.ASCII),
    UI(0x5549, 8, 0, null, StringType.UI),
    UL(0x554c, 8, 0, BinaryType.INT, null),
    UN(0x554e, 12, 0, BinaryType.BYTE, null),
    US(0x5553, 8, 0, BinaryType.USHORT, null),
    UT(0x5554, 12, ' ', null, StringType.TEXT) {

        @Override
        void checkSupportStrings() {
            throw unsupported();
        }
    };

    private static Logger LOG = LoggerFactory.getLogger(VR.class);

    protected final int code;
    protected final int headerLength;
    protected final int paddingByte;
    protected final BinaryType binaryType;
    protected final StringType stringType;

    VR(int code, int headerLength, int paddingByte, BinaryType binaryType,
            StringType stringType) {
        this.code = code;
        this.headerLength = headerLength;
        this.paddingByte = paddingByte;
        this.binaryType = binaryType;
        this.stringType = stringType;
    }

    public int code() {
        return code;
    }

    public int headerLength() {
        return headerLength;
    }

    public static VR valueOf(int code) {
        switch (code) {
        case 0x4145:
            return AE;
        case 0x4153:
            return AS;
        case 0x4154:
            return AT;
        case 0x4353:
            return CS;
        case 0x4441:
            return DA;
        case 0x4453:
            return DS;
        case 0x4454:
            return DT;
        case 0x4644:
            return FD;
        case 0x464c:
            return FL;
        case 0x4953:
            return IS;
        case 0x4c4f:
            return LO;
        case 0x4c54:
            return LT;
        case 0x4f42:
            return OB;
        case 0x4f46:
            return OF;
        case 0x4f57:
            return OW;
        case 0x504e:
            return PN;
        case 0x5348:
            return SH;
        case 0x534c:
            return SL;
        case 0x5351:
            return SQ;
        case 0x5353:
            return SS;
        case 0x5354:
            return ST;
        case 0x544d:
            return TM;
        case 0x5549:
            return UI;
        case 0x554c:
            return UL;
        case 0x554e:
            return UN;
        case 0x5553:
            return US;
        case 0x5554:
            return UT;
        }
        LOG.warn("Unrecogniced VR code: {0}H - treat as UN",
                Integer.toHexString(code));
        return UN;
    }

    byte[] toBytes(Object val, boolean bigEndian, SpecificCharacterSet cs) {
        if (val instanceof byte[])
            return (byte[]) val;

        if (val instanceof String)
            return toBytes((String) val, bigEndian, cs);

        if (val instanceof String[])
            return toBytes((String[]) val, bigEndian, cs);

        throw unsupported();
    }

    private byte[] toBytes(String s, boolean bigEndian,
            SpecificCharacterSet cs) {
        return isBinaryType()
                ? binaryType.stringToBytes(s, bigEndian)
                : stringType.toBytes(s, cs);
    }

    private byte[] toBytes(String[] ss, boolean bigEndian,
            SpecificCharacterSet cs) {
        return isBinaryType()
                ? binaryType.stringsToBytes(ss, bigEndian)
                : stringType.toBytes(ss, cs);
    }

    Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs) {
        if (val instanceof String || val instanceof String[])
            return val;

        if (val instanceof byte[])
            return toStrings((byte[]) val, bigEndian, cs);

        throw unsupported();
    }

    String toString(Object val, boolean bigEndian, SpecificCharacterSet cs,
            int valueIndex, String defVal) {

        if (val instanceof String)
            return valueIndex == 0 ? (String) val : defVal;

        if (val instanceof String[]) {
            String[] ss = (String[]) val;
            return valueIndex < ss.length ? ss[valueIndex] : defVal;
        }

        if (val instanceof byte[])
            return toString((byte[]) val, bigEndian, cs, valueIndex, defVal);

        throw unsupported();
    }

    String toString(byte[] b, boolean bigEndian, SpecificCharacterSet cs,
            int valueIndex, String defVal) {
        return isBinaryType()
                ? binaryType.bytesToString(b, bigEndian, valueIndex, defVal)
                : toString(stringType.toStrings(b, cs), bigEndian, cs,
                        valueIndex, defVal);
    }

    Object toStrings(byte[] b, boolean bigEndian, SpecificCharacterSet cs) {
        return isBinaryType()
                ? binaryType.bytesToStrings(b, bigEndian)
                : stringType.toStrings(b, cs);
        }

    int toInt(Object val, boolean bigEndian, int valueIndex, int defVal) {
        checkSupportInts();
        if (val instanceof Fragments)
            throw unsupported();
        return binaryType.bytesToInt((byte[]) val, bigEndian, valueIndex, 
                defVal);
    }

    int[] toInts(Object val, boolean bigEndian) {
        checkSupportInts();
        if (val instanceof Fragments)
            throw unsupported();
        return binaryType.bytesToInts((byte[]) val, bigEndian);
    }

    float toFloat(Object  val, boolean bigEndian, int valueIndex, float defVal) {
        checkSupportFloats();
        return binaryType.bytesToFloat((byte[]) val, bigEndian, valueIndex, defVal);
    }

    float[] toFloats(Object val, boolean bigEndian) {
        checkSupportFloats();
        return binaryType.bytesToFloats((byte[]) val, bigEndian);
    }

    double toDouble(Object val, boolean bigEndian, int valueIndex,
            double defVal) {
        checkSupportFloats();
        return binaryType.bytesToDouble((byte[]) val, bigEndian, valueIndex, defVal);
    }

    double[] toDoubles(Object val, boolean bigEndian) {
        checkSupportFloats();
        return binaryType.bytesToDoubles((byte[]) val, bigEndian);
    }

    private boolean isBinaryType() {
        return binaryType != null;
    }

    public boolean isStringType() {
        return stringType != null;
    }

    public byte[] toggleEndian(byte[] b, boolean preserve) {
        return isBinaryType() ? binaryType.toggleEndian(b, preserve) : b;
    }

    UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("VR:" + this);
    }

    void checkSupportBytes() { }

    void checkSupportString() { }

    void checkSupportStrings() { }

    void checkSupportInts() {
        if (binaryType == null)
            throw unsupported();
    }

    void checkSupportFloats() {
        throw unsupported();
    }

    Object toValue(byte[] b) {
        checkSupportBytes();
        return b != null && b.length > 0 ? b : null;
    }

    Object toValue(String s, boolean bigEndian) {
        checkSupportString();
        if (s == null || s.length() == 0)
            return null;
        if (isBinaryType())
            return binaryType.stringToBytes(s, bigEndian);
        return s;
    }

    Object toValue(String[] ss, boolean bigEndian) {
        checkSupportStrings();
        if (ss == null || ss.length == 0)
            return null;
        if (ss.length == 1)
            return toValue(ss[0], bigEndian);
        if (isBinaryType())
            return binaryType.stringsToBytes(ss, bigEndian);
        return ss;
    }

    Object toValue(int[] ints, boolean bigEndian) {
        checkSupportInts();
        if (ints == null || ints.length == 0)
            return null;
        return binaryType.intsToBytes(ints, bigEndian);
    }

    Object toValue(float[] floats, boolean bigEndian) {
        checkSupportFloats();
        if (floats == null || floats.length == 0)
            return null;
        return binaryType.floatsToBytes(floats, bigEndian);
    }

    Object toValue(double[] doubles, boolean bigEndian) {
        checkSupportFloats();
        if (doubles == null || doubles.length == 0)
            return null;
        return binaryType.doublesToBytes(doubles, bigEndian);
   }
    
    static int parseIS(String s) {
        return Integer.parseInt(s.charAt(0) == '+' ? s.substring(1) : s);
    }

    static String formatDS(double d) {
        String s = Double.toString(d);
        int l = s.length();
        if (s.startsWith(".0", l-2))
            return s.substring(0, l-2);
        int skip = l - 16;
        int e = s.indexOf('E', l-5);
        return e < 0 ? (skip > 0 ? s.substring(0, 16) : s)
                : s.startsWith(".0", e-2) ? cut(s, e-2, e)
                : skip > 0 ? cut(s, e-skip, e) : s;
    }

    static String formatDS(float f) {
        String s = Float.toString(f);
        int l = s.length();
        if (s.startsWith(".0", l-2))
            return s.substring(0, l-2);
        int e = s.indexOf('E', l-5);
        return e > 0 && s.startsWith(".0", e-2) ? cut(s, e-2, e)  : s;
    }

    static String cut(String s, int begin, int end) {
        int l = s.length();
        char[] ch = new char[l-(end-begin)];
        s.getChars(0, begin, ch, 0);
        s.getChars(end, l, ch, begin);
        return new String(ch);
    }

    public boolean toStringBuilder(Object val, boolean bigEndian,
            SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        if (val == null)
            return true;
        if (val instanceof byte[])
            return toStringBuilder((byte[]) val, bigEndian, cs, maxChars, sb);
        if (val instanceof String)
            return toStringBuilder((String) val, maxChars, sb);
        if (val instanceof String[])
            return toStringBuilder((String[]) val, maxChars, sb);
        if (val instanceof int[])
            return toStringBuilder((int[]) val, maxChars, sb);
        if (val instanceof float[])
            return toStringBuilder((float[]) val, maxChars, sb);
        if (val instanceof double[])
            return toStringBuilder((double[]) val, maxChars, sb);
         
        sb.append(val);
        return true;
    }

    public boolean toStringBuilder(byte[] b, boolean bigEndian,
            SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        return isBinaryType()
            ? binaryType.prompt(b, bigEndian, maxChars, sb)
            : toStringBuilder(stringType.toString(b, cs), maxChars, sb);

   }

    private static boolean toStringBuilder(String s, int maxChars, 
            StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        sb.append(s.trim());
        if (sb.length() > maxLength) {
            sb.setLength(maxLength+1);
            return false;
        }
        return true;
    }

    private static boolean toStringBuilder(String[] ss, int maxChars,
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

    private static boolean toStringBuilder(int[] ints, int maxChars,
            StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        for (int i = 0; i < ints.length; i++) {
            if (i > 0)
                sb.append('\\');
            sb.append(ints[i]);
            if (sb.length() > maxLength) {
                sb.setLength(maxLength+1);
                return false;
            }
        }
        return true;
    }

    private static boolean toStringBuilder(float[] floats, int maxChars,
            StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        for (int i = 0; i < floats.length; i++) {
            if (i > 0)
                sb.append('\\');
            sb.append(formatDS(floats[i]));
            if (sb.length() > maxLength) {
                sb.setLength(maxLength+1);
                return false;
            }
        }
        return true;
    }

    private static boolean toStringBuilder(double[] doubles, int maxChars,
            StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        for (int i = 0; i < doubles.length; i++) {
            if (i > 0)
                sb.append('\\');
            sb.append(formatDS(doubles[i]));
            if (sb.length() > maxLength) {
                sb.setLength(maxLength+1);
                return false;
            }
        }
        return true;
    }

    public int paddingByte() {
        return paddingByte;
    }
}
