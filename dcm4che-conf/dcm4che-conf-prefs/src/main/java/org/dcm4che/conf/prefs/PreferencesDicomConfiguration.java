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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dcm4che.conf.api.AttributeCoercion;
import org.dcm4che.conf.api.AttributeCoercions;
import org.dcm4che.conf.api.ConfigurationAlreadyExistsException;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.conf.api.ConfigurationNotFoundException;
import org.dcm4che.conf.api.DicomConfiguration;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.QueryOption;
import org.dcm4che.net.StorageOptions;
import org.dcm4che.net.TransferCapability;
import org.dcm4che.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class PreferencesDicomConfiguration implements DicomConfiguration {

    private static final String USER_CERTIFICATE = "userCertificate";
    private static final X509Certificate[] EMPTY_X509_CERTIFICATES = {};

    private static final Logger LOG = LoggerFactory.getLogger(PreferencesDicomConfiguration.class);

    private final Preferences rootPrefs;

    private String configurationRoot = "org/dcm4che";

    public PreferencesDicomConfiguration(Preferences rootPrefs) {
        this.rootPrefs = rootPrefs;
    }

    public final void setConfigurationRoot(String configurationRoot) {
        this.configurationRoot = configurationRoot;
    }

    public final String getConfigurationRoot() {
        return configurationRoot;
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

    @Override
    public String deviceRef(String name) {
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
            for (String deviceName : devicePrefs.childrenNames()) {
                Preferences deviceNode = devicePrefs.node(deviceName);
                for (String aet2 : deviceNode.node("dcmNetworkAE").childrenNames())
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
        String pathName = deviceRef(name);
        if (!nodeExists(rootPrefs, pathName))
            throw new ConfigurationNotFoundException();

        return loadDevice(rootPrefs.node(pathName));
    }

    @Override
    public void persist(Device device) throws ConfigurationException {
        String deviceName = device.getDeviceName();
        String pathName = deviceRef(deviceName);
        if (nodeExists(rootPrefs, pathName))
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

    protected void storeChilds(Device device, Preferences deviceNode) {
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
    }

    protected void storeChilds(ApplicationEntity ae, Preferences aeNode) {
        storeTransferCapabilities(ae, aeNode);
    }

    private void storeTransferCapabilities(ApplicationEntity ae,
            Preferences aeNode) {
        Preferences tcsNode = aeNode.node("dicomTransferCapability");
        int tcIndex = 1;
        for (TransferCapability tc : ae.getTransferCapabilities()) {
            Preferences tcNode = tcsNode.node("" + tcIndex++);
            storeTo(tc, tcNode);
        }
    }

    protected void store(AttributeCoercions acs, Preferences parentNode) {
        Preferences acsNode = parentNode.node("dcmAttributeCoercion");
        int acIndex = 1;
        for (AttributeCoercion ac : acs.getAll())
            storeTo(ac, acsNode.node("" + acIndex ++));
    }

    private static void storeTo(AttributeCoercion ac, Preferences prefs) {
        storeNotNull(prefs, "dcmDIMSE", ac.getDimse());
        storeNotNull(prefs, "dicomTransferRole", ac.getRole());
        storeNotNull(prefs, "dicomAETitle", ac.getAETitle());
        storeNotNull(prefs, "dicomSOPClass", ac.getSopClass());
        storeNotNull(prefs, "labeledURI", ac.getURI());
    }

    @Override
    public void merge(Device device) throws ConfigurationException {
        String pathName = deviceRef(device.getDeviceName());
        if (!nodeExists(rootPrefs, pathName))
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

    protected void mergeChilds(Device prev, Device device,
            Preferences devicePrefs) throws BackingStoreException {
        mergeConnections(prev, device, devicePrefs);
        mergeAEs(prev, device, devicePrefs);
    }

    @Override
    public void removeDevice(String name) throws ConfigurationException {
        String pathName = deviceRef(name);
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
        storeNotNull(prefs, "dicomInstalled", conn.getInstalled());

        storeNotEmpty(prefs, "dcmBlacklistedHostname", conn.getBlacklist());
        storeNotDef(prefs, "dcmTCPBacklog",
                conn.getBacklog(), Connection.DEF_BACKLOG);
        storeNotDef(prefs, "dcmTCPConnectTimeout",
                conn.getConnectTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmAARQTimeout",
                conn.getRequestTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmAAACTimeout",
                conn.getAcceptTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmARRPTimeout",
                conn.getReleaseTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmCStoreRspTimeout",
                conn.getCStoreRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmCGetRspTimeout",
                conn.getCGetRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmCFindRspTimeout",
                conn.getCFindRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmCMoveRspTimeout",
                conn.getCMoveRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmCEchoRspTimeout",
                conn.getCEchoRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmNEventReportRspTimeout",
                conn.getNEventReportRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmNGetRspTimeout",
                conn.getNGetRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmNSetRspTimeout",
                conn.getNSetRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmNActionRspTimeout",
                conn.getNActionRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmNCreateRspTimeout",
                conn.getNCreateRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmNDeleteRspTimeout",
                conn.getNDeleteRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmIdleTimeout",
                conn.getIdleTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmTCPCloseDelay",
                conn.getSocketCloseDelay(), Connection.DEF_SOCKETDELAY);
        storeNotDef(prefs, "dcmTCPSendBufferSize",
                conn.getSendBufferSize(), Connection.DEF_BUFFERSIZE);
        storeNotDef(prefs, "dcmTCPReceiveBufferSize",
                conn.getReceiveBufferSize(), Connection.DEF_BUFFERSIZE);
        storeNotDef(prefs, "dcmTCPNoDelay", conn.isTcpNoDelay(), true);
        storeNotDef(prefs, "dcmSendPDULength",
                conn.getSendPDULength(), Connection.DEF_MAX_PDU_LENGTH);
        storeNotDef(prefs, "dcmReceivePDULength",
                conn.getReceivePDULength(), Connection.DEF_MAX_PDU_LENGTH);
        storeNotDef(prefs, "dcmMaxOpsPerformed",
                conn.getMaxOpsPerformed(), Connection.SYNCHRONOUS_MODE);
        storeNotDef(prefs, "dcmMaxOpsInvoked",
                conn.getMaxOpsInvoked(), Connection.SYNCHRONOUS_MODE);
        storeNotDef(prefs, "dcmPackPDV", conn.isPackPDV(), true);
        if (conn.isTls()) {
            storeNotEmpty(prefs, "dcmTLSProtocol", conn.getTlsProtocols());
            storeNotDef(prefs, "dcmTLSNeedClientAuth", conn.isTlsNeedClientAuth(), true);
        }
    }

    protected void storeTo(ApplicationEntity ae, Preferences prefs, List<Connection> devConns) {
        storeNotNull(prefs, "dicomDescription", ae.getDescription());
        storeNotEmpty(prefs, "dicomVendorData", ae.getVendorData());
        storeNotEmpty(prefs, "dicomApplicationCluster", ae.getApplicationClusters());
        storeNotEmpty(prefs, "dicomPreferredCallingAETitle", ae.getPreferredCallingAETitles());
        storeNotEmpty(prefs, "dicomPreferredCalledAETitle", ae.getPreferredCalledAETitles());
        prefs.putBoolean("dicomAssociationInitiator", ae.isAssociationInitiator());
        prefs.putBoolean("dicomAssociationAcceptor",  ae.isAssociationAcceptor());
        storeNotEmpty(prefs, "dicomSupportedCharacterSet", ae.getSupportedCharacterSets());
        storeNotNull(prefs, "dicomInstalled", ae.getInstalled());
        storeConnRefs(prefs, ae, devConns);

        storeNotDef(prefs, "dcmAcceptOnlyPreferredCallingAETitle",
                ae.isAcceptOnlyPreferredCallingAETitles(), false);
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

    protected void storeTo(TransferCapability tc, Preferences prefs) {
        storeNotNull(prefs, "cn", tc.getCommonName());
        storeNotNull(prefs, "dicomSOPClass", tc.getSopClass());
        storeNotNull(prefs, "dicomTransferRole", tc.getRole().toString());
        storeNotEmpty(prefs, "dicomTransferSyntax", tc.getTransferSyntaxes());
        EnumSet<QueryOption> queryOpts = tc.getQueryOptions();
        if (queryOpts != null) {
            storeNotDef(prefs, "dcmRelationalQueries",
                    queryOpts.contains(QueryOption.RELATIONAL), false);
            storeNotDef(prefs, "dcmCombinedDateTimeMatching",
                    queryOpts.contains(QueryOption.DATETIME), false);
            storeNotDef(prefs, "dcmFuzzySemanticMatching",
                    queryOpts.contains(QueryOption.FUZZY), false);
            storeNotDef(prefs, "dcmTimezoneQueryAdjustment",
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

        storeDiff(prefs, "dcmBlacklistedHostname",
                a.getBlacklist(),
                b.getBlacklist());
        storeDiff(prefs, "dcmTCPBacklog",
                a.getBacklog(),
                b.getBacklog(),
                Connection.DEF_BACKLOG);
        storeDiff(prefs, "dcmTCPConnectTimeout",
                a.getConnectTimeout(),
                b.getConnectTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(prefs, "dcmAARQTimeout",
                a.getRequestTimeout(),
                b.getRequestTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(prefs, "dcmAAACTimeout",
                a.getAcceptTimeout(),
                b.getAcceptTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(prefs, "dcmARRPTimeout",
                a.getReleaseTimeout(),
                b.getReleaseTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(prefs, "dcmCStoreRspTimeout",
                a.getCStoreRSPTimeout(),
                b.getCStoreRSPTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(prefs, "dcmCGetRspTimeout",
                a.getCGetRSPTimeout(),
                b.getCGetRSPTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(prefs, "dcmCFindRspTimeout",
                a.getCFindRSPTimeout(),
                b.getCFindRSPTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(prefs, "dcmCMoveRspTimeout",
                a.getCMoveRSPTimeout(),
                b.getCMoveRSPTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(prefs, "dcmCEchoRspTimeout",
                a.getCEchoRSPTimeout(),
                b.getCEchoRSPTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(prefs, "dcmNEventReportRspTimeout",
                a.getNEventReportRSPTimeout(),
                b.getNEventReportRSPTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(prefs, "dcmNGetRspTimeout",
                a.getNGetRSPTimeout(),
                b.getNGetRSPTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(prefs, "dcmNSetRspTimeout",
                a.getNSetRSPTimeout(),
                b.getNSetRSPTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(prefs, "dcmNActionRspTimeout",
                a.getNActionRSPTimeout(),
                b.getNActionRSPTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(prefs, "dcmNCreateRspTimeout",
                a.getNCreateRSPTimeout(),
                b.getNCreateRSPTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(prefs, "dcmNDeleteRspTimeout",
                a.getNDeleteRSPTimeout(),
                b.getNDeleteRSPTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(prefs, "dcmIdleTimeout",
                a.getIdleTimeout(),
                b.getIdleTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(prefs, "dcmTCPCloseDelay",
                a.getSocketCloseDelay(),
                b.getSocketCloseDelay(),
                Connection.DEF_SOCKETDELAY);
        storeDiff(prefs, "dcmTCPSendBufferSize",
                a.getSendBufferSize(),
                b.getSendBufferSize(),
                Connection.DEF_BUFFERSIZE);
        storeDiff(prefs, "dcmTCPReceiveBufferSize",
                a.getReceiveBufferSize(),
                b.getReceiveBufferSize(),
                Connection.DEF_BUFFERSIZE);
        storeDiff(prefs, "dcmTCPNoDelay",
                a.isTcpNoDelay(),
                b.isTcpNoDelay(),
                true);
        storeDiff(prefs, "dcmTLSProtocol",
                a.isTls() ? a.getTlsProtocols() : null,
                b.isTls() ? b.getTlsProtocols() : null);
        storeDiff(prefs, "dcmTLSNeedClientAuth",
                !a.isTls() || a.isTlsNeedClientAuth(),
                !a.isTls() || a.isTlsNeedClientAuth(),
                true);
        storeDiff(prefs, "dcmSendPDULength",
                a.getSendPDULength(),
                b.getSendPDULength(),
                Connection.DEF_MAX_PDU_LENGTH);
        storeDiff(prefs, "dcmReceivePDULength",
                a.getReceivePDULength(),
                b.getReceivePDULength(),
                Connection.DEF_MAX_PDU_LENGTH);
        storeDiff(prefs, "dcmMaxOpsPerformed",
                a.getMaxOpsPerformed(),
                b.getMaxOpsPerformed(),
                Connection.SYNCHRONOUS_MODE);
        storeDiff(prefs, "dcmMaxOpsInvoked",
                a.getMaxOpsInvoked(),
                b.getMaxOpsInvoked(),
                Connection.SYNCHRONOUS_MODE);
        storeDiff(prefs, "dcmPackPDV",
                a.isPackPDV(),
                b.isPackPDV());
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

        storeDiff(prefs, "dcmAcceptOnlyPreferredCallingAETitle",
                a.isAcceptOnlyPreferredCallingAETitles(),
                b.isAcceptOnlyPreferredCallingAETitles(),
                false);

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
        storeDiffs(prefs, a.getQueryOptions(), b.getQueryOptions());
        storeDiffs(prefs, a.getStorageOptions(), b.getStorageOptions());
    }

    private static void storeDiffs(Preferences prefs,
            EnumSet<QueryOption> prev, EnumSet<QueryOption> val) {
        if (prev != null ? prev.equals(val) : val == null)
            return;

        storeDiff(prefs, "dcmRelationalQueries",
                prev != null && prev.contains(QueryOption.RELATIONAL),
                val != null && val.contains(QueryOption.RELATIONAL),
                false);
        storeDiff(prefs, "dcmCombinedDateTimeMatching",
                prev != null && prev.contains(QueryOption.DATETIME),
                val != null && val.contains(QueryOption.DATETIME),
                false);
        storeDiff(prefs, "dcmFuzzySemanticMatching",
                prev != null && prev.contains(QueryOption.FUZZY),
                val != null && val.contains(QueryOption.FUZZY),
                false);
        storeDiff(prefs, "dcmTimezoneQueryAdjustment",
                prev != null && prev.contains(QueryOption.TIMEZONE),
                val != null && val.contains(QueryOption.TIMEZONE),
                false);
    }

    private static void storeDiffs(Preferences prefs,
            StorageOptions prev, StorageOptions val) {
        if (prev != null ? prev.equals(val) : val == null)
            return;

        storeDiff(prefs, "dcmStorageConformance",
                prev != null ? prev.getLevelOfSupport().ordinal() : -1,
                val != null ? val.getLevelOfSupport().ordinal() : -1,
                -1);
        storeDiff(prefs, "dcmDigitalSignatureSupport",
                prev != null ? prev.getDigitalSignatureSupport().ordinal() : -1,
                val != null ? val.getDigitalSignatureSupport().ordinal() : -1,
                -1);
        storeDiff(prefs, "dcmDataElementCoercion",
                prev != null ? prev.getElementCoercion().ordinal() : -1,
                val != null ? val.getElementCoercion().ordinal() : -1,
                -1);
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
                storeChilds(ae, aeNode);
            } else {
                storeDiffs(aeNode, prevAE, ae);
                mergeChilds(prevAE, ae, aeNode);
            }
        }
    }

    protected void merge(AttributeCoercions prevs, AttributeCoercions acs,
            Preferences parentNode) throws BackingStoreException {
        Preferences acsNode = parentNode.node("dcmAttributeCoercion");
        int acIndex = 1;
        Iterator<AttributeCoercion> prevIter = prevs.getAll().iterator();
        for (AttributeCoercion ac : acs.getAll()) {
            Preferences acNode = acsNode.node("" + acIndex++);
            if (prevIter.hasNext())
                storeDiffs(acNode, prevIter.next(), ac);
            else
                storeTo(ac, acNode);
        }
        while (prevIter.hasNext()) {
            prevIter.next();
            acsNode.node("" + acIndex++).removeNode();
        }
    }

    private void storeDiffs(Preferences prefs, AttributeCoercion a, AttributeCoercion b) {
        storeDiff(prefs, "dcmDIMSE", a.getDimse(), b.getDimse());
        storeDiff(prefs, "dicomTransferRole", a.getRole(), b.getRole());
        storeDiff(prefs, "dicomAETitle", a.getAETitle(), b.getAETitle());
        storeDiff(prefs, "dicomSOPClass", a.getSopClass(), b.getSopClass());
        storeDiff(prefs, "labeledURI", a.getURI(), b.getURI());
    }

    protected void mergeChilds(ApplicationEntity prevAE, ApplicationEntity ae,
            Preferences aeNode) throws BackingStoreException {
        merge(prevAE.getTransferCapabilities(), ae.getTransferCapabilities(), aeNode);
    }

    private void merge(Collection<TransferCapability> prevs,
            Collection<TransferCapability> tcs, Preferences aeNode)
            throws BackingStoreException {
        Preferences tcsNode = aeNode.node("dicomTransferCapability");
        int tcIndex = 1;
        Iterator<TransferCapability> prevIter = prevs.iterator();
        for (TransferCapability tc : tcs) {
            Preferences tcNode = tcsNode.node("" + tcIndex++);
            if (prevIter.hasNext())
                storeDiffs(tcNode, prevIter.next(), tc);
            else
                storeTo(tc, tcNode);
        }
        while (prevIter.hasNext()) {
            prevIter.next();
            tcsNode.node("" + tcIndex++).removeNode();
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

    protected void loadChilds(Device device, Preferences deviceNode)
            throws BackingStoreException {
        Preferences connsNode = deviceNode.node("dcmNetworkConnection");
        for (int connIndex : sort(connsNode.childrenNames())) {
            Connection conn = newConnection();
            loadFrom(conn, connsNode.node("" + connIndex));
            try {
                device.addConnection(conn);
            } catch (IOException e) {
                throw new AssertionError(e.getMessage());
            } catch (KeyManagementException e) {
                throw new AssertionError(e.getMessage());
            }
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
    }

    protected void loadChilds(ApplicationEntity ae, Preferences aeNode)
            throws BackingStoreException {
        Preferences tcsNode = aeNode.node("dicomTransferCapability");
        for (String tcIndex : tcsNode.childrenNames())
            ae.addTransferCapability(
                    loadTransferCapability(tcsNode.node(tcIndex)));
    }

    protected void load(AttributeCoercions acs, Preferences deviceNode)
            throws BackingStoreException {
        Preferences acsNode = deviceNode.node("dcmAttributeCoercion");
        for (String acIndex : acsNode.childrenNames()) {
            Preferences acNode = acsNode.node(acIndex);
            acs.add(new AttributeCoercion(
                    acNode.get("dicomSOPClass", null),
                    AttributeCoercion.DIMSE.valueOf(
                            acNode.get("dcmDIMSE", null)),
                    TransferCapability.Role.valueOf(
                            acNode.get("dicomTransferRole", null)),
                    acNode.get("dicomAETitle", null),
                    acNode.get("labeledURI", null)));
        }
    }

    protected static int[] sort(String[] ss) {
        int[] a = new int[ss.length];
        for (int i = 0; i < a.length; i++)
            a[i] = Integer.parseInt(ss[i]);
        Arrays.sort(a);
        return a;
    }

    protected Device newDevice(Preferences deviceNode) {
        return new Device(deviceNode.name());
    }

    protected Connection newConnection() {
        return new Connection();
    }

    protected ApplicationEntity newApplicationEntity(Preferences aeNode) {
        return new ApplicationEntity(aeNode.name());
    }

    protected TransferCapability newTransferCapability() {
        return new TransferCapability();
    }

    protected void loadFrom(TransferCapability tc, Preferences prefs) {
        tc.setCommonName(prefs.get("cn", null));
        tc.setSopClass(prefs.get("dicomSOPClass", null));
        tc.setRole(TransferCapability.Role.valueOf(prefs.get("dicomTransferRole", null)));
        tc.setTransferSyntaxes(stringArray(prefs, "dicomTransferSyntax"));
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

    private TransferCapability loadTransferCapability(Preferences prefs) {
        TransferCapability tc = newTransferCapability();
        loadFrom(tc, prefs);
        return tc;
    }

    protected void loadFrom(Device device, Preferences prefs) throws CertificateException {
        device.setDescription(prefs.get("dicomDescription", null));
        device.setManufacturer(prefs.get("dicomManufacturer", null));
        device.setManufacturerModelName(prefs.get("dicomManufacturerModelName", null));
        device.setSoftwareVersions(stringArray(prefs, "dicomSoftwareVersion"));
        device.setStationName(prefs.get("dicomStationName", null));
        device.setDeviceSerialNumber(prefs.get("dicomDeviceSerialNumber", null));
        device.setIssuerOfPatientID(prefs.get("dicomIssuerOfPatientID", null));
        device.setInstitutionNames(stringArray(prefs, "dicomInstitutionName"));
        device.setInstitutionAddresses(stringArray(prefs, "dicomInstitutionAddress"));
        device.setInstitutionalDepartmentNames(
                stringArray(prefs, "dicomInstitutionalDepartmentName"));
        device.setPrimaryDeviceTypes(stringArray(prefs, "dicomPrimaryDeviceType"));
        device.setRelatedDeviceRefs(stringArray(prefs, "dicomRelatedDeviceReference"));
        for (String ref : stringArray(prefs, "dicomAuthorizedNodeCertificateReference"))
            device.setAuthorizedNodeCertificates(ref, loadCertificates(ref));
        for (String ref : stringArray(prefs, "dicomThisNodeCertificateReference"))
            device.setThisNodeCertificates(ref, loadCertificates(ref));
        device.setVendorData(toVendorData(prefs, "dicomVendorData"));
        try {
            device.setInstalled(prefs.getBoolean("dicomInstalled", false));
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        } catch (KeyManagementException e) {
            throw new AssertionError(e.getMessage());
        }
    }

    protected void loadFrom(Connection conn, Preferences prefs) {
        conn.setCommonName(prefs.get("cn", null));
        conn.setHostname(prefs.get("dicomHostname", null));
        conn.setPort(prefs.getInt("dicomPort", Connection.NOT_LISTENING));
        conn.setTlsCipherSuites(stringArray(prefs, "dicomTLSCipherSuite"));
        try {
            conn.setInstalled(booleanValue(prefs.get("dicomInstalled", null)));
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        } catch (KeyManagementException e) {
            throw new AssertionError(e.getMessage());
        }
        conn.setBlacklist(stringArray(prefs, "dcmBlacklistedHostname"));
        conn.setBacklog(prefs.getInt("dcmTCPBacklog", Connection.DEF_BACKLOG));
        conn.setConnectTimeout(
                prefs.getInt("dcmTCPConnectTimeout", Connection.NO_TIMEOUT));
        conn.setRequestTimeout(
                prefs.getInt("dcmAARQTimeout", Connection.NO_TIMEOUT));
        conn.setAcceptTimeout(
                prefs.getInt("dcmAAACTimeout", Connection.NO_TIMEOUT));
        conn.setReleaseTimeout(
                prefs.getInt("dcmARRPTimeout", Connection.NO_TIMEOUT));
        conn.setCStoreRSPTimeout(prefs.getInt("dcmCStoreRspTimeout", Connection.NO_TIMEOUT));
        conn.setCGetRSPTimeout(prefs.getInt("dcmCGetRspTimeout", Connection.NO_TIMEOUT));
        conn.setCFindRSPTimeout(prefs.getInt("dcmCFindRspTimeout", Connection.NO_TIMEOUT));
        conn.setCMoveRSPTimeout(prefs.getInt("dcmCMoveRspTimeout", Connection.NO_TIMEOUT));
        conn.setCEchoRSPTimeout(prefs.getInt("dcmCEchoRspTimeout", Connection.NO_TIMEOUT));
        conn.setNEventReportRSPTimeout(prefs.getInt("dcmNEventReportRspTimeout", Connection.NO_TIMEOUT));
        conn.setNGetRSPTimeout(prefs.getInt("dcmNGetRspTimeout", Connection.NO_TIMEOUT));
        conn.setNSetRSPTimeout(prefs.getInt("dcmNSetRspTimeout", Connection.NO_TIMEOUT));
        conn.setNActionRSPTimeout(prefs.getInt("dcmNActionRspTimeout", Connection.NO_TIMEOUT));
        conn.setNCreateRSPTimeout(
                prefs.getInt("dcmNCreateRspTimeout", Connection.NO_TIMEOUT));
        conn.setNDeleteRSPTimeout(
                prefs.getInt("dcmNDeleteRspTimeout", Connection.NO_TIMEOUT));
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
        String[] tlsProtocols = stringArray(prefs, "dcmTLSProtocol");
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

    protected static Boolean booleanValue(String s) {
        return s != null ? Boolean.valueOf(s) : null;
    }

    protected static Integer intValue(String s) {
        return s != null ? Integer.valueOf(s) : null;
    }

    protected void loadFrom(ApplicationEntity ae, Preferences prefs) {
        ae.setDescription(prefs.get("dicomDescription", null));
        ae.setVendorData(toVendorData(prefs, "dicomVendorData"));
        ae.setApplicationClusters(stringArray(prefs, "dicomApplicationCluster"));
        ae.setPreferredCallingAETitles(stringArray(prefs, "dicomPreferredCallingAETitle"));
        ae.setPreferredCalledAETitles(stringArray(prefs, "dicomPreferredCalledAETitle"));
        ae.setAssociationInitiator(prefs.getBoolean("dicomAssociationInitiator", false));
        ae.setAssociationAcceptor(prefs.getBoolean("dicomAssociationAcceptor", false));
        ae.setSupportedCharacterSets(stringArray(prefs, "dicomSupportedCharacterSet"));
        ae.setInstalled(booleanValue(prefs.get("dicomInstalled", null)));

        ae.setAcceptOnlyPreferredCallingAETitles(
                prefs.getBoolean("dcmAcceptOnlyPreferredCallingAETitle", false));
}

    private static byte[][] toVendorData(Preferences prefs, String key) {
       int n = prefs.getInt(key + ".#", 0);
       byte[][] bb = new byte[n][];
       for (int i = 0; i < n; i++)
           bb[i] = prefs.getByteArray(key + '.' + (i+1), null);
       return bb;
    }

    protected static String[] stringArray(Preferences prefs, String key)  {
        int n = prefs.getInt(key + ".#", 0);
        if (n == 0)
            return StringUtils.EMPTY_STRING;
        
        String[] ss = new String[n];
        for (int i = 0; i < n; i++)
            ss[i] = prefs.get(key + '.' + (i+1), null);
        return ss;
    }

    protected static void storeNotDef(Preferences prefs, String key, int value, int defVal) {
        if (value != defVal)
            prefs.putInt(key, value);
    }

    protected static void storeNotDef(Preferences prefs, String key, boolean val, boolean defVal) {
        if (val != defVal)
            prefs.putBoolean(key, val);
    }

    protected static void storeNotNull(Preferences prefs, String key, Object value) {
        if (value != null)
            prefs.put(key, value.toString());
    }

    protected static void storeNotEmpty(Preferences prefs, String key, String[] values) {
        if (values != null && values.length != 0) {
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

    protected static void storeDiff(Preferences prefs, String key, Object prev, Object val) {
        if (val == null) {
            if (prev != null)
                prefs.remove(key);
        } else if (!val.equals(prev))
            prefs.put(key, val.toString());
    }

    protected static void storeDiff(Preferences prefs, String key,
            boolean prev, boolean val, boolean defVal) {
        if (prev != val)
            if (val == defVal)
                prefs.remove(key);
            else
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

    protected static void removeKeys(Preferences prefs, String key, int from, int to) {
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

    @Override
    public void persistCertificates(String certRef, X509Certificate... certs)
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
    public void removeCertificates(String certRef)
            throws ConfigurationException {
        Preferences prefs = rootPrefs.node(certRef);
        removeKeys(prefs, USER_CERTIFICATE, 0, 
                prefs.getInt(USER_CERTIFICATE + ".#", 0));
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public X509Certificate[] findCertificates(String certRef)
            throws ConfigurationException {
        try {
            return loadCertificates(certRef);
        } catch (CertificateException e) {
            throw new ConfigurationException(e);
        }
    }

    private X509Certificate[] loadCertificates(String certRef)
            throws CertificateException {
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

}
