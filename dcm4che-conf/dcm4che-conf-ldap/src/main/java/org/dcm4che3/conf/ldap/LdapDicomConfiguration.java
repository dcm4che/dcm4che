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

package org.dcm4che3.conf.ldap;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

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
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public final class LdapDicomConfiguration implements DicomConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(LdapDicomConfiguration.class);

    private static final String CN_UNIQUE_AE_TITLES_REGISTRY = "cn=Unique AE Titles Registry,";
    private static final String CN_DEVICES = "cn=Devices,";
    private static final String DICOM_CONFIGURATION = "DICOM Configuration";
    private static final String DICOM_CONFIGURATION_ROOT = "dicomConfigurationRoot";
    private static final String PKI_USER = "pkiUser";
    private static final String USER_CERTIFICATE_BINARY = "userCertificate;binary";
    private static final X509Certificate[] EMPTY_X509_CERTIFICATES = {};
    private static final Code[] EMPTY_CODES = {};

    private final DirContext ctx;
    private final String baseDN;
    private String configurationDN;
    private String devicesDN;
    private String aetsRegistryDN;
    private String configurationCN = DICOM_CONFIGURATION; 
    private String configurationRoot = DICOM_CONFIGURATION_ROOT;
    private String pkiUser = PKI_USER;
    private String userCertificate = USER_CERTIFICATE_BINARY;
    private boolean extended = true;

    private final List<LdapDicomConfigurationExtension> extensions =
            new ArrayList<LdapDicomConfigurationExtension>();

    public LdapDicomConfiguration() throws ConfigurationException {
        this(ResourceManager.getInitialEnvironment());
    }

    @SuppressWarnings("unchecked")
    public LdapDicomConfiguration(Hashtable<?,?> env)
            throws ConfigurationException {
        try {
            // split baseDN from LDAP URL
            env = (Hashtable<?,?>) env.clone();
            String s = (String) env.get(Context.PROVIDER_URL);
            int end = s.lastIndexOf('/');
            ((Hashtable<Object,Object>) env)
                .put(Context.PROVIDER_URL, s.substring(0, end));
            this.baseDN = s.substring(end+1);
            this.ctx = new InitialDirContext(env);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    public final boolean isExtended() {
        return extended;
    }

    public final void setExtended(boolean extended) {
        this.extended = extended;
    }

    public final void setConfigurationCN(String configurationCN) {
        this.configurationCN = configurationCN;
    }

    public final String getConfigurationCN() {
        return configurationCN;
    }

    public final void setConfigurationRoot(String configurationRoot) {
        this.configurationRoot = configurationRoot;
    }

    public final String getConfigurationRoot() {
        return configurationRoot;
    }

    public void setPkiUser(String pkiUser) {
        this.pkiUser = pkiUser;
    }

    public String getPkiUser() {
        return pkiUser;
    }

    public void setUserCertificate(String userCertificate) {
        this.userCertificate = userCertificate;
    }

    public String getUserCertificate() {
        return userCertificate;
    }

    public void addDicomConfigurationExtension(LdapDicomConfigurationExtension ext) {
        ext.setDicomConfiguration(this);
        extensions.add(ext);
    }

    public boolean removeDicomConfigurationExtension(
            LdapDicomConfigurationExtension ext) {
        if (!extensions.remove(ext))
            return false;

        ext.setDicomConfiguration(null);
        return true;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> T getDicomConfigurationExtension(Class<T> clazz) {
        for (LdapDicomConfigurationExtension ext : extensions) {
            if (clazz.isInstance(ext.getClass()))
                return (T) ext;
        }
        return null;
    }

    @Override
    public synchronized void close() {
        safeClose(ctx);
    }

    @Override
    public synchronized boolean configurationExists() throws ConfigurationException {
        return configurationDN != null || findConfiguration();
    }

    public boolean exists(String dn) throws NamingException {
        try {
            ctx.getAttributes(dn);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public synchronized boolean purgeConfiguration() throws ConfigurationException {
        if (!configurationExists())
            return false;

        try {
            destroySubcontextWithChilds(configurationDN);
            LOG.info("Purge DICOM Configuration at {}", configurationDN);
            clearConfigurationDN();
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
        return true;
    }

    @Override
    public synchronized boolean registerAETitle(String aet) throws ConfigurationException {
        ensureConfigurationExists();
        try {
            createSubcontext(aetDN(aet, aetsRegistryDN),
                    LdapUtils.attrs("dicomUniqueAETitle", "dicomAETitle", aet));
            return true;
        } catch (NameAlreadyBoundException e) {
            return false;
        } catch (NamingException e) {
            throw new ConfigurationException(e);
       }
    }

    @Override
    public synchronized void unregisterAETitle(String aet) throws ConfigurationException {
        if (configurationExists())
            try {
                ctx.destroySubcontext(aetDN(aet, aetsRegistryDN));
            } catch (NameNotFoundException e) {
            } catch (NamingException e) {
                throw new ConfigurationException(e);
            }
    }

    @Override
    public synchronized ApplicationEntity findApplicationEntity(String aet)
            throws ConfigurationException {
        return findDevice(
                "(&(objectclass=dicomNetworkAE)(dicomAETitle=" + aet + "))", aet)
            .getApplicationEntity(aet);
    }

    public synchronized Device findDevice(String filter, String childName)
            throws ConfigurationException {
        if (!configurationExists())
            throw new ConfigurationNotFoundException();

        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctls.setCountLimit(1);
        ctls.setReturningAttributes(StringUtils.EMPTY_STRING);
        ctls.setReturningObjFlag(false);
        NamingEnumeration<SearchResult> ne = null;
        String childDN;
        try {
            ne = ctx.search(devicesDN, filter, ctls);
            if (!ne.hasMore())
                throw new ConfigurationNotFoundException(childName);

            childDN = ne.next().getNameInNamespace();
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } finally {
           LdapUtils.safeClose(ne);
        }
        String deviceDN = childDN.substring(childDN.indexOf(',') + 1);
        return loadDevice(deviceDN);
    }

    @Override
    public synchronized Device findDevice(String name) throws ConfigurationException {
        if (!configurationExists())
            throw new ConfigurationNotFoundException();

        return loadDevice(deviceRef(name));
    }

    @Override
    public synchronized DeviceInfo[] listDeviceInfos(DeviceInfo keys)
            throws ConfigurationException {
        if (!configurationExists())
            return new DeviceInfo[0];

        ArrayList<DeviceInfo> results = new ArrayList<DeviceInfo>();
        NamingEnumeration<SearchResult> ne = null;
        try {
            ne = search(devicesDN, toFilter(keys), "dicomDeviceName",
                "dicomDescription",
                "dicomManufacturer",
                "dicomManufacturerModelName",
                "dicomSoftwareVersion",
                "dicomStationName",
                "dicomInstitutionName",
                "dicomInstitutionDepartmentName",
                "dicomPrimaryDeviceType",
                "dicomInstalled");
            while (ne.hasMore()) {
                DeviceInfo deviceInfo = new DeviceInfo();
                loadFrom(deviceInfo, ne.next().getAttributes());
                results.add(deviceInfo);
            }
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } finally {
           LdapUtils.safeClose(ne);
        }
        return results.toArray(new DeviceInfo[results.size()]);
    }

    private String toFilter(DeviceInfo keys) {
        if (keys == null)
            return "(objectclass=dicomDevice)";

        StringBuilder sb = new StringBuilder();
        sb.append("(&(objectclass=dicomDevice)");
        appendFilter("dicomDeviceName", keys.getDeviceName(), sb);
        appendFilter("dicomDescription", keys.getDescription(), sb);
        appendFilter("dicomManufacturer", keys.getManufacturer(), sb);
        appendFilter("dicomManufacturerModelName", keys.getManufacturerModelName(), sb);
        appendFilter("dicomSoftwareVersion", keys.getSoftwareVersions(), sb);
        appendFilter("dicomStationName", keys.getStationName(), sb);
        appendFilter("dicomInstitutionName", keys.getInstitutionNames(), sb);
        appendFilter("dicomInstitutionDepartmentName", keys.getInstitutionalDepartmentNames(), sb);
        appendFilter("dicomPrimaryDeviceType", keys.getPrimaryDeviceTypes(), sb);
        appendFilter("dicomInstalled", keys.getInstalled(), sb);
        sb.append(")");
        return sb.toString();
    }

    private void appendFilter(String attrid, Boolean value, StringBuilder sb) {
        if (value != null)
            appendFilter(attrid, LdapUtils.toString(value), sb);
    }

    private void appendFilter(String attrid, String value, StringBuilder sb) {
        if (value == null)
            return;

        sb.append('(').append(attrid).append('=').append(value).append(')');
    }

    private void appendFilter(String attrid, String[] values, StringBuilder sb) {
        if (values.length == 0)
            return;

        if (values.length == 1) {
            appendFilter(attrid, values[0], sb);
            return;
        }

        sb.append("(|");
        for (String value : values)
            appendFilter(attrid, value, sb);
        sb.append(")");
    }

    private void loadFrom(DeviceInfo deviceInfo, Attributes attrs)
            throws NamingException {
        deviceInfo.setDeviceName(
                LdapUtils.stringValue(attrs.get("dicomDeviceName"), null));
        deviceInfo.setDescription(
                LdapUtils.stringValue(attrs.get("dicomDescription"), null));
        deviceInfo.setManufacturer(
                LdapUtils.stringValue(attrs.get("dicomManufacturer"), null));
        deviceInfo.setManufacturerModelName(
                LdapUtils.stringValue(attrs.get("dicomManufacturerModelName"), null));
        deviceInfo.setSoftwareVersions(
                LdapUtils.stringArray(attrs.get("dicomSoftwareVersion")));
        deviceInfo.setStationName(
                LdapUtils.stringValue(attrs.get("dicomStationName"), null));
        deviceInfo.setInstitutionNames(
                LdapUtils.stringArray(attrs.get("dicomInstitutionName")));
        deviceInfo.setInstitutionalDepartmentNames(
                LdapUtils.stringArray(attrs.get("dicomInstitutionDepartmentName")));
        deviceInfo.setPrimaryDeviceTypes(
                LdapUtils.stringArray(attrs.get("dicomPrimaryDeviceType")));
        deviceInfo.setInstalled(
                LdapUtils.booleanValue(attrs.get("dicomInstalled"), true));
    }

    @Override
    public synchronized String[] listDeviceNames() throws ConfigurationException {
        if (!configurationExists())
            return StringUtils.EMPTY_STRING;

        return list(devicesDN, "(objectclass=dicomDevice)", "dicomDeviceName");
    }

    @Override
    public synchronized String[] listRegisteredAETitles() throws ConfigurationException {
        if (!configurationExists())
            return StringUtils.EMPTY_STRING;

        return list(aetsRegistryDN, "(objectclass=dicomUniqueAETitle)", "dicomAETitle");
    }

    public synchronized String[] list(String dn, String filter, String attrID)
            throws ConfigurationException {
        ArrayList<String> values = new ArrayList<String>();
        NamingEnumeration<SearchResult> ne = null;
        try {
            ne = search(dn, filter, attrID );
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attributes attrs = sr.getAttributes();
                values.add(LdapUtils.stringValue(attrs.get(attrID), null));
            }
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } finally {
           LdapUtils.safeClose(ne);
        }
        return values.toArray(new String[values.size()]);
    }

    @Override
    public synchronized void persist(Device device) throws ConfigurationException {
        ensureConfigurationExists();
        String deviceName = device.getDeviceName();
        String deviceDN = deviceRef(deviceName);
        boolean rollback = false;
        try {
            createSubcontext(deviceDN, storeTo(device, new BasicAttributes(true)));
            rollback = true;
            storeChilds(deviceDN, device);
            updateCertificates(device);
            rollback = false;
        } catch (NameAlreadyBoundException e) {
            throw new ConfigurationAlreadyExistsException(deviceName);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } catch (CertificateException e) {
            throw new ConfigurationException(e);
        } finally {
            if (rollback)
                try {
                    destroySubcontextWithChilds(deviceDN);
                } catch (NamingException e) {
                    LOG.warn("Rollback failed:", e);
                }
        }
    }

    private void updateCertificates(Device device)
            throws CertificateException, NamingException {
        for (String dn : device.getAuthorizedNodeCertificateRefs())
            updateCertificates(dn, loadCertificates(dn),
                    device.getAuthorizedNodeCertificates(dn));
        for (String dn : device.getThisNodeCertificateRefs())
            updateCertificates(dn, loadCertificates(dn),
                    device.getThisNodeCertificates(dn));
    }

    private void updateCertificates(String dn,
            X509Certificate[] prev, X509Certificate[] certs)
            throws CertificateEncodingException, NamingException {
        if (!LdapUtils.equals(prev, certs))
            storeCertificates(dn, certs);
    }

    private void storeChilds(String deviceDN, Device device) throws NamingException {
        for (Connection conn : device.listConnections())
            createSubcontext(LdapUtils.dnOf(conn, deviceDN), storeTo(conn, new BasicAttributes(true)));
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.storeChilds(deviceDN, device);
        for (ApplicationEntity ae : device.getApplicationEntities()) {
            String aeDN = aetDN(ae.getAETitle(), deviceDN);
            createSubcontext(aeDN, storeTo(ae, deviceDN, new BasicAttributes(true)));
            storeChilds(aeDN, ae);
            for (LdapDicomConfigurationExtension ext : extensions) {
                ext.storeChilds(aeDN, ae);
            }
        }
    }

    private void storeChilds(String aeDN, ApplicationEntity ae)
            throws NamingException {
        for (TransferCapability tc : ae.getTransferCapabilities())
            createSubcontext(dnOf(tc, aeDN), storeTo(tc, new BasicAttributes(true)));
    }

    @Override
    public synchronized void merge(Device device) throws ConfigurationException {
        if (!configurationExists())
            throw new ConfigurationNotFoundException();

        String deviceDN = deviceRef(device.getDeviceName());
        Device prev = loadDevice(deviceDN);
        try {
            modifyAttributes(deviceDN, storeDiffs(prev, device, new ArrayList<ModificationItem>()));
            mergeChilds(prev, device, deviceDN);
            updateCertificates(prev, device);
        } catch (NameNotFoundException e) {
            throw new ConfigurationNotFoundException(e);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } catch (CertificateException e) {
            throw new ConfigurationException(e);
        }
    }

    private void updateCertificates(Device prev, Device device)
            throws CertificateException, NamingException {
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

    private void mergeChilds(Device prev, Device device, String deviceDN)
            throws NamingException {
        mergeConnections(prev, device, deviceDN);
        mergeAEs(prev, device, deviceDN);
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.mergeChilds(prev, device, deviceDN);
    }

    @Override
    public synchronized void removeDevice(String name) throws ConfigurationException {
        if (!configurationExists())
            throw new ConfigurationNotFoundException();

        removeDeviceWithDN(deviceRef(name));
    }

    private void removeDeviceWithDN(String deviceDN) throws ConfigurationException {
        try {
            destroySubcontextWithChilds(deviceDN);
        } catch (NameNotFoundException e) {
            throw new ConfigurationNotFoundException(e);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
    }

    public synchronized void createSubcontext(String name, Attributes attrs)
            throws NamingException {
        safeClose(ctx.createSubcontext(name, attrs));
    }

    public synchronized void destroySubcontext(String dn) throws NamingException {
        ctx.destroySubcontext(dn);
    }

    public synchronized void destroySubcontextWithChilds(String name)
            throws NamingException {
        NamingEnumeration<NameClassPair> list = ctx.list(name);
        try {
            while (list.hasMore())
                destroySubcontextWithChilds(list.next().getNameInNamespace());
        } finally {
            LdapUtils.safeClose(list);
        }
        ctx.destroySubcontext(name);
    }

    private void setConfigurationDN(String configurationDN) {
        this.configurationDN = configurationDN;
        this.devicesDN = CN_DEVICES + configurationDN;
        this.aetsRegistryDN = CN_UNIQUE_AE_TITLES_REGISTRY + configurationDN;
    }

    public String getConfigurationDN() {
        return configurationDN;
    }

   private void clearConfigurationDN() {
        this.configurationDN = null;
        this.devicesDN = null;
        this.aetsRegistryDN = null;
    }

    public void ensureConfigurationExists() throws ConfigurationException {
        if (!configurationExists())
            initConfiguration();
    }

    private void initConfiguration() throws ConfigurationException {
        setConfigurationDN("cn=" + configurationCN + ',' + baseDN);
        try {
            createSubcontext(configurationDN,
                    LdapUtils.attrs(configurationRoot, "cn", configurationCN));
            createSubcontext(devicesDN,
                    LdapUtils.attrs("dicomDevicesRoot", "cn", "Devices"));
            createSubcontext(aetsRegistryDN,
                    LdapUtils.attrs("dicomUniqueAETitlesRegistryRoot", 
                            "cn", "Unique AE Titles Registry"));
            LOG.info("Create DICOM Configuration at {}", configurationDN);
        } catch (NamingException e) {
            clearConfigurationDN();
            throw new ConfigurationException(e);
        }
    }

   private boolean findConfiguration() throws ConfigurationException {
        NamingEnumeration<SearchResult> ne = null;
        try {
            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctls.setCountLimit(1);
            ctls.setReturningAttributes(StringUtils.EMPTY_STRING);
            ctls.setReturningObjFlag(false);
            ne = ctx.search(
                    baseDN,
                    "(&(objectclass=" + configurationRoot
                            + ")(cn=" + configurationCN + "))",
                    ctls);
            if (!ne.hasMore())
                return false;
    
            setConfigurationDN(ne.next().getName() + "," + baseDN);
            return true;
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } finally {
            LdapUtils.safeClose(ne);
        }
    }

    private Attributes storeTo(Device device, Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dicomDevice");
        attrs.put(objectclass);
        LdapUtils.storeNotNull(attrs, "dicomDeviceName", device.getDeviceName());
        LdapUtils.storeNotNull(attrs, "dicomDescription", device.getDescription());
        LdapUtils.storeNotNull(attrs, "dicomManufacturer", device.getManufacturer());
        LdapUtils.storeNotNull(attrs, "dicomManufacturerModelName",
                device.getManufacturerModelName());
        LdapUtils.storeNotEmpty(attrs, "dicomSoftwareVersion",
                device.getSoftwareVersions());
        LdapUtils.storeNotNull(attrs, "dicomStationName", device.getStationName());
        LdapUtils.storeNotNull(attrs, "dicomDeviceSerialNumber",
                device.getDeviceSerialNumber());
        LdapUtils.storeNotNull(attrs, "dicomIssuerOfPatientID",
                device.getIssuerOfPatientID());
        LdapUtils.storeNotNull(attrs, "dicomIssuerOfAccessionNumber",
                device.getIssuerOfAccessionNumber());
        LdapUtils.storeNotNull(attrs, "dicomOrderPlacerIdentifier",
                device.getOrderPlacerIdentifier());
        LdapUtils.storeNotNull(attrs, "dicomOrderFillerIdentifier",
                device.getOrderFillerIdentifier());
        LdapUtils.storeNotNull(attrs, "dicomIssuerOfAdmissionID",
                device.getIssuerOfAdmissionID());
        LdapUtils.storeNotNull(attrs, "dicomIssuerOfServiceEpisodeID",
                device.getIssuerOfServiceEpisodeID());
        LdapUtils.storeNotNull(attrs, "dicomIssuerOfContainerIdentifier",
                device.getIssuerOfContainerIdentifier());
        LdapUtils.storeNotNull(attrs, "dicomIssuerOfSpecimenIdentifier",
                device.getIssuerOfSpecimenIdentifier());
        LdapUtils.storeNotEmpty(attrs, "dicomInstitutionName",
                device.getInstitutionNames());
        LdapUtils.storeNotEmpty(attrs, "dicomInstitutionCode",
                device.getInstitutionCodes());
        LdapUtils.storeNotEmpty(attrs, "dicomInstitutionAddress",
                device.getInstitutionAddresses());
        LdapUtils.storeNotEmpty(attrs, "dicomInstitutionDepartmentName",
                device.getInstitutionalDepartmentNames());
        LdapUtils.storeNotEmpty(attrs, "dicomPrimaryDeviceType",
                device.getPrimaryDeviceTypes());
        LdapUtils.storeNotEmpty(attrs, "dicomRelatedDeviceReference",
                device.getRelatedDeviceRefs());
        LdapUtils.storeNotEmpty(attrs, "dicomAuthorizedNodeCertificateReference",
                device.getAuthorizedNodeCertificateRefs());
        LdapUtils.storeNotEmpty(attrs, "dicomThisNodeCertificateReference",
                device.getThisNodeCertificateRefs());
        storeNotEmpty(attrs, "dicomVendorData", device.getVendorData());
        LdapUtils.storeBoolean(attrs, "dicomInstalled", device.isInstalled());
        if (!extended)
            return attrs;

        objectclass.add("dcmDevice");
        LdapUtils.storeNotDef(attrs, "dcmLimitOpenAssociations", device.getLimitOpenAssociations(), 0);
        LdapUtils.storeNotNull(attrs, "dcmTrustStoreURL", device.getTrustStoreURL());
        LdapUtils.storeNotNull(attrs, "dcmTrustStoreType", device.getTrustStoreType());
        LdapUtils.storeNotNull(attrs, "dcmTrustStorePin", device.getTrustStorePin());
        LdapUtils.storeNotNull(attrs, "dcmTrustStorePinProperty", device.getTrustStorePinProperty());
        LdapUtils.storeNotNull(attrs, "dcmKeyStoreURL", device.getKeyStoreURL());
        LdapUtils.storeNotNull(attrs, "dcmKeyStoreType", device.getKeyStoreType());
        LdapUtils.storeNotNull(attrs, "dcmKeyStorePin", device.getKeyStorePin());
        LdapUtils.storeNotNull(attrs, "dcmKeyStorePinProperty", device.getKeyStorePinProperty());
        LdapUtils.storeNotNull(attrs, "dcmKeyStoreKeyPin", device.getKeyStoreKeyPin());
        LdapUtils.storeNotNull(attrs, "dcmKeyStoreKeyPinProperty", device.getKeyStoreKeyPinProperty());
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.storeTo(device, attrs);
        return attrs;
    }

    private Attributes storeTo(Connection conn, Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dicomNetworkConnection");
        attrs.put(objectclass);
        LdapUtils.storeNotNull(attrs, "cn", conn.getCommonName());
        LdapUtils.storeNotNull(attrs, "dicomHostname", conn.getHostname());
        LdapUtils.storeNotDef(attrs, "dicomPort", conn.getPort(), Connection.NOT_LISTENING);
        LdapUtils.storeNotEmpty(attrs, "dicomTLSCipherSuite", conn.getTlsCipherSuites());
        LdapUtils.storeNotNull(attrs, "dicomInstalled", conn.getInstalled());
        if (!extended)
            return attrs;

        objectclass.add("dcmNetworkConnection");
        LdapUtils.storeNotNull(attrs, "dcmProtocol", 
                StringUtils.nullify(conn.getProtocol(), Protocol.DICOM));
        LdapUtils.storeNotNull(attrs, "dcmHTTPProxy", conn.getHttpProxy());
        LdapUtils.storeNotEmpty(attrs, "dcmBlacklistedHostname", conn.getBlacklist());
        LdapUtils.storeNotDef(attrs, "dcmTCPBacklog",
                conn.getBacklog(), Connection.DEF_BACKLOG);
        LdapUtils.storeNotDef(attrs, "dcmTCPConnectTimeout",
                conn.getConnectTimeout(), Connection.NO_TIMEOUT);
        LdapUtils.storeNotDef(attrs, "dcmAARQTimeout",
                conn.getRequestTimeout(), Connection.NO_TIMEOUT);
        LdapUtils.storeNotDef(attrs, "dcmAAACTimeout",
                conn.getAcceptTimeout(), Connection.NO_TIMEOUT);
        LdapUtils.storeNotDef(attrs, "dcmARRPTimeout",
                conn.getReleaseTimeout(), Connection.NO_TIMEOUT);
        LdapUtils.storeNotDef(attrs, "dcmResponseTimeout",
                conn.getResponseTimeout(), Connection.NO_TIMEOUT);
        LdapUtils.storeNotDef(attrs, "dcmRetrieveTimeout",
                conn.getRetrieveTimeout(), Connection.NO_TIMEOUT);
        LdapUtils.storeNotDef(attrs, "dcmIdleTimeout",
                conn.getIdleTimeout(), Connection.NO_TIMEOUT);
        LdapUtils.storeNotDef(attrs, "dcmTCPCloseDelay",
                conn.getSocketCloseDelay(), Connection.DEF_SOCKETDELAY);
        LdapUtils.storeNotDef(attrs, "dcmTCPSendBufferSize",
                conn.getSendBufferSize(), Connection.DEF_BUFFERSIZE);
        LdapUtils.storeNotDef(attrs, "dcmTCPReceiveBufferSize",
                conn.getReceiveBufferSize(), Connection.DEF_BUFFERSIZE);
        LdapUtils.storeNotDef(attrs, "dcmTCPNoDelay", conn.isTcpNoDelay(), true);
        LdapUtils.storeNotDef(attrs, "dcmSendPDULength",
                conn.getSendPDULength(), Connection.DEF_MAX_PDU_LENGTH);
        LdapUtils.storeNotDef(attrs, "dcmReceivePDULength",
                conn.getReceivePDULength(), Connection.DEF_MAX_PDU_LENGTH);
        LdapUtils.storeNotDef(attrs, "dcmMaxOpsPerformed",
                conn.getMaxOpsPerformed(), Connection.SYNCHRONOUS_MODE);
        LdapUtils.storeNotDef(attrs, "dcmMaxOpsInvoked",
                conn.getMaxOpsInvoked(), Connection.SYNCHRONOUS_MODE);
        LdapUtils.storeNotDef(attrs, "dcmPackPDV", conn.isPackPDV(), true);
        if (conn.isTls()) {
            LdapUtils.storeNotEmpty(attrs, "dcmTLSProtocol", conn.getTlsProtocols());
            LdapUtils.storeNotDef(attrs, "dcmTLSNeedClientAuth", conn.isTlsNeedClientAuth(), true);
        }
        return attrs;
    }

    private Attributes storeTo(ApplicationEntity ae, String deviceDN, Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dicomNetworkAE");
        attrs.put(objectclass);
        LdapUtils.storeNotNull(attrs, "dicomAETitle", ae.getAETitle());
        LdapUtils.storeNotNull(attrs, "dicomDescription", ae.getDescription());
        storeNotEmpty(attrs, "dicomVendorData", ae.getVendorData());
        LdapUtils.storeNotEmpty(attrs, "dicomApplicationCluster", ae.getApplicationClusters());
        LdapUtils.storeNotEmpty(attrs, "dicomPreferredCallingAETitle", ae.getPreferredCallingAETitles());
        LdapUtils.storeNotEmpty(attrs, "dicomPreferredCalledAETitle", ae.getPreferredCalledAETitles());
        LdapUtils.storeBoolean(attrs, "dicomAssociationInitiator", ae.isAssociationInitiator());
        LdapUtils.storeBoolean(attrs, "dicomAssociationAcceptor",  ae.isAssociationAcceptor());
        LdapUtils.storeConnRefs(attrs, ae.getConnections(), deviceDN);
        LdapUtils.storeNotEmpty(attrs, "dicomSupportedCharacterSet", ae.getSupportedCharacterSets());
        LdapUtils.storeNotNull(attrs, "dicomInstalled", ae.getInstalled());
        if (!extended)
            return attrs;

        objectclass.add("dcmNetworkAE");
        LdapUtils.storeNotEmpty(attrs, "dcmAcceptedCallingAETitle", ae.getAcceptedCallingAETitles());
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.storeTo(ae, attrs);
        return attrs;
    }

    private Attributes storeTo(TransferCapability tc, Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dicomTransferCapability");
        attrs.put(objectclass);
        LdapUtils.storeNotNull(attrs, "cn", tc.getCommonName());
        LdapUtils.storeNotNull(attrs, "dicomSOPClass", tc.getSopClass());
        LdapUtils.storeNotNull(attrs, "dicomTransferRole", tc.getRole());
        LdapUtils.storeNotEmpty(attrs, "dicomTransferSyntax", tc.getTransferSyntaxes());
        if (!extended)
            return attrs;

        objectclass.add("dcmTransferCapability");
        EnumSet<QueryOption> queryOpts = tc.getQueryOptions();
        if (queryOpts != null) {
            LdapUtils.storeNotDef(attrs, "dcmRelationalQueries",
                    queryOpts.contains(QueryOption.RELATIONAL), false);
            LdapUtils.storeNotDef(attrs, "dcmCombinedDateTimeMatching",
                    queryOpts.contains(QueryOption.DATETIME), false);
            LdapUtils.storeNotDef(attrs, "dcmFuzzySemanticMatching",
                    queryOpts.contains(QueryOption.FUZZY), false);
            LdapUtils.storeNotDef(attrs, "dcmTimezoneQueryAdjustment",
                    queryOpts.contains(QueryOption.TIMEZONE), false);
        }
        StorageOptions storageOpts = tc.getStorageOptions();
        if (storageOpts != null) {
            LdapUtils.storeInt(attrs, "dcmStorageConformance",
                    storageOpts.getLevelOfSupport().ordinal());
            LdapUtils.storeInt(attrs, "dcmDigitalSignatureSupport",
                    storageOpts.getDigitalSignatureSupport().ordinal());
            LdapUtils.storeInt(attrs, "dcmDataElementCoercion",
                    storageOpts.getElementCoercion().ordinal());
        }
        return attrs;
    }

    @Override
    public synchronized void persistCertificates(String dn, X509Certificate... certs)
            throws ConfigurationException {
        try {
            storeCertificates(dn, certs);
        } catch (NameNotFoundException e) {
            throw new ConfigurationNotFoundException(e);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } catch (CertificateEncodingException e) {
            throw new ConfigurationException(e);
        }
    }

    private void storeCertificates(String dn, X509Certificate... certs)
            throws CertificateEncodingException, NamingException {
        byte[][] vals = new byte[certs.length][];
        for (int i = 0; i < vals.length; i++)
            vals[i] = certs[i].getEncoded();
        Attributes attrs = ctx.getAttributes(dn,
                new String[] { "objectClass" } );
        ModificationItem replaceCert = new ModificationItem(
                DirContext.REPLACE_ATTRIBUTE, attr(userCertificate, vals ));
        ctx.modifyAttributes(dn, 
                LdapUtils.hasObjectClass(attrs, pkiUser)
                     ? new ModificationItem[] { replaceCert }
                     : new ModificationItem[] {
                             new ModificationItem(
                                 DirContext.ADD_ATTRIBUTE,
                                 LdapUtils.attr("objectClass", pkiUser )),
                             replaceCert });
    }

    @Override
    public synchronized void removeCertificates(String dn) throws ConfigurationException {
        try {
            ModificationItem removeCert = new ModificationItem(
                    DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(userCertificate));
            ctx.modifyAttributes(dn, new ModificationItem[] { removeCert });
        } catch (NameNotFoundException e) {
            throw new ConfigurationNotFoundException(e);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public synchronized X509Certificate[] findCertificates(String dn) throws ConfigurationException {
        try {
            return loadCertificates(dn);
        } catch (NameNotFoundException e) {
            throw new ConfigurationNotFoundException(e);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } catch (CertificateException e) {
            throw new ConfigurationException(e);
        }
    }

    private X509Certificate[] loadCertificates(String dn)
            throws NamingException, CertificateException {
        Attributes attrs = ctx.getAttributes(dn, new String[] { userCertificate } );
        Attribute attr = attrs.get(userCertificate);
        if (attr == null)
            return EMPTY_X509_CERTIFICATES;
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        X509Certificate[] certs = new X509Certificate[attr.size()];
        for (int i = 0; i < certs.length; i++)
            certs[i] = (X509Certificate) cf.generateCertificate(
                    new ByteArrayInputStream((byte[]) attr.get(i)));

        return certs;
    }

    public Device loadDevice(String deviceDN) throws ConfigurationException {
        try {
            Attributes attrs = getAttributes(deviceDN);
            Device device = new Device(LdapUtils.stringValue(attrs.get("dicomDeviceName"), null));
            loadFrom(device, attrs);
            loadChilds(device, deviceDN);
            return device;
        } catch (NameNotFoundException e) {
            throw new ConfigurationNotFoundException(e);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } catch (CertificateException e) {
            throw new ConfigurationException(e);
        }
    }

    public Attributes getAttributes(String name) throws NamingException {
        return ctx.getAttributes(name);
    }

    private void loadChilds(Device device, String deviceDN)
            throws NamingException, ConfigurationException {
        loadConnections(device, deviceDN);
        loadApplicationEntities(device, deviceDN);
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.loadChilds(device, deviceDN);
    }

    private void loadFrom(TransferCapability tc, Attributes attrs) throws NamingException {
        tc.setCommonName(LdapUtils.stringValue(attrs.get("cn"), null));
        tc.setSopClass(LdapUtils.stringValue(attrs.get("dicomSOPClass"), null));
        tc.setRole(TransferCapability.Role.valueOf(
                LdapUtils.stringValue(attrs.get("dicomTransferRole"), null)));
        tc.setTransferSyntaxes(LdapUtils.stringArray(attrs.get("dicomTransferSyntax")));
        if (!LdapUtils.hasObjectClass(attrs, "dcmTransferCapability"))
            return;

        tc.setQueryOptions(toQueryOptions(attrs));
        tc.setStorageOptions(toStorageOptions(attrs));
    }

    private static EnumSet<QueryOption> toQueryOptions(Attributes attrs)
            throws NamingException {
        Attribute relational = attrs.get("dcmRelationalQueries");
        Attribute datetime = attrs.get("dcmCombinedDateTimeMatching");
        Attribute fuzzy = attrs.get("dcmFuzzySemanticMatching");
        Attribute timezone = attrs.get("dcmTimezoneQueryAdjustment");
        if (relational == null && datetime == null && fuzzy == null && timezone == null)
            return null;
        EnumSet<QueryOption> opts = EnumSet.noneOf(QueryOption.class);
        if (LdapUtils.booleanValue(relational, false))
            opts.add(QueryOption.RELATIONAL);
        if (LdapUtils.booleanValue(datetime, false))
            opts.add(QueryOption.DATETIME);
        if (LdapUtils.booleanValue(fuzzy, false))
            opts.add(QueryOption.FUZZY);
        if (LdapUtils.booleanValue(timezone, false))
            opts.add(QueryOption.TIMEZONE);
        return opts ;
     }

    private static StorageOptions toStorageOptions(Attributes attrs) throws NamingException {
        Attribute levelOfSupport = attrs.get("dcmStorageConformance");
        Attribute signatureSupport = attrs.get("dcmDigitalSignatureSupport");
        Attribute coercion = attrs.get("dcmDataElementCoercion");
        if (levelOfSupport == null && signatureSupport == null && coercion == null)
            return null;
        StorageOptions opts = new StorageOptions();
        opts.setLevelOfSupport(
                StorageOptions.LevelOfSupport.valueOf(LdapUtils.intValue(levelOfSupport, 3)));
        opts.setDigitalSignatureSupport(
                StorageOptions.DigitalSignatureSupport.valueOf(LdapUtils.intValue(signatureSupport, 0)));
        opts.setElementCoercion(
                StorageOptions.ElementCoercion.valueOf(LdapUtils.intValue(coercion, 2)));
        return opts;
    }

    private void loadFrom(Device device, Attributes attrs)
            throws NamingException, CertificateException {
        device.setDescription(LdapUtils.stringValue(attrs.get("dicomDescription"), null));
        device.setManufacturer(LdapUtils.stringValue(attrs.get("dicomManufacturer"), null));
        device.setManufacturerModelName(LdapUtils.stringValue(attrs.get("dicomManufacturerModelName"), null));
        device.setSoftwareVersions(LdapUtils.stringArray(attrs.get("dicomSoftwareVersion")));
        device.setStationName(LdapUtils.stringValue(attrs.get("dicomStationName"), null));
        device.setDeviceSerialNumber(LdapUtils.stringValue(attrs.get("dicomDeviceSerialNumber"), null));
        device.setIssuerOfPatientID(
                issuerValue(attrs.get("dicomIssuerOfPatientID")));
        device.setIssuerOfAccessionNumber(
                issuerValue(attrs.get("dicomIssuerOfAccessionNumber")));
        device.setOrderPlacerIdentifier(
                issuerValue(attrs.get("dicomOrderPlacerIdentifier")));
        device.setOrderFillerIdentifier(
                issuerValue(attrs.get("dicomOrderFillerIdentifier")));
        device.setIssuerOfAdmissionID(
                issuerValue(attrs.get("dicomIssuerOfAdmissionID")));
        device.setIssuerOfServiceEpisodeID(
                issuerValue(attrs.get("dicomIssuerOfServiceEpisodeID")));
        device.setIssuerOfContainerIdentifier(
                issuerValue(attrs.get("dicomIssuerOfContainerIdentifier")));
        device.setIssuerOfSpecimenIdentifier(
                issuerValue(attrs.get("dicomIssuerOfSpecimenIdentifier")));
        device.setInstitutionNames(LdapUtils.stringArray(attrs.get("dicomInstitutionName")));
        device.setInstitutionCodes(codeArray(attrs.get("dicomInstitutionCode")));
        device.setInstitutionAddresses(LdapUtils.stringArray(attrs.get("dicomInstitutionAddress")));
        device.setInstitutionalDepartmentNames(
                LdapUtils.stringArray(attrs.get("dicomInstitutionDepartmentName")));
        device.setPrimaryDeviceTypes(LdapUtils.stringArray(attrs.get("dicomPrimaryDeviceType")));
        device.setRelatedDeviceRefs(LdapUtils.stringArray(attrs.get("dicomRelatedDeviceReference")));
        for (String dn : LdapUtils.stringArray(attrs.get("dicomAuthorizedNodeCertificateReference")))
            device.setAuthorizedNodeCertificates(dn, loadCertificates(dn));
        for (String dn : LdapUtils.stringArray(attrs.get("dicomThisNodeCertificateReference")))
            device.setThisNodeCertificates(dn, loadCertificates(dn));
        device.setVendorData(byteArrays(attrs.get("dicomVendorData")));
        device.setInstalled(LdapUtils.booleanValue(attrs.get("dicomInstalled"), true));
        if (!LdapUtils.hasObjectClass(attrs, "dcmDevice"))
            return;
        
        device.setLimitOpenAssociations(
                LdapUtils.intValue(attrs.get("dcmLimitOpenAssociations"), 0));
        device.setTrustStoreURL(LdapUtils.stringValue(attrs.get("dcmTrustStoreURL"), null));
        device.setTrustStoreType(LdapUtils.stringValue(attrs.get("dcmTrustStoreType"), null));
        device.setTrustStorePin(LdapUtils.stringValue(attrs.get("dcmTrustStorePin"), null));
        device.setTrustStorePinProperty(
                LdapUtils.stringValue(attrs.get("dcmTrustStorePinProperty"), null));
        device.setKeyStoreURL(LdapUtils.stringValue(attrs.get("dcmKeyStoreURL"), null));
        device.setKeyStoreType(LdapUtils.stringValue(attrs.get("dcmKeyStoreType"), null));
        device.setKeyStorePin(LdapUtils.stringValue(attrs.get("dcmKeyStorePin"), null));
        device.setKeyStorePinProperty(
                LdapUtils.stringValue(attrs.get("dcmKeyStorePinProperty"), null));
        device.setKeyStoreKeyPin(LdapUtils.stringValue(attrs.get("dcmKeyStoreKeyPin"), null));
        device.setKeyStoreKeyPinProperty(
                LdapUtils.stringValue(attrs.get("dcmKeyStoreKeyPinProperty"), null));
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.loadFrom(device, attrs);
    }

    private void loadConnections(Device device, String deviceDN) throws NamingException {
        NamingEnumeration<SearchResult> ne = 
                search(deviceDN, "(objectclass=dicomNetworkConnection)");
        try {
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attributes attrs = sr.getAttributes();
                Connection conn = new Connection();
                loadFrom(conn, attrs);
                device.addConnection(conn);
            }
        } finally {
           LdapUtils.safeClose(ne);
        }
    }

    public NamingEnumeration<SearchResult> search(String dn, String filter)
            throws NamingException {
        return  search(dn, filter, (String[]) null);
    }

    private NamingEnumeration<SearchResult> search(String dn, String filter,
            String... attrs) throws NamingException {
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        ctls.setReturningObjFlag(false);
        ctls.setReturningAttributes(attrs);
        return ctx.search(dn, filter, ctls);
    }

    private void loadFrom(Connection conn, Attributes attrs) throws NamingException {
        conn.setCommonName(LdapUtils.stringValue(attrs.get("cn"), null));
        conn.setHostname(LdapUtils.stringValue(attrs.get("dicomHostname"), null));
        conn.setPort(LdapUtils.intValue(attrs.get("dicomPort"), Connection.NOT_LISTENING));
        conn.setTlsCipherSuites(LdapUtils.stringArray(attrs.get("dicomTLSCipherSuite")));
        conn.setInstalled(LdapUtils.booleanValue(attrs.get("dicomInstalled"), null));
        if (!LdapUtils.hasObjectClass(attrs, "dcmNetworkConnection"))
            return;

        conn.setProtocol(Protocol.valueOf(LdapUtils.stringValue(attrs.get("dcmProtocol"), "DICOM")));
        conn.setHttpProxy(LdapUtils.stringValue(attrs.get("dcmHTTPProxy"), null));
        conn.setBlacklist(LdapUtils.stringArray(attrs.get("dcmBlacklistedHostname")));
        conn.setBacklog(LdapUtils.intValue(attrs.get("dcmTCPBacklog"), Connection.DEF_BACKLOG));
        conn.setConnectTimeout(LdapUtils.intValue(attrs.get("dcmTCPConnectTimeout"),
                Connection.NO_TIMEOUT));
        conn.setRequestTimeout(LdapUtils.intValue(attrs.get("dcmAARQTimeout"),
                Connection.NO_TIMEOUT));
        conn.setAcceptTimeout(LdapUtils.intValue(attrs.get("dcmAAACTimeout"),
                Connection.NO_TIMEOUT));
        conn.setReleaseTimeout(LdapUtils.intValue(attrs.get("dcmARRPTimeout"),
                Connection.NO_TIMEOUT));
        conn.setResponseTimeout(LdapUtils.intValue(attrs.get("dcmResponseTimeout"),
                Connection.NO_TIMEOUT));
        conn.setRetrieveTimeout(LdapUtils.intValue(attrs.get("dcmRetrieveTimeout"),
                Connection.NO_TIMEOUT));
        conn.setIdleTimeout(LdapUtils.intValue(attrs.get("dcmIdleTimeout"),
                Connection.NO_TIMEOUT));
        conn.setSocketCloseDelay(LdapUtils.intValue(attrs.get("dcmTCPCloseDelay"),
                Connection.DEF_SOCKETDELAY));
        conn.setSendBufferSize(LdapUtils.intValue(attrs.get("dcmTCPSendBufferSize"),
                Connection.DEF_BUFFERSIZE));
        conn.setReceiveBufferSize(LdapUtils.intValue(attrs.get("dcmTCPReceiveBufferSize"),
                Connection.DEF_BUFFERSIZE));
        conn.setTcpNoDelay(LdapUtils.booleanValue(attrs.get("dcmTCPNoDelay"), true));
        conn.setTlsNeedClientAuth(LdapUtils.booleanValue(attrs.get("dcmTLSNeedClientAuth"), true));
        String[] tlsProtocols = LdapUtils.stringArray(attrs.get("dcmTLSProtocol"));
        if (tlsProtocols.length > 0)
            conn.setTlsProtocols(tlsProtocols);
        conn.setSendPDULength(LdapUtils.intValue(attrs.get("dcmSendPDULength"),
                Connection.DEF_MAX_PDU_LENGTH));
        conn.setReceivePDULength(LdapUtils.intValue(attrs.get("dcmReceivePDULength"),
                Connection.DEF_MAX_PDU_LENGTH));
        conn.setMaxOpsPerformed(LdapUtils.intValue(attrs.get("dcmMaxOpsPerformed"),
                Connection.SYNCHRONOUS_MODE));
        conn.setMaxOpsInvoked(LdapUtils.intValue(attrs.get("dcmMaxOpsInvoked"),
                Connection.SYNCHRONOUS_MODE));
        conn.setPackPDV(LdapUtils.booleanValue(attrs.get("dcmPackPDV"), true));
    }

    private void loadApplicationEntities(Device device, String deviceDN)
            throws NamingException {
        NamingEnumeration<SearchResult> ne =
                search(deviceDN, "(objectclass=dicomNetworkAE)");
        try {
            while (ne.hasMore()) {
                device.addApplicationEntity(
                        loadApplicationEntity(ne.next(), deviceDN, device));
            }
        } finally {
           LdapUtils.safeClose(ne);
        }
    }

    private ApplicationEntity loadApplicationEntity(SearchResult sr,
            String deviceDN, Device device) throws NamingException {
        Attributes attrs = sr.getAttributes();
        ApplicationEntity ae = new ApplicationEntity(LdapUtils.stringValue(attrs.get("dicomAETitle"), null));
        loadFrom(ae, attrs);
        for (String connDN : LdapUtils.stringArray(attrs.get("dicomNetworkConnectionReference")))
            ae.addConnection(LdapUtils.findConnection(connDN, deviceDN, device));
        loadChilds(ae, sr.getNameInNamespace());
        return ae ;
    }

    private void loadFrom(ApplicationEntity ae, Attributes attrs) throws NamingException {
        ae.setDescription(LdapUtils.stringValue(attrs.get("dicomDescription"), null));
        ae.setVendorData(byteArrays(attrs.get("dicomVendorData")));
        ae.setApplicationClusters(LdapUtils.stringArray(attrs.get("dicomApplicationCluster")));
        ae.setPreferredCallingAETitles(LdapUtils.stringArray(attrs.get("dicomPreferredCallingAETitle")));
        ae.setPreferredCalledAETitles(LdapUtils.stringArray(attrs.get("dicomPreferredCalledAETitle")));
        ae.setAssociationInitiator(LdapUtils.booleanValue(attrs.get("dicomAssociationInitiator"), false));
        ae.setAssociationAcceptor(LdapUtils.booleanValue(attrs.get("dicomAssociationAcceptor"), false));
        ae.setSupportedCharacterSets(LdapUtils.stringArray(attrs.get("dicomSupportedCharacterSet")));
        ae.setInstalled(LdapUtils.booleanValue(attrs.get("dicomInstalled"), null));
        if (!LdapUtils.hasObjectClass(attrs, "dcmNetworkAE"))
            return;

        ae.setAcceptedCallingAETitles(LdapUtils.stringArray(attrs.get("dcmAcceptedCallingAETitle")));
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.loadFrom(ae, attrs);
    }

    private void loadChilds(ApplicationEntity ae, String aeDN) throws NamingException {
        loadTransferCapabilities(ae, aeDN);
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.loadChilds(ae, aeDN);
    }

    private void loadTransferCapabilities(ApplicationEntity ae, String aeDN)
            throws NamingException {
        NamingEnumeration<SearchResult> ne =
                search(aeDN, "(objectclass=dicomTransferCapability)");
        try {
            while (ne.hasMore())
                ae.addTransferCapability(loadTransferCapability(ne.next()));
        } finally {
           LdapUtils.safeClose(ne);
        }
    }

    private TransferCapability loadTransferCapability(SearchResult sr)
            throws NamingException {
        Attributes attrs = sr.getAttributes();
        TransferCapability tc = new TransferCapability();
        loadFrom(tc, attrs);
        return tc;
    }

    private List<ModificationItem> storeDiffs(Device a, Device b,
            List<ModificationItem> mods) {
        LdapUtils.storeDiff(mods, "dicomDescription",
                a.getDescription(),
                b.getDescription());
        LdapUtils.storeDiff(mods, "dicomManufacturer",
                a.getManufacturer(),
                b.getManufacturer());
        LdapUtils.storeDiff(mods, "dicomManufacturerModelName",
                a.getManufacturerModelName(),
                b.getManufacturerModelName());
        LdapUtils.storeDiff(mods, "dicomSoftwareVersion",
                a.getSoftwareVersions(),
                b.getSoftwareVersions());
        LdapUtils.storeDiff(mods, "dicomStationName",
                a.getStationName(),
                b.getStationName());
        LdapUtils.storeDiff(mods, "dicomDeviceSerialNumber",
                a.getDeviceSerialNumber(),
                b.getDeviceSerialNumber());
        LdapUtils.storeDiff(mods, "dicomIssuerOfPatientID",
                a.getIssuerOfPatientID(),
                b.getIssuerOfPatientID());
        LdapUtils.storeDiff(mods, "dicomIssuerOfAccessionNumber",
                a.getIssuerOfAccessionNumber(),
                b.getIssuerOfAccessionNumber());
        LdapUtils.storeDiff(mods, "dicomOrderPlacerIdentifier",
                a.getOrderPlacerIdentifier(),
                b.getOrderPlacerIdentifier());
        LdapUtils.storeDiff(mods, "dicomOrderFillerIdentifier",
                a.getOrderFillerIdentifier(),
                b.getOrderFillerIdentifier());
        LdapUtils.storeDiff(mods, "dicomIssuerOfAdmissionID",
                a.getIssuerOfAdmissionID(),
                b.getIssuerOfAdmissionID());
        LdapUtils.storeDiff(mods, "dicomIssuerOfServiceEpisodeID",
                a.getIssuerOfServiceEpisodeID(),
                b.getIssuerOfServiceEpisodeID());
        LdapUtils.storeDiff(mods, "dicomIssuerOfContainerIdentifier",
                a.getIssuerOfContainerIdentifier(),
                b.getIssuerOfContainerIdentifier());
        LdapUtils.storeDiff(mods, "dicomIssuerOfSpecimenIdentifier",
                a.getIssuerOfSpecimenIdentifier(),
                b.getIssuerOfSpecimenIdentifier());
        LdapUtils.storeDiff(mods, "dicomInstitutionName",
                a.getInstitutionNames(),
                b.getInstitutionNames());
        LdapUtils.storeDiff(mods, "dicomInstitutionCode",
                a.getInstitutionCodes(),
                b.getInstitutionCodes());
        LdapUtils.storeDiff(mods, "dicomInstitutionAddress",
                a.getInstitutionAddresses(),
                b.getInstitutionAddresses());
        LdapUtils.storeDiff(mods, "dicomInstitutionDepartmentName",
                a.getInstitutionalDepartmentNames(),
                b.getInstitutionalDepartmentNames());
        LdapUtils.storeDiff(mods, "dicomPrimaryDeviceType",
                a.getPrimaryDeviceTypes(),
                b.getPrimaryDeviceTypes());
        LdapUtils.storeDiff(mods, "dicomRelatedDeviceReference",
                a.getRelatedDeviceRefs(),
                b.getRelatedDeviceRefs());
        LdapUtils.storeDiff(mods, "dicomAuthorizedNodeCertificateReference",
                a.getAuthorizedNodeCertificateRefs(),
                b.getAuthorizedNodeCertificateRefs());
        LdapUtils.storeDiff(mods, "dicomThisNodeCertificateReference",
                a.getThisNodeCertificateRefs(),
                b.getThisNodeCertificateRefs());
        storeDiff(mods, "dicomVendorData",
                a.getVendorData(),
                b.getVendorData());
        LdapUtils.storeDiff(mods, "dicomInstalled",
                a.isInstalled(),
                b.isInstalled());
        if (!extended)
            return mods;

        LdapUtils.storeDiff(mods, "dcmLimitOpenAssociations",
                a.getLimitOpenAssociations(),
                b.getLimitOpenAssociations());
        LdapUtils.storeDiff(mods, "dcmTrustStoreURL",
                a.getTrustStoreURL(),
                b.getTrustStoreURL());
        LdapUtils.storeDiff(mods, "dcmTrustStoreType",
                a.getTrustStoreType(),
                b.getTrustStoreType());
        LdapUtils.storeDiff(mods, "dcmTrustStorePin",
                a.getTrustStorePin(),
                b.getTrustStorePin());
        LdapUtils.storeDiff(mods, "dcmTrustStorePinProperty",
                a.getTrustStorePinProperty(),
                b.getTrustStorePinProperty());
        LdapUtils.storeDiff(mods, "dcmKeyStoreURL",
                a.getKeyStoreURL(),
                b.getKeyStoreURL());
        LdapUtils.storeDiff(mods, "dcmKeyStoreType",
                a.getKeyStoreType(),
                b.getKeyStoreType());
        LdapUtils.storeDiff(mods, "dcmKeyStorePin",
                a.getKeyStorePin(),
                b.getKeyStorePin());
        LdapUtils.storeDiff(mods, "dcmKeyStorePinProperty",
                a.getKeyStorePinProperty(),
                b.getKeyStorePinProperty());
        LdapUtils.storeDiff(mods, "dcmKeyStoreKeyPin",
                a.getKeyStoreKeyPin(),
                b.getKeyStoreKeyPin());
        LdapUtils.storeDiff(mods, "dcmKeyStoreKeyPinProperty",
                a.getKeyStoreKeyPinProperty(),
                b.getKeyStoreKeyPinProperty());
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.storeDiffs(a, b, mods);
        return mods;
    }

    private List<ModificationItem> storeDiffs(Connection a, Connection b,
            List<ModificationItem> mods) {
        LdapUtils.storeDiff(mods, "dicomHostname",
                a.getHostname(),
                b.getHostname());
        LdapUtils.storeDiff(mods, "dicomPort",
                a.getPort(),
                b.getPort(),
                Connection.NOT_LISTENING);
        LdapUtils.storeDiff(mods, "dicomTLSCipherSuite",
                a.getTlsCipherSuites(),
                b.getTlsCipherSuites());
        LdapUtils.storeDiff(mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled());
        if (!extended)
            return mods;

        LdapUtils.storeDiff(mods, "dcmProtocol",
                StringUtils.nullify(a.getProtocol(), Protocol.DICOM),
                StringUtils.nullify(b.getProtocol(), Protocol.DICOM));
        LdapUtils.storeDiff(mods, "dcmHTTPProxy",
                a.getHttpProxy(),
                b.getHttpProxy());
        LdapUtils.storeDiff(mods, "dcmBlacklistedHostname",
                a.getBlacklist(),
                b.getBlacklist());
        LdapUtils.storeDiff(mods, "dcmTCPBacklog",
                a.getBacklog(),
                b.getBacklog(),
                Connection.DEF_BACKLOG);
        LdapUtils.storeDiff(mods, "dcmTCPConnectTimeout",
                a.getConnectTimeout(),
                b.getConnectTimeout(),
                Connection.NO_TIMEOUT);
        LdapUtils.storeDiff(mods, "dcmAARQTimeout",
                a.getRequestTimeout(),
                b.getRequestTimeout(),
                Connection.NO_TIMEOUT);
        LdapUtils.storeDiff(mods, "dcmAAACTimeout",
                a.getAcceptTimeout(),
                b.getAcceptTimeout(),
                Connection.NO_TIMEOUT);
        LdapUtils.storeDiff(mods, "dcmARRPTimeout",
                a.getReleaseTimeout(),
                b.getReleaseTimeout(),
                Connection.NO_TIMEOUT);
        LdapUtils.storeDiff(mods, "dcmResponseTimeout",
                a.getResponseTimeout(),
                b.getResponseTimeout(),
                Connection.NO_TIMEOUT);
        LdapUtils.storeDiff(mods, "dcmRetrieveTimeout",
                a.getRetrieveTimeout(),
                b.getRetrieveTimeout(),
                Connection.NO_TIMEOUT);
        LdapUtils.storeDiff(mods, "dcmIdleTimeout",
                a.getIdleTimeout(),
                b.getIdleTimeout(),
                Connection.NO_TIMEOUT);
        LdapUtils.storeDiff(mods, "dcmTCPCloseDelay",
                a.getSocketCloseDelay(),
                b.getSocketCloseDelay(),
                Connection.DEF_SOCKETDELAY);
        LdapUtils.storeDiff(mods, "dcmTCPSendBufferSize",
                a.getSendBufferSize(),
                b.getSendBufferSize(),
                Connection.DEF_BUFFERSIZE);
        LdapUtils.storeDiff(mods, "dcmTCPReceiveBufferSize",
                a.getReceiveBufferSize(),
                b.getReceiveBufferSize(),
                Connection.DEF_BUFFERSIZE);
        LdapUtils.storeDiff(mods, "dcmTCPNoDelay",
                a.isTcpNoDelay(),
                b.isTcpNoDelay(),
                true);
        LdapUtils.storeDiff(mods, "dcmTLSProtocol",
                a.isTls() ? a.getTlsProtocols() : StringUtils.EMPTY_STRING,
                b.isTls() ? b.getTlsProtocols() : StringUtils.EMPTY_STRING);
        LdapUtils.storeDiff(mods, "dcmTLSNeedClientAuth",
                !a.isTls() || a.isTlsNeedClientAuth(),
                !a.isTls() || a.isTlsNeedClientAuth(),
                true);
        LdapUtils.storeDiff(mods, "dcmSendPDULength",
                a.getSendPDULength(),
                b.getSendPDULength(),
                Connection.DEF_MAX_PDU_LENGTH);
        LdapUtils.storeDiff(mods, "dcmReceivePDULength",
                a.getReceivePDULength(),
                b.getReceivePDULength(),
                Connection.DEF_MAX_PDU_LENGTH);
        LdapUtils.storeDiff(mods, "dcmMaxOpsPerformed",
                a.getMaxOpsPerformed(),
                b.getMaxOpsPerformed(),
                Connection.SYNCHRONOUS_MODE);
        LdapUtils.storeDiff(mods, "dcmMaxOpsInvoked",
                a.getMaxOpsInvoked(),
                b.getMaxOpsInvoked(),
                Connection.SYNCHRONOUS_MODE);
        LdapUtils.storeDiff(mods, "dcmPackPDV",
                a.isPackPDV(),
                b.isPackPDV(),
                true);
        return mods;
    }

    private List<ModificationItem> storeDiffs(ApplicationEntity a,
            ApplicationEntity b, String deviceDN, List<ModificationItem> mods) {
        LdapUtils.storeDiff(mods, "dicomDescription",
                a.getDescription(),
                b.getDescription());
        storeDiff(mods, "dicomVendorData",
                a.getVendorData(),
                b.getVendorData());
        LdapUtils.storeDiff(mods, "dicomApplicationCluster",
                a.getApplicationClusters(),
                b.getApplicationClusters());
        LdapUtils.storeDiff(mods, "dicomPreferredCallingAETitle",
                a.getPreferredCallingAETitles(),
                b.getPreferredCallingAETitles());
        LdapUtils.storeDiff(mods, "dicomPreferredCalledAETitle",
                a.getPreferredCalledAETitles(),
                b.getPreferredCalledAETitles());
        LdapUtils.storeDiff(mods, "dicomAssociationInitiator",
                a.isAssociationInitiator(),
                b.isAssociationInitiator());
        LdapUtils.storeDiff(mods, "dicomAssociationAcceptor",
                a.isAssociationAcceptor(),
                b.isAssociationAcceptor());
        LdapUtils.storeDiff(mods, "dicomNetworkConnectionReference",
                a.getConnections(),
                b.getConnections(),
                deviceDN);
        LdapUtils.storeDiff(mods, "dicomSupportedCharacterSet",
                a.getSupportedCharacterSets(),
                b.getSupportedCharacterSets());
        LdapUtils.storeDiff(mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled());
        if (!extended)
            return mods;

        LdapUtils.storeDiff(mods, "dcmAcceptedCallingAETitle",
                a.getAcceptedCallingAETitles(),
                b.getAcceptedCallingAETitles());
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.storeDiffs(a, b, mods);
        return mods;
    }

    private List<ModificationItem> storeDiffs(TransferCapability a,
            TransferCapability b, List<ModificationItem> mods) {
        LdapUtils.storeDiff(mods, "dicomSOPClass",
                a.getSopClass(),
                b.getSopClass());
        LdapUtils.storeDiff(mods, "dicomTransferRole",
                a.getRole(),
                b.getRole());
        LdapUtils.storeDiff(mods, "dicomTransferSyntax",
                a.getTransferSyntaxes(),
                b.getTransferSyntaxes());
        if (!extended)
            return mods;

        storeDiffs(a.getQueryOptions(), b.getQueryOptions(), mods);
        storeDiffs(a.getStorageOptions(), b.getStorageOptions(), mods);
        return mods;
    }

    private void storeDiffs(EnumSet<QueryOption> prev,
            EnumSet<QueryOption> val, List<ModificationItem> mods) {
        if (prev != null ? prev.equals(val) : val == null)
            return;

        LdapUtils.storeDiff(mods, "dcmRelationalQueries",
                prev != null && prev.contains(QueryOption.RELATIONAL),
                val != null && val.contains(QueryOption.RELATIONAL),
                false);
        LdapUtils.storeDiff(mods, "dcmCombinedDateTimeMatching",
                prev != null && prev.contains(QueryOption.DATETIME),
                val != null && val.contains(QueryOption.DATETIME),
                false);
        LdapUtils.storeDiff(mods, "dcmFuzzySemanticMatching",
                prev != null && prev.contains(QueryOption.FUZZY),
                val != null && val.contains(QueryOption.FUZZY),
                false);
        LdapUtils.storeDiff(mods, "dcmTimezoneQueryAdjustment",
                prev != null && prev.contains(QueryOption.TIMEZONE),
                val != null && val.contains(QueryOption.TIMEZONE),
                false);
    }

    private void storeDiffs(StorageOptions prev,
            StorageOptions val, List<ModificationItem> mods) {
        if (prev != null ? prev.equals(val) : val == null)
            return;

        LdapUtils.storeDiff(mods, "dcmStorageConformance",
                prev != null ? prev.getLevelOfSupport().ordinal() : -1,
                val != null ? val.getLevelOfSupport().ordinal() : -1,
                -1);
        LdapUtils.storeDiff(mods, "dcmDigitalSignatureSupport",
                prev != null ? prev.getDigitalSignatureSupport().ordinal() : -1,
                val != null ? val.getDigitalSignatureSupport().ordinal() : -1,
                -1);
        LdapUtils.storeDiff(mods, "dcmDataElementCoercion",
                prev != null ? prev.getElementCoercion().ordinal() : -1,
                val != null ? val.getElementCoercion().ordinal() : -1,
                -1);
    }

    private static byte[][] byteArrays(Attribute attr) throws NamingException {
        if (attr == null)
            return new byte[0][];

        byte[][] bb = new byte[attr.size()][];
        for (int i = 0; i < bb.length; i++)
            bb[i] = (byte[]) attr.get(i);

        return bb;
    }

    private Code[] codeArray(Attribute attr) throws NamingException {
        if (attr == null)
            return EMPTY_CODES;

        Code[] codes = new Code[attr.size()];
        for (int i = 0; i < codes.length; i++)
            codes[i] = new Code((String) attr.get(i));

        return codes;
    }

    private static Issuer issuerValue(Attribute attr) throws NamingException {
        return attr != null ? new Issuer((String) attr.get()) : null;
    }

    private void mergeAEs(Device prevDev, Device dev, String deviceDN)
            throws NamingException {
        Collection<String> aets = dev.getApplicationAETitles();
        for (String aet : prevDev.getApplicationAETitles()) {
            if (!aets.contains(aet))
                destroySubcontextWithChilds(aetDN(aet, deviceDN));
        }
        Collection<String> prevAETs = prevDev.getApplicationAETitles();
        for (ApplicationEntity ae : dev.getApplicationEntities()) {
            String aet = ae.getAETitle();
            if (!prevAETs.contains(aet)) {
                String aeDN = aetDN(ae.getAETitle(), deviceDN);
                createSubcontext(aeDN,
                        storeTo(ae, deviceDN, new BasicAttributes(true)));
                storeChilds(aeDN, ae);
            } else
                merge(prevDev.getApplicationEntity(aet), ae, deviceDN);
        }
    }

    private void merge(ApplicationEntity prev, ApplicationEntity ae,
            String deviceDN) throws NamingException {
        String aeDN = aetDN(ae.getAETitle(), deviceDN);
        modifyAttributes(aeDN, storeDiffs(prev, ae, deviceDN, new ArrayList<ModificationItem>()));
        mergeChilds(prev, ae, aeDN);
    }

    private void mergeChilds(ApplicationEntity prev, ApplicationEntity ae,
            String aeDN) throws NamingException {
        merge(prev.getTransferCapabilities(), ae.getTransferCapabilities(), aeDN);
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.mergeChilds(prev, ae, aeDN);
    }

    public void modifyAttributes(String dn, List<ModificationItem> mods)
            throws NamingException {
        if (!mods.isEmpty())
            ctx.modifyAttributes(dn, mods.toArray(new ModificationItem[mods.size()]));
    }

    private void merge(Collection<TransferCapability> prevs,
            Collection<TransferCapability> tcs, String aeDN) throws NamingException {
        for (TransferCapability tc : prevs) {
            String dn = dnOf(tc, aeDN);
            if (findByDN(aeDN, tcs, dn) == null)
                destroySubcontext(dn);
        }
        for (TransferCapability tc : tcs) {
            String dn = dnOf(tc, aeDN);
            TransferCapability prev = findByDN(aeDN, prevs, dn);
            if (prev == null)
                createSubcontext(dn, storeTo(tc, new BasicAttributes(true)));
            else
                modifyAttributes(dn, storeDiffs(prev, tc, new ArrayList<ModificationItem>()));
        }
    }

    private void mergeConnections(Device prevDev, Device device, String deviceDN)
            throws NamingException {
        List<Connection> prevs = prevDev.listConnections();
        List<Connection> conns = device.listConnections();
        for (Connection prev : prevs) {
            String dn = LdapUtils.dnOf(prev, deviceDN);
            if (LdapUtils.findByDN(deviceDN, conns, dn) == null)
                destroySubcontext(dn);
        }
        for (Connection conn : conns) {
            String dn = LdapUtils.dnOf(conn, deviceDN);
            Connection prev = LdapUtils.findByDN(deviceDN, prevs, dn);
            if (prev == null)
                createSubcontext(dn, storeTo(conn, new BasicAttributes(true)));
            else
                modifyAttributes(dn, storeDiffs(prev, conn, new ArrayList<ModificationItem>()));
        }
    }

    private static TransferCapability findByDN(String aeDN, 
            Collection<TransferCapability> tcs, String dn) {
        for (TransferCapability tc : tcs)
            if (dn.equals(dnOf(tc, aeDN)))
                return tc;
        return null;
    }

    private static void storeDiff(List<ModificationItem> mods, String attrId,
            byte[][] prevs, byte[][] vals) {
        if (!equals(prevs, vals))
            mods.add((vals.length == 0)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            attr(attrId, vals)));
    }

    private static boolean equals(byte[][] a, byte[][] a2) {
        int length = a.length;
        if (a2.length != length)
            return false;

        outer:
        for (byte[] o1 : a) {
            for (byte[] o2 : a2)
                if (Arrays.equals(o1, o2))
                    continue outer;
            return false;
        }
        return true;
    }

    private static String aetDN(String aet, String parentDN) {
        return LdapUtils.dnOf("dicomAETitle" ,aet, parentDN);
    }

    @Override
    public String deviceRef(String name) {
        return LdapUtils.dnOf("dicomDeviceName" ,name, devicesDN);
    }

    private static String dnOf(TransferCapability tc, String aeDN) {
        String cn = tc.getCommonName();
        return (cn != null)
            ? LdapUtils.dnOf("cn", cn , aeDN)
            : LdapUtils.dnOf("dicomSOPClass", tc.getSopClass(),
                   "dicomTransferRole", tc.getRole().toString(), aeDN);
    }

    private static void storeNotEmpty(Attributes attrs, String attrID, byte[]... vals) {
        if (vals != null && vals.length > 0)
            attrs.put(attr(attrID, vals));
    }

    private static <T> Attribute attr(String attrID, byte[]... vals) {
        Attribute attr = new BasicAttribute(attrID);
        for (byte[] val : vals)
            attr.add(val);
        return attr;
    }

    private static void safeClose(Context ctx) {
        if (ctx != null)
            try {
                ctx.close();
            } catch (NamingException e) {
            }
    }

    public void store(AttributeCoercions coercions, String parentDN)
            throws NamingException {
            for (AttributeCoercion ac : coercions)
                createSubcontext(
                        LdapUtils.dnOf("cn", ac.getCommonName(), parentDN),
                        storeTo(ac, new BasicAttributes(true)));
        }

    private static Attributes storeTo(AttributeCoercion ac, BasicAttributes attrs) {
        attrs.put("objectclass", "dcmAttributeCoercion");
        attrs.put("cn", ac.getCommonName());
        LdapUtils.storeNotNull(attrs, "dcmDIMSE", ac.getDIMSE());
        LdapUtils.storeNotNull(attrs, "dicomTransferRole", ac.getRole());
        LdapUtils.storeNotEmpty(attrs, "dcmAETitle", ac.getAETitles());
        LdapUtils.storeNotEmpty(attrs, "dcmSOPClass", ac.getSOPClasses());
        LdapUtils.storeNotNull(attrs, "labeledURI", ac.getURI());
        return attrs;
    }

    public void load(AttributeCoercions acs, String dn) throws NamingException {
        NamingEnumeration<SearchResult> ne =
                search(dn, "(objectclass=dcmAttributeCoercion)");
        try {
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attributes attrs = sr.getAttributes();
                acs.add(new AttributeCoercion(
                        LdapUtils.stringValue(attrs.get("cn"), null),
                        LdapUtils.stringArray(attrs.get("dcmSOPClass")),
                        Dimse.valueOf(LdapUtils.stringValue(attrs.get("dcmDIMSE"), null)),
                        TransferCapability.Role.valueOf(
                                LdapUtils.stringValue(attrs.get("dicomTransferRole"), null)),
                        LdapUtils.stringArray(attrs.get("dcmAETitle")),
                        LdapUtils.stringValue(attrs.get("labeledURI"), null)));
            }
        } finally {
           LdapUtils.safeClose(ne);
        }
    }

    public void merge(AttributeCoercions prevs, AttributeCoercions acs, 
            String parentDN) throws NamingException {
        for (AttributeCoercion prev : prevs) {
            String cn = prev.getCommonName();
            if (acs.findByCommonName(cn) == null)
                destroySubcontext(LdapUtils.dnOf("cn", cn, parentDN));
        }
        for (AttributeCoercion ac : acs) {
            String cn = ac.getCommonName();
            String dn = LdapUtils.dnOf("cn", cn, parentDN);
            AttributeCoercion prev = prevs.findByCommonName(cn);
            if (prev == null)
                createSubcontext(dn, storeTo(ac, new BasicAttributes(true)));
            else
                modifyAttributes(dn, storeDiffs(prev, ac, 
                        new ArrayList<ModificationItem>()));
        }
    }

    private List<ModificationItem> storeDiffs(AttributeCoercion prev,
            AttributeCoercion ac, ArrayList<ModificationItem> mods) {
        LdapUtils.storeDiff(mods, "dcmDIMSE", prev.getDIMSE(), ac.getDIMSE());
        LdapUtils.storeDiff(mods, "dicomTransferRole", 
                prev.getRole(),
                ac.getRole());
        LdapUtils.storeDiff(mods, "dcmAETitle", 
                prev.getAETitles(),
                ac.getAETitles());
        LdapUtils.storeDiff(mods, "dcmSOPClass",
                prev.getSOPClasses(),
                ac.getSOPClasses());
        LdapUtils.storeDiff(mods, "labeledURI", prev.getURI(), ac.getURI());
        return mods;
    }

    @Override
    public void sync() throws ConfigurationException {
        // NOOP
    }
}
