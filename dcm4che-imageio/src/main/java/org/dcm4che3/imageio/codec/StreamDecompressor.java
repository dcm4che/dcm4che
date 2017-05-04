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

import org.dcm4che3.data.*;
import org.dcm4che3.imageio.codec.ImageReaderFactory.ImageReaderItem;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLS;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLSImageInputStream;
import org.dcm4che3.imageio.stream.SegmentedImageInputStream;
import org.dcm4che3.io.DicomInputHandler;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.image.*;
import java.io.IOException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jan 2015.
 *
 * @deprecated This is prototype code. StreamDecompressor will be replaced by a Transcoder that supports both stream
 * compression and decompression. For now you can continue using {@link Decompressor} for non-stream decompression.
 */
@Deprecated
public class StreamDecompressor implements CoerceAttributes {

    private static final Logger LOG = LoggerFactory.getLogger(StreamDecompressor.class);

    protected final DicomInputStream in;
    protected final DicomOutputStream out;
    protected final String tsuid;
    protected final TransferSyntaxType tsType;
    protected final Attributes dataset;
    protected ImageReader decompressor;
    protected PatchJPEGLS patchJPEGLS;
    protected boolean pixeldataProcessed;
    protected CoerceAttributes coerceAttributes = this;

    public StreamDecompressor(DicomInputStream in, String tsuid, DicomOutputStream out) {
        this.in = in;
        this.out = out;
        this.tsuid = tsuid;
        this.tsType = TransferSyntaxType.forUID(tsuid);
        if (tsType == null)
            throw new IllegalArgumentException("Unknown Transfer Syntax: " + tsuid);
        if (tsType.isPixeldataEncapsulated()) {
            ImageReaderItem readerItem = ImageReaderFactory.getImageReader(tsuid);
            if (readerItem == null)
                throw new IllegalArgumentException("Unsupported Transfer Syntax: " + tsuid);
            this.decompressor = readerItem.getImageReader();
            LOG.debug("Decompressor: {}", decompressor.getClass().getName());
            this.patchJPEGLS = readerItem.getImageReaderParam().getPatchJPEGLS();
        }
        this.dataset = new Attributes(in.bigEndian(), 64);
    }

    public CoerceAttributes getCoerceAttributes() {
        return coerceAttributes;
    }

    public void setCoerceAttributes(CoerceAttributes coerceAttributes) {
        if (coerceAttributes == null)
            throw new NullPointerException();

        this.coerceAttributes = coerceAttributes;
    }

    @Override
    public Attributes coerce(Attributes attrs) {
        return attrs;
    }

    public boolean decompress() throws IOException {
        in.setDicomInputHandler(handler);
        in.readAttributes(dataset, -1, -1);
        (pixeldataProcessed ? dataset : coerceAttributes.coerce(dataset)).writeTo(out);
        return pixeldataProcessed && decompressor != null;
    }

    public void dispose() {
        if (decompressor != null)
            decompressor.dispose();
    }

    protected void onPixelData(DicomInputStream dis, Attributes attrs) throws IOException {
        int tag = dis.tag();
        VR vr = dis.vr();
        int len = dis.length();
        if (len == -1) {
            if (!tsType.isPixeldataEncapsulated()) {
                throw new IOException("Unexpected encapsulated Pixel Data");
            }
            BufferedImage bi = null;
            ImageParams imageParams = new ImageParams(dataset);
            imageParams.decompress(attrs, tsType);
            if (tsType == TransferSyntaxType.RLE)
                bi = BufferedImageUtils.createBufferedImage(imageParams, null);
            coerceAttributes.coerce(attrs).writeTo(out);
            attrs.clear();
            out.writeHeader(Tag.PixelData, VR.OW, imageParams.getEncodedLength());
            decompressFrames(dis, imageParams, bi);
            if (imageParams.paddingNull())
                out.write(0);
        } else {
            if (tsType.isPixeldataEncapsulated())
                throw new IOException("Pixel Data not encapsulated");
            coerceAttributes.coerce(attrs).writeTo(out);
            attrs.clear();
            out.writeHeader(tag, vr, len);
            StreamUtils.copy(dis, out, len);
        }
        pixeldataProcessed = true;
    }

    protected void decompressFrames(DicomInputStream dis, ImageParams imageParams, BufferedImage bi)
            throws IOException {
        dis.readHeader();
        dis.skipFully(dis.length());
        long pos = dis.getPosition();
        MemoryCacheImageInputStream iis = new MemoryCacheImageInputStream(dis);
        byte[] header = new byte[8];
        boolean singleFrame = imageParams.getFrames() == 1;
        for (int i = 0; i < imageParams.getFrames(); i++) {
            iis.readFully(header);
            SegmentedImageInputStream siis = new SegmentedImageInputStream(
                    iis, iis.getStreamPosition(), ByteUtils.bytesToIntLE(header, 4), singleFrame);
            decompressor.setInput(patchJPEGLS != null
                    ? new PatchJPEGLSImageInputStream(siis, patchJPEGLS)
                    : siis);
            ImageReadParam readParam = decompressor.getDefaultReadParam();
            readParam.setDestination(bi);
            long start = System.currentTimeMillis();
            bi = decompressor.read(0, readParam);
            long lastSegmentEnd = siis.getLastSegmentEnd();
            iis.seek(lastSegmentEnd);
            iis.flushBefore(lastSegmentEnd);
            long end = System.currentTimeMillis();
            if (LOG.isDebugEnabled())
                LOG.debug("Decompressed frame #{} 1:{} in {} ms",
                        i + 1, (float) BufferedImageUtils.sizeOf(bi) / siis.getStreamPosition(), end - start );
            writeFrame(bi);
        }
        iis.readFully(header);
        dis.setPosition(pos + iis.getStreamPosition());
    }

    protected void writeFrame(BufferedImage bi) throws IOException {
        BufferedImageUtils.writeTo(bi, out);
    }

    private final DicomInputHandler handler = new DicomInputHandler() {
        @Override
        public void readValue(DicomInputStream dis, Attributes attrs) throws IOException {
            if (dis.tag() == Tag.PixelData && dis.level() == 0)
                onPixelData(dis, attrs);
            else
                dis.readValue(dis, attrs);
        }

        @Override
        public void readValue(DicomInputStream dis, Sequence seq) throws IOException {
            dis.readValue(dis, seq);
        }

        @Override
        public void readValue(DicomInputStream dis, Fragments frags) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void startDataset(DicomInputStream dis) throws IOException {}

        @Override
        public void endDataset(DicomInputStream dis) throws IOException {}
    };
}
