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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jun 2019
 */
public class JPEGParser implements XPEGParser {

    private static final int MAX_BYTES_BEFORE_SOI = 256;

    private final ByteBuffer buf = ByteBuffer.allocate(4);
    private long positionAfterAPP;
    private int sof;
    private int precision;
    private int columns;
    private int rows;
    private int samples;
    private int ss;

    public JPEGParser(SeekableByteChannel channel) throws IOException {
        findSOI(channel);
        positionAfterAPP = channel.position();
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
        precision = readUByte(channel);
        columns = readUShort(channel);
        rows = readUShort(channel);
        samples = readUByte(channel);
        skip(channel, segment.contentSize - 6);
        while ((segment = nextSegment(channel)).marker != JPEG.SOS) {
            skip(channel, segment.contentSize);
        }
        ss = readInt(channel) & 0xff;
    }

    public long getPositionAfterAPPSegments() {
        return positionAfterAPP;
    }

    @Override
    public Attributes getAttributes(Attributes attrs) {
        if (attrs == null)
            attrs = new Attributes(10);

        attrs.setInt(Tag.SamplesPerPixel, VR.US, samples);
        if (samples == 3) {
            attrs.setString(Tag.PhotometricInterpretation, VR.CS,
                    (sof == JPEG.SOF3 || sof == JPEG.SOF55) ? "RGB" : "YBR_FULL_422");
            attrs.setInt(Tag.PlanarConfiguration, VR.US, 0);
        } else {
            attrs.setString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME2");
        }
        attrs.setInt(Tag.Rows, VR.US, rows);
        attrs.setInt(Tag.Columns, VR.US, columns);
        attrs.setInt(Tag.BitsAllocated, VR.US, precision > 8 ? 16 : 8);
        attrs.setInt(Tag.BitsStored, VR.US, precision);
        attrs.setInt(Tag.HighBit, VR.US, precision - 1);
        attrs.setInt(Tag.PixelRepresentation, VR.US, 0);
        if (!(sof == JPEG.SOF3 || (sof == JPEG.SOF55 && ss == 0)))
            attrs.setString(Tag.LossyImageCompression, VR.CS,  "01");
        return attrs;
    }

    @Override
    public String getTransferSyntaxUID() throws XPEGParserException {
        switch(sof) {
            case JPEG.SOF0:
                return UID.JPEGBaseline1;
            case JPEG.SOF1:
                return UID.JPEGExtended24;
            case JPEG.SOF2:
                return UID.JPEGFullProgressionNonHierarchical1012Retired;
            case JPEG.SOF3:
                return ss == 1 ? UID.JPEGLossless : UID.JPEGLosslessNonHierarchical14;
            case JPEG.SOF55:
                return ss == 0 ? UID.JPEGLSLossless : UID.JPEGLSLossyNearLossless;
        }
        throw new XPEGParserException(String.format("JPEG SOF%d not supported", sof & 0xf));
    }

    private void findSOI(SeekableByteChannel channel) throws IOException {
        int remaining = MAX_BYTES_BEFORE_SOI;
        int b1, b2 = 0;
        do {
            if ((b1 = b2) != 0xff) {
                b1 = readUByte(channel);
                --remaining;
            }
            if (b1 == 0xff && (b2 = readUByte(channel)) == JPEG.SOI)
                return;
        } while (--remaining > 0);
        throw new XPEGParserException("JPEG SOI marker not found");

    }

    private int readUByte(SeekableByteChannel channel) throws IOException {
        buf.clear().limit(1);
        channel.read(buf);
        buf.rewind();
        return buf.get() & 0xff;
    }

    private int readUShort(SeekableByteChannel channel) throws IOException {
        buf.clear().limit(2);
        channel.read(buf);
        buf.rewind();
        return buf.getShort();
    }

    private int readInt(SeekableByteChannel channel) throws IOException {
        buf.clear();
        channel.read(buf);
        buf.rewind();
        return buf.getInt();
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
            throw new XPEGParserException(
                    String.format("unexpected %2XH on position %d", v, channel.position() - 4));
    }
}
