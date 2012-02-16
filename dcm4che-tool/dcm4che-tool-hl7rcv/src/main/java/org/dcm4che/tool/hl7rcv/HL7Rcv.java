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

package org.dcm4che.tool.hl7rcv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.SSLManagerFactory;
import org.dcm4che.tool.common.CLIUtils;
import org.dcm4che.util.StringUtils;
import org.regenstrief.xhl7.HL7XMLReader;
import org.regenstrief.xhl7.HL7XMLWriter;
import org.regenstrief.xhl7.MLLPDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class HL7Rcv extends Device {

    private static final Logger LOG = LoggerFactory.getLogger(HL7Rcv.class);
    private static final ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che.tool.hl7rcv.messages");
    private static final SAXTransformerFactory transfomerFactory =
            (SAXTransformerFactory) TransformerFactory.newInstance();
    private static final AtomicInteger nextMessageControlID =
            new AtomicInteger(new Random().nextInt());

    private Charset charset;
    private Templates templates;

    private Connection conn = new Connection() {

        @Override
        protected void onAccept(Socket s) throws IOException {
            HL7Rcv.this.onAccept(s);
        }
    };

    public HL7Rcv() throws IOException {
        super("hl7rcv");
        addConnection(conn);
    }

    public String getCharsetName() {
        return charset != null ? charset.name() : null;
    }

    public void setCharsetName(String charsetName) {
        this.charset = Charset.forName(charsetName);
    }

    public Templates getTemplates() {
        return templates;
    }

    public void setTemplates(String url)
            throws IOException, TransformerConfigurationException {
        InputStream in = SSLManagerFactory.openFileOrURL(url);
        try {
            this.templates = transfomerFactory.newTemplates(new StreamSource(in));
        } finally {
            in.close();
        }
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        addBindServerOption(opts);
        addIdleTimeoutOption(opts);
        addCharsetNameOption(opts);
        addXSLOption(opts);
        CLIUtils.addSocketOptions(opts);
        CLIUtils.addTLSOptions(opts);
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, HL7Rcv.class);
    }

    @SuppressWarnings("static-access")
    private static void addBindServerOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("[ip:]port")
                .withDescription(rb.getString("bind-server"))
                .withLongOpt("bind")
                .create("b"));
    }

    @SuppressWarnings("static-access")
    private static void addIdleTimeoutOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("ms")
                .withDescription(rb.getString("idle-timeout"))
                .withLongOpt("idle-timeout")
                .create(null));
    }

    @SuppressWarnings("static-access")
    private static void addCharsetNameOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("name")
                .withDescription(rb.getString("charset"))
                .withLongOpt("charset")
                .create(null));
    }

    @SuppressWarnings("static-access")
    private static void addXSLOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("xsl"))
                .withLongOpt("xsl")
                .create(null));
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            HL7Rcv main = new HL7Rcv();
            configureBindServer(main.conn, cl);
            CLIUtils.configure(main.conn, cl);
            main.setCharsetName(cl.getOptionValue("charset", "ISO-8859-1"));
            main.setTemplates(cl.getOptionValue("xsl", "resource:hl7-ack.xsl"));
            ExecutorService executorService = Executors.newCachedThreadPool();
            ScheduledExecutorService scheduledExecutorService = 
                    Executors.newSingleThreadScheduledExecutor();
            main.setScheduledExecutor(scheduledExecutorService);
            main.setExecutor(executorService);
            main.activate();
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

    private void onAccept(Socket s) throws IOException {
        s.setSoTimeout(conn.getIdleTimeout());
        MLLPDriver mllpDriver = 
                new MLLPDriver(s.getInputStream(), s.getOutputStream(), false);
        MyByteArrayOutputStream inbuf = new MyByteArrayOutputStream();
        MyByteArrayOutputStream outbuf = new MyByteArrayOutputStream();
        while (mllpDriver.hasMoreInput()) {
            inbuf.write(mllpDriver.getInputStream());
            LOG.info("Received HL7 Message: {}",
                    promptHL7(inbuf.buf(), inbuf.size()));
            try {
                process(inbuf.buf(), inbuf.size(), outbuf);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            inbuf.reset();
            LOG.info("Send HL7 Message: {}",
                    promptHL7(outbuf.buf(), outbuf.size()));
            mllpDriver.getOutputStream().write(outbuf.buf(), 0, outbuf.size());
            outbuf.reset();
        }
        conn.close(s);
    }

    private void process(byte[] buf, int size, OutputStream out)
            throws Exception  {
        TransformerHandler th =
                transfomerFactory.newTransformerHandler(templates);
        th.setResult(new SAXResult(new HL7XMLWriter(
                new OutputStreamWriter(out, charset))));
        Transformer t = th.getTransformer();
        t.setParameter("MessageControlID",
                nextMessageControlID.getAndIncrement() & 0x7FFFFFFF);
        HL7XMLReader r = new HL7XMLReader();
        r.setContentHandler(th);
        r.parse(new InputSource(new InputStreamReader(
                new ByteArrayInputStream(buf, 0, size), charset)));
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
                StringUtils.appendLine(sb, new String(buf, off, len, charset));
                off += len + 1;
                len = 0;
            }
        if (len > 0) {
            StringUtils.appendLine(sb, new String(buf, off, len, charset));
        }
        return sb.toString();
    }
}
