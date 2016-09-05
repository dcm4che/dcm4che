/*
 *
 * ** BEGIN LICENSE BLOCK *****
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
 * Portions created by the Initial Developer are Copyright (C) 2015
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
 *
 */

package org.dcm4che3.conf.dicom;

import junit.framework.AssertionFailedError;
import org.dcm4che3.conf.core.DefaultBeanVitalizer;
import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.ConfigurableProperty.ConfigurablePropertyType;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;
import org.dcm4che3.conf.core.olock.OLockCopyFilter;
import org.dcm4che3.conf.core.olock.OLockHashCalcFilter;
import org.dcm4che3.conf.core.olock.HashBasedOptimisticLockingConfiguration;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser;
import org.dcm4che3.conf.core.Nodes;
import org.dcm4che3.net.Device;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman K
 */
public class OptimisticLockingTest extends HashBasedOptimisticLockingConfiguration {


    public OptimisticLockingTest() {
        super(null, null);
    }

    @ConfigurableClass
    public static class PartyPlan {

        @ConfigurableProperty(type = ConfigurablePropertyType.OptimisticLockingHash)
        String olock;

        @ConfigurableProperty
        String name;

        @ConfigurableProperty
        int budget;

        @ConfigurableProperty
        Map<String, Party> parties;

        public String getOlock() {
            return olock;
        }

        public void setOlock(String olock) {
            this.olock = olock;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getBudget() {
            return budget;
        }

        public void setBudget(int budget) {
            this.budget = budget;
        }

        public Map<String, Party> getParties() {
            return parties;
        }

        public void setParties(Map<String, Party> parties) {
            this.parties = parties;
        }
    }

    @ConfigurableClass
    public static class Party {
        @ConfigurableProperty(type = ConfigurablePropertyType.OptimisticLockingHash)
        String oLock;

        @ConfigurableProperty(name = "prop1")
        int guests;

        @ConfigurableProperty(name = "prop2")
        boolean outside;

        @ConfigurableProperty(name = "str")
        String occasion;

        public String getoLock() {
            return oLock;
        }

        public void setoLock(String oLock) {
            this.oLock = oLock;
        }

        public int getGuests() {
            return guests;
        }

        public void setGuests(int guests) {
            this.guests = guests;
        }

        public boolean isOutside() {
            return outside;
        }

        public void setOutside(boolean outside) {
            this.outside = outside;
        }

        public String getOccasion() {
            return occasion;
        }

        public void setOccasion(String occasion) {
            this.occasion = occasion;
        }
    }


    @Test
    public void testPartyOlock() throws IOException {
        PartyPlan partyPlan = new PartyPlan();
        partyPlan.setName("great Plan!");
        partyPlan.setBudget(10000);

        Party party = new Party();
        party.setGuests(10);
        party.setOccasion("gettogether");
        party.setOutside(true);

        Party party1 = new Party();
        party1.setGuests(5);
        party1.setOccasion("boring birthday");
        party1.setOutside(false);

        HashMap<String, Party> parties = new HashMap<String, Party>();
        parties.put("p1", party);
        parties.put("p2", party1);

        partyPlan.setParties(parties);


        BeanVitalizer beanVitalizer = new DefaultBeanVitalizer();
        Map<String, Object> oldNode = beanVitalizer.createConfigNodeFromInstance(partyPlan);


        ConfigNodeTraverser.traverseNodeTypesafe(oldNode, new ConfigProperty(PartyPlan.class), new ArrayList<Class>(), new HashMarkingTypesafeNodeFilter());
        ConfigNodeTraverser.traverseMapNode(oldNode, new OLockHashCalcFilter());

        // consistent?
        String originalHash = "HRci3v11AErGV1sHTG5fEtrS5ow=";
        Assert.assertEquals(originalHash, oldNode.get(Configuration.OLOCK_HASH_KEY));

        // remember old hash, change smth, check
        party1.setGuests(party1.getGuests() + 1);

        Map<String, Object> newNode = beanVitalizer.createConfigNodeFromInstance(partyPlan);

        ConfigNodeTraverser.traverseNodeTypesafe(newNode, new ConfigProperty(PartyPlan.class), new ArrayList<Class>(), new HashMarkingTypesafeNodeFilter());
        ConfigNodeTraverser.traverseMapNode(newNode, new OLockHashCalcFilter());

        Assert.assertNotEquals("node changed",
                Nodes.getNode(oldNode, "/parties/p2/" + Configuration.OLOCK_HASH_KEY),
                Nodes.getNode(newNode, "/parties/p2/" + Configuration.OLOCK_HASH_KEY));

        Assert.assertEquals("node not changed",
                Nodes.getNode(oldNode, "/parties/p1/" + Configuration.OLOCK_HASH_KEY),
                Nodes.getNode(newNode, "/parties/p1/" + Configuration.OLOCK_HASH_KEY));

        Assert.assertEquals("parent should not change",
                oldNode.get(Configuration.OLOCK_HASH_KEY),
                newNode.get(Configuration.OLOCK_HASH_KEY));


        // try changing map keys 
        parties.put("aNewOldParty", parties.remove("p1"));

        Map<String, Object> nodeWithMapChanged = beanVitalizer.createConfigNodeFromInstance(partyPlan);

        ConfigNodeTraverser.traverseNodeTypesafe(nodeWithMapChanged, new ConfigProperty(PartyPlan.class), new ArrayList<Class>(), new HashMarkingTypesafeNodeFilter());
        ConfigNodeTraverser.traverseMapNode(nodeWithMapChanged, new OLockHashCalcFilter());

        Assert.assertNotEquals("parent should change",
                newNode.get(Configuration.OLOCK_HASH_KEY),
                nodeWithMapChanged.get(Configuration.OLOCK_HASH_KEY));

        Assert.assertEquals("map entry hash should not change",
                Nodes.getNode(newNode, "/parties/p1/" + Configuration.OLOCK_HASH_KEY),
                Nodes.getNode(nodeWithMapChanged, "/parties/aNewOldParty/" + Configuration.OLOCK_HASH_KEY));


        // try recalc
        ConfigNodeTraverser.traverseMapNode(oldNode, new OLockHashCalcFilter());
        Assert.assertEquals(originalHash, oldNode.get(Configuration.OLOCK_HASH_KEY));

        // try calculating hashes when old hash is there

        ConfigNodeTraverser.traverseMapNode(oldNode, new OLockCopyFilter("#old_hash"));
        ConfigNodeTraverser.traverseMapNode(oldNode, new OLockHashCalcFilter("#old_hash"));
        Assert.assertEquals(originalHash, oldNode.get(Configuration.OLOCK_HASH_KEY));


    }


//    @Test
//    public void testConnectionOlockHash() throws IOException {
//
//        Configuration mockDicomConfStorage = SimpleStorageTest.getMockDicomConfStorage();
//        Map<String,Object> configurationNode1 = (Map<String, Object>) mockDicomConfStorage.getConfigurationNode(DicomPath.DeviceByNameForWrite.set("deviceName", "dcmqrscp").path(), Device.class);
//
//        new DicomNodeTraverser(new ArrayList<Class<?>>()).traverseTree(configurationNode1, Device.class, new OLockHashCalcFilter());
//
//        Assert.assertEquals("BA7C7621709912234F89C4D9BD96A3DD5FB29417", Nodes.getNode(configurationNode1,"/dicomConnection[cn='dicom']/oLock"));
//
//        // extract in '_old_olock' in node being persisted
//        new DicomNodeTraverser(new ArrayList<Class<?>>()).traverseTree(configurationNode1, Device.class, new OLockExtractingFilter("_old_olock"));
//        new DicomNodeTraverser(new ArrayList<Class<?>>()).traverseTree(configurationNode1, Device.class, new OLockHashCalcFilter());
//
//        Assert.assertEquals("BA7C7621709912234F89C4D9BD96A3DD5FB29417", Nodes.getNode(configurationNode1,"/dicomConnection[cn='dicom']/oLock"));
//
//        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(System.out, configurationNode1);
//
//    }

