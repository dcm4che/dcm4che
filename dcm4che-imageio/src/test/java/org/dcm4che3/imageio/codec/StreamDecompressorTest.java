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
 * The Initial Developer of the Original Code is Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011-2015
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
 */

package org.dcm4che3.imageio.codec;

import org.dcm4che.test.data.TestData;
import org.dcm4che3.data.*;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.SafeClose;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class StreamDecompressorTest {

    private static final TestData US_MF_RLE =new TestData("US-PAL-8-10x-echo");

    @Test
    public void testDecompress() throws Exception {
        File srcFile = US_MF_RLE.toFile();
        File outFile = new File("target", srcFile.getName());
        DicomInputStream dis = null;
        DicomOutputStream dos = null;
        StreamDecompressor decompressor = null;
        try {
            dis = new DicomInputStream(srcFile);
            dos = new DicomOutputStream(outFile);
            Attributes fmi = dis.readFileMetaInformation();
            String tsuid = fmi.getString(Tag.TransferSyntaxUID);
            fmi.setString(Tag.TransferSyntaxUID, VR.UI, UID.ExplicitVRLittleEndian);
            dos.writeFileMetaInformation(fmi);
            decompressor = new StreamDecompressor(dis, tsuid, dos);
            decompressor.decompress();
        } finally {
            SafeClose.close(dis);
            SafeClose.close(dos);
            if (decompressor != null)
                decompressor.dispose();
        }
        Attributes ds;
        try {
            dis = new DicomInputStream(outFile);
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            ds = dis.readDataset(-1, -1);
        } finally {
            SafeClose.close(dis);
        }
        int rows = ds.getInt(Tag.Rows, 0);
        int cols = ds.getInt(Tag.Columns, 0);
        int samples = ds.getInt(Tag.SamplesPerPixel, 0);
        int allocated = ds.getInt(Tag.BitsAllocated, 0);
        int frames = ds.getInt(Tag.NumberOfFrames, 0);
        int length = rows * cols * samples * (allocated >>> 3) * frames;
        BulkData pixelData = (BulkData) ds.getValue(Tag.PixelData);
        assertEquals((length + 1) & ~1, pixelData.length());
    }
}