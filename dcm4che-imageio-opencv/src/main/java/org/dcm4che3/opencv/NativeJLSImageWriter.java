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

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.op.ImageConversion;

class NativeJLSImageWriter extends ImageWriter {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    private ImageOutputStream stream = null;

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
        stream = (ImageOutputStream) output;
        stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        RenderedImage renderedImage = image.getRenderedImage();

        // Throws exception if the renderedImage cannot be encoded.
        // ImageUtil.canEncodeImage(this, renderedImage.getColorModel(), renderedImage.getSampleModel());

        // if (renderedImage.getColorModel() instanceof IndexColorModel) {
        // renderedImage = convertTo3BandRGB(renderedImage);
        // }

        try {
            ImageCV mat = ImageConversion.toMat(renderedImage, param.getSourceRegion(), false);

            int cvType = mat.type();
            int elemSize = (int) mat.elemSize1();
            int channels = CvType.channels(cvType);
            // TODO implement interleaved mode
            int dcmFlags =
                CvType.depth(cvType) == CvType.CV_16S ? Imgcodecs.DICOM_IMREAD_SIGNED : Imgcodecs.DICOM_IMREAD_UNSIGNED;
            MatOfInt dicomParams =
                new MatOfInt(Imgcodecs.IMREAD_UNCHANGED, dcmFlags, mat.width(), mat.height(), Imgcodecs.DICOM_CP_JPLS,
                    channels, elemSize * 8, Imgcodecs.ILV_SAMPLE, mat.width() * elemSize,
                        param instanceof JPEGLSImageWriteParam ? ((JPEGLSImageWriteParam) param).getNearLossless() : 0);
            Mat buf = Imgcodecs.dicomJpgWrite(mat, dicomParams, "");
            if (buf.empty()) {
                throw new IIOException("Native JPEG-LS encoding error: null image");
            }

            byte[] bSrcData = new byte[buf.width() * buf.height() * (int) buf.elemSize()];
            buf.get(0, 0, bSrcData);
            stream.write(bSrcData);

        } catch (Throwable t) {
            throw new IIOException("Native JPEG-LS encoding error", t);
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
