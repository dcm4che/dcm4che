package org.dcm4che3.image;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class LookupTableTest {

    @Test
    public void testByteLookupTable() {
        StoredValue sv = new StoredValue.Unsigned(8);
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) data[i] = (byte) (255 - i);
        
        ByteLookupTable lut = new ByteLookupTable(sv, 8, 0, data);
        
        byte[] src = new byte[]{0, 127, (byte) 255};
        byte[] dest = new byte[3];
        lut.lookup(src, 0, dest, 0, 3);
        
        assertArrayEquals(new byte[]{(byte) 255, (byte) 128, 0}, dest);
    }

    @Test
    public void testShortLookupTable() {
        StoredValue sv = new StoredValue.Unsigned(8);
        short[] data = new short[256];
        for (int i = 0; i < 256; i++) data[i] = (short) i;
        
        ShortLookupTable lut = new ShortLookupTable(sv, 16, 0, data);
        
        byte[] src = new byte[]{0, 127, (byte) 255};
        short[] dest = new short[3];
        lut.lookup(src, 0, dest, 0, 3);
        
        assertArrayEquals(new short[]{0, 127, 255}, dest);
    }

    @Test
    public void testAdjustOutBits() {
        StoredValue sv = new StoredValue.Unsigned(8);
        byte[] data = new byte[]{0, 1, 2, 3};
        ByteLookupTable lut = new ByteLookupTable(sv, 2, 0, data);
        
        LookupTable adjusted = lut.adjustOutBits(4);
        assertTrue(adjusted instanceof ByteLookupTable);
        assertEquals(4, adjusted.outBits);
        
        byte[] dest = new byte[4];
        adjusted.lookup(new byte[]{0, 1, 2, 3}, 0, dest, 0, 4);
        assertArrayEquals(new byte[]{0, 4, 8, 12}, dest);
        
        adjusted = adjusted.adjustOutBits(10);
        assertTrue(adjusted instanceof ShortLookupTable);
        assertEquals(10, adjusted.outBits);
    }

    @Test
    public void testInverse() {
        StoredValue sv = new StoredValue.Unsigned(8);
        byte[] data = new byte[]{0, 64, (byte) 128, (byte) 255};
        ByteLookupTable lut = new ByteLookupTable(sv, 8, 0, data);
        
        lut.inverse();
        byte[] dest = new byte[4];
        lut.lookup(new byte[]{0, 1, 2, 3}, 0, dest, 0, 4);
        assertArrayEquals(new byte[]{(byte) 255, (byte) 191, (byte) 127, 0}, dest);
    }

    @Test
    public void testCombine() {
        StoredValue sv = new StoredValue.Unsigned(8);
        byte[] data1 = new byte[256];
        byte[] data2 = new byte[256];
        for (int i = 0; i < 256; i++) {
            data1[i] = (byte) (i / 2); // 8 -> 7 bits
            data2[i] = (byte) (i * 2); // 7 -> 8 bits
        }
        
        ByteLookupTable lut1 = new ByteLookupTable(sv, 7, 0, data1);
        ByteLookupTable lut2 = new ByteLookupTable(new StoredValue.Unsigned(7), 8, 0, data2);
        
        lut1.combine(lut2);
        byte[] dest = new byte[3];
        lut1.lookup(new byte[]{0, 100, (byte) 200}, 0, dest, 0, 3);
        
        // 0 -> 0/2=0 -> 0*2=0
        // 100 -> 100/2=50 -> 50*2=100
        // 200 -> 200/2=100 -> 100*2=200
        assertArrayEquals(new byte[]{0, 100, (byte) 200}, dest);
    }
}
