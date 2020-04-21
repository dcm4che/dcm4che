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

package org.dcm4che3.tool.swappxdata;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.ByteUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Oct 2016
 */
public class SwapPxData {

    private static final String[] USAGE = {
            "usage: swappxdata [--ifBigEndian [--testAll]] <dicom-file>|<directory>...",
            "",
            "The swappxdata utility swaps bytes of uncompressed pixel data with Value",
            "Representation OW.",
            "For each successfully updated file a dot (.) character is written to stdout.",
            "If an error occurs on updating a file, an E character is written to stdout",
            "and a stack trace is written to stderr.",
            "For each file kept untouched, one of the characters:",
            "p - no pixel data",
            "c - compressed pixel data",
            "b - pixel data with Value Representation OB",
            "l - little endian encoded pixel data",
            "8 - pixel data with 8 bits allocated",
            "is written to stdout.",
            "",
            "Options:",
            "--ifBigEndian    test encoding of pixel data; keep files untouched, if the",
            "                 pixel data is encoded with little endian or 8 bits allocated.",
            "                 By default, bytes of uncompressed pixel data with Value",
            "                 Representation OW will be swapped, independent of its",
            "                 encoding.",
            "--testAll        test encoding of pixel data of each file. By default, if one",
            "                 file of a directory is detected as not big endian encoded,",
            "                 all remaining files of the directory are kept also untouched",
            "                 without loading them in memory for testing." };
    private final boolean ifBigEndian;
    private final boolean testAll;
    private String skipDir;
    private char skipChar;
    private int updated;
    private int skipped;
    private int failed;

    public SwapPxData(boolean ifBigEndian, boolean testAll) {
        this.ifBigEndian = ifBigEndian;
        this.testAll = testAll;
    }

    public static void main(String[] args) {
        boolean ifBigEndian = args.length > 0 && args[0].equals("--ifBigEndian");
        boolean testAll = ifBigEndian && args.length > 1 && args[1].equals("--testAll");
        int firstPath = ifBigEndian ? testAll ? 2 : 1 : 0;
        if (args.length == firstPath || args[firstPath].startsWith("-")) {
            for (String line : USAGE) {
                System.out.println(line);
            }
            System.exit(-1);
        }
        SwapPxData inst = new SwapPxData(ifBigEndian, testAll);
        long start = System.currentTimeMillis();
        for (int i = firstPath; i < args.length; i++) {
            inst.processFileOrDirectory(new File(args[i]));
        }
        long stop = System.currentTimeMillis();
        System.out.println();
        log(inst.updated, " files updated");
        log(inst.skipped, " files skipped");
        log(inst.failed, " files failed to update");
        System.out.println("in " + (stop - start) + " ms");
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
        if (skipDir != null && skipDir.equals(file.getParent())) {
            skipped++;
            return skipChar;
        }
        try {
            Attributes dataset;
            try (DicomInputStream is = new DicomInputStream(file)) {
                is.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
                dataset = is.readDataset(-1, -1);
            }
            VR.Holder vr = new VR.Holder();
            Object value = dataset.getValue(Tag.PixelData, vr);
            if ((value instanceof BulkData) && vr.vr == VR.OW) {
                if (ifBigEndian && dataset.getInt(Tag.BitsAllocated, 8) != 16) {
                    skipped++;
                    if (!testAll)
                        skipDir = file.getParent();
                    return skipChar = '8';
                }
                try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                    if (!toggleEndian(raf, (BulkData) value)) {
                        skipped++;
                        if (!testAll)
                            skipDir = file.getParent();
                        return skipChar = 'l';
                    }
                }
                updated++;
                return '.';
            }
            skipped++;
            return value == null ? 'p' : (value instanceof BulkData) ? 'b' : 'c';
        } catch (IOException e) {
            System.err.println("Failed to update " + file + ':');
            e.printStackTrace(System.err);
            failed++;
        }
        return 'E';
    }

    private boolean toggleEndian(RandomAccessFile raf, BulkData bulkData) throws IOException {
        byte[] b = new byte[bulkData.length()];
        raf.seek(bulkData.offset());
        raf.readFully(b);
        if (ifBigEndian) {
            int prevBE = ByteUtils.bytesToShortBE(b, 0);
            int prevLE = ByteUtils.bytesToShortLE(b, 0);
            long diff = 0L;
            for (int off = 2, end = b.length - 1; off < end; off++, off++) {
                int valBE = ByteUtils.bytesToShortBE(b, off);
                diff += Math.abs(valBE - prevBE);
                prevBE = valBE;
                int valLE = ByteUtils.bytesToShortLE(b, off);
                diff -= Math.abs(valLE - prevLE);
                prevLE = valLE;
            }
            if (diff > 0)
                return false;
        }
        raf.seek(bulkData.offset());
        raf.write(ByteUtils.swapShorts(b, 0, b.length));
        return true;
    }

}
