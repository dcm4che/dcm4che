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
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che.data.Attributes;
import org.dcm4che.data.ElementDictionary;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.io.DicomOutputStream;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Association;
import org.dcm4che.net.Commands;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.DimseRSPHandler;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.ExtendedNegotiation;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.tool.common.CLIUtils;
import org.dcm4che.util.SafeClose;
import org.dcm4che.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Main {

    private static final int RELATIONAL_QUERY = 1;
    private static final int DATETIME_MATCH = 2;
    private static final int FUZZY_MATCH = 4;
    private static final int TIMEZONE_MATCH = 8;

    private static enum SOPClass {
        PatientRoot(UID.PatientRootQueryRetrieveInformationModelFIND, 0, null),
        StudyRoot(UID.StudyRootQueryRetrieveInformationModelFIND, 0, null),
        PatientStudyOnly(
                UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired, 0, null),
        MWL(UID.ModalityWorklistInformationModelFIND, 3, "sps-0000.dcm"),
        UPSPull(UID.UnifiedProcedureStepPullSOPClass, 3, "ups-0000.dcm"),
        UPSWatch(UID.UnifiedProcedureStepWatchSOPClass, 3, "ups-0000.dcm"),
        HangingProtocol(UID.HangingProtocolInformationModelFIND, 3, "hp-0000.dcm"),
        ColorPalette(UID.ColorPaletteInformationModelFIND, 3, "pal-0000.dcm");

        final String cuid;
        final int defExtNeg;
        final String outputFileFormat;

        SOPClass(String cuid, int defExtNeg, String outputFileFormat) {
            this.cuid = cuid;
            this.defExtNeg = defExtNeg;
            this.outputFileFormat = outputFileFormat;
        }

        public String outputFileFormatOf(String level) {
            return outputFileFormat != null
                    ? outputFileFormat
                    : level.toLowerCase() + "-0000.dcm";
        }
    }

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che.tool.findscu.messages");

    private static String[] IVR_LE_FIRST = {
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRLittleEndian,
        UID.ExplicitVRBigEndian
    };

    private static String[] EVR_LE_FIRST = {
        UID.ExplicitVRLittleEndian,
        UID.ExplicitVRBigEndian,
        UID.ImplicitVRLittleEndian
    };

    private static String[] EVR_BE_FIRST = {
        UID.ExplicitVRBigEndian,
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian
    };

    private static String[] IVR_LE_ONLY = {
        UID.ImplicitVRLittleEndian
    };

    private final Device device = new Device("findscu");
    private final ApplicationEntity ae = new ApplicationEntity("FINDSCU");
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private int priority;
    private int cancelAfter;
    private SOPClass sopClass = SOPClass.StudyRoot;
    private String[] tss = IVR_LE_FIRST;
    private int extNeg;

    private File outDir;
    private DecimalFormat outFileFormat;
    private int[] outFilter;
    private Attributes keys = new Attributes();

    private Association as;
    private AtomicInteger totNumMatches = new AtomicInteger();

    public Main() {
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

    public final void setCancelAfter(int cancelAfter) {
        this.cancelAfter = cancelAfter;
    }

    public final void setSOPClass(SOPClass sopClass) {
        this.sopClass = sopClass;
    }

    public final void setTransferSyntaxes(String[] tss) {
        this.tss = tss.clone();
    }

    private void setExtendedNegotiation(int flag, boolean enable) {
        if (enable)
            extNeg |= flag;
        else
            extNeg &= ~flag;
    }

    public void setRelationalQueries(boolean enable) {
        setExtendedNegotiation(RELATIONAL_QUERY, enable);
    }

    public void setDatetimeMatching(boolean enable) {
        setExtendedNegotiation(DATETIME_MATCH, enable);
    }

    public void setFuzzySemanticMatching(boolean enable) {
        setExtendedNegotiation(FUZZY_MATCH, enable);
    }

    public void setTimezoneQueryAdjustment(boolean enable) {
        setExtendedNegotiation(TIMEZONE_MATCH, enable);
    }

    public final void setOutputDirectory(File outDir) {
        outDir.mkdirs();
        this.outDir = outDir;
    }

    public void setOutputFileFormat(String outFileFormat) {
        this.outFileFormat = new DecimalFormat(outFileFormat);
    }

    public void setOutputFilter(int[] outFilter) {
        Arrays.sort(outFilter);
        this.outFilter = outFilter;
    }

    public void addKey(int[] tags, String value) {
        Attributes item = keys;
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
        item.setString(tag, vr, value);
    }

    public void addKey(int tag, String value) {
        VR vr = ElementDictionary.vrOf(tag,
                keys.getPrivateCreator(tag));
        keys.setString(tag, vr, value);
    }

    private static CommandLine parseComandLine(String[] args)
                throws ParseException {
            Options opts = new Options();
            addServiceClassOptions(opts);
            addTransferSyntaxOptions(opts);
            addExtendedNegotionOptions(opts);
            addKeyOptions(opts);
            addOutputOptions(opts);
            addQueryLevelOption(opts);
            addCancelOption(opts);
            CLIUtils.addConnectOption(opts);
            CLIUtils.addBindOption(opts, "FINDSCU");
            CLIUtils.addAEOptions(opts, true, false);
            CLIUtils.addDimseRspOption(opts);
            CLIUtils.addPriorityOption(opts);
            CLIUtils.addCommonOptions(opts);
            return CLIUtils.parseComandLine(args, opts, rb, Main.class);
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
       opts.addOption(OptionBuilder
                .withLongOpt("out-attr")
                .hasArgs()
                .withArgName("attr")
                .withDescription(rb.getString("out-attr"))
                .create("o"));
    }

    @SuppressWarnings("static-access")
    private static void addServiceClassOptions(Options opts) {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder
                .withLongOpt("patient-root")
                .withDescription(rb.getString("patient-root"))
                .create("P"));
        group.addOption(OptionBuilder
                .withLongOpt("study-root")
                .withDescription(rb.getString("study-root"))
                .create("S"));
        group.addOption(OptionBuilder
                .withLongOpt("patient-study-only")
                .withDescription(rb.getString("patient-study-only"))
                .create("O"));
        group.addOption(OptionBuilder
                .withLongOpt("mwl")
                .withDescription(rb.getString("mwl"))
                .create("M"));
        group.addOption(OptionBuilder
                .withLongOpt("ups-pull")
                .withDescription(rb.getString("ups-pull"))
                .create("U"));
        group.addOption(OptionBuilder
                .withLongOpt("ups-watch")
                .withDescription(rb.getString("ups-watch"))
                .create("W"));
        group.addOption(OptionBuilder
                .withLongOpt("hanging-protocol")
                .withDescription(rb.getString("hanging-protocol"))
                .create("H"));
        group.addOption(OptionBuilder
                .withLongOpt("color-palette")
                .withDescription(rb.getString("color-palette"))
                .create("C"));
        opts.addOptionGroup(group);
    }

    private static void addExtendedNegotionOptions(Options opts) {
        opts.addOption(null, "relational", false, rb.getString("relational"));
        opts.addOption(null, "datetime", false, rb.getString("datetime"));
        opts.addOption(null, "fuzzy", false, rb.getString("fuzzy"));
        opts.addOption(null, "timezone", false, rb.getString("timezone"));
    }

    @SuppressWarnings("static-access")
    private static void addTransferSyntaxOptions(Options opts) {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder
                .withLongOpt("explicit-vr")
                .withDescription(rb.getString("explicit-vr"))
                .create());
        group.addOption(OptionBuilder
                .withLongOpt("big-endian")
                .withDescription(rb.getString("big-endian"))
                .create());
        group.addOption(OptionBuilder
                .withLongOpt("implicit-vr")
                .withDescription(rb.getString("implicit-vr"))
                .create());
       opts.addOptionGroup(group);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Main main = new Main();
            CLIUtils.configureConnect(main.remote, main.rq, cl);
            CLIUtils.configureBind(main.conn, main.ae, cl);
            CLIUtils.configure(main.conn, main.ae, cl);
            main.setSOPClass(sopClassOf(cl));
            configureKeys(main, cl);
            configureOutput(main, cl);
            configureCancel(main, cl);
            main.setTransferSyntaxes(tssOf(cl));
            main.setPriority(CLIUtils.priorityOf(cl));
            configureExtendedNegotiation(main, cl);
            ExecutorService executorService =
                    Executors.newSingleThreadExecutor();
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newSingleThreadScheduledExecutor();
            main.setExecutor(executorService);
            main.setScheduledExecutorService(scheduledExecutorService);
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

    private static void configureExtendedNegotiation(Main main, CommandLine cl) {
        main.setRelationalQueries(cl.hasOption("relational"));
        main.setDatetimeMatching(cl.hasOption("datetime"));
        main.setFuzzySemanticMatching(cl.hasOption("fuzzy"));
        main.setTimezoneQueryAdjustment(cl.hasOption("timezone"));
    }

    private static void configureOutput(Main main, CommandLine cl) {
        if (cl.hasOption("out-dir"))
            main.setOutputDirectory(new File(cl.getOptionValue("out-dir")));
        if (cl.hasOption("o"))
            main.setOutputFilter(CLIUtils.toTags(cl.getOptionValues("o")));
        main.setOutputFileFormat(cl.hasOption("out-file")
                ? cl.getOptionValue("out-file")
                : main.sopClass.outputFileFormatOf(cl.getOptionValue("L")));
    }

    private static void configureCancel(Main main, CommandLine cl) {
        if (cl.hasOption("cancel"))
            main.setCancelAfter(Integer.parseInt(cl.getOptionValue("cancel")));
    }

    private static void configureKeys(Main main, CommandLine cl) {
        if (cl.hasOption("r")) {
            String[] keys = cl.getOptionValues("r");
            for (int i = 0; i < keys.length; i++)
                main.addKey(CLIUtils.toTags(StringUtils.split(keys[i], '/')),
                        null);
        }
        if (cl.hasOption("m")) {
            String[] keys = cl.getOptionValues("m");
            for (int i = 1; i < keys.length; i++, i++)
                main.addKey(CLIUtils.toTags(StringUtils.split(keys[i-1], '/')),
                        keys[i]);
        }
        if (cl.hasOption("L"))
            main.addKey(Tag.QueryRetrieveLevel, cl.getOptionValue("L"));
    }

    private static SOPClass sopClassOf(CommandLine cl) throws ParseException {
        if (cl.hasOption("P")) {
            requiresQueryLevel("P", cl);
            return SOPClass.PatientRoot;
        }
        if (cl.hasOption("S")) {
            requiresQueryLevel("S", cl);
            return SOPClass.StudyRoot;
        }
        if (cl.hasOption("O")) {
            requiresQueryLevel("O", cl);
            return SOPClass.PatientStudyOnly;
        }
        if (cl.hasOption("M")) {
            noQueryLevel("M", cl);
            return SOPClass.MWL;
        }
        if (cl.hasOption("U")) {
            noQueryLevel("U", cl);
            return SOPClass.UPSPull;
        }
        if (cl.hasOption("W")) {
            noQueryLevel("W", cl);
            return SOPClass.UPSWatch;
        }
        if (cl.hasOption("H")) {
            noQueryLevel("H", cl);
            return SOPClass.HangingProtocol;
        }
        if (cl.hasOption("C")) {
            noQueryLevel("C", cl);
            return SOPClass.ColorPalette;
        }
        throw new ParseException(rb.getString("missing"));
    }

    private static void requiresQueryLevel(String opt, CommandLine cl)
            throws ParseException {
        if (!cl.hasOption("L"))
            throw new ParseException(
                    MessageFormat.format(rb.getString("missing-level"), opt));
    }

    private static void noQueryLevel(String opt, CommandLine cl)
            throws ParseException {
        if (cl.hasOption("L"))
            throw new ParseException(
                    MessageFormat.format(rb.getString("invalid-level"), opt));
    }

    private static String[] tssOf(CommandLine cl) {
        if (cl.hasOption("explicit-vr"))
            return EVR_LE_FIRST;
        if (cl.hasOption("big-endian"))
            return EVR_BE_FIRST;
        if (cl.hasOption("implicit-vr"))
            return IVR_LE_ONLY;
        return IVR_LE_FIRST;
    }

    public void open() throws IOException, InterruptedException {
        rq.addPresentationContext(
                new PresentationContext(1, sopClass.cuid, tss));
        if (extNeg > sopClass.defExtNeg)
            rq.addExtendedNegotiation(
                    new ExtendedNegotiation(sopClass.cuid,
                            toInfo(extNeg | sopClass.defExtNeg)));
        as = ae.connect(conn, remote.getHostname(), remote.getPort(), rq);
    }

    private static byte[] toInfo(int extNeg) {
        if (extNeg == 1)
            return new byte[] { 1 };
        byte[] info = new byte[(extNeg & 8) == 0 ? 3 : 4];
        for (int i = 0; i < info.length; i++, extNeg >>>= 1)
            info[i] = (byte) (extNeg & 1);
        return info;
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

            int cancelAfter = Main.this.cancelAfter;
            int numMatches;

            @Override
            public void onDimseRSP(Association as, Attributes cmd,
                    Attributes data) {
                super.onDimseRSP(as, cmd, data);
                int status = cmd.getInt(Tag.Status, -1);
                if (Commands.isPending(status )) {
                    Main.this.onResult(data);
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

        as.cfind(sopClass.cuid, priority, keys, null, rspHandler);
    }

    private void onResult(Attributes data) {
        int numMatches = totNumMatches.incrementAndGet();
        if (outDir != null) {
            File f = new File(outDir, fname(numMatches));
            DicomOutputStream dos = null;
            try {
                dos = new DicomOutputStream(new BufferedOutputStream(
                        new FileOutputStream(f)), UID.ImplicitVRLittleEndian);
                dos.writeDataset(null, filter(data));
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

    private Attributes filter(Attributes data) {
        return outFilter != null
                ? new Attributes(outFilter.length).addSelected(data, outFilter)
                : data;
    }

}
