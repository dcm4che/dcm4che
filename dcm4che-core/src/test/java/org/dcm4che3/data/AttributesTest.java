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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
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

import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.DateUtils;
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
    public void testSetTimezoneOffsetFromUTC() throws Exception {
        Attributes a = new Attributes();
        a.setDefaultTimeZone(DateUtils.timeZone("+0000"));
        a.setDate(Tag.StudyDateAndTime, new Date(0));
        a.setString(Tag.PatientBirthDate, VR.DA, "19700101");
        a.setString(Tag.PatientBirthTime, VR.TM, "000000.000");
        a.setString(Tag.ContextGroupVersion, VR.DT, "19700101");
        assertEquals("19700101", a.getString(Tag.StudyDate));
        assertEquals("000000.000", a.getString(Tag.StudyTime));

        a.setTimezoneOffsetFromUTC("+0100");
        assertEquals("19700101", a.getString(Tag.StudyDate));
        assertEquals("010000.000", a.getString(Tag.StudyTime));
        assertEquals("19700101", a.getString(Tag.PatientBirthDate));
        assertEquals("000000.000", a.getString(Tag.PatientBirthTime));
        assertEquals("19700101", a.getString(Tag.ContextGroupVersion));

        a.setTimezoneOffsetFromUTC("-0100");
        assertEquals("19691231", a.getString(Tag.StudyDate));
        assertEquals("230000.000", a.getString(Tag.StudyTime));
        assertEquals("19700101", a.getString(Tag.PatientBirthDate));
        assertEquals("000000.000", a.getString(Tag.PatientBirthTime));
        assertEquals("19700101", a.getString(Tag.ContextGroupVersion));
    }


    @Test
    public void testDateRangeSetTimezoneOffsetFromUTC() throws Exception {
        Attributes a = new Attributes();
        a.setDefaultTimeZone(DateUtils.timeZone("+0000"));
        DateRange range = new DateRange(new Date(0), new Date(3600000 * 12));
        a.setDateRange(Tag.StudyDateAndTime, range);
        assertEquals("19700101", a.getString(Tag.StudyDate));
        assertEquals("000000.000-120000.000", a.getString(Tag.StudyTime));
        assertEquals(range, a.getDateRange(Tag.StudyDateAndTime));
        a.setTimezoneOffsetFromUTC("-0100");
        assertEquals("19691231-19700101", a.getString(Tag.StudyDate));
        assertEquals("230000.000-110000.000", a.getString(Tag.StudyTime));
        assertEquals(range, a.getDateRange(Tag.StudyDateAndTime));
        a.setTimezoneOffsetFromUTC("+0100");
        assertEquals("19700101", a.getString(Tag.StudyDate));
        assertEquals("010000.000-130000.000", a.getString(Tag.StudyTime));
        assertEquals(range, a.getDateRange(Tag.StudyDateAndTime));
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

    @Test
    public void testDiffInAnotInBNoPrivate() {
        Attributes a = new Attributes();
        Attributes b = new Attributes();
        
        a.setString(Tag.StudyInstanceUID, VR.UI, "1");
        a.setString(Tag.SeriesInstanceUID, VR.UI, "2");
        a.setString(Tag.SOPInstanceUID, VR.UI, "3");
        
        b.setString(Tag.StudyInstanceUID, VR.UI, "1");
        Attributes diff = a.diff(b, true);
        assertTrue(diff.getString(Tag.SeriesInstanceUID).equalsIgnoreCase("2"));
        assertTrue(diff.getString(Tag.SOPInstanceUID).equalsIgnoreCase("3"));
    }

    @Test
    public void testDiffInBNotInASamePrivateAttrs() {
        Attributes a = new Attributes();
        Attributes b = new Attributes();
        
        a.setString(0x20011001,VR.LO,"Some Prop Attr");
        a.setString(Tag.StudyInstanceUID, VR.UI, "1");
        a.setString(Tag.SeriesInstanceUID, VR.UI, "2");
        a.setString(Tag.SOPInstanceUID, VR.UI, "3");
        a.setString(0x20010010,VR.LO,"vendor1");
        
        b.setString(Tag.StudyInstanceUID, VR.UI, "1");
        b.setString(Tag.PatientID, VR.LO, "4");
        b.setString(0x20010010,VR.LO,"vendor1");
        b.setString(0x20011001,VR.LO,"Some Prop Attr");
        Attributes diff = b.diff(a, false);
        
        assertTrue(diff.getString(Tag.PatientID).equalsIgnoreCase("4"));
    }

    @Test
    public void testDiffInBNotInADifferentPrivateCreator() {
        Attributes a = new Attributes();
        Attributes b = new Attributes();
        
        a.setString(0x20011001,VR.LO,"Some Prop Attr");
        a.setString(Tag.StudyInstanceUID, VR.UI, "1");
        a.setString(Tag.SeriesInstanceUID, VR.UI, "2");
        a.setString(Tag.SOPInstanceUID, VR.UI, "3");
        a.setString(0x20010010,VR.LO,"vendorx");
        
        b.setString(Tag.StudyInstanceUID, VR.UI, "1");
        b.setString(Tag.PatientID, VR.LO, "4");
        b.setString(0x20010010,VR.LO,"vendory");
        b.setString(0x20011001,VR.LO,"Some Prop Attr");
        Attributes diff = b.diff(a, false);
        
        assertTrue(diff.getString(0x20010010).equalsIgnoreCase("vendory"));
        assertTrue(diff.getString(0x20011001).equalsIgnoreCase("Some Prop Attr"));
    }

    @Test
    public void testDiffBothNoPrivate() {
        Attributes a = new Attributes();
        Attributes b = new Attributes();
        
        a.setString(Tag.StudyInstanceUID, VR.UI, "1");
        a.setString(Tag.SeriesInstanceUID, VR.UI, "2");
        a.setString(Tag.SOPInstanceUID, VR.UI, "3");
        
        b.setString(Tag.StudyInstanceUID, VR.UI, "1");
        b.setString(Tag.PatientID, VR.LO, "4");
        
        Attributes diff = a.diff(b, true);
        assertTrue(diff.getString(Tag.SeriesInstanceUID).equalsIgnoreCase("2"));
        assertTrue(diff.getString(Tag.SOPInstanceUID).equalsIgnoreCase("3"));
        assertTrue(diff.getString(Tag.PatientID).equalsIgnoreCase("4"));
    }

    @Test
    public void testDiffBothDifferentPrivateCreator() {
        Attributes a = new Attributes();
        Attributes b = new Attributes();
        
        a.setString(0x20011001,VR.LO,"Some Prop Attr");
        a.setString(Tag.StudyInstanceUID, VR.UI, "1");
        a.setString(Tag.SeriesInstanceUID, VR.UI, "2");
        a.setString(Tag.SOPInstanceUID, VR.UI, "3");
        a.setString(0x20010010,VR.LO,"vendorx");
        
        b.setString(Tag.StudyInstanceUID, VR.UI, "1");
        b.setString(Tag.PatientID, VR.LO, "4");
        b.setString(0x20010010,VR.LO,"vendory");
        b.setString(0x20011001,VR.LO,"Some Prop Attr");
        Attributes diff = b.diff(a, true);
        
        assertTrue(diff.getString(Tag.SeriesInstanceUID).equalsIgnoreCase("2"));
        assertTrue(diff.getString(Tag.SOPInstanceUID).equalsIgnoreCase("3"));
        assertTrue(diff.getString(Tag.PatientID).equalsIgnoreCase("4"));
        assertTrue(diff.getString(0x20010010).equalsIgnoreCase("vendory"));
        assertTrue(diff.getString(0x20011001).equalsIgnoreCase("Some Prop Attr"));
        assertTrue(diff.getString(0x20010011).equalsIgnoreCase("vendorx"));
        assertTrue(diff.getString(0x20011101).equalsIgnoreCase("Some Prop Attr"));
        
    }

    @Test
    public void testAddWithoutBulkData() {
        Attributes a = new Attributes();
        Attributes b = new Attributes();

        a.setInt(Tag.BitsAllocated, VR.US, 8);
        a.setBytes(Tag.PixelData, VR.OW, new byte[1000]);

        Attributes wfItem = new Attributes();
        wfItem.setString(Tag.WaveformOriginality, VR.CS, "ORIGINAL");
        wfItem.setBytes(Tag.WaveformData, VR.OB, new byte[1000]);

        Sequence wfSeq = a.newSequence(Tag.WaveformSequence, 1);
        wfSeq.add(wfItem);

        a.setString("org.dcm4che", 0x99990001, VR.SH, "test");

        b.addWithoutBulkData(a, BulkDataDescriptor.DEFAULT);

        assertEquals(8, b.getInt(Tag.BitsAllocated, 0));
        assertFalse(b.contains(Tag.PixelData));

        Attributes wfItem2 = b.getNestedDataset(Tag.WaveformSequence);
        assertNotNull(wfItem2);
        assertEquals("ORIGINAL", wfItem2.getString(Tag.WaveformOriginality));
        assertFalse(wfItem2.contains(Tag.WaveformData));

        assertEquals("org.dcm4che", b.getString(0x99990010));
        assertEquals("test", b.getString(0x99991001));
    }

    @Test
    public void testItemPointer() {
        Attributes a = new Attributes(1);
        Attributes b = new Attributes(1);
        Attributes c = new Attributes(1);
        Attributes d = new Attributes(1);
        Sequence seq1 = a.newSequence(Tag.ContentSequence, 2);
        Sequence seq2 = b.newSequence("DCM4CHE", 0x99990010, 1);
        seq1.add(b);
        seq1.add(c);
        seq2.add(d);
        ItemPointer[] ipa = {};
        ItemPointer[] ipb = { new ItemPointer(Tag.ContentSequence) };
        ItemPointer[] ipc = { new ItemPointer(Tag.ContentSequence, 1) };
        ItemPointer[] ipd = {
                new ItemPointer(Tag.ContentSequence),
                new ItemPointer("DCM4CHE", 0x99990010)
        };
        assertArrayEquals(ipa, a.itemPointers());
        assertArrayEquals(ipb, b.itemPointers());
        assertArrayEquals(ipc, c.itemPointers());
        assertArrayEquals(ipd, d.itemPointers());
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
}
