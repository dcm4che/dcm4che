package org.dcm4che3.io.stream;

import org.dcm4che3.error.TruncatedPixelDataException;
import org.junit.Test;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class BulkURIImageInputStreamTest {

    @Test
    public void close_EnsureUnderlyingIISClosed() throws IOException {
        MockImageInputStream mockIIS = new MockImageInputStream();
        BulkURIImageInputStream iis = new BulkURIImageInputStream(mockIIS, 0 ,1);
        iis.close();
        assertTrue("BulkURIImageInputStream must close it's underlying stream", mockIIS.isClosed);
    }

    @Test
    public void read_SegmentedByOffsetAndLength() throws URISyntaxException, IOException {
        URL codeIOD = this.getClass().getResource("/code-iod.xml");
        File file = Paths.get(codeIOD.toURI()).toFile();
        try (ImageInputStream iis = new BulkURIImageInputStream(new FileImageInputStream(file), 6, 7)) {
            byte[] buffer = new byte[30];
            assertEquals(7, iis.read(buffer));
            String substr = new String(buffer).trim();
            assertEquals("version",substr);
            assertEquals(-1,iis.read());
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void constructor_NegativeOffset_ThrowsIllegalArgumentException() throws IOException {
        MockImageInputStream iis = new MockImageInputStream();
        long offset = -5;
        int length = 5;
        new BulkURIImageInputStream(iis, offset, length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_NegativeLength_ThrowsIllegalArgumentException() throws IOException {
        MockImageInputStream iis = new MockImageInputStream();
        long offset = 0;
        int length = -1;
        new BulkURIImageInputStream(iis, offset, length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_StreamIsNull_ThrowsIllegalArgumentException() throws IOException {
        long offset = 5;
        int length = 50;
        new BulkURIImageInputStream(null, offset, length);
    }

    @Test(expected = TruncatedPixelDataException.class)
    public void read_EndsPrematurely_ThrowsTruncatedPixelDataException() throws IOException {
        long offset = 0;
        int length = 100;

        try(ImageInputStream iis = new BulkURIImageInputStream( new MockImageInputStream(), offset, length);) {
            byte[] buffer = new byte[30];
            iis.read(buffer);
        }
    }

    private class MockImageInputStream extends ImageInputStreamImpl {

        boolean isClosed = false;


        @Override
        public int read() throws IOException {
            return -1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return -1;
        }


        @Override
        public void close() throws IOException {
            super.close();
            this.isClosed = true;
        }
    }
}