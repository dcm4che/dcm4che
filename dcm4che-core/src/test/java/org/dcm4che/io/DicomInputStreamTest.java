package org.dcm4che.io;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.junit.Test;

public class DicomInputStreamTest {

    @Test
    public void testPart10ExplicitVR() throws Exception {
        Attributes attrs = readFromResource("DICOMDIR", false, false);
        Sequence seq = (Sequence) attrs.getValue(Tag.DirectoryRecordSequence, null);
        assertEquals(44, seq.size());
   }

    @Test
    public void testPart10Deflated() throws Exception {
        Attributes attrs = readFromResource("report_dfl", false, false);
        Sequence seq = (Sequence) attrs.getValue(Tag.ContentSequence, null);
        assertEquals(5, seq.size());
    }

    @Test
    public void testPart10BigEndian() throws Exception {
        Attributes attrs = readFromResource("US-RGB-8-epicard", true, false);
        assertEquals(3, attrs.getInt(Tag.SamplesPerPixel, null, 0, 0));
    }

    @Test
    public void testImplicitVR() throws Exception {
        Attributes attrs = readFromResource("OT-PAL-8-face", false, true);
        assertEquals(1, attrs.getInt(Tag.SamplesPerPixel, null, 0, 0));
    }

    private static Attributes readFromResource(String name, 
            boolean excludeBulkData, boolean includeBulkDataLocator)
            throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        DicomInputStream in = new DicomInputStream(
                new File(cl.getResource(name).toURI()));
        try {
            in.setExcludeBulkData(excludeBulkData);
            in.setIncludeBulkDataLocator(includeBulkDataLocator);
            return in.readDataset(-1, -1);
        } finally {
            in.close();
        }
    }

}
