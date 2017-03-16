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

package org.dcm4che3.tool.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
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
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Priority;
import org.dcm4che3.net.SSLManagerFactory;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.UserIdentityRQ;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.StringUtils;

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
        addRequestTimeoutOption(opts);
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
                .withArgName("[user:password@]host:port")
                .withDescription(rb.getString("proxy"))
                .withLongOpt("proxy")
                .create(null));
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
        addConnectTimeoutOption(opts);
        addAcceptTimeoutOption(opts);
    }

    @SuppressWarnings("static-access")
    public static void addAEOptions(Options opts) {
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
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("ms")
                .withDescription(rb.getString("idle-timeout"))
                .withLongOpt("idle-timeout")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("ms")
                .withDescription(rb.getString("release-timeout"))
                .withLongOpt("release-timeout")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("ms")
                .withDescription(rb.getString("soclose-delay"))
                .withLongOpt("soclose-delay")
                .create(null));
        addSocketOptions(opts);
        addTLSOptions(opts);
    }

    @SuppressWarnings("static-access")
    public static void addRequestTimeoutOption(Options opts) {
        opts.addOption(OptionBuilder
            .hasArg()
            .withArgName("ms")
            .withDescription(rb.getString("request-timeout"))
            .withLongOpt("request-timeout")
            .create(null));
    }

    @SuppressWarnings("static-access")
    public static void addAcceptTimeoutOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("ms")
                .withDescription(rb.getString("accept-timeout"))
                .withLongOpt("accept-timeout")
                .create(null));
    }

    @SuppressWarnings("static-access")
    public static void addSocketOptions(Options opts) {
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
    }

    @SuppressWarnings("static-access")
    public static void addConnectTimeoutOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("ms")
                .withDescription(rb.getString("connect-timeout"))
                .withLongOpt("connect-timeout")
                .create(null));
    }

    @SuppressWarnings("static-access")
    public static void addResponseTimeoutOption(Options opts) {
        opts.addOption(OptionBuilder
            .hasArg()
            .withArgName("ms")
            .withDescription(rb.getString("response-timeout"))
            .withLongOpt("response-timeout")
            .create(null));
    }

    @SuppressWarnings("static-access")
    public static void addRetrieveTimeoutOption(Options opts) {
        opts.addOption(OptionBuilder
            .hasArg()
            .withArgName("ms")
            .withDescription(rb.getString("retrieve-timeout"))
            .withLongOpt("retrieve-timeout")
            .create(null));
    }

    @SuppressWarnings("static-access")
    public static void addTLSOptions(Options opts) {
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
        opts.addOption(null, "tls11", false, rb.getString("tls11"));
        opts.addOption(null, "tls12", false, rb.getString("tls12"));
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

        conn.setHttpProxy(cl.getOptionValue("proxy"));

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
        
        return optVal.endsWith("H")
                ? Integer.parseInt(optVal.substring(0, optVal.length() - 1), 16)
                : Integer.parseInt(optVal);
    }

    public static void configure(Connection conn, CommandLine cl)
            throws ParseException, IOException {
        conn.setReceivePDULength(
                getIntOption(cl, "max-pdulen-rcv", Connection.DEF_MAX_PDU_LENGTH));
        conn.setSendPDULength(
                getIntOption(cl, "max-pdulen-snd", Connection.DEF_MAX_PDU_LENGTH));
        if(cl.hasOption("not-async")) {
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
        conn.setResponseTimeout(getIntOption(cl, "response-timeout", 0));
        conn.setRetrieveTimeout(getIntOption(cl, "retrieve-timeout", 0));
        conn.setIdleTimeout(getIntOption(cl, "idle-timeout", 0));
        conn.setSocketCloseDelay(getIntOption(cl, "soclose-delay", 
                Connection.DEF_SOCKETDELAY));
        conn.setSendBufferSize(getIntOption(cl, "sosnd-buffer", 0));
        conn.setReceiveBufferSize(getIntOption(cl, "sorcv-buffer", 0));
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

        if (cl.hasOption("tls12"))
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

    @SuppressWarnings("static-access")
    public static void addTransferSyntaxOptions(Options opts) {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder
                .withLongOpt("explicit-vr")
                .withDescription(rb.getString("explicit-vr"))
                .create());
        group.addOption(OptionBuilder
                .withLongOpt("big-endian")
                .withDescription(rb.getString("big-endian"))
                .create());
        group.addOption(OptionBuilder
                .withLongOpt("implicit-vr")
                .withDescription(rb.getString("implicit-vr"))
                .create());
        opts.addOptionGroup(group);
    }

    private static String[] IVR_LE_FIRST = {
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRLittleEndian,
        UID.ExplicitVRBigEndianRetired
    };

    private static String[] EVR_LE_FIRST = {
        UID.ExplicitVRLittleEndian,
        UID.ExplicitVRBigEndianRetired,
        UID.ImplicitVRLittleEndian
    };

    private static String[] EVR_BE_FIRST = {
        UID.ExplicitVRBigEndianRetired,
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
        if (ss.length == 0)
            if (vr == VR.SQ)
                item.newSequence(tag, 1).add(new Attributes(0));
            else
                item.setNull(tag, vr);
        else
            item.setString(tag, vr, ss);
    }

    public static void addAttributes(Attributes attrs, String[] optVals) {
        if (optVals != null)
            for (int i = 1; i < optVals.length; i++, i++)
                addAttributes(attrs,
                        toTags(
                                StringUtils.split(optVals[i-1], '/')),
                                StringUtils.split(optVals[i], '/'));
    }

    public static void addEmptyAttributes(Attributes attrs, String[] optVals) {
        if (optVals != null)
            for (int i = 0; i < optVals.length; i++)
                addAttributes(attrs,
                        toTags(StringUtils.split(optVals[i], '/')));
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
        data.update(attrs, null);
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
