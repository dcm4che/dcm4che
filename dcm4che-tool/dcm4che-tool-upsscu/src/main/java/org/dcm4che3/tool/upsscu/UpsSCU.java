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

package org.dcm4che3.tool.upsscu;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.cli.*;
import org.dcm4che3.data.*;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.StreamUtils;

public class UpsSCU {
    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.upsscu.messages");

    public interface RSPHandlerFactory {
        DimseRSPHandler createDimseRSPHandlerForNCreate();
    }

    //default response handler
    private RSPHandlerFactory rspHandlerFactory = new RSPHandlerFactory(){
        @Override
        public DimseRSPHandler createDimseRSPHandlerForNCreate() {
            return new DimseRSPHandler(as.nextMessageID()) {
                @Override
                public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                    super.onDimseRSP(as, cmd, data);
                }
            };
        }
    };

    private final ApplicationEntity ae;
    private final Connection remote;
    private final AAssociateRQ rq = new AAssociateRQ();

    private Association as;
    private String xmlFile;
    private String[] keys;
    private String upsuid;

    public UpsSCU( ApplicationEntity ae) {
        this.remote = new Connection();
        this.ae = ae;
    }

    public void setTransferSyntaxes(String[] tss) {
        rq.addPresentationContext(
                new PresentationContext(1, UID.VerificationSOPClass,
                        UID.ImplicitVRLittleEndian));
        rq.addPresentationContext(
                new PresentationContext(3,
                        UID.UnifiedProcedureStepPushSOPClass,
                        tss));
    }

    public final void setUPSUID(String upsuid) {
        this.upsuid = upsuid;
    }

    public final void setKeys(String[] keys) {
        this.keys = keys;
    }

    public final void setXmlFile(String xmlFile) {
        this.xmlFile = xmlFile;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Device device = new Device("upsscu");
            Connection conn = new Connection();
            device.addConnection(conn);
            ApplicationEntity ae = new ApplicationEntity("UPSSCU");
            device.addApplicationEntity(ae);
            ae.addConnection(conn);
            final UpsSCU main = new UpsSCU(ae);
            configureUps(main, cl);
            CLIUtils.configureConnect(main.remote, main.rq, cl);
            CLIUtils.configureBind(conn, main.ae, cl);
            CLIUtils.configure(conn, cl);
            main.remote.setTlsProtocols(conn.getTlsProtocols());
            main.remote.setTlsCipherSuites(conn.getTlsCipherSuites());
            main.setTransferSyntaxes(CLIUtils.transferSyntaxesOf(cl));
            ExecutorService executorService =
                    Executors.newSingleThreadExecutor();
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newSingleThreadScheduledExecutor();
            device.setExecutor(executorService);
            device.setScheduledExecutor(scheduledExecutorService);
            try {
                main.open();
                main.createUps();
            } finally {
                main.close();
                executorService.shutdown();
                scheduledExecutorService.shutdown();
            }
        } catch (ParseException e) {
            System.err.println("upsscu: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("upsscu: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        CLIUtils.addTransferSyntaxOptions(opts);
        CLIUtils.addConnectOption(opts);
        CLIUtils.addBindOption(opts, "UPSSCU");
        CLIUtils.addAEOptions(opts);
        CLIUtils.addResponseTimeoutOption(opts);
        CLIUtils.addCommonOptions(opts);
        addUPSOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, UpsSCU.class);
    }

    private static void addUPSOptions(Options opts) {
        opts.addOption(Option.builder("s")
                .hasArgs()
                .argName("[seq/]attr=value")
                .valueSeparator('=')
                .desc(rb.getString("set"))
                .build());
        opts.addOption(Option.builder()
                .hasArgs()
                .longOpt("contact")
                .argName("name")
                .desc(rb.getString("contact"))
                .build());
        opts.addOption(Option.builder()
                .hasArgs()
                .longOpt("code")
                .argName("[seq/]attr=Code")
                .valueSeparator('=')
                .desc(rb.getString("code"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .longOpt("reason")
                .argName("reason")
                .desc(rb.getString("reason"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .longOpt("reason-code")
                .argName("code")
                .desc(rb.getString("reason-code"))
                .build());
        opts.addOption(Option.builder("C")
                .hasArg()
                .longOpt("complete")
                .argName("transaction-uid")
                .desc(rb.getString("complete"))
                .build());
        opts.addOption(Option.builder("D")
                .hasArg()
                .longOpt("cancel")
                .argName("transaction-uid")
                .desc(rb.getString("cancel"))
                .build());
        opts.addOption(Option.builder("P")
                .hasArg()
                .longOpt("process")
                .argName("transaction-uid")
                .desc(rb.getString("process"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .longOpt("upsuid")
                .argName("uid")
                .desc(rb.getString("upsuid"))
                .build());
    }

    public void open() throws IOException, InterruptedException,
            IncompatibleConnectionException, GeneralSecurityException {
        as = ae.connect(remote, rq);
    }

    public void close() throws IOException, InterruptedException {
        if (as != null) {
            as.waitForOutstandingRSP();
            as.release();
            as.waitForSocketClose();
        }
    }

    public void createUps() throws Exception {
        createUps(ensureSPSStartDateTime(workItem()));
        as.waitForOutstandingRSP();
    }

    private void createUps(final Attributes workitems) throws IOException, InterruptedException {
        as.ncreate(UID.UnifiedProcedureStepPushSOPClass,
                upsuid, workitems, null, rspHandlerFactory.createDimseRSPHandlerForNCreate());
    }

    private static void configureUps(UpsSCU main, CommandLine cl) throws Exception {
        main.setXmlFile(cl.getArgList().get(0));
        main.setKeys(cl.getOptionValues("s"));
        main.setUPSUID(cl.getOptionValue("upsuid"));
    }

    private Attributes workItem() throws Exception {
        Attributes attrs = new Attributes();
        if (xmlFile != null)
            try (InputStream is = StreamUtils.openFileOrURL(xmlFile.equals("create")
                    ? "resource:create.xml" : xmlFile)) {
                SAXReader.parse(is, attrs);
            }
        CLIUtils.addAttributes(attrs, keys);
        return attrs;
    }

    private Attributes ensureSPSStartDateTime(Attributes ups) {
        Date spsStartDateTime = ups.getDate(Tag.ScheduledProcedureStepStartDateTime);
        if (spsStartDateTime == null)
            ups.setString(Tag.ScheduledProcedureStepStartDateTime, VR.DT, DateUtils.formatDT(null, new Date()));
        return ups;
    }

}
