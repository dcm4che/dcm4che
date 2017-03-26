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

package org.dcm4che3.tool.hl7pix;

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
import org.dcm4che3.hl7.HL7Message;
import org.dcm4che3.hl7.HL7Segment;
import org.dcm4che3.hl7.MLLPConnection;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.tool.common.CLIUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class HL7Pix extends Device {

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.hl7pix.messages");

    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private String sendingApplication = "hl7pix^dcm4che";
    private String receivingApplication = "";
    private String charset;

    private Socket sock;
    private MLLPConnection mllp;

    public HL7Pix() throws IOException {
        super("hl7pix");
        addConnection(conn);
    }

    public String getSendingApplication() {
        return sendingApplication;
    }

    public void setSendingApplication(String sendingApplication) {
        this.sendingApplication = sendingApplication;
    }

    public String getReceivingApplication() {
        return receivingApplication;
    }

    public void setReceivingApplication(String receivingApplication) {
        this.receivingApplication = receivingApplication;
    }

    public void setCharacterSet(String charset) {
        this.charset = charset;
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        addConnectOption(opts);
        addBindOption(opts);
        addCharsetOption(opts);
        CLIUtils.addResponseTimeoutOption(opts);
        CLIUtils.addSocketOptions(opts);
        CLIUtils.addTLSOptions(opts);
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, HL7Pix.class);
    }

    @SuppressWarnings("static-access")
    private static void addCharsetOption(Options opts) {
        opts.addOption(OptionBuilder
                .withLongOpt("charset")
                .hasArg()
                .withArgName("name")
                .withDescription(rb.getString("charset"))
                .create(null));
    }

    @SuppressWarnings("static-access")
    private static void addConnectOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("app^fac@host:port")
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
                .withArgName("app^fac[@ip]")
                .withDescription(rb.getString("bind"))
                .withLongOpt("bind")
                .create("b"));
    }

    private static void configureConnect(HL7Pix hl7pix, CommandLine cl)
            throws MissingOptionException, ParseException {
        String appAtHostPort = cl.getOptionValue("c");
        if (appAtHostPort == null)
            throw new MissingOptionException(
                    CLIUtils.rb.getString("missing-connect-opt"));

        String[] appHostPort = HL7Segment.split(appAtHostPort , '@');
        if (appHostPort.length != 2)
            throw new ParseException(CLIUtils.rb.getString("invalid-connect-opt"));

        String[] hostPort = HL7Segment.split(appHostPort[1], ':');
        if (hostPort.length != 2)
            throw new ParseException(CLIUtils.rb.getString("invalid-connect-opt"));

        hl7pix.setReceivingApplication(appHostPort[0]);
        hl7pix.remote.setHostname(hostPort[0]);
        hl7pix.remote.setPort(Integer.parseInt(hostPort[1]));
        hl7pix.remote.setHttpProxy(cl.getOptionValue("proxy"));
    }

    private static void configureBind(HL7Pix hl7pix, CommandLine cl) {
        String appAtHost = cl.getOptionValue("b");
        if (appAtHost != null) {
            String[] appHost = HL7Segment.split(appAtHost, '@');
            hl7pix.setSendingApplication(appHost[0]);
            if (appHost.length > 1)
                hl7pix.conn.setHostname(appHost[1]);
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            HL7Pix main = new HL7Pix();
            configureConnect(main, cl);
            configureBind(main, cl);
            CLIUtils.configure(main.conn, cl);
            main.setCharacterSet(cl.getOptionValue("charset"));
            main.remote.setTlsProtocols(main.conn.tlsProtocols());
            main.remote.setTlsCipherSuites(main.conn.getTlsCipherSuites());
            List<String> argList = cl.getArgList();
            if (argList.isEmpty())
                throw new ParseException(rb.getString("missing"));
            try {
                main.open();
                main.query(argList.get(0), 
                        argList.subList(1, argList.size()).toArray(new String[0]));
            } finally {
                main.close();
            }
        } catch (ParseException e) {
            System.err.println("hl7pix: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("hl7pix: " + e.getMessage());
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

    public void query(String pid, String[] domains) throws IOException {
        HL7Message qbp = HL7Message.makePixQuery(pid, domains);
        HL7Segment msh = qbp.get(0);
        msh.setSendingApplicationWithFacility(sendingApplication);
        msh.setReceivingApplicationWithFacility(receivingApplication);
        msh.setField(17, charset);
        mllp.writeMessage(qbp.getBytes(charset));
        if (mllp.readMessage() == null)
            throw new IOException("Connection closed by receiver");
    }
}
