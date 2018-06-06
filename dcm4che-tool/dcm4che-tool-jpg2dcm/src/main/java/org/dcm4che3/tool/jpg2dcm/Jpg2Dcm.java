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
 *  Portions created by the Initial Developer are Copyright (C) 2015-2017
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
import org.dcm4che3.imageio.codec.jpeg.JPEGHeader;
import org.dcm4che3.imageio.codec.mpeg.MPEGHeader;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.UIDUtils;

import javax.activation.FileTypeMap;
import java.io.*;
import java.text.MessageFormat;
import java.util.Arrays;
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
    private static final int INIT_BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = 10485768; // 10MiB
    private static final long MAX_FILE_SIZE = 0x7FFFFFFE;

    private static final int[] IUID_TAGS = {
            Tag.StudyInstanceUID,
            Tag.SeriesInstanceUID,
            Tag.SOPInstanceUID
    };

    private static final long[] DA_TM_TAGS = {
            Tag.ContentDateAndTime,
            Tag.InstanceCreationDateAndTime
    };

    private static final int[] TYPE2_TAGS = {
            Tag.StudyID,
            Tag.StudyDate,
            Tag.StudyTime,
            Tag.AccessionNumber,
            Tag.Manufacturer,
            Tag.ReferringPhysicianName,
            Tag.PatientID,
            Tag.PatientName,
            Tag.PatientBirthDate,
            Tag.PatientSex,
    };

    private Attributes metadata;
    private boolean noAPPn;
    private JPEGHeader jpegHeader;
    private byte[] buffer = {};
    private int headerLength;
    private long fileLength;
    private FileType inFileType;

    private void setMetadata(Attributes metadata) {
        this.metadata = metadata;
    }

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
            main.toFileType(inFile);
            main.setMetadata(createMetadata(cl, main.inFileType));
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
        OptionGroup sampleMetadataOG = new OptionGroup();
        opts.addOption(Option.builder("a")
                .hasArgs()
                .argName("[seq/]attr=value")
                .valueSeparator()
                .desc(rb.getString("attr"))
                .build());
        opts.addOption(Option.builder("f")
                .hasArg()
                .argName("xml-file")
                .desc(rb.getString("file"))
                .build());
        opts.addOption(null, "no-app", false, rb.getString("no-app"));
        sampleMetadataOG.addOption(Option.builder()
                .longOpt("sc")
                .hasArg(false)
                .desc(rb.getString("sc"))
                .build());
        sampleMetadataOG.addOption(Option.builder()
                .longOpt("xc")
                .hasArg(false)
                .desc(rb.getString("xc"))
                .build());
        opts.addOptionGroup(sampleMetadataOG);
        CommandLine cl = CLIUtils.parseComandLine(args, opts, rb, Jpg2Dcm.class);
        int numArgs = cl.getArgList().size();
        if (numArgs == 0)
            throw new ParseException(rb.getString("missing"));
        if (numArgs > 2)
            throw new ParseException(rb.getString("too-many"));
        return cl;
    }

    private static Attributes createMetadata(CommandLine cl, FileType inFileType) throws Exception {
        String metadataResource =  cl.hasOption("sc") 
                                ? "resource:secondaryCaptureImageMetadata.xml"
                                : cl.hasOption("xc")
                                    ? "resource:vlPhotographicImageMetadata.xml" : null;
        Attributes metadata = inFileType == FileType.jpeg && metadataResource != null
                                ? SAXReader.parse(StreamUtils.openFileOrURL(metadataResource))
                                : new Attributes();
        if (cl.hasOption("f"))
            metadata = SAXReader.parse(cl.getOptionValue("f"), metadata);
        CLIUtils.addAttributes(metadata, cl.getOptionValues("m"));
        supplementMissingUIDs(metadata);
        supplementMissingValue(metadata, Tag.Modality, "XC");
        supplementMissingValue(metadata, Tag.SeriesNumber, "999");
        supplementMissingValue(metadata, Tag.InstanceNumber, "1");
        supplementMissingDateTime(metadata);
        supplementMissingType2(metadata);
        return metadata;
    }

    private void toFileType(File infile) {
        try {
            inFileType = FileType.valueOf(infile);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(MessageFormat.format(rb.getString("invalid-file-ext"), infile));
        }
    }
    
    private void convert(File infile, File outfile) throws IOException {
        fileLength = infile.length();
        if (fileLength > MAX_FILE_SIZE)
            throw new IllegalArgumentException(MessageFormat.format(rb.getString("file-too-large"), infile));

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(infile))) {
            if (!parseHeader(inFileType, bis))
                throw new IOException(MessageFormat.format(rb.getString("failed-to-parse"), inFileType, infile));

            int itemLen = (int) fileLength;
            try (DicomOutputStream dos = new DicomOutputStream(outfile)) {
                dos.writeDataset(metadata.createFileMetaInformation(inFileType.getTransferSyntaxUID()), metadata);
                dos.writeHeader(Tag.PixelData, VR.OB, -1);
                dos.writeHeader(Tag.Item, null, 0);
                if (jpegHeader != null && noAPPn) {
                    int offset = jpegHeader.offsetAfterAPP();
                    itemLen -= offset - 3;
                    dos.writeHeader(Tag.Item, null, (itemLen + 1) & ~1);
                    dos.write((byte) -1);
                    dos.write((byte) JPEG.SOI);
                    dos.write((byte) -1);
                    dos.write(buffer, offset, headerLength - offset);
                } else {
                    dos.writeHeader(Tag.Item, null, (itemLen + 1) & ~1);
                    dos.write(buffer, 0, headerLength);
                }
                StreamUtils.copy(bis, dos, buffer);
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

    private static void supplementMissingType2(Attributes metadata) {
        for (int tag : TYPE2_TAGS)
            if (!metadata.contains(tag))
                metadata.setNull(tag, DICT.vrOf(tag));
    }

    private boolean parseHeader(FileType fileType, InputStream in) throws IOException {
        int grow = INIT_BUFFER_SIZE;
        while (headerLength == buffer.length && headerLength < MAX_BUFFER_SIZE) {
            buffer = Arrays.copyOf(buffer, grow += headerLength);
            headerLength += StreamUtils.readAvailable(in, buffer, headerLength, buffer.length - headerLength);
            if (fileType.parseHeader(this)) {
                supplementMissingValue(metadata, Tag.SOPClassUID, fileType.getSOPClassUID());
                return true;
            }
        }
        return false;
    }

    private enum FileType {
        jpeg(UID.SecondaryCaptureImageStorage, UID.JPEGBaseline1) {
            @Override
            boolean parseHeader(Jpg2Dcm main) {
                return (main.jpegHeader = new JPEGHeader(main.buffer, JPEG.SOS)).toAttributes(main.metadata) != null;
            }
        },
        mpeg(UID.VideoPhotographicImageStorage, UID.MPEG2) {
            @Override
            boolean parseHeader(Jpg2Dcm main) {
                return new MPEGHeader(main.buffer).toAttributes(main.metadata, main.fileLength) != null;
            }
        };

        private final String cuid;
        private final String tsuid;

        FileType(String cuid, String tsuid) {
            this.cuid = cuid;
            this.tsuid = tsuid;
        }

        public String getSOPClassUID() {
            return cuid;
        }

        public String getTransferSyntaxUID() {
            return tsuid;
        }

        abstract boolean parseHeader(Jpg2Dcm main);

        static FileType valueOf(File file) {
            String contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(file);
            return valueOf(contentType.substring(contentType.lastIndexOf("/")+1));
        }
    }
}
