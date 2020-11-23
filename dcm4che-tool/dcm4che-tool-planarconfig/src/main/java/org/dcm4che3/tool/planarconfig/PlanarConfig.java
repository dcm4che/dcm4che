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
import org.dcm4che3.image.YBR;
import org.dcm4che3.io.DicomInputHandler;
import org.dcm4che3.io.DicomInputStream;

import java.io.*;
import java.util.Arrays;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since May 2020
 */
public class PlanarConfig implements Closeable {
    private static final String[] USAGE = {
            "usage: planarconfig [-v] [--uids] [--3x3 MIN MAX] [--fix[0|1]]",
            "                    <file>|<directory>...",
            "",
            "The planarconfig utility detects the actual planar configuration of",
            "uncompressed pixel data of color images with Photometric Interpretation",
            "RGB or YBR_FULL and optionally correct non matching values of attribute",
            "Planar Configuration of the image.",
            "",
            "The average chroma (s. https://en.wikipedia.org/wiki/HSL_and_HSV) over all",
            "pixels and the sum of absolute differences of sample values of adjoining pixels",
            "is calculated and resulting values on assuming color-by-pixel or color-by-plane",
            "planar configuration are compared. A lower value for the calculated average",
            "chroma and for the sum of absolute differences of sample values of adjoining",
            "indicates that the assumed planar configuration is correct.",
            "",
            "If the calculated values of the average chroma and the sum of absolute",
            "differences of sample values of adjoining pixels indicate contradictory planar",
            "configurations, the average difference of sample values between 3x3 tiles",
            "assuming color-by-pixel is calculated additionally. A value lesser than the",
            "specified lower threshold (default: 10) indicates a color-by-plane, a value",
            "greater than the specified upper threshold (default: 20) a color-by-pixel",
            "planar configuration.",
            "",
            "Otherwise, if the significance of the difference in the average chroma is",
            "greater than the significance of the difference in the sum of absolute",
            "differences of sample values, the detected planar configuration is which",
            "resulted in the lesser chroma value - otherwise the detected planar",
            "configuration is which resulted in lesser differences of sample values of",
            "adjoining pixels.",
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
            "--uids         log SOP Instance UIDs of files with not matching value of",
            "               attribute Planar Configuration in file './uids.log'.",
            "--3x3 MIN MAX  thresholds for 3x3 pattern detection; 10 20 by default",
            "--fix          fix all files with NOT matching value of attribute Planar",
            "               Configuration",
            "--fix0         fix value of attribute Planar Configuration with detected",
            "               color-by-pixel planar configuration to 0",
            "--fix1         fix value of attribute Planar Configuration with detected",
            "               color-by-plane planar configuration to 1",
            "-v             displays average sample difference between 3x3 tiles, average",
            "               chroma and sample differences of adjoining pixels in format:",
            "               3x3=<avg-tile-diff>,",
            "               chroma=[<color-by-pixel>, <color-by-plane>, <significance>],",
            "               diff=[<color-by-pixel>, <color-by-plane>, <significance>]",
            "               for each processed file."
    };
    private static final char[] CORRECT_CH = { '0', '1' };
    private static final char[] WRONG_CH = { 'O', 'I' };
    private final boolean uids;
    private final boolean[] fix;
    private final boolean verbose;
    private final float min3x3;
    private final float max3x3;
    private PrintWriter uidslog;
    private int[] correct = new int[2];
    private int[] wrong = new int[2];
    private int skipped;
    private int failed;

    public PlanarConfig(boolean uids, boolean verbose, float min3x3, float max3x3, boolean... fix) {
        this.uids = uids;
        this.verbose = verbose;
        this.min3x3 = min3x3;
        this.max3x3 = max3x3;
        this.fix = fix;
    }

