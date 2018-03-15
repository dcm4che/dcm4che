package org.dcm4che3.opencv;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;

import org.dcm4che3.imageio.codec.ImageDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FileStreamSegment extends StreamSegment {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileStreamSegment.class);

    private final String filePath;

    FileStreamSegment(File file, long[] startPos, long[] length, ImageDescriptor imageDescriptor) {
        super(startPos, length, imageDescriptor);
        this.filePath = file.getAbsolutePath();
    }

    FileStreamSegment(RandomAccessFile fdes, long[] startPos, long[] length, ImageDescriptor imageDescriptor) {
        super(startPos, length, imageDescriptor);
        this.filePath = getFilePath(fdes);
    }

    FileStreamSegment(ExtendSegmentedInputImageStream stream) {
        super(stream.getSegmentPositions(), stream.getSegmentLengths(), stream.getImageDescriptor());
        this.filePath = stream.getFile().getAbsolutePath();
    }

    public String getFilePath() {
        return filePath;
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
