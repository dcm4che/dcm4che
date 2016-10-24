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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
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
import org.dcm4che3.util.SafeClose;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Oct 2016
 */
public class SwapPxData {

    private int updated;
    private int skipped;
    private int failed;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: swappxdata <dicom-file>|<directory>...");
            System.exit(-1);
        }
        long start = System.currentTimeMillis();
        SwapPxData inst = new SwapPxData();
        for (String path : args) {
            inst.processFileOrDirectory(new File(path));
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
        try {
            Attributes dataset;
            DicomInputStream is = null;
            try {
                is = new DicomInputStream(file);
                is.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
                dataset = is.readDataset(-1, -1);
            } finally {
                SafeClose.close(is);
            }
            VR.Holder vr = new VR.Holder();
            Object value = dataset.getValue(Tag.PixelData, vr);
            if ((value instanceof BulkData) && vr.vr == VR.OW) {
                RandomAccessFile raf = null;
                try {
                    raf = new RandomAccessFile(file, "rw");
                    toggleEndian(raf, (BulkData) value);
                } finally {
                    SafeClose.close(raf);
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

    private void toggleEndian(RandomAccessFile raf, BulkData bulkData) throws IOException {
        byte[] b = new byte[bulkData.length()];
        raf.seek(bulkData.offset());
        raf.readFully(b);
        raf.seek(bulkData.offset());
        raf.write(ByteUtils.swapShorts(b, 0, b.length));
    }

}
