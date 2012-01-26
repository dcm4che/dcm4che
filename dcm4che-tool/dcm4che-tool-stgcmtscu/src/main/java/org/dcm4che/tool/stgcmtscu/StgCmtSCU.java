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

package org.dcm4che.tool.stgcmtscu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.dcm4che.data.Attributes;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Association;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.DimseRSPHandler;
import org.dcm4che.net.IncompatibleConnectionException;
import org.dcm4che.net.Status;
import org.dcm4che.net.TransferCapability;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.net.service.BasicNEventReportSCU;
import org.dcm4che.net.service.DicomServiceRegistry;
import org.dcm4che.tool.common.CLIUtils;
import org.dcm4che.util.SafeClose;
import org.dcm4che.util.StringUtils;
import org.dcm4che.util.UIDUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class StgCmtSCU extends Device {

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che.tool.stgcmtscu.messages");

    private final ApplicationEntity ae = new ApplicationEntity("STGCMTSCU");
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();

    private int iuidFieldIndex = 0;
    private int cuidFieldIndex = 1;
    private char fieldDelim = '\t';
    private boolean keepAlive;

    private Attributes actionInfo = new Attributes();

    private Association as;

    private final HashSet<String> outstandingResults = new HashSet<String>(2);
    private final BasicNEventReportSCU stgcmtResultHandler =
            new BasicNEventReportSCU(UID.StorageCommitmentPushModelSOPClass) {

        @Override
        protected void postNEventReportRSP(Association as, int eventTypeID,
                Attributes eventInfo, Attributes rsp, Object handback) {
            removeOutstandingResult(eventInfo.getString(Tag.TransactionUID));
        }
    };

    public StgCmtSCU() throws IOException {
        super("stgcmtscu");
        addConnection(conn);
        addApplicationEntity(ae);
        ae.addConnection(conn);
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(
                stgcmtResultHandler);
        ae.setDimseRQHandler(serviceRegistry);
        actionInfo.newSequence(Tag.ReferencedSOPSequence, 10);
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            StgCmtSCU stgcmtscu = new StgCmtSCU();
            CLIUtils.configureConnect(stgcmtscu.remote, stgcmtscu.rq, cl);
            CLIUtils.configureBind(stgcmtscu.conn, stgcmtscu.ae, cl);
            CLIUtils.configure(stgcmtscu.conn, stgcmtscu.ae, cl);
            stgcmtscu.remote.setTlsProtocols(stgcmtscu.conn.getTlsProtocols());
            stgcmtscu.remote.setTlsCipherSuites(stgcmtscu.conn.getTlsCipherSuites());
            stgcmtscu.setTransferSyntaxes(CLIUtils.transferSyntaxesOf(cl));
            configureKeepAlive(stgcmtscu, cl);
            configureRequest(stgcmtscu, cl);
            ExecutorService executorService =
                    Executors.newCachedThreadPool();
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newSingleThreadScheduledExecutor();
            stgcmtscu.setExecutor(executorService);
            stgcmtscu.setScheduledExecutor(scheduledExecutorService);
            stgcmtscu.activate();
            try {
                stgcmtscu.open();
                @SuppressWarnings("unchecked")
                List<String> argList = cl.getArgList();
                if (argList.isEmpty())
                    stgcmtscu.sendRequest();
                else
                    for (String arg : argList)
                        stgcmtscu.sendRequest(new File(arg));
            } finally {
                stgcmtscu.close();
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

    private static void configureKeepAlive(StgCmtSCU stgcmtscu, CommandLine cl) {
        stgcmtscu.setKeepAlive(cl.hasOption("keep-alive"));
    }

    private static void configureRequest(StgCmtSCU stgcmtscu, CommandLine cl) {
        if (cl.hasOption("cvs-delimiter"))
            stgcmtscu.setFieldDelimiter(
                    cl.getOptionValue("cvs-delimiter").charAt(0));
        if (cl.hasOption("cvs-iuid-field"))
            stgcmtscu.setIuidField(
                    Integer.parseInt(cl.getOptionValue("cvs-iuid-field")));
        if (cl.hasOption("cvs-cuid-field"))
            stgcmtscu.setCuidField(
                    Integer.parseInt(cl.getOptionValue("cvs-cuid-field")));
        String[] fsRefs = cl.getOptionValues("ref-fs");
        if (fsRefs != null && fsRefs.length == 2) {
            stgcmtscu.setFileSetRef(fsRefs[0], fsRefs[1]);
            fsRefs = null;
        }
        String[] sopRefs = cl.getOptionValues("r");
        if (sopRefs != null) {
            for (int i = 1; i < sopRefs.length; i++, i++) {
                if (fsRefs == null || fsRefs.length <= i)
                    stgcmtscu.addSOPRef(sopRefs[i - 1], sopRefs[i], null, null);
                else
                    stgcmtscu.addSOPRef(sopRefs[i - 1], sopRefs[i], fsRefs[i - 1], fsRefs[i]);
            }
        }
    }

    public void addSOPRef(String iuid, String cuid, String fsid, String fsuid) {
        actionInfo.getSequence(Tag.ReferencedSOPSequence)
            .add(newRefSOP(iuid, cuid, fsid, fsuid));
    }

    private static Attributes newRefSOP(String iuid, String cuid, String fsid, String fsuid) {
        Attributes attrs = new Attributes(4);
        attrs.setString(Tag.ReferencedSOPClassUID, VR.UI, cuid);
        attrs.setString(Tag.ReferencedSOPInstanceUID, VR.UI, iuid);
        addFileSetRef(attrs, fsid, fsuid);
        return attrs;
    }

    public void setFileSetRef(String fsid, String fsuid) {
        addFileSetRef(actionInfo, fsid, fsuid);
    }

    private static void addFileSetRef(Attributes attrs, String fsid, String fsuid) {
        if (fsid != null)
            attrs.setString(Tag.StorageMediaFileSetID, VR.SH, fsid);
        if (fsuid != null)
            attrs.setString(Tag.StorageMediaFileSetUID, VR.UI, fsuid);
    }

    public void setFieldDelimiter(char c) {
        fieldDelim = c;
    }

    public void setIuidField(int i) {
        if (i <= 0) 
            throw new IllegalArgumentException(rb.getString("invalid-field"));
       iuidFieldIndex = i - 1;
    }

    public void setCuidField(int i) {
        if (i <= 0) 
            throw new IllegalArgumentException(rb.getString("invalid-field"));
       cuidFieldIndex = i - 1;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setTransferSyntaxes(String[] tss) {
        rq.addPresentationContext(
                new PresentationContext(1,
                        UID.StorageCommitmentPushModelSOPClass,
                        tss));
        ae.addTransferCapability(
                new TransferCapability(null,
                        UID.StorageCommitmentPushModelSOPClass,
                        TransferCapability.Role.SCU,
                        tss));
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        addKeepAliveOption(opts);
        addRequestOption(opts);
        CLIUtils.addTransferSyntaxOptions(opts);
        CLIUtils.addConnectOption(opts);
        CLIUtils.addBindOption(opts, "STGCMTSCU");
        CLIUtils.addAEOptions(opts, true, true);
        CLIUtils.addNActionRspOption(opts);
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, StgCmtSCU.class);
    }

    private static void addKeepAliveOption(Options opts) {
        opts.addOption(null, "keep-alive", false, rb.getString("keep-alive"));
    }

    @SuppressWarnings("static-access")
    private static void addRequestOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("delim")
                .withDescription(rb.getString("cvs-delimiter"))
                .withLongOpt("cvs-delimiter")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("field")
                .withDescription(rb.getString("cvs-iuid-field"))
                .withLongOpt("cvs-iuid-field")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("field")
                .withDescription(rb.getString("cvs-cuid-field"))
                .withLongOpt("cvs-cuid-field")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArgs()
                .withArgName("iuid:cuid")
                .withValueSeparator(':')
                .withDescription(rb.getString("ref-sop"))
                .withLongOpt("ref-sop")
                .create("r"));
        opts.addOption(OptionBuilder
                .hasArgs()
                .withArgName("id:iuid")
                .withValueSeparator(':')
                .withDescription(rb.getString("ref-fs"))
                .withLongOpt("ref-fs")
                .create(null));
   }

    public void open() throws IOException, InterruptedException,
            IncompatibleConnectionException {
        as = ae.connect(conn, remote, rq);
    }

    public void close() throws IOException, InterruptedException {
        if (as != null && as.isReadyForDataTransfer()) {
            as.waitForOutstandingRSP();
            if (keepAlive)
                waitForOutstandingResults();
            as.release();
        }
        if (conn.isListening()) {
            waitForOutstandingResults();
            waitForNoOpenConnections();
            deactivate();
        }
    }

    private void addOutstandingResult(String tuid) {
        synchronized (outstandingResults ) {
            outstandingResults.add(tuid);
        }
    }

    private void removeOutstandingResult(String tuid) {
        synchronized (outstandingResults ) {
            outstandingResults.remove(tuid);
            outstandingResults.notify();
        }
    }

    private void waitForOutstandingResults() throws InterruptedException {
        synchronized (outstandingResults) {
            while (!outstandingResults.isEmpty()) {
                BasicNEventReportSCU.LOG.info(
                        rb.getString("wait-for-results"),
                        outstandingResults.size());
                outstandingResults.wait();
            }
        }
    }

    public void sendRequest(File file) throws IOException, InterruptedException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file)));
        try {
            String line;
            Sequence refSopSeq = actionInfo.newSequence(Tag.ReferencedSOPSequence, 10);
            while ((line = in.readLine()) != null) {
                String[] fields = StringUtils.split(line, fieldDelim);
                if (iuidFieldIndex < fields.length
                        && cuidFieldIndex < fields.length)
                    refSopSeq.add(newRefSOP(
                            fields[iuidFieldIndex],
                            fields[cuidFieldIndex],
                            null, null));
            }
        } finally {
            SafeClose.close(in);
        }
        sendRequest();
    }

    public void sendRequest() throws IOException, InterruptedException {
        final String tuid = UIDUtils.createUID();
        actionInfo.setString(Tag.TransactionUID, VR.UI, tuid);
        DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                if (cmd.getInt(Tag.Status, -1) != Status.Success)
                    removeOutstandingResult(tuid);
                super.onDimseRSP(as, cmd, data);
            }
        };

        as.naction(UID.StorageCommitmentPushModelSOPClass,
                UID.StorageCommitmentPushModelSOPInstance,
                1, actionInfo, null, rspHandler);
        addOutstandingResult(tuid);
    }

}
