package org.dcm4che3.data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.dcm4che3.util.DateUtils;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the Date and Time handling of {@link Attributes}.
 *
 * @author Gunter Zeilinger <gunterze@protonmail.com>
 * @author Hermann Czedik-Eysenberg (hermann-agfa@czedik.net)
 */
public class AttributesTemporalTest {

    @Test
    public void testGetTemporalLocalDateDA() {
        Attributes a = new Attributes();
        a.setString(Tag.StudyDate, VR.DA, "20110404");
        a.setString(Tag.StudyTime, VR.TM, "15");

        Temporal t = a.getTemporal(Tag.StudyDate);
        assertTrue(t instanceof LocalDate);
        assertEquals(LocalDate.of(2011, 04, 04), t);
    }

    @Test
    public void testGetTemporalLocalTimeTM() {
        Attributes a = new Attributes();
        a.setString(Tag.StudyDate, VR.DA, "20110404");
        a.setString(Tag.StudyTime, VR.TM, "15");

        Temporal t = a.getTemporal(Tag.StudyTime);
        assertTrue(t instanceof LocalTime);
        assertEquals(LocalTime.of(15, 00), t);
    }

    @Test
    public void testGetTemporalDT() {
        Attributes a = new Attributes();
        a.setString(Tag.AcquisitionDateTime, VR.DT, "20230926130127.987654");

        Temporal t = a.getTemporal(Tag.AcquisitionDateTime);
        assertTrue(t instanceof LocalDateTime);
        assertEquals(LocalDateTime.of(2023, 9, 26, 13, 1, 27, 987_654_000), t);
    }

    @Test
    public void testGetTemporalDTWithTZ() {
        Attributes a = new Attributes();
        a.setString(Tag.AcquisitionDateTime, VR.DT, "20230926130127.987654+0200");

        Temporal t = a.getTemporal(Tag.AcquisitionDateTime);
        assertTrue(t instanceof ZonedDateTime);
        assertEquals(LocalDateTime.of(2023, 9, 26, 13, 1, 27, 987_654_000)
                .atOffset(ZoneOffset.ofHours(2)).toZonedDateTime(), t);
    }

    @Test
    public void testGetTemporalDTWithTimezoneOffsetFromUTC() {
        Attributes a = new Attributes();
        a.setString(Tag.AcquisitionDateTime, VR.DT, "20230926130127.987654");
        a.setString(Tag.TimezoneOffsetFromUTC, VR.SH, "-0300");

        Temporal t = a.getTemporal(Tag.AcquisitionDateTime);
        assertTrue(t instanceof ZonedDateTime);
        assertEquals(LocalDateTime.of(2023, 9, 26, 13, 1, 27, 987_654_000)
                .atOffset(ZoneOffset.ofHours(-3)).toInstant(), ((ZonedDateTime)t).toInstant());
    }

    @Test
    public void testGetTemporalDTWithDefaultTimeZone() {
        Attributes a = new Attributes();
        a.setString(Tag.AcquisitionDateTime, VR.DT, "20230926130127.987654");
        a.setDefaultTimeZone(DateUtils.timeZone("-0300"));

        Temporal t = a.getTemporal(Tag.AcquisitionDateTime);
        assertTrue(t instanceof ZonedDateTime);
        assertEquals(LocalDateTime.of(2023, 9, 26, 13, 1, 27, 987_654_000)
                .atOffset(ZoneOffset.ofHours(-3)).toInstant(), ((ZonedDateTime)t).toInstant());
    }

    @Test
    public void testGetTemporalPrecisionWithAndWithoutExplicitTimezone() {
        Attributes a = new Attributes();
        a.setDefaultTimeZone(DateUtils.timeZone("-0300"));
        a.setString(Tag.AcquisitionDateTime, VR.DT, "202309261301+0200", "20231027141516");
        DatePrecision precision = new DatePrecision();

        assertEquals(LocalDateTime.of(2023, 9, 26, 13, 1)
                        .atOffset(ZoneOffset.ofHours(2)).toZonedDateTime(),
                a.getTemporal(Tag.AcquisitionDateTime, 0, precision));
        assertEquals(Calendar.MINUTE, precision.lastField);
        assertTrue(precision.includeTimezone);
        assertEquals(LocalDateTime.of(2023, 10, 27, 14, 15, 16)
                        .atOffset(ZoneOffset.ofHours(-3)).toInstant(),
                ((ZonedDateTime) a.getTemporal(Tag.AcquisitionDateTime, 1, precision)).toInstant());
        assertEquals(Calendar.SECOND, precision.lastField);
        assertFalse(precision.includeTimezone);
    }

