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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.imageio.plugins.dcm.DicomMetaData;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.SafeClose;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jan 2015
 *
 */
public class TestDicomImageReader {

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
    public void testReadRasterFromImageInputStream() throws IOException {
        testReadRasterFromImageInputStream("cplx_p02.dcm", 1);
    }

    @Test
    public void testReadRasterFromCompressedImageInputStream() throws IOException {
        testReadRasterFromImageInputStream("US-PAL-8-10x-echo", 5);
    }

    @Test
    public void testReadRasterFromInputStream() throws IOException {
        testReadRasterFromInputStream("cplx_p02.dcm", 1);
    }

    @Test
    public void testReadRasterFromCompressedInputStream() throws IOException {
        testReadRasterFromInputStream("US-PAL-8-10x-echo", 5);
    }

    @Test
    public void testReadRasterFromAttributes() throws IOException {
        testReadRasterFromAttributes("cplx_p02.dcm", 1);
    }

    @Test
    public void testReadRasterFromCompressedAttributes() throws IOException {
        testReadRasterFromAttributes("US-PAL-8-10x-echo", 5);
    }

    private void testReadRasterFromImageInputStream(String ifname, int imageIndex)
            throws IOException {
        FileImageInputStream iis = new FileImageInputStream(new File("target/test-data/" + ifname));
        try {
            testReadRasterFromInput(iis, imageIndex);
        } finally {
            SafeClose.close(iis);
        }
    }

    private void testReadRasterFromInputStream(String ifname, int imageIndex)
            throws IOException {
        FileInputStream is = new FileInputStream(new File("target/test-data/" + ifname));
        try {
            testReadRasterFromInput(is, imageIndex);
        } finally {
            SafeClose.close(is);
        }
    }

    private void testReadRasterFromAttributes(String ifname, int imageIndex)
            throws IOException {
        DicomInputStream dis = new DicomInputStream(new File("target/test-data/" + ifname));
        Attributes attrs;
        try {
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            attrs = dis.readDataset(-1, -1);
        } finally {
            SafeClose.close(dis);
        }
        testReadRasterFromInput(
                new DicomMetaData(dis.getFileMetaInformation(), attrs),
                imageIndex);
    }

    private void testReadRasterFromInput(Object input, int imageIndex)
            throws IOException {
        reader.setInput(input);
        reader.readRaster(imageIndex, reader.getDefaultReadParam());
    }

}
