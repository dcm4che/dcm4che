package org.dcm4che.io;

import java.io.IOException;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Fragments;
import org.dcm4che.data.Sequence;

public interface DicomInputHandler {

    boolean readValue(DicomInputStream dis, Attributes attrs)
    throws IOException;

    boolean readValue(DicomInputStream dis, Sequence seq)
    throws IOException;

    boolean readValue(DicomInputStream dis, Fragments frags)
    throws IOException;
}
