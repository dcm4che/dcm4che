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

package org.dcm4che3.tool.storescu;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.imageio.codec.Decompressor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.RoleSelection;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.common.DicomFiles;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class StoreSCU {

    public interface RSPHandlerFactory {

        DimseRSPHandler createDimseRSPHandler(File f);
    }

    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.storescu.messages");

    private final ApplicationEntity ae;
    private final Connection remote;
    private final AAssociateRQ rq = new AAssociateRQ();
    private final RelatedGeneralSOPClasses relSOPClasses = new RelatedGeneralSOPClasses();
    private Attributes attrs;
    private String uidSuffix;
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

    private RSPHandlerFactory rspHandlerFactory = new RSPHandlerFactory() {

        @Override
        public DimseRSPHandler createDimseRSPHandler(final File f) {

            return new DimseRSPHandler(as.nextMessageID()) {

                @Override
                public void onDimseRSP(Association as, Attributes cmd,
                        Attributes data) {
                    super.onDimseRSP(as, cmd, data);
                    StoreSCU.this.onCStoreRSP(cmd, f);
                }
            };
        }
    };

    public StoreSCU(ApplicationEntity ae) throws IOException {
        this.remote = new Connection();
        this.ae = ae;
        rq.addPresentationContext(new PresentationContext(1,
                UID.Verification, UID.ImplicitVRLittleEndian));
    }

    public void setRspHandlerFactory(RSPHandlerFactory rspHandlerFactory) {
        this.rspHandlerFactory = rspHandlerFactory;
    }

    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    public Connection getRemoteConnection() {
        return remote;
    }

    public Attributes getAttributes() {
        return attrs;
    }

    public void setAttributes(Attributes attrs) {
        this.attrs = attrs;
    }

    public void setTmpFile(File tmpFile) {
        this.tmpFile = tmpFile;
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public final void setUIDSuffix(String uidSuffix) {
        this.uidSuffix = uidSuffix;
    }

    public final void setTmpFilePrefix(String prefix) {
        this.tmpPrefix = prefix;
    }

    public final void setTmpFileSuffix(String suffix) {
        this.tmpSuffix = suffix;
    }

    public final void setTmpFileDirectory(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        CLIUtils.addConnectOption(opts);
        CLIUtils.addBindClientOption(opts, "STORESCU");
        CLIUtils.addAEOptions(opts);
        CLIUtils.addStoreTimeoutOption(opts);
        CLIUtils.addResponseTimeoutOption(opts);
        CLIUtils.addPriorityOption(opts);
        CLIUtils.addCommonOptions(opts);
        addStoreTCOptions(opts);
        addTmpFileOptions(opts);
        addRelatedSOPClassOptions(opts);
        addAttributesOption(opts);
        addUIDSuffixOption(opts);
        return CLIUtils.parseComandLine(args, opts, rb, StoreSCU.class);
    }

    private static void addAttributesOption(Options opts) {
        opts.addOption(Option.builder("s")
                .hasArgs()
                .argName("[seq.]attr=value")
                .desc(rb.getString("set"))
                .build());
    }

    public static void addUIDSuffixOption(Options opts) {
        opts.addOption(Option.builder().hasArg().argName("suffix")
                .desc(rb.getString("uid-suffix"))
                .longOpt("uid-suffix").build());
    }

    private static void addStoreTCOptions(Options opts) {
        opts.addOption(Option.builder()
                .hasArg()
                .argName("cuid:tsuid[(,|;)...]")
                .desc(rb.getString("store-tc"))
                .longOpt("store-tc")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("file|url")
                .desc(rb.getString("store-tcs"))
                .longOpt("store-tcs")
                .build());
    }

    public static void addTmpFileOptions(Options opts) {
        opts.addOption(Option.builder().hasArg().argName("directory")
                .desc(rb.getString("tmp-file-dir"))
                .longOpt("tmp-file-dir").build());
        opts.addOption(Option.builder().hasArg().argName("prefix")
                .desc(rb.getString("tmp-file-prefix"))
                .longOpt("tmp-file-prefix").build());
        opts.addOption(Option.builder().hasArg().argName("suffix")
                .desc(rb.getString("tmp-file-suffix"))
                .longOpt("tmp-file-suffix").build());
    }

    private static void addRelatedSOPClassOptions(Options opts) {
        opts.addOption(null, "rel-ext-neg", false, rb.getString("rel-ext-neg"));
        opts.addOption(Option.builder().hasArg().argName("file|url")
                .desc(rb.getString("rel-sop-classes"))
                .longOpt("rel-sop-classes").build());
    }

    public static void main(String[] args) {
        long t1, t2;
        try {
            CommandLine cl = parseComandLine(args);
            Device device = new Device("storescu");
            Connection conn = new Connection();
            device.addConnection(conn);
            ApplicationEntity ae = new ApplicationEntity("STORESCU");
            device.addApplicationEntity(ae);
            ae.addConnection(conn);
            StoreSCU main = new StoreSCU(ae);
            configureTmpFile(main, cl);
            CLIUtils.configureConnect(main.remote, main.rq, cl);
            CLIUtils.configureBind(conn, ae, cl);
            CLIUtils.configure(conn, cl);
            main.remote.setTlsProtocols(conn.getTlsProtocols());
            main.remote.setTlsCipherSuites(conn.getTlsCipherSuites());
            configureRelatedSOPClass(main, cl);
            main.setAttributes(new Attributes());
            CLIUtils.addAttributes(main.attrs, cl.getOptionValues("s"));
            main.setUIDSuffix(cl.getOptionValue("uid-suffix"));
            main.setPriority(CLIUtils.priorityOf(cl));
            List<String> argList = cl.getArgList();
            boolean echo = argList.isEmpty();
            if (echo) {
                configureStorageSOPClasses(main, cl);
            } else {
                System.out.println(rb.getString("scanning"));
                t1 = System.currentTimeMillis();
                main.scanFiles(argList);
                t2 = System.currentTimeMillis();
                int n = main.filesScanned;
                System.out.println();
                if (n == 0)
                    return;
                System.out.println(MessageFormat.format(
                        rb.getString("scanned"), n, (t2 - t1) / 1000F,
                        (t2 - t1) / n));
            }
            ExecutorService executorService = Executors
                    .newSingleThreadExecutor();
            ScheduledExecutorService scheduledExecutorService = Executors
                    .newSingleThreadScheduledExecutor();
            device.setExecutor(executorService);
            device.setScheduledExecutor(scheduledExecutorService);
            try {
                t1 = System.currentTimeMillis();
                main.open();
                t2 = System.currentTimeMillis();
                System.out.println(MessageFormat.format(
                        rb.getString("connected"), main.as.getRemoteAET(), t2
                                - t1));
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
                        main.filesSent, mb, s, mb / s));
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

    public static String uidSuffixOf(CommandLine cl) {
        return cl.getOptionValue("uid-suffix");
    }

    private static void configureTmpFile(StoreSCU storescu, CommandLine cl) {
        if (cl.hasOption("tmp-file-dir"))
            storescu.setTmpFileDirectory(new File(cl
                    .getOptionValue("tmp-file-dir")));
        storescu.setTmpFilePrefix(cl.getOptionValue("tmp-file-prefix",
                "storescu-"));
        storescu.setTmpFileSuffix(cl.getOptionValue("tmp-file-suffix"));
    }

    public static void configureRelatedSOPClass(StoreSCU storescu,
            CommandLine cl) throws IOException {
        if (cl.hasOption("rel-ext-neg")) {
            storescu.enableSOPClassRelationshipExtNeg(true);
            Properties p = new Properties();
            CLIUtils.loadProperties(
                    cl.hasOption("rel-sop-classes") ? cl
                            .getOptionValue("rel-ext-neg")
                            : "resource:rel-sop-classes.properties", p);
            storescu.relSOPClasses.init(p);
        }
    }

    private static void configureStorageSOPClasses(StoreSCU main, CommandLine cl)
            throws Exception {
        String[] pcs = cl.getOptionValues("store-tc");
        if (pcs != null)
            for (String pc : pcs) {
                String[] ss = StringUtils.split(pc, ':');
                configureStorageSOPClass(main, ss[0], ss[1]);
            }
        String[] files = cl.getOptionValues("store-tcs");
        if (files != null)
            for (String file : files) {
                Properties p = CLIUtils.loadProperties(file, null);
                Set<Map.Entry<Object, Object>> entrySet = p.entrySet();
                for (Map.Entry<Object, Object> entry : entrySet)
                    configureStorageSOPClass(main, (String) entry.getKey(), (String) entry.getValue());
            }
    }

    private static void configureStorageSOPClass(StoreSCU main, String cuid, String tsuids0) {
        for (String tsuids2 : StringUtils.split(tsuids0, ';')) {
            main.addOfferedStorageSOPClass(CLIUtils.toUID(cuid), CLIUtils.toUIDs(tsuids2));
        }
    }

    public void addOfferedStorageSOPClass(String cuid, String... tsuids) {
        rq.addPresentationContext(new PresentationContext(
                2 * rq.getNumberOfPresentationContexts() + 1, cuid, tsuids));
    }

    public final void enableSOPClassRelationshipExtNeg(boolean enable) {
        relExtNeg = enable;
    }

    public void scanFiles(List<String> fnames) throws IOException {
        this.scanFiles(fnames, true);
    }

    public void scanFiles(List<String> fnames, boolean printout)
            throws IOException {
        tmpFile = File.createTempFile(tmpPrefix, tmpSuffix, tmpDir);
        tmpFile.deleteOnExit();
        final BufferedWriter fileInfos = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(tmpFile)));
        try {
            DicomFiles.scan(fnames, printout, new DicomFiles.Callback() {

                @Override
                public boolean dicomFile(File f, Attributes fmi, long dsPos,
                        Attributes ds) throws IOException {
                    if (!addFile(fileInfos, f, dsPos, fmi, ds))
                        return false;

                    filesScanned++;
                    return true;
                }
            });
        } finally {
            fileInfos.close();
        }
    }

    public void sendFiles() throws IOException {
        BufferedReader fileInfos = new BufferedReader(new InputStreamReader(
                new FileInputStream(tmpFile)));
        try {
            String line;
            while (as.isReadyForDataTransfer()
                    && (line = fileInfos.readLine()) != null) {
                String[] ss = StringUtils.split(line, '\t');
                try {
                    send(new File(ss[4]), Long.parseLong(ss[3]), ss[1], ss[0],
                            ss[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                as.waitForOutstandingRSP();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            SafeClose.close(fileInfos);
        }
    }

    public boolean addFile(BufferedWriter fileInfos, File f, long endFmi,
            Attributes fmi, Attributes ds) throws IOException {
        String cuid = fmi.getString(Tag.MediaStorageSOPClassUID);
        String iuid = fmi.getString(Tag.MediaStorageSOPInstanceUID);
        String ts = fmi.getString(Tag.TransferSyntaxUID);
        if (cuid == null || iuid == null)
            return false;

        fileInfos.write(iuid);
        fileInfos.write('\t');
        fileInfos.write(cuid);
        fileInfos.write('\t');
        fileInfos.write(ts);
        fileInfos.write('\t');
        fileInfos.write(Long.toString(endFmi));
        fileInfos.write('\t');
        fileInfos.write(f.getPath());
        fileInfos.newLine();

        if (rq.containsPresentationContextFor(cuid, ts))
            return true;

        if (!rq.containsPresentationContextFor(cuid)) {
            if (relExtNeg)
                rq.addCommonExtendedNegotiation(relSOPClasses
                        .getCommonExtendedNegotiation(cuid));
            if (!ts.equals(UID.ExplicitVRLittleEndian))
                rq.addPresentationContext(new PresentationContext(rq
                        .getNumberOfPresentationContexts() * 2 + 1, cuid,
                        UID.ExplicitVRLittleEndian));
            if (!ts.equals(UID.ImplicitVRLittleEndian))
                rq.addPresentationContext(new PresentationContext(rq
                        .getNumberOfPresentationContexts() * 2 + 1, cuid,
                        UID.ImplicitVRLittleEndian));
        }
        rq.addPresentationContext(new PresentationContext(rq
                .getNumberOfPresentationContexts() * 2 + 1, cuid, ts));
        return true;
    }

    public void echo() throws IOException, InterruptedException {
        as.cecho().next();
    }

    public void send(final File f, long fmiEndPos, String cuid, String iuid,
            String filets) throws IOException, InterruptedException,
            ParserConfigurationException, SAXException {
        String ts = selectTransferSyntax(cuid, filets);

        if (f.getName().endsWith(".xml")) {
            Attributes parsedDicomFile = SAXReader.parse(new FileInputStream(f));
            if (CLIUtils.updateAttributes(parsedDicomFile, attrs, uidSuffix))
                iuid = parsedDicomFile.getString(Tag.SOPInstanceUID);
            if (!ts.equals(filets)) {
                Decompressor.decompress(parsedDicomFile, filets);
            }
            as.cstore(cuid, iuid, priority,
                    new DataWriterAdapter(parsedDicomFile), ts,
                    rspHandlerFactory.createDimseRSPHandler(f));
        } else {
            if (uidSuffix == null && attrs.isEmpty() && ts.equals(filets)) {
                FileInputStream in = new FileInputStream(f);
                try {
                    in.skip(fmiEndPos);
                    InputStreamDataWriter data = new InputStreamDataWriter(in);
                    as.cstore(cuid, iuid, priority, data, ts,
                            rspHandlerFactory.createDimseRSPHandler(f));
                } finally {
                    SafeClose.close(in);
                }
            } else {
                DicomInputStream in = new DicomInputStream(f);
                try {
                    in.setIncludeBulkData(IncludeBulkData.URI);
                    Attributes data = in.readDataset();
                    if (CLIUtils.updateAttributes(data, attrs, uidSuffix))
                        iuid = data.getString(Tag.SOPInstanceUID);
                    if (!ts.equals(filets)) {
                        Decompressor.decompress(data, filets);
                    }
                    as.cstore(cuid, iuid, priority,
                            new DataWriterAdapter(data), ts,
                            rspHandlerFactory.createDimseRSPHandler(f));
                } finally {
                    SafeClose.close(in);
                }
            }
        }
    }

    private String selectTransferSyntax(String cuid, String filets) {
        Set<String> tss = as.getTransferSyntaxesFor(cuid);
        if (tss.contains(filets))
            return filets;

        if (tss.contains(UID.ExplicitVRLittleEndian))
            return UID.ExplicitVRLittleEndian;

        return UID.ImplicitVRLittleEndian;
    }

    public void close() throws IOException, InterruptedException {
        if (as != null) {
            if (as.isReadyForDataTransfer())
                as.release();
            as.waitForSocketClose();
        }
    }

    public void open() throws IOException, InterruptedException,
            IncompatibleConnectionException, GeneralSecurityException {
        as = ae.connect(remote, rq);
    }

    private void onCStoreRSP(Attributes cmd, File f) {
        int status = cmd.getInt(Tag.Status, -1);
        switch (status) {
        case Status.Success:
            totalSize += f.length();
            ++filesSent;
            System.out.print('.');
            break;
        case Status.CoercionOfDataElements:
        case Status.ElementsDiscarded:
        case Status.DataSetDoesNotMatchSOPClassWarning:
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
