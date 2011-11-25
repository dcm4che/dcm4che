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

import java.io.IOException;
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

    private final DirContext ctx;
    private final String baseDN;
    private String configurationDN;
    private String devicesDN;
    private String aetsRegistryDN;
    private String configurationCN = DICOM_CONFIGURATION; 
    private String configurationRoot = DICOM_CONFIGURATION_ROOT;

    public LdapDicomConfiguration(Hashtable<String, Object> env, String baseDN)
            throws NamingException {
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

    public void close() {
        safeClose(ctx);
    }

    public static Hashtable<String, Object> env(String url) {
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                System.getProperty("org.dcm4che.conf.ldap", "com.sun.jndi.ldap.LdapCtxFactory"));
        env.put("java.naming.ldap.attributes.binary", "dicomVendorData");
        return env;
    }
 
    public static Hashtable<String, Object> authenticate(Hashtable<String, Object> env,
            String userDN, String password) {
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userDN);
        env.put(Context.SECURITY_CREDENTIALS, password);
        return env;
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
            destroySubcontext(ctx, configurationDN);
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
            createSubcontext(ctx, aetDN(aet, aetsRegistryDN),
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
            } catch (NamingException e) {
                throw new ConfigurationException(e);
            }
    }

    @Override
    public ApplicationEntity findApplicationEntity(String aet)
            throws ConfigurationException {
        if (!configurationExists())
            throw new ConfigurationNotFoundException();

        try {
            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctls.setCountLimit(1);
            ctls.setReturningAttributes(StringUtils.EMPTY_STRING);
            ctls.setReturningObjFlag(false);
            NamingEnumeration<SearchResult> ne = ctx.search(
                    devicesDN,
                    "(&(objectclass=dicomNetworkAE)(dicomAETitle=" + aet + "))",
                    ctls);
            String aeDN;
            try {
                if (!ne.hasMore())
                    throw new ConfigurationNotFoundException(aet);
    
                aeDN = ne.next().getNameInNamespace();
            } finally {
               safeClose(ne);
            }
            String deviceDN = aeDN.substring(aeDN.indexOf(',') + 1);
            Device device = loadDevice(deviceDN);
            return device.getApplicationEntity(aet);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public Device findDevice(String name) throws ConfigurationException {
        if (!configurationExists())
            throw new ConfigurationNotFoundException();

        return loadDevice(deviceDN(name));
    }

    @Override
    public void persist(Device device) throws ConfigurationException {
        ensureConfigurationExists();
        String deviceName = device.getDeviceName();
        String deviceDN = deviceDN(deviceName);
        boolean rollback = false;
        try {
            createSubcontext(ctx, deviceDN, storeTo(device, new BasicAttributes(true)));
            rollback = true;
            for (Connection conn : device.listConnections())
                createSubcontext(ctx, dnOf(conn, deviceDN), storeTo(conn, new BasicAttributes(true)));
            for (ApplicationEntity ae : device.getApplicationEntities()) {
                String aeDN = aetDN(ae.getAETitle(), deviceDN);
                createSubcontext(ctx, aeDN, storeTo(ae, deviceDN, new BasicAttributes(true)));
                for (TransferCapability tc : ae.getTransferCapabilities())
                    createSubcontext(ctx, dnOf(tc, aeDN), storeTo(tc, new BasicAttributes(true)));
            }
            rollback = false;
        } catch (NameAlreadyBoundException e) {
            throw new ConfigurationAlreadyExistsException(deviceName);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } finally {
            if (rollback)
                try {
                    destroySubcontext(ctx, deviceDN);
                } catch (NamingException e) {
                    LOG.warn("Rollback failed:", e);
                }
        }
    }

    @Override
    public void merge(Device device) throws ConfigurationException {
        if (!configurationExists())
            throw new ConfigurationNotFoundException();

        String deviceDN = deviceDN(device.getDeviceName());
        Device prev = loadDevice(deviceDN);
        try {
            ctx.modifyAttributes(deviceDN, storeDiffs(prev, device));
            mergeConnections(prev, device, deviceDN);
            mergeAEs(prev, device, deviceDN);
        } catch (NameNotFoundException e) {
            throw new ConfigurationNotFoundException(e);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public void removeDevice(String name) throws ConfigurationException {
        if (!configurationExists())
            throw new ConfigurationNotFoundException();

        removeDeviceWithDN(deviceDN(name));
    }

    private void removeDeviceWithDN(String deviceDN) throws ConfigurationException {
        try {
            destroySubcontext(ctx, deviceDN);
        } catch (NameNotFoundException e) {
            throw new ConfigurationNotFoundException(e);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
    }

    private static void createSubcontext(DirContext ctx, String name,
            Attributes attrs) throws NamingException {
        safeClose(ctx.createSubcontext(name, attrs));
    }

    private static void destroySubcontext(DirContext ctx, String name)
            throws NamingException {
        NamingEnumeration<NameClassPair> list = ctx.list(name);
        try {
            if (list.hasMore()) {
                DirContext subContext = (DirContext) ctx.lookup(name);
                try {
                    do {
                        destroySubcontext(subContext, list.next().getName());
                    } while (list.hasMore());
                } finally {
                    safeClose(subContext);
                }
            }
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
            createSubcontext(ctx, configurationDN,
                    attrs(configurationRoot, "cn", configurationCN));
            createSubcontext(ctx, devicesDN,
                    attrs("dicomDevicesRoot", "cn", "Devices"));
            createSubcontext(ctx, aetsRegistryDN,
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

   protected String objectclassOf(Device device) {
       return "dicomDevice";
   }

   protected String objectclassOf(Connection conn) {
       return "dicomNetworkConnection";
   }

   protected String objectclassOf(ApplicationEntity ae) {
       return "dicomNetworkAE";
   }

   protected String objectclassOf(TransferCapability tc) {
       return "dicomTransferCapability";
   }

   protected Attributes storeTo(Device device, Attributes attrs) {
        attrs.put("objectclass", objectclassOf(device));
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
        attrs.put("objectclass", objectclassOf(conn));
        storeNotNull(attrs, "cn", conn.getCommonName());
        storeNotNull(attrs, "dicomHostname", conn.getHostname());
        storeNotDef(attrs, "dicomPort", conn.getPort(), Connection.NOT_LISTENING);
        storeNotEmpty(attrs, "dicomTLSCipherSuite", conn.getTlsCipherSuites());
        storeBoolean(attrs, "dicomInstalled", conn.getInstalled());
        return attrs;
    }

    protected Attributes storeTo(ApplicationEntity ae, String deviceDN, Attributes attrs) {
        attrs.put("objectclass", objectclassOf(ae));
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
        storeBoolean(attrs, "dicomInstalled", ae.getInstalled());
        return attrs;
    }

    protected Attributes storeTo(TransferCapability tc, Attributes attrs) {
        attrs.put("objectclass", objectclassOf(tc));
        storeNotNull(attrs, "cn", tc.getCommonName());
        storeNotNull(attrs, "dicomSOPClass", tc.getSopClass());
        storeNotNull(attrs, "dicomTransferRole", tc.getRole().toString());
        storeNotEmpty(attrs, "dicomTransferSyntax", tc.getTransferSyntaxes());
        return attrs;
    }

    private Device loadDevice(String deviceDN) throws ConfigurationException {
        try {
            Attributes attrs = ctx.getAttributes(deviceDN);
            Device device = newDevice(toString(attrs.get("dicomDeviceName")));
            loadFrom(device, attrs);
            loadConnections(deviceDN, device);
            loadApplicationEntities(deviceDN, device);
            return device;
        } catch (NameNotFoundException e) {
            throw new ConfigurationNotFoundException(e);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
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

    protected TransferCapability newTransferCapability(Attributes attrs)
            throws NamingException {
        return new TransferCapability(
                toString(attrs.get("cn")),
                toString(attrs.get("dicomSOPClass")),
                TransferCapability.Role.valueOf(toString(attrs.get("dicomTransferRole"))),
                toStrings(attrs.get("dicomTransferSyntax")));
    }

    protected void loadFrom(Device device, Attributes attrs) throws NamingException {
        device.setDescription(toString(attrs.get("dicomDescription")));
        device.setManufacturer(toString(attrs.get("dicomManufacturer")));
        device.setManufacturerModelName(toString(attrs.get("dicomManufacturerModelName")));
        device.setSoftwareVersions(toStrings(attrs.get("dicomSoftwareVersion")));
        device.setStationName(toString(attrs.get("dicomStationName")));
        device.setDeviceSerialNumber(toString(attrs.get("dicomDeviceSerialNumber")));
        device.setIssuerOfPatientID(toString(attrs.get("dicomIssuerOfPatientID")));
        device.setInstitutionNames(toStrings(attrs.get("dicomInstitutionName")));
        device.setInstitutionAddresses(toStrings(attrs.get("dicomInstitutionAddress")));
        device.setInstitutionalDepartmentNames(
                toStrings(attrs.get("dicomInstitutionalDepartmentName")));
        device.setPrimaryDeviceTypes(toStrings(attrs.get("dicomPrimaryDeviceType")));
        device.setRelatedDeviceRefs(toStrings(attrs.get("dicomRelatedDeviceReference")));
        device.setAuthorizedNodeCertificateRefs(
                toStrings(attrs.get("dicomAuthorizedNodeCertificateReference")));
        device.setThisNodeCertificateRefs(
                toStrings(attrs.get("dicomThisNodeCertificateReference")));
        device.setVendorData(toVendorData(attrs.get("dicomVendorData")));
        try {
            device.setInstalled(toBoolean(attrs.get("dicomInstalled"), Boolean.TRUE));
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
    }

    private void loadConnections(String deviceDN, Device device) throws NamingException {
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        ctls.setReturningObjFlag(false);
        NamingEnumeration<SearchResult> ne = ctx.search(
                deviceDN,
                "(objectclass=dicomNetworkConnection)",
                ctls);
        try {
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Connection conn = newConnection();
                loadFrom(conn, sr.getAttributes());
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

    protected void loadFrom(Connection conn, Attributes attrs) throws NamingException {
        conn.setCommonName(toString(attrs.get("cn")));
        conn.setHostname(toString(attrs.get("dicomHostname")));
        conn.setPort(toInt(attrs.get("dicomPort"), Connection.NOT_LISTENING));
        conn.setTlsCipherSuites(toStrings(attrs.get("dicomTLSCipherSuite")));
        try {
            conn.setInstalled(toBoolean(attrs.get("dicomInstalled"), null));
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        }
    }

    private void loadApplicationEntities(String deviceDN, Device device)
            throws NamingException {
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        ctls.setReturningObjFlag(false);
        NamingEnumeration<SearchResult> ne = ctx.search(
                deviceDN,
                "(objectclass=dicomNetworkAE)",
                ctls);
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
        ApplicationEntity ae = newApplicationEntity(toString(attrs.get("dicomAETitle")));
        loadFrom(ae, attrs);
        for (String connDN : toStrings(attrs.get("dicomNetworkConnectionReference")))
            ae.addConnection(findConnection(connDN, deviceDN, device));
        loadTransferCapabilities(sr.getNameInNamespace(), ae);
        return ae ;
    }

    private Connection findConnection(String connDN, String deviceDN, Device device)
            throws NameNotFoundException {
        for (Connection conn : device.listConnections())
            if (dnOf(conn, deviceDN).equals(connDN))
                return conn;

        throw new NameNotFoundException(connDN);
    }

    protected void loadFrom(ApplicationEntity ae, Attributes attrs) throws NamingException {
        ae.setDescription(toString(attrs.get("dicomDescription")));
        ae.setVendorData(toVendorData(attrs.get("dicomVendorData")));
        ae.setApplicationClusters(toStrings(attrs.get("dicomApplicationCluster")));
        ae.setPreferredCallingAETitles(toStrings(attrs.get("dicomPreferredCallingAETitle")));
        ae.setPreferredCalledAETitles(toStrings(attrs.get("dicomPreferredCalledAETitle")));
        ae.setAssociationInitiator(toBoolean(attrs.get("dicomAssociationInitiator"), Boolean.FALSE));
        ae.setAssociationAcceptor(toBoolean(attrs.get("dicomAssociationAcceptor"), Boolean.FALSE));
        ae.setSupportedCharacterSets(toStrings(attrs.get("dicomSupportedCharacterSet")));
        ae.setInstalled(toBoolean(attrs.get("dicomInstalled"), null));
    }

    private void loadTransferCapabilities(String aeDN, ApplicationEntity ae)
            throws NamingException {
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        ctls.setReturningObjFlag(false);
        NamingEnumeration<SearchResult> ne = ctx.search(
                aeDN,
                "(objectclass=dicomTransferCapability)",
                ctls);
        try {
            while (ne.hasMore()) {
                ae.addTransferCapability(
                        newTransferCapability(ne.next().getAttributes()));
            }
        } finally {
           safeClose(ne);
        }
    }

    protected void storeDiffs(Collection<ModificationItem> mods, Device a, Device b) {
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
    }

    protected void storeDiffs(Collection<ModificationItem> mods, Connection a, Connection b) {
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
    }

    protected void storeDiffs(Collection<ModificationItem> mods,
            ApplicationEntity a, ApplicationEntity b, String deviceDN) {
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
    }

    protected void storeDiffs(Collection<ModificationItem> mods,
            TransferCapability a, TransferCapability b) {
        storeDiff(mods, "dicomSOPClass",
                a.getSopClass(),
                b.getSopClass());
        storeDiff(mods, "dicomTransferRole",
                a.getRole().toString(),
                b.getRole().toString());
        storeDiff(mods, "dicomTransferSyntax",
                a.getTransferSyntaxes(),
                b.getTransferSyntaxes());
    }

    static int toInt(Attribute attr, int defVal) throws NamingException {
        return attr != null ? Integer.parseInt((String) attr.get()) : defVal;
    }

    private static byte[][] toVendorData(Attribute attr) throws NamingException {
        if (attr == null)
            return new byte[0][];

        byte[][] bb = new byte[attr.size()][];
        for (int i = 0; i < bb.length; i++)
            bb[i] = (byte[]) attr.get(i);

        return bb;
    }

    static String[] toStrings(Attribute attr) throws NamingException {
        if (attr == null)
            return StringUtils.EMPTY_STRING;

        String[] ss = new String[attr.size()];
        for (int i = 0; i < ss.length; i++)
            ss[i] = (String) attr.get(i);

        return ss;
    }

    private static String toString(Attribute attr) throws NamingException {
        return attr != null ? (String) attr.get() : null;
    }

    static Boolean toBoolean(Attribute attr, Boolean defVal) throws NamingException {
        return attr != null ? Boolean.valueOf((String) attr.get()) : defVal;
    }

    private ModificationItem[] storeDiffs(Device prev, Device device) {
        ArrayList<ModificationItem> mods = new ArrayList<ModificationItem>();
        storeDiffs(mods, prev, device);
        return mods.toArray(new ModificationItem[mods.size()]);
    }

    private void mergeAEs(Device prevDev, Device dev, String deviceDN)
            throws NamingException {
        for (ApplicationEntity ae : prevDev.getApplicationEntities()) {
            String aet = ae.getAETitle();
            if (dev.getApplicationEntity(aet) == null)
                destroySubcontext(ctx, aetDN(aet, deviceDN));
        }
        for (ApplicationEntity ae : dev.getApplicationEntities()) {
            String aet = ae.getAETitle();
            ApplicationEntity prevAE = prevDev.getApplicationEntity(aet);
            if (prevAE == null) {
                String aeDN = aetDN(ae.getAETitle(), deviceDN);
                createSubcontext(ctx, aeDN,
                        storeTo(ae, deviceDN, new BasicAttributes(true)));
                for (TransferCapability tc : ae.getTransferCapabilities())
                    createSubcontext(ctx, dnOf(tc, aeDN),
                            storeTo(tc, new BasicAttributes(true)));
            } else
                merge(prevAE, ae, deviceDN);
        }
    }

    private void merge(ApplicationEntity prev, ApplicationEntity ae,
            String deviceDN) throws NamingException {
        String aeDN = aetDN(ae.getAETitle(), deviceDN);
        ctx.modifyAttributes(aeDN, diffsOf(prev, ae, deviceDN));
        merge(prev.getTransferCapabilities(), ae.getTransferCapabilities(), aeDN);
    }

    private ModificationItem[] diffsOf(ApplicationEntity prev,
            ApplicationEntity ae, String deviceDN) {
        ArrayList<ModificationItem> mods = new ArrayList<ModificationItem>();
        storeDiffs(mods, prev, ae, deviceDN);
        return mods.toArray(new ModificationItem[mods.size()]);
    }

    private void merge(Collection<TransferCapability> prevs,
            Collection<TransferCapability> tcs, String aeDN) throws NamingException {
        for (TransferCapability tc : prevs) {
            String dn = dnOf(tc, aeDN);
            if (findByDN(aeDN, tcs, dn) == null)
                ctx.destroySubcontext(dn);
        }
        for (TransferCapability tc : tcs) {
            String dn = dnOf(tc, aeDN);
            TransferCapability prev = findByDN(aeDN, prevs, dn);
            if (prev == null)
                createSubcontext(ctx, dn, storeTo(tc, new BasicAttributes(true)));
            else
                ctx.modifyAttributes(dn, storeDiffs(prev, tc));
        }
    }

    private ModificationItem[] storeDiffs(TransferCapability prev,
            TransferCapability tc) {
        ArrayList<ModificationItem> mods = new ArrayList<ModificationItem>();
        storeDiffs(mods, prev, tc);
        return mods.toArray(new ModificationItem[mods.size()]);
    }

    private void mergeConnections(Device prevDev, Device device, String deviceDN)
            throws NamingException {
        List<Connection> prevs = prevDev.listConnections();
        List<Connection> conns = device.listConnections();
        for (Connection prev : prevs) {
            String dn = dnOf(prev, deviceDN);
            if (findByDN(deviceDN, conns, dn) == null)
                ctx.destroySubcontext(dn);
        }
        for (Connection conn : conns) {
            String dn = dnOf(conn, deviceDN);
            Connection prev = findByDN(deviceDN, prevs, dn);
            if (prev == null)
                ctx.createSubcontext(dn, storeTo(conn, new BasicAttributes(true)));
            else
                ctx.modifyAttributes(dn, storeDiffs(prev, conn));
        }
    }

    private ModificationItem[] storeDiffs(Connection prev, Connection conn) {
        ArrayList<ModificationItem> mods = new ArrayList<ModificationItem>();
        storeDiffs(mods, prev, conn);
        return mods.toArray(new ModificationItem[mods.size()]);
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


    static void storeDiff(Collection<ModificationItem> mods, String attrId,
            Boolean prev, Boolean val) {
        if (val == null) {
            if (prev != null)
                mods.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                        new BasicAttribute(attrId)));
        } else if (!val.equals(prev))
            mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    new BasicAttribute(attrId, toString(val))));
    }

    private static void storeDiff(Collection<ModificationItem> mods, String attrId,
            byte[][] prevs, byte[][] vals) {
        if (!equals(prevs, vals))
            mods.add((vals.length == 0)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            attr(attrId, vals)));
    }

    private static boolean equals(byte[][] bb1, byte[][] bb2) {
        if (bb1.length != bb2.length)
            return false;
        
        for (int i = 0; i < bb1.length; i++)
            if (!Arrays.equals(bb1[i], bb2[i]))
                return false;

        return true;
    }

    private static void storeDiff(Collection<ModificationItem> mods, String attrId,
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

    static void storeDiff(Collection<ModificationItem> mods, String attrId,
            String[] prevs, String[] vals) {
        if (!Arrays.equals(prevs, vals))
            mods.add((vals.length == 0)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            attr(attrId, vals)));
    }

    static void storeDiff(Collection<ModificationItem> mods, String attrId,
            String prev, String val) {
        if (val == null) {
            if (prev != null)
                mods.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                        new BasicAttribute(attrId)));
        } else if (!val.equals(prev))
            mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    new BasicAttribute(attrId, val)));
    }

    static void storeDiff(Collection<ModificationItem> mods,
            String attrId, int prev, int val, int defVal) {
        if (val != prev)
            mods.add((val == defVal)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            new BasicAttribute(attrId, val)));
    }


    private static String dnOf(String attrID, String attrValue, String parentDN) {
        return attrID + '=' + attrValue + ',' + parentDN;
    }

    private static String dnOf(String attrID1, String attrValue1,
            String attrID2, String attrValue2, String baseDN) {
        return attrID1 + '=' + attrValue1 + '+' + attrID2 + '=' + attrValue2 + ','  + baseDN;
    }

    private static String aetDN(String aet, String parentDN) {
        return dnOf("dicomAETitle" ,aet, parentDN);
    }

    private String deviceDN(String name) {
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

    protected static void storeBoolean(Attributes attrs, String attrID, Boolean val) {
        if (val != null)
            attrs.put(attrID, toString(val));
    }

    private static String toString(boolean b) {
        return b ? "TRUE" : "FALSE";
    }

    protected static void storeNotNull(Attributes attrs, String attrID, Object val) {
        if (val != null)
            attrs.put(attrID, val);
    }

    protected static void storeNotDef(Attributes attrs, String attrID, int val, int defVal) {
        if (val != defVal)
            attrs.put(attrID, Integer.toString(val, 10));
    }

    protected static <T> void storeNotEmpty(Attributes attrs, String attrID, T... vals) {
        if (vals.length > 0)
            attrs.put(attr(attrID, vals));
    }

    private static <T> Attribute attr(String attrID, T... vals) {
        Attribute attr = new BasicAttribute(attrID);
        for (T val : vals)
            attr.add(val);
        return attr;
    }

    private static void storeConnRefs(Attributes attrs, Collection<Connection> conns,
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

    private static <T> void safeClose(NamingEnumeration<T> enumeration) {
        if (enumeration != null)
            try {
                enumeration.close();
            } catch (NamingException e) {
            }
    }

}
