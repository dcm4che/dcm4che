package org.dcm4che3.image;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class VOILUTTest {

    private static final float M = 32.1216f;
    private static final float B = -4047f;
    private static final float C = 40f;
    private static final float W = 400f;

    @Test
    public void testLinearCTExample() {
        assertEquals(20, VOILUT.linear(modality(122), C, W, 0, 255));
        assertEquals(144, VOILUT.linear(modality(128), C, W, 0, 255));
        assertEquals(226, VOILUT.linear(modality(132), C, W, 0, 255));
    }

    @Test
    public void testLinearExactCTExample() {
        assertEquals(20, VOILUT.linearExact(modality(122), C, W, 0, 255));
        assertEquals(143, VOILUT.linearExact(modality(128), C, W, 0, 255));
        assertEquals(225, VOILUT.linearExact(modality(132), C, W, 0, 255));
    }

    @Test
    public void testWindowLookupTableLinear() {
        StoredValue sv = new StoredValue.Unsigned(8);
        LookupTableFactory factory = new LookupTableFactory(sv);
        factory.setModalityLUT(attrs(M, B));
        factory.setWindowCenter(C);
        factory.setWindowWidth(W);
        factory.setVOI(attrs(M, B), 0, 0, true);

        LookupTable lut = factory.createLUT(8);
        assertEquals(20, lookup(lut, sv, 122));
        assertEquals(144, lookup(lut, sv, 128));
        assertEquals(226, lookup(lut, sv, 132));
    }

    @Test
    public void testWindowLookupTableLinearExact() {
        StoredValue sv = new StoredValue.Unsigned(8);
        LookupTableFactory factory = new LookupTableFactory(sv);
        factory.setModalityLUT(attrs(M, B));
        factory.setWindowCenter(C);
        factory.setWindowWidth(W);
        org.dcm4che3.data.Attributes img = attrs(M, B);
        img.setString(org.dcm4che3.data.Tag.VOILUTFunction,
                org.dcm4che3.data.VR.CS, "LINEAR_EXACT");
        factory.setVOI(img, 0, 0, true);

        LookupTable lut = factory.createLUT(8);
        assertEquals(20, lookup(lut, sv, 122));
        assertEquals(143, lookup(lut, sv, 128));
        assertEquals(225, lookup(lut, sv, 132));
    }

    @Test
    public void testOutOfRangeUsesFullCalculation() {
        byte[] lut = new byte[1];
        lut[0] = 99;
        VOIWindowLookupTable table = new VOIWindowLookupTable(
                new StoredValue.Unsigned(8), 8, 100, lut,
                null, M, B, VOILUTFunction.LINEAR, C, W);
        // stored value 122 is outside lut range [100, 100]
        assertEquals(20, lookupRaw(table, (byte) 122));
    }

    private static double modality(int stored) {
        return VOILUT.modalityValue(stored, M, B);
    }

    private static int lookup(LookupTable lut, StoredValue sv, int stored) {
        byte[] src = { (byte) stored };
        byte[] dest = new byte[1];
        lut.lookup(src, 0, dest, 0, 1);
        return dest[0] & 0xff;
    }

    private static int lookupRaw(LookupTable lut, byte raw) {
        byte[] dest = new byte[1];
        lut.lookup(new byte[] { raw }, 0, dest, 0, 1);
        return dest[0] & 0xff;
    }

    private static org.dcm4che3.data.Attributes attrs(float slope, float intercept) {
        org.dcm4che3.data.Attributes attrs = new org.dcm4che3.data.Attributes();
        attrs.setFloat(org.dcm4che3.data.Tag.RescaleSlope,
                org.dcm4che3.data.VR.DS, slope);
        attrs.setFloat(org.dcm4che3.data.Tag.RescaleIntercept,
                org.dcm4che3.data.VR.DS, intercept);
        attrs.setInt(org.dcm4che3.data.Tag.PixelRepresentation,
                org.dcm4che3.data.VR.US, 0);
        attrs.setInt(org.dcm4che3.data.Tag.BitsStored,
                org.dcm4che3.data.VR.US, 8);
        attrs.setFloat(org.dcm4che3.data.Tag.WindowCenter,
                org.dcm4che3.data.VR.DS, C);
        attrs.setFloat(org.dcm4che3.data.Tag.WindowWidth,
                org.dcm4che3.data.VR.DS, W);
        return attrs;
    }
}
