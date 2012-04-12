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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
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
import org.dcm4che.net.DataWriterAdapter;
import org.dcm4che.net.Device;
import org.dcm4che.net.DimseRSPHandler;
import org.dcm4che.net.IncompatibleConnectionException;
import org.dcm4che.net.InputStreamDataWriter;
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.tool.common.CLIUtils;
import org.dcm4che.tool.common.DicomFiles;
import org.dcm4che.util.SafeClose;
import org.dcm4che.util.StringUtils;
import org.dcm4che.util.TagUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class StoreSCU extends Device {

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che.tool.storescu.messages");

    private final ApplicationEntity ae = new ApplicationEntity("STORESCU");
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private final RelatedGeneralSOPClasses relSOPClasses =
            new RelatedGeneralSOPClasses();
    private final Attributes attrs = new Attributes();
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

    public StoreSCU() throws IOException {
        super("storescu");
        addConnection(conn);
        addApplicationEntity(ae);
        ae.addConnection(conn);
        rq.addPresentationContext(
                new PresentationContext(1, UID.VerificationSOPClass,
                        UID.ImplicitVRLittleEndian));
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

    public void addAttribute(int[] tags, String... ss) {
        Attributes item = attrs;
        for (int i = 0; i < tags.length-1; i++) {
            int tag = tags[i];
            Sequence sq = (Sequence) item.getValue(tag);
            if (sq == null)
                sq = item.newSequence(tag, 1);
            if (sq.isEmpty())
                sq.add(new Attributes());
            item = sq.get(0);
        }
        int tag = tags[tags.length-1];
        VR vr = ElementDictionary.vrOf(tag,
                item.getPrivateCreator(tag));
        if (ss.length == 0)
            if (vr == VR.SQ)
                item.newSequence(tag, 1).add(new Attributes(0));
            else
                item.setNull(tag, vr);
        else
            item.setString(tag, vr, ss);
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        CLIUtils.addConnectOption(opts);
        CLIUtils.addBindOption(opts, "STORESCU");
        CLIUtils.addAEOptions(opts);
        CLIUtils.addResponseTimeoutOption(opts);
        CLIUtils.addPriorityOption(opts);
        CLIUtils.addCommonOptions(opts);
        addTmpFileOptions(opts);
        addRelatedSOPClassOptions(opts);
        addAttributesOption(opts);
        addUIDSuffixOption(opts);
        return CLIUtils.parseComandLine(args, opts, rb, StoreSCU.class);
    }

    @SuppressWarnings("static-access")
    private static void addAttributesOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArgs()
                .withArgName("[seq/]attr=value")
                .withValueSeparator('=')
                .withDescription(rb.getString("set"))
                .create("s"));
    }

    @SuppressWarnings("static-access")
    private static void addUIDSuffixOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("suffix")
                .withDescription(rb.getString("uid-suffix"))
                .withLongOpt("uid-suffix")
                .create(null));
    }

    @SuppressWarnings("static-access")
    private static void addTmpFileOptions(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("directory")
                .withDescription(rb.getString("tmp-file-dir"))
                .withLongOpt("tmp-file-dir")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("prefix")
                .withDescription(rb.getString("tmp-file-prefix"))
                .withLongOpt("tmp-file-prefix")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("suffix")
                .withDescription(rb.getString("tmp-file-suffix"))
                .withLongOpt("tmp-file-suffix")
                .create(null));
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
            StoreSCU main = new StoreSCU();
            configureTmpFile(main, cl);
            CLIUtils.configureConnect(main.remote, main.rq, cl);
            CLIUtils.configureBind(main.conn, main.ae, cl);
            CLIUtils.configure(main.conn, cl);
            main.remote.setTlsProtocols(main.conn.getTlsProtocols());
            main.remote.setTlsCipherSuites(main.conn.getTlsCipherSuites());
            configureRelatedSOPClass(main, cl);
            configureAttributes(main, cl);
            main.setUIDSuffix(uidSuffixOf(cl));
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
            main.setScheduledExecutor(scheduledExecutorService);
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

    private static String uidSuffixOf(CommandLine cl) {
        return cl.getOptionValue("uid-suffix");
    }

    private static void configureAttributes(StoreSCU main, CommandLine cl) {
        String[] attrs = cl.getOptionValues("s");
        if (attrs != null)
            for (int i = 1; i < attrs.length; i++, i++)
                main.addAttribute(
                        CLIUtils.toTags(
                                StringUtils.split(attrs[i-1], '/')),
                                StringUtils.split(attrs[i], '/'));
    }

    private static void configureTmpFile(StoreSCU storescu, CommandLine cl) {
        if (cl.hasOption("tmp-file-dir"))
            storescu.setTmpFileDirectory(new File(cl.getOptionValue("tmp-file-dir")));
        storescu.setTmpFilePrefix(cl.getOptionValue("tmp-file-prefix", "storescu-"));
        storescu.setTmpFileSuffix(cl.getOptionValue("tmp-file-suffix"));
    }

    private static void configureRelatedSOPClass(StoreSCU storescu, CommandLine cl)
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
        tmpFile.deleteOnExit();
        final BufferedWriter fileInfos = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(tmpFile)));
        try {
            DicomFiles.scan(fnames, new DicomFiles.Callback() {
                
                @Override
                public void dicomFile(File f, long dsPos, String tsuid, Attributes ds)
                        throws IOException {
                    addFile(fileInfos, f, dsPos,
                            ds.getString(Tag.SOPClassUID, null),
                            ds.getString(Tag.SOPInstanceUID, null),
                            tsuid);
                    filesScanned++;
                }
            });
        } finally {
            fileInfos.close();
        }
    }

    public void sendFiles() throws IOException {
        BufferedReader fileInfos = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(tmpFile)));
        try {
            String line;
            while (as.isReadyForDataTransfer()
                    && (line = fileInfos.readLine()) != null) {
                String[] ss = StringUtils.split(line, '\t');
                try {
                    send(new File(ss[4]), Long.parseLong(ss[3]), ss[1], ss[0], ss[2]);
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

    private void addFile(BufferedWriter fileInfos, File f, long endFmi,
            String cuid, String iuid, String ts) throws IOException {
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
        if (uidSuffix == null && attrs.isEmpty()) {
            FileInputStream in = new FileInputStream(f);
            try {
                in.skip(fmiEndPos);
                InputStreamDataWriter data = new InputStreamDataWriter(in);
                as.cstore(cuid, iuid, priority, data, ts, rspHandler(f));
            } finally {
                SafeClose.close(in);
            }
        } else {
            DicomInputStream in = new DicomInputStream(f);
            try {
                in.setIncludeBulkDataLocator(true);
                Attributes data = in.readDataset(-1, -1);
                if (uidSuffix != null ) {
                    data.setString(Tag.StudyInstanceUID, VR.UI,
                            data.getString(Tag.StudyInstanceUID) + uidSuffix);
                    data.setString(Tag.SeriesInstanceUID, VR.UI,
                            data.getString(Tag.SeriesInstanceUID) + uidSuffix);
                    data.setString(Tag.SOPInstanceUID, VR.UI, iuid += uidSuffix);
                }
                data.update(attrs, null);
                as.cstore(cuid, iuid, priority, new DataWriterAdapter(data), ts,
                        rspHandler(f));
            } finally {
                SafeClose.close(in);
            }
         }
    }

    private DimseRSPHandler rspHandler(final File f) {
        return new DimseRSPHandler(as.nextMessageID()) {
   
            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                StoreSCU.this.onCStoreRSP(cmd, f);
            }
        };
    }

    public void close() throws IOException, InterruptedException {
        if (as != null && as.isReadyForDataTransfer())
            as.release();
    }

    public void open()
            throws IOException, InterruptedException, IncompatibleConnectionException {
        as = ae.connect(conn, remote, rq);
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
