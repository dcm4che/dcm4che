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

package org.dcm4che3.tool.dcmqrscp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.media.DicomDirReader;
import org.dcm4che3.media.DicomDirWriter;
import org.dcm4che3.media.RecordFactory;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.BasicCFindSCP;
import org.dcm4che3.net.service.BasicCGetSCP;
import org.dcm4che3.net.service.BasicCMoveSCP;
import org.dcm4che3.net.service.BasicCStoreSCU;
import org.dcm4che3.net.service.BasicRetrieveTask;
import org.dcm4che3.net.service.CStoreSCU;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.net.service.InstanceLocator;
import org.dcm4che3.net.service.QueryRetrieveLevel;
import org.dcm4che3.net.service.QueryTask;
import org.dcm4che3.net.service.RetrieveTask;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.common.FilesetInfo;
import org.dcm4che3.util.AttributesFormat;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class DcmQRSCP<T extends InstanceLocator> {

    static final Logger LOG = LoggerFactory.getLogger(DcmQRSCP.class);

    private static final String[] PATIENT_ROOT_LEVELS = { "PATIENT", "STUDY",
            "SERIES", "IMAGE" };
    private static final String[] STUDY_ROOT_LEVELS = { "STUDY", "SERIES",
            "IMAGE" };
    private static final String[] PATIENT_STUDY_ONLY_LEVELS = { "PATIENT",
            "STUDY" };
    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.dcmqrscp.messages");

    private Device device = new Device("dcmqrscp");
    private ApplicationEntity ae = new ApplicationEntity("*");
    private final Connection conn = new Connection();

    private File storageDir;
    private File dicomDir;
    private AttributesFormat filePathFormat;
    private RecordFactory recFact;
    private String availability;
    private boolean stgCmtOnSameAssoc;
    private boolean sendPendingCGet;
    private int sendPendingCMoveInterval;
    private final FilesetInfo fsInfo = new FilesetInfo();
    private DicomDirReader ddReader;
    private DicomDirWriter ddWriter;
    private HashMap<String, Connection> remoteConnections = new HashMap<String, Connection>();
    
    private final class CFindSCPImpl extends BasicCFindSCP {

        private final String[] qrLevels;
        private final QueryRetrieveLevel rootLevel;

        public CFindSCPImpl(String sopClass, String... qrLevels) {
            super(sopClass);
            this.qrLevels = qrLevels;
            this.rootLevel = QueryRetrieveLevel.valueOf(qrLevels[0]);
        }

        @Override
        protected QueryTask calculateMatches(Association as,
                PresentationContext pc, Attributes rq, Attributes keys)
                throws DicomServiceException {
            QueryRetrieveLevel level = QueryRetrieveLevel.valueOf(keys,
                    qrLevels);
            level.validateQueryKeys(keys, rootLevel,
                    rootLevel == QueryRetrieveLevel.IMAGE || relational(as, rq));
            DicomDirReader ddr = getDicomDirReader();
            String availability = getInstanceAvailability();
            switch (level) {
            case PATIENT:
                return new PatientQueryTask(as, pc, rq, keys, ddr, availability);
            case STUDY:
                return new StudyQueryTask(as, pc, rq, keys, ddr, availability);
            case SERIES:
                return new SeriesQueryTask(as, pc, rq, keys, ddr, availability);
            case IMAGE:
                return new InstanceQueryTask(as, pc, rq, keys, ddr,
                        availability);
            default:
                assert true;
            }
            throw new AssertionError();
        }

        private boolean relational(Association as, Attributes rq) {
            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            ExtendedNegotiation extNeg = as.getAAssociateAC()
                    .getExtNegotiationFor(cuid);
            return QueryOption.toOptions(extNeg).contains(
                    QueryOption.RELATIONAL);
        }
    }

    private final class CGetSCPImpl extends BasicCGetSCP {

        private final String[] qrLevels;
        private final boolean withoutBulkData;
        private final QueryRetrieveLevel rootLevel;

        public CGetSCPImpl(String sopClass, String... qrLevels) {
            super(sopClass);
            this.qrLevels = qrLevels;
            this.withoutBulkData = qrLevels.length == 0;
            this.rootLevel = withoutBulkData ? QueryRetrieveLevel.IMAGE
                    : QueryRetrieveLevel.valueOf(qrLevels[0]);
        }

        @Override
        protected RetrieveTask calculateMatches(Association as,
                PresentationContext pc, Attributes rq, Attributes keys)
                throws DicomServiceException {
            QueryRetrieveLevel level = withoutBulkData ? QueryRetrieveLevel.IMAGE
                    : QueryRetrieveLevel.valueOf(keys, qrLevels);
            level.validateRetrieveKeys(keys, rootLevel, relational(as, rq));
            List<T> matches = DcmQRSCP.this
                    .calculateMatches(keys);
            if (matches.isEmpty())
                return null;

            CStoreSCU<T> storescu = new CStoreSCUImpl<T>(withoutBulkData);

            BasicRetrieveTask<T> retrieveTask = new BasicRetrieveTask<T>(
                    Dimse.C_GET_RQ, as, pc, rq, matches, as, storescu);
            retrieveTask.setSendPendingRSP(isSendPendingCGet());
            return retrieveTask;
        }

        private boolean relational(Association as, Attributes rq) {
            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            ExtendedNegotiation extNeg = as.getAAssociateAC()
                    .getExtNegotiationFor(cuid);
            return QueryOption.toOptions(extNeg).contains(
                    QueryOption.RELATIONAL);
        }

    }

    private final class CMoveSCPImpl extends BasicCMoveSCP {

        private final String[] qrLevels;
        private final QueryRetrieveLevel rootLevel;

        public CMoveSCPImpl(String sopClass, String... qrLevels) {
            super(sopClass);
            this.qrLevels = qrLevels;
            this.rootLevel = QueryRetrieveLevel.valueOf(qrLevels[0]);
        }

        @Override
        protected RetrieveTask calculateMatches(Association as,
                PresentationContext pc, final Attributes rq, Attributes keys)
                throws DicomServiceException {
            QueryRetrieveLevel level = QueryRetrieveLevel.valueOf(keys,
                    qrLevels);
            level.validateRetrieveKeys(keys, rootLevel, relational(as, rq));
            String moveDest = rq.getString(Tag.MoveDestination);
            final Connection remote = getRemoteConnection(moveDest);
            if (remote == null)
                throw new DicomServiceException(Status.MoveDestinationUnknown,
                        "Move Destination: " + moveDest + " unknown");
            List<T> matches = DcmQRSCP.this.calculateMatches(keys);
            if (matches.isEmpty())
                return null;

            AAssociateRQ aarq = makeAAssociateRQ(as.getLocalAET(), moveDest,
                    matches);
            Association storeas = openStoreAssociation(as, remote, aarq);

            BasicRetrieveTask<T> retrieveTask = new BasicRetrieveTask<T>(
                    Dimse.C_MOVE_RQ, as, pc, rq, matches, storeas,
                    new BasicCStoreSCU<T>());
            retrieveTask
                    .setSendPendingRSPInterval(getSendPendingCMoveInterval());
            return retrieveTask;
        }

        private Association openStoreAssociation(Association as,
                Connection remote, AAssociateRQ aarq)
                throws DicomServiceException {
            try {
                return as.getApplicationEntity().connect(as.getConnection(),
                        remote, aarq);
            } catch (Exception e) {
                throw new DicomServiceException(
                        Status.UnableToPerformSubOperations, e);
            }
        }

        private AAssociateRQ makeAAssociateRQ(String callingAET,
                String calledAET, List<T> matches) {
            AAssociateRQ aarq = new AAssociateRQ();
            aarq.setCalledAET(calledAET);
            aarq.setCallingAET(callingAET);
            for (InstanceLocator match : matches) {
                if (aarq.addPresentationContextFor(match.cuid, match.tsuid)) {
                    if (!UID.ExplicitVRLittleEndian.equals(match.tsuid))
                        aarq.addPresentationContextFor(match.cuid,
                                UID.ExplicitVRLittleEndian);
                    if (!UID.ImplicitVRLittleEndian.equals(match.tsuid))
                        aarq.addPresentationContextFor(match.cuid,
                                UID.ImplicitVRLittleEndian);
                }
            }
            return aarq;
        }

        private boolean relational(Association as, Attributes rq) {
            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            ExtendedNegotiation extNeg = as.getAAssociateAC()
                    .getExtNegotiationFor(cuid);
            return QueryOption.toOptions(extNeg).contains(
                    QueryOption.RELATIONAL);
        }
    }

    public DcmQRSCP() throws IOException {
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.setAssociationAcceptor(true);
        ae.addConnection(conn);
    }
    
    public void init() {
        device.setDimseRQHandler(createServiceRegistry());
    }
    
    protected void addCStoreSCPService(DicomServiceRegistry serviceRegistry ) {
        serviceRegistry.addDicomService(new CStoreSCPImpl(ddWriter, filePathFormat, recFact));
    }
    
    protected void addStgCmtSCPService(DicomServiceRegistry serviceRegistry ) {
        serviceRegistry.addDicomService(new StgCmtSCPImpl(ddReader, remoteConnections,  stgCmtOnSameAssoc, device.getExecutor()));
    }

    private DicomServiceRegistry createServiceRegistry() {
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        
        addCStoreSCPService(serviceRegistry);
        addStgCmtSCPService(serviceRegistry);
        
        serviceRegistry.addDicomService(new CFindSCPImpl(
                UID.PatientRootQueryRetrieveInformationModelFIND,
                PATIENT_ROOT_LEVELS));
        serviceRegistry.addDicomService(new CFindSCPImpl(
                UID.StudyRootQueryRetrieveInformationModelFIND,
                STUDY_ROOT_LEVELS));
        serviceRegistry.addDicomService(new CFindSCPImpl(
                UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired,
                PATIENT_STUDY_ONLY_LEVELS));
        serviceRegistry.addDicomService(new CGetSCPImpl(
                UID.PatientRootQueryRetrieveInformationModelGET,
                PATIENT_ROOT_LEVELS));
        serviceRegistry.addDicomService(new CGetSCPImpl(
                UID.StudyRootQueryRetrieveInformationModelGET,
                STUDY_ROOT_LEVELS));
        serviceRegistry.addDicomService(new CGetSCPImpl(
                UID.PatientStudyOnlyQueryRetrieveInformationModelGETRetired,
                PATIENT_STUDY_ONLY_LEVELS));
        serviceRegistry.addDicomService(new CGetSCPImpl(
                UID.CompositeInstanceRetrieveWithoutBulkDataGET));
        serviceRegistry.addDicomService(new CMoveSCPImpl(
                UID.PatientRootQueryRetrieveInformationModelMOVE,
                PATIENT_ROOT_LEVELS));
        serviceRegistry.addDicomService(new CMoveSCPImpl(
                UID.StudyRootQueryRetrieveInformationModelMOVE,
                STUDY_ROOT_LEVELS));
        serviceRegistry.addDicomService(new CMoveSCPImpl(
                UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired,
                PATIENT_STUDY_ONLY_LEVELS));
        return serviceRegistry;
    }

    public final Device getDevice() {
        return device;
    }
    
    public void setDevice(Device device) {
        this.device = device;
    }
    
    public void setApplicationEntity(ApplicationEntity ae) {
        this.ae = ae;
    }

    public final void setDicomDirectory(File dicomDir) {
        File storageDir = dicomDir.getParentFile();
        if (storageDir.mkdirs())
            System.out.println("M-WRITE " + storageDir);
        this.storageDir = storageDir;
        this.dicomDir = dicomDir;
    }

    public final File getStorageDirectory() {
        return storageDir;
    }

    public final AttributesFormat getFilePathFormat() {
        return filePathFormat;
    }

    public void setFilePathFormat(String pattern) {
        this.filePathFormat = new AttributesFormat(pattern);
    }

    public final File getDicomDirectory() {
        return dicomDir;
    }

    public boolean isWriteable() {
        return storageDir.canWrite();
    }

    public final void setInstanceAvailability(String availability) {
        this.availability = availability;
    }

    public final String getInstanceAvailability() {
        return availability;
    }

    public boolean isStgCmtOnSameAssoc() {
        return stgCmtOnSameAssoc;
    }

    public void setStgCmtOnSameAssoc(boolean stgCmtOnSameAssoc) {
        this.stgCmtOnSameAssoc = stgCmtOnSameAssoc;
    }

    public final void setSendPendingCGet(boolean sendPendingCGet) {
        this.sendPendingCGet = sendPendingCGet;
    }

    public final boolean isSendPendingCGet() {
        return sendPendingCGet;
    }

    public final void setSendPendingCMoveInterval(int sendPendingCMoveInterval) {
        this.sendPendingCMoveInterval = sendPendingCMoveInterval;
    }

    public final int getSendPendingCMoveInterval() {
        return sendPendingCMoveInterval;
    }

    public final void setRecordFactory(RecordFactory recFact) {
        this.recFact = recFact;
    }

    public final RecordFactory getRecordFactory() {
        return recFact;
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        CLIUtils.addFilesetInfoOptions(opts);
        CLIUtils.addBindServerOption(opts);
        CLIUtils.addConnectTimeoutOption(opts);
        CLIUtils.addAcceptTimeoutOption(opts);
        CLIUtils.addAEOptions(opts);
        CLIUtils.addCommonOptions(opts);
        CLIUtils.addResponseTimeoutOption(opts);
        addDicomDirOption(opts);
        addTransferCapabilityOptions(opts);
        addInstanceAvailabilityOption(opts);
        addStgCmtOptions(opts);
        addSendingPendingOptions(opts);
        addRemoteConnectionsOption(opts);
        return CLIUtils.parseComandLine(args, opts, rb, DcmQRSCP.class);
    }

    @SuppressWarnings("static-access")
    private static void addInstanceAvailabilityOption(Options opts) {
        opts.addOption(OptionBuilder.hasArg().withArgName("code")
                .withDescription(rb.getString("availability"))
                .withLongOpt("availability").create());
    }

    private static void addStgCmtOptions(Options opts) {
        opts.addOption(null, "stgcmt-same-assoc", false,
                rb.getString("stgcmt-same-assoc"));
    }

    @SuppressWarnings("static-access")
    private static void addSendingPendingOptions(Options opts) {
        opts.addOption(null, "pending-cget", false,
                rb.getString("pending-cget"));
        opts.addOption(OptionBuilder.hasArg().withArgName("s")
                .withDescription(rb.getString("pending-cmove"))
                .withLongOpt("pending-cmove").create());
    }

    @SuppressWarnings("static-access")
    private static void addDicomDirOption(Options opts) {
        opts.addOption(OptionBuilder.hasArg().withArgName("file")
                .withDescription(rb.getString("dicomdir"))
                .withLongOpt("dicomdir").create());
        opts.addOption(OptionBuilder.hasArg().withArgName("pattern")
                .withDescription(rb.getString("filepath"))
                .withLongOpt("filepath").create(null));
    }

    @SuppressWarnings("static-access")
    private static void addTransferCapabilityOptions(Options opts) {
        opts.addOption(null, "all-storage", false, rb.getString("all-storage"));
        opts.addOption(null, "no-storage", false, rb.getString("no-storage"));
        opts.addOption(null, "no-query", false, rb.getString("no-query"));
        opts.addOption(null, "no-retrieve", false, rb.getString("no-retrieve"));
        opts.addOption(null, "relational", false, rb.getString("relational"));
        opts.addOption(OptionBuilder.hasArg().withArgName("file|url")
                .withDescription(rb.getString("storage-sop-classes"))
                .withLongOpt("storage-sop-classes").create());
        opts.addOption(OptionBuilder.hasArg().withArgName("file|url")
                .withDescription(rb.getString("query-sop-classes"))
                .withLongOpt("query-sop-classes").create());
        opts.addOption(OptionBuilder.hasArg().withArgName("file|url")
                .withDescription(rb.getString("retrieve-sop-classes"))
                .withLongOpt("retrieve-sop-classes").create());
    }

    @SuppressWarnings("static-access")
    private static void addRemoteConnectionsOption(Options opts) {
        opts.addOption(OptionBuilder.hasArg().withArgName("file|url")
                .withDescription(rb.getString("ae-config"))
                .withLongOpt("ae-config").create());
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            DcmQRSCP<InstanceLocator> main = new DcmQRSCP<InstanceLocator>();
            CLIUtils.configure(main.fsInfo, cl);
            CLIUtils.configureBindServer(main.conn, main.ae, cl);
            CLIUtils.configure(main.conn, cl);
            configureDicomFileSet(main, cl);
            configureTransferCapability(main, cl);
            configureInstanceAvailability(main, cl);
            configureStgCmt(main, cl);
            configureSendPending(main, cl);
            configureRemoteConnections(main, cl);
            ExecutorService executorService = Executors.newCachedThreadPool();
            ScheduledExecutorService scheduledExecutorService = Executors
                    .newSingleThreadScheduledExecutor();
            main.device.setScheduledExecutor(scheduledExecutorService);
            main.device.setExecutor(executorService);
            main.device.bindConnections();
            main.init();
        } catch (ParseException e) {
            System.err.println("dcmqrscp: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("dcmqrscp: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void configureDicomFileSet(DcmQRSCP<InstanceLocator> main, CommandLine cl)
            throws ParseException {
        if (!cl.hasOption("dicomdir"))
            throw new MissingOptionException(rb.getString("missing-dicomdir"));
        main.setDicomDirectory(new File(cl.getOptionValue("dicomdir")));
        main.setFilePathFormat(cl.getOptionValue("filepath",
                "DICOM/{0020000D,hash}/{0020000E,hash}/{00080018,hash}"));
        main.setRecordFactory(new RecordFactory());
    }

    private static void configureInstanceAvailability(DcmQRSCP<InstanceLocator> main,
            CommandLine cl) {
        main.setInstanceAvailability(cl.getOptionValue("availability"));
    }

    private static void configureStgCmt(DcmQRSCP<InstanceLocator> main, CommandLine cl) {
        main.setStgCmtOnSameAssoc(cl.hasOption("stgcmt-same-assoc"));
    }

    private static void configureSendPending(DcmQRSCP<InstanceLocator> main, CommandLine cl) {
        main.setSendPendingCGet(cl.hasOption("pending-cget"));
        if (cl.hasOption("pending-cmove"))
            main.setSendPendingCMoveInterval(Integer.parseInt(cl
                    .getOptionValue("pending-cmove")));
    }

    private static void configureTransferCapability(DcmQRSCP<InstanceLocator> main,
            CommandLine cl) throws IOException {
        ApplicationEntity ae = main.ae;
        EnumSet<QueryOption> queryOptions = cl.hasOption("relational") ? EnumSet
                .of(QueryOption.RELATIONAL) : EnumSet.noneOf(QueryOption.class);
        boolean storage = !cl.hasOption("no-storage") && main.isWriteable();
        if (storage && cl.hasOption("all-storage")) {
            TransferCapability tc = new TransferCapability(null, "*",
                    TransferCapability.Role.SCP, "*");
            tc.setQueryOptions(queryOptions);
            ae.addTransferCapability(tc);
        } else {
            ae.addTransferCapability(new TransferCapability(null,
                    UID.VerificationSOPClass, TransferCapability.Role.SCP,
                    UID.ImplicitVRLittleEndian));
            Properties storageSOPClasses = CLIUtils.loadProperties(cl
                    .getOptionValue("storage-sop-classes",
                            "resource:storage-sop-classes.properties"), null);
            if (storage)
                addTransferCapabilities(ae, storageSOPClasses,
                        TransferCapability.Role.SCP, null);
            if (!cl.hasOption("no-retrieve")) {
                addTransferCapabilities(ae, storageSOPClasses,
                        TransferCapability.Role.SCU, null);
                Properties p = CLIUtils.loadProperties(cl.getOptionValue(
                        "retrieve-sop-classes",
                        "resource:retrieve-sop-classes.properties"), null);
                addTransferCapabilities(ae, p, TransferCapability.Role.SCP,
                        queryOptions);
            }
            if (!cl.hasOption("no-query")) {
                Properties p = CLIUtils.loadProperties(cl.getOptionValue(
                        "query-sop-classes",
                        "resource:query-sop-classes.properties"), null);
                addTransferCapabilities(ae, p, TransferCapability.Role.SCP,
                        queryOptions);
            }
        }
        if (storage)
            main.openDicomDir();
        else
            main.openDicomDirForReadOnly();
    }

    private static void addTransferCapabilities(ApplicationEntity ae,
            Properties p, TransferCapability.Role role,
            EnumSet<QueryOption> queryOptions) {
        for (String cuid : p.stringPropertyNames()) {
            String ts = p.getProperty(cuid);
            TransferCapability tc = new TransferCapability(null,
                    CLIUtils.toUID(cuid), role, CLIUtils.toUIDs(ts));
            tc.setQueryOptions(queryOptions);
            ae.addTransferCapability(tc);
        }
    }

    private static void configureRemoteConnections(DcmQRSCP<InstanceLocator> main, CommandLine cl)
            throws Exception {
        String file = cl.getOptionValue("ae-config", "resource:ae.properties");
        Properties aeConfig = CLIUtils.loadProperties(file, null);
        for (Map.Entry<Object, Object> entry : aeConfig.entrySet()) {
            String aet = (String) entry.getKey();
            String value = (String) entry.getValue();
            try {
                String[] hostPortCiphers = StringUtils.split(value, ':');
                String[] ciphers = new String[hostPortCiphers.length - 2];
                System.arraycopy(hostPortCiphers, 2, ciphers, 0, ciphers.length);
                Connection remote = new Connection();
                remote.setHostname(hostPortCiphers[0]);
                remote.setPort(Integer.parseInt(hostPortCiphers[1]));
                remote.setTlsCipherSuites(ciphers);
                main.addRemoteConnection(aet, remote);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid entry in " + file
                        + ": " + aet + "=" + value);
            }
        }
    }

    final DicomDirReader getDicomDirReader() {
        return ddReader;
    }
    
    public void setDicomDirReader(DicomDirReader ddReader) {
        this.ddReader = ddReader;
    }

    final DicomDirWriter getDicomDirWriter() {
        return ddWriter;
    }

    private void openDicomDir() throws IOException {
        if (!dicomDir.exists())
            DicomDirWriter.createEmptyDirectory(dicomDir,
                    UIDUtils.createUIDIfNull(fsInfo.getFilesetUID()),
                    fsInfo.getFilesetID(), fsInfo.getDescriptorFile(),
                    fsInfo.getDescriptorFileCharset());
        ddReader = ddWriter = DicomDirWriter.open(dicomDir);
    }

    private void openDicomDirForReadOnly() throws IOException {
        ddReader = new DicomDirReader(dicomDir);
    }

    public void addRemoteConnection(String aet, Connection remote) {
        remoteConnections.put(aet, remote);
    }

    Connection getRemoteConnection(String dest) {
        return remoteConnections.get(dest);
    }

    public List<T> calculateMatches(Attributes keys)
            throws DicomServiceException {
        try {
            List<T> list = new ArrayList<T>();
            String[] patIDs = keys.getStrings(Tag.PatientID);
            String[] studyIUIDs = keys.getStrings(Tag.StudyInstanceUID);
            String[] seriesIUIDs = keys.getStrings(Tag.SeriesInstanceUID);
            String[] sopIUIDs = keys.getStrings(Tag.SOPInstanceUID);
            DicomDirReader ddr = ddReader;
            Attributes patRec = ddr.findPatientRecord(patIDs);
            while (patRec != null) {
                Attributes studyRec = ddr.findStudyRecord(patRec, studyIUIDs);
                while (studyRec != null) {
                    Attributes seriesRec = ddr.findSeriesRecord(studyRec,
                            seriesIUIDs);
                    while (seriesRec != null) {
                        Attributes instRec = ddr.findLowerInstanceRecord(
                                seriesRec, true, sopIUIDs);
                        while (instRec != null) {
                            String cuid = instRec
                                    .getString(Tag.ReferencedSOPClassUIDInFile);
                            String iuid = instRec
                                    .getString(Tag.ReferencedSOPInstanceUIDInFile);
                            String tsuid = instRec
                                    .getString(Tag.ReferencedTransferSyntaxUIDInFile);
                            String[] fileIDs = instRec
                                    .getStrings(Tag.ReferencedFileID);
                            String uri = ddr.toFile(fileIDs).toURI().toString();
                            list.add((T)new InstanceLocator(cuid, iuid, tsuid, uri));
                            if (sopIUIDs != null && sopIUIDs.length == 1)
                                break;

                            instRec = ddr.findNextInstanceRecord(instRec, true,
                                    sopIUIDs);
                        }
                        if (seriesIUIDs != null && seriesIUIDs.length == 1)
                            break;

                        seriesRec = ddr.findNextSeriesRecord(seriesRec,
                                seriesIUIDs);
                    }
                    if (studyIUIDs != null && studyIUIDs.length == 1)
                        break;

                    studyRec = ddr.findNextStudyRecord(studyRec, studyIUIDs);
                }
                if (patIDs != null && patIDs.length == 1)
                    break;

                patRec = ddr.findNextPatientRecord(patRec, patIDs);
            }
            return list;
        } catch (IOException e) {
            throw new DicomServiceException(
                    Status.UnableToCalculateNumberOfMatches, e);
        }
    }
    
    public Connection getConnection() {
        return conn;
    }
    
    public ApplicationEntity getApplicationEntity() {
        return ae;
    }

}
