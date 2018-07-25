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

package org.dcm4che3.tool.syslogd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Connection.Protocol;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditRecordHandler;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Syslogd {

    private static final ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.syslogd.messages");

    private final Device device = new Device("syslogd");
    private final AuditRecordRepository arr = new AuditRecordRepository();
    private final Connection conn = new Connection();
    private File storageDir;
    private final AuditRecordHandler handler = new AuditRecordHandler() {

        @Override
        public void onMessage(byte[] data, int xmlOffset, int xmlLength,
                Connection conn, InetAddress from) {
            Syslogd.this.onMessage(data, xmlOffset, xmlLength, conn, from);
        }
    };

    public Syslogd() {
        device.addDeviceExtension(arr);
        device.addConnection(conn);
        arr.setAuditRecordHandler(handler);
    }

    public void setStorageDirectory(String storageDir) {
        this.storageDir = storageDir != null ? new File(storageDir) : null;
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        addOptions(opts);
        CLIUtils.addSocketOptions(opts);
        CLIUtils.addTLSOptions(opts);
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, Syslogd.class);
    }

    @SuppressWarnings("static-access")
    public static void addOptions(Options opts) {
        opts.addOption(null, "ignore", false, rb.getString("ignore"));
        opts.addOption(Option.builder()
                .hasArg()
                .argName("path")
                .desc(rb.getString("directory"))
                .longOpt("directory")
                .build());
        opts.addOption(Option.builder("b")
                .hasArg()
                .argName("[ip:]port")
                .desc(rb.getString("bind-server"))
                .longOpt("bind")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("ms")
                .desc(rb.getString("idle-timeout"))
                .longOpt("idle-timeout")
                .build());
        opts.addOption(null, "udp", false, rb.getString("udp"));
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Syslogd main = new Syslogd();
            configure(main, cl);
            ExecutorService executorService = Executors.newCachedThreadPool();
            ScheduledExecutorService scheduledExecutorService = 
                    Executors.newSingleThreadScheduledExecutor();
            main.device.setScheduledExecutor(scheduledExecutorService);
            main.device.setExecutor(executorService);
            main.device.bindConnections();
        } catch (ParseException e) {
            System.err.println("syslogd: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("syslogd: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void configure(Syslogd main, CommandLine cl)
            throws Exception, MalformedURLException, ParseException,
            IOException {
        if (!cl.hasOption("ignore"))
            main.setStorageDirectory(
                    cl.getOptionValue("directory", "."));
        configureBindServer(main.conn, cl);
        main.setProtocol(toProtocol(cl));
        main.arr.addConnection(main.conn);
        CLIUtils.configure(main.conn, cl);
    }

    private void setProtocol(Protocol protocol) {
        conn.setProtocol(protocol);
        arr.addConnection(conn);
    }

    private static void configureBindServer(Connection conn, CommandLine cl)
            throws ParseException {
        if (!cl.hasOption("b"))
            throw new MissingOptionException(
                    CLIUtils.rb.getString("missing-bind-opt"));
        String aeAtHostPort = cl.getOptionValue("b");
        String[] hostAndPort = StringUtils.split(aeAtHostPort, ':');
        int portIndex = hostAndPort.length - 1;
        conn.setPort(Integer.parseInt(hostAndPort[portIndex]));
        if (portIndex > 0)
            conn.setHostname(hostAndPort[0]);
    }

    private static Protocol toProtocol(CommandLine cl) {
        return cl.hasOption("udp")
                ? Connection.Protocol.SYSLOG_UDP
                : Connection.Protocol.SYSLOG_TLS;
    }

    private void onMessage(byte[] data, int off, int len, Connection conn,
            InetAddress from) {
            if (storageDir != null) 
                try {
                    storeToFile(data, off, len,
                            File.createTempFile("syslog", ".xml", storageDir));
                } catch (IOException e) {
                    Connection.LOG.warn("Failed to store received message", e);
                }
    }

    private void storeToFile(byte[] data, int off, int len, File f)
            throws IOException {
        Connection.LOG.info("M-WRITE {}", f);
        f.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(f);
        try {
            out.write(data, off, len);
        } finally {
            out.close();
        }
    }

}
