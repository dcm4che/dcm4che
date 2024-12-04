package org.dcm4che3.data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
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
