package org.dcm4che.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {
    
    private static final int COPY_BUFFER_SIZE = 2048;
    
    public static void readFully(InputStream in, byte b[], int off, int len)
            throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
    }
    
    public static  void skipFully(InputStream in, long n) throws IOException {
        long remaining = n;
        while (remaining > 0) {
            long count = in.skip(remaining);
            if (count <= 0)
                throw new EOFException();
            remaining -= count;
        }
    }

    public static  void copy(InputStream in, OutputStream out, int len,
            byte buf[]) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            int count = in.read(buf, 0, Math.min(len - n, buf.length));
            if (count < 0)
                throw new EOFException();
            out.write(buf, 0, count);
            n += count;
        }
    }

    public static  void copy(InputStream in, OutputStream out, int len)
            throws IOException {
        copy(in, out, len, new byte[Math.min(len, COPY_BUFFER_SIZE)]);
    }

    public static void copy(InputStream in, OutputStream out, int len,
            int swapBytes, byte buf[]) throws IOException {
        if (!(swapBytes == 1 || swapBytes == 2 || swapBytes == 4))
            throw new IllegalArgumentException("swapBytes: " + swapBytes);
        if (len < 0 || (len % swapBytes) != 0)
            throw new IllegalArgumentException("length: " + len);
        int n = 0;
        int off = 0;
        while (n < len) {
            int count = in.read(buf, off, Math.min(len - n, buf.length - off));
            if (count < 0)
                throw new EOFException();
            n += count;
            count += off;
            off = count % swapBytes;
            count -= off;
            switch (swapBytes) {
            case 2:
                ByteUtils.swapShorts(buf, 0, count);
                break;
            case 4:
                ByteUtils.swapInts(buf, 0, count);
                break;
            case 8:
                ByteUtils.swapLongs(buf, 0, count);
                break;
            }
            out.write(buf, 0, count);
            if (off > 0)
                System.arraycopy(buf, count, buf, 0, off);
        }
    }

    public static void copy(InputStream in, OutputStream out, int len,
            int swapBytes) throws IOException {
        copy(in, out, len, swapBytes,
                new byte[Math.min(len, COPY_BUFFER_SIZE)]);
    }
}
