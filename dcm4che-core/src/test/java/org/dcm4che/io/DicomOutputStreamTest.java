package org.dcm4che.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.BulkDataLocator;
import org.dcm4che.data.EncodeOptions;
import org.dcm4che.data.Fragments;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DicomOutputStreamTest {

    private File file;
    private EncodeOptions encOpts;

    @Before
    public void setUp() throws IOException {
        file = File.createTempFile("test", ".dcm");
        encOpts = new EncodeOptions();
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
            return in.readDataset(-1, -1);
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
        cechorq.setString(Tag.AffectedSOPClassUID, null, VR.UI,
                UID.VerificationSOPClass);
        cechorq.setInt(Tag.CommandField, null, VR.US, 0x0030);
        cechorq.setInt(Tag.MessageID, null, VR.US, 1);
        cechorq.setInt(Tag.CommandDataSetType, null, VR.US, 0x0101);
        return cechorq;
    }

    @Test
    public void testWriteDatasetWithoutFileMetaInformation()
            throws IOException {
        testWriteDataset(null);
    }

    @Test
    public void testWriteDataset() throws IOException {
        testWriteDataset(UID.ExplicitVRLittleEndian);
    }

    @Test
    public void testWriteDatasetWithGroupLength() throws IOException {
        encOpts.setGroupLength(true);
        testWriteDataset(UID.ExplicitVRLittleEndian);
    }

    @Test
    public void testWriteDatasetWithoutUndefLength() throws IOException {
        encOpts.setUndefEmptyItemLength(false);
        encOpts.setUndefEmptySequenceLength(false);
        testWriteDataset(UID.ExplicitVRLittleEndian);
    }

    @Test
    public void testWriteDatasetWithUndefEmptyLength() throws IOException {
        encOpts.setUndefEmptyItemLength(true);
        encOpts.setUndefEmptySequenceLength(true);
        testWriteDataset(UID.ExplicitVRLittleEndian);
    }

    @Test
    public void testWriteDatasetBigEndian() throws IOException {
        testWriteDataset(UID.ExplicitVRBigEndian);
    }

    @Test
    public void testWriteDatasetDeflated() throws IOException {
        testWriteDataset(UID.DeflatedExplicitVRLittleEndian);
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

    private void testWriteDataset(String tsuid) throws IOException {
        Attributes ds = dataset();
        Attributes fmi = tsuid != null
                ? ds.createFileMetaInformation(tsuid)
                : null;
        DicomOutputStream out = new DicomOutputStream(file);
        out.setEncodeOptions(encOpts);
        try {
            out.writeDataset(fmi, ds);
        } finally {
            out.close();
        }
        readAttributes();
    }

    private Attributes dataset() {
        Attributes ds = new Attributes();
        ds.setString(Tag.PatientName, null, VR.PN, "Simpson^Homer");
        ds.setNull(Tag.ReferencedPatientSequence, null, VR.SQ);
        ds.newSequence(Tag.ReferencedVisitSequence, null, 0);
        ds.newSequence(Tag.ReferencedStudySequence, null, 1)
                .add(new Attributes());
        ds.setNull(Tag.AccessionNumber, null, VR.SH);
        ds.newSequence(Tag.RequestAttributesSequence, null, 1)
                .add(requestAttributes());
        ds.setString(Tag.SOPClassUID, null, VR.UI, "1.2.3.4");
        ds.setString(Tag.SOPInstanceUID, null, VR.UI, "4.3.2.1");
        BulkDataLocator bdl = new BulkDataLocator(uri("OT-PAL-8-face"), 
                        UID.ImplicitVRLittleEndian, 1654, 307200);
        ds.setValue(Tag.PixelData, null, VR.OW, bdl);
        Fragments frags = ds.newFragments(0x99990010, "DicomOutputStreamTest", VR.OB, 3);
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
        item.setString(Tag.RequestedProcedureID, null, VR.SH, "P1234");
        item.setString(Tag.ScheduledProcedureStepID, null, VR.SH, "S1234");
        item.setString(Tag.AccessionNumber, null, VR.SH, "A1234");
        item.newSequence(Tag.IssuerOfAccessionNumberSequence, null, 1)
                .add(hl7v2HierarchicDesignator());
        return item;
    }

    private Attributes hl7v2HierarchicDesignator() {
        Attributes item= new Attributes();
        item.setString(Tag.LocalNamespaceEntityID, null, VR.UT, "E1234");
        item.setString(Tag.UniversalEntityID, null, VR.UT,
                "ef9d7472-3364-4480-b362-fc2d2a47a0c5");
        item.setString(Tag.UniversalEntityIDType, null, VR.CS, "UUID");
        return item;
    }

}
