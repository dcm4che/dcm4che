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
 *  Java(TM), hosted at https://github.com/dcm4che.
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

import org.apache.commons.cli.*;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.ldap.LdapDicomConfiguration;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.StringUtils;

import javax.naming.Context;
import java.io.Closeable;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.ResourceBundle;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Jun 2017
 */
public class DcmLdap implements Closeable {

    private static ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.dcmldap.messages");

    private static final String DEFAULT_LDAP_URI = "ldap://localhost:389/dc=dcm4che,dc=org";
    private static final String DEFAULT_BIND_DN = "cn=admin,dc=dcm4che,dc=org";
    private static final String DEFAULT_PASSWORD = "secret";

    private final LdapDicomConfiguration conf;
    private String deviceName;
    private String deviceDesc;
    private String deviceType;
    private String aeTitle;
    private String aeDesc;
    private Connection conn;

    public DcmLdap(Hashtable<?, ?> env) throws ConfigurationException {
        conf = new LdapDicomConfiguration(env);
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setDeviceDescription(String deviceDesc) {
        this.deviceDesc = deviceDesc;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setAEDescription(String aeDesc) {
        this.aeDesc = aeDesc;
    }

    public void setAETitle(String aeTitle) {
        this.aeTitle = aeTitle;
    }

    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    public void close() {
        conf.close();
    }

    public void createNetworkAE() throws ConfigurationException {
        Device device = new Device(deviceName != null ? deviceName : aeTitle.toLowerCase());
        device.setDescription(deviceDesc);
        if (deviceType != null) {
            device.setPrimaryDeviceTypes(deviceType);
        }
        device.addConnection(conn);
        ApplicationEntity ae = new ApplicationEntity(aeTitle);
        device.addApplicationEntity(ae);
        ae.setDescription(aeDesc);
        ae.addConnection(conn);
        conf.persist(device, EnumSet.of(DicomConfiguration.Option.REGISTER));
    }

    public void addNetworkAE() throws ConfigurationException {
        Device device = conf.findDevice(deviceName);
        device.addConnection(conn);
        ApplicationEntity ae = new ApplicationEntity(aeTitle);
        device.addApplicationEntity(ae);
        ae.setDescription(aeDesc);
        ae.addConnection(conn);
        conf.merge(device, EnumSet.of(DicomConfiguration.Option.REGISTER));
    }

    public void removeNetworkAE() throws ConfigurationException {
        ApplicationEntity ae = conf.findApplicationEntity(aeTitle);
        Device device = ae.getDevice();
        device.removeApplicationEntity(aeTitle);
        for (Connection conn : ae.getConnections()) {
            device.removeConnection(conn);
        }
        if (device.getApplicationAETitles().isEmpty())
            conf.removeDevice(device.getDeviceName(), EnumSet.of(DicomConfiguration.Option.REGISTER));
        else
            conf.merge(device, EnumSet.of(DicomConfiguration.Option.REGISTER));
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        try {
            OptionGroup cmdGroup = new OptionGroup();
            CommandLine cl = parseComandLine(args, cmdGroup);
            Operation op = Operation.valueOf(cmdGroup.getSelected());
            try (DcmLdap main = new DcmLdap(ldapEnv(cl))) {
                op.configure(main, cl);
                op.perform(main);
            }
        } catch (ParseException e) {
            System.err.println("dcmldap: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        }
    }

    private static Hashtable<?, ?> ldapEnv(CommandLine cl) {
        Hashtable<String, Object> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("java.naming.ldap.attributes.binary", "dicomVendorData");
        env.put(Context.PROVIDER_URL, cl.getOptionValue("H", DEFAULT_LDAP_URI));
        env.put(Context.SECURITY_PRINCIPAL, cl.getOptionValue("D", DEFAULT_BIND_DN));
        env.put(Context.SECURITY_CREDENTIALS, cl.getOptionValue("w", DEFAULT_PASSWORD));
        return env;
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args, OptionGroup cmdGroup) throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        cmdGroup.addOption(Option.builder("c")
                .hasArg()
                .argName("aet@host:port")
                .desc(rb.getString("create"))
                .build());
        cmdGroup.addOption(Option.builder("a")
                .hasArg()
                .argName("aet@host:port")
                .desc(rb.getString("add"))
                .build());
        cmdGroup.addOption(Option.builder("d")
                .hasArg()
                .argName("aet")
                .desc(rb.getString("delete"))
                .build());
        opts.addOptionGroup(cmdGroup);
        opts.addOption(Option.builder("H")
                .hasArg()
                .argName("ldapuri")
                .desc(rb.getString("ldapuri"))
                .build());
        opts.addOption(Option.builder("D")
                .hasArg()
                .argName("binddn")
                .desc(rb.getString("binddn"))
                .build());
        opts.addOption(Option.builder("w")
                .hasArg()
                .argName("passwd")
                .desc(rb.getString("passwd"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("dev")
                .hasArg()
                .argName("name")
                .desc(rb.getString("dev"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("dev-desc")
                .hasArg()
                .argName("string")
                .desc(rb.getString("dev-desc"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("dev-type")
                .hasArg()
                .argName("string")
                .desc(rb.getString("dev-type"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("ae-desc")
                .hasArg()
                .argName("string")
                .desc(rb.getString("ae-desc"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("conn")
                .hasArg()
                .argName("cn")
                .desc(rb.getString("conn-cn"))
                .build());
        CLIUtils.addTLSCipherOptions(opts);
        CommandLine cl = CLIUtils.parseComandLine(args, opts, rb, DcmLdap.class);
        String selected = cmdGroup.getSelected();
        if (selected == null)
            throw new ParseException(rb.getString("missing"));
        if (selected.equals("a") && !cl.hasOption("dev"))
            throw new ParseException(rb.getString("missing-dev"));
        return cl;
    }

    private enum Operation {
        c {
            @Override
            void perform(DcmLdap main) throws ConfigurationException {
                main.createNetworkAE();
            }
        },
        a {
            @Override
            void perform(DcmLdap main) throws ConfigurationException {
                main.addNetworkAE();
            }
        },
        d {
            @Override
            public void configure(DcmLdap main, CommandLine cl) {
                main.setAETitle(cl.getOptionValue("d"));
            }

            @Override
            void perform(DcmLdap main) throws ConfigurationException {
                main.removeNetworkAE();
            }
        };

        void configure(DcmLdap main, CommandLine cl) throws ParseException {
            String aeAtHostPort = cl.getOptionValue(name());
            String[] aeHostPort = StringUtils.split(aeAtHostPort , '@');
            if (aeHostPort.length < 2)
                throw invalidConn();

            String[] hostPort = StringUtils.split(aeHostPort[1], ':');
            if (hostPort.length < 2)
                throw invalidConn();

            Connection conn;
            try {
                conn = new Connection(cl.getOptionValue("conn"), hostPort[0], Integer.parseInt(hostPort[1]));
            } catch (NumberFormatException e) {
                throw invalidConn();
            }
            CLIUtils.configureTLSCipher(conn, cl);
            if (conn.getCommonName() == null)
                conn.setCommonName(conn.isTls() ? "dicom-tls" : "dicom");

            main.setDeviceName(cl.getOptionValue("dev"));
            main.setDeviceDescription(cl.getOptionValue("dev-desc"));
            main.setDeviceType(cl.getOptionValue("dev-type"));
            main.setAETitle(aeHostPort[0]);
            main.setAEDescription(cl.getOptionValue("ae-desc"));
            main.setConnection(conn);
        }

        ParseException invalidConn() {
            return new ParseException(MessageFormat.format(rb.getString("invalid-conn"), name()));
        }

        abstract void perform(DcmLdap main) throws ConfigurationException;
    }
}
