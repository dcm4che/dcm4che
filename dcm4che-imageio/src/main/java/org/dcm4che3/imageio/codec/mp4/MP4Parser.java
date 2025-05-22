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

package org.dcm4che3.imageio.codec.mp4;

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
import java.util.Date;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jun 2019
 */
public class MP4Parser implements XPEGParser {
    private static final int FileBoxType = 0x66747970; // ftyp;
    private static final int MovieBoxType = 0x6d6f6f76; // moov;
    private static final int TrackBoxType = 0x7472616b; // trak
    private static final int MediaBoxType = 0x6d646961; // mdia
    private static final int MediaHeaderBoxType = 0x6d646864; // mdhd
    private static final int MediaInformationBoxType = 0x6d696e66; // minf
    private static final int SampleTableBoxType = 0x7374626c; // stbl
    private static final int SampleDescriptionBoxType = 0x73747364; // stsd
    private static final int VisualSampleEntryTypeAVC1 = 0x61766331; // avc1
    private static final int AvcConfigurationBoxType = 0x61766343; // avcC
    private static final int VisualSampleEntryTypeHVC1 = 0x68766331; // hvc1
    private static final int HevcConfigurationBoxType = 0x68766343; // hvcC
    private static final int SampleSizeBoxType = 0x7374737a; // stsz

    private final ByteBuffer buf = ByteBuffer.allocate(8);
    private MP4FileType mp4FileType;
    private Date creationTime;
    private Date modificationTime;
    private int timescale;
    private long duration;
    private int fp1000s;
    private int rows;
    private int columns;
    private int numFrames;
    private int visualSampleEntryType;
    private int configurationVersion;
    private int profile_idc;
    private int level_idc;

