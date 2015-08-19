package org.dcm4che3.util;

import static org.junit.Assert.*;

import org.dcm4che3.util.StringUtils;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class StringUtilsTest {

    @Test
    public void testMatches() {
        assertTrue(StringUtils.matches("aBcD", "aBcD", false, false));
        assertFalse(StringUtils.matches("aBcD", "abCd", false, false));
        assertTrue(StringUtils.matches("aBcD", "abCd", false, true));
        assertFalse(StringUtils.matches("aBcD", "ab*", false, false));
        assertTrue(StringUtils.matches("aBcD", "ab*", false, true));
        assertTrue(StringUtils.matches("aBcD", "a?c?", false, false));
        assertTrue(StringUtils.matches("aBcD", "a*D*", false, false));
        assertFalse(StringUtils.matches("aBcD", "a*d?", false, true));
    }

}
