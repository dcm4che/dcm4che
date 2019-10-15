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
 * The Initial Developer of the Original Code is
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015-2019
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
 *
 */

package org.dcm4che3.imageio.codec.jpeg;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.XPEGParser;
import org.dcm4che3.imageio.codec.XPEGParserException;
import org.dcm4che3.util.SafeBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jun 2019
 */
public class JPEGParser implements XPEGParser {

    private static final long JPEG2000_SIGNATURE_BOX = 0x6a5020200d0a870aL; // jP\040\040<CR><LF><0x87><LF>;
    private static final int CONTIGUOUS_CODESTREAM_BOX = 0x6a703263; // jp2c;

    private final ByteBuffer buf = ByteBuffer.allocate(8);
    private final long codeStreamPosition;
    private long positionAfterAPP = -1L;
    private final Params params;

    public JPEGParser(SeekableByteChannel channel) throws IOException {
        seekCodeStream(channel);
        codeStreamPosition = channel.position();
        switch (readUShort(channel)) {
            case JPEG.FF_SOI:
                params = new JPEGParams(channel);
                break;
            case JPEG.FF_SOC:
                params = new JPEG2000Params(channel);
                break;
            default:
                throw new XPEGParserException("JPEG SOI/SOC marker not found");
        }
    }

    @Override
    public long getCodeStreamPosition() {
        return codeStreamPosition;
    }

    @Override
    public long getPositionAfterAPPSegments() {
        return positionAfterAPP;
    }

    @Override
    public Attributes getAttributes(Attributes attrs) {
        if (attrs == null)
            attrs = new Attributes(10);

        int samples = params.samplesPerPixel();
        attrs.setInt(Tag.SamplesPerPixel, VR.US, samples);
        if (samples == 3) {
            attrs.setString(Tag.PhotometricInterpretation, VR.CS, params.colorPhotometricInterpretation());
            attrs.setInt(Tag.PlanarConfiguration, VR.US, 0);
        } else {
            attrs.setString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME2");
        }
        attrs.setInt(Tag.Rows, VR.US, params.rows());
        attrs.setInt(Tag.Columns, VR.US, params.columns());
        int bitsStored = params.bitsStored();
        attrs.setInt(Tag.BitsAllocated, VR.US, bitsStored > 8 ? 16 : 8);
        attrs.setInt(Tag.BitsStored, VR.US, bitsStored);
        attrs.setInt(Tag.HighBit, VR.US, bitsStored - 1);
        attrs.setInt(Tag.PixelRepresentation, VR.US, params.pixelRepresentation());
        if (params.lossyImageCompression())
            attrs.setString(Tag.LossyImageCompression, VR.CS,  "01");
        return attrs;
    }

    @Override
    public String getTransferSyntaxUID() throws XPEGParserException {
        return params.transferSyntaxUID();
    }

    private void seekCodeStream(SeekableByteChannel channel) throws IOException {
        long startPos = channel.position();
        if (readInt(channel) != 12 || readLong(channel) != JPEG2000_SIGNATURE_BOX) {
            channel.position(startPos);
            return;
        }

        long size = channel.size();
        long boxPos = channel.position();
        long boxLengthType;
        while (((boxLengthType = readLong(channel)) & 0xffffffff) != CONTIGUOUS_CODESTREAM_BOX) {
            if ((boxPos += (boxLengthType >>> 32)) > size) {
                channel.position(startPos);
                return;
            }
            channel.position(boxPos);
        }
    }

    private int readUShort(SeekableByteChannel channel) throws IOException {
        SafeBuffer.clear(buf).limit(2);
        channel.read(buf);
        SafeBuffer.rewind(buf);
        return buf.getShort() & 0xffff;
    }

    private int readInt(SeekableByteChannel channel) throws IOException {
        SafeBuffer.clear(buf).limit(4);
        channel.read(buf);
        SafeBuffer.rewind(buf);
        return buf.getInt();
    }

    private long readLong(SeekableByteChannel channel) throws IOException {
        SafeBuffer.clear(buf);
        channel.read(buf);
        SafeBuffer.rewind(buf);
        return buf.getLong();
    }

    private void skip(SeekableByteChannel channel, long n) throws IOException {
        channel.position(channel.position() + n);
    }

    private static class Segment {
        final int marker;
        final int contentSize;

        private Segment(int marker, int contentSize) {
            this.marker = marker;
            this.contentSize = contentSize;
        }
    }

    private Segment nextSegment(SeekableByteChannel channel) throws IOException {
        int v = readInt(channel);
        requiresFF(channel, v >>> 24);
        int marker = (v >> 16) & 0xff;
        while (JPEG.isStandalone(marker)) {
            marker = v & 0xff;
            v = (v << 16) | readUShort(channel);
            requiresFF(channel, v >>> 24);
        }
        return new Segment(marker, (v & 0xffff) - 2);
    }

