package org.dcm4che.data;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class VRTest {

    private static final byte[] EMPTY_BYTES = {};
    private static final byte[] BYTE_123 = { -123 };
    private static final byte[] SHORT_12345_BE = { -49, -57 };
    private static final byte[] SHORT_12345_LE = { -57, -49 };
    private static final byte[] INT_12345_BE = { -1, -1, -49, -57 };
    private static final byte[] INT_12345_LE = { -57, -49, -1, -1 };
    private static final byte[] TAG_PIXEL_DATA_BE = { 0x7f, (byte) 0xe0, 0, 0x10 };
    private static final byte[] TAG_PIXEL_DATA_LE = { (byte) 0xe0, 0x7f, 0x10, 0 };
    private static final byte[] FLOAT_PI_LE = { -37, 15, 73, 64 };
    private static final byte[] FLOAT_PI_BE = { 64, 73, 15, -37 };
    private static final byte[] DOUBLE_PI_LE = { 24, 45, 68, 84, -5, 33, 9, 64 };
    private static final byte[] DOUBLE_PI_BE = { 64, 9, 33, -5, 84, 68, 45, 24 };
    private static final byte[] DOUBLE_FLOAT_PI_LE = { 0, 0, 0, 96, -5, 33, 9, 64 };
    private static final byte[] DOUBLE_FLOAT_PI_BE = { 64, 9, 33, -5, 96, 0, 0, 0 };
    private static final int[] INT_123 = { -123 };
    private static final int[] INT_12345 = { -12345 };
    private static final int[] UINT_12345 = { 53191 };
    private static final float[] FLOAT_PI = { (float) Math.PI };
    private static final double[] DOUBLE_PI = { Math.PI };
    private static final double[] DOUBLE_FLOAT_PI = { (float) Math.PI };
    private static final String STRING_PI = Double.toString(Math.PI);
    private static final String STRING_FLOAT_PI = Float.toString((float) Math.PI);
    private static final String[] STRING_FLOAT_PI_A = { STRING_FLOAT_PI };
    private static final String STRING_12345 = "-12345";
    private static final String STRING_123 = "-123";
    private static final String STRING_53191 = "53191";
    private static final String IMAGE_TYPE = "ORIGINAL\\PRIMARY\\AXIAL";
    private static final String[] IMAGE_TYPE_VALUES =
            { "ORIGINAL","PRIMARY", "AXIAL" };
    private static final byte[] IMAGE_TYPE_BYTES = {
            'O', 'R', 'I', 'G', 'I', 'N', 'A', 'L', '\\',
            'P', 'R', 'I', 'M', 'A', 'R', 'Y', '\\',
            'A', 'X', 'I', 'A', 'L' };

    private SpecificCharacterSet cs;

    @Before
    public void setUp() {
        cs = SpecificCharacterSet.valueOf(new String[] { "ISO_IR 100" });
    }

    @Test
    public void testCode() {
        assertEquals(0x4145, VR.AE.code());
    }

    @Test
    public void testHeaderLength() {
        assertEquals(8, VR.AE.headerLength());
        assertEquals(12, VR.SQ.headerLength());
    }

    @Test
    public void testValueOf() {
        assertEquals(VR.AE, VR.valueOf(0x4145));
    }

    @Test
    public void testToBytesStringSpecificCharacterSet() {
        assertArrayEquals(IMAGE_TYPE_BYTES, VR.CS.toBytes(IMAGE_TYPE, cs));
    }

    @Test
    public void testToString() {
        assertEquals(IMAGE_TYPE, VR.CS.toString(IMAGE_TYPE_BYTES, cs));
    }

    @Test
    public void testFirstStringValue() {
        assertEquals(IMAGE_TYPE_VALUES[0], VR.CS.firstStringValue(IMAGE_TYPE));
    }

    @Test
    public void testFirstBinaryValueAsString() {
        assertEquals(STRING_123, VR.OB.firstBinaryValueAsString(BYTE_123, false));
        assertEquals(STRING_123, VR.OB.firstBinaryValueAsString(BYTE_123, true));
        assertEquals(STRING_12345, VR.SS.firstBinaryValueAsString(SHORT_12345_LE, false));
        assertEquals(STRING_12345, VR.SS.firstBinaryValueAsString(SHORT_12345_BE, true));
        assertEquals(STRING_53191, VR.US.firstBinaryValueAsString(SHORT_12345_LE, false));
        assertEquals(STRING_53191, VR.US.firstBinaryValueAsString(SHORT_12345_BE, true));
        assertEquals(STRING_12345, VR.OW.firstBinaryValueAsString(SHORT_12345_LE, false));
        assertEquals(STRING_12345, VR.OW.firstBinaryValueAsString(SHORT_12345_BE, true));
        assertEquals(STRING_12345, VR.SL.firstBinaryValueAsString(INT_12345_LE, false));
        assertEquals(STRING_12345, VR.SL.firstBinaryValueAsString(INT_12345_BE, true));
        assertEquals(STRING_12345, VR.UL.firstBinaryValueAsString(INT_12345_LE, false));
        assertEquals(STRING_12345, VR.UL.firstBinaryValueAsString(INT_12345_BE, true));
        assertEquals(STRING_FLOAT_PI, VR.FL.firstBinaryValueAsString(FLOAT_PI_LE, false));
        assertEquals(STRING_FLOAT_PI, VR.FL.firstBinaryValueAsString(FLOAT_PI_BE, true));
        assertEquals(STRING_FLOAT_PI, VR.OF.firstBinaryValueAsString(FLOAT_PI_LE, false));
        assertEquals(STRING_FLOAT_PI, VR.OF.firstBinaryValueAsString(FLOAT_PI_BE, true));
        assertEquals(STRING_PI, VR.FD.firstBinaryValueAsString(DOUBLE_PI_LE, false));
        assertEquals(STRING_PI, VR.FD.firstBinaryValueAsString(DOUBLE_PI_BE, true));
    }

   @Test
    public void testSplitStringValue() {
        assertArrayEquals(IMAGE_TYPE_VALUES, VR.CS.splitStringValue(IMAGE_TYPE));
    }

    @Test
    public void testToBytesIntBoolean() {
        assertArrayEquals(BYTE_123, VR.OB.toBytes(-123, false));
        assertArrayEquals(BYTE_123, VR.OB.toBytes(-123, true));
        assertArrayEquals(SHORT_12345_LE, VR.SS.toBytes(-12345, false));
        assertArrayEquals(SHORT_12345_BE, VR.SS.toBytes(-12345, true));
        assertArrayEquals(SHORT_12345_LE, VR.US.toBytes(-12345, false));
        assertArrayEquals(SHORT_12345_BE, VR.US.toBytes(-12345, true));
        assertArrayEquals(SHORT_12345_LE, VR.OW.toBytes(-12345, false));
        assertArrayEquals(SHORT_12345_BE, VR.OW.toBytes(-12345, true));
        assertArrayEquals(INT_12345_LE, VR.SL.toBytes(-12345, false));
        assertArrayEquals(INT_12345_BE, VR.SL.toBytes(-12345, true));
        assertArrayEquals(INT_12345_LE, VR.UL.toBytes(-12345, false));
        assertArrayEquals(INT_12345_BE, VR.UL.toBytes(-12345, true));
        assertArrayEquals(TAG_PIXEL_DATA_LE, VR.AT.toBytes(Tag.PixelData, false));
        assertArrayEquals(TAG_PIXEL_DATA_BE, VR.AT.toBytes(Tag.PixelData, true));
    }

    @Test
    public void testToBytesIntArrayBoolean() {
        assertArrayEquals(BYTE_123, VR.OB.toBytes(INT_123, false));
        assertArrayEquals(BYTE_123, VR.OB.toBytes(INT_123, true));
        assertArrayEquals(SHORT_12345_LE, VR.SS.toBytes(INT_12345, false));
        assertArrayEquals(SHORT_12345_BE, VR.SS.toBytes(INT_12345, true));
        assertArrayEquals(SHORT_12345_LE, VR.US.toBytes(INT_12345, false));
        assertArrayEquals(SHORT_12345_BE, VR.US.toBytes(INT_12345, true));
        assertArrayEquals(SHORT_12345_LE, VR.OW.toBytes(INT_12345, false));
        assertArrayEquals(SHORT_12345_BE, VR.OW.toBytes(INT_12345, true));
        assertArrayEquals(INT_12345_LE, VR.SL.toBytes(INT_12345, false));
        assertArrayEquals(INT_12345_BE, VR.SL.toBytes(INT_12345, true));
        assertArrayEquals(INT_12345_LE, VR.UL.toBytes(INT_12345, false));
        assertArrayEquals(INT_12345_BE, VR.UL.toBytes(INT_12345, true));
    }

    @Test
    public void testToBytesFloatBoolean() {
        assertArrayEquals(FLOAT_PI_LE, VR.FL.toBytes((float) Math.PI, false));
        assertArrayEquals(FLOAT_PI_BE, VR.FL.toBytes((float) Math.PI, true));
        assertArrayEquals(FLOAT_PI_LE, VR.OF.toBytes((float) Math.PI, false));
        assertArrayEquals(FLOAT_PI_BE, VR.OF.toBytes((float) Math.PI, true));
        assertArrayEquals(DOUBLE_FLOAT_PI_LE, VR.FD.toBytes((float) Math.PI, false));
        assertArrayEquals(DOUBLE_FLOAT_PI_BE, VR.FD.toBytes((float) Math.PI, true));
    }

    @Test
    public void testToBytesFloatArrayBoolean() {
        assertArrayEquals(FLOAT_PI_LE, VR.FL.toBytes(FLOAT_PI, false));
        assertArrayEquals(FLOAT_PI_BE, VR.FL.toBytes(FLOAT_PI, true));
        assertArrayEquals(FLOAT_PI_LE, VR.OF.toBytes(FLOAT_PI, false));
        assertArrayEquals(FLOAT_PI_BE, VR.OF.toBytes(FLOAT_PI, true));
        assertArrayEquals(DOUBLE_FLOAT_PI_LE, VR.FD.toBytes(FLOAT_PI, false));
        assertArrayEquals(DOUBLE_FLOAT_PI_BE, VR.FD.toBytes(FLOAT_PI, true));
    }

    @Test
    public void testToBytesDoubleBoolean() {
        assertArrayEquals(FLOAT_PI_LE, VR.FL.toBytes(Math.PI, false));
        assertArrayEquals(FLOAT_PI_BE, VR.FL.toBytes(Math.PI, true));
        assertArrayEquals(FLOAT_PI_LE, VR.OF.toBytes(Math.PI, false));
        assertArrayEquals(FLOAT_PI_BE, VR.OF.toBytes(Math.PI, true));
        assertArrayEquals(DOUBLE_PI_LE, VR.FD.toBytes(Math.PI, false));
        assertArrayEquals(DOUBLE_PI_BE, VR.FD.toBytes(Math.PI, true));
    }

    @Test
    public void testToBytesDoubleArrayBoolean() {
        assertArrayEquals(FLOAT_PI_LE, VR.FL.toBytes(DOUBLE_PI, false));
        assertArrayEquals(FLOAT_PI_BE, VR.FL.toBytes(DOUBLE_PI, true));
        assertArrayEquals(FLOAT_PI_LE, VR.OF.toBytes(DOUBLE_PI, false));
        assertArrayEquals(FLOAT_PI_BE, VR.OF.toBytes(DOUBLE_PI, true));
        assertArrayEquals(DOUBLE_PI_LE, VR.FD.toBytes(DOUBLE_PI, false));
        assertArrayEquals(DOUBLE_PI_BE, VR.FD.toBytes(DOUBLE_PI, true));
    }

    @Test
    public void testToIntByteArrayBoolean() {
        assertEquals(-123, VR.OB.toInt(BYTE_123, false));
        assertEquals(-123, VR.OB.toInt(BYTE_123, true));
        assertEquals(-12345, VR.SS.toInt(SHORT_12345_LE, false));
        assertEquals(-12345, VR.SS.toInt(SHORT_12345_BE, true));
        assertEquals(53191, VR.US.toInt(SHORT_12345_LE, false));
        assertEquals(53191, VR.US.toInt(SHORT_12345_BE, true));
        assertEquals(-12345, VR.OW.toInt(SHORT_12345_LE, false));
        assertEquals(-12345, VR.OW.toInt(SHORT_12345_BE, true));
        assertEquals(-12345, VR.SL.toInt(INT_12345_LE, false));
        assertEquals(-12345, VR.SL.toInt(INT_12345_BE, true));
        assertEquals(-12345, VR.UL.toInt(INT_12345_LE, false));
        assertEquals(-12345, VR.UL.toInt(INT_12345_BE, true));
    }

    @Test
    public void testToIntsByteArrayBoolean() {
        assertArrayEquals(INT_123, VR.OB.toInts(BYTE_123, false));
        assertArrayEquals(INT_123, VR.OB.toInts(BYTE_123, true));
        assertArrayEquals(INT_12345, VR.SS.toInts(SHORT_12345_LE, false));
        assertArrayEquals(INT_12345, VR.SS.toInts(SHORT_12345_BE, true));
        assertArrayEquals(UINT_12345, VR.US.toInts(SHORT_12345_LE, false));
        assertArrayEquals(UINT_12345, VR.US.toInts(SHORT_12345_BE, true));
        assertArrayEquals(INT_12345, VR.OW.toInts(SHORT_12345_LE, false));
        assertArrayEquals(INT_12345, VR.OW.toInts(SHORT_12345_BE, true));
        assertArrayEquals(INT_12345, VR.SL.toInts(INT_12345_LE, false));
        assertArrayEquals(INT_12345, VR.SL.toInts(INT_12345_BE, true));
        assertArrayEquals(INT_12345, VR.UL.toInts(INT_12345_LE, false));
        assertArrayEquals(INT_12345, VR.UL.toInts(INT_12345_BE, true));
    }

    @Test
    public void testToIntString() {
        assertEquals(-12345, VR.IS.toInt(STRING_12345));
    }

    @Test
    public void testToIntsString() {
        assertArrayEquals(INT_12345, VR.IS.toInts(STRING_12345));
    }

    @Test
    public void testToFloatByteArrayBoolean() {
        assertEquals((float) Math.PI, VR.FL.toFloat(FLOAT_PI_LE, false), 0);
        assertEquals((float) Math.PI, VR.FL.toFloat(FLOAT_PI_BE, true), 0);
        assertEquals((float) Math.PI, VR.OF.toFloat(FLOAT_PI_LE, false), 0);
        assertEquals((float) Math.PI, VR.OF.toFloat(FLOAT_PI_BE, true), 0);
        assertEquals((float) Math.PI, VR.FD.toFloat(DOUBLE_PI_LE, false), 0);
        assertEquals((float) Math.PI, VR.FD.toFloat(DOUBLE_PI_BE, true), 0);
    }

    @Test
    public void testToFloatsByteArrayBoolean() {
        assertArrayEquals(FLOAT_PI, VR.FL.toFloats(FLOAT_PI_LE, false), 0);
        assertArrayEquals(FLOAT_PI, VR.FL.toFloats(FLOAT_PI_BE, true), 0);
        assertArrayEquals(FLOAT_PI, VR.OF.toFloats(FLOAT_PI_LE, false), 0);
        assertArrayEquals(FLOAT_PI, VR.OF.toFloats(FLOAT_PI_BE, true), 0);
        assertArrayEquals(FLOAT_PI, VR.FD.toFloats(DOUBLE_PI_LE, false), 0);
        assertArrayEquals(FLOAT_PI, VR.FD.toFloats(DOUBLE_PI_BE, true), 0);
    }

    @Test
    public void testToFloatString() {
        assertEquals((float) Math.PI, VR.DS.toFloat(STRING_FLOAT_PI), 0);
    }

    @Test
    public void testToFloatsString() {
        assertArrayEquals(FLOAT_PI, VR.DS.toFloats(STRING_FLOAT_PI), 0);
    }

    @Test
    public void testToDoubleString() {
        assertEquals((float) Math.PI, VR.DS.toDouble(STRING_FLOAT_PI), 0);
    }

    @Test
    public void testToDoublesByteArrayBoolean() {
        assertArrayEquals(DOUBLE_FLOAT_PI, VR.FL.toDoubles(FLOAT_PI_LE, false), 0);
        assertArrayEquals(DOUBLE_FLOAT_PI, VR.FL.toDoubles(FLOAT_PI_BE, true), 0);
        assertArrayEquals(DOUBLE_FLOAT_PI, VR.OF.toDoubles(FLOAT_PI_LE, false), 0);
        assertArrayEquals(DOUBLE_FLOAT_PI, VR.OF.toDoubles(FLOAT_PI_BE, true), 0);
        assertArrayEquals(DOUBLE_PI, VR.FD.toDoubles(DOUBLE_PI_LE, false), 0);
        assertArrayEquals(DOUBLE_PI, VR.FD.toDoubles(DOUBLE_PI_BE, true), 0);
    }

    @Test
    public void testToDoublesString() {
        assertArrayEquals(DOUBLE_FLOAT_PI, VR.DS.toDoubles(STRING_FLOAT_PI), 0);
    }

    @Test
    public void testToDoubleByteArrayBoolean() {
        assertEquals((float) Math.PI, VR.FL.toDouble(FLOAT_PI_LE, false), 0);
        assertEquals((float) Math.PI, VR.FL.toDouble(FLOAT_PI_BE, true), 0);
        assertEquals((float) Math.PI, VR.OF.toDouble(FLOAT_PI_LE, false), 0);
        assertEquals((float) Math.PI, VR.OF.toDouble(FLOAT_PI_BE, true), 0);
        assertEquals(Math.PI, VR.FD.toDouble(DOUBLE_PI_LE, false), 0);
        assertEquals(Math.PI, VR.FD.toDouble(DOUBLE_PI_BE, true), 0);
    }

    @Test
    public void testToValueByteArray() {
        assertEquals(DOUBLE_PI_LE, VR.FD.toValue(DOUBLE_PI_LE));
        assertNull(VR.FD.toValue(EMPTY_BYTES));
    }

    @Test (expected=UnsupportedOperationException.class)
    public void testToValueByteArraySQ() {
        VR.SQ.toValue(DOUBLE_PI_LE);
    }

    @Test
    public void testToValueString() {
        assertArrayEquals(BYTE_123, (byte[]) VR.OB.toValue(STRING_123, false));
        assertArrayEquals(BYTE_123, (byte[]) VR.OB.toValue(STRING_123, true));
        assertArrayEquals(SHORT_12345_LE, (byte[]) VR.SS.toValue(STRING_12345, false));
        assertArrayEquals(SHORT_12345_BE, (byte[]) VR.SS.toValue(STRING_12345, true));
        assertArrayEquals(SHORT_12345_LE, (byte[]) VR.US.toValue(STRING_53191, false));
        assertArrayEquals(SHORT_12345_BE, (byte[]) VR.US.toValue(STRING_53191, true));
        assertArrayEquals(SHORT_12345_LE, (byte[]) VR.OW.toValue(STRING_12345, false));
        assertArrayEquals(SHORT_12345_BE, (byte[]) VR.OW.toValue(STRING_12345, true));
        assertArrayEquals(INT_12345_LE, (byte[]) VR.SL.toValue(STRING_12345, false));
        assertArrayEquals(INT_12345_BE, (byte[]) VR.SL.toValue(STRING_12345, true));
        assertArrayEquals(INT_12345_LE, (byte[]) VR.UL.toValue(STRING_12345, false));
        assertArrayEquals(INT_12345_BE, (byte[]) VR.UL.toValue(STRING_12345, true));
        assertArrayEquals(FLOAT_PI_LE, (byte[]) VR.FL.toValue(STRING_FLOAT_PI, false));
        assertArrayEquals(FLOAT_PI_BE, (byte[]) VR.FL.toValue(STRING_FLOAT_PI, true));
        assertArrayEquals(FLOAT_PI_LE, (byte[]) VR.OF.toValue(STRING_FLOAT_PI, false));
        assertArrayEquals(FLOAT_PI_BE, (byte[]) VR.OF.toValue(STRING_FLOAT_PI, true));
        assertArrayEquals(DOUBLE_PI_LE, (byte[]) VR.FD.toValue(STRING_PI, false));
        assertArrayEquals(DOUBLE_PI_BE, (byte[]) VR.FD.toValue(STRING_PI, true));
        assertEquals(STRING_12345, VR.IS.toValue(STRING_12345, false));
        assertEquals(STRING_12345, VR.IS.toValue(STRING_12345, true));
        assertEquals(STRING_FLOAT_PI, VR.DS.toValue(STRING_FLOAT_PI, false));
        assertEquals(STRING_FLOAT_PI, VR.DS.toValue(STRING_FLOAT_PI, true));
        assertNull(VR.DS.toValue("", false));
        assertNull(VR.DS.toValue("", true));
    }

    @Test (expected=UnsupportedOperationException.class)
    public void testToValueStringSQ() {
        VR.SQ.toValue(STRING_FLOAT_PI, false);
    }

    @Test
    public void testToValueStringArray() {
        assertEquals(IMAGE_TYPE, VR.CS.toValue(IMAGE_TYPE_VALUES, false));
        assertEquals(IMAGE_TYPE, VR.CS.toValue(IMAGE_TYPE_VALUES, true));
        assertArrayEquals(FLOAT_PI_LE, (byte[]) VR.OF.toValue(STRING_FLOAT_PI_A, false));
        assertArrayEquals(FLOAT_PI_BE, (byte[]) VR.OF.toValue(STRING_FLOAT_PI_A, true));
    }

    @Test
    public void testToValueIntBoolean() {
        assertArrayEquals(BYTE_123, (byte[]) VR.OB.toValue(-123, false));
        assertArrayEquals(BYTE_123, (byte[]) VR.OB.toValue(-123, true));
        assertArrayEquals(SHORT_12345_LE, (byte[]) VR.SS.toValue(-12345, false));
        assertArrayEquals(SHORT_12345_BE, (byte[]) VR.SS.toValue(-12345, true));
        assertArrayEquals(SHORT_12345_LE, (byte[]) VR.US.toValue(-12345, false));
        assertArrayEquals(SHORT_12345_BE, (byte[]) VR.US.toValue(-12345, true));
        assertArrayEquals(SHORT_12345_LE, (byte[]) VR.OW.toValue(-12345, false));
        assertArrayEquals(SHORT_12345_BE, (byte[]) VR.OW.toValue(-12345, true));
        assertArrayEquals(INT_12345_LE, (byte[]) VR.SL.toValue(-12345, false));
        assertArrayEquals(INT_12345_BE, (byte[]) VR.SL.toValue(-12345, true));
        assertArrayEquals(INT_12345_LE,(byte[])  VR.UL.toValue(-12345, false));
        assertArrayEquals(INT_12345_BE,(byte[])  VR.UL.toValue(-12345, true));
        assertEquals(STRING_12345, VR.IS.toValue(-12345, false));
        assertEquals(STRING_12345, VR.IS.toValue(-12345, true));
    }

    @Test
    public void testToValueIntArrayBoolean() {
        assertArrayEquals(BYTE_123, (byte[]) VR.OB.toValue(INT_123, false));
        assertArrayEquals(BYTE_123, (byte[]) VR.OB.toValue(INT_123, true));
        assertArrayEquals(SHORT_12345_LE, (byte[]) VR.SS.toValue(INT_12345, false));
        assertArrayEquals(SHORT_12345_BE, (byte[]) VR.SS.toValue(INT_12345, true));
        assertArrayEquals(SHORT_12345_LE, (byte[]) VR.US.toValue(INT_12345, false));
        assertArrayEquals(SHORT_12345_BE, (byte[]) VR.US.toValue(INT_12345, true));
        assertArrayEquals(SHORT_12345_LE, (byte[]) VR.OW.toValue(INT_12345, false));
        assertArrayEquals(SHORT_12345_BE, (byte[]) VR.OW.toValue(INT_12345, true));
        assertArrayEquals(INT_12345_LE, (byte[]) VR.SL.toValue(INT_12345, false));
        assertArrayEquals(INT_12345_BE, (byte[]) VR.SL.toValue(INT_12345, true));
        assertArrayEquals(INT_12345_LE,(byte[])  VR.UL.toValue(INT_12345, false));
        assertArrayEquals(INT_12345_BE,(byte[])  VR.UL.toValue(INT_12345, true));
        assertEquals(STRING_12345, VR.IS.toValue(INT_12345, false));
        assertEquals(STRING_12345, VR.IS.toValue(INT_12345, true));
    }

    @Test
    public void testToValueFloatBoolean() {
        assertArrayEquals(FLOAT_PI_LE, (byte[]) VR.FL.toValue((float) Math.PI, false));
        assertArrayEquals(FLOAT_PI_BE, (byte[]) VR.FL.toValue((float) Math.PI, true));
        assertArrayEquals(FLOAT_PI_LE, (byte[]) VR.OF.toValue((float) Math.PI, false));
        assertArrayEquals(FLOAT_PI_BE, (byte[]) VR.OF.toValue((float) Math.PI, true));
        assertArrayEquals(DOUBLE_FLOAT_PI_LE, (byte[]) VR.FD.toValue((float) Math.PI, false));
        assertArrayEquals(DOUBLE_FLOAT_PI_BE, (byte[]) VR.FD.toValue((float) Math.PI, true));
        assertEquals(STRING_FLOAT_PI, VR.DS.toValue((float) Math.PI, false));
        assertEquals(STRING_FLOAT_PI, VR.DS.toValue((float) Math.PI, true));
     }

    @Test
    public void testToValueFloatArrayBoolean() {
        assertArrayEquals(FLOAT_PI_LE, (byte[]) VR.FL.toValue(FLOAT_PI, false));
        assertArrayEquals(FLOAT_PI_BE, (byte[]) VR.FL.toValue(FLOAT_PI, true));
        assertArrayEquals(FLOAT_PI_LE, (byte[]) VR.OF.toValue(FLOAT_PI, false));
        assertArrayEquals(FLOAT_PI_BE, (byte[]) VR.OF.toValue(FLOAT_PI, true));
        assertArrayEquals(DOUBLE_FLOAT_PI_LE, (byte[]) VR.FD.toValue(FLOAT_PI, false));
        assertArrayEquals(DOUBLE_FLOAT_PI_BE, (byte[]) VR.FD.toValue(FLOAT_PI, true));
        assertEquals(STRING_FLOAT_PI, VR.DS.toValue(FLOAT_PI, false));
        assertEquals(STRING_FLOAT_PI, VR.DS.toValue(FLOAT_PI, true));
    }

    @Test
    public void testToValueDoubleBoolean() {
        assertArrayEquals(FLOAT_PI_LE, (byte[]) VR.FL.toValue(Math.PI, false));
        assertArrayEquals(FLOAT_PI_BE, (byte[]) VR.FL.toValue(Math.PI, true));
        assertArrayEquals(FLOAT_PI_LE, (byte[]) VR.OF.toValue(Math.PI, false));
        assertArrayEquals(FLOAT_PI_BE, (byte[]) VR.OF.toValue(Math.PI, true));
        assertArrayEquals(DOUBLE_PI_LE, (byte[]) VR.FD.toValue(Math.PI, false));
        assertArrayEquals(DOUBLE_PI_BE, (byte[]) VR.FD.toValue(Math.PI, true));
        assertEquals(STRING_FLOAT_PI, VR.DS.toValue(Math.PI, false));
        assertEquals(STRING_FLOAT_PI, VR.DS.toValue(Math.PI, true));
    }

    @Test
    public void testToValueDoubleArrayBoolean() {
        assertArrayEquals(FLOAT_PI_LE, (byte[]) VR.FL.toValue(DOUBLE_PI, false));
        assertArrayEquals(FLOAT_PI_BE, (byte[]) VR.FL.toValue(DOUBLE_PI, true));
        assertArrayEquals(FLOAT_PI_LE, (byte[]) VR.OF.toValue(DOUBLE_PI, false));
        assertArrayEquals(FLOAT_PI_BE, (byte[]) VR.OF.toValue(DOUBLE_PI, true));
        assertArrayEquals(DOUBLE_PI_LE, (byte[]) VR.FD.toValue(DOUBLE_PI, false));
        assertArrayEquals(DOUBLE_PI_BE, (byte[]) VR.FD.toValue(DOUBLE_PI, true));
        assertEquals(STRING_FLOAT_PI, VR.DS.toValue(DOUBLE_PI, false));
        assertEquals(STRING_FLOAT_PI, VR.DS.toValue(DOUBLE_PI, true));
    }

}
