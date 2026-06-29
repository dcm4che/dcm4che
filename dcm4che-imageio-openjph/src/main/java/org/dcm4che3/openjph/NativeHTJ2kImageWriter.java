/* ***** BEGIN LICENSE BLOCK *****
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
 * Portions created by the Initial Developer are Copyright (C) 2025
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
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.openjph;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.nio.ByteOrder;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

import org.dcm4che3.imageio.codec.BytesWithImageImageDescriptor;
import org.dcm4che3.imageio.codec.ImageDescriptor;

/**
 * HTJ2K image writer using OpenJPH native library via JNI.
 *
 * <p>Unlike the OpenCV-based writers, this writer extracts raw pixel bytes
 * directly from {@link RenderedImage} using pure Java AWT APIs, avoiding
 * any OpenCV/weasis-core-img dependency.</p>
 *
 * @since Feb 2025
 */
class NativeHTJ2kImageWriter extends ImageWriter {

    NativeHTJ2kImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public ImageWriteParam getDefaultWriteParam() {
        return new HTJ2kImageWriteParam(getLocale());
    }

    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
        if (output == null) {
            throw new IllegalStateException("output cannot be null");
        }
        if (!(output instanceof ImageOutputStream)) {
            throw new IllegalArgumentException("output is not an ImageOutputStream!");
        }
        ImageOutputStream stream = (ImageOutputStream) output;
        stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        HTJ2kImageWriteParam htj2kParams = (HTJ2kImageWriteParam) param;

        if (!(stream instanceof BytesWithImageImageDescriptor)) {
            throw new IllegalArgumentException("stream does not implement BytesWithImageImageDescriptor!");
        }
        ImageDescriptor desc = ((BytesWithImageImageDescriptor) stream).getImageDescriptor();

        RenderedImage renderedImage = image.getRenderedImage();

