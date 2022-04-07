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
 * Portions created by the Initial Developer are Copyright (C) 2011
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
package org.dcm4che3.net;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Maciek Siemczyk
 */
public class DeviceTest {

    private static final String DEVICE_NAME = "AnyDeviceName";
    
    private static final String AE_TITLE = "MyAeTitle";
    private static final String FIRST_ALIAS_AE_TITLE = "AliasAET";
    private static final String SECOND_ALIAS_AE_TITLE = "AnotherAliasAET";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    private ApplicationEntity applicationEntity = new ApplicationEntity(AE_TITLE);
    
    /**
     * System Under Test (SUT).
     */
    private Device device = new Device(DEVICE_NAME);

    @Test
    public void getApplicationEntity_ThrowsIllegalArgumentException_GivenNull() {

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Application Entity Title (aet) is null");
        
        device.getApplicationEntity(null);
    }
    
    @Test
    public void getApplicationEntity_ReturnsNull_GivenNotExistingAeTitle() {

        applicationEntity.setAETitleAliases(FIRST_ALIAS_AE_TITLE);
        device.addApplicationEntity(applicationEntity);

        assertThat("Returned not null value", device.getApplicationEntity("GoodLuck"), is(nullValue()));
    }
    
    @Test
    public void getApplicationEntity_ReturnsCorrectApplicationEntity_GivenValidAeTitle() {

        // Create and add to device another AE with alias same as the 'main' Application Entity.
        ApplicationEntity anotherApplicationEntity = new ApplicationEntity("NotMyAeTitle");
        anotherApplicationEntity.setAETitleAliases(AE_TITLE);
            
        device.addApplicationEntity(anotherApplicationEntity);
        device.addApplicationEntity(applicationEntity);

        assertThat("Returned wrong AE", device.getApplicationEntity(AE_TITLE), is(applicationEntity));
    }
    
    @Test
    public void getApplicationEntity_ReturnsCorrectApplicationEntity_GivenValidAeTitleAlias() {

        ApplicationEntity aliasedApplicationEntity = new ApplicationEntity("AliasedAe");
        aliasedApplicationEntity.setAETitleAliases(FIRST_ALIAS_AE_TITLE, SECOND_ALIAS_AE_TITLE);
        
        // Add one alias to the 'main' Application Entity just in case.
        applicationEntity.setAETitleAliases("alias");
            
        device.addApplicationEntity(aliasedApplicationEntity);
        device.addApplicationEntity(applicationEntity);

        assertThat(
                "Returned wrong AE",
                device.getApplicationEntity(SECOND_ALIAS_AE_TITLE),
                is(aliasedApplicationEntity));
    }

    @Test
    public void getApplicationEntity_ReturnsCorrectApplicationEntity_GivenNotExistingAeTitleButDeviceHasAeWithDefaultTitle() {

        ApplicationEntity defaultApplicationEntity = new ApplicationEntity(Device.DEFAULT_AE_TITLE);
        
        // Add one alias to the 'main' Application Entity just in case.
        applicationEntity.setAETitleAliases(FIRST_ALIAS_AE_TITLE);
        
        device.addApplicationEntity(defaultApplicationEntity);
        device.addApplicationEntity(applicationEntity);

        assertThat(
                "Returned wrong AE",
                device.getApplicationEntity("ImNotThere"),
                is(defaultApplicationEntity));
    }

    @Test
    public void getApplicationEntity_ReturnsCorrectApplicationEntity_GivenNotExistingAeTitleButDeviceHasAeAliasWithDefaultTitle() {

        ApplicationEntity defaultApplicationEntity = new ApplicationEntity("AET");
        defaultApplicationEntity.setAETitleAliases(Device.DEFAULT_AE_TITLE);
        
        device.addApplicationEntity(defaultApplicationEntity);
        device.addApplicationEntity(applicationEntity);

        assertThat(
                "Returned wrong AE",
                device.getApplicationEntity("ImNotHere"),
                is(defaultApplicationEntity));
    }
    
    @Test
    public void getApplicationAETitles_ReturnsAllAeTitlesAndAliases_WhenDeviceHasTwoApplicationEntitiesWithAeTitleAliases() {

        final String aet = "SomeAET";
        final String aetAlias = "SomeAlias";
        
        ApplicationEntity anotherApplicationEntity = new ApplicationEntity(aet);
        anotherApplicationEntity.setAETitleAliases(aetAlias);
        
        applicationEntity.setAETitleAliases(FIRST_ALIAS_AE_TITLE, SECOND_ALIAS_AE_TITLE);
        
        device.addApplicationEntity(anotherApplicationEntity);
        device.addApplicationEntity(applicationEntity);

        assertThat(
                "Returned Application Entity titles are not correct",
                device.getApplicationAETitles(),
                containsInAnyOrder(AE_TITLE, FIRST_ALIAS_AE_TITLE, SECOND_ALIAS_AE_TITLE, aet, aetAlias));
    }
    
    @Test
    public void testReconfigure() throws Exception {
        
        Device d1 = createDevice("test", "AET1");
        Device d2 = createDevice("test", "AET2");
        d2.setOlockHash("I.hash.you.not");
        d2.setStorageVersion(35);
        
        d1.reconfigure(d2);
        ApplicationEntity ae = d1.getApplicationEntity("AET2");
        assertNotNull(ae);
        List<Connection> conns = ae.getConnections();
        assertEquals(1, conns.size());

        assertThat("Olock Hash", d1.getOlockHash(), equalTo("I.hash.you.not"));
        assertThat("Storage version", d1.getStorageVersion(), equalTo(35L));
    }

    private Device createDevice(String name, String aet) {
        
        final Device device = new Device(name);
        
        Connection connection = new Connection("dicom", "localhost", 11112);
        device.addConnection(connection);

        ApplicationEntity applicationEntity = new ApplicationEntity(aet);
        applicationEntity.addConnection(connection);
        
        device.addApplicationEntity(applicationEntity);
        
        return device;
    }
}
