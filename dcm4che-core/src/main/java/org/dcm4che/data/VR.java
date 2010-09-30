package org.dcm4che.data;


public enum VR {
    AE(('A' << 8) + 'E', 8, null, StringType.ASCII),
    AS(('A' << 8) + 'S', 8, null, StringType.ASCII),
    AT(('A' << 8) + 'T', 8, BinaryType.TAG, null),
    CS(('C' << 8) + 'S', 8, null, StringType.ASCII),
    DA(('D' << 8) + 'A', 8, null, StringType.ASCII),
    DS(('D' << 8) + 'S', 8, null, StringType.ASCII) {

        @Override
        public float toFloat(String s) {
            return Float.parseFloat(s);
        }

        @Override
        public float[] toFloats(String s) {
            String[] strings = splitStringValue(s);
            float[] floats = new float[strings.length];
            for (int i = 0; i < strings.length; i++)
                floats[i] = Float.parseFloat(strings[i]);
            return floats;
        }

        @Override
        public double toDouble(String s) {
            return Float.parseFloat(s);
        }

        @Override
        public double[] toDoubles(String s) {
            String[] strings = splitStringValue(s);
            double[] doubles = new double[strings.length];
            for (int i = 0; i < strings.length; i++)
                doubles[i] = Float.parseFloat(strings[i]);
            return doubles;
        }

        @Override
        public Object toValue(float f, boolean bigEndian) {
            return Float.toString(f);
        }

        @Override
        public Object toValue(float[] floats, boolean bigEndian) {
            if (floats == null || floats.length == 0)
                return null;
            String[] ss = new String[floats.length];
            for (int i = 0; i < floats.length; i++)
                ss[i] = Float.toString(floats[i]);
            return toValue(ss);
        }

        @Override
        public Object toValue(double d, boolean bigEndian) {
            return Float.toString((float) d);
        }

        @Override
        public Object toValue(double[] doubles, boolean bigEndian) {
            if (doubles == null || doubles.length == 0)
                return null;
            String[] strings = new String[doubles.length];
            for (int i = 0; i < doubles.length; i++)
                strings[i] = Float.toString((float) doubles[i]);
            return toValue(strings);
        }

    },
    DT(('D' << 8) + 'T', 8, null, StringType.ASCII),
    FD(('F' << 8) + 'D', 8, BinaryType.DOUBLE, null),
    FL(('F' << 8) + 'L', 8, BinaryType.FLOAT, null),
    IS(('I' << 8) + 'S', 8, null, StringType.ASCII){

        @Override
        public int toInt(String s) {
            return parseInt(firstStringValue(s));
        }

        @Override
        public int[] toInts(String s) {
            String[] strings = splitStringValue(s);
            int[] ints = new int[strings.length];
            for (int i = 0; i < strings.length; i++)
                ints[i] = parseInt(strings[i]);
            return ints;
        }

        private int parseInt(String s) {
            return Integer.parseInt(s.charAt(0) == '+' ? s.substring(1) : s);
        }

        @Override
        public Object toValue(int i, boolean bigEndian) {
            return Integer.toString(i);
        }

        @Override
        public Object toValue(int[] ints, boolean bigEndian) {
            if (ints == null || ints.length == 0)
                return null;
            String[] ss = new String[ints.length];
            for (int i = 0; i < ints.length; i++)
                ss[i] = Integer.toString(ints[i]);
            return toValue(ss);
        }

    },
    LO(('L' << 8) + 'O', 8, null, StringType.STRING),
    LT(('L' << 8) + 'T', 8, null, StringType.TEXT),
    OB(('O' << 8) + 'B', 12, BinaryType.BYTE, null),
    OF(('O' << 8) + 'F', 12, BinaryType.FLOAT, null),
    OW(('O' << 8) + 'W', 12, BinaryType.USHORT, null),
    PN(('P' << 8) + 'N', 8, null, StringType.PN),
    SH(('S' << 8) + 'H', 8, null, StringType.STRING),
    SL(('S' << 8) + 'L', 8, BinaryType.INT, null),
    SQ(('S' << 8) + 'Q', 12, null, null) {
        @Override
        public Object toValue(byte[] value) {
            throw new UnsupportedOperationException("VR:" + this);
        }
    },
    SS(('S' << 8) + 'S', 8, BinaryType.SHORT,null),
    ST(('S' << 8) + 'T', 8, null, StringType.TEXT),
    TM(('T' << 8) + 'M', 8, null, StringType.ASCII),
    UI(('U' << 8) + 'I', 8, null, StringType.UI),
    UL(('U' << 8) + 'L', 8, BinaryType.INT, null),
    UN(('U' << 8) + 'N', 12, null, null),
    US(('U' << 8) + 'S', 8, BinaryType.USHORT, null),
    UT(('U' << 8) + 'T', 12, null, StringType.TEXT);

