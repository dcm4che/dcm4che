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

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che.data.Attributes;
import org.dcm4che.data.ElementDictionary;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Association;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.tool.common.CLIUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Main {

    private enum SOPClass {
        PatientRoot(UID.PatientRootQueryRetrieveInformationModelFIND),
        StudyRoot(UID.StudyRootQueryRetrieveInformationModelFIND),
        PatientStudyOnly(
                UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired),
        MWL(UID.ModalityWorklistInformationModelFIND),
        UPSPull(UID.UnifiedProcedureStepPullSOPClass),
        UPSWatch(UID.UnifiedProcedureStepWatchSOPClass),
        HP(UID.HangingProtocolInformationModelFIND);

        final String cuid;

        SOPClass(String cuid) {
            this.cuid = cuid;
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
    private SOPClass sopClass = SOPClass.StudyRoot;
    private String[] tss = IVR_LE_FIRST;

    private Attributes keys = new Attributes();
    private Association as;

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

    public final void setSOPClass(SOPClass sopClass) {
        this.sopClass = sopClass;
    }

    public final void setTransferSyntaxes(String[] tss) {
        this.tss = tss.clone();
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

    private static CommandLine parseComandLine(String[] args)
                throws ParseException {
            Options opts = new Options();
            addServiceClassOptions(opts);
            addTransferSyntaxOptions(opts);
            addKeyOptions(opts);
            CLIUtils.addConnectOption(opts);
            CLIUtils.addBindOption(opts, "STORESCU");
            CLIUtils.addAEOptions(opts, true, false);
            CLIUtils.addDimseRspOption(opts);
            CLIUtils.addPriorityOption(opts);
            CLIUtils.addCommonOptions(opts);
            return CLIUtils.parseComandLine(args, opts, rb, Main.class);
    }

    @SuppressWarnings("static-access")
    private static void addKeyOptions(Options opts) {
        opts.addOption(OptionBuilder
                .hasArgs()
                .withArgName("[seq/]attr=value")
                .withValueSeparator('=')
                .withDescription(rb.getString("matching-key"))
                .create("q"));
        opts.addOption(OptionBuilder
                .hasArgs()
                .withArgName("[seq/]attr")
                .withDescription(rb.getString("return-key"))
                .create("r"));
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
                .withLongOpt("hp")
                .withDescription(rb.getString("hp"))
                .create("H"));
        opts.addOptionGroup(group);
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

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Main main = new Main();
            CLIUtils.configureConnect(main.remote, main.rq, cl);
            CLIUtils.configureBind(main.conn, main.ae, cl);
            CLIUtils.configure(main.conn, main.ae, cl);
            configureKeys(main, cl);
            main.setSOPClass(sopClassOf(cl));
            main.setTransferSyntaxes(tssOf(cl));
            main.setPriority(CLIUtils.priorityOf(cl));
            ExecutorService executorService =
                    Executors.newSingleThreadExecutor();
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newSingleThreadScheduledExecutor();
            main.setExecutor(executorService);
            main.setScheduledExecutorService(scheduledExecutorService);
            try {
                main.open();
                main.query();
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

    private static void configureKeys(Main main, CommandLine cl) {
        if (cl.hasOption("r")) {
            String[] returnKeys = cl.getOptionValues("r");
            for (int i = 0; i < returnKeys.length; i++)
                main.addKey(CLIUtils.toTags(returnKeys[i]), null);
        }
        if (cl.hasOption("q")) {
            String[] matchingKeys = cl.getOptionValues("q");
            for (int i = 1; i < matchingKeys.length; i++, i++)
                main.addKey(CLIUtils.toTags(matchingKeys[i - 1]), matchingKeys[i]);
        }
    }

    private static SOPClass sopClassOf(CommandLine cl) throws ParseException {
        if (cl.hasOption("P"))
            return SOPClass.PatientRoot;
        if (cl.hasOption("S"))
            return SOPClass.StudyRoot;
        if (cl.hasOption("O"))
            return SOPClass.PatientStudyOnly;
        if (cl.hasOption("M"))
            return SOPClass.MWL;
        if (cl.hasOption("U"))
            return SOPClass.UPSPull;
        if (cl.hasOption("W"))
            return SOPClass.UPSWatch;
        if (cl.hasOption("H"))
            return SOPClass.HP;
        throw new ParseException(rb.getString("missing"));
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
        as = ae.connect(conn, remote.getHostname(), remote.getPort(), rq);
    }

    public void close() throws IOException, InterruptedException {
        if (as != null && as.isReadyForDataTransfer())
            as.release();
    }

    private void query() {
        // TODO Auto-generated method stub
        
    }

}
