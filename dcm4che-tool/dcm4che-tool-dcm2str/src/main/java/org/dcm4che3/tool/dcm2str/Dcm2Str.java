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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015-2019
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

package org.dcm4che3.tool.dcm2str;

import org.apache.commons.cli.*;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.AttributesFormat;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jan 2019
 */
public class Dcm2Str extends SimpleFileVisitor<Path> {
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.dcm2str.messages");
    private final AttributesFormat format;
    private final Attributes cliAttrs;

    public Dcm2Str(AttributesFormat format, Attributes cliAttrs) {
        this.format = format;
        this.cliAttrs = cliAttrs;
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            AttributesFormat format = new AttributesFormat(cl.getOptionValue("p"));
            Attributes cliAttrs = new Attributes();
            CLIUtils.addAttributes(cliAttrs, cl.getOptionValues("s"));
            List<String> pathNames = cl.getArgList();
            if (pathNames.isEmpty())
                System.out.println(format.format(cliAttrs));
            else
                for (String pathName : pathNames)
                    Files.walkFileTree(Paths.get(pathName),
                            EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                            Integer.MAX_VALUE,
                            new Dcm2Str(format, cliAttrs));
        } catch (ParseException e) {
            System.err.println("dcm2str: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("dcm2str: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static CommandLine parseComandLine(String[] args) throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        opts.addOption(Option.builder("p")
                .hasArg()
                .argName("pattern")
                .desc(rb.getString("pattern"))
                .build());
        opts.addOption(Option.builder("s")
                .hasArgs()
                .argName("[seq.]attr=value")
                .desc(rb.getString("str"))
                .build());
        CommandLine cl = CLIUtils.parseComandLine(args, opts, rb, Dcm2Str.class);
        if (!cl.hasOption("p"))
            throw new MissingOptionException(rb.getString("missing-pattern-opt"));
        return cl;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
        try (DicomInputStream dis = new DicomInputStream(path.toFile())) {
            Attributes dataset = dis.readDataset();
            dataset.addAll(cliAttrs);
            System.out.println(format.format(dataset));
        } catch (IOException e) {
            System.err.println("Failed to parse DICOM file " + path);
            e.printStackTrace();
        }
        return FileVisitResult.CONTINUE;
    }
}
