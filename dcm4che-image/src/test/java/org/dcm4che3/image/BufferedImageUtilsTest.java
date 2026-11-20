package org.dcm4che3.image;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.junit.Test;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;

import static org.junit.Assert.*;


public class BufferedImageUtilsTest {

    @Test
    public void testConvertToIntRGB() {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage result = BufferedImageUtils.convertToIntRGB(bi);
        assertEquals(BufferedImage.TYPE_INT_RGB, result.getType());
        assertEquals(10, result.getWidth());
        assertEquals(10, result.getHeight());

        // Should return same image if already DirectColorModel (like TYPE_INT_RGB)
        BufferedImage biInt = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        assertSame(biInt, BufferedImageUtils.convertToIntRGB(biInt));
    }

    @Test
    public void testConvertYBRtoRGB() {
        ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        YBRColorSpace ybrCS = new YBRColorSpace(sRGB, YBR.FULL);
        ColorModel cm = new ComponentColorModel(ybrCS, new int[]{8, 8, 8}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        WritableRaster raster = cm.createCompatibleWritableRaster(1, 1);
        // Y=255, Cb=128, Cr=128 (roughly white)
        raster.setDataElements(0, 0, new byte[]{(byte) 255, (byte) 128, (byte) 128});
        BufferedImage src = new BufferedImage(cm, raster, false, null);

        BufferedImage dst = BufferedImageUtils.convertYBRtoRGB(src, null);
        assertNotNull(dst);
        assertEquals(ColorSpace.TYPE_RGB, dst.getColorModel().getColorSpace().getType());
        
        int rgb = dst.getRGB(0, 0);
        assertEquals(255, (rgb >> 16) & 0xFF, 2);
        assertEquals(255, (rgb >> 8) & 0xFF, 2);
        assertEquals(255, rgb & 0xFF, 2);
    }

    @Test
    public void testConvertPaletteToRGB() {
        int[] cmap = new int[256];
        cmap[1] = 0xFFFF0000; // Red
        IndexColorModel icm = new IndexColorModel(8, 256, cmap, 0, false, -1, DataBuffer.TYPE_BYTE);
        BufferedImage src = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_INDEXED, icm);
        src.getRaster().setSample(0, 0, 0, 1);

        BufferedImage dst = BufferedImageUtils.convertPalettetoRGB(src, null);
        assertNotNull(dst);
        int rgb = dst.getRGB(0, 0);
        assertEquals(0xFFFF0000, rgb);
    }

    @Test
    public void testConvertShortsToBytes() {
        BufferedImage src = new BufferedImage(10, 10, BufferedImage.TYPE_USHORT_GRAY);
        src.getRaster().setSample(0, 0, 0, 123);
        
        BufferedImage dst = BufferedImageUtils.convertShortsToBytes(src, null);
        assertEquals(BufferedImage.TYPE_BYTE_GRAY, dst.getType());
        assertEquals(123, dst.getRaster().getSample(0, 0, 0));
    }

    @Test
    public void testReplaceColorModel() {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        BufferedImage result = BufferedImageUtils.replaceColorModel(bi, cm);
        assertSame(cm, result.getColorModel());
        assertSame(bi.getRaster(), result.getRaster());
    }

    @Test
    public void testConvertColor() {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY);
        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        BufferedImage result = BufferedImageUtils.convertColor(bi, cm);
        assertEquals(cm.getColorSpace(), result.getColorModel().getColorSpace());
        assertEquals(10, result.getWidth());
        assertEquals(10, result.getHeight());
    }

    @Test
    public void testToImagePixelModuleFromBI() {
        // Test Gray
        BufferedImage biGray = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY);
        biGray.getRaster().setSample(0, 0, 0, 255);
        Attributes attrs = BufferedImageUtils.toImagePixelModule(biGray, null);
        assertEquals(1, attrs.getInt(Tag.SamplesPerPixel, 0));
        assertEquals("MONOCHROME2", attrs.getString(Tag.PhotometricInterpretation));
        assertEquals(10, attrs.getInt(Tag.Rows, 0));
        assertEquals(10, attrs.getInt(Tag.Columns, 0));
        byte[] pixelData = attrs.getSafeBytes(Tag.PixelData);
        assertEquals(100, pixelData.length);
        assertEquals((byte) 255, pixelData[0]);

        // Test RGB
        BufferedImage biRGB = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
        biRGB.setRGB(0, 0, 0xFFFF0000); // Red
        attrs = BufferedImageUtils.toImagePixelModule(biRGB, null);
        assertEquals(3, attrs.getInt(Tag.SamplesPerPixel, 0));
        assertEquals("RGB", attrs.getString(Tag.PhotometricInterpretation));
        pixelData = attrs.getSafeBytes(Tag.PixelData);
        assertEquals(300, pixelData.length);
        // DICOM RGB is R, G, B. TYPE_3BYTE_BGR is B, G, R in raster.
        // toRGBPixelData should handle conversion to R, G, B.
        assertEquals((byte) 255, pixelData[0]); // R
        assertEquals((byte) 0, pixelData[1]);   // G
        assertEquals((byte) 0, pixelData[2]);   // B
    }
}
