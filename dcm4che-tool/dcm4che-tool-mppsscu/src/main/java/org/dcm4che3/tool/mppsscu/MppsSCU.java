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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4che3.tool.mppsscu;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.common.DicomFiles;
import org.dcm4che3.util.DateUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class MppsSCU {
    
    public static final class MppsWithIUID {
        
        public String iuid;
        public Attributes mpps;
        
        MppsWithIUID(String iuid, Attributes mpps) {
            this.iuid = iuid;
            this.mpps = mpps;
        }
    }

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.mppsscu.messages");
    private static final ElementDictionary dict =
            ElementDictionary.getStandardElementDictionary();
    private static final String IN_PROGRESS = "IN PROGRESS";
    private static final String COMPLETED = "COMPLETED";
    private static final String DISCONTINUED = "DISCONTINUED";
    private static final int[] MPPS_TOP_LEVEL_ATTRS = {
        Tag.SpecificCharacterSet,
        Tag.Modality,
        Tag.ProcedureCodeSequence,
        Tag.ReferencedPatientSequence,
        Tag.PatientName,
        Tag.PatientID,
        Tag.IssuerOfPatientID,
        Tag.IssuerOfPatientIDQualifiersSequence,
        Tag.PatientBirthDate,
        Tag.PatientSex,
        Tag.StudyID,
        Tag.AdmissionID,
        Tag.IssuerOfAdmissionIDSequence,
        Tag.ServiceEpisodeID,
        Tag.IssuerOfServiceEpisodeID,
        Tag.ServiceEpisodeDescription,
        Tag.PerformedProcedureStepStartDate,
        Tag.PerformedProcedureStepStartTime,
        Tag.PerformedProcedureStepID,
        Tag.PerformedProcedureStepDescription,
        Tag.PerformedProtocolCodeSequence,
        Tag.CommentsOnThePerformedProcedureStep,
    };

    private static final int[] MPPS_TOP_LEVEL_TYPE_2_ATTRS = {
        Tag.ProcedureCodeSequence,
        Tag.ReferencedPatientSequence,
        Tag.PatientName,
        Tag.PatientID,
        Tag.PatientBirthDate,
        Tag.PatientSex,
        Tag.StudyID,
        Tag.PerformedStationName,
        Tag.PerformedLocation,
        Tag.PerformedProcedureStepDescription,
        Tag.PerformedProcedureTypeDescription,
        Tag.PerformedProtocolCodeSequence,
    };

    private static final int[] CREATE_MPPS_TOP_LEVEL_EMPTY_ATTRS = {
        Tag.PerformedProcedureStepEndDate,
        Tag.PerformedProcedureStepEndTime,
        Tag.PerformedProcedureStepDiscontinuationReasonCodeSequence,
        Tag.PerformedSeriesSequence
    };

    private static final int[] FINAL_MPPS_TOP_LEVEL_ATTRS = {
        Tag.SpecificCharacterSet,
        Tag.PerformedProcedureStepEndDate,
        Tag.PerformedProcedureStepEndTime,
        Tag.PerformedProcedureStepStatus,
        Tag.PerformedProcedureStepDiscontinuationReasonCodeSequence,
        Tag.PerformedSeriesSequence
    };

    private static final int[] SSA_ATTRS = {
        Tag.AccessionNumber,
        Tag.IssuerOfAccessionNumberSequence,
        Tag.ReferencedStudySequence,
        Tag.StudyInstanceUID,
        Tag.RequestedProcedureDescription,
        Tag.RequestedProcedureCodeSequence,
        Tag.ScheduledProcedureStepDescription,
        Tag.ScheduledProtocolCodeSequence,
        Tag.ScheduledProcedureStepID,
        Tag.OrderPlacerIdentifierSequence,
        Tag.OrderFillerIdentifierSequence,
        Tag.RequestedProcedureID,
        Tag.PlacerOrderNumberImagingServiceRequest,
        Tag.FillerOrderNumberImagingServiceRequest,
    };

    private static final int[] SSA_TYPE_2_ATTRS = {
        Tag.AccessionNumber,
        Tag.ReferencedStudySequence,
        Tag.StudyInstanceUID,
        Tag.RequestedProcedureDescription,
        Tag.RequestedProcedureID,
        Tag.ScheduledProcedureStepDescription,
        Tag.ScheduledProtocolCodeSequence,
        Tag.ScheduledProcedureStepID,
    };

    private static final int[] PERF_SERIES_ATTRS = {
        Tag.SeriesDescription,
        Tag.PerformingPhysicianName,
        Tag.OperatorsName,
        Tag.ProtocolName,
        Tag.SeriesInstanceUID
    };

    private static final int[] PERF_SERIES_TYPE_2_ATTRS = {
        Tag.RetrieveAETitle,
        Tag.SeriesDescription,
        Tag.PerformingPhysicianName,
        Tag.OperatorsName,
        Tag.ReferencedNonImageCompositeSOPInstanceSequence
    };

    private static final String ppsStartDate;
    private static final String ppsStartTime;
    static {
        Date now = new Date();
        ppsStartDate = DateUtils.formatDA(null, now);
        ppsStartTime = DateUtils.formatTM(null, now);
    }

    

    public interface RSPHandlerFactory {
        
        DimseRSPHandler createDimseRSPHandlerForNCreate(MppsWithIUID mppsWithUID);
        DimseRSPHandler createDimseRSPHandlerForNSet();
    }
    
    //default response handler
    private RSPHandlerFactory rspHandlerFactory = new RSPHandlerFactory(){

        @Override
        public DimseRSPHandler createDimseRSPHandlerForNCreate(final MppsWithIUID mppsWithUID) {
            
            return new DimseRSPHandler(as.nextMessageID()) {
                
                @Override
                public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                    switch(cmd.getInt(Tag.Status, -1)) {
                        case Status.Success:
                        case Status.AttributeListError:
                        case Status.AttributeValueOutOfRange:
                            mppsWithUID.iuid = cmd.getString(
                                    Tag.AffectedSOPInstanceUID, mppsWithUID.iuid);
                            addCreatedMpps(mppsWithUID);
                    }
                    super.onDimseRSP(as, cmd, data);
                }
            };
        }
        
        @Override
        public DimseRSPHandler createDimseRSPHandlerForNSet() {
            
            return new DimseRSPHandler(as.nextMessageID());
        }
    };
    
    //Response handler is settable from outside
    public void setRspHandlerFactory(RSPHandlerFactory rspHandlerFactory) {
        this.rspHandlerFactory = rspHandlerFactory;
    }


    private final ApplicationEntity ae;
    private final Connection remote;
    private final AAssociateRQ rq = new AAssociateRQ();
    private Attributes attrs;
    private String uidSuffix;
    private boolean newPPSID;
    private int serialNo = (int) (System.currentTimeMillis() & 0x7FFFFFFFL);
    private String ppsuid;
    private String ppsid;
    private DecimalFormat ppsidFormat = new DecimalFormat("PPS-0000000000");
    private String protocolName = "UNKNOWN";
    private String archiveRequested;
    private String finalStatus = COMPLETED;
    private Attributes discontinuationReason;

    private Properties codes;
    private HashMap<String,MppsWithIUID> map = new HashMap<String,MppsWithIUID>();
    private ArrayList<MppsWithIUID> created = new ArrayList<MppsWithIUID>();
    private Association as;
    private Device device;

    public MppsSCU(ApplicationEntity ae) throws IOException {
        this.remote = new Connection();
        this.ae = ae;
        this.device = ae.getDevice();
    }

    public Connection getRemoteConnection() {
        return remote;
    }

    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }
    
    public void addCreatedMpps (MppsWithIUID mpps)
    {
        created.add(mpps);
    }

    public final void setUIDSuffix(String uidSuffix) {
        this.uidSuffix = uidSuffix;
    }

    public final void setPPSUID(String ppsuid) {
        this.ppsuid = ppsuid;
    }

    public final void setPPSID(String ppsid) {
        this.ppsid = ppsid;
    }

    public final void setPPSIDStart(int ppsidStart) {
        this.serialNo = ppsidStart;
    }

    public final void setPPSIDFormat(String ppsidFormat) {
        this.ppsidFormat = new DecimalFormat(ppsidFormat);
    }

    public final void setNewPPSID(boolean newPPSID) {
        this.newPPSID = newPPSID;
    }

    public final void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public final void setArchiveRequested(String archiveRequested) {
        this.archiveRequested = archiveRequested;
    }

    public final void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }

    public final void setCodes(Properties codes) {
        this.codes = codes;
    }

    public void setAttributes(Attributes attrs) {
        this.attrs = attrs;
    }

    public final void setDiscontinuationReason(String codeValue) {
        if (codes == null)
            throw new IllegalStateException("codes not initialized");
        String codeMeaning = codes.getProperty(codeValue);
        if (codeMeaning == null)
            throw new IllegalArgumentException("undefined code value: "
                        + codeValue);
        int endDesignator = codeValue.indexOf('-');
        Attributes attrs = new Attributes(3);
        attrs.setString(Tag.CodeValue, VR.SH,
                endDesignator >= 0
                    ? codeValue.substring(endDesignator + 1)
                    : codeValue);
        attrs.setString(Tag.CodingSchemeDesignator, VR.SH,
                endDesignator >= 0
                    ? codeValue.substring(0, endDesignator)
                    : "DCM");
        attrs.setString(Tag.CodeMeaning, VR.LO, codeMeaning);
        this.discontinuationReason = attrs;
    }

    public void setTransferSyntaxes(String[] tss) {
        rq.addPresentationContext(
                new PresentationContext(1, UID.VerificationSOPClass,
                        UID.ImplicitVRLittleEndian));
        rq.addPresentationContext(
                new PresentationContext(3,
                        UID.ModalityPerformedProcedureStepSOPClass,
                        tss));
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Device device = new Device("mppsscu");
            Connection conn = new Connection();
            device.addConnection(conn);
            ApplicationEntity ae = new ApplicationEntity("MPPSSCU");
            device.addApplicationEntity(ae);
            ae.addConnection(conn);
            final MppsSCU main = new MppsSCU(ae);
            configureMPPS(main, cl);
            CLIUtils.configureConnect(main.remote, main.rq, cl);
            CLIUtils.configureBind(conn, main.ae, cl);
            CLIUtils.configure(conn, cl);
            main.remote.setTlsProtocols(conn.tlsProtocols());
            main.remote.setTlsCipherSuites(conn.getTlsCipherSuites());
            main.setTransferSyntaxes(CLIUtils.transferSyntaxesOf(cl));
            main.setAttributes(new Attributes());
            CLIUtils.addAttributes(main.attrs, cl.getOptionValues("s"));
            main.setUIDSuffix(cl.getOptionValue("uid-suffix"));
            List<String> argList = cl.getArgList();
            boolean echo = argList.isEmpty();
            if (!echo) {
                System.out.println(rb.getString("scanning"));
                main.scanFiles(argList, true);
            }
            ExecutorService executorService =
                    Executors.newSingleThreadExecutor();
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newSingleThreadScheduledExecutor();
            device.setExecutor(executorService);
            device.setScheduledExecutor(scheduledExecutorService);
            try {
                main.open();
                if (echo)
                    main.echo();
                else {
                    main.createMpps();
                    main.updateMpps();
                }
            } finally {
                main.close();
                executorService.shutdown();
                scheduledExecutorService.shutdown();
            }
        } catch (ParseException e) {
            System.err.println("mppsscu: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("mppsscu: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }
    
    public void scanFiles(List<String> fnames, boolean printout) throws IOException {
        
        if (printout) System.out.println(rb.getString("scanning"));
        DicomFiles.scan(fnames, printout, new DicomFiles.Callback() {
            
            @Override
            public boolean dicomFile(File f, Attributes fmi, 
                    long dsPos, Attributes ds) {
                if (UID.ModalityPerformedProcedureStepSOPClass.equals(
                        fmi.getString(Tag.MediaStorageSOPClassUID))) {
                    return addMPPS(
                            fmi.getString(Tag.MediaStorageSOPInstanceUID),
                            ds);
                }
                return addInstance(ds);
            }
        });
    }

    private static void configureMPPS(MppsSCU main, CommandLine cl)
            throws Exception {
        main.setNewPPSID(cl.hasOption("ppsid-new"));
        main.setPPSUID(cl.getOptionValue("ppsuid"));
        main.setPPSID(cl.getOptionValue("ppsid"));
        if (cl.hasOption("ppsid-start"))
            main.setPPSIDStart(Integer.parseInt(cl.getOptionValue("ppsid-start")));
        main.setPPSIDFormat(cl.getOptionValue("ppsid-format", "PPS-0000000000"));
        main.setProtocolName(cl.getOptionValue("protocol", "UNKNOWN"));
        if (cl.hasOption("archive"))
            main.setArchiveRequested(cl.getOptionValue("archive"));
        main.setCodes(CLIUtils.loadProperties(
                cl.getOptionValue("code-config", "resource:code.properties"),
                null));
        if (cl.hasOption("dc"))
            main.setFinalStatus(DISCONTINUED);
        if (cl.hasOption("dc-reason"))
            main.setDiscontinuationReason(cl.getOptionValue("dc-reason"));
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        CLIUtils.addTransferSyntaxOptions(opts);
        CLIUtils.addConnectOption(opts);
        CLIUtils.addBindOption(opts, "MPPSSCU");
        CLIUtils.addAEOptions(opts);
        CLIUtils.addResponseTimeoutOption(opts);
        CLIUtils.addCommonOptions(opts);
        addMPPSOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, MppsSCU.class);
    }

    @SuppressWarnings("static-access")
    private static void addMPPSOptions(Options opts) {
        opts.addOption(null, "ppsid-new", false, rb.getString("ppsid-new"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("uid")
                .withDescription(rb.getString("ppsuid"))
                .withLongOpt("ppsuid")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("id")
                .withDescription(rb.getString("ppsid"))
                .withLongOpt("ppsid")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("num")
                .withDescription(rb.getString("ppsid-start"))
                .withLongOpt("ppsid-start")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("pattern")
                .withDescription(rb.getString("ppsid-format"))
                .withLongOpt("ppsid-format")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("name")
                .withDescription(rb.getString("protocol"))
                .withLongOpt("protocol")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("YES|NO")
                .withDescription(rb.getString("archive"))
                .withLongOpt("archive")
                .create());
        opts.addOption(null, "dc", false, rb.getString("dc"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("code-value")
                .withDescription(rb.getString("dc-reason"))
                .withLongOpt("dc-reason")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("code-config"))
                .withLongOpt("code-config")
                .create());
        opts.addOption(OptionBuilder
                .hasArgs()
                .withArgName("[seq/]attr=value")
                .withValueSeparator('=')
                .withDescription(rb.getString("set"))
                .create("s"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("suffix")
                .withDescription(rb.getString("uid-suffix"))
                .withLongOpt("uid-suffix")
                .create(null));
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

    public void echo() throws IOException, InterruptedException {
        as.cecho().next();
    }

    public void createMpps() throws IOException, InterruptedException {
        for (MppsWithIUID mppsWithUID : map.values())
            createMpps(mppsWithUID);
        as.waitForOutstandingRSP();
    }

    private void createMpps(final MppsWithIUID mppsWithUID)
            throws IOException, InterruptedException {
        final String iuid = mppsWithUID.iuid;
        Attributes mpps = mppsWithUID.mpps;
        mppsWithUID.mpps = new Attributes(mpps, FINAL_MPPS_TOP_LEVEL_ATTRS);
        mpps.setString(Tag.PerformedProcedureStepStatus, VR.CS, IN_PROGRESS);
        for (int tag : CREATE_MPPS_TOP_LEVEL_EMPTY_ATTRS)
            mpps.setNull(tag, dict.vrOf(tag));

        as.ncreate(UID.ModalityPerformedProcedureStepSOPClass,
                iuid, mpps, null, rspHandlerFactory.createDimseRSPHandlerForNCreate(mppsWithUID));
    }

    public void updateMpps() throws IOException, InterruptedException {
        for (MppsWithIUID mppsWithIUID : created)
            setMpps(mppsWithIUID);
    }

    private void setMpps(MppsWithIUID mppsWithIUID)
            throws IOException, InterruptedException {
        as.nset(UID.ModalityPerformedProcedureStepSOPClass,
                mppsWithIUID.iuid, mppsWithIUID.mpps, null, rspHandlerFactory.createDimseRSPHandlerForNSet());
    }

    public boolean addInstance(Attributes inst) {
        CLIUtils.updateAttributes(inst, attrs, uidSuffix);
        String suid = inst.getString(Tag.StudyInstanceUID);
        if (suid == null)
            return false;
        MppsWithIUID mppsWithIUID = map.get(suid);
        if (mppsWithIUID == null)
            map.put(suid, mppsWithIUID = new MppsWithIUID(ppsuid(null), createMPPS(inst)));
        updateMPPS(mppsWithIUID.mpps, inst);
        return true;
    }

    public boolean addMPPS(String iuid, Attributes mpps) {
        map.put(iuid, new MppsWithIUID(ppsuid(iuid), mpps));
        return true;
    }

    private String ppsuid(String defval) {
        if (ppsuid == null)
            return defval;
        
        int size = map.size();
        switch (size) {
        case 0:
            return ppsuid;
        case 1:
            map.values().iterator().next().iuid += ".1";
        }
        return ppsuid + '.' + (size + 1);
    }


    private String mkPPSID() {
        if (ppsid != null)
            return ppsid;
        String id = ppsidFormat.format(serialNo);
        if (++serialNo < 0)
            serialNo = 0;
        return id;
    }

    private Attributes createMPPS(Attributes inst) {
        Attributes mpps = new Attributes();
        mpps.setString(Tag.PerformedStationAETitle, VR.AE, ae.getAETitle());
        mpps.setString(Tag.PerformedProcedureStepStartDate, VR.DA,
                inst.getString(Tag.StudyDate, ppsStartDate));
        mpps.setString(Tag.PerformedProcedureStepStartTime, VR.TM,
                inst.getString(Tag.StudyTime, ppsStartTime));
        for (int tag : MPPS_TOP_LEVEL_TYPE_2_ATTRS)
            mpps.setNull(tag, dict.vrOf(tag));
        mpps.addSelected(inst, MPPS_TOP_LEVEL_ATTRS);
        if (newPPSID || !mpps.containsValue(Tag.PerformedProcedureStepID))
            mpps.setString(Tag.PerformedProcedureStepID, VR.CS, mkPPSID());
        mpps.setString(Tag.PerformedProcedureStepEndDate, VR.DA,
                mpps.getString(Tag.PerformedProcedureStepStartDate));
        mpps.setString(Tag.PerformedProcedureStepEndTime, VR.TM,
                mpps.getString(Tag.PerformedProcedureStepStartTime));
        mpps.setString(Tag.PerformedProcedureStepStatus, VR.CS, finalStatus);
        Sequence dcrSeq = mpps.newSequence(Tag.PerformedProcedureStepDiscontinuationReasonCodeSequence, 1);
        if (discontinuationReason != null)
            dcrSeq.add(new Attributes(discontinuationReason));

        Sequence raSeq = inst.getSequence(Tag.RequestAttributesSequence);
        if (raSeq == null || raSeq.isEmpty()) {
            Sequence ssaSeq = 
                    mpps.newSequence(Tag.ScheduledStepAttributesSequence, 1);
            Attributes ssa = new Attributes();
            ssaSeq.add(ssa);
            for (int tag : SSA_TYPE_2_ATTRS)
                ssa.setNull(tag, dict.vrOf(tag));
            ssa.addSelected(inst, SSA_ATTRS);
        } else {
            Sequence ssaSeq =
                    mpps.newSequence(Tag.ScheduledStepAttributesSequence, raSeq.size());
            for (Attributes ra : raSeq) {
                Attributes ssa = new Attributes();
                ssaSeq.add(ssa);
                for (int tag : SSA_TYPE_2_ATTRS)
                    ssa.setNull(tag, dict.vrOf(tag));
                ssa.addSelected(inst, SSA_ATTRS);
                ssa.addSelected(ra, SSA_ATTRS);
            }
        }
        mpps.newSequence(Tag.PerformedSeriesSequence, 1);
        return mpps ;
    }

    private void updateMPPS(Attributes mpps, Attributes inst) {
        String endTime = inst.getString(Tag.AcquisitionTime);
        if (endTime == null) {
            endTime = inst.getString(Tag.ContentTime);
            if (endTime == null)
                endTime = inst.getString(Tag.SeriesTime);
        }
        if (endTime != null && endTime.compareTo(
                mpps.getString(Tag.PerformedProcedureStepEndTime)) > 0)
            mpps.setString(Tag.PerformedProcedureStepEndTime, VR.TM, endTime);
        Sequence prefSeriesSeq = mpps.getSequence(Tag.PerformedSeriesSequence);
        Attributes prefSeries = getPerfSeries(prefSeriesSeq, inst);
        Sequence refSOPSeq = prefSeries.getSequence(Tag.ReferencedImageSequence);
        Attributes refSOP = new Attributes();
        refSOPSeq.add(refSOP);
        refSOP.setString(Tag.ReferencedSOPClassUID, VR.UI,
                inst.getString(Tag.SOPClassUID));
        refSOP.setString(Tag.ReferencedSOPInstanceUID, VR.UI,
                inst.getString(Tag.SOPInstanceUID));
    }

    private Attributes getPerfSeries(Sequence prefSeriesSeq, Attributes inst) {
        String suid = inst.getString(Tag.SeriesInstanceUID);
        for (Attributes prefSeries : prefSeriesSeq) {
            if (suid.equals(prefSeries.getString(Tag.SeriesInstanceUID)))
                return prefSeries;
        }
        Attributes prefSeries = new Attributes();
        prefSeriesSeq.add(prefSeries);
        for (int tag : PERF_SERIES_TYPE_2_ATTRS)
            prefSeries.setNull(tag, dict.vrOf(tag));
        prefSeries.setString(Tag.ProtocolName, VR.LO, protocolName);
        prefSeries.addSelected(inst, PERF_SERIES_ATTRS);
        prefSeries.newSequence(Tag.ReferencedImageSequence, 10);
        if (archiveRequested != null)
            prefSeries.setString(Tag.ArchiveRequested, VR.CS, archiveRequested);
        return prefSeries;
    }
}