    @Test
    public void testGetTemporalLongWithoutTimezone() {
        Attributes a = new Attributes();
        a.setString(Tag.StudyDate, VR.DA, "20110404");
        a.setString(Tag.StudyTime, VR.TM, "15");

        Temporal t = a.getTemporal(Tag.StudyDateAndTime);
        assertTrue(t instanceof LocalDateTime);
        assertEquals(LocalDateTime.of(2011, 4, 4, 15, 0, 0), t);
    }

    @Test
    public void testGetTemporalLongWithTimeZoneOffsetFromUTC() {
        Attributes a = new Attributes();
        a.setString(Tag.StudyDate, VR.DA, "20110404");
        a.setString(Tag.StudyTime, VR.TM, "15");
        a.setString(Tag.TimezoneOffsetFromUTC, VR.SH, "-0300");

        Temporal t = a.getTemporal(Tag.StudyDateAndTime);
        assertTrue(t instanceof ZonedDateTime);
        assertEquals(LocalDateTime.of(2011, 04, 04, 15, 00)
                .atOffset(ZoneOffset.ofHours(-3)).toInstant(), ((ZonedDateTime)t).toInstant());
    }

    @Test
    public void testTemporalLongLocalDateIfNoTimeAvailable() {
        Attributes a = new Attributes();
        a.setString(Tag.StudyDate, VR.DA, "20110404");

        Temporal t = a.getTemporal(Tag.StudyDateAndTime);
        assertTrue(t instanceof LocalDate);
        assertEquals(LocalDate.of(2011, 04, 04), t);
    }

    @Test
    public void testTemporalLongLocalTimeIfNoDateAvailable() {
        Attributes a = new Attributes();
        a.setString(Tag.StudyTime, VR.TM, "15");

        Temporal t = a.getTemporal(Tag.StudyDateAndTime);
        assertTrue(t instanceof LocalTime);
        assertEquals(LocalTime.of(15,0,0), t);
    }

    @Test
    public void testTemporalLongDefaultValueIfNoTagSet() {
        Attributes a = new Attributes();

        Temporal t = a.getTemporal(Tag.StudyDateAndTime);
        assertNull(t);
    }

