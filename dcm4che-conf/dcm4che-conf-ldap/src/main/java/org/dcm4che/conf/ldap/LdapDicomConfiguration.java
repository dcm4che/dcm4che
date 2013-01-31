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

import org.dcm4che.conf.api.AttributeCoercion;
import org.dcm4che.conf.api.AttributeCoercions;
import org.dcm4che.conf.api.ConfigurationAlreadyExistsException;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.conf.api.ConfigurationNotFoundException;
import org.dcm4che.conf.api.DicomConfiguration;
import org.dcm4che.data.Code;
import org.dcm4che.data.Issuer;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.QueryOption;
import org.dcm4che.net.StorageOptions;
import org.dcm4che.net.TransferCapability;
import org.dcm4che.net.Connection.Protocol;
import org.dcm4che.util.StringUtils;
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

    @Override
    public synchronized void close() {
        safeClose(ctx);
    }

    @Override
    public synchronized boolean configurationExists() throws ConfigurationException {
        return configurationDN != null || findConfiguration();
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
                    attrs("dicomUniqueAETitle", "dicomAETitle", aet));
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

    public Device findDevice(String filter, String childName)
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
           safeClose(ne);
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

    private String[] list(String dn, String filter, String attrID)
            throws ConfigurationException {
        ArrayList<String> values = new ArrayList<String>();
        NamingEnumeration<SearchResult> ne = null;
        try {
            ne = search(dn, filter,
                    new String[]{ attrID });
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attributes attrs = sr.getAttributes();
                values.add(stringValue(attrs.get(attrID), null));
            }
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } finally {
           safeClose(ne);
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
        if (!equals(prev, certs))
            storeCertificates(dn, certs);
    }

    private void storeChilds(String deviceDN, Device device) throws NamingException {
        for (Connection conn : device.listConnections())
            createSubcontext(dnOf(conn, deviceDN), storeTo(conn, new BasicAttributes(true)));
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

    public void createSubcontext(String name, Attributes attrs) throws NamingException {
        safeClose(ctx.createSubcontext(name, attrs));
    }

    public void destroySubcontext(String dn) throws NamingException {
        ctx.destroySubcontext(dn);
    }

    public void destroySubcontextWithChilds(String name) throws NamingException {
        NamingEnumeration<NameClassPair> list = ctx.list(name);
        try {
            while (list.hasMore())
                destroySubcontextWithChilds(list.next().getNameInNamespace());
        } finally {
            safeClose(list);
        }
        ctx.destroySubcontext(name);
    }

    private void setConfigurationDN(String configurationDN) {
        this.configurationDN = configurationDN;
        this.devicesDN = CN_DEVICES + configurationDN;
        this.aetsRegistryDN = CN_UNIQUE_AE_TITLES_REGISTRY + configurationDN;
    }

    private void clearConfigurationDN() {
        this.configurationDN = null;
        this.devicesDN = null;
        this.aetsRegistryDN = null;
    }

    private void ensureConfigurationExists() throws ConfigurationException {
        if (!configurationExists())
            initConfiguration();
    }

    private void initConfiguration() throws ConfigurationException {
        setConfigurationDN("cn=" + configurationCN + ',' + baseDN);
        try {
            createSubcontext(configurationDN,
                    attrs(configurationRoot, "cn", configurationCN));
            createSubcontext(devicesDN,
                    attrs("dicomDevicesRoot", "cn", "Devices"));
            createSubcontext(aetsRegistryDN,
                    attrs("dicomUniqueAETitlesRegistryRoot", 
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
            safeClose(ne);
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean hasObjectClass(Attributes attrs, String objectClass)
           throws NamingException {
       NamingEnumeration<String> ne =
           (NamingEnumeration<String>) attrs.get("objectclass").getAll();
       try {
           while (ne.hasMore())
               if (objectClass.equals(ne.next()))
                   return true;
       } finally {
           safeClose(ne);
       }
       return false;
   }

   private Attributes storeTo(Device device, Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dicomDevice");
        attrs.put(objectclass);
        storeNotNull(attrs, "dicomDeviceName", device.getDeviceName());
        storeNotNull(attrs, "dicomDescription", device.getDescription());
        storeNotNull(attrs, "dicomManufacturer", device.getManufacturer());
        storeNotNull(attrs, "dicomManufacturerModelName",
                device.getManufacturerModelName());
        storeNotEmpty(attrs, "dicomSoftwareVersion",
                device.getSoftwareVersions());
        storeNotNull(attrs, "dicomStationName", device.getStationName());
        storeNotNull(attrs, "dicomDeviceSerialNumber",
                device.getDeviceSerialNumber());
        storeNotNull(attrs, "dicomIssuerOfPatientID",
                device.getIssuerOfPatientID());
        storeNotNull(attrs, "dicomIssuerOfAccessionNumber",
                device.getIssuerOfAccessionNumber());
        storeNotNull(attrs, "dicomOrderPlacerIdentifier",
                device.getOrderPlacerIdentifier());
        storeNotNull(attrs, "dicomOrderFillerIdentifier",
                device.getOrderFillerIdentifier());
        storeNotNull(attrs, "dicomIssuerOfAdmissionID",
                device.getIssuerOfAdmissionID());
        storeNotNull(attrs, "dicomIssuerOfServiceEpisodeID",
                device.getIssuerOfServiceEpisodeID());
        storeNotNull(attrs, "dicomIssuerOfContainerIdentifier",
                device.getIssuerOfContainerIdentifier());
        storeNotNull(attrs, "dicomIssuerOfSpecimenIdentifier",
                device.getIssuerOfSpecimenIdentifier());
        storeNotEmpty(attrs, "dicomInstitutionName",
                device.getInstitutionNames());
        storeNotEmpty(attrs, "dicomInstitutionCode",
                device.getInstitutionCodes());
        storeNotEmpty(attrs, "dicomInstitutionAddress",
                device.getInstitutionAddresses());
        storeNotEmpty(attrs, "dicomInstitutionDepartmentName",
                device.getInstitutionalDepartmentNames());
        storeNotEmpty(attrs, "dicomPrimaryDeviceType",
                device.getPrimaryDeviceTypes());
        storeNotEmpty(attrs, "dicomRelatedDeviceReference",
                device.getRelatedDeviceRefs());
        storeNotEmpty(attrs, "dicomAuthorizedNodeCertificateReference",
                device.getAuthorizedNodeCertificateRefs());
        storeNotEmpty(attrs, "dicomThisNodeCertificateReference",
                device.getThisNodeCertificateRefs());
        storeNotEmpty(attrs, "dicomVendorData", device.getVendorData());
        storeBoolean(attrs, "dicomInstalled", device.isInstalled());
        if (!extended)
            return attrs;

        objectclass.add("dcmDevice");
        storeNotDef(attrs, "dcmLimitOpenAssociations", device.getLimitOpenAssociations(), 0);
        storeNotNull(attrs, "dcmTrustStoreURL", device.getTrustStoreURL());
        storeNotNull(attrs, "dcmTrustStoreType", device.getTrustStoreType());
        storeNotNull(attrs, "dcmTrustStorePin", device.getTrustStorePin());
        storeNotNull(attrs, "dcmTrustStorePinProperty", device.getTrustStorePinProperty());
        storeNotNull(attrs, "dcmKeyStoreURL", device.getKeyStoreURL());
        storeNotNull(attrs, "dcmKeyStoreType", device.getKeyStoreType());
        storeNotNull(attrs, "dcmKeyStorePin", device.getKeyStorePin());
        storeNotNull(attrs, "dcmKeyStorePinProperty", device.getKeyStorePinProperty());
        storeNotNull(attrs, "dcmKeyStoreKeyPin", device.getKeyStoreKeyPin());
        storeNotNull(attrs, "dcmKeyStoreKeyPinProperty", device.getKeyStoreKeyPinProperty());
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.storeTo(device, attrs);
        return attrs;
    }

    private Attributes storeTo(Connection conn, Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dicomNetworkConnection");
        attrs.put(objectclass);
        storeNotNull(attrs, "cn", conn.getCommonName());
        storeNotNull(attrs, "dicomHostname", conn.getHostname());
        storeNotDef(attrs, "dicomPort", conn.getPort(), Connection.NOT_LISTENING);
        storeNotEmpty(attrs, "dicomTLSCipherSuite", conn.getTlsCipherSuites());
        storeNotNull(attrs, "dicomInstalled", conn.getInstalled());
        if (!extended)
            return attrs;

        objectclass.add("dcmNetworkConnection");
        storeNotNull(attrs, "dcmProtocol", 
                StringUtils.nullify(conn.getProtocol(), Protocol.DICOM));
        storeNotNull(attrs, "dcmHTTPProxy", conn.getHttpProxy());
        storeNotEmpty(attrs, "dcmBlacklistedHostname", conn.getBlacklist());
        storeNotDef(attrs, "dcmTCPBacklog",
                conn.getBacklog(), Connection.DEF_BACKLOG);
        storeNotDef(attrs, "dcmTCPConnectTimeout",
                conn.getConnectTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcmAARQTimeout",
                conn.getRequestTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcmAAACTimeout",
                conn.getAcceptTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcmARRPTimeout",
                conn.getReleaseTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcmResponseTimeout",
                conn.getResponseTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcmRetrieveTimeout",
                conn.getRetrieveTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcmIdleTimeout",
                conn.getIdleTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcmTCPCloseDelay",
                conn.getSocketCloseDelay(), Connection.DEF_SOCKETDELAY);
        storeNotDef(attrs, "dcmTCPSendBufferSize",
                conn.getSendBufferSize(), Connection.DEF_BUFFERSIZE);
        storeNotDef(attrs, "dcmTCPReceiveBufferSize",
                conn.getReceiveBufferSize(), Connection.DEF_BUFFERSIZE);
        storeNotDef(attrs, "dcmTCPNoDelay", conn.isTcpNoDelay(), true);
        storeNotDef(attrs, "dcmSendPDULength",
                conn.getSendPDULength(), Connection.DEF_MAX_PDU_LENGTH);
        storeNotDef(attrs, "dcmReceivePDULength",
                conn.getReceivePDULength(), Connection.DEF_MAX_PDU_LENGTH);
        storeNotDef(attrs, "dcmMaxOpsPerformed",
                conn.getMaxOpsPerformed(), Connection.SYNCHRONOUS_MODE);
        storeNotDef(attrs, "dcmMaxOpsInvoked",
                conn.getMaxOpsInvoked(), Connection.SYNCHRONOUS_MODE);
        storeNotDef(attrs, "dcmPackPDV", conn.isPackPDV(), true);
        if (conn.isTls()) {
            storeNotEmpty(attrs, "dcmTLSProtocol", conn.getTlsProtocols());
            storeNotDef(attrs, "dcmTLSNeedClientAuth", conn.isTlsNeedClientAuth(), true);
        }
        return attrs;
    }

    private Attributes storeTo(ApplicationEntity ae, String deviceDN, Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dicomNetworkAE");
        attrs.put(objectclass);
        storeNotNull(attrs, "dicomAETitle", ae.getAETitle());
        storeNotNull(attrs, "dicomDescription", ae.getDescription());
        storeNotEmpty(attrs, "dicomVendorData", ae.getVendorData());
        storeNotEmpty(attrs, "dicomApplicationCluster", ae.getApplicationClusters());
        storeNotEmpty(attrs, "dicomPreferredCallingAETitle", ae.getPreferredCallingAETitles());
        storeNotEmpty(attrs, "dicomPreferredCalledAETitle", ae.getPreferredCalledAETitles());
        storeBoolean(attrs, "dicomAssociationInitiator", ae.isAssociationInitiator());
        storeBoolean(attrs, "dicomAssociationAcceptor",  ae.isAssociationAcceptor());
        storeConnRefs(attrs, ae.getConnections(), deviceDN);
        storeNotEmpty(attrs, "dicomSupportedCharacterSet", ae.getSupportedCharacterSets());
        storeNotNull(attrs, "dicomInstalled", ae.getInstalled());
        if (!extended)
            return attrs;

        objectclass.add("dcmNetworkAE");
        storeNotEmpty(attrs, "dcmAcceptedCallingAETitle", ae.getAcceptedCallingAETitles());
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.storeTo(ae, attrs);
        return attrs;
    }

    private Attributes storeTo(TransferCapability tc, Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dicomTransferCapability");
        attrs.put(objectclass);
        storeNotNull(attrs, "cn", tc.getCommonName());
        storeNotNull(attrs, "dicomSOPClass", tc.getSopClass());
        storeNotNull(attrs, "dicomTransferRole", tc.getRole());
        storeNotEmpty(attrs, "dicomTransferSyntax", tc.getTransferSyntaxes());
        if (!extended)
            return attrs;

        objectclass.add("dcmTransferCapability");
        EnumSet<QueryOption> queryOpts = tc.getQueryOptions();
        if (queryOpts != null) {
            storeNotDef(attrs, "dcmRelationalQueries",
                    queryOpts.contains(QueryOption.RELATIONAL), false);
            storeNotDef(attrs, "dcmCombinedDateTimeMatching",
                    queryOpts.contains(QueryOption.DATETIME), false);
            storeNotDef(attrs, "dcmFuzzySemanticMatching",
                    queryOpts.contains(QueryOption.FUZZY), false);
            storeNotDef(attrs, "dcmTimezoneQueryAdjustment",
                    queryOpts.contains(QueryOption.TIMEZONE), false);
        }
        StorageOptions storageOpts = tc.getStorageOptions();
        if (storageOpts != null) {
            storeInt(attrs, "dcmStorageConformance",
                    storageOpts.getLevelOfSupport().ordinal());
            storeInt(attrs, "dcmDigitalSignatureSupport",
                    storageOpts.getDigitalSignatureSupport().ordinal());
            storeInt(attrs, "dcmDataElementCoercion",
                    storageOpts.getElementCoercion().ordinal());
        }
        return attrs;
    }

    private static String toString(Object o) {
        return (o instanceof Boolean)
                ? toString(((Boolean) o).booleanValue())
                : o != null ? o.toString() : null;
    }

    private static String toString(boolean val) {
        return val ? "TRUE" : "FALSE";
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
                hasObjectClass(attrs, pkiUser)
                     ? new ModificationItem[] { replaceCert }
                     : new ModificationItem[] {
                             new ModificationItem(
                                 DirContext.ADD_ATTRIBUTE,
                                 attr("objectClass", pkiUser )),
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
            Device device = new Device(stringValue(attrs.get("dicomDeviceName"), null));
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
        tc.setCommonName(stringValue(attrs.get("cn"), null));
        tc.setSopClass(stringValue(attrs.get("dicomSOPClass"), null));
        tc.setRole(TransferCapability.Role.valueOf(
                stringValue(attrs.get("dicomTransferRole"), null)));
        tc.setTransferSyntaxes(stringArray(attrs.get("dicomTransferSyntax")));
        if (!hasObjectClass(attrs, "dcmTransferCapability"))
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
        if (booleanValue(relational, false))
            opts.add(QueryOption.RELATIONAL);
        if (booleanValue(datetime, false))
            opts.add(QueryOption.DATETIME);
        if (booleanValue(fuzzy, false))
            opts.add(QueryOption.FUZZY);
        if (booleanValue(timezone, false))
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
                StorageOptions.LevelOfSupport.valueOf(intValue(levelOfSupport, 3)));
        opts.setDigitalSignatureSupport(
                StorageOptions.DigitalSignatureSupport.valueOf(intValue(signatureSupport, 0)));
        opts.setElementCoercion(
                StorageOptions.ElementCoercion.valueOf(intValue(coercion, 2)));
        return opts;
    }

    private void loadFrom(Device device, Attributes attrs)
            throws NamingException, CertificateException {
        device.setDescription(stringValue(attrs.get("dicomDescription"), null));
        device.setManufacturer(stringValue(attrs.get("dicomManufacturer"), null));
        device.setManufacturerModelName(stringValue(attrs.get("dicomManufacturerModelName"), null));
        device.setSoftwareVersions(stringArray(attrs.get("dicomSoftwareVersion")));
        device.setStationName(stringValue(attrs.get("dicomStationName"), null));
        device.setDeviceSerialNumber(stringValue(attrs.get("dicomDeviceSerialNumber"), null));
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
        device.setInstitutionNames(stringArray(attrs.get("dicomInstitutionName")));
        device.setInstitutionCodes(codeArray(attrs.get("dicomInstitutionCode")));
        device.setInstitutionAddresses(stringArray(attrs.get("dicomInstitutionAddress")));
        device.setInstitutionalDepartmentNames(
                stringArray(attrs.get("dicomInstitutionDepartmentName")));
        device.setPrimaryDeviceTypes(stringArray(attrs.get("dicomPrimaryDeviceType")));
        device.setRelatedDeviceRefs(stringArray(attrs.get("dicomRelatedDeviceReference")));
        for (String dn : stringArray(attrs.get("dicomAuthorizedNodeCertificateReference")))
            device.setAuthorizedNodeCertificates(dn, loadCertificates(dn));
        for (String dn : stringArray(attrs.get("dicomThisNodeCertificateReference")))
            device.setThisNodeCertificates(dn, loadCertificates(dn));
        device.setVendorData(byteArrays(attrs.get("dicomVendorData")));
        device.setInstalled(booleanValue(attrs.get("dicomInstalled"), true));
        if (!hasObjectClass(attrs, "dcmDevice"))
            return;
        
        device.setLimitOpenAssociations(
                intValue(attrs.get("dcmLimitOpenAssociations"), 0));
        device.setTrustStoreURL(stringValue(attrs.get("dcmTrustStoreURL"), null));
        device.setTrustStoreType(stringValue(attrs.get("dcmTrustStoreType"), null));
        device.setTrustStorePin(stringValue(attrs.get("dcmTrustStorePin"), null));
        device.setTrustStorePinProperty(
                stringValue(attrs.get("dcmTrustStorePinProperty"), null));
        device.setKeyStoreURL(stringValue(attrs.get("dcmKeyStoreURL"), null));
        device.setKeyStoreType(stringValue(attrs.get("dcmKeyStoreType"), null));
        device.setKeyStorePin(stringValue(attrs.get("dcmKeyStorePin"), null));
        device.setKeyStorePinProperty(
                stringValue(attrs.get("dcmKeyStorePinProperty"), null));
        device.setKeyStoreKeyPin(stringValue(attrs.get("dcmKeyStoreKeyPin"), null));
        device.setKeyStoreKeyPinProperty(
                stringValue(attrs.get("dcmKeyStoreKeyPinProperty"), null));
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
           safeClose(ne);
        }
    }

    public NamingEnumeration<SearchResult> search(String dn, String filter)
            throws NamingException {
        return search(dn, filter, null);
    }

    private NamingEnumeration<SearchResult> search(String dn, String filter, String[] attrs)
            throws NamingException {
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        ctls.setReturningObjFlag(false);
        ctls.setReturningAttributes(attrs);
        return ctx.search(dn, filter, ctls);
    }

    private void loadFrom(Connection conn, Attributes attrs) throws NamingException {
        conn.setCommonName(stringValue(attrs.get("cn"), null));
        conn.setHostname(stringValue(attrs.get("dicomHostname"), null));
        conn.setPort(intValue(attrs.get("dicomPort"), Connection.NOT_LISTENING));
        conn.setTlsCipherSuites(stringArray(attrs.get("dicomTLSCipherSuite")));
        conn.setInstalled(booleanValue(attrs.get("dicomInstalled"), null));
        if (!hasObjectClass(attrs, "dcmNetworkConnection"))
            return;

        conn.setProtocol(Protocol.valueOf(stringValue(attrs.get("dcmProtocol"), "DICOM")));
        conn.setHttpProxy(stringValue(attrs.get("dcmHTTPProxy"), null));
        conn.setBlacklist(stringArray(attrs.get("dcmBlacklistedHostname")));
        conn.setBacklog(intValue(attrs.get("dcmTCPBacklog"), Connection.DEF_BACKLOG));
        conn.setConnectTimeout(intValue(attrs.get("dcmTCPConnectTimeout"),
                Connection.NO_TIMEOUT));
        conn.setRequestTimeout(intValue(attrs.get("dcmAARQTimeout"),
                Connection.NO_TIMEOUT));
        conn.setAcceptTimeout(intValue(attrs.get("dcmAAACTimeout"),
                Connection.NO_TIMEOUT));
        conn.setReleaseTimeout(intValue(attrs.get("dcmARRPTimeout"),
                Connection.NO_TIMEOUT));
        conn.setResponseTimeout(intValue(attrs.get("dcmResponseTimeout"),
                Connection.NO_TIMEOUT));
        conn.setRetrieveTimeout(intValue(attrs.get("dcmRetrieveTimeout"),
                Connection.NO_TIMEOUT));
        conn.setIdleTimeout(intValue(attrs.get("dcmIdleTimeout"),
                Connection.NO_TIMEOUT));
        conn.setSocketCloseDelay(intValue(attrs.get("dcmTCPCloseDelay"),
                Connection.DEF_SOCKETDELAY));
        conn.setSendBufferSize(intValue(attrs.get("dcmTCPSendBufferSize"),
                Connection.DEF_BUFFERSIZE));
        conn.setReceiveBufferSize(intValue(attrs.get("dcmTCPReceiveBufferSize"),
                Connection.DEF_BUFFERSIZE));
        conn.setTcpNoDelay(booleanValue(attrs.get("dcmTCPNoDelay"), true));
        conn.setTlsNeedClientAuth(booleanValue(attrs.get("dcmTLSNeedClientAuth"), true));
        String[] tlsProtocols = stringArray(attrs.get("dcmTLSProtocol"));
        if (tlsProtocols.length > 0)
            conn.setTlsProtocols(tlsProtocols);
        conn.setSendPDULength(intValue(attrs.get("dcmSendPDULength"),
                Connection.DEF_MAX_PDU_LENGTH));
        conn.setReceivePDULength(intValue(attrs.get("dcmReceivePDULength"),
                Connection.DEF_MAX_PDU_LENGTH));
        conn.setMaxOpsPerformed(intValue(attrs.get("dcmMaxOpsPerformed"),
                Connection.SYNCHRONOUS_MODE));
        conn.setMaxOpsInvoked(intValue(attrs.get("dcmMaxOpsInvoked"),
                Connection.SYNCHRONOUS_MODE));
        conn.setPackPDV(booleanValue(attrs.get("dcmPackPDV"), true));
    }

    private void loadApplicationEntities(Device device, String deviceDN)
            throws NamingException {
        NamingEnumeration<SearchResult> ne = search(deviceDN, "(objectclass=dicomNetworkAE)");
        try {
            while (ne.hasMore()) {
                device.addApplicationEntity(
                        loadApplicationEntity(ne.next(), deviceDN, device));
            }
        } finally {
           safeClose(ne);
        }
    }

    private ApplicationEntity loadApplicationEntity(SearchResult sr,
            String deviceDN, Device device) throws NamingException {
        Attributes attrs = sr.getAttributes();
        ApplicationEntity ae = new ApplicationEntity(stringValue(attrs.get("dicomAETitle"), null));
        loadFrom(ae, attrs);
        for (String connDN : stringArray(attrs.get("dicomNetworkConnectionReference")))
            ae.addConnection(findConnection(connDN, deviceDN, device));
        loadChilds(ae, sr.getNameInNamespace());
        return ae ;
    }

    public static Connection findConnection(String connDN, String deviceDN, Device device)
            throws NameNotFoundException {
        for (Connection conn : device.listConnections())
            if (dnOf(conn, deviceDN).equalsIgnoreCase(connDN))
                return conn;

        throw new NameNotFoundException(connDN);
    }

    private void loadFrom(ApplicationEntity ae, Attributes attrs) throws NamingException {
        ae.setDescription(stringValue(attrs.get("dicomDescription"), null));
        ae.setVendorData(byteArrays(attrs.get("dicomVendorData")));
        ae.setApplicationClusters(stringArray(attrs.get("dicomApplicationCluster")));
        ae.setPreferredCallingAETitles(stringArray(attrs.get("dicomPreferredCallingAETitle")));
        ae.setPreferredCalledAETitles(stringArray(attrs.get("dicomPreferredCalledAETitle")));
        ae.setAssociationInitiator(booleanValue(attrs.get("dicomAssociationInitiator"), false));
        ae.setAssociationAcceptor(booleanValue(attrs.get("dicomAssociationAcceptor"), false));
        ae.setSupportedCharacterSets(stringArray(attrs.get("dicomSupportedCharacterSet")));
        ae.setInstalled(booleanValue(attrs.get("dicomInstalled"), null));
        if (!hasObjectClass(attrs, "dcmNetworkAE"))
            return;

        ae.setAcceptedCallingAETitles(stringArray(attrs.get("dcmAcceptedCallingAETitle")));
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
        NamingEnumeration<SearchResult> ne = search(aeDN, "(objectclass=dicomTransferCapability)");
        try {
            while (ne.hasMore())
                ae.addTransferCapability(loadTransferCapability(ne.next()));
        } finally {
           safeClose(ne);
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
        storeDiff(mods, "dicomDescription",
                a.getDescription(),
                b.getDescription());
        storeDiff(mods, "dicomManufacturer",
                a.getManufacturer(),
                b.getManufacturer());
        storeDiff(mods, "dicomManufacturerModelName",
                a.getManufacturerModelName(),
                b.getManufacturerModelName());
        storeDiff(mods, "dicomSoftwareVersion",
                a.getSoftwareVersions(),
                b.getSoftwareVersions());
        storeDiff(mods, "dicomStationName",
                a.getStationName(),
                b.getStationName());
        storeDiff(mods, "dicomDeviceSerialNumber",
                a.getDeviceSerialNumber(),
                b.getDeviceSerialNumber());
        storeDiff(mods, "dicomIssuerOfPatientID",
                a.getIssuerOfPatientID(),
                b.getIssuerOfPatientID());
        storeDiff(mods, "dicomIssuerOfAccessionNumber",
                a.getIssuerOfAccessionNumber(),
                b.getIssuerOfAccessionNumber());
        storeDiff(mods, "dicomOrderPlacerIdentifier",
                a.getOrderPlacerIdentifier(),
                b.getOrderPlacerIdentifier());
        storeDiff(mods, "dicomOrderFillerIdentifier",
                a.getOrderFillerIdentifier(),
                b.getOrderFillerIdentifier());
        storeDiff(mods, "dicomIssuerOfAdmissionID",
                a.getIssuerOfAdmissionID(),
                b.getIssuerOfAdmissionID());
        storeDiff(mods, "dicomIssuerOfServiceEpisodeID",
                a.getIssuerOfServiceEpisodeID(),
                b.getIssuerOfServiceEpisodeID());
        storeDiff(mods, "dicomIssuerOfContainerIdentifier",
                a.getIssuerOfContainerIdentifier(),
                b.getIssuerOfContainerIdentifier());
        storeDiff(mods, "dicomIssuerOfSpecimenIdentifier",
                a.getIssuerOfSpecimenIdentifier(),
                b.getIssuerOfSpecimenIdentifier());
        storeDiff(mods, "dicomInstitutionName",
                a.getInstitutionNames(),
                b.getInstitutionNames());
        storeDiff(mods, "dicomInstitutionCode",
                a.getInstitutionCodes(),
                b.getInstitutionCodes());
        storeDiff(mods, "dicomInstitutionAddress",
                a.getInstitutionAddresses(),
                b.getInstitutionAddresses());
        storeDiff(mods, "dicomInstitutionDepartmentName",
                a.getInstitutionalDepartmentNames(),
                b.getInstitutionalDepartmentNames());
        storeDiff(mods, "dicomPrimaryDeviceType",
                a.getPrimaryDeviceTypes(),
                b.getPrimaryDeviceTypes());
        storeDiff(mods, "dicomRelatedDeviceReference",
                a.getRelatedDeviceRefs(),
                b.getRelatedDeviceRefs());
        storeDiff(mods, "dicomAuthorizedNodeCertificateReference",
                a.getAuthorizedNodeCertificateRefs(),
                b.getAuthorizedNodeCertificateRefs());
        storeDiff(mods, "dicomThisNodeCertificateReference",
                a.getThisNodeCertificateRefs(),
                b.getThisNodeCertificateRefs());
        storeDiff(mods, "dicomVendorData",
                a.getVendorData(),
                b.getVendorData());
        storeDiff(mods, "dicomInstalled",
                a.isInstalled(),
                b.isInstalled());
        if (!extended)
            return mods;

        storeDiff(mods, "dcmLimitOpenAssociations",
                a.getLimitOpenAssociations(),
                b.getLimitOpenAssociations());
        storeDiff(mods, "dcmTrustStoreURL",
                a.getTrustStoreURL(),
                b.getTrustStoreURL());
        storeDiff(mods, "dcmTrustStoreType",
                a.getTrustStoreType(),
                b.getTrustStoreType());
        storeDiff(mods, "dcmTrustStorePin",
                a.getTrustStorePin(),
                b.getTrustStorePin());
        storeDiff(mods, "dcmTrustStorePinProperty",
                a.getTrustStorePinProperty(),
                b.getTrustStorePinProperty());
        storeDiff(mods, "dcmKeyStoreURL",
                a.getKeyStoreURL(),
                b.getKeyStoreURL());
        storeDiff(mods, "dcmKeyStoreType",
                a.getKeyStoreType(),
                b.getKeyStoreType());
        storeDiff(mods, "dcmKeyStorePin",
                a.getKeyStorePin(),
                b.getKeyStorePin());
        storeDiff(mods, "dcmKeyStorePinProperty",
                a.getKeyStorePinProperty(),
                b.getKeyStorePinProperty());
        storeDiff(mods, "dcmKeyStoreKeyPin",
                a.getKeyStoreKeyPin(),
                b.getKeyStoreKeyPin());
        storeDiff(mods, "dcmKeyStoreKeyPinProperty",
                a.getKeyStoreKeyPinProperty(),
                b.getKeyStoreKeyPinProperty());
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.storeDiffs(a, b, mods);
        return mods;
    }

    private List<ModificationItem> storeDiffs(Connection a, Connection b,
            List<ModificationItem> mods) {
        storeDiff(mods, "dicomHostname",
                a.getHostname(),
                b.getHostname());
        storeDiff(mods, "dicomPort",
                a.getPort(),
                b.getPort(),
                Connection.NOT_LISTENING);
        storeDiff(mods, "dicomTLSCipherSuite",
                a.getTlsCipherSuites(),
                b.getTlsCipherSuites());
        storeDiff(mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled());
        if (!extended)
            return mods;

        storeDiff(mods, "dcmProtocol",
                StringUtils.nullify(a.getProtocol(), Protocol.DICOM),
                StringUtils.nullify(b.getProtocol(), Protocol.DICOM));
        storeDiff(mods, "dcmHTTPProxy",
                a.getHttpProxy(),
                b.getHttpProxy());
        storeDiff(mods, "dcmBlacklistedHostname",
                a.getBlacklist(),
                b.getBlacklist());
        storeDiff(mods, "dcmTCPBacklog",
                a.getBacklog(),
                b.getBacklog(),
                Connection.DEF_BACKLOG);
        storeDiff(mods, "dcmTCPConnectTimeout",
                a.getConnectTimeout(),
                b.getConnectTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcmAARQTimeout",
                a.getRequestTimeout(),
                b.getRequestTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcmAAACTimeout",
                a.getAcceptTimeout(),
                b.getAcceptTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcmARRPTimeout",
                a.getReleaseTimeout(),
                b.getReleaseTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcmResponseTimeout",
                a.getResponseTimeout(),
                b.getResponseTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcmRetrieveTimeout",
                a.getRetrieveTimeout(),
                b.getRetrieveTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcmIdleTimeout",
                a.getIdleTimeout(),
                b.getIdleTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcmTCPCloseDelay",
                a.getSocketCloseDelay(),
                b.getSocketCloseDelay(),
                Connection.DEF_SOCKETDELAY);
        storeDiff(mods, "dcmTCPSendBufferSize",
                a.getSendBufferSize(),
                b.getSendBufferSize(),
                Connection.DEF_BUFFERSIZE);
        storeDiff(mods, "dcmTCPReceiveBufferSize",
                a.getReceiveBufferSize(),
                b.getReceiveBufferSize(),
                Connection.DEF_BUFFERSIZE);
        storeDiff(mods, "dcmTCPNoDelay",
                a.isTcpNoDelay(),
                b.isTcpNoDelay(),
                true);
        storeDiff(mods, "dcmTLSProtocol",
                a.isTls() ? a.getTlsProtocols() : StringUtils.EMPTY_STRING,
                b.isTls() ? b.getTlsProtocols() : StringUtils.EMPTY_STRING);
        storeDiff(mods, "dcmTLSNeedClientAuth",
                !a.isTls() || a.isTlsNeedClientAuth(),
                !a.isTls() || a.isTlsNeedClientAuth(),
                true);
        storeDiff(mods, "dcmSendPDULength",
                a.getSendPDULength(),
                b.getSendPDULength(),
                Connection.DEF_MAX_PDU_LENGTH);
        storeDiff(mods, "dcmReceivePDULength",
                a.getReceivePDULength(),
                b.getReceivePDULength(),
                Connection.DEF_MAX_PDU_LENGTH);
        storeDiff(mods, "dcmMaxOpsPerformed",
                a.getMaxOpsPerformed(),
                b.getMaxOpsPerformed(),
                Connection.SYNCHRONOUS_MODE);
        storeDiff(mods, "dcmMaxOpsInvoked",
                a.getMaxOpsInvoked(),
                b.getMaxOpsInvoked(),
                Connection.SYNCHRONOUS_MODE);
        storeDiff(mods, "dcmPackPDV",
                a.isPackPDV(),
                b.isPackPDV(),
                true);
        return mods;
    }

    private List<ModificationItem> storeDiffs(ApplicationEntity a,
            ApplicationEntity b, String deviceDN, List<ModificationItem> mods) {
        storeDiff(mods, "dicomDescription",
                a.getDescription(),
                b.getDescription());
        storeDiff(mods, "dicomVendorData",
                a.getVendorData(),
                b.getVendorData());
        storeDiff(mods, "dicomApplicationCluster",
                a.getApplicationClusters(),
                b.getApplicationClusters());
        storeDiff(mods, "dicomPreferredCallingAETitle",
                a.getPreferredCallingAETitles(),
                b.getPreferredCallingAETitles());
        storeDiff(mods, "dicomPreferredCalledAETitle",
                a.getPreferredCalledAETitles(),
                b.getPreferredCalledAETitles());
        storeDiff(mods, "dicomAssociationInitiator",
                a.isAssociationInitiator(),
                b.isAssociationInitiator());
        storeDiff(mods, "dicomAssociationAcceptor",
                a.isAssociationAcceptor(),
                b.isAssociationAcceptor());
        storeDiff(mods, "dicomNetworkConnectionReference",
                a.getConnections(),
                b.getConnections(),
                deviceDN);
        storeDiff(mods, "dicomSupportedCharacterSet",
                a.getSupportedCharacterSets(),
                b.getSupportedCharacterSets());
        storeDiff(mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled());
        if (!extended)
            return mods;

        storeDiff(mods, "dcmAcceptedCallingAETitle",
                a.getAcceptedCallingAETitles(),
                b.getAcceptedCallingAETitles());
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.storeDiffs(a, b, mods);
        return mods;
    }

    private List<ModificationItem> storeDiffs(TransferCapability a,
            TransferCapability b, List<ModificationItem> mods) {
        storeDiff(mods, "dicomSOPClass",
                a.getSopClass(),
                b.getSopClass());
        storeDiff(mods, "dicomTransferRole",
                a.getRole(),
                b.getRole());
        storeDiff(mods, "dicomTransferSyntax",
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

        storeDiff(mods, "dcmRelationalQueries",
                prev != null && prev.contains(QueryOption.RELATIONAL),
                val != null && val.contains(QueryOption.RELATIONAL),
                false);
        storeDiff(mods, "dcmCombinedDateTimeMatching",
                prev != null && prev.contains(QueryOption.DATETIME),
                val != null && val.contains(QueryOption.DATETIME),
                false);
        storeDiff(mods, "dcmFuzzySemanticMatching",
                prev != null && prev.contains(QueryOption.FUZZY),
                val != null && val.contains(QueryOption.FUZZY),
                false);
        storeDiff(mods, "dcmTimezoneQueryAdjustment",
                prev != null && prev.contains(QueryOption.TIMEZONE),
                val != null && val.contains(QueryOption.TIMEZONE),
                false);
    }

    private void storeDiffs(StorageOptions prev,
            StorageOptions val, List<ModificationItem> mods) {
        if (prev != null ? prev.equals(val) : val == null)
            return;

        storeDiff(mods, "dcmStorageConformance",
                prev != null ? prev.getLevelOfSupport().ordinal() : -1,
                val != null ? val.getLevelOfSupport().ordinal() : -1,
                -1);
        storeDiff(mods, "dcmDigitalSignatureSupport",
                prev != null ? prev.getDigitalSignatureSupport().ordinal() : -1,
                val != null ? val.getDigitalSignatureSupport().ordinal() : -1,
                -1);
        storeDiff(mods, "dcmDataElementCoercion",
                prev != null ? prev.getElementCoercion().ordinal() : -1,
                val != null ? val.getElementCoercion().ordinal() : -1,
                -1);
    }

    public static int intValue(Attribute attr, int defVal) throws NamingException {
        return attr != null ? Integer.parseInt((String) attr.get()) : defVal;
    }

    private static byte[][] byteArrays(Attribute attr) throws NamingException {
        if (attr == null)
            return new byte[0][];

        byte[][] bb = new byte[attr.size()][];
        for (int i = 0; i < bb.length; i++)
            bb[i] = (byte[]) attr.get(i);

        return bb;
    }

    public static String[] stringArray(Attribute attr) throws NamingException {
        if (attr == null)
            return StringUtils.EMPTY_STRING;

        String[] ss = new String[attr.size()];
        for (int i = 0; i < ss.length; i++)
            ss[i] = (String) attr.get(i);

        return ss;
    }

    private Code[] codeArray(Attribute attr) throws NamingException {
        if (attr == null)
            return EMPTY_CODES;

        Code[] codes = new Code[attr.size()];
        for (int i = 0; i < codes.length; i++)
            codes[i] = new Code((String) attr.get(i));

        return codes;
    }

    public static String stringValue(Attribute attr, String defVal) throws NamingException {
        return attr != null ? (String) attr.get() : defVal;
    }

    public static boolean booleanValue(Attribute attr, boolean defVal)
            throws NamingException {
        return attr != null ? Boolean.parseBoolean((String) attr.get()) : defVal;
    }

    public static Boolean booleanValue(Attribute attr, Boolean defVal)
            throws NamingException {
        return attr != null ? Boolean.valueOf((String) attr.get()) : defVal;
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
        for (ApplicationEntity ae : dev.getApplicationEntities()) {
            String aet = ae.getAETitle();
            ApplicationEntity prevAE = prevDev.getApplicationEntity(aet);
            if (prevAE == null) {
                String aeDN = aetDN(ae.getAETitle(), deviceDN);
                createSubcontext(aeDN,
                        storeTo(ae, deviceDN, new BasicAttributes(true)));
                storeChilds(aeDN, ae);
            } else
                merge(prevAE, ae, deviceDN);
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
            String dn = dnOf(prev, deviceDN);
            if (findByDN(deviceDN, conns, dn) == null)
                destroySubcontext(dn);
        }
        for (Connection conn : conns) {
            String dn = dnOf(conn, deviceDN);
            Connection prev = findByDN(deviceDN, prevs, dn);
            if (prev == null)
                createSubcontext(dn, storeTo(conn, new BasicAttributes(true)));
            else
                modifyAttributes(dn, storeDiffs(prev, conn, new ArrayList<ModificationItem>()));
        }
    }

    private static Connection findByDN(String deviceDN, 
            List<Connection> conns, String dn) {
        for (Connection conn : conns)
            if (dn.equals(dnOf(conn, deviceDN)))
                return conn;
        return null;
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

    private static <T> boolean equals(T[] a, T[] a2) {
        int length = a.length;
        if (a2.length != length)
            return false;

        outer:
        for (Object o1 : a) {
            for (Object o2 : a2)
                if (o1.equals(o2))
                    continue outer;
            return false;
        }
        return true;
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

    public static void storeDiff(List<ModificationItem> mods, String attrId,
            List<Connection> prevs, List<Connection> conns, String deviceDN) {
        if (!equalsConnRefs(prevs, conns, deviceDN))
            mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    connRefs(conns, deviceDN)));
    }

    private static boolean equalsConnRefs(List<Connection> conns1,
           List<Connection> conns2, String deviceDN) {
        if (conns1.size() != conns2.size())
            return false;
        for (Connection conn1 : conns1)
            if (findByDN(deviceDN, conns2, dnOf(conn1, deviceDN)) == null)
                return false;
        return true;
    }

    public static <T> void storeDiff(List<ModificationItem> mods, String attrId,
            T[] prevs, T[] vals) {
        if (!equals(prevs, vals))
            mods.add((vals != null && vals.length == 0)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            attr(attrId, vals)));
    }

    public static void storeDiff(List<ModificationItem> mods, String attrId,
            Object prev, Object val) {
        if (val == null) {
            if (prev != null)
                mods.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                        new BasicAttribute(attrId)));
        } else if (!val.equals(prev))
            mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    new BasicAttribute(attrId, toString(val))));
    }

    public static void storeDiff(List<ModificationItem> mods,
            String attrId, int prev, int val, int defVal) {
        if (val != prev)
            mods.add((val == defVal)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            new BasicAttribute(attrId, "" + val)));
    }

    public static void storeDiff(List<ModificationItem> mods,
            String attrId, boolean prev, boolean val, boolean defVal) {
        if (val != prev)
            mods.add((val == defVal)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            new BasicAttribute(attrId, toString(val))));
    }


    public static String dnOf(String attrID, String attrValue, String parentDN) {
        return attrID + '=' + attrValue + ',' + parentDN;
    }

    private static String dnOf(String attrID1, String attrValue1,
            String attrID2, String attrValue2, String baseDN) {
        return attrID1 + '=' + attrValue1
                + '+' + attrID2 + '=' + attrValue2
                + ','  + baseDN;
    }

    private static String aetDN(String aet, String parentDN) {
        return dnOf("dicomAETitle" ,aet, parentDN);
    }

    @Override
    public String deviceRef(String name) {
        return dnOf("dicomDeviceName" ,name, devicesDN);
    }

    private static String dnOf(Connection conn, String deviceDN) {
        String cn = conn.getCommonName();
        return (cn != null)
            ? dnOf("cn", cn , deviceDN)
            : (conn.isServer()
                    ? dnOf("dicomHostname", conn.getHostname(),
                           "dicomPort", Integer.toString(conn.getPort()),
                            deviceDN)
                    : dnOf("dicomHostname", conn.getHostname(), deviceDN));
    }

    private static String dnOf(TransferCapability tc, String aeDN) {
        String cn = tc.getCommonName();
        return (cn != null)
            ? dnOf("cn", cn , aeDN)
            : dnOf("dicomSOPClass", tc.getSopClass(),
                   "dicomTransferRole", tc.getRole().toString(), aeDN);
    }

    private static Attributes attrs(String objectclass, String attrID, String attrVal) {
        Attributes attrs = new BasicAttributes(true); // case-ignore
        attrs.put("objectclass", objectclass);
        storeNotNull(attrs, attrID, attrVal);
        return attrs;
    }

    public static void storeNotNull(Attributes attrs, String attrID, Object val) {
        if (val != null)
            attrs.put(attrID, toString(val));
    }

    public static void storeNotDef(Attributes attrs, String attrID, int val, int defVal) {
        if (val != defVal)
            storeInt(attrs, attrID, val);
    }

    public static void storeNotDef(Attributes attrs, String attrID, boolean val, boolean defVal) {
        if (val != defVal)
            storeBoolean(attrs, attrID, val);
    }

    private static Attribute storeBoolean(Attributes attrs, String attrID, boolean val) {
        return attrs.put(attrID, toString(val));
    }

    private static Attribute storeInt(Attributes attrs, String attrID, int val) {
        return attrs.put(attrID, "" + val);
    }

    private static void storeNotEmpty(Attributes attrs, String attrID, byte[]... vals) {
        if (vals != null && vals.length > 0)
            attrs.put(attr(attrID, vals));
    }

    public static <T> void storeNotEmpty(Attributes attrs, String attrID, T... vals) {
        if (vals != null && vals.length > 0)
            attrs.put(attr(attrID, vals));
    }

    private static <T> Attribute attr(String attrID, byte[]... vals) {
        Attribute attr = new BasicAttribute(attrID);
        for (byte[] val : vals)
            attr.add(val);
        return attr;
    }

    private static <T> Attribute attr(String attrID, T... vals) {
        Attribute attr = new BasicAttribute(attrID);
        for (T val : vals)
            attr.add(val.toString());
        return attr;
    }

    public static void storeConnRefs(Attributes attrs, Collection<Connection> conns,
            String deviceDN) {
        if (!conns.isEmpty())
            attrs.put(connRefs(conns, deviceDN));
    }

    private static Attribute connRefs(Collection<Connection> conns,
            String deviceDN) {
        Attribute attr = new BasicAttribute("dicomNetworkConnectionReference");
        for (Connection conn : conns)
            attr.add(dnOf(conn, deviceDN));
        return attr;
    }

    private static void safeClose(Context ctx) {
        if (ctx != null)
            try {
                ctx.close();
            } catch (NamingException e) {
            }
    }

    public static <T> void safeClose(NamingEnumeration<T> enumeration) {
        if (enumeration != null)
            try {
                enumeration.close();
            } catch (NamingException e) {
            }
    }

    public void store(AttributeCoercions coercions, String parentDN)
            throws NamingException {
            for (AttributeCoercion ac : coercions.getAll())
                createSubcontext(dnOf(ac, parentDN), storeTo(ac, new BasicAttributes(true)));
        }

    private static String dnOf(AttributeCoercion ac, String parentDN) {
        StringBuilder sb = new StringBuilder();
        sb.append("dcmDIMSE=").append(ac.getDimse());
        sb.append("+dicomTransferRole=").append(ac.getRole());
        if (ac.getAETitle() != null)
            sb.append("+dicomAETitle=").append(ac.getAETitle());
        if (ac.getSopClass() != null)
            sb.append("+dicomSOPClass=").append(ac.getSopClass());
        sb.append(',').append(parentDN);
        return sb.toString();
    }

    private static Attributes storeTo(AttributeCoercion ac, BasicAttributes attrs) {
        attrs.put("objectclass", "dcmAttributeCoercion");
        storeNotNull(attrs, "dcmDIMSE", ac.getDimse());
        storeNotNull(attrs, "dicomTransferRole", ac.getRole());
        storeNotNull(attrs, "dicomAETitle", ac.getAETitle());
        storeNotNull(attrs, "dicomSOPClass", ac.getSopClass());
        storeNotNull(attrs, "labeledURI", ac.getURI());
        return attrs;
    }

    public void load(AttributeCoercions acs, String dn) throws NamingException {
        NamingEnumeration<SearchResult> ne = search(dn, "(objectclass=dcmAttributeCoercion)");
        try {
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attributes attrs = sr.getAttributes();
                acs.add(new AttributeCoercion(
                        stringValue(attrs.get("dicomSOPClass"), null),
                        Dimse.valueOf(stringValue(attrs.get("dcmDIMSE"), null)),
                        TransferCapability.Role.valueOf(
                                stringValue(attrs.get("dicomTransferRole"), null)),
                        stringValue(attrs.get("dicomAETitle"), null),
                        stringValue(attrs.get("labeledURI"), null)));
            }
        } finally {
           safeClose(ne);
        }
    }

    public void merge(AttributeCoercions prevs, AttributeCoercions acs, String parentDN)
            throws NamingException {
        for (AttributeCoercion prev : prevs.getAll())
            if (acs.findEquals(prev.getSopClass(), prev.getDimse(),
                    prev.getRole(), prev.getAETitle()) == null)
                destroySubcontext(dnOf(prev, parentDN));
        for (AttributeCoercion ac : acs.getAll()) {
            String dn = dnOf(ac, parentDN);
            AttributeCoercion prev = prevs.findEquals(
                    ac.getSopClass(), ac.getDimse(),
                    ac.getRole(), ac.getAETitle());
            if (prev == null)
                createSubcontext(dn, storeTo(ac, new BasicAttributes(true)));
            else
                modifyAttributes(dn, storeDiffs(prev, ac, new ArrayList<ModificationItem>()));
        }
    }

    private List<ModificationItem> storeDiffs(AttributeCoercion prev,
            AttributeCoercion ac, ArrayList<ModificationItem> mods) {
        storeDiff(mods, "labeledURI", prev.getURI(), ac.getURI());
        return mods;
    }
}
