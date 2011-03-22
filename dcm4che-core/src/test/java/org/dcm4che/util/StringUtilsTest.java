package org.dcm4che.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testMatches() {
        assertTrue(StringUtils.matches("aBcD", "aBcD", false));
        assertFalse(StringUtils.matches("aBcD", "abCd", false));
        assertTrue(StringUtils.matches("aBcD", "abCd", true));
        assertFalse(StringUtils.matches("aBcD", "ab*", false));
        assertTrue(StringUtils.matches("aBcD", "ab*", true));
        assertTrue(StringUtils.matches("aBcD", "a?c?", false));
        assertTrue(StringUtils.matches("aBcD", "a*D*", false));
        assertFalse(StringUtils.matches("aBcD", "a*d?", true));
    }

}
