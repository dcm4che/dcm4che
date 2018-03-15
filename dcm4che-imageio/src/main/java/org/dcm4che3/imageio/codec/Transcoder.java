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
 * Java(TM), hosted at https://github.com/dcm4che.
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
import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLSImageInputStream;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLSImageOutputStream;
import org.dcm4che3.imageio.stream.EncapsulatedPixelDataImageInputStream;
import org.dcm4che3.io.*;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.Property;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.util.List;
import java.util.Objects;

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

    private final DicomInputStream dis;

    private final String srcTransferSyntax;

    private final TransferSyntaxType srcTransferSyntaxType;

    private final Attributes dataset;

    private boolean retainFileMetaInformation;

    private boolean includeFileMetaInformation;

    private DicomEncodingOptions encOpts = DicomEncodingOptions.DEFAULT;

    private boolean closeInputStream = true;

    private boolean closeOutputStream = true;

    private boolean deleteBulkDataFiles = true;

    private String destTransferSyntax;

    private TransferSyntaxType destTransferSyntaxType;

    private int maxPixelValueError = -1;

    private int avgPixelValueBlockSize = 1;

    private DicomOutputStream dos;

    private Attributes postPixelData;

    private Handler handler;

    private ImageDescriptor imageDescriptor;

    private EncapsulatedPixelDataImageInputStream encapsulatedPixelData;

    private ImageReaderFactory.ImageReaderParam decompressorParam;

    private ImageReader decompressor;

    private ImageReadParam decompressParam;

    private ImageWriterFactory.ImageWriterParam compressorParam;

    private ImageWriter compressor;

    private ImageWriteParam compressParam;

    private ImageReader verifier;

    private ImageReadParam verifyParam;

    private BufferedImage bi;

    private BufferedImage bi2;

    private String pixelDataBulkDataURI;

    private byte[] buffer;

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
        srcTransferSyntax = dis.getTransferSyntax();
        srcTransferSyntaxType = TransferSyntaxType.forUID(srcTransferSyntax);
        destTransferSyntax = srcTransferSyntax;
        destTransferSyntaxType = srcTransferSyntaxType;
    }

    public void setEncodingOptions(DicomEncodingOptions encOpts) {
        this.encOpts = Objects.requireNonNull(encOpts);
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

    public boolean isCloseInputStream() {
        return closeInputStream;
    }

    public void setCloseInputStream(boolean closeInputStream) {
        this.closeInputStream = closeInputStream;
    }

    public boolean isCloseOutputStream() {
        return closeOutputStream;
    }

    public void setCloseOutputStream(boolean closeOutputStream) {
        this.closeOutputStream = closeOutputStream;
    }

    public boolean isDeleteBulkDataFiles() {
        return deleteBulkDataFiles;
    }

    public void setDeleteBulkDataFiles(boolean deleteBulkDataFiles) {
        this.deleteBulkDataFiles = deleteBulkDataFiles;
    }

    public boolean isIncludeFileMetaInformation() {
        return includeFileMetaInformation;
    }

    public void setIncludeFileMetaInformation(boolean includeFileMetaInformation) {
        this.includeFileMetaInformation = includeFileMetaInformation;
    }

    public boolean isRetainFileMetaInformation() {
        return retainFileMetaInformation;
    }

    public void setRetainFileMetaInformation(boolean retainFileMetaInformation) {
        this.retainFileMetaInformation = retainFileMetaInformation;
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

        if (srcTransferSyntaxType != TransferSyntaxType.NATIVE)
            initDecompressor();
        if (destTransferSyntaxType != TransferSyntaxType.NATIVE)
            initCompressor(tsuid);
    }

    public String getPixelDataBulkDataURI() {
        return pixelDataBulkDataURI;
    }

    public void setPixelDataBulkDataURI(String pixelDataBulkDataURI) {
        this.pixelDataBulkDataURI = pixelDataBulkDataURI;
    }

    public List<File> getBulkDataFiles() {
        return dis.getBulkDataFiles();
    }

    private void initDecompressor() {
        decompressorParam = ImageReaderFactory.getImageReaderParam(srcTransferSyntax);
        if (decompressorParam == null)
            throw new UnsupportedOperationException(
                    "Unsupported Transfer Syntax: " + srcTransferSyntax);

        this.decompressor = ImageReaderFactory.getImageReader(decompressorParam);
        this.decompressParam = decompressor.getDefaultReadParam();
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
        if (decompressor != null)
            decompressor.dispose();
        if (compressor != null)
            compressor.dispose();
        if (verifier != null)
            verifier.dispose();
        if (closeInputStream)
            SafeClose.close(dis);
        if (deleteBulkDataFiles)
            for (File tmpFile : dis.getBulkDataFiles())
                tmpFile.delete();
        if (closeOutputStream && dos != null)
            dos.close();
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

    private final DicomInputHandler dicomInputHandler = new DicomInputHandler() {
        @Override
        public void readValue(DicomInputStream dis, Attributes attrs) throws IOException {
            int tag = dis.tag();
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
            if (dos == null) {
                dis.readValue(dis, frags);
            } else {
                int length = dis.length();
                dos.writeHeader(Tag.Item, null, length);
                StreamUtils.copy(dis, dos, length, buffer());
            }
        }

        @Override
        public void startDataset(DicomInputStream dis) throws IOException {

        }

        @Override
        public void endDataset(DicomInputStream dis) throws IOException {

        }
    };


    private void processPixelData() throws IOException {
        if (decompressor != null)
            initEncapsulatedPixelData();
        VR vr;
        if (compressor != null) {
            vr = VR.OB;
            compressPixelData();
        } else if (decompressor != null) {
            vr = VR.OW;
            decompressPixelData();
        } else {
            vr = dis.vr();
            copyPixelData();
        }
        setPixelDataBulkData(vr);
    }

    private void initEncapsulatedPixelData() throws IOException {
        encapsulatedPixelData = new EncapsulatedPixelDataImageInputStream(dis, imageDescriptor);
    }

    private void decompressPixelData() throws IOException {
        int length = imageDescriptor.getLength();
        int padding = length & 1;
        adjustDataset();
        writeDataset();
        dos.writeHeader(Tag.PixelData, VR.OW, length + padding);
        for (int i = 0; i < imageDescriptor.getFrames(); i++) {
            decompressFrame(i);
            writeFrame();
        }
        if (padding != 0)
            dos.write(0);
    }

    private void copyPixelData() throws IOException {
        int length = dis.length();
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
        for (int i = 0; i < imageDescriptor.getFrames(); i++) {
            if (decompressor == null)
                readFrame();
            else
                bi = decompressFrame(i);
            if (i == 0) {
                extractEmbeddedOverlays();
                adjustDataset();
                writeDataset();
                dos.writeHeader(Tag.PixelData, VR.OB, -1);
                dos.writeHeader(Tag.Item, null, 0);
            }
            nullifyUnusedBits();
            compressFrame(i);
        }
        dis.skipFully(padding);
        dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
    }

    private void setPixelDataBulkData(VR vr) {
        if (pixelDataBulkDataURI != null)
            dataset.setValue(Tag.PixelData, vr, new BulkData(null, pixelDataBulkDataURI, false));
    }

    private void adjustDataset() {
        if (imageDescriptor.getSamples() == 3) {
            PhotometricInterpretation pmi = imageDescriptor.getPhotometricInterpretation();
            int planarConfiguration = imageDescriptor.getPlanarConfiguration();
            if (decompressor != null) {
                pmi = pmi.decompress();
                planarConfiguration = srcTransferSyntaxType.getPlanarConfiguration();
            }
            if (compressor != null) {
                pmi = pmi.compress(destTransferSyntax);
                planarConfiguration = destTransferSyntaxType.getPlanarConfiguration();
            }
            dataset.setString(Tag.PhotometricInterpretation, VR.CS,  pmi.toString());
            dataset.setInt(Tag.PlanarConfiguration, VR.US, planarConfiguration);
        }
    }

    private void extractEmbeddedOverlays() {
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

    private void nullifyUnusedBits() {
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

    private BufferedImage decompressFrame(int frameIndex) throws IOException {
        decompressor.setInput(decompressorParam.patchJPEGLS != null
                ? new PatchJPEGLSImageInputStream(encapsulatedPixelData, decompressorParam.patchJPEGLS)
                : encapsulatedPixelData);
        if (srcTransferSyntaxType == TransferSyntaxType.RLE)
            initBufferedImage();
        decompressParam.setDestination(bi);
        long start = System.currentTimeMillis();
        bi = decompressor.read(0, decompressParam);
        long end = System.currentTimeMillis();
        if (LOG.isDebugEnabled())
            LOG.debug("Decompressed frame #{} 1:{} in {} ms",
                    frameIndex + 1, (float) sizeOf(bi) / encapsulatedPixelData.getStreamPosition(), end - start);
        encapsulatedPixelData.seekNextFrame();
        return bi;
    }

    private void compressFrame(int frameIndex) throws IOException {
        ExtMemoryCacheImageOutputStream ios = new ExtMemoryCacheImageOutputStream(imageDescriptor);
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
        verify(ios, frameIndex);
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

    private void readFrame() throws IOException {
        initBufferedImage();
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

    private void writeFrame() throws IOException {
        WritableRaster raster = bi.getRaster();
        SampleModel sm = raster.getSampleModel();
        DataBuffer db = raster.getDataBuffer();
        switch (db.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                write(sm, ((DataBufferByte) db).getBankData());
                break;
            case DataBuffer.TYPE_USHORT:
                write(sm, ((DataBufferUShort) db).getData());
                break;
            case DataBuffer.TYPE_SHORT:
                write(sm, ((DataBufferShort) db).getData());
                break;
            case DataBuffer.TYPE_INT:
                write(sm, ((DataBufferInt) db).getData());
                break;
            default:
                throw new UnsupportedOperationException("Unsupported Datatype: " + db.getDataType());
        }
    }

    private void write(SampleModel sm, byte[][] bankData) throws IOException {
        int h = sm.getHeight();
        int w = sm.getWidth();
        ComponentSampleModel csm = (ComponentSampleModel) sm;
        int len = w * csm.getPixelStride();
        int stride = csm.getScanlineStride();
        if (csm.getBandOffsets()[0] != 0)
            bgr2rgb(bankData[0]);
        for (byte[] b : bankData)
            for (int y = 0, off = 0; y < h; ++y, off += stride)
                dos.write(b, off, len);
    }

    private static void bgr2rgb(byte[] bs) {
        for (int i = 0, j = 2; j < bs.length; i += 3, j += 3) {
            byte b = bs[i];
            bs[i] = bs[j];
            bs[j] = b;
        }
    }

    private void write(SampleModel sm, short[] data) throws IOException {
        int h = sm.getHeight();
        int w = sm.getWidth();
        int stride = ((ComponentSampleModel) sm).getScanlineStride();
        byte[] b = new byte[w * 2];
        for (int y = 0; y < h; ++y) {
            for (int i = 0, j = y * stride; i < b.length;) {
                short s = data[j++];
                b[i++] = (byte) s;
                b[i++] = (byte) (s >> 8);
            }
            dos.write(b);
        }
    }

    private void write(SampleModel sm, int[] data) throws IOException {
        int h = sm.getHeight();
        int w = sm.getWidth();
        int stride = ((SinglePixelPackedSampleModel) sm).getScanlineStride();
        byte[] b = new byte[w * 3];
        for (int y = 0; y < h; ++y) {
            for (int i = 0, j = y * stride; i < b.length;) {
                int s = data[j++];
                b[i++] = (byte) (s >> 16);
                b[i++] = (byte) (s >> 8);
                b[i++] = (byte) s;
            }
            dos.write(b);
        }
    }

    private void initDicomOutputStream() throws IOException {
        dos = new DicomOutputStream(handler.newOutputStream(this, dataset),
                includeFileMetaInformation ? UID.ExplicitVRLittleEndian : destTransferSyntax);
        dos.setEncodingOptions(encOpts);
    }

    private void writeDataset() throws IOException {
        Attributes fmi = null;
        if (includeFileMetaInformation) {
            if (retainFileMetaInformation)
                fmi = dis.getFileMetaInformation();
            if (fmi == null)
                fmi = dataset.createFileMetaInformation(destTransferSyntax);
            else
                fmi.setString(Tag.TransferSyntaxUID, VR.UI, destTransferSyntax);
        }
        dos.writeDataset(fmi, dataset);
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

    private void initBufferedImage() {
        if (bi != null)
            return;

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
        bi = new BufferedImage(cm, raster, false, null);
    }

    private int[] bandOffsets(int samples) {
        int[] offsets = new int[samples];
        for (int i = 0; i < samples; i++)
            offsets[i] = i;
        return offsets;
    }

    private void verify(ImageOutputStream cache, int index)
            throws IOException {
        if (verifier == null)
            return;

        long prevStreamPosition = cache.getStreamPosition();
        int prevBitOffset = cache.getBitOffset();
        cache.seek(0);
        verifier.setInput(cache);
        verifyParam.setDestination(bi2);
        long start = System.currentTimeMillis();
        bi2 = verifier.read(0, verifyParam);
        int maxDiff = maxDiff(bi.getRaster(), bi2.getRaster());
        long end = System.currentTimeMillis();
        if (LOG.isDebugEnabled())
            LOG.debug("Verified compressed frame #{} in {} ms - max pixel value error: {}",
                    new Object[] { index + 1, end - start, maxDiff });
        if (maxDiff > maxPixelValueError)
            throw new CompressionVerificationException(maxDiff);
        cache.seek(prevStreamPosition);
        cache.setBitOffset(prevBitOffset);
    }

    private int maxDiff(WritableRaster raster, WritableRaster raster2) {
        ComponentSampleModel csm =
                (ComponentSampleModel) raster.getSampleModel();
        ComponentSampleModel csm2 =
                (ComponentSampleModel) raster2.getSampleModel();
        DataBuffer db = raster.getDataBuffer();
        DataBuffer db2 = raster2.getDataBuffer();
        int blockSize = avgPixelValueBlockSize;
        if (blockSize > 1) {
            int w = csm.getWidth();
            int h = csm.getHeight();
            int maxY = (h / blockSize - 1) * blockSize;
            int maxX = (w / blockSize - 1) * blockSize;
            int[] samples = new int[blockSize * blockSize];
            int diff, maxDiff = 0;
            for (int b = 0; b < csm.getNumBands(); b++)
                for (int y = 0; y < maxY; y += blockSize) {
                    for (int x = 0; x < maxX; x += blockSize) {
                        if (maxDiff < (diff = Math.abs(
                                sum(csm.getSamples(
                                        x, y, blockSize, blockSize, b, samples, db))
                                        - sum(csm2.getSamples(
                                        x, y, blockSize, blockSize, b, samples, db2)))))
                            maxDiff = diff;
                    }
                }
            return maxDiff / samples.length;
        }
        switch (db.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                return maxDiff(csm, ((DataBufferByte) db).getBankData(),
                        csm2, ((DataBufferByte) db2).getBankData());
            case DataBuffer.TYPE_USHORT:
                return maxDiff(csm, ((DataBufferUShort) db).getData(),
                        csm2, ((DataBufferUShort) db2).getData());
            case DataBuffer.TYPE_SHORT:
                return maxDiff(csm, ((DataBufferShort) db).getData(),
                        csm2, ((DataBufferShort) db2).getData());
            default:
                throw new UnsupportedOperationException(
                        "Unsupported Datatype: " + db.getDataType());
        }
    }

    private int sum(int[] samples) {
        int sum = 0;
        for (int sample : samples)
            sum += sample;
        return sum;
    }

    private int maxDiff(ComponentSampleModel csm, short[] data,
                        ComponentSampleModel csm2, short[] data2) {
        int w = csm.getWidth() * csm.getPixelStride();
        int h = csm.getHeight();
        int stride = csm.getScanlineStride();
        int stride2 = csm2.getScanlineStride();
        int diff, maxDiff = 0;
        for (int y = 0; y < h; y++) {
            for (int j = w, i = y * stride, i2 = y * stride2; j-- > 0; i++, i2++) {
                if (maxDiff < (diff = Math.abs(data[i] - data2[i2])))
                    maxDiff = diff;
            }
        }
        return maxDiff;
    }

    private int maxDiff(ComponentSampleModel csm, byte[][] banks,
                        ComponentSampleModel csm2, byte[][] banks2) {
        int w = csm.getWidth();
        int h = csm.getHeight();
        int bands = csm.getNumBands();
        int stride = csm.getScanlineStride();
        int pixelStride = csm.getPixelStride();
        int[] bankIndices = csm.getBankIndices();
        int[] bandOffsets = csm.getBandOffsets();
        int stride2 = csm2.getScanlineStride();
        int pixelStride2 = csm2.getPixelStride();
        int[] bankIndices2 = csm2.getBankIndices();
        int[] bandOffsets2 = csm2.getBandOffsets();
        int diff, maxDiff = 0;
        for (int b = 0; b < bands; b++) {
            byte[] bank = banks[bankIndices[b]];
            byte[] bank2 = banks2[bankIndices2[b]];
            int off = bandOffsets[b];
            int off2 = bandOffsets2[b];
            for (int y = 0; y < h; y++) {
                for (int x = w, i = y * stride + off, i2 = y * stride2 + off2;
                     x-- > 0; i += pixelStride, i2 += pixelStride2) {
                    if (maxDiff < (diff = Math.abs(bank[i] - bank2[i2])))
                        maxDiff = diff;
                }
            }
        }
        return maxDiff;
    }
}
