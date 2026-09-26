package org.dcm4che3.image;

import static org.junit.Assert.*;
import org.junit.Test;


public class PhotometricInterpretationTest {

    @Test
    public void testFromString() {
        assertEquals(PhotometricInterpretation.MONOCHROME1, PhotometricInterpretation.fromString("MONOCHROME1"));
        assertEquals(PhotometricInterpretation.RGB, PhotometricInterpretation.fromString("RGB"));
        assertEquals(PhotometricInterpretation.YBR_FULL, PhotometricInterpretation.fromString("YBR_FULL"));
    }

    @Test
    public void testIsMonochrome() {
        assertTrue(PhotometricInterpretation.MONOCHROME1.isMonochrome());
        assertTrue(PhotometricInterpretation.MONOCHROME2.isMonochrome());
        assertFalse(PhotometricInterpretation.RGB.isMonochrome());
    }

    @Test
    public void testIsYBR() {
        assertTrue(PhotometricInterpretation.YBR_FULL.isYBR());
        assertTrue(PhotometricInterpretation.YBR_RCT.isYBR());
        assertFalse(PhotometricInterpretation.RGB.isYBR());
    }

    @Test
    public void testIsInverse() {
        assertTrue(PhotometricInterpretation.MONOCHROME1.isInverse());
        assertFalse(PhotometricInterpretation.MONOCHROME2.isInverse());
    }

    @Test
    public void testFrameLength() {
        // MONOCHROME2, 10x10, 1 sample, 8 bits -> 100 bytes
        assertEquals(100, PhotometricInterpretation.MONOCHROME2.frameLength(10, 10, 1, 8));
        // RGB, 10x10, 3 samples, 8 bits -> 300 bytes
        assertEquals(300, PhotometricInterpretation.RGB.frameLength(10, 10, 3, 8));
        // MONOCHROME2, 10x10, 1 sample, 16 bits -> 200 bytes
        assertEquals(200, PhotometricInterpretation.MONOCHROME2.frameLength(10, 10, 1, 16));
    }
}
