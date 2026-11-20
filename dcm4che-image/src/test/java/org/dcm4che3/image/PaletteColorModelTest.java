package org.dcm4che3.image;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Test;

import java.awt.color.ColorSpace;
import java.awt.image.DataBuffer;

import static org.junit.Assert.assertEquals;


public class PaletteColorModelTest {

    @Test
    public void test8BitLUT() {
        Attributes ds = new Attributes();
        ds.setInt(Tag.RedPaletteColorLookupTableDescriptor, VR.US, 256, 0, 8);
        ds.setInt(Tag.GreenPaletteColorLookupTableDescriptor, VR.US, 256, 0, 8);
        ds.setInt(Tag.BluePaletteColorLookupTableDescriptor, VR.US, 256, 0, 8);
        
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];
        for (int i = 0; i < 256; i++) {
            r[i] = (byte) i;
            g[i] = (byte) (255 - i);
            b[i] = (byte) (i / 2);
        }
        ds.setBytes(Tag.RedPaletteColorLookupTableData, VR.OW, r);
        ds.setBytes(Tag.GreenPaletteColorLookupTableData, VR.OW, g);
        ds.setBytes(Tag.BluePaletteColorLookupTableData, VR.OW, b);

        PaletteColorModel pcm = new PaletteColorModel(8, DataBuffer.TYPE_BYTE, ColorSpace.getInstance(ColorSpace.CS_sRGB), ds);
        
        assertEquals(0xFF00FF00, pcm.getRGB(0));
        assertEquals(0xFF7F803F, pcm.getRGB(127));
        assertEquals(0xFFFF007F, pcm.getRGB(255));
    }

    @Test
    public void testSegmentedLUT() {
        Attributes ds = new Attributes();
        // 256 entries, 16 bits
        ds.setInt(Tag.RedPaletteColorLookupTableDescriptor, VR.US, 256, 0, 16);
        ds.setInt(Tag.GreenPaletteColorLookupTableDescriptor, VR.US, 256, 0, 16);
        ds.setInt(Tag.BluePaletteColorLookupTableDescriptor, VR.US, 256, 0, 16);

        // Segmented Red LUT: Discrete segment (Op=0) with 2 entries
        // Segmented Green LUT: Linear segment (Op=1)
        // Segmented Blue LUT: Discrete segment
        
        // Red: 0, 2, 0x1111, 0x2222 -> results in 0x11, 0x22 (high bytes)
        ds.setInt(Tag.SegmentedRedPaletteColorLookupTableData, VR.OW, 0, 2, 0x1100, 0x2200);
        // Green: Linear: 1, 254, 0x0000, 0xFFFF -> wait, n entries after first
        // 0, 1, 0x0000 -> Discrete segment with 1 entry (first entry)
        // 1, 255, 0xFFFF -> Linear segment for the rest
        ds.setInt(Tag.SegmentedGreenPaletteColorLookupTableData, VR.OW, 0, 1, 0x0000, 1, 255, 0xFFFF);
        ds.setInt(Tag.SegmentedBluePaletteColorLookupTableData, VR.OW, 0, 256); // Too simple? 
        // Let's just fill it with something
        int[] bSeg = new int[258];
        bSeg[0] = 0; bSeg[1] = 256;
        for(int i=0; i<256; i++) bSeg[i+2] = (i << 8);
        ds.setInt(Tag.SegmentedBluePaletteColorLookupTableData, VR.OW, bSeg);

        PaletteColorModel pcm = new PaletteColorModel(8, DataBuffer.TYPE_BYTE, ColorSpace.getInstance(ColorSpace.CS_sRGB), ds);
        
        assertEquals(0x11, pcm.getRed(0));
        assertEquals(0x22, pcm.getRed(1));
        assertEquals(0x00, pcm.getGreen(0));
        assertEquals(0xFF, pcm.getGreen(255));
        assertEquals(0x00, pcm.getBlue(0));
        assertEquals(128, pcm.getBlue(128));
    }
}
