/*
 * **** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015-2018
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4che3.opencv;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageReadParam;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.dcm4che3.data.BulkData;
import org.dcm4che3.imageio.codec.BytesWithImageImageDescriptor;
import org.dcm4che3.imageio.codec.ImageDescriptor;
import org.dcm4che3.imageio.stream.SegmentedInputImageStream;
import org.dcm4che3.io.RandomAccessFileProvider;
import org.opencv.osgi.OpenCVNativeLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nicolas Roduit
 * @since Mar 2018
 */
public abstract class StreamSegment {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamSegment.class);

    static {
        // Load the native OpenCV library
        OpenCVNativeLoader loader = new OpenCVNativeLoader();
        loader.init();
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
        } else if (iis instanceof BytesWithImageImageDescriptor) {
            BytesWithImageImageDescriptor stream = (BytesWithImageImageDescriptor) iis;
            return new MemoryStreamSegment(stream.getBytes(), stream.getImageDescriptor());
        }
        throw new IllegalArgumentException("No stream adaptor found for " + iis.getClass().getName() + "!");
    }
    
    public static boolean supportsInputStream(Object iis) {
        // This list must reflect getStreamSegment()'s implementation
        return
            (iis instanceof ExtendSegmentedInputImageStream) ||
            (iis instanceof SegmentedInputImageStream) ||
            (iis instanceof BytesWithImageImageDescriptor)
        ;
    }

    private static StreamSegment getFileStreamSegment(SegmentedInputImageStream iis) {
        try {

                ImageInputStream fstream = iis.getStream();
                RandomAccessFile raf = null;
                if( fstream instanceof RandomAccessFileProvider) {
                    raf = ((RandomAccessFileProvider) fstream).getRandomAccessFile();
                    if( raf==null ) throw new NullPointerException("getRandomAccessFile on "+fstream+" returned null");
                } else if (fstream instanceof FileImageInputStream) {
                    // This will fail in later versions of Java
                    Field fRaf = FileImageInputStream.class.getDeclaredField("raf");
                    fRaf.setAccessible(true);
                    raf = (RandomAccessFile) fRaf.get(fstream);
                } else if (fstream instanceof FileCacheImageInputStream) {
                    // This will fail in later versions of Java
                    Field fRaf = FileCacheImageInputStream.class.getDeclaredField("cache");
                    fRaf.setAccessible(true);
                    raf = (RandomAccessFile) fRaf.get(fstream);
                }

                if (raf != null) {
                    long[][] seg = getSegments(iis);
                    if (seg != null) {
                        /*
                         * PS 3.5.8.2 Though a fragment may not contain encoded data from more than one frame, the
                         * encoded data from one frame may span multiple fragments. See note in Section 8.2.
                         */
                        return new FileStreamSegment(raf, seg[0], seg[1], iis.getImageDescriptor());
                    }
                }
                if (fstream instanceof MemoryCacheImageInputStream) {
                    MemoryCacheImageInputStream mstream = (MemoryCacheImageInputStream) fstream;
                    byte[] b = MemoryStreamSegment.getByte(MemoryStreamSegment.getByteArrayInputStream(mstream));
                    if (b != null) {
                        long[][] seg = getSegments(iis);
                        if (seg != null) {
                            int offset = (int) seg[0][0];
                            return new MemoryStreamSegment(
                                ByteBuffer.wrap(Arrays.copyOfRange(b, offset, offset + (int) seg[1][0])),
                                iis.getImageDescriptor());
                        }
                    }
                }
                LOGGER.error("Cannot read SegmentedInputImageStream with {} ", fstream.getClass());
        } catch (Exception e) {
            LOGGER.error("Building FileStreamSegment from SegmentedInputImageStream", e);
        }
        return null;
    }

    private static long[][] getSegments(SegmentedInputImageStream iis) throws IOException {
        Integer curSegment = iis.getCurSegment();
        if (curSegment != null && curSegment >= 0) {
            ImageDescriptor desc = iis.getImageDescriptor();
                List<Object> fragments = iis.getFragments();
                Integer lastSegment = iis.getLastSegment();
                if (!desc.isMultiframe() && lastSegment < fragments.size()) {
                    lastSegment = fragments.size();
                }
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
                return new long[][] { segPositions, segLength };
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
}
