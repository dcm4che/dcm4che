package org.dcm4che3.imageio.stream;

import org.dcm4che3.io.PathProvider;
import org.dcm4che3.io.PathRandomAccessFile;
import org.dcm4che3.io.RandomAccessFileProvider;

import javax.imageio.stream.FileImageInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RAFFileImageInputStream extends FileImageInputStream implements RandomAccessFileProvider, PathProvider {
    private final PathRandomAccessFile raf;

    public RAFFileImageInputStream(File f) throws FileNotFoundException, IOException {
        this(new PathRandomAccessFile(f,"r"));
    }

    public RAFFileImageInputStream(PathRandomAccessFile raf) {
        super(raf);
        this.raf = raf;
    }

    @Override public RandomAccessFile getRandomAccessFile() {
        return raf;
    }

    @Override public String getPath() {
        return raf.getPath();
    }
}
