package org.dcm4che3.media;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.media.DicomDirReader;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class DicomDirReaderTest {

    private static File toFile(String name) throws Exception {
        return new File("target/test-data/" + name);
    }

    @Test
    public void testInitDicomDirReader() throws Exception {
        DicomDirReader r = new DicomDirReader(toFile("DICOMDIR"));
        try {
            assertEquals("1.3.6.1.4.1.5962.1.5.1175775772.5737.0",
                    r.getFileSetUID());
            assertNull(r.getFileSetID());
            assertEquals(0, r.getFileSetConsistencyFlag());
            assertFalse(r.isEmpty());
        } finally {
            r.close();
        }
    }

    @Test
    public void testReadAll() throws Exception {
        DicomDirReader r = new DicomDirReader(toFile("DICOMDIR"));
        try {
            assertEquals(44, readNext(r, r.readFirstRootDirectoryRecord()));
        } finally {
            r.close();
        }
    }

    private int readNext(DicomDirReader r, Attributes rec) throws IOException {
        int count = 0;
        while (rec != null) {
            count += 1 + readNext(r, r.readLowerDirectoryRecord(rec));
            rec = r.readNextDirectoryRecord(rec);
        }
        return count;
    }
}