    public MP4Parser(SeekableByteChannel channel) throws IOException {
        long position = channel.position();
        Box box = nextBox(channel, channel.size() - position);
        if (box.type == FileBoxType) {
            mp4FileType = new MP4FileType(readInt(channel), readInt(channel), readInts(channel, box.end));
        } else {
            channel.position(position);
        }
        parseMovieBox(channel, findBox(channel, channel.size(), MovieBoxType));
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public Date getModificationTime() {
        return modificationTime;
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
    public MP4FileType getMP4FileType() {
        return mp4FileType;
    }

    @Override
    public Attributes getAttributes(Attributes attrs) {
        if (attrs == null)
            attrs = new Attributes(14);

        attrs.setInt(Tag.CineRate, VR.IS, (fp1000s + 500) / 1000);
        attrs.setFloat(Tag.FrameTime, VR.DS, 1_000_000.f / fp1000s);
        attrs.setInt(Tag.SamplesPerPixel, VR.US, 3);
        attrs.setString(Tag.PhotometricInterpretation, VR.CS, "YBR_PARTIAL_420");
        attrs.setInt(Tag.PlanarConfiguration, VR.US, 0);
        attrs.setInt(Tag.FrameIncrementPointer, VR.AT, Tag.FrameTime);
        attrs.setInt(Tag.NumberOfFrames, VR.IS, numFrames);
        attrs.setInt(Tag.Rows, VR.US, rows);
        attrs.setInt(Tag.Columns, VR.US, columns);
        attrs.setInt(Tag.BitsAllocated, VR.US, 8);
        attrs.setInt(Tag.BitsStored, VR.US, 8);
        attrs.setInt(Tag.HighBit, VR.US, 7);
        attrs.setInt(Tag.PixelRepresentation, VR.US, 0);
        attrs.setString(Tag.LossyImageCompression, VR.CS, "01");
        return attrs;
    }

    @Override
    public String getTransferSyntaxUID(boolean fragmented) throws XPEGParserException {
        switch (visualSampleEntryType) {
            case VisualSampleEntryTypeAVC1:
                switch (profile_idc) {
                    case 100: // High Profile
                        if (level_idc <= 41)
                            return isBDCompatible()
                                    ? fragmented ? UID.MPEG4HP41BDF : UID.MPEG4HP41BD
                                    : fragmented ? UID.MPEG4HP41F : UID.MPEG4HP41;
                        else if (level_idc <= 42)
                            // TODO: distinguish between MPEG4HP422D
                            //  and MPEG4HP423D
                            return fragmented ? UID.MPEG4HP422DF : UID.MPEG4HP422D;
                        break;
                    case 128: // Stereo High Profile
                        if (level_idc <= 42)
                            return UID.MPEG4HP42STEREO;
                        break;
                }
                throw profileLevelNotSupported("MPEG-4 AVC profile_idc/level_idc: %d/%d not supported");
            case VisualSampleEntryTypeHVC1:
                if (level_idc <= 153) {
                    switch (profile_idc) {
                        case 1: // Main Profile
                            return UID.HEVCMP51;
                        case 2: // Main 10 Profile
                            return UID.HEVCM10P51;
                    }
                }
                throw profileLevelNotSupported("MPEG-4 HEVC profile_idc/level_idc: %d/%d not supported");
        }
        throw new AssertionError("visualSampleEntryType:" + visualSampleEntryType);
    }

    private XPEGParserException profileLevelNotSupported(String format) {
        return new XPEGParserException(String.format(format, profile_idc, level_idc));
    }

    private boolean isBDCompatible() {
        return rows == 1080
                ? columns == 1920
                    && (fp1000s == 23976 || fp1000s == 24000 || fp1000s == 25000 || fp1000s == 29970)
                : rows == 720 && columns == 1280
                    && (fp1000s == 23976 || fp1000s == 24000 || fp1000s == 50000 || fp1000s == 59940);
    }

    private Box nextBox(SeekableByteChannel channel, long remaining) throws IOException {
        long pos = channel.position();
        long type = readLong(channel);
        long size = type >>> 32;
        return new Box((int) type, pos + (size == 0 ? remaining : size == 1 ? readLong(channel) : size));
    }

    private Box findBox(SeekableByteChannel channel, long end, int type) throws IOException {
        long remaining;
        while ((remaining = end - channel.position()) > 0) {
            Box box = nextBox(channel, remaining);
            if (box.type == type)
                return box;
            channel.position(box.end);
        }
        throw new XPEGParserException(boxNotFound(type));
    }

    private static String boxNotFound(int type) {
        return String.format("%c%c%c%c box not found",
                (type >> 24) & 0xff,
                (type >> 16) & 0xff,
                (type >> 8) & 0xff,
                type & 0xff);
    }

    private int[] readInts(SeekableByteChannel channel, long end) throws IOException {
        int[] values = new int[(int) ((end - channel.position()) / 4)];
        for (int i = 0; i < values.length; i++) {
            values[i] = readInt(channel);
        }
        return values;
    }

    private byte readByte(SeekableByteChannel channel) throws IOException {
        SafeBuffer.clear(buf).limit(1);
        channel.read(buf);
        SafeBuffer.rewind(buf);
        return buf.get();
    }

    private short readShort(SeekableByteChannel channel) throws IOException {
        SafeBuffer.clear(buf).limit(2);
        channel.read(buf);
        SafeBuffer.rewind(buf);
        return buf.getShort();
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

    private static Date toDate(long val) {
        return val > 0 ? new Date((val - 2082844800L) * 1000L) : null;
    }

    private static class Box {
        final int type;
        final long end;

        Box(int type, long end) {
            this.type = type;
            this.end = end;
        }
    }

    private void parseMovieBox(SeekableByteChannel channel, Box box) throws IOException {
        do {
            parseTrackBox(channel, findBox(channel, box.end, TrackBoxType));
        } while (visualSampleEntryType == 0);
        channel.position(box.end);
    }

    private void parseTrackBox(SeekableByteChannel channel, Box box) throws IOException {
        parseMediaBox(channel, findBox(channel, box.end, MediaBoxType));
        channel.position(box.end);
    }

    private void parseMediaBox(SeekableByteChannel channel, Box box) throws IOException {
        parseMediaHeaderBox(channel, findBox(channel, box.end, MediaHeaderBoxType));
        parseMediaInformationBox(channel, findBox(channel, box.end, MediaInformationBoxType));
        channel.position(box.end);
    }

    private void parseMediaHeaderBox(SeekableByteChannel channel, Box box) throws IOException {
        if ((readInt(channel) >>> 24) == 1) {
            creationTime = toDate(readLong(channel));
            modificationTime = toDate(readLong(channel));
            timescale = readInt(channel);
            duration = readLong(channel);
        } else {
            creationTime = toDate(readInt(channel) & 0xffffffffL);
            modificationTime = toDate(readInt(channel) & 0xffffffffL);
            timescale = readInt(channel);
            duration = readInt(channel) & 0xffffffffL;
        }
        channel.position(box.end);
    }

    private void parseMediaInformationBox(SeekableByteChannel channel, Box box) throws IOException {
        parseSampleTableBox(channel,
                findBox(channel, box.end, SampleTableBoxType));
        channel.position(box.end);
    }

    private void parseSampleTableBox(SeekableByteChannel channel, Box box) throws IOException {
        parseSampleDescriptionBox(channel,
                findBox(channel, box.end, SampleDescriptionBoxType));
        parseSampleSizeBox(channel,
                findBox(channel, box.end, SampleSizeBoxType));
        channel.position(box.end);
    }

    private void parseSampleDescriptionBox(SeekableByteChannel channel, Box box) throws IOException {
        skip(channel, 8);
        parseVisualSampleEntry(channel, nextBox(channel, box.end));
        channel.position(box.end);
    }

    private void parseVisualSampleEntry(SeekableByteChannel channel, Box box) throws IOException {
        switch (box.type) {
            case VisualSampleEntryTypeAVC1:
                parseVisualSampleEntryHeader(channel, box);
                parseAvcConfigurationBox(channel,
                        findBox(channel, box.end, AvcConfigurationBoxType));
                break;
            case VisualSampleEntryTypeHVC1:
                parseVisualSampleEntryHeader(channel, box);
                parseHevcConfigurationBox(channel,
                        findBox(channel, box.end, HevcConfigurationBoxType));
                break;
        }
        channel.position(box.end);
    }

    private void parseVisualSampleEntryHeader(SeekableByteChannel channel, Box box) throws IOException {
        visualSampleEntryType = box.type;
        skip(channel, 24);
        int val = readInt(channel);
        columns = val >>> 16;
        rows = val & 0xffff;
        skip(channel, 50);
    }

    private void parseAvcConfigurationBox(SeekableByteChannel channel, Box box) throws IOException {
        int val = readInt(channel);
        configurationVersion = val >>> 24;
        profile_idc = (val >> 16) & 0xff;
        level_idc = val & 0xff;
        channel.position(box.end);
    }

    private void parseHevcConfigurationBox(SeekableByteChannel channel, Box box) throws IOException {
        int val = readShort(channel);
        configurationVersion = val >>> 8;
        profile_idc = val & 0x1F;
        skip(channel, 10);
        level_idc = readByte(channel) & 0xff;
        channel.position(box.end);
    }

    private void parseSampleSizeBox(SeekableByteChannel channel, Box box) throws IOException {
        skip(channel, 8);
        numFrames = readInt(channel);
        fp1000s = (int) ((numFrames * 1000L * timescale + (duration >> 1)) / duration);
        channel.position(box.end);
    }

}
