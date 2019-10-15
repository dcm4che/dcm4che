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

package org.dcm4che3.imageio.codec.mpeg;

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
public class MPEG2Parser implements XPEGParser {

    private static final int BUFFER_SIZE = 8162;
    private static final int SEQUENCE_HEADER_STREAM_ID = (byte) 0xb3;
    private static final int GOP_HEADER_STREAM_ID = (byte) 0xb8;
    private static final String[] ASPECT_RATIO_1_1 = { "1", "1" };
    private static final String[] ASPECT_RATIO_4_3 = { "4", "3" };
    private static final String[] ASPECT_RATIO_16_9 = { "16", "9" };
    private static final String[] ASPECT_RATIO_221_100 = { "221", "100" };
    private static final String[][] ASPECT_RATIOS = {
            ASPECT_RATIO_1_1,
            ASPECT_RATIO_4_3,
            ASPECT_RATIO_16_9,
            ASPECT_RATIO_221_100
    };
    private static int[] FPS = {
            24, 1001,
            24, 1000,
            25, 1000,
            30, 1001,
            30, 1000,
            50, 1000,
            60, 1001,
            60, 1000
    };

    private final byte[] data = new byte[BUFFER_SIZE];
    private final ByteBuffer buf = ByteBuffer.wrap(data);
    private final int columns;
    private final int rows;
    private final int aspectRatio;
    private final int frameRate;
    private final int duration;

    public MPEG2Parser(SeekableByteChannel channel) throws IOException {
        Packet packet;
        while (!isVideoStream((packet = nextPacket(channel)).startCode)) {
            skip(channel, packet.length);
        }
        findSequenceHeader(channel, packet.length);
        SafeBuffer.clear(buf).limit(7);
        channel.read(buf);
        columns = ((data[0] & 0xff) << 4) | ((data[1] & 0xf0) >> 4);
        rows = ((data[1] & 0x0f) << 8) | (data[2] & 0xff);
        aspectRatio = (data[3] >> 4) & 0x0f;
        frameRate = Math.max(1, Math.min(data[3] & 0x0f, 8));
        int lastGOP = findLastGOP(channel);
        int hh = (data[lastGOP] & 0x7c) >> 2;
        int mm = ((data[lastGOP] & 0x03) << 4) | ((data[lastGOP + 1] & 0xf0) >> 4);
        int ss = ((data[lastGOP + 1] & 0x07) << 3) | ((data[lastGOP + 2] & 0xe0) >> 5);
        duration = hh * 3600 + mm * 60 + ss;
    }

    @Override
    public long getCodeStreamPosition() {
        return 0;
    }

    @Override
    public long getPositionAfterAPPSegments() {
        return -1L;
    }

    @Override
    public Attributes getAttributes(Attributes attrs) {
        if (attrs == null)
            attrs = new Attributes(15);

        int frameRate2 = (frameRate - 1) << 1;
        int fps = FPS[frameRate2];
        attrs.setInt(Tag.CineRate, VR.IS, fps);
        attrs.setFloat(Tag.FrameTime, VR.DS, ((float) FPS[frameRate2 + 1]) / fps);
        attrs.setInt(Tag.SamplesPerPixel, VR.US, 3);
        attrs.setString(Tag.PhotometricInterpretation, VR.CS, "YBR_PARTIAL_420");
        attrs.setInt(Tag.PlanarConfiguration, VR.US, 0);
        attrs.setInt(Tag.FrameIncrementPointer, VR.AT, Tag.FrameTime);
        attrs.setInt(Tag.NumberOfFrames, VR.IS, (int) (duration * fps * 1000L / FPS[frameRate2 + 1]));
        attrs.setInt(Tag.Rows, VR.US, rows);
        attrs.setInt(Tag.Columns, VR.US, columns);
        if (aspectRatio > 0 && aspectRatio < 5)
            attrs.setString(Tag.PixelAspectRatio, VR.IS, ASPECT_RATIOS[aspectRatio-1]);
        attrs.setInt(Tag.BitsAllocated, VR.US, 8);
        attrs.setInt(Tag.BitsStored, VR.US, 8);
        attrs.setInt(Tag.HighBit, VR.US, 7);
        attrs.setInt(Tag.PixelRepresentation, VR.US, 0);
        attrs.setString(Tag.LossyImageCompression, VR.CS,  "01");
        return attrs;
    }

    @Override
    public String getTransferSyntaxUID() {
        return frameRate <= 5 && columns <= 720 ? UID.MPEG2 : UID.MPEG2MainProfileHighLevel;
    }

    private void findSequenceHeader(SeekableByteChannel channel, int length) throws IOException {
        int remaining = length;
        SafeBuffer.clear(buf).limit(3);
        while ((remaining -= buf.remaining()) > 1) {
            channel.read(buf);
            SafeBuffer.rewind(buf);
            if (((data[0] << 16) | (data[1] << 8) | data[2]) == 1) {
                SafeBuffer.clear(buf).limit(1);
                remaining--;
                channel.read(buf);
                SafeBuffer.rewind(buf);
                if (buf.get() == SEQUENCE_HEADER_STREAM_ID)
                    return;
                SafeBuffer.limit(buf, 3);
            }
            SafeBuffer.position(buf, data[2] == 0 ? data[1] == 0 ? 2 : 1 : 0);
            data[0] = 0;
        }
        throw new XPEGParserException("MPEG2 sequence header not found");
    }

    private void skip(SeekableByteChannel channel, long n) throws IOException {
        channel.position(channel.position() + n);
    }

    private int findLastGOP(SeekableByteChannel channel) throws IOException {
        long pos = channel.size() - 8;
        do {
            pos = Math.max(0, pos + 8 - BUFFER_SIZE);
            channel.position(pos);
            SafeBuffer.clear(buf);
            channel.read(buf);
            int i = 0;
            while (i + 8 < BUFFER_SIZE) {
                if (((data[i] << 16) | (data[i + 1] << 8) | data[i + 2]) == 1) {
                    if (data[i + 3] == GOP_HEADER_STREAM_ID)
                        return i + 4;
                }
                i += data[i + 2] == 0 ? data[i + 1] == 0 ? 1 : 2 : 3;
            }
        } while (pos > 0);
        throw new XPEGParserException("last MPEG2 Group of Pictures not found");
    }

    private static class Packet {
        final int startCode;
        final int length;

        private Packet(int startCode, int length) {
            this.startCode = startCode;
            this.length = length;
        }
    }

    private Packet nextPacket(SeekableByteChannel channel) throws IOException {
        SafeBuffer.clear(buf).limit(6);
        channel.read(buf);
        SafeBuffer.rewind(buf);
        int startCode = buf.getInt();
        if ((startCode & 0xfffffe00) != 0) {
            throw new XPEGParserException(
                    String.format("Invalid MPEG2 start code %4XH on position %d", startCode, channel.position() - 6));
        }
        return new Packet(startCode, packetLength(startCode));
    }

    private int packetLength(int startCode) {
        return isPackHeader(startCode) ? ((data[4] & 0xc0) != 0) ? 8 : 6 : buf.getShort() & 0xffff;
    }

    private static boolean isPackHeader(int startCode) {
        return startCode == 0x1ba;
    }

    private static boolean isVideoStream(int startCode) {
        return (startCode & 0xfffff0) == 0x1e0;
    }
}
