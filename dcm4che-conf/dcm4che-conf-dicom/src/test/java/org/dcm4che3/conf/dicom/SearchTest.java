/*
 * **** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2014
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */
package org.dcm4che3.conf.dicom;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Configuration;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Roman K
 */
public class SearchTest {

    @Test
    public void searchTest() throws ConfigurationException {
        Configuration storage;
        if (System.getProperty("org.dcm4che.conf.storage") == null) {
            storage = SimpleStorageTest.getMockDicomConfStorage();
        } else {
            storage = SimpleStorageTest.getConfigurationStorage();
        }
        searchTestForStorage(storage);
    }

    public void searchTestForStorage(Configuration storage) throws ConfigurationException {



        // arc device is there
        Iterator search = storage.search(DicomPath.DeviceNameByAEName.set("aeName", "DCM4CHEE").path());
        Assert.assertEquals("dcm4chee-arc", search.next());

        // mppsscp device is there
        search = storage.search(DicomPath.DeviceNameByAEName.set("aeName", "MPPSSCP").path());
        Assert.assertEquals("mppsscp", search.next());

        // its 14 devices
        search = storage.search(DicomPath.AllDeviceNames.path());
        ArrayList<String> names = new ArrayList<String>();
        while (search.hasNext())
            names.add((String) search.next());
        Assert.assertEquals(14,names.size());

        // 12 aes
        search = storage.search(DicomPath.AllAETitles.path());
        names = new ArrayList<String>();
        while (search.hasNext())
            names.add((String) search.next());
        Assert.assertTrue(names.contains("DCM4CHEE"));
        Assert.assertEquals(12,names.size());

        // hl7 app
        search = storage.search(DicomPath.DeviceNameByHL7AppName.set("hl7AppName", "HL7RCV^DCM4CHEE").path());
        Assert.assertEquals("hl7rcv",search.next());
        search = storage.search(DicomPath.AllHL7AppNames.path());
        Assert.assertTrue(search.next().equals("HL7RCV^DCM4CHEE") || search.next().equals("HL7RCV^DCM4CHEE"));

    }


}
