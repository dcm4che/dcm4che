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

package org.dcm4che.tool.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che.data.ElementDictionary;
import org.dcm4che.io.DicomEncodingOptions;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.SSLManagerFactory;
import org.dcm4che.net.Priority;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.UserIdentityRQ;
import org.dcm4che.util.SafeClose;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class CLIUtils {

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che.tool.common.messages");

    public static void addCommonOptions(Options opts) {
        opts.addOption("h", "help", false, rb.getString("help"));
        opts.addOption("V", "version", false, rb.getString("version"));
    }

    @SuppressWarnings("static-access")
    public static void addBindOption(Options opts, String defAET) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("aet[@ip][:port]")
                .withDescription(
                        MessageFormat.format(rb.getString("bind"), defAET))
                .withLongOpt("bind")
                .create("b"));
    }

    @SuppressWarnings("static-access")
    public static void addBindServerOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("[aet[@ip]:]port")
                .withDescription(rb.getString("bind-server"))
                .withLongOpt("bind")
                .create("b"));
    }

    @SuppressWarnings("static-access")
    public static void addConnectOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("aet@host:port")
                .withDescription(rb.getString("connect"))
                .withLongOpt("connect")
                .create("c"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("name")
                .withDescription(rb.getString("user"))
                .withLongOpt("user")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("password")
                .withDescription(rb.getString("user-pass"))
                .withLongOpt("user-pass")
                .create(null));
        opts.addOption(null, "user-rsp", false, rb.getString("user-rsp"));
    }

    @SuppressWarnings("static-access")
    public static void addAEOptions(Options opts, boolean requestor,
            boolean acceptor) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("length")
                .withDescription(rb.getString("max-pdulen-rcv"))
                .withLongOpt("max-pdulen-rcv")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("length")
                .withDescription(rb.getString("max-pdulen-snd"))
                .withLongOpt("max-pdulen-snd")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("no")
                .withDescription(rb.getString("max-ops-invoked"))
                .withLongOpt("max-ops-invoked")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("no")
                .withDescription(rb.getString("max-ops-performed"))
                .withLongOpt("max-ops-performed")
                .create(null));
        opts.addOption(null, "not-async", false, rb.getString("not-async"));
        opts.addOption(null, "not-pack-pdv", false, rb.getString("not-pack-pdv"));
        if (requestor) {
            opts.addOption(OptionBuilder
                    .hasArg()
                    .withArgName("timeout")
                    .withDescription(rb.getString("connect-timeout"))
                    .withLongOpt("connect-timeout")
                    .create(null));
            opts.addOption(OptionBuilder
                    .hasArg()
                    .withArgName("timeout")
                    .withDescription(rb.getString("accept-timeout"))
                    .withLongOpt("accept-timeout")
                    .create(null));
        }
        if (acceptor) {
            opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("timeout")
                .withDescription(rb.getString("request-timeout"))
                .withLongOpt("request-timeout")
                .create(null));
        }
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("timeout")
                .withDescription(rb.getString("idle-timeout"))
                .withLongOpt("idle-timeout")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("timeout")
                .withDescription(rb.getString("release-timeout"))
                .withLongOpt("release-timeout")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("delay")
                .withDescription(rb.getString("soclose-delay"))
                .withLongOpt("soclose-delay")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("length")
                .withDescription(rb.getString("sosnd-buffer"))
                .withLongOpt("sosnd-buffer")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("length")
                .withDescription(rb.getString("sorcv-buffer"))
                .withLongOpt("sorcv-buffer")
                .create(null));
        opts.addOption(null, "tcp-delay", false, rb.getString("tcp-delay"));
        addTLSOptions(opts);
    }

    @SuppressWarnings("static-access")
    public static void addCStoreRspOption(Options opts) {
        opts.addOption(OptionBuilder
            .hasArg()
            .withArgName("timeout")
            .withDescription(rb.getString("cstore-rsp-timeout"))
            .withLongOpt("cstore-rsp-timeout")
            .create(null));
    }

    @SuppressWarnings("static-access")
    public static void addCGetRspOption(Options opts) {
        opts.addOption(OptionBuilder
            .hasArg()
            .withArgName("timeout")
            .withDescription(rb.getString("cget-rsp-timeout"))
            .withLongOpt("cget-rsp-timeout")
            .create(null));
    }

    @SuppressWarnings("static-access")
    public static void addCFindRspOption(Options opts) {
        opts.addOption(OptionBuilder
            .hasArg()
            .withArgName("timeout")
            .withDescription(rb.getString("cfind-rsp-timeout"))
            .withLongOpt("cfind-rsp-timeout")
            .create(null));
    }

    @SuppressWarnings("static-access")
    public static void addCMoveRspOption(Options opts) {
        opts.addOption(OptionBuilder
            .hasArg()
            .withArgName("timeout")
            .withDescription(rb.getString("cmove-rsp-timeout"))
            .withLongOpt("cmove-rsp-timeout")
            .create(null));
    }

    @SuppressWarnings("static-access")
    public static void addCEchoRspOption(Options opts) {
        opts.addOption(OptionBuilder
            .hasArg()
            .withArgName("timeout")
            .withDescription(rb.getString("cecho-rsp-timeout"))
            .withLongOpt("cecho-rsp-timeout")
            .create(null));
    }

    @SuppressWarnings("static-access")
    public static void addNEventRspOption(Options opts) {
        opts.addOption(OptionBuilder
            .hasArg()
            .withArgName("timeout")
            .withDescription(rb.getString("nevent-report-rsp-timeout"))
            .withLongOpt("nevent-report-rsp-timeout")
            .create(null));
    }

    @SuppressWarnings("static-access")
    public static void addNGetRspOption(Options opts) {
        opts.addOption(OptionBuilder
            .hasArg()
            .withArgName("timeout")
            .withDescription(rb.getString("nget-rsp-timeout"))
            .withLongOpt("nget-rsp-timeout")
            .create(null));
    }

    @SuppressWarnings("static-access")
    public static void addNSetRspOption(Options opts) {
        opts.addOption(OptionBuilder
            .hasArg()
            .withArgName("timeout")
            .withDescription(rb.getString("nset-rsp-timeout"))
            .withLongOpt("nset-rsp-timeout")
            .create(null));
    }

    @SuppressWarnings("static-access")
    public static void addNActionRspOption(Options opts) {
        opts.addOption(OptionBuilder
            .hasArg()
            .withArgName("timeout")
            .withDescription(rb.getString("naction-rsp-timeout"))
            .withLongOpt("naction-rsp-timeout")
            .create(null));
    }

    @SuppressWarnings("static-access")
    public static void addNCreateRspOption(Options opts) {
        opts.addOption(OptionBuilder
            .hasArg()
            .withArgName("timeout")
            .withDescription(rb.getString("ncreate-rsp-timeout"))
            .withLongOpt("ncreate-rsp-timeout")
            .create(null));
    }

    @SuppressWarnings("static-access")
    public static void addNDeleteRspOption(Options opts) {
        opts.addOption(OptionBuilder
            .hasArg()
            .withArgName("timeout")
            .withDescription(rb.getString("ndelete-rsp-timeout"))
            .withLongOpt("ndelete-rsp-timeout")
            .create(null));
    }

    @SuppressWarnings("static-access")
    private static void addTLSOptions(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("cipher")
                .withDescription(rb.getString("tls-cipher"))
                .withLongOpt("tls-cipher")
                .create(null));
        opts.addOption(null, "tls", false, rb.getString("tls"));
        opts.addOption(null, "tls-null", false, rb.getString("tls-null"));
        opts.addOption(null, "tls-3des", false, rb.getString("tls-3des"));
        opts.addOption(null, "tls-aes", false, rb.getString("tls-aes"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("protocol")
                .withDescription(rb.getString("tls-protocol"))
                .withLongOpt("tls-protocol")
                .create(null));
        opts.addOption(null, "tls1", false, rb.getString("tls1"));
        opts.addOption(null, "ssl3", false, rb.getString("ssl3"));
        opts.addOption(null, "ssl2Hello", false, rb.getString("ssl2Hello"));
        opts.addOption(null, "tls-noauth", false, rb.getString("tls-noauth"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("key-store"))
                .withLongOpt("key-store")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("storetype")
                .withDescription(rb.getString("key-store-type"))
                .withLongOpt("key-store-type")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("password")
                .withDescription(rb.getString("key-store-pass"))
                .withLongOpt("key-store-pass")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("password")
                .withDescription(rb.getString("key-pass"))
                .withLongOpt("key-pass")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("trust-store"))
                .withLongOpt("trust-store")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("storetype")
                .withDescription(rb.getString("trust-store-type"))
                .withLongOpt("trust-store-type")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("password")
                .withDescription(rb.getString("trust-store-pass"))
                .withLongOpt("trust-store-pass")
                .create(null));
    }

    @SuppressWarnings("static-access")
    public static void addPriorityOption(Options opts) {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder
                .withLongOpt("prior-high")
                .withDescription(rb.getString("prior-high"))
                .create());
        group.addOption(OptionBuilder
                .withLongOpt("prior-low")
                .withDescription(rb.getString("prior-low"))
                .create());
        opts.addOptionGroup(group);
    }

    public static CommandLine parseComandLine(String[] args, Options opts, 
            ResourceBundle rb2, Class<?> clazz) throws ParseException {
        CommandLineParser parser = new PosixParser();
        CommandLine cl = parser.parse(opts, args);
        if (cl.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                    rb2.getString("usage"),
                    rb2.getString("description"), opts,
                    rb2.getString("example"));
            System.exit(0);
        }
        if (cl.hasOption("V")) {
            Package p = clazz.getPackage();
            String s = p.getName();
            System.out.println(s.substring(s.lastIndexOf('.')+1) + ": " +
                   p.getImplementationVersion());
            System.exit(0);
        }
        return cl;
    }

    public static void configureConnect(Connection conn,
            AAssociateRQ rq, CommandLine cl) throws ParseException {
        if (!cl.hasOption("c"))
            throw new MissingOptionException(
                    rb.getString("missing-connect-opt"));
        String aeAtHostPort = cl.getOptionValue("c");
        String[] aeHostPort = split(aeAtHostPort , '@', 0);
        if (aeHostPort[1] == null)
            throw new ParseException(rb.getString("invalid-connect-opt"));
        
        String[] hostPort = split(aeHostPort[1], ':', 0);
        if (hostPort[1] == null)
            throw new ParseException(rb.getString("invalid-connect-opt"));

        rq.setCalledAET(aeHostPort[0]);
        conn.setHostname(hostPort[0]);
        conn.setPort(Integer.parseInt(hostPort[1]));

        if (cl.hasOption("user"))
            rq.setUserIdentityRQ(cl.hasOption("user-pass")
                    ? new UserIdentityRQ(cl.getOptionValue("user"),
                            cl.getOptionValue("user-pass").toCharArray())
                    : new UserIdentityRQ(cl.getOptionValue("user"),
                            cl.hasOption("user-rsp")));
    }

    public static void configureBind(Connection conn,
            ApplicationEntity ae, CommandLine cl) throws ParseException {
        if (cl.hasOption("b")) {
            String aeAtHostPort = cl.getOptionValue("b");
            String[] aeHostPort = split(aeAtHostPort, '@', 0);
            ae.setAETitle(aeHostPort[0]);
            if (aeHostPort[1] != null) {
                String[] hostPort = split(aeHostPort[1], ':', 0);
                conn.setHostname(hostPort[0]);
                if (hostPort[1] != null)
                    conn.setPort(Integer.parseInt(hostPort[1]));
            }
        }
    }

    public static void configureBindServer(Connection conn,
            ApplicationEntity ae, CommandLine cl) throws ParseException {
        if (!cl.hasOption("b"))
            throw new MissingOptionException(rb.getString("missing-bind-opt"));
        String aeAtHostPort = cl.getOptionValue("b");
        String[] aeAtHostAndPort = split(aeAtHostPort, ':', 1);
        conn.setPort(Integer.parseInt(aeAtHostAndPort[1]));
        if (aeAtHostAndPort[0] != null) {
            String[] aeHost = split(aeAtHostAndPort[0], '@', 0);
            ae.setAETitle(aeHost[0]);
            if (aeHost[1] != null)
                conn.setHostname(aeHost[1]);
        }
    }

    private static String[] split(String s, char delim, int defPos) {
        String[] s2 = new String[2];
        int pos = s.indexOf(delim);
        if (pos != -1) {
            s2[0] = s.substring(0, pos);
            s2[1] = s.substring(pos + 1);
        } else {
            s2[defPos] = s;
        }
        return s2;
    }

    public static int priorityOf(CommandLine cl) {
        return cl.hasOption("prior-high")
                ? Priority.HIGH
                : cl.hasOption("prior-low") 
                        ? Priority.LOW
                        : Priority.NORMAL;
    }

    public static int getIntOption(CommandLine cl, String opt, int defVal) {
        String optVal = cl.getOptionValue(opt);
        if (optVal == null)
            return defVal;
        
        return optVal.endsWith("H")
                ? Integer.parseInt(optVal.substring(0, optVal.length() - 1), 16)
                : Integer.parseInt(optVal);
    }

    public static void configure(Connection conn, ApplicationEntity ae,
            CommandLine cl)
            throws ParseException, IOException {
        if (cl.hasOption("max-pdulen-rcv"))
            conn.setReceivePDULength(Integer.parseInt(
                    cl.getOptionValue("max-pdulen-rcv")));
        if (cl.hasOption("max-pdulen-snd"))
            conn.setSendPDULength(Integer.parseInt(
                    cl.getOptionValue("max-pdulen-snd")));
        if(cl.hasOption("not-async")) {
            conn.setMaxOpsInvoked(1);
            conn.setMaxOpsPerformed(1);
        } else {
            int maxOpsInvoked = 0;
            if (cl.hasOption("max-ops-invoked"))
                maxOpsInvoked = Integer.parseInt(
                        cl.getOptionValue("max-ops-invoked"));
            conn.setMaxOpsInvoked(maxOpsInvoked);
            int maxOpsPerformed = 0;
            if (cl.hasOption("max-ops-performed"))
                maxOpsPerformed = Integer.parseInt(
                        cl.getOptionValue("max-ops-performed"));
            conn.setMaxOpsPerformed(maxOpsPerformed);
        }
        conn.setPackPDV(!cl.hasOption("not-pack-pdv"));
        if (cl.hasOption("connect-timeout"))
            conn.setConnectTimeout(
                    Integer.parseInt(cl.getOptionValue("connect-timeout")));
        if (cl.hasOption("request-timeout"))
            conn.setRequestTimeout(
                    Integer.parseInt(cl.getOptionValue("request-timeout")));
        if (cl.hasOption("accept-timeout"))
            conn.setAcceptTimeout(
                    Integer.parseInt(cl.getOptionValue("accept-timeout")));
        if (cl.hasOption("release-timeout"))
            conn.setReleaseTimeout(
                    Integer.parseInt(cl.getOptionValue("release-timeout")));
        if (cl.hasOption("cstore-rsp-timeout"))
            conn.setCStoreRSPTimeout(
                    Integer.parseInt(cl.getOptionValue("cstore-rsp-timeout")));
        if (cl.hasOption("cget-rsp-timeout"))
            conn.setCGetRSPTimeout(
                    Integer.parseInt(cl.getOptionValue("cget-rsp-timeout")));
        if (cl.hasOption("cfind-rsp-timeout"))
            conn.setCFindRSPTimeout(
                    Integer.parseInt(cl.getOptionValue("cfind-rsp-timeout")));
        if (cl.hasOption("cmove-rsp-timeout"))
            conn.setCMoveRSPTimeout(
                    Integer.parseInt(cl.getOptionValue("cmove-rsp-timeout")));
        if (cl.hasOption("cecho-rsp-timeout"))
            conn.setCEchoRSPTimeout(
                    Integer.parseInt(cl.getOptionValue("cecho-rsp-timeout")));
        if (cl.hasOption("nevent-report-rsp-timeout"))
            conn.setNEventReportRSPTimeout(
                    Integer.parseInt(cl.getOptionValue("nevent-report-rsp-timeout")));
        if (cl.hasOption("nget-rsp-timeout"))
            conn.setNGetRSPTimeout(
                    Integer.parseInt(cl.getOptionValue("nget-rsp-timeout")));
        if (cl.hasOption("nset-rsp-timeout"))
            conn.setNSetRSPTimeout(
                    Integer.parseInt(cl.getOptionValue("nset-rsp-timeout")));
        if (cl.hasOption("naction-rsp-timeout"))
            conn.setNActionRSPTimeout(
                    Integer.parseInt(cl.getOptionValue("naction-rsp-timeout")));
        if (cl.hasOption("ncreate-rsp-timeout"))
            conn.setNCreateRSPTimeout(
                    Integer.parseInt(cl.getOptionValue("ncreate-rsp-timeout")));
        if (cl.hasOption("ndelete-rsp-timeout"))
            conn.setNDeleteRSPTimeout(
                    Integer.parseInt(cl.getOptionValue("ndelete-rsp-timeout")));
        if (cl.hasOption("idle-timeout"))
            conn.setIdleTimeout(
                    Integer.parseInt(cl.getOptionValue("idle-timeout")));
        if (cl.hasOption("soclose-delay"))
            conn.setSocketCloseDelay(
                    Integer.parseInt(cl.getOptionValue("soclose-delay")));
        if (cl.hasOption("sosnd-buffer"))
            conn.setSendBufferSize(
                    Integer.parseInt(cl.getOptionValue("sosnd-buffer")));
        if (cl.hasOption("sorcv-buffer"))
            conn.setReceiveBufferSize(
                    Integer.parseInt(cl.getOptionValue("sorcv-buffer")));
        conn.setTcpNoDelay(!cl.hasOption("tcp-delay"));
        configureTLS(conn, cl);
    }

    private static void configureTLS(Connection conn, CommandLine cl)
            throws ParseException, IOException {
        if (cl.hasOption("tls"))
            conn.setTlsCipherSuites(
                    "SSL_RSA_WITH_NULL_SHA",
                    "TLS_RSA_WITH_AES_128_CBC_SHA",
                    "SSL_RSA_WITH_3DES_EDE_CBC_SHA");
        else if (cl.hasOption("tls-null"))
            conn.setTlsCipherSuites("SSL_RSA_WITH_NULL_SHA");
        else if (cl.hasOption("tls-3des"))
            conn.setTlsCipherSuites("SSL_RSA_WITH_3DES_EDE_CBC_SHA");
        else if (cl.hasOption("tls-aes"))
            conn.setTlsCipherSuites(
                    "TLS_RSA_WITH_AES_128_CBC_SHA",
                    "SSL_RSA_WITH_3DES_EDE_CBC_SHA");
        else if (cl.hasOption("tls-cipher"))
            conn.setTlsCipherSuites(cl.getOptionValues("tls-cipher"));
        else
            return;

        if (cl.hasOption("tls1"))
            conn.setTlsProtocols("TLSv1");
        else if (cl.hasOption("ssl3"))
            conn.setTlsProtocols("SSLv3");
        else if (cl.hasOption("ssl2Hello"))
            conn.setTlsProtocols("SSLv2Hello", "SSLv3", "TLSv1");
        else if (cl.hasOption("tls-protocol"))
            conn.setTlsProtocols(cl.getOptionValues("tls-protocol"));

        conn.setTlsNeedClientAuth(!cl.hasOption("tls-noauth"));

        String keyStoreURL = cl.getOptionValue("key-store", "resource:key.jks");
        String keyStoreType =  cl.getOptionValue("key-store-type", "JKS");
        String keyStorePass = cl.getOptionValue("key-store-pass", "secret");
        String keyPass = cl.getOptionValue("key-pass", keyStorePass);
        String trustStoreURL = cl.getOptionValue("trust-store", "resource:cacerts.jks");
        String trustStoreType =  cl.getOptionValue("trust-store-type", "JKS");
        String trustStorePass = cl.getOptionValue("trust-store-pass", "secret");

        Device device = conn.getDevice();
        try {
            device.setKeyManager(SSLManagerFactory.createKeyManager(
                    keyStoreType, keyStoreURL, keyStorePass, keyPass));
            device.setTrustManager(SSLManagerFactory.createTrustManager(
                    trustStoreType, trustStoreURL, trustStorePass));
        } catch (UnrecoverableKeyException e) {
            throw new IOException(e);
        } catch (KeyStoreException e) {
            throw new IOException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        } catch (CertificateException e) {
            throw new IOException(e);
        }
    }

    public static Properties loadProperties(String url, Properties p)
            throws IOException {
        if (p == null)
            p = new Properties();
        InputStream in = SSLManagerFactory.openFileOrURL(url);
        try {
            p.load(in);
        } finally {
            SafeClose.close(in);
        }
        return p;
    }

    @SuppressWarnings("static-access")
    public static void addEncodingOptions(Options opts) {
        opts.addOption(null, "group-len", false, rb.getString("group-len"));
        OptionGroup sqlenGroup = new OptionGroup();
        sqlenGroup.addOption(OptionBuilder
                .withLongOpt("expl-seq-len")
                .withDescription(rb.getString("expl-seq-len"))
                .create(null));
        sqlenGroup.addOption(OptionBuilder
                .withLongOpt("undef-seq-len")
                .withDescription(rb.getString("undef-seq-len"))
                .create(null));
        opts.addOptionGroup(sqlenGroup);
        OptionGroup itemlenGroup = new OptionGroup();
        itemlenGroup.addOption(OptionBuilder
                .withLongOpt("expl-item-len")
                .withDescription(rb.getString("expl-item-len"))
                .create(null));
        itemlenGroup.addOption(OptionBuilder
                .withLongOpt("undef-item-len")
                .withDescription(rb.getString("undef-item-len"))
                .create(null));
        opts.addOptionGroup(itemlenGroup);
    }

    public static DicomEncodingOptions encodingOptionsOf(CommandLine cl)
            throws ParseException {
        if (cl.hasOption("expl-item-len") && cl.hasOption("undef-item-len")
                || cl.hasOption("expl-seq-len") && cl.hasOption("undef-seq-len"))
                throw new ParseException(
                        rb.getString("conflicting-enc-opts"));
        return new DicomEncodingOptions(
                cl.hasOption("group-len"),
                !cl.hasOption("expl-seq-len"),
                cl.hasOption("undef-seq-len"),
                !cl.hasOption("expl-item-len"),
                cl.hasOption("undef-item-len"));
    }

    public static int[] toTags(String[] tagOrKeywords) {
        int[] tags = new int[tagOrKeywords.length];
        for (int i = 0; i < tags.length; i++)
            tags[i] = toTag(tagOrKeywords[i]);
        return tags;
    }

    public static int toTag(String tagOrKeyword) {
        try {
            return Integer.parseInt(tagOrKeyword, 16);
        } catch (IllegalArgumentException e) {
            int tag = ElementDictionary.tagForKeyword(tagOrKeyword, null);
            if (tag == -1)
                throw new IllegalArgumentException(tagOrKeyword);
            return tag;
        }
    }

    @SuppressWarnings("static-access")
    public static void addFilesetInfoOptions(Options opts) {
        opts.addOption(OptionBuilder
                .withLongOpt("fs-desc")
                .hasArg()
                .withArgName("txtfile")
                .withDescription(rb.getString("fs-desc"))
                .create());
        opts.addOption(OptionBuilder
                .withLongOpt("fs-desc-cs")
                .hasArg()
                .withArgName("code")
                .withDescription(rb.getString("fs-desc-cs"))
                .create());
        opts.addOption(OptionBuilder
                .withLongOpt("fs-id")
                .hasArg()
                .withArgName("id")
                .withDescription(rb.getString("fs-id"))
                .create());
        opts.addOption(OptionBuilder
                .withLongOpt("fs-uid")
                .hasArg()
                .withArgName("uid")
                .withDescription(rb.getString("fs-uid"))
                .create());
    }

    public static void configure(FilesetInfo fsInfo, CommandLine cl) {
        fsInfo.setFilesetUID(cl.getOptionValue("fs-uid"));
        fsInfo.setFilesetID(cl.getOptionValue("fs-id"));
        if (cl.hasOption("fs-desc"))
            fsInfo.setDescriptorFile(new File(cl.getOptionValue("fs-desc")));
        fsInfo.setDescriptorFileCharset(cl.getOptionValue("fs-desc-cs"));
    }


}
