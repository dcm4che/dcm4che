/*
 * *** BEGIN LICENSE BLOCK *****
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
 * J4Care.
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.imageio.codec;

import org.dcm4che3.data.*;
import org.dcm4che3.image.Overlays;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLSImageOutputStream;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputHandler;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.Property;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jul 2015
 */
public class Transcoder implements Closeable {

    public interface Handler {

        OutputStream newOutputStream(Transcoder transcoder, Attributes dataset) throws IOException;

    }

    private final static Logger LOG = LoggerFactory.getLogger(Transcoder.class);

    private final static int BUFFER_SIZE = 8192;

    private Handler handler;

    private boolean includeFileMetaInformation;

    private ImageDescriptor imageDescriptor;

    private String destTransferSyntax;

    private boolean closeOutputStream = true;

    private final TransferSyntaxType srcTransferSyntaxType;
    private TransferSyntaxType destTransferSyntaxType;
    private Attributes postPixelData;
    private final DicomInputStream dis;
    private final Attributes dataset;
    private DicomOutputStream dos;
    private byte[] buffer;
    private ImageWriterFactory.ImageWriterParam compressorParam;

    private ImageWriter compressor;
    private ImageWriteParam compressParam;
    private ImageReader verifier;
    private ImageReadParam verifyParam;
    private int maxPixelValueError = -1;
    private int avgPixelValueBlockSize = 1;
    private final DicomInputHandler dicomInputHandler = new DicomInputHandler() {
        @Override
        public void readValue(DicomInputStream dis, Attributes attrs) throws IOException {
            final int tag = dis.tag();
            if (dis.level() == 0 && tag == Tag.PixelData) {
                imageDescriptor = new ImageDescriptor(attrs);
                initDicomOutputStream();
                processPixelData();
                postPixelData = new Attributes(dis.bigEndian());
            } else {
                dis.readValue(dis, attrs);
                if (postPixelData != null && dis.level() == 0)
                    postPixelData.addSelected(attrs, attrs.getPrivateCreator(tag), tag);
            }
        }

        @Override
        public void readValue(DicomInputStream dis, Sequence seq) throws IOException {
            dis.readValue(dis, seq);
        }

        @Override
        public void readValue(DicomInputStream dis, Fragments frags) throws IOException {
            final int length = dis.length();
            dos.writeHeader(Tag.Item, null, length);
            StreamUtils.copy(dis, dos, length, buffer());
        }

        @Override
        public void startDataset(DicomInputStream dis) throws IOException {

        }

        @Override
        public void endDataset(DicomInputStream dis) throws IOException {

        }
    };

    public Transcoder(File f) throws IOException {
        this(new DicomInputStream(f));
    }

    public Transcoder(InputStream in) throws IOException {
        this(new DicomInputStream(in));
    }

    public Transcoder(InputStream in, String tsuid) throws IOException {
        this(new DicomInputStream(in, tsuid));
    }

    public Transcoder(DicomInputStream dis) throws IOException {
        this.dis = dis;
        dis.readFileMetaInformation();
        dis.setDicomInputHandler(dicomInputHandler);
        dataset = new Attributes(dis.bigEndian(), 64);
        srcTransferSyntaxType = TransferSyntaxType.forUID(dis.getTransferSyntax());
        destTransferSyntax = dis.getTransferSyntax();
        destTransferSyntaxType = srcTransferSyntaxType;
    }

    public void setConcatenateBulkDataFiles(boolean catBlkFiles) {
        dis.setConcatenateBulkDataFiles(catBlkFiles);
    }

    public void setIncludeBulkData(DicomInputStream.IncludeBulkData includeBulkData) {
        dis.setIncludeBulkData(includeBulkData);
    }

    public void setBulkDataDescriptor(BulkDataDescriptor bulkDataDescriptor) {
        dis.setBulkDataDescriptor(bulkDataDescriptor);
    }

    public void setBulkDataDirectory(File blkDirectory) {
        dis.setBulkDataDirectory(blkDirectory);
    }

    public boolean isCloseOutputStream() {
        return closeOutputStream;
    }

    public void setCloseOutputStream(boolean closeOutputStream) {
        this.closeOutputStream = closeOutputStream;
    }

