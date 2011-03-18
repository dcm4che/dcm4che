package org.dcm4che.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class RAFInputStreamAdapter extends InputStream {

    private final RandomAccessFile raf;
    private long markedPos;
    private IOException markException;

    public RAFInputStreamAdapter(RandomAccessFile raf) {
        if (raf == null)
            throw new NullPointerException();
        this.raf = raf;
    }

    @Override
    public int read() throws IOException {
        return raf.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return raf.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return raf.skipBytes((int) n);
    }

    @Override
    public synchronized void mark(int readlimit) {
        try {
            this.markedPos = raf.getFilePointer();
            this.markException = null;
        } catch (IOException e) {
            this.markException = e;
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        if (markException != null)
            throw markException;
        raf.seek(markedPos);
    }

    @Override
    public boolean markSupported() {
        return true;
    }
}
