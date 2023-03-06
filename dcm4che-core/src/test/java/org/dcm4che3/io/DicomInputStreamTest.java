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
    public void testPixelDataLarger2GiB() throws Exception {

        // Two temporary files - one to hold 2.1+Gib of PixelData and one to
        // hold the encoded DICOM file containing that PixelData. The goal of
        // this test is to ensure that DicomInputStream doesn't throw an
        // exception when it receives PixelData in excess of Integer.MAX_VALUE.
        final File pixelDataFile = File.createTempFile("dcm3che-", "-3gib-pixel-data");
        final File dicomFile = File.createTempFile("dcm3che-", "-3gib-dicom");

        try {

            // This will hold the total number of pixel data written.
            long totalPixelDataBytes = 0;

            // Fill a buffer with random data. We're going to verify that
            // the data we read back in matches this buffer.
            final byte[] buffer = new byte[1024 * 1024 * 18];
            for (int i = 0; i < buffer.length; ++i)
                buffer[i] = (byte) Math.round(Math.random() * 128);

            // This test writes in excess of 2.5GiB of data for good measure.
            try (final FileOutputStream outputStream = new FileOutputStream(pixelDataFile)) {
                while (totalPixelDataBytes < 2508221998L) {
                    outputStream.write(buffer);
                    totalPixelDataBytes += buffer.length;
                }
            }

            final String patientId = "ABC123";

            // Need a DICOM file with a regular attribute to verify that we
            // can read the resulting file back.
            final Attributes attributes = new Attributes();
            attributes.setString(Tag.PatientID, VR.LO, patientId);
            attributes.setValue(Tag.PixelData, VR.OB, new BulkData(pixelDataFile.toURI().toString(), 0, totalPixelDataBytes, false));

            final Attributes fmi = new Attributes();
            fmi.setString(Tag.FileMetaInformationGroupLength, VR.UL, "1");
            fmi.setString(Tag.MediaStorageSOPClassUID, VR.UI, UID.ComputedRadiographyImageStorage);
            fmi.setString(Tag.TransferSyntaxUID, VR.UI, UID.ExplicitVRLittleEndian);

            try (final DicomOutputStream outputStream = new DicomOutputStream(dicomFile)) {
                outputStream.writeDataset(fmi, attributes);
            }

            // Now read the resulting file back in. Verify that both the regular
            // tags and the giant PixelData is valid.
            try (final DicomInputStream inputStream = new DicomInputStream(dicomFile)) {
                final Attributes _attributes = inputStream.readDataset();
                assert(_attributes.getString(Tag.PatientID).equals(patientId));

                final Object _pixelData = _attributes.getValue(Tag.PixelData);
                assert(_pixelData instanceof BulkData);

                final BulkData pixelData = (BulkData) _pixelData;
                assert(pixelData.longLength() == totalPixelDataBytes);

                // We're not going to compare the whole thing - just the first
                // segment should match byte-for-byte. We assume the rest of
                // the data will be correct.
                try (final InputStream inputStream2 = pixelData.openStream()) {
                    final byte[] buffer2 = new byte[buffer.length];
                    inputStream2.read(buffer2);

                    for (int i = 0; i < buffer.length; ++i) {
                        assert(buffer[i] == buffer2[i]);
                    }
                }

            }

        } finally {
            pixelDataFile.delete();
            dicomFile.delete();
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
