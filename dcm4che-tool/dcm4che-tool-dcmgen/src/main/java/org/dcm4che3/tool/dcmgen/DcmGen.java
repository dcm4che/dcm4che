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
package org.dcm4che3.tool.dcmgen;

import java.util.LinkedList;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.tool.common.CLIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

public class DcmGen{

    private static final Logger LOG = LoggerFactory.getLogger(DcmGen.class);
    
    private static Options options;

    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.DcmGen.messages");

    private int instanceCount;

    private int seriesCount;

    public DcmGen() {}

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        options = new Options();
        options.addOption(null, "override", true, rb.getString("override"));
        CLIUtils.addCommonOptions(options);
        return CLIUtils.parseComandLine(args,options, rb, DcmGen.class);
    }


    public static void main(String[] args) {
        CommandLine cl = null;
        try {
            DcmGen main = new DcmGen();
            cl = parseComandLine(args);
            Attributes overrideAttrs = new Attributes();
            
            if(cl.hasOption("override")) {
                CLIUtils.addAttributes(overrideAttrs, cl.getOptionValues("override"));    
            }
            
            if(cl.getArgList().size()<2) {
                throw new ParseException("Missing required arguments");
            }
            else {
                if(cl.getArgList().size()==3) {
                    
                }
                else if(cl.getArgList().size()==2) {
                    
                }
                else {
                    throw new ParseException("Too many arguments specified");
                }
            }

        } catch (ParseException e) {
            LOG.error("DcmGen\t" + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        }
    }

    private static boolean isUID(String key) {
        return key.matches("[012]((\\.0)|(\\.[1-9]\\d*))+");
    }
    
}