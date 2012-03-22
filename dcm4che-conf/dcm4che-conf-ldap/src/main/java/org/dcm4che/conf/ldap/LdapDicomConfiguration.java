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
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
public class LdapDicomConfiguration implements DicomConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(LdapDicomConfiguration.class);

    private static final String CN_UNIQUE_AE_TITLES_REGISTRY = "cn=Unique AE Titles Registry,";
    private static final String CN_DEVICES = "cn=Devices,";
    private static final String DICOM_CONFIGURATION = "DICOM Configuration";
    private static final String DICOM_CONFIGURATION_ROOT = "dicomConfigurationRoot";
    private static final String PKI_USER = "pkiUser";
    private static final String USER_CERTIFICATE_BINARY = "userCertificate;binary";
    private static final X509Certificate[] EMPTY_X509_CERTIFICATES = {};

    private final DirContext ctx;
    private final String baseDN;
    private String configurationDN;
    private String devicesDN;
    private String aetsRegistryDN;
    private String configurationCN = DICOM_CONFIGURATION; 
    private String configurationRoot = DICOM_CONFIGURATION_ROOT;
    private String pkiUser = PKI_USER;
    private String userCertificate = USER_CERTIFICATE_BINARY;

    public LdapDicomConfiguration(Hashtable<String, Object> env, String baseDN)
            throws NamingException {
        if (baseDN == null)
            throw new NullPointerException("baseDN");
        this.ctx = new InitialDirContext(env);
        this.baseDN = baseDN;
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

    public void close() {
        safeClose(ctx);
    }

    @Override
    public boolean configurationExists() throws ConfigurationException {
        return configurationDN != null || findConfiguration();
    }

    @Override
    public boolean purgeConfiguration() throws ConfigurationException {
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
    public boolean registerAETitle(String aet) throws ConfigurationException {
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
    public void unregisterAETitle(String aet) throws ConfigurationException {
        if (configurationExists())
            try {
                ctx.destroySubcontext(aetDN(aet, aetsRegistryDN));
            } catch (NameNotFoundException e) {
            } catch (NamingException e) {
                throw new ConfigurationException(e);
            }
    }

    @Override
    public ApplicationEntity findApplicationEntity(String aet)
            throws ConfigurationException {
        return findDevice(
                "(&(objectclass=dicomNetworkAE)(dicomAETitle=" + aet + "))", aet)
            .getApplicationEntity(aet);
    }

    protected Device findDevice(String filter, String childName)
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
    public Device findDevice(String name) throws ConfigurationException {
        if (!configurationExists())
            throw new ConfigurationNotFoundException();

        return loadDevice(deviceRef(name));
    }

    @Override
    public String[] listDeviceNames() throws ConfigurationException {
        if (!configurationExists())
            return StringUtils.EMPTY_STRING;

        NamingEnumeration<SearchResult> ne = null;
        ArrayList<String> deviceNames = new ArrayList<String>();
        try {
            ne = search(devicesDN, "(objectclass=dicomDevice)",
                    new String[]{ "dicomDeviceName" });
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attributes attrs = sr.getAttributes();
                deviceNames.add(stringValue(attrs.get("dicomDeviceName")));
            }
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } finally {
           safeClose(ne);
        }
        return deviceNames.toArray(new String[deviceNames.size()]);
    }

    @Override
    public void persist(Device device) throws ConfigurationException {
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

    protected void storeChilds(String deviceDN, Device device) throws NamingException {
        for (Connection conn : device.listConnections())
            createSubcontext(dnOf(conn, deviceDN), storeTo(conn, new BasicAttributes(true)));
        for (ApplicationEntity ae : device.getApplicationEntities()) {
            String aeDN = aetDN(ae.getAETitle(), deviceDN);
            createSubcontext(aeDN, storeTo(ae, deviceDN, new BasicAttributes(true)));
            storeChilds(aeDN, ae);
        }
    }

    protected void storeChilds(String aeDN, ApplicationEntity ae)
            throws NamingException {
        for (TransferCapability tc : ae.getTransferCapabilities())
            createSubcontext(dnOf(tc, aeDN), storeTo(tc, new BasicAttributes(true)));
    }

    @Override
    public void merge(Device device) throws ConfigurationException {
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

    protected void mergeChilds(Device prev, Device device, String deviceDN)
            throws NamingException {
        mergeConnections(prev, device, deviceDN);
        mergeAEs(prev, device, deviceDN);
    }

    @Override
    public void removeDevice(String name) throws ConfigurationException {
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

    protected void createSubcontext(String name, Attributes attrs) throws NamingException {
        safeClose(ctx.createSubcontext(name, attrs));
    }

    protected void destroySubcontext(String dn) throws NamingException {
        ctx.destroySubcontext(dn);
    }

    protected void destroySubcontextWithChilds(String name) throws NamingException {
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

    protected Attribute objectClassesOf(Device dev, Attribute attr) {
        attr.add("dicomDevice");
        return attr;
    }

    protected Attribute objectClassesOf(Connection ae, Attribute attr) {
        attr.add("dicomNetworkConnection");
        return attr;
    }

    protected Attribute objectClassesOf(ApplicationEntity ae, Attribute attr) {
        attr.add("dicomNetworkAE");
        return attr;
    }

    protected Attribute objectClassesOf(TransferCapability tc, Attribute attr) {
        attr.add("dicomTransferCapability");
        return attr;
    }

    @SuppressWarnings("unchecked")
    protected static boolean hasObjectClass(Attributes attrs, String objectClass)
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

   protected Attributes storeTo(Device device, Attributes attrs) {
        attrs.put(objectClassesOf(device, new BasicAttribute("objectclass")));
        storeNotNull(attrs, "dicomDeviceName", device.getDeviceName());
        storeNotNull(attrs, "dicomDescription", device.getDescription());
        storeNotNull(attrs, "dicomManufacturer", device.getManufacturer());
        storeNotNull(attrs, "dicomManufacturerModelName",
                device.getManufacturerModelName());
        storeNotEmpty(attrs, "dicomSoftwareVersion", device.getSoftwareVersion());
        storeNotNull(attrs, "dicomStationName", device.getStationName());
        storeNotNull(attrs, "dicomDeviceSerialNumber", device.getDeviceSerialNumber());
        storeNotNull(attrs, "dicomIssuerOfPatientID", device.getIssuerOfPatientID());
        storeNotEmpty(attrs, "dicomInstitutionName",device.getInstitutionNames());
        storeNotEmpty(attrs, "dicomInstitutionAddress",device.getInstitutionAddresses());
        storeNotEmpty(attrs, "dicomInstitutionalDepartmentName",
                device.getInstitutionalDepartmentNames());
        storeNotEmpty(attrs, "dicomPrimaryDeviceType", device.getPrimaryDeviceTypes());
        storeNotEmpty(attrs, "dicomRelatedDeviceReference", device.getRelatedDeviceRefs());
        storeNotEmpty(attrs, "dicomAuthorizedNodeCertificateReference",
                device.getAuthorizedNodeCertificateRefs());
        storeNotEmpty(attrs, "dicomThisNodeCertificateReference",
                device.getThisNodeCertificateRefs());
        storeNotEmpty(attrs, "dicomVendorData", device.getVendorData());
        storeBoolean(attrs, "dicomInstalled", device.isInstalled());
        return attrs;
    }

    protected Attributes storeTo(Connection conn, Attributes attrs) {
        attrs.put(objectClassesOf(conn, new BasicAttribute("objectclass")));
        storeNotNull(attrs, "cn", conn.getCommonName());
        storeNotNull(attrs, "dicomHostname", conn.getHostname());
        storeNotDef(attrs, "dicomPort", conn.getPort(), Connection.NOT_LISTENING);
        storeNotEmpty(attrs, "dicomTLSCipherSuite", conn.getTlsCipherSuites());
        storeNotNull(attrs, "dicomInstalled", conn.getInstalled());
        return attrs;
    }

    protected Attributes storeTo(ApplicationEntity ae, String deviceDN, Attributes attrs) {
        attrs.put(objectClassesOf(ae, new BasicAttribute("objectclass")));
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
        return attrs;
    }

    protected Attributes storeTo(TransferCapability tc, Attributes attrs) {
        attrs.put(objectClassesOf(tc, new BasicAttribute("objectclass")));
        storeNotNull(attrs, "cn", tc.getCommonName());
        storeNotNull(attrs, "dicomSOPClass", tc.getSopClass());
        storeNotNull(attrs, "dicomTransferRole", tc.getRole());
        storeNotEmpty(attrs, "dicomTransferSyntax", tc.getTransferSyntaxes());
        return attrs;
    }

    protected static String toString(Object o) {
        return (o instanceof Boolean)
                ? toString(((Boolean) o).booleanValue())
                : o != null ? o.toString() : null;
    }

    private static String toString(boolean val) {
        return val ? "TRUE" : "FALSE";
    }

    @Override
    public void persistCertificates(String dn, X509Certificate... certs)
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
    public void removeCertificates(String dn) throws ConfigurationException {
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
    public X509Certificate[] findCertificates(String dn) throws ConfigurationException {
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

    private Device loadDevice(String deviceDN) throws ConfigurationException {
        try {
            Attributes attrs = ctx.getAttributes(deviceDN);
            Device device = newDevice(attrs);
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

    protected void loadChilds(Device device, String deviceDN)
            throws NamingException {
        loadConnections(device, deviceDN);
        loadApplicationEntities(device, deviceDN);
    }

    protected Device newDevice(Attributes attrs) throws NamingException {
        return new Device(stringValue(attrs.get("dicomDeviceName")));
    }

    protected Connection newConnection(Attributes attrs) throws NamingException {
        return new Connection();
    }

    protected ApplicationEntity newApplicationEntity(Attributes attrs) throws NamingException {
        return new ApplicationEntity(stringValue(attrs.get("dicomAETitle")));
    }

    protected TransferCapability newTransferCapability(Attributes attrs) throws NamingException {
        return new TransferCapability();
    }

    protected void loadFrom(TransferCapability tc, Attributes attrs) throws NamingException {
        tc.setCommonName(stringValue(attrs.get("cn")));
        tc.setSopClass(stringValue(attrs.get("dicomSOPClass")));
        tc.setRole(TransferCapability.Role.valueOf(stringValue(attrs.get("dicomTransferRole"))));
        tc.setTransferSyntaxes(stringArray(attrs.get("dicomTransferSyntax")));
    }

    protected void loadFrom(Device device, Attributes attrs)
            throws NamingException, CertificateException {
        device.setDescription(stringValue(attrs.get("dicomDescription")));
        device.setManufacturer(stringValue(attrs.get("dicomManufacturer")));
        device.setManufacturerModelName(stringValue(attrs.get("dicomManufacturerModelName")));
        device.setSoftwareVersions(stringArray(attrs.get("dicomSoftwareVersion")));
        device.setStationName(stringValue(attrs.get("dicomStationName")));
        device.setDeviceSerialNumber(stringValue(attrs.get("dicomDeviceSerialNumber")));
        device.setIssuerOfPatientID(stringValue(attrs.get("dicomIssuerOfPatientID")));
        device.setInstitutionNames(stringArray(attrs.get("dicomInstitutionName")));
        device.setInstitutionAddresses(stringArray(attrs.get("dicomInstitutionAddress")));
        device.setInstitutionalDepartmentNames(
                stringArray(attrs.get("dicomInstitutionalDepartmentName")));
        device.setPrimaryDeviceTypes(stringArray(attrs.get("dicomPrimaryDeviceType")));
        device.setRelatedDeviceRefs(stringArray(attrs.get("dicomRelatedDeviceReference")));
        for (String dn : stringArray(attrs.get("dicomAuthorizedNodeCertificateReference")))
            device.setAuthorizedNodeCertificates(dn, loadCertificates(dn));
        for (String dn : stringArray(attrs.get("dicomThisNodeCertificateReference")))
            device.setThisNodeCertificates(dn, loadCertificates(dn));
        device.setVendorData(byteArrays(attrs.get("dicomVendorData")));
        try {
            device.setInstalled(booleanValue(attrs.get("dicomInstalled"), true));
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
    }

    private void loadConnections(Device device, String deviceDN) throws NamingException {
        NamingEnumeration<SearchResult> ne = 
                search(deviceDN, "(objectclass=dicomNetworkConnection)");
        try {
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attributes attrs = sr.getAttributes();
                Connection conn = newConnection(attrs);
                loadFrom(conn, attrs);
                try {
                    device.addConnection(conn);
                } catch (IOException e) {
                    throw new AssertionError(e.getMessage());
                }
            }
        } finally {
           safeClose(ne);
        }
    }

    protected NamingEnumeration<SearchResult> search(String dn, String filter)
            throws NamingException {
        return search(dn, filter, null);
    }

    protected NamingEnumeration<SearchResult> search(String dn, String filter, String[] attrs)
            throws NamingException {
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        ctls.setReturningObjFlag(false);
        ctls.setReturningAttributes(attrs);
        return ctx.search(dn, filter, ctls);
    }

    protected void loadFrom(Connection conn, Attributes attrs) throws NamingException {
        conn.setCommonName(stringValue(attrs.get("cn")));
        conn.setHostname(stringValue(attrs.get("dicomHostname")));
        conn.setPort(intValue(attrs.get("dicomPort"), Connection.NOT_LISTENING));
        conn.setTlsCipherSuites(stringArray(attrs.get("dicomTLSCipherSuite")));
        try {
            conn.setInstalled(booleanValue(attrs.get("dicomInstalled"), null));
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
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
        ApplicationEntity ae = newApplicationEntity(attrs);
        loadFrom(ae, attrs);
        for (String connDN : stringArray(attrs.get("dicomNetworkConnectionReference")))
            ae.addConnection(findConnection(connDN, deviceDN, device));
        loadChilds(ae, sr.getNameInNamespace());
        return ae ;
    }

    protected Connection findConnection(String connDN, String deviceDN, Device device)
            throws NameNotFoundException {
        for (Connection conn : device.listConnections())
            if (dnOf(conn, deviceDN).equals(connDN))
                return conn;

        throw new NameNotFoundException(connDN);
    }

    protected void loadFrom(ApplicationEntity ae, Attributes attrs) throws NamingException {
        ae.setDescription(stringValue(attrs.get("dicomDescription")));
        ae.setVendorData(byteArrays(attrs.get("dicomVendorData")));
        ae.setApplicationClusters(stringArray(attrs.get("dicomApplicationCluster")));
        ae.setPreferredCallingAETitles(stringArray(attrs.get("dicomPreferredCallingAETitle")));
        ae.setPreferredCalledAETitles(stringArray(attrs.get("dicomPreferredCalledAETitle")));
        ae.setAssociationInitiator(booleanValue(attrs.get("dicomAssociationInitiator"), false));
        ae.setAssociationAcceptor(booleanValue(attrs.get("dicomAssociationAcceptor"), false));
        ae.setSupportedCharacterSets(stringArray(attrs.get("dicomSupportedCharacterSet")));
        ae.setInstalled(booleanValue(attrs.get("dicomInstalled"), null));
    }

    protected void loadChilds(ApplicationEntity ae, String aeDN) throws NamingException {
        loadTransferCapabilities(ae, aeDN);
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
        TransferCapability tc = newTransferCapability(attrs);
        loadFrom(tc, attrs);
        return tc;
    }

    protected List<ModificationItem> storeDiffs(Device a, Device b,
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
                a.getSoftwareVersion(),
                b.getSoftwareVersion());
        storeDiff(mods, "dicomStationName",
                a.getStationName(),
                b.getStationName());
        storeDiff(mods, "dicomDeviceSerialNumber",
                a.getDeviceSerialNumber(),
                b.getDeviceSerialNumber());
        storeDiff(mods, "dicomIssuerOfPatientID",
                a.getIssuerOfPatientID(),
                b.getIssuerOfPatientID());
        storeDiff(mods, "dicomInstitutionName",
                a.getInstitutionNames(),
                b.getInstitutionNames());
        storeDiff(mods, "dicomInstitutionAddress",
                a.getInstitutionAddresses(),
                b.getInstitutionAddresses());
        storeDiff(mods, "dicomInstitutionalDepartmentName",
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
        return mods;
    }

    protected List<ModificationItem> storeDiffs(Connection a, Connection b,
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
        return mods;
    }

    protected List<ModificationItem> storeDiffs(ApplicationEntity a,
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
        return mods;
    }

    protected List<ModificationItem> storeDiffs(TransferCapability a,
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
        return mods;
    }

    protected static int intValue(Attribute attr, int defVal) throws NamingException {
        return attr != null ? Integer.parseInt((String) attr.get()) : defVal;
    }

    protected static Integer intValue(Attribute attr, Integer defVal) throws NamingException {
        return attr != null ? Integer.valueOf((String) attr.get()) : defVal;
    }

    protected static byte[][] byteArrays(Attribute attr) throws NamingException {
        if (attr == null)
            return new byte[0][];

        byte[][] bb = new byte[attr.size()][];
        for (int i = 0; i < bb.length; i++)
            bb[i] = (byte[]) attr.get(i);

        return bb;
    }

    protected static String[] stringArray(Attribute attr) throws NamingException {
        if (attr == null)
            return StringUtils.EMPTY_STRING;

        String[] ss = new String[attr.size()];
        for (int i = 0; i < ss.length; i++)
            ss[i] = (String) attr.get(i);

        return ss;
    }

    protected static String stringValue(Attribute attr) throws NamingException {
        return attr != null ? (String) attr.get() : null;
    }

    protected static boolean booleanValue(Attribute attr, boolean defVal)
            throws NamingException {
        return attr != null ? Boolean.parseBoolean((String) attr.get()) : defVal;
    }

    protected static Boolean booleanValue(Attribute attr, Boolean defVal)
            throws NamingException {
        return attr != null ? Boolean.valueOf((String) attr.get()) : defVal;
    }

    private void mergeAEs(Device prevDev, Device dev, String deviceDN)
            throws NamingException {
        for (ApplicationEntity ae : prevDev.getApplicationEntities()) {
            String aet = ae.getAETitle();
            if (dev.getApplicationEntity(aet) == null)
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

    protected void mergeChilds(ApplicationEntity prev, ApplicationEntity ae,
            String aeDN) throws NamingException {
        merge(prev.getTransferCapabilities(), ae.getTransferCapabilities(), aeDN);
    }

    protected void modifyAttributes(String dn, List<ModificationItem> mods)
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

    protected static <T> boolean equals(T[] a, T[] a2) {
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

    protected static void storeDiff(List<ModificationItem> mods, String attrId,
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

    protected static <T> void storeDiff(List<ModificationItem> mods, String attrId,
            T[] prevs, T[] vals) {
        if (!equals(prevs, vals))
            mods.add((vals != null && vals.length == 0)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            attr(attrId, vals)));
    }

    protected static void storeDiff(List<ModificationItem> mods, String attrId,
            Object prev, Object val) {
        if (val == null) {
            if (prev != null)
                mods.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                        new BasicAttribute(attrId)));
        } else if (!val.equals(prev))
            mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    new BasicAttribute(attrId, toString(val))));
    }

    protected static void storeDiff(List<ModificationItem> mods,
            String attrId, int prev, int val, int defVal) {
        if (val != prev)
            mods.add((val == defVal)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            new BasicAttribute(attrId, "" + val)));
    }

    protected static void storeDiff(List<ModificationItem> mods,
            String attrId, boolean prev, boolean val, boolean defVal) {
        if (val != prev)
            mods.add((val == defVal)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            new BasicAttribute(attrId, toString(val))));
    }


    protected static String dnOf(String attrID, String attrValue, String parentDN) {
        return attrID + '=' + attrValue + ',' + parentDN;
    }

    protected static String dnOf(String attrID1, String attrValue1,
            String attrID2, String attrValue2, String baseDN) {
        return attrID1 + '=' + attrValue1 + '+' + attrID2 + '=' + attrValue2 + ','  + baseDN;
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
            : conn.isServer()
                 ? dnOf("dicomHostname", conn.getHostname(), "dicomPort",
                         Integer.toString(conn.getPort()) , deviceDN)
                 : dnOf("dicomHostname", conn.getHostname(), deviceDN);
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

    protected static void storeNotNull(Attributes attrs, String attrID, Object val) {
        if (val != null)
            attrs.put(attrID, toString(val));
    }

    protected static void storeNotDef(Attributes attrs, String attrID, int val, int defVal) {
        if (val != defVal)
            storeInt(attrs, attrID, val);
    }

    protected static void storeNotDef(Attributes attrs, String attrID, boolean val, boolean defVal) {
        if (val != defVal)
            storeBoolean(attrs, attrID, val);
    }

    protected static Attribute storeBoolean(Attributes attrs, String attrID, boolean val) {
        return attrs.put(attrID, toString(val));
    }

    protected static Attribute storeInt(Attributes attrs, String attrID, int val) {
        return attrs.put(attrID, "" + val);
    }

    protected static void storeInts(Attributes attrs, String attrID, int... vals) {
        Attribute attr = new BasicAttribute(attrID);
        for (int val : vals)
            attr.add("" + val);
        attrs.put(attr);
    }

    private static void storeNotEmpty(Attributes attrs, String attrID, byte[]... vals) {
        if (vals != null && vals.length > 0)
            attrs.put(attr(attrID, vals));
    }

    protected static <T> void storeNotEmpty(Attributes attrs, String attrID, T... vals) {
        if (vals != null && vals.length > 0)
            attrs.put(attr(attrID, vals));
    }

    private static <T> Attribute attr(String attrID, byte[]... vals) {
        Attribute attr = new BasicAttribute(attrID);
        for (byte[] val : vals)
            attr.add(val);
        return attr;
    }

    protected static <T> Attribute attr(String attrID, T... vals) {
        Attribute attr = new BasicAttribute(attrID);
        for (T val : vals)
            attr.add(val.toString());
        return attr;
    }

    protected static void storeConnRefs(Attributes attrs, Collection<Connection> conns,
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

    protected static <T> void safeClose(NamingEnumeration<T> enumeration) {
        if (enumeration != null)
            try {
                enumeration.close();
            } catch (NamingException e) {
            }
    }

}
