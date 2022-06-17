/*
 * **** BEGIN LICENSE BLOCK *****
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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015-2018
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
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4che3.tool.deidentify;

import org.apache.commons.cli.*;
import org.dcm4che3.data.*;
import org.dcm4che3.deident.DeIdentifier;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Deidentify {

    private static ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.deidentify.messages");

    private final DeIdentifier deidentifier;
    private DicomEncodingOptions encOpts = DicomEncodingOptions.DEFAULT;

    public Deidentify(DeIdentifier.Option... options) {
        deidentifier = new DeIdentifier(options);
    }

    public void setEncodingOptions(DicomEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args) throws ParseException{
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        CLIUtils.addEncodingOptions(opts);
        opts.addOption(Option.builder()
                .desc(rb.getString("retain-date"))
                .longOpt("retain-date")
                .build());
        opts.addOption(Option.builder()
                .desc(rb.getString("retain-org"))
                .longOpt("retain-org")
                .build());
        opts.addOption(Option.builder()
                .desc(rb.getString("retain-dev"))
                .longOpt("retain-dev")
                .build());
        opts.addOption(Option.builder()
                .desc(rb.getString("retain-uid"))
                .longOpt("retain-uid")
                .build());
        opts.addOption(Option.builder()
                .desc(rb.getString("retain-pid-hash"))
                .longOpt("retain-pid-hash")
                .build());
        opts.addOption(Option.builder("s")
                .hasArgs().argName("attr=value").valueSeparator('=')
                .desc(rb.getString("set"))
                .build());
        CommandLine cl = CLIUtils.parseComandLine(args, opts, rb, Deidentify.class);
        return cl;
    }

     public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Deidentify main = new Deidentify(options(cl));
            main.setEncodingOptions(CLIUtils.encodingOptionsOf(cl));
            main.setDummyValues(cl.getOptionValues("s"));
            final List<String> argList = cl.getArgList();
            int argc = argList.size();
            if (argc < 2)
                throw new ParseException(rb.getString("missing"));
            File dest = new File(argList.get(argc-1));
            if ((argc > 2 || new File(argList.get(0)).isDirectory())
                    && !dest.isDirectory())
                throw new ParseException(
                        MessageFormat.format(rb.getString("nodestdir"), dest));
            for (String src : argList.subList(0, argc-1))
                main.mtranscode(new File(src), dest);
        } catch (ParseException e) {
            System.err.println("deidentify: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("deidentify: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private void setDummyValues(String[] optVals) {
        if (optVals != null)
            for (int i = 1; i < optVals.length; i++, i++) {
                int tag = CLIUtils.toTag(optVals[i - 1]);
                VR vr = ElementDictionary.getStandardElementDictionary().vrOf(tag);
                deidentifier.setDummyValue(tag, vr, optVals[i]);
            }
    }

    private static DeIdentifier.Option[] options(CommandLine cl) {
        EnumSet<DeIdentifier.Option> options = EnumSet.noneOf(DeIdentifier.Option.class);
        if (cl.hasOption("retain-date"))
            options.add(DeIdentifier.Option.RetainLongitudinalTemporalInformationFullDatesOption);
        if (cl.hasOption("retain-dev"))
            options.add(DeIdentifier.Option.RetainDeviceIdentityOption);
        if (cl.hasOption("retain-org"))
            options.add(DeIdentifier.Option.RetainInstitutionIdentityOption);
        if (cl.hasOption("retain-uid"))
            options.add(DeIdentifier.Option.RetainUIDsOption);
        if (cl.hasOption("retain-pid-hash"))
            options.add(DeIdentifier.Option.RetainPatientIDHashOption);
        return options.toArray(new DeIdentifier.Option[0]);
    }

    private void mtranscode(File src, File dest) {
         if (src.isDirectory()) {
             dest.mkdir();
             for (File file : src.listFiles())
                 mtranscode(file, new File(dest, file.getName()));
             return;
         }
         if (dest.isDirectory())
             dest = new File(dest, src.getName());
         try {
             transcode(src, dest);
             System.out.println(
                     MessageFormat.format(rb.getString("deidentified"),
                             src, dest));
         } catch (Exception e) {
             System.out.println(
                     MessageFormat.format(rb.getString("failed"),
                             src, e.getMessage()));
             e.printStackTrace(System.out);
         }
     }

    public void transcode(File src, File dest) throws IOException {
        Attributes fmi;
        Attributes dataset;
        try (DicomInputStream dis = new DicomInputStream(src)) {
            dis.setIncludeBulkData(IncludeBulkData.URI);
            fmi = dis.readFileMetaInformation();
            dataset = dis.readDataset();
        }
        deidentifier.deidentify(dataset);
        if (fmi != null)
            fmi = dataset.createFileMetaInformation(fmi.getString(Tag.TransferSyntaxUID));
        try (DicomOutputStream dos = new DicomOutputStream(dest)) {
            dos.setEncodingOptions(encOpts);
            dos.writeDataset(fmi, dataset);
        }
    }

}
