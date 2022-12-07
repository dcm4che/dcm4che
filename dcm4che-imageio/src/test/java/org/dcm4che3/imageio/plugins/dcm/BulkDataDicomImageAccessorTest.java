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
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.imageio.plugins.dcm;


import org.dcm4che.test.data.TestData;
import org.dcm4che3.data.DatasetWithFMI;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.metadata.DefaultMetaDataFactory;
import org.dcm4che3.imageio.metadata.DicomMetaDataFactory;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@RunWith(Parameterized.class)
public class BulkDataDicomImageAccessorTest extends DicomMetaDataTest {


    @Parameterized.Parameters(name="{1} -> Read entire: {0}.  InputStream: {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {true, BulkDataDescriptor.DEFAULT, false},
                {false, BulkDataDescriptor.DEFAULT , false},
                {true, BulkDataDescriptor.DEFAULT , true},
                {true, BulkDataDescriptor.PIXELDATA, false},
                {false, BulkDataDescriptor.PIXELDATA, false},
                {true, BulkDataDescriptor.PIXELDATA, true},
        });
    }

    @Parameterized.Parameter(0)
    public boolean readEntireFile;

    @Parameterized.Parameter(1)
    public BulkDataDescriptor descriptor;

    @Parameterized.Parameter(2)
    public boolean useInputStream;

    @Override
    DicomMetaDataFactory createMetadataFactory() {
        DefaultMetaDataFactory factory = new DefaultMetaDataFactory(readEntireFile);
        factory.setDescriptor(descriptor);
        return factory;
    }

    @Override
    protected DicomMetaData createMetadata(TestData data) throws IOException {
        if(useInputStream) {
            // People can still manually construct DicomMetadata from Attributes
            try(DicomInputStream dis = new DicomInputStream(data.toURL().openStream())) {
                dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.YES);
                DatasetWithFMI datasetWithFMI = dis.readDatasetWithFMI();
                return new DicomMetaData(datasetWithFMI.getFileMetaInformation(), datasetWithFMI.getDataset());
            }
        }
        else {
            return this.createMetadataFactory().readMetaData(data.toFile());
        }
    }
}