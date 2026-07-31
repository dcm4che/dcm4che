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
 * Java(TM), hosted at https://github.com/dcm4che.
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

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import jakarta.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.assertj.core.api.Fail;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReader;
import org.dcm4che3.imageio.plugins.dcm.DicomMetaData;
import org.dcm4che3.imageio.stream.RAFFileImageInputStream;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.SafeClose;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jan 2015
 *
 */
public class TestDicomImageReader {
    private static final Logger log = LoggerFactory.getLogger(TestDicomImageReader.class);

	private static final String TEST_DATA_DIR = "target/test-data/";
    private static final String CPLX_P02 = "cplx_p02.dcm";
    private static final String NM_MF = "NM-MONO2-16-13x-heart";
    private static final String NM_MF_CHECKSUM = "B2813DA2FE5B79A1B3CAF18DBD25023E2F84D4FE";
    private static final String US_MF_RLE = "US-PAL-8-10x-echo";
    private static final String US_MF_RLE_CHECKSUM = "5F4909DEDD7D1E113CC69172C693B4705FEE5B46";
    private static final String REPORT_DFL = "report_dfl";
    private static final String NM_JPLY = "NM1_JPLY";

    DicomImageReader reader;
    
    private static final String postPixelCreator = "TEST";
    private static final int postPixelTag = 0xE0290001;

    @Before
    public void setUp() throws Exception {
        reader = (DicomImageReader) ImageIO.getImageReadersByFormatName("DICOM").next();
    }

    @After
    public void tearDown() throws Exception {
        if (reader != null)
            reader.dispose();
    }
    
    @Test
    public void testReadNoPixelData_InputStream() throws FileNotFoundException, IOException {
    	try(FileInputStream is = new FileInputStream(new File(TEST_DATA_DIR+REPORT_DFL))) {
    		reader.setInput(is);
    		reader.getStreamMetadata();
    		Attributes withPostPixelData = reader.readPostPixeldata();
    		assertThat(withPostPixelData.getString(Tag.ValueType)).isEqualTo("CONTAINER");
    	}
    }

    @Test
    public void testReadNoPixelData_ImageInputStream() throws FileNotFoundException, IOException {
    	try(FileImageInputStream is = new RAFFileImageInputStream(new File(TEST_DATA_DIR+REPORT_DFL))) {
    		reader.setInput(is);
    		reader.getStreamMetadata();
    		Attributes withPostPixelData = reader.readPostPixeldata();
    		assertThat(withPostPixelData.getString(Tag.ValueType)).isEqualTo("CONTAINER");
    	}
    }

    @Test 
    public void testReadCompressedPostPixelData_fromImageInputStream() throws IOException {
        try(FileImageInputStream is = new RAFFileImageInputStream(new File("target/test-data/" + US_MF_RLE))) {
            testReadPostPixelData(is);
        }
    }
    
    @Test 
    public void testReadUncompressedPostPixelData_fromImageInputStream() throws IOException {
        try(FileImageInputStream is = new RAFFileImageInputStream(new File("target/test-data/" + NM_MF))) {
            testReadPostPixelData(is);
        }
    }

    /**
     * This test previously throw an out of memory exception, shows that the changes work.
     * @throws IOException
     */
    @Test
    public void testReadCompressedSingleFrame_fromImageInputStream() throws IOException {
        File file = new File("target/test-data/"+ NM_JPLY);
        try(FileImageInputStream is = new RAFFileImageInputStream(file)) {
            log.trace("Reading file {} iis {}", file, is);
            reader.setInput(is);
            Raster r = reader.readRaster(0, reader.getDefaultReadParam());
        }
    }

    @Test 
    public void testReadCompressedPostPixelData_fromInputStream() throws IOException {
        try(FileInputStream is = new FileInputStream(new File("target/test-data/" + US_MF_RLE))) {
            testReadPostPixelData(is);
        }
    }
    
    @Test 
    public void testReadUncompressedPostPixelData_fromInputStream() throws IOException {
        try(FileInputStream is = new FileInputStream(new File("target/test-data/" + NM_MF))) {
            testReadPostPixelData(is);
        }
    }
    
