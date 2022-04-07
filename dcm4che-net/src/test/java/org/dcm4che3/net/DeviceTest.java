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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class DeviceTest {

    private static final String AE_TITLE = "MyAeTitle";
    private static final String FIRST_ALIAS_AE_TITLE = "AliasAeTitle";
    private static final String SECOND_ALIAS_AE_TITLE = "AnotherAliasAeTitle";
    private static final String DEFAULT_AE_TITLE = "*";

    private ApplicationEntity ae;
    private ApplicationEntity defaultAE;

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

    @Test
    public void getApplicationEntity_ReturnsSameApplicationEntity_GivenAeTitleOrAETitleAliases() {

        Device device = createDevice("AnyDeviceName", AE_TITLE);
        device.getApplicationEntity(AE_TITLE)
              .setAETitleAliases(Arrays.asList(FIRST_ALIAS_AE_TITLE, SECOND_ALIAS_AE_TITLE));

        assertThat("Main AE is not correct", device.getApplicationEntity(AE_TITLE), is(ae));
        assertThat("First alias AE of the device should be same as the main one",
                   device.getApplicationEntity(FIRST_ALIAS_AE_TITLE),
                   is(ae));
        assertThat("Second alias AE of the device should be same as the main one",
                   device.getApplicationEntity(SECOND_ALIAS_AE_TITLE),
                   is(ae));
    }

    @Test
    public void getApplicationEntity_ReturnsDefault_GivenThatDefaultExistsAndAeTitleOrAETitleAliasesDoesNotExist() {

        Device device = createDevice("AnyDeviceName", AE_TITLE);
        device.getApplicationEntity(AE_TITLE)
              .setAETitleAliases(Arrays.asList(FIRST_ALIAS_AE_TITLE, SECOND_ALIAS_AE_TITLE));

        assertThat("Default AE of the device should be returned if AE title does not exist",
                   device.getApplicationEntity("AnyAeTitleThatDoesNotExist"),
                   is(defaultAE));
    }

    @Test
    public void getApplicationAETitles_ReturnsAETitlesIncludingAliasAeTitles_WhenApplicationEntityHasAliasAeTitle() {

        Device device = createDevice("AnyDeviceName", AE_TITLE);
        device.getApplicationEntity(AE_TITLE)
              .setAETitleAliases(Arrays.asList(FIRST_ALIAS_AE_TITLE, SECOND_ALIAS_AE_TITLE));

        assertThat("Application Entity titles are not correct",
                   device.getApplicationAETitles(),
                   containsInAnyOrder(AE_TITLE, FIRST_ALIAS_AE_TITLE, SECOND_ALIAS_AE_TITLE, DEFAULT_AE_TITLE));
    }

    private Device createDevice(String name, String aet) {
        Device dev = new Device(name);
        Connection conn = new Connection("dicom", "localhost", 11112);
        dev.addConnection(conn);

        defaultAE = new ApplicationEntity("*");
        dev.addApplicationEntity(defaultAE);
        defaultAE.addConnection(conn);

        ae = new ApplicationEntity(aet);
        dev.addApplicationEntity(ae);
        ae.addConnection(conn);
        return dev;
    }

}
