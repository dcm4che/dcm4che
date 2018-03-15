package org.dcm4che3.opencv;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public class NativeJLSImageWriterSpi extends ImageWriterSpi {

    public NativeJLSImageWriterSpi() {
        this(NativeJLSImageWriter.class);
    }

    public NativeJLSImageWriterSpi(Class<? extends NativeJLSImageWriter> writer) {
        super("Weasis Team", "1.0", NativeJLSImageReaderSpi.NAMES, NativeJLSImageReaderSpi.SUFFIXES,
            NativeJLSImageReaderSpi.MIMES, writer.getName(), new Class[] { ImageOutputStream.class },
            new String[] { NativeJLSImageReaderSpi.class.getName() }, false, null, null, null, null, false, null, null,
            null, null);
    }

    @Override
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        ColorModel colorModel = type.getColorModel();

        if (colorModel instanceof IndexColorModel) {
            // No need to check further: writer converts to 8-8-8 RGB.
            return true;
        }

        SampleModel sampleModel = type.getSampleModel();

        // Ensure all channels have the same bit depth
        int bitDepth;
        if (colorModel != null) {
            int[] componentSize = colorModel.getComponentSize();
            bitDepth = componentSize[0];
            for (int i = 1; i < componentSize.length; i++) {
                if (componentSize[i] != bitDepth) {
                    return false;
                }
            }
        } else {
            int[] sampleSize = sampleModel.getSampleSize();
            bitDepth = sampleSize[0];
            for (int i = 1; i < sampleSize.length; i++) {
                if (sampleSize[i] != bitDepth) {
                    return false;
                }
            }
        }

        // Ensure bitDepth is no more than 16
        if (bitDepth > 16) {
            return false;
        }

        // Check number of bands.
        int numBands = sampleModel.getNumBands();
        if (numBands < 1 || numBands > 4) {
            return false;
        }

        return true;
    }

    @Override
    public String getDescription(Locale locale) {
        return "Natively-accelerated JPEG-LS Image Writer (CharLS based)";
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) throws IOException {
        return new NativeJLSImageWriter(this);
    }
}