    @Test
    public void testDeco() throws IOException {

        Configuration mockDicomConfStorage = SimpleStorageTest.getMockDicomConfStorage();


        Configuration lockedConfig = new HashBasedOptimisticLockingConfiguration(mockDicomConfStorage, new ArrayList<Class>());

        // imitate 3 users simultaneously getting the same node

        Map<String, Object> configurationNode1 = (Map<String, Object>) lockedConfig.getConfigurationNode(DicomPath.devicePath("dcmqrscp"), Device.class);
        Map<String, Object> configurationNode2 = (Map<String, Object>) lockedConfig.getConfigurationNode(DicomPath.devicePath("dcmqrscp"), Device.class);
        Map<String, Object> configurationNode3 = (Map<String, Object>) lockedConfig.getConfigurationNode(DicomPath.devicePath("dcmqrscp"), Device.class);

        // imitate simultaneous changes

        Nodes.replacePrimitive(configurationNode1, false, new Path("dicomNetworkAE", "DCMQRSCP", "dicomAssociationAcceptor").getPathItems());
        Nodes.replacePrimitive(configurationNode2, false, new Path("dicomNetworkAE", "DCMQRSCP", "dicomAssociationInitiator").getPathItems());
        Nodes.replacePrimitive(configurationNode3, "12345", new Path("dcmKeyStorePin").getPathItems());

        // persist some changes from 1st user
        lockedConfig.persistNode(DicomPath.devicePath("dcmqrscp"), configurationNode1, Device.class);

        // persist some conflicting changes from 2nd user - should fail
        try {
            lockedConfig.persistNode(DicomPath.devicePath("dcmqrscp"), configurationNode2, Device.class);
            throw new AssertionFailedError("Should have failed!");
        } catch (Exception e) {
            // its ok
        }

        // persist some non-conflicting changes from 3rd user - should be fine
        lockedConfig.persistNode(DicomPath.devicePath("dcmqrscp"), configurationNode3, Device.class);


        // assert the changes that were supposed to be persisted
        Map<String, Object> newNode = (Map<String, Object>) lockedConfig.getConfigurationNode(DicomPath.devicePath("dcmqrscp"), Device.class);

        Assert.assertEquals(
                "12345",
                Nodes.getNode(newNode, "/dcmKeyStorePin"));

        Assert.assertEquals(
                false,
                Nodes.getNode(newNode, "/dicomNetworkAE/DCMQRSCP/dicomAssociationAcceptor"));

        Assert.assertEquals(
                true,
                Nodes.getNode(newNode, "/dicomNetworkAE/DCMQRSCP/dicomAssociationInitiator"));


    }
}
