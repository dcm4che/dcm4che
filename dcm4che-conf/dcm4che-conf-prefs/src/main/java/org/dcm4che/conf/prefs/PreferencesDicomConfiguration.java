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

    private Preferences configurationPrefs;
    private Preferences devicePrefs;
    private Preferences aetsRegistryPrefs;
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
        if (configurationPrefs != null)
            return true;

        if (!nodeExists(rootPrefs, configurationRoot))
            return false;

        initConfigurationPrefs();
        return true;
    }

    @Override
    public boolean purgeConfiguration() throws ConfigurationException {
        if (!configurationExists())
            return false;

        try {
            configurationPrefs.removeNode();
            LOG.info("Purge DICOM Configuration at {}", configurationPrefs);
            clearConfigurationPrefs();
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
        return true;
    }

    @Override
    public boolean registerAETitle(String aet) throws ConfigurationException {
        ensureConfigurationPrefs();
        try {
            if (aetsRegistryPrefs.nodeExists(aet))
                return false;
            aetsRegistryPrefs.node(aet);
            aetsRegistryPrefs.flush();
            return true;
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public void unregisterAETitle(String aet) throws ConfigurationException {
        if (configurationExists())
            try {
                if (aetsRegistryPrefs.nodeExists(aet)) {
                    aetsRegistryPrefs.node(aet).removeNode();
                    aetsRegistryPrefs.flush();
                }
            } catch (BackingStoreException e) {
                throw new ConfigurationException(e);
            }
    }


    @Override
    public ApplicationEntity findApplicationEntity(String aet)
            throws ConfigurationException {
        if (!configurationExists())
            throw new ConfigurationNotFoundException();
        
        try {
            for (String deviceName : devicePrefs.childrenNames()) {
                Preferences deviceNode = devicePrefs.node(deviceName);
                for (String aet2 : deviceNode.node("dicomNetworkAE").childrenNames())
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
        if (!configurationExists() || !nodeExists(devicePrefs, name))
            throw new ConfigurationNotFoundException();

        return loadDevice(devicePrefs.node(name));
    }

    @Override
    public void persist(Device device) throws ConfigurationException {
        ensureConfigurationPrefs();
        String deviceName = device.getDeviceName();
        if (nodeExists(devicePrefs, deviceName))
            throw new ConfigurationAlreadyExistsException(deviceName);

        Preferences deviceNode = devicePrefs.node(deviceName);
        storeTo(device, deviceNode);
        Preferences connsNode = deviceNode.node("dicomNetworkConnection");
        int connIndex = 1;
        List<Connection> devConns = device.listConnections();
        for (Connection conn : devConns)
            storeTo(conn, connsNode.node("" + connIndex++));
        Preferences aesNode = deviceNode.node("dicomNetworkAE");
        for (ApplicationEntity ae : device.getApplicationEntities()) {
            Preferences aeNode = aesNode.node(ae.getAETitle());
            storeTo(ae, aeNode);
            int refCount = 0;
            for (Connection conn : ae.getConnections()) {
                aeNode.putInt("dicomNetworkConnectionReference." + (++refCount), 
                        devConns.indexOf(conn) + 1);
            }
            aeNode.putInt("dicomNetworkConnectionReference.#", refCount);
            Preferences tcsNode = aeNode.node("dicomTransferCapability");
            int tcIndex = 1;
            for (TransferCapability tc : ae.getTransferCapabilities()) {
                Preferences tcNode = tcsNode.node(Integer.toString(tcIndex++, 10));
                storeTo(tcNode, tc);
            }
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


    @Override
    public void merge(Device device) throws ConfigurationException {
        String deviceName = device.getDeviceName();
        if (!configurationExists() || !nodeExists(devicePrefs, deviceName))
            throw new ConfigurationNotFoundException();

        Preferences deviceNode = devicePrefs.node(deviceName);
        mergeTo(device, deviceNode);
        //TODO
    }

    @Override
    public void removeDevice(String name) throws ConfigurationException {
        if (!configurationExists() || !nodeExists(devicePrefs, name))
            throw new ConfigurationNotFoundException();

        Preferences node = devicePrefs.node(name);
        try {
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

    private void ensureConfigurationPrefs() throws ConfigurationException {
        if (configurationPrefs == null)
            initConfigurationPrefs();
    }

    private void initConfigurationPrefs() throws ConfigurationException {
        configurationPrefs = rootPrefs.node(configurationRoot);
        devicePrefs = configurationPrefs.node("dicomDevicesRoot");
        aetsRegistryPrefs = configurationPrefs.node("dicomUniqueAETitlesRegistryRoot");
        try {
            configurationPrefs.flush();
        } catch (BackingStoreException e) {
            clearConfigurationPrefs();
            throw new ConfigurationException(e);
        }
    }

    private void clearConfigurationPrefs() {
        configurationPrefs = null;
        devicePrefs = null;
        aetsRegistryPrefs = null;
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

        storeNotEmpty(prefs, "dicomBlacklistedHostname", conn.getBlacklist());
        storeNotDef(prefs, "dicomTCPBacklog",
                conn.getBacklog(), Connection.DEF_BACKLOG);
        storeNotDef(prefs, "dicomTCPConnectTimeout",
                conn.getConnectTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dicomAssociationRequestTimeout",
                conn.getRequestTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dicomAssociationAcknowledgeTimeout",
                conn.getAcceptTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dicomAssociationReleaseTimeout",
                conn.getReleaseTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dicomDIMSEResponseTimeout",
                conn.getDimseRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dicomCGetResponseTimeout",
                conn.getCGetRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dicomCMoveResponseTimeout",
                conn.getCMoveRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dicomAssociationIdleTimeout",
                conn.getIdleTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dicomTCPCloseDelay",
                conn.getSocketCloseDelay(), Connection.DEF_SOCKETDELAY);
        storeNotDef(prefs, "dicomTCPSendBufferSize",
                conn.getSendBufferSize(), Connection.DEF_BUFFERSIZE);
        storeNotDef(prefs, "dicomTCPReceiveBufferSize",
                conn.getReceiveBufferSize(), Connection.DEF_BUFFERSIZE);
        storeBoolean(prefs, "dicomTCPNoDelay", conn.isTcpNoDelay());
        storeNotEmpty(prefs, "dicomTLSProtocol", conn.getTlsProtocols());
        storeBoolean(prefs, "dicomTLSNeedClientAuth", conn.isTlsNeedClientAuth());
    }

    protected void storeTo(ApplicationEntity ae, Preferences prefs) {
        storeNotNull(prefs, "dicomDescription", ae.getDescription());
        storeNotEmpty(prefs, "dicomVendorData", ae.getVendorData());
        storeNotEmpty(prefs, "dicomApplicationCluster", ae.getApplicationClusters());
        storeNotEmpty(prefs, "dicomPreferredCallingAETitle", ae.getPreferredCallingAETitles());
        storeNotEmpty(prefs, "dicomPreferredCalledAETitle", ae.getPreferredCalledAETitles());
        storeBoolean(prefs, "dicomAssociationInitiator", ae.isAssociationInitiator());
        storeBoolean(prefs, "dicomAssociationAcceptor",  ae.isAssociationAcceptor());
        storeNotEmpty(prefs, "dicomSupportedCharacterSet", ae.getSupportedCharacterSets());
        storeBoolean(prefs, "dicomInstalled", ae.getInstalled());

        storeNotDef(prefs, "dicomSendPDULength",
                ae.getSendPDULength(), ApplicationEntity.DEF_MAX_PDU_LENGTH);
        storeNotDef(prefs, "dicomReceivePDULength",
                ae.getReceivePDULength(), ApplicationEntity.DEF_MAX_PDU_LENGTH);
        storeNotDef(prefs, "dicomMaxOpsPerformed",
                ae.getMaxOpsPerformed(), ApplicationEntity.SYNCHRONOUS_MODE);
        storeNotDef(prefs, "dicomMaxOpsInvoked",
                ae.getMaxOpsInvoked(), ApplicationEntity.SYNCHRONOUS_MODE);
        storeBoolean(prefs, "dicomPackPDV", ae.isPackPDV());
    }

    protected void storeTo(Preferences prefs, TransferCapability tc) {
        storeNotNull(prefs, "cn", tc.getCommonName());
        storeNotNull(prefs, "dicomSOPClass", tc.getSopClass());
        storeNotNull(prefs, "dicomTransferRole", tc.getRole().toString());
        storeNotEmpty(prefs, "dicomTransferSyntax", tc.getTransferSyntaxes());
    }

    private Device loadDevice(Preferences deviceNode) throws ConfigurationException {
        try {
            Device device = newDevice(deviceNode.name());
            loadFrom(device, deviceNode);
            Preferences connsNode = deviceNode.node("dicomNetworkConnection");
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
            Preferences aesNode = deviceNode.node("dicomNetworkAE");
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

    protected Connection newConnection() {
        return new Connection();
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
        conn.setBlacklist(toStrings(prefs, "dicomBlacklistedHostname"));
        conn.setBacklog(prefs.getInt("dicomTCPBacklog", Connection.DEF_BACKLOG));
        conn.setConnectTimeout(
                prefs.getInt("dicomTCPConnectTimeout", Connection.NO_TIMEOUT));
        conn.setRequestTimeout(
                prefs.getInt("dicomAssociationRequestTimeout", Connection.NO_TIMEOUT));
        conn.setAcceptTimeout(
                prefs.getInt("dicomAssociationAcknowledgeTimeout", Connection.NO_TIMEOUT));
        conn.setReleaseTimeout(
                prefs.getInt("dicomAssociationReleaseTimeout", Connection.NO_TIMEOUT));
        conn.setDimseRSPTimeout(
                prefs.getInt("dicomDIMSEResponseTimeout", Connection.NO_TIMEOUT));
        conn.setCGetRSPTimeout(
                prefs.getInt("dicomCGetResponseTimeout", Connection.NO_TIMEOUT));
        conn.setCMoveRSPTimeout(
                prefs.getInt("dicomCMoveResponseTimeout", Connection.NO_TIMEOUT));
        conn.setIdleTimeout(
                prefs.getInt("dicomAssociationIdleTimeout", Connection.NO_TIMEOUT));
        conn.setSocketCloseDelay(
                prefs.getInt("dicomTCPCloseDelay", Connection.DEF_SOCKETDELAY));
        conn.setSendBufferSize(
                prefs.getInt("dicomTCPSendBufferSize", Connection.DEF_BUFFERSIZE));
        conn.setReceiveBufferSize(
                prefs.getInt("dicomTCPReceiveBufferSize", Connection.DEF_BUFFERSIZE));
        conn.setTcpNoDelay(prefs.getBoolean("dicomTCPNoDelay", true));
        conn.setTlsNeedClientAuth(prefs.getBoolean("dicomTLSNeedClientAuth", true));
        conn.setTlsProtocols(toStrings(prefs, "dicomTLSProtocol"));
    }

    private static Boolean toBoolean(String s) {
        return s != null ? Boolean.valueOf(s) : null;
    }

    protected ApplicationEntity newApplicationEntity(String aet) {
        return new ApplicationEntity(aet);
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
    }

    protected TransferCapability newTransferCapability(Preferences prefs)
            throws BackingStoreException {
        return new TransferCapability(
                prefs.get("cn", null),
                prefs.get("dicomSOPClass", null),
                TransferCapability.Role.valueOf(prefs.get("dicomTransferRole", null)),
                toStrings(prefs, "dicomTransferSyntax"));
    }

    protected void mergeTo(Device device, Preferences prefs) {
        // TODO Auto-generated method stub
        
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
}
