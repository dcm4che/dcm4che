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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.imageio.codec.jpeg.JPEGParser;
import org.dcm4che3.io.DicomInputStream;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
        test("US-RGB-8-epicard", "US-RGB-8-epicard", UID.ExplicitVRBigEndian, true);
    }

    @Test
    public void testCopyDeflated() throws Exception {
        test("report_dfl", "report_dfl", UID.DeflatedExplicitVRLittleEndian, true);
    }

    @Test
    public void testCopyJPEG12bit() throws Exception {
        test("NM1_JPLY", "NM1_JPLY", UID.JPEGExtended12Bit, true);
    }

    @Test
    public void testBigEndian2LittleEndian() throws Exception {
        test("US-RGB-8-epicard", "US-RGB-8-epicard.littleEndian", UID.ImplicitVRLittleEndian, false);
    }

    @Test
    public void testDecompressJPEG12bit() throws Exception {
        test("NM1_JPLY", "NM1_JPLY.unc", UID.ExplicitVRLittleEndian, true);
    }

    @Test
    public void testDecompressMF() throws Exception {
        test("US-PAL-8-10x-echo", "US-PAL-8-10x-echo.unc", UID.ExplicitVRLittleEndian, true);
    }

    @Test
    public void testDecompressJpeglsPaletteMF() throws Exception {
        test("jpeg-ls-Palette.dcm", "jpeg-ls-Palette-raw.dcm", UID.ExplicitVRLittleEndian, true);
    }

    @Test
    public void testCompressMF() throws Exception {
        test("cplx_p02.dcm", "cplx_p02_jply.dcm", UID.JPEGBaseline8Bit, true);
    }

    @Test
    public void testCompressEmbeddedOverlays() throws Exception {
        test("ovly_p01.dcm", "ovly_p01_jply.dcm", UID.JPEGExtended12Bit, true);
    }

    @Test
    public void testCompressPerPlaneRGB() throws Exception {
        test("US-RGB-8-epicard", "US-RGB-8-epicard_jply", UID.JPEGBaseline8Bit, true);
    }

    @Test
    public void testCompressPerPixelRGB() throws Exception {
        test("US-RGB-8-esopecho", "US-RGB-8-esopecho_jply", UID.JPEGBaseline8Bit, true);
    }

    @Test
    public void testCompressPerPixelRgb2JpegLossless() throws Exception {
        test("US-RGB-8-esopecho", "US-RGB-8-esopecho-jpegLossless.dcm", UID.JPEGLosslessSV1, true);
    }

    @Test
    public void testTranscodePaletteRleMf2RgbJpegls() throws Exception {
        test("US-PAL-8-10x-echo", "US-PAL-8-10x-echo-jpegls.dcm", UID.JPEGLSNearLossless, true);
    }

    @Test
    public void testTranscodeJpeglsPaletteMf2RgbJ2k() throws Exception {
        test("jpeg-ls-Palette.dcm", "jpeg-ls-Palette-j2k.dcm", UID.JPEG2000, true);
    }

    @Test
    public void testTranscodeYbrFullRle2RgbJ2k() throws Exception {
        test("YBR_FULL-RLE.dcm", "YBR_FULL-RLE-j2k.dcm", UID.JPEG2000, true);
    }

    @Test
    public void testTranscodeYbr422Raw2RgbJpegLossless() throws Exception {
        test("YBR_422.dcm", "YBR_422-jpegLossless.dcm", UID.JPEGLosslessSV1, true);
    }

    @Test
    public void testTranscodeYbr422Raw2RgbJ2k() throws Exception {
        test("YBR_422.dcm", "YBR_422-j2k.dcm", UID.JPEG2000, true);
    }

    @Test
    public void testCompress12BitsJPLL() throws Exception {
        test("MR2_UNC", "MR2_UNC-JPLL.dcm", UID.JPEGLosslessSV1, true);
        assertEquals(12, jpegBitsPerSample("MR2_UNC-JPLL.dcm"));
    }

    @Test
    public void testCompress12BitsJLSL() throws Exception {
        test("MR2_UNC", "MR2_UNC-JLSL.dcm", UID.JPEGLSLossless, true);
        assertEquals(12, jpegBitsPerSample("MR2_UNC-JLSL.dcm"));
    }

    @Test
    public void testCompress12BitsJ2KR() throws Exception {
        test("MR2_UNC", "MR2_UNC-J2KR.dcm", UID.JPEG2000Lossless, true);
        assertEquals(12, jpegBitsPerSample("MR2_UNC-J2KR.dcm"));
    }

    @Test
    public void testSigned12BitsJ2KI() throws Exception {
        test("test16signed.dcm", "test16signed-J2KI.dcm", UID.JPEG2000, true);
        assertEquals(1, jpegPixelRepresentation("test16signed-J2KI.dcm"));
    }

    private int jpegBitsPerSample(String ofname) throws IOException {
        final File ofile = new File("target/test-out/" + ofname);
        long jpegPos = jpegPos(ofile);
        try (SeekableByteChannel channel = Files.newByteChannel(ofile.toPath())) {
            channel.position(jpegPos);
            return new JPEGParser(channel).getAttributes(null).getInt(Tag.BitsStored, -1);
        }
    }

    private int jpegPixelRepresentation(String ofname) throws IOException {
        final File ofile = new File("target/test-out/" + ofname);
        long jpegPos = jpegPos(ofile);
        try (SeekableByteChannel channel = Files.newByteChannel(ofile.toPath())) {
            channel.position(jpegPos);
            return new JPEGParser(channel).getAttributes(null).getInt(Tag.PixelRepresentation, -1);
        }
    }

    private long jpegPos(File ofile) throws IOException {
        try (DicomInputStream dis = new DicomInputStream(ofile)) {
            dis.readDatasetUntilPixelData();
            return dis.getPosition() + 16;
        }
    }

    private void test(String ifname, String ofname, final String outts, boolean fmi)
            throws IOException {
        final File ifile = new File("target/test-data/" + ifname);
        final File ofile = new File("target/test-out/" + ofname);
        Transcoder.Handler handler = new Transcoder.Handler() {
            @Override
            public OutputStream newOutputStream(Transcoder transcoder, Attributes dataset) throws IOException {
                return new FileOutputStream(ofile);
            }
        };
        try (Transcoder transcoder = new Transcoder(ifile)) {
            transcoder.setIncludeFileMetaInformation(fmi);
            transcoder.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            transcoder.setDestinationTransferSyntax(outts);
            transcoder.transcode(handler);
        }
    }

    @Test
    public void testTranscodeFileMultipleTimes() throws Exception {
        test("MR-JPEGLosslessSV1.dcm", UID.JPEGLSLossless, UID.JPEGLosslessSV1);
        test("CT-JPEGLosslessSV1.dcm", UID.ExplicitVRLittleEndian, UID.JPEGLSLossless, UID.JPEGLosslessSV1);
    }

    private void test(String srcFileName, String... transferSyntaxList) throws Exception {
        String newSrcFileName = null;

        for (int i = 0; i < transferSyntaxList.length; i++) {
            String transferSyntax = transferSyntaxList[i];
            String dstFileName = transferSyntax + ".dcm";

            if (i == 0) {
                test(srcFileName, dstFileName, transferSyntax, true);
            } else {
                test(newSrcFileName, dstFileName, transferSyntax, true);
            }

            Path source = Paths.get("target/test-out/", dstFileName);
            Path target = Paths.get("target/test-data/", dstFileName);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            newSrcFileName = dstFileName;
        }
    }

}