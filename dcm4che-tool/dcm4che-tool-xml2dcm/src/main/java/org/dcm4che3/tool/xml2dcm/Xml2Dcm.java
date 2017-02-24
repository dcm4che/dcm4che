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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.ContentHandlerAdapter;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.tool.common.CLIUtils;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Tool to convert XML to DICOM.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Xml2Dcm {

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che3.tool.xml2dcm.messages");

    private IncludeBulkData includeBulkData = IncludeBulkData.URI;
    private boolean catBlkFiles = false;
    private String blkFilePrefix = "blk";
    private String blkFileSuffix;
    private File blkDirectory;
    private Attributes blkAttrs;
    private String tsuid;
    private boolean withfmi;
    private boolean nofmi;
    private DicomEncodingOptions encOpts = DicomEncodingOptions.DEFAULT;
    private List<File> bulkDataFiles;
    private Attributes fmi;
    private Attributes dataset;

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

    public final void setBulkDataAttributes(Attributes blkAttrs) {
        this.blkAttrs = blkAttrs;
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
        addIOFileNameOptions(opts);
        addBulkdataOptions(opts);
        addFileEncodingOptions(opts);
        CommandLine cl = CLIUtils.parseComandLine(args, opts, rb, Xml2Dcm.class);
        if (!(cl.hasOption("x") || cl.hasOption("i")))
            throw new ParseException(rb.getString("missing-i-x"));
        return cl;
    }

     @SuppressWarnings("static-access")
     private static void addIOFileNameOptions(Options opts) {
         opts.addOption(OptionBuilder
                 .hasArg()
                 .withArgName("xml-file")
                 .withDescription(rb.getString("x-file"))
                 .create("x"));
         opts.addOption(OptionBuilder
                 .hasArg()
                 .withArgName("dicom-file")
                 .withDescription(rb.getString("i-file"))
                 .create("i"));
         opts.addOption(OptionBuilder
                 .hasArg()
                 .withArgName("dicom-file")
                 .withDescription(rb.getString("o-file"))
                 .create("o"));
      }


     @SuppressWarnings("static-access")
     private static void addBulkdataOptions(Options opts) {
         OptionGroup blkGroup = new OptionGroup();
         blkGroup.addOption(OptionBuilder
                 .withLongOpt("no-bulkdata")
                 .withDescription(rb.getString("no-bulkdata"))
                 .create("B"));
         blkGroup.addOption(OptionBuilder
                 .withLongOpt("alloc-bulkdata")
                 .withDescription(rb.getString("alloc-bulkdata"))
                 .create("b"));
         opts.addOptionGroup(blkGroup);
         opts.addOption(OptionBuilder
                 .withLongOpt("blk-file-dir")
                 .hasArg()
                 .withArgName("directory")
                 .withDescription(rb.getString("blk-file-dir"))
                 .create("d"));
         opts.addOption(OptionBuilder
                 .withLongOpt("blk-file-prefix")
                 .hasArg()
                 .withArgName("prefix")
                 .withDescription(rb.getString("blk-file-prefix"))
                 .create());
         opts.addOption(OptionBuilder
                 .withLongOpt("blk-file-suffix")
                 .hasArg()
                 .withArgName("suffix")
                 .withDescription(rb.getString("blk-file-suffix"))
                 .create());
         opts.addOption("c", "cat-blk-files", false,
                  rb.getString("cat-blk-files"));
         opts.addOption(null, "keep-blk-files", false,
                 rb.getString("keep-blk-files"));
         opts.addOption(OptionBuilder
                 .withLongOpt("blk-spec")
                 .hasArg()
                 .withArgName("xml-file")
                 .withDescription(rb.getString("blk-spec"))
                 .create("X"));
     }

     @SuppressWarnings("static-access")
     private static void addFileEncodingOptions(Options opts) {
        opts.addOption(OptionBuilder
                .withLongOpt("transfer-syntax")
                .hasArg()
                .withArgName("uid")
                .withDescription(rb.getString("transfer-syntax"))
                .create("t"));
        OptionGroup fmiGroup = new OptionGroup();
        fmiGroup.addOption(OptionBuilder
                .withLongOpt("no-fmi")
                .withDescription(rb.getString("no-fmi"))
                .create("F"));
        fmiGroup.addOption(OptionBuilder
                .withLongOpt("fmi")
                .withDescription(rb.getString("fmi"))
                .create("f"));
        opts.addOptionGroup(fmiGroup);
        CLIUtils.addEncodingOptions(opts);
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Xml2Dcm main = new Xml2Dcm();
            configureBulkdata(main, cl);
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
        if (cl.hasOption("X")) {
            xml2dcm.setBulkDataAttributes(
                    parseXML(cl.getOptionValue("X")));
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
        if (blkAttrs != null)
            dis.setBulkDataDescriptor(BulkDataDescriptor.valueOf(blkAttrs));
        dis.setBulkDataDirectory(blkDirectory);
        dis.setBulkDataFilePrefix(blkFilePrefix);
        dis.setBulkDataFileSuffix(blkFileSuffix);
        dis.setConcatenateBulkDataFiles(catBlkFiles);
        dataset = dis.readDataset(-1, -1);
        fmi = dis.getFileMetaInformation();
        bulkDataFiles = dis.getBulkDataFiles();
    }

    public void mergeXML(String fname) throws Exception {
        if (dataset == null)
            dataset = new Attributes();
        ContentHandlerAdapter ch = new ContentHandlerAdapter(dataset);
        parseXML(fname, ch);
        Attributes fmi2 = ch.getFileMetaInformation();
        if (fmi2 != null)
            fmi = fmi2;
    }


    public void mergeXML(InputStream inputStream) throws Exception {
        if (dataset == null) {
            dataset = new Attributes();
        }
        ContentHandlerAdapter ch = new ContentHandlerAdapter(dataset);
        parseXML(inputStream, ch);
        Attributes fmi2 = ch.getFileMetaInformation();
        if (fmi2 != null) {
            fmi = fmi2;
        }
    }

    public static Attributes parseXML(String fname) throws Exception {
        Attributes attrs = new Attributes();
        ContentHandlerAdapter ch = new ContentHandlerAdapter(attrs);
        parseXML(fname, ch);
        return attrs;
    }

    private static void parseXML(String fname, ContentHandlerAdapter ch)
            throws Exception {
        if (fname.equals("-")) {
            parseXML(System.in, ch);
        } else {
            parseXML(new FileInputStream(fname), ch);
        }
    }

    private static void parseXML(InputStream inputStream, ContentHandlerAdapter ch)
            throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = f.newSAXParser();
        p.parse(inputStream, ch);
    }

}
