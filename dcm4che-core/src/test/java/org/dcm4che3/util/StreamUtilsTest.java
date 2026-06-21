package org.dcm4che3.util;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.*;

public class StreamUtilsTest {

    @Test
    public void testReadAvailable() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        InputStream in = new ByteArrayInputStream(data);
        byte[] buf = new byte[3];
        assertEquals(3, StreamUtils.readAvailable(in, buf, 0, 3));
        assertArrayEquals(new byte[]{1, 2, 3}, buf);
        assertEquals(2, StreamUtils.readAvailable(in, buf, 0, 3));
        assertEquals(4, buf[0]);
        assertEquals(5, buf[1]);
    }

    @Test
    public void testReadFully() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        InputStream in = new ByteArrayInputStream(data);
        byte[] buf = new byte[5];
        StreamUtils.readFully(in, buf, 0, 5);
        assertArrayEquals(data, buf);
    }

    @Test(expected = EOFException.class)
    public void testReadFullyEOF() throws IOException {
        byte[] data = {1, 2, 3};
        InputStream in = new ByteArrayInputStream(data);
        byte[] buf = new byte[5];
        StreamUtils.readFully(in, buf, 0, 5);
    }

    @Test
    public void testSkipFully() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        InputStream in = new ByteArrayInputStream(data);
        StreamUtils.skipFully(in, 3);
        assertEquals(4, in.read());
    }

    @Test
    public void testCopy() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        InputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtils.copy(in, out);
        assertArrayEquals(data, out.toByteArray());
    }

    @Test
    public void testCopyLen() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        InputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtils.copy(in, out, 3);
        assertArrayEquals(new byte[]{1, 2, 3}, out.toByteArray());
    }

    @Test
    public void testNullOutputStream() throws IOException {
        OutputStream out = StreamUtils.nullOutputStream();
        out.write(1);
        out.write(new byte[10]);
        // Should not throw exception
    }
}
