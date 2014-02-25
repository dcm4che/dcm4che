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

package org.dcm4che3.tool.mppsscp;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
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
import org.dcm4che3.data.IOD;
import org.dcm4che3.data.ValidationResult;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.BasicMPPSSCP;
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
public class MppsSCP {

   private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.mppsscp.messages");

   private static final Logger LOG = LoggerFactory.getLogger(MppsSCP.class);

   private final Device device = new Device("mppsscp");
   private final ApplicationEntity ae = new ApplicationEntity("*");
   private final Connection conn = new Connection();
   private File storageDir;
   private IOD mppsNCreateIOD;
   private IOD mppsNSetIOD;

   private final BasicMPPSSCP mppsSCP = new BasicMPPSSCP() {
       
       @Override
       protected Attributes create(Association as, Attributes rq,
               Attributes rqAttrs, Attributes rsp) throws DicomServiceException {
           return MppsSCP.this.create(as, rq, rqAttrs);
       }
       
       @Override
       protected Attributes set(Association as, Attributes rq, Attributes rqAttrs,
               Attributes rsp) throws DicomServiceException {
           return MppsSCP.this.set(as, rq, rqAttrs);
       }
   };

   public MppsSCP() throws IOException {
       device.addConnection(conn);
       device.addApplicationEntity(ae);
       ae.setAssociationAcceptor(true);
       ae.addConnection(conn);
       DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
       serviceRegistry.addDicomService(new BasicCEchoSCP());
       serviceRegistry.addDicomService(mppsSCP);
       ae.setDimseRQHandler(serviceRegistry);
   }

   public void setStorageDirectory(File storageDir) {
       if (storageDir != null)
           storageDir.mkdirs();
       this.storageDir = storageDir;
   }

   public File getStorageDirectory() {
       return storageDir;
   }

   private void setMppsNCreateIOD(IOD mppsNCreateIOD) {
       this.mppsNCreateIOD = mppsNCreateIOD;
   }

   private void setMppsNSetIOD(IOD mppsNSetIOD) {
       this.mppsNSetIOD = mppsNSetIOD;
   }

   public static void main(String[] args) {
       try {
           CommandLine cl = parseComandLine(args);
           MppsSCP main = new MppsSCP();
           CLIUtils.configureBindServer(main.conn, main.ae, cl);
           CLIUtils.configure(main.conn, cl);
           configureTransferCapability(main.ae, cl);
           configureStorageDirectory(main, cl);
           configureIODs(main, cl);
           ExecutorService executorService = Executors.newCachedThreadPool();
           ScheduledExecutorService scheduledExecutorService = 
                   Executors.newSingleThreadScheduledExecutor();
           main.device.setScheduledExecutor(scheduledExecutorService);
           main.device.setExecutor(executorService);
           main.device.bindConnections();
       } catch (ParseException e) {
           System.err.println("mppsscp: " + e.getMessage());
           System.err.println(rb.getString("try"));
           System.exit(2);
       } catch (Exception e) {
           System.err.println("mppsscp: " + e.getMessage());
           e.printStackTrace();
           System.exit(2);
       }
   }

