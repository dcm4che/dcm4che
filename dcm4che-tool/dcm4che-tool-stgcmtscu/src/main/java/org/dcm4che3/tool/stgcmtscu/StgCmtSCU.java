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

package org.dcm4che3.tool.stgcmtscu;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.AssociationStateException;
import org.dcm4che3.net.Commands;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.AbstractDicomService;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.DicomService;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.common.DicomFiles;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class StgCmtSCU {

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.stgcmtscu.messages");
    
    private static final Logger LOG = LoggerFactory.getLogger(StgCmtSCU.class);

    private final ApplicationEntity ae;
    private final Connection remote;
    private final AAssociateRQ rq = new AAssociateRQ();
    private Attributes attrs;
    private String uidSuffix;
    private File storageDir;
    private boolean keepAlive;
    private int splitTag;
    private int status;
    private HashMap<String,List<String>> map = new HashMap<String,List<String>>();
    private Association as;

    private final HashSet<String> outstandingResults = new HashSet<String>(2);
    private final DicomService stgcmtResultHandler =
            new AbstractDicomService(UID.StorageCommitmentPushModelSOPClass) {

             @Override
             public void onDimseRQ(Association as, PresentationContext pc,
                     Dimse dimse, Attributes cmd, Attributes data)
                     throws IOException {
                 if (dimse != Dimse.N_EVENT_REPORT_RQ)
                     throw new DicomServiceException(Status.UnrecognizedOperation);

                 int eventTypeID = cmd.getInt(Tag.EventTypeID, 0);
                 if (eventTypeID != 1 && eventTypeID != 2) 
                     throw new DicomServiceException(Status.NoSuchEventType)
                                 .setEventTypeID(eventTypeID);
                 String tuid = data.getString(Tag.TransactionUID);
                 try {
                     Attributes rsp = Commands.mkNEventReportRSP(cmd, status);
                     Attributes rspAttrs = StgCmtSCU.this.eventRecord(as, cmd, data);
                     as.writeDimseRSP(pc, rsp, rspAttrs);
                 } catch (AssociationStateException e) {
                     LOG.warn("{} << N-EVENT-RECORD-RSP failed: {}", as, e.getMessage());
                 } finally {
                     removeOutstandingResult(tuid);
                 }
             }
    };

    public StgCmtSCU(ApplicationEntity ae) throws IOException {
        this.remote = new Connection();
        this.ae = ae;
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        serviceRegistry.addDicomService(stgcmtResultHandler);
        ae.setDimseRQHandler(serviceRegistry);
    }

    public StgCmtSCU(ApplicationEntity ae, DicomService stgCmtResultHndlr ) throws IOException {
        this.remote = new Connection();
        this.ae = ae;
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        serviceRegistry.addDicomService(stgCmtResultHndlr);
        ae.setDimseRQHandler(serviceRegistry);
    }

    public Connection getRemoteConnection() {
        return remote;
    }

    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    public void setStorageDirectory(File storageDir) {
        if (storageDir != null)
            storageDir.mkdirs();
        this.storageDir = storageDir;
    }

    public File getStorageDirectory() {
        return storageDir;
    }

    public final void setUIDSuffix(String uidSuffix) {
        this.uidSuffix = uidSuffix;
    }

    public void setAttributes(Attributes attrs) {
        this.attrs = attrs;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Device device = new Device("stgcmtscu");
            Connection conn = new Connection();
            device.addConnection(conn);
            ApplicationEntity ae = new ApplicationEntity("STGCMTSCU");
            device.addApplicationEntity(ae);
            ae.addConnection(conn);
            final StgCmtSCU stgcmtscu = new StgCmtSCU(ae);
            CLIUtils.configureConnect(stgcmtscu.remote, stgcmtscu.rq, cl);
            CLIUtils.configureBind(conn, stgcmtscu.ae, cl);
            CLIUtils.configure(conn, cl);
            stgcmtscu.remote.setTlsProtocols(conn.getTlsProtocols());
            stgcmtscu.remote.setTlsCipherSuites(conn.getTlsCipherSuites());
            stgcmtscu.setTransferSyntaxes(CLIUtils.transferSyntaxesOf(cl));
            stgcmtscu.setStatus(CLIUtils.getIntOption(cl, "status", 0));
            stgcmtscu.setSplitTag(getSplitTag(cl));
            stgcmtscu.setKeepAlive(cl.hasOption("keep-alive"));
            stgcmtscu.setStorageDirectory(getStorageDirectory(cl));
            stgcmtscu.setAttributes(new Attributes());
            CLIUtils.addAttributes(stgcmtscu.attrs, cl.getOptionValues("s"));
            stgcmtscu.setUIDSuffix(cl.getOptionValue("uid-suffix"));
            List<String> argList = cl.getArgList();
            boolean echo = argList.isEmpty();
            if (!echo) {
                LOG.info(rb.getString("scanning"));
                DicomFiles.scan(argList, new DicomFiles.Callback() {
                    
                    @Override
                    public boolean dicomFile(File f, Attributes fmi, long dsPos,
                            Attributes ds) {
                        return stgcmtscu.addInstance(ds);
                    }
                });
            }
            ExecutorService executorService =
                    Executors.newCachedThreadPool();
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newSingleThreadScheduledExecutor();
            device.setExecutor(executorService);
            device.setScheduledExecutor(scheduledExecutorService);
            device.bindConnections();
            try {
                stgcmtscu.open();
                if (echo)
                    stgcmtscu.echo();
                else
                    stgcmtscu.sendRequests();
             } finally {
                stgcmtscu.close();
                if (conn.isListening()) {
                    device.waitForNoOpenConnections();
                    device.unbindConnections();
                }
                executorService.shutdown();
                scheduledExecutorService.shutdown();
            }
        } catch (ParseException e) {
            System.err.println("stgcmtscu: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("stgcmtscu: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    public static File getStorageDirectory(CommandLine cl) {
        return cl.hasOption("ignore")
                ? null
                : new File(cl.getOptionValue("directory", "."));
    }

    public static int getSplitTag(CommandLine cl) {
        return cl.hasOption("one-by-study") 
                ? Tag.StudyInstanceUID
                : cl.hasOption("one-by-series")
                        ? Tag.SeriesInstanceUID
                        : 0;
    }

    public void setSplitTag(int splitTag) {
        this.splitTag = splitTag;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setTransferSyntaxes(String[] tss) {
        rq.addPresentationContext(
                new PresentationContext(1, UID.VerificationSOPClass,
                        UID.ImplicitVRLittleEndian));
        rq.addPresentationContext(
                new PresentationContext(2,
                        UID.StorageCommitmentPushModelSOPClass,
                        tss));
        ae.addTransferCapability(
                new TransferCapability(null,
                        UID.VerificationSOPClass,
                        TransferCapability.Role.SCP,
                        UID.ImplicitVRLittleEndian));
        ae.addTransferCapability(
                new TransferCapability(null,
                        UID.StorageCommitmentPushModelSOPClass,
                        TransferCapability.Role.SCU,
                        tss));
    }

    public boolean addInstance(Attributes inst) {
        CLIUtils.updateAttributes(inst, attrs, uidSuffix);
        String cuid = inst.getString(Tag.SOPClassUID);
        String iuid = inst.getString(Tag.SOPInstanceUID);
        String splitkey = splitTag != 0 ? inst.getString(splitTag) : "";
        if (cuid == null || iuid == null || splitkey == null)
            return false;

        List<String> refSOPs = map.get(splitkey);
        if (refSOPs == null)
            map.put(splitkey, refSOPs = new ArrayList<String>());

        refSOPs.add(cuid);
        refSOPs.add(iuid);
        return true;
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        CLIUtils.addTransferSyntaxOptions(opts);
        CLIUtils.addConnectOption(opts);
        CLIUtils.addBindOption(opts, "STGCMTSCU");
        CLIUtils.addRequestTimeoutOption(opts);
        CLIUtils.addAEOptions(opts);
        CLIUtils.addResponseTimeoutOption(opts);
        CLIUtils.addCommonOptions(opts);
        addStgCmtOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, StgCmtSCU.class);
    }

    @SuppressWarnings("static-access")
    public static void addStgCmtOptions(Options opts) {
        opts.addOption(null, "ignore", false,
                rb.getString("ignore"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("path")
                .withDescription(rb.getString("directory"))
                .withLongOpt("directory")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("code")
                .withDescription(rb.getString("status"))
                .withLongOpt("status")
                .create(null));
        opts.addOption(null, "keep-alive", false, rb.getString("keep-alive"));
        opts.addOption(null, "one-per-study", false, rb.getString("one-per-study"));
        opts.addOption(null, "one-per-series", false, rb.getString("one-per-series"));
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

    public void echo() throws IOException, InterruptedException {
        as.cecho().next();
    }

    public void close() throws IOException, InterruptedException {
        if (as != null) {
            if (as.isReadyForDataTransfer()) {
                as.waitForOutstandingRSP();
                if (keepAlive)
                    waitForOutstandingResults(as);
                as.release();
            }
            as.waitForSocketClose();
        }
        waitForOutstandingResults(as);
    }

    public void addOutstandingResult(String tuid) {
        synchronized (outstandingResults ) {
            outstandingResults.add(tuid);
        }
    }

    public void removeOutstandingResult(String tuid) {
        synchronized (outstandingResults ) {
            outstandingResults.remove(tuid);
            outstandingResults.notify();
        }
    }

    private void waitForOutstandingResults(Association as) throws InterruptedException {
        synchronized (outstandingResults) {

            int requestTimeout = as.getConnection().getRequestTimeout();
            long started = System.currentTimeMillis();

            int lastSize = -1;
            while (!outstandingResults.isEmpty()) {
                if (outstandingResults.size() != lastSize) {
                    lastSize = outstandingResults.size();
                    System.out.println(MessageFormat.format(rb.getString("wait-for-results"),outstandingResults.size()));
                }

                outstandingResults.wait(100);

                // timeout 10 sec
                if (requestTimeout > 0 && System.currentTimeMillis() - started > requestTimeout)
                    throw new RuntimeException("Timeout (10 sec) while waiting for storage commitment");
            }
        }
    }

    public Attributes makeActionInfo(List<String> refSOPs) {
        Attributes actionInfo = new Attributes(2);
        actionInfo.setString(Tag.TransactionUID, VR.UI, UIDUtils.createUID());
        int n = refSOPs.size() / 2;
        Sequence refSOPSeq = actionInfo.newSequence(Tag.ReferencedSOPSequence, n);
        for (int i = 0, j = 0; j < n; j++) {
            Attributes refSOP = new Attributes(2);
            refSOP.setString(Tag.ReferencedSOPClassUID, VR.UI, refSOPs.get(i++));
            refSOP.setString(Tag.ReferencedSOPInstanceUID, VR.UI, refSOPs.get(i++));
            refSOPSeq.add(refSOP);
        }
        return actionInfo;
    }

    public void sendRequests() throws IOException, InterruptedException {
        for (List<String> refSOPs : map.values())
            sendRequest(makeActionInfo(refSOPs));
    }

    private void sendRequest(Attributes actionInfo) throws IOException, InterruptedException {
        final String tuid = actionInfo.getString(Tag.TransactionUID);
        DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                if (cmd.getInt(Tag.Status, -1) != Status.Success)
                    removeOutstandingResult(tuid );
                super.onDimseRSP(as, cmd, data);
            }
        };

        as.naction(UID.StorageCommitmentPushModelSOPClass,
                UID.StorageCommitmentPushModelSOPInstance,
                1, actionInfo, null, rspHandler);
        addOutstandingResult(tuid);
    }

    private Attributes eventRecord(Association as, Attributes cmd, Attributes eventInfo)
            throws DicomServiceException {
        if (storageDir == null)
            return null;

        String cuid = cmd.getString(Tag.AffectedSOPClassUID);
        String iuid = cmd.getString(Tag.AffectedSOPInstanceUID);
        String tuid = eventInfo.getString(Tag.TransactionUID);
        File file = new File(storageDir, tuid );
        DicomOutputStream out = null;
        LOG.info("{}: M-WRITE {}", as, file);
        try {
            out = new DicomOutputStream(file);
            out.writeDataset(
                    Attributes.createFileMetaInformation(iuid, cuid,
                            UID.ExplicitVRLittleEndian),
                    eventInfo);
        } catch (IOException e) {
            LOG.warn(as + ": Failed to store Storage Commitment Result:", e);
            throw new DicomServiceException(Status.ProcessingFailure, e);
        } finally {
            SafeClose.close(out);
        }
        return null;
    }
}