    private void testReadPostPixelData(Object input) throws IOException {
        reader.setInput(input);
        DicomMetaData metadataOrig = reader.getStreamMetadata();
        Attributes attr = metadataOrig.getAttributes();
        assertThat(attr.getString(postPixelCreator, postPixelTag)).isNull();
        int frames = attr.getInt(Tag.NumberOfFrames, 1);
        for(int i=0; i<frames; i+=3) {
            Raster r= reader.readRaster(i, null);
            assertThat(r).isNotNull();
        }
        Attributes postAttr = reader.readPostPixeldata();
        assertThat(postAttr.getString(postPixelCreator,postPixelTag)).isEqualTo("Value");
        if( input instanceof ImageInputStream ) {
            // Must not throw an exception
            reader.readRaster(0, null);
        } else {
            try {
                reader.readRaster(0, null);
                Fail.fail("Should not be able to read the raster on an input stream after reading post pixel data.");
            } catch(IllegalStateException e) {
                // Expected to throw an exception
            }
        }
    }

    @Test
    public void testGeneratePixelDataFragments_fromEmpty() {
        byte[] basicOffsetTable = new byte[0];
        Attributes attributes = new Attributes();
        Fragments pixelDataFragments = attributes.newFragments(Tag.PixelData, VR.OB, 16);
        DicomImageReader.generateOffsetLengths(pixelDataFragments, 1, basicOffsetTable, 16384);
        assertThat(pixelDataFragments).hasSize(1)
            .contains(new BulkData("compressedPixelData://",16384l+8,-1,false));
        
        pixelDataFragments.clear();
        DicomImageReader.generateOffsetLengths(pixelDataFragments, 4, basicOffsetTable, 16384);
        assertThat(pixelDataFragments).hasSize(4)
        .contains(new BulkData("compressedPixelData://",16384l+8,-1,false),
                new BulkData("compressedPixelData://", -1, -1, false),
                new BulkData("compressedPixelData://", -1, -1, false),
                new BulkData("compressedPixelData://", -1, -1, false));
    }
    
    @Test
    public void testGeneratePixelDataFragments_fromPartialTable() throws IOException {
        Attributes attributes = new Attributes();
        long start = 10944l;
        long current = start;
        long[] longOffsets = new long[]{current, current+=128, current+=65536, current+= 0xffffff00l, current+=2048, current+= 1000 }; 
        int[] offsets = new int[longOffsets.length+2];
        for(int i=0; i<offsets.length; i++) offsets[i] = i< longOffsets.length ? (int) (longOffsets[i]-start) : 1;        
        byte[] basicOffsetTable = ByteUtils.intsToBytesLE(offsets);

        Fragments pixelDataFragments = attributes.newFragments(Tag.PixelData, VR.OB, 16);
        DicomImageReader.generateOffsetLengths(pixelDataFragments, offsets.length, basicOffsetTable, longOffsets[0]);
        assertThat(pixelDataFragments).hasSize(offsets.length)
        .contains(
                new BulkData("compressedPixelData://",longOffsets[0]+8, longOffsets[1]-longOffsets[0]-8,false),
                new BulkData("compressedPixelData://",longOffsets[1]+8, longOffsets[2]-longOffsets[1]-8,false),
                new BulkData("compressedPixelData://",longOffsets[2]+8, longOffsets[3]-longOffsets[2]-8,false),
                new BulkData("compressedPixelData://",longOffsets[3]+8, longOffsets[4]-longOffsets[3]-8,false),
                new BulkData("compressedPixelData://",longOffsets[4]+8, longOffsets[5]-longOffsets[4]-8,false),
                new BulkData("compressedPixelData://",longOffsets[5]+8,-1,false),
                new BulkData("compressedPixelData://", -1, -1, false),
                new BulkData("compressedPixelData://", -1, -1, false));
    }
    
    
    @Test
    public void testReadRasterFromImageInputStream_NM() throws Exception {
        Raster result = testReadRasterFromImageInputStream(NM_MF, 5);
        assertThat(rasterChecksum(result)).isEqualTo(NM_MF_CHECKSUM);
    }

