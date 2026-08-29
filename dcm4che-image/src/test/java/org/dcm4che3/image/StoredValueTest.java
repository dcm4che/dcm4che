package org.dcm4che3.image;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class StoredValueTest {

    @Test
    public void testUnsigned8() {
        StoredValue sv = new StoredValue.Unsigned(8);
        assertEquals(0, sv.minValue());
        assertEquals(255, sv.maxValue());
        assertEquals(0, sv.valueOf(0));
        assertEquals(255, sv.valueOf(255));
        assertEquals(0, sv.valueOf(256));
        assertEquals(1, sv.valueOf(257));
    }

    @Test
    public void testUnsigned12() {
        StoredValue sv = new StoredValue.Unsigned(12);
        assertEquals(0, sv.minValue());
        assertEquals(4095, sv.maxValue());
        assertEquals(4095, sv.valueOf(4095));
        assertEquals(0, sv.valueOf(4096));
    }

    @Test
    public void testSigned8() {
        StoredValue sv = new StoredValue.Signed(8);
        assertEquals(-128, sv.minValue());
        assertEquals(127, sv.maxValue());
        assertEquals(0, sv.valueOf(0));
        assertEquals(127, sv.valueOf(127));
        assertEquals(-128, sv.valueOf(128));
        assertEquals(-1, sv.valueOf(255));
    }

    @Test
    public void testSigned16() {
        StoredValue sv = new StoredValue.Signed(16);
        assertEquals(-32768, sv.minValue());
        assertEquals(32767, sv.maxValue());
        assertEquals(32767, sv.valueOf(0x7FFF));
        assertEquals(-32768, sv.valueOf(0x8000));
        assertEquals(-1, sv.valueOf(0xFFFF));
    }

    @Test
    public void testValueOfAttributes() {
        Attributes attrs = new Attributes();
        attrs.setInt(Tag.BitsStored, VR.US, 12);
        attrs.setInt(Tag.PixelRepresentation, VR.US, 0);
        StoredValue sv = StoredValue.valueOf(attrs);
        assertTrue(sv instanceof StoredValue.Unsigned);
        assertEquals(4095, sv.maxValue());

        attrs.setInt(Tag.PixelRepresentation, VR.US, 1);
        sv = StoredValue.valueOf(attrs);
        assertTrue(sv instanceof StoredValue.Signed);
        assertEquals(2047, sv.maxValue());
        assertEquals(-2048, sv.minValue());
    }

    @Test
    public void testValueOfAttributesDefaultBitsStored() {
        Attributes attrs = new Attributes();
        attrs.setInt(Tag.BitsAllocated, VR.US, 16);
        attrs.setInt(Tag.PixelRepresentation, VR.US, 0);
        StoredValue sv = StoredValue.valueOf(attrs);
        assertEquals(65535, sv.maxValue());
    }
}
