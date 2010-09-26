package org.dcm4che.util;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

public class DateUtilsTest {

    private final long SECOND = 1000L;
    private final long MINUTE = 60 * SECOND;
    private final long HOUR = 60 * MINUTE;
    private final long DAY = 24 * HOUR;
    private final long YEAR = 365 * DAY;

    @Before
    public void setUp() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+2"));
    }

    @Test
    public void testFormatDA() {
        assertEquals("19700101", DateUtils.formatDA(new Date(0)));
    }

    @Test
    public void testFormatTM() {
        assertEquals("020000.000", DateUtils.formatTM(new Date(0)));
    }

    @Test
    public void testFormatDT() {
        assertEquals("19700101020000.000", DateUtils.formatDT(new Date(0)));
    }

    @Test
    public void testFormatDTwithTZ() {
        assertEquals("19700101020000.000+0200",
                DateUtils.formatDT(new Date(0), Calendar.MILLISECOND, true));
    }

    @Test
    public void testParseDA() {
        assertEquals(-2 * HOUR,
                DateUtils.parseDA("19700101").getTime());
    }

    @Test
    public void testParseDAacrnema() {
        assertEquals(-2 * HOUR,
                DateUtils.parseDA("1970.01.01").getTime());
    }

    @Test
    public void testParseDAceil() {
        assertEquals(DAY - 2 * HOUR - 1,
                DateUtils.parseDA("19700101", true).getTime());
    }

    @Test
    public void testParseTM() {
        assertEquals(0,
                DateUtils.parseTM("020000.000").getTime());
    }

    @Test
    public void testParseTMacrnema() {
        assertEquals(0,
                DateUtils.parseTM("02:00:00").getTime());
    }

    @Test
    public void testParseTMceil() {
        assertEquals(MINUTE - 1,
                DateUtils.parseTM("0200", true).getTime());
    }

    @Test
    public void testParseDT() {
        assertEquals(0,
                DateUtils.parseDT("19700101020000.000").getTime());
    }

    @Test
    public void testParseWithTZ() {
        assertEquals(2 * HOUR,
                DateUtils.parseDT("19700101020000.000+0000").getTime());
    }

    @Test
    public void testParseDTceil() {
        assertEquals(YEAR - 2 * HOUR - 1,
                DateUtils.parseDT("1970", true).getTime());
    }

}
