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
package org.dcm4che.imageio.codec;

import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.BulkDataLocator;
import org.dcm4che.data.Fragments;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
import org.dcm4che.data.Value;
import org.dcm4che.image.PhotometricInterpretation;
import org.dcm4che.imageio.stream.SegmentedInputImageStream;
import org.dcm4che.io.DicomEncodingOptions;
import org.dcm4che.io.DicomOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Decompressor implements Value {

    private static final Logger LOG = LoggerFactory.getLogger(Decompressor.class);

    private final PhotometricInterpretation pmi;
    private final Fragments pixeldataFragments;
    private final File file;
    private final ImageReader decompressor;
    private final int length;
    private final int frames;

    private Decompressor(Attributes dataset, PhotometricInterpretation pmi,
            Fragments pixeldataFragments, ImageReader decompressor) {
        this.frames = dataset.getInt(Tag.NumberOfFrames, 1);
        this.length = dataset.getInt(Tag.Rows, 0)
                    * dataset.getInt(Tag.Columns, 0)
                    * dataset.getInt(Tag.SamplesPerPixel, 0)
                    * (dataset.getInt(Tag.BitsAllocated, 8)>>>3)
                    * frames;
        this.pmi = pmi;
        this.pixeldataFragments = pixeldataFragments;
        this.decompressor = decompressor;
        
        int numFragments = pixeldataFragments.size();
        if (frames == 1 ? (numFragments < 2)
                        : (numFragments != frames + 1))
            throw new IllegalArgumentException(
                    "Number of Pixel Data Fragments: "
                    + numFragments + " does not match " + frames);

        Object o = pixeldataFragments.get(1);
        if (!(o instanceof BulkDataLocator)) {
            throw new IllegalArgumentException(
                    "Unsupported Pixel Data Fragment: " + o.getClass());
        }
        BulkDataLocator bdl = (BulkDataLocator) o;
        try {
            this.file = new File(new URI(bdl.uri));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI:" + bdl.uri);
        }
    }

    public static boolean decompress(Attributes dataset, String tsuid) throws IOException {
        Object pixeldata = dataset.getValue(Tag.PixelData);
        if (pixeldata == null)
            return false;

        if (!(pixeldata instanceof Fragments))
            return false;

        ImageReaderFactory.ImageReaderParam param =
                ImageReaderFactory.getImageReaderParam(tsuid);
        if (param == null)
            throw new IOException("Unsupported Transfer Syntax: " + tsuid);

        
        PhotometricInterpretation pmi = PhotometricInterpretation.fromString(
                dataset.getString(Tag.PhotometricInterpretation, "MONOCHROME2"));

        if (pmi.changeToRGBonDecompress())
            dataset.setString(Tag.PhotometricInterpretation, VR.CS, "RGB");

        dataset.setValue(Tag.PixelData, VR.OW,
                new Decompressor(dataset, pmi, (Fragments) pixeldata,
                        ImageReaderFactory.getImageReader(param)));
        return true;
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
        return getEncodedLength(encOpts, vr);
    }

    @Override
    public int getEncodedLength(DicomEncodingOptions encOpts, VR vr) {
        return (length + 1) & ~1;
    }
    
    private void writeTo(OutputStream out) throws IOException {
        ImageInputStream iis = new FileImageInputStream(file);
        try {
            for (int i = 0; i < frames; ++i) {
                decompressor.reset();
                decompressor.setInput(
                        new SegmentedInputImageStream(iis, pixeldataFragments, i));
                if (LOG.isDebugEnabled())
                    LOG.debug("Start decompressing frame #" + (i + 1));
                Raster raster = !pmi.changeToRGBonDecompress() && decompressor.canReadRaster()
                        ? decompressor.readRaster(0, null)
                        : decompressor.read(0, null).getRaster();
                if (LOG.isDebugEnabled())
                    LOG.debug("Finished decompressing frame #" + (i + 1));
                writeTo(raster, out);
            }
            if ((length & 1) != 0)
                out.write(0);
        } finally {
            try { iis.close(); } catch (IOException ignore) {}
        }
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
        for (byte[] b : bankData)
            for (int y = 0, off = 0; y < h; ++y, off += stride)
                out.write(b, off, len);
    }

    private void bgr2rgb(byte[] bs) {
        for (int i = 0, j = 2; j < bs.length; i += 3, j += 3) {
            byte b = bs[i];
            bs[i] = bs[j];
            bs[j] = b;
        }
    }

    private void writeTo(SampleModel sm, short[] data, OutputStream out)
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

    private void writeTo(SampleModel sm, int[] data, OutputStream out)
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
