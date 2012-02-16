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

package org.dcm4che.tool.hl7snd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.IncompatibleConnectionException;
import org.dcm4che.tool.common.CLIUtils;
import org.dcm4che.util.SafeClose;
import org.dcm4che.util.StringUtils;
import org.regenstrief.xhl7.MLLPDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class HL7Snd extends Device {

    private static final Logger LOG = LoggerFactory.getLogger(HL7Snd.class);
    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che.tool.hl7snd.messages");

    private final Connection conn = new Connection();
    private final Connection remote = new Connection();

    private int hl7RSPTimeout;

    private Socket sock;
    private MLLPDriver mllpDriver;

    public HL7Snd() throws IOException {
        super("hl7snd");
        addConnection(conn);
    }

    public final int getHl7RSPTimeout() {
        return hl7RSPTimeout;
    }

    public final void setHl7RSPTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.hl7RSPTimeout = timeout;
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        addConnectOption(opts);
        addBindOption(opts);
        addHl7AckTimeoutOption(opts);
        CLIUtils.addSocketOptions(opts);
        CLIUtils.addTLSOptions(opts);
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, HL7Snd.class);
    }

    @SuppressWarnings("static-access")
    private static void addConnectOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("host:port")
                .withDescription(rb.getString("connect"))
                .withLongOpt("connect")
                .create("c"));
        CLIUtils.addConnectTimeoutOption(opts);
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
    public static void addHl7AckTimeoutOption(Options opts) {
        opts.addOption(OptionBuilder
            .hasArg()
            .withArgName("ms")
            .withDescription(rb.getString("hl7-rsp-timeout"))
            .withLongOpt("hl7-rsp-timeout")
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
    }

    private static void configureBind(Connection conn, CommandLine cl) {
        if (cl.hasOption("b"))
            conn.setHostname(cl.getOptionValue("b"));
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            HL7Snd main = new HL7Snd();
            configureConnect(main.remote, cl);
            configureBind(main.conn, cl);
            CLIUtils.configure(main.conn, cl);
            if (cl.hasOption("hl7-rsp-timeout"))
                main.setHl7RSPTimeout(
                        Integer.parseInt(cl.getOptionValue("hl7-rsp-timeout")));
            main.remote.setTlsProtocols(main.conn.getTlsProtocols());
            main.remote.setTlsCipherSuites(main.conn.getTlsCipherSuites());
            try {
                main.open();
                main.sendFiles(cl.getArgList());
            } finally {
                main.close();
            }
        } catch (ParseException e) {
            System.err.println("hl7snd: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("hl7snd: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    public void open() throws IOException, IncompatibleConnectionException {
        sock = conn.connect(remote);
        sock.setSoTimeout(hl7RSPTimeout);
        mllpDriver = new MLLPDriver(sock.getInputStream(), sock.getOutputStream(), true);
    }

    public void close() {
        conn.close(sock);
    }

    public void sendFiles(List<String> pathnames) throws IOException {
        MyByteArrayOutputStream inbuf = new MyByteArrayOutputStream();
        for (String pathname : pathnames) {
            File f = new File(pathname);
            byte[] outbuf = new byte[(int) f.length()];
            FileInputStream in = new FileInputStream(f);
            try {
                in.read(outbuf);
            } finally {
                SafeClose.close(in);
            }
            LOG.info("Send HL7 Message: {}", promptHL7(outbuf, outbuf.length));
            mllpDriver.getOutputStream().write(outbuf);
            mllpDriver.turn();
            inbuf.write(mllpDriver.getInputStream());
            LOG.info("Received HL7 Message: {}", 
                    promptHL7(inbuf.buf(), inbuf.size()));
            inbuf.reset();
        }
    }

    private static class MyByteArrayOutputStream extends ByteArrayOutputStream {

        byte[] buf() {
            return buf;
        }

        void write(InputStream in) throws IOException {
            int b;
            while ((b = in.read()) != -1)
                write(b);
        }
    }

    private String promptHL7(byte[] buf, int size) {
        StringBuilder sb = new StringBuilder(size);
        int off = 0, len = 0;
        for (int i = 0; i < size; i++)
            if (buf[i] != 0x0D)
                len++;
            else {
                StringUtils.appendLine(sb, new String(buf, off, len));
                off += len + 1;
                len = 0;
            }
        if (len > 0) {
            StringUtils.appendLine(sb, new String(buf, off, len));
        }
        return sb.toString();
    }
}
