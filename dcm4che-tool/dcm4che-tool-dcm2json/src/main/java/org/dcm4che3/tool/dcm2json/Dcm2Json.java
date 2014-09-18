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

package org.dcm4che3.tool.dcm2json;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.SafeClose;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Dcm2Json {

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che3.tool.dcm2json.messages");

    private boolean indent = false;
    private IncludeBulkData includeBulkData = IncludeBulkData.URI;
    private boolean catBlkFiles = false;
    private String blkFilePrefix = "blk";
    private String blkFileSuffix;
    private File blkDirectory;
    private Attributes blkAttrs;

    public final void setIndent(boolean indent) {
        this.indent = indent;
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

    public final void setBulkDataAttributes(Attributes blkAttrs) {
        this.blkAttrs = blkAttrs;
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        opts.addOption("I", "indent", false, rb.getString("indent"));
        addBulkdataOptions(opts);

        return CLIUtils.parseComandLine(args, opts, rb, Dcm2Json.class);
    }

    @SuppressWarnings("static-access")
    private static void addBulkdataOptions(Options opts) {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder
                .withLongOpt("no-bulkdata")
                .withDescription(rb.getString("no-bulkdata"))
                .create("B"));
        group.addOption(OptionBuilder
                .withLongOpt("with-bulkdata")
                .withDescription(rb.getString("with-bulkdata"))
                .create("b"));
        opts.addOptionGroup(group);
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
                .withDescription(rb.getString("blk-file-dir"))
                .create());
        opts.addOption("c", "cat-blk-files", false,
                rb.getString("cat-blk-files"));
        opts.addOption(OptionBuilder
                .withLongOpt("blk-spec")
                .hasArg()
                .withArgName("json-file")
                .withDescription(rb.getString("blk-spec"))
                .create("J"));
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Dcm2Json main = new Dcm2Json();
            main.setIndent(cl.hasOption("I"));
            configureBulkdata(main, cl);
            String fname = fname(cl.getArgList());
            if (fname.equals("-")) {
                main.parse(new DicomInputStream(System.in));
            } else {
                DicomInputStream dis =
                        new DicomInputStream(new File(fname));
                try {
                   ByteArrayOutputStream os = (ByteArrayOutputStream) main.parse(dis);
                   System.out.println(os.toString());
                } finally {
                    dis.close();
                }
            }
        } catch (ParseException e) {
            System.err.println("dcm2json: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("dcm2json: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void configureBulkdata(Dcm2Json dcm2json, CommandLine cl)
            throws Exception {
        if (cl.hasOption("b")) {
            dcm2json.setIncludeBulkData(IncludeBulkData.YES);
        }
        if (cl.hasOption("B")) {
            dcm2json.setIncludeBulkData(IncludeBulkData.NO);
        }
        if (cl.hasOption("blk-file-prefix")) {
            dcm2json.setBulkDataFilePrefix(
                    cl.getOptionValue("blk-file-prefix"));
        }
        if (cl.hasOption("blk-file-suffix")) {
            dcm2json.setBulkDataFileSuffix(
                    cl.getOptionValue("blk-file-suffix"));
        }
        if (cl.hasOption("d")) {
            File tempDir = new File(cl.getOptionValue("d"));
            dcm2json.setBulkDataDirectory(tempDir);
        }
        dcm2json.setConcatenateBulkDataFiles(cl.hasOption("c"));
        if (cl.hasOption("J")) {
            dcm2json.setBulkDataAttributes(
                    parseJSON(cl.getOptionValue("J")));
        }
    }

    private static Attributes parseJSON(String fname) throws Exception {
        InputStream in = new FileInputStream(fname);
        try {
            JSONReader reader = new JSONReader(
                    Json.createParser(new InputStreamReader(in, "UTF-8")));
             return reader.readDataset(null);
        } finally {
            SafeClose.close(in);
        }
    }


    private static String fname(List<String> argList) throws ParseException {
        int numArgs = argList.size();
        if (numArgs == 0)
            throw new ParseException(rb.getString("missing"));
        if (numArgs > 1)
            throw new ParseException(rb.getString("too-many"));
        return argList.get(0);
    }

    public OutputStream parse(DicomInputStream dis) throws IOException {
        dis.setIncludeBulkData(includeBulkData);
        if (blkAttrs != null)
            dis.setBulkDataDescriptor(BulkDataDescriptor.valueOf(blkAttrs));
        dis.setBulkDataDirectory(blkDirectory);
        dis.setBulkDataFilePrefix(blkFilePrefix);
        dis.setBulkDataFileSuffix(blkFileSuffix);
        dis.setConcatenateBulkDataFiles(catBlkFiles);
        OutputStream os = new ByteArrayOutputStream(10*8*1024);
        JsonGenerator jsonGen = createGenerator(os);
        JSONWriter jsonWriter = new JSONWriter(jsonGen);
        dis.setDicomInputHandler(jsonWriter);
        dis.readDataset(-1, -1);
        jsonGen.flush();
        return os;
    }

    private JsonGenerator createGenerator(OutputStream out) {
        Map<String, ?> conf = new HashMap<String, Object>(2);
        if (indent)
            conf.put(JsonGenerator.PRETTY_PRINTING, null);
        return Json.createGeneratorFactory(conf).createGenerator(out);
    }

}
