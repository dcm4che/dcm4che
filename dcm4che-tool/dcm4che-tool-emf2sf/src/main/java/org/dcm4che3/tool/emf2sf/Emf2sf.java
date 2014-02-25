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

package org.dcm4che3.tool.emf2sf;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.emf.MultiframeExtractor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.SafeClose;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Emf2sf {

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.emf2sf.messages");

    private MultiframeExtractor extractor = new MultiframeExtractor();
    private int[] frames;
    private DecimalFormat outFileFormat;
    private File outDir;

    public final void setOutputDirectory(File outDir) {
        outDir.mkdirs();
        this.outDir = outDir;
    }

    public final void setOutputFileFormat(String outFileFormat) {
        this.outFileFormat = new DecimalFormat(outFileFormat);
    }

    public final void setFrames(int[] frames) {
        this.frames = frames;
    }

    public void setPreserveSeriesInstanceUID(boolean PreserveSeriesInstanceUID) {
        extractor.setPreserveSeriesInstanceUID(PreserveSeriesInstanceUID);
    }

    public void setInstanceNumberFormat(String instanceNumberFormat) {
        extractor.setInstanceNumberFormat(instanceNumberFormat);
    }
    
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Emf2sf main = new Emf2sf();
            if (cl.hasOption("frame"))
                main.setFrames(toFrames(cl.getOptionValues("frame")));
            main.setPreserveSeriesInstanceUID(cl.hasOption("not-chseries"));
            main.setOutputDirectory(new File(cl.getOptionValue("out-dir", ".")));
            if (cl.hasOption("out-file"))
                main.setOutputFileFormat(cl.getOptionValue("out-file"));
            long start = System.currentTimeMillis();
            int n = main.extract(new File(fname(cl.getArgList())));
            long end = System.currentTimeMillis();
            System.out.println();
            System.out.println(
                    MessageFormat.format(rb.getString("extracted"), n,
                            (end - start) / 1000f));
       } catch (ParseException e) {
            System.err.println("emf2sf: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("emf2sf: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static int[] toFrames(String[] ss) throws ParseException {
        if (ss == null)
            return null;
        
        int[] is = new int[ss.length];
        for (int i = 0; i < is.length; i++)
            try {
                is[i] = Integer.parseInt(ss[i]) - 1;
            } catch (NumberFormatException e) {
                throw new ParseException(
                        "Invalid argument of option --frame: " + ss[i]);
            }

        return is;
    }

    private String fname(File srcFile, int frame) {
        if (outFileFormat != null)
            synchronized (outFileFormat) {
                return outFileFormat.format(frame);
            }
        return String.format(srcFile.getName() + "-%04d", frame);
    }

    public int extract(File file) throws IOException {
        Attributes src;
        DicomInputStream dis = new DicomInputStream(file);
        try {
            dis.setIncludeBulkData(IncludeBulkData.URI);
            src = dis.readDataset(-1, -1);
        } finally {
            SafeClose.close(dis);
        }
        Attributes fmi = dis.getFileMetaInformation();
        if (frames == null) {
            int n = src.getInt(Tag.NumberOfFrames, 1);
            for (int frame = 0; frame < n; ++frame)
                extract(file, fmi, src, frame);
            return n;
        } else {
            for (int frame : frames)
                extract(file, fmi, src, frame);
            return frames.length;
        }
    }

    private void extract(File file, Attributes fmi, Attributes src, int frame)
            throws IOException {
        Attributes sf = extractor.extract(src, frame);
        DicomOutputStream out = new DicomOutputStream(
                new File(outDir, fname(file, frame+1)));
        try {
            out.writeDataset(fmi != null
                    ? sf.createFileMetaInformation(
                            fmi.getString(Tag.TransferSyntaxUID))
                    : null, sf);
            System.out.print('.');
        } finally {
            SafeClose.close(out);
        }
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        opts.addOption(OptionBuilder
                .withLongOpt("frame")
                .hasArgs()
                .withArgName("no[,..]")
                .withValueSeparator(',')
                .withDescription(rb.getString("frame"))
                .create("f"));
        opts.addOption(null, "not-chseries", false, rb.getString("not-chseries"));
        opts.addOption(OptionBuilder
                .withLongOpt("inst-no")
                .hasArg()
                .withArgName("format")
                .withDescription(rb.getString("inst-no"))
                .create());
        opts.addOption(OptionBuilder
                .withLongOpt("out-dir")
                .hasArg()
                .withArgName("directory")
                .withDescription(rb.getString("out-dir"))
                .create());
        opts.addOption(OptionBuilder
                .withLongOpt("out-file")
                .hasArg()
                .withArgName("name")
                .withDescription(rb.getString("out-file"))
                .create());
        return CLIUtils.parseComandLine(args, opts, rb, Emf2sf.class);
    }

    private static String fname(List<String> argList) throws ParseException {
        int numArgs = argList.size();
        if (numArgs == 0)
            throw new ParseException(rb.getString("missing"));
        if (numArgs > 1)
            throw new ParseException(rb.getString("too-many"));
        return argList.get(0);
    }
}
