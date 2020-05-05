package org.dcm4che3.io.stream;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.net.URI;

/**
 * URIs which match "java:iis" will return a shared image input stream.
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class DelegateImageInputStreamLoader implements ImageInputStreamLoader<URI> {

    private final ImageInputStream iis;

    public DelegateImageInputStreamLoader(ImageInputStream iis) {
        this.iis = iis;
    }

    @Override
    public ImageInputStream openStream(URI input) throws IOException {
        return new DelegateImageInputStream(iis);
    }
}
