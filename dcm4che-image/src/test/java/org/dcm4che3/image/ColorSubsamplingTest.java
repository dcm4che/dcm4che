package org.dcm4che3.image;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ColorSubsamplingTest {

    @Test
    public void testYBR_XXX_422() {
        ColorSubsampling cs = ColorSubsampling.YBR_XXX_422;
        assertEquals(200, cs.frameLength(10, 10));

        // x=0, y=0 -> (10*0 + 0)*2 - (0%2) = 0
        assertEquals(0, cs.indexOfY(0, 0, 10));
        // x=1, y=0 -> (10*0 + 1)*2 - (1%2) = 2 - 1 = 1
        assertEquals(1, cs.indexOfY(1, 0, 10));
        // x=2, y=0 -> (10*0 + 2)*2 - (2%2) = 4 - 0 = 4
        assertEquals(4, cs.indexOfY(2, 0, 10));

        // x=0, y=0 -> (10*0*2) + ((0>>1)<<2) + 2 = 2
        assertEquals(2, cs.indexOfBR(0, 0, 10));
        // x=1, y=0 -> (10*0*2) + ((1>>1)<<2) + 2 = 2 (same as x=0)
        assertEquals(2, cs.indexOfBR(1, 0, 10));
        // x=2, y=0 -> (10*0*2) + ((2>>1)<<2) + 2 = 0 + 4 + 2 = 6
        assertEquals(6, cs.indexOfBR(2, 0, 10));
    }

    @Test
    public void testYBR_XXX_420() {
        ColorSubsampling cs = ColorSubsampling.YBR_XXX_420;
        assertEquals(150, cs.frameLength(10, 10));

        // y=0 (even)
        // x=0 -> 10 * (0*2+0) + (0*2 - 0) = 0
        assertEquals(0, cs.indexOfY(0, 0, 10));
        // x=1 -> 10 * (0*2+0) + (1*2 - 1) = 1
        assertEquals(1, cs.indexOfY(1, 0, 10));

        // y=1 (odd)
        // withoutBR = 0, withBR = 1
        // x=0 -> 10 * (1*2+0) + 0 = 20
        assertEquals(20, cs.indexOfY(0, 1, 10));
        // x=1 -> 10 * (1*2+0) + 1 = 21
        assertEquals(21, cs.indexOfY(1, 1, 10));

        // y=2 (even)
        // withoutBR = 1, withBR = 1
        // x=0 -> 10 * (1*2+1) + 0 = 30
        assertEquals(30, cs.indexOfY(0, 2, 10));

        // BR indices
        // y=0, x=0 -> 10 * 0 * 3 + 0 + 2 = 2
        assertEquals(2, cs.indexOfBR(0, 0, 10));
        // y=1, x=0 -> 10 * 0 * 3 + 0 + 2 = 2 (same as y=0)
        assertEquals(2, cs.indexOfBR(0, 1, 10));
        // y=2, x=0 -> 10 * 1 * 3 + 0 + 2 = 32
        assertEquals(32, cs.indexOfBR(0, 2, 10));
    }
}
