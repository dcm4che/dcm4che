package org.dcm4che3.image;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;


public class YBRTest {

    private static final float EPSILON = 0.001f;

    @Test
    public void testFull() {
        YBR ybr = YBR.FULL;
        // Black (0,0,0) RGB -> (0, 0.5, 0.5) YBR
        assertArrayEquals(new float[]{0f, 0.5f, 0.5f}, ybr.fromRGB(new float[]{0f, 0f, 0f}), EPSILON);
        // White (1,1,1) RGB -> (1, 0.5, 0.5) YBR
        assertArrayEquals(new float[]{1f, 0.5f, 0.5f}, ybr.fromRGB(new float[]{1f, 1f, 1f}), EPSILON);

        // Back to RGB
        assertArrayEquals(new float[]{0f, 0f, 0f}, ybr.toRGB(new float[]{0f, 0.5f, 0.5f}), EPSILON);
        assertArrayEquals(new float[]{1f, 1f, 1f}, ybr.toRGB(new float[]{1f, 0.5f, 0.5f}), EPSILON);
    }

    @Test
    public void testPartial() {
        YBR ybr = YBR.PARTIAL;
        // Test values can be verified against standard formulas or just ensure they are consistent
        float[] blackYBR = ybr.fromRGB(new float[]{0f, 0f, 0f});
        assertArrayEquals(new float[]{0f, 0f, 0f}, ybr.toRGB(blackYBR), EPSILON);

        float[] whiteRGB = new float[]{1f, 1f, 1f};
        float[] whiteYBR = ybr.fromRGB(whiteRGB);
        assertArrayEquals(whiteRGB, ybr.toRGB(whiteYBR), EPSILON);
    }
}
