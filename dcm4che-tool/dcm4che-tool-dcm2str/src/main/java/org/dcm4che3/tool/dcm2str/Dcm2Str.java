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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Jan 2019
 */
public class Dcm2Str {
    private static final Logger LOG = LoggerFactory.getLogger(Dcm2Str.class);
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.dcm2str.messages");
    private static String pattern;
    private static String[] uidOptVals;
    private static List<String> pathNames;

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            init(cl);
            dcm2str();
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
                .desc(rb.getString("pattern"))
                .build());
        opts.addOption(Option.builder("s")
                .hasArgs()
                .argName("[seq/]attr=value")
                .valueSeparator('=')
                .desc(rb.getString("str"))
                .build());
        return CLIUtils.parseComandLine(args, opts, rb, Dcm2Str.class);
    }

    private static void init(CommandLine cl) throws Exception {
        if ((pattern = cl.getOptionValue("p")) == null)
            throw new MissingOptionException("Missing Attributes Format pattern");

        uidOptVals = cl.getOptionValues("s");
        pathNames = cl.getArgList();
    }

    private static void dcm2str() throws IOException {
        for (String pathName : pathNames) {
            Path path = Paths.get(pathName);
            if (Files.isDirectory(path)) {
                try {
                    Files.walkFileTree(path, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                            new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs1) throws IOException {
                                    convert(filePath);
                                    return FileVisitResult.CONTINUE;
                                }
                            });
                } catch (IOException e) {
                    LOG.warn(e.getMessage());
                }
            } else
                convert(path);
        }
        if (pathNames.isEmpty())
            convert(null);
    }

    private static void convert(Path path) throws IOException {
        Attributes attrs = toAttributes(path);
        CLIUtils.addAttributes(attrs, uidOptVals);
        System.out.println(MessageFormat.format(
                rb.getString("converted"),
                path != null ? path : "",
                new AttributesFormat(pattern).format(attrs)));
    }

    private static Attributes toAttributes(Path path) throws IOException {
        return path != null
                ? new DicomInputStream(new FileInputStream(path.toFile())).readDataset(-1, -1)
                : new Attributes();
    }
}