    private static CommandLine parseComandLine(String[] args) throws ParseException {
        Options opts = new Options();
        CLIUtils.addBindServerOption(opts);
        CLIUtils.addAEOptions(opts);
        CLIUtils.addCommonOptions(opts);
        addStorageDirectoryOptions(opts);
        addTransferCapabilityOptions(opts);
        addIODOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, MppsSCP.class);
    }

    @SuppressWarnings("static-access")
    private static void addStorageDirectoryOptions(Options opts) {
        opts.addOption(null, "ignore", false,
                rb.getString("ignore"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("path")
                .withDescription(rb.getString("directory"))
                .withLongOpt("directory")
                .create(null));
    }

    @SuppressWarnings("static-access")
    private static void addTransferCapabilityOptions(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("sop-classes"))
                .withLongOpt("sop-classes")
                .create(null));
    }

    @SuppressWarnings("static-access")
    private static void addIODOptions(Options opts) {
        opts.addOption(null, "no-validate", false,
                rb.getString("no-validate"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("ncreate-iod"))
                .withLongOpt("ncreate-iod")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("nset-iod"))
                .withLongOpt("nset-iod")
                .create(null));
    }

    private static void configureStorageDirectory(MppsSCP main, CommandLine cl) {
        if (!cl.hasOption("ignore")) {
            main.setStorageDirectory(
                    new File(cl.getOptionValue("directory", ".")));
        }
    }

    private static void configureIODs(MppsSCP main, CommandLine cl)
            throws IOException {
        if (!cl.hasOption("no-validate")) {
            main.setMppsNCreateIOD(IOD.load(
                    cl.getOptionValue("mpps-ncreate-iod", 
                            "resource:mpps-ncreate-iod.xml")));
            main.setMppsNSetIOD(IOD.load(
                    cl.getOptionValue("mpps-nset-iod", 
                            "resource:mpps-nset-iod.xml")));
        }
    }

    private static void configureTransferCapability(ApplicationEntity ae,
            CommandLine cl) throws IOException {
        Properties p = CLIUtils.loadProperties(
                cl.getOptionValue("sop-classes", 
                        "resource:sop-classes.properties"),
                null);
        for (String cuid : p.stringPropertyNames()) {
            String ts = p.getProperty(cuid);
            ae.addTransferCapability(
                    new TransferCapability(null, cuid,
                                TransferCapability.Role.SCP,
                                toUIDs(StringUtils.split(ts, ','))));
        }
    }

    private static String[] toUIDs(String[] names) {
        String[] uids = new String[names.length];
        for (int i = 0; i < uids.length; i++)
            uids[i] = UID.forName(names[i].trim());
        return uids ;
    }

    private Attributes create(Association as, Attributes rq, Attributes rqAttrs)
            throws DicomServiceException {
        if (mppsNCreateIOD != null) {
            ValidationResult result = rqAttrs.validate(mppsNCreateIOD);
            if (!result.isValid())
                throw DicomServiceException.valueOf(result, rqAttrs);
        }
        if (storageDir == null)
            return null;
        String cuid = rq.getString(Tag.AffectedSOPClassUID);
        String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
        File file = new File(storageDir, iuid);
        if (file.exists())
            throw new DicomServiceException(Status.DuplicateSOPinstance).
                setUID(Tag.AffectedSOPInstanceUID, iuid);
        DicomOutputStream out = null;
        LOG.info("{}: M-WRITE {}", as, file);
        try {
            out = new DicomOutputStream(file);
            out.writeDataset(
                    Attributes.createFileMetaInformation(iuid, cuid,
                            UID.ExplicitVRLittleEndian),
                    rqAttrs);
        } catch (IOException e) {
            LOG.warn(as + ": Failed to store MPPS:", e);
            throw new DicomServiceException(Status.ProcessingFailure, e);
        } finally {
            SafeClose.close(out);
        }
        return null;
    }

    private Attributes set(Association as, Attributes rq, Attributes rqAttrs)
            throws DicomServiceException {
        if (mppsNSetIOD != null) {
            ValidationResult result = rqAttrs.validate(mppsNSetIOD);
            if (!result.isValid())
                throw DicomServiceException.valueOf(result, rqAttrs);
        }
        if (storageDir == null)
            return null;
        String cuid = rq.getString(Tag.RequestedSOPClassUID);
        String iuid = rq.getString(Tag.RequestedSOPInstanceUID);
        File file = new File(storageDir, iuid);
        if (!file.exists())
            throw new DicomServiceException(Status.NoSuchObjectInstance).
                setUID(Tag.AffectedSOPInstanceUID, iuid);
        LOG.info("{}: M-UPDATE {}", as, file);
        Attributes data;
        DicomInputStream in = null;
        try {
            in = new DicomInputStream(file);
            data = in.readDataset(-1, -1);
        } catch (IOException e) {
            LOG.warn(as + ": Failed to read MPPS:", e);
            throw new DicomServiceException(Status.ProcessingFailure, e);
        } finally {
            SafeClose.close(in);
        }
        if (!"IN PROGRESS".equals(data.getString(Tag.PerformedProcedureStepStatus)))
            BasicMPPSSCP.mayNoLongerBeUpdated();

        data.addAll(rqAttrs);
        DicomOutputStream out = null;
        try {
            out = new DicomOutputStream(file);
            out.writeDataset(
                    Attributes.createFileMetaInformation(iuid, cuid, UID.ExplicitVRLittleEndian),
                    data);
        } catch (IOException e) {
            LOG.warn(as + ": Failed to update MPPS:", e);
            throw new DicomServiceException(Status.ProcessingFailure, e);
        } finally {
            SafeClose.close(out);
        }
        return null;
    }
}
