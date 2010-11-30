package org.dcm4che.data;

import static org.junit.Assert.*;

import org.dcm4che.util.ByteUtils;
import org.junit.Test;

public class VRTest {

    private static final SpecificCharacterSet CS =
            SpecificCharacterSet.DEFAULT;

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

    private static final Sequence SEQUENCE = new Sequence(null, 0);
    private static final Fragments FRAGMENTS = new Fragments(VR.OB, false, 0);

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
        assertEquals(0x4f46, VR.OF.code());
        assertEquals(0x4f57, VR.OW.code());
        assertEquals(0x504e, VR.PN.code());
        assertEquals(0x5348, VR.SH.code());
        assertEquals(0x534c, VR.SL.code());
        assertEquals(0x5351, VR.SQ.code());
        assertEquals(0x5353, VR.SS.code());
        assertEquals(0x5354, VR.ST.code());
        assertEquals(0x544d, VR.TM.code());
        assertEquals(0x5549, VR.UI.code());
        assertEquals(0x554c, VR.UL.code());
        assertEquals(0x554e, VR.UN.code());
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
        assertEquals(12, VR.OW.headerLength());
        assertEquals(8, VR.PN.headerLength());
        assertEquals(8, VR.SH.headerLength());
        assertEquals(8, VR.SL.headerLength());
        assertEquals(12, VR.SQ.headerLength());
        assertEquals(8, VR.SS.headerLength());
        assertEquals(8, VR.ST.headerLength());
        assertEquals(8, VR.TM.headerLength());
        assertEquals(8, VR.UI.headerLength());
        assertEquals(8, VR.UL.headerLength());
        assertEquals(12, VR.UN.headerLength());
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
        assertEquals(VR.OF, VR.valueOf(0x4f46));
        assertEquals(VR.OW, VR.valueOf(0x4f57));
        assertEquals(VR.PN, VR.valueOf(0x504e));
        assertEquals(VR.SH, VR.valueOf(0x5348));
        assertEquals(VR.SL, VR.valueOf(0x534c));
        assertEquals(VR.SQ, VR.valueOf(0x5351));
        assertEquals(VR.SS, VR.valueOf(0x5353));
        assertEquals(VR.ST, VR.valueOf(0x5354));
        assertEquals(VR.TM, VR.valueOf(0x544d));
        assertEquals(VR.UI, VR.valueOf(0x5549));
        assertEquals(VR.UL, VR.valueOf(0x554c));
        assertEquals(VR.UN, VR.valueOf(0x554e));
        assertEquals(VR.US, VR.valueOf(0x5553));
        assertEquals(VR.UT, VR.valueOf(0x5554));
    }

    @Test
    public void testToBytes() {
        assertArrayEquals(DCM4CHEE_AS_AE,
                VR.AE.toBytes("DCM4CHEE", false, CS));
        assertArrayEquals(INTS_AS_IS,
                VR.IS.toBytes(INTS_AS_STRINGS, false, CS));
        assertArrayEquals(FLOATS_AS_DS, VR.DS.toBytes(FLOATS_AS_STRINGS, false, CS));
    }

    @Test
    public void testToValue() {
        assertArrayEquals(TAGS_AS_AT, (byte[]) VR.AT.toValue(TAGS, false));
        assertArrayEquals(INTS_AS_STRINGS,
                (String[]) VR.IS.toValue(INTS, false));
        assertArrayEquals(INTS_AS_OB, (byte[]) VR.OB.toValue(INTS, false));
        assertArrayEquals(INTS_AS_SS, (byte[]) VR.OW.toValue(INTS, false));
        assertArrayEquals(INTS_AS_SL, (byte[]) VR.SL.toValue(INTS, false));
        assertArrayEquals(INTS_AS_SS, (byte[]) VR.SS.toValue(INTS, false));
        assertArrayEquals(INTS_AS_SL, (byte[]) VR.UL.toValue(INTS, false));
        assertArrayEquals(INTS_AS_SS, (byte[]) VR.US.toValue(INTS, false));
        assertArrayEquals(FLOATS_AS_STRINGS,
                (String[]) VR.DS.toValue(FLOATS, false));
        assertArrayEquals(FLOATS_AS_FL, (byte[]) VR.FL.toValue(FLOATS, false));
        assertArrayEquals(FLOATS_AS_FD, (byte[]) VR.FD.toValue(FLOATS, false));
        assertArrayEquals(FLOATS_AS_FL, (byte[]) VR.OF.toValue(FLOATS, false));
        assertArrayEquals(FLOATS_AS_STRINGS,
                (String[]) VR.DS.toValue(DOUBLES, false));
        assertArrayEquals(DOUBLES_AS_FL, (byte[]) VR.FL.toValue(DOUBLES, false));
        assertArrayEquals(DOUBLES_AS_FD, (byte[]) VR.FD.toValue(DOUBLES, false));
        assertArrayEquals(DOUBLES_AS_FL, (byte[]) VR.OF.toValue(DOUBLES, false));
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testSequenceToBytes() {
        VR.SQ.toBytes(SEQUENCE, false, CS);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testFragmentsToBytes() {
        VR.OB.toBytes(FRAGMENTS, false, CS);
    }

    @Test
    public void testToStrings() {
        assertEquals("DCM4CHEE",
                VR.AE.toStrings(DCM4CHEE_AS_AE, false, CS));
        assertArrayEquals(INTS_AS_STRINGS,
                (String[]) VR.IS.toStrings(INTS_AS_IS, false, CS));
        assertArrayEquals(FLOATS_AS_STRINGS,
                (String[]) VR.DS.toStrings(FLOATS_AS_DS, false, CS));
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
       assertArrayEquals(INTS, VR.OW.toInts(INTS_AS_SS, false));
       assertArrayEquals(INTS, VR.SL.toInts(INTS_AS_SL, false));
       assertArrayEquals(INTS, VR.SS.toInts(INTS_AS_SS, false));
       assertArrayEquals(INTS, VR.UL.toInts(INTS_AS_SL, false));
       assertArrayEquals(UINTS, VR.US.toInts(INTS_AS_SS, false));
       assertArrayEquals(INTS, VR.IS.toInts(INTS_AS_STRINGS, false));
       assertArrayEquals(new int[] { INTS[2] },
               VR.IS.toInts(INTS_AS_STRINGS[2], false));
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
        assertArrayEquals(FLOATS, VR.FL.toFloats(FLOATS_AS_FL, false), 0);
        assertArrayEquals(FLOATS, VR.FD.toFloats(FLOATS_AS_FD, false), 0);
        assertArrayEquals(FLOATS, VR.OF.toFloats(FLOATS_AS_FL, false), 0);
        assertArrayEquals(FLOATS, VR.DS.toFloats(FLOATS_AS_STRINGS, false), 0);
        assertArrayEquals(new float[] { FLOATS[1] },
                VR.DS.toFloats(FLOATS_AS_STRINGS[1], false), 0);
    }

    @Test
    public void testToDoubles() {
        assertArrayEquals(DOUBLES_AS_FLOAT,
                VR.FL.toDoubles(DOUBLES_AS_FL, false), 0);
        assertArrayEquals(DOUBLES, VR.FD.toDoubles(DOUBLES_AS_FD, false), 0);
        assertArrayEquals(DOUBLES_AS_FLOAT,
                VR.OF.toDoubles(DOUBLES_AS_FL, false), 0);
        assertArrayEquals(DOUBLES,
                VR.DS.toDoubles(FLOATS_AS_STRINGS, false), 0);
        assertArrayEquals(new double[] { DOUBLES[1] },
                VR.DS.toDoubles(FLOATS_AS_STRINGS[1], false), 0);
    }

    @Test
    public void testToggleEndian() {
        assertArrayEquals(DCM4CHEE_AS_AE,
                VR.AE.toggleEndian(DCM4CHEE_AS_AE, true));
        assertArrayEquals(INTS_AS_OB, VR.OB.toggleEndian(INTS_AS_OB, true));
        assertArrayEquals(INTS_AS_SS_BE, VR.SS.toggleEndian(INTS_AS_SS, true));
        assertArrayEquals(INTS_AS_SL_BE, VR.SL.toggleEndian(INTS_AS_SL, true));
        assertArrayEquals(TAGS_AS_AT_BE, VR.AT.toggleEndian(TAGS_AS_AT, true));
    }

    @Test
    public void testCheckSupportBytes() {
        VR.AE.checkSupportBytes();
        VR.AS.checkSupportBytes();
        VR.AT.checkSupportBytes();
        VR.CS.checkSupportBytes();
        VR.DA.checkSupportBytes();
        VR.DS.checkSupportBytes();
        VR.DT.checkSupportBytes();
        VR.FD.checkSupportBytes();
        VR.FL.checkSupportBytes();
        VR.IS.checkSupportBytes();
        VR.LO.checkSupportBytes();
        VR.LT.checkSupportBytes();
        VR.OB.checkSupportBytes();
        VR.OF.checkSupportBytes();
        VR.OW.checkSupportBytes();
        VR.PN.checkSupportBytes();
        VR.SH.checkSupportBytes();
        VR.SL.checkSupportBytes();
        VR.SS.checkSupportBytes();
        VR.ST.checkSupportBytes();
        VR.TM.checkSupportBytes();
        VR.UI.checkSupportBytes();
        VR.UL.checkSupportBytes();
        VR.UN.checkSupportBytes();
        VR.US.checkSupportBytes();
        VR.UT.checkSupportBytes();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testSQCheckSupportBytes() {
        VR.SQ.checkSupportBytes();
    }

    @Test
    public void testCheckSupportString() {
        VR.AE.checkSupportString();
        VR.AS.checkSupportString();
        VR.AT.checkSupportString();
        VR.CS.checkSupportString();
        VR.DA.checkSupportString();
        VR.DS.checkSupportString();
        VR.DT.checkSupportString();
        VR.FD.checkSupportString();
        VR.FL.checkSupportString();
        VR.IS.checkSupportString();
        VR.LO.checkSupportString();
        VR.LT.checkSupportString();
        VR.OB.checkSupportString();
        VR.OF.checkSupportString();
        VR.OW.checkSupportString();
        VR.PN.checkSupportString();
        VR.SH.checkSupportString();
        VR.SL.checkSupportString();
        VR.SS.checkSupportString();
        VR.ST.checkSupportString();
        VR.TM.checkSupportString();
        VR.UI.checkSupportString();
        VR.UL.checkSupportString();
        VR.UN.checkSupportString();
        VR.US.checkSupportString();
        VR.UT.checkSupportString();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testSQCheckSupportString() {
        VR.SQ.checkSupportString();
    }

    @Test
    public void testCheckSupportStrings() {
        VR.AE.checkSupportStrings();
        VR.AS.checkSupportStrings();
        VR.AT.checkSupportStrings();
        VR.CS.checkSupportStrings();
        VR.DA.checkSupportStrings();
        VR.DS.checkSupportStrings();
        VR.DT.checkSupportStrings();
        VR.FD.checkSupportStrings();
        VR.FL.checkSupportStrings();
        VR.IS.checkSupportStrings();
        VR.LO.checkSupportStrings();
        VR.OB.checkSupportStrings();
        VR.OF.checkSupportStrings();
        VR.OW.checkSupportStrings();
        VR.PN.checkSupportStrings();
        VR.SH.checkSupportStrings();
        VR.SL.checkSupportStrings();
        VR.SS.checkSupportStrings();
        VR.TM.checkSupportStrings();
        VR.UI.checkSupportStrings();
        VR.UL.checkSupportStrings();
        VR.UN.checkSupportStrings();
        VR.US.checkSupportStrings();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testSQCheckSupportStrings() {
        VR.SQ.checkSupportStrings();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testLTCheckSupportStrings() {
        VR.LT.checkSupportStrings();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testSTCheckSupportStrings() {
        VR.ST.checkSupportStrings();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testUTCheckSupportStrings() {
        VR.UT.checkSupportStrings();
    }

    @Test
    public void testCheckSupportInts() {
        VR.AT.checkSupportInts();
        VR.IS.checkSupportInts();
        VR.OB.checkSupportInts();
        VR.OW.checkSupportInts();
        VR.SL.checkSupportInts();
        VR.SS.checkSupportInts();
        VR.UL.checkSupportInts();
        VR.US.checkSupportInts();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testSQCheckSupportInts() {
        VR.SQ.checkSupportInts();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testAECheckSupportInts() {
        VR.AE.checkSupportInts();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testFDCheckSupportInts() {
        VR.FD.checkSupportInts();
    }

    @Test
    public void testCheckSupportFloats() {
        VR.DS.checkSupportFloats();
        VR.FD.checkSupportFloats();
        VR.FL.checkSupportFloats();
        VR.OF.checkSupportFloats();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testISCheckSupportFloats() {
        VR.IS.checkSupportFloats();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testOWCheckSupportFloats() {
        VR.OW.checkSupportFloats();
    }

}