    private void requiresFF(SeekableByteChannel channel, int v) throws IOException {
        if (v != 0xff)
            throw new XPEGParserException(String.format("unexpected %2XH on position %d", v, channel.position() - 4));
    }

    private interface Params {
        int samplesPerPixel();
        int rows();
        int columns();
        int bitsStored();
        int pixelRepresentation();
        boolean lossyImageCompression();
        String colorPhotometricInterpretation();
        String transferSyntaxUID() throws XPEGParserException;
    }

    private class JPEGParams implements Params {

        final int sof;
        final ByteBuffer sofParams;
        final ByteBuffer sosParams;

        JPEGParams(SeekableByteChannel channel) throws IOException {
            Segment segment;
            while (JPEG.isAPP((segment = nextSegment(channel)).marker)) {
                skip(channel, segment.contentSize);
                positionAfterAPP = channel.position();
            }
            while (!JPEG.isSOF(segment.marker)) {
                skip(channel, segment.contentSize);
                segment = nextSegment(channel);
            }
            sof = segment.marker;
            channel.read(sofParams = ByteBuffer.allocate(segment.contentSize));
            while ((segment = nextSegment(channel)).marker != JPEG.SOS) {
                skip(channel, segment.contentSize);
            }
            channel.read(sosParams = ByteBuffer.allocate(segment.contentSize));
        }

        @Override
        public int samplesPerPixel() {
            return sofParams.get(5) & 0xff;
        }

        @Override
        public int rows() {
            return sofParams.getShort(1) & 0xffff;
        }

        @Override
        public int columns() {
            return sofParams.getShort(3) & 0xffff;
        }

        @Override
        public int bitsStored() {
            return sofParams.get(0) & 0xff;
        }

        @Override
        public int pixelRepresentation() {
            return 0;
        }

        @Override
        public boolean lossyImageCompression() {
            return !(sof == JPEG.SOF3 || (sof == JPEG.SOF55 && sosParams.get(3) == 0));
        }

        @Override
        public String colorPhotometricInterpretation() {
            return sof == JPEG.SOF3 || sof == JPEG.SOF55 ? "RGB" : "YBR_FULL_422";
        }

        @Override
        public String transferSyntaxUID() throws XPEGParserException {
            switch(sof) {
                case JPEG.SOF0:
                    return UID.JPEGBaseline1;
                case JPEG.SOF1:
                    return UID.JPEGExtended24;
                case JPEG.SOF2:
                    return UID.JPEGFullProgressionNonHierarchical1012Retired;
                case JPEG.SOF3:
                    return sosParams.get(3) == 1 ? UID.JPEGLossless : UID.JPEGLosslessNonHierarchical14;
                case JPEG.SOF55:
                    return sosParams.get(3) == 0 ? UID.JPEGLSLossless : UID.JPEGLSLossyNearLossless;
            }
            throw new XPEGParserException(String.format("JPEG SOF%d not supported", sof & 0xf));
        }
    }

    private class JPEG2000Params implements Params {

        final ByteBuffer sizParams;
        final ByteBuffer codParams;

        JPEG2000Params(SeekableByteChannel channel) throws IOException {
            Segment segment;
            while ((segment = nextSegment(channel)).marker != JPEG.SIZ) {
                skip(channel, segment.contentSize);
            }
            channel.read(sizParams = ByteBuffer.allocate(segment.contentSize));
            while ((segment = nextSegment(channel)).marker != JPEG.COD) {
                skip(channel, segment.contentSize);
            }
            channel.read(codParams = ByteBuffer.allocate(segment.contentSize));
        }

        @Override
        public int samplesPerPixel() {
            return sizParams.getShort(34) & 0xffff; // Csiz
        }

        @Override
        public int rows() {
            return sizParams.getInt(6) - sizParams.getInt(14); // Ysiz - YOsiz;
        }

        @Override
        public int columns() {
            return sizParams.getInt(2) - sizParams.getInt(10); // Xsiz - XOsiz;
        }

        @Override
        public int bitsStored() {
            return (sizParams.get(36) & 0x7f) + 1; // Ssiz
        }

        @Override
        public int pixelRepresentation() {
            return sizParams.get(36) < 0 ? 1 : 0; // Ssiz
        }

        @Override
        public boolean lossyImageCompression() {
            return codParams.get(9) == 0; // Wavelet Transformation
        }

        @Override
        public String colorPhotometricInterpretation() {
            return codParams.get(4) == 0 ? "RGB"    // Multiple component transformation
                    : lossyImageCompression() ? "YBR_ICT" : "YBR_RCT";
        }

        @Override
        public String transferSyntaxUID() {
            return lossyImageCompression() ? UID.JPEG2000 : UID.JPEG2000LosslessOnly;
        }
    }
}
