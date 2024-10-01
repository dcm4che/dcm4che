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

package org.dcm4che3.tool.hl7rcv;

import org.apache.commons.cli.*;
import org.dcm4che3.hl7.*;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Connection.Protocol;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.hl7.HL7Application;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4che3.net.hl7.HL7MessageListener;
import org.dcm4che3.net.hl7.UnparsedHL7Message;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.StringUtils;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class HL7Rcv {

    private static final ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.hl7rcv.messages");
    private static SAXTransformerFactory factory =
            (SAXTransformerFactory) TransformerFactory.newInstance();

    private final Device device = new Device("hl7rcv");
    private final HL7DeviceExtension hl7Ext = new HL7DeviceExtension();
    private final HL7Application hl7App = new HL7Application("*");
    private final Connection conn = new Connection();
    private String storageDir;
    private String charset;
    private Templates tpls;
    private String[] xsltParams;
    private boolean useUUIDForFilename;
    private int responseDelay;

    private final HL7MessageListener handler = new HL7MessageListener() {

        @Override
        public UnparsedHL7Message onMessage(HL7Application hl7App, Connection conn, Socket s, UnparsedHL7Message msg)
                throws HL7Exception {
            try {
                return HL7Rcv.this.onMessage(msg);
            } catch (Exception e) {
                throw new HL7Exception(
                        new ERRSegment(msg.msh()).setUserMessage(e.getMessage()),
                        e);
            }
        }
    };

    public HL7Rcv() throws IOException {
        conn.setProtocol(Protocol.HL7);
        device.addDeviceExtension(hl7Ext);
        device.addConnection(conn);
        hl7Ext.addHL7Application(hl7App);
        hl7App.setAcceptedMessageTypes("*");
        hl7App.addConnection(conn);
        hl7App.setHL7MessageListener(handler);
    }

    public void setStorageDirectory(String storageDir) {
        this.storageDir = storageDir;
    }

    public void setXSLT(URL xslt) throws Exception {
        tpls = SAXTransformer.newTemplates(
                new StreamSource(xslt.openStream(), xslt.toExternalForm()));
    }

    public void setXSLTParameters(String[] xsltParams) {
        this.xsltParams = xsltParams;
    }

    public void setCharacterSet(String charset) {
        this.charset = charset;
    }

    public void setUseUUIDForFilename(boolean useUUIDForFilename) {
        this.useUUIDForFilename = useUUIDForFilename;
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        addOptions(opts);
        CLIUtils.addMLLP2Option(opts);
        CLIUtils.addSocketOptions(opts);
        CLIUtils.addTLSOptions(opts);
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, HL7Rcv.class);
    }

    @SuppressWarnings("static-access")
    public static void addOptions(Options opts) {
        opts.addOption(null, "ignore", false, rb.getString("ignore"));
        opts.addOption(null, "uuid", false, rb.getString("uuid"));
        opts.addOption(Option.builder()
                .hasArg()
                .argName("path")
                .desc(rb.getString("directory"))
                .longOpt("directory")
                .build());
        opts.addOption(Option.builder("x")
                .longOpt("xsl")
                .hasArg()
                .argName("xsl-file")
                .desc(rb.getString("xsl"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("xsl-param")
                .hasArgs()
                .valueSeparator('=')
                .argName("name=value")
                .desc(rb.getString("xsl-param"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("charset")
                .hasArg()
                .argName("name")
                .desc(rb.getString("charset"))
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
        opts.addOption(Option.builder()
                .hasArg()
                .argName("ms")
                .desc(rb.getString("response-delay"))
                .longOpt("response-delay")
                .build());
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            HL7Rcv main = new HL7Rcv();
            configure(main, cl);
            ExecutorService executorService = Executors.newCachedThreadPool();
            ScheduledExecutorService scheduledExecutorService = 
                    Executors.newSingleThreadScheduledExecutor();
            main.device.setScheduledExecutor(scheduledExecutorService);
            main.device.setExecutor(executorService);
            main.device.bindConnections();
        } catch (ParseException e) {
            System.err.println("hl7rcv: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("hl7rcv: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void configure(HL7Rcv main, CommandLine cl)
            throws Exception {
        main.setUseUUIDForFilename(cl.hasOption("uuid"));
        if (!cl.hasOption("ignore"))
            main.setStorageDirectory(
                    cl.getOptionValue("directory", "."));
        if (cl.hasOption("x")) {
            String s = cl.getOptionValue("x");
            main.setXSLT(new File(s).toURI().toURL());
            main.setXSLTParameters(cl.getOptionValues("xsl-param"));
        }
        main.setCharacterSet(cl.getOptionValue("charset"));
        main.responseDelay = CLIUtils.getIntOption(cl, "response-delay", 0);
        main.conn.setProtocol(CLIUtils.isMLLP2(cl) ? Protocol.HL7_MLLP2 : Protocol.HL7);
        configureBindServer(main.conn, cl);
        CLIUtils.configure(main.conn, cl);
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

    private UnparsedHL7Message onMessage(UnparsedHL7Message msg)
                throws Exception {
            if (storageDir != null)
                storeToFile(msg.data(), new File(
                            new File(storageDir, msg.msh().getMessageType()),
                                    useUUIDForFilename
                                            ? UUID.randomUUID().toString()
                                            : msg.msh().getField(9, "_NULL_")));
            if (responseDelay > 0)
                try {
                    Thread.sleep(responseDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
        return new UnparsedHL7Message(tpls == null
                ? HL7Message.makeACK(msg.msh(), HL7Exception.AA, null).getBytes(null)
                : xslt(msg));
    }

    private void storeToFile(byte[] data, File f) throws IOException {
        Connection.LOG.info("M-WRITE {}", f);
        f.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(f);
        try {
            out.write(data);
        } finally {
            out.close();
        }
    }

    private byte[] xslt(UnparsedHL7Message msg)
            throws Exception {
        String charsetName = HL7Charset.toCharsetName(msg.msh().getField(17, charset));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TransformerHandler th = factory.newTransformerHandler(tpls);
        Transformer t = th.getTransformer();
        t.setParameter("MessageControlID", HL7Segment.nextMessageControlID());
        t.setParameter("DateTimeOfMessage", HL7Segment.timeStamp(new Date()));
        if (xsltParams != null)
            for (int i = 1; i < xsltParams.length; i++, i++)
                t.setParameter(xsltParams[i-1], xsltParams[i]);
        th.setResult(new SAXResult(new HL7ContentHandler(
                new OutputStreamWriter(out, charsetName))));
        new HL7Parser(th).parse(new InputStreamReader(
                new ByteArrayInputStream(msg.data()),
                charsetName));
        return out.toByteArray();
    }

    public Device getDevice() {
        return device;
    }

    public Connection getConn() {
        return conn;
    }
}
