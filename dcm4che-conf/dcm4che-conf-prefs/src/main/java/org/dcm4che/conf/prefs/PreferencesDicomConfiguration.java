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
        storeChilds(device, deviceNode);
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
        storeNotDef(prefs, "dcmDimseRspTimeout",
                conn.getDimseRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmCGetRspTimeout",
                conn.getCGetRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmCMoveRspTimeout",
                conn.getCMoveRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmIdleTimeout",
                conn.getIdleTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(prefs, "dcmTCPCloseDelay",
                conn.getSocketCloseDelay(), Connection.DEF_SOCKETDELAY);
        storeNotDef(prefs, "dcmTCPSendBufferSize",
                conn.getSendBufferSize(), Connection.DEF_BUFFERSIZE);
        storeNotDef(prefs, "dcmTCPReceiveBufferSize",
                conn.getReceiveBufferSize(), Connection.DEF_BUFFERSIZE);
        storeBoolean(prefs, "dcmTCPNoDelay", conn.isTcpNoDelay());
        storeNotEmpty(prefs, "dcmTLSProtocol", conn.getTlsProtocols());
        storeBoolean(prefs, "dcmTLSNeedClientAuth", conn.isTlsNeedClientAuth());
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

        storeNotDef(prefs, "dcmSendPDULength",
                ae.getSendPDULength(), ApplicationEntity.DEF_MAX_PDU_LENGTH);
        storeNotDef(prefs, "dcmReceivePDULength",
                ae.getReceivePDULength(), ApplicationEntity.DEF_MAX_PDU_LENGTH);
        storeNotDef(prefs, "dcmMaxOpsPerformed",
                ae.getMaxOpsPerformed(), ApplicationEntity.SYNCHRONOUS_MODE);
        storeNotDef(prefs, "dcmMaxOpsInvoked",
                ae.getMaxOpsInvoked(), ApplicationEntity.SYNCHRONOUS_MODE);
        storeBoolean(prefs, "dcmPackPDV", ae.isPackPDV());
        storeBoolean(prefs, "dcmAcceptOnlyPreferredCallingAETitle",
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

    protected void storeTo(TransferCapability tc, Preferences prefs) {
        storeNotNull(prefs, "cn", tc.getCommonName());
        storeNotNull(prefs, "dicomSOPClass", tc.getSopClass());
        storeNotNull(prefs, "dicomTransferRole", tc.getRole().toString());
        storeNotEmpty(prefs, "dicomTransferSyntax", tc.getTransferSyntaxes());
        EnumSet<QueryOption> queryOpts = tc.getQueryOptions();
        if (queryOpts != null) {
            storeBoolean(prefs, "dcmRelationalQueries",
                    queryOpts.contains(QueryOption.RELATIONAL));
            storeBoolean(prefs, "dcmCombinedDateTimeMatching",
                    queryOpts.contains(QueryOption.DATETIME));
            storeBoolean(prefs, "dcmFuzzySemanticMatching",
                    queryOpts.contains(QueryOption.FUZZY));
            storeBoolean(prefs, "dcmTimezoneQueryAdjustment",
                    queryOpts.contains(QueryOption.TIMEZONE));
        }
        StorageOptions storageOpts = tc.getStorageOptions();
        if (storageOpts != null) {
            storeInt(prefs, "dcmStorageConformance",
                    storageOpts.getLevelOfSupport().ordinal());
            storeInt(prefs, "dcmDigitalSignatureSupport",
                    storageOpts.getDigitalSignatureSupport().ordinal());
            storeInt(prefs, "dcmDataElementCoercion",
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

        storeDiff(prefs, "dcmSendPDULength",
                a.getSendPDULength(),
                b.getSendPDULength(),
                ApplicationEntity.DEF_MAX_PDU_LENGTH);
        storeDiff(prefs, "dcmReceivePDULength",
                a.getReceivePDULength(),
                b.getReceivePDULength(),
                ApplicationEntity.DEF_MAX_PDU_LENGTH);
        storeDiff(prefs, "dcmMaxOpsPerformed",
                a.getMaxOpsPerformed(),
                b.getMaxOpsPerformed(),
                ApplicationEntity.SYNCHRONOUS_MODE);
        storeDiff(prefs, "dcmMaxOpsInvoked",
                a.getMaxOpsInvoked(),
                b.getMaxOpsInvoked(),
                ApplicationEntity.SYNCHRONOUS_MODE);
        storeDiff(prefs, "dcmPackPDV",
                a.isPackPDV(),
                b.isPackPDV());
        storeDiff(prefs, "dcmAcceptOnlyPreferredCallingAETitle",
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
        storeDiffs(prefs, a.getQueryOptions(), b.getQueryOptions());
        storeDiffs(prefs, a.getStorageOptions(), b.getStorageOptions());
    }

    private static void storeDiffs(Preferences prefs,
            EnumSet<QueryOption> prev, EnumSet<QueryOption> val) {
        storeDiff(prefs, "dcmRelationalQueries",
                prev != null ? prev.contains(QueryOption.RELATIONAL) : null,
                val != null ? val.contains(QueryOption.RELATIONAL) : null);
        storeDiff(prefs, "dcmCombinedDateTimeMatching",
                prev != null ? prev.contains(QueryOption.DATETIME) : null,
                val != null ? val.contains(QueryOption.DATETIME) : null);
        storeDiff(prefs, "dcmFuzzySemanticMatching",
                prev != null ? prev.contains(QueryOption.FUZZY) : null,
                val != null ? val.contains(QueryOption.FUZZY) : null);
        storeDiff(prefs, "dcmTimezoneQueryAdjustment",
                prev != null ? prev.contains(QueryOption.TIMEZONE) : null,
                val != null ? val.contains(QueryOption.TIMEZONE) : null);
    }

    private static void storeDiffs(Preferences prefs,
            StorageOptions prev, StorageOptions val) {
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
                storeTo(tc, tcNode);
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
        for (int tcIndex : sort(tcsNode.childrenNames()))
            ae.addTransferCapability(
                    loadTransferCapability(tcsNode.node("" + tcIndex)));
    }

    protected void load(AttributeCoercions acs, Preferences deviceNode)
            throws BackingStoreException {
        Preferences acsNode = deviceNode.node("dcmAttributeCoercion");
        for (int acIndex : sort(acsNode.childrenNames())) {
            Preferences acNode = acsNode.node("" + acIndex);
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

    protected void loadFrom(Device device, Preferences prefs) {
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
        device.setAuthorizedNodeCertificateRefs(
                stringArray(prefs, "dicomAuthorizedNodeCertificateReference"));
        device.setThisNodeCertificateRefs(
                stringArray(prefs, "dicomThisNodeCertificateReference"));
        device.setVendorData(toVendorData(prefs, "dicomVendorData"));
        try {
            device.setInstalled(prefs.getBoolean("dicomInstalled", false));
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
    }

    protected void loadFrom(Connection conn, Preferences prefs) {
        conn.setCommonName(prefs.get("cn", null));
        conn.setHostname(prefs.get("dicomHostname", null));
        conn.setPort(prefs.getInt("dicomPort", Connection.NOT_LISTENING));
        conn.setTlsCipherSuites(stringArray(prefs, "dicomTLSCipherSuite"));
        try {
            conn.setInstalled(toBoolean(prefs.get("dicomInstalled", null)));
        } catch (IOException e) {
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
        conn.setDimseRSPTimeout(
                prefs.getInt("dcmDimseRspTimeout", Connection.NO_TIMEOUT));
        conn.setCGetRSPTimeout(
                prefs.getInt("dcmCGetRspTimeout", Connection.NO_TIMEOUT));
        conn.setCMoveRSPTimeout(
                prefs.getInt("dcmCMoveRspTimeout", Connection.NO_TIMEOUT));
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
        conn.setTlsProtocols(stringArray(prefs, "dcmTLSProtocol"));
    }

    private static Boolean toBoolean(String s) {
        return s != null ? Boolean.valueOf(s) : null;
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
        ae.setInstalled(toBoolean(prefs.get("dicomInstalled", null)));

        ae.setSendPDULength(prefs.getInt("dcmSendPDULength",
                ApplicationEntity.DEF_MAX_PDU_LENGTH));
        ae.setReceivePDULength(prefs.getInt("dcmReceivePDULength",
                ApplicationEntity.DEF_MAX_PDU_LENGTH));
        ae.setMaxOpsPerformed(prefs.getInt("dcmMaxOpsPerformed",
                ApplicationEntity.SYNCHRONOUS_MODE));
        ae.setMaxOpsInvoked(prefs.getInt("dcmMaxOpsInvoked",
                ApplicationEntity.SYNCHRONOUS_MODE));
        ae.setPackPDV(prefs.getBoolean("dcmPackPDV", true));
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

    protected static void storeBoolean(Preferences prefs, String key, Boolean value) {
        if (value != null)
            prefs.putBoolean(key, value);
    }

    protected static void storeNotDef(Preferences prefs, String key, int value, int defVal) {
        if (value != defVal)
            storeInt(prefs, key, value);
    }

    protected static void storeInt(Preferences prefs, String key, int value) {
        prefs.putInt(key, value);
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
                storeInt(prefs, key, val);
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
