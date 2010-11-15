package org.dcm4che.io;

import static org.junit.Assert.*;

import org.junit.Test;
import org.dcm4che.data.Attributes;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;

public class DicomInputStreamTest {

    @Test
    public void testPart10ExplicitVR() throws Exception {
        Attributes attrs = readFromResource("DICOMDIR");
        Sequence seq = attrs.getSequence(Tag.DirectoryRecordSequence, null);
        assertEquals(44, seq.size());
   }

    @Test
    public void testPart10Deflated() throws Exception {
        Attributes attrs = readFromResource("report_dfl");
        Sequence seq = attrs.getSequence(Tag.ContentSequence, null);
        assertEquals(5, seq.size());
    }

    @Test
    public void testPart10BigEndian() throws Exception {
        Attributes attrs = 
                readFromResource("US-RGB-8-epicard");
        assertEquals(3, attrs.getInt(Tag.SamplesPerPixel, null, 0, 0));
    }

    @Test
    public void testImplicitVR() throws Exception {
        Attributes attrs = readFromResource("OT-PAL-8-face");
        assertEquals(1, attrs.getInt(Tag.SamplesPerPixel, null, 0, 0));
    }

    private static Attributes readFromResource(String name) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        DicomInputStream in = new DicomInputStream(cl.getResourceAsStream(name));
        try {
            return in.readAttributes(-1);
        } finally {
            in.close();
        }
    }

}
