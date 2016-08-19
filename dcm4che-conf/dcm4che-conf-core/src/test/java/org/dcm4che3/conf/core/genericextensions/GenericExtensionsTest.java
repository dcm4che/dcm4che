/*
 * *** BEGIN LICENSE BLOCK *****
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
 *  Portions created by the Initial Developer are Copyright (C) 2015
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

package org.dcm4che3.conf.core.genericextensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che3.conf.core.DefaultTypeSafeConfiguration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;
import org.dcm4che3.conf.core.storage.SingleJsonFileConfigurationStorage;
import org.junit.Assert;
import org.junit.Test;

public class GenericExtensionsTest {

    @Test
    public void test() throws ConfigurationException {


        final SingleJsonFileConfigurationStorage storage = new SingleJsonFileConfigurationStorage("target/config.json");
        storage.persistNode(Path.ROOT, new HashMap<String, Object>(), null);

        List myExtensions = new ArrayList();
        myExtensions.add(MyClassFirstExtension.class);
        myExtensions.add(MyClassSecondExtension.class);


        Map<Class, List<Class>> exts = new HashMap<Class, List<Class>>();
        exts.put(MyClassExtension.class, myExtensions);


        DefaultTypeSafeConfiguration configuration = new DefaultTypeSafeConfiguration(storage, null, exts);
        BeanVitalizer vitalizer = configuration.getVitalizer();


        ConfigClassWithExtensions myStuff = new ConfigClassWithExtensions();
        myStuff.setMyProp("aValue");

        MyClassFirstExtension firstExtension = new MyClassFirstExtension();
        firstExtension.setMyFirstBoolParam(true);
        firstExtension.setMyFirstParam("firstIsfirst");
        myStuff.getExtensions().put(MyClassFirstExtension.class, firstExtension);

        MyClassSecondExtension secondExtension = new MyClassSecondExtension();
        secondExtension.setMySecondParam(17);
        secondExtension.setMySecondBoolParam(true);
        myStuff.getExtensions().put(MyClassSecondExtension.class, secondExtension);

        Map<String, Object> node = vitalizer.createConfigNodeFromInstance(myStuff);
        storage.persistNode(Path.ROOT, node, ConfigClassWithExtensions.class);

        ConfigClassWithExtensions loaded = vitalizer.newConfiguredInstance(node, ConfigClassWithExtensions.class);
        MyClassFirstExtension myClassFirstExtension = (MyClassFirstExtension) loaded.getExtensions().get(MyClassFirstExtension.class);
        MyClassSecondExtension myClassSecondExtension = (MyClassSecondExtension) loaded.getExtensions().get(MyClassSecondExtension.class);

        Assert.assertEquals(myClassFirstExtension.getMyFirstParam(),firstExtension.getMyFirstParam());
        Assert.assertEquals(myClassSecondExtension.getMySecondParam(), secondExtension.getMySecondParam());
        Assert.assertEquals(loaded.getExtensions().size(), 2);


        Assert.assertEquals(myClassFirstExtension.getParent(), loaded);
        Assert.assertEquals(loaded.getMyProp(), myStuff.getMyProp());

        // test empty / null
        ConfigClassWithExtensions myEmptyStuff = new ConfigClassWithExtensions();
        vitalizer.newConfiguredInstance(vitalizer.createConfigNodeFromInstance(myEmptyStuff), ConfigClassWithExtensions.class);

        ConfigClassWithExtensions myNullEmptyStuff = new ConfigClassWithExtensions();
        myNullEmptyStuff.setExtensions(null);
        vitalizer.newConfiguredInstance(vitalizer.createConfigNodeFromInstance(myNullEmptyStuff), ConfigClassWithExtensions.class);


    }

}
