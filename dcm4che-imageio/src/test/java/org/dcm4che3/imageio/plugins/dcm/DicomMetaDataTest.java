package org.dcm4che3.imageio.plugins.dcm;


import org.dcm4che.test.data.TestData;
import org.dcm4che3.error.UnavailableFrameException;
import org.dcm4che3.imageio.metadata.DicomMetaDataFactory;
import org.junit.Test;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Abstract test class to test DicomMetaData implementations against a standard set of expectations.
 */
public abstract class DicomMetaDataTest {

    private static final String NM_MF = "NM-MONO2-16-13x-heart";
    private static final String US_MF_RLE = "US-PAL-8-10x-echo";
    private static final String MR_SEGMENTED_SINGLE_FRAME = "MR_SegmentedSingleFrame.dcm";


    abstract DicomMetaDataFactory createMetadataFactory();

    @Test
    public void countFrames_Uncompressed() throws IOException {
        DicomMetaData metaData = createMetadata(NM_MF);
        int count = metaData.getAccessor().countFrames();
        assertEquals("We must be able to calculate the number of frames from the pixel data",13, count);
    }

    @Test
    public void countFrames_Compressed_RLE() throws IOException {
        DicomMetaData metaData = createMetadata(US_MF_RLE);
        int count = metaData.getAccessor().countFrames();
        assertEquals("We must be able to calculate the number of frames from the pixel data",10, count);
    }

    @Test
    public void openStream_Compressed_RLE_KnownFrame() throws IOException {
        DicomMetaData metaData = createMetadata(US_MF_RLE);
        try(ImageInputStream iis = metaData.getAccessor().openPixelStream(5)) {
            assertEquals("The image input stream must be the length of the appropriate fragment..",43946, iis.length());
        }
    }


    @Test(expected = UnavailableFrameException.class)
    public void openStream_Compressed_RLE_FrameOutOfBounds() throws IOException {
        DicomMetaData metaData = createMetadata(US_MF_RLE);
        int outOfBounds = metaData.getAccessor().countFrames() + 5;
        try(ImageInputStream iis = metaData.getAccessor().openPixelStream(outOfBounds)) {
            fail("We must throw a specific error when a non-existant frame is requested. ");
        }
    }

    @Test
    public void countFrames_SingleFrame_Segmented() throws IOException {
        DicomMetaData metaData = createMetadata(MR_SEGMENTED_SINGLE_FRAME);
        int count = metaData.getAccessor().countFrames();
        assertEquals("This is a segmented single frame.  Must be able to detect that",1, count);
    }


    @Test
    public void openStream_SingleFrame_Segmented() throws IOException {
        DicomMetaData metaData = createMetadata(MR_SEGMENTED_SINGLE_FRAME);
        try(ImageInputStream iis = metaData.getAccessor().openPixelStream(-1)) {
            assertEquals("The data stream must be the length of the segments combined.",153390, iis.length());
        }
    }



    protected DicomMetaData createMetadata(String fileName) throws IOException {
        return createMetadata(new TestData(fileName));
    }

    private DicomMetaData createMetadata(TestData data) throws IOException {
        return this.createMetadataFactory().readMetaData(data.toFile());
    }


}
