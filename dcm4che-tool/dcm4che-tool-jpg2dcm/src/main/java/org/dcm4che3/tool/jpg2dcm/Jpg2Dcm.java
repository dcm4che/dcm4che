/*
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  J4Care.
 *  Portions created by the Initial Developer are Copyright (C) 2015-2019
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 */

package org.dcm4che3.tool.jpg2dcm;

import org.apache.commons.cli.*;
import org.dcm4che3.data.*;
import org.dcm4che3.imageio.codec.jpeg.JPEG;
import org.dcm4che3.imageio.codec.jpeg.JPEGParser;
import org.dcm4che3.imageio.codec.mp4.MP4Parser;
import org.dcm4che3.imageio.codec.mpeg.MPEG2Parser;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.UIDUtils;

import java.io.*;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Jul 2017
 */
public class Jpg2Dcm {

    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.jpg2dcm.messages");

    private static final int[] IUID_TAGS = {
            Tag.StudyInstanceUID,
            Tag.SeriesInstanceUID,
            Tag.SOPInstanceUID
    };

    private static final long[] DA_TM_TAGS = {
            Tag.ContentDateAndTime,
            Tag.InstanceCreationDateAndTime
    };
    
    private static Attributes metadata;
    private boolean noAPPn;
    private static FileType inFileType;


