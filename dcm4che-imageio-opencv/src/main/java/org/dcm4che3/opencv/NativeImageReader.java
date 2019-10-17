/*
 * **** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015-2018
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * **** END LICENSE BLOCK *****
 *
 */

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
import java.nio.ByteBuffer;
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

/**
 * @author Nicolas Roduit
 * @since Mar 2018
 */
public class NativeImageReader extends ImageReader implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeImageReader.class);

    private final boolean canEncodeSigned;

    private final ImageParameters params = new ImageParameters();

    private ImageInputStream iis;

    protected NativeImageReader(ImageReaderSpi originatingProvider, boolean canEncodeSigned) {
        super(originatingProvider);
        this.canEncodeSigned = canEncodeSigned;
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

        if (nType < 0 || (nType > ImageParameters.TYPE_BIT)) {
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
        PlanarImage img = getNativeImage(param);
        BufferedImage bufferedImage = ImageConversion.toBufferedImage(img);
        if (img != null) {
            img.release();
        }
        return bufferedImage;
    }

    private PlanarImage getNativeImage(ImageReadParam param) throws IOException {
        StreamSegment seg = StreamSegment.getStreamSegment(iis, param);
        ImageDescriptor desc = seg.getImageDescriptor();

        int dcmFlags =
            (canEncodeSigned && desc.isSigned()) ? Imgcodecs.DICOM_IMREAD_SIGNED : Imgcodecs.DICOM_IMREAD_UNSIGNED;
        if (ybr2rgb(desc.getPhotometricInterpretation())) {
            dcmFlags |= Imgcodecs.DICOM_IMREAD_YBR;
        }

        if (seg instanceof FileStreamSegment) {
            MatOfDouble positions = null;
            MatOfDouble lengths = null;
            try {
            positions = new MatOfDouble(ExtendSegmentedInputImageStream.getDoubleArray(seg.getSegPosition()));
            lengths = new MatOfDouble(ExtendSegmentedInputImageStream.getDoubleArray(seg.getSegLength()));
            return ImageCV.toImageCV(Imgcodecs.dicomJpgFileRead(((FileStreamSegment) seg).getFilePath(), positions,
                lengths, dcmFlags, Imgcodecs.IMREAD_UNCHANGED));
            } finally {
                closeMat(positions);
                closeMat(lengths);
            }
        } else if (seg instanceof MemoryStreamSegment) {
            Mat buf = null;
            try {
                ByteBuffer b = ((MemoryStreamSegment) seg).getCache();
                buf = new Mat(1, b.limit(), CvType.CV_8UC1);
                buf.put(0, 0, b.array());
                return ImageCV.toImageCV(Imgcodecs.dicomJpgMatRead(buf, dcmFlags, Imgcodecs.IMREAD_UNCHANGED));
            } finally {
                closeMat(buf);
            }
        }
        return null;
    }

    private boolean ybr2rgb(PhotometricInterpretation pmi) {
        // Preserve YBR for JPEG Lossless (1.2.840.10008.1.2.4.57, 1.2.840.10008.1.2.4.70)
        if (params.getJpegMarker() == 0xffc3) {
            return false;
        }
        switch (pmi) {
            case MONOCHROME1:
            case MONOCHROME2:
            case PALETTE_COLOR:
                return false;
            case RGB:
                // Force JPEG Baseline (1.2.840.10008.1.2.4.50) to YBR_FULL_422 color model when RGB with JFIF header
                // (error made by some constructors). RGB color model doesn't make sense for lossy jpeg with JFIF header.
                return params.isJFIF() && params.getJpegMarker() == 0xffc0;
        }
        return true;
    }

    public static void closeMat(Mat mat) {
        if (mat != null) {
            mat.release();
        }
    }

    public static SOFSegment getSOFSegment(ImageInputStream iis) throws IOException {
        iis.mark();
        try {
            boolean jfif = false;
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
                    return getSOF(iis, jfif, (byte1 << 8) + byte2);
                }
                // 0xffc0: // SOF_0: JPEG baseline
                // 0xffc1: // SOF_1: JPEG extended sequential DCT
                // 0xffc2: // SOF_2: JPEG progressive DCT
                // 0xffc3: // SOF_3: JPEG lossless sequential
                if ((byte2 >= 0xC0) && (byte2 <= 0xC3)) {
                    return getSOF(iis, jfif, (byte1 << 8) + byte2);
                }
                // 0xffc5: // SOF_5: differential (hierarchical) extended sequential, Huffman
                // 0xffc6: // SOF_6: differential (hierarchical) progressive, Huffman
                // 0xffc7: // SOF_7: differential (hierarchical) lossless, Huffman
                if ((byte2 >= 0xC5) && (byte2 <= 0xC7)) {
                    return getSOF(iis, jfif, (byte1 << 8) + byte2);
                }
                // 0xffc9: // SOF_9: extended sequential, arithmetic
                // 0xffca: // SOF_10: progressive, arithmetic
                // 0xffcb: // SOF_11: lossless, arithmetic
                if ((byte2 >= 0xC9) && (byte2 <= 0xCB)) {
                    return getSOF(iis, jfif, (byte1 << 8) + byte2);
                }
                // 0xffcd: // SOF_13: differential (hierarchical) extended sequential, arithmetic
                // 0xffce: // SOF_14: differential (hierarchical) progressive, arithmetic
                // 0xffcf: // SOF_15: differential (hierarchical) lossless, arithmetic
                if ((byte2 >= 0xCD) && (byte2 <= 0xCF)) {
                    return getSOF(iis, jfif, (byte1 << 8) + byte2);
                }
                if (byte2 == 0xE0) {
                    jfif = true;
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

    protected static SOFSegment getSOF(ImageInputStream iis, boolean jfif, int marker) throws IOException {
        readUnsignedShort(iis);
        int samplePrecision = readUnsignedByte(iis);
        int lines = readUnsignedShort(iis);
        int samplesPerLine = readUnsignedShort(iis);
        int componentsInFrame = readUnsignedByte(iis);
        return new SOFSegment(jfif, marker, samplePrecision, lines, samplesPerLine, componentsInFrame);
    }

    public ImageParameters buildImage(ImageInputStream iis) throws IOException {
        if (iis != null && params.getBytesPerLine() < 1) {
            SOFSegment sof = getSOFSegment(iis);
            if (sof != null) {
                params.setJFIF(sof.isJFIF());
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
