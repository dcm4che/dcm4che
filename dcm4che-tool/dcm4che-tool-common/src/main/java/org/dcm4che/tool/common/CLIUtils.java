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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.pdu.AAssociateRQ;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class CLIUtils {

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che.tool.common.common");

    private static final char[] SECRET = { 's', 'e', 'c', 'r', 'e', 't' };

    public static void addCommonOptions(Options opts) {
        opts.addOption("h", "help", false, rb.getString("help"));
        opts.addOption("V", "version", false, rb.getString("version"));
    }

    @SuppressWarnings("static-access")
    public static void addLocalRequestorOption(Options opts, String defAET) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("aet[@host][:port]")
                .withDescription(rb.getString("L").replace("{}", defAET))
                .create("L"));
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
        opts.addOption(null, "ssl3", false, rb.getString("ssl3"));
        opts.addOption(null, "ssl2Hello", false, rb.getString("ssl2Hello"));
        opts.addOption(null, "tls-noauth", false, rb.getString("tls-noauth"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("key"))
                .withLongOpt("key")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("password")
                .withDescription(rb.getString("key-pass"))
                .withLongOpt("key-pass")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("password")
                .withDescription(rb.getString("key-pass2"))
                .withLongOpt("key-pass2")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("cacerts"))
                .withLongOpt("cacerts")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("password")
                .withDescription(rb.getString("cacerts-pass"))
                .withLongOpt("cacerts-pass")
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

    public static void configureRemoteAcceptor(Connection conn,
            AAssociateRQ rq, String aeAtHostPort) {
        String[] aeHostPort = split(aeAtHostPort, '@', 0);
        rq.setCalledAET(aeHostPort[0]);
        if (aeHostPort[1] == null) {
            conn.setHostname("127.0.0.1");
            conn.setPort(104);
        } else {
            String[] hostPort = split(aeHostPort[1], ':', 0);
            conn.setHostname(hostPort[0]);
            conn.setPort((hostPort[1] != null 
                                    ? Integer.parseInt(hostPort[1])
                                    : 104));
        }
    }

    public static void configureLocalRequestor(Connection conn,
            ApplicationEntity ae, CommandLine cl) {
        if (cl.hasOption("L")) {
            String aeAtHostPort = cl.getOptionValue("L");
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

    public static void configureLocalAcceptor(Connection conn,
            ApplicationEntity ae, String aetAtHostPort) {
        String[] aetAndPort = split(aetAtHostPort, ':', 1);
        conn.setPort(Integer.parseInt(aetAndPort[1]));
        if (aetAndPort[0] != null) {
            String[] aetAndIP = split(aetAndPort[0], '@', 0);
            ae.setAETitle(aetAndIP[0]);
            if (aetAndIP[1] != null)
                conn.setHostname(aetAndIP[1]);
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

    public static void configure(ApplicationEntity ae, CommandLine cl) {
        if (cl.hasOption("max-pdulen-rcv"))
            ae.setMaxPDULengthReceive(Integer.parseInt(
                    cl.getOptionValue("max-pdulen-rcv")));
        if (cl.hasOption("max-pdulen-snd"))
            ae.setMaxPDULengthSend(Integer.parseInt(
                    cl.getOptionValue("max-pdulen-snd")));
        if(cl.hasOption("not-async")) {
            ae.setMaxOpsInvoked(1);
            ae.setMaxOpsPerformed(1);
        } else {
            int maxOpsInvoked = 0;
            if (cl.hasOption("max-ops-invoked"))
                maxOpsInvoked = Integer.parseInt(
                        cl.getOptionValue("max-ops-invoked"));
            ae.setMaxOpsInvoked(maxOpsInvoked);
            int maxOpsPerformed = 0;
            if (cl.hasOption("max-ops-performed"))
                maxOpsPerformed = Integer.parseInt(
                        cl.getOptionValue("max-ops-performed"));
            ae.setMaxOpsPerformed(maxOpsPerformed);
        }
   }

    public static int priorityOf(CommandLine cl) {
        return cl.hasOption("prior-high")
                ? 1
                : cl.hasOption("prior-low") 
                        ? 2
                        : 0;
    }


    public static void configureTLS(Connection conn, CommandLine cl)
            throws ParseException, GeneralSecurityException, IOException {
        if (cl.hasOption("tls"))
            conn.setTLSCipherSuite(
                    "SSL_RSA_WITH_NULL_SHA",
                    "TLS_RSA_WITH_AES_128_CBC_SHA",
                    "SSL_RSA_WITH_3DES_EDE_CBC_SHA");
        else if (cl.hasOption("tls-null"))
            conn.setTLSCipherSuite("SSL_RSA_WITH_NULL_SHA");
        else if (cl.hasOption("tls-3des"))
            conn.setTLSCipherSuite("SSL_RSA_WITH_3DES_EDE_CBC_SHA");
        else if (cl.hasOption("tls-aes"))
            conn.setTLSCipherSuite(
                    "TLS_RSA_WITH_AES_128_CBC_SHA",
                    "SSL_RSA_WITH_3DES_EDE_CBC_SHA");
        else if (cl.hasOption("tls-cipher"))
            conn.setTLSCipherSuite(cl.getOptionValues("tls-cipher"));
        else
            return;

        if (cl.hasOption("tls1"))
            conn.setTLSProtocol("TLSv1");
        else if (cl.hasOption("ssl3"))
            conn.setTLSProtocol("SSLv3");
        else if (cl.hasOption("ssl2Hello"))
            conn.setTLSProtocol("SSLv2Hello", "SSLv3", "TLSv1");
        else if (cl.hasOption("tls-protocol"))
            conn.setTLSProtocol(cl.getOptionValues("tls-protocol"));

        conn.setTLSNeedClientAuth(!cl.hasOption("tls-noauth"));

        String keyStoreURL = cl.hasOption("key")
                ? cl.getOptionValue("key")
                : "resource:key.jks";
        char[] keyStorePass = cl.hasOption("key-pass")
                ? cl.getOptionValue("key-pass").toCharArray()
                : SECRET;
        char[] keyPass = cl.hasOption("key-pass2")
                ? cl.getOptionValue("key-pass2").toCharArray()
                : keyStorePass;
        String trustStoreURL = cl.hasOption("cacerts")
                ? cl.getOptionValue("cacerts")
                : "resource:cacerts.jks";
        char[] trustStorePass = cl.hasOption("cacerts-pass")
                ? cl.getOptionValue("cacerts-pass").toCharArray()
                : SECRET;

        KeyStore keyStore = loadKeyStore(keyStoreURL, keyStorePass);
        KeyStore trustStore = loadKeyStore(trustStoreURL, trustStorePass);
        conn.getDevice().initTLS(keyStore,
                keyPass != null ? keyPass : keyStorePass,
                trustStore);
    }

    private static KeyStore loadKeyStore(String url, char[] password)
            throws GeneralSecurityException, IOException {
        KeyStore key = KeyStore.getInstance(toKeyStoreType(url));
        InputStream in = openFileOrURL(url);
        try {
            key.load(in, password);
        } finally {
            in.close();
        }
        return key;
    }

    public static InputStream openFileOrURL(String url) throws IOException {
        if (url.startsWith("resource:")) {
            return Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(url.substring(9));
        }
        try {
            return new URL(url).openStream();
        } catch (MalformedURLException e) {
            return new FileInputStream(url);
        }
    }

    private static String toKeyStoreType(String fname) {
        return fname.endsWith(".p12") || fname.endsWith(".P12")
                 ? "PKCS12" : "JKS";
    }

    public static Properties loadProperties(String url) throws IOException {
        Properties p = new Properties();
        InputStream in = openFileOrURL(url);
        try {
            p.load(in);
        } finally {
            try { in.close(); } catch (IOException ignore) {}
        }
        return p;
    }

}