    @Test
    public void testReadRasterFromCompressedImageInputStream() throws Exception {
        Raster result = testReadRasterFromImageInputStream(US_MF_RLE, 5);
        assertThat(rasterChecksum(result)).isEqualTo(US_MF_RLE_CHECKSUM);
    }

    @Test
    public void testReadRasterFromAttributes() throws Exception {
        Raster result = testReadRasterFromAttributes(NM_MF, 5, IncludeBulkData.URI);
        assertThat(rasterChecksum(result)).isEqualTo(NM_MF_CHECKSUM);
    }

    @Test
    public void testReadRasterFromCompressedAttributes() throws Exception {
        Raster result = testReadRasterFromAttributes(US_MF_RLE, 5, IncludeBulkData.URI);
        assertThat(rasterChecksum(result)).isEqualTo(US_MF_RLE_CHECKSUM);
    }

    @Test
    public void testReadRasterFromAttributesWithInMemoryBulkData() throws Exception {
        Raster result = testReadRasterFromAttributes(NM_MF, 5, IncludeBulkData.YES);
        assertThat(rasterChecksum(result)).isEqualTo(NM_MF_CHECKSUM);
    }

    @Test
    public void testReadRasterFromCompressedAttributesWithInMemoryBulkData() throws Exception {
        Raster result = testReadRasterFromAttributes(US_MF_RLE, 5, IncludeBulkData.YES);
        assertThat(rasterChecksum(result)).isEqualTo(US_MF_RLE_CHECKSUM);
    }

    @Test
    public void testReadRasterFromImageInputStream_CPLX() throws IOException {
        testReadRasterFromImageInputStream(CPLX_P02, 1);
    }

    @Test
    public void testReadLastRasterFromCompressedImageInputStream() throws IOException {
        testReadRasterFromImageInputStream(US_MF_RLE, 9);
    }

    @Test
    public void testReadFirstRasterFromCompressedImageInputStream() throws IOException {
        testReadRasterFromImageInputStream(US_MF_RLE, 0);
    }

    @Test
    public void testReadRasterFromInputStream() throws IOException {
        testReadRasterFromInputStream(CPLX_P02, 1);
    }

    @Test
    public void testReadRasterFromCompressedInputStream() throws IOException {
        testReadRasterFromInputStream(US_MF_RLE, 5);
    }

    @Test
    public void testReadRasterFromAttributes_cplx() throws IOException {
        testReadRasterFromAttributes(CPLX_P02, 1, IncludeBulkData.URI);
    }

    private Raster testReadRasterFromImageInputStream(String ifname, int imageIndex)
            throws IOException {
        FileImageInputStream iis = new RAFFileImageInputStream(new File("target/test-data/" + ifname));
        try {
            return testReadRasterFromInput(iis, imageIndex);
        } finally {
            SafeClose.close(iis);
        }
    }

    private Raster testReadRasterFromInputStream(String ifname, int imageIndex)
            throws IOException {
        FileInputStream is = new FileInputStream(new File("target/test-data/" + ifname));
        try {
            return testReadRasterFromInput(is, imageIndex);
        } finally {
            SafeClose.close(is);
        }
    }

    private Raster testReadRasterFromAttributes(String ifname, int imageIndex, IncludeBulkData includeBulkData) throws IOException {
        DicomInputStream dis = new DicomInputStream(new File("target/test-data/" + ifname));
        Attributes attrs;
        try {
            dis.setIncludeBulkData(includeBulkData);
            attrs = dis.readDataset();
        } finally {
            SafeClose.close(dis);
        }
        return testReadRasterFromInput(new DicomMetaData(dis.getFileMetaInformation(), attrs), imageIndex);
    }

    private Raster testReadRasterFromInput(Object input, int imageIndex)
            throws IOException {
        reader.setInput(input);
        return reader.readRaster(imageIndex, reader.getDefaultReadParam());
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
