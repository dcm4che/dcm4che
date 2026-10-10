package org.dcm4che3.image;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Test;
import org.w3c.dom.Node;

import javax.imageio.IIOImage;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.spi.ImageReaderSpi;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.*;

public class BufferedImageUtilsTest {

    @Test
    public void testConvertToIntRGB_DirectColorModel() {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        BufferedImage result = BufferedImageUtils.convertToIntRGB(bi);
        assertSame(bi, result);
    }

    @Test
    public void testConvertToIntRGB_NonDirectColorModel() {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage result = BufferedImageUtils.convertToIntRGB(bi);
        assertNotSame(bi, result);
        assertEquals(BufferedImage.TYPE_INT_RGB, result.getType());
        assertEquals(10, result.getWidth());
        assertEquals(10, result.getHeight());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertToIntRGB_InvalidComponents() {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImageUtils.convertToIntRGB(bi);
    }

    @Test
    public void testConvertYBRtoRGB() {
        // YCbCr (YBR_FULL) is often represented as 3-component byte data.
        // We'll use a simple RGB image and pretend it's YBR for the sake of calling the method,
        // as the method just does CS conversion.
        BufferedImage src = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage result = BufferedImageUtils.convertYBRtoRGB(src, null);
        assertNotNull(result);
        assertEquals(10, result.getWidth());
        assertEquals(10, result.getHeight());
        assertEquals(DataBuffer.TYPE_BYTE, result.getColorModel().getTransferType());
    }

    @Test
    public void testConvertPaletteToRGB() {
        IndexColorModel icm = new IndexColorModel(8, 2, new byte[]{0, (byte)255}, new byte[]{0, (byte)255}, new byte[]{0, (byte)255});
        BufferedImage src = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_INDEXED, icm);
        BufferedImage result = BufferedImageUtils.convertPalettetoRGB(src, null);
        assertNotNull(result);
        assertEquals(ColorSpace.TYPE_RGB, result.getColorModel().getColorSpace().getType());
    }

    @Test
    public void testConvertShortsToBytes_Byte() {
        BufferedImage src = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage result = BufferedImageUtils.convertShortsToBytes(src, null);
        assertEquals(BufferedImage.TYPE_BYTE_GRAY, result.getType());
    }

    @Test
    public void testConvertShortsToBytes_UShort() {
        BufferedImage src = new BufferedImage(10, 10, BufferedImage.TYPE_USHORT_GRAY);
        BufferedImage result = BufferedImageUtils.convertShortsToBytes(src, null);
        assertEquals(BufferedImage.TYPE_BYTE_GRAY, result.getType());
    }

    @Test
    public void testReplaceColorModel() {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ColorModel cm = new DirectColorModel(24, 0xff0000, 0x00ff00, 0x0000ff);
        BufferedImage result = BufferedImageUtils.replaceColorModel(bi, cm);
        assertEquals(cm, result.getColorModel());
        assertSame(bi.getRaster(), result.getRaster());
    }

    @Test
    public void testConvertColor() {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[]{8}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        BufferedImage result = BufferedImageUtils.convertColor(bi, cm);
        assertEquals(cm.getColorSpace().getType(), result.getColorModel().getColorSpace().getType());
    }

