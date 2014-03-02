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

package org.dcm4che3.tool.xml2prefs;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Xml2Prefs {

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.xml2prefs.messages");

    public static void main(String[] args) {
        try {
            BufferedInputStream is = new BufferedInputStream( 
                    new FileInputStream(fname(args)));
            try {
                Preferences.importPreferences(is);
            } finally {
                is.close();
            }
        } catch (Exception e) {
            System.err.println("xml2prefs: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static String fname(String[] args) {
        if (args.length == 0) {
            System.err.println(rb.getString("missing"));
            System.err.println(rb.getString("usage"));
            System.exit(2);
        }
        String fname = args[0];
        if (fname.equals("-h") || fname.equals("--help")) {
            System.out.println(rb.getString("usage"));
            System.exit(0);
        } else if (fname.equals("-V") || fname.equals("--version")) {
            Package p = Xml2Prefs.class.getPackage();
            String s = p.getName();
            System.out.println(s.substring(s.lastIndexOf('.')+1) + ": " +
                   p.getImplementationVersion());
            System.exit(0);
        }
        if (fname.startsWith("-")) {
            System.err.println(MessageFormat.format(rb.getString("unrecognized-option"), fname));
            System.err.println(rb.getString("usage"));
            System.exit(2);
        }
        if (args.length > 1) {
            System.err.println(rb.getString("too-many"));
            System.err.println(rb.getString("usage"));
            System.exit(2);
        }
        return fname;
    }
}
