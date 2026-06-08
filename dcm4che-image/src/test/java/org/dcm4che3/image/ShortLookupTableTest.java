package org.dcm4che3.image;

import org.junit.Test;
import static org.junit.Assert.*;

public class ShortLookupTableTest {

    @Test
    public void testStandardConstructor() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        short[] lut = new short[256];
        for (int i = 0; i < 256; i++) {
            lut[i] = (short) i;
        }
        ShortLookupTable table = new ShortLookupTable(inBits, 8, 0, lut);
        
        assertEquals(256, table.length());
        assertEquals(8, table.outBits);
        assertEquals(0, table.offset);
    }

    @Test
    public void testSlopeConstructor() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        // minOut=0, maxOut=100, size=256
        ShortLookupTable table = new ShortLookupTable(inBits, 8, 0, 100, 0, 256, false);
        
        assertEquals(256, table.length());
        assertEquals(0, lookup(table, 0));
        assertEquals(100, lookup(table, 255));
        assertEquals(50, lookup(table, 127), 1); // Mid point
    }

    @Test
    public void testSlopeConstructorFlip() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        ShortLookupTable table = new ShortLookupTable(inBits, 8, 0, 100, 0, 256, true);
        
        assertEquals(100, lookup(table, 0));
        assertEquals(0, lookup(table, 255));
    }

    @Test
    public void testSlopeConstructorSingleValue() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        ShortLookupTable table = new ShortLookupTable(inBits, 8, 50, 50, 0, 256, false);
        
        assertEquals(1, table.length());
        assertEquals(50, lookup(table, 0));
        assertEquals(50, lookup(table, 255));
    }

    @Test
    public void testLookupByteToByte() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        short[] lut = new short[256];
        for (int i = 0; i < 256; i++) lut[i] = (short) (i / 2);
        ShortLookupTable table = new ShortLookupTable(inBits, 8, 0, lut);

        byte[] src = {0, (byte) 128, (byte) 255};
        byte[] dest = new byte[3];
        table.lookup(src, 0, dest, 0, 3);

        assertEquals(0, dest[0]);
        assertEquals(64, dest[1]);
        assertEquals(127, dest[2]);
    }

    @Test
    public void testLookupShortToByte() {
        StoredValue inBits = new StoredValue.Unsigned(16);
        short[] lut = new short[65536];
        for (int i = 0; i < 65536; i++) lut[i] = (short) (i >> 8);
        ShortLookupTable table = new ShortLookupTable(inBits, 8, 0, lut);

        short[] src = {0, (short) 32768, (short) 65535};
        byte[] dest = new byte[3];
        table.lookup(src, 0, dest, 0, 3);

        assertEquals(0, dest[0]);
        assertEquals((byte) 128, dest[1]);
        assertEquals((byte) 255, dest[2]);
    }

    @Test
    public void testLookupByteToShort() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        short[] lut = new short[256];
        for (int i = 0; i < 256; i++) lut[i] = (short) (i * 10);
        ShortLookupTable table = new ShortLookupTable(inBits, 16, 0, lut);

        byte[] src = {0, 10, (byte) 255};
        short[] dest = new short[3];
        table.lookup(src, 0, dest, 0, 3);

        assertEquals(0, dest[0]);
        assertEquals(100, dest[1]);
        assertEquals(2550, dest[2]);
    }

    @Test
    public void testLookupShortToShort() {
        StoredValue inBits = new StoredValue.Unsigned(16);
        short[] lut = new short[65536];
        for (int i = 0; i < 65536; i++) lut[i] = (short) (i - 32768);
        ShortLookupTable table = new ShortLookupTable(inBits, 16, 0, lut);

        short[] src = {0, (short) 32768, (short) 65535};
        short[] dest = new short[3];
        table.lookup(src, 0, dest, 0, 3);

        assertEquals((short) -32768, dest[0]);
        assertEquals(0, dest[1]);
        assertEquals(32767, dest[2]);
    }

    @Test
    public void testAdjustOutBitsUp() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        short[] lut = {0, 1, 2, 3};
        ShortLookupTable table = new ShortLookupTable(inBits, 2, 0, lut);

        table.adjustOutBits(4);
        assertEquals(4, table.outBits);
        assertEquals(0, lookup(table, 0));
        assertEquals(4, lookup(table, 1));
        assertEquals(8, lookup(table, 2));
        assertEquals(12, lookup(table, 3));
    }

    @Test
    public void testAdjustOutBitsDown() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        short[] lut = {0, 16, 32, 64};
        ShortLookupTable table = new ShortLookupTable(inBits, 8, 0, lut);

        table.adjustOutBits(4);
        assertEquals(4, table.outBits);
        assertEquals(0, lookup(table, 0));
        assertEquals(1, lookup(table, 1));
        assertEquals(2, lookup(table, 2));
        assertEquals(4, lookup(table, 3));
    }

    @Test
    public void testInverse() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        short[] lut = {0, 10, 20, 30};
        ShortLookupTable table = new ShortLookupTable(inBits, 8, 0, lut);

        table.inverse();
        int maxOut = 255;
        assertEquals(maxOut, lookup(table, 0));
        assertEquals(maxOut - 10, lookup(table, 1));
        assertEquals(maxOut - 20, lookup(table, 2));
        assertEquals(maxOut - 30, lookup(table, 3));
    }

    @Test
    public void testCombine() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        short[] lut1 = {0, 1, 2, 3};
        ShortLookupTable table1 = new ShortLookupTable(inBits, 8, 0, lut1);

        short[] lut2 = {10, 20, 30, 40};
        ShortLookupTable table2 = new ShortLookupTable(inBits, 8, 0, lut2);

        table1.combine(table2);
        assertEquals(10, lookup(table1, 0));
        assertEquals(20, lookup(table1, 1));
        assertEquals(30, lookup(table1, 2));
        assertEquals(40, lookup(table1, 3));
    }

    @Test
    public void testIndexClamping() {
        StoredValue inBits = new StoredValue.Unsigned(8);
        short[] lut = {10, 20}; // length 2
        // offset 100
        ShortLookupTable table = new ShortLookupTable(inBits, 8, 100, lut);

        // pixel 100 -> index 0 -> 10
        assertEquals(10, lookup(table, 100));
        // pixel 101 -> index 1 -> 20
        assertEquals(20, lookup(table, 101));
        // pixel 99 -> index -1 -> clamped to 0 -> 10
        assertEquals(10, lookup(table, 99));
        // pixel 102 -> index 2 -> clamped to 1 -> 20
        assertEquals(20, lookup(table, 102));
    }

    @Test
    public void testSignedStoredValue() {
        StoredValue inBits = new StoredValue.Signed(8);
        short[] lut = new short[256];
        for (int i = 0; i < 256; i++) lut[i] = (short) i;
        // offset -128
        ShortLookupTable table = new ShortLookupTable(inBits, 8, -128, lut);

        // StoredValue.Signed(8) valueOf:
        // 0 -> 0
        // 127 -> 127
        // 128 (0x80) -> -128
        // 255 (0xFF) -> -1
        
        // pixel 0 -> value 0 -> index 0 - (-128) = 128
        assertEquals(128, lookup(table, 0));
        // pixel 127 -> value 127 -> index 127 + 128 = 255
        assertEquals(255, lookup(table, 127));
        // pixel 128 -> value -128 -> index -128 + 128 = 0
        assertEquals(0, lookup(table, 128));
        // pixel 255 -> value -1 -> index -1 + 128 = 127
        assertEquals(127, lookup(table, 255));
    }

    // Helper for easier assertions
    private int lookup(ShortLookupTable table, int pixel) {
        short[] src = {(short) pixel};
        short[] dest = new short[1];
        table.lookup(src, 0, dest, 0, 1);
        return dest[0] & 0xffff;
    }
}
