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

package org.dcm4che.tool.storescu;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Association;
import org.dcm4che.net.Connection;
import org.dcm4che.net.DataWriter;
import org.dcm4che.net.Device;
import org.dcm4che.net.DimseRSPHandler;
import org.dcm4che.net.IncompatibleConnectionException;
import org.dcm4che.net.InputStreamDataWriter;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.tool.common.CLIUtils;
import org.dcm4che.util.SafeClose;
import org.dcm4che.util.TagUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Main {

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che.tool.storescu.messages");

    private final Device device = new Device("storescu");
    private final ApplicationEntity ae = new ApplicationEntity("STORESCU");
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private final RelatedGeneralSOPClasses relSOPClasses =
            new RelatedGeneralSOPClasses();
    private boolean relExtNeg;
    private int priority;
    private String tmpPrefix = "storescu-";
    private String tmpSuffix;
    private File tmpDir;
    private File tmpFile;
    private Association as;

    private long totalSize;
    private int filesScanned;
    private int filesSent;

    public Main() throws IOException, KeyManagementException {
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
    }

    public void setScheduledExecutorService(ScheduledExecutorService service) {
        device.setScheduledExecutor(service);
    }

    public void setExecutor(Executor executor) {
        device.setExecutor(executor);
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        CLIUtils.addConnectOption(opts);
        CLIUtils.addBindOption(opts, "STORESCU");
        CLIUtils.addAEOptions(opts, true, false);
        CLIUtils.addCEchoRspOption(opts);
        CLIUtils.addCStoreRspOption(opts);
        CLIUtils.addPriorityOption(opts);
        CLIUtils.addCommonOptions(opts);
        addRelatedSOPClassOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, Main.class);
    }

    @SuppressWarnings("static-access")
    private static void addRelatedSOPClassOptions(Options opts) {
        opts.addOption(null, "rel-ext-neg", false,
                rb.getString("rel-ext-neg"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("rel-sop-classes"))
                .withLongOpt("rel-sop-classes")
                .create(null));
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        long t1, t2;
        try {
            CommandLine cl = parseComandLine(args);
            Main main = new Main();
            CLIUtils.configureConnect(main.remote, main.rq, cl);
            CLIUtils.configureBind(main.conn, main.ae, cl);
            CLIUtils.configure(main.conn, main.ae, cl);
            main.remote.setTlsProtocols(main.conn.getTlsProtocols());
            main.remote.setTlsCipherSuites(main.conn.getTlsCipherSuites());
            configureRelatedSOPClass(main, cl);
            main.setPriority(CLIUtils.priorityOf(cl));
            List<String> argList = cl.getArgList();
            boolean echo = argList.isEmpty();
            if (!echo) {
                System.out.println(rb.getString("scanning"));
                t1 = System.currentTimeMillis();
                main.scanFiles(argList);
                t2 = System.currentTimeMillis();
                int n = main.filesScanned;
                System.out.println();
                System.out.println(MessageFormat.format(
                        rb.getString("scanned"), n,
                        (t2 - t1) / 1000F, (t2 - t1) / n));
            }
            ExecutorService executorService =
                    Executors.newSingleThreadExecutor();
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newSingleThreadScheduledExecutor();
            main.setExecutor(executorService);
            main.setScheduledExecutorService(scheduledExecutorService);
            try {
                t1 = System.currentTimeMillis();
                main.open();
                t2 = System.currentTimeMillis();
                System.out.println(MessageFormat.format(
                        rb.getString("connected"),
                        main.as.getRemoteAET(), t2 - t1));
                if (echo)
                    main.echo();
                else {
                    t1 = System.currentTimeMillis();
                    main.sendFiles();
                    t2 = System.currentTimeMillis();
                }
            } finally {
                main.close();
                executorService.shutdown();
                scheduledExecutorService.shutdown();
            }
            if (main.filesScanned > 0) {
                float s = (t2 - t1) / 1000F;
                float mb = main.totalSize / 1048576F;
                System.out.println(MessageFormat.format(rb.getString("sent"),
                        main.filesSent,
                        mb, s, mb / s));
            }
        } catch (ParseException e) {
            System.err.println("storescu: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("storescu: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void configureRelatedSOPClass(Main storescu, CommandLine cl)
            throws IOException {
        if (cl.hasOption("rel-ext-neg")) {
            storescu.enableSOPClassRelationshipExtNeg(true);
            Properties p = new Properties();
            CLIUtils.loadProperties(cl.hasOption("rel-sop-classes")
                    ? cl.getOptionValue("rel-ext-neg")
                    : "resource:rel-sop-classes.properties",
                    p);
            storescu.relSOPClasses.init(p);
        }
    }

    public final void enableSOPClassRelationshipExtNeg(boolean enable) {
        relExtNeg = enable;
    }

    public void scanFiles(List<String> fnames) throws IOException {
        tmpFile = File.createTempFile(tmpPrefix, tmpSuffix, tmpDir);
        DataOutputStream fileInfos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(tmpFile)));
        try {
            for (String fname : fnames)
                scanFile(fileInfos, new File(fname));
        } finally {
            SafeClose.close(fileInfos);
        }
    }

    public void sendFiles() throws IOException {
        DataInputStream fileInfos = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(tmpFile)));
        try {
            while (as.isReadyForDataTransfer()) {
                String fpath = fileInfos.readUTF();
                long fmiEndPos = fileInfos.readLong();
                String cuid = fileInfos.readUTF();
                String iuid = fileInfos.readUTF();
                String ts = fileInfos.readUTF();
                try {
                    send(new File(fpath), fmiEndPos, cuid, iuid, ts);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }            
        } catch (EOFException eof) {
            try {
                as.waitForOutstandingRSP();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            SafeClose.close(fileInfos);
            tmpFile.delete();
        }
    }

    private void scanFile(DataOutputStream fileInfos, File f) {
        if (f.isDirectory()) {
            for (String s : f.list())
                scanFile(fileInfos, new File(f, s));
            return;
        }
        DicomInputStream in = null;
        try {
            in = new DicomInputStream(f);
            Attributes fmi = in.readFileMetaInformation();
            long dsPos = in.getPosition();
            Attributes ds = in.readDataset(-1, Tag.SOPInstanceUID);
            ds.setBytes(Tag.SOPInstanceUID, VR.UI, in.readValue());
            addFile(fileInfos, f, dsPos,
                    ds.getString(Tag.SOPClassUID, null),
                    ds.getString(Tag.SOPInstanceUID, null),
                    fmi != null
                            ? fmi.getString(Tag.TransferSyntaxUID, null)
                            : in.explicitVR() 
                                    ? in.bigEndian()
                                            ? UID.ExplicitVRBigEndian
                                            : UID.ExplicitVRLittleEndian
                                    : UID.ImplicitVRLittleEndian);
            filesScanned++;
            System.out.print('.');
        } catch (IOException e) {
            System.out.print('E');
            e.printStackTrace();
        } finally {
            SafeClose.close(in);
        }
    }

    private void addFile(DataOutputStream fileInfos, File f, long endFmi,
            String cuid, String iuid, String ts) throws IOException {
        fileInfos.writeUTF(f.getPath());
        fileInfos.writeLong(endFmi);
        fileInfos.writeUTF(cuid);
        fileInfos.writeUTF(iuid);
        fileInfos.writeUTF(ts);

        if (rq.containsPresentationContextFor(cuid, ts))
            return;

        if (!rq.containsPresentationContextFor(cuid)) {
            if (relExtNeg)
                rq.addCommonExtendedNegotiation(
                        relSOPClasses.getCommonExtendedNegotiation(cuid));
            if (!ts.equals(UID.ImplicitVRLittleEndian))
                rq.addPresentationContext(
                        new PresentationContext(
                                rq.getNumberOfPresentationContexts() * 2 + 1,
                                cuid, UID.ImplicitVRLittleEndian));
        }
        rq.addPresentationContext(
                new PresentationContext(
                        rq.getNumberOfPresentationContexts() * 2 + 1,
                        cuid, ts));
    }

    public void echo() throws IOException, InterruptedException {
        as.cecho().next();
    }

    public void send(final File f, long fmiEndPos, String cuid, String iuid,
            String ts) throws IOException, InterruptedException {
        FileInputStream in = new FileInputStream(f);
        in.skip(fmiEndPos);
        DataWriter data = new InputStreamDataWriter(in);

        DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                Main.this.onCStoreRSP(cmd, f);
            }
        };

        as.cstore(cuid, iuid, priority, data , ts, rspHandler);
    }

    public void close() throws IOException, InterruptedException {
        if (as != null && as.isReadyForDataTransfer())
            as.release();
    }

    public void open()
            throws IOException, InterruptedException, IncompatibleConnectionException, KeyManagementException {
        if (rq.getNumberOfPresentationContexts() == 0)
            rq.addPresentationContext(
                    new PresentationContext(1, UID.VerificationSOPClass,
                            UID.ImplicitVRLittleEndian));
        as = ae.connect(conn, remote, rq);
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
            System.err.println(MessageFormat.format(rb.getString("warning"),
                    TagUtils.shortToHexString(status), f));
            System.err.println(cmd);
            break;
        default:
            System.out.print('E');
            System.err.println(MessageFormat.format(rb.getString("error"),
                    TagUtils.shortToHexString(status), f));
                   System.err.println(cmd);
        }
    }
}
