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
 * The Initial Developer of the Original Code is Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011-2015
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
 */

package org.dcm4che3.imageio.codec;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.image.Overlays;
import org.dcm4che3.imageio.codec.ImageReaderFactory.ImageReaderItem;
import org.dcm4che3.imageio.codec.ImageWriterFactory.ImageWriterItem;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLS;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLSImageOutputStream;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.Property;
import org.dcm4che3.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.*;
import java.io.IOException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Feb 2015.
 *
 * @deprecated This is prototype code. StreamCompressor will be replaced by a Transcoder that supports both stream
 * compression and decompression. For now you can continue using {@link Compressor} for non-stream compression.
 */
@Deprecated
public class StreamCompressor extends StreamDecompressor {

    private static final Logger LOG = LoggerFactory.getLogger(StreamCompressor.class);

    private TransferSyntaxType compressTsType;
    private ImageWriter compressor;
    private PatchJPEGLS compressPatchJPEGLS;
    private ImageWriteParam compressParam;
    private int maxPixelValueError = -1;
    private int avgPixelValueBlockSize = 1;
    private ImageReader verifier;
    private ImageReadParam verifyParam;
    private ImageParams imageParams;
    private BufferedImage bi2;
    private int frameIndex;

    public StreamCompressor(DicomInputStream in, String inTransferSyntaxUID, DicomOutputStream out) {
        super(in, inTransferSyntaxUID, out);
    }

    public boolean compress(String compressTsuid, Property... params) throws IOException {
        if (compressTsuid == null)
            throw new NullPointerException("compressTsuid");

        this.compressTsType = TransferSyntaxType.forUID(compressTsuid);
        if (compressTsType == null)
            throw new IllegalArgumentException("Unknown Transfer Syntax: " + compressTsuid);

        ImageWriterItem writerItem =
                ImageWriterFactory.getImageWriterParam(compressTsuid);
        if (writerItem == null)
            throw new UnsupportedOperationException(
                    "Unsupported Transfer Syntax: " + compressTsuid);

        this.compressor = writerItem.getImageWriter();
        LOG.debug("Compressor: {}", compressor.getClass().getName());
        this.compressPatchJPEGLS = writerItem.getImageWriterParam().getPatchJPEGLS();
        this.compressParam = compressor.getDefaultWriteParam();
        int count = 0;
        for (Property property : cat(writerItem.getImageWriterParam().getImageWriteParams(), params)) {
            String name = property.getName();
            if (name.equals("maxPixelValueError"))
                this.maxPixelValueError = ((Number) property.getValue()).intValue();
            else if (name.equals("avgPixelValueBlockSize"))
                this.avgPixelValueBlockSize = ((Number) property.getValue()).intValue();
            else {
                if (count++ == 0)
                    compressParam.setCompressionMode(
                            ImageWriteParam.MODE_EXPLICIT);
                property.setAt(compressParam);
            }
        }

        if (maxPixelValueError >= 0) {
            ImageReaderItem readerItem = ImageReaderFactory.getImageReader(compressTsuid);
            if (readerItem == null)
                throw new IllegalArgumentException("Unsupported Transfer Syntax: " + compressTsuid);
            this.verifier = readerItem.getImageReader();
            LOG.debug("Verifier: {}", verifier.getClass().getName());
            this.verifyParam = verifier.getDefaultReadParam();
        }
        decompress();
        return pixeldataProcessed;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (compressor != null)
            compressor.dispose();
        if (verifier != null)
            verifier.dispose();
    }

