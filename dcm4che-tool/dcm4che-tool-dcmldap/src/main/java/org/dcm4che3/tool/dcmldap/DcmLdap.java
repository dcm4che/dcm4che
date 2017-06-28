/*
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  J4Care.
 *  Portions created by the Initial Developer are Copyright (C) 2015-2017
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 */

package org.dcm4che3.tool.dcmldap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.ldap.LdapDicomConfiguration;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.tool.common.CLIUtils;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.ResourceBundle;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Jun 2017
 */
public class DcmLdap {

    private static ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.dcmldap.messages");

    private static LdapDicomConfiguration config;

    private static final String LDAP_URI = "ldap://localhost:389/dc=dcm4che,dc=org";
    private static final String BIND_DN = "cn=admin,dc=dcm4che,dc=org";
    private static final String PASSWORD = "secret";

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        try {
            CommandLine cl = parseComandLine(args);
            setupLDAP(cl);
            if (cl.hasOption("a"))
                addNetworkAE(cl);
            if (cl.hasOption("c"))
                createNetworkAE(cl);
            if (cl.hasOption("d"))
                removeNetworkAE(cl);
        } catch (ParseException e) {
            System.err.println("dcmldap: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        }
    }

    private static void setupLDAP(CommandLine cl) throws ConfigurationException {
        Hashtable<String, Object> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("java.naming.ldap.attributes.binary", "dicomVendorData");
        env.put(Context.PROVIDER_URL, cl.hasOption("H") ? cl.getOptionValue("H") : LDAP_URI);
        env.put(Context.SECURITY_PRINCIPAL, cl.hasOption("D") ? cl.getOptionValue("D") : BIND_DN);
        env.put(Context.SECURITY_CREDENTIALS, cl.hasOption("w") ? cl.getOptionValue("w") : PASSWORD);
        config = new LdapDicomConfiguration(env);
    }

    private static void addNetworkAE(CommandLine cl) throws ConfigurationException, ParseException {
        Device device = config.findDevice(cl.getOptionValue("dev"));
        createAE(cl, device);
        config.merge(device, options(true));
    }

    private static void createNetworkAE(CommandLine cl) throws ConfigurationException, ParseException {
        Device device = new Device();
        createAE(cl, device);
        config.persist(device, options(true));
    }

    private static void removeNetworkAE(CommandLine cl) throws ConfigurationException, NamingException {
        String aet = cl.getOptionValue("d");
        ApplicationEntity ae = config.findApplicationEntity(aet);
        Device dev = ae.getDevice();
        dev.removeApplicationEntity(ae);
        dev.removeConnection(ae.getConnections().get(0));
        config.merge(dev, options(true));
        if (dev.getApplicationAETitles().isEmpty())
            config.removeDevice(dev.getDeviceName(), null);
    }

    private static void createAE(CommandLine cl, Device device) throws ParseException {
        String deviceName = device.getDeviceName();
        String optVal = deviceName == null ? cl.getOptionValue("c") : cl.getOptionValue("a");

        String[] aeHostPort = split(optVal , '@');
        String[] hostPort = split(aeHostPort[1], ':');

        if (deviceName == null) {
            String name = cl.hasOption("dev") ? cl.getOptionValue("dev") : aeHostPort[0].toLowerCase();
            device.setDeviceName(name);
            if (cl.hasOption("dev-desc"))
                device.setDescription(cl.getOptionValue("dev-desc"));
            if (cl.hasOption("dev-type"))
                device.setPrimaryDeviceTypes(cl.getOptionValue("dev-type"));
        }

        Connection conn = createConn(hostPort, cl);
        device.addConnection(conn);

        ApplicationEntity ae = new ApplicationEntity(aeHostPort[0]);
        ae.addConnection(conn);
        if (cl.hasOption("ae-desc"))
            ae.setDescription(cl.getOptionValue("ae-desc"));
        device.addApplicationEntity(ae);
    }

    private static Connection createConn(String[] hostPort, CommandLine cl) {
        Connection conn = new Connection();
        conn.setHostname(hostPort[0]);
        conn.setPort(Integer.valueOf(hostPort[1]));
        configureTLS(cl, conn);
        String cn = cl.hasOption("conn")
                        ? cl.getOptionValue("conn")
                        : conn.getTlsCipherSuites().length > 1
                            ? "dicom-tls"
                            : "dicom";
        conn.setCommonName(cn);
        return conn;
    }

    private static void configureTLS(CommandLine cl, Connection conn) {
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
    }

    private static String[] split(String s, char delim) throws ParseException {
        int defPos = 0;
        String[] s2 = new String[2];
        int pos = s.indexOf(delim);
        if (pos != -1) {
            s2[0] = s.substring(0, pos);
            s2[1] = s.substring(pos + 1);
        } else {
            s2[defPos] = s;
        }
        if (s2[1] == null)
            throw new ParseException("Read how to specify <aet@host:port> in dcmldap help.");
        return s2;
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args) throws ParseException{
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        OptionGroup cmdGroup = new OptionGroup();
        cmdGroup.addOption(OptionBuilder
                .hasArg()
                .withArgName("aet@host:port")
                .withDescription(rb.getString("create"))
                .create("c"));
        cmdGroup.addOption(OptionBuilder
                .hasArg()
                .withArgName("aet@host:port")
                .withDescription(rb.getString("add"))
                .create("a"));
        cmdGroup.addOption(OptionBuilder
                .hasArg()
                .withArgName("aet")
                .withDescription(rb.getString("delete"))
                .create("d"));
        opts.addOptionGroup(cmdGroup);
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("ldapuri")
                .withDescription(rb.getString("ldapuri"))
                .create("H"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("binddn")
                .withDescription(rb.getString("binddn"))
                .create("D"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("passwd")
                .withDescription(rb.getString("passwd"))
                .create("w"));
        opts.addOption(OptionBuilder
                .withLongOpt("dev")
                .hasArg()
                .withArgName("name")
                .withDescription(rb.getString("dev"))
                .create(null));
        opts.addOption(OptionBuilder
                .withLongOpt("dev-desc")
                .hasArg()
                .withArgName("string")
                .withDescription(rb.getString("dev-desc"))
                .create(null));
        opts.addOption(OptionBuilder
                .withLongOpt("dev-type")
                .hasArg()
                .withArgName("string")
                .withDescription(rb.getString("dev-type"))
                .create(null));
        opts.addOption(OptionBuilder
                .withLongOpt("ae-desc")
                .hasArg()
                .withArgName("string")
                .withDescription(rb.getString("ae-desc"))
                .create(null));
        opts.addOption(OptionBuilder
                .withLongOpt("conn")
                .hasArg()
                .withArgName("cn")
                .withDescription(rb.getString("conn-cn"))
                .create(null));
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
        opts.addOption(null, "tls", false, rb.getString("tls"));
        CommandLine cl = CLIUtils.parseComandLine(args, opts, rb, DcmLdap.class);
        String selected = cmdGroup.getSelected();
        if (selected == null)
            throw new ParseException(rb.getString("missing"));
        if (selected.equals("a") && !cl.hasOption("dev"))
            throw new ParseException(rb.getString("missing-dev"));
        return cl;
    }

    private static EnumSet<DicomConfiguration.Option> options(boolean register) {
        EnumSet<DicomConfiguration.Option> options = EnumSet.of(
                DicomConfiguration.Option.PRESERVE_VENDOR_DATA,
                DicomConfiguration.Option.PRESERVE_CERTIFICATE);
        if (register)
            options.add(DicomConfiguration.Option.REGISTER);
        return options;
    }
}
