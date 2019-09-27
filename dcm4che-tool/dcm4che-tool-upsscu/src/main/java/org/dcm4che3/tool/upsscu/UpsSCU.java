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

import java.io.*;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
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
import org.dcm4che3.util.TagUtils;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Sep 2019
 */
public class UpsSCU {
    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.upsscu.messages");

    public interface RSPHandlerFactory {
        DimseRSPHandler createDimseRSPHandlerForCFind();
        DimseRSPHandler createDimseRSPHandlerForNCreate();
        DimseRSPHandler createDimseRSPHandlerForNSet();
        DimseRSPHandler createDimseRSPHandlerForNGet();
        DimseRSPHandler createDimseRSPHandlerForNAction();
        DimseRSPHandler createDimseRSPHandlerForNEvent();
    }

    //default response handler
    private RSPHandlerFactory rspHandlerFactory = new RSPHandlerFactory(){
        @Override
        public DimseRSPHandler createDimseRSPHandlerForCFind() {
            return new DimseRSPHandler(as.nextMessageID()) {
                @Override
                public void onDimseRSP(Association as, Attributes cmd,
                                       Attributes data) {
                    //TODO
                }
            };
        }

        @Override
        public DimseRSPHandler createDimseRSPHandlerForNCreate() {
            return new DimseRSPHandler(as.nextMessageID()) {
                @Override
                public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                    super.onDimseRSP(as, cmd, data);
                }
            };
        }

