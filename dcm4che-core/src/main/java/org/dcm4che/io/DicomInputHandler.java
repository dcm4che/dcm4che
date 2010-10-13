package org.dcm4che.io;

import java.io.IOException;

public interface DicomInputHandler {

    boolean readValue(DicomInputStream dicomInputStream) throws IOException;

}
