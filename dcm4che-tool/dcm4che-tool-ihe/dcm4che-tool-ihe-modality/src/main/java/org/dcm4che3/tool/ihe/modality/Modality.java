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

package org.dcm4che3.tool.ihe.modality;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.GeneralSecurityException;
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
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.common.DicomFiles;
import org.dcm4che3.tool.mkkos.MkKOS;
import org.dcm4che3.tool.mppsscu.MppsSCU;
import org.dcm4che3.tool.stgcmtscu.StgCmtSCU;
import org.dcm4che3.tool.storescu.StoreSCU;
import org.dcm4che3.util.UIDUtils;

/**
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Modality {
    
    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.ihe.modality.messages");
    
    static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    
    private static String calledAET;
    
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
            checkOptions(cl);
            CLIUtils.configureBind(conn, ae, cl);
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
            calledAET = storescu.getAAssociateRQ().getCalledAET();
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
            storescu.setUIDSuffix(StoreSCU.uidSuffixOf(cl));
            Attributes attrs = new Attributes();
            CLIUtils.addAttributes(attrs, cl.getOptionValues("s"));
            mppsscu.setAttributes(attrs);
            storescu.setAttributes(attrs);
            stgcmtscu.setAttributes(attrs);
            setTlsParams(mppsscu.getRemoteConnection(), conn);
            setTlsParams(storescu.getRemoteConnection(), conn);
            setTlsParams(stgcmtscu.getRemoteConnection(), conn);
            String tmpPrefix = "iocmtest-";
            String tmpSuffix = null;
            File tmpDir = null;
            configureTmpFile(storescu, tmpPrefix, tmpSuffix, tmpDir, cl);
            String mppsiuid = UIDUtils.createUID();
            mppsscu.setPPSUID(mppsiuid);
            if(cl.hasOption("kos-title")) {
                List<String> fname = Arrays.asList(mkkos(cl));
                scanFiles(fname, tmpPrefix, tmpSuffix, tmpDir, mppsscu, storescu, stgcmtscu);
            } else {
                stgcmtscu.setUIDSuffix(cl.getOptionValue("uid-suffix"));
                storescu.setUIDSuffix(cl.getOptionValue("uid-suffix"));
                mppsscu.setUIDSuffix(cl.getOptionValue("uid-suffix"));
                scanFiles(cl.getArgList(), tmpPrefix, tmpSuffix, tmpDir, mppsscu, storescu, stgcmtscu);
            }
            ExecutorService executorService =
                    Executors.newCachedThreadPool();
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newSingleThreadScheduledExecutor();
            device.setExecutor(executorService);
            device.setScheduledExecutor(scheduledExecutorService);
            device.bindConnections();
            try {
                boolean sendMpps = cl.hasOption("mpps");
                boolean sendLateMpps = cl.hasOption("mpps-late");
                if (sendMpps || sendLateMpps) {
                    sendMpps(mppsscu, sendMpps);
                    addReferencedPerformedProcedureStepSequence(mppsiuid, storescu);
                } else {
                    nullifyReferencedPerformedProcedureStepSequence(storescu);
                }
                sendObjects(storescu);
                if (sendLateMpps)
                    sendMppsNSet(mppsscu);
                if (cl.hasOption("stgcmt"))
                    sendStgCmt(stgcmtscu);
            } finally {
                if (conn.isListening()) {
                    device.waitForNoOpenConnections();
                    device.unbindConnections();
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

    private static void checkOptions(CommandLine cl) throws ParseException {
        if (!cl.hasOption("b"))
            throw new MissingOptionException(
                    CLIUtils.rb.getString("missing-bind-opt"));
        if (!cl.hasOption("c"))
            throw new MissingOptionException(
                    CLIUtils.rb.getString("missing-connect-opt"));
        if (cl.hasOption("mpps") && cl.hasOption("mpps-late"))
            throw new ParseException(rb.getString("mpps-error"));
    }

    public static void setTlsParams(Connection remote, Connection conn) {
        remote.setTlsProtocols(conn.tlsProtocols());
        remote.setTlsCipherSuites(conn.getTlsCipherSuites());
    }

    private static void addReferencedPerformedProcedureStepSequence(String mppsiuid,
            StoreSCU storescu) {
        Attributes attrs = storescu.getAttributes();
        Sequence seq = attrs.newSequence(Tag.ReferencedPerformedProcedureStepSequence, 1);
        Attributes item = new Attributes(2);
        item.setString(Tag.ReferencedSOPClassUID, VR.UI, UID.ModalityPerformedProcedureStepSOPClass);
        item.setString(Tag.ReferencedSOPInstanceUID, VR.UI, mppsiuid);
        seq.add(item);
    }

    private static void nullifyReferencedPerformedProcedureStepSequence(StoreSCU storescu) {
        Attributes attrs = storescu.getAttributes();
        attrs.setNull(Tag.ReferencedPerformedProcedureStepSequence, VR.SQ);
    }

    @SuppressWarnings("unchecked")
    private static String mkkos(CommandLine cl) throws Exception {
        printNextStepMessage("Will now generate a Key Object for files in " + cl.getArgList());
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
            public boolean dicomFile(File f, Attributes fmi,
                    long dsPos, Attributes ds) {
                return mkkos.addInstance(ds);
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
            InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
        printNextStepMessage("Will now send Storage Commitment to " + calledAET);
        try {
            stgcmtscu.open();
            stgcmtscu.sendRequests();
        } finally {
            stgcmtscu.close();
        }
    }

    private static void sendMpps(MppsSCU mppsscu, boolean sendNSet) throws IOException,
            InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
        try {
            printNextStepMessage("Will now send MPPS N-CREATE to " + calledAET);
            mppsscu.open();
            mppsscu.createMpps();
            if (sendNSet) {
                printNextStepMessage("Will now send MPPS N-SET to " + calledAET);
                mppsscu.updateMpps();
            }
        } finally {
            mppsscu.close();
        }
    }

    private static void sendMppsNSet(MppsSCU mppsscu) throws IOException, InterruptedException,
            IncompatibleConnectionException, GeneralSecurityException {
        try {
            printNextStepMessage("Will now send MPPS N-SET to " + calledAET);
            mppsscu.open();
            mppsscu.updateMpps();
        } finally {
            mppsscu.close();
        }
    }

    private static void printNextStepMessage(String message) throws IOException {
        System.out.println("===========================================================");
        System.out.println(message + ". Press <enter> to continue.");
        System.out.println("===========================================================");
        bufferedReader.read();
    }

    private static void sendObjects(StoreSCU storescu) throws IOException,
            InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
        printNextStepMessage("Will now send DICOM object(s) to " + calledAET);
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
        OptionGroup mpps = new OptionGroup();
        mpps.addOption(OptionBuilder
                .withDescription(rb.getString("mpps-late"))
                .withLongOpt("mpps-late")
                .create());
        mpps.addOption(OptionBuilder
                .withDescription(rb.getString("mpps"))
                .withLongOpt("mpps")
                .create());
        opts.addOptionGroup(mpps);
        opts.addOption(OptionBuilder
                .withDescription(rb.getString("stgcmt"))
                .withLongOpt("stgcmt")
                .create());
        opts.addOption(null, "dc", false, rb.getString("dc"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("code-value")
                .withDescription(rb.getString("dc-reason"))
                .withLongOpt("dc-reason")
                .create());
        opts.addOption(OptionBuilder
                .hasArgs()
                .withArgName("[seq/]attr=value")
                .withValueSeparator('=')
                .withDescription(rb.getString("set"))
                .create("s"));
   }
    
    private static void scanFiles(List<String> fnames, String tmpPrefix, String tmpSuffix,
            File tmpDir, final MppsSCU mppsscu, final StoreSCU storescu, final StgCmtSCU stgcmtscu)
            throws IOException {
        printNextStepMessage("Will now scan files in " + fnames);
        File tmpFile = File.createTempFile(tmpPrefix, tmpSuffix, tmpDir);
        tmpFile.deleteOnExit();
        final BufferedWriter fileInfos = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tmpFile)));
        try {
            DicomFiles.scan(fnames, new DicomFiles.Callback() {

                @Override
                public boolean dicomFile(File f, Attributes fmi, long dsPos,
                        Attributes ds) throws Exception {
                    return mppsscu.addInstance(ds)
                        && storescu.addFile(fileInfos, f, dsPos, fmi, ds)
                        && stgcmtscu.addInstance(ds);
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