    @Test
    public void testGetTemporalSingleTagVariants() {
        Attributes a = new Attributes();
        a.setString(Tag.AcquisitionDateTime, VR.DT, "202309261301+0200", "20231027141516-0300");
        Temporal first = LocalDateTime.of(2023, 9, 26, 13, 1)
                .atOffset(ZoneOffset.ofHours(2)).toZonedDateTime();
        Temporal second = LocalDateTime.of(2023, 10, 27, 14, 15, 16)
                .atOffset(ZoneOffset.ofHours(-3)).toZonedDateTime();
        Temporal fallback = LocalDate.of(2000, 1, 1);
        DatePrecision precision = new DatePrecision();

        assertEquals(first, a.getTemporal(Tag.AcquisitionDateTime, precision));
        assertEquals(Calendar.MINUTE, precision.lastField);
        assertTrue(precision.includeTimezone);
        assertEquals(first, a.getTemporal(Tag.AcquisitionDateTime, fallback));
        precision = new DatePrecision();
        assertEquals(first, a.getTemporal(Tag.AcquisitionDateTime, fallback, precision));
        assertEquals(Calendar.MINUTE, precision.lastField);
        assertTrue(precision.includeTimezone);
        assertEquals(second, a.getTemporal(Tag.AcquisitionDateTime, 1));
        precision = new DatePrecision();
        assertEquals(second, a.getTemporal(Tag.AcquisitionDateTime, 1, precision));
        assertEquals(Calendar.SECOND, precision.lastField);
        assertTrue(precision.includeTimezone);
        assertEquals(second, a.getTemporal(Tag.AcquisitionDateTime, 1, fallback));
        precision = new DatePrecision();
        assertEquals(second, a.getTemporal(Tag.AcquisitionDateTime, 1, fallback, precision));
        assertEquals(Calendar.SECOND, precision.lastField);
        assertTrue(precision.includeTimezone);

        assertEquals(fallback, a.getTemporal(Tag.AcquisitionDateTime, 2, fallback));
        assertEquals(fallback, a.getTemporal(Tag.AcquisitionDateTime, 2, fallback, new DatePrecision()));
        assertEquals(fallback, a.getTemporal(Tag.StudyDate, fallback));
        assertEquals(fallback, a.getTemporal(Tag.StudyDate, fallback, new DatePrecision()));
        assertNull(a.getTemporal(Tag.AcquisitionDateTime, 2));
        assertNull(a.getTemporal(Tag.AcquisitionDateTime, 2, new DatePrecision()));
        a.setNull(Tag.StudyDate, VR.DA);
        assertEquals(fallback, a.getTemporal(Tag.StudyDate, fallback));
        a.setString(Tag.StudyDate, VR.DA, "invalid");
        assertEquals(fallback, a.getTemporal(Tag.StudyDate, fallback));
        a.setString(Tag.PatientName, VR.PN, "Test");
        assertEquals(fallback, a.getTemporal(Tag.PatientName, fallback));
    }

    @Test
    public void testGetTemporalPrivateSingleTagVariants() {
        Attributes a = new Attributes();
        String creator = "TEMPORAL";
        int tag = 0x00990010;
        a.setString(creator, tag, VR.DT, "202309261301+0200", "20231027141516-0300");
        Temporal first = LocalDateTime.of(2023, 9, 26, 13, 1)
                .atOffset(ZoneOffset.ofHours(2)).toZonedDateTime();
        Temporal second = LocalDateTime.of(2023, 10, 27, 14, 15, 16)
                .atOffset(ZoneOffset.ofHours(-3)).toZonedDateTime();
        Temporal fallback = LocalDate.of(2000, 1, 1);
        DatePrecision precision = new DatePrecision();

        assertEquals(first, a.getTemporal(creator, tag));
        assertEquals(first, a.getTemporal(creator, tag, precision));
        assertEquals(Calendar.MINUTE, precision.lastField);
        assertTrue(precision.includeTimezone);
        assertEquals(first, a.getTemporal(creator, tag, fallback));
        precision = new DatePrecision();
        assertEquals(first, a.getTemporal(creator, tag, fallback, precision));
        assertEquals(Calendar.MINUTE, precision.lastField);
        assertTrue(precision.includeTimezone);
        assertEquals(first, a.getTemporal(creator, tag, VR.DT));
        precision = new DatePrecision();
        assertEquals(first, a.getTemporal(creator, tag, VR.DT, precision));
        assertEquals(Calendar.MINUTE, precision.lastField);
        assertTrue(precision.includeTimezone);
        assertEquals(first, a.getTemporal(creator, tag, VR.DT, fallback));
        precision = new DatePrecision();
        assertEquals(first, a.getTemporal(creator, tag, VR.DT, fallback, precision));
        assertEquals(Calendar.MINUTE, precision.lastField);
        assertTrue(precision.includeTimezone);
        assertEquals(second, a.getTemporal(creator, tag, 1));
        precision = new DatePrecision();
        assertEquals(second, a.getTemporal(creator, tag, 1, precision));
        assertEquals(Calendar.SECOND, precision.lastField);
        assertTrue(precision.includeTimezone);
        assertEquals(second, a.getTemporal(creator, tag, 1, fallback));
        precision = new DatePrecision();
        assertEquals(second, a.getTemporal(creator, tag, 1, fallback, precision));
        assertEquals(Calendar.SECOND, precision.lastField);
        assertTrue(precision.includeTimezone);
        assertEquals(second, a.getTemporal(creator, tag, VR.DT, 1));
        precision = new DatePrecision();
        assertEquals(second, a.getTemporal(creator, tag, VR.DT, 1, precision));
        assertEquals(Calendar.SECOND, precision.lastField);
        assertTrue(precision.includeTimezone);
        assertEquals(second, a.getTemporal(creator, tag, VR.DT, 1, fallback));
        precision = new DatePrecision();
        assertEquals(second, a.getTemporal(creator, tag, VR.DT, 1, fallback, precision));
        assertEquals(Calendar.SECOND, precision.lastField);
        assertTrue(precision.includeTimezone);
        assertEquals(fallback, a.getTemporal("OTHER", tag, fallback));
        assertEquals(fallback, a.getTemporal("OTHER", tag, fallback, new DatePrecision()));
        assertEquals(fallback, a.getTemporal("OTHER", tag, VR.DT, fallback));
        assertEquals(fallback, a.getTemporal("OTHER", tag, VR.DT, fallback, new DatePrecision()));
        assertEquals(fallback, a.getTemporal(creator, tag, 2, fallback));
        assertEquals(fallback, a.getTemporal(creator, tag, 2, fallback, new DatePrecision()));
        assertEquals(fallback, a.getTemporal(creator, tag, VR.DT, 2, fallback));
        assertEquals(fallback, a.getTemporal(creator, tag, VR.DT, 2, fallback, new DatePrecision()));
        assertNull(a.getTemporal(creator, tag, 2));
        assertNull(a.getTemporal(creator, tag, 2, new DatePrecision()));
        assertNull(a.getTemporal(creator, tag, VR.DT, 2));
        assertNull(a.getTemporal(creator, tag, VR.DT, 2, new DatePrecision()));

        int overrideTag = 0x00990011;
        a.setString(creator, overrideTag, VR.LO, "20110404");
        assertEquals(LocalDate.of(2011, 4, 4), a.getTemporal(creator, overrideTag, VR.DA));
    }