        @Override
        public DimseRSPHandler createDimseRSPHandlerForNSet() {
            return new DimseRSPHandler(as.nextMessageID()){
                @Override
                public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                    super.onDimseRSP(as, cmd, data);
                }
            };
        };

        @Override
        public DimseRSPHandler createDimseRSPHandlerForNGet() {
            return new DimseRSPHandler(as.nextMessageID()) {
                @Override
                public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                    super.onDimseRSP(as, cmd, data);
                }
            };
        }

        @Override
        public DimseRSPHandler createDimseRSPHandlerForNAction() {
            return new DimseRSPHandler(as.nextMessageID()) {
                @Override
                public void onDimseRSP(Association as, Attributes cmd,
                                       Attributes data) {
                    //TODO
                }
            };
        };

        @Override
        public DimseRSPHandler createDimseRSPHandlerForNEvent() {
            return new DimseRSPHandler(as.nextMessageID()) {
                @Override
                public void onDimseRSP(Association as, Attributes cmd,
                                       Attributes data) {
                    //TODO
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
    private int[] tags;
    private String upsiuid;
    private State state;
    private CommandType commandType = CommandType.Find;

    public UpsSCU( ApplicationEntity ae) {
        this.remote = new Connection();
        this.ae = ae;
    }

    public void addVerificationPresentationContext() {
        rq.addPresentationContext(
                new PresentationContext(1, UID.VerificationSOPClass,
                        UID.ImplicitVRLittleEndian));
    }

    public final void setUPSIUID(String upsiuid) {
        this.upsiuid = upsiuid;
    }

    public final void setKeys(String[] keys) {
        this.keys = keys;
    }

    public void setState(State state, String transactionUID) {
        this.state = state.setUid(transactionUID);
    }

    public final void setType(CommandType commandType, String[] tss) {
        this.commandType = commandType;
        rq.addPresentationContext(new PresentationContext(3, commandType.negotiatingSOPClassUID, tss));
    }

    public final void setXmlFile(String xmlFile) {
        this.xmlFile = xmlFile;
    }

    public void setTags(int[] tags) {
        this.tags = tags;
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
            main.addVerificationPresentationContext();
            ExecutorService executorService =
                    Executors.newSingleThreadExecutor();
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newSingleThreadScheduledExecutor();
            device.setExecutor(executorService);
            device.setScheduledExecutor(scheduledExecutorService);
            try {
                main.open();
                main.process();
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
        opts.addOption(Option.builder("C")
                .hasArg()
                .longOpt("command")
                .desc(rb.getString("command"))
                .build());
        opts.addOption(Option.builder("S")
                .hasArg()
                .longOpt("state")
                .desc(rb.getString("state"))
                .build());
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
                .hasArg()
                .longOpt("reason")
                .argName("reason")
                .desc(rb.getString("reason"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .longOpt("truid")
                .argName("uid")
                .desc(rb.getString("truid"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .longOpt("reason-code")
                .argName("code")
                .desc(rb.getString("reason-code"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .longOpt("upsiuid")
                .argName("uid")
                .desc(rb.getString("upsiuid"))
                .build());
        opts.addOption(Option.builder("r")
                .hasArgs()
                .argName("[seq/]attr")
                .desc(rb.getString("return"))
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

    public void process() throws Exception {
        switch (commandType) {
            case Create:
                create();
                break;
            case Update:
                updateUps();
                break;
            case Get:
            case GetPull:
            case GetWatch:
                get();
                break;
            case Find:
            case FindWatch:
                find();
                break;
            case ChangeState:
                changeState();
                break;
            case RequestCancel:
            case RequestCancelWatch:
                requestCancel();
                break;
            case Subscribe:
            case Unsubscribe:
                subscribeUnsubscribe();
                break;
            case Suspend:
                suspend();
                break;
            case Receive:
                receive();
                break;
        }
    }

    private void create() throws Exception {
        createUps(ensureSPSStartDateTime(workItem(xmlFile == null
                ? "resource:create.xml" : xmlFile)));
        as.waitForOutstandingRSP();
    }

    private void createUps(final Attributes workitems) throws IOException, InterruptedException {
        as.ncreate(UID.UnifiedProcedureStepPushSOPClass,
                upsiuid,
                workitems,
                null,
                rspHandlerFactory.createDimseRSPHandlerForNCreate());
    }

    private void updateUps() throws Exception {
        as.nset(UID.UnifiedProcedureStepPullSOPClass,
                UID.UnifiedProcedureStepPushSOPClass,
                upsiuid,
                workItem(xmlFile == null || xmlFile.equals("update") ? null : xmlFile),
                null,
                rspHandlerFactory.createDimseRSPHandlerForNSet());
    }

    private void get() throws IOException, InterruptedException {
        as.nget(commandType.getNegotiatingSOPClassUID(),
                UID.UnifiedProcedureStepPushSOPClass,
                upsiuid,
                tags,
                rspHandlerFactory.createDimseRSPHandlerForNGet());
    }

    private static void configureUps(UpsSCU main, CommandLine cl) throws ParseException {
        if (!cl.getArgList().isEmpty()) {
            if (cl.getArgList().size() > 1)
                throw new IllegalArgumentException(rb.getString("too-many-xml-files"));

            main.setXmlFile(cl.getArgList().get(0));
        }
        main.setUPSIUID(cl.getOptionValue("upsiuid"));
        main.setKeys(cl.getOptionValues("s"));
        if (cl.hasOption("r"))
            main.setTags(toTags(cl.getOptionValues("r")));
        configureCommand(main, cl);
        configureStateChange(main, cl);
        if (main.upsiuid == null && (main.commandType.name().startsWith("Get") || main.commandType == CommandType.Update))
            throw new MissingOptionException(rb.getString("missing-ups-iuid"));
    }
    
    private static void configureCommand(UpsSCU main, CommandLine cl) throws ParseException {
        main.setType(CommandType.valueOf(cl), CLIUtils.transferSyntaxesOf(cl));
    }

    private static void configureStateChange(UpsSCU main, CommandLine cl) throws ParseException {
        if (main.commandType != CommandType.ChangeState)
            return;

        if (!cl.hasOption("S"))
            throw new MissingOptionException(rb.getString("missing-state"));

        if (!cl.hasOption("truid"))
            throw new MissingOptionException(rb.getString("missing-tr-uid"));

        main.setState(stateOf(cl), cl.getOptionValue("transaction-uid"));
    }

    private static State stateOf(CommandLine cl) throws ParseException {
        try {
            return State.valueOf(cl.getOptionValue("S").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ParseException(
                    MessageFormat.format(
                        rb.getString("invalid-change-state"),
                        cl.getOptionValue("S")));
        }
    }

    private static int[] toTags(String[] tagsAsStr) {
        int[] tags = new int[tagsAsStr.length];
        for (int i = 0; i < tagsAsStr.length; i++)
            tags[i] = TagUtils.forName(tagsAsStr[i]);
        return tags;
    }

    private Attributes workItem(String xmlFile) throws Exception {
        Attributes attrs = new Attributes();
        if (xmlFile != null)
            try (InputStream is = StreamUtils.openFileOrURL(xmlFile)) {
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

    private Attributes state(State state) {
        Attributes attrs = new Attributes();
        attrs.setString(Tag.TransactionUID, VR.UI, state.getUid());
        attrs.setString(Tag.ProcedureStepState, VR.CS, state.getCode());
        return attrs;
    }

    enum CommandType {
        Create(UID.UnifiedProcedureStepPushSOPClass),
        Update(UID.UnifiedProcedureStepPullSOPClass),
        Find(UID.UnifiedProcedureStepPullSOPClass),
        FindWatch(UID.UnifiedProcedureStepWatchSOPClass),
        Get(UID.UnifiedProcedureStepPushSOPClass),
        GetPull(UID.UnifiedProcedureStepPullSOPClass),
        GetWatch(UID.UnifiedProcedureStepWatchSOPClass),
        ChangeState(UID.UnifiedProcedureStepPullSOPClass),
        RequestCancel(UID.UnifiedProcedureStepPushSOPClass),
        RequestCancelWatch(UID.UnifiedProcedureStepWatchSOPClass),
        Subscribe(UID.UnifiedProcedureStepWatchSOPClass),
        Unsubscribe(UID.UnifiedProcedureStepWatchSOPClass),
        Suspend(UID.UnifiedProcedureStepWatchSOPClass),
        Receive(UID.UnifiedProcedureStepEventSOPClass);

        private String negotiatingSOPClassUID;

        CommandType(String negotiatingSOPClassUID) {
            this.negotiatingSOPClassUID = negotiatingSOPClassUID;
        }

        String getNegotiatingSOPClassUID() {
            return negotiatingSOPClassUID;
        }

        static CommandType valueOf(CommandLine cl) throws ParseException {
            try {
                return cl.hasOption("C")
                        ? CommandType.valueOf(cl.getOptionValue("C"))
                        : CommandType.Find;
            } catch (IllegalArgumentException e) {
                throw new ParseException(
                        MessageFormat.format(
                                rb.getString("invalid-command-name"),
                                cl.getOptionValue("C")));
            }
        }
    }

    enum State {
        COMPLETE("COMPLETED"),
        PROCESS("IN PROGRESS"),
        DISCONTINUE("CANCELED");

        private String code;
        private String uid;

        State(String code) {
            this.code = code;
        }

        String getCode() {
            return code;
        }

        String getUid() {
            return uid;
        }

        State setUid(String val) {
            uid = val;
            return this;
        }
    }

    private void find() {
        //TODO
    }

    private void changeState() {
        //TODO
    }

    private void requestCancel() {
        //TODO
    }

    private void subscribeUnsubscribe() {
        //TODO
    }

    private void suspend() {
        //TODO
    }

    private void receive() {
        //TODO
    }

}