    public void transcode(Handler handler) throws IOException {
        this.handler = handler;
        dis.readAttributes(dataset, -1, -1);

        if (dos == null) {
            initDicomOutputStream();
            writeDataset();
        } else if (postPixelData != null)
            dos.writeDataset(null, postPixelData);
    }

    public boolean isIncludeFileMetaInformation() {
        return includeFileMetaInformation;
    }

    public void setIncludeFileMetaInformation(boolean includeFileMetaInformation) {
        this.includeFileMetaInformation = includeFileMetaInformation;
    }

    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    public String getSourceTransferSyntax() {
        return dis.getTransferSyntax();
    }

    public TransferSyntaxType getSourceTransferSyntaxType() {
        return srcTransferSyntaxType;
    }

    public String getDestinationTransferSyntax() {
        return destTransferSyntax;
    }

    public void setDestinationTransferSyntax(String tsuid) {
        if (tsuid.equals(destTransferSyntax))
            return;

        this.destTransferSyntaxType = TransferSyntaxType.forUID(tsuid);
        this.destTransferSyntax = tsuid;
        if (destTransferSyntaxType != TransferSyntaxType.NATIVE)
            initCompressor(tsuid);
    }

    private void initCompressor(String tsuid) {
        compressorParam = ImageWriterFactory.getImageWriterParam(tsuid);
        if (compressorParam == null)
            throw new UnsupportedOperationException(
                    "Unsupported Transfer Syntax: " + tsuid);

        this.compressor = ImageWriterFactory.getImageWriter(compressorParam);
        LOG.debug("Compressor: {}", compressor.getClass().getName());

        this.compressParam = compressor.getDefaultWriteParam();
    }

    public void setCompressParams(Property[] imageWriteParams) {
        int count = 0;
        for (Property property : cat(compressorParam.getImageWriteParams(), imageWriteParams)) {
            String name = property.getName();
            if (name.equals("maxPixelValueError"))
                this.maxPixelValueError = ((Number) property.getValue()).intValue();
            else if (name.equals("avgPixelValueBlockSize"))
                this.avgPixelValueBlockSize = ((Number) property.getValue()).intValue();
            else {
                if (count++ == 0)
                    compressParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                property.setAt(compressParam);
            }
        }
        if (maxPixelValueError >= 0) {
            ImageReaderFactory.ImageReaderParam readerParam =
                    ImageReaderFactory.getImageReaderParam(destTransferSyntax);
            if (readerParam == null)
                throw new UnsupportedOperationException(
                        "Unsupported Transfer Syntax: " + destTransferSyntax);

            this.verifier = ImageReaderFactory.getImageReader(readerParam);
            this.verifyParam = verifier.getDefaultReadParam();
            LOG.debug("Verifier: {}", verifier.getClass().getName());
        }
    }


    @Override
    public void close() throws IOException {
        if (compressor != null)
            compressor.dispose();
        if (verifier != null)
            verifier.dispose();
        SafeClose.close(dis);
        if (closeOutputStream)
            SafeClose.close(dos);
        for (File tmpFile : dis.getBulkDataFiles())
            tmpFile.delete();

    }

    private void processPixelData()
            throws IOException {
        if (compressor == null) {
            copyPixelData();
        } else {
            compressPixelData();
        }
    }

    private void copyPixelData() throws IOException {
        final int length = dis.length();
        writeDataset();
        dos.writeHeader(Tag.PixelData, dis.vr(), length);
        if (length == -1) {
            dis.readValue(dis, dataset);
            dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
        } else {
            if (dis.bigEndian() == dos.isBigEndian())
                StreamUtils.copy(dis, dos, length, buffer());
            else
                StreamUtils.copy(dis, dos, length, dis.vr().numEndianBytes(), buffer());
        }
    }

    private void compressPixelData() throws IOException {
        int padding = dis.length() - imageDescriptor.getLength();
        BufferedImage bi = createBufferedImage();
        for (int i = 0; i < imageDescriptor.getFrames(); i++) {
            readFrame(bi);
            if (i == 0) {
                adjustDataset(bi);
                writeDataset();
                dos.writeHeader(Tag.PixelData, VR.OB, -1);
                dos.writeHeader(Tag.Item, null, 0);
            }
            nullifyUnusedBits(bi);
            compressFrame(bi, i);
        }
        dis.skipFully(padding);
        dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
    }


