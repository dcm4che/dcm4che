/*
 * *** BEGIN LICENSE BLOCK *****
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
 * Portions created by the Initial Developer are Copyright (C) 2013
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.imageio.codec;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jul 2015
 */
public class TranscoderTest {

    @BeforeClass
    public static void before() {
        File odir = new File("target/test-out/");
        odir.mkdir();
        for (File f : odir.listFiles())
            f.delete();
    }

    @Test
    public void testCopyIVR() throws Exception {
        test("OT-PAL-8-face", "OT-PAL-8-face", UID.ImplicitVRLittleEndian, false);
    }

    @Test
    public void testCopyBigEndian() throws Exception {
        test("US-RGB-8-epicard", "US-RGB-8-epicard", UID.ExplicitVRBigEndianRetired, true);
    }

    @Test
    public void testCopyDeflated() throws Exception {
        test("report_dfl", "report_dfl", UID.DeflatedExplicitVRLittleEndian, true);
    }

    @Test
    public void testCopyJPEG12bit() throws Exception {
        test("NM1_JPLY", "NM1_JPLY", UID.JPEGExtended24, true);
    }

    @Test
    public void testBigEndian2LittleEndian() throws Exception {
        test("US-RGB-8-epicard", "US-RGB-8-epicard.littleEndian", UID.ImplicitVRLittleEndian, false);
    }

    @Test
    public void testDecompressJPEG12bit() throws Exception {
        if (Boolean.getBoolean("JIIO"))
            test("NM1_JPLY", "NM1_JPLY.unc", UID.ExplicitVRLittleEndian, true);
    }

    @Test
    public void testDecompressMF() throws Exception {
        test("US-PAL-8-10x-echo", "US-PAL-8-10x-echo.unc", UID.ExplicitVRLittleEndian, true);
    }

    @Test
    public void testCompressMF() throws Exception {
        test("cplx_p02.dcm", "cplx_p02_jply.dcm", UID.JPEGBaseline1, true);
    }

    @Test
    public void testCompressEmbeddedOverlays() throws Exception {
        if (Boolean.getBoolean("JIIO"))
            test("ovly_p01.dcm", "ovly_p01_jply.dcm", UID.JPEGExtended24, true);
    }

    @Test
    public void testCompressPerPlaneRGB() throws Exception {
        test("US-RGB-8-epicard", "US-RGB-8-epicard_jply", UID.JPEGBaseline1, true);
    }

    @Test
    public void testCompressPerPixelRGB() throws Exception {
        test("US-RGB-8-esopecho", "US-RGB-8-esopecho_jply", UID.JPEGBaseline1, true);
    }

    private void test(String ifname, String ofname, final String outts, boolean fmi) throws IOException {

        final File ifile = new File("target/test-data/" + ifname);
        final File ofile = new File("target/test-out/" + ofname);
        Transcoder.Handler handler = new Transcoder.Handler() {
            @Override
            public OutputStream newOutputStream(Transcoder transcoder, Attributes dataset) throws IOException {
                transcoder.setDestinationTransferSyntax(outts);
                return new FileOutputStream(ofile);
            }
        };
        try (Transcoder transcoder = new Transcoder(ifile)) {
            transcoder.setIncludeFileMetaInformation(fmi);
            transcoder.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            transcoder.transcode(handler);
        }
    }
}