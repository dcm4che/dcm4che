/* ***** BEGIN LICENSE BLOCK *****
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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2013
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
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.imageio.plugins.rle;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che3.util.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class RLEImageReader extends ImageReader {

    private static Logger LOG = LoggerFactory.getLogger(RLEImageReader.class);

    private static final String UNKNOWN_IMAGE_TYPE =
            "RLE Image Reader needs ImageReadParam.destination or "
            + "ImageReadParam.destinationType specified";
    private static final String UNSUPPORTED_DATA_TYPE =
            "Unsupported Data Type of ImageReadParam.destination or "
            + "ImageReadParam.destinationType: ";
    private static final String MISMATCH_NUM_RLE_SEGMENTS =
            "Number of RLE Segments does not match image type: ";

    private final int[] header = new int[16];

    private final byte[] buf = new byte[8192];

    private long headerPos;

    private long bufOff;

    private int bufPos;

    private int bufLen;

    private ImageInputStream iis;

    private int width;

    private int height;

    protected RLEImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly,
            boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        resetInternalState();
        iis = (ImageInputStream) input;
    }

    private void resetInternalState() {
        width = 0;
        height = 0;
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        return 1;
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        return width;
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        return height;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex)
            throws IOException {
        return null;
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }


    @Override
    public boolean canReadRaster() {
        return true;
    }

    @Override
    public Raster readRaster(int imageIndex, ImageReadParam param)
            throws IOException {
        checkIndex(imageIndex);

        WritableRaster raster = getDestinationRaster(param);
        read(raster.getDataBuffer());
        return raster;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param)
            throws IOException {
        checkIndex(imageIndex);

        BufferedImage bi = getDestination(param);
        read(bi.getRaster().getDataBuffer());
        return bi;
    }

    private void checkIndex(int imageIndex) {
        if (imageIndex != 0)
            throw new IndexOutOfBoundsException("imageIndex: " + imageIndex);
    }

    private BufferedImage getDestination(ImageReadParam param) {
        if (param == null)
            throw new IllegalArgumentException(UNKNOWN_IMAGE_TYPE);

        BufferedImage bi = param.getDestination();
        if (bi != null) {
            width = bi.getWidth();
            height = bi.getHeight();
            return bi;
        }
        
        ImageTypeSpecifier imageType = param.getDestinationType();
        if (imageType != null) {
            SampleModel sm = imageType.getSampleModel();
            width = sm.getWidth();
            height = sm.getHeight();
            return imageType.createBufferedImage(width, height);
        }
        throw new IllegalArgumentException(UNKNOWN_IMAGE_TYPE);
    }

    private WritableRaster getDestinationRaster(ImageReadParam param) {
        if (param == null)
            throw new IllegalArgumentException(UNKNOWN_IMAGE_TYPE);

        BufferedImage bi = param.getDestination();
        if (bi != null) {
            width = bi.getWidth();
            height = bi.getHeight();
            return bi.getRaster();
        }

        ImageTypeSpecifier imageType = param.getDestinationType();
        if (imageType != null) {
            SampleModel sm = imageType.getSampleModel();
            width = sm.getWidth();
            height = sm.getHeight();
            return Raster.createWritableRaster(sm, null);
        }
        throw new IllegalArgumentException(UNKNOWN_IMAGE_TYPE);
    }

    private void read(DataBuffer db) throws IOException {
        switch (db.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            read(((DataBufferByte) db).getBankData());
            break;
        case DataBuffer.TYPE_USHORT:
            read(((DataBufferUShort) db).getData());
            break;
        case DataBuffer.TYPE_SHORT:
            read(((DataBufferShort) db).getData());
            break;
        default:
            throw new IllegalArgumentException(
                    UNSUPPORTED_DATA_TYPE + db.getDataType());
        }
    }

    private void read(byte[][] bands) throws IOException {
        readRLEHeader(bands.length);
        for (int i = 0; i < bands.length; i++)
            unrle(i+1, bands[i]);
    }

    private void read(short[] data) throws IOException {
        readRLEHeader(2);
        Arrays.fill(data, (short) 0);
        unrle(1, data);
        unrle(2, data);
    }

    private void seekSegment(int seg) throws IOException {
        long streamPos = headerPos + (header[seg] & 0xffffffffL);
        int bufPos = (int) (streamPos - bufOff);
        if (bufPos >= 0 && bufPos <= bufLen)
            this.bufPos = bufPos;
        else {
            iis.seek(streamPos);
            this.bufPos = bufLen; // force fillBuffer on nextByte()
        }
    }


    private void readRLEHeader(int numSegments) throws IOException {
        fillBuffer();
        if (bufLen < 64)
            throw new EOFException();
        for (int i = 0, off = 0; i < header.length; i++, off += 4)
            header[i] = ByteUtils.bytesToIntLE(buf, off);
        bufPos = 64;
        if (header[0] != numSegments)
            throw new IOException(MISMATCH_NUM_RLE_SEGMENTS + header[0]);
    }

    private void unrle(int seg, byte[] data) throws IOException {
        seekSegment(seg);
        int pos = 0;
        try {
            int n;
            int end;
            byte val;
            while (pos < data.length) {
                n = nextByte();
                if (n >= 0) {
                    read(data, pos, ++n);
                    pos += n;
                } else if (n != -128) {
                    end = pos + 1 - n;
                    val = nextByte();
                    while (pos < end)
                        data[pos++] = val;
                }
            }
        } catch (EOFException e) {
            LOG.info("RLE Segment #{} too short, set missing {} bytes to 0",
                    seg, data.length - pos);
        } catch (IndexOutOfBoundsException e) {
            LOG.info("RLE Segment #{} too long, truncate surplus bytes", seg);
        }
    }

    private void read(byte[] data, int pos, int len) throws IOException {
        int remaining = len;
        int n;
        while (remaining > 0) {
            n = bufLen - bufPos;
            if (n <= 0) {
                fillBuffer();
                n = bufLen - bufPos;
            }
            if ((remaining -= n) < 0)
                n += remaining;
            System.arraycopy(buf, bufPos, data, pos, n);
            bufPos += n;
            pos += n;
        }
    }

    private void unrle(int seg, short[] data) throws IOException {
        seekSegment(seg);
        int pos = 0;
        try {
            int shift = seg == 1 ? 8 : 0;
            int n;
            int end;
            int val;
            while (pos < data.length) {
                n = nextByte();
                if (n >= 0) {
                    read(data, pos, ++n, shift);
                    pos += n;
                } else if (n != -128) {
                    end = pos + 1 - n;
                    val = (nextByte() & 0xff) << shift;
                    while (pos < end)
                        data[pos++] |= val;
                }
            }
        } catch (EOFException e) {
            LOG.info("RLE Segment #{} too short, set missing {} bytes to 0",
                    seg, data.length - pos);
        } catch (IndexOutOfBoundsException e) {
            LOG.info("RLE Segment #{} to long, truncate surplus bytes", seg);
        }
    }

    private void read(short[] data, int pos, int len, int shift) throws IOException {
        int remaining = len;
        int n;
        while (remaining > 0) {
            n = bufLen - bufPos;
            if (n <= 0) {
                fillBuffer();
                n = bufLen - bufPos;
            }
            if ((remaining -= n) < 0)
                n += remaining;
            while (n-- > 0)
                data[pos++] |= (buf[bufPos++] & 0xff) << shift;
        }
    }

    private void fillBuffer() throws IOException {
        bufOff = iis.getStreamPosition();
        bufPos = 0;
        bufLen = iis.read(buf);
        if (bufLen <= 0)
            throw new EOFException();
    }

    private byte nextByte() throws IOException {
        if (bufPos >= bufLen)
            fillBuffer();

        return buf[bufPos++];
    }

}