    private void adjustDataset(BufferedImage bi) {
        extractEmbeddedOverlays(bi);
        if (imageDescriptor.getSamples() == 3) {
            dataset.setString(Tag.PhotometricInterpretation, VR.CS,
                    imageDescriptor.getPhotometricInterpretation()
                            .decompress()
                            .compress(destTransferSyntax)
                            .toString());
            dataset.setInt(Tag.PlanarConfiguration, VR.US, destTransferSyntaxType.getPlanarConfiguration());
        }
    }

    private void extractEmbeddedOverlays(BufferedImage bi) {
        for (int gg0000 : imageDescriptor.getEmbeddedOverlays()) {
            int ovlyRow = dataset.getInt(Tag.OverlayRows | gg0000, 0);
            int ovlyColumns = dataset.getInt(Tag.OverlayColumns | gg0000, 0);
            int ovlyBitPosition = dataset.getInt(Tag.OverlayBitPosition | gg0000, 0);
            int mask = 1 << ovlyBitPosition;
            int ovlyLength = ovlyRow * ovlyColumns;
            byte[] ovlyData = new byte[(((ovlyLength+7)>>>3)+1)&(~1)];
            Overlays.extractFromPixeldata(bi.getRaster(), mask, ovlyData, 0, ovlyLength);
            dataset.setInt(Tag.OverlayBitsAllocated | gg0000, VR.US, 1);
            dataset.setInt(Tag.OverlayBitPosition | gg0000, VR.US, 0);
            dataset.setBytes(Tag.OverlayData | gg0000, VR.OB, ovlyData);
            LOG.debug("Extracted embedded overlay #{} from bit #{}", (gg0000 >>> 17) + 1, ovlyBitPosition);
        }
    }

    private void nullifyUnusedBits(BufferedImage bi) {
        if (imageDescriptor.getBitsStored() < imageDescriptor.getBitsAllocated()) {
            DataBuffer db = bi.getRaster().getDataBuffer();
            switch (db.getDataType()) {
                case DataBuffer.TYPE_USHORT:
                    nullifyUnusedBits(((DataBufferUShort) db).getData());
                    break;
                case DataBuffer.TYPE_SHORT:
                    nullifyUnusedBits(((DataBufferShort) db).getData());
                    break;
            }
        }
    }

    private void nullifyUnusedBits(short[] data) {
        int mask = (1<<imageDescriptor.getBitsStored())-1;
        for (int i = 0; i < data.length; i++)
            data[i] &= mask;
    }

    private void compressFrame(BufferedImage bi, int frameIndex) throws IOException {
        ExtMemoryCacheImageOutputStream ios = new ExtMemoryCacheImageOutputStream();
        compressor.setOutput(compressorParam.patchJPEGLS != null
                ? new PatchJPEGLSImageOutputStream(ios, compressorParam.patchJPEGLS)
                : ios);
        long start = System.currentTimeMillis();
        compressor.write(null, new IIOImage(bi, null, null), compressParam);
        long end = System.currentTimeMillis();
        int length = (int) ios.getStreamPosition();
        if (LOG.isDebugEnabled())
            LOG.debug("Compressed frame #{} {}:1 in {} ms",
                   frameIndex + 1, (float) sizeOf(bi) / length, end - start);
        if ((length & 1) != 0) {
            ios.write(0);
            length++;
        }
        dos.writeHeader(Tag.Item, null, length);
        ios.setOutputStream(dos);
        ios.flush();
    }

    static int sizeOf(BufferedImage bi) {
        DataBuffer db = bi.getData().getDataBuffer();
        return db.getSize() * db.getNumBanks() * (DataBuffer.getDataTypeSize(db.getDataType()) / 8);
    }

