package org.dcm4che.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class RAFOutputStreamAdapter extends OutputStream {

    private final RandomAccessFile raf;

    public RAFOutputStreamAdapter(RandomAccessFile raf) {
        this.raf = raf;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        raf.write(b, off, len);
    }
    
    @Override
    public void write(int b) throws IOException {
        raf.write(b);
    }

}
