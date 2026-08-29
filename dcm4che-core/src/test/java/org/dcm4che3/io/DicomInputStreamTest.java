package org.dcm4che3.io;

import java.io.*;
import java.util.List;

import org.dcm4che3.data.*;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

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
    public void testPart10SkipNotAllAtOnce() throws Exception {
        Attributes result;
        try (DicomInputStream in = new DicomInputStream(
                new BufferedInputStream(new FileInputStream("target/test-data/DICOMDIR")){
            @Override
            public synchronized long skip(long n) throws IOException {
                return super.skip(Math.min(n, 128L));
            }
        })) {
            in.setIncludeBulkData(IncludeBulkData.YES);
            result = in.readDataset();
        }
        Attributes attrs = result;
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
        Attributes attrs = readFrom("US-RGB-8-epicard", IncludeBulkData.NO);
        assertEquals(3, attrs.getInt(Tag.SamplesPerPixel, 0));
    }

    @Test
    public void testImplicitVR() throws Exception {
        Attributes attrs = readFrom("OT-PAL-8-face", IncludeBulkData.URI);
        assertEquals(1, attrs.getInt(Tag.SamplesPerPixel, 0));
    }

    @Test
    public void testBulkDataLarger2GiB() throws Exception {
            Attributes attrs = readFrom("3gb-bulk-data-truncated", IncludeBulkData.URI);
            Object blkdata = attrs.getValue(Tag.PixelData);
            assertTrue(blkdata instanceof BulkData);
            assertEquals(3221225472L, ((BulkData) blkdata).longLength());
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

    @Test
    public void testNoPreambleDataContainsDICMatByte128() throws Exception {
        Attributes attrs = readFrom("no_preamble_dicm_in_data", IncludeBulkData.NO);
        assertEquals("DICMA1", attrs.getString(Tag.StationName));
    }

    private static Attributes readFrom(String name, IncludeBulkData includeBulkData) throws Exception {
        return readFrom(name, includeBulkData, false);
    }

    private static Attributes readFrom(String name, IncludeBulkData includeBulkData, boolean readWithLimit) throws Exception {
        try ( DicomInputStream in =
                readWithLimit ?
                        DicomInputStream.createWithLimitFromFileLength(new File("target/test-data/" + name)) :
                        new DicomInputStream(new File("target/test-data/" + name))) {
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
    public void testNoOutOfMemoryErrorOnInvalidLengthIfStreamLengthKnown() throws IOException {
        byte[] b = { 8, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 'e', 'v', 'i', 'l', 'l', 'e', 'n', 'g', 'h' };
        EOFException exception = assertThrows(
                EOFException.class,
                () -> {
                    try ( DicomInputStream in = DicomInputStream.createWithLimit(new ByteArrayInputStream(b), b.length)) {
                        in.readDataset();
                    }
                }
        );

        assertEquals("Length 1735288172 for tag (7665,6C69) @ 12 exceeds remaining 1 (pos: 20)",
                exception.getMessage());
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

    @Test
    public void testCorrectEmptySequenceVRDefinedLength() throws IOException {
        byte[] b = {  0x08, 0, 0x11, 0x30,
                      'U', 'N', 0x00, 0x00, // UN
                      0x00, 0x00, 0x00, 0x00 };
        Attributes attrs;
        try (DicomInputStream in = new DicomInputStream(new ByteArrayInputStream(b), UID.ExplicitVRLittleEndian)) {
            attrs = in.readDataset();
        }
        assertTrue(attrs.contains(Tag.SourceIrradiationEventSequence));
        assertEquals(VR.SQ, attrs.getVR(Tag.SourceIrradiationEventSequence));
        assertEquals(0, attrs.getSequence(Tag.SourceIrradiationEventSequence).size());
    }

    @Test
    public void testCorrectEmptySequenceVrUndefinedLength() throws IOException {
        byte[] b = {  0x08, 0, 0x11, 0x30,
                'U', 'N', 0, 0, // UN
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, // Undefined Length
                (byte) 0xFE, (byte) 0xFF, (byte) 0xDD, (byte) 0xE0, //Sequence Delimitation Item
                0, 0, 0, 0 //Item Length
                 };
        Attributes attrs;
        try (DicomInputStream in = new DicomInputStream(new ByteArrayInputStream(b), UID.ExplicitVRLittleEndian)) {
            attrs = in.readDataset();
        }
        assertTrue(attrs.contains(Tag.SourceIrradiationEventSequence));
        assertEquals(VR.SQ, attrs.getVR(Tag.SourceIrradiationEventSequence));
        assertEquals(0, attrs.getSequence(Tag.SourceIrradiationEventSequence).size());
    }

    @Test
    // 2024c part 5 6.2.2 note 5
    public void testCorrectableSequenceVrDefinedLength() throws IOException {
        byte[] b = {  0x08, 0, 0x11, 0x30,
                'U', 'N', 0, 0, // it should be a SQ
                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Length
                (byte) 0xFE, (byte) 0xFF,           0, (byte) 0xE0, // Item Tag (FFFE, E000)
                (byte) 0x34, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 52 Length
                0x08, 0, 0x10, 0x30,
                0x2c, 0, 0 , 0 ,// length 44
                '2', '.', '2', '5', '.', '5', '3', '8', '2', '0',
                '0', '6', '2', '6', '9', '4', '6', '7', '5', '8',
                '2', '7', '4', '2', '8', '4', '1', '2', '9', '6',
                '7', '9', '1', '9', '9', '4', '0', '8', '4', '7',
                '7', '2', '5',   0,
        };

        Attributes attrs;
        try (DicomInputStream in = new DicomInputStream(new ByteArrayInputStream(b), UID.ExplicitVRLittleEndian)) {
            attrs = in.readDataset();
        }
        assertTrue(attrs.contains(Tag.SourceIrradiationEventSequence));
        assertEquals(VR.SQ, attrs.getVR(Tag.SourceIrradiationEventSequence));
        assertEquals(1, attrs.getSequence(Tag.SourceIrradiationEventSequence).size());
        assertEquals(44, attrs.getSequence(Tag.SourceIrradiationEventSequence).get(0).getBytes(Tag.IrradiationEventUID).length );
    }

    @Test
    // 2024c part 5 6.2.2 note 5
    public void testCorrectableUiVr() throws IOException {
        byte[] b = {  0x08, 0, 0x10, 0x30,
                'U', 'N', 0, 0, // it should be a SQ
                (byte) 0x2C, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Length
                '2', '.', '2', '5', '.', '5', '3', '8', '2', '0',
                '0', '6', '2', '6', '9', '4', '6', '7', '5', '8',
                '2', '7', '4', '2', '8', '4', '1', '2', '9', '6',
                '7', '9', '1', '9', '9', '4', '0', '8', '4', '7',
                '7', '2', '5',   0,
        };

        Attributes attrs;
        try (DicomInputStream in = new DicomInputStream(new ByteArrayInputStream(b), UID.ExplicitVRLittleEndian)) {
            attrs = in.readDataset();
        }
        assertTrue(attrs.contains(Tag.IrradiationEventUID));
        assertEquals(44, attrs.getBytes(Tag.IrradiationEventUID).length);
    }

    @Test
    // 2024c part 5 6.2.2 note 5
    public void testCorrectableSequenceVrUndefinedLength() throws IOException {
        byte[] b = {  0x08, 0, 0x11, 0x30,
                      'U', 'N', 0, 0, // it should be a SQ
                      (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, // Undefined Length
                      (byte) 0xFE, (byte) 0xFF,           0, (byte) 0xE0, // Item Tag (FFFE, E000)
                      (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, // Undefined Length
                      0x08, 0, 0x10, 0x30,
                      0x2c, 0, 0 , 0 ,// length 44
                      '2', '.', '2', '5', '.', '5', '3', '8', '2', '0',
                      '0', '6', '2', '6', '9', '4', '6', '7', '5', '8',
                      '2', '7', '4', '2', '8', '4', '1', '2', '9', '6',
                      '7', '9', '1', '9', '9', '4', '0', '8', '4', '7',
                      '7', '2', '5',   0,
                      (byte) 0xFE, (byte) 0xFF,       0x0D, (byte) 0xE0, //Item Delimitation Item
                      0, 0, 0, 0, //Item Length
                      (byte) 0xFE, (byte) 0xFF, (byte) 0xDD, (byte) 0xE0, //Sequence Delimitation Item
                      0, 0, 0, 0 //Item Length
                   };

        Attributes attrs;
        try (DicomInputStream in = new DicomInputStream(new ByteArrayInputStream(b), UID.ExplicitVRLittleEndian)) {
            attrs = in.readDataset();
        }
        assertTrue(attrs.contains(Tag.SourceIrradiationEventSequence));
        assertEquals(VR.SQ, attrs.getVR(Tag.SourceIrradiationEventSequence));
        assertEquals(1, attrs.getSequence(Tag.SourceIrradiationEventSequence).size());
        assertEquals(44, attrs.getSequence(Tag.SourceIrradiationEventSequence).get(0).getBytes(Tag.IrradiationEventUID).length);
    }

    @Test
    public void testUncorrectableUNSequenceVR() throws IOException {
        byte[] b = {  0x08,  0, 0x11, 0x30,
                      'U', 'N', 0, 0,
                      0x48,  0, 0, 0,
                      0x10, 0x48, (byte) 0xDD, 0x1D,
                      0x22, 0x02,        0x11, 0x48,
                      (byte) 0xE6, (byte) 0xB7, (byte) 0xCD, (byte) 0xCD, (byte) 0xED, (byte) 0xFD, (byte) 0xEE, (byte) 0xB7,
                      (byte) 0xEC, (byte) 0xD3, (byte) 0xDC, (byte) 0x99, (byte) 0xE5, (byte) 0xC9, (byte) 0xDE, (byte) 0x99,
                      (byte) 0xEC, (byte) 0xCC, (byte) 0xDD, (byte) 0x81, (byte) 0xE5, (byte) 0xCC, (byte) 0xC0, (byte) 0x85,
                      (byte) 0xE4, (byte) 0xCF, (byte) 0xDE, (byte) 0x82, (byte) 0xE9, (byte) 0xCB, (byte) 0xD8, (byte) 0x80,
                      (byte) 0xEF, (byte) 0xD3, (byte) 0xDF, (byte) 0x82, (byte) 0xE9, (byte) 0xC9, (byte) 0xDF, (byte) 0x81,
                      (byte) 0xE8, (byte) 0xC5, (byte) 0xDD, (byte) 0x83, (byte) 0xF3, (byte) 0xC9, (byte) 0xDB, (byte) 0x8E,
                      (byte) 0xEB, (byte) 0xD3, (byte) 0xDC, (byte) 0x80, (byte) 0xE8, (byte) 0xCE, (byte) 0xDD, (byte) 0x81,
                             0x10,        0x48, (byte) 0xD0,        0x1D, (byte) 0xDD, (byte) 0xFD, (byte) 0xEE, (byte) 0xB7};

        Attributes attrs;
        try (DicomInputStream in = new DicomInputStream(new ByteArrayInputStream(b), UID.ExplicitVRLittleEndian)) {
            attrs = in.readDataset();
        }
        assertTrue(attrs.contains(Tag.SourceIrradiationEventSequence));
        assertEquals(VR.UN, attrs.getVR(Tag.SourceIrradiationEventSequence));
        assertEquals(72, attrs.getBytes(Tag.SourceIrradiationEventSequence).length);
    }

    @Test
    public void testSRTag0040A170IsObservationClass() throws Exception {
        Attributes attrs = readFrom("Tag-0040-A170-VR-CS.dcm", IncludeBulkData.NO);
        Attributes findings = attrs.getNestedDataset(Tag.FindingsSequenceTrial);
        assertNotNull(findings);
        Attributes contentItem1 = findings.getNestedDataset(Tag.ContentSequence);
        assertNotNull(contentItem1);
        assertEquals(VR.CS, contentItem1.getVR(Tag.PurposeOfReferenceCodeSequence));
        assertEquals("NAMED TYPE", contentItem1.getString(Tag.PurposeOfReferenceCodeSequence));
    }

    @Test
    public void testUNSequenceIVR_LE() throws Exception {
        byte[] data = {
                0x23, 0x01, 0x10, 0x00, 4, 0, 0, 0, 'T', 'E', 'S', 'T',
                0x23, 0x01, 0x56, 0x10, 18, 0, 0, 0,
                -2, -1, 0, -32, 10, 0, 0, 0,
                0x08, 0x00, 0x01, 0x03, 2, 0, 0, 0, 0x23, 0x01};
        testUNSequence(data, UID.ImplicitVRLittleEndian);
    }

    @Test
    public void testUNSequenceEVR_LE_UN_IVR_LE() throws Exception {
        byte[] data = {
                0x23, 0x01, 0x10, 0x00, 'L', 'O', 4, 0, 'T', 'E', 'S', 'T',
                0x23, 0x01, 0x56, 0x10, 'U', 'N', 0, 0, 18, 0, 0, 0,
                -2, -1, 0, -32, 10, 0, 0, 0,
                0x08, 0x00, 0x01, 0x03, 2, 0, 0, 0, 0x23, 0x01};
        testUNSequence(data, UID.ExplicitVRLittleEndian);
    }

    @Test
    public void testUNSequenceEVR_LE_UN_EVR_LE() throws Exception {
        byte[] data = {
                0x23, 0x01, 0x10, 0x00, 'L', 'O', 4, 0, 'T', 'E', 'S', 'T',
                0x23, 0x01, 0x56, 0x10, 'U', 'N', 0, 0, 18, 0, 0, 0,
                -2, -1, 0, -32, 10, 0, 0, 0,
                0x08, 0x00, 0x01, 0x03, 'U', 'S', 2, 0, 0x23, 0x01};
        testUNSequence(data, UID.ExplicitVRLittleEndian);
    }

    @Test
    public void testUNSequenceEVR_BE_UN_IVR_LE() throws Exception {
        byte[] data = {
                0x01, 0x23, 0x00, 0x10, 'L', 'O', 0, 4, 'T', 'E', 'S', 'T',
                0x01, 0x23, 0x10, 0x56, 'U', 'N', 0, 0, 0, 0, 0, 18,
                -2, -1, 0, -32, 10, 0, 0, 0,
                0x08, 0x00, 0x01, 0x03, 2, 0, 0, 0, 0x23, 0x01};
        testUNSequence(data, UID.ExplicitVRBigEndian);
    }
    @Test
    public void testUNSequenceEVR_BE_UN_EVR_BE() throws Exception {
        byte[] data = {
                0x01, 0x23, 0x00, 0x10, 'L', 'O', 0, 4, 'T', 'E', 'S', 'T',
                0x01, 0x23, 0x10, 0x56, 'U', 'N', 0, 0, 0, 0, 0, 18,
                -1, -2, -32, 0, 0, 0, 0, 10,
                0x00, 0x08, 0x03, 0x01, 'U', 'S', 0, 2, 0x01, 0x23};
        testUNSequence(data, UID.ExplicitVRBigEndian);
    }

    private void testUNSequence(byte[] value, String tsuid) throws IOException {
        assertEquals(0x0123,
                new DicomInputStream(new ByteArrayInputStream(value), tsuid)
                        .readDataset()
                        .getNestedDataset("TEST", 0x01230056)
                        .getInt(Tag.PrivateGroupReference, 0));
    }

}
