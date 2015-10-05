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
 * Portions created by the Initial Developer are Copyright (C) 2011
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
package org.dcm4che3.imageio.codec;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Value;
import org.dcm4che3.image.Overlays;
import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLS;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLSImageOutputStream;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;

/**
 * Compresses the pixel data of DICOM images to a lossless or lossy encapsulated transfer syntax format.
 * <p>
 * If the source image is already compressed it will be transcoded (i.e. first decompressed then compressed again).
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class Compressor implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(Compressor.class);

    private final Attributes dataset;
    private final String compressTsuid;
    private Object pixels;
    private VR.Holder pixeldataVR = new VR.Holder();
    private final TransferSyntaxType tsType;
    private ImageWriter compressor;
    private ImageReader verifier;
    private PatchJPEGLS compressPatchJPEGLS;
    private ImageWriteParam compressParam;
    private ImageInputStream iis;
    private IOException ex;
    private int[] embeddedOverlays;
    private int maxPixelValueError = -1;
    private int avgPixelValueBlockSize = 1;
    private BufferedImage decompressedImageForVerification;
    private ImageParams imageParams;
    private Decompressor decompressor = null;
    private BufferedImage uncompressedImage;

    private ImageReadParam verifyParam;

    public Compressor(Attributes dataset, String tsuid, String compressTsuid, Property... compressParams) {
        if (compressTsuid == null)
            throw new NullPointerException("compressTsuid");

        this.dataset = dataset;
        this.imageParams = new ImageParams(dataset);
        this.tsType = TransferSyntaxType.forUID(tsuid);
        this.compressTsuid = compressTsuid;

        pixels = dataset.getValue(Tag.PixelData, pixeldataVR);
        if (pixels == null)
            return;

        if (pixels instanceof BulkData) {
            PhotometricInterpretation pmi = imageParams.getPhotometricInterpretation();
            if (pmi.isSubSambled())
                throw new UnsupportedOperationException(
                        "Unsupported Photometric Interpretation: " + pmi);
            if (((BulkData) pixels).length() < imageParams.getLength())
                throw new IllegalArgumentException(
                        "Pixel data too short: " + ((BulkData) pixels).length()
                        + " instead " + imageParams.getLength() + " bytes");
        }

        if (pixels instanceof Fragments)
            this.decompressor = new Decompressor(dataset, tsuid);

        embeddedOverlays = Overlays.getEmbeddedOverlayGroupOffsets(dataset);

        ImageWriterFactory.ImageWriterParam param =
                ImageWriterFactory.getImageWriterParam(compressTsuid);
        if (param == null)
            throw new UnsupportedOperationException(
                    "Unsupported Transfer Syntax: " + compressTsuid);

        this.compressor = ImageWriterFactory.getImageWriter(param);
        LOG.debug("Compressor: {}", compressor.getClass().getName());
        this.compressPatchJPEGLS = param.patchJPEGLS;

        this.compressParam = compressor.getDefaultWriteParam();
        int count = 0;
        for (Property property : cat(param.getImageWriteParams(), compressParams)) {
            String name = property.getName();
            if (name.equals("maxPixelValueError")) {
                maxPixelValueError = ((Number) property.getValue()).intValue();
            } else if (name.equals("avgPixelValueBlockSize")) {
                avgPixelValueBlockSize = ((Number) property.getValue()).intValue();
            } else if(name.equals("compressionType")) {
                compressParam.setCompressionType((String)property.getValue());
            } else {
                if (count++ == 0) {
                    compressParam.setCompressionMode(
                            ImageWriteParam.MODE_EXPLICIT);
                }
                property.setAt(compressParam);
            }
        }

        if (maxPixelValueError >= 0) {
            ImageReaderFactory.ImageReaderParam readerParam =
                    ImageReaderFactory.getImageReaderParam(compressTsuid);
            if (readerParam == null)
                throw new UnsupportedOperationException(
                        "Unsupported Transfer Syntax: " + compressTsuid);

            this.verifier = ImageReaderFactory.getImageReader(readerParam);
            this.verifyParam = verifier.getDefaultReadParam();
            LOG.debug("Verifier: {}", verifier.getClass().getName());
        }
    }

    public boolean compress() throws IOException {

        if (pixels == null)
            return false;

        TransferSyntaxType compressTsType = TransferSyntaxType.forUID(compressTsuid);
        if (decompressor == null || tsType == TransferSyntaxType.RLE)
            uncompressedImage = BufferedImageUtils.createBufferedImage(imageParams, compressTsType);
        imageParams.compress(dataset, compressTsType);
        int frames = imageParams.getFrames();
        Fragments compressedPixeldata =
                dataset.newFragments(Tag.PixelData, VR.OB, frames + 1);
        compressedPixeldata.add(Value.NULL);
        for (int i = 0; i < frames; i++) {
            CompressedFrame frame = new CompressedFrame(i);
            if (needToExtractEmbeddedOverlays())
                frame.compress();
            compressedPixeldata.add(frame);
        }
        for (int gg0000 : embeddedOverlays) {
            dataset.setInt(Tag.OverlayBitsAllocated | gg0000, VR.US, 1);
            dataset.setInt(Tag.OverlayBitPosition | gg0000, VR.US, 0);
        }
        return true;
    }

    private boolean needToExtractEmbeddedOverlays() {
        return embeddedOverlays.length != 0;
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

    public void close() {
        if (iis != null)
            try { iis.close(); } catch (IOException ignore) {}
        dispose();
    }

    public void dispose() {
        if (compressor != null)
            compressor.dispose();

        if (decompressor != null)
            decompressor.dispose();

        if (verifier != null)
            verifier.dispose();

        compressor = null;
        verifier = null;
    }

    private class CompressedFrame implements Value {

        private int frameIndex;
        private int streamLength;
        private CacheOutputStream cacheout = new CacheOutputStream();
        private MemoryCacheImageOutputStream cache;

        public CompressedFrame(int frameIndex) throws IOException {
            this.frameIndex = frameIndex;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writeTo(out);
            return out.toByteArray();
        }

        @Override
        public void writeTo(DicomOutputStream out, VR vr) throws IOException {
            writeTo(out);
        }

        @Override
        public int calcLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
            return getEncodedLength(encOpts, explicitVR, vr);
        }

        @Override
        public int getEncodedLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
            try {
                compress();
            } catch (IOException e) {
                return -1;
            }
            return (streamLength + 1) & ~1;
        }
        
        private void writeTo(OutputStream out) throws IOException {
            compress();
            cacheout.set(out);
            long start = System.currentTimeMillis();
            cache.close();
            if ((streamLength & 1) != 0)
                out.write(0);
            long end = System.currentTimeMillis();
            LOG.debug("Flushed frame #{} from memory in {} ms", frameIndex + 1, end - start);
        }

        private void compress() throws IOException {
            if (cache != null)
                return;

            if (ex != null)
                throw ex;

            try {
                BufferedImage imageToCompress = Compressor.this.readFrame(frameIndex);
                Compressor.this.extractEmbeddedOverlays(frameIndex, imageToCompress);
                if (imageParams.getBitsStored() < imageParams.getBitsAllocated())
                    BufferedImageUtils.nullifyUnusedBits(imageParams.getBitsStored(),
                            imageToCompress.getRaster().getDataBuffer());
                cache = new MemoryCacheImageOutputStream(cacheout) {

                    @Override
                    public void flush() throws IOException {
                        // defer flush to writeTo()
                        LOG.debug("Ignore invoke of MemoryCacheImageOutputStream.flush()");
                    }
                };
                compressor.setOutput(compressPatchJPEGLS != null
                        ? new PatchJPEGLSImageOutputStream(cache, compressPatchJPEGLS)
                        : cache);
                long start = System.currentTimeMillis();
                compressor.write(null, new IIOImage(imageToCompress, null, null), compressParam);
                long end = System.currentTimeMillis();
                streamLength = (int) cache.getStreamPosition();
                if (LOG.isDebugEnabled())
                    LOG.debug("Compressed frame #{} {}:1 in {} ms", 
                            frameIndex + 1,
                            (float) BufferedImageUtils.sizeOf(imageToCompress) / streamLength,
                            end - start);
                Compressor.this.verify(cache, frameIndex);
            } catch (IOException ex) {
                cache = null;
                Compressor.this.ex = ex;
                throw ex;
            }
        }

    }

    private static class CacheOutputStream extends FilterOutputStream {

        public CacheOutputStream() {
            super(null);
        }

        public void set(OutputStream out) {
            this.out = out;
        }
    }

    public BufferedImage readFrame(int frameIndex) throws IOException {
        if (iis == null)
            iis = createImageInputStream(frameIndex);

        if (decompressor != null)
            return decompressor.decompressFrame(iis, frameIndex);

        if (pixels instanceof BulkData) {
            iis.setByteOrder(((BulkData)pixels).bigEndian
                    ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            iis.seek(((BulkData)pixels).offset() + imageParams.getFrameLength() * frameIndex);
        } else {
            iis.setByteOrder(dataset.bigEndian()
                    ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            iis.seek(imageParams.getFrameLength() * frameIndex);
        }
        DataBuffer db = uncompressedImage.getRaster().getDataBuffer();
        switch (db.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            byte[][] data = ((DataBufferByte) db).getBankData();
            for (byte[] bs : data)
                iis.readFully(bs);
            if (pixels instanceof BulkData) {
                if (((BulkData)pixels).bigEndian && pixeldataVR.vr == VR.OW)
                    ByteUtils.swapShorts(data);
            } else {
                if (dataset.bigEndian() && pixeldataVR.vr == VR.OW)
                    ByteUtils.swapShorts(data);
            }
            break;
        case DataBuffer.TYPE_USHORT:
            readFully(((DataBufferUShort) db).getData());
            break;
        case DataBuffer.TYPE_SHORT:
            readFully(((DataBufferShort) db).getData());
            break;
        default:
            throw new UnsupportedOperationException(
                    "Unsupported Datatype: " + db.getDataType());
        }
        return uncompressedImage;
    }

    private void verify(ImageInputStream iis, int index)
            throws IOException {
        if (verifier == null)
            return;

        iis.seek(0);
        verifier.setInput(iis);
        verifyParam.setDestination(decompressedImageForVerification);
        long start = System.currentTimeMillis();
        decompressedImageForVerification = verifier.read(0, verifyParam);
        int maxDiff =  BufferedImageUtils.maxDiff(uncompressedImage.getRaster(), decompressedImageForVerification.getRaster(), avgPixelValueBlockSize);
        long end = System.currentTimeMillis();
        if (LOG.isDebugEnabled())
            LOG.debug("Verified compressed frame #{} in {} ms - max pixel value error: {}",
                    index + 1, end - start, maxDiff);
        if (maxDiff > maxPixelValueError)
            throw new CompressionVerificationException(maxDiff);

    }

     private void extractEmbeddedOverlays(int frameIndex, BufferedImage bi) {
        for (int gg0000 : embeddedOverlays) {
            int ovlyRow = dataset.getInt(Tag.OverlayRows | gg0000, 0);
            int ovlyColumns = dataset.getInt(Tag.OverlayColumns | gg0000, 0);
            int ovlyBitPosition = dataset.getInt(Tag.OverlayBitPosition | gg0000, 0);
            int mask = 1 << ovlyBitPosition;
            int ovlyLength = ovlyRow * ovlyColumns;
            byte[] ovlyData = dataset.getSafeBytes(Tag.OverlayData | gg0000);
            if (ovlyData == null) {
                ovlyData = new byte[(((ovlyLength*imageParams.getFrames()+7)>>>3)+1)&(~1)];
                dataset.setBytes(Tag.OverlayData | gg0000, VR.OB, ovlyData);
            }
            Overlays.extractFromPixeldata(bi.getRaster(), mask, ovlyData,
                    ovlyLength * frameIndex, ovlyLength);
            LOG.debug("Extracted embedded overlay #{} from bit #{} of frame #{}",
                    (gg0000 >>> 17) + 1, ovlyBitPosition, frameIndex + 1);
        }
    }

    private void readFully(short[] data) throws IOException {
        iis.readFully(data, 0, data.length);
    }

    public ImageInputStream createImageInputStream(int frameIndex) throws IOException {

        if (pixels instanceof BulkData) {
            return new FileImageInputStream(((BulkData) pixels).getFile());
        }

        if (pixels instanceof byte[]) {
            return new MemoryCacheImageInputStream(new ByteArrayInputStream((byte[])pixels));
        }

        return null;
    }

    /**
     * @return (Pessimistic) estimation of the maximum heap memory (in bytes) that will be needed at any moment in time
     * during compression.
     */
    public long getEstimatedNeededMemory() {
        if (pixels == null)
            return 0;

        long memoryNeededDuringDecompression = 0;

        long uncompressedFrameLength = imageParams.getFrameLength();

        if (decompressor != null) {
            // memory for decompression, if decompression is needed
            memoryNeededDuringDecompression += decompressor.getEstimatedNeededMemory();
        }

        long memoryNeededDuringCompression = 0;

        // Memory for the uncompressed buffered image (only one frame, as frames are always processed sequentially)
        memoryNeededDuringCompression += uncompressedFrameLength;

        if (verifier != null) {
            // verification step done, an additional uncompressed buffered image needs to be allocated
            memoryNeededDuringCompression += uncompressedFrameLength;
        }

        // Memory for one compressed frame
        // (For now: pessimistic assumption that same memory as for the uncompressed frame is needed. This very much
        // depends on the compression algorithm, properties and the compressor implementation.)
        long compressedFrameLength = uncompressedFrameLength;

        if (!needToExtractEmbeddedOverlays()) {
            // As the compression happens lazily on demand (when writing to the OutputStream), we just need to keep one
            // frame in memory at one moment in time.
            memoryNeededDuringCompression += compressedFrameLength;
        } else {
            // if embedded overlays need to be extracted, compression for each frame is done eagerly, so all compressed
            // frames have to be kept in memory
            memoryNeededDuringCompression += compressedFrameLength * imageParams.getFrames();
        }

        // as decompression and compression are executed sequentially (in between GC can run),
        // therefore we have to consider the maximum of the two
        return Math.max(memoryNeededDuringDecompression, memoryNeededDuringCompression);
    }
}
