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

import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Value;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLS;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLSImageInputStream;
import org.dcm4che3.imageio.stream.SegmentedImageInputStream;
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

    private final Attributes dataset;
    private Object pixels;
    private final String tsuid;
    private final TransferSyntaxType tsType;
    private ImageParams imageParams;
    private BufferedImage bi;
    private ImageReader imageReader;
    private ImageReadParam readParam;
    private PatchJPEGLS patchJPEGLS;

    public Decompressor(Attributes dataset, String tsuid) {
        if (tsuid == null)
            throw new NullPointerException("tsuid");

        this.dataset = dataset;
        this.tsuid = tsuid;
        this.tsType = TransferSyntaxType.forUID(tsuid);
        this.pixels = dataset.getValue(Tag.PixelData);
        if (this.pixels == null)
            return;

        if (tsType == null)
            throw new IllegalArgumentException("Unknown Transfer Syntax: " + tsuid);

        this.imageParams = new ImageParams(dataset);
        int frames = imageParams.getFrames();

        if (this.pixels instanceof Fragments) {
            if (!tsType.isPixeldataEncapsulated())
                throw new IllegalArgumentException("Encapusulated Pixel Data"
                        + "with Transfer Syntax: " + tsuid);

            int numFragments = ((Fragments)this.pixels).size();
            if (frames == 1 ? (numFragments < 2)
                            : (numFragments != frames + 1))
                throw new IllegalArgumentException(
                        "Number of Pixel Data Fragments: "
                        + numFragments + " does not match " + frames);

            ImageReaderFactory.ImageReaderParam param =
                    ImageReaderFactory.getImageReaderParam(tsuid);
            if (param == null)
                throw new UnsupportedOperationException(
                        "Unsupported Transfer Syntax: " + tsuid);

            this.imageReader = ImageReaderFactory.getImageReader(param);
            LOG.debug("Decompressor: {}", imageReader.getClass().getName());
            this.readParam = imageReader.getDefaultReadParam();
            this.patchJPEGLS = param.patchJPEGLS;
        }
    }

    public void dispose() {
        if (imageReader != null)
            imageReader.dispose();

        imageReader = null;
    }

    public boolean decompress() {
        if (imageReader == null)
            return false;

        imageParams.decompress(dataset, tsType);
        if (tsType == TransferSyntaxType.RLE)
            bi = BufferedImageUtils.createBufferedImage(imageParams, tsType);

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
                return imageParams.getEncodedLength();
            }

            @Override
            public int getEncodedLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
                return imageParams.getEncodedLength();
            }
        });
        return true;
    }

    public static boolean decompress(Attributes dataset, String tsuid) {
        return new Decompressor(dataset, tsuid).decompress();
    }

    public void writeTo(OutputStream out) throws IOException {
        int frames = imageParams.getFrames();
        try {
            for (int i = 0; i < frames; ++i) {
                ImageInputStream iis = createImageInputStream(i);
                writeFrameTo(iis, i, out);
                close(iis);
            }
            if (imageParams.paddingNull())
                out.write(0);
        } finally {

            imageReader.dispose();
        }
    }

    private void close (ImageInputStream iis) {
        try { iis.close(); } catch (IOException ignore) {}
    }

    public void writeFrameTo(ImageInputStream iis, int frameIndex,
            OutputStream out) throws IOException {
        BufferedImageUtils.writeTo(decompressFrame(iis, frameIndex), out);
    }

    @SuppressWarnings("resource")
    protected BufferedImage decompressFrame(ImageInputStream iis, int index)
            throws IOException {

        if (pixels instanceof Fragments && ((Fragments) pixels).get(index+1) instanceof BulkData)
            iis = SegmentedImageInputStream.ofFrame(iis, (Fragments) pixels, index, imageParams.getFrames());

        imageReader.setInput(patchJPEGLS != null
                ? new PatchJPEGLSImageInputStream(iis, patchJPEGLS)
                : iis);
        readParam.setDestination(bi);
        long start = System.currentTimeMillis();
        bi = imageReader.read(0, readParam);
        long end = System.currentTimeMillis();
        if (LOG.isDebugEnabled())
            LOG.debug("Decompressed frame #{} 1:{} in {} ms", 
                    new Object[] {index + 1,
                    (float) BufferedImageUtils.sizeOf(bi) / iis.getStreamPosition(),
                    end - start });
        return bi;
    }

    public ImageInputStream createImageInputStream() throws IOException {
        return createImageInputStream(0);
    }

    public ImageInputStream createImageInputStream(int frameIndex) throws IOException {

        if (pixels instanceof Fragments) {
            Fragments pixelFragments = (Fragments) pixels;
            if (pixelFragments.get(frameIndex + 1) instanceof BulkData)
                return new FileImageInputStream(((BulkData) pixelFragments.get(frameIndex + 1)).getFile());
            else if (pixelFragments.get(frameIndex + 1) instanceof byte[])
                return new MemoryCacheImageInputStream(new ByteArrayInputStream((byte[])pixelFragments.get(frameIndex + 1)));
            else
                return null;
        }

        if (pixels instanceof byte[]) {
            return new MemoryCacheImageInputStream(new ByteArrayInputStream((byte[])pixels));
        }

        return null;
    }


}