    private void setNoAPPn(boolean noAPPn) {
        this.noAPPn = noAPPn;
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Jpg2Dcm main = new Jpg2Dcm();
            main.setNoAPPn(cl.hasOption("no-app"));
            @SuppressWarnings("unchecked") final List<String> argList = cl.getArgList();
            File inFile = new File(argList.get(0));
            createMetadata(cl, inFile);
            main.convert(inFile, new File(argList.get(1)));
        } catch (ParseException e) {
            System.err.println("jpg2dcm: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("jpg2dcm: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static CommandLine parseComandLine(String[] args) throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        opts.addOption(Option.builder("m")
                .hasArgs()
                .argName("[seq/]attr=value")
                .valueSeparator()
                .desc(rb.getString("metadata"))
                .build());
        opts.addOption(Option.builder("f")
                .hasArg()
                .argName("xml-file")
                .desc(rb.getString("file"))
                .build());
        opts.addOption(null, "no-app", false, rb.getString("no-app"));
        CommandLine cl = CLIUtils.parseComandLine(args, opts, rb, Jpg2Dcm.class);
        int numArgs = cl.getArgList().size();
        if (numArgs == 0)
            throw new ParseException(rb.getString("missing"));
        if (numArgs > 2)
            throw new ParseException(rb.getString("too-many"));
        return cl;
    }

    private static void createMetadata(CommandLine cl, File inFile) throws Exception {
        inFileType = FileType.valueOf(inFile.toPath());
        metadata = SAXReader.parse(StreamUtils.openFileOrURL(inFileType.sampleMetadataFile));
        if (cl.hasOption("f"))
            metadata.addAll(SAXReader.parse(cl.getOptionValue("f"), metadata));
        CLIUtils.addAttributes(metadata, cl.getOptionValues("m"));
        supplementMissingUIDs(metadata);
        supplementMissingValue(metadata, Tag.SeriesNumber, "999");
        supplementMissingValue(metadata, Tag.InstanceNumber, "1");
        supplementMissingValue(metadata, Tag.SOPClassUID, inFileType.getSOPClassUID());
        supplementMissingDateTime(metadata);
        metadata = inFileType.getAttributes(new FileInputStream(inFile).getChannel(), metadata);
    }

    private void convert(File infile, File outfile) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(infile))) {
            int itemLen = (int) infile.length();
            try (DicomOutputStream dos = new DicomOutputStream(outfile)) {
                dos.writeDataset(metadata.createFileMetaInformation(inFileType.getTransferSyntaxUID()), metadata);
                dos.writeHeader(Tag.PixelData, VR.OB, -1);
                dos.writeHeader(Tag.Item, null, 0);
                if (noAPPn && inFileType == FileType.JPEG) {
                    itemLen -= inFileType.getPositionAfterAPPSegments() - 3;
                    dos.writeHeader(Tag.Item, null, (itemLen + 1) & ~1);
                    dos.write((byte) -1);
                    dos.write((byte) JPEG.SOI);
                    dos.write((byte) -1);
                } else
                    dos.writeHeader(Tag.Item, null, (itemLen + 1) & ~1);
                StreamUtils.copy(bis, dos);
                if ((itemLen & 1) != 0)
                    dos.write(0);
                dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
            }
        }
        System.out.println(MessageFormat.format(rb.getString("converted"), infile, outfile));
    }

    private static void supplementMissingUIDs(Attributes metadata) {
        for (int tag : IUID_TAGS)
            if (!metadata.containsValue(tag))
                metadata.setString(tag, VR.UI, UIDUtils.createUID());
    }

    private static void supplementMissingValue(Attributes metadata, int tag, String value) {
        if (!metadata.containsValue(tag))
            metadata.setString(tag, DICT.vrOf(tag), value);
    }

    private static void supplementMissingDateTime(Attributes metadata) {
        Date now = new Date();
        for (long tag : DA_TM_TAGS)
            if (!metadata.containsValue((int) (tag >>> 32)))
                metadata.setDate(tag, now);
    }

    private enum FileType {
        JPEG(UID.SecondaryCaptureImageStorage, "resource:secondaryCaptureImageMetadata.xml") {
            @Override
            Attributes getAttributes(SeekableByteChannel channel, Attributes attrs) throws IOException {
                setTsuid(UID.JPEGBaseline1);
                JPEGParser jpegParser = new JPEGParser(channel);
                setPositionAfterAPPSegments(jpegParser.getPositionAfterAPPSegments());
                return jpegParser.getAttributes(attrs);
            }
        },
        MPEG2(UID.VideoPhotographicImageStorage, "resource:vlPhotographicImageMetadata.xml") {
            @Override
            Attributes getAttributes(SeekableByteChannel channel, Attributes attrs) throws IOException {
                setTsuid(UID.MPEG2);
                return new MPEG2Parser(channel).getAttributes(attrs);
            }
        },
        MP4(UID.VideoPhotographicImageStorage, "resource:vlPhotographicImageMetadata.xml") {
            @Override
            Attributes getAttributes(SeekableByteChannel channel, Attributes attrs) throws IOException {
                MP4Parser mp4Parser = new MP4Parser(channel);
                setTsuid(mp4Parser.getTransferSyntaxUID());
                return mp4Parser.getAttributes(attrs);
            }
        };

        abstract Attributes getAttributes(SeekableByteChannel channel, Attributes attrs) throws IOException;

        private final String cuid;
        private final String sampleMetadataFile;
        private String tsuid;
        private long positionAfterAPPSegments;

        FileType(String cuid, String sampleMetadataFile) {
            this.cuid = cuid;
            this.sampleMetadataFile = sampleMetadataFile;
        }

        public String getSOPClassUID() {
            return cuid;
        }

        public String getTransferSyntaxUID() {
            return tsuid;
        }

        public void setTsuid(String tsuid) {
            this.tsuid = tsuid;
        }

        public long getPositionAfterAPPSegments() {
            return positionAfterAPPSegments;
        }

        public void setPositionAfterAPPSegments(long positionAfterAPPSegments) {
            this.positionAfterAPPSegments = positionAfterAPPSegments;
        }

        static FileType valueOf(Path path) throws IOException {
            String contentType = Files.probeContentType(path);
            String contentTypeSubType = contentType.substring(contentType.indexOf("/")+1);
            switch (contentTypeSubType) {
                case "jpeg":
                    return JPEG;
                case "mpeg":
                    return MPEG2;
                case "mp4":
                    return MP4;
            }
            throw new IllegalArgumentException(MessageFormat.format(rb.getString("invalid-file-ext"), path));
        }
    }
}
