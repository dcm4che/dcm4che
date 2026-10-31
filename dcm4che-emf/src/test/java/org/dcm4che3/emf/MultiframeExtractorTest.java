package org.dcm4che3.emf;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.junit.Test;

public class MultiframeExtractorTest {

    @Test
    public void testExtractTrueColorFrame() throws Exception {
        int rows = 2, columns = 2, samplesPerPixel = 3, frames = 2;
        int frameLength = rows * columns * samplesPerPixel;
        byte[] pixelData = new byte[frameLength * frames];
        for (int i = 0; i < pixelData.length; i++)
            pixelData[i] = (byte) i;

        Attributes emf = new Attributes();
        emf.setString(Tag.SOPClassUID, VR.UI, UID.MultiFrameTrueColorSecondaryCaptureImageStorage);
        emf.setString(Tag.SeriesInstanceUID, VR.UI, "1.2.3");
        emf.setString(Tag.SOPInstanceUID, VR.UI, "1.2.3.1");
        emf.setInt(Tag.SamplesPerPixel, VR.US, samplesPerPixel);
        emf.setString(Tag.PhotometricInterpretation, VR.CS, "RGB");
        emf.setInt(Tag.Rows, VR.US, rows);
        emf.setInt(Tag.Columns, VR.US, columns);
        emf.setInt(Tag.BitsAllocated, VR.US, 8);
        emf.setInt(Tag.NumberOfFrames, VR.IS, frames);
        emf.setBytes(Tag.PixelData, VR.OB, pixelData);

        Attributes sf = new MultiframeExtractor().extract(emf, 1);

        assertArrayEquals(Arrays.copyOfRange(pixelData, frameLength, 2 * frameLength),
                sf.getBytes(Tag.PixelData));
    }
}