        try {
            int width = renderedImage.getWidth();
            int height = renderedImage.getHeight();
            int samples = desc.getSamples();
            int bitsStored = desc.getBitsCompressed();
            boolean signed = desc.isSigned();

            byte[] rawPixelData = extractPixelData(renderedImage, width, height, samples, bitsStored, signed);

            boolean reversible = htj2kParams.isCompressionLossless();
            float compressionRatio = reversible ? 0.0f : (float) htj2kParams.getCompressionRatiofactor();
            int progressionOrder = htj2kParams.getProgressionOrder();
            int decompositions = htj2kParams.getDecompositions();

            byte[] encoded = OpenJPH.encode(
                    rawPixelData,
                    width,
                    height,
                    samples,
                    bitsStored,
                    signed,
                    reversible,
                    compressionRatio,
                    progressionOrder,
                    decompositions);

            stream.write(encoded);
        } catch (OpenJPHException e) {
            throw new IIOException("Native HTJ2K encoding error", e);
        } catch (Throwable t) {
            throw new IIOException("Native HTJ2K encoding error", t);
        }
    }

    /**
     * Extract raw pixel data from a RenderedImage into a byte array
     * in pixel-interleaved layout suitable for OpenJPH.
     *
     * <p>Uses direct access to backing arrays when possible to avoid
     * unnecessary memory copies. Falls back to {@code getSamples()} for
     * non-standard raster layouts.</p>
     */
    private static byte[] extractPixelData(RenderedImage ri, int width, int height,
                                           int samples, int bitsStored, boolean signed) {
        // BufferedImage.getRaster() returns the live raster (no copy).
        // RenderedImage.getData() creates a full copy of the raster.
        Raster raster;
        if (ri instanceof BufferedImage) {
            raster = ((BufferedImage) ri).getRaster();
        } else {
            raster = ri.getData();
        }
        int dataType = raster.getDataBuffer().getDataType();

        if (dataType == DataBuffer.TYPE_BYTE) {
            return extractBytePixelData(raster, width, height, samples);
        } else if (dataType == DataBuffer.TYPE_USHORT || dataType == DataBuffer.TYPE_SHORT) {
            return extractShortPixelData(raster, width, height, samples);
        } else {
            throw new IllegalArgumentException("Unsupported DataBuffer type: " + dataType);
        }
    }

    private static byte[] extractBytePixelData(Raster raster, int width, int height, int samples) {
        int pixelCount = width * height;
        int totalBytes = pixelCount * samples;

        // Fast path: direct access to backing byte[] avoids int[] intermediates
        DataBuffer dataBuffer = raster.getDataBuffer();
        SampleModel sm = raster.getSampleModel();
        if (dataBuffer instanceof DataBufferByte && sm instanceof ComponentSampleModel) {
            ComponentSampleModel csm = (ComponentSampleModel) sm;
            byte[] bank = ((DataBufferByte) dataBuffer).getData();
            if (bank.length == totalBytes) {
                if (samples == 1
                        && csm.getPixelStride() == 1
                        && csm.getScanlineStride() == width) {
                    return bank;
                }
                if (samples > 1
                        && csm.getPixelStride() == samples
                        && csm.getScanlineStride() == width * samples
                        && hasSequentialBandOffsets(csm.getBandOffsets(), samples)) {
                    return bank;
                }
            }
        }

        // Fallback: getSamples() returns int[] (4x memory for 8-bit data)
        byte[] result = new byte[totalBytes];
        if (samples == 1) {
            int[] pixels = raster.getSamples(0, 0, width, height, 0, (int[]) null);
            for (int i = 0; i < pixels.length; i++) {
                result[i] = (byte) pixels[i];
            }
        } else {
            int[][] bands = new int[samples][];
            for (int b = 0; b < samples; b++) {
                bands[b] = raster.getSamples(0, 0, width, height, b, (int[]) null);
            }
            for (int i = 0; i < pixelCount; i++) {
                for (int b = 0; b < samples; b++) {
                    result[i * samples + b] = (byte) bands[b][i];
                }
            }
        }
        return result;
    }

    private static byte[] extractShortPixelData(Raster raster, int width, int height, int samples) {
        int pixelCount = width * height;
        byte[] result = new byte[pixelCount * samples * 2];

        // Fast path: convert directly from backing short[] to little-endian byte[]
        // Avoids the int[] intermediate from getSamples() (saves 2x memory per sample)
        DataBuffer dataBuffer = raster.getDataBuffer();
        SampleModel sm = raster.getSampleModel();
        if (sm instanceof ComponentSampleModel) {
            ComponentSampleModel csm = (ComponentSampleModel) sm;
            short[] bank = null;
            if (dataBuffer instanceof DataBufferUShort) {
                bank = ((DataBufferUShort) dataBuffer).getData();
            } else if (dataBuffer instanceof DataBufferShort) {
                bank = ((DataBufferShort) dataBuffer).getData();
            }
            if (bank != null) {
                boolean contiguous = samples == 1
                        ? csm.getPixelStride() == 1 && csm.getScanlineStride() == width
                        : csm.getPixelStride() == samples
                                && csm.getScanlineStride() == width * samples
                                && hasSequentialBandOffsets(csm.getBandOffsets(), samples);
                if (contiguous && bank.length == pixelCount * samples) {
                    for (int i = 0; i < bank.length; i++) {
                        int val = bank[i] & 0xFFFF;
                        result[i * 2] = (byte) val;
                        result[i * 2 + 1] = (byte) (val >>> 8);
                    }
                    return result;
                }
            }
        }

        // Fallback: getSamples() returns int[] (4 bytes per sample value)
        if (samples == 1) {
            int[] pixels = raster.getSamples(0, 0, width, height, 0, (int[]) null);
            for (int i = 0; i < pixels.length; i++) {
                int val = pixels[i];
                result[i * 2] = (byte) (val & 0xFF);
                result[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
            }
        } else {
            int[][] bands = new int[samples][];
            for (int b = 0; b < samples; b++) {
                bands[b] = raster.getSamples(0, 0, width, height, b, (int[]) null);
            }
            for (int i = 0; i < pixelCount; i++) {
                for (int b = 0; b < samples; b++) {
                    int val = bands[b][i];
                    int offset = (i * samples + b) * 2;
                    result[offset] = (byte) (val & 0xFF);
                    result[offset + 1] = (byte) ((val >> 8) & 0xFF);
                }
            }
        }
        return result;
    }

    private static boolean hasSequentialBandOffsets(int[] bandOffsets, int samples) {
        if (bandOffsets.length != samples) return false;
        for (int i = 0; i < samples; i++) {
            if (bandOffsets[i] != i) return false;
        }
        return true;
    }

    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }
}
