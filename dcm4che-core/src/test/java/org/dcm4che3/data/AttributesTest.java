/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.data;

import static org.junit.Assert.*;

import java.util.Date;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.DatePrecision;
import org.dcm4che3.data.DateRange;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.StringUtils;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class AttributesTest {

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
     * Test method for {@link org.dcm4che3.data.Attributes#setDate(long, java.util.Date)}.
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
    public void testCreatorTagOf() {
        Attributes a = new Attributes();
        a.setString(0x00090010, VR.LO, "CREATOR1");
        a.setString(0x00091010, VR.LO, "VALUE1");
        a.setNull(0x00090015, VR.LO);
        a.setString(0x00090020, VR.LO, "CREATOR2");
        a.setString(0x00092010, VR.LO, "VALUE2");
        a.setNull("CREATOR3", 0x00090010, VR.LO);
        assertEquals("VALUE1", a.getString("CREATOR1", 0x00090010, null, null));
        assertEquals("VALUE2", a.getString("CREATOR2", 0x00090010, null, null));
        assertEquals("CREATOR3", a.getString(0x00090021));
    }

    @Test
    public void testEqualsPrivate() {
        Attributes a1 = new Attributes();
        a1.setString(0x00090010, VR.LO, "CREATOR1");
        a1.setString(0x00091010, VR.LO, "VALUE1");
        Attributes a2 = new Attributes();
        a2.setString(0x00090020, VR.LO, "CREATOR1");
        a2.setString(0x00092010, VR.LO, "VALUE1");
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));
    }

    @Test
    public void testEqualValues() {
        Attributes a = new Attributes();
        a.setString(Tag.PatientName, VR.PN, "Simson^Homer");
        a.setString(0x00090010, VR.LO, "CREATOR1");
        a.setString(0x00091010, VR.LO, "VALUE1");

        Attributes b = new Attributes();
        b.setString(Tag.PatientName, VR.PN, "Simson^Homer^^^");
        b.setString(0x00090020, VR.LO, "CREATOR1");
        b.setString(0x00092010, VR.LO, "VALUE1");
        b.setString(0x00090010, VR.LO, "CREATOR2");
        b.setString(0x00091010, VR.LO, "VALUE2");

        assertTrue(a.equalValues(b, Tag.PatientName));
        assertTrue(b.equalValues(a, Tag.PatientName));
        assertTrue(b.equalValues(a, "CREATOR1", 0x00090010));
        assertTrue(a.equalValues(b, "CREATOR1", 0x00090010));

        assertFalse(a.equalValues(b, "CREATOR2", 0x00090010));
    }

    @Test
    public void testEqualsIS() {
        Attributes a1 = new Attributes();
        a1.setString(Tag.ReferencedFrameNumber, VR.IS, "54");
        a1.setString(Tag.InstanceNumber, VR.IS, "IMG0077");
        Attributes a2 = new Attributes();
        a2.setString(Tag.ReferencedFrameNumber, VR.IS, "0054");
        a2.setString(Tag.InstanceNumber, VR.IS, "IMG0077");
        assertTrue(a1.equals(a2));
        assertEquals("54", a2.getString(Tag.ReferencedFrameNumber));
    }

    @Test
    public void testEqualsDS() {
        Attributes a1 = new Attributes();
        a1.setString(Tag.PixelSpacing, VR.DS, ".5",".5");
        Attributes a2 = new Attributes();
        a2.setString(Tag.PixelSpacing, VR.DS, "+0.50", "5E-1");
        assertTrue(a1.equals(a2));
        assertArrayEquals(new String[]{ "0.5","0.5" }, a1.getStrings(Tag.PixelSpacing));
        assertArrayEquals(new String[]{ "0.5","0.5" }, a2.getStrings(Tag.PixelSpacing));
    }

    @Test
    public void testGetDS() {
        Attributes a = new Attributes();
        a.setString(Tag.PixelSpacing, VR.DS, ".5",".5");
        assertArrayEquals(new double[]{ 0.5, 0.5 }, a.getDoubles(Tag.PixelSpacing), 0);
        assertArrayEquals(new float[]{ 0.5f, 0.5f }, a.getFloats(Tag.PixelSpacing), 0);
    }

    @Test
    public void testTreatWhiteSpacesAsNoValue() {
        byte[] WHITESPACES = { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' };
        Attributes a = new Attributes();
        a.setBytes(Tag.AccessionNumber, VR.SH, WHITESPACES);
        a.setBytes(Tag.StudyDescription, VR.LO, WHITESPACES);
        a.setBytes(Tag.InstanceNumber, VR.IS, WHITESPACES);
        a.setBytes(Tag.PixelSpacing, VR.DS, WHITESPACES);
        assertFalse(a.containsValue(Tag.AccessionNumber));
        assertNull(a.getString(Tag.StudyDescription));
        assertEquals(-1, a.getInt(Tag.InstanceNumber, -1));
        assertArrayEquals(ByteUtils.EMPTY_DOUBLES, a.getDoubles(Tag.PixelSpacing), 0);
        assertArrayEquals(ByteUtils.EMPTY_FLOATS, a.getFloats(Tag.PixelSpacing), 0);
   }

    @Test
    public void testSetSpecificCharacterSet() throws Exception {
        String NAME = "\u00c4neas^R\u00fcdiger";
        Attributes a = new Attributes();
        a.setSpecificCharacterSet("ISO_IR 100");
        a.setBytes(Tag.PatientName, VR.PN, NAME.getBytes("ISO-8859-1"));
        a.setSpecificCharacterSet("ISO_IR 192");
        assertArrayEquals(NAME.getBytes("UTF-8"), a.getBytes(Tag.PatientName));
    }

    @Test
    public void testReadWronglyEncodedDatasetByChangingDefaultCharacterSet() throws Exception {
        Attributes a = new Attributes();

        a.setString(Tag.SpecificCharacterSet, VR.CS, "ISO 2022 IR 6"); // ASCII
        
        String NAME = "\u00c4neas^R\u00fcdiger";
        // simulate wrong encoding using "ISO-8859-1" (ISO_IR 100) instead of "ISO 2022 IR 6" (ASCII)
        a.setBytes(Tag.PatientName, VR.PN, NAME.getBytes("ISO-8859-1"));
        
        // changing the default character set makes it possible to read such bad data
        SpecificCharacterSet.setDefaultCharacterSet("ISO_IR 100");
        try {
            assertEquals(NAME, a.getString(Tag.PatientName));
        } finally {
            // reset the default character set, because other tests will run within the same JVM
            SpecificCharacterSet.setDefaultCharacterSet(null);
        }
    }

    @Test
    public void testSetTimezoneOffsetFromUTC() throws Exception {
        Attributes a = new Attributes();
        a.setDefaultTimeZone(DateUtils.timeZone("+0000"));
        a.setDate(Tag.StudyDateAndTime, new Date(0));
        assertEquals("19700101", a.getString(Tag.StudyDate));
        assertEquals("000000.000", a.getString(Tag.StudyTime));
        a.setTimezoneOffsetFromUTC("+0100");
        assertEquals("19700101", a.getString(Tag.StudyDate));
        assertEquals("010000.000", a.getString(Tag.StudyTime));
        a.setTimezoneOffsetFromUTC("-0100");
        assertEquals("19691231", a.getString(Tag.StudyDate));
        assertEquals("230000.000", a.getString(Tag.StudyTime));
    }


    @Test
    public void testDateRangeSetTimezoneOffsetFromUTC() throws Exception {
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

    @Test
    public void testGetModified() {
        Attributes original = createOriginal();
        Attributes other = modify(original);
        Attributes modified = original.getModified(other, null);
        assertEquals(4, modified.size());
        assertModified(modified);
    }

    @Test
    public void testGetRemovedOrModified() {
        Attributes original = createOriginal();
        Attributes other = modify(original);
        Attributes modified = original.getRemovedOrModified(other);
        assertEquals(5, modified.size());
        assertEquals("AccessionNumber", modified.getString(Tag.AccessionNumber));
        assertModified(modified);
    }

    private void assertModified(Attributes modified) {
        assertEquals("PatientID", modified.getString(Tag.PatientID));
        Attributes modOtherPID = modified.getNestedDataset(Tag.OtherPatientIDsSequence);
        assertNotNull(modOtherPID);
        assertEquals("OtherPatientID", modOtherPID.getString(Tag.PatientID));
        assertEquals("PrivateCreatorB", modified.getString(0x00990010));
        assertEquals("0099xx02B", modified.getString(0x00991002));
    }

    private Attributes modify(Attributes original) {
        Attributes other = new Attributes(original.size()+2);
        other.setString("PrivateCreatorC", 0x00990002, VR.LO, "New0099xx02C");
        other.addAll(original);
        other.remove(Tag.AccessionNumber);
        other.setString(Tag.PatientName, VR.LO, "Added^Patient^Name");
        other.setString(Tag.PatientID, VR.LO, "ModifiedPatientID");
        other.getNestedDataset(Tag.OtherPatientIDsSequence)
                .setString(Tag.PatientID, VR.LO, "ModifiedOtherPatientID");
        other.setString("PrivateCreatorB", 0x00990002, VR.LO, "Modfied0099xx02B");
        return other;
    }

    private Attributes createOriginal() {
        Attributes original = new Attributes();
        Attributes otherPID = new Attributes();
        Attributes rqAttrs = new Attributes();
        original.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        original.setNull(Tag.PatientName, VR.PN);
        original.setString(Tag.PatientID, VR.LO, "PatientID");
        original.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");
        original.newSequence(Tag.OtherPatientIDsSequence, 1).add(otherPID);
        original.newSequence(Tag.RequestAttributesSequence, 1).add(rqAttrs);
        original.setString("PrivateCreatorA", 0x00990001, VR.LO, "0099xx01A");
        original.setString("PrivateCreatorB", 0x00990001, VR.LO, "0099xx01B");
        original.setString("PrivateCreatorB", 0x00990002, VR.LO, "0099xx02B");
        otherPID.setString(Tag.PatientID, VR.LO, "OtherPatientID");
        otherPID.setString(Tag.IssuerOfPatientID, VR.LO, "OtherIssuerOfPatientID");
        rqAttrs.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID");
        rqAttrs.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID");
        return original;
    }

    @Test
    public void testRemovePrivateAttributes() {
        Attributes original = createOriginal();
        assertEquals(11, original.size());
        assertEquals(1, original.removePrivateAttributes("PrivateCreatorA", 0x0099));
        assertEquals(9, original.size());
        assertEquals(2, original.removePrivateAttributes("PrivateCreatorB", 0x0099));
        assertEquals(6, original.size());
    }

    @Test
    public void testRemovePrivateAttributes2() {
        Attributes original = createOriginal();
        assertEquals(11, original.size());
        assertEquals(5, original.removePrivateAttributes());
        assertEquals(6, original.size());
    }

    @Test
    public void testSetString() {
        String[] MODALITIES_IN_STUDY = { "CT", "MR", "PR" };
        Attributes a = new Attributes();
        a.setString(Tag.ModalitiesInStudy, VR.CS, StringUtils.concat(MODALITIES_IN_STUDY, '\\'));
        assertArrayEquals(MODALITIES_IN_STUDY, a.getStrings(Tag.ModalitiesInStudy));
        assertEquals(MODALITIES_IN_STUDY[0], a.getString(Tag.ModalitiesInStudy));
    }

    @Test
    public void testDiffPN() {
        Attributes a = new Attributes(2);
        a.setString(Tag.PatientName, VR.PN, "Simson^Homer");
        a.setNull(Tag.ReferringPhysicianName, VR.PN);
        Attributes b = new Attributes(3);
        b.setString(Tag.PatientName, VR.PN, "Simson^Homer^^^");
        b.setString(Tag.ReferringPhysicianName, VR.PN, "^^^^");
        b.setString(Tag.RequestingPhysician, VR.PN, "^^^^");
        int[] selection = {
                Tag.ReferringPhysicianName,
                Tag.PatientName,
                Tag.RequestingPhysician};
        assertEquals(0, a.diff(b, selection, null));
        assertEquals(0, b.diff(a, selection, null));
    }

    @Test
    public void testContainsTagInRange_First() {
        Attributes a = new Attributes(1);
        a.setString(Tag.IssuerOfPatientID, VR.LO, "Issuer");

        assertTrue(a.containsTagInRange(Tag.IssuerOfPatientID, Tag.SourcePatientGroupIdentificationSequence));
    }

    @Test
    public void testContainsTagInRange_Last() {
        Attributes a = new Attributes(1);
        a.newSequence(Tag.SourcePatientGroupIdentificationSequence, 0);

        assertTrue(a.containsTagInRange(Tag.IssuerOfPatientID, Tag.SourcePatientGroupIdentificationSequence));
    }

    @Test
    public void testContainsTagInRange_Middle() {
        Attributes a = new Attributes(1);
        a.setString(Tag.TypeOfPatientID, VR.CS, "RFID");

        assertTrue(a.containsTagInRange(Tag.IssuerOfPatientID, Tag.SourcePatientGroupIdentificationSequence));
    }

    @Test
    public void testContainsTagInRange_Not() {
        Attributes a = new Attributes(2);
        a.setString(Tag.PatientID, VR.LO, "123");
        a.newSequence(Tag.GroupOfPatientsIdentificationSequence, 0);

        assertFalse(a.containsTagInRange(Tag.IssuerOfPatientID, Tag.SourcePatientGroupIdentificationSequence));
    }

    @Test
    public void testContainsTagInRange_Not_Empty() {
        Attributes a = new Attributes();

        assertFalse(a.containsTagInRange(Tag.IssuerOfPatientID, Tag.SourcePatientGroupIdentificationSequence));
    }
}