    @Test
    public void testGetTemporalExplicitVrVariants() {
        Attributes a = new Attributes();
        String creator = "TEMPORAL";
        int tag = 0x00990010;
        Temporal fallback = LocalDate.of(2000, 1, 1);
        LocalDate first = LocalDate.of(2011, 4, 4);
        LocalDate second = LocalDate.of(2011, 4, 5);
        DatePrecision precision = new DatePrecision();

        a.setString(creator, tag, VR.LO, "20110404", "20110405");
        assertEquals(first, a.getTemporal(creator, tag, VR.DA, precision));
        assertEquals(Calendar.DAY_OF_MONTH, precision.lastField);
        a.setString(creator, tag, VR.LO, "20110404", "20110405");
        assertEquals(first, a.getTemporal(creator, tag, VR.DA, fallback));
        a.setString(creator, tag, VR.LO, "20110404", "20110405");
        assertEquals(first, a.getTemporal(creator, tag, VR.DA, fallback, new DatePrecision()));
        a.setString(creator, tag, VR.LO, "20110404", "20110405");
        assertEquals(second, a.getTemporal(creator, tag, VR.DA, 1));
        a.setString(creator, tag, VR.LO, "20110404", "20110405");
        assertEquals(second, a.getTemporal(creator, tag, VR.DA, 1, new DatePrecision()));
        a.setString(creator, tag, VR.LO, "20110404", "20110405");
        assertEquals(second, a.getTemporal(creator, tag, VR.DA, 1, fallback));
        a.setString(creator, tag, VR.LO, "20110404", "20110405");
        assertEquals(second, a.getTemporal(creator, tag, VR.DA, 1, fallback, new DatePrecision()));
    }

