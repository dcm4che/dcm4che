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

package org.dcm4che3.tool.hl7snd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.hl7.MLLPConnection;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class HL7Snd extends Device {

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.hl7snd.messages");

    private final Connection conn = new Connection();
    private final Connection remote = new Connection();

    private Socket sock;
    private MLLPConnection mllp;

    public HL7Snd() throws IOException {
        super("hl7snd");
        addConnection(conn);
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        addConnectOption(opts);
        addBindOption(opts);
        CLIUtils.addResponseTimeoutOption(opts);
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
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("[user:password@]host:port")
                .withDescription(rb.getString("proxy"))
                .withLongOpt("proxy")
                .create(null));
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
            HL7Snd main = new HL7Snd();
            configureConnect(main.remote, cl);
            configureBind(main.conn, cl);
            CLIUtils.configure(main.conn, cl);
            main.remote.setTlsProtocols(main.conn.tlsProtocols());
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

    public void open() throws IOException, IncompatibleConnectionException, GeneralSecurityException {
        sock = conn.connect(remote);
        sock.setSoTimeout(conn.getResponseTimeout());
        mllp = new MLLPConnection(sock);
    }

    public void close() {
        conn.close(sock);
    }

    public void sendFiles(List<String> pathnames) throws IOException {
        for (String pathname : pathnames) {
            mllp.writeMessage(readFile(pathname));
            if (mllp.readMessage() == null)
                throw new IOException("Connection closed by receiver");
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
