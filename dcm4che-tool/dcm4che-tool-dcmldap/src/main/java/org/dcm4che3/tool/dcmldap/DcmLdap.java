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

import org.apache.commons.cli.*;
import org.dcm4che3.tool.common.CLIUtils;

import java.util.ResourceBundle;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jun 2017
 */
public class DcmLdap {

    private static ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.dcmldap.messages");

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            DcmLdap main = new DcmLdap();
            //TODO
        } catch (ParseException e) {
            System.err.println("dcmldap: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        }
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

}
