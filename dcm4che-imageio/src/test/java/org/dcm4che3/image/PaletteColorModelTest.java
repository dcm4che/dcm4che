package org.dcm4che3.image;

import org.dcm4che.test.data.TestData;
import org.dcm4che3.data.DatasetWithFMI;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.junit.Test;

import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;

public class PaletteColorModelTest {


    @Test
    public void toIndexColorModel_EquivalentToPaletteColorModel() throws IOException {
        TestData data = new TestData("US-PAL-8-10x-echo");
        DatasetWithFMI dataset = loadAttributes(data.toURL());

        int allocated = dataset.getDataset().getInt(Tag.BitsAllocated, 8);
        PaletteColorModel paletteColorModel = ColorModelFactory.createPaletteColorModel(allocated, DataBuffer.TYPE_BYTE, dataset.getDataset());
        ColorModel indexColorModel = paletteColorModel.toIndexColorModel();

        for(int i = 0; i< 256; i++) {
            assertEquals(paletteColorModel.getRed(i), indexColorModel.getRed(i));
            assertEquals(paletteColorModel.getBlue(i), indexColorModel.getBlue(i));
            assertEquals(paletteColorModel.getGreen(i), indexColorModel.getGreen(i));
            assertEquals(paletteColorModel.getRGB(i), indexColorModel.getRGB(i));
        }

    }
    DatasetWithFMI loadAttributes(URL fileURL) throws IOException {
        try(DicomInputStream dis = new DicomInputStream(fileURL.openStream())) {
            return dis.readDatasetWithFMI();
        }
    }
}