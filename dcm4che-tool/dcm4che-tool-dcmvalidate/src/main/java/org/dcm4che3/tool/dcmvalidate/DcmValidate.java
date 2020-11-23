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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4che3.tool.dcmvalidate;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IOD;
import org.dcm4che3.data.ValidationResult;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.SafeClose;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class DcmValidate {

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.dcmvalidate.messages");

    private IOD iod;

    public final void setIOD(IOD iod) {
        this.iod = iod;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            DcmValidate main = new DcmValidate();
            String iodFile = cl.getOptionValue("iod");
            if (iodFile == null)
                throw new MissingOptionException(Arrays.asList("iod"));
            main.setIOD(IOD.load(iodFile));
            List<String> fnames = cl.getArgList();
            if (fnames.isEmpty())
                throw new ParseException(rb.getString("missing"));
            
            for (String fname : fnames)
                validate(main, new File(fname));

        } catch (ParseException e) {
            System.err.println("DcmValidate: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (IOException e) {
            System.err.println("DcmValidate: " + e.getMessage());
            System.exit(2);
        }
    }

    private static void validate(DcmValidate main, File file) {
        if (file.isDirectory())
            for (File f : file.listFiles())
                validate(main, f);
        
        main.validate(file);
    }

    public void validate(File file) {
        if (iod == null)
            throw new IllegalStateException("IOD net initialized");
        DicomInputStream dis = null;
        try {
            System.out.print("Validate: " + file + " ... ");
            dis = new DicomInputStream(file);
            Attributes attrs = dis.readDataset();
            ValidationResult result = attrs.validate(iod);
            if (result.isValid())
                System.out.println("OK");
            else {
                System.out.println("FAILED:");
                System.out.println(result.asText(attrs));
            }
        } catch (IOException e) {
            System.out.println("FAILED: " + e.getMessage());
        } finally {
            SafeClose.close(dis);
        }
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        opts.addOption(Option.builder()
                .longOpt("iod")
                .hasArg()
                .argName("iod-file")
                .desc(rb.getString("iod"))
                .build());
        return CLIUtils.parseComandLine(args, opts, rb, DcmValidate.class);
    }
}