    @Test
    public void testGetTemporalCombinedTagVariants() {
        Attributes a = new Attributes();
        a.setString(Tag.StudyDate, VR.DA, "20110404");
        a.setString(Tag.StudyTime, VR.TM, "1530");
        Temporal expected = LocalDateTime.of(2011, 4, 4, 15, 30);
        Temporal fallback = LocalDate.of(2000, 1, 1);
        DatePrecision precision = new DatePrecision();

        assertEquals(expected, a.getTemporal(Tag.StudyDateAndTime, precision));
        assertEquals(Calendar.MINUTE, precision.lastField);
        assertEquals(expected, a.getTemporal(Tag.StudyDateAndTime, fallback));
        precision = new DatePrecision();
        assertEquals(expected, a.getTemporal(Tag.StudyDateAndTime, fallback, precision));
        assertEquals(Calendar.MINUTE, precision.lastField);
        assertEquals(expected, a.getTemporal(null, Tag.StudyDateAndTime));
        precision = new DatePrecision();
        assertEquals(expected, a.getTemporal(null, Tag.StudyDateAndTime, precision));
        assertEquals(Calendar.MINUTE, precision.lastField);
        assertEquals(expected, a.getTemporal(null, Tag.StudyDateAndTime, fallback));
        precision = new DatePrecision();
        assertEquals(expected, a.getTemporal(null, Tag.StudyDateAndTime, fallback, precision));
        assertEquals(Calendar.MINUTE, precision.lastField);

        a.setDefaultTimeZone(DateUtils.timeZone("+0200"));
        assertEquals(((LocalDateTime) expected).atOffset(ZoneOffset.ofHours(2)).toInstant(),
                ((ZonedDateTime) a.getTemporal(Tag.StudyDateAndTime, fallback)).toInstant());
        a.remove(Tag.StudyTime);
        precision = new DatePrecision();
        assertEquals(LocalDate.of(2011, 4, 4), a.getTemporal(Tag.StudyDateAndTime, fallback, precision));
        assertEquals(Calendar.DAY_OF_MONTH, precision.lastField);
        a.remove(Tag.StudyDate);
        a.setString(Tag.StudyTime, VR.TM, "1530");
        precision = new DatePrecision();
        assertEquals(LocalTime.of(15, 30), a.getTemporal(Tag.StudyDateAndTime, fallback, precision));
        assertEquals(Calendar.MINUTE, precision.lastField);
        a.remove(Tag.StudyTime);
        assertEquals(fallback, a.getTemporal(Tag.StudyDateAndTime, fallback));
        assertEquals(fallback, a.getTemporal(Tag.StudyDateAndTime, fallback, new DatePrecision()));
        assertEquals(fallback, a.getTemporal(null, Tag.StudyDateAndTime, fallback));
        assertEquals(fallback, a.getTemporal(null, Tag.StudyDateAndTime, fallback, new DatePrecision()));
    }

    @Test
    public void testGetTemporalPrivateCombinedTagVariants() {
        Attributes a = new Attributes();
        String creator = "TEMPORAL";
        int dateTag = 0x00990010;
        int timeTag = 0x00990011;
        long combinedTag = ((long) dateTag << 32) | (timeTag & 0xffffffffL);
        a.setString(creator, dateTag, VR.DA, "20110404");
        a.setString(creator, timeTag, VR.TM, "1530");
        Temporal expected = LocalDateTime.of(2011, 4, 4, 15, 30);
        Temporal fallback = LocalDate.of(2000, 1, 1);
        DatePrecision precision = new DatePrecision();

        assertEquals(expected, a.getTemporal(creator, combinedTag));
        assertEquals(expected, a.getTemporal(creator, combinedTag, precision));
        assertEquals(Calendar.MINUTE, precision.lastField);
        assertEquals(expected, a.getTemporal(creator, combinedTag, fallback));
        precision = new DatePrecision();
        assertEquals(expected, a.getTemporal(creator, combinedTag, fallback, precision));
        assertEquals(Calendar.MINUTE, precision.lastField);
        assertEquals(fallback, a.getTemporal("OTHER", combinedTag, fallback));
        assertEquals(fallback, a.getTemporal("OTHER", combinedTag, fallback, new DatePrecision()));
        a.setNull(creator, timeTag, VR.TM);
        assertEquals(LocalDate.of(2011, 4, 4), a.getTemporal(creator, combinedTag, fallback));
        a.setNull(creator, dateTag, VR.DA);
        assertEquals(fallback, a.getTemporal(creator, combinedTag, fallback));
    }

