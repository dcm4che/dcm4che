package org.dcm4che3.util;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TeeInputStreamTest {

    @Test
    public void testTee() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TeeInputStream tis = new TeeInputStream(new ByteArrayInputStream(data), out);
        
        assertEquals(1, tis.read());
        byte[] buf = new byte[2];
        assertEquals(2, tis.read(buf));
        assertArrayEquals(new byte[]{2, 3}, buf);
        
        assertEquals(3, out.size());
        assertArrayEquals(new byte[]{1, 2, 3}, out.toByteArray());
        
        assertEquals(2, tis.read(buf, 0, 2));
        assertArrayEquals(new byte[]{4, 5}, buf);
        
        assertArrayEquals(data, out.toByteArray());
        assertEquals(-1, tis.read());
    }
}
