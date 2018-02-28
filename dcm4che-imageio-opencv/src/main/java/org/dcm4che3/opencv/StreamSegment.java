package org.dcm4che3.opencv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.UID;
import org.dcm4che3.imageio.codec.ImageDescriptor;
import org.dcm4che3.imageio.codec.Transcoder;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLSImageInputStream;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReaderSpi;
import org.dcm4che3.imageio.stream.EncapsulatedPixelDataImageInputStream;
import org.dcm4che3.imageio.stream.SegmentedInputImageStream;
import org.dcm4che3.io.DicomInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StreamSegment {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamSegment.class);

    static {
        // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary("opencv_java340");
    }

    private final long[] segPosition;
    private final long[] segLength;
    private final ImageDescriptor imageDescriptor;

    StreamSegment(long[] startPos, long[] length, ImageDescriptor imageDescriptor) {
        this.segPosition = startPos;
        this.segLength = length;
        this.imageDescriptor = imageDescriptor;
    }

    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    public static StreamSegment getStreamSegment(ImageInputStream iis, ImageReadParam param) throws IOException {

        if (iis instanceof ExtendSegmentedInputImageStream) {
            return new FileStreamSegment((ExtendSegmentedInputImageStream) iis);
        } else if (iis instanceof SegmentedInputImageStream) {
            return getFileStreamSegment((SegmentedInputImageStream) iis);
        } else if (iis instanceof FileCacheImageInputStream) {
            throw new IllegalArgumentException("No adaptor implemented yet for FileCacheImageInputStream");
        } else if (iis instanceof EncapsulatedPixelDataImageInputStream) {
            EncapsulatedPixelDataImageInputStream stream = (EncapsulatedPixelDataImageInputStream) iis;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            stream.transferTo(out);
            return new MemoryStreamSegment(out.toByteArray(), stream.getImageDescriptor());
        } else if (iis instanceof PatchJPEGLSImageInputStream) {
            PatchJPEGLSImageInputStream stream = (PatchJPEGLSImageInputStream) iis;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            stream.transferTo(out);
            return new MemoryStreamSegment(out.toByteArray(), stream.getImageDescriptor());
        }
        throw new IllegalArgumentException("No stream adaptor found for " + iis.getClass().getName() + "!");
    }

    private static FileStreamSegment getFileStreamSegment(SegmentedInputImageStream iis) {
        try {
            Class<? extends ImageInputStream> clazz = iis.getClass();
            Field fStream = clazz.getDeclaredField("stream");
            Field fCurSegment = clazz.getDeclaredField("curSegment");
            if (fCurSegment != null && fStream != null) {
                fCurSegment.setAccessible(true);
                fStream.setAccessible(true);

                ImageInputStream fstream = (ImageInputStream) fStream.get(iis);
                Field fRaf = FileImageInputStream.class.getDeclaredField("raf");
                if (fRaf != null && fstream instanceof FileImageInputStream) {
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
                             * PS 3.5.8.2 Though a fragment may not contain encoded data from more than one frame, the
                             * encoded data from one frame may span multiple fragments. See note in Section 8.2.
                             */
                            return new FileStreamSegment(raf, segPositions, segLength, iis.getImageDescriptor());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Building FileStreamSegment from SegmentedInputImageStream", e);
        }
        return null;
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

    private static ExtendSegmentedInputImageStream buildSegmentedImageInputStream(File file, int frameIndex,
        boolean jpeg2000, Fragments pixelDataFragments, ImageDescriptor imageDescriptor) throws IOException {
        long[] offsets;
        int[] length;
        int frames = imageDescriptor.getFrames();
        ArrayList<Integer> fragmentsPositions = new ArrayList<>();

        int nbFragments = pixelDataFragments.size();

        if (frames >= nbFragments - 1) {
            // nbFrames > nbFragments should never happen
            offsets = new long[1];
            length = new int[offsets.length];
            int index = frameIndex < nbFragments - 1 ? frameIndex + 1 : nbFragments - 1;
            BulkData bulkData = (BulkData) pixelDataFragments.get(index);
            offsets[0] = bulkData.offset();
            length[0] = bulkData.length();
        } else {
            if (frames == 1) {
                offsets = new long[nbFragments - 1];
                length = new int[offsets.length];
                for (int i = 0; i < length.length; i++) {
                    BulkData bulkData = (BulkData) pixelDataFragments.get(i + frameIndex + 1);
                    offsets[i] = bulkData.offset();
                    length[i] = bulkData.length();
                }
            } else {
                // Multi-frames where each frames can have multiple fragments.
                if (fragmentsPositions.isEmpty()) {
                    try (ImageInputStream srcStream = ImageIO.createImageInputStream(file)) {
                        for (int i = 1; i < nbFragments; i++) {
                            BulkData bulkData = (BulkData) pixelDataFragments.get(i);
                            ImageInputStream stream = new org.dcm4che3.imageio.stream.SegmentedInputImageStream(
                                srcStream, bulkData.offset(), bulkData.length(), frames <= 1);
                            if (jpeg2000 ? decodeJpeg2000(stream) : decodeJpeg(stream)) {
                                fragmentsPositions.add(i);
                            }
                        }
                    }
                }

                if (fragmentsPositions.size() == frames) {
                    int start = fragmentsPositions.get(frameIndex);
                    int end = (frameIndex + 1) >= fragmentsPositions.size() ? nbFragments
                        : fragmentsPositions.get(frameIndex + 1);

                    offsets = new long[end - start];
                    length = new int[offsets.length];
                    for (int i = 0; i < offsets.length; i++) {
                        BulkData bulkData = (BulkData) pixelDataFragments.get(start + i);
                        offsets[i] = bulkData.offset();
                        length[i] = bulkData.length();
                    }
                } else {
                    throw new IOException("Cannot match all the fragments to all the frames!"); //$NON-NLS-1$
                }
            }
        }

        return new ExtendSegmentedInputImageStream(file, offsets, length, imageDescriptor);
    }

    private static boolean decodeJpeg2000(ImageInputStream iis) throws IOException {
        iis.mark();
        try {
            int marker = (iis.read() << 8) | iis.read();

            if (marker == 0xFF4F) {
                return true;
            }

            iis.reset();
            iis.mark();
            byte[] b = new byte[12];
            iis.readFully(b);

            // Verify the signature box
            // The length of the signature box is 12
            if (b[0] != 0 || b[1] != 0 || b[2] != 0 || b[3] != 12) {
                return false;
            }

            // The signature box type is "jP "
            if ((b[4] & 0xff) != 0x6A || (b[5] & 0xFF) != 0x50 || (b[6] & 0xFF) != 0x20 || (b[7] & 0xFF) != 0x20) {
                return false;
            }

            // The signature content is 0x0D0A870A
            if ((b[8] & 0xFF) != 0x0D || (b[9] & 0xFF) != 0x0A || (b[10] & 0xFF) != 0x87 || (b[11] & 0xFF) != 0x0A) {
                return false;
            }

            return true;
        } finally {
            iis.reset();
        }
    }

    private static boolean decodeJpeg(ImageInputStream iis) throws IOException {
        // jpeg and jpeg-ls
        iis.mark();
        try {
            int byte1 = iis.read();
            int byte2 = iis.read();
            // Magic numbers for JPEG (general jpeg marker)
            if ((byte1 != 0xFF) || (byte2 != 0xD8)) {
                return false;
            }
            do {
                byte1 = iis.read();
                byte2 = iis.read();
                // Something wrong, but try to read it anyway
                if (byte1 != 0xFF) {
                    break;
                }
                // Start of scan
                if (byte2 == 0xDA) {
                    break;
                }
                // Start of Frame, also known as SOF55, indicates a JPEG-LS file.
                if (byte2 == 0xF7) {
                    return true;
                }
                // 0xffc0: // SOF_0: JPEG baseline
                // 0xffc1: // SOF_1: JPEG extended sequential DCT
                // 0xffc2: // SOF_2: JPEG progressive DCT
                // 0xffc3: // SOF_3: JPEG lossless sequential
                if ((byte2 >= 0xC0) && (byte2 <= 0xC3)) {
                    return true;
                }
                // 0xffc5: // SOF_5: differential (hierarchical) extended sequential, Huffman
                // 0xffc6: // SOF_6: differential (hierarchical) progressive, Huffman
                // 0xffc7: // SOF_7: differential (hierarchical) lossless, Huffman
                if ((byte2 >= 0xC5) && (byte2 <= 0xC7)) {
                    return true;
                }
                // 0xffc9: // SOF_9: extended sequential, arithmetic
                // 0xffca: // SOF_10: progressive, arithmetic
                // 0xffcb: // SOF_11: lossless, arithmetic
                if ((byte2 >= 0xC9) && (byte2 <= 0xCB)) {
                    return true;
                }
                // 0xffcd: // SOF_13: differential (hierarchical) extended sequential, arithmetic
                // 0xffce: // SOF_14: differential (hierarchical) progressive, arithmetic
                // 0xffcf: // SOF_15: differential (hierarchical) lossless, arithmetic
                if ((byte2 >= 0xCD) && (byte2 <= 0xCF)) {
                    return true;
                }
                int length = iis.read() << 8;
                length += iis.read();
                length -= 2;
                while (length > 0) {
                    length -= iis.skipBytes(length);
                }
            } while (true);
            return true;
        } finally {
            iis.reset();
        }
    }

    public static void main(String[] args) throws IOException {
        IIORegistry.getDefaultInstance().registerServiceProvider(new DicomImageReaderSpi());
        IIORegistry.getDefaultInstance().registerServiceProvider(new NativeJLSImageWriterSpi());

        final File ifile = new File("/home/nicolas/Data/Pictures/ImagesTest/DICOM/Compression and TSUID/jai-jpls-issue/JAI_JLSL_NOT_PATCHED.dcm");
        final File ofile = new File("/home/nicolas/Data/Pictures/out/a-jpglsnonpatch.dcm");
        Transcoder.Handler handler = new Transcoder.Handler() {
            @Override
            public OutputStream newOutputStream(Transcoder transcoder, Attributes dataset) throws IOException {
                transcoder.setDestinationTransferSyntax(UID.ExplicitVRLittleEndian);
                return new FileOutputStream(ofile);
            }
        };
        try (Transcoder transcoder = new Transcoder(ifile)) {
            transcoder.setIncludeFileMetaInformation(true);
            transcoder.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            transcoder.transcode(handler);
        }
    }
}