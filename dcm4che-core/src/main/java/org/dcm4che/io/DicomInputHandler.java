package org.dcm4che.io;

import java.io.IOException;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Fragments;
import org.dcm4che.data.Sequence;

public interface DicomInputHandler {

    void readValue(DicomInputStream dis, Attributes attrs)
    throws IOException;

    void readValue(DicomInputStream dis, Sequence seq)
    throws IOException;

    void readValue(DicomInputStream dis, Fragments frags)
    throws IOException;
}
