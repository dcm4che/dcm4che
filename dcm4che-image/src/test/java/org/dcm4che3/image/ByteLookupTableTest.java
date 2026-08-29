package org.dcm4che3.image;

import org.junit.Test;
import static org.junit.Assert.*;

public class ByteLookupTableTest {

    @Test
    public void testConstructorAndLength() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        byte[] lut = new byte[256];
        ByteLookupTable blt = new ByteLookupTable(inBits, 8, 0, lut);
        assertEquals(256, blt.length());
    }

    @Test
    public void testSlopeConstructor() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        // ByteLookupTable(StoredValue inBits, int outBits, int minOut, int maxOut, int offset, int size, boolean flip)
        ByteLookupTable blt = new ByteLookupTable(inBits, 8, 0, 255, 0, 256, false);
        assertEquals(256, blt.length());
        byte[] dest = new byte[1];
        blt.lookup(new byte[]{0}, 0, dest, 0, 1);
        assertEquals(0, dest[0]);
        blt.lookup(new byte[]{(byte) 255}, 0, dest, 0, 1);
        assertEquals((byte) 255, dest[0]);
        
        // Test mid value
        blt.lookup(new byte[]{127}, 0, dest, 0, 1);
        // (127 * 255 + 127) / 255 + 0 = 127 + 127/255 = 127
        assertEquals(127, dest[0]);
    }

    @Test
    public void testSlopeConstructorFlip() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        ByteLookupTable blt = new ByteLookupTable(inBits, 8, 0, 255, 0, 256, true);
        byte[] dest = new byte[1];
        blt.lookup(new byte[]{0}, 0, dest, 0, 1);
        assertEquals((byte) 255, dest[0]);
        blt.lookup(new byte[]{(byte) 255}, 0, dest, 0, 1);
        assertEquals(0, dest[0]);
    }
    
    @Test
    public void testSlopeConstructorMinMaxEqual() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        ByteLookupTable blt = new ByteLookupTable(inBits, 8, 128, 128, 0, 256, false);
        assertEquals(1, blt.length());
        byte[] dest = new byte[1];
        blt.lookup(new byte[]{0}, 0, dest, 0, 1);
        assertEquals((byte) 128, dest[0]);
        blt.lookup(new byte[]{(byte) 255}, 0, dest, 0, 1);
        assertEquals((byte) 128, dest[0]);
    }

    @Test
    public void testLookupByteToByte() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        byte[] lut = new byte[256];
        for (int i = 0; i < 256; i++) lut[i] = (byte) (255 - i);
        ByteLookupTable blt = new ByteLookupTable(inBits, 8, 0, lut);

        byte[] src = {0, 1, (byte) 255};
        byte[] dest = new byte[3];
        blt.lookup(src, 0, dest, 0, 3);
        assertArrayEquals(new byte[]{(byte) 255, (byte) 254, 0}, dest);
    }

    @Test
    public void testLookupShortToByte() {
        StoredValue inBits = new StoredValue.Unsigned(16);
        byte[] lut = new byte[256];
        for (int i = 0; i < 256; i++) lut[i] = (byte) i;
        ByteLookupTable blt = new ByteLookupTable(inBits, 8, 0, lut);

        short[] src = {0, 1, 255, 256}; // 256 should be clamped
        byte[] dest = new byte[4];
        blt.lookup(src, 0, dest, 0, 4);
        assertArrayEquals(new byte[]{0, 1, (byte) 255, (byte) 255}, dest);
    }

    @Test
    public void testLookupByteToShort() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        byte[] lut = new byte[256];
        for (int i = 0; i < 256; i++) lut[i] = (byte) i;
        ByteLookupTable blt = new ByteLookupTable(inBits, 8, 0, lut);

        byte[] src = {0, 1, (byte) 255};
        short[] dest = new short[3];
        blt.lookup(src, 0, dest, 0, 3);
        assertArrayEquals(new short[]{0, 1, 255}, dest);
    }

    @Test
    public void testLookupShortToShort() {
        StoredValue inBits = new StoredValue.Unsigned(16);
        byte[] lut = new byte[256];
        for (int i = 0; i < 256; i++) lut[i] = (byte) i;
        ByteLookupTable blt = new ByteLookupTable(inBits, 8, 0, lut);

        short[] src = {0, 1, 255, 500};
        short[] dest = new short[4];
        blt.lookup(src, 0, dest, 0, 4);
        assertArrayEquals(new short[]{0, 1, 255, 255}, dest);
    }

    @Test
    public void testAdjustOutBitsUpscale() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        byte[] lut = new byte[256];
        for (int i = 0; i < 256; i++) lut[i] = (byte) i;
        ByteLookupTable blt = new ByteLookupTable(inBits, 8, 0, lut);

        LookupTable adjusted = blt.adjustOutBits(10);
        assertTrue(adjusted instanceof ShortLookupTable);
        assertEquals(10, adjusted.outBits);
        
        short[] dest = new short[1];
        adjusted.lookup(new byte[]{1}, 0, dest, 0, 1);
        assertEquals(4, dest[0]); // 1 << 2
    }

    @Test
    public void testAdjustOutBitsDownscale() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        byte[] lut = new byte[256];
        for (int i = 0; i < 256; i++) lut[i] = (byte) 0xFF;
        ByteLookupTable blt = new ByteLookupTable(inBits, 8, 0, lut);

        LookupTable adjusted = blt.adjustOutBits(4);
        assertSame(blt, adjusted);
        assertEquals(4, adjusted.outBits);
        
        byte[] dest = new byte[1];
        blt.lookup(new byte[]{0}, 0, dest, 0, 1);
        assertEquals(0x0F, dest[0]); // 0xFF >> 4
    }
    
    @Test
    public void testAdjustOutBitsLeftShift() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        byte[] lut = new byte[256];
        lut[1] = 1;
        ByteLookupTable blt = new ByteLookupTable(inBits, 4, 0, lut);

        LookupTable adjusted = blt.adjustOutBits(8);
        assertSame(blt, adjusted);
        assertEquals(8, adjusted.outBits);
        
        byte[] dest = new byte[1];
        blt.lookup(new byte[]{1}, 0, dest, 0, 1);
        assertEquals(16, dest[0]); // 1 << 4
    }

    @Test
    public void testInverse() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        byte[] lut = new byte[256];
        lut[0] = 0;
        lut[255] = (byte) 255;
        ByteLookupTable blt = new ByteLookupTable(inBits, 8, 0, lut);

        blt.inverse();
        
        byte[] dest = new byte[1];
        blt.lookup(new byte[]{0}, 0, dest, 0, 1);
        assertEquals((byte) 255, dest[0]);
        blt.lookup(new byte[]{(byte) 255}, 0, dest, 0, 1);
        assertEquals(0, dest[0]);
    }

    @Test
    public void testCombineByte() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        byte[] lut1 = new byte[256];
        for (int i = 0; i < 256; i++) lut1[i] = (byte) i;
        ByteLookupTable blt1 = new ByteLookupTable(inBits, 8, 0, lut1);

        byte[] lut2 = new byte[256];
        for (int i = 0; i < 256; i++) lut2[i] = (byte) (255 - i);
        ByteLookupTable blt2 = new ByteLookupTable(inBits, 8, 0, lut2);

        LookupTable combined = blt1.combine(blt2);
        assertSame(blt1, combined);
        
        byte[] dest = new byte[1];
        combined.lookup(new byte[]{0}, 0, dest, 0, 1);
        assertEquals((byte) 255, dest[0]);
    }

    @Test
    public void testCombineShort() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        byte[] lut1 = new byte[256];
        for (int i = 0; i < 256; i++) lut1[i] = (byte) i;
        ByteLookupTable blt1 = new ByteLookupTable(inBits, 8, 0, lut1);

        short[] lut2 = new short[256];
        for (int i = 0; i < 256; i++) lut2[i] = (short) (i + 1000);
        ShortLookupTable slt2 = new ShortLookupTable(inBits, 16, 0, lut2);

        LookupTable combined = blt1.combine(slt2);
        assertTrue(combined instanceof ShortLookupTable);
        assertEquals(16, combined.outBits);
        
        short[] dest = new short[1];
        combined.lookup(new byte[]{10}, 0, dest, 0, 1);
        assertEquals(1010, dest[0]);
    }
    
    @Test
    public void testIndexWithOffset() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        byte[] lut = new byte[10];
        for (int i = 0; i < 10; i++) lut[i] = (byte) i;
        // offset 100, lut size 10
        ByteLookupTable blt = new ByteLookupTable(inBits, 8, 100, lut);
        
        byte[] dest = new byte[1];
        blt.lookup(new byte[]{100}, 0, dest, 0, 1);
        assertEquals(0, dest[0]);
        
        blt.lookup(new byte[]{105}, 0, dest, 0, 1);
        assertEquals(5, dest[0]);
        
        blt.lookup(new byte[]{109}, 0, dest, 0, 1);
        assertEquals(9, dest[0]);
        
        // Underflow
        blt.lookup(new byte[]{50}, 0, dest, 0, 1);
        assertEquals(0, dest[0]);
        
        // Overflow
        blt.lookup(new byte[]{(byte) 200}, 0, dest, 0, 1);
        assertEquals(9, dest[0]);
    }

    @Test
    public void testSignedStoredValue() {
        StoredValue inBits = new StoredValue.Signed(8);
        byte[] lut = new byte[256];
        for (int i = 0; i < 256; i++) lut[i] = (byte) i;
        // offset -128, size 256. 
        // inBits.valueOf(pixel) for signed 8-bit will return -128 to 127.
        // index = valueOf(pixel) - offset = valueOf(pixel) - (-128) = valueOf(pixel) + 128.
        // pixel -128 -> index 0
        // pixel 0 -> index 128
        // pixel 127 -> index 255
        ByteLookupTable blt = new ByteLookupTable(inBits, 8, -128, lut);

        byte[] dest = new byte[1];
        blt.lookup(new byte[]{(byte) -128}, 0, dest, 0, 1);
        assertEquals(0, dest[0]);

        blt.lookup(new byte[]{0}, 0, dest, 0, 1);
        assertEquals((byte) 128, dest[0]);

        blt.lookup(new byte[]{127}, 0, dest, 0, 1);
        assertEquals((byte) 255, dest[0]);
    }
}
