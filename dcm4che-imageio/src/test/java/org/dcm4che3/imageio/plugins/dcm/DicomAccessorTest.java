package org.dcm4che3.imageio.plugins.dcm;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.stream.ImageInputStream;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteOrder;

import static org.junit.Assert.*;

public class DicomAccessorTest {
    private Attributes attributes;
    private Attributes fmi;
    private VR pixelDataVR;

    private DicomAccessor dicomAccessor = new BasicDicomAccessor();

    @Before
    public void setup() {
        this.attributes = new Attributes();
        this.fmi = new Attributes();
        this.pixelDataVR = null;
    }

    @Test
    public void getTransferSyntax_Default() {
        String expectedTS = UID.ExplicitVRLittleEndian;
        this.fmi.setString(Tag.TransferSyntaxUID, VR.UI, expectedTS);
        assertEquals(expectedTS, dicomAccessor.getTransferSyntax());
    }


    @Test
    public void getTransferSyntaxType_Default() {
        String expectedTS = UID.ExplicitVRLittleEndian;
        this.fmi.setString(Tag.TransferSyntaxUID, VR.UI, expectedTS);
        assertEquals(TransferSyntaxType.forUID(expectedTS), dicomAccessor.getTransferSyntaxType());
    }

    @Test
    public void getSOPInstanceUID_Default() {
        String expected = "1.2.3.4.5";
        this.attributes.setString(Tag.SOPInstanceUID, VR.UI, expected);
        assertEquals(expected, dicomAccessor.getSOPInstanceUID());
    }

    @Test
    public void getSOPClassUID_Default() {
        String expected = UID.BasicTextSRStorage;
        this.attributes.setString(Tag.SOPClassUID, VR.UI, expected);
        assertEquals(expected, dicomAccessor.getSOPClassUID());
    }

    @Test
    public void getColumns_Default() {
        int expected = 512;
        this.attributes.setInt(Tag.Columns, VR.US, expected);
        assertEquals(expected, dicomAccessor.getColumns());
    }

    @Test
    public void getRows_Default() {
        int expected = 256;
        this.attributes.setInt(Tag.Rows, VR.US, expected);
        assertEquals(expected, dicomAccessor.getRows());
    }

    @Test
    public void getSamplesPerPixel_Default() {
        int expected = 8;
        this.attributes.setInt(Tag.SamplesPerPixel, VR.US, expected);
        assertEquals(expected, dicomAccessor.getSamplesPerPixel());
    }

    @Test
    public void getBitsAllocated_Default() {
        int expected = 8;
        this.attributes.setInt(Tag.BitsAllocated, VR.US, expected);
        assertEquals(expected, dicomAccessor.getBitsAllocated());
    }

    @Test
    public void getBitsStored_Default() {
        int expected = 8;
        this.attributes.setInt(Tag.BitsAllocated, VR.US, expected);
        assertEquals(expected, dicomAccessor.getBitsStored());
    }

    @Test
    public void isMonochrome_MONOCHROME1() {
        this.attributes.setInt(Tag.SamplesPerPixel, VR.US, 1);
        this.attributes.setString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME1");
        assertTrue(this.dicomAccessor.isMonochrome());
    }

    @Test
    public void isMonochrome_PALETTE_COLOR() {
        this.attributes.setInt(Tag.SamplesPerPixel, VR.US, 1);
        this.attributes.setString(Tag.PhotometricInterpretation, VR.CS, "PALETTE COLOR");
        assertFalse(this.dicomAccessor.isMonochrome());
    }

    @Test
    public void isMonochrome_RGB() {
        this.attributes.setInt(Tag.SamplesPerPixel, VR.US, 3);
        this.attributes.setString(Tag.PhotometricInterpretation, VR.CS, "RGB");
        assertFalse(this.dicomAccessor.isMonochrome());
    }

    @Test
    public void getPhotometricInterpretation_MONOCHROME() {
        // Must handle the invalid "MONOCHROME" case
        this.attributes.setString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME");
        PhotometricInterpretation pmi = dicomAccessor.getPhotometricInterpretation();
        assertEquals(PhotometricInterpretation.MONOCHROME2, pmi);
    }

    @Test
    public void getPhotometricInterpretation_YBR_FULL_422() {
        // Must handle the invalid "MONOCHROME" case
        this.attributes.setString(Tag.PhotometricInterpretation, VR.CS, "YBR_FULL_422");
        PhotometricInterpretation pmi = dicomAccessor.getPhotometricInterpretation();
        assertEquals(PhotometricInterpretation.YBR_FULL_422, pmi);
    }

