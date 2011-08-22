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

package org.dcm4che.tool.storescp;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Association;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.PDVInputStream;
import org.dcm4che.net.Status;
import org.dcm4che.net.TransferCapability;
import org.dcm4che.net.service.BasicCEchoSCP;
import org.dcm4che.net.service.BasicCStoreSCP;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4che.net.service.DicomServiceRegistry;
import org.dcm4che.tool.common.CLIUtils;
import org.dcm4che.util.FilePathFormat;
import org.dcm4che.util.SafeClose;
import org.dcm4che.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Main {

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che.tool.storescp.messages");

    private static final String PART_EXT = ".part";

    private final Device device = new Device("storescp");
    private final ApplicationEntity ae = new ApplicationEntity("*");
    private final Connection conn = new Connection();
    private File storageDir;
    private FilePathFormat filePathFormat;

    private final BasicCStoreSCP storageSCP = new BasicCStoreSCP("*") {

        @Override
        protected void store(Association as, Attributes rq,
                PDVInputStream data, String tsuid, Attributes rsp)
                throws IOException {
            if (storageDir != null)
                super.store(as, rq, data, tsuid, rsp);
        }

        @Override
        protected File createFile(Association as, Attributes rq, Object storage)
                throws DicomServiceException {
            return new File(storageDir, rq.getString(Tag.AffectedSOPInstanceUID) + PART_EXT);
        }

        @Override
        protected File process(Association as, Attributes rq, String tsuid,
                Attributes rsp, Object storage, File file, MessageDigest digest)
                throws DicomServiceException {
            File dst;
            if (filePathFormat == null) {
                String fname = file.getName();
                dst = new File(storageDir, fname.substring(0, fname.lastIndexOf('.')));
            } else {
                Attributes ds;
                DicomInputStream in = null;
                try {
                    in = new DicomInputStream(file);
                    in.setIncludeBulkData(false);
                    ds = in.readDataset(-1, Tag.PixelData);
                } catch (IOException e) {
                    LOG.warn(as + ": Failed to decode dataset:", e);
                    throw new DicomServiceException(rq, Status.CannotUnderstand);
                } finally {
                    SafeClose.close(in);
                }
                dst = new File(storageDir, filePathFormat.format(ds));
            }
            File dir = dst.getParentFile();
            dir.mkdirs();
            dst.delete();
            if (file.renameTo(dst))
                LOG.info("{}: M-RENAME {} to {}", new Object[] {as, file, dst});
            else {
                LOG.warn("{}: Failed to M-RENAME {} to {}", new Object[] {as, file, dst});
                throw new DicomServiceException(rq, Status.OutOfResources, "Failed to rename file");
            }
            return null;
        }

    };

    public Main() throws IOException {
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.setAssociationAcceptor(true);
        ae.addConnection(conn);
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        serviceRegistry.addDicomService(storageSCP);
        ae.setDimseRQHandler(serviceRegistry);
    }

    public void setScheduledExecutor(ScheduledExecutorService scheduledExecutor) {
        device.setScheduledExecutor(scheduledExecutor);
    }

    public void setExecutor(Executor executor) {
        device.setExecutor(executor);
    }

    public void setStorageDirectory(File storageDir) {
        if (storageDir != null)
            storageDir.mkdirs();
        this.storageDir = storageDir;
    }

    public void setStorageFilePathFormat(String pattern) {
        this.filePathFormat = new FilePathFormat(pattern);
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        CLIUtils.addBindServerOption(opts);
        CLIUtils.addAEOptions(opts, false, true);
        CLIUtils.addCommonOptions(opts);
        addStorageDirectoryOptions(opts);
        addTransferCapabilityOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, Main.class);
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
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("pattern")
                .withDescription(rb.getString("filepath"))
                .withLongOpt("filepath")
                .create(null));
    }

    @SuppressWarnings("static-access")
    private static void addTransferCapabilityOptions(Options opts) {
        opts.addOption(null, "accept-unknown", false,
                rb.getString("accept-unknown"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("storage-sop-classes"))
                .withLongOpt("storage-sop-classes")
                .create(null));
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Main main = new Main();
            CLIUtils.configureBindServer(main.conn, main.ae, cl);
            CLIUtils.configure(main.conn, main.ae, cl);
            configureTransferCapability(main.ae, cl);
            configureStorageDirectory(main, cl);
            ExecutorService executorService = Executors.newCachedThreadPool();
            ScheduledExecutorService scheduledExecutorService = 
                    Executors.newSingleThreadScheduledExecutor();
            main.setScheduledExecutor(scheduledExecutorService);
            main.setExecutor(executorService);
            main.activate();
        } catch (ParseException e) {
            System.err.println("storescp: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("storescp: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void configureStorageDirectory(Main main, CommandLine cl) {
        if (!cl.hasOption("ignore")) {
            main.setStorageDirectory(
                    new File(cl.getOptionValue("directory", ".")));
            if (cl.hasOption("filepath"))
                main.setStorageFilePathFormat(cl.getOptionValue("filepath"));
        }
    }

    private static void configureTransferCapability(ApplicationEntity ae,
            CommandLine cl) throws IOException {
        if (cl.hasOption("accept-unknown")) {
            ae.addTransferCapability(
                    new TransferCapability(null, 
                            "*",
                            TransferCapability.Role.SCP,
                            "*"));
        } else {
            ae.addTransferCapability(
                    new TransferCapability(null, 
                            UID.VerificationSOPClass,
                            TransferCapability.Role.SCP,
                            UID.ImplicitVRLittleEndian));
            Properties p = CLIUtils.loadProperties(
                    cl.getOptionValue("storage-sop-classes", 
                            "resource:storage-sop-classes.properties"),
                    null);
            for (String cuid : p.stringPropertyNames()) {
                String ts = p.getProperty(cuid);
                ae.addTransferCapability(
                        ts.equals("*")
                            ? new TransferCapability(null, cuid,
                                    TransferCapability.Role.SCP, "*")
                            : new TransferCapability(null, cuid,
                                    TransferCapability.Role.SCP,
                                    StringUtils.split(ts, ',')));
            }
        }
     }

    private void activate() throws IOException {
        device.activate();
    }

}
