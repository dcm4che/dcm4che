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

package org.dcm4che.conf.prefs;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dcm4che.conf.api.ConfigurationAlreadyExistsException;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.conf.api.ConfigurationNotFoundException;
import org.dcm4che.conf.api.DicomConfiguration;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.TransferCapability;
import org.dcm4che.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class PreferencesDicomConfiguration implements DicomConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(PreferencesDicomConfiguration.class);

    private static final String DICOM_CONFIGURATION_ROOT = "dicomConfigurationRoot";

    private final Preferences rootPrefs;

    private String configurationRoot = DICOM_CONFIGURATION_ROOT;

    public PreferencesDicomConfiguration(Preferences rootPrefs) {
        this.rootPrefs = rootPrefs;
    }

    public final void setConfigurationRoot(String configurationRoot) {
        this.configurationRoot = configurationRoot;
    }

    public final String getConfigurationRoot() {
        return configurationRoot ;
    }

    @Override
    public boolean configurationExists() throws ConfigurationException {
        return nodeExists(rootPrefs, configurationRoot);
    }

    @Override
    public boolean purgeConfiguration() throws ConfigurationException {
        if (!configurationExists())
            return false;

        try {
            Preferences node = rootPrefs.node(configurationRoot);
            node.removeNode();
            node.flush();
            LOG.info("Purge DICOM Configuration {}", node);
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
        return true;
    }

    @Override
    public boolean registerAETitle(String aet) throws ConfigurationException {
        String pathName = aetRegistryPathNameOf(aet);
        if (nodeExists(rootPrefs, pathName))
            return false;
        try {
            rootPrefs.node(pathName).flush();
            return true;
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

   @Override
    public void unregisterAETitle(String aet) throws ConfigurationException {
        String pathName = aetRegistryPathNameOf(aet);
        if (nodeExists(rootPrefs, pathName))
        try {
            Preferences node = rootPrefs.node(pathName);
            node.removeNode();
            node.flush();
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    private String aetRegistryPathNameOf(String aet) {
        return configurationRoot + "/dicomUniqueAETitlesRegistryRoot/" + aet;
    }

    private String devicePathNameOf(String name) {
        return configurationRoot + "/dicomDevicesRoot/" + name;
    }

    private String devicesPathName() {
        return configurationRoot + "/dicomDevicesRoot";
    }

    @Override
    public ApplicationEntity findApplicationEntity(String aet)
            throws ConfigurationException {
        String pathName = devicesPathName();
        if (!nodeExists(rootPrefs, pathName))
            throw new ConfigurationNotFoundException();
        
        try {
            Preferences devicePrefs = rootPrefs.node(pathName);
            for (String deviceName : devicePrefs .childrenNames()) {
                Preferences deviceNode = devicePrefs.node(deviceName);
                for (String aet2 : deviceNode.node("dcm4cheNetworkAE").childrenNames())
                    if (aet.equals(aet2))
                        return loadDevice(deviceNode).getApplicationEntity(aet);
            }
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
        throw new ConfigurationNotFoundException();
    }

    @Override
    public Device findDevice(String name) throws ConfigurationException {
        String pathName = devicePathNameOf(name);
        if (!nodeExists(rootPrefs, pathName))
            throw new ConfigurationNotFoundException();

        return loadDevice(rootPrefs.node(pathName));
    }

    @Override
    public void persist(Device device) throws ConfigurationException {
        String deviceName = device.getDeviceName();
        String pathName = devicePathNameOf(deviceName);
        if (nodeExists(rootPrefs, pathName))
            throw new ConfigurationAlreadyExistsException(pathName);

        Preferences deviceNode = rootPrefs.node(pathName);
        storeTo(device, deviceNode);
        Preferences connsNode = deviceNode.node("dcm4cheNetworkConnection");
        int connIndex = 1;
        List<Connection> devConns = device.listConnections();
        for (Connection conn : devConns)
            storeTo(conn, connsNode.node("" + connIndex++));
        Preferences aesNode = deviceNode.node("dcm4cheNetworkAE");
        for (ApplicationEntity ae : device.getApplicationEntities()) {
            Preferences aeNode = aesNode.node(ae.getAETitle());
            storeTo(ae, aeNode, devConns);
            storeTransferCapabilities(ae, aeNode);
        }
        try {
            deviceNode.flush();
            deviceNode = null;
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        } finally {
            if (deviceNode != null)
                try {
                    deviceNode.removeNode();
                    deviceNode.flush();
                } catch (BackingStoreException e) {
                    LOG.warn("Rollback failed:", e);
                }
        }
    }

    private void storeTransferCapabilities(ApplicationEntity ae,
            Preferences aeNode) {
        Preferences tcsNode = aeNode.node("dicomTransferCapability");
        int tcIndex = 1;
        for (TransferCapability tc : ae.getTransferCapabilities()) {
            Preferences tcNode = tcsNode.node("" + tcIndex++);
            storeTo(tcNode, tc);
        }
    }


    @Override
    public void merge(Device device) throws ConfigurationException {
        String pathName = devicePathNameOf(device.getDeviceName());
        if (!nodeExists(rootPrefs, pathName))
            throw new ConfigurationNotFoundException();
        
        Preferences devicePrefs = rootPrefs.node(pathName);
        Device prev = loadDevice(devicePrefs);
        try {
            storeDiffs(devicePrefs, prev, device);
            mergeConnections(prev, device, devicePrefs);
            mergeAEs(prev, device, devicePrefs);
            devicePrefs.flush();
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public void removeDevice(String name) throws ConfigurationException {
        String pathName = devicePathNameOf(name);
        if (!nodeExists(rootPrefs, pathName))
            throw new ConfigurationNotFoundException();

        try {
            Preferences node = rootPrefs.node(pathName);
            node.removeNode();
            node.flush();
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    private static boolean nodeExists(Preferences prefs, String pathName)
            throws ConfigurationException {
        try {
            return prefs.nodeExists(pathName);
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    protected void storeTo(Device device, Preferences prefs) {
        storeNotNull(prefs, "dicomDescription", device.getDescription());
        storeNotNull(prefs, "dicomManufacturer", device.getManufacturer());
        storeNotNull(prefs, "dicomManufacturerModelName", device.getManufacturerModelName());
        storeNotEmpty(prefs, "dicomSoftwareVersion", device.getSoftwareVersion());
        storeNotNull(prefs, "dicomStationName", device.getStationName());
        storeNotNull(prefs, "dicomDeviceSerialNumber", device.getDeviceSerialNumber());
        storeNotNull(prefs, "dicomIssuerOfPatientID", device.getIssuerOfPatientID());
        storeNotEmpty(prefs, "dicomInstitutionName",device.getInstitutionNames());
        storeNotEmpty(prefs, "dicomInstitutionAddress",device.getInstitutionAddresses());
        storeNotEmpty(prefs, "dicomInstitutionalDepartmentName",
                device.getInstitutionalDepartmentNames());
        storeNotEmpty(prefs, "dicomPrimaryDeviceType", device.getPrimaryDeviceTypes());
        storeNotEmpty(prefs, "dicomRelatedDeviceReference", device.getRelatedDeviceRefs());
        storeNotEmpty(prefs, "dicomAuthorizedNodeCertificateReference",
                device.getAuthorizedNodeCertificateRefs());
        storeNotEmpty(prefs, "dicomThisNodeCertificateReference",
                device.getThisNodeCertificateRefs());
        storeNotEmpty(prefs, "dicomVendorData", device.getVendorData());
        prefs.putBoolean("dicomInstalled", device.isInstalled());
    }

    protected void storeTo(Connection conn, Preferences prefs) {
        storeNotNull(prefs, "cn", conn.getCommonName());
        storeNotNull(prefs, "dicomHostname", conn.getHostname());
        storeNotDef(prefs, "dicomPort", conn.getPort(), Connection.NOT_LISTENING);
        storeNotEmpty(prefs, "dicomTLSCipherSuite", conn.getTlsCipherSuites());
        storeBoolean(prefs, "dicomInstalled", conn.getInstalled());

        storeNotEmpty(prefs, "dcm4cheBlacklistedHostname", conn.getBlacklist());
        storeNotDef(prefs, "dcm4cheTCPBacklog",
                conn.getBacklog(), Connection.DEF_BACKLOG);
        storeNotDef(prefs, "dcm4cheTCPConnectTimeout",
                conn.getConnectTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcm4cheAssociationRequestTimeout",
                conn.getRequestTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcm4cheAssociationAcknowledgeTimeout",
                conn.getAcceptTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcm4cheAssociationReleaseTimeout",
                conn.getReleaseTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcm4cheDIMSEResponseTimeout",
                conn.getDimseRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcm4cheCGetResponseTimeout",
                conn.getCGetRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcm4cheCMoveResponseTimeout",
                conn.getCMoveRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcm4cheAssociationIdleTimeout",
                conn.getIdleTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcm4cheTCPCloseDelay",
                conn.getSocketCloseDelay(), Connection.DEF_SOCKETDELAY);
        storeNotDef(prefs, "dcm4cheTCPSendBufferSize",
                conn.getSendBufferSize(), Connection.DEF_BUFFERSIZE);
        storeNotDef(prefs, "dcm4cheTCPReceiveBufferSize",
                conn.getReceiveBufferSize(), Connection.DEF_BUFFERSIZE);
        storeBoolean(prefs, "dcm4cheTCPNoDelay", conn.isTcpNoDelay());
        storeNotEmpty(prefs, "dcm4cheTLSProtocol", conn.getTlsProtocols());
        storeBoolean(prefs, "dcm4cheTLSNeedClientAuth", conn.isTlsNeedClientAuth());
    }

    protected void storeTo(ApplicationEntity ae, Preferences prefs, List<Connection> devConns) {
        storeNotNull(prefs, "dicomDescription", ae.getDescription());
        storeNotEmpty(prefs, "dicomVendorData", ae.getVendorData());
        storeNotEmpty(prefs, "dicomApplicationCluster", ae.getApplicationClusters());
        storeNotEmpty(prefs, "dicomPreferredCallingAETitle", ae.getPreferredCallingAETitles());
        storeNotEmpty(prefs, "dicomPreferredCalledAETitle", ae.getPreferredCalledAETitles());
        storeBoolean(prefs, "dicomAssociationInitiator", ae.isAssociationInitiator());
        storeBoolean(prefs, "dicomAssociationAcceptor",  ae.isAssociationAcceptor());
        storeNotEmpty(prefs, "dicomSupportedCharacterSet", ae.getSupportedCharacterSets());
        storeBoolean(prefs, "dicomInstalled", ae.getInstalled());
        storeConnRefs(prefs, ae, devConns);

        storeNotDef(prefs, "dcm4cheSendPDULength",
                ae.getSendPDULength(), ApplicationEntity.DEF_MAX_PDU_LENGTH);
        storeNotDef(prefs, "dcm4cheReceivePDULength",
                ae.getReceivePDULength(), ApplicationEntity.DEF_MAX_PDU_LENGTH);
        storeNotDef(prefs, "dcm4cheMaxOpsPerformed",
                ae.getMaxOpsPerformed(), ApplicationEntity.SYNCHRONOUS_MODE);
        storeNotDef(prefs, "dcm4cheMaxOpsInvoked",
                ae.getMaxOpsInvoked(), ApplicationEntity.SYNCHRONOUS_MODE);
        storeBoolean(prefs, "dcm4chePackPDV", ae.isPackPDV());
        storeBoolean(prefs, "dcm4cheAcceptOnlyPreferredCallingAETitle",
                ae.isAcceptOnlyPreferredCallingAETitles());
    }

    private void storeConnRefs(Preferences prefs, ApplicationEntity ae,
            List<Connection> devConns) {
        int refCount = 0;
        for (Connection conn : ae.getConnections()) {
            prefs.putInt("dicomNetworkConnectionReference." + (++refCount), 
                    devConns.indexOf(conn) + 1);
        }
        prefs.putInt("dicomNetworkConnectionReference.#", refCount);
    }

    protected void storeTo(Preferences prefs, TransferCapability tc) {
        storeNotNull(prefs, "cn", tc.getCommonName());
        storeNotNull(prefs, "dicomSOPClass", tc.getSopClass());
        storeNotNull(prefs, "dicomTransferRole", tc.getRole().toString());
        storeNotEmpty(prefs, "dicomTransferSyntax", tc.getTransferSyntaxes());
    }

    protected void storeDiffs(Preferences prefs, Device a, Device b) {
        storeDiff(prefs, "dicomDescription",
                a.getDescription(),
                b.getDescription());
        storeDiff(prefs, "dicomManufacturer",
                a.getManufacturer(),
                b.getManufacturer());
        storeDiff(prefs, "dicomManufacturerModelName",
                a.getManufacturerModelName(),
                b.getManufacturerModelName());
        storeDiff(prefs, "dicomSoftwareVersion",
                a.getSoftwareVersion(),
                b.getSoftwareVersion());
        storeDiff(prefs, "dicomStationName",
                a.getStationName(),
                b.getStationName());
        storeDiff(prefs, "dicomDeviceSerialNumber",
                a.getDeviceSerialNumber(),
                b.getDeviceSerialNumber());
        storeDiff(prefs, "dicomIssuerOfPatientID",
                a.getIssuerOfPatientID(),
                b.getIssuerOfPatientID());
        storeDiff(prefs, "dicomInstitutionName",
                a.getInstitutionNames(),
                b.getInstitutionNames());
        storeDiff(prefs, "dicomInstitutionAddress",
                a.getInstitutionAddresses(),
                b.getInstitutionAddresses());
        storeDiff(prefs, "dicomInstitutionalDepartmentName",
                a.getInstitutionalDepartmentNames(),
                b.getInstitutionalDepartmentNames());
        storeDiff(prefs, "dicomPrimaryDeviceType",
                a.getPrimaryDeviceTypes(),
                b.getPrimaryDeviceTypes());
        storeDiff(prefs, "dicomRelatedDeviceReference",
                a.getRelatedDeviceRefs(),
                b.getRelatedDeviceRefs());
        storeDiff(prefs, "dicomAuthorizedNodeCertificateReference",
                a.getAuthorizedNodeCertificateRefs(),
                b.getAuthorizedNodeCertificateRefs());
        storeDiff(prefs, "dicomThisNodeCertificateReference",
                a.getThisNodeCertificateRefs(),
                b.getThisNodeCertificateRefs());
        storeDiff(prefs, "dicomVendorData",
                a.getVendorData(),
                b.getVendorData());
        storeDiff(prefs, "dicomInstalled",
                a.isInstalled(),
                b.isInstalled());
    }

    protected void storeDiffs(Preferences prefs, Connection a, Connection b) {
        storeDiff(prefs, "cn",
                a.getCommonName(),
                b.getCommonName());
        storeDiff(prefs, "dicomHostname",
                a.getHostname(),
                b.getHostname());
        storeDiff(prefs, "dicomPort",
                a.getPort(),
                b.getPort(),
                Connection.NOT_LISTENING);
        storeDiff(prefs, "dicomTLSCipherSuite",
                a.getTlsCipherSuites(),
                b.getTlsCipherSuites());
        storeDiff(prefs, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled());
    }

    protected void storeDiffs(Preferences prefs, ApplicationEntity a, ApplicationEntity b) {
        storeDiff(prefs, "dicomDescription",
                a.getDescription(),
                b.getDescription());
        storeDiff(prefs, "dicomVendorData",
                a.getVendorData(),
                b.getVendorData());
        storeDiff(prefs, "dicomApplicationCluster",
                a.getApplicationClusters(),
                b.getApplicationClusters());
        storeDiff(prefs, "dicomPreferredCallingAETitle",
                a.getPreferredCallingAETitles(),
                b.getPreferredCallingAETitles());
        storeDiff(prefs, "dicomPreferredCalledAETitle",
                a.getPreferredCalledAETitles(),
                b.getPreferredCalledAETitles());
        storeDiff(prefs, "dicomAssociationInitiator",
                a.isAssociationInitiator(),
                b.isAssociationInitiator());
        storeDiff(prefs, "dicomAssociationAcceptor",
                a.isAssociationAcceptor(),
                b.isAssociationAcceptor());
        storeDiffConnRefs(prefs, a, b);
        storeDiff(prefs, "dicomSupportedCharacterSet",
                a.getSupportedCharacterSets(),
                b.getSupportedCharacterSets());
        storeDiff(prefs, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled());

        storeDiff(prefs, "dcm4cheSendPDULength",
                a.getSendPDULength(),
                b.getSendPDULength(),
                ApplicationEntity.DEF_MAX_PDU_LENGTH);
        storeDiff(prefs, "dcm4cheReceivePDULength",
                a.getReceivePDULength(),
                b.getReceivePDULength(),
                ApplicationEntity.DEF_MAX_PDU_LENGTH);
        storeDiff(prefs, "dcm4cheMaxOpsPerformed",
                a.getMaxOpsPerformed(),
                b.getMaxOpsPerformed(),
                ApplicationEntity.SYNCHRONOUS_MODE);
        storeDiff(prefs, "dcm4cheMaxOpsInvoked",
                a.getMaxOpsInvoked(),
                b.getMaxOpsInvoked(),
                ApplicationEntity.SYNCHRONOUS_MODE);
        storeDiff(prefs, "dcm4chePackPDV",
                a.isPackPDV(),
                b.isPackPDV());
        storeDiff(prefs, "dcm4cheAcceptOnlyPreferredCallingAETitle",
                a.isAcceptOnlyPreferredCallingAETitles(),
                b.isAcceptOnlyPreferredCallingAETitles());

    }

    protected void storeDiffs(Preferences prefs,
            TransferCapability a, TransferCapability b) {
        storeDiff(prefs, "dicomSOPClass",
                a.getSopClass(),
                b.getSopClass());
        storeDiff(prefs, "dicomTransferRole",
                a.getRole().toString(),
                b.getRole().toString());
        storeDiff(prefs, "dicomTransferSyntax",
                a.getTransferSyntaxes(),
                b.getTransferSyntaxes());
    }

    private static void storeDiffConnRefs(Preferences prefs,
            ApplicationEntity a, ApplicationEntity b) {
        List<Connection> prevDevConns = a.getDevice().listConnections();
        List<Connection> prevConns = a.getConnections();
        int prevSize = prevConns.size();
        List<Connection> devConns = b.getDevice().listConnections();
        List<Connection> conns = b.getConnections();
        int size = conns.size();
        removeKeys(prefs, "dicomNetworkConnectionReference", size, prevSize);
        for (int i = 0; i < size; i++) {
            int ref = devConns.indexOf(conns.get(i));
            if (i >= prevSize || ref != prevDevConns.indexOf(prevConns.get(i)))
                prefs.putInt("dicomNetworkConnectionReference." + (i + 1), ref + 1);
        }
        if (prevSize != size && size != 0)
            prefs.putInt("dicomNetworkConnectionReference.#", size);
    }

    private void mergeConnections(Device prevDev, Device device, Preferences deviceNode)
            throws BackingStoreException {
        Preferences connsNode = deviceNode.node("dcm4cheNetworkConnection");
        List<Connection> prevs = prevDev.listConnections();
        List<Connection> conns = device.listConnections();
        int prevsSize = prevs.size();
        int connsSize = conns.size();
        int i = 0;
        for (int n = Math.min(prevsSize, connsSize); i < n; ++i)
            storeDiffs(connsNode.node("" + (i+1)), prevs.get(i), conns.get(i));
        for (; i < prevsSize; ++i)
            connsNode.node("" + (i+1)).removeNode();
        for (; i < connsSize; ++i)
            storeTo(conns.get(i), connsNode.node("" + (i+1)));
    }

    private void mergeAEs(Device prevDev, Device dev, Preferences deviceNode)
            throws BackingStoreException {
        Preferences aesNode = deviceNode.node("dcm4cheNetworkAE");
        for (ApplicationEntity ae : prevDev.getApplicationEntities()) {
            String aet = ae.getAETitle();
            if (dev.getApplicationEntity(aet) == null)
                aesNode.node(aet).removeNode();
        }
        List<Connection> devConns = dev.listConnections();
        for (ApplicationEntity ae : dev.getApplicationEntities()) {
            String aet = ae.getAETitle();
            ApplicationEntity prevAE = prevDev.getApplicationEntity(aet);
            Preferences aeNode = aesNode.node(aet);
            if (prevAE == null) {
                storeTo(ae, aeNode, devConns);
                storeTransferCapabilities(ae, aeNode);
            } else {
                storeDiffs(aeNode, prevAE, ae);
                merge(prevAE.getTransferCapabilities(), ae.getTransferCapabilities(), aeNode);
            }
        }
    }

    private void merge(Collection<TransferCapability> prevs,
            Collection<TransferCapability> tcs, Preferences aeNode) {
        Preferences tcsNode = aeNode.node("dicomTransferCapability");
        int tcIndex = 1;
        Iterator<TransferCapability> prevIter = prevs.iterator();
        for (TransferCapability tc : tcs) {
            Preferences tcNode = tcsNode.node("" + tcIndex++);
            if (prevIter.hasNext())
                storeDiffs(tcNode, prevIter.next(), tc);
            else
                storeTo(tcNode, tc);
        }
    }

    private Device loadDevice(Preferences deviceNode) throws ConfigurationException {
        try {
            Device device = newDevice(deviceNode.name());
            loadFrom(device, deviceNode);
            Preferences connsNode = deviceNode.node("dcm4cheNetworkConnection");
            for (int connIndex : sort(connsNode.childrenNames())) {
                Connection conn = newConnection();
                loadFrom(conn, connsNode.node("" + connIndex));
                try {
                    device.addConnection(conn);
                } catch (IOException e) {
                    throw new AssertionError(e.getMessage());
                }
            }
            List<Connection> devConns = device.listConnections();
            Preferences aesNode = deviceNode.node("dcm4cheNetworkAE");
            for (String aet : aesNode.childrenNames()) {
                Preferences aeNode = aesNode.node(aet);
                ApplicationEntity ae = newApplicationEntity(aet);
                loadFrom(ae, aeNode);
                int n = aeNode.getInt("dicomNetworkConnectionReference.#", 0);
                for (int i = 0; i < n; i++) {
                    ae.addConnection(devConns.get(
                            aeNode.getInt("dicomNetworkConnectionReference." + (i+1), 0) - 1));
                }
                Preferences tcsNode = aeNode.node("dicomTransferCapability");
                for (int tcIndex : sort(tcsNode.childrenNames()))
                    ae.addTransferCapability(newTransferCapability(tcsNode.node("" + tcIndex)));
                device.addApplicationEntity(ae);
            }
            return device;
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    private static int[] sort(String[] ss) {
        int[] a = new int[ss.length];
        for (int i = 0; i < a.length; i++)
            a[i] = Integer.parseInt(ss[i]);
        Arrays.sort(a);
        return a;
    }

    protected Device newDevice(String name) {
        return new Device(name);
    }

    protected Connection newConnection() {
        return new Connection();
    }

    protected ApplicationEntity newApplicationEntity(String aet) {
        return new ApplicationEntity(aet);
    }

    protected TransferCapability newTransferCapability(Preferences prefs)
            throws BackingStoreException {
        return new TransferCapability(
                prefs.get("cn", null),
                prefs.get("dicomSOPClass", null),
                TransferCapability.Role.valueOf(prefs.get("dicomTransferRole", null)),
                toStrings(prefs, "dicomTransferSyntax"));
    }

    protected void loadFrom(Device device, Preferences attrs) throws BackingStoreException {
        device.setDescription(attrs.get("dicomDescription", null));
        device.setManufacturer(attrs.get("dicomManufacturer", null));
        device.setManufacturerModelName(attrs.get("dicomManufacturerModelName", null));
        device.setSoftwareVersions(toStrings(attrs, "dicomSoftwareVersion"));
        device.setStationName(attrs.get("dicomStationName", null));
        device.setDeviceSerialNumber(attrs.get("dicomDeviceSerialNumber", null));
        device.setIssuerOfPatientID(attrs.get("dicomIssuerOfPatientID", null));
        device.setInstitutionNames(toStrings(attrs, "dicomInstitutionName"));
        device.setInstitutionAddresses(toStrings(attrs, "dicomInstitutionAddress"));
        device.setInstitutionalDepartmentNames(
                toStrings(attrs, "dicomInstitutionalDepartmentName"));
        device.setPrimaryDeviceTypes(toStrings(attrs, "dicomPrimaryDeviceType"));
        device.setRelatedDeviceRefs(toStrings(attrs, "dicomRelatedDeviceReference"));
        device.setAuthorizedNodeCertificateRefs(
                toStrings(attrs, "dicomAuthorizedNodeCertificateReference"));
        device.setThisNodeCertificateRefs(
                toStrings(attrs, "dicomThisNodeCertificateReference"));
        device.setVendorData(toVendorData(attrs, "dicomVendorData"));
        try {
            device.setInstalled(attrs.getBoolean("dicomInstalled", false));
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
    }

    protected void loadFrom(Connection conn, Preferences prefs) throws BackingStoreException {
        conn.setCommonName(prefs.get("cn", null));
        conn.setHostname(prefs.get("dicomHostname", null));
        conn.setPort(prefs.getInt("dicomPort", Connection.NOT_LISTENING));
        conn.setTlsCipherSuites(toStrings(prefs, "dicomTLSCipherSuite"));
        try {
            conn.setInstalled(toBoolean(prefs.get("dicomInstalled", null)));
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
        conn.setBlacklist(toStrings(prefs, "dcm4cheBlacklistedHostname"));
        conn.setBacklog(prefs.getInt("dcm4cheTCPBacklog", Connection.DEF_BACKLOG));
        conn.setConnectTimeout(
                prefs.getInt("dcm4cheTCPConnectTimeout", Connection.NO_TIMEOUT));
        conn.setRequestTimeout(
                prefs.getInt("dcm4cheAssociationRequestTimeout", Connection.NO_TIMEOUT));
        conn.setAcceptTimeout(
                prefs.getInt("dcm4cheAssociationAcknowledgeTimeout", Connection.NO_TIMEOUT));
        conn.setReleaseTimeout(
                prefs.getInt("dcm4cheAssociationReleaseTimeout", Connection.NO_TIMEOUT));
        conn.setDimseRSPTimeout(
                prefs.getInt("dcm4cheDIMSEResponseTimeout", Connection.NO_TIMEOUT));
        conn.setCGetRSPTimeout(
                prefs.getInt("dcm4cheCGetResponseTimeout", Connection.NO_TIMEOUT));
        conn.setCMoveRSPTimeout(
                prefs.getInt("dcm4cheCMoveResponseTimeout", Connection.NO_TIMEOUT));
        conn.setIdleTimeout(
                prefs.getInt("dcm4cheAssociationIdleTimeout", Connection.NO_TIMEOUT));
        conn.setSocketCloseDelay(
                prefs.getInt("dcm4cheTCPCloseDelay", Connection.DEF_SOCKETDELAY));
        conn.setSendBufferSize(
                prefs.getInt("dcm4cheTCPSendBufferSize", Connection.DEF_BUFFERSIZE));
        conn.setReceiveBufferSize(
                prefs.getInt("dcm4cheTCPReceiveBufferSize", Connection.DEF_BUFFERSIZE));
        conn.setTcpNoDelay(prefs.getBoolean("dcm4cheTCPNoDelay", true));
        conn.setTlsNeedClientAuth(prefs.getBoolean("dcm4cheTLSNeedClientAuth", true));
        conn.setTlsProtocols(toStrings(prefs, "dcm4cheTLSProtocol"));
    }

    private static Boolean toBoolean(String s) {
        return s != null ? Boolean.valueOf(s) : null;
    }

    protected void loadFrom(ApplicationEntity ae, Preferences prefs)
            throws BackingStoreException {
        ae.setDescription(prefs.get("dicomDescription", null));
        ae.setVendorData(toVendorData(prefs, "dicomVendorData"));
        ae.setApplicationClusters(toStrings(prefs, "dicomApplicationCluster"));
        ae.setPreferredCallingAETitles(toStrings(prefs, "dicomPreferredCallingAETitle"));
        ae.setPreferredCalledAETitles(toStrings(prefs, "dicomPreferredCalledAETitle"));
        ae.setAssociationInitiator(prefs.getBoolean("dicomAssociationInitiator", false));
        ae.setAssociationAcceptor(prefs.getBoolean("dicomAssociationAcceptor", false));
        ae.setSupportedCharacterSets(toStrings(prefs, "dicomSupportedCharacterSet"));
        ae.setInstalled(toBoolean(prefs.get("dicomInstalled", null)));

        ae.setSendPDULength(prefs.getInt("dcm4cheSendPDULength",
                ApplicationEntity.DEF_MAX_PDU_LENGTH));
        ae.setReceivePDULength(prefs.getInt("dcm4cheReceivePDULength",
                ApplicationEntity.DEF_MAX_PDU_LENGTH));
        ae.setMaxOpsPerformed(prefs.getInt("dcm4cheMaxOpsPerformed",
                ApplicationEntity.SYNCHRONOUS_MODE));
        ae.setMaxOpsInvoked(prefs.getInt("dcm4cheMaxOpsInvoked",
                ApplicationEntity.SYNCHRONOUS_MODE));
        ae.setPackPDV(prefs.getBoolean("dcm4chePackPDV", true));
        ae.setAcceptOnlyPreferredCallingAETitles(
                prefs.getBoolean("dcm4cheAcceptOnlyPreferredCallingAETitle", false));
}

    private static byte[][] toVendorData(Preferences prefs, String key)
           throws BackingStoreException {
       int n = prefs.getInt(key + ".#", 0);
       byte[][] bb = new byte[n][];
       for (int i = 0; i < n; i++)
           bb[i] = prefs.getByteArray(key + '.' + (i+1), null);
       return bb;
    }

    private static String[] toStrings(Preferences prefs, String key)
            throws BackingStoreException {
        int n = prefs.getInt(key + ".#", 0);
        if (n == 0)
            return StringUtils.EMPTY_STRING;
        
        String[] ss = new String[n];
        for (int i = 0; i < n; i++)
            ss[i] = prefs.get(key + '.' + (i+1), null);
        return ss;
    }

    protected static void storeBoolean(Preferences prefs, String key, Boolean value) {
        if (value != null)
            prefs.putBoolean(key, value);
    }

    private void storeNotDef(Preferences prefs, String key, int value, int defVal) {
        if (value != defVal)
            prefs.putInt(key, value);
    }

    protected static void storeNotNull(Preferences prefs, String key, String value) {
        if (value != null)
            prefs.put(key, value);
    }

    protected static void storeNotEmpty(Preferences prefs, String key, String[] values) {
        if (values.length != 0) {
            int count = 0;
            for (String value : values)
                prefs.put(key + '.' + (++count), value);
            prefs.putInt(key + ".#", count);
        }
    }

    protected static void storeNotEmpty(Preferences prefs, String key, byte[][] values) {
        if (values.length != 0) {
            int count = 0;
            for (byte[] value : values)
                prefs.putByteArray(key + '.' + (++count), value);
            prefs.putInt(key + ".#", count);
        }
    }

    protected static void storeDiff(Preferences prefs, String key,
            String prev, String val) {
        if (val == null) {
            if (prev != null)
                prefs.remove(key);
        } else if (!val.equals(prev))
            prefs.put(key, val);
    }

    protected static void storeDiff(Preferences prefs, String key,
            boolean prev, boolean val) {
        if (prev != val)
            prefs.putBoolean(key, val);
    }

    protected static void storeDiff(Preferences prefs, String key,
            Boolean prev, Boolean val) {
        if (val == null) {
            if (prev != null)
                prefs.remove(key);
        } else if (!val.equals(prev))
            prefs.putBoolean(key, val);
    }

    protected static void storeDiff(Preferences prefs, String key, int prev, int val, int defVal) {
        if (prev != val)
            if (val == defVal)
                prefs.remove(key);
            else
                prefs.putInt(key, val);
     }

    protected static void storeDiff(Preferences prefs, String key,
            String[] prevs, String[] vals) {
        if (!Arrays.equals(prevs, vals)) {
            removeKeys(prefs, key, vals.length, prevs.length);
            storeNotEmpty(prefs, key, vals);
        }
    }

    protected static void storeDiff(Preferences prefs, String key,
            byte[][] prevs, byte[][] vals) {
        if (!equals(prevs, vals)) {
            removeKeys(prefs, key, vals.length, prevs.length);
            storeNotEmpty(prefs, key, vals);
        }
    }

    private static void removeKeys(Preferences prefs, String key, int from, int to) {
        for (int i = from; i < to;) 
            prefs.remove(key + '.' + (++i));
        if (from == 0)
            prefs.remove(key + ".#");
    }

    private static boolean equals(byte[][] bb1, byte[][] bb2) {
        if (bb1.length != bb2.length)
            return false;
        
        for (int i = 0; i < bb1.length; i++)
            if (!Arrays.equals(bb1[i], bb2[i]))
                return false;

        return true;
    }

}
