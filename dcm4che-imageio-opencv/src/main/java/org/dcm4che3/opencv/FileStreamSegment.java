package org.dcm4che3.opencv;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FileStreamSegment extends StreamSegment {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileStreamSegment.class);

    private final RandomAccessFile file;
    private final int fileID;

    FileStreamSegment(RandomAccessFile fdes, int fileID, long[] startPos, long[] length) {
        super(startPos, length);
        this.file = fdes;
        this.fileID = fileID;
    }

//    public FileStreamSegment(ExtendImageParam param) throws FileNotFoundException {
//        super(param.getSegmentPositions(), param.getSegmentLengths());
//        this.file = new RandomAccessFile(param.getFile(), "r");
//        this.fileID = -1;
//    }

    public RandomAccessFile getFile() {
        return file;
    }
    
    public String getFilePath() {
        return getFilePath(file);
    }
    
    public int getFileID() {
        return fileID;
    }

    public static int getFileIDfromFileDescriptor(FileDescriptor fileDS) {
        if (fileDS != null && fileDS.valid()) {
            try {
                Field fid = FileDescriptor.class.getDeclaredField("fd");
                if (fid != null) {
                    fid.setAccessible(true);
                    return fid.getInt(fileDS);
                }
            } catch (Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error("getFileIDfromFileDescriptor exception", e); //$NON-NLS-1$
                } else {
                    LOGGER.error(e.getMessage());
                }
            }
        }
        // not valid id
        return -1;
    }

    @Override
    public ByteBuffer getDirectByteBuffer(int segment) throws IOException {
        if (segPosition == null || segPosition.length <= segment || segLength == null || segLength.length <= segment) {
            throw new IllegalArgumentException("Invalid position of the file to read!");
        }
        MappedByteBuffer buffer =
            file.getChannel().map(FileChannel.MapMode.READ_ONLY, segPosition[segment], segLength[segment]);
        // For performance reason, native order is preferred.
        buffer.order(ByteOrder.nativeOrder());
        return buffer;
    }

    @Override
    public ByteBuffer getDirectByteBuffer(int startSeg, int endSeg) throws IOException {
        if (segPosition == null || segPosition.length <= endSeg || segLength == null || segLength.length <= endSeg) {
            throw new IllegalArgumentException("Invalid position of the file to read!");
        }
        long length = segLength[startSeg];

        // DICOM PS 3.5.8.2 Handle frame with multiple fragments
        if (startSeg < endSeg) {
            for (int i = startSeg + 1; i <= endSeg; i++) {
                length += segLength[i];
            }
            ByteBuffer buffer = ByteBuffer.allocateDirect((int) length);
            buffer.order(ByteOrder.nativeOrder());
            for (int i = startSeg; i <= endSeg; i++) {
                buffer.put(file.getChannel().map(FileChannel.MapMode.READ_ONLY, segPosition[i], segLength[i]));
            }

            StreamSegment.safeToBuffer(buffer).rewind();
            return buffer;
        }

        MappedByteBuffer buffer = file.getChannel().map(FileChannel.MapMode.READ_ONLY, segPosition[startSeg], length);
        buffer.order(ByteOrder.nativeOrder());
        return buffer;
    }

    public static String getFilePath(RandomAccessFile file) {
        try {
            Field fpath = RandomAccessFile.class.getDeclaredField("path");
            if (fpath != null) {
                fpath.setAccessible(true);
                return (String) fpath.get(file);
            }
        } catch (Exception e) {
            LOGGER.error("get path from RandomAccessFile", e); //$NON-NLS-1$
        }
        return null;
    }
    
    public static RandomAccessFile getRandomAccessFile(FileImageInputStream fstream) {
        try {
            Field fRaf = FileImageInputStream.class.getDeclaredField("raf");
            if (fRaf != null) {
                fRaf.setAccessible(true);
                return (RandomAccessFile) fRaf.get(fstream);
            }
        } catch (Exception e) {
            LOGGER.error("getFileDescriptor from FileImageInputStream", e); //$NON-NLS-1$
        }
        return null;
    }

    public static RandomAccessFile getRandomAccessFile(FileImageOutputStream fstream) {
        try {
            Field fRaf = FileImageOutputStream.class.getDeclaredField("raf");
            if (fRaf != null) {
                fRaf.setAccessible(true);
                return (RandomAccessFile) fRaf.get(fstream);
            }
        } catch (Exception e) {
            LOGGER.error("getFileDescriptor from FileImageOutputStream", e); //$NON-NLS-1$
        }
        return null;
    }
}
