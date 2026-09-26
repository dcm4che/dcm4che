package org.dcm4che3.util;

import static org.junit.Assert.*;
import org.junit.Test;

public class RegExTest {

    @Test
    public void testMatch() {
        RegEx regex = new RegEx("a*b");
        assertTrue(regex.match("b"));
        assertTrue(regex.match("ab"));
        assertTrue(regex.match("aaab"));
        assertFalse(regex.match("abc"));
        assertFalse(regex.match("ba"));
    }
}
