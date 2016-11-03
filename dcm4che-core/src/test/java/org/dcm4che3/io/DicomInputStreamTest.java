package org.dcm4che3.io;

import static org.junit.Assert.*;

import java.io.File;

import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class DicomInputStreamTest {

    @Test
    public void testPart10ExplicitVR() throws Exception {
        Attributes attrs = readFromResource("DICOMDIR", IncludeBulkData.YES);
        Sequence seq = attrs.getSequence(null, Tag.DirectoryRecordSequence);
        assertEquals(44, seq.size());
   }

    @Test
    public void testPart10Deflated() throws Exception {
        Attributes attrs = readFromResource("report_dfl", IncludeBulkData.YES);
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
    public void testReadDatasetExcludesStopTagByDefault() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        DicomInputStream in = new DicomInputStream(
                new File(cl.getResource("OT-PAL-8-face").toURI()));
        try {
            in.setAddBulkDataReferences(true);
            in.setIncludeBulkData(IncludeBulkData.URI);
            Attributes attrs = in.readDataset(-1, Tag.BitsAllocated);
            assertFalse(attrs.contains(Tag.BitsAllocated));
            assertEquals(Tag.BitsAllocated, in.tag());
        } finally {
            in.close();
        }
    }

    @Test
    public void testReadDatasetIncludeStopTag() throws Exception {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        DicomInputStream in = new DicomInputStream(
                new File(cl.getResource("OT-PAL-8-face").toURI()));
        try {
            in.setAddBulkDataReferences(true);
            in.setIncludeBulkData(IncludeBulkData.URI);
            Attributes attrs = in.readDataset(-1, Tag.BitsAllocated, true);
            assertTrue(attrs.contains(Tag.BitsAllocated));
            assertEquals(Tag.BitsStored, in.tag());
        } finally {
            in.close();
        }
    }

    private static Attributes readFromResource(String name,
            IncludeBulkData includeBulkData)
            throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        DicomInputStream in = new DicomInputStream(
                new File(cl.getResource(name).toURI()));
        try {
            in.setIncludeBulkData(includeBulkData);
            in.setAddBulkDataReferences(includeBulkData == IncludeBulkData.URI);
            return in.readDataset(-1, -1);
        } finally {
            in.close();
        }
    }

}
