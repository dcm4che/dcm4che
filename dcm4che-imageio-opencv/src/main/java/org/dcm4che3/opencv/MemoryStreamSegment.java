package org.dcm4che3.opencv;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import javax.imageio.stream.MemoryCacheImageInputStream;

import org.dcm4che3.imageio.codec.ImageDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MemoryStreamSegment extends StreamSegment {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryStreamSegment.class);

    private final ByteBuffer cache;

    MemoryStreamSegment(ByteBuffer b, ImageDescriptor imageDescriptor) {
        super(new long[] { 0 }, new long[] { b.limit() }, imageDescriptor);
        this.cache = b;
    }

    public ByteBuffer getCache() {
        return cache;
    }

    public static ByteArrayInputStream getByteArrayInputStream(MemoryCacheImageInputStream inputStream) {
        if (inputStream != null) {
            try {
                Field fid = MemoryCacheImageInputStream.class.getDeclaredField("stream");
                if (fid != null) {
                    fid.setAccessible(true);
                    return (ByteArrayInputStream) fid.get(inputStream);
                }
            } catch (Exception e) {
                LOGGER.error("Cannot get inputstream", e);
            }
        }
        return null;
    }
}
