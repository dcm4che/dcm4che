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

import org.apache.commons.cli.*;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.AttributesFormat;
import org.dcm4che3.util.ByteUtils;

import java.io.*;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Oct 2016
 */
public class SwapPxData implements Closeable {

    private static ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.swappxdata.messages");

    private final boolean ifBigEndian;
    private final boolean testAll;
    private final AttributesFormat logPattern;
    private final File logFile;
    private String skipDir;
    private PrintWriter uidslog;
    private char skipChar;
    private int updated;
    private int skipped;
    private int failed;

    public SwapPxData(boolean ifBigEndian, boolean testAll, AttributesFormat logPattern, File logFile) {
        this.ifBigEndian = ifBigEndian;
        this.testAll = testAll;
        this.logPattern = logPattern;
        this.logFile = logFile;
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            final List<String> argList = cl.getArgList();
            if (argList.isEmpty())
                throw new ParseException(rb.getString("missing"));
            AttributesFormat logPattern = toAttributesFormat(cl);
            File logFile = new File(cl.getOptionValue("log-file", "uids.log"));
            if (logPattern != null && logFile.exists()) {
                throw new IOException(logFile + " already exists");
            }
            try (SwapPxData inst = new SwapPxData(
                    cl.hasOption("if-big-endian"),
                    cl.hasOption("test-all"),
                    logPattern,
                    logFile)) {
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
                if (inst.uidslog != null)
                    System.out.println("created " + logFile);
            }
        } catch (ParseException e) {
            System.err.println("swappxdata: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("swappxdata: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static AttributesFormat toAttributesFormat(CommandLine cl) {
        String pattern = cl.hasOption("uids") ? "{00080018}" : cl.getOptionValue("log");
        return pattern != null ? new AttributesFormat(pattern) : null;
    }

    private static CommandLine parseComandLine(String[] args) throws ParseException {
        Options opts = new Options();
        opts.addOption(null, "if-big-endian", false, rb.getString("if-big-endian"));
        opts.addOption(null, "test-all", false, rb.getString("test-all"));
        opts.addOption(null, "uids", false, rb.getString("uids"));
        opts.addOption(Option.builder()
                .hasArg()
                .argName("pattern")
                .desc(rb.getString("log"))
                .longOpt("log")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("path")
                .desc(rb.getString("log-file"))
                .longOpt("log-file")
                .build());
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, SwapPxData.class);
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
                dataset = is.readDataset();
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
                    if (!toggleEndian(raf, (BulkData) value, dataset.getInt(Tag.BitsStored, 16))) {
                        skipped++;
                        if (!testAll)
                            skipDir = file.getParent();
                        return skipChar = 'l';
                    }
                }
                if (logPattern != null)
                    uidslog().println(logPattern.format(dataset));
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

    private PrintWriter uidslog() throws IOException {
        if (uidslog == null)
            uidslog = new PrintWriter(logFile);
        return uidslog;
    }

    private boolean toggleEndian(RandomAccessFile raf, BulkData bulkData, int bitsStored) throws IOException {
        int mask = (1 << bitsStored) - 1;
        byte[] b = new byte[bulkData.length()];
        raf.seek(bulkData.offset());
        raf.readFully(b);
        if (ifBigEndian) {
            int prevBE = ByteUtils.bytesToShortBE(b, 0) & mask;
            int prevLE = ByteUtils.bytesToShortLE(b, 0) & mask;
            long diff = 0L;
            for (int off = 2, end = b.length - 1; off < end; off++, off++) {
                int valBE = ByteUtils.bytesToShortBE(b, off) & mask;
                diff += diff(valBE, prevBE, mask);
                prevBE = valBE;
                int valLE = ByteUtils.bytesToShortLE(b, off) & mask;
                diff -= diff(valLE, prevLE, mask);
                prevLE = valLE;
            }
            if (diff > 0)
                return false;
        }
        raf.seek(bulkData.offset());
        raf.write(ByteUtils.swapShorts(b, 0, b.length));
        return true;
    }

    private static int diff(int val, int prev, int mask) {
        int diff = Math.abs(val - prev);
        return diff != mask ? diff : 0; // suppress Black/White diffs
    }

    @Override
    public void close() throws IOException {
        if (uidslog != null)
            uidslog.close();
    }
}
