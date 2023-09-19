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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 *
 */
public class AttributesTest {

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
    public void testPrivateTagEqualsWithoutPrivateCreator() {
        Attributes a1 = new Attributes();
        a1.setString(0x00091010, VR.LO, "VALUE1");
        Attributes a2 = new Attributes();
        a2.setString(0x00091010, VR.LO, "VALUE1");
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));
    }

    @Test
    public void testPrivateTagNotEqualsWithoutPrivateCreator() {
        Attributes a1 = new Attributes();
        a1.setString(0x00090010, VR.LO, "CREATOR1");
        a1.setString(0x00091010, VR.LO, "VALUE1");
        Attributes a2 = new Attributes();
        a2.setString(0x00090020, VR.LO, "CREATOR2");
        a2.setString(0x00091010, VR.LO, "VALUE1");
        assertFalse(a1.equals(a2));
        assertFalse(a2.equals(a1));
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
        ItemPointer[] ipb = { new ItemPointer(Tag.ContentSequence, 0) };
        ItemPointer[] ipc = { new ItemPointer(Tag.ContentSequence, 1) };
        ItemPointer[] ipd = {
                new ItemPointer(Tag.ContentSequence, 0),
                new ItemPointer("DCM4CHE", 0x99990010, 0)
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

    @Test
    public void testPreserveEmptyPrivateCreator() {
        Attributes original = new Attributes();
        original.setString(0x00990010, VR.LO, "PrivateCreatorA");
        original.setString(0x00990020, VR.LO, "PrivateCreatorB");
        Attributes copy = new Attributes(original);
        assertEquals("PrivateCreatorA", copy.getString(0x00990010));
        assertEquals("PrivateCreatorB", copy.getString(0x00990020));
    }

    @Test
    public void testPreserveDuplicatePrivateCreator() {
        Attributes original = new Attributes();
        original.setString(0x00990010, VR.LO, "PrivateCreatorA");
        original.setString(0x00990020, VR.LO, "PrivateCreatorA");
        original.setString(0x00991001, VR.LO, "Private1");
        original.setString(0x00992001, VR.LO, "Private2");
        Attributes copy = new Attributes(original);
        assertEquals("PrivateCreatorA", copy.getString(0x00990010));
        assertEquals("PrivateCreatorA", copy.getString(0x00990020));
        assertEquals("Private1", copy.getString(0x00991001));
        assertEquals("Private2", copy.getString(0x00992001));
    }

    @Test
    public void testMergePrivateGroups() {
        Attributes attrs = new Attributes();
        attrs.setString("PrivateCreatorA", 0x00990001, VR.LO, "1A");
        Attributes other = new Attributes();
        other.setString("PrivateCreatorB", 0x00990001, VR.LO, "1B");
        other.setString("PrivateCreatorA", 0x00990002, VR.LO, "2A");
        attrs.addAll(other);
        assertEquals(5, attrs.size());
        assertEquals("PrivateCreatorA", attrs.getString(0x00990010));
        assertEquals("PrivateCreatorB", attrs.getString(0x00990011));
        assertEquals("1A", attrs.getString(0x00991001));
        assertEquals("2A", attrs.getString(0x00991002));
        assertEquals("1B", attrs.getString(0x00991101));
    }

    @Test
    public void testRemoveOverlayData() {
        Attributes attrs = new Attributes();
        attrs.setNull(Tag.SpecificCharacterSet, VR.CS);
        attrs.setInt(Tag.OverlayRows, VR.US, 1234);
        attrs.setInt(Tag.OverlayColumns, VR.US, 1234);
        attrs.setInt(Tag.OverlayRows | 0x00020000, VR.US, 1234);
        attrs.setInt(Tag.OverlayColumns | 0x00020000, VR.US, 1234);
        attrs.setNull("PRIVATE", 0x60110001, VR.OB);
        attrs.setInt(Tag.OverlayRows | 0x001E0000, VR.US, 1234);
        attrs.setInt(Tag.OverlayColumns | 0x001E0000, VR.US, 1234);
        attrs.setInt(Tag.OverlayRows | 0x00200000, VR.US, 1234);
        attrs.setInt(Tag.OverlayColumns | 0x00200000, VR.US, 1234);
        attrs.setNull(Tag.PixelData, VR.OB);
        attrs.removeOverlayData();
        assertEquals(6, attrs.size());
    }

    @Test
    public void testNullPrivateCreator() {
        Attributes attrs = new Attributes();
        attrs.setNull(0x00990010, VR.LO);
        assertTrue(new Attributes(attrs).contains(0x00990010));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFromIncompatibleCharacterSet() {
        Attributes a = new Attributes();
        a.setString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 100");
        a.setBytes(Tag.PatientName, VR.PN, "Äneas^Rüdiger".getBytes(StandardCharsets.ISO_8859_1));
        Attributes b = new Attributes();
        b.setNull(Tag.PatientName, VR.PN);
        b.addSelected(a, Tag.PatientName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddIncompatibleCharacterSet() {
        Attributes a = new Attributes();
        a.setNull(Tag.SpecificCharacterSet, VR.CS);
        Attributes b = new Attributes();
        b.setString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 100");
        b.setBytes(Tag.PatientName, VR.PN, "Äneas^Rüdiger".getBytes(StandardCharsets.ISO_8859_1));
        b.addAll(a);
    }

    @Test
    public void testAddFromCompatibleCharacterSet() {
        Attributes a = new Attributes();
        a.setBytes(Tag.PatientName, VR.PN, "Aeneas^Ruediger".getBytes(StandardCharsets.US_ASCII));
        Attributes b = new Attributes();
        b.setString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 100");
        b.setNull(Tag.PatientName, VR.PN);
        b.addSelected(a, Tag.PatientName);
        assertEquals("Aeneas^Ruediger", b.getString(Tag.PatientName));
    }

    @Test
    public void testAddCompatibleCharacterSet() {
        Attributes a = new Attributes();
        a.setString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 100");
        a.setBytes(Tag.PatientName, VR.PN, "Äneas^Rüdiger".getBytes(StandardCharsets.ISO_8859_1));
        Attributes b = new Attributes();
        b.setNull(Tag.PatientName, VR.PN);
        b.addAll(a);
        assertEquals("ISO_IR 100", b.getString(Tag.SpecificCharacterSet));
        assertEquals("Äneas^Rüdiger", b.getString(Tag.PatientName));
    }

    @Test
    public void testAddCompatibleCharacterSet2() {
        Attributes a = new Attributes();
        a.setString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 100");
        a.setBytes(Tag.PatientName, VR.PN, "Äneas^Rüdiger".getBytes(StandardCharsets.ISO_8859_1));
        a.setString(Tag.PatientSex, VR.CS, "M");
        Attributes b = new Attributes();
        b.setNull(Tag.PatientName, VR.PN);
        b.addSelected(a, Tag.PatientSex);
    }

    @Test
    public void testAddCompatibleCharacterSet3() throws IOException {
        Attributes a = new Attributes();
        a.setString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 100");
        a.setBytes(Tag.PatientName, VR.PN, "Äneas^Rüdiger".getBytes(StandardCharsets.ISO_8859_1));
        Attributes b = new Attributes();
        b.setString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 192");
        b.addSelected(a, Tag.PatientName);
        assertArrayEquals("Äneas^Rüdiger".getBytes(StandardCharsets.UTF_8), b.getBytes(Tag.PatientName));
    }

    @Test
    public void testAddCompatibleCharacterSet4() throws IOException {
        Attributes a = new Attributes();
        a.setString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 192");
        a.setString(Tag.PatientSex, VR.CS, "M");
        Attributes b = new Attributes();
        b.setString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 100");
        b.setBytes(Tag.PatientName, VR.PN, "Äneas^Rüdiger".getBytes(StandardCharsets.ISO_8859_1));
        b.addAll(a);
        assertArrayEquals("Äneas^Rüdiger".getBytes(StandardCharsets.UTF_8), b.getBytes(Tag.PatientName));
    }

    @Test
    public void testAddCompatibleCharacterSet5() throws IOException {
        Attributes a = new Attributes();
        a.setString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 100");
        a.setBytes(Tag.PatientName, VR.PN, "Äneas^Rüdiger".getBytes(StandardCharsets.ISO_8859_1));
        Attributes b = new Attributes();
        b.setString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 192");
        b.addSelected(a, null, Tag.PatientName);
        assertArrayEquals("Äneas^Rüdiger".getBytes(StandardCharsets.UTF_8), b.getBytes(Tag.PatientName));
    }

    @Test
    public void testAddShouldCorrectlyDecodeStrings() {
        Attributes aLeft = new Attributes();
        aLeft.setSpecificCharacterSet("ISO_IR 192");

        Attributes aRight = new Attributes();
        aRight.setSpecificCharacterSet("ISO_IR 100");
        byte[] studyid = new byte[] {0x33, 0x33, 0x34, 0x31, 0x34, 0x20};
        aRight.setBytes(Tag.StudyID, VR.SH, studyid);

        aLeft.addNotSelected(aRight, Tag.SpecificCharacterSet);

        assertEquals("Padding space should be removed", "33414", aLeft.getString(Tag.StudyID));
    }

    @Test
    public void testAddSelectedShouldCorrectlyDecodeStrings() {
        Attributes aLeft = new Attributes();
        aLeft.setSpecificCharacterSet("ISO_IR 192");

        Attributes aRight = new Attributes();
        aRight.setSpecificCharacterSet("ISO_IR 100");
        byte[] studyid = new byte[] {0x33, 0x33, 0x34, 0x31, 0x34, 0x20};
        aRight.setBytes(Tag.StudyID, VR.SH, studyid);

        aLeft.addSelected(aRight, null, Tag.StudyID);

        assertEquals("Padding space should be removed", "33414", aLeft.getString(Tag.StudyID));
    }

    @Test
    public void testGetValuePrivateCreatorSh() {
        Attributes dataset = new Attributes();

        String shPrivateCreator = "shPrivateCreator";
        int privateTag = 0x15030003;
        String privateValue = "some private value";

        int resolvedPrivateCreatorTag = 0x15030010;
        int resolvedPrivateTag = 0x15031003;

        dataset.setString(resolvedPrivateCreatorTag, VR.SH, shPrivateCreator);
        dataset.setString(resolvedPrivateTag, VR.LO, privateValue);

        assertEquals(privateValue, dataset.getString(shPrivateCreator, privateTag));
    }

    @Test
    public void testReadOnly() {
        Attributes attrs = new Attributes();
        Sequence seq = attrs.newSequence(Tag.OtherPatientIDsSequence, 1);
        Attributes otherPID = new Attributes();
        otherPID.setString(Tag.PatientID, VR.LO, "PatientID");
        otherPID.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");
        seq.add(otherPID);
        attrs.setReadOnly();
        assertTrue(otherPID.isReadOnly());
        assertEquals("PatientID", otherPID.getString(Tag.PatientID));
        try {
            otherPID.setString(Tag.PatientID, VR.LO, "ChangedPatientID");
            fail("Expected exception: java.lang.UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}
        try {
            otherPID.remove(Tag.PatientID);
            fail("Expected exception: java.lang.UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}
        try {
            seq.clear();
            fail("Expected exception: java.lang.UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}
    }

    @Test
    public void testMixedEndian() throws IOException {
        Attributes bigEndian = new Attributes(true);
        Attributes modifiedAttributes = new Attributes();
        modifiedAttributes.setInt(Tag.PixelRepresentation, VR.US, 1);
        assertFalse(modifiedAttributes.bigEndian());
        assertArrayEquals(modifiedAttributes.getBytes(Tag.PixelRepresentation), new byte[]{1,0});
        bigEndian.addOriginalAttributes(
                null,
                new Date(),
                "COERCE",
                "dcm4che",
                modifiedAttributes);
        Attributes originalAttributes = bigEndian.getNestedDataset(Tag.OriginalAttributesSequence);
        assertTrue(originalAttributes.bigEndian());
        assertTrue(modifiedAttributes.bigEndian());
        assertArrayEquals(modifiedAttributes.getBytes(Tag.PixelRepresentation), new byte[]{0,1});
    }

    @Test
    public void testThatNoExceptionWhenPublicTagsAfterPrivateCreators() {
        Attributes attributes = new Attributes();
        attributes.setString("MyCreator", 0x00290018, VR.LO, "foo");

        Attributes toAdd = new Attributes();
        toAdd.setString("MyCreator2", 0x00290018, VR.LO, "bar");
        toAdd.setString(Tag.PerformedProcedureStepDescription, VR.LO, "CTABD  Abdomen");

        attributes.addAll(toAdd);

        assertEquals(5, attributes.size());
    }
}
