package org.dcm4che3.util;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class CountingInputStreamTest {

    @Test
    public void testCounting() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        CountingInputStream cis = new CountingInputStream(new ByteArrayInputStream(data));
        
        assertEquals(0, cis.getCount());
        
        assertEquals(1, cis.read());
        assertEquals(1, cis.getCount());
        
        byte[] buf = new byte[2];
        assertEquals(2, cis.read(buf));
        assertEquals(3, cis.getCount());
        
        assertEquals(1, cis.skip(1));
        assertEquals(4, cis.getCount());
        
        assertEquals(5, cis.read());
        assertEquals(5, cis.getCount());
        
        assertEquals(-1, cis.read());
        assertEquals(5, cis.getCount());
    }

    @Test
    public void testMarkReset() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        CountingInputStream cis = new CountingInputStream(new ByteArrayInputStream(data));
        
        cis.read(); // 1
        cis.mark(10);
        long markCount = cis.getCount();
        assertEquals(1, markCount);
        
        cis.read(); // 2
        cis.read(); // 3
        assertEquals(3, cis.getCount());
        
        cis.reset();
        assertEquals(markCount, cis.getCount());
        assertEquals(2, cis.read());
    }
}