    private Property[] cat(Property[] a, Property[] b) {
        if (a.length == 0)
            return b;
        if (b.length == 0)
            return a;
        Property[] c = new Property[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    @Override
    protected void onPixelData(DicomInputStream dis, Attributes attrs) throws IOException {
        int tag = dis.tag();
        VR vr = dis.vr();
        int len = dis.length();
        BufferedImage bi = null;
        this.imageParams = new ImageParams(dataset);
        if (decompressor != null)
            imageParams.decompress(attrs, tsType);

        if (decompressor == null || tsType == TransferSyntaxType.RLE)
            bi = BufferedImageUtils.createBufferedImage(imageParams, compressTsType);

        imageParams.compress(attrs, compressTsType);
        coerceAttributes.coerce(attrs).writeTo(out);
        attrs.clear();
        out.writeHeader(Tag.PixelData, VR.OB, -1);
        out.writeHeader(Tag.Item, null, 0);

        if (len == -1) {
            if (!tsType.isPixeldataEncapsulated()) {
                throw new IOException("Unexpected encapsulated Pixel Data");
            }
            decompressFrames(dis, imageParams, bi);
        } else {
            if (tsType.isPixeldataEncapsulated())
                throw new IOException("Pixel Data not encapsulated");
            int padding = len - imageParams.getLength();
            if (padding < 0)
                throw new IllegalArgumentException(
                        "Pixel data too short: " + len + " instead "
                                + imageParams.getLength() + " bytes");

            byte[] buf = null;
            DataBuffer dataBuffer = bi.getRaster().getDataBuffer();
            if (dataBuffer.getDataType() != DataBuffer.TYPE_BYTE)
                buf = new byte[imageParams.getFrameLength()];

            for (int i = 0; i < imageParams.getFrames(); i++) {
                readFrame(dis, dataBuffer, buf);
                writeFrame(bi);
            }
            dis.skipFully(padding);
        }
        out.writeHeader(Tag.SequenceDelimitationItem, null, 0);
        pixeldataProcessed = true;
    }

   private void readFrame(DicomInputStream dis, DataBuffer db, byte[] buf) throws IOException {
        switch (db.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            byte[][] data = ((DataBufferByte) db).getBankData();
            for (byte[] bs : data)
                dis.readFully(bs);
            if (dis.bigEndian() && dis.vr() == VR.OW)
                ByteUtils.swapShorts(data);
            break;
        case DataBuffer.TYPE_USHORT:
            dis.readFully(buf);
            ByteUtils.bytesToShorts(buf, 0, ((DataBufferUShort) db).getData(), 0, buf.length >> 1, dis.bigEndian());
            break;
        case DataBuffer.TYPE_SHORT:
            dis.readFully(buf);
            ByteUtils.bytesToShorts(buf, 0, ((DataBufferShort) db).getData(), 0, buf.length >> 1, dis.bigEndian());
            break;
        default:
            throw new UnsupportedOperationException("Unsupported Datatype: " + db.getDataType());
        }
   }

    @Override
    protected void writeFrame(BufferedImage bi) throws IOException {
        if (imageParams.getBitsStored() < imageParams.getBitsAllocated())
            BufferedImageUtils.nullifyUnusedBits(imageParams.getBitsStored(), bi.getRaster().getDataBuffer());

        MemoryCacheImageOutputStream compressedFrame = new MemoryCacheImageOutputStream(out) {

            @Override
            public void flush() throws IOException {
                // defer flush to close()
                LOG.debug("Ignore invoke of MemoryCacheImageOutputStream.flush()");
            }
        };
        compressor.setOutput(compressPatchJPEGLS != null
                ? new PatchJPEGLSImageOutputStream(compressedFrame, compressPatchJPEGLS)
                : compressedFrame);
        long start = System.currentTimeMillis();
        compressor.write(null, new IIOImage(bi, null, null), compressParam);
        long end = System.currentTimeMillis();
        int streamLength = (int) compressedFrame.getStreamPosition();
        if (LOG.isDebugEnabled())
            LOG.debug("Compressed frame #{} {}:1 in {} ms",
                    frameIndex+1, (float) BufferedImageUtils.sizeOf(bi) / streamLength,  end - start);
        verify(compressedFrame, bi);
        out.writeHeader(Tag.Item, null, (streamLength + 1) & ~1);
        start = System.currentTimeMillis();
        compressedFrame.close();
        if ((streamLength & 1) != 0)
            out.write(0);
        end = System.currentTimeMillis();
        LOG.debug("Flushed frame #{} from memory in {} ms", frameIndex+1, start - end);
        frameIndex++;
    }

    private void verify(ImageInputStream iis, BufferedImage bi) throws IOException {
        if (verifier == null)
            return;

        iis.seek(0);
        verifier.setInput(iis);
        verifyParam.setDestination(bi2);
        long start = System.currentTimeMillis();
        bi2 = verifier.read(0, verifyParam);
        int maxDiff =  BufferedImageUtils.maxDiff(bi.getRaster(), bi2.getRaster(), avgPixelValueBlockSize);
        long end = System.currentTimeMillis();
        LOG.debug("Verified compressed frame #{} in {} ms - max pixel value error: {}",
                frameIndex+1, end - start, maxDiff);
        if (maxDiff > maxPixelValueError)
            throw new CompressionVerificationException(maxDiff);
    }
}
