package org.dcm4che3.opencv;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public class NativeJLSImageReaderSpi extends ImageReaderSpi {

    static final String[] NAMES = { "jpeg-ls-cv", "jpeg-ls", "JPEG-LS" };
    static final String[] SUFFIXES = { "jls" };
    static final String[] MIMES = { "image/jpeg-ls" };

    public NativeJLSImageReaderSpi() {
        super("Weasis Team", "1.0", NAMES, SUFFIXES, MIMES, NativeImageReader.class.getName(),
            new Class[] { ImageInputStream.class }, new String[] {NativeJLSImageWriterSpi.class.getName()}, false, // supportsStandardStreamMetadataFormat
            null, // nativeStreamMetadataFormatName
            null, // nativeStreamMetadataFormatClassName
            null, // extraStreamMetadataFormatNames
            null, // extraStreamMetadataFormatClassNames
            false, // supportsStandardImageMetadataFormat
            null, null, null, null);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Natively-accelerated JPEG-LS Image Reader (CharLS based)";
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        if (!(source instanceof ImageInputStream)) {
            return false;
        }
        ImageInputStream iis = (ImageInputStream) source;
        iis.mark();
        int byte1 = iis.read();
        int byte2 = iis.read();
        int byte3 = iis.read();
        int byte4 = iis.read();
        iis.reset();
        // Magic numbers for JPEG (general jpeg marker): 0xFFD8
        // Start of Frame, also known as SOF55, indicates a JPEG-LS file
        if ((byte1 == 0xFF) && (byte2 == 0xD8) && (byte3 == 0xFF) && (byte4 == 0xF7)) {
            return true;
        }
        return false;
    }

    @Override
    public ImageReader createReaderInstance(Object extension) throws IIOException {
        return new NativeImageReader(this);
    }
}
