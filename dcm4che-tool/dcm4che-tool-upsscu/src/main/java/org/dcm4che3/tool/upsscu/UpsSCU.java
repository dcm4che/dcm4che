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
import org.dcm4che3.net.service.*;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Sep 2019
 */
public class UpsSCU {
    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.upsscu.messages");
    private static final Logger LOG = LoggerFactory.getLogger(UpsSCU.class);

    public interface RSPHandlerFactory {
        DimseRSPHandler createDimseRSPHandlerForCFind();
        DimseRSPHandler createDimseRSPHandlerForNCreate();
        DimseRSPHandler createDimseRSPHandlerForNSet();
        DimseRSPHandler createDimseRSPHandlerForNGet();
        DimseRSPHandler createDimseRSPHandlerForNAction();
    }

    private static final DicomService upsscuNEventRqHandler =
            new AbstractDicomService(UID.UnifiedProcedureStepPushSOPClass) {
                @Override
                public void onDimseRQ(Association as, PresentationContext pc,
                                      Dimse dimse, Attributes cmd, PDVInputStream data)
                        throws IOException {
                    if (dimse != Dimse.N_EVENT_REPORT_RQ)
                        throw new DicomServiceException(Status.UnrecognizedOperation);

                    int eventTypeID = cmd.getInt(Tag.EventTypeID, 0);
                    if (eventTypeID == 0 || eventTypeID > 5)
                        throw new DicomServiceException(Status.NoSuchEventType).setEventTypeID(eventTypeID);

                    try {
                        as.writeDimseRSP(pc, Commands.mkNEventReportRSP(cmd, status));
                    } catch (AssociationStateException e) {
                        LOG.warn("{} << N-EVENT-RECORD-RSP failed: {}", as, e.getMessage());
                    }
                }

                @Override
                protected void onDimseRQ(Association as, PresentationContext pc,
                                         Dimse dimse, Attributes cmd, Attributes data) {
                    throw new UnsupportedOperationException();
                }
            };

