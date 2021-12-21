package org.dcm4che3.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.dcm4che3.io.BasicBulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link Attributes} including {@link BulkData} values.
 *
 * @author Hermann Czedik-Eysenberg (hermann.czedik-eysenberg@agfa.com)
 */
public class AttributesBulkDataTest {

    private byte[] BYTES = { Byte.MIN_VALUE, 0, Byte.MAX_VALUE, 0 };
    private String[] STRINGS = { "VALUE1", "VALUE2" };
    private String[] AGES = { "018M", "018Y" };
    private String[] DATES = { "19560708", "20010203" };
    private String[] TIMES = { "1956", "2001" };
    private String[] UIDS = { UID.CTImageStorage, UID.MRImageStorage };
    private int[] TAGS = {
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
            Tag.SelectorUIValue,
            Tag.SelectorOVValue,
            Tag.SelectorSVValue,
            Tag.SelectorUVValue
    };
    private int[] INTS = { Short.MIN_VALUE,  Short.MAX_VALUE };
    private long[] LONGS = { Long.MIN_VALUE,  Short.MAX_VALUE };
    private int[] UINTS = { 0xffff,  Short.MAX_VALUE };
    private float[] FLOATS = { -Float.MIN_VALUE,  0.1234f, Float.MAX_VALUE };
    private double[] DOUBLES = { -Double.MIN_VALUE,  0.1234, Double.MAX_VALUE };
    private String[] DOUBLES_AS_STRINGS = { "-4.9E-324", "0.1234", "1.7976931348E308" };
    private String URI = "http://host/path";

    @Test
    public void testBulkdataLittleEndian() throws IOException {
        Attributes a = createTestAttributes();
        DicomInputStream in = asDicomInputStream(a, UID.ExplicitVRLittleEndian);
        try {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            in.setBulkDataDescriptor(new BasicBulkDataDescriptor().excludeDefaults().addTag(TAGS));
            in.setConcatenateBulkDataFiles(true);
            Attributes b = in.readDataset();
            assertValues(b);
        } finally {
            for (File f : in.getBulkDataFiles()) {
                f.delete();
            }
        }
    }

    @Test
    public void testBulkdataBigEndian() throws IOException {
        Attributes a = createTestAttributes();
        DicomInputStream in = asDicomInputStream(a, UID.ExplicitVRBigEndian);
        try {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            in.setBulkDataDescriptor(new BasicBulkDataDescriptor().excludeDefaults().addTag(TAGS));
            in.setConcatenateBulkDataFiles(true);
            Attributes b = in.readDataset();
            assertValues(b);
        } finally {
            for (File f : in.getBulkDataFiles()) {
                f.delete();
            }
        }
    }

    @Test
    public void testBulkdataBigEndianToLittleEndian() throws IOException {
        Attributes a = createTestAttributes();
        DicomInputStream in = asDicomInputStream(a, UID.ExplicitVRBigEndian);
        try {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            in.setBulkDataDescriptor(new BasicBulkDataDescriptor().excludeDefaults().addTag(TAGS));
            in.setConcatenateBulkDataFiles(true);
            Attributes attributesBigEndian = in.readDataset();

            // store bigEndian BulkData references in a littleEndian Attributes
            Attributes attributesLittleEndian = new Attributes();
            attributesLittleEndian.addAll(attributesBigEndian);

            assertValues(attributesLittleEndian);
        } finally {
            for (File f : in.getBulkDataFiles()) {
                f.delete();
            }
        }
    }

    @Test
    public void testBulkdataLittleEndianToBigEndian() throws IOException {
        Attributes a = createTestAttributes();
        DicomInputStream in = asDicomInputStream(a, UID.ExplicitVRLittleEndian);
        try {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            in.setBulkDataDescriptor(new BasicBulkDataDescriptor().excludeDefaults().addTag(TAGS));
            in.setConcatenateBulkDataFiles(true);
            Attributes attributesLittleEndian = in.readDataset();

            // store littleEndian BulkData references in a bigEndian Attributes
            Attributes attributesBigEndian = new Attributes(true);
            attributesBigEndian.addAll(attributesLittleEndian);

            assertValues(attributesBigEndian);
        } finally {
            for (File f : in.getBulkDataFiles()) {
                f.delete();
            }
        }
    }

    private void assertValues(Attributes b) throws IOException {
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
        assertArrayEquals(DOUBLES_AS_STRINGS, b.getStrings(Tag.SelectorFDValue));
        assertEquals(DOUBLES[0], b.getDouble(Tag.SelectorFDValue, 0), 0);
        assertArrayEquals(INTS, b.getInts(Tag.SelectorOLValue));
        assertEquals(FLOATS[0], b.getFloat(Tag.SelectorFLValue, 0), 0);
        assertArrayEquals(UINTS, b.getInts(Tag.SelectorULValue));
        assertEquals(UINTS[0], b.getInt(Tag.SelectorUSValue, 0));
        assertArrayEquals(INTS, b.getInts(Tag.SelectorSLValue));
        assertEquals(INTS[0], b.getInt(Tag.SelectorSSValue, 0));
        assertArrayEquals(UIDS, b.getStrings(Tag.SelectorUIValue));
        assertArrayEquals(LONGS, b.getLongs(Tag.SelectorOVValue));
        assertEquals(LONGS[0], b.getLong(Tag.SelectorSVValue, 0));
        assertEquals(LONGS[0], b.getLong(Tag.SelectorUVValue, 0));
    }

    private Attributes createTestAttributes() {
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
        a.setLong(Tag.SelectorOVValue, VR.OV, LONGS);
        a.setLong(Tag.SelectorSVValue, VR.SV, LONGS);
        a.setLong(Tag.SelectorUVValue, VR.UV, LONGS);
        return a;
    }

    private static DicomInputStream asDicomInputStream(Attributes a, String transferSyntaxUID) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DicomOutputStream out = new DicomOutputStream(baos, transferSyntaxUID)) {
            out.writeDataset(null, a);
        }
        return new DicomInputStream(new ByteArrayInputStream(baos.toByteArray()), transferSyntaxUID);
    }
}
