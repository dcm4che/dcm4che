package org.dcm4che3.opencv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.junit.Test;

public class NativeImageReaderTest {

    @Test
    public void testGetSOFSegmentWithFillBytes() throws Exception {
        byte[] jpeg = {
                (byte) 0xFF, (byte) 0xD8,                           // SOI
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xE0, // fill bytes before APP0
                0x00, 0x04, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xC0,              // fill byte before SOF0
                0x00, 0x0B, 0x08, 0x00, 0x10, 0x00, 0x20, 0x01, 0x01, 0x11, 0x00
        };
        try (ImageInputStream iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(jpeg))) {
            SOFSegment sof = NativeImageReader.getSOFSegment(iis);
            assertNotNull(sof);
            assertEquals(0xFFC0, sof.getMarker());
            assertEquals(8, sof.getSamplePrecision());
            assertEquals(16, sof.getLines());
            assertEquals(32, sof.getSamplesPerLine());
            assertEquals(1, sof.getComponents());
        }
    }
}
