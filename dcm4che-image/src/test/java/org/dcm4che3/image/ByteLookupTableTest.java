package org.dcm4che3.image;

import org.junit.Test;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import static org.junit.Assert.*;


public class ByteLookupTableTest {

    @Test
    public void testConstructorWithRamp() {
        StoredValue sv = new StoredValue.Unsigned(8);
        // minOut=0, maxOut=255, offset=0, size=256, flip=false
        ByteLookupTable lut = new ByteLookupTable(sv, 8, 0, 255, 0, 256, false);
        assertEquals(256, lut.length());
        byte[] dest = new byte[3];
        lut.lookup(new byte[]{0, 127, (byte) 255}, 0, dest, 0, 3);
        assertArrayEquals(new byte[]{0, 127, (byte) 255}, dest);

        // Test flip=true
        lut = new ByteLookupTable(sv, 8, 0, 255, 0, 256, true);
        lut.lookup(new byte[]{0, 127, (byte) 255}, 0, dest, 0, 3);
        assertArrayEquals(new byte[]{(byte) 255, (byte) 128, 0}, dest);
    }

    @Test
    public void testConstructorWithConstantValue() {
        StoredValue sv = new StoredValue.Unsigned(8);
        // minOut=128, maxOut=128, offset=0, size=256, flip=false
        ByteLookupTable lut = new ByteLookupTable(sv, 8, 128, 128, 0, 256, false);
        assertEquals(1, lut.length()); // Special case in constructor: if minOut == maxOut, lut.length is 1
        byte[] dest = new byte[3];
        lut.lookup(new byte[]{0, 127, (byte) 255}, 0, dest, 0, 3);
        assertArrayEquals(new byte[]{(byte) 128, (byte) 128, (byte) 128}, dest);
    }

    @Test
    public void testLookupShortToByte() {
        StoredValue sv = new StoredValue.Unsigned(8);
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) data[i] = (byte) i;
        ByteLookupTable lut = new ByteLookupTable(sv, 8, 0, data);

        short[] src = new short[]{0, 127, 255};
        byte[] dest = new byte[3];
        lut.lookup(src, 0, dest, 0, 3);
        assertArrayEquals(new byte[]{0, 127, (byte) 255}, dest);
    }

    @Test
    public void testLookupByteToShort() {
        StoredValue sv = new StoredValue.Unsigned(8);
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) data[i] = (byte) i;
        ByteLookupTable lut = new ByteLookupTable(sv, 8, 0, data);

        byte[] src = new byte[]{0, 127, (byte) 255};
        short[] dest = new short[3];
        lut.lookup(src, 0, dest, 0, 3);
        assertArrayEquals(new short[]{0, 127, 255}, dest);
    }

    @Test
    public void testLookupShortToShort() {
        StoredValue sv = new StoredValue.Unsigned(8);
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) data[i] = (byte) i;
        ByteLookupTable lut = new ByteLookupTable(sv, 8, 0, data);

        short[] src = new short[]{0, 127, 255};
        short[] dest = new short[3];
        lut.lookup(src, 0, dest, 0, 3);
        assertArrayEquals(new short[]{0, 127, 255}, dest);
    }

    @Test
    public void testAdjustOutBitsDown() {
        StoredValue sv = new StoredValue.Unsigned(8);
        byte[] data = new byte[]{(byte) 255, (byte) 128, 0};
        ByteLookupTable lut = new ByteLookupTable(sv, 8, 0, data);

        // Adjust from 8 to 4 bits (right shift by 4)
        LookupTable adjusted = lut.adjustOutBits(4);
        assertTrue(adjusted instanceof ByteLookupTable);
        assertEquals(4, adjusted.outBits);

        byte[] dest = new byte[3];
        adjusted.lookup(new byte[]{0, 1, 2}, 0, dest, 0, 3);
        // 255 >> 4 = 15
        // 128 >> 4 = 8
        // 0 >> 4 = 0
        assertArrayEquals(new byte[]{15, 8, 0}, dest);
    }

    @Test
    public void testCombineWithShortLUT() {
        StoredValue sv = new StoredValue.Unsigned(8);
        byte[] data1 = new byte[256];
        for (int i = 0; i < 256; i++) data1[i] = (byte) i;
        ByteLookupTable lut1 = new ByteLookupTable(sv, 8, 0, data1);

        short[] data2 = new short[256];
        for (int i = 0; i < 256; i++) data2[i] = (short) (i * 10);
        ShortLookupTable lut2 = new ShortLookupTable(new StoredValue.Unsigned(8), 12, 0, data2);

        LookupTable combined = lut1.combine(lut2);
        assertTrue(combined instanceof ShortLookupTable);
        assertEquals(12, combined.outBits);

        short[] dest = new short[3];
        combined.lookup(new byte[]{0, 10, 20}, 0, dest, 0, 3);
        assertArrayEquals(new short[]{0, 100, 200}, dest);
    }

    @Test
    public void testIndexClamping() {
        StoredValue sv = new StoredValue.Unsigned(8);
        byte[] data = new byte[10];
        for (int i = 0; i < 10; i++) data[i] = (byte) i;
        // offset 100, so valid range is [100, 109]
        ByteLookupTable lut = new ByteLookupTable(sv, 8, 100, data);

        byte[] src = new byte[]{0, 100, 105, (byte) 255};
        byte[] dest = new byte[4];
        lut.lookup(src, 0, dest, 0, 4);

        // 0 -> index = 0 - 100 = -100 -> clamp to 0 -> lut[0] = 0
        // 100 -> index = 100 - 100 = 0 -> lut[0] = 0
        // 105 -> index = 105 - 100 = 5 -> lut[5] = 5
        // 255 -> index = 255 - 100 = 155 -> clamp to 9 -> lut[9] = 9
        assertArrayEquals(new byte[]{0, 0, 5, 9}, dest);
    }

    @Test
    public void testLookupRaster() {
        StoredValue sv = new StoredValue.Unsigned(8);
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) data[i] = (byte) (255 - i);
        ByteLookupTable lut = new ByteLookupTable(sv, 8, 0, data);

        WritableRaster src = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 2, 2, 1, null);
        src.setDataElements(0, 0, new byte[]{0});
        src.setDataElements(1, 0, new byte[]{10});
        src.setDataElements(0, 1, new byte[]{100});
        src.setDataElements(1, 1, new byte[]{(byte) 255});

        WritableRaster dest = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 2, 2, 1, null);

        lut.lookup(src, dest);

        byte[] destData = ((java.awt.image.DataBufferByte) dest.getDataBuffer()).getData();
        // 0 -> 255
        // 10 -> 245
        // 100 -> 155
        // 255 -> 0
        assertArrayEquals(new byte[]{(byte) 255, (byte) 245, (byte) 155, 0}, destData);
    }
}
