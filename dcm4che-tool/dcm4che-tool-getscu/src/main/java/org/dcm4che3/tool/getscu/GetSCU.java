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

package org.dcm4che3.tool.getscu;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.RoleSelection;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class GetSCU {
    
    private static final Logger LOG = LoggerFactory.getLogger(GetSCU.class); 

    public static enum InformationModel {
        PatientRoot(UID.PatientRootQueryRetrieveInformationModelGET, "STUDY"),
        StudyRoot(UID.StudyRootQueryRetrieveInformationModelGET, "STUDY"),
        PatientStudyOnly(UID.PatientStudyOnlyQueryRetrieveInformationModelGETRetired, "STUDY"),
        CompositeInstanceRoot(UID.CompositeInstanceRootRetrieveGET, "IMAGE"),
        WithoutBulkData(UID.CompositeInstanceRetrieveWithoutBulkDataGET, null),
        HangingProtocol(UID.HangingProtocolInformationModelGET, null),
        ColorPalette(UID.ColorPaletteQueryRetrieveInformationModelGET, null);

        final String cuid;
        final String level;

        InformationModel(String cuid, String level) {
            this.cuid = cuid;
            this.level = level;
       }
    }

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che3.tool.getscu.messages");

    private static final int[] DEF_IN_FILTER = {
        Tag.SOPInstanceUID,
        Tag.StudyInstanceUID,
        Tag.SeriesInstanceUID
    };

    private final Device device = new Device("getscu");
    private final ApplicationEntity ae;
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private int priority;
    private InformationModel model;
    private File storageDir;
    private Attributes keys = new Attributes();
    private int[] inFilter = DEF_IN_FILTER;
    private Association as;

    private BasicCStoreSCP storageSCP = new BasicCStoreSCP("*") {

        @Override
        protected void store(Association as, PresentationContext pc, Attributes rq,
                PDVInputStream data, Attributes rsp)
                throws IOException {
            if (storageDir == null)
                return;

            String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            String tsuid = pc.getTransferSyntax();
            File file = new File(storageDir, iuid );
            try {
                storeTo(as, as.createFileMetaInformation(iuid, cuid, tsuid),
                        data, file);
            } catch (Exception e) {
                throw new DicomServiceException(Status.ProcessingFailure, e);
            }

        }


    };

    public GetSCU() throws IOException {
        ae = new ApplicationEntity("GETSCU");
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
        device.setDimseRQHandler(createServiceRegistry());
    }
    
    public ApplicationEntity getApplicationEntity() {
        return ae;
    }

    public Connection getRemoteConnection() {
        return remote;
    }
    
    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }
    
    public Association getAssociation() {
        return as;
    }

    public Device getDevice() {
        return device;
    }    
    
    public Attributes getKeys() {
        return keys;
    }
    
    private void storeTo(Association as, Attributes fmi, 
            PDVInputStream data, File file) throws IOException  {
        LOG.info("{}: M-WRITE {}", as, file);
        file.getParentFile().mkdirs();
        DicomOutputStream out = new DicomOutputStream(file);
        try {
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
        } finally {
            SafeClose.close(out);
        }
    }

    private DicomServiceRegistry createServiceRegistry() {
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(storageSCP);
        return serviceRegistry;
    }

    public void setStorageDirectory(File storageDir) {
        if (storageDir != null)
            if (storageDir.mkdirs())
                System.out.println("M-WRITE " + storageDir);
        this.storageDir = storageDir;
    }

    public final void setPriority(int priority) {
        this.priority = priority;
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
            addStorageDirectoryOptions(opts);
            CLIUtils.addConnectOption(opts);
            CLIUtils.addBindOption(opts, "GETSCU");
            CLIUtils.addAEOptions(opts);
            CLIUtils.addRetrieveTimeoutOption(opts);
            CLIUtils.addPriorityOption(opts);
            CLIUtils.addCommonOptions(opts);
            return CLIUtils.parseComandLine(args, opts, rb, GetSCU.class);
    }

    @SuppressWarnings("static-access")
    private static void addRetrieveLevelOption(Options opts) {
        opts.addOption(Option.builder("L")
                .hasArg()
                .argName("PATIENT|STUDY|SERIES|IMAGE|FRAME")
                .desc(rb.getString("level"))
                .build());
   }

    @SuppressWarnings("static-access")
    private static void addStorageDirectoryOptions(Options opts) {
        opts.addOption(null, "ignore", false,
                rb.getString("ignore"));
        opts.addOption(Option.builder()
                .hasArg()
                .argName("path")
                .desc(rb.getString("directory"))
                .longOpt("directory")
                .build());
    }

    @SuppressWarnings("static-access")
    private static void addKeyOptions(Options opts) {
        opts.addOption(Option.builder("m")
                .hasArgs()
                .argName("attr=value")
                .valueSeparator('=')
                .desc(rb.getString("match"))
                .build());
        opts.addOption(Option.builder("i")
                .hasArgs()
                .argName("attr")
                .desc(rb.getString("in-attr"))
                .build());
    }

    @SuppressWarnings("static-access")
    private static void addServiceClassOptions(Options opts) {
        opts.addOption(Option.builder("M")
                .hasArg()
                .argName("name")
                .desc(rb.getString("model"))
                .build());
        opts.addOption(null, "relational", false, rb.getString("relational"));
        CLIUtils.addTransferSyntaxOptions(opts);
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


    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            GetSCU main = new GetSCU();
            CLIUtils.configureConnect(main.remote, main.rq, cl);
            CLIUtils.configureBind(main.conn, main.ae, cl);
            CLIUtils.configure(main.conn, cl);
            main.remote.setTlsProtocols(main.conn.getTlsProtocols());
            main.remote.setTlsCipherSuites(main.conn.getTlsCipherSuites());
            configureServiceClass(main, cl);
            configureKeys(main, cl);
            main.setPriority(CLIUtils.priorityOf(cl));
            configureStorageDirectory(main, cl);
            ExecutorService executorService =
                    Executors.newSingleThreadExecutor();
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newSingleThreadScheduledExecutor();
            main.device.setExecutor(executorService);
            main.device.setScheduledExecutor(scheduledExecutorService);
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
            System.err.println("getscu: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("getscu: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void configureServiceClass(GetSCU main, CommandLine cl)
            throws Exception {
        main.setInformationModel(informationModelOf(cl),
                CLIUtils.transferSyntaxesOf(cl), cl.hasOption("relational"));
        String[] pcs = cl.getOptionValues("store-tc");
        if (pcs != null)
            for (String pc : pcs) {
                String[] ss = StringUtils.split(pc, ':');
                configureStorageSOPClass(main, ss[0], ss[1]);
            }
        String[] files = cl.getOptionValues("store-tcs");
        if (pcs == null && files == null)
            files = new String[] { "resource:store-tcs.properties" };
        if (files != null)
            for (String file : files) {
                Properties p = CLIUtils.loadProperties(file, null);
                Set<Entry<Object, Object>> entrySet = p.entrySet();
                for (Entry<Object, Object> entry : entrySet)
                    configureStorageSOPClass(main, (String) entry.getKey(), (String) entry.getValue());
            }
    }

    private static void configureStorageSOPClass(GetSCU main, String cuid, String tsuids0) {
        String[] tsuids1 = StringUtils.split(tsuids0, ';');
        for (String tsuids2 : tsuids1) {
            main.addOfferedStorageSOPClass(CLIUtils.toUID(cuid), CLIUtils.toUIDs(tsuids2));
        }
    }

     public void addOfferedStorageSOPClass(String cuid, String... tsuids) {
        if (!rq.containsPresentationContextFor(cuid))
            rq.addRoleSelection(new RoleSelection(cuid, false, true));
        rq.addPresentationContext(new PresentationContext(
                2 * rq.getNumberOfPresentationContexts() + 1, cuid, tsuids));
    }

    private static void configureStorageDirectory(GetSCU main, CommandLine cl) {
        if (!cl.hasOption("ignore")) {
            main.setStorageDirectory(
                    new File(cl.getOptionValue("directory", ".")));
        }
    }

    private static void configureKeys(GetSCU main, CommandLine cl) {
        if (cl.hasOption("m")) {
            String[] keys = cl.getOptionValues("m");
            for (int i = 1; i < keys.length; i++, i++)
                main.addKey(CLIUtils.toTag(keys[i - 1]), StringUtils.split(keys[i], '/'));
        }
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
            throw new ParseException(
                    MessageFormat.format(
                            rb.getString("invalid-model-name"),
                            cl.getOptionValue("M")));
        }
    }

    public void open() throws IOException, InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
        as = ae.connect(conn, remote, rq);
    }

    public void close() throws IOException, InterruptedException {
        if (as != null && as.isReadyForDataTransfer()) {
            as.waitForOutstandingRSP();
            as.release();
        }
    }

    public void retrieve(File f) throws IOException, InterruptedException {
        Attributes attrs = new Attributes();
        DicomInputStream dis = null;
        try {
            attrs.addSelected(new DicomInputStream(f).readDataset(-1, -1), inFilter);
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
         DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd,
                    Attributes data) {
                super.onDimseRSP(as, cmd, data);
            }
        };

        retrieve (keys, rspHandler);
    }
    
    public void retrieve(DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        retrieve(keys, rspHandler);
    }
    
    private void retrieve(Attributes keys, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        as.cget(model.cuid, priority, keys, null, rspHandler);
    }

}