    private final int code;
    private final int headerLength;
    private final BinaryType binaryType;
    private final StringType stringType;

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
        case ('A' << 8) + 'E':
            return AE;
        case ('A' << 8) + 'S':
            return AS;
        case ('A' << 8) + 'T':
            return AT;
        case ('C' << 8) + 'S':
            return CS;
        case ('D' << 8) + 'A':
            return DA;
        case ('D' << 8) + 'S':
            return DS;
        case ('D' << 8) + 'T':
            return DT;
        case ('F' << 8) + 'D':
            return FD;
        case ('F' << 8) + 'L':
            return FL;
        case ('I' << 8) + 'S':
            return IS;
        case ('L' << 8) + 'O':
            return LO;
        case ('L' << 8) + 'T':
            return LT;
        case ('O' << 8) + 'B':
            return OB;
        case ('O' << 8) + 'F':
            return OF;
        case ('O' << 8) + 'W':
            return OW;
        case ('P' << 8) + 'N':
            return PN;
        case ('S' << 8) + 'H':
            return SH;
        case ('S' << 8) + 'L':
            return SL;
        case ('S' << 8) + 'Q':
            return SQ;
        case ('S' << 8) + 'S':
            return SS;
        case ('S' << 8) + 'T':
            return ST;
        case ('T' << 8) + 'M':
            return TM;
        case ('U' << 8) + 'I':
            return UI;
        case ('U' << 8) + 'L':
            return UL;
        case ('U' << 8) + 'N':
            return UN;
        case ('U' << 8) + 'S':
            return US;
        case ('U' << 8) + 'T':
            return UT;
        }
        throw new IllegalArgumentException(
                String.format("Illegal VR code: %04XH", code));
    }

    public byte[] toBytes(String value, SpecificCharacterSet cs) {
        checkStringType();
        return stringType.toBytes(value, cs);
    }

    public String toString(byte[] value, SpecificCharacterSet cs) {
        checkStringType();
        return stringType.toString(value, cs);
    }

    public String firstStringValue(String s) {
        checkStringType();
        return stringType.first(s);
    }

    public String[] splitStringValue(String s) {
        checkStringType();
        return stringType.split(s);
    }

    public byte[] toBytes(int i, boolean bigEndian) {
        checkBinaryType();
        return binaryType.intToBytes(i, bigEndian);
    }

    public byte[] toBytes(int[] intarr, boolean bigEndian) {
        checkBinaryType();
        return binaryType.intsToBytes(intarr, bigEndian);
    }

    public byte[] toBytes(float f, boolean bigEndian) {
        checkBinaryType();
        return binaryType.floatToBytes(f, bigEndian);
    }

    public byte[] toBytes(float[] floatarr, boolean bigEndian) {
        checkBinaryType();
        return binaryType.floatsToBytes(floatarr, bigEndian);
    }

    public byte[] toBytes(double d, boolean bigEndian) {
        checkBinaryType();
        return binaryType.doubleToBytes(d, bigEndian);
    }

    public byte[] toBytes(double[] doublearr, boolean bigEndian) {
        checkBinaryType();
        return binaryType.doublesToBytes(doublearr, bigEndian);
    }

    public int toInt(byte[] value, boolean bigEndian) {
        checkBinaryType();
        return binaryType.bytesToInt(value, 0, bigEndian);
    }

    public int[] toInts(byte[] value, boolean bigEndian) {
        checkBinaryType();
        return binaryType.bytesToInts(value, bigEndian);
    }

    public int toInt(String s) {
        throw new UnsupportedOperationException("VR:" + this);
    }

    public int[] toInts(String s) {
        throw new UnsupportedOperationException("VR:" + this);
    }

    public float toFloat(byte[] value, boolean bigEndian) {
        checkBinaryType();
        return binaryType.bytesToFloat(value, 0, bigEndian);
    }

    public float[] toFloats(byte[] value, boolean bigEndian) {
        checkBinaryType();
        return binaryType.bytesToFloats(value, bigEndian);
    }

    public float toFloat(String string) {
        throw new UnsupportedOperationException("VR:" + this);
    }

    public float[] toFloats(String string) {
        throw new UnsupportedOperationException("VR:" + this);
    }

    public double toDouble(String string) {
        throw new UnsupportedOperationException("VR:" + this);
    }

    public double[] toDoubles(byte[] value, boolean bigEndian) {
        checkBinaryType();
        return binaryType.bytesToDoubles(value, bigEndian);
    }

    public double[] toDoubles(String string) {
        throw new UnsupportedOperationException("VR:" + this);
    }

    public double toDouble(byte[] value, boolean bigEndian) {
        checkBinaryType();
        return binaryType.bytesToDouble(value, 0, bigEndian);
    }

    public void checkStringType() {
        if (stringType == null)
            throw new UnsupportedOperationException("VR:" + this);
    }

    public void checkBinaryType() {
        if (binaryType == null)
            throw new UnsupportedOperationException("VR:" + this);
    }

    public Object toValue(byte[] val) {
        if (val == null || val.length == 0)
            return null;
        return val;
    }

    public Object toValue(String val) {
        checkStringType();
        if (val == null || val.length() == 0)
            return null;
        return val;
    }

    public Object toValue(String[] val) {
        checkStringType();
        if (val == null || val.length == 0)
            return null;
        return stringType.join(val);
    }

    public Object toValue(int val, boolean bigEndian) {
        checkBinaryType();
        return binaryType.intToBytes(val, bigEndian);
    }

    public Object toValue(int[] val, boolean bigEndian) {
        checkBinaryType();
        if (val == null || val.length == 0)
            return null;
        return binaryType.intsToBytes(val, bigEndian);
    }

    public Object toValue(float f, boolean bigEndian) {
        checkBinaryType();
        return binaryType.floatToBytes(f, bigEndian);
    }

    public Object toValue(float[] val, boolean bigEndian) {
        checkBinaryType();
        if (val == null || val.length == 0)
            return null;
        return binaryType.floatsToBytes(val, bigEndian);
    }

    public Object toValue(double d, boolean bigEndian) {
        checkBinaryType();
        return binaryType.doubleToBytes(d, bigEndian);
    }

    public Object toValue(double[] val, boolean bigEndian) {
        checkBinaryType();
        if (val == null || val.length == 0)
            return null;
        return binaryType.doublesToBytes(val, bigEndian);
    }

}
