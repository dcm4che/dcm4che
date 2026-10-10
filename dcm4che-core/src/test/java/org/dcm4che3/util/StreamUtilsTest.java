package org.dcm4che3.util;

import org.junit.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;


public class StreamUtilsTest {

    @Test
    public void testReadAvailable() throws IOException {
        byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        byte[] buf = new byte[20];
        int read = StreamUtils.readAvailable(in, buf, 0, 20);
        assertEquals(data.length, read);
        assertArrayEquals(data, subset(buf, data.length));
    }

    @Test
    public void testReadFully() throws IOException {
        byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        byte[] buf = new byte[data.length];
        StreamUtils.readFully(in, buf, 0, data.length);
        assertArrayEquals(data, buf);
    }

    @Test(expected = EOFException.class)
    public void testReadFullyEOF() throws IOException {
        InputStream in = new ByteArrayInputStream("Short".getBytes());
        byte[] buf = new byte[10];
        StreamUtils.readFully(in, buf, 0, 10);
    }

    @Test
    public void testSkipFully() throws IOException {
        InputStream in = new ByteArrayInputStream("0123456789".getBytes());
        StreamUtils.skipFully(in, 5);
        assertEquals('5', in.read());
    }

    @Test
    public void testCopy() throws IOException {
        byte[] data = "Copy this".getBytes();
        InputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtils.copy(in, out);
        assertArrayEquals(data, out.toByteArray());
    }

    @Test
    public void testCopySwapBytes() throws IOException {
        byte[] data = {0, 1, 2, 3};
        InputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtils.copy(in, out, 4, 2);
        assertArrayEquals(new byte[]{1, 0, 3, 2}, out.toByteArray());
    }

    @Test
    public void testNullOutputStream() throws IOException {
        OutputStream out = StreamUtils.nullOutputStream();
        out.write(1);
        out.write(new byte[10]);
        out.flush();
        out.close();
    }

    private byte[] subset(byte[] b, int len) {
        byte[] res = new byte[len];
        System.arraycopy(b, 0, res, 0, len);
        return res;
    }
}
