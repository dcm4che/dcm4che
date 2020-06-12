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
 *  Portions created by the Initial Developer are Copyright (C) 2018-2019
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

package org.dcm4che3.tool.pdf2dcm;

import org.apache.commons.cli.*;
import org.dcm4che3.data.*;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.UIDUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since June 2018
 */

public class Pdf2Dcm {
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.pdf2dcm.messages");
    private static final long MAX_FILE_SIZE = 0x7FFFFFFE;
    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();

    private static final int[] IUID_TAGS = {
            Tag.StudyInstanceUID,
            Tag.SeriesInstanceUID,
            Tag.SOPInstanceUID
    };

    private static final int[] TYPE2_TAGS = {
            Tag.ContentDate,
            Tag.ContentTime,
            Tag.AcquisitionDateTime
    };

    private static Attributes staticMetadata;

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Pdf2Dcm pdf2Dcm = new Pdf2Dcm();
            final List<String> argList = cl.getArgList();
            int argc = argList.size();
            if (argc < 2)
                throw new ParseException(rb.getString("missing"));
            File dest = new File(argList.get(argc-1));
            if ((argc > 2 || new File(argList.get(0)).isDirectory())
                    && !dest.isDirectory())
                throw new ParseException(
                        MessageFormat.format(rb.getString("nodestdir"), dest));
            createStaticMetadata(cl);
            pdf2Dcm.convert(cl.getArgList());
        } catch (ParseException e) {
            System.err.println("pdf2dcm: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("pdf2dcm: " + e.getMessage());
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
                .desc(rb.getString("metadata"))
                .build());
        opts.addOption(Option.builder("f")
                .hasArg()
                .argName("xml-file")
                .desc(rb.getString("file"))
                .build());
        return CLIUtils.parseComandLine(args, opts, rb, Pdf2Dcm.class);
    }

    enum FileType {
        PDF("resource:encapsulatedPDFMetadata.xml"),
        XML("resource:encapsulatedCDAMetadata.xml"),
        SLA("resource:encapsulatedSTLMetadata.xml"),
        MTL("resource:encapsulatedMTLMetadata.xml"),
        OBJ("resource:encapsulatedOBJMetadata.xml");

        private String sampleMetadataFile;

        public String getSampleMetadataFile() {
            return sampleMetadataFile;
        }

        FileType(String sampleMetadataFile) {
            this.sampleMetadataFile = sampleMetadataFile;
        }

        static FileType valueOf(Path path) throws IOException {
            String contentType = contentTypeOfFile(path);
            if (contentType == null)
                throw new IllegalArgumentException(
                        MessageFormat.format(rb.getString("content-type-undetermined"), path));

            try {
                return valueOf(contentType.substring(contentType.indexOf("/") + 1).toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        MessageFormat.format(rb.getString("invalid-content-type"), contentType, path));
            }
        }
    }

    private static String contentTypeOfFile(Path path) throws IOException {
        String contentType = Files.probeContentType(path);
        if (contentType != null)
            return contentType;

        String fileName = path.toFile().getName();
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
        return ext.equalsIgnoreCase("obj")
                ? "model/obj"
                : ext.equalsIgnoreCase("mtl")
                    ? "model/mtl" : null;
    }

    private static void createStaticMetadata(CommandLine cl) throws Exception {
        staticMetadata = new Attributes();
        if (cl.hasOption("f"))
            staticMetadata = SAXReader.parse(cl.getOptionValue("f"));

        CLIUtils.addAttributes(staticMetadata, cl.getOptionValues("m"));
        supplementMissingUIDs(staticMetadata);
        supplementType2Tags(staticMetadata);
    }

    private Attributes createMetadata(FileType fileType) throws Exception {
        Attributes fileMetadata = SAXReader.parse(StreamUtils.openFileOrURL(fileType.getSampleMetadataFile()));
        fileMetadata.addAll(staticMetadata);
        if ((fileType == FileType.SLA || fileType == FileType.OBJ) && !fileMetadata.containsValue(Tag.FrameOfReferenceUID))
            fileMetadata.setString(Tag.FrameOfReferenceUID, VR.UI, UIDUtils.createUID());
        return fileMetadata;
    }

    private void convert(List<String> args) throws Exception {
        int argsSize = args.size();
        Path destPath = Paths.get(args.get(argsSize - 1));
        for (String src : args.subList(0, argsSize - 1)) {
            Path srcPath = Paths.get(src);
            if (Files.isDirectory(srcPath))
                Files.walkFileTree(srcPath, new Pdf2DcmFileVisitor(srcPath, destPath));
            else if (Files.isDirectory(destPath))
                convert(srcPath, destPath.resolve(srcPath.getFileName() + ".dcm"));
            else
                convert(srcPath, destPath);
        }
    }

    class Pdf2DcmFileVisitor extends SimpleFileVisitor<Path> {
        private Path srcPath;
        private Path destPath;

        Pdf2DcmFileVisitor(Path srcPath, Path destPath) {
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
        Attributes fileMetadata = createMetadata(fileType);
        File srcFile = srcFilePath.toFile();
        File destFile = destFilePath.toFile();
        long fileLength = srcFile.length();
        if (fileLength > MAX_FILE_SIZE)
            throw new IllegalArgumentException(MessageFormat.format(rb.getString("file-too-large"), srcFile));

        try (DicomOutputStream dos = new DicomOutputStream(destFile)) {
            dos.writeDataset(fileMetadata.createFileMetaInformation(UID.ExplicitVRLittleEndian), fileMetadata);
            dos.writeAttribute(Tag.EncapsulatedDocument, VR.OB, Files.readAllBytes(srcFile.toPath()));
        }
        System.out.println(MessageFormat.format(rb.getString("converted"), srcFile, destFile));
    }

    private static void supplementMissingUIDs(Attributes metadata) {
        for (int tag : IUID_TAGS)
            if (!metadata.containsValue(tag))
                metadata.setString(tag, VR.UI, UIDUtils.createUID());
    }

    private static void supplementType2Tags(Attributes metadata) {
        for (int tag : TYPE2_TAGS)
            if (!metadata.contains(tag))
                metadata.setNull(tag, DICT.vrOf(tag));
    }
}
