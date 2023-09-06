/*
 * ** BEGIN LICENSE BLOCK *****
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
 * Portions created by the Initial Developer are Copyright (C) 2016
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
 * ** END LICENSE BLOCK *****
 */

package org.dcm4che3.tool.maskpxdata;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.*;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.ByteUtils;

import java.io.*;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jun 2022
 */
public class MaskPxData {

    private static ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.maskpxdata.messages");

    private final int pxval;
    private final int[] regions;
    private int updated;
    private int skipped;
    private int failed;
    public MaskPxData(int pxval, int[] regions) {
        this.pxval = pxval;
        this.regions = regions;
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            final List<String> argList = cl.getArgList();
            if (argList.isEmpty())
                throw new ParseException(rb.getString("missing"));
            MaskPxData inst = new MaskPxData(toPxVal(cl.getOptionValue("pxval")), toRegions(cl.getOptionValues("r")));
            long start = System.currentTimeMillis();
            for (String arg : argList) {
                inst.processFileOrDirectory(new File(arg));
            }
            long stop = System.currentTimeMillis();
            System.out.println();
            log(inst.updated, " files updated");
            log(inst.skipped, " files skipped");
            log(inst.failed, " files failed to update");
            System.out.println("in " + (stop - start) + " ms");
        } catch (ParseException e) {
            System.err.println("maskpxdata: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("maskpxdata: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static int[] toRegions(String[] ss) throws ParseException {
        if (ss == null)
            throw new ParseException("Missing option: r");

        if (ss.length % 4 != 0)
            throw new ParseException("Missing argument for option: r");

        int[] regions = new int[ss.length];
        try {
            for (int i = 0; i < regions.length; i++) {
                regions[i] = Integer.parseUnsignedInt(ss[i]);
            }
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid argument for option: r");
        }
        return regions;
    }

    private static int toPxVal(String s) throws ParseException {
        try {
            return s != null
                    ? s.charAt(0) == '#'
                    ? Integer.parseInt(s.substring(1), 16)
                    : Integer.parseInt(s)
                    : 0;
        } catch (NumberFormatException e) {
            throw new ParseException("Missing argument for option: pxval");
        }
    }

    private static CommandLine parseComandLine(String[] args) throws ParseException {
        Options opts = new Options();
        opts.addOption(Option.builder("r")
                .longOpt("region")
                .hasArgs()
                .valueSeparator(',')
                .type(Number.class)
                .argName("x>,<y>,<w>,<h")
                .desc(rb.getString("region"))
                .build());
        opts.addOption(null, "pxval", true, rb.getString("pxval"));
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, MaskPxData.class);
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
            boolean bigEndian;
            Attributes attrs;
            try (DicomInputStream is = new DicomInputStream(file)) {
                is.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
                Attributes fmi = is.readFileMetaInformation();
                bigEndian = fmi != null && UID.ExplicitVRBigEndian.equals(fmi.getString(Tag.TransferSyntaxUID));
                attrs = is.readDataset();
            }
            VR.Holder vr = new VR.Holder();
            Object value = attrs.getValue(Tag.PixelData, vr);
            if ((value instanceof BulkData)) {
                try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                    maskPxData(raf, bigEndian, attrs, (BulkData) value);
                }
                updated++;
                return '.';
            }
            skipped++;
            return value == null ? 'p' : 'c';
        } catch (IOException e) {
            System.err.println("Failed to update " + file + ':');
            e.printStackTrace(System.err);
            failed++;
        }
        return 'E';
    }

    private void maskPxData(RandomAccessFile raf, boolean bigEndian, Attributes attrs, BulkData bulkData)
            throws IOException {
        int rows = attrs.getInt(Tag.Rows, 0);
        int columns = attrs.getInt(Tag.Columns, 0);
        int frames = attrs.getInt(Tag.NumberOfFrames, 1);
        int samples = attrs.getInt(Tag.SamplesPerPixel, 1);
        int planeSize = rows * columns;
        int bytesAllocated = attrs.getInt(Tag.BitsAllocated, 16) / 8;
        int planarConfig = attrs.getInt(Tag.PlanarConfiguration, 0);
        Mask mask = samples == 3
                ? planarConfig == 0 ? this::maskColorByPixel : this::maskColorByPlane
                : bytesAllocated == 1 ? this::maskByte : this::maskWord;
        int frameSize = planeSize * samples * bytesAllocated;
        byte[] b = new byte[frameSize];
        for (int f = 0; f < frames; f++) {
            long pos = bulkData.offset() + f * frameSize;
            raf.seek(pos);
            raf.readFully(b);
            for (int i = 0; i < regions.length; i += 4)
                for (int j = 0; j < regions[i + 3]; j++)
                    for (int k = 0, off = (j + regions[i + 1]) * columns + regions[i]; k < regions[i + 2]; k++)
                        mask.apply(b, off + k, bigEndian, planeSize);
            raf.seek(pos);
            raf.write(b);
        }
    }

    interface Mask {
        void apply(byte[] b, int off, boolean bigEndian, int planeSize);
    }

    private void maskByte(byte[] b, int off, boolean bigEndian, int planeSize) {
        b[off] = (byte) pxval;
    }

    private void maskWord(byte[] b, int off, boolean bigEndian, int planeSize) {
        ByteUtils.shortToBytes(pxval, b, off * 2, bigEndian);
    }

    private void maskColor(byte[] b, int i, int d) {
        b[i] = (byte) (pxval >> 16);
        b[i+=d] = (byte) (pxval >> 8);
        b[i+d] = (byte) pxval;
    }

    private void maskColorByPixel(byte[] b, int off, boolean bigEndian, int planeSize) {
        maskColor(b, off*3, 1);
    }

    private void maskColorByPlane(byte[] b, int off, boolean bigEndian, int planeSize) {
        maskColor(b, off, planeSize);
    }
}
