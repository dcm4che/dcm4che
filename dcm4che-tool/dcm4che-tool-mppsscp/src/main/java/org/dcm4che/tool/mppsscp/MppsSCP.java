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

package org.dcm4che.tool.mppsscp;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che.data.UID;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.TransferCapability;
import org.dcm4che.net.service.BasicCEchoSCP;
import org.dcm4che.net.service.DicomService;
import org.dcm4che.net.service.DicomServiceRegistry;
import org.dcm4che.tool.common.CLIUtils;
import org.dcm4che.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class MppsSCP extends Device {

   private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che.tool.mppsscp.messages");

   private final ApplicationEntity ae = new ApplicationEntity("*");
   private final Connection conn = new Connection();
   private File storageDir;
   private final DicomService mppsSCP = new MppsSCPImpl(this);

   public MppsSCP() throws KeyManagementException, IOException {
       super("mppsscp");
       addConnection(conn);
       addApplicationEntity(ae);
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

   public static void main(String[] args) {
       try {
           CommandLine cl = parseComandLine(args);
           MppsSCP main = new MppsSCP();
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
        CLIUtils.addAEOptions(opts, false, true);
        CLIUtils.addCommonOptions(opts);
        addStorageDirectoryOptions(opts);
        addTransferCapabilityOptions(opts);
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

    private static void configureStorageDirectory(MppsSCP main, CommandLine cl) {
        if (!cl.hasOption("ignore")) {
            main.setStorageDirectory(
                    new File(cl.getOptionValue("directory", ".")));
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

}
