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

package org.dcm4che.tool.findscu;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.List;
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
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.io.DicomOutputStream;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Association;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.DimseRSPHandler;
import org.dcm4che.net.IncompatibleConnectionException;
import org.dcm4che.net.QueryOption;
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.ExtendedNegotiation;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.tool.common.CLIUtils;
import org.dcm4che.util.SafeClose;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class FindSCU extends Device {

    private static enum InformationModel {
        PatientRoot(UID.PatientRootQueryRetrieveInformationModelFIND, "STUDY"),
        StudyRoot(UID.StudyRootQueryRetrieveInformationModelFIND, "STUDY"),
        PatientStudyOnly(UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired, "STUDY"),
        MWL(UID.ModalityWorklistInformationModelFIND, null),
        UPSPull(UID.UnifiedProcedureStepPullSOPClass, null),
        UPSWatch(UID.UnifiedProcedureStepWatchSOPClass, null),
        HangingProtocol(UID.HangingProtocolInformationModelFIND, null),
        ColorPalette(UID.ColorPaletteInformationModelFIND, null);

        final String cuid;
        final String level;

        InformationModel(String cuid, String level) {
            this.cuid = cuid;
            this.level = level;
       }

        public void adjustQueryOptions(EnumSet<QueryOption> queryOptions) {
            if (level == null) {
                queryOptions.add(QueryOption.RELATIONAL);
                queryOptions.add(QueryOption.DATETIME);
            }
        }
    }

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che.tool.findscu.messages");

    private final ApplicationEntity ae = new ApplicationEntity("FINDSCU");
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private int priority;
    private int cancelAfter;
    private InformationModel model;

    private File outDir;
    private DecimalFormat outFileFormat;
    private int[] inFilter;
    private Attributes keys = new Attributes();

    private Association as;
    private AtomicInteger totNumMatches = new AtomicInteger();

    public FindSCU() throws IOException {
        super("findscu");
        addConnection(conn);
        addApplicationEntity(ae);
        ae.addConnection(conn);
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public final void setInformationModel(InformationModel model, String[] tss,
            EnumSet<QueryOption> queryOptions) {
       this.model = model;
       rq.addPresentationContext(new PresentationContext(1, model.cuid, tss));
       if (!queryOptions.isEmpty()) {
           model.adjustQueryOptions(queryOptions);
           rq.addExtendedNegotiation(new ExtendedNegotiation(model.cuid, 
                   QueryOption.toExtendedNegotiationInformation(queryOptions)));
       }
       if (model.level != null)
           addLevel(model.level);
    }

    public void addLevel(String s) {
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, s);
    }

    public final void setCancelAfter(int cancelAfter) {
        this.cancelAfter = cancelAfter;
    }

    public final void setOutputDirectory(File outDir) {
        outDir.mkdirs();
        this.outDir = outDir;
    }

    public final void setOutputFileFormat(String outFileFormat) {
        this.outFileFormat = new DecimalFormat(outFileFormat);
    }

    public final void setInputFilter(int[] inFilter) {
        this.inFilter = inFilter;
    }

    private static CommandLine parseComandLine(String[] args)
                throws ParseException {
            Options opts = new Options();
            addServiceClassOptions(opts);
            addKeyOptions(opts);
            addOutputOptions(opts);
            addQueryLevelOption(opts);
            addCancelOption(opts);
            CLIUtils.addConnectOption(opts);
            CLIUtils.addBindOption(opts, "FINDSCU");
            CLIUtils.addAEOptions(opts);
            CLIUtils.addResponseTimeoutOption(opts);
            CLIUtils.addPriorityOption(opts);
            CLIUtils.addCommonOptions(opts);
            return CLIUtils.parseComandLine(args, opts, rb, FindSCU.class);
    }

    @SuppressWarnings("static-access")
    private static void addServiceClassOptions(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("name")
                .withDescription(rb.getString("model"))
                .create("M"));
        CLIUtils.addTransferSyntaxOptions(opts);
        opts.addOption(null, "relational", false, rb.getString("relational"));
        opts.addOption(null, "datetime", false, rb.getString("datetime"));
        opts.addOption(null, "fuzzy", false, rb.getString("fuzzy"));
        opts.addOption(null, "timezone", false, rb.getString("timezone"));
    }

    @SuppressWarnings("static-access")
    private static void addQueryLevelOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("PATIENT|STUDY|SERIES|IMAGE")
                .withDescription(rb.getString("level"))
                .create("L"));
   }

    @SuppressWarnings("static-access")
    private static void addCancelOption(Options opts) {
        opts.addOption(OptionBuilder
                .withLongOpt("cancel")
                .hasArg()
                .withArgName("num-matches")
                .withDescription(rb.getString("cancel"))
                .create());
    }

    @SuppressWarnings("static-access")
    private static void addKeyOptions(Options opts) {
        opts.addOption(OptionBuilder
                .hasArgs()
                .withArgName("[seq/]attr=value")
                .withValueSeparator('=')
                .withDescription(rb.getString("match"))
                .create("m"));
        opts.addOption(OptionBuilder
                .hasArgs()
                .withArgName("[seq/]attr")
                .withDescription(rb.getString("return"))
                .create("r"));
        opts.addOption(OptionBuilder
                .hasArgs()
                .withArgName("attr")
                .withDescription(rb.getString("in-attr"))
                .create("i"));
    }

    @SuppressWarnings("static-access")
    private static void addOutputOptions(Options opts) {
        opts.addOption(OptionBuilder
                .withLongOpt("out-dir")
                .hasArg()
                .withArgName("directory")
                .withDescription(rb.getString("out-dir"))
                .create());
        opts.addOption(OptionBuilder
                .withLongOpt("out-file")
                .hasArg()
                .withArgName("name")
                .withDescription(rb.getString("out-file"))
                .create());
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            FindSCU main = new FindSCU();
            CLIUtils.configureConnect(main.remote, main.rq, cl);
            CLIUtils.configureBind(main.conn, main.ae, cl);
            CLIUtils.configure(main.conn, cl);
            main.remote.setTlsProtocols(main.conn.getTlsProtocols());
            main.remote.setTlsCipherSuites(main.conn.getTlsCipherSuites());
            configureServiceClass(main, cl);
            configureKeys(main, cl);
            configureOutput(main, cl);
            configureCancel(main, cl);
            main.setPriority(CLIUtils.priorityOf(cl));
            ExecutorService executorService =
                    Executors.newSingleThreadExecutor();
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newSingleThreadScheduledExecutor();
            main.setExecutor(executorService);
            main.setScheduledExecutor(scheduledExecutorService);
            try {
                main.open();
                List<String> argList = cl.getArgList();
                if (argList.isEmpty())
                    main.query();
                else
                    for (String arg : argList)
                        main.query(new File(arg));
            } finally {
                main.close();
                executorService.shutdown();
                scheduledExecutorService.shutdown();
            }
       } catch (ParseException e) {
            System.err.println("findscu: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("findscu: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static EnumSet<QueryOption> queryOptionsOf(FindSCU main, CommandLine cl) {
        EnumSet<QueryOption> queryOptions = EnumSet.noneOf(QueryOption.class);
        if (cl.hasOption("relational"))
            queryOptions.add(QueryOption.RELATIONAL);
        if (cl.hasOption("datetime"))
            queryOptions.add(QueryOption.DATETIME);
        if (cl.hasOption("fuzzy"))
            queryOptions.add(QueryOption.FUZZY);
        if (cl.hasOption("timezone"))
            queryOptions.add(QueryOption.TIMEZONE);
        return queryOptions;
    }

    private static void configureOutput(FindSCU main, CommandLine cl) {
        if (cl.hasOption("out-dir"))
            main.setOutputDirectory(new File(cl.getOptionValue("out-dir")));
        main.setOutputFileFormat(cl.getOptionValue("out-file", "000.dcm"));
    }

    private static void configureCancel(FindSCU main, CommandLine cl) {
        if (cl.hasOption("cancel"))
            main.setCancelAfter(Integer.parseInt(cl.getOptionValue("cancel")));
    }

    private static void configureKeys(FindSCU main, CommandLine cl) {
        CLIUtils.addEmptyAttributes(main.keys, cl.getOptionValues("r"));
        CLIUtils.addAttributes(main.keys, cl.getOptionValues("m"));
        if (cl.hasOption("L"))
            main.addLevel(cl.getOptionValue("L"));
        if (cl.hasOption("i"))
            main.setInputFilter(CLIUtils.toTags(cl.getOptionValues("i")));
    }

    private static void configureServiceClass(FindSCU main, CommandLine cl) throws ParseException {
        main.setInformationModel(informationModelOf(cl), 
                CLIUtils.transferSyntaxesOf(cl), queryOptionsOf(main, cl));
    }

    private static InformationModel informationModelOf(CommandLine cl) throws ParseException {
        try {
            return cl.hasOption("M")
                    ? InformationModel.valueOf(cl.getOptionValue("M"))
                    : InformationModel.StudyRoot;
        } catch(IllegalArgumentException e) {
            throw new ParseException(
                    MessageFormat.format(
                            rb.getString("invalid-model-name"),
                            cl.getOptionValue("M")));
        }
    }

    public void open() throws IOException, InterruptedException,
            IncompatibleConnectionException, KeyManagementException {
        as = ae.connect(conn, remote, rq);
    }

    public void close() throws IOException, InterruptedException {
        if (as != null && as.isReadyForDataTransfer()) {
            as.waitForOutstandingRSP();
            as.release();
        }
    }

    public void query(File f) throws IOException, InterruptedException {
        Attributes attrs;
        DicomInputStream dis = null;
        try {
            attrs = new DicomInputStream(f).readDataset(-1, -1);
            if (inFilter != null) {
                attrs = new Attributes(inFilter.length + 1);
                attrs.addSelected(attrs, inFilter);
            }
        } finally {
            SafeClose.close(dis);
        }
        attrs.addAll(keys);
        query(attrs);
    }

   public void query() throws IOException, InterruptedException {
        query(keys);
    }

    private void query(Attributes keys) throws IOException, InterruptedException {
         DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            int cancelAfter = FindSCU.this.cancelAfter;
            int numMatches;

            @Override
            public void onDimseRSP(Association as, Attributes cmd,
                    Attributes data) {
                super.onDimseRSP(as, cmd, data);
                int status = cmd.getInt(Tag.Status, -1);
                if (Status.isPending(status)) {
                    FindSCU.this.onResult(data);
                    ++numMatches;
                    if (cancelAfter != 0 && numMatches >= cancelAfter)
                        try {
                            cancel(as);
                            cancelAfter = 0;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
        };

        as.cfind(model.cuid, priority, keys, null, rspHandler);
    }

    private void onResult(Attributes data) {
        int numMatches = totNumMatches.incrementAndGet();
        if (outDir != null) {
            File f = new File(outDir, fname(numMatches));
            DicomOutputStream dos = null;
            try {
                dos = new DicomOutputStream(new BufferedOutputStream(
                        new FileOutputStream(f)), UID.ImplicitVRLittleEndian);
                dos.writeDataset(null, data);
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                SafeClose.close(dos);
            }
        }
    }

    private String fname(int i) {
        synchronized (outFileFormat) {
            return outFileFormat.format(i);
        }
    }

}
