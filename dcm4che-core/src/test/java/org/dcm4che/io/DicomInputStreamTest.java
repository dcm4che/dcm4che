package org.dcm4che.io;

import org.dcm4che.data.Attributes;
import org.junit.Test;

public class DicomInputStreamTest {

    @Test
    public void testPart10ExplicitVR() throws Exception {
        Attributes attrs = 
                readFromResource("DICOMDIR");
    }

    @Test
    public void testPart10Deflated() throws Exception {
        Attributes attrs = 
                readFromResource("report_dfl");
    }

    @Test
    public void testPart10BigEndian() throws Exception {
        Attributes attrs = 
                readFromResource("US-RGB-8-epicard");
    }

    @Test
    public void testImplicitVR() throws Exception {
        Attributes attrs = 
                readFromResource("OT-PAL-8-face");
    }

    private static Attributes readFromResource(String name) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        DicomInputStream in = new DicomInputStream(
                cl.getResourceAsStream(name));
        try {
            return in.readAttributes(-1);
        } finally {
            in.close();
        }
    }

}
