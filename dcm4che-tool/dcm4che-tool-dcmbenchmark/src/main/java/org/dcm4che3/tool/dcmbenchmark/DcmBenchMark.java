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
 * Portions created by the Initial Developer are Copyright (C) 2022
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

package org.dcm4che3.tool.dcmbenchmark;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.tool.common.CLIUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Dec 2022
 */
public class DcmBenchMark {

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.dcmbenchmark.messages");

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            File file = new File(fname(cl.getArgList()));
            int samples = getOptionInt(cl, "n", 10);
            int measurements = getOptionInt(cl, "m", 10);
            boolean pixelData = cl.hasOption("p");
            boolean accumulate = cl.hasOption("a");
            boolean gc = cl.hasOption("g");
            System.out.println("N\tTime (ns)\tMemory (bytes)");
            Runtime rt = Runtime.getRuntime();
            List<Attributes> list = new LinkedList<>();
            for (int i = 1; i <= measurements; i++) {
                long start = System.nanoTime();
                for (int j = 0; j < samples; j++) {
                    try (DicomInputStream dis = new DicomInputStream(file)) {
                        list.add(pixelData ? dis.readDataset() : dis.readDatasetUntilPixelData());
                    }
                }
                long end = System.nanoTime();
                if (gc) rt.gc();
                if (!accumulate) list.clear();
                System.out.printf("%d\t%d\t%d%n", i * samples, end - start, rt.totalMemory() - rt.freeMemory());
            }
        } catch (ParseException e) {
            System.err.println("dcmbenchmark: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (IOException e) {
            System.err.println("dcmbenchmark: " + e.getMessage());
            System.exit(2);
        }
    }

    private static int getOptionInt(CommandLine cl, String opt, int def) throws ParseException {
        String v = cl.getOptionValue(opt);
        try {
            return v != null ? Integer.parseInt(v) : def;
        } catch (NumberFormatException e) {
            throw new ParseException(e.getMessage());
        }
    }

    private static String fname(List<String> argList) throws ParseException {
        int numArgs = argList.size();
        if (numArgs == 0)
            throw new ParseException(rb.getString("missing"));
        if (numArgs > 1)
            throw new ParseException(rb.getString("too-many"));
        return argList.get(0);
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        opts.addOption(Option.builder("n")
                .hasArg()
                .argName("no")
                .desc(rb.getString("samples"))
                .build());
        opts.addOption(Option.builder("m")
                .hasArg()
                .argName("no")
                .desc(rb.getString("measurements"))
                .build());
        opts.addOption(Option.builder("a")
                .desc(rb.getString("accumulate"))
                .build());
        opts.addOption(Option.builder("p")
                .desc(rb.getString("pixelData"))
                .build());
        opts.addOption(Option.builder("g")
                .desc(rb.getString("gc"))
                .build());
        return CLIUtils.parseComandLine(args, opts, rb, DcmBenchMark.class);
    }
}