    private void readFrame(BufferedImage bi) throws IOException {
        WritableRaster raster = bi.getRaster();
        DataBuffer dataBuffer = raster.getDataBuffer();
        switch (dataBuffer.getDataType()) {
            case DataBuffer.TYPE_SHORT:
                readFully(((DataBufferShort) dataBuffer).getData());
                break;
            case DataBuffer.TYPE_USHORT:
                readFully(((DataBufferUShort) dataBuffer).getData());
                break;
            case DataBuffer.TYPE_BYTE:
                readFully(((DataBufferByte) dataBuffer).getBankData());
                break;
        }
    }

    private void readFully(byte[][] bb) throws IOException {
        for (byte[] b : bb) {
            dis.readFully(b);
        }
        if (dis.bigEndian())
            ByteUtils.swapShorts(bb);
    }

    private void readFully(short[] s) throws IOException {
        int off = 0;
        int len = s.length;
        byte[] b = buffer();
        while (len > 0) {
            int nelts = Math.min(len, b.length/2);
            dis.readFully(b, 0, nelts * 2);
            toShorts(b, s, off, nelts, dis.bigEndian());
            off += nelts;
            len -= nelts;
        }
    }

    private void toShorts(byte[] b, short[] s, int off, int len, boolean bigEndian) {
        int boff = 0;
        if (bigEndian) {
            for (int j = 0; j < len; j++) {
                int b0 = b[boff];
                int b1 = b[boff + 1] & 0xff;
                s[off + j] = (short)((b0 << 8) | b1);
                boff += 2;
            }
        } else {
            for (int j = 0; j < len; j++) {
                int b0 = b[boff + 1];
                int b1 = b[boff] & 0xff;
                s[off + j] = (short)((b0 << 8) | b1);
                boff += 2;
            }
        }
    }


    private byte[] buffer() {
        if (buffer == null)
            buffer = new byte[BUFFER_SIZE];
        return buffer;
    }

    private void initDicomOutputStream() throws IOException {
        dos = new DicomOutputStream(handler.newOutputStream(this, dataset),
                includeFileMetaInformation ? UID.ExplicitVRLittleEndian : destTransferSyntax);
    }

    private void writeDataset() throws IOException {
        dos.writeDataset(includeFileMetaInformation
                        ? dataset.createFileMetaInformation(destTransferSyntax)
                        : null,
                dataset);
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

    private BufferedImage createBufferedImage() {
        int rows = imageDescriptor.getRows();
        int cols = imageDescriptor.getColumns();
        int samples = imageDescriptor.getSamples();
        int bitsAllocated = imageDescriptor.getBitsAllocated();
        int bitsStored = Math.min(imageDescriptor.getBitsStored(), destTransferSyntaxType.getMaxBitsStored());
        boolean signed = imageDescriptor.isSigned() && destTransferSyntaxType.canEncodeSigned();
        boolean banded = imageDescriptor.isBanded() || srcTransferSyntaxType == TransferSyntaxType.RLE;
        int dataType = bitsAllocated > 8
                ? (signed ? DataBuffer.TYPE_SHORT : DataBuffer.TYPE_USHORT)
                : DataBuffer.TYPE_BYTE;
        ComponentColorModel cm = samples == 1
                ? new ComponentColorModel(
                    ColorSpace.getInstance(ColorSpace.CS_GRAY),
                    new int[] { bitsStored },
                    false, // hasAlpha
                    false, // isAlphaPremultiplied,
                    Transparency.OPAQUE,
                    dataType)
                :  new ComponentColorModel(
                    ColorSpace.getInstance(ColorSpace.CS_sRGB),
                    new int[] { bitsStored, bitsStored, bitsStored },
                    false, // hasAlpha
                    false, // isAlphaPremultiplied,
                    Transparency.OPAQUE,
                    dataType);

        SampleModel sm = banded
                ? new BandedSampleModel(dataType, cols, rows, samples)
                : new PixelInterleavedSampleModel(dataType, cols, rows,
                samples, cols * samples, bandOffsets(samples));
        WritableRaster raster = Raster.createWritableRaster(sm, null);
        return new BufferedImage(cm, raster, false, null);
    }

    private int[] bandOffsets(int samples) {
        int[] offsets = new int[samples];
        for (int i = 0; i < samples; i++)
            offsets[i] = i;
        return offsets;
    }

}
