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
import org.dcm4che3.imageio.codec.mp4.MP4FileType;
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
    public String toString() {
        return "JPEGParser{" +
                "codeStreamPos=" + codeStreamPosition +
                ", posAfterAPP=" + positionAfterAPP +
                ", " + params +
                '}';
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
    public MP4FileType getMP4FileType() {
        return null;
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
    public String getTransferSyntaxUID(boolean fragmented) throws XPEGParserException {
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
        while ((int) (boxLengthType = readLong(channel)) != CONTIGUOUS_CODESTREAM_BOX) {
            long boxLength = boxLengthType >>> 32;
            if (boxLength == 1) boxLength = readLong(channel);
            if (boxLength <= 0 || (boxPos += boxLength) > size) {
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
                    return UID.JPEGBaseline8Bit;
                case JPEG.SOF1:
                    return UID.JPEGExtended12Bit;
                case JPEG.SOF2:
                    return UID.JPEGFullProgressionNonHierarchical1012;
                case JPEG.SOF3:
                    return sosParams.get(3) == 1 ? UID.JPEGLosslessSV1 : UID.JPEGLossless;
                case JPEG.SOF55:
                    return sosParams.get(3) == 0 ? UID.JPEGLSLossless : UID.JPEGLSNearLossless;
            }
            throw new XPEGParserException(String.format("JPEG SOF%d not supported", sof & 0xf));
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(512);
            sb.append("JPEGParams{\n  SOF").append(sof & 0xf).append("{Lf=").append(sofParams.limit() + 2);
            sb.append(", P=").append(sofParams.get(0) & 0xff);
            sb.append(", Y=").append(sofParams.getShort(1) & 0xffff);
            sb.append(", X=").append(sofParams.getShort(3) & 0xffff);
            sb.append(", Nf=").append(sofParams.get(5) & 0xff);
            sb.append(", Comps{");
            for (int i = 6; i + 2 < sofParams.limit();) {
                sb.append('{').append(sofParams.get(i++) & 0xff)
                        .append(':').append((sofParams.get(i) & 0xf0) >>> 4)
                        .append(':').append(sofParams.get(i++) & 0xf)
                        .append(':').append(sofParams.get(i++) & 0xff)
                        .append('}').append(',');;
            }
            sb.setLength(sb.length()-1);
            sb.append("}},\n  SOS{Ls=").append(sosParams.limit() + 2);
            sb.append(", Ns=").append(sosParams.get(0) & 0xff);
            sb.append(", Comps{");
            int i = 1;
            while (i + 4 < sosParams.limit()) {
                sb.append('{').append(sosParams.get(i++) & 0xff)
                        .append(':').append((sosParams.get(i) & 0xf0) >>> 4)
                        .append(':').append(sosParams.get(i++) & 0xf)
                        .append('}').append(',');
            }
            sb.setLength(sb.length()-1);
            sb.append("}, Ss=").append(sosParams.get(i++) & 0xff);
            sb.append(", Se=").append(sosParams.get(i++) & 0xff);
            sb.append(", Ah=").append((sosParams.get(i) & 0xf0) >>> 4);
            sb.append(", Al=").append(sosParams.get(i) & 0xf);
            sb.append("}}");
            return sb.toString();
        }
    }

    private class JPEG2000Params implements Params {

        final ByteBuffer sizParams;
        final ByteBuffer codParams;
        final boolean tlm;

        JPEG2000Params(SeekableByteChannel channel) throws IOException {
            ByteBuffer sizParams = null;
            ByteBuffer codParams = null;
            boolean tlm = false;
            Segment segment;
            do {
                segment = nextSegment(channel);
                switch (segment.marker) {
                    case JPEG.SIZ:
                        channel.read(sizParams = ByteBuffer.allocate(segment.contentSize));
                        break;
                    case JPEG.COD:
                        channel.read(codParams = ByteBuffer.allocate(segment.contentSize));
                        break;
                    case JPEG.TLM:
                        tlm = true;
                    default:
                        skip(channel, segment.contentSize);
                }
            } while (segment.marker != JPEG.SOT);
            this.sizParams = sizParams;
            this.codParams = codParams;
            this.tlm = tlm;
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
            return (sizParams.getShort(0) & 0b0100_0000_0000_0000) != 0
                    ? lossyImageCompression() ? UID.HTJ2K : isRPCL() ? UID.HTJ2KLosslessRPCL : UID.HTJ2KLossless
                    : lossyImageCompression() ? UID.JPEG2000 : UID.JPEG2000Lossless;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(512);
            sb.append("JPEG2000Params{\n  SIZ{Lsiz=").append(sizParams.limit() + 2);
            sb.append(", Rsiz=").append(toBinaryString(sizParams.getShort(0) & 0xffff));
            sb.append(", Xsiz=").append(sizParams.getInt(2) & 0xffffffffL);
            sb.append(", Ysiz=").append(sizParams.getInt(6) & 0xffffffffL);
            sb.append(", XOsiz=").append(sizParams.getInt(10) & 0xffffffffL);
            sb.append(", YOsiz=").append(sizParams.getInt(14) & 0xffffffffL);
            sb.append(", XTsiz=").append(sizParams.getInt(18) & 0xffffffffL);
            sb.append(", YTsiz=").append(sizParams.getInt(22) & 0xffffffffL);
            sb.append(", XTOsiz=").append(sizParams.getInt(26) & 0xffffffffL);
            sb.append(", YTOsiz=").append(sizParams.getInt(30) & 0xffffffffL);
            sb.append(", numXTiles=").append(numXTiles());
            sb.append(", numYTiles=").append(numYTiles());
            sb.append(", Csiz=").append(sizParams.getShort(34) & 0xffff);
            sb.append(", Comps{");
            for (int i = 36; i + 2 < sizParams.limit();) {
                sb.append('{');
                int ssiz = sizParams.get(i++);
                if (ssiz < 0) sb.append('±');
                sb.append((ssiz & 0x7f) + 1)
                        .append(':')
                        .append(sizParams.get(i++) & 0xff)
                        .append(':')
                        .append(sizParams.get(i++) & 0xff)
                        .append('}').append(',');
            }
            sb.setLength(sb.length()-1);
            sb.append("}},\n  COD{Lcod=").append(codParams.limit() + 2);
            sb.append(", Scod=").append(toBinaryString(codParams.get(0) & 0xff));
            sb.append(", SGcod{P=").append(toProgressionOrder(codParams.get(1) & 0xff));
            sb.append(", Layers=").append(codParams.getShort(2) & 0xffff);
            sb.append(", RCT/ICT=").append(codParams.get(4));
            sb.append("}, SPcod{NL=").append(decompositions());
            sb.append(", cb-width=").append(4 << (codParams.get(6) & 0xff));
            sb.append(", cb-height=").append(4 << (codParams.get(7) & 0xff));
            sb.append(", cb-style=").append(toBinaryString(codParams.get(8) & 0xff));
            sb.append(", Wavelet=").append(toTransformation(codParams.get(9) & 0xff));
            if (codParams.limit() > 10) {
                sb.append(", Precincts{");
                for (int i = 10; i < codParams.limit(); i++) {
                    sb.append('{').append(codParams.get(i) & 0xf)
                            .append(',').append((codParams.get(i) & 0xf0) >>> 4)
                            .append('}').append(',');
                }
                sb.setLength(sb.length() - 1);
            }
            sb.append(tlm ? "}}}\n  TLM}" : "}}}}");
            return sb.toString();
        }

        private String toTransformation(int i) {
            switch (i) {
                case 0:
                    return "9-7";
                case 1:
                    return "5-3";
            }
            return Integer.toString(i);
        }

        private String toProgressionOrder(int i) {
            switch (i) {
                case 0:
                    return "LRCP";
                case 1:
                    return "RLCP";
                case 2:
                    return "RPCL";
                case 3:
                    return "PCRL";
                case 4:
                    return "CPRL";
            }
            return Integer.toString(i);
        }

        private String toBinaryString(int i) {
            String s = Integer.toBinaryString(i);
            int l = s.length();
            if (l <= 4) return s;
            StringBuilder sb = new StringBuilder(s);
            while (l > 4) sb.insert(l -= 4, '_');
            return sb.toString();
        }

        private boolean isRPCL() {
            return tlm && isProgressionOrderRPCL()
                    && isBlockSize64x64()
                    && numXTiles() == 1
                    && numYTiles() == 1
                    && (Math.min(rows(), columns()) >>> decompositions()) <= 64;
        }

        private boolean isProgressionOrderRPCL() {
            return codParams.get(1) == 2;
        }

        private int decompositions() {
            return codParams.get(5) & 0xff;
        }

        private boolean isBlockSize64x64() {
            return (codParams.get(6) & 0xff) == 4 && (codParams.get(7) & 0xff) == 4;
        }

        private int numXTiles() {
            return numTiles(2, 18, 26);
        }

        private int numYTiles() {
            return numTiles(6, 22, 30);
        }

        private int numTiles(int iSize, int iTsiz, int iTOsiz) {
            long tSize = sizParams.getInt(iTsiz) & 0xffffffffL;
            return (int) (((sizParams.getInt(iSize) & 0xffffffffL)
                    - (sizParams.getInt(iTOsiz) & 0xffffffffL) + tSize - 1)
                    / tSize);
        }
    }
}
