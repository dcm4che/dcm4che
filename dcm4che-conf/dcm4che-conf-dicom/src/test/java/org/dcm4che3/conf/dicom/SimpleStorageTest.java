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

import org.dcm4che3.conf.core.api.ConfigurableClassExtension;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.storage.SimpleCachingConfigurationDecorator;
import org.dcm4che3.conf.core.storage.SingleJsonFileConfigurationStorage;
import org.dcm4che3.conf.dicom.configclasses.SomeDeviceExtension;
import org.dcm4che3.conf.dicom.misc.DeepEqualsDiffer;
import org.dcm4che3.net.TCGroupConfigAEExtension;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.URL;
import java.util.*;

/**
 * @author Roman K
 */
@RunWith(JUnit4.class)
public class SimpleStorageTest {

    public static Configuration getConfigurationStorage() throws ConfigurationException {

        CommonDicomConfigurationWithHL7 configurationWithHL7 = createCommonDicomConfiguration();
        return configurationWithHL7.getConfigurationStorage();
    }

    public static CommonDicomConfigurationWithHL7 createCommonDicomConfiguration(List<ConfigurableClassExtension> extensions) throws ConfigurationException {
        if (System.getProperty("org.dcm4che.conf.filename") == null)
            System.setProperty("org.dcm4che.conf.filename", "target/config.json");


        DicomConfigurationBuilder builder = DicomConfigurationBuilder.newConfigurationBuilder(System.getProperties());
        builder.registerDeviceExtension(HL7DeviceExtension.class);
        builder.registerAEExtension(TCGroupConfigAEExtension.class);
        builder.registerDeviceExtension(SomeDeviceExtension.class);

        for (ConfigurableClassExtension extension : extensions) {
            builder.registerExtensionForBaseExtension(extension.getClass(), extension.getBaseClass());
        }

        builder.uuidIndexing();

        return builder.build();

    }


    public static CommonDicomConfigurationWithHL7 createCommonDicomConfiguration() throws ConfigurationException {
        if (System.getProperty("org.dcm4che.conf.filename") == null)
            System.setProperty("org.dcm4che.conf.filename", "target/config.json");


        DicomConfigurationBuilder builder = DicomConfigurationBuilder.newConfigurationBuilder(System.getProperties());
        builder.registerDeviceExtension(HL7DeviceExtension.class);
        builder.registerAEExtension(TCGroupConfigAEExtension.class);
        builder.registerDeviceExtension(SomeDeviceExtension.class);
        builder.uuidIndexing();
        builder.extensionMerge(true);
        return builder.build();
    }


    public static Configuration getMockDicomConfStorage() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("mockConfig.json");
        String path = resource.getPath();
        SingleJsonFileConfigurationStorage storage = new SingleJsonFileConfigurationStorage(path);
        return new SimpleCachingConfigurationDecorator(storage, true);
    }

    @Test
    public void testSave() throws ConfigurationException {
        Configuration xCfg = getConfigurationStorage();

        Map<String, Object> p1 = new HashMap<String, Object>();
        p1.put("prop1", 56);
        p1.put("prop2", "I am cool");

        Map<String, Object> p2 = new HashMap<String, Object>();
        p2.put("prop1", true);
        p2.put("prop2", Arrays.asList(1, 2, 3));

        Map<String, Object> p3 = new HashMap<String, Object>();
        p3.put("p1", p1);
        p3.put("p2", p2);

        xCfg.persistNode(Path.ROOT, p3, null);

        DeepEqualsDiffer.assertDeepEquals("Stored config node must be equal to the one loaded", p3, xCfg.getConfigurationNode(Path.ROOT, null));

        xCfg.persistNode(new Path("p2","newProp"), p1, null);

        xCfg.removeNode(new Path("p2","prop11"));

        Iterator search = xCfg.search("/*[contains(prop2,'I am ')]");
        Object o = search.next();
        DeepEqualsDiffer.assertDeepEquals("Search should work. ", o, p1);
    }

    @Test
    public void nodeExists() throws ConfigurationException {
        Configuration configurationStorage = getConfigurationStorage();
        configurationStorage.persistNode(Path.ROOT, new HashMap<String, Object>(), null);
        Assert.assertEquals(configurationStorage.nodeExists(new Path("asd","fdg","sdsf")), false);

    }

    @Test
    public void testSpecialSymbols() throws ConfigurationException {
        Configuration xCfg = getConfigurationStorage();

        // serialize to confignode


        Map<String, Object> pd = new HashMap<String, Object>();
        pd.put("_prop", "hey");
        pd.put("prop1", Arrays.asList(1, 2, 3));

        Map<String, Object> pd1 = new HashMap<String, Object>();
        pd1.put("prop1", "aVal");

        Map<String, Object> p2 = new HashMap<String, Object>();
        p2.put("device1", pd);
        p2.put("device2", Arrays.asList(1, 2, 3));
        p2.put("de[]viceWeirdo", pd1);

        Map<String, Object> p1 = new HashMap<String, Object>();
        p1.put("dicomDevicesRoot", p2);
        p1.put("Unique AE Titles Registry", "I am a cool AppEntity");

        Map<String, Object> p3 = new HashMap<String, Object>();
        p3.put("dicomConfigurationRoot", p1);

        xCfg.persistNode(Path.ROOT, p3, null);

        Assert.assertEquals(xCfg.search("/dicomConfigurationRoot/dicomDevicesRoot/device1/_prop").next().toString(), "hey");

        Assert.assertNotNull(xCfg.search("/dicomConfigurationRoot[@name='dicomDevicesRoot'][@name='de[]viceWeirdo']/prop1").next().toString(), "aVal");

    }
}
