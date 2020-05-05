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

package org.dcm4che3.tool.planarconfig;

import org.dcm4che3.data.*;
import org.dcm4che3.io.DicomInputHandler;
import org.dcm4che3.io.DicomInputStream;

import java.io.*;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since May 2020
 */
public class PlanarConfig implements Closeable {
    private static final String[] USAGE = {
            "usage: planarconfig [--uids] [--fix] <file>|<directory>...",
            "",
            "The planarconfig utility detects the actual planar configuration of",
            "uncompressed pixel data of color images with Photometric Interpretation",
            "RGB or YBR_FULL and optionally correct non matching values of attribute",
            "Planar Configuration of the image.",
            "",
            "For each processed file one of the characters:",
            "p - no pixel data",
            "c - compressed pixel data",
            "m - monochrome (or palette color) pixel data",
            "0 - detected color-by-pixel planar configuration, matching with value 0",
            "    of attribute Planar Configuration",
            "O - detected color-by-pixel planar configuration, NOT matching with value 1",
            "    of attribute Planar Configuration",
            "1 - detected color-by-plane planar configuration, matching with value 1",
            "    of attribute Planar Configuration",
            "I - detected color-by-plane planar configuration, NOT matching with value 0",
            "    of attribute Planar Configuration",
            "is written to stdout.",
            "If an error occurs on processing a file, an E character is written to stdout",
            "and a stack trace is written to stderr.",
            "",
            "Options:",
            "--uids   log SOP Instance UIDs of files with not matching value of attribute",
            "         Planar Configuration in file 'uids.log' in working directory.",
            "--fix    fix files with NOT matching value of attribute Planar Configuration"
    };
    private static final char[] CORRECT_CH = { '0', '1' };
    private static final char[] WRONG_CH = { 'O', 'I' };
    private final boolean uids;
    private final boolean fix;
    private PrintWriter uidslog;
    private int[] correct = new int[2];
    private int[] wrong = new int[2];
    private int skipped;
    private int failed;

    public PlanarConfig(boolean uids, boolean fix) {
        this.uids = uids;
        this.fix = fix;
    }

    public static void main(String[] args) {
        boolean uids, fix;
        int firstArg = 0;
        if (uids = args.length > 0 && args[0].equals("--uids")) firstArg++;
        if (fix = args.length > firstArg && args[firstArg].equals("--fix")) firstArg++;
        if (args.length == firstArg || args[firstArg].startsWith("-")) {
            for (String line : USAGE) {
                System.out.println(line);
            }
            System.exit(-1);
        }
        if (uids && (new File("uids.log").exists())) {
            System.out.println("uids.log already exists");
            System.exit(-1);
        }
        try (PlanarConfig inst = new PlanarConfig(uids, fix)) {
            long start = System.currentTimeMillis();
            for (int i = firstArg; i < args.length; i++) {
                inst.processFileOrDirectory(new File(args[i]));
            }
            long stop = System.currentTimeMillis();
            System.out.println();
            System.out.println("Processed");
            log(inst.correct[0], " files with color-by-pixel Planar Configuration with matching attribute value 0");
            log(inst.wrong[0], " files with color-by-pixel Planar Configuration with non-matching attribute value 1");
            log(inst.correct[1], " files with color-by-plane Planar Configuration with matching attribute value 1");
            log(inst.wrong[1], " files with color-by-plane Planar Configuration with non-matching attribute value 0");
            log(inst.skipped, " files skipped");
            log(inst.failed, " files failed to process");
            System.out.println("in " + (stop - start) + " ms");
            if (inst.uidslog != null)
                System.out.println("created uids.log");
        } catch (IOException e) {
            System.err.println("Failed to close uids.log:\n");
            e.printStackTrace(System.err);
        }
    }

    private static void log(int n, String suffix) {
        if (n > 0)
            System.out.println(n + suffix);
    }

