package org.dcm4che.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public enum VR {
    AE(0x4145, 8, ' ', StringValueType.ASCII),
    AS(0x4153, 8, ' ', StringValueType.ASCII),
    AT(0x4154, 8, 0, BinaryValueType.TAG),
    CS(0x4353, 8, ' ', StringValueType.ASCII),
    DA(0x4441, 8, ' ', StringValueType.ASCII),
    DS(0x4453, 8, ' ', StringValueType.DS),
    DT(0x4454, 8, ' ', StringValueType.ASCII),
    FD(0x4644, 8, 0, BinaryValueType.DOUBLE),
    FL(0x464c, 8, 0, BinaryValueType.FLOAT),
    IS(0x4953, 8, ' ', StringValueType.IS),
    LO(0x4c4f, 8, ' ', StringValueType.STRING),
    LT(0x4c54, 8, ' ', StringValueType.TEXT),
    OB(0x4f42, 12, 0, BinaryValueType.BYTE),
    OF(0x4f46, 12, 0, BinaryValueType.FLOAT),
    OW(0x4f57, 12, 0, BinaryValueType.SHORT),
    PN(0x504e, 8, ' ', StringValueType.STRING),
    SH(0x5348, 8, ' ', StringValueType.STRING),
    SL(0x534c, 8, 0, BinaryValueType.INT),
    SQ(0x5351, 12, 0, SequenceValueType.SQ),
    SS(0x5353, 8, 0, BinaryValueType.SHORT),
    ST(0x5354, 8, ' ', StringValueType.TEXT),
    TM(0x544d, 8, ' ', StringValueType.ASCII),
    UI(0x5549, 8, 0, StringValueType.ASCII),
    UL(0x554c, 8, 0, BinaryValueType.INT),
    UN(0x554e, 12, 0, BinaryValueType.BYTE),
    US(0x5553, 8, 0, BinaryValueType.USHORT),
    UT(0x5554, 12, ' ', StringValueType.TEXT);

    private static Logger LOG = LoggerFactory.getLogger(VR.class);

    protected final int code;
    protected final int headerLength;
    protected final int paddingByte;
    protected final ValueType valueType;

    VR(int code, int headerLength, int paddingByte, ValueType valueType) {
        this.code = code;
        this.headerLength = headerLength;
        this.paddingByte = paddingByte;
        this.valueType = valueType;
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

    public int code() {
        return code;
    }

    public int headerLength() {
        return headerLength;
    }

    public int paddingByte() {
        return paddingByte;
    }

    public boolean isStringType() {
        return valueType instanceof StringValueType;
    }

    public int numEndianBytes() {
        return valueType.numEndianBytes();
    }

    public byte[] toggleEndian(byte[] b, boolean preserve) {
        return valueType.toggleEndian(b, preserve);
    }

    public byte[] toBytes(Object val, SpecificCharacterSet cs) {
        return valueType.toBytes(val, cs);
    }

    Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs) {
        return valueType.toStrings(val, bigEndian, cs);
    }

    String toString(Object val, boolean bigEndian, int valueIndex,
            String defVal) {
        return valueType.toString(val, bigEndian, valueIndex, defVal);
    }

    int toInt(Object val, boolean bigEndian, int valueIndex, int defVal) {
        return valueType.toInt(val, bigEndian, valueIndex, defVal);
    }

    int[] toInts(Object val, boolean bigEndian) {
        return valueType.toInts(val, bigEndian);
    }

    float toFloat(Object  val, boolean bigEndian, int valueIndex, float defVal) {
        return valueType.toFloat(val, bigEndian, valueIndex, defVal);
    }

    float[] toFloats(Object val, boolean bigEndian) {
        return valueType.toFloats(val, bigEndian);
    }

    double toDouble(Object val, boolean bigEndian, int valueIndex,
            double defVal) {
        return valueType.toDouble(val, bigEndian, valueIndex, defVal);
    }

    double[] toDoubles(Object val, boolean bigEndian) {
        return valueType.toDoubles(val, bigEndian);
    }

    Object toValue(byte[] b) {
        return valueType.toValue(b);
    }

    Object toValue(String s, boolean bigEndian) {
        return valueType.toValue(s, bigEndian);
    }

    Object toValue(String[] ss, boolean bigEndian) {
        return valueType.toValue(ss, bigEndian);
    }

    Object toValue(int[] is, boolean bigEndian) {
        return valueType.toValue(is, bigEndian);
    }

    Object toValue(float[] fs, boolean bigEndian) {
        return valueType.toValue(fs, bigEndian);
    }

    Object toValue(double[] ds, boolean bigEndian) {
        return valueType.toValue(ds, bigEndian);
    }

    public boolean prompt(Object val, boolean bigEndian,
            SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        return valueType.prompt(val, bigEndian, cs, maxChars, sb);
    }
}