    public static void main(String[] args) {
        boolean uids = false;
        boolean verbose = false;
        boolean fix0 = false;
        boolean fix1 = false;
        float min3x3 = 10.f;
        float max3x3 = 20.f;
        int firstArg = 0;
        for (; args.length > firstArg; firstArg++) {
            switch (args[firstArg]) {
                case "--uids":
                    uids = true;
                    continue;
                case "-v":
                    verbose = true;
                    continue;
                case "--3x3":
                    try {
                        min3x3 = Float.parseFloat(args[++firstArg]);
                        max3x3 = Float.parseFloat(args[++firstArg]);
                        if (max3x3 < min3x3)
                            min3x3 = -1.f;
                    } catch (Exception e) {
                        min3x3 = -1.f;
                    }
                    continue;
                case "--fix":
                    fix0 = true;
                    fix1 = true;
                    continue;
                case "--fix0":
                    fix0 = true;
                    continue;
                case "--fix1":
                    fix1 = true;
                    continue;
            }
            break;
        }
        if (min3x3 < 0 || args.length == firstArg || args[firstArg].startsWith("-")) {
            for (String line : USAGE) {
                System.out.println(line);
            }
            System.exit(-1);
        }
        if (uids && (new File("uids.log").exists())) {
            System.out.println("uids.log already exists");
            System.exit(-1);
        }
        try (PlanarConfig inst = new PlanarConfig(uids, verbose, min3x3, max3x3, fix0, fix1)) {
            long start = System.currentTimeMillis();
            for (int i = firstArg; i < args.length; i++) {
                inst.processFileOrDirectory(new File(args[i]));
            }
            long stop = System.currentTimeMillis();
            System.out.println();
            System.out.println("Processed");
            log(inst.correct[0],
                    " files with color-by-pixel Planar Configuration with matching attribute value 0");
            log(inst.wrong[0],
                    " files with color-by-pixel Planar Configuration with non-matching attribute value 1 (fixed="
                            + fix0 + ')');
            log(inst.correct[1],
                    " files with color-by-plane Planar Configuration with matching attribute value 1");
            log(inst.wrong[1],
                    " files with color-by-plane Planar Configuration with non-matching attribute value 0 (fixed="
                            + fix1 + ')');
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
                dataset = is.readDataset();
            }
            VR.Holder vr = new VR.Holder();
            Object value = dataset.getValue(Tag.PixelData, vr);
            if (value == null) {
                skipped++;
                return 'p';
            }
            ColorPMI colorPMI;
            try {
                colorPMI = ColorPMI.valueOf(dataset.getString(Tag.PhotometricInterpretation));
            } catch (IllegalArgumentException e) {
                skipped++;
                return 'm';
            }
            if (!(value instanceof BulkData)) {
                skipped++;
                return 'c';
            }
            int pc;
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                pc = planarConfiguration(file, raf, (BulkData) value, dataset, colorPMI);
                if (pc == dataset.getInt(Tag.PlanarConfiguration, 0)) {
                    correct[pc]++;
                    return CORRECT_CH[pc];
                }
                if (fix[pc]) {
                    writePlanarConfiguration.writeTo(raf, pc);
                }
            }
            wrong[pc]++;
            if (uids && (fix[pc] || !fix[1-pc]))
                uidslog().println(dataset.getString(Tag.SOPInstanceUID));
            return WRONG_CH[pc];
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

