package org.dcm4che3.util;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.dcm4che3.data.DatePrecision;
import org.dcm4che3.util.DateUtils;
import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import java.time.DateTimeException;

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
    public void testParseLocalDA() {
        assertEquals(LocalDate.of(1970, 1, 1), DateUtils.parseLocalDA("19700101"));
        assertEquals(LocalDate.of(1970, 1, 1), DateUtils.parseLocalDA("1970.01.01"));
        assertEquals(LocalDate.of(1970, 1, 1), DateUtils.parseLocalDA("1970-01-01"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLocalDA_MixedSeparators() {
        DateUtils.parseLocalDA("1970.01-01");
    }

    @Test(expected = DateTimeException.class)
    public void testParseLocalDA_InvalidDate() {
        DateUtils.parseLocalDA("20230229");
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
    public void testParseLocalTM() {
        DatePrecision precision = new DatePrecision();
        assertEquals(LocalTime.of(2,0,0,0), DateUtils.parseLocalTM( "020000.000", precision));
        assertEquals(Calendar.MILLISECOND, precision.lastField);
    }

    @Test
    public void testParseLocalTMnanos() {
        DatePrecision precision = new DatePrecision();
        assertEquals(LocalTime.of(2,0,0,1000), DateUtils.parseLocalTM( "020000.000001", precision));
        assertEquals(LocalTime.of(2,0,0,123_000_000), DateUtils.parseLocalTM( "020000.123", precision));
        assertEquals(LocalTime.of(23,59,59,999_999_000), DateUtils.parseLocalTM( "235959.999999", precision));
        assertEquals(LocalTime.of(12, 0, 0, 123_456_789), DateUtils.parseLocalTM("120000.123456789", precision));
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
    public void testParseTMrounding() {
        DatePrecision precision = new DatePrecision();
        assertEquals(123, cal(tz, DateUtils.parseTM(tz, "120000.1234", precision)).get(Calendar.MILLISECOND));
        assertEquals(124, cal(tz, DateUtils.parseTM(tz, "120000.1235", precision)).get(Calendar.MILLISECOND));
    }

    private Calendar cal(TimeZone tz, Date date) {
        Calendar cal = Calendar.getInstance(tz);
        cal.setTime(date);
        return cal;
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

    @Test
    public void testFormatTimezoneOffsetFromUTC() {
        assertEquals("+0200", DateUtils.formatTimezoneOffsetFromUTC(tz));
        assertEquals("-0500", DateUtils.formatTimezoneOffsetFromUTC(TimeZone.getTimeZone("GMT-05:00")));
    }

    @Test
    public void testParseTemporalDT() {
        DatePrecision precision = new DatePrecision();
        Temporal result = DateUtils.parseTemporalDT("19700101120000", precision);
        assertEquals(LocalDateTime.of(1970, 1, 1, 12, 0), result);

        result = DateUtils.parseTemporalDT("19700101120000+0200", precision);
        assertEquals(OffsetDateTime.of(1970, 1, 1, 12, 0, 0, 0, ZoneOffset.ofHours(2)), result);
    }

}