    /**
     * Test method for {@link org.dcm4che3.data.Attributes#getDate(long, java.util.Date)}.
     */
    @Test
    public void testGetDateLongDate() {
        Attributes a = new Attributes();
        a.setString(Tag.StudyDate, VR.DA, "20110404");
        a.setString(Tag.StudyTime, VR.TM, "15");
        Date d = a.getDate(Tag.StudyDateAndTime);
        assertEquals("20110404150000.000", DateUtils.formatDT(null, d));
    }

    /**
     * Test method for {@link org.dcm4che3.data.Attributes#getDateRange(int, org.dcm4che3.data.DateRange)}.
     */
    @Test
    public void testGetDateRangeIntDateRange() {
        Attributes a = new Attributes();
        a.setString(Tag.StudyDate, VR.DA, "20110404-20110405");
        DateRange range = a.getDateRange(Tag.StudyDate, null);
        assertEquals("20110404000000.000",
                DateUtils.formatDT(null, range.getStartDate()));
        assertEquals("20110405235959.999",
                DateUtils.formatDT(null, range.getEndDate()));
    }

    /**
     * Test method for {@link org.dcm4che3.data.Attributes#getDateRange(long, org.dcm4che3.data.DateRange)}.
     */
    @Test
    public void testGetDateRangeLongDateRange() {
        Attributes a = new Attributes();
        a.setString(Tag.StudyDate, VR.DA, "20110404");
        a.setString(Tag.StudyTime, VR.TM, "15-20");
        DateRange range = a.getDateRange(Tag.StudyDateAndTime, null);
        assertEquals("20110404150000.000",
                DateUtils.formatDT(null, range.getStartDate()));
        assertEquals("20110404205959.999",
                DateUtils.formatDT(null, range.getEndDate()));
    }

    /**
     * Test method for {@link org.dcm4che3.data.Attributes#setDate(long, Date...)}.
     */
    @Test
    public void testSetDateLongDate() {
        Attributes a = new Attributes();
        a.setDate(Tag.StudyDateAndTime,
                DateUtils.parseDT(null, "20110404150000.000", new DatePrecision()));
        assertEquals("20110404", a.getString(Tag.StudyDate, null));
        assertEquals("150000.000", a.getString(Tag.StudyTime, null));
    }

    /**
     * Test method for {@link org.dcm4che3.data.Attributes#setDateRange(int, org.dcm4che3.data.VR, org.dcm4che3.data.DateRange)}.
     */
    @Test
    public void testSetDateRangeIntVRDateRange() {
        Attributes a = new Attributes();
        Date lower = DateUtils.parseDA(null, "20110404");
        Date upper = DateUtils.parseDA(null, "20110405");
        a.setDateRange(Tag.StudyDate, VR.DA, new DateRange(lower, upper));
        assertEquals("20110404-20110405", a.getString(Tag.StudyDate, null));
    }

    /**
     * Test method for {@link org.dcm4che3.data.Attributes#setDateRange(long, org.dcm4che3.data.DateRange)}.
     */
    @Test
    public void testSetDateRangeLongDateRange() {
        Attributes a = new Attributes();
        Date lower = DateUtils.parseDT(null, "2011040415", new DatePrecision());
        a.setDateRange(Tag.StudyDateAndTime, new DateRange(lower, null));
        assertEquals("20110404-", a.getString(Tag.StudyDate, null));
        assertEquals("150000.000-", a.getString(Tag.StudyTime, null));
    }

