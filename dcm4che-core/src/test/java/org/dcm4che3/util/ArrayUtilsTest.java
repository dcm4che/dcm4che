package org.dcm4che3.util;

import org.junit.Test;
import static org.junit.Assert.*;


public class ArrayUtilsTest {

    @Test
    public void testLongsToInts() {
        long[] in = {1L, 2L, 3L, 4294967296L}; // 4294967296L is 2^32, should become 0 when cast to int
        int[] expected = {1, 2, 3, 0};
        assertArrayEquals(expected, ArrayUtils.longsToInts(in));
    }

    @Test
    public void testLongsToIntsEmpty() {
        assertArrayEquals(new int[0], ArrayUtils.longsToInts(new long[0]));
    }

    @Test
    public void testIntsToLongs() {
        int[] in = {1, 2, 3, -1};
        long[] expected = {1L, 2L, 3L, -1L};
        assertArrayEquals(expected, ArrayUtils.intsToLong(in));
    }

    @Test
    public void testIntsToLongsEmpty() {
        assertArrayEquals(new long[0], ArrayUtils.intsToLong(new int[0]));
    }
}
