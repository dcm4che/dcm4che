package org.dcm4che3.image;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Test;

import java.awt.color.ColorSpace;
import java.awt.image.*;

import static org.junit.Assert.*;

public class PaletteColorModelTest {

    @Test
    public void testConstructorAndAccessors() {
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

        assertEquals(8, pcm.getPixelSize());
        assertEquals(255, pcm.getRed(255));
        assertEquals(0, pcm.getGreen(255));
        assertEquals(127, pcm.getBlue(255));
        assertEquals(0xff, pcm.getAlpha(255));
        assertEquals(0xffFF007F, pcm.getRGB(255));
    }

    @Test
    public void testPerColorLUT() {
        Attributes ds = new Attributes();
        // Different lengths or offsets force PerColor LUT
        ds.setInt(Tag.RedPaletteColorLookupTableDescriptor, VR.US, 2, 0, 8);
        ds.setInt(Tag.GreenPaletteColorLookupTableDescriptor, VR.US, 2, 1, 8);
        ds.setInt(Tag.BluePaletteColorLookupTableDescriptor, VR.US, 2, 0, 8);
        
        ds.setBytes(Tag.RedPaletteColorLookupTableData, VR.OW, new byte[]{10, 20});
        ds.setBytes(Tag.GreenPaletteColorLookupTableData, VR.OW, new byte[]{30, 40});
        ds.setBytes(Tag.BluePaletteColorLookupTableData, VR.OW, new byte[]{50, 60});

        PaletteColorModel pcm = new PaletteColorModel(8, DataBuffer.TYPE_BYTE, ColorSpace.getInstance(ColorSpace.CS_sRGB), ds);

        // Pixel 0: R[0]=10, G[clamped to 0]=30, B[0]=50
        assertEquals(10, pcm.getRed(0));
        assertEquals(30, pcm.getGreen(0));
        assertEquals(50, pcm.getBlue(0));

        // Pixel 1: R[1]=20, G[1-1=0]=30, B[1]=60
        assertEquals(20, pcm.getRed(1));
        assertEquals(30, pcm.getGreen(1));
        assertEquals(60, pcm.getBlue(1));
        
        // Pixel 2: R[clamped to 1]=20, G[2-1=1]=40, B[clamped to 1]=60
        assertEquals(20, pcm.getRed(2));
        assertEquals(40, pcm.getGreen(2));
        assertEquals(60, pcm.getBlue(2));
    }

    @Test
    public void testSegmentedLUT() {
        Attributes ds = new Attributes();
        ds.setInt(Tag.RedPaletteColorLookupTableDescriptor, VR.US, 10, 0, 16);
        ds.setInt(Tag.GreenPaletteColorLookupTableDescriptor, VR.US, 10, 0, 16);
        ds.setInt(Tag.BluePaletteColorLookupTableDescriptor, VR.US, 10, 0, 16);
        
        // Segmented Data: OpCode, Length, Value(s)
        // Red: Discrete (Op 0), Length 5, Values 1000..5000 (scaled down to 8-bit in PaletteColorModel)
        // Then Linear (Op 1), Length 5, End Value 10000
        int[] rSegm = {
            0, 5, 1000, 2000, 3000, 4000, 5000,
            1, 5, 10000
        };
        ds.setInt(Tag.SegmentedRedPaletteColorLookupTableData, VR.OW, rSegm);
        // Green/Blue same for simplicity or just dummy
        ds.setInt(Tag.SegmentedGreenPaletteColorLookupTableData, VR.OW, rSegm);
        ds.setInt(Tag.SegmentedBluePaletteColorLookupTableData, VR.OW, rSegm);

        PaletteColorModel pcm = new PaletteColorModel(8, DataBuffer.TYPE_BYTE, ColorSpace.getInstance(ColorSpace.CS_sRGB), ds);

        // Inflation writes (Value >> 8) to byte[]
        assertEquals(1000 >> 8, pcm.getRed(0));
        assertEquals(5000 >> 8, pcm.getRed(4));
        // Linear: y0=5000, y1=10000, n=5. Steps: (10000-5000)/5 = 1000.
        // Index 5: 5000 + 1000 = 6000
        assertEquals(6000 >> 8, pcm.getRed(5));
        assertEquals(10000 >> 8, pcm.getRed(9));
    }

