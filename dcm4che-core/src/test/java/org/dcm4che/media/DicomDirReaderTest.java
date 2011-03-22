package org.dcm4che.media;

import static org.junit.Assert.*;

import java.io.File;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.junit.Test;

public class DicomDirReaderTest {

    private static File toFile(String name) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return new File(cl.getResource(name).toURI());
    }

    @Test
    public void testInitDicomDirReader() throws Exception {
        DicomDirReader r = new DicomDirReader(toFile("DICOMDIR"));
        try {
            Attributes fsInfo = r.getFilesetInformation();
            assertNull(fsInfo.getString(Tag.FileSetID, null, 0, null));
            assertEquals(374, fsInfo.getInt(
                    Tag.OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity,
                    null, 0, 0));
            assertEquals(8686, fsInfo.getInt(
                    Tag.OffsetOfTheLastDirectoryRecordOfTheRootDirectoryEntity,
                    null, 0, 0));
            assertEquals(0,
                    fsInfo.getInt(Tag.FileSetConsistencyFlag, null, 0, 0));
        } finally {
            r.close();
        }
    }
}
