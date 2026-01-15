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
 * Java(TM), hosted at https://github.com/dcm4che.
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

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Value;
import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLS;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLSImageInputStream;
import org.dcm4che3.imageio.stream.RAFFileImageInputStream;
import org.dcm4che3.imageio.stream.SegmentedInputImageStream;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Decompressor {

    private static final Logger LOG = LoggerFactory.getLogger(Decompressor.class);

    protected final Attributes dataset;
    protected final String tsuid;
    protected final TransferSyntaxType tstype;
    protected Fragments pixeldataFragments;
    protected File file;
    protected int rows;
    protected int cols;
    protected int samples;
    protected PhotometricInterpretation pmi;
    protected PhotometricInterpretation pmiAfterDecompression;
    protected int bitsAllocated;
    protected int bitsStored;
    protected boolean banded;
    protected boolean signed;
    protected int frames;
    protected int frameLength;
    protected int length;
    protected BufferedImage bi;
    protected ImageReader decompressor;
    protected ImageReadParam readParam;
    protected PatchJPEGLS patchJpegLS;
    protected ImageDescriptor imageDescriptor;

    public Decompressor(Attributes dataset, String tsuid) {
        if (tsuid == null)
            throw new NullPointerException("tsuid");

        this.dataset = dataset;
        this.tsuid = tsuid;
        this.tstype = TransferSyntaxType.forUID(tsuid);
        Object pixeldata = dataset.getValue(Tag.PixelData);
        if (pixeldata == null)
            return;

        if (tstype == null)
            throw new IllegalArgumentException("Unknown Transfer Syntax: " + tsuid);
        this.rows = dataset.getInt(Tag.Rows, 0);
        this.cols = dataset.getInt(Tag.Columns, 0);
        this.samples = dataset.getInt(Tag.SamplesPerPixel, 0);
        this.pmi = PhotometricInterpretation.fromString(
                dataset.getString(Tag.PhotometricInterpretation, "MONOCHROME2"));
        this.pmiAfterDecompression = pmi;
        this.bitsAllocated = dataset.getInt(Tag.BitsAllocated, 8);
        this.bitsStored = dataset.getInt(Tag.BitsStored, bitsAllocated);
        this.banded = dataset.getInt(Tag.PlanarConfiguration, 0) != 0;
        this.signed = dataset.getInt(Tag.PixelRepresentation, 0) != 0;
        this.frames = dataset.getInt(Tag.NumberOfFrames, 1);
        this.frameLength = rows * cols * samples * bitsAllocated / 8;
        this.length = frameLength * frames;
        this.imageDescriptor = new ImageDescriptor(dataset);
        
        if (pixeldata instanceof Fragments) {
            if (!tstype.isPixeldataEncapsulated())
                throw new IllegalArgumentException("Encapusulated Pixel Data"
                        + "with Transfer Syntax: " + tsuid);
            this.pixeldataFragments = (Fragments) pixeldata;

            int numFragments = pixeldataFragments.size();
            if (frames == 1 ? (numFragments < 2)
                            : (numFragments != frames + 1))
                throw new IllegalArgumentException(
                        "Number of Pixel Data Fragments: "
                        + numFragments + " does not match " + frames);

            this.file = ((BulkData) pixeldataFragments.get(1)).getFile();
            ImageReaderFactory.ImageReaderParam param =
                    ImageReaderFactory.getImageReaderParam(tsuid);
            if (param == null)
                throw new UnsupportedOperationException(
                        "Unsupported Transfer Syntax: " + tsuid);

            this.decompressor = ImageReaderFactory.getImageReader(param);
            LOG.debug("Decompressor: {}", decompressor.getClass().getName());
            this.readParam = decompressor.getDefaultReadParam();
            this.patchJpegLS = param.patchJPEGLS;
            this.pmiAfterDecompression = pmi.isYBR() && TransferSyntaxType.isYBRCompression(tsuid)
                    ? PhotometricInterpretation.RGB
                    : pmi;
        } else {
            this.file = ((BulkData) pixeldata).getFile();
        }
    }

    public void dispose() {
        if (decompressor != null)
            decompressor.dispose();

        decompressor = null;
    }

    public boolean decompress() {
        if (decompressor == null)
            return false;
 
        if (tstype == TransferSyntaxType.RLE)
            bi = createBufferedImage(bitsStored, true, signed);

        dataset.setValue(Tag.PixelData, VR.OW, new Value() {

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Decompressor.this.writeTo(out);
                return out.toByteArray();
            }

            @Override
            public void writeTo(DicomOutputStream out, VR vr) throws IOException {
                Decompressor.this.writeTo(out);
            }

            @Override
            public int calcLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
                return getEncodedLength(encOpts, explicitVR, vr);
            }

            @Override
            public int getEncodedLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
                return (length + 1) & ~1;
            }
        });
        if (samples > 1) {
            dataset.setString(Tag.PhotometricInterpretation, VR.CS, 
                    pmiAfterDecompression.toString());

            dataset.setInt(Tag.PlanarConfiguration, VR.US,
                    tstype.getPlanarConfiguration());
        }
        return true;
    }

    public static boolean decompress(Attributes dataset, String tsuid) {
        return new Decompressor(dataset, tsuid).decompress();
    }

    protected BufferedImage createBufferedImage(int bitsStored,
            boolean banded, boolean signed) {
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
                        samples, cols * samples, bandOffsets());
        WritableRaster raster = Raster.createWritableRaster(sm, null);
        return new BufferedImage(cm, raster, false, null);
    }

    private int[] bandOffsets() {
        int[] offsets = new int[samples];
        for (int i = 0; i < samples; i++)
            offsets[i] = i;
        return offsets;
    }

    public void writeTo(OutputStream out) throws IOException {
        ImageInputStream iis = createImageInputStream();
        try {
            for (int i = 0; i < frames; ++i)
                writeFrameTo(iis, i, out);
            if ((length & 1) != 0)
                out.write(0);
        } finally {
            try { iis.close(); } catch (IOException ignore) {}
            decompressor.dispose();
        }
    }

    public FileImageInputStream createImageInputStream()
            throws IOException {
        return new RAFFileImageInputStream(file);
    }

    public void writeFrameTo(ImageInputStream iis, int frameIndex,
            OutputStream out) throws IOException {
        writeTo(decompressFrame(iis, frameIndex).getRaster(), out);
    }

    @SuppressWarnings("resource")
    protected BufferedImage decompressFrame(ImageInputStream iis, int index)
            throws IOException {
        SegmentedInputImageStream siis =
                new SegmentedInputImageStream(iis, pixeldataFragments, index);
        siis.setImageDescriptor(imageDescriptor);
        decompressor.setInput(patchJpegLS != null
                ? new PatchJPEGLSImageInputStream(siis, patchJpegLS)
                : siis);
        readParam.setDestination(bi);
        long start = System.currentTimeMillis();
        bi = decompressor.read(0, readParam);
        long end = System.currentTimeMillis();
        if (LOG.isDebugEnabled())
            LOG.debug("Decompressed frame #{} 1:{} in {} ms", 
                    new Object[] {index + 1,
                    (float) sizeOf(bi) / siis.getStreamPosition(),
                    end - start });
        return bi;
    }

    static int sizeOf(BufferedImage bi) {
        DataBuffer db = bi.getData().getDataBuffer();
        return db.getSize() * db.getNumBanks()
                * (DataBuffer.getDataTypeSize(db.getDataType()) >>> 3);
    }

    private void writeTo(Raster raster, OutputStream out) throws IOException {
        SampleModel sm = raster.getSampleModel();
        DataBuffer db = raster.getDataBuffer();
        switch (db.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            writeTo(sm, ((DataBufferByte) db).getBankData(), out);
            break;
        case DataBuffer.TYPE_USHORT:
            writeTo(sm, ((DataBufferUShort) db).getData(), out);
            break;
        case DataBuffer.TYPE_SHORT:
            writeTo(sm, ((DataBufferShort) db).getData(), out);
            break;
        case DataBuffer.TYPE_INT:
            writeTo(sm, ((DataBufferInt) db).getData(), out);
            break;
        default:
            throw new UnsupportedOperationException(
                    "Unsupported Datatype: " + db.getDataType());
        }
    }

    private void writeTo(SampleModel sm, byte[][] bankData, OutputStream out)
            throws IOException {
        int h = sm.getHeight();
        int w = sm.getWidth();
        ComponentSampleModel csm = (ComponentSampleModel) sm;
        int len = w * csm.getPixelStride();
        int stride = csm.getScanlineStride();
        if (csm.getBandOffsets()[0] != 0)
            bgr2rgb(bankData[0]);
        if (imageDescriptor.getBitsAllocated() == 16) {
            byte[] buf = new byte[len << 1];
            int j0 = 0;
            if (out instanceof DicomOutputStream) {
            	j0 = ((DicomOutputStream)out).isBigEndian() ? 1 : 0;
            }
            for (byte[] b : bankData)
                for (int y = 0, off = 0; y < h; ++y, off += stride) {
                	out.write(to16BitsAllocated(b, off, len, buf, j0));
                }
        } else {
            for (byte[] b : bankData)
                for (int y = 0, off = 0; y < h; ++y, off += stride)
                	out.write(b, off, len);
        }
    }
    
    private byte[] to16BitsAllocated(byte[] b, int off, int len, byte[] buf, int j0) {
        for (int i = 0, j = j0; i < len; i++, j++, j++) {
            buf[j] = b[off + i];
        }
        return buf;
    }

    private static void bgr2rgb(byte[] bs) {
        for (int i = 0, j = 2; j < bs.length; i += 3, j += 3) {
            byte b = bs[i];
            bs[i] = bs[j];
            bs[j] = b;
        }
    }

    private static void writeTo(SampleModel sm, short[] data, OutputStream out)
            throws IOException {
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
            out.write(b);
        }
    }

    private static void writeTo(SampleModel sm, int[] data, OutputStream out)
            throws IOException {
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
            out.write(b);
        }
    }

}
