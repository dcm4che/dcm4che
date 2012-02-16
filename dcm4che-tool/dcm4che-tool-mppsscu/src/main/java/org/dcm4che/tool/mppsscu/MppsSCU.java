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

package org.dcm4che.tool.mppsscu;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che.data.Attributes;
import org.dcm4che.data.ElementDictionary;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Association;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.DimseRSPHandler;
import org.dcm4che.net.IncompatibleConnectionException;
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.tool.common.CLIUtils;
import org.dcm4che.util.DateUtils;
import org.dcm4che.util.SafeClose;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class MppsSCU extends Device {

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che.tool.mppsscu.messages");
    private static final ElementDictionary dict =
            ElementDictionary.getStandardElementDictionary();
    private static final String IN_PROGRESS = "IN PROGRESS";
    private static final String COMPLETED = "COMPLETED";
    private static final String DISCONTINUED = "DISCONTINUED";
    private static final int[] MPPS_TOP_LEVEL_ATTRS = {
        Tag.SpecificCharacterSet,
        Tag.Modality,
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
        Tag.PerformedProcedureStepDiscontinuationReasonCodeSequence
    };

    private static final int[] SSA_ATTRS = {
        Tag.AccessionNumber,
        Tag.IssuerOfAccessionNumberSequence,
        Tag.ReferencedStudySequence,
        Tag.StudyInstanceUID,
        Tag.RequestedProcedureDescription,
        Tag.RequestedProcedureCodeSequence,
        Tag.RequestedProcedureID,
        Tag.ScheduledProcedureStepDescription,
        Tag.ScheduledProtocolCodeSequence,
        Tag.ScheduledProcedureStepID,
        Tag.OrderPlacerIdentifierSequence,
        Tag.OrderFillerIdentifierSequence,
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
        Tag.OperatorsName
    };
    private static final String ppsStartDate;
    private static final String ppsStartTime;
    static {
        Date now = new Date();
        ppsStartDate = DateUtils.formatDA(null, now);
        ppsStartTime = DateUtils.formatTM(null, now);
    }

    private final ApplicationEntity ae = new ApplicationEntity("MPPSSCU");
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private boolean newPPSID;
    private int serialNo = (int) (System.currentTimeMillis() & 0x7FFFFFFFL);
    private String ppsid;
    private DecimalFormat ppsidFormat = new DecimalFormat("PPS-0000000000");
    private String protocolName = "UNKNOWN";
    private String archiveRequested;
    private String finalStatus = COMPLETED;
    private Attributes discontinuationReason;

    private Properties codes = new Properties();
    private HashMap<String,Attributes> map = new HashMap<String,Attributes>();
    private Association as;
    private final AtomicInteger outstanding = new AtomicInteger(0);

    public MppsSCU() throws IOException {
        super("mppsscu");
        addConnection(conn);
        addApplicationEntity(ae);
        ae.addConnection(conn);
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

    public final void setDiscontinuationReason(String codeValue) {
        if (codes == null)
            throw new IllegalStateException("codes not initialized");
        String codeMeaning = codes.getProperty(codeValue);
        if (codeMeaning == null)
            throw new IllegalArgumentException("undefined code value: "
                        + codeValue);
        Attributes attrs = new Attributes(3);
        attrs.setString(Tag.CodeValue, VR.SH, codeValue);
        attrs.setString(Tag.CodingSchemeDesignator, VR.SH, "DCM");
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
            MppsSCU main = new MppsSCU();
            configureMPPS(main, cl);
            CLIUtils.configureConnect(main.remote, main.rq, cl);
            CLIUtils.configureBind(main.conn, main.ae, cl);
            CLIUtils.configure(main.conn, cl);
            main.remote.setTlsProtocols(main.conn.getTlsProtocols());
            main.remote.setTlsCipherSuites(main.conn.getTlsCipherSuites());
            main.setTransferSyntaxes(CLIUtils.transferSyntaxesOf(cl));
            List<String> argList = cl.getArgList();
            boolean echo = argList.isEmpty();
            if (!echo) {
                System.out.println(rb.getString("scanning"));
                for (String fname : argList)
                    main.scanFile(new File(fname));
            }
            ExecutorService executorService =
                    Executors.newSingleThreadExecutor();
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newSingleThreadScheduledExecutor();
            main.setExecutor(executorService);
            main.setScheduledExecutor(scheduledExecutorService);
            try {
                main.open();
                if (echo)
                    main.echo();
                else {
                    main.sendMpps();
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

    private static void configureMPPS(MppsSCU main, CommandLine cl)
            throws Exception {
        main.setNewPPSID(cl.hasOption("ppsid-new"));
        if (cl.hasOption("ppsid"))
            main.setPPSID(cl.getOptionValue("ppsid"));
        if (cl.hasOption("ppsid-start"))
            main.setPPSIDStart(Integer.parseInt(cl.getOptionValue("ppsid-start")));
        if (cl.hasOption("ppsid-format"))
            main.setPPSIDFormat(cl.getOptionValue("ppsid-format"));
        if (cl.hasOption("protocol"))
            main.setProtocolName(cl.getOptionValue("protocol"));
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
        addMPPSOptions(opts);
        CLIUtils.addTransferSyntaxOptions(opts);
        CLIUtils.addConnectOption(opts);
        CLIUtils.addBindOption(opts, "MPPSSCU");
        CLIUtils.addAEOptions(opts);
        CLIUtils.addCEchoRspOption(opts);
        CLIUtils.addNCreateRspOption(opts);
        CLIUtils.addNSetRspOption(opts);
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, MppsSCU.class);
    }

    @SuppressWarnings("static-access")
    private static void addMPPSOptions(Options opts) {
        opts.addOption(null, "ppsid-new", false, rb.getString("ppsid-new"));
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
    }

    public void open() throws IOException, InterruptedException,
            IncompatibleConnectionException {
        as = ae.connect(conn, remote, rq);
    }

    public void close() throws IOException, InterruptedException {
        if (as != null) {
            synchronized (outstanding) {
                while (outstanding.get() > 0)
                    outstanding.wait();
            }
            as.release();
        }
    }

    public void echo() throws IOException, InterruptedException {
        as.cecho().next();
    }

    public void sendMpps() throws IOException, InterruptedException {
        for (Attributes mpps : map.values())
            sendMpps(mpps);
    }

    private void sendMpps(Attributes mpps) throws IOException, InterruptedException {
        final Attributes finalMpps = new Attributes(5);
        finalMpps.addSelected(mpps,
                Tag.SpecificCharacterSet,
                Tag.PerformedProcedureStepEndDate,
                Tag.PerformedProcedureStepEndTime,
                Tag.PerformedSeriesSequence);
        finalMpps.setString(Tag.PerformedProcedureStepStatus, VR.CS, finalStatus );
        if (discontinuationReason != null)
            finalMpps.newSequence(
                    Tag.PerformedProcedureStepDiscontinuationReasonCodeSequence, 1)
                .add(discontinuationReason);
        mpps.setNull(Tag.PerformedSeriesSequence, VR.SQ);
        mpps.setNull(Tag.PerformedProcedureStepEndDate, VR.DA);
        mpps.setNull(Tag.PerformedProcedureStepEndTime, VR.TM);

        DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                MppsSCU.this.onNCreateRSP(cmd, data, finalMpps);
            }

            @Override
            public void onClose(Association as) {
                super.onClose(as);
                MppsSCU.this.decrementOutstanding();
            }
        };
        outstanding.incrementAndGet();
        try {
            as.ncreate(UID.ModalityPerformedProcedureStepSOPClass, null, mpps, null, rspHandler);
        } catch (IOException e) {
            outstanding.decrementAndGet();
            throw e;
        }
    }

    private void onNCreateRSP(Attributes rsp, Attributes data, Attributes finalMpps) {
        int status = rsp.getInt(Tag.Status, -1);
        switch(status) {
        case Status.Success:
        case Status.AttributeListError:
        case Status.AttributeValueOutOfRange:
            try {
                DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {
                
                    @Override
                    public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                        super.onDimseRSP(as, cmd, data);
                        decrementOutstanding();
                    }

                    @Override
                    public void onClose(Association as) {
                        super.onClose(as);
                        decrementOutstanding();
                    }
                };
                as.nset(UID.ModalityPerformedProcedureStepSOPClass,
                        rsp.getString(Tag.AffectedSOPInstanceUID),
                        finalMpps, null, rspHandler);
            } catch (Exception e) {
                e.printStackTrace();
                decrementOutstanding();
            }
            break;
        default:
            decrementOutstanding();
        }
    }

    private void decrementOutstanding() {
        if (outstanding.decrementAndGet() <= 0)
            synchronized (outstanding) {
                outstanding.notify();
            }
    }

    public void scanFile(File f) {
        if (f.isDirectory()) {
            for (String s : f.list())
                scanFile(new File(f, s));
            return;
        }
        DicomInputStream in = null;
        try {
            in = new DicomInputStream(f);
            in.setIncludeBulkData(false);
            addInstance(in.readDataset(-1, Tag.PixelData));
            System.out.print('.');
        } catch (IOException e) {
            System.out.print('E');
            e.printStackTrace();
        } finally {
            SafeClose.close(in);
        }
    }

    public void addInstance(Attributes inst) {
        String suid = inst.getString(Tag.StudyInstanceUID);
        if (suid == null)
            return;
        Attributes mpps = map.get(inst.getString(Tag.StudyInstanceUID));
        if (mpps == null)
            map.put(suid, mpps = createMPPS(inst));
        updateMPPS(mpps, inst);
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
        mpps.setString(Tag.PerformedProcedureStepStatus, VR.CS, IN_PROGRESS);

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
        return prefSeries ;
    }
}
