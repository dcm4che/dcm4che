package org.dcm4che3.util;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CountingOutputStreamTest {

    @Test
    public void testCounting() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CountingOutputStream cos = new CountingOutputStream(baos);
        
        assertEquals(0, cos.getCount());
        
        cos.write(1);
        assertEquals(1, cos.getCount());
        
        byte[] data = {2, 3, 4};
        cos.write(data);
        assertEquals(4, cos.getCount());
        
        cos.write(data, 1, 1); // write '3'
        assertEquals(5, cos.getCount());
        
        assertArrayEquals(new byte[]{1, 2, 3, 4, 3}, baos.toByteArray());
    }
}