    @Test
    public void testIndirectSegmentedLUT() {
        Attributes ds = new Attributes();
        ds.setInt(Tag.RedPaletteColorLookupTableDescriptor, VR.US, 4, 0, 16);
        ds.setInt(Tag.GreenPaletteColorLookupTableDescriptor, VR.US, 4, 0, 16);
        ds.setInt(Tag.BluePaletteColorLookupTableDescriptor, VR.US, 4, 0, 16);

        // Red: Indirect (Op 2), n=1 (one indirect segment), Offset 3 (pointing to a Discrete segment)
        // The data at Offset 3 is Discrete (Op 0), Length 2, Values 10000, 20000
        int[] rSegm = {
            2, 1, 3, 0, // Op 2, n=1, offset=3 (lower 16, upper 16)
            0, 2, 10000, 20000
        };
        ds.setInt(Tag.SegmentedRedPaletteColorLookupTableData, VR.OW, rSegm);
        ds.setInt(Tag.SegmentedGreenPaletteColorLookupTableData, VR.OW, rSegm);
        ds.setInt(Tag.SegmentedBluePaletteColorLookupTableData, VR.OW, rSegm);

        PaletteColorModel pcm = new PaletteColorModel(8, DataBuffer.TYPE_BYTE, ColorSpace.getInstance(ColorSpace.CS_sRGB), ds);
        // It should follow the indirect reference and inflate the discrete segment
        assertEquals(10000 >> 8, pcm.getRed(0));
        assertEquals(20000 >> 8, pcm.getRed(1));
    }

    @Test
    public void testConvertToColorSpace() {
        Attributes ds = new Attributes();
        ds.setInt(Tag.RedPaletteColorLookupTableDescriptor, VR.US, 256, 0, 8);
        ds.setInt(Tag.GreenPaletteColorLookupTableDescriptor, VR.US, 256, 0, 8);
        ds.setInt(Tag.BluePaletteColorLookupTableDescriptor, VR.US, 256, 0, 8);
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) data[i] = (byte) i;
        ds.setBytes(Tag.RedPaletteColorLookupTableData, VR.OW, data);
        ds.setBytes(Tag.GreenPaletteColorLookupTableData, VR.OW, data);
        ds.setBytes(Tag.BluePaletteColorLookupTableData, VR.OW, data);

        PaletteColorModel pcm = new PaletteColorModel(8, DataBuffer.TYPE_BYTE, ColorSpace.getInstance(ColorSpace.CS_sRGB), ds);
        
        // Convert to CIEXYZ
        PaletteColorModel pcmXYZ = pcm.convertTo(ColorSpace.getInstance(ColorSpace.CS_CIEXYZ));
        
        assertNotSame(pcm, pcmXYZ);
        assertEquals(ColorSpace.TYPE_XYZ, pcmXYZ.getColorSpace().getType());
        
