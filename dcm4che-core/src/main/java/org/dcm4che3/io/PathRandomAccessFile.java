package org.dcm4che3.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class PathRandomAccessFile extends RandomAccessFile implements PathProvider {
    private final String path;

    public PathRandomAccessFile(File f, String mode) throws IOException {
        super(f,mode);
        this.path = f.getAbsolutePath();
    }

    @Override public String getPath() {
        return path;
    }
}
