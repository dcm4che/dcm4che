package org.dcm4che3.opencv;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageWriter;

public class NativeJLSLossyImageWriterSpi extends NativeJLSImageWriterSpi {

    public NativeJLSLossyImageWriterSpi() {
        super(NativeJLSLossyImageWriter.class);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Natively-accelerated JPEG-LS near-lossless Image Writer (CharLS based)";
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) throws IOException {
        return new NativeJLSLossyImageWriter(this);
    }
}
