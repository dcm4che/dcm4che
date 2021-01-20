/* ***** BEGIN LICENSE BLOCK *****
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
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
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
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.tool.xml2dcm;

import org.apache.commons.cli.*;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.*;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.tool.common.CLIUtils;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 */
public class Xml2Dcm {

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che3.tool.xml2dcm.messages");

    private boolean lenient = false;
    private IncludeBulkData includeBulkData = IncludeBulkData.URI;
    private boolean catBlkFiles = false;
    private String blkFilePrefix = "blk";
    private String blkFileSuffix;
    private File blkDirectory;
    private BasicBulkDataDescriptor bulkDataDescriptor = new BasicBulkDataDescriptor();
    private String tsuid;
    private boolean withfmi;
    private boolean nofmi;
    private DicomEncodingOptions encOpts = DicomEncodingOptions.DEFAULT;
    private List<File> bulkDataFiles;
    private Attributes fmi;
    private Attributes dataset;

    public void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    public final void setIncludeBulkData(IncludeBulkData includeBulkData) {
        this.includeBulkData = includeBulkData;
    }

    public final void setConcatenateBulkDataFiles(boolean catBlkFiles) {
        this.catBlkFiles = catBlkFiles;
    }

    public final void setBulkDataFilePrefix(String blkFilePrefix) {
        this.blkFilePrefix = blkFilePrefix;
    }

    public final void setBulkDataFileSuffix(String blkFileSuffix) {
        this.blkFileSuffix = blkFileSuffix;
    }

    public final void setBulkDataDirectory(File blkDirectory) {
        this.blkDirectory = blkDirectory;
    }

    public void setBulkDataNoDefaults(boolean excludeDefaults) {
        bulkDataDescriptor.excludeDefaults(excludeDefaults);
    }

    public void setBulkDataLengthsThresholdsFromStrings(String[] thresholds) {
        bulkDataDescriptor.setLengthsThresholdsFromStrings(thresholds);
    }

    public final void setTransferSyntax(String uid) {
        this.tsuid = uid;
    }

    public final void setWithFileMetaInformation(boolean withfmi) {
        this.withfmi = withfmi;
    }

    public final void setNoFileMetaInformation(boolean nofmi) {
        this.nofmi = nofmi;
    }

    public final void setEncodingOptions(DicomEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        addLenientOption(opts);
        addIOFileNameOptions(opts);
        addBulkdataOptions(opts);
        addFileEncodingOptions(opts);
        CommandLine cl = CLIUtils.parseComandLine(args, opts, rb, Xml2Dcm.class);
        if (!(cl.hasOption("x") || cl.hasOption("i")))
            throw new ParseException(rb.getString("missing-i-x"));
        return cl;
    }

    private static void addLenientOption(Options opts) {
        opts.addOption(Option.builder()
                .longOpt("lenient")
                .desc(rb.getString("lenient"))
                .build());
    }

    private static void addIOFileNameOptions(Options opts) {
         opts.addOption(Option.builder("x")
                 .hasArg()
                 .argName("xml-file")
                 .desc(rb.getString("x-file"))
                 .build());
         opts.addOption(Option.builder("i")
                 .hasArg()
                 .argName("dicom-file")
                 .desc(rb.getString("i-file"))
                 .build());
         opts.addOption(Option.builder("o")
                 .hasArg()
                 .argName("dicom-file")
                 .desc(rb.getString("o-file"))
                 .build());
      }


     private static void addBulkdataOptions(Options opts) {
         OptionGroup blkGroup = new OptionGroup();
         blkGroup.addOption(Option.builder("B")
                 .longOpt("no-bulkdata")
                 .desc(rb.getString("no-bulkdata"))
                 .build());
         blkGroup.addOption(Option.builder("b")
                 .longOpt("alloc-bulkdata")
                 .desc(rb.getString("alloc-bulkdata"))
                 .build());
         opts.addOptionGroup(blkGroup);
         opts.addOption(Option.builder("d")
                 .longOpt("blk-file-dir")
                 .hasArg()
                 .argName("directory")
                 .desc(rb.getString("blk-file-dir"))
                 .build());
         opts.addOption(Option.builder()
                 .longOpt("blk-file-prefix")
                 .hasArg()
                 .argName("prefix")
                 .desc(rb.getString("blk-file-prefix"))
                 .build());
         opts.addOption(Option.builder()
                 .longOpt("blk-file-suffix")
                 .hasArg()
                 .argName("suffix")
                 .desc(rb.getString("blk-file-suffix"))
                 .build());
         opts.addOption("c", "cat-blk-files", false,
                  rb.getString("cat-blk-files"));
         opts.addOption(null, "keep-blk-files", false,
                 rb.getString("keep-blk-files"));
         opts.addOption(null, "blk-nodefs", false,
                 rb.getString("blk-nodefs"));
         opts.addOption(Option.builder(null)
                 .longOpt("blk")
                 .hasArgs()
                 .argName("[seq.]attr")
                 .desc(rb.getString("blk"))
                 .build());
         opts.addOption(Option.builder(null)
                 .longOpt("blk-vr")
                 .hasArgs()
                 .argName("vr[,...]=length")
                 .desc(rb.getString("blk-vr"))
                 .build());
     }

