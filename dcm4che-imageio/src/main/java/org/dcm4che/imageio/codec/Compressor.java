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

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.BulkDataLocator;
import org.dcm4che.data.Fragments;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
import org.dcm4che.data.Value;
import org.dcm4che.imageio.codec.ImageWriterFactory.ImageWriterParam;
import org.dcm4che.io.DicomEncodingOptions;
import org.dcm4che.io.DicomOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Compressor extends Decompressor implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(Compressor.class);

    private BulkDataLocator pixeldata;
    private ImageWriter compressor;
    private ImageWriteParam compressParam;
    private ImageWriterParam compressorParam;
    private ImageInputStream iis;
    private IOException ex;

    public Compressor(Attributes dataset, String tsuid) {
        super(dataset, tsuid);

        Object pixeldata = dataset.getValue(Tag.PixelData);
        if (pixeldata == null)
            return;

        if (pixeldata instanceof BulkDataLocator) {
            this.pixeldata = (BulkDataLocator) pixeldata;
            if (pmi.isSubSambled())
                throw new UnsupportedOperationException(
                        "Unsupported Photometric Interpretation: " + pmi);
            if (this.pixeldata.length < length)
                throw new IllegalArgumentException(
                        "Pixel data too short: " + this.pixeldata.length
                        + " instead " + length + " bytes");
        }

    }

    public boolean compress(String tsuid, Map<String,Object> params)
            throws IOException {

        if (tsuid == null)
            throw new NullPointerException("desttsuid");

        if (frames == 0)
            return false;

        ImageWriterFactory.ImageWriterParam param =
                ImageWriterFactory.getImageWriterParam(tsuid);
        if (param == null)
            throw new UnsupportedOperationException(
                    "Unsupported Transfer Syntax: " + tsuid);

        this.compressor = ImageWriterFactory.getImageWriter(param);
        this.compressorParam = param;
        this.compressParam = compressor.getDefaultWriteParam();
        param.initImageWriteParam(compressParam);
        if (params != null)
            ImageWriterFactory.initImageWriteParam(compressParam, params);

        adjustDestination();
        adjustAttributes();
        Fragments compressedPixeldata = new Fragments(VR.OB, false, frames + 1);
        compressedPixeldata.add(Value.NULL);
        for (int i = 0; i < frames; i++)
            compressedPixeldata.add(new CompressedFrame(i));
        return true;
    }

    public void close() {
        if (iis != null)
            try { iis.close(); } catch (IOException ignore) {}
        dispose();
    }

    @Override
    public void dispose() {
        super.dispose();

        if (compressor != null)
            compressor.dispose();

        compressor = null;
    }

    @Override
    protected void adjustAttributes() {
        super.adjustAttributes();
        if (compressorParam != null && samples > 1
                && compressorParam.photometricInterpretation != null) {
            dataset.setString(Tag.PhotometricInterpretation, VR.CS, 
                    compressorParam.photometricInterpretation);
        }
    }

    private class CompressedFrame implements Value {

        private int frameIndex;
        private CacheOutputStream cacheout = new CacheOutputStream();
        private MemoryCacheImageOutputStream cache;

        public CompressedFrame(int frameIndex) {
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
            return getEncodedLength(encOpts, vr);
        }

        @Override
        public int getEncodedLength(DicomEncodingOptions encOpts, VR vr) {
            try {
                compress();
            } catch (IOException e) {
                return -1;
            }
            return (((int) cache.length()) + 1) & ~1;
        }
        
        private void writeTo(OutputStream out) throws IOException {
            compress();
            try {
                cacheout.set(out);
                cache.flush();
                if ((cache.length() & 1) != 0)
                    out.write(0);
            } finally {
                try { cache.close(); } catch (IOException ignore) {}
                cache = null;
            }
        }

        private void compress() throws IOException {
            if (cache != null)
                return;

            if (ex != null)
                throw ex;

            try {
                BufferedImage bi = Compressor.this.readFrame(frameIndex);
                cache = new MemoryCacheImageOutputStream(cacheout);
                compressor.setOutput(cache);
                long start = System.currentTimeMillis();
                compressor.write(null, new IIOImage(bi, null, null), compressParam);
                long end = System.currentTimeMillis();
                if (LOG.isDebugEnabled())
                    LOG.debug("Compressed frame #{} {}:1 in {} ms", 
                            new Object[] {frameIndex + 1,
                            (float) sizeOf(bi) / cache.length(),
                            end - start });
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
            iis = new FileImageInputStream(file);

        if (decompressor != null)
            return decompressFrame(iis, frameIndex);

        iis.seek(pixeldata.offset + frameLength * frameIndex);
        DataBuffer db = destination.getRaster().getDataBuffer();
        switch (db.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            for (byte[] bs : ((DataBufferByte) db).getBankData())
                iis.readFully(bs);
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
        return destination;
    }

    private void readFully(short[] data) throws IOException {
        iis.readFully(data, 0, data.length);
    }

}
