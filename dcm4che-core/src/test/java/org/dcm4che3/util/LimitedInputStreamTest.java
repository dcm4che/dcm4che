package org.dcm4che3.util;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class LimitedInputStreamTest {

    @Test
    public void testLimit() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        LimitedInputStream lis = new LimitedInputStream(new ByteArrayInputStream(data), 3, false);
        
        assertEquals(3, lis.getRemaining());
        assertEquals(1, lis.read());
        assertEquals(2, lis.getRemaining());
        
        byte[] buf = new byte[10];
        assertEquals(2, lis.read(buf));
        assertEquals(0, lis.getRemaining());
        
        assertEquals(-1, lis.read());
    }

    @Test
    public void testSkip() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        LimitedInputStream lis = new LimitedInputStream(new ByteArrayInputStream(data), 4, false);
        
        assertEquals(2, lis.skip(2));
        assertEquals(2, lis.getRemaining());
        assertEquals(3, lis.read());
        assertEquals(1, lis.getRemaining());
    }

    @Test
    public void testMarkReset() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        LimitedInputStream lis = new LimitedInputStream(new ByteArrayInputStream(data), 5, false);
        
        lis.read(); // 1
        lis.mark(10);
        
        lis.read(); // 2
        lis.read(); // 3
        assertEquals(2, lis.getRemaining());
        
        lis.reset();
        assertEquals(4, lis.getRemaining());
        assertEquals(2, lis.read());
    }

    @Test
    public void testCloseSource() throws IOException {
        final boolean[] sourceClosed = {false};
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[5]) {
            @Override
            public void close() throws IOException {
                sourceClosed[0] = true;
                super.close();
            }
        };
        
        LimitedInputStream lis1 = new LimitedInputStream(bais, 5, false);
        lis1.close();
        assertFalse(sourceClosed[0]);
        
        LimitedInputStream lis2 = new LimitedInputStream(bais, 5, true);
        lis2.close();
        assertTrue(sourceClosed[0]);
    }
}
