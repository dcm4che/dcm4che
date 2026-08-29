package org.dcm4che3.util;

import static org.junit.Assert.*;
import org.junit.Test;

public class ArrayUtilsTest {

    @Test
    public void testLongsToInts() {
        long[] longs = {1L, 2L, (long)Integer.MAX_VALUE, (long)Integer.MIN_VALUE};
        int[] expected = {1, 2, Integer.MAX_VALUE, Integer.MIN_VALUE};
        assertArrayEquals(expected, ArrayUtils.longsToInts(longs));
    }

    @Test
    public void testIntsToLongs() {
        int[] ints = {1, 2, Integer.MAX_VALUE, Integer.MIN_VALUE};
        long[] expected = {1L, 2L, (long)Integer.MAX_VALUE, (long)Integer.MIN_VALUE};
        assertArrayEquals(expected, ArrayUtils.intsToLong(ints));
    }

    @Test
    public void testEmptyArrays() {
        assertArrayEquals(new int[0], ArrayUtils.longsToInts(new long[0]));
        assertArrayEquals(new long[0], ArrayUtils.intsToLong(new int[0]));
    }
}
