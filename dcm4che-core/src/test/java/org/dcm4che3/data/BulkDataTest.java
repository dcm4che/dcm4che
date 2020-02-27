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

package org.dcm4che3.data;

import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.*;

public class BulkDataTest {
    private static final String US_MF_RLE = "../../src/test/data/US-PAL-8-10x-echo";


    public static final String URL_PATH = "file:/path";
    public static final String URL_OFFSET = "file:/path?offset=1234&length=5678";
    public static final String URL_OFFSETS = "file:/path?offsets=1,2,3,4&lengths=5,6,7,8";
    public static final long[] OFFSETS = { 1, 2, 3, 4 };
    public static final int[] LENGTHS = { 5, 6, 7, 8 };
    public static final int OFFSET = 1234;
    public static final int LENGTH = 5678;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testParseOffset() {
        BulkData bd = new BulkData(null, URL_OFFSET, false);
        assertEquals(URL_PATH, bd.uriWithoutQuery());
        assertEquals(OFFSET, bd.offset());
        assertEquals(LENGTH, bd.length());
        assertEquals( URI.create("file:/path"), bd.toFileURI());
    }

    @Test
    public void testParseOffsets() {
        BulkData bd = new BulkData(null, URL_OFFSETS, false);
        assertEquals(URL_PATH, bd.uriWithoutQuery());
        assertArrayEquals(OFFSETS, bd.offsets());
        assertArrayEquals(LENGTHS, bd.lengths());
        assertEquals(-1, bd.length());
        assertEquals( URI.create("file:/path"), bd.toFileURI());
    }

    @Test
    public void testURIOffsets() {
        BulkData bd = new BulkData(URL_PATH, OFFSETS, LENGTHS, false);
        assertEquals(URL_OFFSETS, bd.uri);
        assertEquals( URI.create("file:/path"), bd.toFileURI());
    }


    @Test
    public void writeTo_ReadSectionOfText() throws URISyntaxException, IOException {
        URI log4jURI = getClass().getResource("/log4j.properties").toURI();
        BulkData bd = new BulkData(log4jURI.toString(), 0, 21, false );

        byte[] bytes = bd.toBytes(VR.ST, false);
        String actual = new String(bytes);
        assertEquals("log4j.rootLogger=INFO", actual);
    }

    @Test
    public void writeTo_HandlesPixelData() throws URISyntaxException, IOException, NoSuchAlgorithmException {
        URI fileURI = getClass().getResource("/OT-PAL-8-face").toURI();
        File file = Paths.get(fileURI).toFile();
        try(DicomInputStream dis = new DicomInputStream(file)) {
            dis.setURI(fileURI.toString());
            dis.setBulkDataDescriptor(BulkDataDescriptor.PIXELDATA);
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);

            DatasetWithFMI datasetWithFMI = dis.readDatasetWithFMI();
            VR pixelVR = datasetWithFMI.getDataset().getVR(Tag.PixelData);
            BulkData bulkData = (BulkData) datasetWithFMI.getDataset().getValue(Tag.PixelData);
            assertEquals(fileURI, bulkData.toFileURI());
            assertThat(bulkData.getURI(), CoreMatchers.endsWith("OT-PAL-8-face?offset=1654&length=307200"));
            byte[] pixelData = bulkData.toBytes(pixelVR, false);
            assertEquals("17AA58445D74CB965A7634EBAEFE66710372511B", toHexDigest(pixelData));
        }
    }

    private String toHexDigest(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(bytes);
        return new HexBinaryAdapter().marshal(digest.digest());
    }
}