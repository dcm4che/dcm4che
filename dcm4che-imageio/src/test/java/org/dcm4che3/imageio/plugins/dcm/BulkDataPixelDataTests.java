//
///////////////////////////////////////////////////////////////
//                C O P Y R I G H T  (c) 2020                //
//        Agfa HealthCare N.V. and/or its affiliates         //
//                    All Rights Reserved                    //
///////////////////////////////////////////////////////////////
//                                                           //
//       THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF      //
//        Agfa HealthCare N.V. and/or its affiliates.        //
//      The copyright notice above does not evidence any     //
//     actual or intended publication of such source code.   //
//                                                           //
///////////////////////////////////////////////////////////////
//


package org.dcm4che3.imageio.plugins.dcm;

import org.dcm4che3.data.*;
import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.imageio.metadata.BulkDataDicomImageAccessor;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit tests to ensure that we properly handle pixel data in the BulkDataDicomImageAccessor.
 */
public class BulkDataPixelDataTests {

    DatasetWithFMI datasetWithFMI;
    @Before
    public void setup() {
        datasetWithFMI = new DatasetWithFMI(new Attributes(), new Attributes());
    }

    @Test
    public void containsPixelData_NoPixelDataTag_ReturnsFalse() {
        BulkDataDicomImageAccessor imageAccessor = new BulkDataDicomImageAccessor(null, datasetWithFMI);
        assertFalse(imageAccessor.containsPixelData());
    }

    @Test
    public void containsPixelData_EmptyFragments_ReturnsFalse() {
        datasetWithFMI.getDataset().setValue(Tag.PixelData, VR.OB, new Fragments(VR.OB, false, 0));
        BulkDataDicomImageAccessor imageAccessor = new BulkDataDicomImageAccessor(null, datasetWithFMI);
        assertFalse(imageAccessor.containsPixelData());
    }

    @Test
    public void containsPixelData_EmptyBytes_ReturnsFalse() {
        datasetWithFMI.getDataset().setValue(Tag.PixelData, VR.OB, new byte[0]);
        BulkDataDicomImageAccessor imageAccessor = new BulkDataDicomImageAccessor(null, datasetWithFMI);
        assertFalse(imageAccessor.containsPixelData());
    }

    @Test
    public void containsPixelData_FragmentsBulkData_ReturnsTrue() {
        Fragments fragments = new Fragments(VR.OB, false, 1);
        fragments.add(new BulkData("file:///c/file.dcm", 100, 10, false ));
        datasetWithFMI.getDataset().setValue(Tag.PixelData, VR.OB, fragments);
        BulkDataDicomImageAccessor imageAccessor = new BulkDataDicomImageAccessor(null, datasetWithFMI);
        assertTrue(imageAccessor.containsPixelData());
    }


    @Test
    public void containsPixelData_FragmentsBytes_ReturnsTrue() {
        Fragments fragments = new Fragments(VR.OB, true, 1);
        fragments.add(new byte[1]);
        datasetWithFMI.getDataset().setValue(Tag.PixelData, VR.OB, fragments);
        BulkDataDicomImageAccessor imageAccessor = new BulkDataDicomImageAccessor(null, datasetWithFMI);
        assertTrue(imageAccessor.containsPixelData());
    }

    @Test
    public void countFrames_UncompressedPadded_MustIgnorePadding() throws IOException {
        int rows = 101;
        int cols = 51;
        int samples = 1;
        int allocated = 8;

        PhotometricInterpretation pmi = PhotometricInterpretation.MONOCHROME2;
        int frameLength = pmi.frameLength(cols, rows, samples, allocated);
        assertEquals("odd frame length", 1, frameLength % 2);

        datasetWithFMI.getFileMetaInformation().setString(Tag.TransferSyntaxUID, VR.CS, UID.ExplicitVRLittleEndian);
        datasetWithFMI.getDataset().setInt(Tag.Rows, VR.IS, rows);
        datasetWithFMI.getDataset().setInt(Tag.Columns, VR.IS, cols);
        datasetWithFMI.getDataset().setInt(Tag.BitsAllocated, VR.IS, allocated);
        datasetWithFMI.getDataset().setInt(Tag.SamplesPerPixel, VR.IS, samples);
        datasetWithFMI.getDataset().setString(Tag.PhotometricInterpretation, VR.CS, pmi.toString());

        datasetWithFMI.getDataset().setValue(Tag.PixelData, VR.OB, new byte[15454]);// padded by one byte
        BulkDataDicomImageAccessor imageAccessor = new BulkDataDicomImageAccessor(null, datasetWithFMI);
        assertTrue(imageAccessor.containsPixelData());
        assertEquals(3, imageAccessor.countFrames());
    }


    /**
     * The number of frames cannot be calculated for a study that does not contain all the required attributes
     */
    @Test
    public void countFrames_UncompressedMissingAttributes_ReturnsMinus1() throws IOException {
        int samples = 1;
        int allocated = 8;

        PhotometricInterpretation pmi = PhotometricInterpretation.MONOCHROME2;
        datasetWithFMI.getFileMetaInformation().setString(Tag.TransferSyntaxUID, VR.CS, UID.ExplicitVRLittleEndian);
        datasetWithFMI.getDataset().setInt(Tag.BitsAllocated, VR.IS, allocated);
        datasetWithFMI.getDataset().setInt(Tag.SamplesPerPixel, VR.IS, samples);
        datasetWithFMI.getDataset().setString(Tag.PhotometricInterpretation, VR.CS, pmi.toString());

        datasetWithFMI.getDataset().setValue(Tag.PixelData, VR.OB, new byte[15454]);// padded by one byte
        BulkDataDicomImageAccessor imageAccessor = new BulkDataDicomImageAccessor(null, datasetWithFMI);
        assertTrue(imageAccessor.containsPixelData());
        assertEquals("Frame length is ambiguous without rows / cols", -1, imageAccessor.countFrames());
    }
}
