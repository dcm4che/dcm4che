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

package org.dcm4che3.tool.movescu;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.*;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.SafeClose;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class MoveSCU extends Device {

    private static enum InformationModel {
        PatientRoot(UID.PatientRootQueryRetrieveInformationModelMove, "STUDY"),
        StudyRoot(UID.StudyRootQueryRetrieveInformationModelMove, "STUDY"),
        PatientStudyOnly(UID.PatientStudyOnlyQueryRetrieveInformationModelMove, "STUDY"),
        CompositeInstanceRoot(UID.CompositeInstanceRootRetrieveMove, "IMAGE"),
        HangingProtocol(UID.HangingProtocolInformationModelMove, null),
        ColorPalette(UID.ColorPaletteQueryRetrieveInformationModelMove, null);

        final String cuid;
        final String level;

        InformationModel(String cuid, String level) {
            this.cuid = cuid;
            this.level = level;
       }
    }

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che3.tool.movescu.messages");

    private static final int[] DEF_IN_FILTER = {
        Tag.SOPInstanceUID,
        Tag.StudyInstanceUID,
        Tag.SeriesInstanceUID
    };

    private final ApplicationEntity ae = new ApplicationEntity("MOVESCU");
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private int priority;
    private String destination;
    private InformationModel model;
    private Attributes keys = new Attributes();
    private int[] inFilter = DEF_IN_FILTER;
    private Association as;
    private int cancelAfter;
    private boolean releaseEager;
    private ScheduledFuture<?> scheduledCancel;

    public MoveSCU() throws IOException {
        super("movescu");
        addConnection(conn);
        addApplicationEntity(ae);
        ae.addConnection(conn);
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public void setCancelAfter(int cancelAfter) {
        this.cancelAfter = cancelAfter;
    }

    public void setReleaseEager(boolean releaseEager) {
        this.releaseEager = releaseEager;
    }

    public final void setInformationModel(InformationModel model, String[] tss,
            boolean relational) {
       this.model = model;
       rq.addPresentationContext(new PresentationContext(1, model.cuid, tss));
       if (relational)
           rq.addExtendedNegotiation(new ExtendedNegotiation(model.cuid, new byte[]{1}));
       if (model.level != null)
           addLevel(model.level);
    }

    public void addLevel(String s) {
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, s);
    }

    public final void setDestination(String destination) {
        this.destination = destination;
    }

    public void addKey(int tag, String... ss) {
        VR vr = ElementDictionary.vrOf(tag, keys.getPrivateCreator(tag));
        keys.setString(tag, vr, ss);
    }

    public final void setInputFilter(int[] inFilter) {
        this.inFilter  = inFilter;
    }

    private static CommandLine parseComandLine(String[] args)
                throws ParseException {
            Options opts = new Options();
            addServiceClassOptions(opts);
            addKeyOptions(opts);
            addRetrieveLevelOption(opts);
            addDestinationOption(opts);
            addCancelAfterOption(opts);
            addRetrieveEagerOption(opts);
            CLIUtils.addConnectOption(opts);
            CLIUtils.addBindClientOption(opts, "MOVESCU");
            CLIUtils.addAEOptions(opts);
            CLIUtils.addSendTimeoutOption(opts);
            CLIUtils.addRetrieveTimeoutOption(opts);
            CLIUtils.addPriorityOption(opts);
            CLIUtils.addCommonOptions(opts);
            return CLIUtils.parseComandLine(args, opts, rb, MoveSCU.class);
    }

    private static void addRetrieveLevelOption(Options opts) {
        opts.addOption(Option.builder("L")
                .hasArg()
                .argName("PATIENT|STUDY|SERIES|IMAGE|FRAME")
                .desc(rb.getString("level"))
                .build());
   }

    private static void addDestinationOption(Options opts) {
        opts.addOption(Option.builder()
                .longOpt("dest")
                .hasArg()
                .argName("aet")
                .desc(rb.getString("dest"))
                .build());
        
    }

    private static void addCancelAfterOption(Options opts) {
        opts.addOption(Option.builder()
                .hasArg()
                .argName("ms")
                .desc(rb.getString("cancel-after"))
                .longOpt("cancel-after")
                .build());
    }

    private static void addRetrieveEagerOption(Options opts) {
        opts.addOption(null, "release-eager", false, rb.getString("release-eager"));
    }

    private static void addKeyOptions(Options opts) {
        opts.addOption(Option.builder("m")
                .hasArgs()
                .argName("[seq.]attr=value")
                .desc(rb.getString("match"))
                .build());
        opts.addOption(Option.builder("i")
                .hasArgs()
                .argName("attr")
                .desc(rb.getString("in-attr"))
                .build());
    }

    private static void addServiceClassOptions(Options opts) {
        opts.addOption(Option.builder("M")
                .hasArg()
                .argName("name")
                .desc(rb.getString("model"))
                .build());
        CLIUtils.addTransferSyntaxOptions(opts);
        opts.addOption(null, "relational", false, rb.getString("relational"));
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            MoveSCU main = new MoveSCU();
            CLIUtils.configureConnect(main.remote, main.rq, cl);
            CLIUtils.configureBind(main.conn, main.ae, cl);
            CLIUtils.configure(main.conn, cl);
            main.remote.setTlsProtocols(main.conn.getTlsProtocols());
            main.remote.setTlsCipherSuites(main.conn.getTlsCipherSuites());
            configureServiceClass(main, cl);
            configureKeys(main, cl);
            main.setPriority(CLIUtils.priorityOf(cl));
            main.setDestination(destinationOf(cl));
            main.setCancelAfter(CLIUtils.getIntOption(cl, "cancel-after", 0));
            main.setReleaseEager(cl.hasOption("release-eager"));
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
                    main.retrieve();
                else
                    for (String arg : argList)
                        main.retrieve(new File(arg));
            } finally {
                main.close();
                executorService.shutdown();
                scheduledExecutorService.shutdown();
            }
       } catch (ParseException e) {
            System.err.println("movescu: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("movescu: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void configureServiceClass(MoveSCU main, CommandLine cl) throws ParseException {
        main.setInformationModel(informationModelOf(cl),
                CLIUtils.transferSyntaxesOf(cl), cl.hasOption("relational"));
    }

    private static String destinationOf(CommandLine cl) throws ParseException {
        if (cl.hasOption("dest"))
            return cl.getOptionValue("dest");
        throw new ParseException(rb.getString("missing-dest"));
    }

    private static void configureKeys(MoveSCU main, CommandLine cl) {
        CLIUtils.addAttributes(main.keys, cl.getOptionValues("m"));
        if (cl.hasOption("L"))
            main.addLevel(cl.getOptionValue("L"));
        if (cl.hasOption("i"))
            main.setInputFilter(CLIUtils.toTags(cl.getOptionValues("i")));
    }

    private static InformationModel informationModelOf(CommandLine cl) throws ParseException {
        try {
            return cl.hasOption("M")
                    ? InformationModel.valueOf(cl.getOptionValue("M"))
                    : InformationModel.StudyRoot;
        } catch(IllegalArgumentException e) {
            throw new ParseException(MessageFormat.format(
                    rb.getString("invalid-model-name"),
                    cl.getOptionValue("M")));
        }
    }

    public void open() throws IOException, InterruptedException,
            IncompatibleConnectionException, GeneralSecurityException {
        as = ae.connect(conn, remote, rq);
    }

    public void close() throws IOException, InterruptedException {
        if (scheduledCancel != null && releaseEager) { // release by scheduler thread
            return;
        }
        if (as != null && as.isReadyForDataTransfer()) {
            if (!releaseEager) {
                as.waitForOutstandingRSP();
            }
            as.release();
        }
    }

    public void retrieve(File f) throws IOException, InterruptedException {
        Attributes attrs = new Attributes();
        DicomInputStream dis = null;
        try {
            attrs.addSelected(new DicomInputStream(f).readDataset(), inFilter);
        } finally {
            SafeClose.close(dis);
        }
        attrs.addAll(keys);
        retrieve(attrs);
    }

    public void retrieve() throws IOException, InterruptedException {
        retrieve(keys);
    }

    private void retrieve(Attributes keys) throws IOException, InterruptedException {
        final DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd,
                    Attributes data) {
                super.onDimseRSP(as, cmd, data);
            }
        };

        as.cmove(model.cuid, priority, keys, null, destination, rspHandler);
        if (cancelAfter > 0) {
            scheduledCancel = schedule(new Runnable() {
                                                       @Override
                                                       public void run() {
                                                           try {
                                                               rspHandler.cancel(as);
                                                               if (releaseEager) {
                                                                   as.release();
                                                               }
                                                           } catch (IOException e) {
                                                               e.printStackTrace();
                                                           }
                                                       }
                                                   },
                    cancelAfter,
                    TimeUnit.MILLISECONDS);
        }
    }

}
