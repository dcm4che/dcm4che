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

package org.dcm4che.tool.ihe.modality;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che.data.Attributes;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.IncompatibleConnectionException;
import org.dcm4che.tool.common.CLIUtils;
import org.dcm4che.tool.common.DicomFiles;
import org.dcm4che.tool.mkkos.MkKOS;
import org.dcm4che.tool.mppsscu.MppsSCU;
import org.dcm4che.tool.stgcmtscu.StgCmtSCU;
import org.dcm4che.tool.storescu.StoreSCU;
import org.dcm4che.util.UIDUtils;

/**
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Modality {
    
    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che.tool.ihe.modality.messages");
    
    static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    
    @SuppressWarnings({ "unchecked" })
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            if(cl.getArgList().isEmpty())
                throw new MissingOptionException(
                        rb.getString("missing-i-file"));
            final Device device = new Device("modality");
            final Connection conn = new Connection();
            final ApplicationEntity ae = new ApplicationEntity("MODALITY");
            if (!cl.hasOption("b"))
                throw new MissingOptionException(
                        CLIUtils.rb.getString("missing-bind-opt"));
            CLIUtils.configureBind(conn, ae, cl);
            if (!cl.hasOption("c"))
                throw new MissingOptionException(
                        CLIUtils.rb.getString("missing-connect-opt"));
            CLIUtils.configure(conn, cl);
            device.addConnection(conn);
            device.addApplicationEntity(ae);
            ae.addConnection(conn);
            final MppsSCU mppsscu = new MppsSCU(ae);
            final StoreSCU storescu = new StoreSCU(ae);
            final StgCmtSCU stgcmtscu = new StgCmtSCU(ae);
            CLIUtils.configureConnect(mppsscu.getRemoteConnection(), mppsscu.getAAssociateRQ(), cl);
            CLIUtils.configureConnect(stgcmtscu.getRemoteConnection(), stgcmtscu.getAAssociateRQ(), cl);
            CLIUtils.configureConnect(storescu.getRemoteConnection(), storescu.getAAssociateRQ(), cl);
            mppsscu.setTransferSyntaxes(CLIUtils.transferSyntaxesOf(cl));
            mppsscu.setCodes(CLIUtils.loadProperties(
                    cl.getOptionValue("code-config", "resource:code.properties"), null));
            if (cl.hasOption("dc"))
                mppsscu.setFinalStatus("DISCONTINUED");
            if (cl.hasOption("dc-reason"))
                mppsscu.setDiscontinuationReason(cl.getOptionValue("dc-reason"));
            stgcmtscu.setTransferSyntaxes(CLIUtils.transferSyntaxesOf(cl));
            stgcmtscu.setStorageDirectory(StgCmtSCU.getStorageDirectory(cl));
            StoreSCU.configureRelatedSOPClass(storescu, cl);
            StoreSCU.configureAttributes(storescu, cl);
            storescu.setUIDSuffix(StoreSCU.uidSuffixOf(cl));
            setTlsParams(mppsscu.getRemoteConnection(), conn);
            setTlsParams(storescu.getRemoteConnection(), conn);
            setTlsParams(stgcmtscu.getRemoteConnection(), conn);
            String tmpPrefix = "iocmtest-";
            String tmpSuffix = null;
            File tmpDir = null;
            configureTmpFile(storescu, tmpPrefix, tmpSuffix, tmpDir, cl);
            if(cl.hasOption("kos-title")) {
                List<String> fname = Arrays.asList(mkkos(cl));
                scanFiles(fname, tmpPrefix, tmpSuffix, tmpDir, mppsscu, storescu, stgcmtscu);
            } else {
                stgcmtscu.setUIDSuffix(cl.getOptionValue("uid-suffix"));
                storescu.setUIDSuffix(cl.getOptionValue("uid-suffix"));
                mppsscu.setUIDSuffix(cl.getOptionValue("uid-suffix"));
                scanFiles(cl.getArgList(), tmpPrefix, tmpSuffix, tmpDir, mppsscu, storescu, stgcmtscu);
            }
            String mppsiuid = UIDUtils.createUID();
            mppsscu.setPPSUID(mppsiuid);
            ExecutorService executorService =
                    Executors.newCachedThreadPool();
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newSingleThreadScheduledExecutor();
            device.setExecutor(executorService);
            device.setScheduledExecutor(scheduledExecutorService);
            device.activate();
            try {
                if(!cl.hasOption("late-mpps"))
                    sendMpps(mppsscu);
                addRPPSS(mppsiuid, storescu);
                sendObjects(storescu);
                if(cl.hasOption("late-mpps"))
                    sendMpps(mppsscu);
                sendStgCmt(stgcmtscu);
            } finally {
                if (conn.isListening()) {
                    device.waitForNoOpenConnections();
                    device.deactivate();
                }
                executorService.shutdown();
                scheduledExecutorService.shutdown();
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }

    }

    public static void setTlsParams(Connection remote, Connection conn) {
        remote.setTlsProtocols(conn.getTlsProtocols());
        remote.setTlsCipherSuites(conn.getTlsCipherSuites());
    }

    private static void addRPPSS(String mppsiuid, StoreSCU storescu) throws IOException {
        Attributes attrs = storescu.getAttributes();
        Sequence seq = attrs.newSequence(Tag.ReferencedPerformedProcedureStepSequence, 1);
        Attributes item = new Attributes(2);
        item.setString(Tag.ReferencedSOPClassUID, VR.UI, UID.ModalityPerformedProcedureStepSOPClass);
        item.setString(Tag.ReferencedSOPInstanceUID, VR.UI, mppsiuid);
        seq.add(item);
    }

    @SuppressWarnings("unchecked")
    private static String mkkos(CommandLine cl) throws Exception {
        System.out.println("\n===========================================================");
        System.out.println("Will now generate a Key Object for files in " + cl.getArgList() + ". Press <enter> to continue.");
        System.out.println("===========================================================");
        bufferedReader.read();
        final MkKOS mkkos = new MkKOS();
        mkkos.setUIDSuffix(cl.getOptionValue("uid-suffix"));
        mkkos.setCodes(CLIUtils.loadProperties(
                cl.getOptionValue("code-config", "resource:code.properties"),
                null));
        mkkos.setDocumentTitle(mkkos.toCodeItem(documentTitleOf(cl)));
        mkkos.setKeyObjectDescription(cl.getOptionValue("desc"));
        mkkos.setSeriesNumber(cl.getOptionValue("series-no", "999"));
        mkkos.setInstanceNumber(cl.getOptionValue("inst-no", "1"));
        mkkos.setOutputFile(MkKOS.outputFileOf(cl));
        mkkos.setNoFileMetaInformation(cl.hasOption("F"));
        mkkos.setTransferSyntax(cl.getOptionValue("t", UID.ExplicitVRLittleEndian));
        mkkos.setEncodingOptions(CLIUtils.encodingOptionsOf(cl));
        DicomFiles.scan(cl.getArgList(), new DicomFiles.Callback() {

            @Override
            public void dicomFile(File f, long dsPos, String tsuid, Attributes ds) {
                mkkos.addInstance(ds);
            }
        });
        System.out.println();
        mkkos.writeKOS();
        System.out.println(MessageFormat.format(rb.getString("stored"), mkkos.getFname()));
        return mkkos.getFname();
    }

    private static String documentTitleOf(CommandLine cl) throws MissingOptionException {
        if (!cl.hasOption("kos-title"))
            throw new MissingOptionException(rb.getString("missing-title"));
        return cl.getOptionValue("kos-title");
    }

    private static void sendStgCmt(StgCmtSCU stgcmtscu) throws IOException,
            InterruptedException, IncompatibleConnectionException {
        System.out.println("\n===========================================================");
        System.out.println("Will now send Storage Commitment to " + stgcmtscu.getAAssociateRQ().getCalledAET() + ". Press <enter> to continue.");
        System.out.println("===========================================================");
        bufferedReader.read();
        try {
            stgcmtscu.open();
            stgcmtscu.sendRequests();
        } finally {
            stgcmtscu.close();
        }
    }

    private static void sendMpps(MppsSCU mppsscu) throws IOException,
            InterruptedException, IncompatibleConnectionException {
        System.out.println("\n===========================================================");
        System.out.println("Will now send MPPS to " + mppsscu.getAAssociateRQ().getCalledAET()
                + ". Press <enter> to continue.");
        System.out.println("===========================================================");
        bufferedReader.read();
        try {
            mppsscu.open();
            mppsscu.sendMpps();
        } finally {
            mppsscu.close();
        }
    }
    
    private static void sendObjects(StoreSCU storescu) throws IOException,
            InterruptedException, IncompatibleConnectionException {
        System.out.println("\n===========================================================");
        System.out.println("Will now send objects to " + storescu.getAAssociateRQ().getCalledAET() + ". Press <enter> to continue.");
        System.out.println("===========================================================");
        bufferedReader.read();
        try {
            storescu.open();
            storescu.sendFiles();
        } finally {
            storescu.close();
        }
    }
    
    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        CLIUtils.addTransferSyntaxOptions(opts);
        CLIUtils.addConnectOption(opts);
        CLIUtils.addAEOptions(opts);
        CLIUtils.addResponseTimeoutOption(opts);
        CLIUtils.addCommonOptions(opts);
        CLIUtils.addBindOption(opts, "IOCMTEST");
        StoreSCU.addTmpFileOptions(opts);
        StoreSCU.addUIDSuffixOption(opts);
        addOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, Modality.class);
    }
    
    @SuppressWarnings("static-access")
    private static void addOptions(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("code-value")
                .withDescription(rb.getString("kos-title"))
                .withLongOpt("kos-title")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file")
                .withDescription(rb.getString("o-file"))
                .create("o"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file")
                .withDescription(rb.getString("code-config"))
                .withLongOpt("code-config")
                .create());
        opts.addOption(OptionBuilder
                .withDescription(rb.getString("late-mpps"))
                .withLongOpt("late-mpps")
                .create());
        opts.addOption(null, "dc", false, rb.getString("dc"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("code-value")
                .withDescription(rb.getString("dc-reason"))
                .withLongOpt("dc-reason")
                .create());
   }
    
    private static void scanFiles(List<String> fnames, String tmpPrefix, String tmpSuffix,
            File tmpDir, final MppsSCU mppsscu, final StoreSCU storescu, final StgCmtSCU stgcmtscu)
            throws IOException {
        System.out.println("\n===========================================================");
        System.out.println("Press <enter> to scan files in " + fnames);
        System.out.println("===========================================================");
        bufferedReader.read();
        File tmpFile = File.createTempFile(tmpPrefix, tmpSuffix, tmpDir);
        tmpFile.deleteOnExit();
        final BufferedWriter fileInfos = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tmpFile)));
        try {
            DicomFiles.scan(fnames, new DicomFiles.Callback() {

                @Override
                public void dicomFile(File f, long dsPos, String tsuid, Attributes ds)
                        throws IOException {
                    mppsscu.addInstance(ds);
                    storescu.addFile(fileInfos, f, dsPos, ds.getString(Tag.SOPClassUID, null),
                            ds.getString(Tag.SOPInstanceUID, null), tsuid);
                    stgcmtscu.addInstance(ds);
                }
            });
            storescu.setTmpFile(tmpFile);
        } finally {
            fileInfos.close();
        }
        System.out.println(" Done");
    }
    
    private static void configureTmpFile(StoreSCU storescu, String tmpPrefix, String tmpSuffix,
            File tmpDir, CommandLine cl) {
        if (cl.hasOption("tmp-file-dir")) {
            tmpDir = new File(cl.getOptionValue("tmp-file-dir"));
            storescu.setTmpFileDirectory(tmpDir);
        }
        tmpPrefix = cl.getOptionValue("tmp-file-prefix", "iocmtest-");
        storescu.setTmpFilePrefix(tmpPrefix);
        tmpSuffix = cl.getOptionValue("tmp-file-suffix");
        storescu.setTmpFileSuffix(tmpSuffix);
    }
}
