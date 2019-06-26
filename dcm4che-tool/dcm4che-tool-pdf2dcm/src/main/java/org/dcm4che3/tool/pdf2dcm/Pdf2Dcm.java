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

package org.dcm4che3.tool.pdf2dcm;

import org.apache.commons.cli.*;
import org.dcm4che3.data.*;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.UIDUtils;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since June 2018
 */

public class Pdf2Dcm {
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.pdf2dcm.messages");
    private static final long MAX_FILE_SIZE = 0x7FFFFFFE;

    private static final int[] IUID_TAGS = {
            Tag.StudyInstanceUID,
            Tag.SeriesInstanceUID,
            Tag.SOPInstanceUID
    };

    private Attributes metadata;

    private void setMetadata(Attributes metadata) {
        this.metadata = metadata;
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Pdf2Dcm pdf2Dcm = new Pdf2Dcm();
            final List<String> argList = cl.getArgList();
            File inFile = new File(argList.get(0));
            pdf2Dcm.setMetadata(createMetadata(cl, inFile));
            pdf2Dcm.convert(inFile, new File(argList.get(1)));
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
                .valueSeparator()
                .desc(rb.getString("metadata"))
                .build());
        opts.addOption(Option.builder("f")
                .hasArg()
                .argName("xml-file")
                .desc(rb.getString("file"))
                .build());
        CommandLine cl = CLIUtils.parseComandLine(args, opts, rb, Pdf2Dcm.class);
        int numArgs = cl.getArgList().size();
        if (numArgs == 0)
            throw new ParseException(rb.getString("missing"));
        if (numArgs > 2)
            throw new ParseException(rb.getString("too-many"));
        return cl;
    }

    enum FileType {
        PDF("resource:encapsulatedPDFMetadata.xml"),
        CDA("resource:encapsulatedCDAMetadata.xml");

        String sampleMetadataURL;

        FileType(String sampleMetadataURL) {
            this.sampleMetadataURL = sampleMetadataURL;
        }
    }

    private static Attributes createMetadata(CommandLine cl, File bulkDataFile) throws Exception {
        Attributes metadata = SAXReader.parse(StreamUtils.openFileOrURL(getFileType(bulkDataFile).sampleMetadataURL));
        if (cl.hasOption("f"))
            metadata.addAll(SAXReader.parse(cl.getOptionValue("f")));
        CLIUtils.addAttributes(metadata, cl.getOptionValues("m"));
        supplementMissingUIDs(metadata);
        supplementMissingDateTime(metadata, Tag.ContentDateAndTime, new Date());
        supplementMissingDateTime(metadata, Tag.AcquisitionDateTime, new Date(bulkDataFile.lastModified()));
        return metadata;
    }

    private static FileType getFileType(File bulkDataFile) throws IOException {
        String bulkDataFileName = bulkDataFile.getPath();
        FileType fileType;
        byte[] buffer = new byte[5];
        InputStream is = new FileInputStream(bulkDataFileName);
        StreamUtils.readAvailable(is, buffer, 0, 5);
        is.close();
        if (buffer[0] == 0x25 && // %
                buffer[1] == 0x50 && // P
                buffer[2] == 0x44 && // D
                buffer[3] == 0x46 && // F
                buffer[4] == 0x2D) {
            fileType = FileType.PDF;
        } else {
            try {
                SAXParserFactory f = SAXParserFactory.newInstance();
                SAXParser p = f.newSAXParser();
                p.parse(bulkDataFileName, new DefaultHandler());
                fileType = FileType.CDA;
            } catch (Exception e) {
                throw new IllegalArgumentException("File type not supported.");
            }
        }
        return fileType;
    }

    private void convert(File infile, File outfile) throws IOException {
        long fileLength = infile.length();
        if (fileLength > MAX_FILE_SIZE)
            throw new IllegalArgumentException(MessageFormat.format(rb.getString("file-too-large"), infile));

        try (DicomOutputStream dos = new DicomOutputStream(outfile)) {
            dos.writeDataset(metadata.createFileMetaInformation(UID.ExplicitVRLittleEndian), metadata);
            dos.writeAttribute(Tag.EncapsulatedDocument, VR.OB, Files.readAllBytes(infile.toPath()));
        }
        System.out.println(MessageFormat.format(rb.getString("converted"), infile, outfile));
    }

    private static void supplementMissingUIDs(Attributes metadata) {
        for (int tag : IUID_TAGS)
            if (!metadata.containsValue(tag))
                metadata.setString(tag, VR.UI, UIDUtils.createUID());
    }

    private static void supplementMissingDateTime(Attributes metadata, long tag, Date date) {
        if (!metadata.containsValue((int) (tag >>> 32)))
            metadata.setDate(tag, date);
    }
}
