package org.dcm4che3.io.stream;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;


public interface ImageInputStreamLoader<T> {

    /**
     * Open an image input stream based on the indicated input location.
     */
    ImageInputStream openStream(T input) throws IOException;
}