        // Check conversion for some pixel
        int rgb = pcm.getRGB(128);
        int xyz = pcmXYZ.getRGB(128);
        // The getRGB in PaletteColorModel for non-sRGB still returns packed "components" 
        // because its LUT was created from converted values.
        // Wait, convertTo(rgb, src, cs) actually converts components.
        assertNotEquals(rgb, xyz);
    }

    @Test
    public void testConvertToIntDiscrete() {
        Attributes ds = new Attributes();
        ds.setInt(Tag.RedPaletteColorLookupTableDescriptor, VR.US, 256, 0, 8);
        ds.setInt(Tag.GreenPaletteColorLookupTableDescriptor, VR.US, 256, 0, 8);
        ds.setInt(Tag.BluePaletteColorLookupTableDescriptor, VR.US, 256, 0, 8);
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) data[i] = (byte) i;
        ds.setBytes(Tag.RedPaletteColorLookupTableData, VR.OW, data);
        ds.setBytes(Tag.GreenPaletteColorLookupTableData, VR.OW, data);
        ds.setBytes(Tag.BluePaletteColorLookupTableData, VR.OW, data);

        PaletteColorModel pcm = new PaletteColorModel(8, DataBuffer.TYPE_BYTE, ColorSpace.getInstance(ColorSpace.CS_sRGB), ds);
        
        // Test with DataBufferByte
        WritableRaster rasterByte = pcm.createCompatibleWritableRaster(10, 10);
        byte[] pixelsByte = ((DataBufferByte) rasterByte.getDataBuffer()).getData();
        for (int i = 0; i < pixelsByte.length; i++) pixelsByte[i] = (byte) i;
        
        BufferedImage biByte = pcm.convertToIntDiscrete(rasterByte);
        assertEquals(BufferedImage.TYPE_INT_RGB, biByte.getType());
        assertEquals(pcm.getRGB(5), biByte.getRGB(5, 0));

        // Test with DataBufferUShort (for 16-bit PCM)
        Attributes ds16 = new Attributes();
        ds16.setInt(Tag.RedPaletteColorLookupTableDescriptor, VR.US, 256, 0, 16);
        ds16.setInt(Tag.GreenPaletteColorLookupTableDescriptor, VR.US, 256, 0, 16);
        ds16.setInt(Tag.BluePaletteColorLookupTableDescriptor, VR.US, 256, 0, 16);
        byte[] data16 = new byte[512]; // 256 entries * 2 bytes
        for (int i = 0; i < 256; i++) {
            data16[i*2] = (byte) i; // high byte
        }
        ds16.setBytes(Tag.RedPaletteColorLookupTableData, VR.OW, data16);
        ds16.setBytes(Tag.GreenPaletteColorLookupTableData, VR.OW, data16);
        ds16.setBytes(Tag.BluePaletteColorLookupTableData, VR.OW, data16);

        PaletteColorModel pcm16 = new PaletteColorModel(16, DataBuffer.TYPE_USHORT, ColorSpace.getInstance(ColorSpace.CS_sRGB), ds16);
        WritableRaster rasterShort = pcm16.createCompatibleWritableRaster(10, 10);
        short[] pixelsShort = ((DataBufferUShort) rasterShort.getDataBuffer()).getData();
        for (int i = 0; i < pixelsShort.length; i++) pixelsShort[i] = (short) i;

        BufferedImage biShort = pcm16.convertToIntDiscrete(rasterShort);
        assertEquals(BufferedImage.TYPE_INT_RGB, biShort.getType());
        assertEquals(pcm16.getRGB(5), biShort.getRGB(5, 0));
    }

    @Test
    public void testIsCompatible() {
        Attributes ds = new Attributes();
        ds.setInt(Tag.RedPaletteColorLookupTableDescriptor, VR.US, 256, 0, 8);
        ds.setInt(Tag.GreenPaletteColorLookupTableDescriptor, VR.US, 256, 0, 8);
        ds.setInt(Tag.BluePaletteColorLookupTableDescriptor, VR.US, 256, 0, 8);
        ds.setBytes(Tag.RedPaletteColorLookupTableData, VR.OW, new byte[256]);
        ds.setBytes(Tag.GreenPaletteColorLookupTableData, VR.OW, new byte[256]);
        ds.setBytes(Tag.BluePaletteColorLookupTableData, VR.OW, new byte[256]);

        PaletteColorModel pcm = new PaletteColorModel(8, DataBuffer.TYPE_BYTE, ColorSpace.getInstance(ColorSpace.CS_sRGB), ds);
        
        assertTrue(pcm.isCompatibleRaster(pcm.createCompatibleWritableRaster(1, 1)));
        
        SampleModel sm3 = new ComponentSampleModel(DataBuffer.TYPE_BYTE, 1, 1, 3, 3, new int[]{0, 1, 2});
        assertFalse(pcm.isCompatibleSampleModel(sm3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingLUTDescriptor() {
        Attributes ds = new Attributes();
        new PaletteColorModel(8, DataBuffer.TYPE_BYTE, ColorSpace.getInstance(ColorSpace.CS_sRGB), ds);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalLinearSegment() {
        Attributes ds = new Attributes();
        ds.setInt(Tag.RedPaletteColorLookupTableDescriptor, VR.US, 10, 0, 16);
        ds.setInt(Tag.GreenPaletteColorLookupTableDescriptor, VR.US, 10, 0, 16);
        ds.setInt(Tag.BluePaletteColorLookupTableDescriptor, VR.US, 10, 0, 16);
        
        // Linear segment (Op 1) cannot be first
        int[] rSegm = {1, 5, 10000};
        ds.setInt(Tag.SegmentedRedPaletteColorLookupTableData, VR.OW, rSegm);
        ds.setInt(Tag.SegmentedGreenPaletteColorLookupTableData, VR.OW, rSegm);
        ds.setInt(Tag.SegmentedBluePaletteColorLookupTableData, VR.OW, rSegm);

        new PaletteColorModel(8, DataBuffer.TYPE_BYTE, ColorSpace.getInstance(ColorSpace.CS_sRGB), ds);
    }
}
