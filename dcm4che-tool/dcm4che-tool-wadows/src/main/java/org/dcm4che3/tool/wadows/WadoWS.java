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
 *  Portions created by the Initial Developer are Copyright (C) 2015-2018
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

package org.dcm4che3.tool.wadows;

import org.apache.commons.cli.*;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.xdsi.RetrieveImagingDocumentSetRequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since June 2018
 */

public class WadoWS {
    private static final Logger LOG = LoggerFactory.getLogger(WadoWS.class);
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.wadows.messages");
    private String url;
    private String user;
    private String accept;
    private Attributes kosAttr;
    private String[] tsuids;
    private String[] contentTypes;
    private String study;
    private static File outDir;
    private static int count;

    public WadoWS() {}

    public static void main(String[] args) {
        try {
            WadoWS wadoWS = new WadoWS();
            init(parseComandLine(args), wadoWS);
            wadoWS.wado();
        } catch (ParseException e) {
            System.err.println("wadows: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("wadows: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    public final void setOutputDirectory(File dir) {
        dir.mkdirs();
        outDir = dir;
    }

    public final void setKOSAttr(String path) throws IOException {
        if (path == null)
            return;

        this.kosAttr = new DicomInputStream(new FileInputStream(new File(path)))
                .readDataset(-1, -1);
    }
    
    public final void setTSUIDs(String[] tsuids) {
        this.tsuids = tsuids;
    }

    public final void setContentTypes(String[] contentTypes) {
        this.contentTypes = contentTypes;
    }

    public final void setStudy(String study) {
        this.study = study;
    }

    public final void setUser(String user) {
        this.user = user;
    }

    public final void setURL(String url) {
        this.url = url;
    }

    public final void setAcceptType(String accept) {
        this.accept = accept;
    }

    private static CommandLine parseComandLine(String[] args) throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        opts.addOption(Option.builder("a")
                .longOpt("accept")
                .hasArg(true)
                .desc(rb.getString("accept"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("out-dir")
                .hasArg()
                .argName("directory")
                .desc(rb.getString("out-dir"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("url")
                .longOpt("url")
                .desc(rb.getString("url"))
                .build());
        opts.addOption(Option.builder("u")
                .hasArg()
                .argName("user:password")
                .longOpt("user")
                .desc(rb.getString("user"))
                .build());
        opts.addOption(Option.builder("f")
                .hasArg()
                .argName("file")
                .longOpt("file")
                .desc(rb.getString("file"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("study")
                .longOpt("study")
                .desc(rb.getString("study"))
                .build());
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder()
                .longOpt("tsuid")
                .hasArgs()
                .desc(rb.getString("tsuid"))
                .build());
        group.addOption(Option.builder("t")
                .longOpt("contentType")
                .hasArgs()
                .desc(rb.getString("contentType"))
                .build());
        opts.addOptionGroup(group);
        return CLIUtils.parseComandLine(args, opts, rb, WadoWS.class);
    }

    private static void init(CommandLine cl, WadoWS wadoWS) throws Exception {
        wadoWS.setURL(cl.getOptionValue("url"));
        if (wadoWS.url == null)
            throw new MissingOptionException("Missing url.");
        wadoWS.setUser(cl.getOptionValue("u"));
        wadoWS.setAcceptType(cl.getOptionValue("a"));
        if (cl.hasOption("out-dir"))
            wadoWS.setOutputDirectory(new File(cl.getOptionValue("out-dir")));
        wadoWS.setKOSAttr(cl.getOptionValue("f"));
        wadoWS.setTSUIDs(cl.getOptionValues("tsuids"));
        wadoWS.setContentTypes(cl.getOptionValues("t"));
        wadoWS.setStudy(cl.getOptionValue("study"));
        cl.getOptionValues("t");
    }

    private void wado() {
        RetrieveImagingDocumentSetRequestType.StudyRequest req = new RetrieveImagingDocumentSetRequestType.StudyRequest();
        req.setStudyInstanceUID("");
        req.getSeriesRequest().add(createSeriesReq());
    }

    private RetrieveImagingDocumentSetRequestType.StudyRequest.SeriesRequest createSeriesReq() {
        RetrieveImagingDocumentSetRequestType.StudyRequest.SeriesRequest req = new RetrieveImagingDocumentSetRequestType.StudyRequest.SeriesRequest();
        req.setSeriesInstanceUID("");
        return req;
    }
}