     private static void addFileEncodingOptions(Options opts) {
        opts.addOption(Option.builder("t")
                .longOpt("transfer-syntax")
                .hasArg()
                .argName("uid")
                .desc(rb.getString("transfer-syntax"))
                .build());
        OptionGroup fmiGroup = new OptionGroup();
        fmiGroup.addOption(Option.builder("F")
                .longOpt("no-fmi")
                .desc(rb.getString("no-fmi"))
                .build());
        fmiGroup.addOption(Option.builder("f")
                .longOpt("fmi")
                .desc(rb.getString("fmi"))
                .build());
        opts.addOptionGroup(fmiGroup);
        CLIUtils.addEncodingOptions(opts);
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Xml2Dcm main = new Xml2Dcm();
            configureBulkdata(main, cl);
            main.setLenient(cl.hasOption("lenient"));
            if (cl.hasOption("t")) {
                main.setTransferSyntax(cl.getOptionValue("t"));
            }
            main.setWithFileMetaInformation(cl.hasOption("f"));
            main.setNoFileMetaInformation(cl.hasOption("F"));
            main.setEncodingOptions(CLIUtils.encodingOptionsOf(cl));
            try {
                if (cl.hasOption("i")) {
                    String fname = cl.getOptionValue("i");
                    if (fname.equals("-")) {
                        main.parse(new DicomInputStream(System.in));
                    } else {
                        DicomInputStream dis = 
                                new DicomInputStream(new File(fname));
                        try {
                            main.parse(dis);
                        } finally {
                            dis.close();
                        }
                    }
                }

                if (cl.hasOption("x"))
                    main.mergeXML(cl.getOptionValue("x"));

                OutputStream out = cl.hasOption("o") 
                        ? new FileOutputStream(cl.getOptionValue("o"))
                        : new FileOutputStream(FileDescriptor.out);
                try {
                    main.writeTo(out);
                } finally {
                    out.close();
                }
            } finally {
                if (!cl.hasOption("keep-blk-files"))
                    main.delBulkDataFiles();
            }
        } catch (ParseException e) {
            System.err.println("xml2dcm: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("xml2dcm: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void configureBulkdata(Xml2Dcm xml2dcm, CommandLine cl)
            throws Exception {
        if (cl.hasOption("b")) {
            xml2dcm.setIncludeBulkData(IncludeBulkData.YES);
        }
        if (cl.hasOption("B")) {
            xml2dcm.setIncludeBulkData(IncludeBulkData.NO);
        }
        if (cl.hasOption("blk-file-prefix")) {
            xml2dcm.setBulkDataFilePrefix(
                    cl.getOptionValue("blk-file-prefix"));
        }
        if (cl.hasOption("blk-file-suffix")) {
            xml2dcm.setBulkDataFileSuffix(
                    cl.getOptionValue("blk-file-suffix"));
        }
        if (cl.hasOption("d")) {
            File tempDir = new File(cl.getOptionValue("d"));
            xml2dcm.setBulkDataDirectory(tempDir);
        }
        xml2dcm.setConcatenateBulkDataFiles(cl.hasOption("c"));
        xml2dcm.setBulkDataNoDefaults(cl.hasOption("blk-nodefs"));
        if (cl.hasOption("blk")) {
            CLIUtils.addTagPaths(xml2dcm.bulkDataDescriptor, cl.getOptionValues("blk"));
        }
        if (cl.hasOption("blk-vr")) {
            xml2dcm.setBulkDataLengthsThresholdsFromStrings(cl.getOptionValues("blk-vr"));
        }
    }

    public void writeTo(OutputStream out) throws IOException {
        if (nofmi)
            fmi = null;
        else if (fmi == null
                ? withfmi
                : tsuid != null && !tsuid.equals(
                        fmi.getString(Tag.TransferSyntaxUID, null))) {
            fmi = dataset.createFileMetaInformation(tsuid);
        }
        DicomOutputStream dos = new DicomOutputStream(
                new BufferedOutputStream(out),
                fmi != null
                        ? UID.ExplicitVRLittleEndian
                        : tsuid != null 
                                ? tsuid
                                : UID.ImplicitVRLittleEndian);
        dos.setEncodingOptions(encOpts);
        dos.writeDataset(fmi, dataset);
        dos.finish();
        dos.flush();
    }

    public void delBulkDataFiles() {
        if (bulkDataFiles != null)
            for (File f : bulkDataFiles)
                f.delete();
    }

    public void parse(DicomInputStream dis) throws IOException {
        dis.setIncludeBulkData(includeBulkData);
        dis.setBulkDataDescriptor(bulkDataDescriptor);
        dis.setBulkDataDirectory(blkDirectory);
        dis.setBulkDataFilePrefix(blkFilePrefix);
        dis.setBulkDataFileSuffix(blkFileSuffix);
        dis.setConcatenateBulkDataFiles(catBlkFiles);
        dataset = dis.readDataset();
        fmi = dis.getFileMetaInformation();
        bulkDataFiles = dis.getBulkDataFiles();
    }

    public void mergeXML(String fname) throws Exception {
        ContentHandlerAdapter ch = new ContentHandlerAdapter(dataset, lenient);
        parseXML(fname, ch);
        dataset = ch.getDataset();
        Attributes fmi2 = ch.getFileMetaInformation();
        if (fmi2 != null)
            fmi = fmi2;
    }

    public static Attributes parseXML(String fname) throws Exception {
        Attributes attrs = new Attributes();
        ContentHandlerAdapter ch = new ContentHandlerAdapter(attrs);
        parseXML(fname, ch);
        return attrs;
    }

    private static void parseXML(String fname, ContentHandlerAdapter ch)
            throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = f.newSAXParser();
        if (fname.equals("-")) {
            p.parse(System.in, ch);
        } else {
            p.parse(new File(fname), ch);
        }
    }

}
