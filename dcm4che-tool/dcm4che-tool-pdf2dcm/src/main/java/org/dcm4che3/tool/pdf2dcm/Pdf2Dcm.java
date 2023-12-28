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
import java.util.Locale;
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
    private static FileContentType fileContentType;
    private static boolean encapsulatedDocLength;

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
            initialize(cl);
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
                .hasArg()
                .argName("contentType")
                .longOpt("contentType")
                .desc(rb.getString("contentType"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("encapsulatedDocLength")
                .desc(rb.getString("encapsulatedDocLength"))
                .build());
        return CLIUtils.parseComandLine(args, opts, rb, Pdf2Dcm.class);
    }

    enum FileContentType {
        PDF("resource:encapsulatedPDFMetadata.xml"),
        CDA("resource:encapsulatedCDAMetadata.xml"),
        STL("resource:encapsulatedSTLMetadata.xml"),
        MTL("resource:encapsulatedMTLMetadata.xml"),
        OBJ("resource:encapsulatedOBJMetadata.xml"),
        GENOZIP("resource:encapsulatedGenozipMetadata.xml"),
        VCF_BZIP2("resource:encapsulatedVCFBzip2Metadata.xml"),
        DOC_BZIP2("resource:encapsulatedDocumentBzip2Metadata.xml");

        private final String sampleMetadataFile;

        public String getSampleMetadataFile() {
            return sampleMetadataFile;
        }

        FileContentType(String sampleMetadataFile) {
            this.sampleMetadataFile = sampleMetadataFile;
        }

        static FileContentType valueOf(Path path) throws IOException {
            String fileName = path.toFile().getName();
            String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
            String contentType = Files.probeContentType(path);
            return fileContentType(contentType != null ? contentType : ext);
        }
    }

    private static FileContentType fileContentType(String s) {
        switch (s.toLowerCase(Locale.ENGLISH)) {
            case "stl":
            case "model/stl":
            case "model/x.stl-binary":
            case "application/sla":
                return FileContentType.STL;
            case "pdf":
            case "application/pdf":
                return FileContentType.PDF;
            case "xml":
            case "application/xml":
                return FileContentType.CDA;
            case "mtl":
            case "model/mtl":
                return FileContentType.MTL;
            case "obj":
            case "model/obj":
                return FileContentType.OBJ;
            case "genozip":
            case "application/vnd.genozip":
                return FileContentType.GENOZIP;
            case "vcf.bz2":
            case "vcfbzip2":
            case "vcfbz2":
            case "application/prs.vcfbzip2":
                return FileContentType.VCF_BZIP2;
            case "boz":
            case "bz2":
            case "application/x-bzip2":
                return FileContentType.DOC_BZIP2;
            default:
                throw new IllegalArgumentException(
                        MessageFormat.format(rb.getString("content-type-undetermined"), s));
        }
    }

    private static void initialize(CommandLine cl) throws Exception {
        createStaticMetadata(cl);
        if (cl.hasOption("contentType"))
            fileContentType = fileContentType(cl.getOptionValue("contentType"));
        encapsulatedDocLength = cl.hasOption("encapsulatedDocLength");
    }

    private static void createStaticMetadata(CommandLine cl) throws Exception {
        staticMetadata = new Attributes();
        if (cl.hasOption("f"))
            staticMetadata = SAXReader.parse(cl.getOptionValue("f"));

        CLIUtils.addAttributes(staticMetadata, cl.getOptionValues("s"));
        supplementMissingUIDs(staticMetadata);
        supplementType2Tags(staticMetadata);
    }

    private Attributes createMetadata(FileContentType fileContentType, File srcFile) throws Exception {
        Attributes fileMetadata = SAXReader.parse(StreamUtils.openFileOrURL(fileContentType.getSampleMetadataFile()));
        fileMetadata.addAll(staticMetadata);
        if ((fileContentType == FileContentType.STL
                || fileContentType == FileContentType.OBJ)
                && !fileMetadata.containsValue(Tag.FrameOfReferenceUID))
            fileMetadata.setString(Tag.FrameOfReferenceUID, VR.UI, UIDUtils.createUID());
        if (encapsulatedDocLength)
            fileMetadata.setLong(Tag.EncapsulatedDocumentLength, VR.UL, srcFile.length());
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
        private final Path srcPath;
        private final Path destPath;

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
        FileContentType fileContentType1 = fileContentType != null
                                            ? fileContentType : FileContentType.valueOf(srcFilePath);
        File srcFile = srcFilePath.toFile();
        File destFile = destFilePath.toFile();
        Attributes fileMetadata = createMetadata(fileContentType1, srcFile);
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
