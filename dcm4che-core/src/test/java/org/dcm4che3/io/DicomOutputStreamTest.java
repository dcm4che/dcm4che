package org.dcm4che3.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.*;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.UIDUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class DicomOutputStreamTest {

    private File file;

    @Before
    public void setUp() throws IOException {
        file = File.createTempFile("test", ".dcm");
    }

    @After
    public void cleanUp() throws IOException {
        file.delete();
    }

   @Test
    public void testWriteCommand() throws IOException {
       DicomOutputStream out = new DicomOutputStream(
               new FileOutputStream(file), UID.ImplicitVRLittleEndian);
       try {
           out.writeCommand(cechorq());
       } finally {
           out.close();
       }
       assertEquals(4, readAttributes().size());
    }

    private Attributes readAttributes() throws IOException {
        DicomInputStream in = new DicomInputStream(file);
        try {
            return in.readDataset();
        } finally {
            in.close();
        }
    }

    private Attributes deserializeAttributes() throws Exception {
        ObjectInputStream in =
                new ObjectInputStream(new FileInputStream(file));
        try {
            return (Attributes) in.readObject();
        } finally {
            in.close();
        }
    }


    private Attributes cechorq() {
        Attributes cechorq = new Attributes();
        cechorq.setString(Tag.AffectedSOPClassUID, VR.UI,
                UID.Verification);
        cechorq.setInt(Tag.CommandField, VR.US, 0x0030);
        cechorq.setInt(Tag.MessageID, VR.US, 1);
        cechorq.setInt(Tag.CommandDataSetType, VR.US, 0x0101);
        return cechorq;
    }

    @Test
    public void testWriteDatasetWithoutFileMetaInformation()
            throws IOException {
        DicomOutputStream out = new DicomOutputStream(file);
        testWriteDataset(out, null);
    }

    @Test
    public void testWriteDataset() throws IOException {
        DicomOutputStream out = new DicomOutputStream(file);
    }

    @Test
    public void testWriteDatasetWithGroupLength() throws IOException {
        DicomOutputStream out = new DicomOutputStream(file);
        out.setEncodingOptions(
                new DicomEncodingOptions(true, true, false, true, false));
        testWriteDataset(out, UID.ExplicitVRLittleEndian);
    }

    @Test
    public void testWriteDatasetWithoutUndefLength() throws IOException {
        DicomOutputStream out = new DicomOutputStream(file);
        out.setEncodingOptions(
                new DicomEncodingOptions(false, false, false, false, false));
        testWriteDataset(out, UID.ExplicitVRLittleEndian);
    }

    @Test
    public void testWriteDatasetWithUndefEmptyLength() throws IOException {
        DicomOutputStream out = new DicomOutputStream(file);
        out.setEncodingOptions(
                new DicomEncodingOptions(false, true, true, true, true));
        testWriteDataset(out, UID.ExplicitVRLittleEndian);
    }

    @Test
    public void testWriteDatasetBigEndian() throws IOException {
        DicomOutputStream out = new DicomOutputStream(file);
        testWriteDataset(out, UID.ExplicitVRBigEndian);
    }

    @Test
    public void testWriteDatasetDeflated() throws IOException {
        DicomOutputStream out = new DicomOutputStream(file);
        testWriteDataset(out, UID.DeflatedExplicitVRLittleEndian);
    }

    @Test
    public void testSerializeDataset() throws Exception {
        ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(file));
        try {
            out.writeObject(dataset());
            out.writeUTF("END");
        } finally {
            out.close();
        }
        deserializeAttributes();
    }

    private void testWriteDataset(DicomOutputStream out, String tsuid)
            throws IOException {
        Attributes ds = dataset();
        Attributes fmi = tsuid != null
                ? ds.createFileMetaInformation(tsuid)
                : null;
        try {
            out.writeDataset(fmi, ds);
        } finally {
            out.close();
        }
        readAttributes();
    }

    private Attributes dataset() {
        Attributes ds = new Attributes();
        ds.setString(Tag.PatientName, VR.PN, "Simpson^Homer");
        ds.setNull(Tag.ReferencedPatientSequence, VR.SQ);
        ds.newSequence(Tag.ReferencedVisitSequence, 0);
        ds.newSequence(Tag.ReferencedStudySequence, 1)
                .add(new Attributes());
        ds.setNull(Tag.AccessionNumber, VR.SH);
        ds.newSequence(Tag.RequestAttributesSequence, 1)
                .add(requestAttributes());
        ds.setString(Tag.SOPClassUID, VR.UI, "1.2.3.4");
        ds.setString(Tag.SOPInstanceUID, VR.UI, "4.3.2.1");
        BulkData bdl = new BulkData(
                uri("OT-PAL-8-face"), 1654, 307200, false);
        ds.setValue(Tag.PixelData, VR.OW, bdl);
        Fragments frags = ds.newFragments("DicomOutputStreamTest", 0x99990010, VR.OB, 3);
        frags.add(null);
        frags.add(new byte[] { 1, 2, 3, 4 });
        frags.add(bdl);
        return ds;
    }

    private static String uri(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.getResource(name).toString();
    }

    private Attributes requestAttributes() {
        Attributes item = new Attributes();
        item.setString(Tag.RequestedProcedureID, VR.SH, "P1234");
        item.setString(Tag.ScheduledProcedureStepID, VR.SH, "S1234");
        item.setString(Tag.AccessionNumber, VR.SH, "A1234");
        item.newSequence(Tag.IssuerOfAccessionNumberSequence, 1)
                .add(hl7v2HierarchicDesignator());
        return item;
    }

    private Attributes hl7v2HierarchicDesignator() {
        Attributes item= new Attributes();
        item.setString(Tag.LocalNamespaceEntityID, VR.UT, "E1234");
        item.setString(Tag.UniversalEntityID, VR.UT,
                "ef9d7472-3364-4480-b362-fc2d2a47a0c5");
        item.setString(Tag.UniversalEntityIDType, VR.CS, "UUID");
        return item;
    }

    @Test(expected = IllegalStateException.class)
    public void testWriteFMIDeflated() throws IOException {
        try (DicomOutputStream out = new DicomOutputStream(
                new ByteArrayOutputStream(), UID.DeflatedExplicitVRLittleEndian)) {
            out.writeFileMetaInformation(
                    Attributes.createFileMetaInformation(UIDUtils.createUID(),
                            UID.CTImageStorage, UID.DeflatedExplicitVRLittleEndian));
        }
    }


    @Test
    public void testWriteDeflatedEvenLength() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DicomOutputStream dos = new DicomOutputStream(
                out, UID.DeflatedExplicitVRLittleEndian)) {
            Attributes attrs = new Attributes();
            attrs.setString(Tag.SOPClassUID, VR.UI, UID.CTImageStorage);
            dos.writeDataset(null, attrs);
        }
        assertEquals("odd number of bytes", 0, out.size() & 1);
    }
}
