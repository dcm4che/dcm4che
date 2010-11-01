package org.dcm4che.data;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class VRTest {

    private static final byte[] BYTE_123 = { -123 };
    private static final byte[] SHORT_12345_BE = { -49, -57 };
    private static final byte[] SHORT_12345_LE = { -57, -49 };
    private static final byte[] INT_12345_BE = { -1, -1, -49, -57 };
    private static final byte[] INT_12345_LE = { -57, -49, -1, -1 };
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
    public void testStringsToBytes() {
        assertArrayEquals(IMAGE_TYPE_BYTES,
                VR.CS.toBytes(IMAGE_TYPE, false, cs));
    }

    @Test
    public void testBytesToStrings() {
        assertArrayEquals(IMAGE_TYPE_VALUES, 
                (String[]) VR.CS.toStrings(IMAGE_TYPE_BYTES, false, cs));
        assertEquals(STRING_123, VR.OB.toStrings(BYTE_123, false, cs));
        assertEquals(STRING_123, VR.OB.toStrings(BYTE_123, true, cs));
        assertEquals(STRING_12345, VR.SS.toStrings(SHORT_12345_LE, false, cs));
        assertEquals(STRING_12345, VR.SS.toStrings(SHORT_12345_BE, true, cs));
        assertEquals(STRING_53191, VR.US.toStrings(SHORT_12345_LE, false, cs));
        assertEquals(STRING_53191, VR.US.toStrings(SHORT_12345_BE, true, cs));
        assertEquals(STRING_12345, VR.OW.toStrings(SHORT_12345_LE, false, cs));
        assertEquals(STRING_12345, VR.OW.toStrings(SHORT_12345_BE, true, cs));
        assertEquals(STRING_12345, VR.SL.toStrings(INT_12345_LE, false, cs));
        assertEquals(STRING_12345, VR.SL.toStrings(INT_12345_BE, true, cs));
        assertEquals(STRING_12345, VR.UL.toStrings(INT_12345_LE, false, cs));
        assertEquals(STRING_12345, VR.UL.toStrings(INT_12345_BE, true, cs));
        assertEquals(STRING_FLOAT_PI, VR.FL.toStrings(FLOAT_PI_LE, false, cs));
        assertEquals(STRING_FLOAT_PI, VR.FL.toStrings(FLOAT_PI_BE, true, cs));
        assertEquals(STRING_FLOAT_PI, VR.OF.toStrings(FLOAT_PI_LE, false, cs));
        assertEquals(STRING_FLOAT_PI, VR.OF.toStrings(FLOAT_PI_BE, true, cs));
        assertEquals(STRING_PI, VR.FD.toStrings(DOUBLE_PI_LE, false, cs));
        assertEquals(STRING_PI, VR.FD.toStrings(DOUBLE_PI_BE, true, cs));
    }

    @Test
    public void testIntsToBytes() {
        assertArrayEquals(BYTE_123, VR.OB.toBytes(INT_123, false, cs));
        assertArrayEquals(BYTE_123, VR.OB.toBytes(INT_123, true, cs));
        assertArrayEquals(SHORT_12345_LE, VR.SS.toBytes(INT_12345, false, cs));
        assertArrayEquals(SHORT_12345_BE, VR.SS.toBytes(INT_12345, true, cs));
        assertArrayEquals(SHORT_12345_LE, VR.US.toBytes(INT_12345, false, cs));
        assertArrayEquals(SHORT_12345_BE, VR.US.toBytes(INT_12345, true, cs));
        assertArrayEquals(SHORT_12345_LE, VR.OW.toBytes(INT_12345, false, cs));
        assertArrayEquals(SHORT_12345_BE, VR.OW.toBytes(INT_12345, true, cs));
        assertArrayEquals(INT_12345_LE, VR.SL.toBytes(INT_12345, false, cs));
        assertArrayEquals(INT_12345_BE, VR.SL.toBytes(INT_12345, true, cs));
        assertArrayEquals(INT_12345_LE, VR.UL.toBytes(INT_12345, false, cs));
        assertArrayEquals(INT_12345_BE, VR.UL.toBytes(INT_12345, true, cs));
    }

    @Test
    public void testFloatsToBytes() {
        assertArrayEquals(FLOAT_PI_LE, VR.FL.toBytes(FLOAT_PI, false, cs));
        assertArrayEquals(FLOAT_PI_BE, VR.FL.toBytes(FLOAT_PI, true, cs));
        assertArrayEquals(FLOAT_PI_LE, VR.OF.toBytes(FLOAT_PI, false, cs));
        assertArrayEquals(FLOAT_PI_BE, VR.OF.toBytes(FLOAT_PI, true, cs));
        assertArrayEquals(DOUBLE_FLOAT_PI_LE, VR.FD.toBytes(FLOAT_PI, false, cs));
        assertArrayEquals(DOUBLE_FLOAT_PI_BE, VR.FD.toBytes(FLOAT_PI, true, cs));
    }

    @Test
    public void testDoublesToBytes() {
        assertArrayEquals(FLOAT_PI_LE, VR.FL.toBytes(DOUBLE_PI, false, cs));
        assertArrayEquals(FLOAT_PI_BE, VR.FL.toBytes(DOUBLE_PI, true, cs));
        assertArrayEquals(FLOAT_PI_LE, VR.OF.toBytes(DOUBLE_PI, false, cs));
        assertArrayEquals(FLOAT_PI_BE, VR.OF.toBytes(DOUBLE_PI, true, cs));
        assertArrayEquals(DOUBLE_PI_LE, VR.FD.toBytes(DOUBLE_PI, false, cs));
        assertArrayEquals(DOUBLE_PI_BE, VR.FD.toBytes(DOUBLE_PI, true, cs));
    }

    @Test
    public void testBytesToInts() {
        assertArrayEquals(INT_123, VR.OB.toInts(BYTE_123, false, cs));
        assertArrayEquals(INT_123, VR.OB.toInts(BYTE_123, true, cs));
        assertArrayEquals(INT_12345, VR.SS.toInts(SHORT_12345_LE, false, cs));
        assertArrayEquals(INT_12345, VR.SS.toInts(SHORT_12345_BE, true, cs));
        assertArrayEquals(UINT_12345, VR.US.toInts(SHORT_12345_LE, false, cs));
        assertArrayEquals(UINT_12345, VR.US.toInts(SHORT_12345_BE, true, cs));
        assertArrayEquals(INT_12345, VR.OW.toInts(SHORT_12345_LE, false, cs));
        assertArrayEquals(INT_12345, VR.OW.toInts(SHORT_12345_BE, true, cs));
        assertArrayEquals(INT_12345, VR.SL.toInts(INT_12345_LE, false, cs));
        assertArrayEquals(INT_12345, VR.SL.toInts(INT_12345_BE, true, cs));
        assertArrayEquals(INT_12345, VR.UL.toInts(INT_12345_LE, false, cs));
        assertArrayEquals(INT_12345, VR.UL.toInts(INT_12345_BE, true, cs));
    }

    @Test
    public void testStringToInts() {
        assertArrayEquals(INT_12345, VR.IS.toInts(STRING_12345, false, cs));
    }

    @Test
    public void testBytesToFloats() {
        assertArrayEquals(FLOAT_PI, VR.FL.toFloats(FLOAT_PI_LE, false, cs), 0);
        assertArrayEquals(FLOAT_PI, VR.FL.toFloats(FLOAT_PI_BE, true, cs), 0);
        assertArrayEquals(FLOAT_PI, VR.OF.toFloats(FLOAT_PI_LE, false, cs), 0);
        assertArrayEquals(FLOAT_PI, VR.OF.toFloats(FLOAT_PI_BE, true, cs), 0);
        assertArrayEquals(FLOAT_PI, VR.FD.toFloats(DOUBLE_PI_LE, false, cs), 0);
        assertArrayEquals(FLOAT_PI, VR.FD.toFloats(DOUBLE_PI_BE, true, cs), 0);
    }

    @Test
    public void testStringToFloats() {
        assertArrayEquals(FLOAT_PI, VR.DS.toFloats(STRING_FLOAT_PI, false, cs), 0);
    }

   @Test
    public void testBytesToDoubles() {
        assertArrayEquals(DOUBLE_FLOAT_PI, VR.FL.toDoubles(FLOAT_PI_LE, false, cs), 0);
        assertArrayEquals(DOUBLE_FLOAT_PI, VR.FL.toDoubles(FLOAT_PI_BE, true, cs), 0);
        assertArrayEquals(DOUBLE_FLOAT_PI, VR.OF.toDoubles(FLOAT_PI_LE, false, cs), 0);
        assertArrayEquals(DOUBLE_FLOAT_PI, VR.OF.toDoubles(FLOAT_PI_BE, true, cs), 0);
        assertArrayEquals(DOUBLE_PI, VR.FD.toDoubles(DOUBLE_PI_LE, false, cs), 0);
        assertArrayEquals(DOUBLE_PI, VR.FD.toDoubles(DOUBLE_PI_BE, true, cs), 0);
    }
}
