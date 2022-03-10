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
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4che3.tool.agfa2dcm;

import org.apache.commons.cli.*;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.AttributesFormat;
import org.dcm4che3.util.StreamUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Jan 2021
 */
public class Agfa2Dcm {

    private static ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.agfa2dcm.messages");
    private static final byte[] ARCHIVE_BLOB = { 'A', 'R', 'C', 'H', 'I', 'V', 'E', '-', 'B', 'L', 'O', 'B' };
    private static final int ARCHIVE_BLOB_TAG_LE = 0x52414843;
    private static final int ARCHIVE_BLOB_TAG_BE = 0x41524348;

    private static boolean isArchiveBlobTag(DicomInputStream dis) {
        int tag = dis.tag();
        return tag == ARCHIVE_BLOB_TAG_LE || tag == ARCHIVE_BLOB_TAG_BE;
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            extract(Paths.get(fname(cl.getArgList())),
                    cl.getOptionValue("d", "."),
                    new AttributesFormat(cl.getOptionValue("p", "{00080018}")));
        } catch (ParseException e) {
            System.err.println("agfa2dcm: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("agfa2dcm: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        addStorageDirectoryOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, Agfa2Dcm.class);
    }

    private static void addStorageDirectoryOptions(Options opts) {
        opts.addOption(Option.builder("d")
                .hasArg()
                .argName("out-dir")
                .desc(rb.getString("out-dir"))
                .build());
        opts.addOption(Option.builder("p")
                .hasArg()
                .argName("pattern")
                .desc(rb.getString("pattern"))
                .build());
    }

    private static String fname(List<String> argList) throws ParseException {
        int numArgs = argList.size();
        if (numArgs == 0)
            throw new ParseException(rb.getString("missing"));
        if (numArgs > 1)
            throw new ParseException(rb.getString("too-many"));
        return argList.get(0);
    }

    private static void extract(Path blobPath, String outDir, AttributesFormat format) throws IOException {
        byte[] buffer = new byte[16384];
        List<Part> list = new ArrayList<>();
        try (InputStream in = new BufferedInputStream(Files.newInputStream(blobPath))) {
            StreamUtils.readFully(in, buffer, 0, 16384);
            byte[] header = new byte[12];
            StreamUtils.readFully(in, header, 0, 12);
            if (!Arrays.equals(header, ARCHIVE_BLOB))
                throw new IOException ("Missing ARCHIVE-BLOB header");

            DicomInputStream dis = null;
            Part part = null;
            for(int headerLength = 12; dis == null || isArchiveBlobTag(dis); headerLength = dis.explicitVR() ? 12 : 8) {
                if (part != null) part.length -= headerLength;
                dis = new DicomInputStream(in, 128 - headerLength);
                dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
                Attributes attrs = dis.readDataset(Agfa2Dcm::isArchiveBlobTag);
                list.add(part = new Part(format.format(attrs), dis.getPosition() + headerLength));
            }
        }
        try (InputStream in = Files.newInputStream(blobPath)) {
            StreamUtils.skipFully(in, 16384);
            for (Part part : list) {
                Path outPath = Paths.get(outDir, part.path);
                Files.createDirectories(outPath.getParent());
                try (OutputStream out = Files.newOutputStream(outPath)) {
                    StreamUtils.copy(in, out, part.length, buffer);
                }
                System.out.println(part.path);
            }
        }
    }

    private static class Part {
        String path;
        long length;

        private Part(String path, long length) {
            this.path = path;
            this.length = length;
        }
    }
}
