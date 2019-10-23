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
import java.nio.channels.ByteChannel;
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
    private static final int BUFFER_SIZE = 8162;
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

    private boolean noAPPn;
    private boolean photo;
    private Attributes staticMetadata = new Attributes();
    private byte[] buf = new byte[BUFFER_SIZE];

    private void setNoAPPn(boolean noAPPn) {
        this.noAPPn = noAPPn;
    }

    private void setPhoto(boolean photo) {
        this.photo = photo;
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Jpg2Dcm main = new Jpg2Dcm();
            @SuppressWarnings("unchecked") final List<String> argList = cl.getArgList();
            int argc = argList.size();
            if (argc < 2)
                throw new ParseException(rb.getString("missing"));
            File dest = new File(argList.get(argc-1));
            if ((argc > 2 || new File(argList.get(0)).isDirectory())
                    && !dest.isDirectory())
                throw new ParseException(
                        MessageFormat.format(rb.getString("nodestdir"), dest));
            main.setNoAPPn(cl.hasOption("no-app"));
            main.setPhoto(cl.hasOption("xc"));
            createStaticMetadata(cl, main.staticMetadata);
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

    private static void createStaticMetadata(CommandLine cl, Attributes staticMetadata) throws Exception {
        if (cl.hasOption("f"))
            SAXReader.parse(cl.getOptionValue("f"), staticMetadata);

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
        ContentType fileType = ContentType.probe(srcFilePath);
        Attributes fileMetadata = SAXReader.parse(StreamUtils.openFileOrURL(fileType.getSampleMetadataFile(photo)));
        fileMetadata.addAll(staticMetadata);
        supplementMissingValue(fileMetadata, Tag.SOPClassUID, fileType.getSOPClassUID(photo));
        try (SeekableByteChannel channel = Files.newByteChannel(srcFilePath);
                DicomOutputStream dos = new DicomOutputStream(destFilePath.toFile())) {
            XPEGParser parser = fileType.newParser(channel);
            parser.getAttributes(fileMetadata);
            dos.writeDataset(fileMetadata.createFileMetaInformation(parser.getTransferSyntaxUID()), fileMetadata);
            dos.writeHeader(Tag.PixelData, VR.OB, -1);
            dos.writeHeader(Tag.Item, null, 0);
            if (noAPPn && parser.getPositionAfterAPPSegments() > 0) {
                copyPixelData(channel, parser.getPositionAfterAPPSegments(), dos,
                        (byte) 0xFF, (byte) JPEG.SOI);
            } else {
                copyPixelData(channel, parser.getCodeStreamPosition(), dos);
            }
            dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
        }
        System.out.println(MessageFormat.format(rb.getString("converted"), srcFilePath, destFilePath));
    }

    private void copyPixelData(SeekableByteChannel channel, long position, DicomOutputStream dos, byte... prefix)
            throws IOException {
        long codeStreamSize = channel.size() - position + prefix.length;
        dos.writeHeader(Tag.Item, null, (int) ((codeStreamSize + 1) & ~1));
        dos.write(prefix);
        channel.position(position);
        copy(channel, dos);
        if ((codeStreamSize & 1) != 0)
            dos.write(0);
    }

    private void copy(ByteChannel in, OutputStream out) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(buf);
        int read;
        while ((read = in.read(bb)) > 0) {
            out.write(buf, 0, read);
            bb.clear();
        }
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

    private enum ContentType {
        IMAGE_JPEG {
            @Override
            String getSampleMetadataFile(boolean photo) {
                return photo
                        ? "resource:vlPhotographicImageMetadata.xml"
                        : "resource:secondaryCaptureImageMetadata.xml";
            }

            @Override
            String getSOPClassUID(boolean photo) {
                return photo
                        ? UID.VLPhotographicImageStorage
                        : UID.SecondaryCaptureImageStorage;
            }

            @Override
            XPEGParser newParser(SeekableByteChannel channel) throws IOException {
                return new JPEGParser(channel);
            }
        },
        VIDEO_MPEG {
            @Override
            XPEGParser newParser(SeekableByteChannel channel) throws IOException {
                return new MPEG2Parser(channel);
            }
        },
        VIDEO_MP4 {
            @Override
            XPEGParser newParser(SeekableByteChannel channel) throws IOException {
                return new MP4Parser(channel);
            }
        };

        static ContentType probe(Path path) throws IOException {
            String type = Files.probeContentType(path);
            if (type == null)
                throw new IllegalArgumentException(
                        MessageFormat.format(rb.getString("unsupported-file-ext"), path));
            switch (type.toLowerCase()) {
                case "image/jpeg":
                case "image/jp2":
                    return ContentType.IMAGE_JPEG;
                case "video/mpeg":
                    return ContentType.VIDEO_MPEG;
                case "video/mp4":
                    return ContentType.VIDEO_MP4;
            }
            throw new IllegalArgumentException(
                    MessageFormat.format(rb.getString("unsupported-content-type"), type, path));
        }

        String getSampleMetadataFile(boolean photo) {
            return "resource:vlPhotographicImageMetadata.xml";
        }

        String getSOPClassUID(boolean photo) {
            return UID.VideoPhotographicImageStorage;
        }

        abstract XPEGParser newParser(SeekableByteChannel channel) throws IOException;
    }
}
