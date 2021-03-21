package org.dcm4che3.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.util.Collections;
import java.util.List;

import org.dcm4che3.data.*;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class DicomInputStreamTest {

    @Test
    public void testPart10ExplicitVR() throws Exception {
        Attributes attrs = readFrom("DICOMDIR", IncludeBulkData.YES);
        Sequence seq = attrs.getSequence(null, Tag.DirectoryRecordSequence);
        assertEquals(44, seq.size());
   }

    @Test
    public void testPart10Deflated() throws Exception {
        Attributes attrs = readFrom("report_dfl", IncludeBulkData.YES);
        Sequence seq = attrs.getSequence(null, Tag.ContentSequence);
        assertEquals(5, seq.size());
    }

    @Test
    public void testPart10BigEndian() throws Exception {
        Attributes attrs = readFrom("US-RGB-8-epicard", IncludeBulkData.NO);
        assertEquals(3, attrs.getInt(Tag.SamplesPerPixel, 0));
    }

    @Test
    public void testImplicitVR() throws Exception {
        Attributes attrs = readFrom("OT-PAL-8-face", IncludeBulkData.URI);
        assertEquals(1, attrs.getInt(Tag.SamplesPerPixel, 0));
    }

    @Test
    public void testBulkDataLimit() throws Exception {
        try {
            readFrom("3gb-bulk-data-truncated", IncludeBulkData.URI);
        } catch (DicomStreamException e) {
            assertEquals("0x7FE00010 " + DicomInputStream.VALUE_TOO_LARGE, e.getMessage());
        }
    }

    @Test
    public void testSpoolDataFragments() throws Exception {
        List<File> bulkDataFiles;
        Object pixeldata;
        try (DicomInputStream in = new DicomInputStream(new File("target/test-data/US-PAL-8-10x-echo"))) {
            in.setIncludeBulkData(IncludeBulkData.URI);
            in.setURI(null); // force spooling
            in.setConcatenateBulkDataFiles(false);
            pixeldata = in.readDataset().getValue(Tag.PixelData);
            bulkDataFiles = in.getBulkDataFiles();
        }
        try {
            assertTrue(pixeldata instanceof Fragments);
            Fragments fragments = (Fragments) pixeldata;
            assertEquals(11, fragments.size());
            assertEquals(14, bulkDataFiles.size());
            for (int i = 0; i < 10; i++) {
                Object fragment = fragments.get(++i);
                assertTrue(fragment instanceof BulkData);
                assertEquals(bulkDataFiles.get(i + 3), ((BulkData)fragment).getFile());
            }
        } finally {
            for (File bulkDataFile : bulkDataFiles) {
                bulkDataFile.delete();
            }
        }
    }

    private static Attributes readFrom(String name, IncludeBulkData includeBulkData) throws Exception {
        try ( DicomInputStream in = new DicomInputStream(new File("target/test-data/" + name))) {
            in.setIncludeBulkData(includeBulkData);
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
    public void correctVR() throws IOException {
        byte[] b = { 0x08, 0, 0x68, 0, 'U', 'K', 16, 0,
                'F', 'O', 'R', ' ', 'P', 'R', 'E', 'S', 'E', 'N', 'T', 'A', 'T', 'I', 'O', 'N' };
        Attributes attrs;
        try (DicomInputStream in = new DicomInputStream(new ByteArrayInputStream(b), UID.ExplicitVRLittleEndian)) {
            attrs = in.readDataset();
        }
        assertEquals("FOR PRESENTATION", attrs.getString(Tag.PresentationIntentType));
    }
}
