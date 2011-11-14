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

package org.dcm4che.conf.ldap;

import static org.junit.Assert.*;

import org.dcm4che.data.UID;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.TransferCapability;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapDeviceManagerTest {

    private LdapDeviceManager deviceManager = new LdapDeviceManager();

    @Before
    public void setUp() throws Exception {
        deviceManager.setLdapURI("ldap://localhost:389/dc=nodomain");
        deviceManager.setSimpleAuthentication("cn=admin,dc=nodomain", "admin");
    }

    @Test
    public void testConnect() throws Exception {
        deviceManager.connect();
        deviceManager.close();
    }


    @Test
    public void testInitConfiguration() throws Exception {
        deviceManager.connect();
        deviceManager.initConfiguration();
        deviceManager.registerAETitle("DCM4CHEE");
        deviceManager.persist(createDevice());
        deviceManager.purgeConfiguration();
        deviceManager.close();
    }

    private Device createDevice() throws Exception {
        Device device = new Device("DCM4CHEE");
        Connection conn = new Connection();
        conn.setHostname("host.dcm4che.org");
        conn.setPort(11112);
        device.addConnection(conn);
        ApplicationEntity ae = new ApplicationEntity("DCM4CHEE");
        ae.addConnection(conn);
        ae.addTransferCapability(
                new TransferCapability(null, UID.VerificationSOPClass, TransferCapability.Role.SCU,
                        UID.ImplicitVRLittleEndian));
        device.addApplicationEntity(ae);
        return device ;
    }

}
