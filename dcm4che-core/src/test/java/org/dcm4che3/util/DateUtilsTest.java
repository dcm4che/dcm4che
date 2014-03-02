package org.dcm4che3.util;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.dcm4che3.data.DatePrecision;
import org.dcm4che3.util.DateUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class DateUtilsTest {

    private final long SECOND = 1000L;
    private final long MINUTE = 60 * SECOND;
    private final long HOUR = 60 * MINUTE;
    private final long DAY = 24 * HOUR;
    private final long YEAR = 365 * DAY;
    private TimeZone tz;
    
    @Before
    public void setUp() throws Exception {
        tz = DateUtils.timeZone("+0200");
    }

    @Test
    public void testFormatDA() {
        assertEquals("19700101", DateUtils.formatDA(tz, new Date(0)));
    }

    @Test
    public void testFormatTM() {
        assertEquals("020000.000", DateUtils.formatTM(tz, new Date(0)));
    }

    @Test
    public void testFormatDT() {
        assertEquals("19700101020000.000", DateUtils.formatDT(tz, new Date(0)));
    }

    @Test
    public void testFormatDTwithTZ() {
        assertEquals("19700101020000.000+0200",
                DateUtils.formatDT(tz, new Date(0),
                        new DatePrecision(Calendar.MILLISECOND, true)));
    }

    @Test
    public void testParseDA() {
        assertEquals(-2 * HOUR,
                DateUtils.parseDA(tz, "19700101").getTime());
    }

    @Test
    public void testParseDAacrnema() {
        assertEquals(-2 * HOUR,
                DateUtils.parseDA(tz, "1970.01.01").getTime());
    }

    @Test
    public void testParseDAceil() {
        assertEquals(DAY - 2 * HOUR - 1,
                DateUtils.parseDA(tz, "19700101", true).getTime());
    }

    @Test
    public void testParseTM() {
        DatePrecision precision = new DatePrecision();
        assertEquals(0,
                DateUtils.parseTM(tz, "020000.000", precision).getTime());
        assertEquals(Calendar.MILLISECOND, precision.lastField);
    }

    @Test
    public void testParseTMacrnema() {
        DatePrecision precision = new DatePrecision();
        assertEquals(0,
                DateUtils.parseTM(tz, "02:00:00", precision).getTime());
        assertEquals(Calendar.SECOND, precision.lastField);
    }

    @Test
    public void testParseTMceil() {
        DatePrecision precision = new DatePrecision();
        assertEquals(MINUTE - 1,
                DateUtils.parseTM(tz, "0200", true, precision).getTime());
        assertEquals(Calendar.MINUTE, precision.lastField);
    }

    @Test
    public void testParseDT() {
        DatePrecision precision = new DatePrecision();
        assertEquals(0,
                DateUtils.parseDT(tz, "19700101020000.000", precision).getTime());
        assertEquals(Calendar.MILLISECOND, precision.lastField);
        assertFalse(precision.includeTimezone);
    }

    @Test
    public void testParseWithTZ() {
        DatePrecision precision = new DatePrecision();
        assertEquals(2 * HOUR,
                DateUtils.parseDT(tz, "19700101020000.000+0000", precision).getTime());
        assertEquals(Calendar.MILLISECOND, precision.lastField);
        assertTrue(precision.includeTimezone);
    }

    @Test
    public void testParseDTceil() {
        DatePrecision precision = new DatePrecision();
        assertEquals(YEAR - 2 * HOUR - 1,
                DateUtils.parseDT(tz, "1970", true, precision).getTime());
        assertEquals(Calendar.YEAR, precision.lastField);
        assertFalse(precision.includeTimezone);
    }

}
