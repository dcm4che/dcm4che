package org.dcm4che3.opencv;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SampleModel;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.imageio.codec.ImageDescriptor;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageConversion;

public class NativeImageReader extends ImageReader implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeImageReader.class);

    private final ImageParameters params = new ImageParameters();

    private ImageInputStream iis;

    protected NativeImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public void dispose() {
        resetInternalState();
    }

    @Override
    public void close() {
        dispose();
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        if (input != null && !(input instanceof ImageInputStream)) {
            throw new IllegalArgumentException("input is not an ImageInputStream!");
        }
        resetInternalState();
        iis = (ImageInputStream) input;
        try {
            buildImage(iis);
        } catch (IOException e) {
            LOGGER.error("Find image parameters", e);
        }
    }

    private void resetInternalState() {
        params.setBytesPerLine(0);
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        return 1;
    }

    @Override
    public int getWidth(int frameIndex) throws IOException {
        return params.getWidth();
    }

    @Override
    public int getHeight(int frameIndex) throws IOException {
        return params.getHeight();
    }

    /**
     * Creates a <code>ImageTypeSpecifier</code> from the <code>ImageParameters</code>. The default sample model is
     * pixel interleaved and the default color model is CS_GRAY or CS_sRGB and IndexColorModel with palettes.
     */
    protected static final ImageTypeSpecifier createImageType(ImageParameters params, ColorSpace colorSpace,
        byte[] redPalette, byte[] greenPalette, byte[] bluePalette, byte[] alphaPalette) throws IOException {
        return createImageType(params,
            createColorModel(params, colorSpace, redPalette, greenPalette, bluePalette, alphaPalette));
    }

    private static ColorModel createColorModel(ImageParameters params, ColorSpace colorSpace, byte[] redPalette,
        byte[] greenPalette, byte[] bluePalette, byte[] alphaPalette) {
        int nType = params.getDataType();
        int nBands = params.getSamplesPerPixel();
        int nBitDepth = params.getBitsPerSample();

        ColorModel colorModel;
        if (nBands == 1 && redPalette != null && greenPalette != null && bluePalette != null
            && redPalette.length == greenPalette.length && redPalette.length == bluePalette.length) {

            // Build IndexColorModel
            int paletteLength = redPalette.length;
            if (alphaPalette != null) {
                byte[] alphaTmp = alphaPalette;
                if (alphaPalette.length != paletteLength) {
                    alphaTmp = new byte[paletteLength];
                    if (alphaPalette.length > paletteLength) {
                        System.arraycopy(alphaPalette, 0, alphaTmp, 0, paletteLength);
                    } else {
                        System.arraycopy(alphaPalette, 0, alphaTmp, 0, alphaPalette.length);
                        for (int i = alphaPalette.length; i < paletteLength; i++) {
                            alphaTmp[i] = (byte) 255; // Opaque.
                        }
                    }
                }
                colorModel =
                    new IndexColorModel(nBitDepth, paletteLength, redPalette, greenPalette, bluePalette, alphaTmp);
            } else {
                colorModel = new IndexColorModel(nBitDepth, paletteLength, redPalette, greenPalette, bluePalette);
            }
        } else if (nType == ImageParameters.TYPE_BIT) {
            // 0 -> 0x00 (black), 1 -> 0xff (white)
            byte[] comp = new byte[] { (byte) 0x00, (byte) 0xFF };
            colorModel = new IndexColorModel(1, 2, comp, comp, comp);
        } else {
            ColorSpace cs;
            boolean hasAlpha;
            if (colorSpace != null
                && (colorSpace.getNumComponents() == nBands || colorSpace.getNumComponents() == nBands - 1)) {
                cs = colorSpace;
                hasAlpha = colorSpace.getNumComponents() + 1 == nBands;
            } else {
                cs = ColorSpace.getInstance(nBands < 3 ? ColorSpace.CS_GRAY : ColorSpace.CS_sRGB);
                hasAlpha = nBands % 2 == 0;
            }

            int[] bits = new int[nBands];
            for (int i = 0; i < nBands; i++) {
                bits[i] = nBitDepth;
            }
            colorModel = new ComponentColorModel(cs, bits, hasAlpha, false,
                hasAlpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE, nType);
        }
        return colorModel;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int frameIndex) throws IOException {

        ImageTypeSpecifier imageType = createImageType(params, null, null, null, null, null);

        return Collections.singletonList(imageType).iterator();
    }

    protected static final ImageTypeSpecifier createImageType(ImageParameters params, ColorModel colorModel)
        throws IOException {

        int nType = params.getDataType();
        int nWidth = params.getWidth();
        int nHeight = params.getHeight();
        int nBands = params.getSamplesPerPixel();
        int nBitDepth = params.getBitsPerSample();
        int nScanlineStride = params.getBytesPerLine() / ((nBitDepth + 7) / 8);

        // TODO should handle all types.
        if (nType < 0 || (nType > 2 && nType != ImageParameters.TYPE_BIT)) {
            throw new UnsupportedOperationException("Unsupported data type" + " " + nType);
        }

        int[] bandOffsets = new int[nBands];
        for (int i = 0; i < nBands; i++) {
            bandOffsets[i] = i;
        }
        SampleModel sampleModel =
            new PixelInterleavedSampleModel(nType, nWidth, nHeight, nBands, nScanlineStride, bandOffsets);

        return new ImageTypeSpecifier(colorModel, sampleModel);
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        return ImageConversion.toBufferedImage(getNativeImage(param));
    }

    private PlanarImage getNativeImage(ImageReadParam param) throws IOException {
        StreamSegment seg = StreamSegment.getStreamSegment(iis, param);
        ImageDescriptor desc = seg.getImageDescriptor();

        int dcmFlags = desc.isSigned() ? Imgcodecs.DICOM_IMREAD_SIGNED : Imgcodecs.DICOM_IMREAD_UNSIGNED;
        // Force JPEG Baseline (1.2.840.10008.1.2.4.50) to YBR_FULL_422 color model when RGB (error made by some
        // constructors). RGB color model doesn't make sense for lossy jpeg.
        // http://dicom.nema.org/medical/dicom/current/output/chtml/part05/sect_8.2.html#sect_8.2.1
        PhotometricInterpretation pmi = desc.getPhotometricInterpretation();
        if (pmi.name().startsWith("YBR") || ("RGB".equalsIgnoreCase(pmi.name()) && params.getJpegMarker() == 0xffc0)) {
            dcmFlags |= Imgcodecs.DICOM_IMREAD_YBR;
        }

        if (seg instanceof FileStreamSegment) {

            MatOfDouble positions =
                new MatOfDouble(ExtendSegmentedInputImageStream.getDoubleArray(seg.getSegPosition()));
            MatOfDouble lengths = new MatOfDouble(ExtendSegmentedInputImageStream.getDoubleArray(seg.getSegLength()));

            return ImageCV.toImageCV(Imgcodecs.dicomJpgRead(((FileStreamSegment) seg).getFilePath(), positions, lengths,
                dcmFlags, Imgcodecs.IMREAD_UNCHANGED));
        } else if (seg instanceof MemoryStreamSegment) {
            byte[] b = ((MemoryStreamSegment) seg).getCache();
            Mat buf = new Mat(1, b.length, CvType.CV_8UC1);
            buf.put(0, 0, b);
            return ImageCV.toImageCV(Imgcodecs.dicomJpgRead(buf, dcmFlags, Imgcodecs.IMREAD_UNCHANGED));
        }
        return null;
    }

    public static SOFSegment getSOFSegment(ImageInputStream iis) throws IOException {
        iis.mark();
        try {
            int byte1 = iis.read();
            int byte2 = iis.read();
            // Magic numbers for JPEG (general jpeg marker)
            if ((byte1 != 0xFF) || (byte2 != 0xD8)) {
                return null;
            }
            do {
                byte1 = iis.read();
                byte2 = iis.read();
                // Something wrong, but try to read it anyway
                if (byte1 != 0xFF) {
                    break;
                }
                // Start of scan
                if (byte2 == 0xDA) {
                    break;
                }
                // Start of Frame, also known as SOF55, indicates a JPEG-LS file.
                if (byte2 == 0xF7) {
                    return getSOF(iis, (byte1 << 8) + byte2);
                }
                // 0xffc0: // SOF_0: JPEG baseline
                // 0xffc1: // SOF_1: JPEG extended sequential DCT
                // 0xffc2: // SOF_2: JPEG progressive DCT
                // 0xffc3: // SOF_3: JPEG lossless sequential
                if ((byte2 >= 0xC0) && (byte2 <= 0xC3)) {
                    return getSOF(iis, (byte1 << 8) + byte2);
                }
                // 0xffc5: // SOF_5: differential (hierarchical) extended sequential, Huffman
                // 0xffc6: // SOF_6: differential (hierarchical) progressive, Huffman
                // 0xffc7: // SOF_7: differential (hierarchical) lossless, Huffman
                if ((byte2 >= 0xC5) && (byte2 <= 0xC7)) {
                    return getSOF(iis, (byte1 << 8) + byte2);
                }
                // 0xffc9: // SOF_9: extended sequential, arithmetic
                // 0xffca: // SOF_10: progressive, arithmetic
                // 0xffcb: // SOF_11: lossless, arithmetic
                if ((byte2 >= 0xC9) && (byte2 <= 0xCB)) {
                    return getSOF(iis, (byte1 << 8) + byte2);
                }
                // 0xffcd: // SOF_13: differential (hierarchical) extended sequential, arithmetic
                // 0xffce: // SOF_14: differential (hierarchical) progressive, arithmetic
                // 0xffcf: // SOF_15: differential (hierarchical) lossless, arithmetic
                if ((byte2 >= 0xCD) && (byte2 <= 0xCF)) {
                    return getSOF(iis, (byte1 << 8) + byte2);
                }
                int length = iis.read() << 8;
                length += iis.read();
                length -= 2;
                while (length > 0) {
                    length -= iis.skipBytes(length);
                }
            } while (true);
            return null;
        } finally {
            iis.reset();
        }
    }

    protected static SOFSegment getSOF(ImageInputStream iis, int marker) throws IOException {
        readUnsignedShort(iis);
        int samplePrecision = readUnsignedByte(iis);
        int lines = readUnsignedShort(iis);
        int samplesPerLine = readUnsignedShort(iis);
        int componentsInFrame = readUnsignedByte(iis);
        return new SOFSegment(marker, samplePrecision, lines, samplesPerLine, componentsInFrame);
    }

    public ImageParameters buildImage(ImageInputStream iis) throws IOException {
        if (iis != null && params.getBytesPerLine() < 1) {
            SOFSegment sof = getSOFSegment(iis);
            if (sof != null) {
                params.setJpegMarker(sof.getMarker());
                params.setWidth(sof.getSamplesPerLine());
                params.setHeight(sof.getLines());
                params.setBitsPerSample(sof.getSamplePrecision());
                params.setSamplesPerPixel(sof.getComponents());
                params.setBytesPerLine(
                    params.getWidth() * params.getSamplesPerPixel() * ((params.getBitsPerSample() + 7) / 8));
                return params;
            }
        }
        return null;
    }

    private static final int readUnsignedByte(ImageInputStream iis) throws IOException {
        int ch = iis.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return ch;
    }

    private static final int readUnsignedShort(ImageInputStream iis) throws IOException {
        int ch1 = iis.read();
        int ch2 = iis.read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        return (ch1 << 8) + ch2;
    }

}
