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

package org.dcm4che3.conf.prefs;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dcm4che3.data.UID;
import org.dcm4che3.conf.api.AttributeCoercion;
import org.dcm4che3.conf.api.AttributeCoercions;
import org.dcm4che3.conf.api.ConfigurationAlreadyExistsException;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceInfo;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.StorageOptions;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.Connection.Protocol;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public final class PreferencesDicomConfiguration implements DicomConfiguration {

    private static final String DICOM_CONFIGURATION_ROOT =
            "dicomConfigurationRoot";
    private static final String DICOM_DEVICES_ROOT = 
            "dicomConfigurationRoot/dicomDevicesRoot";
    private static final String DICOM_UNIQUE_AE_TITLES_REGISTRY_ROOT =
            "dicomConfigurationRoot/dicomUniqueAETitlesRegistryRoot";
    private static final String CONF_ROOT_PROPERTY = 
            "org.dcm4che.conf.prefs.configurationRoot";
    private static final String USER_CERTIFICATE = "userCertificate";
    private static final X509Certificate[] EMPTY_X509_CERTIFICATES = {};
    private static final Code[] EMPTY_CODES = {};

    private static final Logger LOG = LoggerFactory.getLogger(
            PreferencesDicomConfiguration.class);

    private final Preferences rootPrefs;
    private final List<PreferencesDicomConfigurationExtension> extensions =
            new ArrayList<PreferencesDicomConfigurationExtension>();

    public PreferencesDicomConfiguration() {
        this(rootPrefs());
    }

    private static Preferences rootPrefs() {
        Preferences prefs = Preferences.userRoot();
        String pathName = System.getProperty(CONF_ROOT_PROPERTY);
        return pathName != null ? prefs.node(pathName) : prefs;
    }

    public PreferencesDicomConfiguration(Preferences rootPrefs) {
        this.rootPrefs = rootPrefs;
    }

    public final Preferences getRootPrefs() {
        return rootPrefs;
    }

    public void addDicomConfigurationExtension(PreferencesDicomConfigurationExtension ext) {
        ext.setDicomConfiguration(this);
        extensions.add(ext);
    }

    public boolean removeDicomConfigurationExtension(
            PreferencesDicomConfigurationExtension ext) {
        if (!extensions.remove(ext))
            return false;

        ext.setDicomConfiguration(null);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getDicomConfigurationExtension(Class<T> clazz) {
        for (PreferencesDicomConfigurationExtension ext : extensions) {
            if (clazz.isInstance(ext.getClass()))
                return (T) ext;
        }
        return null;
    }

    @Override
    public boolean configurationExists() throws ConfigurationException {
        return PreferencesUtils.nodeExists(rootPrefs, DICOM_CONFIGURATION_ROOT);
    }

    public Preferences getDicomConfigurationRoot() throws ConfigurationException {
        if (!PreferencesUtils.nodeExists(rootPrefs, DICOM_CONFIGURATION_ROOT))
            throw new ConfigurationNotFoundException();

        return rootPrefs.node(DICOM_CONFIGURATION_ROOT);
    }

    @Override
    public synchronized boolean purgeConfiguration() throws ConfigurationException {
        if (!configurationExists())
            return false;

        try {
            Preferences node = rootPrefs.node(DICOM_CONFIGURATION_ROOT);
            node.removeNode();
            node.flush();
            LOG.info("Purge DICOM Configuration {}", node);
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
        return true;
    }

    @Override
    public synchronized boolean registerAETitle(String aet) throws ConfigurationException {
        String pathName = aetRegistryPathNameOf(aet);
        if (PreferencesUtils.nodeExists(rootPrefs, pathName))
            return false;
        try {
            rootPrefs.node(pathName).flush();
            return true;
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public synchronized void unregisterAETitle(String aet)
            throws ConfigurationException {
        PreferencesUtils.removeNode(rootPrefs, aetRegistryPathNameOf(aet));
    }

    private String aetRegistryPathNameOf(String aet) {
        return DICOM_UNIQUE_AE_TITLES_REGISTRY_ROOT + '/' + aet;
    }

    @Override
    public String deviceRef(String name) {
        return DICOM_DEVICES_ROOT + '/' + name;
    }

    @Override
    public synchronized ApplicationEntity findApplicationEntity(String aet)
            throws ConfigurationException {
        return findDevice("dcmNetworkAE", aet).getApplicationEntity(aet);
    }

    public synchronized Device findDevice(String nodeName, String aet)
            throws ConfigurationException {
         if (!PreferencesUtils.nodeExists(rootPrefs, DICOM_DEVICES_ROOT))
            throw new ConfigurationNotFoundException();
        
        try {
            Preferences devicePrefs = rootPrefs.node(DICOM_DEVICES_ROOT);
            for (String deviceName : devicePrefs.childrenNames()) {
                Preferences deviceNode = devicePrefs.node(deviceName);
                for (String aet2 : deviceNode.node(nodeName).childrenNames())
                    if (aet.equals(aet2))
                        return loadDevice(deviceNode);
            }
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
        throw new ConfigurationNotFoundException(aet);
    }

    @Override
    public synchronized Device findDevice(String name) throws ConfigurationException {
        return loadDevice(deviceRef(name));
    }


    @Override
    public DeviceInfo[] listDeviceInfos(DeviceInfo keys)
            throws ConfigurationException {
        if (!PreferencesUtils.nodeExists(rootPrefs, DICOM_DEVICES_ROOT))
            return new DeviceInfo[0];

        ArrayList<DeviceInfo> results = new ArrayList<DeviceInfo>();
        try {
            Preferences devicePrefs = rootPrefs.node(DICOM_DEVICES_ROOT);
            for (String deviceName : devicePrefs.childrenNames()) {
                Preferences deviceNode = devicePrefs.node(deviceName);
                DeviceInfo deviceInfo = new DeviceInfo();
                deviceInfo.setDeviceName(deviceName);
                loadFrom(deviceInfo, deviceNode);
                if (match(deviceInfo, keys))
                    results.add(deviceInfo);
            }
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
        return results.toArray(new DeviceInfo[results.size()]) ;
    }

    private boolean match(DeviceInfo deviceInfo, DeviceInfo keys) {
        if (keys == null)
            return true;

        return StringUtils.matches(
                deviceInfo.getDeviceName(),
                keys.getDeviceName(),
                true, true)
            && StringUtils.matches(
                deviceInfo.getDescription(),
                keys.getDescription(),
                true, true)
            && StringUtils.matches(
                deviceInfo.getManufacturer(),
                keys.getManufacturer(),
                true, true)
            && StringUtils.matches(
                deviceInfo.getManufacturerModelName(),
                keys.getManufacturerModelName(),
                true, true)
            && matches(
                deviceInfo.getSoftwareVersions(),
                keys.getSoftwareVersions())
            && StringUtils.matches(
                deviceInfo.getStationName(),
                keys.getStationName(),
                true, true)
            && matches(
                deviceInfo.getInstitutionNames(),
                keys.getInstitutionNames())
            && matches(
                deviceInfo.getInstitutionalDepartmentNames(),
                keys.getInstitutionalDepartmentNames())
            && matches(
                deviceInfo.getPrimaryDeviceTypes(),
                keys.getPrimaryDeviceTypes())
            && (keys.getInstalled() == null
             || keys.getInstalled().equals(deviceInfo.getInstalled()));
    }

    private boolean matches(String[] values, String[] keys) {
        if (keys.length == 0)
            return true;

        for (String key : keys)
            for (String value : values)
                if (StringUtils.matches(value, key, true, true))
                    return true;

        return false;
    }

    private void loadFrom(DeviceInfo deviceInfo, Preferences prefs) {
        deviceInfo.setDescription(prefs.get("dicomDescription", null));
        deviceInfo.setManufacturer(prefs.get("dicomManufacturer", null));
        deviceInfo.setManufacturerModelName(
                prefs.get("dicomManufacturerModelName", null));
        deviceInfo.setSoftwareVersions(
                PreferencesUtils.stringArray(prefs, "dicomSoftwareVersion"));
        deviceInfo.setStationName(prefs.get("dicomStationName", null));
        deviceInfo.setInstitutionNames(
                PreferencesUtils.stringArray(prefs, "dicomInstitutionName"));
        deviceInfo.setInstitutionalDepartmentNames(
                PreferencesUtils.stringArray(prefs, "dicomInstitutionalDepartmentName"));
        deviceInfo.setPrimaryDeviceTypes(
                PreferencesUtils.stringArray(prefs, "dicomPrimaryDeviceType"));
        deviceInfo.setInstalled(prefs.getBoolean("dicomInstalled", false));
    }

    public synchronized Device loadDevice(String pathName) throws ConfigurationException,
            ConfigurationNotFoundException {
        if (!PreferencesUtils.nodeExists(rootPrefs, pathName))
            throw new ConfigurationNotFoundException();

        return loadDevice(rootPrefs.node(pathName));
    }

    @Override
    public String[] listDeviceNames() throws ConfigurationException {
        if (!PreferencesUtils.nodeExists(rootPrefs, DICOM_DEVICES_ROOT))
            return StringUtils.EMPTY_STRING;

        try {
            return rootPrefs.node(DICOM_DEVICES_ROOT).childrenNames();
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public String[] listRegisteredAETitles() throws ConfigurationException {
        if (!PreferencesUtils.nodeExists(rootPrefs, DICOM_UNIQUE_AE_TITLES_REGISTRY_ROOT))
            return StringUtils.EMPTY_STRING;

        try {
            return rootPrefs.node(DICOM_UNIQUE_AE_TITLES_REGISTRY_ROOT).childrenNames();
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public synchronized void persist(Device device) throws ConfigurationException {
        String deviceName = device.getDeviceName();
        String pathName = deviceRef(deviceName);
        if (PreferencesUtils.nodeExists(rootPrefs, pathName))
            throw new ConfigurationAlreadyExistsException(pathName);

        Preferences deviceNode = rootPrefs.node(pathName);
        storeTo(device, deviceNode);
        storeChilds(device, deviceNode);
        try {
            updateCertificates(device);
            deviceNode.flush();
            deviceNode = null;
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        } catch (CertificateException e) {
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

    private void updateCertificates(Device device)
            throws CertificateException, BackingStoreException {
        for (String dn : device.getAuthorizedNodeCertificateRefs())
            updateCertificates(dn, loadCertificates(dn),
                    device.getAuthorizedNodeCertificates(dn));
        for (String dn : device.getThisNodeCertificateRefs())
            updateCertificates(dn, loadCertificates(dn),
                    device.getThisNodeCertificates(dn));
    }

    private void updateCertificates(String ref, 
            X509Certificate[] prev, X509Certificate[] certs)
            throws CertificateEncodingException, BackingStoreException {
        if (!Arrays.equals(prev, certs))
            storeCertificates(ref, certs);
    }

    private void storeChilds(Device device, Preferences deviceNode) {
        Preferences connsNode = deviceNode.node("dcmNetworkConnection");
        int connIndex = 1;
        List<Connection> devConns = device.listConnections();
        for (Connection conn : devConns)
            storeTo(conn, connsNode.node("" + connIndex++));
        Preferences aesNode = deviceNode.node("dcmNetworkAE");
        for (ApplicationEntity ae : device.getApplicationEntities()) {
            Preferences aeNode = aesNode.node(ae.getAETitle());
            storeTo(ae, aeNode, devConns);
            storeChilds(ae, aeNode);
        }

        for (PreferencesDicomConfigurationExtension ext : extensions)
            ext.storeChilds(device, deviceNode);
    }

    private void storeChilds(ApplicationEntity ae, Preferences aeNode) {
        storeTransferCapabilities(ae, aeNode);

        for (PreferencesDicomConfigurationExtension ext : extensions)
            ext.storeChilds(ae, aeNode);
    }

    private void storeTransferCapabilities(ApplicationEntity ae,
            Preferences aeNode) {
        Preferences tcsNode = aeNode.node("dicomTransferCapability");
        storeTransferCapabilities(ae, TransferCapability.Role.SCP, tcsNode);
        storeTransferCapabilities(ae, TransferCapability.Role.SCU, tcsNode);
    }

    private void storeTransferCapabilities(ApplicationEntity ae, Role role,
            Preferences tcsNode) {
        Preferences roleNode = tcsNode.node(role.name());
        for (TransferCapability tc : ae.getTransferCapabilitiesWithRole(role))
            storeTo(tc, roleNode.node(tc.getSopClass()));
    }

    public void store(AttributeCoercions acs, Preferences parentNode) {
        Preferences acsNode = parentNode.node("dcmAttributeCoercion");
        for (AttributeCoercion ac : acs)
            storeTo(ac, acsNode.node(ac.getCommonName()));
    }

    private static void storeTo(AttributeCoercion ac, Preferences prefs) {
        PreferencesUtils.storeNotNull(prefs, "dcmDIMSE", ac.getDIMSE());
        PreferencesUtils.storeNotNull(prefs, "dicomTransferRole", ac.getRole());
        PreferencesUtils.storeNotEmpty(prefs, "dcmAETitle", ac.getAETitles());
        PreferencesUtils.storeNotEmpty(prefs, "dcmSOPClass", ac.getSOPClasses());
        PreferencesUtils.storeNotNull(prefs, "labeledURI", ac.getURI());
    }

    @Override
    public synchronized void merge(Device device) throws ConfigurationException {
        String pathName = deviceRef(device.getDeviceName());
        if (!PreferencesUtils.nodeExists(rootPrefs, pathName))
            throw new ConfigurationNotFoundException();
        
        Preferences devicePrefs = rootPrefs.node(pathName);
        Device prev = loadDevice(devicePrefs);
        try {
            storeDiffs(devicePrefs, prev, device);
            mergeChilds(prev, device, devicePrefs);
            updateCertificates(prev, device);
            devicePrefs.flush();
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        } catch (CertificateException e) {
            throw new ConfigurationException(e);
        }
    }

    private void updateCertificates(Device prev, Device device)
            throws CertificateException, BackingStoreException {
        for (String dn : device.getAuthorizedNodeCertificateRefs()) {
            X509Certificate[] prevCerts = prev.getAuthorizedNodeCertificates(dn);
            updateCertificates(dn,
                    prevCerts != null ? prevCerts : loadCertificates(dn),
                    device.getAuthorizedNodeCertificates(dn));
        }
        for (String dn : device.getThisNodeCertificateRefs()) {
            X509Certificate[] prevCerts = prev.getThisNodeCertificates(dn);
            updateCertificates(dn,
                    prevCerts != null ? prevCerts : loadCertificates(dn),
                    device.getThisNodeCertificates(dn));
        }
    }

    private void mergeChilds(Device prev, Device device,
            Preferences devicePrefs) throws BackingStoreException {
        mergeConnections(prev, device, devicePrefs);
        mergeAEs(prev, device, devicePrefs);
        for (PreferencesDicomConfigurationExtension ext : extensions)
            ext.mergeChilds(prev, device, devicePrefs);
    }

    @Override
    public synchronized void removeDevice(String name) throws ConfigurationException {
        String pathName = deviceRef(name);
        if (!PreferencesUtils.nodeExists(rootPrefs, pathName))
            throw new ConfigurationNotFoundException();

        try {
            Preferences node = rootPrefs.node(pathName);
            node.removeNode();
            node.flush();
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    private void storeTo(Device device, Preferences prefs) {
        PreferencesUtils.storeNotNull(prefs, "dicomDescription", device.getDescription());
        PreferencesUtils.storeNotNull(prefs, "dicomManufacturer", device.getManufacturer());
        PreferencesUtils.storeNotNull(prefs, "dicomManufacturerModelName",
                device.getManufacturerModelName());
        PreferencesUtils.storeNotEmpty(prefs, "dicomSoftwareVersion",
                device.getSoftwareVersions());
        PreferencesUtils.storeNotNull(prefs, "dicomStationName", device.getStationName());
        PreferencesUtils.storeNotNull(prefs, "dicomDeviceSerialNumber",
                device.getDeviceSerialNumber());
        PreferencesUtils.storeNotNull(prefs, "dicomIssuerOfPatientID",
                device.getIssuerOfPatientID());
        PreferencesUtils.storeNotNull(prefs, "dicomIssuerOfAccessionNumber",
                device.getIssuerOfAccessionNumber());
        PreferencesUtils.storeNotNull(prefs, "dicomOrderPlacerIdentifier",
                device.getOrderPlacerIdentifier());
        PreferencesUtils.storeNotNull(prefs, "dicomOrderFillerIdentifier",
                device.getOrderFillerIdentifier());
        PreferencesUtils.storeNotNull(prefs, "dicomIssuerOfAdmissionID",
                device.getIssuerOfAdmissionID());
        PreferencesUtils.storeNotNull(prefs, "dicomIssuerOfServiceEpisodeID",
                device.getIssuerOfServiceEpisodeID());
        PreferencesUtils.storeNotNull(prefs, "dicomIssuerOfContainerIdentifier",
                device.getIssuerOfContainerIdentifier());
        PreferencesUtils.storeNotNull(prefs, "dicomIssuerOfSpecimenIdentifier",
                device.getIssuerOfSpecimenIdentifier());
        PreferencesUtils.storeNotEmpty(prefs, "dicomInstitutionName",
                device.getInstitutionNames());
        PreferencesUtils.storeNotEmpty(prefs, "dicomInstitutionCode",
                device.getInstitutionCodes());
        PreferencesUtils.storeNotEmpty(prefs, "dicomInstitutionAddress",
                device.getInstitutionAddresses());
        PreferencesUtils.storeNotEmpty(prefs, "dicomInstitutionalDepartmentName",
                device.getInstitutionalDepartmentNames());
        PreferencesUtils.storeNotEmpty(prefs, "dicomPrimaryDeviceType",
                device.getPrimaryDeviceTypes());
        PreferencesUtils.storeNotEmpty(prefs, "dicomRelatedDeviceReference",
                device.getRelatedDeviceRefs());
        PreferencesUtils.storeNotEmpty(prefs, "dicomAuthorizedNodeCertificateReference",
                device.getAuthorizedNodeCertificateRefs());
        PreferencesUtils.storeNotEmpty(prefs, "dicomThisNodeCertificateReference",
                device.getThisNodeCertificateRefs());
        storeNotEmpty(prefs, "dicomVendorData", device.getVendorData());
        prefs.putBoolean("dicomInstalled", device.isInstalled());
        
        PreferencesUtils.storeNotDef(prefs, "dcmLimitOpenAssociations", device.getLimitOpenAssociations(), 0);
        PreferencesUtils.storeNotNull(prefs, "dcmTrustStoreURL", device.getTrustStoreURL());
        PreferencesUtils.storeNotNull(prefs, "dcmTrustStoreType", device.getTrustStoreType());
        PreferencesUtils.storeNotNull(prefs, "dcmTrustStorePin", device.getTrustStorePin());
        PreferencesUtils.storeNotNull(prefs, "dcmTrustStorePinProperty", device.getTrustStorePinProperty());
        PreferencesUtils.storeNotNull(prefs, "dcmKeyStoreURL", device.getKeyStoreURL());
        PreferencesUtils.storeNotNull(prefs, "dcmKeyStoreType", device.getKeyStoreType());
        PreferencesUtils.storeNotNull(prefs, "dcmKeyStorePin", device.getKeyStorePin());
        PreferencesUtils.storeNotNull(prefs, "dcmKeyStorePinProperty", device.getKeyStorePinProperty());
        PreferencesUtils.storeNotNull(prefs, "dcmKeyStoreKeyPin", device.getKeyStoreKeyPin());
        PreferencesUtils.storeNotNull(prefs, "dcmKeyStoreKeyPinProperty", device.getKeyStoreKeyPinProperty());

        for (PreferencesDicomConfigurationExtension ext : extensions)
            ext.storeTo(device, prefs);
    }

    private void storeTo(Connection conn, Preferences prefs) {
        PreferencesUtils.storeNotNull(prefs, "cn", conn.getCommonName());
        PreferencesUtils.storeNotNull(prefs, "dicomHostname", conn.getHostname());
        PreferencesUtils.storeNotDef(prefs, "dicomPort", conn.getPort(), Connection.NOT_LISTENING);
        PreferencesUtils.storeNotEmpty(prefs, "dicomTLSCipherSuite", conn.getTlsCipherSuites());
        PreferencesUtils.storeNotNull(prefs, "dicomInstalled", conn.getInstalled());

        PreferencesUtils.storeNotNull(prefs, "dcmProtocol", 
                StringUtils.nullify(conn.getProtocol(), Protocol.DICOM));
        PreferencesUtils.storeNotNull(prefs, "dcmHTTPProxy", conn.getHttpProxy());
        PreferencesUtils.storeNotEmpty(prefs, "dcmBlacklistedHostname", conn.getBlacklist());
        PreferencesUtils.storeNotDef(prefs, "dcmTCPBacklog",
                conn.getBacklog(), Connection.DEF_BACKLOG);
        PreferencesUtils.storeNotDef(prefs, "dcmTCPConnectTimeout",
                conn.getConnectTimeout(), Connection.NO_TIMEOUT);
        PreferencesUtils.storeNotDef(prefs, "dcmAARQTimeout",
                conn.getRequestTimeout(), Connection.NO_TIMEOUT);
        PreferencesUtils.storeNotDef(prefs, "dcmAAACTimeout",
                conn.getAcceptTimeout(), Connection.NO_TIMEOUT);
        PreferencesUtils.storeNotDef(prefs, "dcmARRPTimeout",
                conn.getReleaseTimeout(), Connection.NO_TIMEOUT);
        PreferencesUtils.storeNotDef(prefs, "dcmResponseTimeout",
                conn.getResponseTimeout(), Connection.NO_TIMEOUT);
        PreferencesUtils.storeNotDef(prefs, "dcmRetrieveTimeout",
                conn.getRetrieveTimeout(), Connection.NO_TIMEOUT);
        PreferencesUtils.storeNotDef(prefs, "dcmIdleTimeout",
                conn.getIdleTimeout(), Connection.NO_TIMEOUT);
        PreferencesUtils.storeNotDef(prefs, "dcmTCPCloseDelay",
                conn.getSocketCloseDelay(), Connection.DEF_SOCKETDELAY);
        PreferencesUtils.storeNotDef(prefs, "dcmTCPSendBufferSize",
                conn.getSendBufferSize(), Connection.DEF_BUFFERSIZE);
        PreferencesUtils.storeNotDef(prefs, "dcmTCPReceiveBufferSize",
                conn.getReceiveBufferSize(), Connection.DEF_BUFFERSIZE);
        PreferencesUtils.storeNotDef(prefs, "dcmTCPNoDelay", conn.isTcpNoDelay(), true);
        PreferencesUtils.storeNotDef(prefs, "dcmSendPDULength",
                conn.getSendPDULength(), Connection.DEF_MAX_PDU_LENGTH);
        PreferencesUtils.storeNotDef(prefs, "dcmReceivePDULength",
                conn.getReceivePDULength(), Connection.DEF_MAX_PDU_LENGTH);
        PreferencesUtils.storeNotDef(prefs, "dcmMaxOpsPerformed",
                conn.getMaxOpsPerformed(), Connection.SYNCHRONOUS_MODE);
        PreferencesUtils.storeNotDef(prefs, "dcmMaxOpsInvoked",
                conn.getMaxOpsInvoked(), Connection.SYNCHRONOUS_MODE);
        PreferencesUtils.storeNotDef(prefs, "dcmPackPDV", conn.isPackPDV(), true);
        if (conn.isTls()) {
            PreferencesUtils.storeNotEmpty(prefs, "dcmTLSProtocol", conn.getTlsProtocols());
            PreferencesUtils.storeNotDef(prefs, "dcmTLSNeedClientAuth", conn.isTlsNeedClientAuth(), true);
        }
    }

    private void storeTo(ApplicationEntity ae, Preferences prefs, List<Connection> devConns) {
        PreferencesUtils.storeNotNull(prefs, "dicomDescription", ae.getDescription());
        storeNotEmpty(prefs, "dicomVendorData", ae.getVendorData());
        PreferencesUtils.storeNotEmpty(prefs, "dicomApplicationCluster", ae.getApplicationClusters());
        PreferencesUtils.storeNotEmpty(prefs, "dicomPreferredCallingAETitle", ae.getPreferredCallingAETitles());
        PreferencesUtils.storeNotEmpty(prefs, "dicomPreferredCalledAETitle", ae.getPreferredCalledAETitles());
        prefs.putBoolean("dicomAssociationInitiator", ae.isAssociationInitiator());
        prefs.putBoolean("dicomAssociationAcceptor",  ae.isAssociationAcceptor());
        PreferencesUtils.storeNotEmpty(prefs, "dicomSupportedCharacterSet", ae.getSupportedCharacterSets());
        PreferencesUtils.storeNotNull(prefs, "dicomInstalled", ae.getInstalled());
        PreferencesUtils.storeConnRefs(prefs, ae.getConnections(), devConns);
        PreferencesUtils.storeNotEmpty(prefs, "dcmAcceptedCallingAETitle", ae.getAcceptedCallingAETitles());
        for (PreferencesDicomConfigurationExtension ext : extensions)
            ext.storeTo(ae, prefs);
    }

    private void storeTo(TransferCapability tc, Preferences prefs) {
        PreferencesUtils.storeNotNull(prefs, "cn", tc.getCommonName());
        PreferencesUtils.storeNotEmpty(prefs, "dicomTransferSyntax", tc.getTransferSyntaxes());
        EnumSet<QueryOption> queryOpts = tc.getQueryOptions();
        if (queryOpts != null) {
            PreferencesUtils.storeNotDef(prefs, "dcmRelationalQueries",
                    queryOpts.contains(QueryOption.RELATIONAL), false);
            PreferencesUtils.storeNotDef(prefs, "dcmCombinedDateTimeMatching",
                    queryOpts.contains(QueryOption.DATETIME), false);
            PreferencesUtils.storeNotDef(prefs, "dcmFuzzySemanticMatching",
                    queryOpts.contains(QueryOption.FUZZY), false);
            PreferencesUtils.storeNotDef(prefs, "dcmTimezoneQueryAdjustment",
                    queryOpts.contains(QueryOption.TIMEZONE), false);
        }
        StorageOptions storageOpts = tc.getStorageOptions();
        if (storageOpts != null) {
            prefs.putInt("dcmStorageConformance",
                    storageOpts.getLevelOfSupport().ordinal());
            prefs.putInt("dcmDigitalSignatureSupport",
                    storageOpts.getDigitalSignatureSupport().ordinal());
            prefs.putInt("dcmDataElementCoercion",
                    storageOpts.getElementCoercion().ordinal());
        }
    }

    private void storeDiffs(Preferences prefs, Device a, Device b) {
        PreferencesUtils.storeDiff(prefs, "dicomDescription",
                a.getDescription(),
                b.getDescription());
        PreferencesUtils.storeDiff(prefs, "dicomManufacturer",
                a.getManufacturer(),
                b.getManufacturer());
        PreferencesUtils.storeDiff(prefs, "dicomManufacturerModelName",
                a.getManufacturerModelName(),
                b.getManufacturerModelName());
        storeDiff(prefs, "dicomSoftwareVersion",
                a.getSoftwareVersions(),
                b.getSoftwareVersions());
        PreferencesUtils.storeDiff(prefs, "dicomStationName",
                a.getStationName(),
                b.getStationName());
        PreferencesUtils.storeDiff(prefs, "dicomDeviceSerialNumber",
                a.getDeviceSerialNumber(),
                b.getDeviceSerialNumber());
        PreferencesUtils.storeDiff(prefs, "dicomIssuerOfPatientID",
                a.getIssuerOfPatientID(),
                b.getIssuerOfPatientID());
        PreferencesUtils.storeDiff(prefs, "dicomIssuerOfAccessionNumber",
                a.getIssuerOfAccessionNumber(),
                b.getIssuerOfAccessionNumber());
        PreferencesUtils.storeDiff(prefs, "dicomOrderPlacerIdentifier",
                a.getOrderPlacerIdentifier(),
                b.getOrderPlacerIdentifier());
        PreferencesUtils.storeDiff(prefs, "dicomOrderFillerIdentifier",
                a.getOrderFillerIdentifier(),
                b.getOrderFillerIdentifier());
        PreferencesUtils.storeDiff(prefs, "dicomIssuerOfAdmissionID",
                a.getIssuerOfAdmissionID(),
                b.getIssuerOfAdmissionID());
        PreferencesUtils.storeDiff(prefs, "dicomIssuerOfServiceEpisodeID",
                a.getIssuerOfServiceEpisodeID(),
                b.getIssuerOfServiceEpisodeID());
        PreferencesUtils.storeDiff(prefs, "dicomIssuerOfContainerIdentifier",
                a.getIssuerOfContainerIdentifier(),
                b.getIssuerOfContainerIdentifier());
        PreferencesUtils.storeDiff(prefs, "dicomIssuerOfSpecimenIdentifier",
                a.getIssuerOfSpecimenIdentifier(),
                b.getIssuerOfSpecimenIdentifier());
        storeDiff(prefs, "dicomInstitutionName",
                a.getInstitutionNames(),
                b.getInstitutionNames());
        storeDiff(prefs, "dicomInstitutionCode",
                a.getInstitutionCodes(),
                b.getInstitutionCodes());
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
        PreferencesUtils.storeDiff(prefs, "dicomInstalled",
                a.isInstalled(),
                b.isInstalled());

        PreferencesUtils.storeDiff(prefs, "dcmLimitOpenAssociations",
                a.getLimitOpenAssociations(),
                b.getLimitOpenAssociations());
        PreferencesUtils.storeDiff(prefs, "dcmTrustStoreURL",
                a.getTrustStoreURL(),
                b.getTrustStoreURL());
        PreferencesUtils.storeDiff(prefs, "dcmTrustStoreType",
                a.getTrustStoreType(),
                b.getTrustStoreType());
        PreferencesUtils.storeDiff(prefs, "dcmTrustStorePin",
                a.getTrustStorePin(),
                b.getTrustStorePin());
        PreferencesUtils.storeDiff(prefs, "dcmTrustStorePinProperty",
                a.getTrustStorePinProperty(),
                b.getTrustStorePinProperty());
        PreferencesUtils.storeDiff(prefs, "dcmKeyStoreURL",
                a.getKeyStoreURL(),
                b.getKeyStoreURL());
        PreferencesUtils.storeDiff(prefs, "dcmKeyStoreType",
                a.getKeyStoreType(),
                b.getKeyStoreType());
        PreferencesUtils.storeDiff(prefs, "dcmKeyStorePin",
                a.getKeyStorePin(),
                b.getKeyStorePin());
        PreferencesUtils.storeDiff(prefs, "dcmKeyStorePinProperty",
                a.getKeyStorePinProperty(),
                b.getKeyStorePinProperty());
        PreferencesUtils.storeDiff(prefs, "dcmKeyStoreKeyPin",
                a.getKeyStoreKeyPin(),
                b.getKeyStoreKeyPin());
        PreferencesUtils.storeDiff(prefs, "dcmKeyStoreKeyPinProperty",
                a.getKeyStoreKeyPinProperty(),
                b.getKeyStoreKeyPinProperty());

        for (PreferencesDicomConfigurationExtension ext : extensions)
            ext.storeDiffs(a, b, prefs);
    }

    private void storeDiffs(Preferences prefs, Connection a, Connection b) {
        PreferencesUtils.storeDiff(prefs, "cn",
                a.getCommonName(),
                b.getCommonName());
        PreferencesUtils.storeDiff(prefs, "dicomHostname",
                a.getHostname(),
                b.getHostname());
        PreferencesUtils.storeDiff(prefs, "dicomPort",
                a.getPort(),
                b.getPort(),
                Connection.NOT_LISTENING);
        storeDiff(prefs, "dicomTLSCipherSuite",
                a.getTlsCipherSuites(),
                b.getTlsCipherSuites());
        PreferencesUtils.storeDiff(prefs, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled());

        PreferencesUtils.storeDiff(prefs, "dcmProtocol",
                StringUtils.nullify(a.getProtocol(), Protocol.DICOM),
                StringUtils.nullify(b.getProtocol(), Protocol.DICOM));
        PreferencesUtils.storeDiff(prefs, "dcmHTTPProxy",
                a.getHttpProxy(),
                b.getHttpProxy());
        storeDiff(prefs, "dcmBlacklistedHostname",
                a.getBlacklist(),
                b.getBlacklist());
        PreferencesUtils.storeDiff(prefs, "dcmTCPBacklog",
                a.getBacklog(),
                b.getBacklog(),
                Connection.DEF_BACKLOG);
        PreferencesUtils.storeDiff(prefs, "dcmTCPConnectTimeout",
                a.getConnectTimeout(),
                b.getConnectTimeout(),
                Connection.NO_TIMEOUT);
        PreferencesUtils.storeDiff(prefs, "dcmAARQTimeout",
                a.getRequestTimeout(),
                b.getRequestTimeout(),
                Connection.NO_TIMEOUT);
        PreferencesUtils.storeDiff(prefs, "dcmAAACTimeout",
                a.getAcceptTimeout(),
                b.getAcceptTimeout(),
                Connection.NO_TIMEOUT);
        PreferencesUtils.storeDiff(prefs, "dcmARRPTimeout",
                a.getReleaseTimeout(),
                b.getReleaseTimeout(),
                Connection.NO_TIMEOUT);
        PreferencesUtils.storeDiff(prefs, "dcmResponseTimeout",
                a.getResponseTimeout(),
                b.getResponseTimeout(),
                Connection.NO_TIMEOUT);
        PreferencesUtils.storeDiff(prefs, "dcmRetrieveTimeout",
                a.getRetrieveTimeout(),
                b.getRetrieveTimeout(),
                Connection.NO_TIMEOUT);
        PreferencesUtils.storeDiff(prefs, "dcmIdleTimeout",
                a.getIdleTimeout(),
                b.getIdleTimeout(),
                Connection.NO_TIMEOUT);
        PreferencesUtils.storeDiff(prefs, "dcmTCPCloseDelay",
                a.getSocketCloseDelay(),
                b.getSocketCloseDelay(),
                Connection.DEF_SOCKETDELAY);
        PreferencesUtils.storeDiff(prefs, "dcmTCPSendBufferSize",
                a.getSendBufferSize(),
                b.getSendBufferSize(),
                Connection.DEF_BUFFERSIZE);
        PreferencesUtils.storeDiff(prefs, "dcmTCPReceiveBufferSize",
                a.getReceiveBufferSize(),
                b.getReceiveBufferSize(),
                Connection.DEF_BUFFERSIZE);
        PreferencesUtils.storeDiff(prefs, "dcmTCPNoDelay",
                a.isTcpNoDelay(),
                b.isTcpNoDelay(),
                true);
        storeDiff(prefs, "dcmTLSProtocol",
                a.isTls() ? a.getTlsProtocols() : StringUtils.EMPTY_STRING,
                b.isTls() ? b.getTlsProtocols() : StringUtils.EMPTY_STRING);
        PreferencesUtils.storeDiff(prefs, "dcmTLSNeedClientAuth",
                !a.isTls() || a.isTlsNeedClientAuth(),
                !a.isTls() || a.isTlsNeedClientAuth(),
                true);
        PreferencesUtils.storeDiff(prefs, "dcmSendPDULength",
                a.getSendPDULength(),
                b.getSendPDULength(),
                Connection.DEF_MAX_PDU_LENGTH);
        PreferencesUtils.storeDiff(prefs, "dcmReceivePDULength",
                a.getReceivePDULength(),
                b.getReceivePDULength(),
                Connection.DEF_MAX_PDU_LENGTH);
        PreferencesUtils.storeDiff(prefs, "dcmMaxOpsPerformed",
                a.getMaxOpsPerformed(),
                b.getMaxOpsPerformed(),
                Connection.SYNCHRONOUS_MODE);
        PreferencesUtils.storeDiff(prefs, "dcmMaxOpsInvoked",
                a.getMaxOpsInvoked(),
                b.getMaxOpsInvoked(),
                Connection.SYNCHRONOUS_MODE);
        PreferencesUtils.storeDiff(prefs, "dcmPackPDV",
                a.isPackPDV(),
                b.isPackPDV());
}

    private void storeDiffs(Preferences prefs, ApplicationEntity a, ApplicationEntity b) {
        PreferencesUtils.storeDiff(prefs, "dicomDescription",
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
        PreferencesUtils.storeDiff(prefs, "dicomAssociationInitiator",
                a.isAssociationInitiator(),
                b.isAssociationInitiator());
        PreferencesUtils.storeDiff(prefs, "dicomAssociationAcceptor",
                a.isAssociationAcceptor(),
                b.isAssociationAcceptor());
        PreferencesUtils.storeDiffConnRefs(prefs, 
                a.getConnections(), a.getDevice().listConnections(), 
                b.getConnections(), b.getDevice().listConnections());
        storeDiff(prefs, "dicomSupportedCharacterSet",
                a.getSupportedCharacterSets(),
                b.getSupportedCharacterSets());
        PreferencesUtils.storeDiff(prefs, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled());

        storeDiff(prefs, "dcmAcceptedCallingAETitle",
                a.getAcceptedCallingAETitles(),
                b.getAcceptedCallingAETitles());

        for (PreferencesDicomConfigurationExtension ext : extensions)
            ext.storeDiffs(a, b, prefs);
    }

    private void storeDiffs(Preferences prefs,
            TransferCapability a, TransferCapability b) {
        PreferencesUtils.storeDiff(prefs, "cn",
                a.getCommonName(),
                b.getCommonName());
        storeDiff(prefs, "dicomTransferSyntax",
                a.getTransferSyntaxes(),
                b.getTransferSyntaxes());
        storeDiffs(prefs, a.getQueryOptions(), b.getQueryOptions());
        storeDiffs(prefs, a.getStorageOptions(), b.getStorageOptions());
    }

    private static void storeDiffs(Preferences prefs,
            EnumSet<QueryOption> prev, EnumSet<QueryOption> val) {
        if (prev != null ? prev.equals(val) : val == null)
            return;

        PreferencesUtils.storeDiff(prefs, "dcmRelationalQueries",
                prev != null && prev.contains(QueryOption.RELATIONAL),
                val != null && val.contains(QueryOption.RELATIONAL),
                false);
        PreferencesUtils.storeDiff(prefs, "dcmCombinedDateTimeMatching",
                prev != null && prev.contains(QueryOption.DATETIME),
                val != null && val.contains(QueryOption.DATETIME),
                false);
        PreferencesUtils.storeDiff(prefs, "dcmFuzzySemanticMatching",
                prev != null && prev.contains(QueryOption.FUZZY),
                val != null && val.contains(QueryOption.FUZZY),
                false);
        PreferencesUtils.storeDiff(prefs, "dcmTimezoneQueryAdjustment",
                prev != null && prev.contains(QueryOption.TIMEZONE),
                val != null && val.contains(QueryOption.TIMEZONE),
                false);
    }

    private static void storeDiffs(Preferences prefs,
            StorageOptions prev, StorageOptions val) {
        if (prev != null ? prev.equals(val) : val == null)
            return;

        PreferencesUtils.storeDiff(prefs, "dcmStorageConformance",
                prev != null ? prev.getLevelOfSupport().ordinal() : -1,
                val != null ? val.getLevelOfSupport().ordinal() : -1,
                -1);
        PreferencesUtils.storeDiff(prefs, "dcmDigitalSignatureSupport",
                prev != null ? prev.getDigitalSignatureSupport().ordinal() : -1,
                val != null ? val.getDigitalSignatureSupport().ordinal() : -1,
                -1);
        PreferencesUtils.storeDiff(prefs, "dcmDataElementCoercion",
                prev != null ? prev.getElementCoercion().ordinal() : -1,
                val != null ? val.getElementCoercion().ordinal() : -1,
                -1);
    }

    private void mergeConnections(Device prevDev, Device device, Preferences deviceNode)
            throws BackingStoreException {
        Preferences connsNode = deviceNode.node("dcmNetworkConnection");
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
        Preferences aesNode = deviceNode.node("dcmNetworkAE");
        Collection<String> aets = dev.getApplicationAETitles();
        for (String aet : prevDev.getApplicationAETitles()) {
            if (!aets.contains(aet))
                aesNode.node(aet).removeNode();
        }
        Collection<String> prevAETs = prevDev.getApplicationAETitles();
        List<Connection> devConns = dev.listConnections();
        for (ApplicationEntity ae : dev.getApplicationEntities()) {
            String aet = ae.getAETitle();
            Preferences aeNode = aesNode.node(aet);
            if (!prevAETs.contains(aet)) {
                storeTo(ae, aeNode, devConns);
                storeChilds(ae, aeNode);
            } else {
                ApplicationEntity prevAE = prevDev.getApplicationEntity(aet);
                storeDiffs(aeNode, prevAE, ae);
                mergeChilds(prevAE, ae, aeNode);
            }
        }
    }

    public void merge(AttributeCoercions prevs, AttributeCoercions acs,
            Preferences parentNode) throws BackingStoreException {
        Preferences acsNode = parentNode.node("dcmAttributeCoercion");
        for (AttributeCoercion prev : prevs) {
            String cn = prev.getCommonName();
            if (acs.findByCommonName(prev.getCommonName()) == null)
                acsNode.node(cn).removeNode();
        }
        for (AttributeCoercion ac : acs) {
            String cn = ac.getCommonName();
            Preferences acNode = acsNode.node(cn);
            AttributeCoercion prev = prevs.findByCommonName(cn);
            if (prev == null)
                storeTo(ac, acNode);
            else
                storeDiffs(acNode, prev, ac);
        }
    }

    private void storeDiffs(Preferences prefs, AttributeCoercion a, AttributeCoercion b) {
        PreferencesUtils.storeDiff(prefs, "dcmDIMSE", a.getDIMSE(), b.getDIMSE());
        PreferencesUtils.storeDiff(prefs, "dicomTransferRole", a.getRole(), b.getRole());
        PreferencesUtils.storeDiff(prefs, "dcmAETitle", a.getAETitles(), b.getAETitles());
        PreferencesUtils.storeDiff(prefs, "dcmSOPClass", a.getSOPClasses(), b.getSOPClasses());
        PreferencesUtils.storeDiff(prefs, "labeledURI", a.getURI(), b.getURI());
    }

    private void mergeChilds(ApplicationEntity prevAE, ApplicationEntity ae,
            Preferences aeNode) throws BackingStoreException {
        mergeTransferCapabilities(prevAE, ae, aeNode);

        for (PreferencesDicomConfigurationExtension ext : extensions)
            ext.mergeChilds(prevAE, ae, aeNode);
    }

    private void mergeTransferCapabilities(ApplicationEntity prevAE,
            ApplicationEntity ae, Preferences aeNode)
            throws BackingStoreException {
        Preferences tcsNode = aeNode.node("dicomTransferCapability");
        mergeTransferCapabilities(prevAE, ae, tcsNode, TransferCapability.Role.SCU);
        mergeTransferCapabilities(prevAE, ae, tcsNode, TransferCapability.Role.SCP);
    }

    private void mergeTransferCapabilities(ApplicationEntity prevAE,
            ApplicationEntity ae, Preferences tcsNode, Role role)
                    throws BackingStoreException {
        Preferences roleNode = tcsNode.node(role.name());
        for (TransferCapability tc : prevAE.getTransferCapabilitiesWithRole(role))
            if (ae.getTransferCapabilityFor(tc.getSopClass(), role) == null)
                roleNode.node(tc.getSopClass()).removeNode();
        for (TransferCapability tc : ae.getTransferCapabilitiesWithRole(role)) {
            Preferences tcNode = roleNode.node(tc.getSopClass());
            TransferCapability prev = 
                    prevAE.getTransferCapabilityFor(tc.getSopClass(), role);
            if (prev == null)
                storeTo(tc, tcNode);
            else
                storeDiffs(tcNode, prev, tc);
        }
        
    }

    private Device loadDevice(Preferences deviceNode) throws ConfigurationException {
        try {
            Device device = newDevice(deviceNode);
            loadFrom(device, deviceNode);
            loadChilds(device, deviceNode);
            return device;
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        } catch (CertificateException e) {
            throw new ConfigurationException(e);
        }
    }

    private void loadChilds(Device device, Preferences deviceNode)
            throws BackingStoreException, ConfigurationException {
        Preferences connsNode = deviceNode.node("dcmNetworkConnection");
        for (int connIndex : sort(connsNode.childrenNames())) {
            Connection conn = newConnection();
            loadFrom(conn, connsNode.node("" + connIndex));
            device.addConnection(conn);
        }
        List<Connection> devConns = device.listConnections();
        Preferences aesNode = deviceNode.node("dcmNetworkAE");
        for (String aet : aesNode.childrenNames()) {
            Preferences aeNode = aesNode.node(aet);
            ApplicationEntity ae = newApplicationEntity(aeNode);
            loadFrom(ae, aeNode);
            int n = aeNode.getInt("dicomNetworkConnectionReference.#", 0);
            for (int i = 0; i < n; i++) {
                ae.addConnection(devConns.get(
                        aeNode.getInt("dicomNetworkConnectionReference." + (i+1), 0) - 1));
            }
            loadChilds(ae, aeNode);
            device.addApplicationEntity(ae);
        }
        for (PreferencesDicomConfigurationExtension ext : extensions)
            ext.loadChilds(device, deviceNode);
    }

    private void loadChilds(ApplicationEntity ae, Preferences aeNode)
            throws BackingStoreException {
        Preferences tcsNode = aeNode.node("dicomTransferCapability");
        loadTransferCapabilities(ae, tcsNode, TransferCapability.Role.SCU);
        loadTransferCapabilities(ae, tcsNode, TransferCapability.Role.SCP);

        for (PreferencesDicomConfigurationExtension ext : extensions)
            ext.loadChilds(ae, aeNode);
    }

    private void loadTransferCapabilities(ApplicationEntity ae,
            Preferences tcsNode, Role role) throws BackingStoreException {
        Preferences roleNode = tcsNode.node(role.name());
        for (String cuid : roleNode.childrenNames())
            ae.addTransferCapability(
                    loadTransferCapability(roleNode.node(cuid), cuid, role));
    }

    public void load(AttributeCoercions acs, Preferences aeNode)
            throws BackingStoreException {
        Preferences acsNode = aeNode.node("dcmAttributeCoercion");
        for (String cn : acsNode.childrenNames()) {
            Preferences acNode = acsNode.node(cn);
            acs.add(new AttributeCoercion(
                    cn,
                    PreferencesUtils.stringArray(acNode, "dcmSOPClass"),
                    Dimse.valueOf(acNode.get("dcmDIMSE", null)),
                    TransferCapability.Role.valueOf(
                            acNode.get("dicomTransferRole", null)),
                    PreferencesUtils.stringArray(acNode, "dcmAETitle"),
                    acNode.get("labeledURI", null)));
        }
    }

    private static int[] sort(String[] ss) {
        int[] a = new int[ss.length];
        for (int i = 0; i < a.length; i++)
            a[i] = Integer.parseInt(ss[i]);
        Arrays.sort(a);
        return a;
    }

    private Device newDevice(Preferences deviceNode) {
        return new Device(deviceNode.name());
    }

    private Connection newConnection() {
        return new Connection();
    }

    private ApplicationEntity newApplicationEntity(Preferences aeNode) {
        return new ApplicationEntity(aeNode.name());
    }

    private void loadFrom(TransferCapability tc, Preferences prefs) {
        tc.setCommonName(prefs.get("cn", null));
        tc.setTransferSyntaxes(PreferencesUtils.stringArray(prefs, "dicomTransferSyntax"));
        tc.setQueryOptions(toQueryOptions(prefs));
        tc.setStorageOptions(toStorageOptions(prefs));
    }

    private static EnumSet<QueryOption> toQueryOptions(Preferences prefs) {
        String relational = prefs.get("dcmRelationalQueries", null);
        String datetime = prefs.get("dcmCombinedDateTimeMatching", null);
        String fuzzy = prefs.get("dcmFuzzySemanticMatching", null);
        String timezone = prefs.get("dcmTimezoneQueryAdjustment", null);
        if (relational == null && datetime == null && fuzzy == null && timezone == null)
            return null;
        EnumSet<QueryOption> opts = EnumSet.noneOf(QueryOption.class);
        if (Boolean.parseBoolean(relational))
            opts.add(QueryOption.RELATIONAL);
        if (Boolean.parseBoolean(datetime))
            opts.add(QueryOption.DATETIME);
        if (Boolean.parseBoolean(fuzzy))
            opts.add(QueryOption.FUZZY);
        if (Boolean.parseBoolean(timezone))
            opts.add(QueryOption.TIMEZONE);
        return opts ;
    }

    private static StorageOptions toStorageOptions(Preferences prefs) {
        int levelOfSupport = prefs.getInt("dcmStorageConformance", -1);
        int signatureSupport = prefs.getInt("dcmDigitalSignatureSupport", -1);
        int coercion = prefs.getInt("dcmDataElementCoercion", -1);
        if (levelOfSupport == -1 && signatureSupport == -1 && coercion == -1)
            return null;
        StorageOptions opts = new StorageOptions();
        if (levelOfSupport != -1)
            opts.setLevelOfSupport(StorageOptions.LevelOfSupport.valueOf(levelOfSupport));
        if (signatureSupport != -1)
            opts.setDigitalSignatureSupport(
                    StorageOptions.DigitalSignatureSupport.valueOf(signatureSupport));
        if (coercion != -1)
            opts.setElementCoercion(StorageOptions.ElementCoercion.valueOf(coercion));
        return opts;
    }

    private TransferCapability loadTransferCapability(Preferences prefs,
            String cuid, Role role) {
        TransferCapability tc = new TransferCapability(null, cuid, role,
                UID.ImplicitVRLittleEndian);
        loadFrom(tc, prefs);
        return tc;
    }

    private void loadFrom(Device device, Preferences prefs)
            throws CertificateException, BackingStoreException {
        device.setDescription(prefs.get("dicomDescription", null));
        device.setManufacturer(prefs.get("dicomManufacturer", null));
        device.setManufacturerModelName(prefs.get("dicomManufacturerModelName", null));
        device.setSoftwareVersions(PreferencesUtils.stringArray(prefs, "dicomSoftwareVersion"));
        device.setStationName(prefs.get("dicomStationName", null));
        device.setDeviceSerialNumber(prefs.get("dicomDeviceSerialNumber", null));
        device.setIssuerOfPatientID(
                issuerOf(prefs.get("dicomIssuerOfPatientID", null)));
        device.setIssuerOfAccessionNumber(
                issuerOf(prefs.get("dicomIssuerOfAccessionNumber", null)));
        device.setOrderPlacerIdentifier(
                issuerOf(prefs.get("dicomOrderPlacerIdentifier", null)));
        device.setOrderFillerIdentifier(
                issuerOf(prefs.get("dicomOrderFillerIdentifier", null)));
        device.setIssuerOfAdmissionID(
                issuerOf(prefs.get("dicomIssuerOfAdmissionID", null)));
        device.setIssuerOfServiceEpisodeID(
                issuerOf(prefs.get("dicomIssuerOfServiceEpisodeID", null)));
        device.setIssuerOfContainerIdentifier(
                issuerOf(prefs.get("dicomIssuerOfContainerIdentifier", null)));
        device.setIssuerOfSpecimenIdentifier(
                issuerOf(prefs.get("dicomIssuerOfSpecimenIdentifier", null)));
        device.setInstitutionNames(PreferencesUtils.stringArray(prefs, "dicomInstitutionName"));
        device.setInstitutionCodes(codeArray(prefs, "dicomInstitutionCode"));
        device.setInstitutionAddresses(PreferencesUtils.stringArray(prefs, "dicomInstitutionAddress"));
        device.setInstitutionalDepartmentNames(
                PreferencesUtils.stringArray(prefs, "dicomInstitutionalDepartmentName"));
        device.setPrimaryDeviceTypes(PreferencesUtils.stringArray(prefs, "dicomPrimaryDeviceType"));
        device.setRelatedDeviceRefs(PreferencesUtils.stringArray(prefs, "dicomRelatedDeviceReference"));
        for (String ref : PreferencesUtils.stringArray(prefs, "dicomAuthorizedNodeCertificateReference"))
            device.setAuthorizedNodeCertificates(ref, loadCertificates(ref));
        for (String ref : PreferencesUtils.stringArray(prefs, "dicomThisNodeCertificateReference"))
            device.setThisNodeCertificates(ref, loadCertificates(ref));
        device.setVendorData(toVendorData(prefs, "dicomVendorData"));
        device.setInstalled(prefs.getBoolean("dicomInstalled", false));
        
        device.setLimitOpenAssociations(
                prefs.getInt("dcmLimitOpenAssociations", 0));
        device.setTrustStoreURL(prefs.get("dcmTrustStoreURL", null));
        device.setTrustStoreType(prefs.get("dcmTrustStoreType", null));
        device.setTrustStorePin(prefs.get("dcmTrustStorePin", null));
        device.setTrustStorePinProperty(
                prefs.get("dcmTrustStorePinProperty", null));
        device.setKeyStoreURL(prefs.get("dcmKeyStoreURL", null));
        device.setKeyStoreType(prefs.get("dcmKeyStoreType", null));
        device.setKeyStorePin(prefs.get("dcmKeyStorePin", null));
        device.setKeyStorePinProperty(
                prefs.get("dcmKeyStorePinProperty", null));
        device.setKeyStoreKeyPin(prefs.get("dcmKeyStoreKeyPin", null));
        device.setKeyStoreKeyPinProperty(
                prefs.get("dcmKeyStoreKeyPinProperty", null));

        for (PreferencesDicomConfigurationExtension ext : extensions)
            ext.loadFrom(device, prefs);
    }

    private void loadFrom(Connection conn, Preferences prefs) {
        conn.setCommonName(prefs.get("cn", null));
        conn.setHostname(prefs.get("dicomHostname", null));
        conn.setPort(prefs.getInt("dicomPort", Connection.NOT_LISTENING));
        conn.setTlsCipherSuites(PreferencesUtils.stringArray(prefs, "dicomTLSCipherSuite"));
        conn.setInstalled(PreferencesUtils.booleanValue(prefs.get("dicomInstalled", null)));

        conn.setProtocol(Protocol.valueOf(prefs.get("dcmProtocol", "DICOM")));
        conn.setHttpProxy(prefs.get("dcmHTTPProxy", null));
        conn.setBlacklist(PreferencesUtils.stringArray(prefs, "dcmBlacklistedHostname"));
        conn.setBacklog(prefs.getInt("dcmTCPBacklog", Connection.DEF_BACKLOG));
        conn.setConnectTimeout(
                prefs.getInt("dcmTCPConnectTimeout", Connection.NO_TIMEOUT));
        conn.setRequestTimeout(
                prefs.getInt("dcmAARQTimeout", Connection.NO_TIMEOUT));
        conn.setAcceptTimeout(
                prefs.getInt("dcmAAACTimeout", Connection.NO_TIMEOUT));
        conn.setReleaseTimeout(
                prefs.getInt("dcmARRPTimeout", Connection.NO_TIMEOUT));
        conn.setResponseTimeout(prefs.getInt("dcmResponseTimeout", Connection.NO_TIMEOUT));
        conn.setRetrieveTimeout(prefs.getInt("dcmRetrieveTimeout", Connection.NO_TIMEOUT));
        conn.setIdleTimeout(
                prefs.getInt("dcmIdleTimeout", Connection.NO_TIMEOUT));
        conn.setSocketCloseDelay(
                prefs.getInt("dcmTCPCloseDelay", Connection.DEF_SOCKETDELAY));
        conn.setSendBufferSize(
                prefs.getInt("dcmTCPSendBufferSize", Connection.DEF_BUFFERSIZE));
        conn.setReceiveBufferSize(
                prefs.getInt("dcmTCPReceiveBufferSize", Connection.DEF_BUFFERSIZE));
        conn.setTcpNoDelay(prefs.getBoolean("dcmTCPNoDelay", true));
        conn.setTlsNeedClientAuth(prefs.getBoolean("dcmTLSNeedClientAuth", true));
        String[] tlsProtocols = PreferencesUtils.stringArray(prefs, "dcmTLSProtocol");
        if (tlsProtocols.length > 0)
            conn.setTlsProtocols(tlsProtocols);
        conn.setSendPDULength(prefs.getInt("dcmSendPDULength",
                Connection.DEF_MAX_PDU_LENGTH));
        conn.setReceivePDULength(prefs.getInt("dcmReceivePDULength",
                Connection.DEF_MAX_PDU_LENGTH));
        conn.setMaxOpsPerformed(prefs.getInt("dcmMaxOpsPerformed",
                Connection.SYNCHRONOUS_MODE));
        conn.setMaxOpsInvoked(prefs.getInt("dcmMaxOpsInvoked",
                Connection.SYNCHRONOUS_MODE));
        conn.setPackPDV(prefs.getBoolean("dcmPackPDV", true));
    }

    private void loadFrom(ApplicationEntity ae, Preferences prefs) {
        ae.setDescription(prefs.get("dicomDescription", null));
        ae.setVendorData(toVendorData(prefs, "dicomVendorData"));
        ae.setApplicationClusters(PreferencesUtils.stringArray(prefs, "dicomApplicationCluster"));
        ae.setPreferredCallingAETitles(PreferencesUtils.stringArray(prefs, "dicomPreferredCallingAETitle"));
        ae.setPreferredCalledAETitles(PreferencesUtils.stringArray(prefs, "dicomPreferredCalledAETitle"));
        ae.setAssociationInitiator(prefs.getBoolean("dicomAssociationInitiator", false));
        ae.setAssociationAcceptor(prefs.getBoolean("dicomAssociationAcceptor", false));
        ae.setSupportedCharacterSets(PreferencesUtils.stringArray(prefs, "dicomSupportedCharacterSet"));
        ae.setInstalled(PreferencesUtils.booleanValue(prefs.get("dicomInstalled", null)));

        PreferencesUtils.storeNotEmpty(prefs, "dcmAcceptedCallingAETitle", ae.getAcceptedCallingAETitles());
        ae.setAcceptedCallingAETitles(
                PreferencesUtils.stringArray(prefs, "dcmAcceptOnlyPreferredCallingAETitle"));

        for (PreferencesDicomConfigurationExtension ext : extensions)
            ext.loadFrom(ae, prefs);
}

    private static byte[][] toVendorData(Preferences prefs, String key) {
       int n = prefs.getInt(key + ".#", 0);
       byte[][] bb = new byte[n][];
       for (int i = 0; i < n; i++)
           bb[i] = prefs.getByteArray(key + '.' + (i+1), null);
       return bb;
    }

    private static Issuer issuerOf(String s) {
        return s != null ? new Issuer(s) : null;
    }

    private static Code[] codeArray(Preferences prefs, String key) {
        int n = prefs.getInt(key + ".#", 0);
        if (n == 0)
            return EMPTY_CODES;
        
        Code[] codes = new Code[n];
        for (int i = 0; i < n; i++)
            codes[i] = new Code(prefs.get(key + '.' + (i+1), null));
        return codes;
    }

    private static void storeNotEmpty(Preferences prefs, String key, byte[][] values) {
        if (values != null && values.length != 0) {
            int count = 0;
            for (byte[] value : values)
                prefs.putByteArray(key + '.' + (++count), value);
            prefs.putInt(key + ".#", count);
        }
    }

    private static <T> void storeDiff(Preferences prefs, String key, T[] prevs, T[] vals) {
        if (!Arrays.equals(prevs, vals)) {
            PreferencesUtils.removeKeys(prefs, key, vals.length, prevs.length);
            PreferencesUtils.storeNotEmpty(prefs, key, vals);
        }
    }

    private static void storeDiff(Preferences prefs, String key,
            byte[][] prevs, byte[][] vals) {
        if (!equals(prevs, vals)) {
            PreferencesUtils.removeKeys(prefs, key, vals.length, prevs.length);
            storeNotEmpty(prefs, key, vals);
        }
    }

    private static boolean equals(byte[][] bb1, byte[][] bb2) {
        if (bb1.length != bb2.length)
            return false;
        
        for (int i = 0; i < bb1.length; i++)
            if (!Arrays.equals(bb1[i], bb2[i]))
                return false;

        return true;
    }

    @Override
    public synchronized void persistCertificates(String certRef, X509Certificate... certs)
            throws ConfigurationException {
        try {
            storeCertificates(certRef, certs);
        } catch (CertificateEncodingException e) {
            throw new ConfigurationException(e);
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    private void storeCertificates(String certRef, X509Certificate... certs)
            throws CertificateEncodingException, BackingStoreException {
        if (certs != null && certs.length != 0) {
            int count = 0;
            Preferences prefs = rootPrefs.node(certRef);
            for (X509Certificate cert : certs)
                prefs.putByteArray(USER_CERTIFICATE + '.' + (++count),
                        cert.getEncoded());
            prefs.putInt(USER_CERTIFICATE + ".#", count);
            prefs.flush();
        }
    }

    @Override
    public synchronized void removeCertificates(String certRef)
            throws ConfigurationException {
        Preferences prefs = rootPrefs.node(certRef);
        PreferencesUtils.removeKeys(prefs, USER_CERTIFICATE, 0, 
                prefs.getInt(USER_CERTIFICATE + ".#", 0));
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public synchronized X509Certificate[] findCertificates(String certRef)
            throws ConfigurationException {
        try {
            return loadCertificates(certRef);
        } catch (CertificateException e) {
            throw new ConfigurationException(e);
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    private X509Certificate[] loadCertificates(String certRef)
            throws CertificateException, BackingStoreException {
        if (!rootPrefs.nodeExists(certRef))
            return EMPTY_X509_CERTIFICATES;

        Preferences prefs = rootPrefs.node(certRef);
        int n = prefs.getInt(USER_CERTIFICATE + ".#", 0);
        if (n == 0)
            return EMPTY_X509_CERTIFICATES;
        
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        X509Certificate[] certs = new X509Certificate[n];
        for (int i = 0; i < n; i++)
            certs[i] = (X509Certificate) cf.generateCertificate(
                    new ByteArrayInputStream(
                            prefs.getByteArray(USER_CERTIFICATE + '.' + (i+1), null)));
        return certs;
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public void sync() throws ConfigurationException {
        try {
            rootPrefs.sync();
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

}