    @Test
    public void testSetAndGetDateAndTime() {
        // set system timezone to make this test predictable on any Locale
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        Attributes a = new Attributes();
        a.setTimezoneOffsetFromUTC("+1200");

        Date dateAndTime = new Date((24*60*60 + 15*60*60)*1000); // 1970-01-02 15:00:00 (UTC)
        Date dateOnly = new Date(24*60*60*1000); // 1970-01-02 00:00:00 (UTC)
        Date timeOnly = new Date(15*60*60*1000); // 1970-01-01 15:00:00 (UTC)

        // setting/getting StudyDateAndTime together does apply timezone offset (by default)
        a.setDate(Tag.StudyDateAndTime, dateAndTime);
        assertEquals("19700103", a.getString(Tag.StudyDate));
        assertEquals("030000.000", a.getString(Tag.StudyTime));
        assertEquals(dateAndTime, a.getDate(Tag.StudyDateAndTime));

        // setting/getting date and time separately does not apply timezone offset (by default)
        a.setDate(Tag.StudyDate, VR.DA, dateAndTime);
        a.setDate(Tag.StudyTime, VR.TM, dateAndTime);
        assertEquals("19700102", a.getString(Tag.StudyDate));
        assertEquals("150000.000", a.getString(Tag.StudyTime));
        assertEquals(dateOnly, a.getDate(Tag.StudyDate));
        assertEquals(timeOnly, a.getDate(Tag.StudyTime));
    }

    @Test
    public void testSetTimezoneOffsetFromUTC() {
        Attributes a = new Attributes();
        a.setDefaultTimeZone(DateUtils.timeZone("+0000"));
        a.setDate(Tag.StudyDateAndTime, new Date(0));
        a.setDate(Tag.DateAndTimeOfLastCalibration, new Date(0), new Date(100000000));
        a.setString(Tag.PatientBirthDate, VR.DA, "19700101");
        a.setString(Tag.PatientBirthTime, VR.TM, "000000.000");
        a.setString(Tag.ContextGroupVersion, VR.DT, "19700101");
        assertEquals("19700101", a.getString(Tag.StudyDate));
        assertEquals("000000.000", a.getString(Tag.StudyTime));
        assertArrayEquals(new String[] {"19700101", "19700102"}, a.getStrings(Tag.DateOfLastCalibration));
        assertArrayEquals(new String[] {"000000.000", "034640.000"}, a.getStrings(Tag.TimeOfLastCalibration));

        a.setTimezoneOffsetFromUTC("+0100");
        assertEquals("19700101", a.getString(Tag.StudyDate));
        assertEquals("010000.000", a.getString(Tag.StudyTime));
        assertEquals("19700101", a.getString(Tag.PatientBirthDate));
        assertEquals("000000.000", a.getString(Tag.PatientBirthTime));
        assertEquals("19700101", a.getString(Tag.ContextGroupVersion));
        assertArrayEquals(new String[] {"19700101", "19700102"}, a.getStrings(Tag.DateOfLastCalibration));
        assertArrayEquals(new String[] {"010000.000", "044640.000"}, a.getStrings(Tag.TimeOfLastCalibration));

        a.setTimezoneOffsetFromUTC("-0100");
        assertEquals("19691231", a.getString(Tag.StudyDate));
        assertEquals("230000.000", a.getString(Tag.StudyTime));
        assertEquals("19700101", a.getString(Tag.PatientBirthDate));
        assertEquals("000000.000", a.getString(Tag.PatientBirthTime));
        assertEquals("19700101", a.getString(Tag.ContextGroupVersion));
        assertArrayEquals(new String[] {"19691231", "19700102"}, a.getStrings(Tag.DateOfLastCalibration));
        assertArrayEquals(new String[] {"230000.000", "024640.000"}, a.getStrings(Tag.TimeOfLastCalibration));
    }

    @Test
    public void testDateRangeSetTimezoneOffsetFromUTC() {
        Attributes a = new Attributes();
        a.setDefaultTimeZone(DateUtils.timeZone("+0000"));
        a.setDateRange(Tag.StudyDateAndTime,
                new DateRange(new Date(0), new Date(3600000 * 12)));
        assertEquals("19700101", a.getString(Tag.StudyDate));
        assertEquals("000000.000-120000.000", a.getString(Tag.StudyTime));
        a.setTimezoneOffsetFromUTC("-0100");
        assertEquals("19691231-19700101", a.getString(Tag.StudyDate));
        assertEquals("230000.000-110000.000", a.getString(Tag.StudyTime));
        a.setTimezoneOffsetFromUTC("+0100");
        assertEquals("19700101", a.getString(Tag.StudyDate));
        assertEquals("010000.000-130000.000", a.getString(Tag.StudyTime));
    }

}
