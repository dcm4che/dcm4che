package org.dcm4che.data;


public enum VR {
    AE(0x4145, 8, null, StringType.ASCII),
    AS(0x4153, 8, null, StringType.ASCII),
    AT(0x4154, 8, BinaryType.TAG, null),
    CS(0x4353, 8, null, StringType.ASCII),
    DA(0x4441, 8, null, StringType.ASCII),
    DS(0x4453, 8, null, StringType.ASCII) {

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
            return stringType.join(ss);
        }

        @Override
        public Object toValue(double d, boolean bigEndian) {
            return Float.toString((float) d);
        }

        @Override
        public Object toValue(double[] doubles, boolean bigEndian) {
            if (doubles == null || doubles.length == 0)
                return null;
            String[] ss = new String[doubles.length];
            for (int i = 0; i < doubles.length; i++)
                ss[i] = Float.toString((float) doubles[i]);
            return stringType.join(ss);
        }

    },
    DT(0x4454, 8, null, StringType.ASCII),
    FD(0x4644, 8, BinaryType.DOUBLE, null),
    FL(0x464c, 8, BinaryType.FLOAT, null),
    IS(0x4953, 8, null, StringType.ASCII){

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
            return stringType.join(ss);
        }

    },
    LO(0x4c4f, 8, null, StringType.STRING),
    LT(0x4c54, 8, null, StringType.TEXT),
    OB(0x4f42, 12, BinaryType.BYTE, null),
    OF(0x4f46, 12, BinaryType.FLOAT, null),
    OW(0x4f57, 12, BinaryType.SHORT, null),
    PN(0x504e, 8, null, StringType.PN),
    SH(0x5348, 8, null, StringType.STRING),
    SL(0x534c, 8, BinaryType.INT, null),
    SQ(0x5351, 12, null, null) {
        @Override
        public Object toValue(byte[] value) {
            throw new UnsupportedOperationException("VR:" + this);
        }

        @Override
        public Object toValue(String val, boolean bigEndian) {
            throw new UnsupportedOperationException("VR:" + this);
        }

        @Override
        public Object toValue(String[] val, boolean bigEndian) {
            throw new UnsupportedOperationException("VR:" + this);
           
        }
   },
    SS(0x5353, 8, BinaryType.SHORT,null),
    ST(0x5354, 8, null, StringType.TEXT),
    TM(0x544d, 8, null, StringType.ASCII),
    UI(0x5549, 8, null, StringType.UI),
    UL(0x554c, 8, BinaryType.INT, null),
    UN(0x554e, 12, null, null),
    US(0x5553, 8, BinaryType.USHORT, null),
    UT(0x5554, 12, null, StringType.TEXT);

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

    public String firstBinaryValueAsString(byte[] value, boolean bigEndian) {
        checkBinaryType();
        return binaryType.bytesToString(value, 0, bigEndian);
    }

    public String[] splitStringValue(String s) {
        checkStringType();
        return stringType.split(s);
    }

    public String[] binaryValueAsStrings(byte[] value, boolean bigEndian) {
        checkBinaryType();
        return binaryType.bytesToStrings(value, bigEndian);
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

    boolean isStringType() {
        return stringType != null;
    }

    boolean isBinaryType() {
        return binaryType != null;
    }

    void checkStringType() {
        if (stringType == null)
            throw new UnsupportedOperationException("VR:" + this);
    }

    void checkBinaryType() {
        if (binaryType == null)
            throw new UnsupportedOperationException("VR:" + this);
    }

    public Object toValue(byte[] val) {
        if (val == null || val.length == 0)
            return null;
        return val;
    }

    public Object toValue(String val, boolean bigEndian) {
        if (val == null || val.length() == 0)
            return null;
        if (isBinaryType())
            return binaryType.stringToBytes(val, bigEndian);
        return val;
    }

    public Object toValue(String[] val, boolean bigEndian) {
        if (val == null || val.length == 0)
            return null;
        if (isBinaryType())
            return binaryType.stringsToBytes(val, bigEndian);
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

    public void toggleEndian(byte[] value) {
        if (isBinaryType())
            binaryType.toggleEndian(value);
    }

    public void toggleEndian(Object value) {
        if (value instanceof byte[])
            toggleEndian((byte[]) value);
    }
}
