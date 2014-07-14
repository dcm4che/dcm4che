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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

package org.dcm4che3.tool.prefs2xml;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Prefs2Xml {

    private static final String CONF_ROOT_PROPERTY = 
            "org.dcm4che.conf.prefs.configurationRoot";
    private static final String DICOM_CONFIGURATION_ROOT =
            "/dicomConfigurationRoot";
    private static final String DICOM_DEVICES_ROOT = 
            "/dicomDevicesRoot/";

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.prefs2xml.messages");

    public static void main(String[] args) throws Exception {
        String pathName = null;
        boolean system = false;
        for (String arg : args) {
            if (arg.equals("-h") || arg.equals("--help")) {
                System.out.println(rb.getString("usage"));
                System.exit(0);
            }
            if (arg.equals("-V") || arg.equals("--version")) {
                Package p = Prefs2Xml.class.getPackage();
                String s = p.getName();
                System.out.println(s.substring(s.lastIndexOf('.')+1) + ": " +
                       p.getImplementationVersion());
                System.exit(0);
            }
            if (arg.equals("-s") || arg.equals("--system")) {
                system = true;
            } else if (arg.startsWith("-")) {
                System.err.println(MessageFormat.format(
                        rb.getString("unrecognized-option"), arg));
                System.err.println(rb.getString("usage"));
                System.exit(2);
            }
            if (pathName != null) {
                System.err.println(rb.getString("too-many"));
                System.err.println(rb.getString("usage"));
                System.exit(2);
            }
            pathName = arg;
        }
        if (pathName == null || pathName.charAt(0) != '/')
            pathName = System.getProperty(CONF_ROOT_PROPERTY, "")
                    + DICOM_CONFIGURATION_ROOT 
                    + (pathName == null ? "" : DICOM_DEVICES_ROOT + pathName);

        Preferences prefs = system
                ? Preferences.systemRoot()
                : Preferences.userRoot();
        if (!prefs.nodeExists(pathName)) {
            System.err.println(MessageFormat.format(
                    rb.getString("no-such-path"), pathName));
            System.exit(2);
        }
        prefs.node(pathName).exportSubtree(System.out);
    }

}
