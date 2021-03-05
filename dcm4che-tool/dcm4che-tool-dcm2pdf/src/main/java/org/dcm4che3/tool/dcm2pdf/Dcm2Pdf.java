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
 *  Portions created by the Initial Developer are Copyright (C) 2019
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

package org.dcm4che3.tool.dcm2pdf;

import org.apache.commons.cli.*;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.tool.common.CLIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since July 2019
 */

public class Dcm2Pdf {
    private static final Logger LOG = LoggerFactory.getLogger(Dcm2Pdf.class);
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.dcm2pdf.messages");
    private FileType fileType;

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Dcm2Pdf dcm2pdf = new Dcm2Pdf();
            dcm2pdf.setFileType(cl);
            final List<String> argList = cl.getArgList();
            int argc = argList.size();
            if (argc < 2)
                throw new ParseException(rb.getString("missing"));
            File dest = new File(argList.get(argc-1));
            if ((argc > 2 || new File(argList.get(0)).isDirectory())
                    && !dest.isDirectory())
                throw new ParseException(
                        MessageFormat.format(rb.getString("nodestdir"), dest));
            dcm2pdf.convert(cl.getArgList());
        } catch (ParseException e) {
            System.err.println("dcm2pdf: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("dcm2pdf: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static CommandLine parseComandLine(String[] args) throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder()
                .longOpt("cda")
                .hasArg(false)
                .desc(rb.getString("cda"))
                .build());
        group.addOption(Option.builder()
                .longOpt("mtl")
                .hasArg(false)
                .desc(rb.getString("mtl"))
                .build());
        group.addOption(Option.builder()
                .longOpt("obj")
                .hasArg(false)
                .desc(rb.getString("obj"))
                .build());
        group.addOption(Option.builder()
                .longOpt("stl")
                .hasArg(false)
                .desc(rb.getString("stl"))
                .build());
        opts.addOptionGroup(group);
        return CLIUtils.parseComandLine(args, opts, rb, Dcm2Pdf.class);
    }

    private void setFileType(CommandLine cl) {
        this.fileType = cl.hasOption("cda")
                ? FileType.CDA
                : cl.hasOption("mtl")
                    ? FileType.MTL
                    : cl.hasOption("obj")
                        ? FileType.OBJ
                        : cl.hasOption("stl")
                            ? FileType.STL :  FileType.PDF;
    }

    enum FileType {
        PDF(UID.EncapsulatedPDFStorage, ".pdf"),
        CDA(UID.EncapsulatedCDAStorage, ".xml"),
        MTL(UID.EncapsulatedMTLStorage, ".mtl"),
        OBJ(UID.EncapsulatedOBJStorage, ".obj"),
        STL(UID.EncapsulatedSTLStorage, ".stl");

        private final String cuid;
        private final String ext;

        FileType(String cuid, String ext) {
            this.cuid = cuid;
            this.ext = ext;
        }

        public String getCuid() {
            return cuid;
        }

        public String getExt() {
            return ext;
        }
    }

    private void convert(List<String> args) throws IOException {
        int argsSize = args.size();
        Path destPath = Paths.get(args.get(argsSize - 1));
        for (String src : args.subList(0, argsSize - 1)) {
            Path srcPath = Paths.get(src);
            if (Files.isDirectory(srcPath)) {
                Files.walkFileTree(srcPath, new Dcm2PdfFileVisitor(srcPath, destPath));
            } else if (Files.isDirectory(destPath)) {
                convert(srcPath, destPath.resolve(suffix(srcPath)));
            } else {
                convert(srcPath, destPath);
            }
        }
    }

    class Dcm2PdfFileVisitor extends SimpleFileVisitor<Path> {
        private final Path srcPath;
        private final Path destPath;

        Dcm2PdfFileVisitor(Path srcPath, Path destPath) {
            this.srcPath = srcPath;
            this.destPath = destPath;
        }

        @Override
        public FileVisitResult visitFile(Path srcFilePath, BasicFileAttributes attrs) throws IOException {
            Path destFilePath = resolveDestFilePath(srcFilePath);
            if (!Files.isDirectory(destFilePath))
                Files.createDirectories(destFilePath);
            convert(srcFilePath, destFilePath.resolve(suffix(srcFilePath)));
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

    private String suffix(Path src) {
        return src.getFileName() + fileType.getExt();
    }

    private void convert(Path src, Path dest) {
        try (DicomInputStream dis = new DicomInputStream(src.toFile())) {
            Attributes attributes = dis.readDataset();
            String sopCUID = attributes.getString(Tag.SOPClassUID);
            if (!sopCUID.equals(fileType.getCuid())) {
                LOG.info("DICOM file {} with {} SOP Class cannot be converted to file type {}",
                        src, UID.nameOf(sopCUID), fileType);
                return;
            }

            FileOutputStream fos = new FileOutputStream(dest.toFile());
            byte[] value = (byte[]) attributes.getValue(Tag.EncapsulatedDocument);
            fos.write(value, 0, value.length - 1);
            byte lastByte = value[value.length - 1];
            if (lastByte != 0)
                fos.write(lastByte);
            System.out.println(MessageFormat.format(rb.getString("converted"), src, dest));
        } catch (Exception e) {
            System.out.println(MessageFormat.format(rb.getString("failed"), src, e.getMessage()));
            e.printStackTrace(System.out);
        }
    }
}