    private void processFileOrDirectory(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            for (File fileOrSubDir : fileOrDir.listFiles()) {
                processFileOrDirectory(fileOrSubDir);
            }
        } else
            System.out.print(processFile(fileOrDir));
    }

    private char processFile(File file) {
        try {
            Attributes dataset;
            WritePlanarConfiguration writePlanarConfiguration = new WritePlanarConfiguration();
            try (DicomInputStream is = new DicomInputStream(file)) {
                is.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
                is.setDicomInputHandler(writePlanarConfiguration);
                dataset = is.readDataset(-1, -1);
            }
            VR.Holder vr = new VR.Holder();
            Object value = dataset.getValue(Tag.PixelData, vr);
            if ((value instanceof BulkData) && dataset.getInt(Tag.SamplesPerPixel, 1) == 3) {
                int pc;
                try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                    pc = planarConfiguration(raf, (BulkData) value, dataset);
                    if (pc == dataset.getInt(Tag.PlanarConfiguration, 0)) {
                        correct[pc]++;
                        return CORRECT_CH[pc];
                    }
                    if (fix) {
                        writePlanarConfiguration.writeTo(raf, pc);
                    }
                }
                wrong[pc]++;
                if (uids)
                    uidslog().println(dataset.getString(Tag.SOPInstanceUID));
                return WRONG_CH[pc];
            }
            skipped++;
            return value == null ? 'p' : (value instanceof BulkData) ? 'm' : 'c';
        } catch (IOException e) {
            System.err.println("Failed to update " + file + ':');
            e.printStackTrace(System.err);
            failed++;
        }
        return 'E';
    }

    private PrintWriter uidslog() throws IOException {
        if (uidslog == null)
            uidslog = new PrintWriter("uids.log");
        return uidslog;
    }

    private int planarConfiguration(RandomAccessFile raf, BulkData bulkData, Attributes dataset) throws IOException {
        byte[] b = new byte[bulkData.length()];
        raf.seek(bulkData.offset());
        raf.readFully(b);
        long diff = 0L;
        int rows = dataset.getInt(Tag.Rows, 1);
        int cols = dataset.getInt(Tag.Columns, 1);
        int plane = rows * cols;
        int plane2 = plane * 2;
        int[][] prevSamples = {
                { b[0] & 0xff, b[1] & 0xff, b[2] & 0xff },
                { b[0] & 0xff, b[plane] & 0xff, b[plane2] & 0xff }
        };
        for (int i = 1; i < plane; i++) {
            int i3 = i * 3;
            int[] perPixel = { b[i3] & 0xff, b[i3 + 1] & 0xff, b[i3 + 2] & 0xff };
            int[] perPlane= { b[i] & 0xff, b[i + plane] & 0xff, b[i + plane2] & 0xff };
            diff += Math.abs(perPixel[0] - prevSamples[0][0]);
            diff += Math.abs(perPixel[1] - prevSamples[0][1]);
            diff += Math.abs(perPixel[2] - prevSamples[0][2]);
            diff -= Math.abs(perPlane[0] - prevSamples[1][0]);
            diff -= Math.abs(perPlane[1] - prevSamples[1][1]);
            diff -= Math.abs(perPlane[2] - prevSamples[1][2]);
            prevSamples[0] = perPixel;
            prevSamples[1] = perPlane;
        }
        return diff > 0 ? 1 : 0;
    }

    @Override
    public void close() throws IOException {
        if (uidslog != null)
            uidslog.close();
    }

    private static class WritePlanarConfiguration implements DicomInputHandler {
        Long pcPos;
        int shift;
        @Override
        public void readValue(DicomInputStream dis, Attributes attrs) throws IOException {
            if (dis.tag() == Tag.PlanarConfiguration) {
                pcPos = dis.getPosition();
                shift = dis.bigEndian() ? 0 : 8;
            }
            dis.readValue(dis, attrs);
        }

        @Override
        public void readValue(DicomInputStream dis, Sequence seq) throws IOException {
            dis.readValue(dis, seq);
        }

        @Override
        public void readValue(DicomInputStream dis, Fragments frags) throws IOException {
            dis.readValue(dis, frags);
        }

        @Override
        public void startDataset(DicomInputStream dis) throws IOException {
            dis.startDataset(dis);
        }

        @Override
        public void endDataset(DicomInputStream dis) throws IOException {
            dis.endDataset(dis);
        }

        public void writeTo(RandomAccessFile raf, int pc) throws IOException {
            raf.seek(pcPos);
            raf.writeShort(pc << shift);
        }
    }
}
