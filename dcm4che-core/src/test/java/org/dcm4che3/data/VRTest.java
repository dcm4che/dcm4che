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

import org.dcm4che3.util.ByteUtils;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class VRTest {

    private static final SpecificCharacterSet CS =
            SpecificCharacterSet.ASCII;

    private static final byte[] DCM4CHEE_AS_AE = 
            { 'D', 'C', 'M', '4', 'C', 'H', 'E', 'E' };

    private static final int[] INTS = { 0, 1, -2 };
    private static final int[] UINTS = { 0, 1, -2 & 0xffff };
    private static final String[] INTS_AS_STRINGS = { "0", "1", "-2" };
    private static final byte[] INTS_AS_IS =
            { '0', '\\' , '1', '\\', '-', '2'};
    private static final byte[] INTS_AS_OB = { 0, 1, -2 };
    private static final byte[] INTS_AS_SS = { 0, 0, 1, 0, -2, -1 };
    private static final byte[] INTS_AS_SS_BE = { 0, 0, 0, 1, -1, -2 };
    private static final byte[] INTS_AS_SL =
            { 0, 0, 0, 0, 1, 0, 0, 0, -2, -1, -1, -1 };
    private static final byte[] INTS_AS_SL_BE =
            { 0, 0, 0, 0, 0, 0, 0, 1, -1, -1, -1, -2 };

    private static final int[] TAGS = { Tag.PatientID, Tag.StudyID };
    private static final String[] TAGS_AS_STRINGS = { "00100020", "00200010" };
    private static final byte[] TAGS_AS_AT =
            { 0x10, 0x00, 0x20, 0x00, 0x20, 0x00, 0x10, 0x00 };
    private static final byte[] TAGS_AS_AT_BE =
            { 0x00, 0x10, 0x00, 0x20, 0x00, 0x20, 0x00, 0x10 };

    private static final float[] FLOATS = { 0, 0.12f };
    private static final String[] FLOATS_AS_STRINGS = { "0", "0.12" };
    private static final byte[] FLOATS_AS_DS =
            { '0', '\\' , '0', '.', '1', '2'};
    private static final byte[] FLOATS_AS_FL = new byte[8];
    private static final byte[] FLOATS_AS_FL_BE = new byte[8];
    private static final byte[] FLOATS_AS_FD = new byte[16];

    private static final double[] DOUBLES = { 0, 0.12 };
    private static final double[] DOUBLES_AS_FLOAT = { 0, (float) 0.12 };
    private static final byte[] DOUBLES_AS_FL = new byte[8];
    private static final byte[] DOUBLES_AS_FD = new byte[16];
    private static final byte[] DOUBLES_AS_FD_BE = new byte[16];
    static {
        ByteUtils.floatToBytesLE(FLOATS[0], FLOATS_AS_FL, 0);
        ByteUtils.floatToBytesLE(FLOATS[1], FLOATS_AS_FL, 4);
        ByteUtils.floatToBytesBE(FLOATS[0], FLOATS_AS_FL_BE, 0);
        ByteUtils.floatToBytesBE(FLOATS[1], FLOATS_AS_FL_BE, 4);
        ByteUtils.doubleToBytesLE(FLOATS[0], FLOATS_AS_FD, 0);
        ByteUtils.doubleToBytesLE(FLOATS[1], FLOATS_AS_FD, 8);
        ByteUtils.floatToBytesLE((float) DOUBLES[0], DOUBLES_AS_FL, 0);
        ByteUtils.floatToBytesLE((float) DOUBLES[1], DOUBLES_AS_FL, 4);
        ByteUtils.doubleToBytesLE(DOUBLES[0], DOUBLES_AS_FD, 0);
        ByteUtils.doubleToBytesLE(DOUBLES[1], DOUBLES_AS_FD, 8);
        ByteUtils.doubleToBytesBE(DOUBLES[0], DOUBLES_AS_FD_BE, 0);
        ByteUtils.doubleToBytesBE(DOUBLES[1], DOUBLES_AS_FD_BE, 8);
    }

    private static final Sequence SEQUENCE = new Sequence(null, null, 0, 0);
    private static final Fragments FRAGMENTS = new Fragments(null, Tag.PixelData, VR.OB, false, 0);

    @Test
    public void testCode() {
        assertEquals(0x4145, VR.AE.code());
        assertEquals(0x4153, VR.AS.code());
        assertEquals(0x4154, VR.AT.code());
        assertEquals(0x4353, VR.CS.code());
        assertEquals(0x4441, VR.DA.code());
        assertEquals(0x4453, VR.DS.code());
        assertEquals(0x4454, VR.DT.code());
        assertEquals(0x4644, VR.FD.code());
        assertEquals(0x464c, VR.FL.code());
        assertEquals(0x4953, VR.IS.code());
        assertEquals(0x4c4f, VR.LO.code());
        assertEquals(0x4c54, VR.LT.code());
        assertEquals(0x4f42, VR.OB.code());
        assertEquals(0x4f44, VR.OD.code());
        assertEquals(0x4f46, VR.OF.code());
        assertEquals(0x4f4c, VR.OL.code());
        assertEquals(0x4f57, VR.OW.code());
        assertEquals(0x504e, VR.PN.code());
        assertEquals(0x5348, VR.SH.code());
        assertEquals(0x534c, VR.SL.code());
        assertEquals(0x5351, VR.SQ.code());
        assertEquals(0x5353, VR.SS.code());
        assertEquals(0x5354, VR.ST.code());
        assertEquals(0x544d, VR.TM.code());
        assertEquals(0x5543, VR.UC.code());
        assertEquals(0x5549, VR.UI.code());
        assertEquals(0x554c, VR.UL.code());
        assertEquals(0x554e, VR.UN.code());
        assertEquals(0x5552, VR.UR.code());
        assertEquals(0x5553, VR.US.code());
        assertEquals(0x5554, VR.UT.code());
    }

    @Test
    public void testHeaderLength() {
        assertEquals(8, VR.AE.headerLength());
        assertEquals(8, VR.AS.headerLength());
        assertEquals(8, VR.AT.headerLength());
        assertEquals(8, VR.CS.headerLength());
        assertEquals(8, VR.DA.headerLength());
        assertEquals(8, VR.DS.headerLength());
        assertEquals(8, VR.DT.headerLength());
        assertEquals(8, VR.FD.headerLength());
        assertEquals(8, VR.FL.headerLength());
        assertEquals(8, VR.IS.headerLength());
        assertEquals(8, VR.LO.headerLength());
        assertEquals(8, VR.LT.headerLength());
        assertEquals(12, VR.OB.headerLength());
        assertEquals(12, VR.OF.headerLength());
        assertEquals(12, VR.OL.headerLength());
        assertEquals(12, VR.OW.headerLength());
        assertEquals(8, VR.PN.headerLength());
        assertEquals(8, VR.SH.headerLength());
        assertEquals(8, VR.SL.headerLength());
        assertEquals(12, VR.SQ.headerLength());
        assertEquals(8, VR.SS.headerLength());
        assertEquals(8, VR.ST.headerLength());
        assertEquals(8, VR.TM.headerLength());
        assertEquals(12, VR.UC.headerLength());
        assertEquals(8, VR.UI.headerLength());
        assertEquals(8, VR.UL.headerLength());
        assertEquals(12, VR.UN.headerLength());
        assertEquals(12, VR.UR.headerLength());
        assertEquals(8, VR.US.headerLength());
        assertEquals(12, VR.UT.headerLength());
    }

    @Test
    public void testValueOf() {
        assertEquals(VR.AE, VR.valueOf(0x4145));
        assertEquals(VR.AS, VR.valueOf(0x4153));
        assertEquals(VR.AT, VR.valueOf(0x4154));
        assertEquals(VR.CS, VR.valueOf(0x4353));
        assertEquals(VR.DA, VR.valueOf(0x4441));
        assertEquals(VR.DS, VR.valueOf(0x4453));
        assertEquals(VR.DT, VR.valueOf(0x4454));
        assertEquals(VR.FD, VR.valueOf(0x4644));
        assertEquals(VR.FL, VR.valueOf(0x464c));
        assertEquals(VR.IS, VR.valueOf(0x4953));
        assertEquals(VR.LO, VR.valueOf(0x4c4f));
        assertEquals(VR.LT, VR.valueOf(0x4c54));
        assertEquals(VR.OB, VR.valueOf(0x4f42));
        assertEquals(VR.OD, VR.valueOf(0x4f44));
        assertEquals(VR.OF, VR.valueOf(0x4f46));
        assertEquals(VR.OL, VR.valueOf(0x4f4c));
        assertEquals(VR.OW, VR.valueOf(0x4f57));
        assertEquals(VR.PN, VR.valueOf(0x504e));
        assertEquals(VR.SH, VR.valueOf(0x5348));
        assertEquals(VR.SL, VR.valueOf(0x534c));
        assertEquals(VR.SQ, VR.valueOf(0x5351));
        assertEquals(VR.SS, VR.valueOf(0x5353));
        assertEquals(VR.ST, VR.valueOf(0x5354));
        assertEquals(VR.TM, VR.valueOf(0x544d));
        assertEquals(VR.UC, VR.valueOf(0x5543));
        assertEquals(VR.UI, VR.valueOf(0x5549));
        assertEquals(VR.UL, VR.valueOf(0x554c));
        assertEquals(VR.UN, VR.valueOf(0x554e));
        assertEquals(VR.UR, VR.valueOf(0x5552));
        assertEquals(VR.US, VR.valueOf(0x5553));
        assertEquals(VR.UT, VR.valueOf(0x5554));
    }

    @Test
    public void testToBytes() {
        assertArrayEquals(DCM4CHEE_AS_AE, VR.AE.toBytes("DCM4CHEE", CS));
        assertArrayEquals(INTS_AS_IS, VR.IS.toBytes(INTS_AS_STRINGS, CS));
        assertArrayEquals(INTS_AS_IS, VR.IS.toBytes(INTS, CS));
        assertArrayEquals(FLOATS_AS_DS, VR.DS.toBytes(FLOATS_AS_STRINGS, CS));
        assertArrayEquals(FLOATS_AS_DS, VR.DS.toBytes(DOUBLES, CS));
    }

    @Test
    public void testToValue() {
        assertArrayEquals(TAGS_AS_AT, (byte[]) VR.AT.toValue(TAGS, false));
        assertArrayEquals(TAGS_AS_AT, (byte[]) VR.AT.toValue(TAGS_AS_STRINGS, false));
        assertArrayEquals(INTS_AS_OB, (byte[]) VR.OB.toValue(INTS, false));
        assertArrayEquals(INTS_AS_SS, (byte[]) VR.OW.toValue(INTS, false));
        assertArrayEquals(INTS_AS_SL, (byte[]) VR.OL.toValue(INTS, false));
        assertArrayEquals(INTS_AS_SL, (byte[]) VR.SL.toValue(INTS, false));
        assertArrayEquals(INTS_AS_SS, (byte[]) VR.SS.toValue(INTS, false));
        assertArrayEquals(INTS_AS_SL, (byte[]) VR.UL.toValue(INTS, false));
        assertArrayEquals(INTS_AS_SS, (byte[]) VR.US.toValue(INTS, false));
        assertArrayEquals(FLOATS_AS_STRINGS,
                (String[]) VR.DS.toValue(FLOATS, false));
        assertArrayEquals(FLOATS_AS_FL, (byte[]) VR.FL.toValue(FLOATS, false));
        assertArrayEquals(FLOATS_AS_FD, (byte[]) VR.FD.toValue(FLOATS, false));
        assertArrayEquals(FLOATS_AS_FL, (byte[]) VR.OF.toValue(FLOATS, false));
        assertArrayEquals(DOUBLES_AS_FL, (byte[]) VR.FL.toValue(DOUBLES, false));
        assertArrayEquals(DOUBLES_AS_FD, (byte[]) VR.FD.toValue(DOUBLES, false));
        assertArrayEquals(DOUBLES_AS_FL, (byte[]) VR.OF.toValue(DOUBLES, false));
        assertArrayEquals(DOUBLES_AS_FD, (byte[]) VR.OD.toValue(DOUBLES, false));
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testSequenceToBytes() {
        VR.SQ.toBytes(SEQUENCE, CS);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testFragmentsToBytes() {
        VR.OB.toBytes(FRAGMENTS, CS);
    }

    @Test
    public void testToStrings() {
        assertEquals("DCM4CHEE",
                VR.AE.toStrings(DCM4CHEE_AS_AE, false, CS));
        assertArrayEquals(INTS_AS_STRINGS,
                (String[]) VR.IS.toStrings(INTS_AS_IS, false, CS));
        assertArrayEquals(INTS_AS_STRINGS,
                (String[]) VR.IS.toStrings(INTS, false, CS));
        assertArrayEquals(TAGS_AS_STRINGS,
                (String[]) VR.AT.toStrings(TAGS_AS_AT, false, CS));
        assertArrayEquals(FLOATS_AS_STRINGS,
                (String[]) VR.DS.toStrings(FLOATS_AS_DS, false, CS));
        assertArrayEquals(FLOATS_AS_STRINGS,
                (String[]) VR.DS.toStrings(DOUBLES, false, CS));
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testSequenceToString() {
        VR.SQ.toStrings(SEQUENCE, false, CS);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testFragmentsToString() {
        VR.OB.toStrings(FRAGMENTS, false, CS);
    }

   @Test
    public void testToInts() {
       assertArrayEquals(INTS, VR.OB.toInts(INTS_AS_OB, false));
       assertArrayEquals(INTS, VR.OL.toInts(INTS_AS_SL, false));
       assertArrayEquals(INTS, VR.OW.toInts(INTS_AS_SS, false));
       assertArrayEquals(INTS, VR.SL.toInts(INTS_AS_SL, false));
       assertArrayEquals(INTS, VR.SS.toInts(INTS_AS_SS, false));
       assertArrayEquals(INTS, VR.UL.toInts(INTS_AS_SL, false));
       assertArrayEquals(UINTS, VR.US.toInts(INTS_AS_SS, false));
    }

   @Test(expected=UnsupportedOperationException.class)
   public void testFloatsToInts() {
       VR.FL.toInts(FLOATS, false);
   }

   @Test(expected=UnsupportedOperationException.class)
   public void testDoublesToInts() {
       VR.FD.toInts(DOUBLES, false);
   }

   @Test(expected=UnsupportedOperationException.class)
   public void testDSToInts() {
       VR.DS.toInts(FLOATS_AS_DS, false);
   }

   @Test(expected=UnsupportedOperationException.class)
   public void testDSToInts2() {
       VR.DS.toInts(FLOATS_AS_STRINGS, false);
   }

   @Test(expected=UnsupportedOperationException.class)
   public void testFLToInts() {
       VR.FL.toInts(FLOATS_AS_FL, false);
   }

   @Test(expected=UnsupportedOperationException.class)
   public void testOFToInts() {
       VR.OF.toInts(DOUBLES_AS_FL, false);
   }

   @Test(expected=UnsupportedOperationException.class)
   public void testSequenceToInts() {
       VR.SQ.toInts(SEQUENCE, false);
   }

   @Test(expected=UnsupportedOperationException.class)
   public void testFragmentsToInts() {
       VR.OB.toInts(FRAGMENTS, false);
   }

    @Test
    public void testToFloats() {
        assertArrayEquals(FLOATS, VR.DS.toFloats(DOUBLES, false), 0);
        assertArrayEquals(FLOATS, VR.FL.toFloats(FLOATS_AS_FL, false), 0);
        assertArrayEquals(FLOATS, VR.FD.toFloats(FLOATS_AS_FD, false), 0);
        assertArrayEquals(FLOATS, VR.OF.toFloats(FLOATS_AS_FL, false), 0);
    }

    @Test
    public void testToDoubles() {
        assertArrayEquals(DOUBLES_AS_FLOAT,
                VR.FL.toDoubles(DOUBLES_AS_FL, false), 0);
        assertArrayEquals(DOUBLES, VR.FD.toDoubles(DOUBLES_AS_FD, false), 0);
        assertArrayEquals(DOUBLES_AS_FLOAT,
                VR.OF.toDoubles(DOUBLES_AS_FL, false), 0);
    }

    @Test
    public void testToggleEndian() {
        assertArrayEquals(DCM4CHEE_AS_AE,
                VR.AE.toggleEndian(DCM4CHEE_AS_AE, true));
        assertArrayEquals(INTS_AS_OB, VR.OB.toggleEndian(INTS_AS_OB, true));
        assertArrayEquals(INTS_AS_SL_BE, VR.OL.toggleEndian(INTS_AS_SL, true));
        assertArrayEquals(INTS_AS_SS_BE, VR.SS.toggleEndian(INTS_AS_SS, true));
        assertArrayEquals(INTS_AS_SL_BE, VR.SL.toggleEndian(INTS_AS_SL, true));
        assertArrayEquals(TAGS_AS_AT_BE, VR.AT.toggleEndian(TAGS_AS_AT, true));
    }

}
