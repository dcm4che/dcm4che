/* ***** BEGIN LICENSE BLOCK *****
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
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2013-2015
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
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.imageio.dcm;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.imageio.plugins.dcm.DicomMetaData;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.util.SafeClose;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jan 2015
 *
 */
public class TestDicomImageReader {

    private static final String NM_MF = "src/test/data/NM-MONO2-16-13x-heart";
    private static final String NM_MF_CHECKSUM = "B2813DA2FE5B79A1B3CAF18DBD25023E2F84D4FE";
    private static final String US_MF_RLE = "src/test/data/US-PAL-8-10x-echo";
    private static final String US_MF_RLE_CHECKSUM = "5F4909DEDD7D1E113CC69172C693B4705FEE5B46";


    ImageReader reader;

    @Before
    public void setUp() throws Exception {
        reader = ImageIO.getImageReadersByFormatName("DICOM").next();
    }

    @After
    public void tearDown() throws Exception {
        if (reader != null)
            reader.dispose();
    }

    @Test
    public void testReadRasterFromImageInputStream() throws Exception {
        Raster result = testReadRasterFromImageInputStream(NM_MF, 5);
        Assert.assertEquals(NM_MF_CHECKSUM, rasterChecksum(result));
    }

    @Test
    public void testReadRasterFromCompressedImageInputStream() throws Exception {
        Raster result = testReadRasterFromImageInputStream(US_MF_RLE, 5);
        Assert.assertEquals(US_MF_RLE_CHECKSUM, rasterChecksum(result));
    }

    @Test
    public void testReadRasterFromAttributes() throws Exception {
        Raster result = testReadRasterFromAttributes(NM_MF, 5, IncludeBulkData.URI);
        Assert.assertEquals(NM_MF_CHECKSUM, rasterChecksum(result));
    }

    @Test
    public void testReadRasterFromCompressedAttributes() throws Exception {
        Raster result = testReadRasterFromAttributes(US_MF_RLE, 5, IncludeBulkData.URI);
        Assert.assertEquals(US_MF_RLE_CHECKSUM, rasterChecksum(result));
    }

    @Test
    public void testReadRasterFromAttributesWithInMemoryBulkData() throws Exception {
        Raster result = testReadRasterFromAttributes(NM_MF, 5, IncludeBulkData.YES);
        Assert.assertEquals(NM_MF_CHECKSUM, rasterChecksum(result));
    }

    @Test
    public void testReadRasterFromCompressedAttributesWithInMemoryBulkData() throws Exception {
        Raster result = testReadRasterFromAttributes(US_MF_RLE, 5, IncludeBulkData.YES);
        Assert.assertEquals(US_MF_RLE_CHECKSUM, rasterChecksum(result));
    }

    private Raster testReadRasterFromImageInputStream(String pathname, int imageIndex)
            throws IOException {
        FileImageInputStream iis = new FileImageInputStream(new File(pathname));
        try {
            return testReadRasterFromInput(iis, imageIndex);
        } finally {
            SafeClose.close(iis);
        }
    }

    private Raster testReadRasterFromAttributes(String pathname, int imageIndex, IncludeBulkData includeBulkData)
            throws IOException {
        DicomInputStream dis = new DicomInputStream(new File(pathname));
        Attributes attrs;
        try {
            dis.setIncludeBulkData(includeBulkData);
            attrs = dis.readDataset(-1, -1);
        } finally {
            SafeClose.close(dis);
        }
        return testReadRasterFromInput(
                new DicomMetaData(dis.getFileMetaInformation(), attrs),
                imageIndex);
    }

    private Raster testReadRasterFromInput(Object input, int imageIndex)
            throws IOException {
        reader.setInput(input);
        return reader.readRaster(imageIndex, null);
    }

    /**
     * Extracts data from the Raster and returns the SHA1 checksum. Does not verify anything else
     * about the structure of the raster (color space, etc.).
     */
    private String rasterChecksum(Raster raster) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        if (raster.getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT) {
            DataBufferUShort dataBuffer = (DataBufferUShort) raster.getDataBuffer();
            for (short[] bank : dataBuffer.getBankData()) {
                ByteBuffer buf = ByteBuffer.allocate(bank.length * 2);
                buf.asShortBuffer().put(bank);
                digest.update(buf);
            }
        } else if (raster.getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE) {
            DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
            for (byte[] bank : dataBuffer.getBankData()) {
                digest.update(bank);
            }
        } else {
            throw new RuntimeException("Raster is neither USHORT nor BYTE.");
        }

        return new HexBinaryAdapter().marshal(digest.digest());
    }

}
