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

package org.dcm4che3.tool.hl7pdq;

import org.apache.commons.cli.*;
import org.dcm4che3.hl7.HL7Message;
import org.dcm4che3.hl7.HL7Segment;
import org.dcm4che3.hl7.MLLPConnection;
import org.dcm4che3.hl7.MLLPRelease;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.tool.common.CLIUtils;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class HL7Pdq extends Device {

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.hl7pdq.messages");

    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private MLLPRelease mllpRelease;
    private String sendingApplication = "hl7pdq^dcm4che";
    private String receivingApplication = "";
    private String charset;

    private Socket sock;
    private MLLPConnection mllp;

    public HL7Pdq() throws IOException {
        super("hl7pdq");
        addConnection(conn);
    }

    public void setMLLPRelease(MLLPRelease mllpRelease) {
        this.mllpRelease = mllpRelease;
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
        CLIUtils.addMLLP2Option(opts);
        CLIUtils.addResponseTimeoutOption(opts);
        CLIUtils.addSocketOptions(opts);
        CLIUtils.addTLSOptions(opts);
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, HL7Pdq.class);
    }

    @SuppressWarnings("static-access")
    private static void addCharsetOption(Options opts) {
        opts.addOption(Option.builder()
                .longOpt("charset")
                .hasArg()
                .argName("name")
                .desc(rb.getString("charset"))
                .build());
    }

    @SuppressWarnings("static-access")
    private static void addConnectOption(Options opts) {
        opts.addOption(Option.builder("c")
                .hasArg()
                .argName("app^fac@host:port")
                .desc(rb.getString("connect"))
                .longOpt("connect")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("[user:password@]host:port")
                .desc(rb.getString("proxy"))
                .longOpt("proxy")
                .build());
        CLIUtils.addConnectTimeoutOption(opts);
    }

    @SuppressWarnings("static-access")
    private static void addBindOption(Options opts) {
        opts.addOption(Option.builder("b")
                .hasArg()
                .argName("app^fac[@ip]")
                .desc(rb.getString("bind"))
                .longOpt("bind")
                .build());
    }

    private static void configureConnect(HL7Pdq hl7pdq, CommandLine cl) throws ParseException {
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

        hl7pdq.setReceivingApplication(appHostPort[0]);
        hl7pdq.remote.setHostname(hostPort[0]);
        hl7pdq.remote.setPort(Integer.parseInt(hostPort[1]));
        hl7pdq.remote.setHttpProxy(cl.getOptionValue("proxy"));
    }

    private static void configureBind(HL7Pdq hl7pdq, CommandLine cl) {
        String appAtHost = cl.getOptionValue("b");
        if (appAtHost != null) {
            String[] appHost = HL7Segment.split(appAtHost, '@');
            hl7pdq.setSendingApplication(appHost[0]);
            if (appHost.length > 1)
                hl7pdq.conn.setHostname(appHost[1]);
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            HL7Pdq main = new HL7Pdq();
            configureConnect(main, cl);
            configureBind(main, cl);
            CLIUtils.configure(main.conn, cl);
            main.setMLLPRelease(CLIUtils.isMLLP2(cl) ? MLLPRelease.MLLP2 : MLLPRelease.MLLP1);
            main.setCharacterSet(cl.getOptionValue("charset"));
            main.remote.setTlsProtocols(main.conn.getTlsProtocols());
            main.remote.setTlsCipherSuites(main.conn.getTlsCipherSuites());
            List<String> argList = cl.getArgList();
            int numQueryParams = countQueryParams(argList.iterator());
            if (numQueryParams == 0)
                throw new ParseException(rb.getString("missing"));
            try {
                main.open();
                main.query(argList.subList(0, numQueryParams).toArray(new String[0]),
                        argList.subList(numQueryParams, argList.size()).toArray(new String[0]));
            } finally {
                main.close();
            }
        } catch (ParseException e) {
            System.err.println("hl7pdq: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("hl7pdq: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static int countQueryParams(Iterator<String> args) {
        int count = 0;
        while (args.hasNext() && args.next().charAt(0) == '@') count++;
        return count;
    }

    public void open() throws IOException, IncompatibleConnectionException, GeneralSecurityException {
        sock = conn.connect(remote);
        sock.setSoTimeout(conn.getResponseTimeout());
        mllp = new MLLPConnection(sock, mllpRelease);
    }

    public void close() {
        conn.close(sock);
    }

    public void query(String[] queryParams, String[] domains) throws IOException {
        HL7Message qbp = HL7Message.makePdqQuery(queryParams, domains);
        HL7Segment msh = qbp.get(0);
        msh.setSendingApplicationWithFacility(sendingApplication);
        msh.setReceivingApplicationWithFacility(receivingApplication);
        msh.setField(17, charset);
        mllp.writeMessage(qbp.getBytes(charset));
        if (mllp.readMessage() == null)
            throw new IOException("Connection closed by receiver");
    }
}