    @Test
    public void testToImagePixelModule_Gray() {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY);
        Attributes attrs = new Attributes();
        BufferedImageUtils.toImagePixelModule(bi, attrs);
        assertEquals(1, attrs.getInt(Tag.SamplesPerPixel, 0));
        assertEquals("MONOCHROME2", attrs.getString(Tag.PhotometricInterpretation));
        assertEquals(10, attrs.getInt(Tag.Rows, 0));
        assertEquals(10, attrs.getInt(Tag.Columns, 0));
    }

    @Test
    public void testToImagePixelModule_RGB() {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
        Attributes attrs = new Attributes();
        BufferedImageUtils.toImagePixelModule(bi, attrs);
        assertEquals(3, attrs.getInt(Tag.SamplesPerPixel, 0));
        assertEquals("RGB", attrs.getString(Tag.PhotometricInterpretation));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToImagePixelModule_UnsupportedColorSpace() {
        // Create a 4-component CMYK image
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_4BYTE_ABGR);
        // TYPE_4BYTE_ABGR is usually still in RGB space, so try to force another one if possible
        ColorSpace cs = new ColorSpace(ColorSpace.TYPE_CMYK, 4) {
            @Override public float[] toRGB(float[] colorvalue) { return new float[3]; }
            @Override public float[] fromRGB(float[] rgbvalue) { return new float[4]; }
            @Override public float[] toCIEXYZ(float[] colorvalue) { return new float[3]; }
            @Override public float[] fromCIEXYZ(float[] colorvalue) { return new float[4]; }
        };
        ComponentColorModel cm = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        WritableRaster raster = cm.createCompatibleWritableRaster(10, 10);
        BufferedImage biUnsupported = new BufferedImage(cm, raster, false, null);
        BufferedImageUtils.toImagePixelModule(biUnsupported, null);
    }

    @Test
    public void testToImagePixelModule_Reader() throws IOException {
        final BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_INDEXED);
        ImageReader reader = new StubImageReader(new BufferedImage[]{bi});

        Attributes attrs = new Attributes();
        BufferedImageUtils.toImagePixelModule(reader, attrs);

        assertEquals(3, attrs.getInt(Tag.SamplesPerPixel, 0)); // Palette converted to RGB
        assertEquals("RGB", attrs.getString(Tag.PhotometricInterpretation));
    }

    @Test
    public void testToImagePixelModule_AnimatedGif() throws IOException {
        final BufferedImage bi1 = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_INDEXED);
        final BufferedImage bi2 = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_INDEXED);

        IIOMetadata metadata1 = new StubIIOMetadata(createGifMetadata("10"));
        IIOMetadata metadata2 = new StubIIOMetadata(createGifMetadata("20"));

        ImageReader reader = new StubImageReader(
                new BufferedImage[]{bi1, bi2},
                new IIOMetadata[]{metadata1, metadata2});

        Attributes attrs = new Attributes();
        BufferedImageUtils.toImagePixelModule(reader, attrs);

        assertEquals(2, attrs.getInt(Tag.NumberOfFrames, 0));
        assertEquals("100", attrs.getString(Tag.FrameTime));
        assertArrayEquals(new String[]{"100", "200"}, attrs.getStrings(Tag.FrameTimeVector));
    }

    private IIOMetadataNode createGifMetadata(String delayTime) {
        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_gif_image_1.0");
        IIOMetadataNode gce = new IIOMetadataNode("GraphicControlExtension");
        gce.setAttribute("delayTime", delayTime);
        root.appendChild(gce);
        IIOMetadataNode id = new IIOMetadataNode("ImageDescriptor");
        id.setAttribute("imageLeftPosition", "0");
        id.setAttribute("imageTopPosition", "0");
        root.appendChild(id);
        return root;
    }

    private static class StubImageReader extends ImageReader {
        private final BufferedImage[] images;
        private final IIOMetadata[] metadata;

        protected StubImageReader(BufferedImage[] images) {
            this(images, null);
        }

        protected StubImageReader(BufferedImage[] images, IIOMetadata[] metadata) {
            super(null);
            this.images = images;
            this.metadata = metadata;
        }

        @Override
        public int getNumImages(boolean allowSearch) {
            return images.length;
        }

        @Override
        public int getWidth(int imageIndex) {
            return images[imageIndex].getWidth();
        }

        @Override
        public int getHeight(int imageIndex) {
            return images[imageIndex].getHeight();
        }

        @Override
        public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
            return null;
        }

        @Override
        public IIOMetadata getStreamMetadata() {
            return null;
        }

        @Override
        public IIOMetadata getImageMetadata(int imageIndex) {
            return metadata != null ? metadata[imageIndex] : null;
        }

        @Override
        public BufferedImage read(int imageIndex, ImageReadParam param) {
            return images[imageIndex];
        }

        @Override
        public IIOImage readAll(int imageIndex, ImageReadParam param) {
            if (imageIndex >= images.length) throw new IndexOutOfBoundsException();
            return new IIOImage(images[imageIndex], null, getImageMetadata(imageIndex));
        }
    }

    private static class StubIIOMetadata extends IIOMetadata {
        private final Node tree;

        protected StubIIOMetadata(Node tree) {
            this.tree = tree;
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }

        @Override
        public Node getAsTree(String formatName) {
            return tree;
        }

        @Override
        public void mergeTree(String formatName, Node root) {
        }

        @Override
        public void reset() {
        }
    }
}