    //default response handler
    private RSPHandlerFactory rspHandlerFactory = new RSPHandlerFactory() {
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
    private int[] tags;
    private static int status;
    private String upsiuid;
    private Operation operation;
    private Attributes requestCancel;
    private Attributes changeState;
    private Attributes subscriptionAction;

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

    public final void setType(Operation operation, String[] tss) {
        this.operation = operation;
        rq.addPresentationContext(new PresentationContext(3, operation.negotiatingSOPClassUID, tss));
    }

    public final void setXmlFile(String xmlFile) {
        this.xmlFile = xmlFile;
    }

    public void setTags(int[] tags) {
        this.tags = tags;
    }

    public void setChangeState(Attributes changeState) {
        this.changeState = changeState;
    }

    public void setRequestCancel(Attributes requestCancel) {
        this.requestCancel = requestCancel;
    }

    public void setSubscriptionAction(Attributes subscriptionAction) {
        this.subscriptionAction = subscriptionAction;
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
                    Executors.newCachedThreadPool();
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newSingleThreadScheduledExecutor();
            device.setExecutor(executorService);
            device.setScheduledExecutor(scheduledExecutorService);
            device.bindConnections();
            try {
                main.open();
                main.process();
            } finally {
                main.close();
                if (main.operation != Operation.receive) {
                    executorService.shutdown();
                    scheduledExecutorService.shutdown();
                }
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
        OptionGroup changeState = new OptionGroup();
        changeState.addOption(Option.builder("P")
                .hasArg()
                .argName("transaction-uid")
                .longOpt("process")
                .desc(rb.getString("process"))
                .build());
        changeState.addOption(Option.builder("C")
                .hasArg()
                .argName("transaction-uid")
                .longOpt("complete")
                .desc(rb.getString("complete"))
                .build());
        changeState.addOption(Option.builder("D")
                .hasArg()
                .argName("transaction-uid")
                .longOpt("cancel")
                .desc(rb.getString("cancel"))
                .build());
        opts.addOptionGroup(changeState);
        opts.addOption(Option.builder()
                .hasArg()
                .longOpt("contact")
                .argName("name")
                .desc(rb.getString("contact"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .longOpt("contact-uri")
                .argName("uri")
                .desc(rb.getString("contact-uri"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("filtered-global")
                .desc(rb.getString("filtered-global"))
                .build());
        opts.addOption(Option.builder("l")
                .longOpt("lock")
                .desc(rb.getString("lock"))
                .build());
        opts.addOption(Option.builder("m")
                .hasArgs()
                .argName("[seq/]attr=value")
                .valueSeparator('=')
                .desc(rb.getString("match"))
                .build());
        opts.addOption(Option.builder("O")
                .hasArg()
                .longOpt("operation")
                .argName("name")
                .desc(rb.getString("operation"))
                .build());
        opts.addOption(Option.builder("r")
                .hasArgs()
                .argName("[seq/]attr")
                .desc(rb.getString("return"))
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
        opts.addOption(Option.builder()
                .hasArg()
                .longOpt("receiving-ae")
                .argName("aet")
                .desc(rb.getString("receiving-ae"))
                .build());
        opts.addOption(Option.builder("s")
                .hasArgs()
                .argName("[seq/]attr=value")
                .valueSeparator('=')
                .desc(rb.getString("set"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .longOpt("status")
                .argName("code")
                .desc(rb.getString("status"))
                .build());
        OptionGroup serviceClassGroup = new OptionGroup();
        serviceClassGroup.addOption(Option.builder("p")
                .longOpt("pull")
                .desc(rb.getString("pull"))
                .build());
        serviceClassGroup.addOption(Option.builder("w")
                .longOpt("watch")
                .desc(rb.getString("watch"))
                .build());
        opts.addOptionGroup(serviceClassGroup);
        opts.addOption(Option.builder("u")
                .hasArg()
                .longOpt("upsiuid")
                .argName("uid")
                .desc(rb.getString("upsiuid"))
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

    private static void configureUps(UpsSCU main, CommandLine cl) throws ParseException {
        if (!cl.getArgList().isEmpty()) {
            if (cl.getArgList().size() > 1)
                throw new IllegalArgumentException(rb.getString("too-many-xml-files"));

            main.setXmlFile(cl.getArgList().get(0));
        }
        main.setUPSIUID(cl.getOptionValue("u"));
        main.setKeys(cl.getOptionValues("s"));
        if (cl.hasOption("r"))
            main.setTags(toTags(cl.getOptionValues("r")));
        configureOperation(main, cl);
        if (main.upsiuid == null && main.operation.checkUPSIUID)
            throw new MissingOptionException(rb.getString("missing-ups-iuid"));
    }

    private static void configureOperation(UpsSCU main, CommandLine cl) throws MissingOptionException {
        Operation operation = Operation.valueOf(cl);
        String[] tss = CLIUtils.transferSyntaxesOf(cl);
        main.setType(operation, tss);
        if (operation == Operation.changeState)
            configureChangeState(main, cl);
        if (operation == Operation.requestCancel)
            configureRequestCancel(main, cl);
        if (operation == Operation.subscriptionAction)
            configureSubscribeUnsubscribe(main, cl);
        if (operation == Operation.receive)
            configureReceive(main, tss, cl);
    }

    private static void configureChangeState(UpsSCU main, CommandLine cl) throws MissingOptionException {
        if (cl.hasOption("P"))
            main.setChangeState(state(cl.getOptionValue("P"), "IN PROGRESS"));
        else if (cl.hasOption("C"))
            main.setChangeState(state(cl.getOptionValue("C"), "COMPLETED"));
        else if (cl.hasOption("D"))
            main.setChangeState(state(cl.getOptionValue("D"), "CANCELED"));
        else
            throw new MissingOptionException(rb.getString("missing-change-state"));
    }

    private static void configureRequestCancel(UpsSCU main, CommandLine cl) {
        Attributes attrs = new Attributes();
        if (cl.hasOption("reason"))
            attrs.setString(Tag.ReasonForCancellation, VR.LT, cl.getOptionValue("reason"));
        if (cl.hasOption("reason-code"))
            attrs.newSequence(Tag.ProcedureStepDiscontinuationReasonCodeSequence, 1)
                    .add(new Code(cl.getOptionValue("reason-code")).toItem());
        if (cl.hasOption("contact-uri"))
            attrs.setString(Tag.ContactURI, VR.UR, cl.getOptionValue("contact-uri"));
        if (cl.hasOption("contact"))
            attrs.setString(Tag.ContactDisplayName, VR.LO, cl.getOptionValue("contact"));
        main.setRequestCancel(attrs);
    }

    private static void configureSubscribeUnsubscribe(UpsSCU main, CommandLine cl) throws MissingOptionException {
        Attributes attrs = new Attributes();
        attrs.setString(Tag.ReceivingAE, VR.AE,
                cl.hasOption("receiving-ae") ? cl.getOptionValue("receiving-ae") : main.ae.getAETitle());
        
        if (main.operation.getActionTypeID() == 3) {
            attrs.setString(Tag.DeletionLock, VR.LO, cl.hasOption("l") ? "TRUE" : "FALSE");
            if (cl.hasOption("filtered-global")) {
                if (!cl.hasOption("m"))
                    throw new MissingOptionException(rb.getString("missing-matching-keys"));

                CLIUtils.addAttributes(attrs, cl.getOptionValues("m"));
                main.setUPSIUID(UID.UPSFilteredGlobalSubscriptionSOPInstance);
            }
        }

        if (main.upsiuid == null)
            main.setUPSIUID(UID.UPSGlobalSubscriptionSOPInstance);

        main.setSubscriptionAction(attrs);
    }

    private static void configureReceive(UpsSCU main, String[] tss, CommandLine cl) {
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        serviceRegistry.addDicomService(upsscuNEventRqHandler);
        main.ae.setDimseRQHandler(serviceRegistry);
        main.ae.addTransferCapability(
                new TransferCapability(null,
                        UID.VerificationSOPClass,
                        TransferCapability.Role.SCP,
                        UID.ImplicitVRLittleEndian));
        main.ae.addTransferCapability(
                new TransferCapability(null,
                        main.operation.negotiatingSOPClassUID,
                        TransferCapability.Role.SCU,
                        tss));
        status = CLIUtils.getIntOption(cl, "status", 0);
    }

    private static int[] toTags(String[] tagsAsStr) {
        int[] tags = new int[tagsAsStr.length];
        for (int i = 0; i < tagsAsStr.length; i++)
            tags[i] = TagUtils.forName(tagsAsStr[i]);
        return tags;
    }

    public void process() throws Exception {
        switch (operation) {
            case create:
                createUps();
                break;
            case update:
                updateUps();
                break;
            case get:
                getUps();
                break;
            case changeState:
                actionOnUps(changeState, 1);
                break;
            case requestCancel:
                actionOnUps(requestCancel, 2);
                break;
            case subscriptionAction:
                actionOnUps(subscriptionAction, operation.getActionTypeID());
                break;
        }
    }

    private void createUps() throws Exception {
        as.ncreate(operation.getNegotiatingSOPClassUID(),
                upsiuid,
                ensureSPSStartDateTime(workItem(xmlFile == null
                        ? "resource:create.xml" : xmlFile)),
                null,
                rspHandlerFactory.createDimseRSPHandlerForNCreate());
        as.waitForOutstandingRSP();
    }

    private void updateUps() throws Exception {
        as.nset(operation.getNegotiatingSOPClassUID(),
                UID.UnifiedProcedureStepPushSOPClass,
                upsiuid,
                workItem(xmlFile == null || xmlFile.equals("update") ? null : xmlFile),
                null,
                rspHandlerFactory.createDimseRSPHandlerForNSet());
    }

    private void getUps() throws IOException, InterruptedException {
        as.nget(operation.getNegotiatingSOPClassUID(),
                UID.UnifiedProcedureStepPushSOPClass,
                upsiuid,
                tags,
                rspHandlerFactory.createDimseRSPHandlerForNGet());
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
        if (!ups.containsValue(Tag.ScheduledProcedureStepStartDateTime))
            ups.setString(Tag.ScheduledProcedureStepStartDateTime, VR.DT, DateUtils.formatDT(null, new Date()));
        return ups;
    }

    private static Attributes state(String uid, String code) {
        Attributes attrs = new Attributes();
        attrs.setString(Tag.TransactionUID, VR.UI, uid);
        attrs.setString(Tag.ProcedureStepState, VR.CS, code);
        return attrs;
    }

    enum Operation {
        create(UID.UnifiedProcedureStepPushSOPClass, false),
        update(UID.UnifiedProcedureStepPullSOPClass, true),
        get(UID.UnifiedProcedureStepPushSOPClass, true),
        changeState(UID.UnifiedProcedureStepPullSOPClass, true),
        requestCancel(UID.UnifiedProcedureStepPushSOPClass, true),
        subscriptionAction(UID.UnifiedProcedureStepWatchSOPClass, false),
        receive(UID.UnifiedProcedureStepEventSOPClass, false);

        private String negotiatingSOPClassUID;
        private boolean checkUPSIUID;
        private int actionTypeID;

        Operation(String negotiatingSOPClassUID, boolean checkUPSIUID) {
            this.negotiatingSOPClassUID = negotiatingSOPClassUID;
            this.checkUPSIUID = checkUPSIUID;
        }

        String getNegotiatingSOPClassUID() {
            return negotiatingSOPClassUID;
        }

        Operation setNegotiatingSOPClassUID(String val) {
            this.negotiatingSOPClassUID = val;
            return this;
        }

        int getActionTypeID() {
            return actionTypeID;
        }

        Operation setActionTypeID(int val) {
            this.actionTypeID = val;
            return this;
        }

        static Operation valueOf(CommandLine cl) {
            switch (cl.getOptionValue("O")) {
                case "update":
                    return update;
                case "get":
                    return cl.hasOption("p")
                            ? get.setNegotiatingSOPClassUID(UID.UnifiedProcedureStepPullSOPClass)
                            : cl.hasOption("w")
                                ? get.setNegotiatingSOPClassUID(UID.UnifiedProcedureStepWatchSOPClass)
                                : get;
                case "changeState":
                    return changeState;
                case "requestCancel":
                    return cl.hasOption("w")
                            ? requestCancel.setNegotiatingSOPClassUID(UID.UnifiedProcedureStepWatchSOPClass)
                            : requestCancel;
                case "subscribe":
                    return subscriptionAction.setActionTypeID(3);
                case "unsubscribe":
                    return subscriptionAction.setActionTypeID(4);
                case "suspendGlobal":
                    return subscriptionAction.setActionTypeID(5);
                case "receive":
                    return receive;
                default:
                    return create;
            }
        }
    }

    private void actionOnUps(Attributes data, int actionTypeId) throws IOException, InterruptedException {
        as.naction(operation.negotiatingSOPClassUID,
                UID.UnifiedProcedureStepPushSOPClass,
                upsiuid,
                actionTypeId,
                data,
                null,
                rspHandlerFactory.createDimseRSPHandlerForNAction());
    }

}
