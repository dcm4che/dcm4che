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

import org.dcm4che3.io.BasicBulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.StringUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.*;

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
    public void testGetModified_LIB_363()
    {
        // tests the fix for LIB-363

        Attributes original = new Attributes();
        original.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        
        Attributes other = new Attributes();
        other.setString(Tag.SOPInstanceUID, VR.UI, "1.2.3.4");
        other.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber2");

        Attributes modified = original.getModified(other, null);

        Attributes expected = new Attributes();
        expected.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");

        assertEquals(expected, modified);
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

    @Test
    public void testBulkdata() throws IOException {
        byte[] BYTES = { Byte.MIN_VALUE, 0, Byte.MAX_VALUE, 0 };
        String[] STRINGS = { "VALUE1", "VALUE2" };
        String[] AGES = { "018M", "018Y" };
        String[] DATES = { "19560708", "20010203" };
        String[] TIMES = { "1956", "2001" };
        String[] UIDS = { UID.CTImageStorage, UID.MRImageStorage };
        int[] TAGS = {
                Tag.SelectorAEValue,
                Tag.SelectorASValue,
                Tag.SelectorATValue,
                Tag.SelectorDAValue,
                Tag.SelectorCSValue,
                Tag.SelectorDTValue,
                Tag.SelectorISValue,
                Tag.SelectorOBValue,
                Tag.SelectorLOValue,
                Tag.SelectorOFValue,
                Tag.SelectorLTValue,
                Tag.SelectorOWValue,
                Tag.SelectorPNValue,
                Tag.SelectorTMValue,
                Tag.SelectorSHValue,
                Tag.SelectorUNValue,
                Tag.SelectorSTValue,
                Tag.SelectorUCValue,
                Tag.SelectorUTValue,
                Tag.SelectorURValue,
                Tag.SelectorDSValue,
                Tag.SelectorODValue,
                Tag.SelectorFDValue,
                Tag.SelectorOLValue,
                Tag.SelectorFLValue,
                Tag.SelectorULValue,
                Tag.SelectorUSValue,
                Tag.SelectorSLValue,
                Tag.SelectorSSValue,
                Tag.SelectorUIValue
        };
        int[] INTS = { Short.MIN_VALUE,  Short.MAX_VALUE };
        int[] UINTS = { 0xffff,  Short.MAX_VALUE };
        float[] FLOATS = { -Float.MIN_VALUE,  0.1234f, Float.MAX_VALUE };
        double[] DOUBLES = { -Double.MIN_VALUE,  0.1234, Double.MAX_VALUE };
        String URI = "http://host/path";

        Attributes a = new Attributes();
        a.setString(Tag.SelectorAEValue, VR.AE, STRINGS);
        a.setString(Tag.SelectorASValue, VR.AS, AGES);
        a.setInt(Tag.SelectorATValue, VR.AT, TAGS);
        a.setString(Tag.SelectorDAValue, VR.DA, DATES);
        a.setString(Tag.SelectorCSValue, VR.CS, STRINGS);
        a.setString(Tag.SelectorDTValue, VR.DT, DATES);
        a.setInt(Tag.SelectorISValue, VR.IS, INTS);
        a.setBytes(Tag.SelectorOBValue, VR.OB, BYTES);
        a.setString(Tag.SelectorLOValue, VR.LO, STRINGS);
        a.setFloat(Tag.SelectorOFValue, VR.OF, FLOATS);
        a.setString(Tag.SelectorLTValue, VR.LT, URI);
        a.setInt(Tag.SelectorOWValue, VR.OW, INTS);
        a.setString(Tag.SelectorPNValue, VR.PN, STRINGS);
        a.setString(Tag.SelectorTMValue, VR.TM, TIMES);
        a.setString(Tag.SelectorSHValue, VR.SH, STRINGS);
        a.setBytes(Tag.SelectorUNValue, VR.UN, BYTES);
        a.setString(Tag.SelectorSTValue, VR.ST, URI);
        a.setString(Tag.SelectorUCValue, VR.UC, STRINGS);
        a.setString(Tag.SelectorUTValue, VR.UT, URI);
        a.setString(Tag.SelectorURValue, VR.UR, URI);
        a.setFloat(Tag.SelectorDSValue, VR.DS, FLOATS);
        a.setDouble(Tag.SelectorODValue, VR.OD, DOUBLES);
        a.setDouble(Tag.SelectorFDValue, VR.FD, DOUBLES);
        a.setInt(Tag.SelectorOLValue, VR.OL, INTS);
        a.setFloat(Tag.SelectorFLValue, VR.FL, FLOATS);
        a.setInt(Tag.SelectorULValue, VR.UL, UINTS);
        a.setInt(Tag.SelectorUSValue, VR.US, UINTS);
        a.setInt(Tag.SelectorSLValue, VR.SL, INTS);
        a.setInt(Tag.SelectorSSValue, VR.SS, INTS);
        a.setString(Tag.SelectorUIValue, VR.UI, UIDS);
        DicomInputStream in = asDicomInputStream(a);
        try {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            in.setBulkDataDescriptor(new BasicBulkDataDescriptor().excludeDefaults().addTag(TAGS));
            in.setConcatenateBulkDataFiles(true);
            Attributes b = in.readDataset(-1, -1);
            for (int tag : TAGS) {
                assertTrue(b.getValue(tag) instanceof BulkData);
            }
            assertArrayEquals(STRINGS, b.getStrings(Tag.SelectorAEValue));
            assertArrayEquals(AGES, b.getStrings(Tag.SelectorASValue));
            assertEquals(TAGS[0], b.getInt(Tag.SelectorATValue, 0));
            assertArrayEquals(DATES, b.getStrings(Tag.SelectorDAValue));
            assertEquals(STRINGS[0], b.getString(Tag.SelectorCSValue));
            assertArrayEquals(DATES, b.getStrings(Tag.SelectorDTValue));
            assertArrayEquals(INTS, b.getInts(Tag.SelectorISValue));
            assertArrayEquals(BYTES, b.getBytes(Tag.SelectorOBValue));
            assertArrayEquals(STRINGS, b.getStrings(Tag.SelectorLOValue));
            assertArrayEquals(FLOATS, b.getFloats(Tag.SelectorOFValue), 0);
            assertEquals(URI, b.getString(Tag.SelectorLTValue));
            assertArrayEquals(INTS, b.getInts(Tag.SelectorOWValue));
            assertArrayEquals(STRINGS, b.getStrings(Tag.SelectorPNValue));
            assertArrayEquals(TIMES, b.getStrings(Tag.SelectorTMValue));
            assertArrayEquals(STRINGS, b.getStrings(Tag.SelectorSHValue));
            assertArrayEquals(BYTES, b.getBytes(Tag.SelectorUNValue));
            assertEquals(URI, b.getString(Tag.SelectorSTValue));
            assertArrayEquals(STRINGS, b.getStrings(Tag.SelectorUCValue));
            assertEquals(URI, b.getString(Tag.SelectorUTValue));
            assertEquals(URI, b.getString(Tag.SelectorURValue));
            assertArrayEquals(FLOATS, b.getFloats(Tag.SelectorDSValue), 0);
            assertArrayEquals(DOUBLES, b.getDoubles(Tag.SelectorODValue), 0);
            assertEquals(DOUBLES[0], b.getDouble(Tag.SelectorFDValue, 0), 0);
            assertArrayEquals(INTS, b.getInts(Tag.SelectorOLValue));
            assertEquals(FLOATS[0], b.getFloat(Tag.SelectorFLValue, 0), 0);
            assertArrayEquals(UINTS, b.getInts(Tag.SelectorULValue));
            assertEquals(UINTS[0], b.getInt(Tag.SelectorUSValue, 0));
            assertArrayEquals(INTS, b.getInts(Tag.SelectorSLValue));
            assertEquals(INTS[0], b.getInt(Tag.SelectorSSValue, 0));
            assertArrayEquals(UIDS, b.getStrings(Tag.SelectorUIValue));
        } finally {
            for (File f : in.getBulkDataFiles()) {
                f.delete();
            }
        }
    }

    private static DicomInputStream asDicomInputStream(Attributes a) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DicomOutputStream out = new DicomOutputStream(baos, UID.ExplicitVRLittleEndian)) {
            out.writeDataset(null, a);
        }
        return new DicomInputStream(new ByteArrayInputStream(baos.toByteArray()), UID.ExplicitVRLittleEndian);
    }
    

    @Test
    public void testAddSelectedWithSelectionAttributes()
    {
        Attributes original = new Attributes();
        Attributes otherPID = new Attributes();
        original.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        original.setNull(Tag.PatientName, VR.PN);
        original.setString(Tag.PatientID, VR.LO, "PatientID");
        original.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");
        original.newSequence(Tag.OtherPatientIDsSequence, 1).add(otherPID);
        Sequence requestAttributesSequence = original.newSequence(Tag.RequestAttributesSequence, 2);
        Attributes rqAttrs1 = new Attributes();
        rqAttrs1.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID1");
        rqAttrs1.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID1");
        Attributes rqAttrs2 = new Attributes();
        rqAttrs2.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID2");
        rqAttrs2.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID2");
        requestAttributesSequence.add(rqAttrs1);
        requestAttributesSequence.add(rqAttrs2);
        original.setString("PrivateCreatorA", 0x00990001, VR.LO, "0099xx01A");
        original.setString("PrivateCreatorB", 0x00990001, VR.LO, "0099xx01B");
        original.setString("PrivateCreatorB", 0x00990002, VR.LO, "0099xx02B");
        otherPID.setString(Tag.PatientID, VR.LO, "OtherPatientID");
        otherPID.setString(Tag.IssuerOfPatientID, VR.LO, "OtherIssuerOfPatientID");

        Attributes selection = new Attributes();
        selection.setNull(Tag.AccessionNumber, VR.SH);
        selection.setNull(Tag.PatientName, VR.PN);
        // select complete other patient id sequence
        selection.newSequence(Tag.OtherPatientIDsSequence, 0);
        // sub-selection inside the RequestAttributesSequence
        Attributes rqAttrsSelection = new Attributes();
        rqAttrsSelection.setNull(Tag.ScheduledProcedureStepID, VR.LO);
        selection.newSequence(Tag.RequestAttributesSequence, 1).add(rqAttrsSelection);

        // filter the original with the selection
        Attributes filtered = new Attributes();
        filtered.addSelected(original, selection); // THIS is the method we want to test here

        // that is the expected result
        Attributes filteredExpected = new Attributes();
        filteredExpected.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        filteredExpected.setNull(Tag.PatientName, VR.PN);
        Attributes filteredExpectedOtherPID = new Attributes();
        filteredExpectedOtherPID.setString(Tag.PatientID, VR.LO, "OtherPatientID");
        filteredExpectedOtherPID.setString(Tag.IssuerOfPatientID, VR.LO, "OtherIssuerOfPatientID");
        filteredExpected.newSequence(Tag.OtherPatientIDsSequence, 1).add(filteredExpectedOtherPID);
        Sequence requestAttributesSequenceFilteredExpected = filteredExpected.newSequence(Tag.RequestAttributesSequence, 2);
        Attributes rqAttrs1FilteredExpected = new Attributes();
        rqAttrs1FilteredExpected.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID1");
        Attributes rqAttrs2FilteredExpected = new Attributes();
        rqAttrs2FilteredExpected.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID2");
        requestAttributesSequenceFilteredExpected.add(rqAttrs1FilteredExpected);
        requestAttributesSequenceFilteredExpected.add(rqAttrs2FilteredExpected);

        assertEquals(filteredExpected, filtered);
    }

    @Test
    public void testAddSelectedWithSelectionAttributesInsideSequence()
    {
        Attributes original = new Attributes();
        Sequence requestAttributesSequence = original.newSequence(Tag.RequestAttributesSequence, 2);
        Attributes rqAttrs1 = new Attributes();
        rqAttrs1.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID1");
        rqAttrs1.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID1");
        Attributes rqAttrs2 = new Attributes();
        rqAttrs2.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID2");
        rqAttrs2.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID2");
        requestAttributesSequence.add(rqAttrs1);
        requestAttributesSequence.add(rqAttrs2);

        Attributes selection = new Attributes();
        // sub-selection inside the RequestAttributesSequence
        // this test just documents the behavior that for the selection only the first item within a sequence is considered
        Attributes rqAttrsSelection = new Attributes();
        rqAttrsSelection.setNull(Tag.ScheduledProcedureStepID, VR.LO);
        Attributes rqAttrsIgnoredSelection = new Attributes();
        rqAttrsIgnoredSelection.setNull(Tag.RequestedProcedureID, VR.LO);
        Sequence requestAttrsSeqSelection = selection.newSequence(Tag.RequestAttributesSequence, 2);
        requestAttrsSeqSelection.add(rqAttrsSelection);
        requestAttrsSeqSelection.add(rqAttrsIgnoredSelection); // this one will not be considered for the selection

        // filter the original with the selection
        Attributes filtered = new Attributes();
        filtered.addSelected(original, selection); // THIS is the method we want to test here

        // that is the expected result
        Attributes filteredExpected = new Attributes();
        Sequence requestAttributesSequenceFilteredExpected = filteredExpected.newSequence(Tag.RequestAttributesSequence, 2);
        Attributes rqAttrs1FilteredExpected = new Attributes();
        rqAttrs1FilteredExpected.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID1");
        Attributes rqAttrs2FilteredExpected = new Attributes();
        rqAttrs2FilteredExpected.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID2");
        requestAttributesSequenceFilteredExpected.add(rqAttrs1FilteredExpected);
        requestAttributesSequenceFilteredExpected.add(rqAttrs2FilteredExpected);

        assertEquals(filteredExpected, filtered);
    }

    @Test
    public void testAddSelectedWithSelectionAttributesPrivateTags()
    {
        // tests the fix for LIB-362

        Attributes original = new Attributes();
        original.setString("PrivateCreatorA", 0x00990001, VR.LO, "0099xx01A");
        original.setString("PrivateCreatorB", 0x00990001, VR.LO, "0099xx01B");

        Attributes selection = new Attributes();
        selection.setNull("PrivateCreatorB", 0x00990001, VR.LO);

        Attributes filtered = new Attributes();
        filtered.addSelected(original, selection); // THIS is the method we want to test here

        // that is the expected result
        Attributes filteredExpected = new Attributes();
        filteredExpected.setString("PrivateCreatorB", 0x00990001, VR.LO, "0099xx01B");

        assertEquals(filteredExpected, filtered);
    }

    @Test
    public void testAddSelectedWithSelectionAttributesPrivateTags2()
    {
        Attributes original = new Attributes();
        original.setString("PrivateCreatorA", 0x00990001, VR.LO, "0099xx01A");
        original.setString("PrivateCreatorB", 0x00990001, VR.LO, "0099xx01B");
        original.setString("PrivateCreatorC", 0x00990001, VR.LO, "0099xx01C");

        Attributes selection = new Attributes();
        selection.setNull("PrivateCreatorA", 0x00990001, VR.LO);
        selection.setNull("PrivateCreatorC", 0x00990001, VR.LO);

        Attributes filtered = new Attributes();
        filtered.addSelected(original, selection); // THIS is the method we want to test here

        // that is the expected result
        Attributes filteredExpected = new Attributes();
        filteredExpected.setString("PrivateCreatorA", 0x00990001, VR.LO, "0099xx01A");
        filteredExpected.setString("PrivateCreatorC", 0x00990001, VR.LO, "0099xx01C");

        assertEquals(filteredExpected, filtered);
    }

    @Test
    public void testAddSelectedWithSelectionAttributesInsidePrivateSequence()
    {
        Attributes original = new Attributes();
        original.setString("PrivateCreatorA", 0x00990001, VR.LO, "0099xx01A");
        Sequence privateSeq = original.newSequence("PrivateCreatorB", 0x00990001, 2);
        privateSeq.add(new Attributes());
        privateSeq.get(0).setString(Tag.SOPInstanceUID, VR.UI, "1.2.3.4");
        privateSeq.get(0).setString(Tag.SOPClassUID, VR.UI, "4.3.2.1");

        Attributes selection = new Attributes();
        Sequence privateSeqSelection = selection.newSequence("PrivateCreatorB", 0x00990001, 2);
        privateSeqSelection.add(new Attributes());
        privateSeqSelection.get(0).setNull(Tag.SOPInstanceUID, VR.UI);

        Attributes filtered = new Attributes();
        filtered.addSelected(original, selection); // THIS is the method we want to test here

        // that is the expected result
        Attributes filteredExpected = new Attributes();
        Sequence privateSeqExpected = filteredExpected.newSequence("PrivateCreatorB", 0x00990001, 2);
        privateSeqExpected.add(new Attributes());
        privateSeqExpected.get(0).setString(Tag.SOPInstanceUID, VR.UI, "1.2.3.4");

        assertEquals(filteredExpected, filtered);
    }
}
