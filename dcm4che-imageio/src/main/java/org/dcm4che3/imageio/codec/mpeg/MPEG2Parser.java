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
import org.dcm4che3.data.VR;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jun 2019
 */
public class MPEG2Parser {

    private static final int SEQUENCE_HEADER_STREAM_ID = 0xb3;
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

    private final byte[] data = new byte[7];
    private final ByteBuffer buf = ByteBuffer.wrap(data);
    private final int columns;
    private final int rows;
    private final int aspectRatio;
    private final int frameRate;
    private final int bitRate;
    private final long size;

    public MPEG2Parser(SeekableByteChannel channel) throws IOException {
        Packet packet;
        while (!isVideoStream((packet = nextPacket(channel)).startCode)) {
            skip(channel, packet.length);
        }
        findSequenceHeader(channel, packet.length);
        buf.clear();
        channel.read(buf);
        columns = ((data[0] & 0xff) << 4) | ((data[1] & 0xf0) >> 4);
        rows = ((data[1] & 0x0f) << 8) | (data[2] & 0xFF);
        aspectRatio = (data[3] >> 4) & 0x0f;
        frameRate = data[3] & 0x0f;
        bitRate = (((data[4] & 0xff) << 24) | ((data[5] & 0xff) << 16) | ((data[6] & 0xff) << 8)) >> 14;
        size = channel.size();
    }

    public Attributes getAttributes(Attributes attrs) {
        if (attrs == null)
            attrs = new Attributes(15);

        int numFrames = 9999;
        if (frameRate > 0 && frameRate < 9) {
            int frameRate2 = (frameRate - 1) << 1;
            attrs.setInt(Tag.CineRate, VR.IS, FPS[frameRate2]);
            attrs.setFloat(Tag.FrameTime, VR.DS, ((float) FPS[frameRate2 + 1]) / FPS[frameRate2]);
            if (bitRate > 0)
                numFrames = (int) (20 * size * FPS[frameRate2] / FPS[frameRate2 + 1] / bitRate);
        }
        attrs.setInt(Tag.SamplesPerPixel, VR.US, 3);
        attrs.setString(Tag.PhotometricInterpretation, VR.CS, "YBR_PARTIAL_420");
        attrs.setInt(Tag.PlanarConfiguration, VR.US, 0);
        attrs.setInt(Tag.FrameIncrementPointer, VR.AT, Tag.FrameTime);
        attrs.setInt(Tag.NumberOfFrames, VR.IS, numFrames);
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

    private void findSequenceHeader(SeekableByteChannel channel, int length) throws IOException {
        int remaining = length;
        int startPrefix;
        buf.clear().limit(3);
        while ((remaining -= buf.remaining()) > 1) {
            channel.read(buf);
            buf.rewind();
            if ((startPrefix = (data[0] << 16) | (data[1] << 8) | data[2]) == 1) {
                buf.clear().limit(1);
                remaining--;
                channel.read(buf);
                buf.rewind();
                if ((buf.get() & 0xff) == SEQUENCE_HEADER_STREAM_ID)
                    return;
                buf.limit(3);
            }
            buf.position(Math.min(2, Integer.numberOfTrailingZeros(startPrefix)));
            data[0] = data[1] = 0;
        }
        throw new MPEG2ParserException("sequence header not found");
    }

    private void skip(SeekableByteChannel channel, long n) throws IOException {
        channel.position(channel.position() + n);
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
        buf.clear().limit(6);
        channel.read(buf);
        buf.rewind();
        int startCode = buf.getInt();
        if ((startCode & 0xfffffe00) != 0) {
            throw new MPEG2ParserException(
                    String.format("Invalid start code %4XH on position %d", startCode, channel.position() - 6));
        }
        return new Packet(startCode, isPackHeader(startCode) ? 8 : buf.getShort() & 0xffff);
    }

    private static boolean isPackHeader(int startCode) {
        return startCode == 0x1ba;
    }

    private static boolean isVideoStream(int startCode) {
        return (startCode & 0xfffff0) == 0x1e0;
    }
}
