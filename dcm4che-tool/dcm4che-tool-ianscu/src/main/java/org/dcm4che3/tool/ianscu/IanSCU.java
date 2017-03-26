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

package org.dcm4che3.tool.ianscu;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.common.DicomFiles;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class IanSCU {

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.ianscu.messages");

    private final Device device = new Device("ianscu");
    private final ApplicationEntity ae = new ApplicationEntity("IANSCU");
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private final Attributes attrs = new Attributes();
    private String uidSuffix;
    private String refPpsIUID;
    private String refPpsCUID = UID.ModalityPerformedProcedureStepSOPClass;
    private String availability = "ONLINE";
    private String retrieveAET;
    private String retrieveURI;
    private String retrieveURL;
    private String retrieveUID;

    private HashMap<String,Attributes> map = new HashMap<String,Attributes>();
    private Association as;

    public IanSCU() {
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
    }

    public final void setUIDSuffix(String uidSuffix) {
        this.uidSuffix = uidSuffix;
    }

    public void setTransferSyntaxes(String[] tss) {
        rq.addPresentationContext(
                new PresentationContext(1, UID.VerificationSOPClass,
                        UID.ImplicitVRLittleEndian));
        rq.addPresentationContext(
                new PresentationContext(3,
                        UID.InstanceAvailabilityNotificationSOPClass,
                        tss));
    }

    public void setRefPpsIUID(String refPpsIUID) {
        this.refPpsIUID = refPpsIUID;
    }

    public void setRefPpsCUID(String refPpsCUID) {
        this.refPpsCUID = refPpsCUID;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public void setRetrieveAET(String retrieveAET) {
        this.retrieveAET = retrieveAET;
    }

    public String getRetrieveAET() {
        return retrieveAET != null ? retrieveAET : ae.getAETitle();
    }

    public void setRetrieveURL(String retrieveURL) {
        this.retrieveURL = retrieveURL;
    }

    public void setRetrieveURI(String retrieveURI) {
        this.retrieveURI = retrieveURI;
    }

    public void setRetrieveUID(String retrieveUID) {
        this.retrieveUID = retrieveUID;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            final IanSCU main = new IanSCU();
            configureIAN(main, cl);
            CLIUtils.configureConnect(main.remote, main.rq, cl);
            CLIUtils.configureBind(main.conn, main.ae, cl);
            CLIUtils.configure(main.conn, cl);
            main.remote.setTlsProtocols(main.conn.tlsProtocols());
            main.remote.setTlsCipherSuites(main.conn.getTlsCipherSuites());
            main.setTransferSyntaxes(CLIUtils.transferSyntaxesOf(cl));
            CLIUtils.addAttributes(main.attrs, cl.getOptionValues("s"));
            main.setUIDSuffix(cl.getOptionValue("uid-suffix"));
            List<String> argList = cl.getArgList();
            boolean echo = argList.isEmpty();
            if (!echo) {
                System.out.println(rb.getString("scanning"));
                DicomFiles.scan(argList, new DicomFiles.Callback() {
                    
                    @Override
                    public boolean dicomFile(File f, Attributes fmi, long dsPos,
                            Attributes ds) {
                        if (UID.InstanceAvailabilityNotificationSOPClass.equals(
                                fmi.getString(Tag.MediaStorageSOPClassUID))) {
                            return main.addIAN(
                                    fmi.getString(Tag.MediaStorageSOPInstanceUID),
                                    ds);
                        }
                        return main.addInstance(ds);
                    }
                });
            }
            ExecutorService executorService =
                    Executors.newSingleThreadExecutor();
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newSingleThreadScheduledExecutor();
            main.device.setExecutor(executorService);
            main.device.setScheduledExecutor(scheduledExecutorService);
            try {
                main.open();
                if (echo)
                    main.echo();
                else
                    main.sendIans();
            } finally {
                main.close();
                executorService.shutdown();
                scheduledExecutorService.shutdown();
            }
        } catch (ParseException e) {
            System.err.println("ianscu: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("ianscu: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        CLIUtils.addTransferSyntaxOptions(opts);
        CLIUtils.addConnectOption(opts);
        CLIUtils.addBindOption(opts, "IANSCU");
        CLIUtils.addAEOptions(opts);
        CLIUtils.addResponseTimeoutOption(opts);
        CLIUtils.addCommonOptions(opts);
        addIANOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, IanSCU.class);
    }

    @SuppressWarnings("static-access")
    private static void addIANOptions(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("uid")
                .withDescription(rb.getString("pps-iuid"))
                .withLongOpt("pps-iuid")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("uid")
                .withDescription(rb.getString("pps-cuid"))
                .withLongOpt("pps-cuid")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("code-string")
                .withDescription(rb.getString("availability"))
                .withLongOpt("availability")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("aet")
                .withDescription(rb.getString("retrieve-aet"))
                .withLongOpt("retrieve-aet")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("uri")
                .withDescription(rb.getString("retrieve-url"))
                .withLongOpt("retrieve-url")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("uri")
                .withDescription(rb.getString("retrieve-uri"))
                .withLongOpt("retrieve-uri")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("uid")
                .withDescription(rb.getString("retrieve-uid"))
                .withLongOpt("retrieve-uid")
                .create());
        opts.addOption(OptionBuilder
                .hasArgs()
                .withArgName("[seq/]attr=value")
                .withValueSeparator('=')
                .withDescription(rb.getString("set"))
                .create("s"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("suffix")
                .withDescription(rb.getString("uid-suffix"))
                .withLongOpt("uid-suffix")
                .create(null));
    }

    private static void configureIAN(IanSCU main, CommandLine cl)
            throws Exception {
        main.setRefPpsIUID(cl.getOptionValue("pps-iuid"));
        main.setRefPpsCUID(cl.getOptionValue("pps-cuid", 
                UID.ModalityPerformedProcedureStepSOPClass));
        main.setAvailability(cl.getOptionValue("availability", "ONLINE"));
        main.setRetrieveAET(cl.getOptionValue("retrieve-aet"));
        main.setRetrieveURI(cl.getOptionValue("retrieve-uri"));
        main.setRetrieveURL(cl.getOptionValue("retrieve-url"));
        main.setRetrieveUID(cl.getOptionValue("retrieve-uid"));
    }

    public void open() throws IOException, InterruptedException,
            IncompatibleConnectionException, GeneralSecurityException {
        as = ae.connect(conn, remote, rq);
    }

    public void close() throws IOException, InterruptedException {
        if (as != null) {
            as.release();
        }
    }

    public void echo() throws IOException, InterruptedException {
        as.cecho().next();
    }

    public void sendIans() throws IOException, InterruptedException {
        for (Attributes ian : map.values())
            sendIan(ian);
    }

    private void sendIan(Attributes ian) throws IOException, InterruptedException {
        as.ncreate(UID.InstanceAvailabilityNotificationSOPClass, null, ian, null,
                new DimseRSPHandler(as.nextMessageID()));
    }

    public boolean addInstance(Attributes inst) {
        CLIUtils.updateAttributes(inst, attrs, uidSuffix);
        String suid = inst.getString(Tag.StudyInstanceUID);
        if (suid == null)
            return false;

        Attributes ian = map.get(suid);
        if (ian == null)
            map.put(suid, ian = createIAN(inst));
        updateIAN(ian, inst);
        return true;
    }

    public boolean addIAN(String iuid, Attributes ian) {
        map.put(iuid, ian);
        return true;
    }

    private Attributes createIAN(Attributes inst) {
        Attributes ian = new Attributes(3);
        Sequence refPpsSeq =
                ian.newSequence(Tag.ReferencedPerformedProcedureStepSequence, 1);
        if (refPpsIUID != null) {
            Attributes refPps = new Attributes(3);
            refPps.setString(Tag.ReferencedSOPClassUID, VR.UI, refPpsCUID);
            refPps.setString(Tag.ReferencedSOPInstanceUID, VR.UI, refPpsIUID);
            refPps.setNull(Tag.PerformedWorkitemCodeSequence,VR.SQ); 
            refPpsSeq.add(refPps);
        }
        ian.newSequence(Tag.ReferencedSeriesSequence, 1);
        ian.setString(Tag.StudyInstanceUID, VR.UI,
                inst.getString(Tag.StudyInstanceUID));
        return ian ;
    }

    private void updateIAN(Attributes mpps, Attributes inst) {
        Sequence refSeriesSeq = mpps.getSequence(Tag.ReferencedSeriesSequence);
        Attributes refSeries = getRefSeries(refSeriesSeq, inst);
        Sequence refSOPSeq = refSeries.getSequence(Tag.ReferencedSOPSequence);
        Attributes refSOP = new Attributes(6);
        refSOP.setString(Tag.RetrieveAETitle, VR.AE, getRetrieveAET());
        refSOP.setString(Tag.InstanceAvailability, VR.CS, availability);
        refSOP.setString(Tag.ReferencedSOPClassUID, VR.UI,
                inst.getString(Tag.SOPClassUID));
        refSOP.setString(Tag.ReferencedSOPInstanceUID, VR.UI,
                inst.getString(Tag.SOPInstanceUID));
        if (retrieveURL != null)
            refSOP.setString(Tag.RetrieveURL, VR.UR, retrieveURL);
        if (retrieveURI != null)
            refSOP.setString(Tag.RetrieveURI, VR.UR, retrieveURI);
        if (retrieveUID != null)
            refSOP.setString(Tag.RetrieveLocationUID, VR.UI, retrieveUID);
        refSOPSeq.add(refSOP);
    }

    private Attributes getRefSeries(Sequence refSeriesSeq, Attributes inst) {
        String suid = inst.getString(Tag.SeriesInstanceUID);
        for (Attributes refSeries : refSeriesSeq) {
            if (suid.equals(refSeries.getString(Tag.SeriesInstanceUID)))
                return refSeries;
        }
        Attributes refSeries = new Attributes(2);
        refSeries.newSequence(Tag.ReferencedSOPSequence, 10);
        refSeries.setString(Tag.SeriesInstanceUID, VR.CS, suid);
        refSeriesSeq.add(refSeries);
        return refSeries ;
    }
}
