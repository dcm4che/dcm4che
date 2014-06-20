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

package org.dcm4che3.tool.probetc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.print.attribute.standard.Destination;
import javax.sound.midi.Track;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.ldap.LdapDicomConfiguration;
import org.dcm4che3.conf.prefs.PreferencesDicomConfiguration;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.net.pdu.AAssociateAC;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.UserIdentityRQ;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */
/*
 * usage as a testtool ProbeTC ptc = new ProbeTC("STORESCP@0.0.0.0:11115",
 * "STORESCP", "DCM4CHEE-PROXY", "STGCMTSCU", "prefs" ,null); ptc.probeAndSet();
 */
public class ProbeTC {

    private String destinationAET;
    private static String probedAET;
    private String sourceAET;
    private ApplicationEntity probedAE;
    private File ldapConfigurationFile;
    private String configurationType;
    private String configType;
    private Properties ldapProps;
    private String callingAET;
    static final Logger LOG = LoggerFactory.getLogger(ProbeTC.class);
    private static Options opts;
    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.probetc.messages");
    private Connection destination;
    private AAssociateRQ rq = new AAssociateRQ();

    ProbeTC() {
    }

    public ProbeTC(String probedae, String destinationaet, String sourceaet,
            String callingaet, String configurationType,
            File ldapConfigurationFile) throws ParseException {
        if (probedae == null || destinationaet == null || sourceaet == null
                || callingaet == null || configurationType == null) {
            LOG.error("null initialization parameter");
            throw new NullPointerException();
        }
        this.setConfigType(configurationType);
        this.setDestinationAET(destinationaet);

        if (ldapConfigurationFile != null
                && configurationType.compareToIgnoreCase("ldap") == 0)
            this.setLdapConfigurationFile(ldapConfigurationFile);
        else if (configurationType.compareToIgnoreCase("ldap") == 0) {
            LOG.error("Ldap properties file has to be a valid file");
            throw new NullPointerException();
        }
        this.setSourceAET(sourceaet);
        String tmpOption = probedae;
        String aeTitle = tmpOption.split("@")[0];

        if (tmpOption.split("@")[1] == null)
            throw new ParseException(rb.getString("invalid-probed-ae"));
        String host = tmpOption.split("@")[1].split(":")[0];
        int port = Integer.parseInt(tmpOption.split("@")[1].split(":")[1]);
        probedAET = aeTitle;
        Connection conn = new Connection();
        conn.setHostname(host);
        conn.setPort(port);
        conn.setInstalled(true);
        setDestination(conn);

        this.setProbedAE(new ApplicationEntity(aeTitle));
        this.getProbedAE().addConnection(conn);
        this.setCallingAET(callingaet);
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        opts = new Options();
        opts.addOption(OptionBuilder.hasArg().withArgName("aet@host:port")
                .withDescription(rb.getString("probed-ae"))
                .withLongOpt("probed-ae").create("p"));
        opts.addOption("d", "destination-aet", true,
                rb.getString("destination-aet"));
        opts.addOption("s", "source-aet", true, rb.getString("source-aet"));
        opts.addOption("t", "Title", true, rb.getString("Title"));
        opts.addOption("c", "configuration", true,
                rb.getString("configuration"));
        opts.addOption("f", "file", true, rb.getString("file"));
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, ProbeTC.class);
    }

    public void probeAndSet() {
        ProbeTC instance = this;
        Device device = new Device(instance.getCallingAET());
        Connection conn = new Connection();
        device.addConnection(conn);
        ApplicationEntity ae = new ApplicationEntity(instance.getCallingAET());
        device.addApplicationEntity(ae);
        ae.addConnection(conn);

        conn.setReceivePDULength(Connection.DEF_MAX_PDU_LENGTH);
        conn.setSendPDULength(Connection.DEF_MAX_PDU_LENGTH);

        conn.setMaxOpsInvoked(0);
        conn.setMaxOpsPerformed(0);

        conn.setPackPDV(true);
        conn.setConnectTimeout(0);
        conn.setRequestTimeout(0);
        conn.setAcceptTimeout(0);
        conn.setReleaseTimeout(0);
        conn.setResponseTimeout(0);
        conn.setRetrieveTimeout(0);
        conn.setIdleTimeout(0);
        conn.setSocketCloseDelay(Connection.DEF_SOCKETDELAY);
        conn.setSendBufferSize(0);
        conn.setReceiveBufferSize(0);
        conn.setTcpNoDelay(true);

        // no tls in this implementation (for tls use command line tool)
        if (instance.getConfigType().compareToIgnoreCase("ldap") == 0) {
            InputStream is = null;
            try {
                is = new FileInputStream(instance.getLdapConfigurationFile());
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Properties p = new Properties();
            try {
                p.load(is);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                LdapDicomConfiguration conf = new LdapDicomConfiguration(p);
                LOG.info("Started Loading LDAP configuration");
                ApplicationEntity sourceAE = conf
                        .findApplicationEntity(instance.sourceAET);
                ArrayList<TransferCapability> tcs = (ArrayList<TransferCapability>) sourceAE
                        .getTransferCapabilities();
                ArrayList<PresentationContext> pcs = addChunkedPCsandSend(ae,
                        device, instance, tcs);
                // print accepted ones
                ArrayList<PresentationContext> acceptedPCs = new ArrayList<PresentationContext>();
                for (PresentationContext pc : pcs)
                    if (pc.isAccepted())
                        acceptedPCs.add(pc);

                ApplicationEntity destinationAE = conf
                        .findApplicationEntity(instance.destinationAET);
                Device toStore = destinationAE.getDevice();
                TransferCapability[] TCs = mergeTCs(acceptedPCs);
                for (TransferCapability tc : TCs)
                    toStore.getApplicationEntity(instance.destinationAET)
                            .addTransferCapability(tc);

                conf.merge(toStore);
                conf.close();
                return;
            } catch (ConfigurationException e) {
                LOG.error("Configuration backend error - {}", e);
            }
        } else {

            try {
                PreferencesDicomConfiguration conf = new PreferencesDicomConfiguration();
                LOG.info("Started Loading LDAP configuration");
                ApplicationEntity sourceAE = conf
                        .findApplicationEntity(instance.sourceAET);
                ArrayList<TransferCapability> tcs = (ArrayList<TransferCapability>) sourceAE
                        .getTransferCapabilities();
                ArrayList<PresentationContext> pcs = addChunkedPCsandSend(ae,
                        device, instance, tcs);
                // print accepted ones
                ArrayList<PresentationContext> acceptedPCs = new ArrayList<PresentationContext>();
                for (PresentationContext pc : pcs)
                    if (pc.isAccepted())
                        acceptedPCs.add(pc);

                ApplicationEntity destinationAE = conf
                        .findApplicationEntity(instance.destinationAET);
                Device toStore = destinationAE.getDevice();
                TransferCapability[] TCs = mergeTCs(acceptedPCs);
                for (TransferCapability tc : TCs)
                    toStore.getApplicationEntity(instance.destinationAET)
                            .addTransferCapability(tc);

                conf.merge(toStore);
                conf.close();
                return;
            } catch (ConfigurationException e) {
                LOG.error("Configuration backend error - {}", e);
            }
        }

    }

    public static void main(String[] args) throws ParseException, IOException,
            InterruptedException, IncompatibleConnectionException,
            GeneralSecurityException {
        CommandLine cl = null;

        cl = parseComandLine(args);
        ProbeTC instance = new ProbeTC();
        Device device = null;
        if (cl.hasOption("t")) {
            device = new Device(cl.getOptionValue("t").toLowerCase());
        } else {
            LOG.error("Missing AETitle");
        }
        Connection conn = new Connection();
        device.addConnection(conn);
        ApplicationEntity ae = new ApplicationEntity(cl.getOptionValue("t")
                .toUpperCase());
        if (cl.hasOption("d")) {
            instance.setDestinationAET(cl.getOptionValue("d"));
        } else {
            LOG.error("Missing destination AET");
        }
        if (cl.hasOption("s")) {
            instance.setSourceAET(cl.getOptionValue("s"));
        } else {
            LOG.error("Missing source AET");
        }
        device.addApplicationEntity(ae);
        ae.addConnection(conn);

        instance.destination = new Connection();

        configureConnect(instance.destination, instance.rq, cl, ae);
        CLIUtils.configure(conn, cl);
        // instance.sourceAE = new
        // ApplicationEntity(cl.getOptionValue("s").split(":")[0]);

        // here load the TCs
        if (cl.hasOption("c")) {
            if (cl.getOptionValue("c").compareToIgnoreCase("ldap") == 0) {
                try {
                    InputStream is = null;
                    Properties p = new Properties();
                    if (cl.hasOption("f")) {
                        is = new FileInputStream(new File(
                                cl.getOptionValue("f")));
                    } else {
                        LOG.info("No ldap.properties file specified using default file from etc");
                        is = new FileInputStream(new File(
                                "../etc/probetc/ldap.properties"));
                    }
                    p.load(is);
                    LdapDicomConfiguration conf = new LdapDicomConfiguration(p);
                    LOG.info("Started Loading LDAP configuration");
                    ApplicationEntity sourceAE = conf
                            .findApplicationEntity(instance.sourceAET);
                    ArrayList<TransferCapability> tcs = (ArrayList<TransferCapability>) sourceAE
                            .getTransferCapabilities();
                    ArrayList<PresentationContext> pcs = addChunkedPCsandSend(
                            ae, device, instance, tcs);
                    // print accepted ones
                    ArrayList<PresentationContext> acceptedPCs = new ArrayList<PresentationContext>();
                    for (PresentationContext pc : pcs)
                        if (pc.isAccepted())
                            acceptedPCs.add(pc);

                    LOG.info("Probed the source ae and found the following accepted presentation contexts");
                    for (PresentationContext pc : acceptedPCs) {
                        LOG.info("PC[" + pc.getPCID() + "]\tAbstractSyntax:"
                                + pc.getAbstractSyntax() + "\n with "
                                + " the following Transfer-Syntax:["
                                + pc.getTransferSyntax() + "]");

                    }
                    LOG.info("finished probing TCs");
                    LOG.info("Adding Accepted TCs to configuration backend");
                    ApplicationEntity destinationAE = conf
                            .findApplicationEntity(instance.destinationAET);
                    Device toStore = destinationAE.getDevice();
                    TransferCapability[] TCs = mergeTCs(acceptedPCs);
                    for (TransferCapability tc : TCs)
                        toStore.getApplicationEntity(instance.destinationAET)
                                .addTransferCapability(tc);

                    conf.merge(toStore);
                    logAddedTCs(TCs, destinationAE);
                    conf.close();
                    System.exit(1);
                } catch (ConfigurationException e) {
                    LOG.error("Configuration backend error - {}", e);
                }
            } else if (cl.getOptionValue("c").compareToIgnoreCase("prefs") == 0) {
                // prefs
                try {

                    PreferencesDicomConfiguration conf = new PreferencesDicomConfiguration();
                    LOG.info("Started Loading LDAP configuration");
                    ApplicationEntity sourceAE = conf
                            .findApplicationEntity(instance.sourceAET);
                    ArrayList<TransferCapability> tcs = (ArrayList<TransferCapability>) sourceAE
                            .getTransferCapabilities();
                    ArrayList<PresentationContext> pcs = addChunkedPCsandSend(
                            ae, device, instance, tcs);
                    // print accepted ones
                    ArrayList<PresentationContext> acceptedPCs = new ArrayList<PresentationContext>();
                    for (PresentationContext pc : pcs)
                        if (pc.isAccepted())
                            acceptedPCs.add(pc);

                    LOG.info("Probed the source ae and found the following accepted presentation contexts");
                    for (PresentationContext pc : acceptedPCs) {
                        LOG.info("PC[" + pc.getPCID() + "]\tAbstractSyntax:"
                                + pc.getAbstractSyntax() + "\n with "
                                + " the following Transfer-Syntax:["
                                + pc.getTransferSyntax() + "]");

                    }
                    LOG.info("finished probing TCs");
                    LOG.info("Adding Accepted TCs to configuration backend");
                    ApplicationEntity destinationAE = conf
                            .findApplicationEntity(instance.destinationAET);
                    Device toStore = destinationAE.getDevice();
                    TransferCapability[] TCs = mergeTCs(acceptedPCs);
                    for (TransferCapability tc : TCs)
                        toStore.getApplicationEntity(instance.destinationAET)
                                .addTransferCapability(tc);

                    conf.merge(toStore);
                    logAddedTCs(TCs, destinationAE);
                    conf.close();
                    System.exit(1);
                } catch (ConfigurationException e) {
                    LOG.error("Configuration backend error - {}", e);
                }
            }
        } else {
            // ldap
            try {
                InputStream is = null;
                Properties p = new Properties();
                if (cl.hasOption("f")) {
                    is = new FileInputStream(new File(cl.getOptionValue("f")));
                } else {
                    LOG.info("No ldap.properties file specified using default file from etc");
                    is = new FileInputStream(new File(
                            "../etc/probetc/ldap.properties"));
                }
                p.load(is);
                LdapDicomConfiguration conf = new LdapDicomConfiguration(p);
                LOG.info("Started Loading LDAP configuration");
                ApplicationEntity sourceAE = conf
                        .findApplicationEntity(instance.sourceAET);
                ArrayList<TransferCapability> tcs = (ArrayList<TransferCapability>) sourceAE
                        .getTransferCapabilities();
                ArrayList<PresentationContext> pcs = addChunkedPCsandSend(ae,
                        device, instance, tcs);
                // print accepted ones
                ArrayList<PresentationContext> acceptedPCs = new ArrayList<PresentationContext>();
                for (PresentationContext pc : pcs)
                    if (pc.isAccepted())
                        acceptedPCs.add(pc);

                LOG.info("Probed the source ae and found the following accepted presentation contexts");
                for (PresentationContext pc : acceptedPCs) {
                    LOG.info("PC[" + pc.getPCID() + "]\tAbstractSyntax:"
                            + pc.getAbstractSyntax() + "\n with "
                            + " the following Transfer-Syntax:["
                            + pc.getTransferSyntax() + "]");

                }
                LOG.info("finished probing TCs");
                LOG.info("Adding Accepted TCs to configuration backend");
                ApplicationEntity destinationAE = conf
                        .findApplicationEntity(instance.destinationAET);
                Device toStore = destinationAE.getDevice();
                TransferCapability[] TCs = mergeTCs(acceptedPCs);
                for (TransferCapability tc : TCs)
                    toStore.getApplicationEntity(instance.destinationAET)
                            .addTransferCapability(tc);

                conf.merge(toStore);
                logAddedTCs(TCs, destinationAE);
                conf.close();
                System.exit(1);
            } catch (ConfigurationException e) {
                LOG.error("Configuration backend error - {}", e);
            }

        }

    }

    public static void configureConnect(Connection conn, AAssociateRQ rq,
            CommandLine cl, ApplicationEntity ae) throws ParseException,
            IOException {
        if (!cl.hasOption("p"))
            throw new MissingOptionException(rb.getString("missing-probed-ae"));
        String tmpOption = cl.getOptionValue("p");
        String aeTitle = tmpOption.split("@")[0];

        if (tmpOption.split("@")[1] == null)
            throw new ParseException(rb.getString("invalid-probed-ae"));
        String host = tmpOption.split("@")[1].split(":")[0];
        int port = Integer.parseInt(tmpOption.split("@")[1].split(":")[1]);
        probedAET = aeTitle;
        conn.setHostname(host);
        conn.setPort(port);
        conn.setInstalled(true);
        ae = new ApplicationEntity(aeTitle);
        ae.addConnection(conn);
    }

    public static Association openAssociation(ApplicationEntity ae,
            AAssociateRQ rq, Connection remote) throws IOException,
            InterruptedException, IncompatibleConnectionException,
            GeneralSecurityException {
        Association as = ae.connect(remote, rq);
        return as;
    }

    private static void logAddedTCs(TransferCapability[] tCs,
            ApplicationEntity destinationAE) {
        File log = new File("probe-tc-log-[ae=" + probedAET + "]");
        FileWriter logWriter = null;
        try {
            logWriter = new FileWriter(log);
        } catch (IOException e) {
            LOG.error("Unable to get output stream for log file - {}", e);
        }
        try {
            for (TransferCapability tc : tCs)
                logWriter.write(tc.toString());
        } catch (IOException e) {
            LOG.error("Error writing log for transfer capabilities set - {}", e);
        } finally {
            try {
                logWriter.close();
            } catch (IOException e) {
                LOG.error("Unable to close log File - {} - {}", log.getName(),
                        e);
            }
        }

    }

    private static TransferCapability[] mergeTCs(
            ArrayList<PresentationContext> acceptedPCs) {
        ArrayList<TransferCapability> tmpTCs = new ArrayList<TransferCapability>();
        for (PresentationContext pc : acceptedPCs) {
            String abstractSyntax = pc.getAbstractSyntax();
            if (containsAbstractSyntax(tmpTCs, abstractSyntax)) {
                continue;
            }
            TransferCapability tmpTC = new TransferCapability();
            tmpTC.setRole(Role.SCP);
            ArrayList<String> tmpTS = new ArrayList<String>();
            tmpTC.setSopClass(abstractSyntax);
            for (PresentationContext tmp : acceptedPCs) {

                if (tmp.getAbstractSyntax().compareToIgnoreCase(abstractSyntax) == 0) {
                    if (!tmpTS.contains(tmp.getTransferSyntax())) {
                        tmpTS.add(tmp.getTransferSyntax());
                    }
                }
            }
            String[] tmpTSStr = new String[tmpTS.size()];
            tmpTS.toArray(tmpTSStr);
            tmpTC.setTransferSyntaxes(tmpTSStr);
            tmpTCs.add(tmpTC);

        }
        TransferCapability[] TCs = new TransferCapability[tmpTCs.size()];
        tmpTCs.toArray(TCs);
        return TCs;
    }

    private static boolean containsAbstractSyntax(
            ArrayList<TransferCapability> tmpTCs, String abstractSyntax) {
        for (TransferCapability tc : tmpTCs) {
            if (tc.getSopClass().compareToIgnoreCase(abstractSyntax) == 0) {
                return true;
            }
        }
        return false;
    }

    private static ArrayList<PresentationContext> addChunkedPCsandSend(
            ApplicationEntity ae, Device device, ProbeTC instance,
            ArrayList<TransferCapability> tcs) {
        initThreads(device);
        int pcID = 1;
        ArrayList<ArrayList<PresentationContext>> lst = new ArrayList<ArrayList<PresentationContext>>();
        ArrayList<PresentationContext> fullListSingleTS = new ArrayList<PresentationContext>();
        ArrayList<PresentationContext> allACPCs = new ArrayList<PresentationContext>();

        for (TransferCapability tc : tcs)
            for (String ts : tc.getTransferSyntaxes()) {
                fullListSingleTS.add(new PresentationContext(pcID, tc
                        .getSopClass(), ts));
                pcID++;
                if (fullListSingleTS.size() > 127) {
                    lst.add(fullListSingleTS);
                    pcID = 1;
                    fullListSingleTS = new ArrayList<PresentationContext>();
                }
            }

        instance.rq.setCallingAET(ae.getAETitle());
        instance.rq.setCalledAET(probedAET);
        // now start sending 128 each
        for (ArrayList<PresentationContext> subList : lst) {
            instance.rq = new AAssociateRQ();
            instance.rq.setCallingAET(ae.getAETitle());
            instance.rq.setCalledAET(probedAET);
            for (PresentationContext pc : subList)
                instance.rq.addPresentationContext(pc);
            try {
                // send
                Association as = openAssociation(ae, instance.rq,
                        instance.destination);
                // cache the pcs
                for (PresentationContext pcAC : as.getAAssociateAC()
                        .getPresentationContexts()) {
                    if (pcAC.isAccepted())
                        allACPCs.add(instance.rq.getPresentationContext(pcAC
                                .getPCID()));
                }

                as.release();
            } catch (Exception e) {
                e.printStackTrace();
                // LOG.info("destination rejected the association for the following reason:\n"
                // + as.getException());
                System.exit(1);
            }
        }

        return allACPCs;
    }

    private static void initThreads(Device device) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ScheduledExecutorService scheduledExecutorService = Executors
                .newSingleThreadScheduledExecutor();
        device.setExecutor(executorService);
        device.setScheduledExecutor(scheduledExecutorService);
    }

    public String getDestinationAET() {
        return destinationAET;
    }

    public void setDestinationAET(String destinationAET) {
        this.destinationAET = destinationAET;
    }

    public String getSourceAET() {
        return sourceAET;
    }

    public void setSourceAET(String sourceAET) {
        this.sourceAET = sourceAET;
    }

    public ApplicationEntity getProbedAE() {
        return probedAE;
    }

    public void setProbedAE(ApplicationEntity probedAE) {
        this.probedAE = probedAE;
    }

    public File getLdapConfigurationFile() {
        return ldapConfigurationFile;
    }

    public void setLdapConfigurationFile(File ldapConfigurationFile) {
        this.ldapConfigurationFile = ldapConfigurationFile;
    }

    public String getConfigurationType() {
        return configurationType;
    }

    public void setConfigurationType(String configurationType) {
        this.configurationType = configurationType;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public Connection getDestination() {
        return destination;
    }

    public void setDestination(Connection destination) {
        this.destination = destination;
    }

    public AAssociateRQ getRq() {
        return rq;
    }

    public void setRq(AAssociateRQ rq) {
        this.rq = rq;
    }

    public String getCallingAET() {
        return callingAET;
    }

    public void setCallingAET(String callingAET) {
        this.callingAET = callingAET;
    }
}