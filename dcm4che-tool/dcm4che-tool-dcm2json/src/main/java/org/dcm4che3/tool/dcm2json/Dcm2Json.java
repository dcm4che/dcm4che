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

package org.dcm4che3.tool.dcm2json;

import jakarta.json.Json;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonGenerator;
import org.apache.commons.cli.*;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.BasicBulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.tool.common.CLIUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

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
    private BasicBulkDataDescriptor bulkDataDescriptor = new BasicBulkDataDescriptor();
    private boolean encodeAsNumber;

    public final void setIndent(boolean indent) {
        this.indent = indent;
    }

    public final void setEncodeAsNumber(boolean encodeAsNumber) {
        this.encodeAsNumber = encodeAsNumber;
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

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        opts.addOption("I", "indent", false, rb.getString("indent"));
        opts.addOption("N", "encode-as-number", false, rb.getString("encode-as-number"));
        addBulkdataOptions(opts);

        return CLIUtils.parseComandLine(args, opts, rb, Dcm2Json.class);
    }

    @SuppressWarnings("static-access")
    private static void addBulkdataOptions(Options opts) {
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("B")
                .longOpt("no-bulkdata")
                .desc(rb.getString("no-bulkdata"))
                .build());
        group.addOption(Option.builder("b")
                .longOpt("with-bulkdata")
                .desc(rb.getString("with-bulkdata"))
                .build());
        opts.addOptionGroup(group);
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

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Dcm2Json main = new Dcm2Json();
            main.setIndent(cl.hasOption("I"));
            main.setEncodeAsNumber(cl.hasOption("N"));
            configureBulkdata(main, cl);
            String fname = fname(cl.getArgList());
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
        } catch (ParseException e) {
            System.err.println("dcm2xml: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("dcm2xml: " + e.getMessage());
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
        dcm2json.setBulkDataNoDefaults(cl.hasOption("blk-nodefs"));
        if (cl.hasOption("blk")) {
            CLIUtils.addTagPaths(dcm2json.bulkDataDescriptor, cl.getOptionValues("blk"));
        }
        if (cl.hasOption("blk-vr")) {
            dcm2json.setBulkDataLengthsThresholdsFromStrings(cl.getOptionValues("blk-vr"));
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

    public void parse(DicomInputStream dis) throws IOException {
        dis.setIncludeBulkData(includeBulkData);
        dis.setBulkDataDescriptor(bulkDataDescriptor);
        dis.setBulkDataDirectory(blkDirectory);
        dis.setBulkDataFilePrefix(blkFilePrefix);
        dis.setBulkDataFileSuffix(blkFileSuffix);
        dis.setConcatenateBulkDataFiles(catBlkFiles);
        JsonGenerator jsonGen = createGenerator(System.out);
        JSONWriter jsonWriter = new JSONWriter(jsonGen);
        if (encodeAsNumber) {
            jsonWriter.setJsonType(VR.DS, JsonValue.ValueType.NUMBER);
            jsonWriter.setJsonType(VR.IS, JsonValue.ValueType.NUMBER);
            jsonWriter.setJsonType(VR.SV, JsonValue.ValueType.NUMBER);
            jsonWriter.setJsonType(VR.UV, JsonValue.ValueType.NUMBER);
        }
        dis.setDicomInputHandler(jsonWriter);
        dis.readDataset();
        jsonGen.flush();
    }

    private JsonGenerator createGenerator(OutputStream out) {
        Map<String, ?> conf = new HashMap<String, Object>(2);
        if (indent)
            conf.put(JsonGenerator.PRETTY_PRINTING, null);
        return Json.createGeneratorFactory(conf).createGenerator(out);
    }

}
