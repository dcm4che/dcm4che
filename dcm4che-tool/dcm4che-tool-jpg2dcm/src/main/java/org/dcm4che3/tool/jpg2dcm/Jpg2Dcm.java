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
import org.dcm4che3.util.ByteUtils;
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

    private static final int[] TYPE2_TAGS = {
            Tag.ContentDate,
            Tag.ContentTime
    };

    private boolean noAPPn;
    private boolean photo;
    private String tsuid;
    private ContentType contentType;
    private long fragmentLength = 4294967294L; // 2^32-2;
    private Attributes staticMetadata = new Attributes();
    private byte[] buf = new byte[BUFFER_SIZE];

    private void setNoAPPn(boolean noAPPn) {
        this.noAPPn = noAPPn;
    }

    private void setPhoto(boolean photo) {
        this.photo = photo;
    }

    private void setTSUID(String tsuid) {
        this.tsuid = tsuid;
    }

    public void setContentType(String s) {
        ContentType contentType = ContentType.of(s);
        if (contentType == null)
            throw new IllegalArgumentException(MessageFormat.format(rb.getString("unsupported-content-type"), s));
        this.contentType = contentType;
    }

    public void setFragmentLength(long fragmentLength) {
        if (fragmentLength < 1024 || fragmentLength > 4294967294L)
            throw new IllegalArgumentException("Maximal Fragment Length must be in the range of [1024, 4294967294].");
        this.fragmentLength = fragmentLength & ~1;
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Jpg2Dcm main = new Jpg2Dcm();
            List<String> argList = cl.getArgList();
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
            main.setTSUID(cl.getOptionValue("tsuid", null));
            if (cl.hasOption("content-type"))
                main.setContentType(cl.getOptionValue("content-type"));
            if (cl.hasOption("F"))
                main.setFragmentLength(Long.parseLong(cl.getOptionValue("F")));
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
        opts.addOption(Option.builder("s")
                .hasArgs()
                .argName("[seq.]attr=value")
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
                .longOpt("tsuid")
                .hasArg()
                .argName("uid")
                .desc(rb.getString("tsuid"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("content-type")
                .hasArg()
                .argName("type")
                .desc(rb.getString("content-type"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("no-app")
                .hasArg(false)
                .desc(rb.getString("no-app"))
                .build());
        opts.addOption(Option.builder("F")
                .longOpt("fragment")
                .hasArg()
                .argName("length")
                .desc(rb.getString("fragment"))
                .build());
        return CLIUtils.parseComandLine(args, opts, rb, Jpg2Dcm.class);
    }

    private static void createStaticMetadata(CommandLine cl, Attributes staticMetadata) throws Exception {
        if (cl.hasOption("f"))
            SAXReader.parse(cl.getOptionValue("f"), staticMetadata);

        CLIUtils.addAttributes(staticMetadata, cl.getOptionValues("s"));
        supplementMissingUIDs(staticMetadata);
        supplementMissingValue(staticMetadata, Tag.SeriesNumber, "999");
        supplementMissingValue(staticMetadata, Tag.InstanceNumber, "1");
        supplementType2Tags(staticMetadata);
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
        ContentType contentType = this.contentType;
        if (contentType == null) {
            String probeContentType = Files.probeContentType(srcFilePath);
            if (probeContentType == null) {
                System.out.println(MessageFormat.format(rb.getString("probe-content-type-failed"), srcFilePath));
                return;
            }
            contentType = ContentType.of(probeContentType);
            if (contentType == null) {
                System.out.println(MessageFormat.format(
                        rb.getString("unsupported-content-type-of-file"), probeContentType, srcFilePath));
                return;
            }
        }
        Attributes fileMetadata = SAXReader.parse(StreamUtils.openFileOrURL(contentType.getSampleMetadataFile(photo)));
        fileMetadata.addAll(staticMetadata);
        supplementMissingValue(fileMetadata, Tag.SOPClassUID, contentType.getSOPClassUID(photo));
        try (SeekableByteChannel channel = Files.newByteChannel(srcFilePath);
                DicomOutputStream dos = new DicomOutputStream(destFilePath.toFile())) {
            XPEGParser parser = contentType.newParser(channel);
            parser.getAttributes(fileMetadata);
            byte[] prefix = ByteUtils.EMPTY_BYTES;
            if (noAPPn && parser.getPositionAfterAPPSegments() > 0) {
                channel.position(parser.getPositionAfterAPPSegments());
                prefix = new byte[] { (byte) 0xFF, (byte) JPEG.SOI };
            } else {
                channel.position(parser.getCodeStreamPosition());
            }
            long codeStreamSize = channel.size() - channel.position() + prefix.length;
            dos.writeDataset(fileMetadata.createFileMetaInformation(
                    tsuid != null ? tsuid : parser.getTransferSyntaxUID(codeStreamSize > fragmentLength)),
                            fileMetadata);
            dos.writeHeader(Tag.PixelData, VR.OB, -1);
            dos.writeHeader(Tag.Item, null, 0);
            do {
                long len = Math.min(codeStreamSize, fragmentLength);
                dos.writeHeader(Tag.Item, null, (int) ((len + 1) & ~1));
                dos.write(prefix);
                copy(channel, len - prefix.length, dos);
                if ((len & 1) != 0)
                    dos.write(0);
                prefix = ByteUtils.EMPTY_BYTES;
                codeStreamSize -= len;
            } while (codeStreamSize > 0);
            dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
        }
        System.out.println(MessageFormat.format(rb.getString("converted"), srcFilePath, destFilePath));
    }

    private void copy(ByteChannel in, long len, OutputStream out) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(buf);
        int read;
        while (len > 0){
            bb.position(0);
            bb.limit((int) Math.min(len, buf.length));
            read = in.read(bb);
            out.write(buf, 0, read);
            len -= read;
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

    private static void supplementType2Tags(Attributes metadata) {
        for (int tag : TYPE2_TAGS)
            if (!metadata.contains(tag))
                metadata.setNull(tag, DICT.vrOf(tag));
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

        static ContentType of(String type) {
            switch (type.toLowerCase()) {
                case "image/jpeg":
                case "image/jp2":
                case "image/j2c":
                case "image/jph":
                case "image/jphc":
                    return ContentType.IMAGE_JPEG;
                case "video/mpeg":
                    return ContentType.VIDEO_MPEG;
                case "video/mp4":
                case "video/quicktime":
                    return ContentType.VIDEO_MP4;
            }
            return null;
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
