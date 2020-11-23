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

package org.dcm4che3.tool.dcmqrscp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.media.DicomDirReader;
import org.dcm4che3.media.DicomDirWriter;
import org.dcm4che3.media.RecordFactory;
import org.dcm4che3.media.RecordType;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.AssociationStateException;
import org.dcm4che3.net.Commands;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.*;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.common.FilesetInfo;
import org.dcm4che3.util.AttributesFormat;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class DcmQRSCP {

    static final Logger LOG = LoggerFactory.getLogger(DcmQRSCP.class);

    private static final EnumSet<QueryRetrieveLevel2> PATIENT_ROOT_LEVELS = EnumSet.of(
            QueryRetrieveLevel2.PATIENT,
            QueryRetrieveLevel2.STUDY,
            QueryRetrieveLevel2.SERIES,
            QueryRetrieveLevel2.IMAGE);
    private static final EnumSet<QueryRetrieveLevel2> STUDY_ROOT_LEVELS = EnumSet.of(
            QueryRetrieveLevel2.STUDY,
            QueryRetrieveLevel2.SERIES,
            QueryRetrieveLevel2.IMAGE);
    private static final EnumSet<QueryRetrieveLevel2> PATIENT_STUDY_ONLY_LEVELS = EnumSet.of(
            QueryRetrieveLevel2.PATIENT,
            QueryRetrieveLevel2.STUDY);
    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.dcmqrscp.messages");

    private final Device device = new Device("dcmqrscp");
    private final ApplicationEntity ae = new ApplicationEntity("*");
    private final Connection conn = new Connection();

    private File storageDir;
    private File dicomDir;
    private AttributesFormat filePathFormat;
    private RecordFactory recFact;
    private String availability;
    private boolean relationalLenient;
    private boolean stgCmtOnSameAssoc;
    private boolean sendPendingCGet;
    private int sendPendingCMoveInterval;
    private int delayCFind;
    private int delayCStore;
    private int errorCFind;
    private int errorCMove;
    private int errorCGet;
    private boolean ignoreCaseOfPN;
    private boolean matchNoValue;
    private final FilesetInfo fsInfo = new FilesetInfo();
    private DicomDirReader ddReader;
    private DicomDirWriter ddWriter;
    private HashMap<String, Connection> remoteConnections = new HashMap<String, Connection>();

    private final class CStoreSCPImpl extends BasicCStoreSCP {

        CStoreSCPImpl() {
            super("*");
        }

        @Override
        protected void store(Association as, PresentationContext pc,
                Attributes rq, PDVInputStream data, Attributes rsp)
                throws IOException {
            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
            String tsuid = pc.getTransferSyntax();
            File file = new File(storageDir, iuid);
            try {
                Attributes fmi = as.createFileMetaInformation(iuid, cuid, tsuid);
                storeTo(as, fmi, data, file);
                Attributes attrs = parse(file);
                File dest = getDestinationFile(attrs);
                renameTo(as, file, dest);
                file = dest;
                if (addDicomDirRecords(as, attrs, fmi, file)) {
                    LOG.info("{}: M-UPDATE {}", as, dicomDir);
                } else {
                    LOG.info("{}: ignore received object", as);
                    deleteFile(as, file);
                }
                
            } catch (Exception e) {
                deleteFile(as, file);
                throw new DicomServiceException(Status.ProcessingFailure, e);
            }
        }
    };

    private final class StgCmtSCPImpl extends AbstractDicomService {

        public StgCmtSCPImpl() {
            super(UID.StorageCommitmentPushModel);
        }

        @Override
        public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse,
                Attributes rq, Attributes actionInfo) throws IOException {
            if (dimse != Dimse.N_ACTION_RQ)
                throw new DicomServiceException(Status.UnrecognizedOperation);

            int actionTypeID = rq.getInt(Tag.ActionTypeID, 0);
            if (actionTypeID != 1)
                throw new DicomServiceException(Status.NoSuchActionType)
                            .setActionTypeID(actionTypeID);

            Attributes rsp = Commands.mkNActionRSP(rq, Status.Success);
            String callingAET = as.getCallingAET();
            String calledAET = as.getCalledAET();
            Connection remoteConnection = getRemoteConnection(callingAET);
            if (remoteConnection == null)
                throw new DicomServiceException(Status.ProcessingFailure,
                        "Unknown Calling AET: " + callingAET);
            Attributes eventInfo =
                    calculateStorageCommitmentResult(calledAET, actionInfo);
            try {
                as.writeDimseRSP(pc, rsp, null);
                device.execute(new SendStgCmtResult(as, eventInfo,
                        stgCmtOnSameAssoc, remoteConnection));
            } catch (AssociationStateException e) {
                LOG.warn("{} << N-ACTION-RSP failed: {}", as, e.getMessage());
            }
        }

    }

    private final class CFindSCPImpl extends BasicCFindSCP {

        private final EnumSet<QueryRetrieveLevel2> qrLevels;

        public CFindSCPImpl(String sopClass, EnumSet<QueryRetrieveLevel2> qrLevels) {
            super(sopClass);
            this.qrLevels = qrLevels;
        }

        @Override
        protected QueryTask calculateMatches(Association as, PresentationContext pc,
                Attributes rq, Attributes keys) throws DicomServiceException {
            QueryRetrieveLevel2 level = QueryRetrieveLevel2.validateQueryIdentifier(
                    keys, qrLevels, relational(as, rq), relationalLenient);
            if (errorCFind != 0)
                throw new DicomServiceException(errorCFind);

            switch(level) {
            case PATIENT:
                return new PatientQueryTask(as, pc, rq, keys, DcmQRSCP.this);
            case STUDY:
                return new StudyQueryTask(as, pc, rq, keys, DcmQRSCP.this);
            case SERIES:
                return new SeriesQueryTask(as, pc, rq, keys, DcmQRSCP.this);
            case IMAGE:
                return new InstanceQueryTask(as, pc, rq, keys, DcmQRSCP.this);
            default:
                assert true;
            }
            throw new AssertionError();
        }

        private boolean relational(Association as, Attributes rq) {
            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            ExtendedNegotiation extNeg = as.getAAssociateAC().getExtNegotiationFor(cuid);
            return QueryOption.toOptions(extNeg).contains(QueryOption.RELATIONAL);
        }
    }

    private final class CGetSCPImpl extends BasicCGetSCP {

        private final EnumSet<QueryRetrieveLevel2> qrLevels;
        private final boolean withoutBulkData;

        public CGetSCPImpl(String sopClass, EnumSet<QueryRetrieveLevel2> qrLevels) {
            super(sopClass);
            this.qrLevels = qrLevels;
            this.withoutBulkData = qrLevels.size() == 1;
        }

        @Override
        protected RetrieveTask calculateMatches(Association as, PresentationContext pc,
                Attributes rq, Attributes keys) throws DicomServiceException {
            QueryRetrieveLevel2.validateRetrieveIdentifier(
                    keys, qrLevels, relational(as, rq), relationalLenient);
            if (errorCGet != 0)
                throw new DicomServiceException(errorCGet);

            List<InstanceLocator> matches = DcmQRSCP.this.calculateMatches(keys);
            if (matches.isEmpty())
                return null;

            RetrieveTaskImpl retrieveTask = new RetrieveTaskImpl(
                    Dimse.C_GET_RQ, as, pc, rq, matches, as, withoutBulkData, delayCStore);
            retrieveTask.setSendPendingRSP(isSendPendingCGet());
            return retrieveTask;
        }

        private boolean relational(Association as, Attributes rq) {
            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            ExtendedNegotiation extNeg = as.getAAssociateAC().getExtNegotiationFor(cuid);
            return QueryOption.toOptions(extNeg).contains(QueryOption.RELATIONAL);
        }

    }

    private final class CMoveSCPImpl extends BasicCMoveSCP {

        private final EnumSet<QueryRetrieveLevel2> qrLevels;

        public CMoveSCPImpl(String sopClass, EnumSet<QueryRetrieveLevel2>  qrLevels) {
            super(sopClass);
            this.qrLevels = qrLevels;
        }

        @Override
        protected RetrieveTask calculateMatches(Association as, PresentationContext pc,
                final Attributes rq, Attributes keys) throws DicomServiceException {
            QueryRetrieveLevel2.validateRetrieveIdentifier(
                    keys, qrLevels, relational(as, rq), relationalLenient);
            if (errorCMove != 0)
                throw new DicomServiceException(errorCMove);

            String moveDest = rq.getString(Tag.MoveDestination);
            final Connection remote = getRemoteConnection(moveDest);
            if (remote == null)
                throw new DicomServiceException(Status.MoveDestinationUnknown,
                        "Move Destination: " + moveDest + " unknown");
            List<InstanceLocator> matches = DcmQRSCP.this.calculateMatches(keys);
            if (matches.isEmpty())
                return null;

            AAssociateRQ aarq = makeAAssociateRQ(as.getLocalAET(), moveDest, matches);
            Association storeas = openStoreAssociation(as, remote, aarq);
            BasicRetrieveTask retrieveTask = new RetrieveTaskImpl(
                    Dimse.C_MOVE_RQ, as, pc, rq, matches, storeas, false, delayCStore);
            retrieveTask.setSendPendingRSPInterval(getSendPendingCMoveInterval());
            return retrieveTask;
        }

        private Association openStoreAssociation(Association as,
                Connection remote, AAssociateRQ aarq) throws DicomServiceException {
            try {
                return as.getApplicationEntity().connect(
                        as.getConnection(), remote, aarq);
            } catch (Exception e) {
                throw new DicomServiceException(Status.UnableToPerformSubOperations, e);
            }
        }

        private AAssociateRQ makeAAssociateRQ(String callingAET,
                String calledAET, List<InstanceLocator> matches) {
            AAssociateRQ aarq = new AAssociateRQ();
            aarq.setCalledAET(calledAET);
            aarq.setCallingAET(callingAET);
            for (InstanceLocator match : matches) {
                if (aarq.addPresentationContextFor(match.cuid, match.tsuid)) {
                    if (!UID.ExplicitVRLittleEndian.equals(match.tsuid))
                        aarq.addPresentationContextFor(match.cuid, UID.ExplicitVRLittleEndian);
                    if (!UID.ImplicitVRLittleEndian.equals(match.tsuid))
                        aarq.addPresentationContextFor(match.cuid, UID.ImplicitVRLittleEndian);
                }
            }
            return aarq;
        }

        private boolean relational(Association as, Attributes rq) {
            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            ExtendedNegotiation extNeg = as.getAAssociateAC().getExtNegotiationFor(cuid);
            return QueryOption.toOptions(extNeg).contains(QueryOption.RELATIONAL);
        }
    }

    public DcmQRSCP() throws IOException {
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.setAssociationAcceptor(true);
        ae.addConnection(conn);
        device.setDimseRQHandler(createServiceRegistry());
    }

    private void storeTo(Association as, Attributes fmi, 
            PDVInputStream data, File file) throws IOException  {
        LOG.info("{}: M-WRITE {}", as, file);
        file.getParentFile().mkdirs();
        DicomOutputStream out = new DicomOutputStream(file);
        try {
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
        } finally {
            SafeClose.close(out);
        }
    }

    private File getDestinationFile(Attributes attrs) {
        File file = new File(storageDir, filePathFormat.format(attrs));
        while (file.exists())
            file = new File(file.getParentFile(),
                    TagUtils.toHexString(new Random().nextInt()));
        return file;
    }

    private static void renameTo(Association as, File from, File dest)
            throws IOException {
        LOG.info("{}: M-RENAME {}", new Object[]{ as, from, dest });
        dest.getParentFile().mkdirs();
        if (!from.renameTo(dest))
            throw new IOException("Failed to rename " + from + " to " + dest);
    }

    private static Attributes parse(File file) throws IOException {
        DicomInputStream in = new DicomInputStream(file);
        try {
            in.setIncludeBulkData(IncludeBulkData.NO);
            return in.readDatasetUntilPixelData();
        } finally {
            SafeClose.close(in);
        }
    }


    private static void deleteFile(Association as, File file) {
        if (file.delete())
            LOG.info("{}: M-DELETE {}", as, file);
        else
            LOG.warn("{}: M-DELETE {} failed!", as, file);
    }

    private DicomServiceRegistry createServiceRegistry() {
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        serviceRegistry.addDicomService(new CStoreSCPImpl());
        serviceRegistry.addDicomService(new StgCmtSCPImpl());
        serviceRegistry.addDicomService(
                new CFindSCPImpl(
                        UID.PatientRootQueryRetrieveInformationModelFind,
                        PATIENT_ROOT_LEVELS));
        serviceRegistry.addDicomService(
                new CFindSCPImpl(
                        UID.StudyRootQueryRetrieveInformationModelFind,
                        STUDY_ROOT_LEVELS));
        serviceRegistry.addDicomService(
                new CFindSCPImpl(
                        UID.PatientStudyOnlyQueryRetrieveInformationModelFind,
                        PATIENT_STUDY_ONLY_LEVELS));
        serviceRegistry.addDicomService(
                new CGetSCPImpl(
                        UID.PatientRootQueryRetrieveInformationModelGet,
                        PATIENT_ROOT_LEVELS));
        serviceRegistry.addDicomService(
                new CGetSCPImpl(
                        UID.StudyRootQueryRetrieveInformationModelGet,
                        STUDY_ROOT_LEVELS));
        serviceRegistry.addDicomService(
                new CGetSCPImpl(
                        UID.PatientStudyOnlyQueryRetrieveInformationModelGet,
                        PATIENT_STUDY_ONLY_LEVELS));
        serviceRegistry.addDicomService(
                new CGetSCPImpl(
                        UID.CompositeInstanceRetrieveWithoutBulkDataGet,
                        EnumSet.of(QueryRetrieveLevel2.IMAGE)));
        serviceRegistry.addDicomService(
                new CMoveSCPImpl(
                        UID.PatientRootQueryRetrieveInformationModelMove,
                        PATIENT_ROOT_LEVELS));
        serviceRegistry.addDicomService(
                new CMoveSCPImpl(
                        UID.StudyRootQueryRetrieveInformationModelMove,
                        STUDY_ROOT_LEVELS));
        serviceRegistry.addDicomService(
                new CMoveSCPImpl(
                        UID.PatientStudyOnlyQueryRetrieveInformationModelMove,
                        PATIENT_STUDY_ONLY_LEVELS));
        return serviceRegistry ;
    }

    public final Device getDevice() {
        return device;
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

    public boolean isIgnoreCaseOfPN() {
        return ignoreCaseOfPN;
    }

    public void setIgnoreCaseOfPN(boolean ignoreCaseOfPN) {
        this.ignoreCaseOfPN = ignoreCaseOfPN;
    }

    public boolean isMatchNoValue() {
        return matchNoValue;
    }

    public void setMatchNoValue(boolean matchNoValue) {
        this.matchNoValue = matchNoValue;
    }

    public boolean isRelationalLenient() {
        return relationalLenient;
    }

    public void setRelationalLenient(boolean relationalLenient) {
        this.relationalLenient = relationalLenient;
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

    public int getDelayCFind() {
        return delayCFind;
    }

    public void setDelayCFind(int delayCFind) {
        this.delayCFind = delayCFind;
    }

    public int getDelayCStore() {
        return delayCStore;
    }

    public void setDelayCStore(int delayCStore) {
        this.delayCStore = delayCStore;
    }

    public int getErrorCFind() {
        return errorCFind;
    }

    public void setErrorCFind(int errorCFind) {
        this.errorCFind = errorCFind;
    }

    public int getErrorCMove() {
        return errorCMove;
    }

    public void setErrorCMove(int errorCMove) {
        this.errorCMove = errorCMove;
    }

    public int getErrorCGet() {
        return errorCGet;
    }

    public void setErrorCGet(int errorCGet) {
        this.errorCGet = errorCGet;
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
        CLIUtils.addSendTimeoutOption(opts);
        CLIUtils.addStoreTimeoutOption(opts);
        CLIUtils.addResponseTimeoutOption(opts);
        addDicomDirOption(opts);
        addTransferCapabilityOptions(opts);
        addInstanceAvailabilityOption(opts);
        addMatchingOptions(opts);
        addStgCmtOptions(opts);
        addSendingPendingOptions(opts);
        addDelayCFindOptions(opts);
        addDelayCStoreOptions(opts);
        addRemoteConnectionsOption(opts);
        addRoleSelectLenientOption(opts);
        addRelationalLenientOption(opts);
        addErrorStatusOption(opts, "cfind-error");
        addErrorStatusOption(opts, "cmove-error");
        addErrorStatusOption(opts, "cget-error");
        return CLIUtils.parseComandLine(args, opts, rb, DcmQRSCP.class);
    }

    private static void addErrorStatusOption(Options opts, String option) {
        opts.addOption(Option.builder()
                .hasArg()
                .argName("code")
                .desc(rb.getString(option))
                .longOpt(option)
                .build());
    }

    private static Options addRoleSelectLenientOption(Options opts) {
        return opts.addOption(null, "role-select-lenient", false, rb.getString("role-select-lenient"));
    }

    private static Options addRelationalLenientOption(Options opts) {
        return opts.addOption(null, "relational-lenient", false, rb.getString("relational-lenient"));
    }

    @SuppressWarnings("static-access")
    private static void addInstanceAvailabilityOption(Options opts) {
        opts.addOption(Option.builder()
                .hasArg()
                .argName("code")
                .desc(rb.getString("availability"))
                .longOpt("availability")
                .build());
    }

    private static void addMatchingOptions(Options opts) {
        opts.addOption(null, "match-pn-icase", false, rb.getString("match-pn-icase"));
        opts.addOption(null, "match-no-value", false, rb.getString("match-no-value"));
    }

    private static void addStgCmtOptions(Options opts) {
        opts.addOption(null, "stgcmt-same-assoc", false, rb.getString("stgcmt-same-assoc"));
    }

    @SuppressWarnings("static-access")
    private static void addSendingPendingOptions(Options opts) {
        opts.addOption(null, "pending-cget", false, rb.getString("pending-cget"));
        opts.addOption(Option.builder()
                .hasArg()
                .argName("s")
                .desc(rb.getString("pending-cmove"))
                .longOpt("pending-cmove")
                .build());
    }

    @SuppressWarnings("static-access")
    private static void addDelayCFindOptions(Options opts) {
        opts.addOption(Option.builder()
                .hasArg()
                .argName("ms")
                .desc(rb.getString("delay-cfind"))
                .longOpt("delay-cfind")
                .build());
    }

    @SuppressWarnings("static-access")
    private static void addDelayCStoreOptions(Options opts) {
        opts.addOption(Option.builder()
                .hasArg()
                .argName("ms")
                .desc(rb.getString("delay-cstore"))
                .longOpt("delay-cstore")
                .build());
    }

    @SuppressWarnings("static-access")
    private static void addDicomDirOption(Options opts) {
        opts.addOption(Option.builder()
                .hasArg()
                .argName("file")
                .desc(rb.getString("dicomdir"))
                .longOpt("dicomdir")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("pattern")
                .desc(rb.getString("filepath"))
                .longOpt("filepath")
                .build());
        opts.addOption(Option.builder()
                .longOpt("record-config")
                .hasArg()
                .argName("file|url")
                .desc(rb.getString("record-config"))
                .build());
    }

    @SuppressWarnings("static-access")
    private static void addTransferCapabilityOptions(Options opts) {
        opts.addOption(null, "all-storage", false, rb.getString("all-storage"));
        opts.addOption(null, "no-storage", false, rb.getString("no-storage"));
        opts.addOption(null, "no-query", false, rb.getString("no-query"));
        opts.addOption(null, "no-retrieve", false, rb.getString("no-retrieve"));
        opts.addOption(null, "relational", false, rb.getString("relational"));
        opts.addOption(Option.builder()
                .hasArg()
                .argName("file|url")
                .desc(rb.getString("storage-sop-classes"))
                .longOpt("storage-sop-classes")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("file|url")
                .desc(rb.getString("query-sop-classes"))
                .longOpt("query-sop-classes")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("file|url")
                .desc(rb.getString("retrieve-sop-classes"))
                .longOpt("retrieve-sop-classes")
                .build());
    }

    @SuppressWarnings("static-access")
    private static void addRemoteConnectionsOption(Options opts) {
        opts.addOption(Option.builder()
                .hasArg()
                .argName("file|url")
                .desc(rb.getString("ae-config"))
                .longOpt("ae-config")
                .build());
     }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            DcmQRSCP main = new DcmQRSCP();
            CLIUtils.configure(main.fsInfo, cl);
            CLIUtils.configureBindServer(main.conn, main.ae, cl);
            CLIUtils.configure(main.conn, cl);
            configureDicomFileSet(main, cl);
            configureTransferCapability(main, cl);
            configureInstanceAvailability(main, cl);
            configureMatching(main, cl);
            configureStgCmt(main, cl);
            configureSendPending(main, cl);
            configureDelayCFind(main, cl);
            configureDelayCStore(main, cl);
            configureRemoteConnections(main, cl);
            configureRoleSelectLenient(main, cl);
            configureRelationalLenient(main, cl);
            main.setErrorCFind(CLIUtils.getIntOption(cl, "cfind-error", 0));
            main.setErrorCMove(CLIUtils.getIntOption(cl, "cmove-error", 0));
            main.setErrorCGet(CLIUtils.getIntOption(cl, "cget-error", 0));
            ExecutorService executorService = Executors.newCachedThreadPool();
            ScheduledExecutorService scheduledExecutorService = 
                    Executors.newSingleThreadScheduledExecutor();
            main.device.setScheduledExecutor(scheduledExecutorService);
            main.device.setExecutor(executorService);
            main.device.bindConnections();
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

    private static void configureRelationalLenient(DcmQRSCP main, CommandLine cl) {
        main.setRelationalLenient(cl.hasOption("relational-lenient"));
    }

    private static void configureRoleSelectLenient(DcmQRSCP main, CommandLine cl) {
        main.device.setRoleSelectionNegotiationLenient(cl.hasOption("role-select-lenient"));
    }

    private static void configureDicomFileSet(DcmQRSCP main, CommandLine cl) throws Exception {
        if (!cl.hasOption("dicomdir"))
            throw new MissingOptionException(rb.getString("missing-dicomdir"));
        main.setDicomDirectory(new File(cl.getOptionValue("dicomdir")));
        main.setFilePathFormat(cl.getOptionValue("filepath", 
                        "DICOM/{0020000D,hash}/{0020000E,hash}/{00080018,hash}"));
        RecordFactory recFact = new RecordFactory();
        if (cl.hasOption("record-config"))
            recFact.loadConfiguration(cl.getOptionValue("record-config"));
        main.setRecordFactory(recFact);
    }

    private static void configureInstanceAvailability(DcmQRSCP main, CommandLine cl) {
        main.setInstanceAvailability(cl.getOptionValue("availability"));
    }

    private static void configureMatching(DcmQRSCP main, CommandLine cl) {
        main.setIgnoreCaseOfPN(cl.hasOption("match-pn-icase"));
        main.setMatchNoValue(cl.hasOption("match-no-value"));
    }

    private static void configureStgCmt(DcmQRSCP main, CommandLine cl) {
        main.setStgCmtOnSameAssoc(cl.hasOption("stgcmt-same-assoc"));
    }

    private static void configureSendPending(DcmQRSCP main, CommandLine cl) {
        main.setSendPendingCGet(cl.hasOption("pending-cget"));
        if (cl.hasOption("pending-cmove"))
                main.setSendPendingCMoveInterval(
                        Integer.parseInt(cl.getOptionValue("pending-cmove")));
    }

    private static void configureDelayCFind(DcmQRSCP main, CommandLine cl) {
        if (cl.hasOption("delay-cfind"))
                main.setDelayCFind(Integer.parseInt(cl.getOptionValue("delay-cfind")));
    }

    private static void configureDelayCStore(DcmQRSCP main, CommandLine cl) {
        if (cl.hasOption("delay-cstore"))
                main.setDelayCStore(Integer.parseInt(cl.getOptionValue("delay-cstore")));
    }

    private static void configureTransferCapability(DcmQRSCP main, CommandLine cl)
            throws IOException {
        ApplicationEntity ae = main.ae;
        EnumSet<QueryOption> queryOptions = cl.hasOption("relational")
                ? EnumSet.of(QueryOption.RELATIONAL)
                : EnumSet.noneOf(QueryOption.class);
        boolean storage = !cl.hasOption("no-storage") && main.isWriteable();
        if (storage && cl.hasOption("all-storage")) {
            TransferCapability tc = new TransferCapability(null, 
                    "*",
                    TransferCapability.Role.SCP,
                    "*");
            tc.setQueryOptions(queryOptions);
            ae.addTransferCapability(tc);
        } else {
            ae.addTransferCapability(
                    new TransferCapability(null, 
                            UID.Verification,
                            TransferCapability.Role.SCP,
                            UID.ImplicitVRLittleEndian));
            Properties storageSOPClasses = CLIUtils.loadProperties(
                    cl.getOptionValue("storage-sop-classes",
                            "resource:storage-sop-classes.properties"),
                    null);
            if (storage)
                addTransferCapabilities(ae, storageSOPClasses,
                        TransferCapability.Role.SCP, null);
            if (!cl.hasOption("no-retrieve")) {
                addTransferCapabilities(ae, storageSOPClasses,
                        TransferCapability.Role.SCU, null);
                Properties p = CLIUtils.loadProperties(
                        cl.getOptionValue("retrieve-sop-classes",
                                "resource:retrieve-sop-classes.properties"),
                        null);
                addTransferCapabilities(ae, p, TransferCapability.Role.SCP, queryOptions);
            }
            if (!cl.hasOption("no-query")) {
                Properties p = CLIUtils.loadProperties(
                        cl.getOptionValue("query-sop-classes",
                                "resource:query-sop-classes.properties"),
                        null);
                addTransferCapabilities(ae, p, TransferCapability.Role.SCP, queryOptions);
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

    private static void configureRemoteConnections(DcmQRSCP main, CommandLine cl)
            throws Exception {
        String file = cl.getOptionValue("ae-config", "resource:ae.properties");
        Properties aeConfig = CLIUtils.loadProperties(file, null);
        for (Map.Entry<Object, Object> entry : aeConfig.entrySet()) {
            String aet = (String) entry.getKey();
            String value = (String) entry.getValue();
            try {
                String[] hostPortCiphers = StringUtils.split(value, ':');
                String[] ciphers = new String[hostPortCiphers.length-2];
                System.arraycopy(hostPortCiphers, 2, ciphers, 0, ciphers.length);
                Connection remote = new Connection();
                remote.setHostname(hostPortCiphers[0]);
                remote.setPort(Integer.parseInt(hostPortCiphers[1]));
                remote.setTlsCipherSuites(ciphers);
                main.addRemoteConnection(aet, remote);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Invalid entry in " + file + ": " + aet + "=" + value);
            }
        }
    }

    final DicomDirReader getDicomDirReader() {
         return ddReader;
    }

    final DicomDirWriter getDicomDirWriter() {
         return ddWriter;
    }

    private void openDicomDir() throws IOException {
        if (!dicomDir.exists())
            DicomDirWriter.createEmptyDirectory(dicomDir,
                    UIDUtils.createUIDIfNull(fsInfo.getFilesetUID()),
                    fsInfo.getFilesetID(),
                    fsInfo.getDescriptorFile(), 
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

    public List<InstanceLocator> calculateMatches(Attributes keys)
            throws DicomServiceException {
        try {
            List<InstanceLocator> list = new ArrayList<InstanceLocator>();
            String[] patIDs = keys.getStrings(Tag.PatientID);
            String[] studyIUIDs = keys.getStrings(Tag.StudyInstanceUID);
            String[] seriesIUIDs = keys.getStrings(Tag.SeriesInstanceUID);
            String[] sopIUIDs = keys.getStrings(Tag.SOPInstanceUID);
            DicomDirReader ddr = ddReader;
            Attributes patRec = ddr.findPatientRecord(patIDs);
            while (patRec != null) {
                Attributes studyRec = ddr.findStudyRecord(patRec, studyIUIDs);
                while (studyRec != null) {
                    Attributes seriesRec = ddr.findSeriesRecord(studyRec, seriesIUIDs);
                    while (seriesRec != null) {
                        Attributes instRec = ddr.findLowerInstanceRecord(seriesRec, true, sopIUIDs);
                        while (instRec != null) {
                            String cuid = instRec.getString(Tag.ReferencedSOPClassUIDInFile);
                            String iuid = instRec.getString(Tag.ReferencedSOPInstanceUIDInFile);
                            String tsuid = instRec.getString(Tag.ReferencedTransferSyntaxUIDInFile);
                            String[] fileIDs = instRec.getStrings(Tag.ReferencedFileID);
                            String uri = ddr.toFile(fileIDs).toURI().toString();
                            list.add(new InstanceLocator(cuid, iuid, tsuid, uri));
                            if (sopIUIDs != null && sopIUIDs.length == 1)
                                break;
    
                            instRec = ddr.findNextInstanceRecord(instRec, true, sopIUIDs);
                        }
                        if (seriesIUIDs != null && seriesIUIDs.length == 1)
                            break;
    
                        seriesRec = ddr.findNextSeriesRecord(seriesRec, seriesIUIDs);
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
            throw new DicomServiceException(Status.UnableToCalculateNumberOfMatches, e);
        }
    }

    public Attributes calculateStorageCommitmentResult(String calledAET,
            Attributes actionInfo) throws DicomServiceException {
        Sequence requestSeq = actionInfo.getSequence(Tag.ReferencedSOPSequence);
        int size = requestSeq.size();
        String[] sopIUIDs = new String[size];
        Attributes eventInfo = new Attributes(6);
        eventInfo.setString(Tag.RetrieveAETitle, VR.AE, calledAET);
        eventInfo.setString(Tag.StorageMediaFileSetID, VR.SH, ddReader.getFileSetID());
        eventInfo.setString(Tag.StorageMediaFileSetUID, VR.SH, ddReader.getFileSetUID());
        eventInfo.setString(Tag.TransactionUID, VR.UI, actionInfo.getString(Tag.TransactionUID));
        Sequence successSeq = eventInfo.newSequence(Tag.ReferencedSOPSequence, size);
        Sequence failedSeq = eventInfo.newSequence(Tag.FailedSOPSequence, size);
        LinkedHashMap<String, String> map =
                new LinkedHashMap<String, String>(size * 4 / 3);
        for (int i = 0; i < sopIUIDs.length; i++) {
            Attributes item = requestSeq.get(i);
            map.put(sopIUIDs[i] = item.getString(Tag.ReferencedSOPInstanceUID),
                    item.getString(Tag.ReferencedSOPClassUID));
        }
        DicomDirReader ddr = ddReader;
        try {
            Attributes patRec = ddr.findPatientRecord();
            while (patRec != null) {
                Attributes studyRec = ddr.findStudyRecord(patRec);
                while (studyRec != null) {
                    Attributes seriesRec = ddr.findSeriesRecord(studyRec);
                    while (seriesRec != null) {
                        Attributes instRec = ddr.findLowerInstanceRecord(seriesRec, true, sopIUIDs);
                        while (instRec != null) {
                            String iuid = instRec.getString(Tag.ReferencedSOPInstanceUIDInFile);
                            String cuid = map.remove(iuid);
                            if (cuid.equals(instRec.getString(Tag.ReferencedSOPClassUIDInFile)))
                                successSeq.add(refSOP(iuid, cuid, Status.Success));
                            else
                                failedSeq.add(refSOP(iuid, cuid, Status.ClassInstanceConflict));
                            instRec = ddr.findNextInstanceRecord(instRec, true, sopIUIDs);
                        }
                        seriesRec = ddr.findNextSeriesRecord(seriesRec);
                    }
                    studyRec = ddr.findNextStudyRecord(studyRec);
                }
                patRec = ddr.findNextPatientRecord(patRec);
            }
        } catch (IOException e) {
            LOG.info("Failed to M-READ " + dicomDir, e);
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            failedSeq.add(refSOP(entry.getKey(), entry.getValue(), Status.NoSuchObjectInstance));
        }
        if (failedSeq.isEmpty())
            eventInfo.remove(Tag.FailedSOPSequence);
        return eventInfo;
    }

    boolean addDicomDirRecords(Association as, Attributes ds, Attributes fmi,
            File f) throws IOException {
        DicomDirWriter ddWriter = getDicomDirWriter();
        RecordFactory recFact = getRecordFactory();
        String pid = ds.getString(Tag.PatientID, null);
        String styuid = ds.getString(Tag.StudyInstanceUID, null);
        String seruid = ds.getString(Tag.SeriesInstanceUID, null);
        String iuid = fmi.getString(Tag.MediaStorageSOPInstanceUID, null);
        if (pid == null)
            ds.setString(Tag.PatientID, VR.LO, pid = styuid);
    
        Attributes patRec = ddWriter.findPatientRecord(pid);
        if (patRec == null) {
            patRec = recFact.createRecord(RecordType.PATIENT, null,
                    ds, null, null);
            ddWriter.addRootDirectoryRecord(patRec);
        }
        Attributes studyRec = ddWriter.findStudyRecord(patRec, styuid);
        if (studyRec == null) {
            studyRec = recFact.createRecord(RecordType.STUDY, null,
                    ds, null, null);
            ddWriter.addLowerDirectoryRecord(patRec, studyRec);
        }
        Attributes seriesRec = ddWriter.findSeriesRecord(studyRec, seruid);
        if (seriesRec == null) {
            seriesRec = recFact.createRecord(RecordType.SERIES, null,
                    ds, null, null);
            ddWriter.addLowerDirectoryRecord(studyRec, seriesRec);
        }
        Attributes instRec = 
                ddWriter.findLowerInstanceRecord(seriesRec, false, iuid);
        if (instRec != null)
            return false;
    
        instRec = recFact.createRecord(ds, fmi, ddWriter.toFileIDs(f));
        ddWriter.addLowerDirectoryRecord(seriesRec, instRec);
        ddWriter.commit();
        return true;
    }

    private static Attributes refSOP(String iuid, String cuid, int failureReason) {
        Attributes attrs = new Attributes(3);
        attrs.setString(Tag.ReferencedSOPClassUID, VR.UI, cuid);
        attrs.setString(Tag.ReferencedSOPInstanceUID, VR.UI, iuid);
        if (failureReason != Status.Success)
            attrs.setInt(Tag.FailureReason, VR.US, failureReason);
        return attrs ;
    }

}
