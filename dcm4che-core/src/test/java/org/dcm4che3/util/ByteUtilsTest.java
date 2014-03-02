package org.dcm4che3.util;

import static org.junit.Assert.*;

import org.dcm4che3.data.Tag;
import org.dcm4che3.util.ByteUtils;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class ByteUtilsTest {

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

    @Test
    public void testBytesToUShortBE() {
        assertEquals(-12345 & 0xffff,
                ByteUtils.bytesToUShortBE(SHORT_12345_BE, 0));
    }

    @Test
    public void testBytesToUShortLE() {
        assertEquals(-12345 & 0xffff,
                ByteUtils.bytesToUShortLE(SHORT_12345_LE, 0));
    }

    @Test
    public void testBytesToShortBE() {
        assertEquals(-12345,
                ByteUtils.bytesToShortBE(SHORT_12345_BE, 0));
    }

    @Test
    public void testBytesToShortLE() {
        assertEquals(-12345,
                ByteUtils.bytesToShortLE(SHORT_12345_LE, 0));
    }

    @Test
    public void testBytesToIntBE() {
        assertEquals(-12345,
                ByteUtils.bytesToIntBE(INT_12345_BE, 0));
    }

    @Test
    public void testBytesToIntLE() {
        assertEquals(-12345,
                ByteUtils.bytesToIntLE(INT_12345_LE, 0));
    }

    @Test
    public void testBytesToTagBE() {
        assertEquals(Tag.PixelData,
                ByteUtils.bytesToTagBE(TAG_PIXEL_DATA_BE, 0));
    }

    @Test
    public void testBytesToTagLE() {
        assertEquals(Tag.PixelData,
                ByteUtils.bytesToTagLE(TAG_PIXEL_DATA_LE, 0));
    }

    @Test
    public void testBytesToFloatBE() {
        assertEquals((float) Math.PI,
                ByteUtils.bytesToFloatBE(FLOAT_PI_BE, 0), 0);
    }

    @Test
    public void testBytesToFloatLE() {
        assertEquals((float) Math.PI,
                ByteUtils.bytesToFloatLE(FLOAT_PI_LE, 0), 0);
    }

    @Test
    public void testBytesToDoubleBE() {
        assertEquals(Math.PI,
                ByteUtils.bytesToDoubleBE(DOUBLE_PI_BE, 0), 0);
    }

    @Test
    public void testBytesToDoubleLE() {
        assertEquals(Math.PI,
                ByteUtils.bytesToDoubleLE(DOUBLE_PI_LE, 0), 0);
    }

    @Test
    public void testShortToBytesBE() {
        assertArrayEquals(SHORT_12345_BE,
                ByteUtils.shortToBytesBE(-12345, new byte[2] , 0));
    }

    @Test
    public void testShortToBytesLE() {
        assertArrayEquals(SHORT_12345_LE,
                ByteUtils.shortToBytesLE(-12345, new byte[2] , 0));
    }

    @Test
    public void testIntToBytesBE() {
        assertArrayEquals(INT_12345_BE,
                ByteUtils.intToBytesBE(-12345, new byte[4] , 0));
    }

    @Test
    public void testIntToBytesLE() {
        assertArrayEquals(INT_12345_LE,
                ByteUtils.intToBytesLE(-12345, new byte[4] , 0));
    }

    @Test
    public void testTagToBytesBE() {
        assertArrayEquals(TAG_PIXEL_DATA_BE,
                ByteUtils.tagToBytesBE(Tag.PixelData, new byte[4] , 0));
    }

    @Test
    public void testTagToBytesLE() {
        assertArrayEquals(TAG_PIXEL_DATA_LE,
                ByteUtils.tagToBytesLE(Tag.PixelData, new byte[4] , 0));
    }

    @Test
    public void testFloatToBytesBE() {
        assertArrayEquals(FLOAT_PI_BE ,
                ByteUtils.floatToBytesBE((float) Math.PI, new byte[4], 0));
     }

    @Test
    public void testFloatToBytesLE() {
        assertArrayEquals(FLOAT_PI_LE ,
                ByteUtils.floatToBytesLE((float) Math.PI, new byte[4], 0));
    }

    @Test
    public void testDoubleToBytesBE() {
        assertArrayEquals(DOUBLE_PI_BE ,
                ByteUtils.doubleToBytesBE(Math.PI, new byte[8], 0));
    }

    @Test
    public void testDoubleToBytesLE() {
        assertArrayEquals(DOUBLE_PI_LE ,
                ByteUtils.doubleToBytesLE(Math.PI, new byte[8], 0));
    }

    @Test
    public void testSwapShorts() {
        assertArrayEquals(TAG_PIXEL_DATA_LE ,
                ByteUtils.swapShorts(TAG_PIXEL_DATA_BE.clone(), 0,
                        TAG_PIXEL_DATA_BE.length));
    }

    @Test
    public void testSwapInts() {
        assertArrayEquals(FLOAT_PI_LE ,
                ByteUtils.swapInts(FLOAT_PI_BE.clone(), 0,
                        FLOAT_PI_BE.length));
    }

    @Test
    public void testSwapLongs() {
        assertArrayEquals(DOUBLE_PI_LE ,
                ByteUtils.swapLongs(DOUBLE_PI_BE.clone(), 0,
                        DOUBLE_PI_BE.length));
    }

}
