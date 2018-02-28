package org.dcm4che3.opencv;

import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
 
final class NativeJLSLossyImageWriter extends NativeJLSImageWriter {

    NativeJLSLossyImageWriter(ImageWriterSpi originatingProvider) throws IOException {
        super(originatingProvider);
    }

    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
        writeImage(streamMetadata, image, param, true);
    }
}
