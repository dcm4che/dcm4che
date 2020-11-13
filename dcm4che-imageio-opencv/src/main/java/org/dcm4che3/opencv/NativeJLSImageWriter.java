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

import java.awt.image.DataBuffer;
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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.osgi.OpenCVNativeLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.op.ImageConversion;

/**
 * @author Nicolas Roduit
 * @since Mar 2018
 */
class NativeJLSImageWriter extends ImageWriter {
    static {
        // Load the native OpenCV library
        OpenCVNativeLoader loader = new OpenCVNativeLoader();
        loader.init();
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeJLSImageWriter.class);

    NativeJLSImageWriter(ImageWriterSpi originatingProvider) throws IOException {
        super(originatingProvider);
    }

    @Override
    public ImageWriteParam getDefaultWriteParam() {
        return new JPEGLSImageWriteParam(getLocale());
    }

    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
        if (output == null) {
            throw new IllegalStateException("input cannot be null");
        }

        if (!(output instanceof ImageOutputStream)) {
            throw new IllegalArgumentException("input is not an ImageInputStream!");
        }
        ImageOutputStream stream = (ImageOutputStream) output;
        stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        JPEGLSImageWriteParam jpegParams = (JPEGLSImageWriteParam) param;

        if (!(stream instanceof BytesWithImageImageDescriptor)) {
            throw new IllegalArgumentException("stream does not implement BytesWithImageImageDescriptor!");
        }
        ImageDescriptor desc = ((BytesWithImageImageDescriptor) stream).getImageDescriptor();
        PhotometricInterpretation pi = desc.getPhotometricInterpretation();

        if (jpegParams.isCompressionLossless() && (PhotometricInterpretation.YBR_FULL_422 == pi
            || PhotometricInterpretation.YBR_PARTIAL_422 == pi || PhotometricInterpretation.YBR_PARTIAL_420 == pi
            || PhotometricInterpretation.YBR_ICT == pi || PhotometricInterpretation.YBR_RCT == pi)) {
            throw new IllegalArgumentException(
                "True lossless encoder: Photometric interpretation is not supported: " + pi);
        }

        RenderedImage renderedImage = image.getRenderedImage();
        Mat buf = null;
        MatOfInt dicomParams = null;
        try {
            ImageCV mat = null;
            try {
                // Band interleaved mode (PlanarConfiguration = 1) is converted to pixel interleaved
                // So the input image has always a pixel interleaved mode mode((PlanarConfiguration = 0)
                mat = ImageConversion.toMat(renderedImage, param.getSourceRegion(), false);

                int jpeglsNLE = jpegParams.getNearLossless();
                int bitCompressed = desc.getBitsCompressed();
                int cvType = mat.type();
                int channels = CvType.channels(cvType);
                int epi = channels == 1 ? Imgcodecs.EPI_Monochrome2 : Imgcodecs.EPI_RGB;
                boolean signed = desc.isSigned();
                int dcmFlags = signed ? Imgcodecs.DICOM_FLAG_SIGNED : Imgcodecs.DICOM_FLAG_UNSIGNED;
                if(signed) {
                    LOGGER.warn("Force compression to JPEG-LS lossless as lossy is not adapted to signed data.");
                    jpeglsNLE = 0;
                    bitCompressed = 16; // Extend to bit allocated to avoid exception as negative values are treated as large positive values
                }
                // Specific case not well supported by jpeg and jpeg-ls encoder that reduce the stream to 8-bit
                if(bitCompressed == 8 && renderedImage.getSampleModel().getTransferType() != DataBuffer.TYPE_BYTE){
                  bitCompressed = 12;
                }

                int[] params = new int[16];
                params[Imgcodecs.DICOM_PARAM_IMREAD] = Imgcodecs.IMREAD_UNCHANGED; // Image flags
                params[Imgcodecs.DICOM_PARAM_DCM_IMREAD] = dcmFlags; // DICOM flags
                params[Imgcodecs.DICOM_PARAM_WIDTH] = mat.width(); // Image width
                params[Imgcodecs.DICOM_PARAM_HEIGHT] = mat.height(); // Image height
                params[Imgcodecs.DICOM_PARAM_COMPRESSION] = Imgcodecs.DICOM_CP_JPLS; // Type of compression
                params[Imgcodecs.DICOM_PARAM_COMPONENTS] = channels; // Number of components
                params[Imgcodecs.DICOM_PARAM_BITS_PER_SAMPLE] = bitCompressed; // Bits per sample
                params[Imgcodecs.DICOM_PARAM_INTERLEAVE_MODE] = Imgcodecs.ILV_SAMPLE; // Interleave mode
                params[Imgcodecs.DICOM_PARAM_COLOR_MODEL] = epi; // Photometric interpretation
                params[Imgcodecs.DICOM_PARAM_JPEGLS_LOSSY_ERROR] = jpeglsNLE; // Lossy error for jpeg-ls

                dicomParams = new MatOfInt(params);
                buf = Imgcodecs.dicomJpgWrite(mat, dicomParams, "");
                if (buf.empty()) {
                    throw new IIOException("Native JPEG-LS encoding error: null image");
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
            throw new IIOException("Native JPEG-LS encoding error", t);
        } finally {
            NativeImageReader.closeMat(dicomParams);
            NativeImageReader.closeMat(buf);
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
