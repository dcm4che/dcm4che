package org.dcm4che3.io;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.util.LimitedInputStream;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class DicomInputStreamTest {

    @Test
    public void testPart10ExplicitVR() throws Exception {
        testPart10ExplicitVR(false);
    }

    @Test
    public void testPart10ExplicitVRWithLimit() throws Exception {
        testPart10ExplicitVR(true);
    }

    private void testPart10ExplicitVR(boolean readWithLimit) throws Exception {
        Attributes attrs = readFrom("DICOMDIR", IncludeBulkData.YES, readWithLimit);
        Sequence seq = attrs.getSequence(null, Tag.DirectoryRecordSequence);
        assertEquals(44, seq.size());
    }

    @Test
    public void testPart10Deflated() throws Exception {
        testPart10Deflated(false);
    }

    @Test
    public void testPart10DeflatedWithLimit() throws Exception {
        testPart10Deflated(true);
    }

    private void testPart10Deflated(boolean readWithLimit) throws Exception {
        Attributes attrs = readFrom("report_dfl", IncludeBulkData.YES, readWithLimit);
        Sequence seq = attrs.getSequence(null, Tag.ContentSequence);
        assertEquals(5, seq.size());
    }

    @Test
    public void testPart10BigEndian() throws Exception {
        Attributes attrs = readFromResource("US-RGB-8-epicard", IncludeBulkData.NO);
        assertEquals(3, attrs.getInt(Tag.SamplesPerPixel, 0));
    }

    @Test
    public void testImplicitVR() throws Exception {
        Attributes attrs = readFromResource("OT-PAL-8-face", IncludeBulkData.URI);
        assertEquals(1, attrs.getInt(Tag.SamplesPerPixel, 0));
        Object pixelData = attrs.getValue(Tag.PixelData);
        assertNotNull(pixelData);
        assertTrue(pixelData instanceof BulkData);
        Attributes item = attrs.getNestedDataset(Tag.ReferencedBulkDataSequence);
        assertNotNull(item);
        assertEquals(3, item.size());
        assertEquals(Tag.PixelData, item.getInt(Tag.SelectorAttribute, 0));
        assertEquals("OW", item.getString(Tag.SelectorAttributeVR));
        assertEquals(((BulkData) pixelData).uri, item.getString(Tag.RetrieveURL));
    }

    @Test
    public void testNoPreambleDataContainsDICMatByte128() throws Exception {
        Attributes attrs = readFromResource("no_preamble_dicm_in_data", IncludeBulkData.NO);
        assertEquals("DICMA1", attrs.getString(Tag.StationName));
    }

    private static Attributes readFromResource(String name, IncludeBulkData includeBulkData) throws Exception {
        return readFrom(name, includeBulkData, false);
    }

    private static Attributes readFrom(String name, IncludeBulkData includeBulkData, boolean readWithLimit) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL resource = cl.getResource(name);
        if (resource == null) {
            throw new FileNotFoundException("Could not resolve resource with name: '" + name + "'");
        }
        File file = new File(resource.toURI());
        try (DicomInputStream in = readWithLimit
                ? DicomInputStream.createWithLimitFromFileLength(file)
                : new DicomInputStream(file)) {
            in.setIncludeBulkData(includeBulkData);
            in.setAddBulkDataReferences(includeBulkData == IncludeBulkData.URI);
            return in.readDataset();
        }
    }

    @Test(expected = EOFException.class)
    public void testNoOutOfMemoryErrorOnInvalidLength() throws IOException {
        byte[] b = { 8, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 'e', 'v', 'i', 'l', 'l', 'e', 'n', 'g', 'h' };
        try ( DicomInputStream in = new DicomInputStream(new ByteArrayInputStream(b))) {
            in.readDataset();
        }
    }

    @Test
    public void testNoOutOfMemoryErrorOnInvalidLengthIfStreamLengthKnown() throws IOException {
        byte[] b = { 8, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 'e', 'v', 'i', 'l', 'l', 'e', 'n', 'g', 'h' };
        try ( DicomInputStream in = new DicomInputStream(new LimitedInputStream(new ByteArrayInputStream(b), b.length, true))) {
            in.readDataset();
            fail("Expected exception to be thrown because length of tag exceeds size of dicom stream");
        } catch (Exception exception) {
            assertEquals("Length 1735288172 for tag (7665,6C69) @ 12 exceeds remaining 1 (pos: 20)",
                    exception.getMessage());
        }
    }

    @Test
    public void testSRTag0040A170IsObservationClass() throws Exception {
        Attributes attrs =readFromResource("Tag-0040-A170-VR-CS.dcm", IncludeBulkData.NO);
        Attributes findings = attrs.getNestedDataset(Tag.FindingsSequenceTrial);
        assertNotNull(findings);
        Attributes contentItem1 = findings.getNestedDataset(Tag.ContentSequence);
        assertNotNull(contentItem1);
        assertEquals(VR.CS, contentItem1.getVR(Tag.PurposeOfReferenceCodeSequence));
        assertEquals("NAMED TYPE", contentItem1.getString(Tag.PurposeOfReferenceCodeSequence));
    }
}
