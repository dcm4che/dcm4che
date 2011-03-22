package org.dcm4che.media;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.io.RAFInputStreamAdapter;
import org.dcm4che.util.IntHashMap;

public class DicomDirReader {

    private final File file;
    private final RandomAccessFile raf;
    private final DicomInputStream dis;
    private final Attributes fsInfo;
    private final long dirRecSeqPos;
    private final IntHashMap<Attributes> cache = new IntHashMap<Attributes>();

    public DicomDirReader(File file) throws IOException {
        this.file = file;
        this.raf = new RandomAccessFile(file, "r");
        this.dis = new DicomInputStream(new RAFInputStreamAdapter(raf));
        this.fsInfo = dis.readDataset(-1, Tag.DirectoryRecordSequence);
        this.dirRecSeqPos = dis.getPosition();
    }

    public final Attributes getFileMetaInformation() {
        try {
            return dis.getFileMetaInformation();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public final Attributes getFilesetInformation() {
        return fsInfo;
    }

    public void close() throws IOException {
        raf.close();
    }

}
