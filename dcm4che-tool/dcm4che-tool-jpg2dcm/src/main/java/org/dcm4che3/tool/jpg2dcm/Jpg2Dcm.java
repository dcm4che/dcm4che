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
import org.dcm4che3.imageio.codec.XPEGParser;
import org.dcm4che3.imageio.codec.jpeg.JPEG;
import org.dcm4che3.imageio.codec.jpeg.JPEGParser;
import org.dcm4che3.imageio.codec.mp4.MP4Parser;
import org.dcm4che3.imageio.codec.mpeg.MPEG2Parser;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.UIDUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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

    private static Attributes staticMetadata;
    private boolean noAPPn;
    private static boolean vlPhotographicImage;


    private void setNoAPPn(boolean noAPPn) {
        this.noAPPn = noAPPn;
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Jpg2Dcm main = new Jpg2Dcm();
            main.setNoAPPn(cl.hasOption("no-app"));
            vlPhotographicImage = cl.hasOption("xc");
            @SuppressWarnings("unchecked") final List<String> argList = cl.getArgList();
            int argc = argList.size();
            if (argc < 2)
                throw new ParseException(rb.getString("missing"));
            File dest = new File(argList.get(argc-1));
            if ((argc > 2 || new File(argList.get(0)).isDirectory())
                    && !dest.isDirectory())
                throw new ParseException(
                        MessageFormat.format(rb.getString("nodestdir"), dest));
            createStaticMetadata(cl);
            main.convert(cl.getArgList());
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
        opts.addOption(Option.builder()
                .longOpt("xc")
                .hasArg(false)
                .desc(rb.getString("xc"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("no-app")
                .hasArg(false)
                .desc(rb.getString("no-app"))
                .build());
        return CLIUtils.parseComandLine(args, opts, rb, Jpg2Dcm.class);
    }

    private Attributes createMetadata(Path srcFilePath, FileType fileType) throws Exception {
        Attributes fileMetadata = SAXReader.parse(StreamUtils.openFileOrURL(fileType.getSampleMetadataFile()));
        fileMetadata.addAll(staticMetadata);
        supplementMissingValue(fileMetadata, Tag.SOPClassUID, fileType.getSOPClassUID());
        try(FileInputStream fis = new FileInputStream(srcFilePath.toFile())) {
            fileType.parse(fis.getChannel());
            fileMetadata = fileType.getParser().getAttributes(fileMetadata);
        }
        return fileMetadata;
    }

    private static void createStaticMetadata(CommandLine cl) throws Exception {
        staticMetadata = new Attributes();
        if (cl.hasOption("f"))
            staticMetadata = SAXReader.parse(cl.getOptionValue("f"));

        CLIUtils.addAttributes(staticMetadata, cl.getOptionValues("m"));
        supplementMissingUIDs(staticMetadata);
        supplementMissingValue(staticMetadata, Tag.SeriesNumber, "999");
        supplementMissingValue(staticMetadata, Tag.InstanceNumber, "1");
        supplementMissingDateTime(staticMetadata);
    }

    private void convert(List<String> args) throws Exception {
        int argsSize = args.size();
        Path destPath = Paths.get(args.get(argsSize - 1));
        for (String src : args.subList(0, argsSize - 1)) {
            Path srcPath = Paths.get(src);
            if (Files.isDirectory(srcPath))
                Files.walkFileTree(srcPath, new Jpg2DcmFileVisitor(srcPath, destPath));
            else if (Files.isDirectory(destPath))
                convert(srcPath, destPath.resolve(srcPath.getFileName() + ".dcm"));
            else
                convert(srcPath, destPath);
        }
    }

    class Jpg2DcmFileVisitor extends SimpleFileVisitor<Path> {
        private Path srcPath;
        private Path destPath;

        Jpg2DcmFileVisitor(Path srcPath, Path destPath) {
            this.srcPath = srcPath;
            this.destPath = destPath;
        }

        @Override
        public FileVisitResult visitFile(Path srcFilePath, BasicFileAttributes attrs) throws IOException {
            Path destFilePath = resolveDestFilePath(srcFilePath);
            if (!Files.isDirectory(destFilePath))
                Files.createDirectories(destFilePath);
            try {
                convert(srcFilePath, destFilePath.resolve(srcFilePath.getFileName() + ".dcm"));
            } catch (SAXException | ParserConfigurationException e) {
                System.out.println(MessageFormat.format(rb.getString("failed"), srcFilePath, e.getMessage()));
                e.printStackTrace(System.out);
                return FileVisitResult.TERMINATE;
            } catch (Exception e) {
                System.out.println(MessageFormat.format(rb.getString("failed"), srcFilePath, e.getMessage()));
                e.printStackTrace(System.out);
            }
            return FileVisitResult.CONTINUE;
        }

        private Path resolveDestFilePath(Path srcFilePath) {
            int srcPathNameCount = srcPath.getNameCount();
            int srcFilePathNameCount = srcFilePath.getNameCount() - 1;
            if (srcPathNameCount == srcFilePathNameCount)
                return destPath;

            return destPath.resolve(srcFilePath.subpath(srcPathNameCount, srcFilePathNameCount));
        }
    }

    private void convert(Path srcFilePath, Path destFilePath) throws Exception {
        FileType fileType = FileType.valueOf(srcFilePath);
        Attributes fileMetadata = createMetadata(srcFilePath, fileType);
        XPEGParser parser = fileType.getParser();
        int offset = (int) parser.getCodeStreamPosition();
        File srcFile = srcFilePath.toFile();
        int length = (int) srcFile.length();
        int itemLen = length;
        File destFile = destFilePath.toFile();
        try (DicomOutputStream dos = new DicomOutputStream(destFile)) {
            dos.writeDataset(fileMetadata.createFileMetaInformation(parser.getTransferSyntaxUID()), fileMetadata);
            dos.writeHeader(Tag.PixelData, VR.OB, -1);
            dos.writeHeader(Tag.Item, null, 0);
            long positionAfterAPPSegments = parser.getPositionAfterAPPSegments();
            if (noAPPn && fileType == FileType.JPEG && positionAfterAPPSegments != -1L) {
                offset = (int) positionAfterAPPSegments;
                length -= offset;
                itemLen -= offset - 2;
                dos.writeHeader(Tag.Item, null, (itemLen + 1) & ~1);
                dos.write((byte) -1);
                dos.write((byte) JPEG.SOI);
            } else {
                if (offset > 0)
                    itemLen = length -= offset;
                dos.writeHeader(Tag.Item, null, (itemLen + 1) & ~1);
            }

            // Faster chunked read
            try (RandomAccessFile file = new RandomAccessFile(srcFilePath.toFile(), "r");
                    FileChannel fileChannel = file.getChannel();) {
                MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());

                // Ignore offseted byte
                while (offset > 0 && mappedByteBuffer.hasRemaining()) {
                    mappedByteBuffer.get();
                    --offset;
                }

                // Write file content
                while (mappedByteBuffer.hasRemaining()) {
                    dos.write(mappedByteBuffer.get());
                }
            }

            if ((itemLen & 1) != 0) {
                dos.write(0);
            }

            dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
        }
        System.out.println(MessageFormat.format(rb.getString("converted"), srcFile, destFile));
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
        JPEG(UID.SecondaryCaptureImageStorage,
                vlPhotographicImage
                        ? "resource:vlPhotographicImageMetadata.xml" : "resource:secondaryCaptureImageMetadata.xml") {
            @Override
            void parse(SeekableByteChannel channel) throws IOException {
                setParser(new JPEGParser(channel));
            }
        },
        MPEG(UID.VideoPhotographicImageStorage, "resource:vlPhotographicImageMetadata.xml") {
            @Override
            void parse(SeekableByteChannel channel) throws IOException {
                setParser(new MPEG2Parser(channel));
            }
        },
        MP4(UID.VideoPhotographicImageStorage, "resource:vlPhotographicImageMetadata.xml") {
            @Override
            void parse(SeekableByteChannel channel) throws IOException {
                setParser(new MP4Parser(channel));
            }
        };

        abstract void parse(SeekableByteChannel channel) throws IOException;

        private final String cuid;
        private final String sampleMetadataFile;
        private XPEGParser parser;

        FileType(String cuid, String sampleMetadataFile) {
            this.cuid = cuid;
            this.sampleMetadataFile = sampleMetadataFile;
        }

        public String getSOPClassUID() {
            return cuid;
        }

        String getSampleMetadataFile() {
            return sampleMetadataFile;
        }

        public XPEGParser getParser() {
            return parser;
        }

        void setParser(XPEGParser parser) {
            this.parser = parser;
        }

        static FileType valueOf(Path path) throws IOException {
            String contentType = Files.probeContentType(path);
            try {
                String subType = contentType.substring(contentType.indexOf("/") + 1).toUpperCase();
                return subType.equals("JP2") ? JPEG : valueOf(subType);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        MessageFormat.format(rb.getString("invalid-file-ext"), contentType, path));
            }
        }
    }
}