    private int planarConfiguration(File file, RandomAccessFile raf, BulkData bulkData, Attributes dataset,
            ColorPMI colorPMI) throws IOException {
        byte[] b = new byte[bulkData.length()];
        raf.seek(bulkData.offset());
        raf.readFully(b);
        int rows = dataset.getInt(Tag.Rows, 1);
        int cols = dataset.getInt(Tag.Columns, 1);
        int plane = rows * cols;
        int plane2 = plane * 2;
        int[][] prevSamples = {
                { b[0] & 0xff, b[1] & 0xff, b[2] & 0xff },
                { b[0] & 0xff, b[plane] & 0xff, b[plane2] & 0xff }
        };
        long chromaPerPixel = colorPMI.chroma(prevSamples[0]);
        long chromaPerPlane = colorPMI.chroma(prevSamples[1]);
        long diffPerPixel = 0L;
        long diffPerPlane = 0L;
        for (int i = 1; i < plane; i++) {
            int i3 = i * 3;
            int[] perPixel = { b[i3] & 0xff, b[i3 + 1] & 0xff, b[i3 + 2] & 0xff };
            int[] perPlane= { b[i] & 0xff, b[i + plane] & 0xff, b[i + plane2] & 0xff };
            chromaPerPixel += colorPMI.chroma(perPixel);
            chromaPerPlane += colorPMI.chroma(perPlane);
            if (!colorPMI.isWhite(prevSamples[0]) && !colorPMI.isWhite(prevSamples[0])) {
                diffPerPixel += Math.abs(perPixel[0] - prevSamples[0][0]);
                diffPerPixel += Math.abs(perPixel[1] - prevSamples[0][1]);
                diffPerPixel += Math.abs(perPixel[2] - prevSamples[0][2]);
            }
            if (!colorPMI.isWhite(prevSamples[1]) && !colorPMI.isWhite(prevSamples[1])) {
                diffPerPlane += Math.abs(perPlane[0] - prevSamples[1][0]);
                diffPerPlane += Math.abs(perPlane[1] - prevSamples[1][1]);
                diffPerPlane += Math.abs(perPlane[2] - prevSamples[1][2]);
            }
            prevSamples[0] = perPixel;
            prevSamples[1] = perPlane;
        }
        int chromaPlanarConfig = chromaPerPixel > chromaPerPlane ? 1 : 0;
        int diffPlanarConfig = diffPerPixel > diffPerPlane ? 1 : 0;
        if (!verbose) {
            if (chromaPlanarConfig == diffPlanarConfig) return diffPlanarConfig;
        }
        long minChroma = Math.min(chromaPerPixel, chromaPerPlane);
        long maxChroma = Math.max(chromaPerPixel, chromaPerPlane);
        long minDiff = Math.min(diffPerPixel, diffPerPlane);
        long maxDiff = Math.max(diffPerPixel, diffPerPlane);
        int cols2 = cols * 2;
        int cols3 = cols * 3;
        long diff9 = 0;
        int r3 = rows / 3;
        int c3 = cols / 3;
        for (int r = 0; r < r3; r++) {
            for (int c = 0, i1 = r * cols3, i2 = i1 + plane, i3 = i2 + plane; c < c3; c++) {
                diff9 += diff3(b, i1++, cols, cols2);
                diff9 += diff3(b, i1++, cols, cols2);
                diff9 += diff3(b, i1++, cols, cols2);
                diff9 += diff3(b, i2++, cols, cols2);
                diff9 += diff3(b, i2++, cols, cols2);
                diff9 += diff3(b, i2++, cols, cols2);
                diff9 += diff3(b, i3++, cols, cols2);
                diff9 += diff3(b, i3++, cols, cols2);
                diff9 += diff3(b, i3++, cols, cols2);
            }
        }
        float diff9Float = diff9 / ((float) (c3 * r3 * 9));
        if (verbose) {
            float[] chroma = {
                    chromaPerPixel / (float) plane,
                    chromaPerPlane / (float) plane,
                    1 - minChroma / (float) maxChroma
            };
            float[] diff = {
                    diffPerPixel / (float) plane,
                    diffPerPlane / (float) plane,
                    1 - minDiff / (float) maxDiff
            };
            System.out.println();
            System.out.print(file + ": 3x3=" + diff9Float
                    + ", chroma=" + Arrays.toString(chroma)
                    + ", diff=" + Arrays.toString(diff)
                    + " -> ");
        }
        if (chromaPlanarConfig == diffPlanarConfig) return diffPlanarConfig;
        if (diff9Float < min3x3) return 1;
        if (diff9Float > max3x3) return 0;
        return (maxChroma * minDiff > minChroma * maxDiff) ? chromaPlanarConfig : diffPlanarConfig;
    }

    private int diff3(byte[] b, int i, int cols, int cols2) {
        return diff3(b[i] & 0xff, b[i + cols] & 0xff, b[i + cols2] & 0xff);
    }

    private static int diff3(int a, int b, int c) {
        return Math.max(Math.max(a, b), c) - Math.min(Math.min(a, b), c);
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

    private enum ColorPMI {
        RGB {
            @Override
            boolean isWhite(int[] samples) {
                return samples[0] == 255 && samples[1] == 255 && samples[2] == 255;
            }
        },
        YBR_FULL {
            @Override
            boolean isWhite(int[] samples) {
                return samples[0] == 255 && samples[1] == 128 && samples[2] == 128;
            }

            @Override
            int chroma(int[] samples) {
                float[] ybr = { samples[0] / 255f, samples[1] / 255f, samples[2] / 255f};
                float[] rgb = YBR.FULL.toRGB(ybr);
                return diff3(
                        (int) (rgb[0] * 255),
                        (int) (rgb[1] * 255),
                        (int) (rgb[2] * 255));
            }
        };
        abstract boolean isWhite(int[] samples);
        int chroma(int[] samples) {
            return diff3(samples[0], samples[1], samples[2]);
        };
    }
}
