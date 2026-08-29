package org.dcm4che3.image;

import org.junit.Test;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;

import static org.junit.Assert.*;


public class SampledComponentColorModelTest {

    @Test
    public void testProperties() {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        SampledComponentColorModel cm = new SampledComponentColorModel(cs, ColorSubsampling.YBR_XXX_422);
        
        assertEquals(24, cm.getPixelSize());
        assertEquals(Transparency.OPAQUE, cm.getTransparency());
        assertEquals(DataBuffer.TYPE_BYTE, cm.getTransferType());
        assertEquals(cs, cm.getColorSpace());
    }

    @Test
    public void testCompatibility() {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        SampledComponentColorModel cm = new SampledComponentColorModel(cs, ColorSubsampling.YBR_XXX_422);
        
        SampleModel sm = cm.createCompatibleSampleModel(10, 10);
        assertTrue(sm instanceof SampledComponentSampleModel);
        assertTrue(cm.isCompatibleSampleModel(sm));
        
        Raster raster = Raster.createRaster(sm, new DataBufferByte(100), null);
        assertTrue(cm.isCompatibleRaster(raster));
        
        assertFalse(cm.isCompatibleSampleModel(new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, 10, 10,
                new int[]{0xFF0000, 0xFF00, 0xFF})));
    }

    @Test
    public void testGetRGBFromInt() {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        SampledComponentColorModel cm = new SampledComponentColorModel(cs, ColorSubsampling.YBR_XXX_422);
        
        int pixel = 0x112233;
        assertEquals(0x33, cm.getBlue(pixel));
        assertEquals(0x2200, cm.getGreen(pixel));
        assertEquals(0x110000, cm.getRed(pixel));
        assertEquals(255, cm.getAlpha(pixel));
    }

    @Test
    public void testGetRGBFromObject() {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        SampledComponentColorModel cm = new SampledComponentColorModel(cs, ColorSubsampling.YBR_XXX_422);
        
        // Input is byte[] representing [R, G, B] or similar depending on ColorSpace.
        // For CS_sRGB, getRGB treats ba[0] as R, ba[1] as G, ba[2] as B.
        byte[] pixelData = new byte[]{(byte) 255, 0, 0}; // Pure Red
        
        int rgb = cm.getRGB(pixelData);
        assertEquals(0xFFFF0000, rgb | 0xFF000000);
        
        assertEquals(255, cm.getRed(pixelData));
        assertEquals(0, cm.getGreen(pixelData));
        assertEquals(0, cm.getBlue(pixelData));
        assertEquals(255, cm.getAlpha(pixelData));
    }

    @Test
    public void testGetRGBFromYBR() {
        ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        YBRColorSpace ybrCS = new YBRColorSpace(sRGB, YBR.FULL);
        SampledComponentColorModel cm = new SampledComponentColorModel(ybrCS, ColorSubsampling.YBR_XXX_422);

        // Y=255, Cb=128, Cr=128 should be roughly white in RGB
        byte[] ybrData = new byte[]{(byte) 255, (byte) 128, (byte) 128};
        int rgb = cm.getRGB(ybrData);
        
        // RGB should be roughly 255, 255, 255
        assertEquals(255, (rgb >> 16) & 0xFF, 2);
        assertEquals(255, (rgb >> 8) & 0xFF, 2);
        assertEquals(255, rgb & 0xFF, 2);
    }
    
    // Helper to avoid dependency on java.awt.image.DataBufferByte which might have different visibility/availability
    private static class DataBufferByte extends DataBuffer {
        public DataBufferByte(int size) {
            super(TYPE_BYTE, size);
        }
        @Override
        public int getElem(int bank, int i) { return 0; }
        @Override
        public void setElem(int bank, int i, int val) {}
    }
}
