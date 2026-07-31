package org.dcm4che3.io;

import java.io.RandomAccessFile;

public interface RandomAccessFileProvider {
    RandomAccessFile getRandomAccessFile();
}
