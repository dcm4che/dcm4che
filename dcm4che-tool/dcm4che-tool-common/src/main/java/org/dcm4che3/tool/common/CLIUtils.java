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

package org.dcm4che3.tool.common;

import org.apache.commons.cli.*;
import org.dcm4che3.data.*;
import org.dcm4che3.io.BasicBulkDataDescriptor;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.UserIdentityRQ;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class CLIUtils {

    public static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che3.tool.common.messages");

    public static void addCommonOptions(Options opts) {
        opts.addOption("h", "help", false, rb.getString("help"));
        opts.addOption("V", "version", false, rb.getString("version"));
    }

    public static void addBindOption(Options opts, String defAET) {
        opts.addOption(Option.builder("b")
                .hasArg()
                .argName("aet[@ip][:port]")
                .desc(
                        MessageFormat.format(rb.getString("bind"), defAET))
                .longOpt("bind")
                .build());
    }

    public static void addBindClientOption(Options opts, String defAET) {
        opts.addOption(Option.builder("b")
                .hasArg()
                .argName("aet[@ip]")
                .desc(MessageFormat.format(rb.getString("bind-client"), defAET))
                .longOpt("bind")
                .build());
    }

    public static void addConnectOption(Options opts) {
        opts.addOption(Option.builder("c")
                .hasArg()
                .argName("aet@host:port")
                .desc(rb.getString("connect"))
                .longOpt("connect")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("[user:password@]host:port")
                .desc(rb.getString("proxy"))
                .longOpt("proxy")
                .build());
        addUserIdentityOptions(opts);
        addConnectTimeoutOption(opts);
        addAcceptTimeoutOption(opts);
    }

    public static void addBindServerOption(Options opts) {
        opts.addOption(Option.builder("b")
                .hasArg()
                .argName("[aet[@ip]:]port")
                .desc(rb.getString("bind-server"))
                .longOpt("bind")
                .build());
        addRequestTimeoutOption(opts);
    }

    private static void addUserIdentityOptions(Options opts) {
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder()
                .hasArg()
                .argName("name")
                .desc(rb.getString("user"))
                .longOpt("user")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("assertion")
                .desc(rb.getString("user-saml"))
                .longOpt("user-saml")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("token")
                .desc(rb.getString("user-jwt"))
                .longOpt("user-jwt")
                .build());
        opts.addOptionGroup(group);
        opts.addOption(Option.builder()
                .hasArg()
                .argName("password")
                .desc(rb.getString("user-pass"))
                .longOpt("user-pass")
                .build());
        opts.addOption(null, "user-rsp", false, rb.getString("user-rsp"));
    }

    public static void addAEOptions(Options opts) {
        opts.addOption(Option.builder()
                .hasArg()
                .argName("length")
                .desc(rb.getString("max-pdulen-rcv"))
                .longOpt("max-pdulen-rcv")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("length")
                .desc(rb.getString("max-pdulen-snd"))
                .longOpt("max-pdulen-snd")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("no")
                .desc(rb.getString("max-ops-invoked"))
                .longOpt("max-ops-invoked")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("no")
                .desc(rb.getString("max-ops-performed"))
                .longOpt("max-ops-performed")
                .build());
        opts.addOption(null, "not-async", false, rb.getString("not-async"));
        opts.addOption(null, "not-pack-pdv", false, rb.getString("not-pack-pdv"));
        opts.addOption(Option.builder()
                .hasArg()
                .argName("ms")
                .desc(rb.getString("idle-timeout"))
                .longOpt("idle-timeout")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("ms")
                .desc(rb.getString("release-timeout"))
                .longOpt("release-timeout")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("ms")
                .desc(rb.getString("soclose-delay"))
                .longOpt("soclose-delay")
                .build());
        addSocketOptions(opts);
        addTLSOptions(opts);
    }

    public static void addRequestTimeoutOption(Options opts) {
        opts.addOption(Option.builder()
            .hasArg()
            .argName("ms")
            .desc(rb.getString("request-timeout"))
            .longOpt("request-timeout")
            .build());
    }

    public static void addAcceptTimeoutOption(Options opts) {
        opts.addOption(Option.builder()
                .hasArg()
                .argName("ms")
                .desc(rb.getString("accept-timeout"))
                .longOpt("accept-timeout")
                .build());
    }

    public static void addSocketOptions(Options opts) {
        opts.addOption(Option.builder()
                .hasArg()
                .argName("length")
                .desc(rb.getString("sosnd-buffer"))
                .longOpt("sosnd-buffer")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("length")
                .desc(rb.getString("sorcv-buffer"))
                .longOpt("sorcv-buffer")
                .build());
        opts.addOption(null, "tcp-delay", false, rb.getString("tcp-delay"));
    }

    public static void addConnectTimeoutOption(Options opts) {
        opts.addOption(Option.builder()
                .hasArg()
                .argName("ms")
                .desc(rb.getString("connect-timeout"))
                .longOpt("connect-timeout")
                .build());
    }

    public static void addSendTimeoutOption(Options opts) {
        opts.addOption(Option.builder()
            .hasArg()
            .argName("ms")
            .desc(rb.getString("send-timeout"))
            .longOpt("send-timeout")
            .build());
    }

    public static void addStoreTimeoutOption(Options opts) {
        opts.addOption(Option.builder()
            .hasArg()
            .argName("ms")
            .desc(rb.getString("store-timeout"))
            .longOpt("store-timeout")
            .build());
    }

    public static void addResponseTimeoutOption(Options opts) {
        opts.addOption(Option.builder()
            .hasArg()
            .argName("ms")
            .desc(rb.getString("response-timeout"))
            .longOpt("response-timeout")
            .build());
    }

    public static void addRetrieveTimeoutOption(Options opts) {
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder()
            .hasArg()
            .argName("ms")
            .desc(rb.getString("retrieve-timeout"))
            .longOpt("retrieve-timeout")
            .build());
        group.addOption(Option.builder()
            .hasArg()
            .argName("ms")
            .desc(rb.getString("retrieve-timeout-total"))
            .longOpt("retrieve-timeout-total")
            .build());
        opts.addOptionGroup(group);
    }

    public static void addTLSOptions(Options opts) {
        addTLSCipherOptions(opts);
        opts.addOption(Option.builder()
                .hasArg()
                .argName("protocol")
                .desc(rb.getString("tls-protocol"))
                .longOpt("tls-protocol")
                .build());
        opts.addOption(null, "tls1", false, rb.getString("tls1"));
        opts.addOption(null, "tls11", false, rb.getString("tls11"));
        opts.addOption(null, "tls12", false, rb.getString("tls12"));
        opts.addOption(null, "tls13", false, rb.getString("tls13"));
        opts.addOption(null, "ssl3", false, rb.getString("ssl3"));
        opts.addOption(null, "ssl2Hello", false, rb.getString("ssl2Hello"));
        opts.addOption(null, "tls-eia-https", false, rb.getString("tls-eia-https"));
        opts.addOption(null, "tls-eia-ldaps", false, rb.getString("tls-eia-ldaps"));
        opts.addOption(null, "tls-noauth", false, rb.getString("tls-noauth"));
        opts.addOption(Option.builder()
                .hasArg()
                .argName("file|url")
                .desc(rb.getString("key-store"))
                .longOpt("key-store")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("storetype")
                .desc(rb.getString("key-store-type"))
                .longOpt("key-store-type")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("password")
                .desc(rb.getString("key-store-pass"))
                .longOpt("key-store-pass")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("password")
                .desc(rb.getString("key-pass"))
                .longOpt("key-pass")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("file|url")
                .desc(rb.getString("trust-store"))
                .longOpt("trust-store")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("storetype")
                .desc(rb.getString("trust-store-type"))
                .longOpt("trust-store-type")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("password")
                .desc(rb.getString("trust-store-pass"))
                .longOpt("trust-store-pass")
                .build());
    }

    public static void addTLSCipherOptions(Options opts) {
        opts.addOption(Option.builder()
                .hasArg()
                .argName("cipher")
                .desc(rb.getString("tls-cipher"))
                .longOpt("tls-cipher")
                .build());
        opts.addOption(null, "tls", false, rb.getString("tls"));
        opts.addOption(null, "tls-null", false, rb.getString("tls-null"));
        opts.addOption(null, "tls-3des", false, rb.getString("tls-3des"));
        opts.addOption(null, "tls-aes", false, rb.getString("tls-aes"));
    }

    public static void addMLLP2Option(Options opts) {
        opts.addOption(null, "mllp2", false, rb.getString("mllp2"));
    }

    public static boolean isMLLP2(CommandLine cl) {
        return cl.hasOption("mllp2");
    }

    public static void addPriorityOption(Options opts) {
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder()
                .longOpt("prior-high")
                .desc(rb.getString("prior-high"))
                .build());
        group.addOption(Option.builder()
                .longOpt("prior-low")
                .desc(rb.getString("prior-low"))
                .build());
        opts.addOptionGroup(group);
    }

    public static CommandLine parseComandLine(String[] args, Options opts,
            ResourceBundle rb2, Class<?> clazz) throws ParseException {
        CommandLineParser parser = new DetectEndOfOptionsPosixParser();
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

        conn.setHttpProxy(cl.getOptionValue("proxy"));

        if (cl.hasOption("user"))
            rq.setUserIdentityRQ(cl.hasOption("user-pass")
                    ? UserIdentityRQ.usernamePasscode(
                            cl.getOptionValue("user"),
                            cl.getOptionValue("user-pass").toCharArray(),
                            cl.hasOption("user-rsp"))
                    : UserIdentityRQ.username(
                            cl.getOptionValue("user"),
                            cl.hasOption("user-rsp")));
        else if (cl.hasOption("user-saml"))
            rq.setUserIdentityRQ(UserIdentityRQ.saml(
                    cl.getOptionValue("user-saml"),
                    cl.hasOption("user-rsp")));
        else if (cl.hasOption("user-jwt"))
            rq.setUserIdentityRQ(UserIdentityRQ.jwt(
                    cl.getOptionValue("user-jwt"),
                    cl.hasOption("user-rsp")));
    }

    public static void configureBind(Connection conn,
            ApplicationEntity ae, CommandLine cl) throws ParseException {
        if (cl.hasOption("b")) {
            String aeAtHostPort = cl.getOptionValue("b");
            String[] aeAtHostAndPort = split(aeAtHostPort, ':', 0);
            String[] aeHost = split(aeAtHostAndPort[0], '@', 0);
            ae.setAETitle(aeHost[0]);
            if (aeHost[1] != null)
                conn.setHostname(aeHost[1]);
            if (aeAtHostAndPort[1] != null)
                conn.setPort(Integer.parseInt(aeAtHostAndPort[1]));
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
        
        return parseInt(optVal);
    }

    private static int parseInt(String optVal) {
        return optVal.endsWith("H")
                ? Integer.parseInt(optVal.substring(0, optVal.length() - 1), 16)
                : Integer.parseInt(optVal);
    }

    public static int[] getIntsOption(CommandLine cl, String opt) {
        String[] optVals = cl.getOptionValues(opt);
        if (optVals == null)
            return null;

        int[] intVals = new int[optVals.length];
        for (int i = 0; i < optVals.length; i++) {
            intVals[i] = parseInt(optVals[i]);
        }
        return intVals;
    }

    public static void configure(Connection conn, CommandLine cl)
            throws ParseException, IOException {
        conn.setReceivePDULength(
                getIntOption(cl, "max-pdulen-rcv", Connection.DEF_MAX_PDU_LENGTH));
        conn.setSendPDULength(
                getIntOption(cl, "max-pdulen-snd", Connection.DEF_MAX_PDU_LENGTH));
        if (cl.hasOption("not-async")) {
            conn.setMaxOpsInvoked(1);
            conn.setMaxOpsPerformed(1);
        } else {
            conn.setMaxOpsInvoked(getIntOption(cl, "max-ops-invoked", 0));
            conn.setMaxOpsPerformed(getIntOption(cl, "max-ops-performed", 0));
        }
        conn.setPackPDV(!cl.hasOption("not-pack-pdv"));
        conn.setConnectTimeout(getIntOption(cl, "connect-timeout", 0));
        conn.setRequestTimeout(getIntOption(cl, "request-timeout", 0));
        conn.setAcceptTimeout(getIntOption(cl, "accept-timeout", 0));
        conn.setReleaseTimeout(getIntOption(cl, "release-timeout", 0));
        conn.setSendTimeout(getIntOption(cl, "send-timeout", 0));
        conn.setStoreTimeout(getIntOption(cl, "store-timeout", 0));
        conn.setResponseTimeout(getIntOption(cl, "response-timeout", 0));
        if (cl.hasOption("retrieve-timeout")) {
            conn.setRetrieveTimeout(getIntOption(cl, "retrieve-timeout", 0));
            conn.setRetrieveTimeoutTotal(false);
        } else if (cl.hasOption("retrieve-timeout-total")) {
            conn.setRetrieveTimeout(getIntOption(cl, "retrieve-timeout-total", 0));
            conn.setRetrieveTimeoutTotal(true);
        }
        conn.setIdleTimeout(getIntOption(cl, "idle-timeout", 0));
        conn.setSocketCloseDelay(getIntOption(cl, "soclose-delay", 
                Connection.DEF_SOCKETDELAY));
        conn.setSendBufferSize(getIntOption(cl, "sosnd-buffer", 0));
        conn.setReceiveBufferSize(getIntOption(cl, "sorcv-buffer", 0));
        conn.setTcpNoDelay(!cl.hasOption("tcp-delay"));
        configureTLS(conn, cl);
    }

    public static boolean configureTLSCipher(Connection conn, CommandLine cl) throws ParseException {
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

        return conn.isTls();
    }

    private static void configureTLS(Connection conn, CommandLine cl)
            throws ParseException, IOException {
        if (!configureTLSCipher(conn, cl))
            return;

        if (cl.hasOption("tls13"))
            conn.setTlsProtocols("TLSv1.3");
        else if (cl.hasOption("tls12"))
            conn.setTlsProtocols("TLSv1.2");
        else if (cl.hasOption("tls11"))
            conn.setTlsProtocols("TLSv1.1");
        else if (cl.hasOption("tls1"))
            conn.setTlsProtocols("TLSv1");
        else if (cl.hasOption("ssl3"))
            conn.setTlsProtocols("SSLv3");
        else if (cl.hasOption("ssl2Hello"))
            conn.setTlsProtocols("SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2");
        else if (cl.hasOption("tls-protocol"))
            conn.setTlsProtocols(cl.getOptionValues("tls-protocol"));

        if (cl.hasOption("tls-eia-https"))
            conn.setTlsEndpointIdentificationAlgorithm(Connection.EndpointIdentificationAlgorithm.HTTPS);
        else if (cl.hasOption("tls-eia-ldaps"))
            conn.setTlsEndpointIdentificationAlgorithm(Connection.EndpointIdentificationAlgorithm.LDAPS);

        conn.setTlsNeedClientAuth(!cl.hasOption("tls-noauth"));

        String keyStoreURL = cl.getOptionValue("key-store", "resource:key.p12");
        String keyStoreType =  cl.getOptionValue("key-store-type", "PKCS12");
        String keyStorePass = cl.getOptionValue("key-store-pass", "secret");
        String keyPass = cl.getOptionValue("key-pass", keyStorePass);
        String trustStoreURL = cl.getOptionValue("trust-store", "resource:cacerts.p12");
        String trustStoreType =  cl.getOptionValue("trust-store-type", "PKCS12");
        String trustStorePass = cl.getOptionValue("trust-store-pass", "secret");

        Device device = conn.getDevice();
        try {
            if (!keyStoreURL.isEmpty())
                device.setKeyManager(SSLManagerFactory.createKeyManager(
                        keyStoreType, keyStoreURL, keyStorePass, keyPass));
            device.setTrustManager(SSLManagerFactory.createTrustManager(
                    trustStoreType, trustStoreURL, trustStorePass));
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    public static Properties loadProperties(String url, Properties p)
            throws IOException {
        if (p == null)
            p = new Properties();
        InputStream in = StreamUtils.openFileOrURL(url);
        try {
            p.load(in);
        } finally {
            SafeClose.close(in);
        }
        return p;
    }

    public static void addEncodingOptions(Options opts) {
        opts.addOption(null, "group-len", false, rb.getString("group-len"));
        OptionGroup sqlenGroup = new OptionGroup();
        sqlenGroup.addOption(Option.builder()
                .longOpt("expl-seq-len")
                .desc(rb.getString("expl-seq-len"))
                .build());
        sqlenGroup.addOption(Option.builder()
                .longOpt("undef-seq-len")
                .desc(rb.getString("undef-seq-len"))
                .build());
        opts.addOptionGroup(sqlenGroup);
        OptionGroup itemlenGroup = new OptionGroup();
        itemlenGroup.addOption(Option.builder()
                .longOpt("expl-item-len")
                .desc(rb.getString("expl-item-len"))
                .build());
        itemlenGroup.addOption(Option.builder()
                .longOpt("undef-item-len")
                .desc(rb.getString("undef-item-len"))
                .build());
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
            return Integer.parseUnsignedInt(tagOrKeyword, 16);
        } catch (IllegalArgumentException e) {
            int tag = ElementDictionary.tagForKeyword(tagOrKeyword, null);
            if (tag == -1)
                throw new IllegalArgumentException(tagOrKeyword);
            return tag;
        }
    }

    public static void addFilesetInfoOptions(Options opts) {
        opts.addOption(Option.builder()
                .longOpt("fs-desc")
                .hasArg()
                .argName("txtfile")
                .desc(rb.getString("fs-desc"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("fs-desc-cs")
                .hasArg()
                .argName("code")
                .desc(rb.getString("fs-desc-cs"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("fs-id")
                .hasArg()
                .argName("id")
                .desc(rb.getString("fs-id"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("fs-uid")
                .hasArg()
                .argName("uid")
                .desc(rb.getString("fs-uid"))
                .build());
    }

    public static void configure(FilesetInfo fsInfo, CommandLine cl) {
        fsInfo.setFilesetUID(cl.getOptionValue("fs-uid"));
        fsInfo.setFilesetID(cl.getOptionValue("fs-id"));
        if (cl.hasOption("fs-desc"))
            fsInfo.setDescriptorFile(new File(cl.getOptionValue("fs-desc")));
        fsInfo.setDescriptorFileCharset(cl.getOptionValue("fs-desc-cs"));
    }

    public static void addTransferSyntaxOptions(Options opts) {
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder()
                .longOpt("explicit-vr")
                .desc(rb.getString("explicit-vr"))
                .build());
        group.addOption(Option.builder()
                .longOpt("big-endian")
                .desc(rb.getString("big-endian"))
                .build());
        group.addOption(Option.builder()
                .longOpt("implicit-vr")
                .desc(rb.getString("implicit-vr"))
                .build());
        opts.addOptionGroup(group);
    }

    private static String[] IVR_LE_FIRST = {
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRLittleEndian,
        UID.ExplicitVRBigEndian
    };

    private static String[] EVR_LE_FIRST = {
        UID.ExplicitVRLittleEndian,
        UID.ExplicitVRBigEndian,
        UID.ImplicitVRLittleEndian
    };

    private static String[] EVR_BE_FIRST = {
        UID.ExplicitVRBigEndian,
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian
    };

    private static String[] IVR_LE_ONLY = {
        UID.ImplicitVRLittleEndian
    };

    public static String[] transferSyntaxesOf(CommandLine cl) {
        if (cl.hasOption("explicit-vr"))
            return EVR_LE_FIRST;
        if (cl.hasOption("big-endian"))
            return EVR_BE_FIRST;
        if (cl.hasOption("implicit-vr"))
            return IVR_LE_ONLY;
        return IVR_LE_FIRST;
    }

    public static void addAttributes(Attributes attrs, int[] tags, String... ss) {
        Attributes item = attrs;
        for (int i = 0; i < tags.length-1; i++) {
            int tag = tags[i];
            Sequence sq = item.getSequence(tag);
            if (sq == null)
                sq = item.newSequence(tag, 1);
            if (sq.isEmpty())
                sq.add(new Attributes());
            item = sq.get(0);
        }
        int tag = tags[tags.length-1];
        VR vr = ElementDictionary.vrOf(tag,
                item.getPrivateCreator(tag));
        if (ss.length == 0 || ss.length == 1 && ss[0].isEmpty())
            if (vr == VR.SQ)
                item.newSequence(tag, 1).add(new Attributes(0));
            else
                item.setNull(tag, vr);
        else
            item.setString(tag, vr, ss);
    }

    public static void addAttributes(Attributes attrs, String[] optVals) {
        if (optVals != null)
            for (String optVal : optVals) {
                int delim = optVal.indexOf('=');
                if (delim < 0) {
                    addAttributes(attrs,
                            toTags(StringUtils.split(optVal, '.')));
                } else {
                    addAttributes(attrs,
                            toTags(StringUtils.split(optVal.substring(0, delim), '.')),
                            optVal.substring(delim + 1));
                }
            }
    }

    public static void addEmptyAttributes(Attributes attrs, String[] optVals) {
        if (optVals != null)
            for (int i = 0; i < optVals.length; i++)
                addAttributes(attrs,
                        toTags(StringUtils.split(optVals[i], '.')));
    }

    public static void addTagPaths(BasicBulkDataDescriptor desc, String[] optVals) {
        if (optVals != null)
            for (int i = 0; i < optVals.length; i++)
                desc.addTagPath(toTags(StringUtils.split(optVals[i], '.')));
    }

    public static boolean updateAttributes(Attributes data, Attributes attrs,
            String uidSuffix) {
        if (attrs.isEmpty() && uidSuffix == null)
            return false;
        if (uidSuffix != null ) {
            data.setString(Tag.StudyInstanceUID, VR.UI,
                    data.getString(Tag.StudyInstanceUID) + uidSuffix);
            data.setString(Tag.SeriesInstanceUID, VR.UI,
                    data.getString(Tag.SeriesInstanceUID) + uidSuffix);
            data.setString(Tag.SOPInstanceUID, VR.UI, 
                    data.getString(Tag.SOPInstanceUID) + uidSuffix);
        }
        data.update(Attributes.UpdatePolicy.OVERWRITE, attrs, null);
        return true;
    }

    public static String[] toUIDs(String s) {
        if (s.equals("*"))
            return new String[] { "*" };

        String[] uids = StringUtils.split(s, ',');
        for (int i = 0; i < uids.length; i++)
            uids[i] = toUID(uids[i]);
        return uids ;
    }

    public static String toUID(String uid) {
        uid = uid.trim();
        return (uid.equals("*") || Character.isDigit(uid.charAt(0)))
                ? uid
                : UID.forName(uid);
    }

}
