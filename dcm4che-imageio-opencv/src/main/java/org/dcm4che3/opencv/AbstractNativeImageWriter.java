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

import java.awt.image.RenderedImage;
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

import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.imageio.codec.BytesWithImageImageDescriptor;
import org.dcm4che3.imageio.codec.ImageDescriptor;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.natives.NativeLibrary;

/**
 * Base class for the native (OpenCV-backed) DICOM image writers. It owns the boilerplate shared by every codec
 * (output validation, native {@code Mat} life cycle, error wrapping and the empty stream-metadata implementations);
 * subclasses only provide the codec name, the source-to-{@code Mat} conversion and the native encoding parameters.
 *
 * @author Nicolas Roduit
 * @since Mar 2018
 */
abstract class AbstractNativeImageWriter extends ImageWriter {

    static {
        NativeLibrary.loadLibraryFromLibraryName();
    }

    AbstractNativeImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    /** Human-readable codec name used in error messages (e.g. {@code "JPEG"}). */
    abstract String codecName();

    /** Converts the source image to the {@code Mat} layout (color model, signedness) expected by this codec. */
    abstract ImageCV toMat(RenderedImage image, ImageWriteParam param, ImageDescriptor desc);

    /** Builds the native DICOM encoding parameters for the converted {@code Mat}. */
    abstract MatOfInt buildDicomParams(ImageCV mat, RenderedImage image, ImageWriteParam param, ImageDescriptor desc);

    /** Codec-specific pre-encoding validation; does nothing by default. */
    void validate(ImageWriteParam param, ImageDescriptor desc) {
        // no-op
    }

    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
        ImageOutputStream stream = requireOutputStream();
        stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        ImageDescriptor desc = requireImageDescriptor(stream);
        validate(param, desc);

        RenderedImage renderedImage = image.getRenderedImage();
        Mat buf = null;
        MatOfInt dicomParams = null;
        try {
            ImageCV mat = null;
            try {
                mat = toMat(renderedImage, param, desc);
                dicomParams = buildDicomParams(mat, renderedImage, param, desc);
                buf = Imgcodecs.dicomJpgWrite(mat, dicomParams, "");
                if (buf.empty()) {
                    throw new IIOException("Native " + codecName() + " encoding error: null image");
                }
            } finally {
                if (mat != null) {
                    mat.release();
                }
            }
            byte[] bSrcData = new byte[buf.width() * buf.height() * (int) buf.elemSize()];
            buf.get(0, 0, bSrcData);
            stream.write(bSrcData);
        } catch (Throwable t) {
            throw new IIOException("Native " + codecName() + " encoding error", t);
        } finally {
            NativeImageReader.closeMat(dicomParams);
            NativeImageReader.closeMat(buf);
        }
    }

    private ImageOutputStream requireOutputStream() {
        if (output == null) {
            throw new IllegalStateException("output cannot be null");
        }
        if (!(output instanceof ImageOutputStream)) {
            throw new IllegalArgumentException("output is not an ImageOutputStream!");
        }
        return (ImageOutputStream) output;
    }

    private static ImageDescriptor requireImageDescriptor(ImageOutputStream stream) {
        if (!(stream instanceof BytesWithImageImageDescriptor)) {
            throw new IllegalArgumentException("stream does not implement BytesWithImageImageDescriptor!");
        }
        return ((BytesWithImageImageDescriptor) stream).getImageDescriptor();
    }

    /** Color model accepted by the true-lossless codecs: monochrome for one channel, otherwise RGB. */
    static int monochromeOrRgb(int channels) {
        return channels == 1 ? Imgcodecs.EPI_Monochrome2 : Imgcodecs.EPI_RGB;
    }

    static void rejectChromaSubsampledLossless(boolean lossless, PhotometricInterpretation pi) {
        if (lossless && (PhotometricInterpretation.YBR_FULL_422 == pi
            || PhotometricInterpretation.YBR_PARTIAL_422 == pi || PhotometricInterpretation.YBR_PARTIAL_420 == pi
            || PhotometricInterpretation.YBR_ICT == pi || PhotometricInterpretation.YBR_RCT == pi)) {
            throw new IllegalArgumentException(
                "True lossless encoder: Photometric interpretation is not supported: " + pi);
        }
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
