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

package org.dcm4che3.tool.syslog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Connection.Protocol;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Syslog {

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.syslog.messages");

    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AuditLogger auditLogger = new AuditLogger();
    private final AuditRecordRepository arr = new AuditRecordRepository();
    private final Device logDevice = new Device("syslog");
    private final Device arrDevice = new Device("syslogd");
    private int delayBetweenMessages;


    public Syslog() throws IOException {
        logDevice.addDeviceExtension(auditLogger);
        logDevice.addConnection(conn);
        arrDevice.addDeviceExtension(arr);
        arrDevice.addConnection(remote);
        auditLogger.addAuditRecordRepositoryDevice(arrDevice);
    }

    private void setProtocol(Connection.Protocol protocol) {
        conn.setProtocol(protocol);
        remote.setProtocol(protocol);
        auditLogger.addConnection(conn);
        arr.addConnection(remote);
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        addConnectOption(opts);
        addBindOption(opts);
        addAuditLogger(opts);
        addSendOptions(opts);
        CLIUtils.addSocketOptions(opts);
        CLIUtils.addTLSOptions(opts);
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, Syslog.class);
    }

    @SuppressWarnings("static-access")
    private static void addConnectOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("host:port")
                .withDescription(rb.getString("connect"))
                .withLongOpt("connect")
                .create("c"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("[user:password@]host:port")
                .withDescription(rb.getString("proxy"))
                .withLongOpt("proxy")
                .create(null));
        opts.addOption(null, "udp", false, rb.getString("udp"));
        CLIUtils.addConnectTimeoutOption(opts);
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("ms")
                .withDescription(rb.getString("idle-timeout"))
                .withLongOpt("idle-timeout")
                .create(null));
    }

    @SuppressWarnings("static-access")
    private static void addBindOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("ip")
                .withDescription(rb.getString("bind"))
                .withLongOpt("bind")
                .create("b"));
    }

    @SuppressWarnings("static-access")
    private static void addAuditLogger(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("facility")
                .withDescription(rb.getString("facility"))
                .withLongOpt("facility")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("level")
                .withDescription(rb.getString("level"))
                .withLongOpt("level")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("name")
                .withDescription(rb.getString("app-name"))
                .withLongOpt("app-name")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("id")
                .withDescription(rb.getString("msg-id"))
                .withLongOpt("msg-id")
                .create(null));
        opts.addOption(null, "utc", false, rb.getString("utc"));
        opts.addOption(null, "no-bom", false, rb.getString("no-bom"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("s")
                .withDescription(rb.getString("retry"))
                .withLongOpt("retry")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("dir")
                .withDescription(rb.getString("spool-dir"))
                .withLongOpt("spool-dir")
                .create(null));
    }

    @SuppressWarnings("static-access")
    private static void addSendOptions(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("ms")
                .withDescription(rb.getString("delay"))
                .withLongOpt("delay")
                .create(null));
    }

    private static void configureConnect(Connection conn, CommandLine cl)
            throws MissingOptionException, ParseException {
        if (!cl.hasOption("c"))
            throw new MissingOptionException(
                    CLIUtils.rb.getString("missing-connect-opt"));

        String[] hostPort = StringUtils.split(cl.getOptionValue("c"), ':');
        if (hostPort.length != 2)
            throw new ParseException(CLIUtils.rb.getString("invalid-connect-opt"));
        
        conn.setHostname(hostPort[0]);
        conn.setPort(Integer.parseInt(hostPort[1]));
 
        conn.setHttpProxy(cl.getOptionValue("proxy"));
    }

    private static void configureBind(Connection conn, CommandLine cl) {
        if (cl.hasOption("b"))
            conn.setHostname(cl.getOptionValue("b"));
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Syslog main = new Syslog();
            configureConnect(main.remote, cl);
            main.setProtocol(toProtocol(cl));
            configureAuditLogger(main.auditLogger, cl);
            main.setDelayBetweenMessages(
                    CLIUtils.getIntOption(cl, "delay", 0));
            configureBind(main.conn, cl);
            CLIUtils.configure(main.conn, cl);
            try {
                main.init();
                main.sendFiles(cl.getArgList());
                main.waitForNoQueuedMessages();
            } finally {
                main.close();
            }
        } catch (ParseException e) {
            System.err.println("syslog: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("syslog: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private void waitForNoQueuedMessages() throws InterruptedException {
        auditLogger.waitForNoQueuedMessages(0);
    }

    private void setDelayBetweenMessages(int delayBetweenMessages) {
        this.delayBetweenMessages = delayBetweenMessages;
    }

    private static void configureAuditLogger(AuditLogger logger, CommandLine cl) {
        logger.setFacility(toFacility(cl));
        logger.setSuccessSeverity(toSeverity(cl));
        logger.setApplicationName(cl.getOptionValue("app-name"));
        logger.setMessageID(cl.getOptionValue("msg-id", AuditLogger.MESSAGE_ID));
        logger.setIncludeBOM(!cl.hasOption("no-bom"));
        logger.setTimestampInUTC(cl.hasOption("utc"));
        if (cl.hasOption("spool-dir"))
            logger.setSpoolDirectory(new File(cl.getOptionValue("spool-dir")));
        logger.setRetryInterval(CLIUtils.getIntOption(cl, "retry", 0));
    }

    private static AuditLogger.Severity toSeverity(CommandLine cl) {
         return AuditLogger.Severity.valueOf(
                 cl.getOptionValue("level", "info"));
    }

    private static AuditLogger.Facility toFacility(CommandLine cl) {
        return AuditLogger.Facility.valueOf(
                cl.getOptionValue("facility", "authpriv"));
    }

    private static Protocol toProtocol(CommandLine cl) {
        return cl.hasOption("udp")
                ? Connection.Protocol.SYSLOG_UDP
                : Connection.Protocol.SYSLOG_TLS;
    }

    public void init() {
        remote.setTlsProtocols(conn.tlsProtocols());
        remote.setTlsCipherSuites(conn.getTlsCipherSuites());
        logDevice.setScheduledExecutor(Executors.newSingleThreadScheduledExecutor());
        auditLogger.sendQueuedMessages();
    }

    public void close() {
        auditLogger.closeActiveConnection();
        ScheduledExecutorService scheduler = logDevice.getScheduledExecutor();
        if (scheduler != null)
            scheduler.shutdown();
    }

    public void sendFiles(List<String> pathnames) throws Exception {
        int count = 0;
        for (String pathname : pathnames) {
            if (count++ > 0 && delayBetweenMessages > 0)
                Thread.sleep(delayBetweenMessages);
            byte[] b = readFile(pathname);
            auditLogger.write(auditLogger.timeStamp(),
                    auditLogger.getSuccessSeverity(),
                    b, 0, b.length);
        }
    }

    private byte[] readFile(String pathname) throws IOException {
        FileInputStream in = null;
        try {
            if (pathname.equals("-")) {
                in = new FileInputStream(FileDescriptor.in);
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                StreamUtils.copy(in, buf);
                return buf.toByteArray();
            } else {
                File f = new File(pathname);
                in = new FileInputStream(f);
                byte[] b = new byte[(int) f.length()];
                StreamUtils.readFully(in, b, 0, b.length);
                return b;
            }
        } finally {
            SafeClose.close(in);
        }
    }
}