    @Test
    public void getNumberOfFrames_Default() {
        int expected = 32;
        this.attributes.setInt(Tag.NumberOfFrames, VR.IS, expected);
        assertEquals(expected, this.dicomAccessor.getNumberOfFrames());
    }

    @Test
    public void isSingleFrame_True() {
        int expected = 1;
        this.attributes.setInt(Tag.NumberOfFrames, VR.IS, expected);
        assertEquals(true, this.dicomAccessor.isSingleFrame());
    }

    @Test
    public void isSingleFrame_False() {
        int expected = 5;
        this.attributes.setInt(Tag.NumberOfFrames, VR.IS, expected);
        assertEquals(false, this.dicomAccessor.isSingleFrame());
    }

    @Test
    public void getPlanarConfiguration_Default() {
        int expected = 1;
        this.attributes.setInt(Tag.PlanarConfiguration, VR.US, expected);
        assertEquals(expected, this.dicomAccessor.getPlanarConfiguration());
    }

    @Test
    public void isBanded_True() {
        int samples = 3;
        int planar = 1;
        this.attributes.setInt(Tag.SamplesPerPixel, VR.US, samples);
        this.attributes.setInt(Tag.PlanarConfiguration, VR.US, planar);
        assertTrue( this.dicomAccessor.isBanded());
    }

    @Test
    public void isBanded_False() {
        int samples = 1;
        int planar = 0;
        this.attributes.setInt(Tag.SamplesPerPixel, VR.US, samples);
        this.attributes.setInt(Tag.PlanarConfiguration, VR.US, planar);
        assertFalse( this.dicomAccessor.isBanded());
    }

    @Test
    public void getDataType_TYPE_BYTE() {
        this.attributes.setInt(Tag.BitsAllocated, VR.US, 8);
        assertEquals(DataBuffer.TYPE_BYTE, this.dicomAccessor.getDataType());
    }

    @Test
    public void getDataType_TYPE_USHORT() {
        this.attributes.setInt(Tag.BitsAllocated, VR.US, 12);
        assertEquals(DataBuffer.TYPE_USHORT, this.dicomAccessor.getDataType());
    }

    @Test
    public void isSwapShortsRequired_OW_BIGENDIAN() {
        this.pixelDataVR = VR.OW;
        this.attributes = new Attributes(true);
        assertTrue(this.dicomAccessor.isSwapShortsRequired());
    }

    @Test
    public void isSwapShortsRequired_OB_LITTLEENDIAN() {
        this.pixelDataVR = VR.OB;
        assertFalse(this.dicomAccessor.isSwapShortsRequired());
    }

    @Test
    public void isCompressed_ExplicitVRLittleEndian() {
        this.fmi.setString(Tag.TransferSyntaxUID, VR.UI, UID.ExplicitVRLittleEndian);
        assertFalse(this.dicomAccessor.isCompressed());
    }

    @Test
    public void isCompressed_JPEGLossless() {
        this.fmi.setString(Tag.TransferSyntaxUID, VR.UI, UID.JPEGLossless);
        assertTrue(this.dicomAccessor.isCompressed());
    }

    @Test
    public void isOB_True() {
        this.pixelDataVR = VR.OB;
        assertTrue(this.dicomAccessor.isOB());
    }

    @Test
    public void isOB_False() {
        this.pixelDataVR = VR.OW;
        assertFalse(this.dicomAccessor.isOB());
    }

    @Test
    public void getByteOrder_BIG_ENDIAN() {
        this.attributes = new Attributes(true);
        assertEquals(ByteOrder.BIG_ENDIAN, this.dicomAccessor.getByteOrder());
    }

    @Test
    public void getByteOrder_LITTLE_ENDIAN() {
        this.attributes = new Attributes();
        assertEquals(ByteOrder.LITTLE_ENDIAN, this.dicomAccessor.getByteOrder());
    }

    private class BasicDicomAccessor implements DicomAccessor {

        @Override
        public Attributes getAttributes() {
            return attributes;
        }

        @Override
        public ImageInputStream openPixelStream(int frameIndex) throws IOException {
            return null;
        }

        @Override
        public boolean containsPixelData() {
            return false;
        }

        @Override
        public VR getPixelDataVR() {
            return pixelDataVR;
        }

        @Override
        public int countFrames() throws IOException {
            return 0;
        }

        @Override
        public URI getURI() {
            return null;
        }

        @Override
        public Attributes getFileMetaInformation() {
            return fmi;
        }
    }
}