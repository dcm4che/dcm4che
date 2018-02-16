package org.dcm4che3.opencv;

import java.io.IOException;

import javax.imageio.ImageReader;

import org.dcm4che3.imageio.plugins.dcm.DicomImageReaderSpi;

public class NativeDicomImageReaderSpi extends DicomImageReaderSpi {
    @Override
    public ImageReader createReaderInstance(Object extension)
            throws IOException {
        return new NativeDicomImageReader(this);
    }
}
