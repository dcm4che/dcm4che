package org.dcm4che.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public enum VR {
    AE(0x4145, 8, null, StringType.ASCII),
    AS(0x4153, 8, null, StringType.ASCII),
    AT(0x4154, 8, BinaryType.TAG, null),
    CS(0x4353, 8, null, StringType.ASCII),
    DA(0x4441, 8, null, StringType.ASCII),
    DS(0x4453, 8, null, StringType.ASCII) {

        @Override
        void checkSupportFloats() { }

        @Override
        protected byte[] floatsToBytes(float[] floats, boolean bigEndian) {
            String s = formatDS(floats[0]);
            if (floats.length > 1) {
                StringBuilder sb = new StringBuilder(floats.length << 4);
                sb.append(s);
                for (int i = 1; i < floats.length; i++)
                    sb.append('\\').append(floats[i]);
                s = sb.toString();
            }
            return SpecificCharacterSet.DEFAULT.encode(s, null);
        }

        @Override
        protected byte[] doublesToBytes(double[] doubles, boolean bigEndian) {
            String s = formatDS(doubles[0]);
            if (doubles.length > 1) {
                StringBuilder sb = new StringBuilder(doubles.length << 4);
                sb.append(s);
                for (int i = 1; i < doubles.length; i++)
                    sb.append('\\').append(doubles[i]);
                s = sb.toString();
            }
            return SpecificCharacterSet.DEFAULT.encode(s, null);
        }

        @Override
        protected float[] bytesToFloats(byte[] b, boolean bigEndian) {
            Object o = stringType.toStrings(b, null);
            return (o instanceof String) 
                    ? stringToFloats((String) o)
                    : stringsToFloats((String[]) o);
        }

        @Override
        protected double[] bytesToDoubles(byte[] b, boolean bigEndian) {
            Object o = stringType.toStrings(b, null);
            return (o instanceof String) 
                    ? stringToDoubles((String) o)
                    : stringsToDoubles((String[]) o);
        }
    },
    DT(0x4454, 8, null, StringType.ASCII),
    FD(0x4644, 8, BinaryType.DOUBLE, null) {

        @Override
        void checkSupportInts() {
            throw unsupported();
        }

        @Override
        void checkSupportFloats() {}
    },
    FL(0x464c, 8, BinaryType.FLOAT, null) {

        @Override
        void checkSupportInts() {
            throw unsupported();
        }

        @Override
        void checkSupportFloats() {}
    },
    IS(0x4953, 8, null, StringType.ASCII) {

        @Override
        void checkSupportInts() { }

        @Override
        protected byte[] intsToBytes(int[] ints, boolean bigEndian) {
            String s = Integer.toString(ints[0]);
            if (ints.length > 1) {
                StringBuilder sb = new StringBuilder(ints.length << 4);
                sb.append(s);
                for (int i = 1; i < ints.length; i++)
                    sb.append('\\').append(ints[i]);
                s = sb.toString();
            }
            return SpecificCharacterSet.DEFAULT.encode(s, null);
        }

        @Override
        protected int[] bytesToInts(byte[] b, boolean bigEndian) {
            Object o = stringType.toStrings(b, null);
            return (o instanceof String) 
                    ? stringToInts((String) o)
                    : stringsToInts((String[]) o);
        }
    },
    LO(0x4c4f, 8, null, StringType.STRING),
    LT(0x4c54, 8, null, StringType.TEXT) {

        @Override
        void checkSupportStrings() {
            throw unsupported();
        }
    },
    OB(0x4f42, 12, BinaryType.BYTE, null),
    OF(0x4f46, 12, BinaryType.FLOAT, null) {

        @Override
        void checkSupportInts() {
            throw unsupported();
        }

        @Override
        void checkSupportFloats() {}
    },
    OW(0x4f57, 12, BinaryType.SHORT, null),
    PN(0x504e, 8, null, StringType.PN),
    SH(0x5348, 8, null, StringType.STRING),
    SL(0x534c, 8, BinaryType.INT, null),
    SQ(0x5351, 12, null, null){

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
    SS(0x5353, 8, BinaryType.SHORT,null),
    ST(0x5354, 8, null, StringType.TEXT) {

        @Override
        void checkSupportStrings() {
            throw unsupported();
        }
    },
    TM(0x544d, 8, null, StringType.ASCII),
    UI(0x5549, 8, null, StringType.UI),
    UL(0x554c, 8, BinaryType.INT, null),
    UN(0x554e, 12, BinaryType.BYTE, null),
    US(0x5553, 8, BinaryType.USHORT, null),
    UT(0x5554, 12, null, StringType.TEXT) {

        @Override
        void checkSupportStrings() {
            throw unsupported();
        }
    };

    private static Logger LOG = LoggerFactory.getLogger(VR.class);

    protected final int code;
    protected final int headerLength;
    protected final BinaryType binaryType;
    protected final StringType stringType;

    VR(int code, int headerLength, BinaryType binaryType, StringType stringType) {
        this.code = code;
        this.headerLength = headerLength;
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
            return stringToBytes((String) val, bigEndian, cs);

        if (val instanceof String[])
            return stringsToBytes((String[]) val, bigEndian, cs);

        if (val instanceof int[])
            return intsToBytes((int[]) val, bigEndian);

        if (val instanceof float[])
            return floatsToBytes((float[]) val, bigEndian);

        if (val instanceof double[])
            return doublesToBytes((double[]) val, bigEndian);

        throw unsupported();
    }

    private byte[] stringToBytes(String s, boolean bigEndian,
            SpecificCharacterSet cs) {
        return isBinaryType()
                ? binaryType.stringToBytes(s, bigEndian)
                : stringType.toBytes(s, cs);
    }

    private byte[] stringsToBytes(String[] ss, boolean bigEndian,
            SpecificCharacterSet cs) {
        return isBinaryType()
                ? binaryType.stringsToBytes(ss, bigEndian)
                : stringType.toBytes(ss, cs);
    }

    protected byte[] intsToBytes(int[] ints, boolean bigEndian) {
        return binaryType.intsToBytes(ints, bigEndian);
    }

    protected byte[] floatsToBytes(float[] floats, boolean bigEndian) {
        return binaryType.floatsToBytes(floats, bigEndian);
    }

    protected byte[] doublesToBytes(double[] doubles, boolean bigEndian) {
        return binaryType.doublesToBytes(doubles, bigEndian);
    }

    Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs) {
        if (val instanceof String || val instanceof String[])
            return val;

        if (val instanceof byte[])
            return bytesToString((byte[]) val, bigEndian, cs);

        if (val instanceof int[])
            return intsToString((int[]) val);

        if (val instanceof float[])
            return floatsToString((float[]) val);

        if (val instanceof double[])
            return doublesToString((double[]) val);

        throw unsupported();
    }

    private Object bytesToString(byte[] b, boolean bigEndian,
            SpecificCharacterSet cs) {
        return isBinaryType()
            ? binaryType.bytesToStrings(b, bigEndian)
            : stringType.toStrings(b, cs);
    }

    private Object intsToString(int[] ints) {
        if (ints.length == 1)
            return Integer.toString(ints[0]);

        String[] ss = new String[ints.length];
        for (int i = 0; i < ints.length; i++)
            ss[i] = Integer.toString(ints[i]);
        return ss;
    }

    private Object floatsToString(float[] floats) {
        if (floats.length == 1)
            return formatDS(floats[0]);

        String[] ss = new String[floats.length];
        for (int i = 0; i < floats.length; i++)
            ss[i] = formatDS(floats[i]);
        return ss;
    }

    private Object doublesToString(double[] doubles) {
        if (doubles.length == 1)
            return formatDS(doubles[0]);

        String[] ss = new String[doubles.length];
        for (int i = 0; i < doubles.length; i++)
            ss[i] = formatDS(doubles[i]);
        return ss;
    }

    int[] toInts(Object val, boolean bigEndian) {
        if (val instanceof int[])
            return (int[]) val;

        if (val instanceof byte[])
            return bytesToInts((byte[]) val, bigEndian);

        if (val instanceof String)
            return stringToInts((String) val);

        if (val instanceof String[])
            return stringsToInts((String[]) val);

        throw unsupported();
    }

    protected int[] bytesToInts(byte[] b, boolean bigEndian) {
        checkSupportInts();
        return binaryType.bytesToInts(b, bigEndian);
    }

    protected int[] stringToInts(String s) {
        checkSupportInts();
        return new int[]{ parseIS(s) };
    }

    protected int[] stringsToInts(String[] ss) {
        checkSupportInts();
        int[] ints = new int[ss.length];
        for (int i = 0; i < ss.length; i++)
            ints[i] = parseIS(ss[i]);
        return ints;
    }

    float[] toFloats(Object val, boolean bigEndian) {
        if (val instanceof float[])
            return (float[]) val;

        if (val instanceof double[])
            return doublesToFloats((double[]) val);

        if (val instanceof byte[])
            return bytesToFloats((byte[]) val, bigEndian);

        if (val instanceof String)
            return stringToFloats((String) val);

        if (val instanceof String[])
            return stringsToFloats((String[]) val);

        throw unsupported();
    }

    private float[] doublesToFloats(double[] doubles) {
        float[] floats = new float[doubles.length];
        for (int i = 0; i < doubles.length; i++)
            floats[i] = (float) doubles[i];
        return floats ;
    }

    protected float[] bytesToFloats(byte[] b, boolean bigEndian) {
        checkSupportFloats();
        return binaryType.bytesToFloats(b, bigEndian);
    }

    protected float[] stringToFloats(String s) {
        checkSupportFloats();
        return new float[] { Float.parseFloat(s) };
    }

    protected float[] stringsToFloats(String[] ss) {
        checkSupportFloats();
        float[] floats = new float[ss.length];
        for (int i = 0; i < ss.length; i++)
            floats[i] = Float.parseFloat(ss[i]);
        return floats;
    }

    double[] toDoubles(Object val, boolean bigEndian) {
        if (val instanceof double[])
            return (double[]) val;

        if (val instanceof float[])
            return floatsToDoubles((float[]) val);

        if (val instanceof byte[])
            return bytesToDoubles((byte[]) val, bigEndian);

        if (val instanceof String)
            return stringToDoubles((String) val);

        if (val instanceof String[])
            return stringsToDoubles((String[]) val);

        throw unsupported();
    }

    private double[] floatsToDoubles(float[] floats) {
        double[] doubles = new double[floats.length];
        for (int i = 0; i < floats.length; i++)
            doubles[i] = floats[i];
        return doubles ;
    }

    protected double[] bytesToDoubles(byte[] b, boolean bigEndian) {
        checkSupportFloats();
        return binaryType.bytesToDoubles(b, bigEndian);
    }

    protected double[] stringToDoubles(String s) {
        checkSupportFloats();
        return new double[] { Double.parseDouble(s) };
    }

    protected double[] stringsToDoubles(String[] ss) {
        checkSupportFloats();
        double[] doubles = new double[ss.length];
        for (int i = 0; i < ss.length; i++)
            doubles[i] = Double.parseDouble(ss[i]);
        return doubles;
    }

    private boolean isBinaryType() {
        return binaryType != null;
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

    public boolean promptValue(Object val, boolean bigEndian,
            SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        if (val == null)
            return true;
        if (val instanceof byte[])
            return promptBytes((byte[]) val, bigEndian, cs, maxChars, sb);
        if (val instanceof String)
            return promptString((String) val, maxChars, sb);
        if (val instanceof String[])
            return promptStrings((String[]) val, maxChars, sb);
        if (val instanceof int[])
            return promptInts((int[]) val, maxChars, sb);
        if (val instanceof float[])
            return promptFloats((float[]) val, maxChars, sb);
        if (val instanceof double[])
            return promptDoubles((double[]) val, maxChars, sb);
         
        sb.append(val);
        return true;
    }

    public boolean promptBytes(byte[] b, boolean bigEndian,
            SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        return isBinaryType()
            ? binaryType.prompt(b, bigEndian, maxChars, sb)
            : promptString(stringType.toString(b, cs), maxChars, sb);

   }

    private static boolean promptString(String s, int maxChars, 
            StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        sb.append(s.trim());
        if (sb.length() > maxLength) {
            sb.setLength(maxLength+1);
            return false;
        }
        return true;
    }

    private static boolean promptStrings(String[] ss, int maxChars,
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

    private static boolean promptInts(int[] ints, int maxChars,
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

    private static boolean promptFloats(float[] floats, int maxChars,
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

    private static boolean promptDoubles(double[] doubles, int maxChars,
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

}
