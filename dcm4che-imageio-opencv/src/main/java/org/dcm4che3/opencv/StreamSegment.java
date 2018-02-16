package org.dcm4che3.opencv;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.List;

import javax.imageio.ImageReadParam;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.dcm4che3.data.BulkData;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StreamSegment {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamSegment.class);

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    protected final long[] segPosition;
    protected final long[] segLength;

    StreamSegment(long[] startPos, long[] length) {
        this.segPosition = startPos;
        this.segLength = length;
    }

    public static StreamSegment getStreamSegment(ImageInputStream iis, ImageReadParam param) throws IOException {

        // // Works with DicomMediaIO
        // if (param instanceof ExtendImageParam) {
        // return new FileStreamSegment((ExtendImageParam) param);
        // }
        if (iis instanceof org.dcm4che3.imageio.stream.SegmentedInputImageStream
            || iis instanceof ExtendSegmentedInputImageStream) {
            try {
                boolean superClass = iis instanceof ExtendSegmentedInputImageStream;
                Class<? extends Object> clazz = superClass ? iis.getClass().getSuperclass() : iis.getClass();
                Field fStream = clazz.getDeclaredField("stream");
                Field fCurSegment = clazz.getDeclaredField("curSegment");
                if (fCurSegment != null && fStream != null) {
                    fCurSegment.setAccessible(true);
                    fStream.setAccessible(true);

                    FileImageInputStream fstream = (FileImageInputStream) fStream.get(iis);
                    Field fRaf = FileImageInputStream.class.getDeclaredField("raf");
                    if (fRaf != null) {
                        fRaf.setAccessible(true);
                        Integer curSegment = (Integer) fCurSegment.get(iis);
                        if (curSegment != null && curSegment >= 0) {
                            Field ffragments = clazz.getDeclaredField("fragments");
                            Field flastSegment = clazz.getDeclaredField("lastSegment");
                            if (ffragments != null && flastSegment != null) {
                                ffragments.setAccessible(true);
                                flastSegment.setAccessible(true);
                                List<Object> fragments = (List<Object>) ffragments.get(iis);
                                Integer lastSegment = (Integer) flastSegment.get(iis);
                                RandomAccessFile raf = (RandomAccessFile) fRaf.get(fstream);

                                long[] segPositions = new long[lastSegment - curSegment];
                                long[] segLength = new long[segPositions.length];
                                long beforePos = 0;

                                for (int i = curSegment; i < lastSegment; i++) {
                                    synchronized (fragments) {
                                        if (i < fragments.size()) {
                                            Object fragment = fragments.get(i);
                                            int k = i - curSegment;
                                            if (fragment instanceof BulkData) {
                                                BulkData bulk = (BulkData) fragment;
                                                segPositions[k] = bulk.offset();
                                                segLength[k] = bulk.length();
                                            } else {
                                                byte[] byteFrag = (byte[]) fragment;
                                                segPositions[k] = beforePos;
                                                segLength[k] = byteFrag.length;
                                            }
                                            beforePos += segLength[k] & 0xFFFFFFFFl;
                                        }
                                    }
                                }
                                /*
                                 * PS 3.5.8.2 Though a fragment may not contain encoded data from more than one frame,
                                 * the encoded data from one frame may span multiple fragments. See note in Section 8.2.
                                 */
                                return new FileStreamSegment(raf,
                                    FileStreamSegment.getFileIDfromFileDescriptor(raf.getFD()), segPositions,
                                    segLength);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error("getFileDescriptor from SegmentedInputImageStream", e); //$NON-NLS-1$
                } else {
                    LOGGER.error(e.getMessage());
                }
            }
        } else if (iis instanceof FileImageInputStream) {
            RandomAccessFile raf = FileStreamSegment.getRandomAccessFile((FileImageInputStream) iis);
            if (raf != null) {
                return new FileStreamSegment(raf, FileStreamSegment.getFileIDfromFileDescriptor(raf.getFD()),
                    new long[] { 0 }, new long[] { raf.length() });
            }
        } else if (iis instanceof FileCacheImageInputStream) {
            throw new IllegalArgumentException("No adaptor implemented yet for FileCacheImageInputStream");
        } else if (iis instanceof MemoryCacheImageInputStream) {
            ByteArrayInputStream stream =
                MemoryStreamSegment.getByteArrayInputStream((MemoryCacheImageInputStream) iis);
            if (stream != null) {
                byte[] b = getByte(stream);
                if (b != null) {
                return new MemoryStreamSegment(b);
                }
            }
            throw new IllegalArgumentException("No adaptor implemented for this type of MemoryCacheImageInputStream");
        }
        throw new IllegalArgumentException("No stream adaptor found for " + iis.getClass().getName() + "!");
    }

    public long[] getSegPosition() {
        return segPosition;
    }

    public long[] getSegLength() {
        return segLength;
    }

    public static byte[] getByte(ByteArrayInputStream inputStream) {
        if (inputStream != null) {
            try {
                Field fid = ByteArrayInputStream.class.getDeclaredField("buf");
                if (fid != null) {
                    fid.setAccessible(true);
                    return (byte[]) fid.get(inputStream);
                }
            } catch (Exception e) {
                LOGGER.error("Cannot get bytes from inputstream", e);
            }
        }
        return null;
    }

    public abstract ByteBuffer getDirectByteBuffer(int segment) throws IOException;

    public abstract ByteBuffer getDirectByteBuffer(int startSeg, int endSeg) throws IOException;

    /**
     * Java 9 introduces overridden methods with covariant return types for the following methods in
     * java.nio.ByteBuffer:
     * 
     * @see Buffer clear​()
     * @see Buffer flip​()
     * @see Buffer limit​(int newLimit)
     * @see Buffer mark​()
     * @see Buffer position​(int newPosition)
     * @see Buffer reset​()
     * @see Buffer rewind​()
     *
     *      In Java 9 they all now return ByteBuffer, whereas the methods they override return Buffer, resulting in
     *      exceptions like this when executing on Java 8 and lower: java.lang.NoSuchMethodError:
     *      java.nio.ByteBuffer.limit(I)Ljava/nio/ByteBuffer This is because the generated byte code includes the static
     *      return type of the method, which is not found on Java 8 and lower because the overloaded methods with
     *      covariant return types don't exist. The solution is to cast ByteBuffer instances to Buffer before calling
     *      the method.
     * 
     * @param buf
     *            is a ByteBuffer
     * @return Buffer
     */
    public static Buffer safeToBuffer(ByteBuffer buf) {
        // Explicit cast for compatibility with covariant return type on JDK 9's ByteBuffer
        return buf;
    }

}