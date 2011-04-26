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

import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che.net.ApplicationEntity;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class CLIUtils {

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che.tool.common.common");

    public static void addCommonOptions(Options opts) {
        opts.addOption("h", "help", false, rb.getString("help"));
        opts.addOption("V", "version", false, rb.getString("version"));
    }

    @SuppressWarnings("static-access")
    public static void addAEOptions(Options opts) {
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


    public static void configure(ApplicationEntity ae, CommandLine cl) {
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
}
