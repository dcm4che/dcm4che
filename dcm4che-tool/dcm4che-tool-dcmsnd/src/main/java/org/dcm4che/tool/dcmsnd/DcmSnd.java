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

package org.dcm4che.tool.dcmsnd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Association;
import org.dcm4che.net.Connection;
import org.dcm4che.net.DataWriter;
import org.dcm4che.net.Device;
import org.dcm4che.net.DimseRSPHandler;
import org.dcm4che.net.PDVOutputStream;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.tool.common.CLIUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class DcmSnd {

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che.tool.dcmsnd.dcmsnd");

    private final Device device = new Device("dcmsnd");
    private final ApplicationEntity ae = new ApplicationEntity("DCMSND");
    private final Connection conn = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private String host = "localhost";
    private int port = 104;
    private String tmpPrefix = "dcmsnd-";
    private String tmpSuffix;
    private File tmpDir;
    private File tmpFile;
    private Association as;

    private int priority;

    private long totalSize;
    private int filesScanned;
    private int filesSent;

    public DcmSnd() {
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
    }

    public void setCallingAET(String aet) {
        ae.setAETitle(aet);
    }

    public void setCalledAET(String aet) {
        rq.setCalledAET(aet);
    }

    public void setRemoteHost(String host) {
        this.host = host;
    }

    public void setRemotePort(int port) {
        this.port = port;
    }

    public void setLocalHost(String hostname) {
        conn.setHostname(hostname);
    }

    public void setMaxOpsInvoked(int maxOpsInvoked) {
        ae.setMaxOpsInvoked(maxOpsInvoked);
    }

    public void setMaxOpsPerformed(int maxOpsPerformed) {
        ae.setMaxOpsPerformed(maxOpsPerformed);
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        CLIUtils.addAEOptions(opts);
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, DcmSnd.class);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        long t1, t2;
        try {
            CommandLine cl = parseComandLine(args);
            DcmSnd dcmsnd = new DcmSnd();
            List<String> argList = cl.getArgList();
            if (argList.isEmpty())
                throw new ParseException(rb.getString("missing"));
            String remoteAE = argList.get(0);
            String[] calledAETAddress = split(remoteAE, '@');
            dcmsnd.setCalledAET(calledAETAddress[0]);
            if (calledAETAddress[1] == null) {
                dcmsnd.setRemoteHost("127.0.0.1");
                dcmsnd.setRemotePort(104);
            } else {
                String[] hostPort = split(calledAETAddress[1], ':');
                dcmsnd.setRemoteHost(hostPort[0]);
                dcmsnd.setRemotePort((hostPort[1] != null 
                                        ? Integer.parseInt(hostPort[1])
                                        : 104));
            }
            if (cl.hasOption("L")) {
                String localAE = cl.getOptionValue("L");
                String[] callingAETHost = split(localAE, '@');
                dcmsnd.setCallingAET(callingAETHost[0]);
                if (callingAETHost[1] != null) {
                    dcmsnd.setLocalHost(callingAETHost[1]);
                }
            }
            CLIUtils.configure(dcmsnd.ae, cl);
            int nArgs = argList.size();
            boolean echo = nArgs == 1;
            if (!echo) {
                System.out.println(rb.getString("scanning"));
                t1 = System.currentTimeMillis();
                dcmsnd.scanFiles(argList.subList(1, nArgs));
                t2 = System.currentTimeMillis();
                int n = dcmsnd.getNumberOfScannedFiles();
                System.out.printf(rb.getString("scanned"), n,
                        (t2 - t1) / 1000F, (t2 - t1) / n);
            }
            ExecutorService executorService =
                    Executors.newSingleThreadExecutor();
            try {
                t1 = System.currentTimeMillis();
                dcmsnd.open(executorService);
                t2 = System.currentTimeMillis();
                System.out.printf(rb.getString("connected"), remoteAE,
                        (t2 - t1) / 1000F);
                if (echo)
                    dcmsnd.echo();
                else {
                    t1 = System.currentTimeMillis();
                    dcmsnd.sendFiles();
                    t2 = System.currentTimeMillis();
                }
            } finally {
                dcmsnd.close();
                executorService.shutdown();
            }
            if (dcmsnd.getNumberOfScannedFiles() > 0) {
                float s = (t2 - t1) / 1000F;
                float mb = dcmsnd.getTotalSizeSent() / 1048576F;
                System.out.printf(rb.getString("sent"),
                        dcmsnd.getNumberOfFilesSent(),
                        mb, s, mb / s);
            }
        } catch (ParseException e) {
            System.err.println("dcmsnd: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("dcmsnd: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    public final int getNumberOfScannedFiles() {
        return filesScanned;
    }

    public final int getNumberOfFilesSent() {
        return filesSent;
    }

    public final long getTotalSizeSent() {
        return totalSize;
    }

    public void scanFiles(List<String> fnames) throws IOException {
        tmpFile = File.createTempFile(tmpPrefix, tmpSuffix, tmpDir);
        BufferedWriter sendList = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(tmpFile)));
        try {
            for (String fname : fnames)
                scanFile(sendList, new File(fname));
        } finally {
            try { sendList.close(); } catch (IOException ignore) {}
        }
    }

    public void sendFiles() throws IOException {
        BufferedReader sendList = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(tmpFile)));
        try {
            while (as.isReadyForDataTransfer()) {
                String cuid = sendList.readLine();
                if (cuid == null) {
                    try {
                        as.waitForOutstandingRSP();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                String iuid = sendList.readLine();
                String ts = sendList.readLine();
                String fpath = sendList.readLine();
                try {
                    send(new File(fpath), cuid, iuid, ts);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }            
        } finally {
            try { sendList.close(); } catch (IOException ignore) {}
            tmpFile.delete();
        }
    }

    private void scanFile(BufferedWriter sendList, File f) {
        if (f.isDirectory()) {
            for (String s : f.list())
                scanFile(sendList, new File(f, s));
            return;
        }
        DicomInputStream in = null;
        try {
            in = new DicomInputStream(f);
            Attributes fmi = in.readFileMetaInformation();
            if (fmi != null) {
                addFile(sendList, f,
                        fmi.getString(Tag.MediaStorageSOPClassUID, null),
                        fmi.getString(Tag.MediaStorageSOPInstanceUID, null),
                        fmi.getString(Tag.TransferSyntaxUID, null));
            } else {
                Attributes ds = in.readDataset(-1, Tag.SOPInstanceUID);
                addFile(sendList, f,
                        ds.getString(Tag.SOPClassUID, null),
                        ds.getString(Tag.SOPInstanceUID, null),
                        in.explicitVR() 
                                ? in.bigEndian()
                                        ? UID.ExplicitVRBigEndian
                                        : UID.ExplicitVRLittleEndian
                                : UID.ImplicitVRLittleEndian);
            }
            filesScanned++;
            System.out.print('.');
        } catch (IOException e) {
            System.out.print('E');
            e.printStackTrace();
        } finally {
            if (in != null)
                try { in.close(); } catch (IOException ignore) {}
        }
    }

    private void addFile(BufferedWriter sendList, File f, String cuid,
            String iuid, String ts) throws IOException {
        sendList.append(cuid);
        sendList.newLine();
        sendList.append(iuid);
        sendList.newLine();
        sendList.append(ts);
        sendList.newLine();
        sendList.append(f.getPath());
        sendList.newLine();

        boolean firstPCforCUID = true;
        for (PresentationContext pc : rq.getPresentationContexts())
            if (cuid.equals(pc.getAbstractSyntax())) {
                firstPCforCUID = false;
                if (ts.equals(pc.getTransferSyntax()))
                    return;
            }

        if (firstPCforCUID && !ts.equals(UID.ImplicitVRLittleEndian))
            rq.addPresentationContext(
                    new PresentationContext(
                            rq.getNumberOfPresentationContexts() * 2 + 1,
                            cuid, UID.ImplicitVRLittleEndian));

        rq.addPresentationContext(
                new PresentationContext(
                        rq.getNumberOfPresentationContexts() * 2 + 1,
                        cuid, ts));
    }

    public void echo() throws IOException, InterruptedException {
        as.cecho().next();
    }

    public void send(final File f, String cuid, String iuid, String ts)
            throws IOException, InterruptedException {
        final DicomInputStream in = new DicomInputStream(f);
        in.readFileMetaInformation();
        DataWriter data = new DataWriter() {

            @Override
            public void writeTo(PDVOutputStream out, String tsuid)
                    throws IOException {
                out.copyFrom(in);
            }
        };

        DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd,
                    Attributes data) {
                DcmSnd.this.onCStoreRSP(cmd, f);
            }
        };

        as.cstore(cuid, iuid, priority, data , ts, rspHandler);
    }

    public void close() throws IOException, InterruptedException {
        if (as != null) {
            as.waitForOutstandingRSP();
            as.release();
        }
    }

    public void open(Executor executor)
            throws IOException, InterruptedException {
        device.setExecutor(executor);
        if (rq.getNumberOfPresentationContexts() == 0)
            rq.addPresentationContext(
                    new PresentationContext(1, UID.VerificationSOPClass,
                            UID.ImplicitVRLittleEndian));
        as = ae.connect(conn, host, port, rq);
    }

    private static String[] split(String s, char delim) {
        String[] s2 = { s, null };
        int pos = s.indexOf(delim);
        if (pos != -1) {
            s2[0] = s.substring(0, pos);
            s2[1] = s.substring(pos + 1);
        }
        return s2;
    }

    private void onCStoreRSP(Attributes cmd, File f) {
        int status = cmd.getInt(Tag.Status, -1);
        switch (status) {
        case 0:
            totalSize += f.length();
            ++filesSent;
            System.out.print('.');
            break;
        case 0xB000:
        case 0xB006:
        case 0xB007:
            totalSize += f.length();
            ++filesSent;
            System.err.format(rb.getString("warning"), status, f);
            System.err.println(cmd);
            break;
        default:
            System.out.print('E');
            System.err.format(rb.getString("error"), status, f);
            System.err.println(cmd);
        }
    }
}
