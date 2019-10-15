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
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2017
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
import java.util.*;

import javax.naming.*;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.dcm4che3.conf.api.ConfigurationChanges;
import org.dcm4che3.conf.api.*;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.io.BasicBulkDataDescriptor;
import org.dcm4che3.net.*;
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
    private static final String CN_UNIQUE_WEB_APP_NAMES_REGISTRY = "cn=Unique Web Application Names Registry,";
    private static final String CN_DEVICES = "cn=Devices,";
    private static final String DICOM_CONFIGURATION = "DICOM Configuration";
    private static final String DICOM_CONFIGURATION_ROOT = "dicomConfigurationRoot";
    private static final String PKI_USER = "pkiUser";
    private static final String USER_CERTIFICATE_BINARY = "userCertificate;binary";
    private static final X509Certificate[] EMPTY_X509_CERTIFICATES = {};

    private final ReconnectDirContext ctx;
    private final String baseDN;
    private String configurationDN;
    private String devicesDN;
    private String aetsRegistryDN;
    private String webAppsRegistryDN;
    private String configurationCN = DICOM_CONFIGURATION;
    private String configurationRoot = DICOM_CONFIGURATION_ROOT;
    private String pkiUser = PKI_USER;
    private String userCertificate = USER_CERTIFICATE_BINARY;
    private boolean extended = true;

    private final List<LdapDicomConfigurationExtension> extensions = new ArrayList<>();

    /**
     * Needed for avoiding infinite loops when dealing with extensions containing circular references
     * e.g., one device extension references another device which has an extension that references the former device.
     * Devices that have been created but not fully loaded are added to this threadlocal. See loadDevice.
     */
    private ThreadLocal<Map<String,Device>> currentlyLoadedDevicesLocal = new ThreadLocal<>();

    static final String[] AE_ATTRS = {
            "dicomDeviceName",
            "dicomAETitle",
            "dcmOtherAETitle",
            "dicomDescription",
            "dicomAssociationInitiator",
            "dicomAssociationAcceptor",
            "dicomApplicationCluster",
            "dicomInstalled",
            "hl7ApplicationName",
            "dicomNetworkConnectionReference"
    };

    static final String[] WEBAPP_ATTRS = {
            "dicomDeviceName",
            "dcmWebAppName",
            "dcmWebServicePath",
            "dcmWebServiceClass",
            "dcmKeycloakClientID",
            "dicomAETitle",
            "dicomDescription",
            "dicomApplicationCluster",
            "dicomInstalled",
            "dicomNetworkConnectionReference"
    };

    public LdapDicomConfiguration() throws ConfigurationException {
        this(ResourceManager.getInitialEnvironment());
    }

    @SuppressWarnings("unchecked")
    public LdapDicomConfiguration(Hashtable<?,?> env)
            throws ConfigurationException {
        Hashtable<String,String> map = new Hashtable();
        for (Map.Entry<?, ?> entry : env.entrySet())
            map.put((String) entry.getKey(),
                    StringUtils.replaceSystemProperties((String) entry.getValue()));

        try {
            // split baseDN from LDAP URL
            String s = map.get(Context.PROVIDER_URL);
            int end = s.lastIndexOf('/');
            map.put(Context.PROVIDER_URL, s.substring(0, end));
            this.baseDN = s.substring(end+1);
            this.ctx = new ReconnectDirContext(map);
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
            if (clazz.isInstance(ext))
                return (T) ext;
        }
        return null;
    }

    @Override
    public synchronized void close() {
        ctx.close();
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
            registerAET(aet);
            return true;
        } catch (AETitleAlreadyExistsException e) {
            return false;
        }
    }

    @Override
    public synchronized boolean registerWebAppName(String webAppName) throws ConfigurationException {
        ensureConfigurationExists();
        try {
            registerWebApp(webAppName);
            return true;
        } catch (WebAppAlreadyExistsException e) {
            return false;
        }
    }

    private String registerAET(String aet) throws ConfigurationException {
        try {
            String dn = aetDN(aet, aetsRegistryDN);
            createSubcontext(dn, LdapUtils.attrs("dicomUniqueAETitle", "dicomAETitle", aet));
            return dn;
        } catch (NameAlreadyBoundException e) {
            throw new AETitleAlreadyExistsException("AE Title '" + aet + "' already exists");
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
    }

    private String registerWebApp(String webAppName) throws ConfigurationException {
        try {
            String dn = webAppDN(webAppName, webAppsRegistryDN);
            createSubcontext(dn, LdapUtils.attrs("dcmUniqueWebAppName", "dcmWebAppName", webAppName));
            return dn;
        } catch (NameAlreadyBoundException e) {
            throw new WebAppAlreadyExistsException("Web Application '" + webAppName + "' already exists");
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
    public synchronized void unregisterWebAppName(String webAppName) throws ConfigurationException {
        if (configurationExists())
            try {
                ctx.destroySubcontext(webAppDN(webAppName, webAppsRegistryDN));
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

    @Override
    public synchronized WebApplication findWebApplication(String name) throws ConfigurationException {
        return findDevice("(&(objectclass=dcmWebApp)(dcmWebAppName=" + name + "))", name)
            .getWebApplication(name);
    }

    public synchronized Device findDevice(String filter, String childName)
            throws ConfigurationException {
        if (!configurationExists())
            throw new ConfigurationNotFoundException();

        SearchControls ctls = searchControlSubtreeScope(1, StringUtils.EMPTY_STRING, false);
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

    public Connection findConnection(String connDN, Map<String, Connection> cache)
            throws NamingException, ConfigurationException {
        Connection conn = cache.get(connDN);
        if (conn == null) {
            try {
                String[] attrIds = {"dicomHostname", "dicomPort", "dicomTLSCipherSuite", "dicomInstalled"};
                Attributes attrs = ctx.getAttributes(connDN, attrIds);
                cache.put(connDN, conn = new Connection());
                loadFrom(conn, attrs, false);
            } catch (NameNotFoundException e) {
                throw new ConfigurationException(e);
            }
        }
        return conn;
    }

    private SearchControls searchControlSubtreeScope(int countLimit, String[] returningAttrs, boolean returningObjFlag) {
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctls.setCountLimit(countLimit);
        ctls.setReturningAttributes(returningAttrs);
        ctls.setReturningObjFlag(returningObjFlag);
        return ctls;
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
                "dicomInstalled",
                "objectClass");
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

    private static <T> void appendFilter(String attrid, T value, StringBuilder sb) {
        if (value == null)
            return;

        sb.append('(').append(attrid).append('=').append(LdapUtils.toString(value)).append(')');
    }

    private static <T> void appendFilter(String attrid, T[] values, StringBuilder sb) {
        if (values.length == 0)
            return;

        if (values.length == 1) {
            appendFilter(attrid, values[0], sb);
            return;
        }

        sb.append("(|");
        for (T value : values)
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
        deviceInfo.setArcDevExt(LdapUtils.hasObjectClass(attrs, "dcmArchiveDevice"));
    }

    private void loadFrom(ApplicationEntityInfo aetInfo, Attributes attrs, String deviceName,
            Map<String, Connection> connCache)
            throws NamingException, ConfigurationException {
        aetInfo.setDeviceName(deviceName);
        aetInfo.setAETitle(
                LdapUtils.stringValue(attrs.get("dicomAETitle"), null));
        aetInfo.setOtherAETitle(
                LdapUtils.stringArray(attrs.get("dcmOtherAETitle")));
        aetInfo.setDescription(
                LdapUtils.stringValue(attrs.get("dicomDescription"), null));
        aetInfo.setAssociationInitiator(
                LdapUtils.booleanValue(attrs.get("dicomAssociationInitiator"), true));
        aetInfo.setAssociationAcceptor(
                LdapUtils.booleanValue(attrs.get("dicomAssociationAcceptor"), true));
        aetInfo.setInstalled(
                LdapUtils.booleanValue(attrs.get("dicomInstalled"), null));
        aetInfo.setApplicationClusters(
                LdapUtils.stringArray(attrs.get("dicomApplicationCluster")));
        aetInfo.setHl7ApplicationName(
                LdapUtils.stringValue(attrs.get("hl7ApplicationName"), null));
        for (String connDN : LdapUtils.stringArray(attrs.get("dicomNetworkConnectionReference")))
            aetInfo.getConnections().add(findConnection(connDN, connCache));
    }

    private void loadFrom(WebApplicationInfo webappInfo, Attributes attrs, String deviceName,
            Map<String, Connection> connCache, Map<String, KeycloakClient> keycloakClientCache)
            throws NamingException, ConfigurationException {
        webappInfo.setDeviceName(deviceName);
        webappInfo.setApplicationName(LdapUtils.stringValue(attrs.get("dcmWebAppName"), null));
        webappInfo.setDescription(LdapUtils.stringValue(attrs.get("dicomDescription"), null));
        webappInfo.setServicePath(LdapUtils.stringValue(attrs.get("dcmWebServicePath"), null));
        webappInfo.setServiceClasses(LdapUtils.enumArray(WebApplication.ServiceClass.class, attrs.get("dcmWebServiceClass")));
        webappInfo.setAETitle(LdapUtils.stringValue(attrs.get("dicomAETitle"), null));
        webappInfo.setApplicationClusters(LdapUtils.stringArray(attrs.get("dicomApplicationCluster")));
        String keycloakClientID = LdapUtils.stringValue(attrs.get("dcmKeycloakClientID"), null);
        webappInfo.setKeycloakClientID(keycloakClientID);
        webappInfo.setInstalled(LdapUtils.booleanValue(attrs.get("dicomInstalled"), null));
        for (String connDN : LdapUtils.stringArray(attrs.get("dicomNetworkConnectionReference")))
            webappInfo.getConnections().add(findConnection(connDN, connCache));
        if (keycloakClientID != null)
            webappInfo.setKeycloakClient(findKeycloakClient(keycloakClientID, deviceName, keycloakClientCache));
    }

    private KeycloakClient findKeycloakClient(String clientID, String deviceName, Map<String, KeycloakClient> cache)
            throws NamingException, ConfigurationException {
        String keycloakClientDN = keycloakClientDN(clientID, deviceRef(deviceName));
        KeycloakClient keycloakClient = cache.get(keycloakClientDN);
        if (keycloakClient == null) {
            try {
                Attributes attrs = ctx.getAttributes(keycloakClientDN);
                cache.put(keycloakClientDN, keycloakClient = new KeycloakClient(clientID));
                loadFrom(keycloakClient, attrs);
            } catch (NameNotFoundException e) {
                throw new ConfigurationException(e);
            }
        }
        return keycloakClient;
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

    @Override
    public synchronized String[] listRegisteredWebAppNames() throws ConfigurationException {
        if (!configurationExists())
            return StringUtils.EMPTY_STRING;

        return list(webAppsRegistryDN, "(objectclass=dcmUniqueWebAppName)", "dcmWebAppName");
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
    public synchronized ConfigurationChanges persist(Device device, EnumSet<Option> options)
            throws ConfigurationException {
        ensureConfigurationExists();
        String deviceName = device.getDeviceName();
        String deviceDN = deviceRef(deviceName);
        boolean rollback = false;
        ArrayList<String> destroyDNs = new ArrayList<>();

        try {
            if (options != null && options.contains(Option.REGISTER))
                register(device, destroyDNs);

            ConfigurationChanges diffs = configurationChangesOf(options);
            ConfigurationChanges.ModifiedObject ldapObj =
                    ConfigurationChanges.addModifiedObject(diffs, deviceDN, ConfigurationChanges.ChangeType.C);
            createSubcontext(deviceDN,
                    storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                            device, new BasicAttributes(true)));
            rollback = true;
            storeChilds(ConfigurationChanges.nullifyIfNotVerbose(diffs, diffs), deviceDN, device);
            if (options == null || !options.contains(Option.PRESERVE_CERTIFICATE))
                updateCertificates(device);
            rollback = false;
            destroyDNs.clear();
            return diffs;
        } catch (NameAlreadyBoundException e) {
            throw new ConfigurationAlreadyExistsException(deviceName);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } catch (CertificateException e) {
            throw new ConfigurationException(e);
        } finally {
            if (rollback) {
                try {
                    destroySubcontextWithChilds(deviceDN);
                } catch (NamingException e) {
                    LOG.warn("Rollback failed:", e);
                }
            }
            unregister(destroyDNs);
        }
    }

    private ConfigurationChanges configurationChangesOf(EnumSet<Option> options) {
        return options != null
                && (options.contains(Option.CONFIGURATION_CHANGES)
                || options.contains(Option.CONFIGURATION_CHANGES_VERBOSE))
                    ? new ConfigurationChanges(options.contains(Option.CONFIGURATION_CHANGES_VERBOSE))
                    : null;
    }

    private void unregister(ArrayList<String> destroyDNs) {
        for (String dn : destroyDNs) {
            try {
                destroySubcontext(dn);
            } catch (NamingException e) {
                LOG.warn("Unregister {} failed:", dn, e);
            }
        }
    }

    private void register(Device device, List<String> dns) throws ConfigurationException {
        for (String aet : device.getApplicationAETitles())
            if (!aet.equals("*"))
                dns.add(registerAET(aet));
        for (String webAppName : device.getWebApplicationNames())
            if (!webAppName.equals("*"))
                dns.add(registerWebApp(webAppName));
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.register(device, dns);
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

    private void storeChilds(ConfigurationChanges diffs, String deviceDN, Device device)
            throws NamingException, ConfigurationException {
        for (Connection conn : device.listConnections()) {
            String dn = LdapUtils.dnOf(conn, deviceDN);
            ConfigurationChanges.ModifiedObject ldapObj =
                    ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.C);
            createSubcontext(dn, storeTo(ldapObj, conn, new BasicAttributes(true)));
        }
        for (ApplicationEntity ae : device.getApplicationEntities())
            store(diffs, ae, deviceDN);
        if (extended) {
            for (WebApplication webapp : device.getWebApplications())
                store(diffs, webapp, deviceDN);
            for (KeycloakClient client : device.getKeycloakClients())
                store(diffs, client, deviceDN);
            for (LdapDicomConfigurationExtension ext : extensions)
                ext.storeChilds(diffs, deviceDN, device);
        }
    }

    private void store(ConfigurationChanges diffs, ApplicationEntity ae, String deviceDN) throws NamingException {
        String aeDN = aetDN(ae.getAETitle(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, aeDN, ConfigurationChanges.ChangeType.C);
        createSubcontext(aeDN,
                storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                        ae, deviceDN, new BasicAttributes(true)));
        storeChilds(ConfigurationChanges.nullifyIfNotVerbose(diffs, diffs), aeDN, ae);
    }

    private void store(ConfigurationChanges diffs, WebApplication webapp, String deviceDN) throws NamingException {
        String webappDN = webAppDN(webapp.getApplicationName(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, webappDN, ConfigurationChanges.ChangeType.C);
        createSubcontext(webappDN,
                storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                        webapp, deviceDN, new BasicAttributes(true)));
    }

    private void store(ConfigurationChanges diffs, KeycloakClient client, String deviceDN) throws NamingException {
        String clientDN = keycloakClientDN(client.getKeycloakClientID(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, clientDN, ConfigurationChanges.ChangeType.C);
        createSubcontext(clientDN,
                storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                        client, new BasicAttributes(true)));
    }

    private void storeChilds(ConfigurationChanges diffs, String aeDN, ApplicationEntity ae)
            throws NamingException {
        for (TransferCapability tc : ae.getTransferCapabilities()) {
            ConfigurationChanges.ModifiedObject ldapObj =
                    ConfigurationChanges.addModifiedObject(diffs, aeDN, ConfigurationChanges.ChangeType.C);
            createSubcontext(dnOf(tc, aeDN), storeTo(ldapObj, tc, new BasicAttributes(true)));
        }
        if (extended)
            for (LdapDicomConfigurationExtension ext : extensions)
                ext.storeChilds(diffs, aeDN, ae);
    }

    @Override
    public ConfigurationChanges merge(Device device, EnumSet<Option> options) throws ConfigurationException {
        ConfigurationChanges diffs = configurationChangesOf(options);
        merge(device, options, diffs);
        return diffs;
    }

    private synchronized void merge(Device device, EnumSet<Option> options, ConfigurationChanges diffs)
        throws ConfigurationException {
        if (!configurationExists())
            throw new ConfigurationNotFoundException();

        String deviceDN = deviceRef(device.getDeviceName());
        Device prev = loadDevice(deviceDN);
        ArrayList<String> destroyDNs = new ArrayList<>();
        try {
            boolean register = options != null && options.contains(Option.REGISTER);
            boolean preserveVendorData = options != null && options.contains(Option.PRESERVE_VENDOR_DATA);
            if (register) {
                registerDiff(prev, device, destroyDNs);
            }
            ConfigurationChanges.ModifiedObject ldapObj =
                    ConfigurationChanges.addModifiedObject(diffs, deviceDN, ConfigurationChanges.ChangeType.U);
            modifyAttributes(deviceDN,
                    storeDiffs(ldapObj, prev, device, new ArrayList<ModificationItem>(), preserveVendorData));
            ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
            mergeChilds(diffs, prev, device, deviceDN, preserveVendorData);
            destroyDNs.clear();
            if (register) {
                markForUnregister(prev, device, destroyDNs);
            }
            if (options == null || !options.contains(Option.PRESERVE_CERTIFICATE))
                updateCertificates(prev, device);
        } catch (NameNotFoundException e) {
            throw new ConfigurationNotFoundException(e);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } catch (CertificateException e) {
            throw new ConfigurationException(e);
        } finally {
            unregister(destroyDNs);
        }
    }

    private void registerDiff(Device prev, Device device, List<String> dns) throws ConfigurationException {
        for (String aet : device.getApplicationAETitles())
            if (!aet.equals("*") && prev.getApplicationEntity(aet) == null)
                dns.add(registerAET(aet));
        for (String webAppName : device.getWebApplicationNames())
            if (!webAppName.equals("*") && prev.getWebApplication(webAppName) == null)
                dns.add(registerWebApp(webAppName));
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.registerDiff(prev, device, dns);
    }

    private void markForUnregister(Device prev, Device device, List<String> dns) {
        for (String aet : prev.getApplicationAETitles())
            if (!aet.equals("*") && device.getApplicationEntity(aet) == null)
                dns.add(aetDN(aet, aetsRegistryDN));
        for (String webAppName : prev.getWebApplicationNames())
            if (!webAppName.equals("*") && device.getWebApplication(webAppName) == null)
                dns.add(webAppDN(webAppName, webAppsRegistryDN));
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.markForUnregister(prev, device, dns);
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

    private void mergeChilds(ConfigurationChanges diffs, Device prev, Device device, String deviceDN, boolean preserveVendorData)
            throws NamingException, ConfigurationException {
        mergeConnections(diffs, prev, device, deviceDN);
        mergeAEs(diffs, prev, device, deviceDN, preserveVendorData);
        mergeWebApps(diffs, prev, device, deviceDN);
        mergeKeycloakClients(diffs, prev, device, deviceDN);
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.mergeChilds(diffs, prev, device, deviceDN);
    }

    @Override
    public synchronized ConfigurationChanges removeDevice(String name, EnumSet<Option> options)
            throws ConfigurationException {
        if (!configurationExists())
            throw new ConfigurationNotFoundException();

        String dn = deviceRef(name);
        removeDeviceWithDN(dn, options != null && options.contains(Option.REGISTER));
        ConfigurationChanges diffs = new ConfigurationChanges(false);
        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
        return diffs;
    }

    private void markForUnregister(String deviceDN, List<String> dns)
            throws NamingException, ConfigurationException {
        NamingEnumeration<SearchResult> ne =
                search(deviceDN, "(objectclass=dicomNetworkAE)", StringUtils.EMPTY_STRING);
        try {
            while (ne.hasMore()) {
                String rdn = ne.next().getName();
                if (!rdn.equals("dicomAETitle=*"))
                    dns.add(rdn + ',' + aetsRegistryDN);
                if (!rdn.equals("dcmWebAppName=*"))
                    dns.add(rdn + ',' + webAppsRegistryDN);
            }
        } finally {
            LdapUtils.safeClose(ne);
        }
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.markForUnregister(deviceDN, dns);
    }

    private void removeDeviceWithDN(String deviceDN, boolean unregister) throws ConfigurationException {
        try {
            ArrayList<String> destroyDNs = new ArrayList<>();
            if (unregister)
                markForUnregister(deviceDN, destroyDNs);
            destroySubcontextWithChilds(deviceDN);
            unregister(destroyDNs);
        } catch (NameNotFoundException e) {
            throw new ConfigurationNotFoundException(e);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
    }

    public synchronized void createSubcontext(String name, Attributes attrs)
            throws NamingException {
        ctx.createSubcontextAndClose(name, attrs);
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
        this.webAppsRegistryDN = CN_UNIQUE_WEB_APP_NAMES_REGISTRY + configurationDN;
    }

    public String getConfigurationDN() {
        return configurationDN;
    }

    private void clearConfigurationDN() {
        this.configurationDN = null;
        this.devicesDN = null;
        this.aetsRegistryDN = null;
        this.webAppsRegistryDN = null;
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
            createSubcontext(webAppsRegistryDN,
                    LdapUtils.attrs("dcmUniqueWebAppNamesRegistryRoot",
                            "cn", "Unique Web Application Names Registry"));
            LOG.info("Create DICOM Configuration at {}", configurationDN);
        } catch (NamingException e) {
            clearConfigurationDN();
            throw new ConfigurationException(e);
        }
    }

    private boolean findConfiguration() throws ConfigurationException {
        NamingEnumeration<SearchResult> ne = null;
        try {
            SearchControls ctls = searchControlSubtreeScope(1, StringUtils.EMPTY_STRING, false);
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

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, Device device, Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dicomDevice");
        attrs.put(objectclass);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomDeviceName", device.getDeviceName(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomDescription", device.getDescription(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomDeviceUID", device.getDeviceUID(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomManufacturer", device.getManufacturer(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomManufacturerModelName",
                device.getManufacturerModelName(), null);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dicomSoftwareVersion",
                device.getSoftwareVersions());
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomStationName", device.getStationName(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomDeviceSerialNumber",
                device.getDeviceSerialNumber(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomIssuerOfPatientID",
                device.getIssuerOfPatientID(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomIssuerOfAccessionNumber",
                device.getIssuerOfAccessionNumber(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomOrderPlacerIdentifier",
                device.getOrderPlacerIdentifier(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomOrderFillerIdentifier",
                device.getOrderFillerIdentifier(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomIssuerOfAdmissionID",
                device.getIssuerOfAdmissionID(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomIssuerOfServiceEpisodeID",
                device.getIssuerOfServiceEpisodeID(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomIssuerOfContainerIdentifier",
                device.getIssuerOfContainerIdentifier(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomIssuerOfSpecimenIdentifier",
                device.getIssuerOfSpecimenIdentifier(), null);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dicomInstitutionName",
                device.getInstitutionNames());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dicomInstitutionCode",
                device.getInstitutionCodes());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dicomInstitutionAddress",
                device.getInstitutionAddresses());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dicomInstitutionDepartmentName",
                device.getInstitutionalDepartmentNames());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dicomPrimaryDeviceType",
                device.getPrimaryDeviceTypes());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dicomRelatedDeviceReference",
                device.getRelatedDeviceRefs());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dicomAuthorizedNodeCertificateReference",
                device.getAuthorizedNodeCertificateRefs());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dicomThisNodeCertificateReference",
                device.getThisNodeCertificateRefs());
        storeNotEmpty(ldapObj, attrs, "dicomVendorData", device.getVendorData());
        LdapUtils.storeBoolean(ldapObj, attrs, "dicomInstalled", device.isInstalled());
        if (!extended)
            return attrs;

        objectclass.add("dcmDevice");
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmRoleSelectionNegotiationLenient",
                device.isRoleSelectionNegotiationLenient(), false);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmLimitOpenAssociations", device.getLimitOpenAssociations(), 0);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmLimitAssociationsInitiatedBy",
                device.getLimitAssociationsInitiatedBy());
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmTrustStoreURL", device.getTrustStoreURL(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmTrustStoreType", device.getTrustStoreType(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmTrustStorePin", device.getTrustStorePin(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmTrustStorePinProperty", device.getTrustStorePinProperty(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmKeyStoreURL", device.getKeyStoreURL(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmKeyStoreType", device.getKeyStoreType(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmKeyStorePin", device.getKeyStorePin(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmKeyStorePinProperty", device.getKeyStorePinProperty(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmKeyStoreKeyPin", device.getKeyStoreKeyPin(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmKeyStoreKeyPinProperty", device.getKeyStoreKeyPinProperty(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmTimeZoneOfDevice", device.getTimeZoneOfDevice(), null);
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.storeTo(ldapObj, device, attrs);
        return attrs;
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, Connection conn, Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dicomNetworkConnection");
        attrs.put(objectclass);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "cn", conn.getCommonName(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomHostname", conn.getHostname(), null);
        LdapUtils.storeNotDef(ldapObj, attrs, "dicomPort", conn.getPort(), Connection.NOT_LISTENING);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dicomTLSCipherSuite", conn.getTlsCipherSuites());
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomInstalled", conn.getInstalled(), null);
        if (!extended)
            return attrs;

        objectclass.add("dcmNetworkConnection");
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmProtocol", conn.getProtocol(), Protocol.DICOM);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmHTTPProxy", conn.getHttpProxy(), null);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmBlacklistedHostname", conn.getBlacklist());
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmTCPBacklog",
                conn.getBacklog(), Connection.DEF_BACKLOG);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmTCPConnectTimeout",
                conn.getConnectTimeout(), Connection.NO_TIMEOUT);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmAARQTimeout",
                conn.getRequestTimeout(), Connection.NO_TIMEOUT);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmAAACTimeout",
                conn.getAcceptTimeout(), Connection.NO_TIMEOUT);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmARRPTimeout",
                conn.getReleaseTimeout(), Connection.NO_TIMEOUT);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmResponseTimeout",
                conn.getResponseTimeout(), Connection.NO_TIMEOUT);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmRetrieveTimeout",
                conn.getRetrieveTimeout(), Connection.NO_TIMEOUT);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmRetrieveTimeoutTotal",
                conn.isRetrieveTimeoutTotal(), false);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmIdleTimeout",
                conn.getIdleTimeout(), Connection.NO_TIMEOUT);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmTCPCloseDelay",
                conn.getSocketCloseDelay(), Connection.DEF_SOCKETDELAY);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmTCPSendBufferSize",
                conn.getSendBufferSize(), Connection.DEF_BUFFERSIZE);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmTCPReceiveBufferSize",
                conn.getReceiveBufferSize(), Connection.DEF_BUFFERSIZE);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmTCPNoDelay", conn.isTcpNoDelay(), true);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmBindAddress", conn.getBindAddress(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmClientBindAddress", conn.getClientBindAddress(), null);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmSendPDULength",
                conn.getSendPDULength(), Connection.DEF_MAX_PDU_LENGTH);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmReceivePDULength",
                conn.getReceivePDULength(), Connection.DEF_MAX_PDU_LENGTH);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmMaxOpsPerformed",
                conn.getMaxOpsPerformed(), Connection.SYNCHRONOUS_MODE);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmMaxOpsInvoked",
                conn.getMaxOpsInvoked(), Connection.SYNCHRONOUS_MODE);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmPackPDV", conn.isPackPDV(), true);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmTLSProtocol", conn.getTlsProtocols(), Connection.DEFAULT_TLS_PROTOCOLS);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmTLSNeedClientAuth", conn.isTlsNeedClientAuth(), true);
        return attrs;
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, ApplicationEntity ae, String deviceDN, Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dicomNetworkAE");
        attrs.put(objectclass);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomAETitle", ae.getAETitle(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomDescription", ae.getDescription(), null);
        storeNotEmpty(ldapObj, attrs, "dicomVendorData", ae.getVendorData());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dicomApplicationCluster", ae.getApplicationClusters());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dicomPreferredCallingAETitle", ae.getPreferredCallingAETitles());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dicomPreferredCalledAETitle", ae.getPreferredCalledAETitles());
        LdapUtils.storeBoolean(ldapObj, attrs, "dicomAssociationInitiator", ae.isAssociationInitiator());
        LdapUtils.storeBoolean(ldapObj, attrs, "dicomAssociationAcceptor",  ae.isAssociationAcceptor());
        LdapUtils.storeConnRefs(ldapObj, attrs, ae.getConnections(), deviceDN);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dicomSupportedCharacterSet", ae.getSupportedCharacterSets());
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomInstalled", ae.getInstalled(), null);
        if (!extended)
            return attrs;

        objectclass.add("dcmNetworkAE");
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmRoleSelectionNegotiationLenient",
                ae.getRoleSelectionNegotiationLenient(), null);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmPreferredTransferSyntax",
                LdapUtils.addOrdinalPrefix(ae.getPreferredTransferSyntaxes()));
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "hl7ApplicationName", ae.getHl7ApplicationName(), null);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmAcceptedCallingAETitle", ae.getAcceptedCallingAETitles());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmOtherAETitle", ae.getOtherAETitles());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmMasqueradeCallingAETitle", ae.getMasqueradeCallingAETitles());
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.storeTo(ldapObj, ae, attrs);
        return attrs;
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, WebApplication webapp, String deviceDN,
                               Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dcmWebApp");
        attrs.put(objectclass);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmWebAppName", webapp.getApplicationName(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomDescription", webapp.getDescription(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmWebServicePath", webapp.getServicePath(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmKeycloakClientID", webapp.getKeycloakClientID(), null);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmWebServiceClass", webapp.getServiceClasses());
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomAETitle", webapp.getAETitle(), null);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dicomApplicationCluster", webapp.getApplicationClusters());
        LdapUtils.storeConnRefs(ldapObj, attrs, webapp.getConnections(), deviceDN);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomInstalled", webapp.getInstalled(), null);
        return attrs;
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, KeycloakClient client, Attributes attrs) {
        attrs.put("objectclass", "dcmKeycloakClient");
        attrs.put("dcmKeycloakClientID", client.getKeycloakClientID());
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmURI", client.getKeycloakServerURL(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmKeycloakRealm", client.getKeycloakRealm(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmKeycloakGrantType",
                client.getKeycloakGrantType(), KeycloakClient.GrantType.client_credentials);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmKeycloakClientSecret",
                client.getKeycloakClientSecret(), null);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmTLSAllowAnyHostname",
                client.isTLSAllowAnyHostname(), false);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmTLSDisableTrustManager",
                client.isTLSDisableTrustManager(), false);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "uid", client.getUserID(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "userPassword", client.getPassword(), null);
        return attrs;
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, TransferCapability tc, Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dicomTransferCapability");
        attrs.put(objectclass);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "cn", tc.getCommonName(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomSOPClass", tc.getSopClass(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomTransferRole", tc.getRole(), null);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dicomTransferSyntax", tc.getTransferSyntaxes());
        if (!extended)
            return attrs;

        objectclass.add("dcmTransferCapability");
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmPreferredTransferSyntax",
                LdapUtils.addOrdinalPrefix(tc.getPreferredTransferSyntaxes()));
        EnumSet<QueryOption> queryOpts = tc.getQueryOptions();
        if (queryOpts != null) {
            LdapUtils.storeNotDef(ldapObj, attrs, "dcmRelationalQueries",
                    queryOpts.contains(QueryOption.RELATIONAL), false);
            LdapUtils.storeNotDef(ldapObj, attrs, "dcmCombinedDateTimeMatching",
                    queryOpts.contains(QueryOption.DATETIME), false);
            LdapUtils.storeNotDef(ldapObj, attrs, "dcmFuzzySemanticMatching",
                    queryOpts.contains(QueryOption.FUZZY), false);
            LdapUtils.storeNotDef(ldapObj, attrs, "dcmTimezoneQueryAdjustment",
                    queryOpts.contains(QueryOption.TIMEZONE), false);
        }
        StorageOptions storageOpts = tc.getStorageOptions();
        if (storageOpts != null) {
            LdapUtils.storeInt(ldapObj, attrs, "dcmStorageConformance",
                    storageOpts.getLevelOfSupport().ordinal());
            LdapUtils.storeInt(ldapObj, attrs, "dcmDigitalSignatureSupport",
                    storageOpts.getDigitalSignatureSupport().ordinal());
            LdapUtils.storeInt(ldapObj, attrs, "dcmDataElementCoercion",
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

    @Override
    public byte[][] loadDeviceVendorData(String deviceName) throws ConfigurationException {
        if (!configurationExists())
            throw new ConfigurationNotFoundException();

        try {
            Attributes attrs = getAttributes(deviceRef(deviceName), new String[]{ "dicomVendorData" });
            return byteArrays(attrs.get("dicomVendorData"));
        } catch (NameNotFoundException e) {
            throw new ConfigurationNotFoundException("Device with specified name not found", e);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public ConfigurationChanges updateDeviceVendorData(String deviceName, byte[]... vendorData)
            throws ConfigurationException {
        String deviceRef = deviceRef(deviceName);
        if (!configurationExists())
            throw new ConfigurationNotFoundException();

        ConfigurationChanges diffs = new ConfigurationChanges(false);
        try {
            Attributes attrs = getAttributes(deviceRef, new String[]{"dicomVendorData"});
            byte[][] prev = byteArrays(attrs.get("dicomVendorData"));
            ConfigurationChanges.ModifiedObject ldapObj =
                    ConfigurationChanges.addModifiedObject(diffs, deviceRef, ConfigurationChanges.ChangeType.C);
            List<ModificationItem> mods = new ArrayList<>(1);
            storeDiff(ldapObj, mods, "dicomVendorData", prev, vendorData);
            modifyAttributes(deviceRef, mods);
         } catch (NameNotFoundException e) {
            throw new ConfigurationNotFoundException("Device with specified name not found", e);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
        return diffs;
    }

    public Device loadDevice(String deviceDN) throws ConfigurationException {
        // get the device cache for this loading phase
        Map<String, Device> deviceCache = currentlyLoadedDevicesLocal.get();

        // if there is none, create one for the current thread and remember that it should be cleaned up when the device is loaded
        boolean doCleanUpCache = false;
        if (deviceCache == null) {
            doCleanUpCache = true;
            deviceCache = new HashMap<String, Device>();
            currentlyLoadedDevicesLocal.set(deviceCache);
        }

        // if a requested device is already being (was) loaded, do not load it again, just return existing Device object 
        if (deviceCache.containsKey(deviceDN))
            return deviceCache.get(deviceDN);
                
        
        try {
            Attributes attrs = getAttributes(deviceDN);
            Device device = new Device(LdapUtils.stringValue(attrs.get("dicomDeviceName"), null));

            // remember this device so it won't be loaded again in this run
            deviceCache.put(deviceDN, device);
                        
            loadFrom(device, attrs);
            loadChilds(device, deviceDN);
            return device;
        } catch (NameNotFoundException e) {
            throw new ConfigurationNotFoundException("Device with specified name not found",e);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } catch (CertificateException e) {
            throw new ConfigurationException(e);
        } finally {

            // if this loadDevice call initialized the cache, then clean it up
            if (doCleanUpCache) currentlyLoadedDevicesLocal.remove();
        }
        
    }

    public Attributes getAttributes(String name) throws NamingException {
        return ctx.getAttributes(name);
    }

    public Attributes getAttributes(String name, String[] attrIDs) throws NamingException {
        return ctx.getAttributes(name, attrIDs);
    }

    private void loadChilds(Device device, String deviceDN)
            throws NamingException, ConfigurationException {
        loadConnections(device, deviceDN);
        loadApplicationEntities(device, deviceDN);
        loadWebApplications(device, deviceDN);
        loadKeycloakClients(device, deviceDN);
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

        tc.setPreferredTransferSyntaxes(LdapUtils.removeOrdinalPrefix(
                LdapUtils.stringArray(attrs.get("dcmPreferredTransferSyntax"))));
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
        device.setDeviceUID(LdapUtils.stringValue(attrs.get("dicomDeviceUID"), null));
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
        device.setInstitutionCodes(LdapUtils.codeArray(attrs.get("dicomInstitutionCode")));
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
        
        device.setRoleSelectionNegotiationLenient(
                LdapUtils.booleanValue(attrs.get("dcmRoleSelectionNegotiationLenient"), false));
        device.setLimitOpenAssociations(
                LdapUtils.intValue(attrs.get("dcmLimitOpenAssociations"), 0));
        device.setLimitAssociationsInitiatedBy(
                LdapUtils.stringArray(attrs.get("dcmLimitAssociationsInitiatedBy")));
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
        device.setTimeZoneOfDevice(LdapUtils.timeZoneValue(attrs.get("dcmTimeZoneOfDevice"), null));
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
                loadFrom(conn, attrs, LdapUtils.hasObjectClass(attrs, "dcmNetworkConnection"));
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

    public NamingEnumeration<SearchResult> search(String dn, String filter,
            String... attrs) throws NamingException {
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        ctls.setReturningObjFlag(false);
        ctls.setReturningAttributes(attrs);
        return ctx.search(dn, filter, ctls);
    }

    private void loadFrom(Connection conn, Attributes attrs, boolean extended)
            throws NamingException {
        conn.setCommonName(LdapUtils.stringValue(attrs.get("cn"), null));
        conn.setHostname(LdapUtils.stringValue(attrs.get("dicomHostname"), null));
        conn.setPort(LdapUtils.intValue(attrs.get("dicomPort"), Connection.NOT_LISTENING));
        conn.setTlsCipherSuites(LdapUtils.stringArray(attrs.get("dicomTLSCipherSuite")));
        conn.setInstalled(LdapUtils.booleanValue(attrs.get("dicomInstalled"), null));
        if (!extended)
            return;

        conn.setProtocol(LdapUtils.enumValue(Protocol.class, attrs.get("dcmProtocol"), Protocol.DICOM));
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
        conn.setRetrieveTimeoutTotal(LdapUtils.booleanValue(attrs.get("dcmRetrieveTimeoutTotal"), false));
        conn.setIdleTimeout(LdapUtils.intValue(attrs.get("dcmIdleTimeout"),
                Connection.NO_TIMEOUT));
        conn.setSocketCloseDelay(LdapUtils.intValue(attrs.get("dcmTCPCloseDelay"),
                Connection.DEF_SOCKETDELAY));
        conn.setSendBufferSize(LdapUtils.intValue(attrs.get("dcmTCPSendBufferSize"),
                Connection.DEF_BUFFERSIZE));
        conn.setReceiveBufferSize(LdapUtils.intValue(attrs.get("dcmTCPReceiveBufferSize"),
                Connection.DEF_BUFFERSIZE));
        conn.setTcpNoDelay(LdapUtils.booleanValue(attrs.get("dcmTCPNoDelay"), true));
        conn.setBindAddress(LdapUtils.stringValue(attrs.get("dcmBindAddress"), null));
        conn.setClientBindAddress(LdapUtils.stringValue(attrs.get("dcmClientBindAddress"), null));
        conn.setTlsNeedClientAuth(LdapUtils.booleanValue(attrs.get("dcmTLSNeedClientAuth"), true));
        conn.setTlsProtocols(LdapUtils.stringArray(attrs.get("dcmTLSProtocol"), Connection.DEFAULT_TLS_PROTOCOLS));
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
            throws NamingException, ConfigurationException {
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
            String deviceDN, Device device) throws NamingException, ConfigurationException {
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

        ae.setRoleSelectionNegotiationLenient(
                LdapUtils.booleanValue(attrs.get("dcmRoleSelectionNegotiationLenient"), null));
        ae.setAcceptedCallingAETitles(LdapUtils.stringArray(attrs.get("dcmAcceptedCallingAETitle")));
        ae.setPreferredTransferSyntaxes(LdapUtils.removeOrdinalPrefix(
                LdapUtils.stringArray(attrs.get("dcmPreferredTransferSyntax"))));
        ae.setOtherAETitles(LdapUtils.stringArray(attrs.get("dcmOtherAETitle")));
        ae.setMasqueradeCallingAETitles(LdapUtils.stringArray(attrs.get("dcmMasqueradeCallingAETitle")));
        ae.setHl7ApplicationName(LdapUtils.stringValue(attrs.get("hl7ApplicationName"), null));
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.loadFrom(ae, attrs);
    }

    private void loadChilds(ApplicationEntity ae, String aeDN) throws NamingException, ConfigurationException {
        loadTransferCapabilities(ae, aeDN);
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.loadChilds(ae, aeDN);
    }

    private void loadWebApplications(Device device, String deviceDN)
            throws NamingException, ConfigurationException {
        NamingEnumeration<SearchResult> ne =
                search(deviceDN, "(objectclass=dcmWebApp)");
        try {
            while (ne.hasMore()) {
                device.addWebApplication(
                        loadWebApplication(ne.next(), deviceDN, device));
            }
        } finally {
            LdapUtils.safeClose(ne);
        }
    }

    private WebApplication loadWebApplication(SearchResult sr, String deviceDN, Device device)
            throws NamingException, ConfigurationException {
        Attributes attrs = sr.getAttributes();
        WebApplication webapp = new WebApplication(LdapUtils.stringValue(attrs.get("dcmWebAppName"), null));
        loadFrom(webapp, attrs);
        for (String connDN : LdapUtils.stringArray(attrs.get("dicomNetworkConnectionReference")))
            webapp.addConnection(LdapUtils.findConnection(connDN, deviceDN, device));
        return webapp ;
    }

    private void loadFrom(WebApplication webapp, Attributes attrs) throws NamingException {
        webapp.setDescription(LdapUtils.stringValue(attrs.get("dicomDescription"), null));
        webapp.setServicePath(LdapUtils.stringValue(attrs.get("dcmWebServicePath"), null));
        webapp.setKeycloakClientID(LdapUtils.stringValue(attrs.get("dcmKeycloakClientID"), null));
        webapp.setServiceClasses(LdapUtils.enumArray(WebApplication.ServiceClass.class, attrs.get("dcmWebServiceClass")));
        webapp.setAETitle(LdapUtils.stringValue(attrs.get("dicomAETitle"), null));
        webapp.setApplicationClusters(LdapUtils.stringArray(attrs.get("dicomApplicationCluster")));
        webapp.setInstalled(LdapUtils.booleanValue(attrs.get("dicomInstalled"), null));
    }

    private void loadKeycloakClients(Device device, String deviceDN) throws NamingException {
        NamingEnumeration<SearchResult> ne = search(deviceDN, "(objectclass=dcmKeycloakClient)");
        try {
            while (ne.hasMore()) {
                device.addKeycloakClient(loadKeycloakClient(ne.next()));
            }
        } finally {
            LdapUtils.safeClose(ne);
        }
    }

    private KeycloakClient loadKeycloakClient(SearchResult sr) throws NamingException {
        Attributes attrs = sr.getAttributes();
        KeycloakClient client = new KeycloakClient(LdapUtils.stringValue(attrs.get("dcmKeycloakClientID"), null));
        loadFrom(client, attrs);
        return client ;
    }

    private void loadFrom(KeycloakClient client, Attributes attrs) throws NamingException {
        client.setKeycloakServerURL(LdapUtils.stringValue(attrs.get("dcmURI"), null));
        client.setKeycloakRealm(LdapUtils.stringValue(attrs.get("dcmKeycloakRealm"), null));
        client.setKeycloakGrantType(LdapUtils.enumValue(KeycloakClient.GrantType.class, attrs.get("dcmKeycloakGrantType"),
                        KeycloakClient.GrantType.client_credentials));
        client.setKeycloakClientSecret(LdapUtils.stringValue(attrs.get("dcmKeycloakClientSecret"), null));
        client.setTLSAllowAnyHostname(LdapUtils.booleanValue(attrs.get("dcmTLSAllowAnyHostname"), false));
        client.setTLSDisableTrustManager(LdapUtils.booleanValue(attrs.get("dcmTLSDisableTrustManager"), false));
        client.setUserID(LdapUtils.stringValue(attrs.get("uid"), null));
        client.setPassword(LdapUtils.stringValue(attrs.get("userPassword"), null));
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

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, Device a, Device b,
                                              List<ModificationItem> mods, boolean preserveVendorData) {
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomDescription",
                a.getDescription(),
                b.getDescription(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomDeviceUID",
                a.getDeviceUID(),
                b.getDeviceUID(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomManufacturer",
                a.getManufacturer(),
                b.getManufacturer(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomManufacturerModelName",
                a.getManufacturerModelName(),
                b.getManufacturerModelName(), null);
        LdapUtils.storeDiff(ldapObj, mods, "dicomSoftwareVersion",
                a.getSoftwareVersions(),
                b.getSoftwareVersions());
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomStationName",
                a.getStationName(),
                b.getStationName(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomDeviceSerialNumber",
                a.getDeviceSerialNumber(),
                b.getDeviceSerialNumber(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomIssuerOfPatientID",
                a.getIssuerOfPatientID(),
                b.getIssuerOfPatientID(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomIssuerOfAccessionNumber",
                a.getIssuerOfAccessionNumber(),
                b.getIssuerOfAccessionNumber(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomOrderPlacerIdentifier",
                a.getOrderPlacerIdentifier(),
                b.getOrderPlacerIdentifier(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomOrderFillerIdentifier",
                a.getOrderFillerIdentifier(),
                b.getOrderFillerIdentifier(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomIssuerOfAdmissionID",
                a.getIssuerOfAdmissionID(),
                b.getIssuerOfAdmissionID(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomIssuerOfServiceEpisodeID",
                a.getIssuerOfServiceEpisodeID(),
                b.getIssuerOfServiceEpisodeID(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomIssuerOfContainerIdentifier",
                a.getIssuerOfContainerIdentifier(),
                b.getIssuerOfContainerIdentifier(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomIssuerOfSpecimenIdentifier",
                a.getIssuerOfSpecimenIdentifier(),
                b.getIssuerOfSpecimenIdentifier(), null);
        LdapUtils.storeDiff(ldapObj, mods, "dicomInstitutionName",
                a.getInstitutionNames(),
                b.getInstitutionNames());
        LdapUtils.storeDiff(ldapObj, mods, "dicomInstitutionCode",
                a.getInstitutionCodes(),
                b.getInstitutionCodes());
        LdapUtils.storeDiff(ldapObj, mods, "dicomInstitutionAddress",
                a.getInstitutionAddresses(),
                b.getInstitutionAddresses());
        LdapUtils.storeDiff(ldapObj, mods, "dicomInstitutionDepartmentName",
                a.getInstitutionalDepartmentNames(),
                b.getInstitutionalDepartmentNames());
        LdapUtils.storeDiff(ldapObj, mods, "dicomPrimaryDeviceType",
                a.getPrimaryDeviceTypes(),
                b.getPrimaryDeviceTypes());
        LdapUtils.storeDiff(ldapObj, mods, "dicomRelatedDeviceReference",
                a.getRelatedDeviceRefs(),
                b.getRelatedDeviceRefs());
        LdapUtils.storeDiff(ldapObj, mods, "dicomAuthorizedNodeCertificateReference",
                a.getAuthorizedNodeCertificateRefs(),
                b.getAuthorizedNodeCertificateRefs());
        LdapUtils.storeDiff(ldapObj, mods, "dicomThisNodeCertificateReference",
                a.getThisNodeCertificateRefs(),
                b.getThisNodeCertificateRefs());
        if (!preserveVendorData)
            storeDiff(ldapObj, mods, "dicomVendorData",
                    a.getVendorData(),
                    b.getVendorData());
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomInstalled",
                a.isInstalled(),
                b.isInstalled(), null);
        if (!extended)
            return mods;

        LdapUtils.storeDiffObject(ldapObj, mods, "dcmRoleSelectionNegotiationLenient",
                a.isRoleSelectionNegotiationLenient(),
                b.isRoleSelectionNegotiationLenient(), false);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmLimitOpenAssociations",
                a.getLimitOpenAssociations(),
                b.getLimitOpenAssociations(), null);
        LdapUtils.storeDiff(ldapObj, mods, "dcmLimitAssociationsInitiatedBy",
                a.getLimitAssociationsInitiatedBy(),
                b.getLimitAssociationsInitiatedBy());
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmTrustStoreURL",
                a.getTrustStoreURL(),
                b.getTrustStoreURL(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmTrustStoreType",
                a.getTrustStoreType(),
                b.getTrustStoreType(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmTrustStorePin",
                a.getTrustStorePin(),
                b.getTrustStorePin(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmTrustStorePinProperty",
                a.getTrustStorePinProperty(),
                b.getTrustStorePinProperty(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmKeyStoreURL",
                a.getKeyStoreURL(),
                b.getKeyStoreURL(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmKeyStoreType",
                a.getKeyStoreType(),
                b.getKeyStoreType(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmKeyStorePin",
                a.getKeyStorePin(),
                b.getKeyStorePin(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmKeyStorePinProperty",
                a.getKeyStorePinProperty(),
                b.getKeyStorePinProperty(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmKeyStoreKeyPin",
                a.getKeyStoreKeyPin(),
                b.getKeyStoreKeyPin(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmKeyStoreKeyPinProperty",
                a.getKeyStoreKeyPinProperty(),
                b.getKeyStoreKeyPinProperty(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmTimeZoneOfDevice",
                a.getTimeZoneOfDevice(),
                b.getTimeZoneOfDevice(), null);
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.storeDiffs(ldapObj, a, b, mods);
        return mods;
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, Connection a, Connection b,
                                              List<ModificationItem> mods) {
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomHostname",
                a.getHostname(),
                b.getHostname(), null);
        LdapUtils.storeDiff(ldapObj, mods, "dicomPort",
                a.getPort(),
                b.getPort(),
                Connection.NOT_LISTENING);
        LdapUtils.storeDiff(ldapObj, mods, "dicomTLSCipherSuite",
                a.getTlsCipherSuites(),
                b.getTlsCipherSuites());
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled(), null);
        if (!extended)
            return mods;

        LdapUtils.storeDiffObject(ldapObj, mods, "dcmProtocol", a.getProtocol(), b.getProtocol(), Protocol.DICOM);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmHTTPProxy",
                a.getHttpProxy(),
                b.getHttpProxy(), null);
        LdapUtils.storeDiff(ldapObj, mods, "dcmBlacklistedHostname",
                a.getBlacklist(),
                b.getBlacklist());
        LdapUtils.storeDiff(ldapObj, mods, "dcmTCPBacklog",
                a.getBacklog(),
                b.getBacklog(),
                Connection.DEF_BACKLOG);
        LdapUtils.storeDiff(ldapObj, mods, "dcmTCPConnectTimeout",
                a.getConnectTimeout(),
                b.getConnectTimeout(),
                Connection.NO_TIMEOUT);
        LdapUtils.storeDiff(ldapObj, mods, "dcmAARQTimeout",
                a.getRequestTimeout(),
                b.getRequestTimeout(),
                Connection.NO_TIMEOUT);
        LdapUtils.storeDiff(ldapObj, mods, "dcmAAACTimeout",
                a.getAcceptTimeout(),
                b.getAcceptTimeout(),
                Connection.NO_TIMEOUT);
        LdapUtils.storeDiff(ldapObj, mods, "dcmARRPTimeout",
                a.getReleaseTimeout(),
                b.getReleaseTimeout(),
                Connection.NO_TIMEOUT);
        LdapUtils.storeDiff(ldapObj, mods, "dcmResponseTimeout",
                a.getResponseTimeout(),
                b.getResponseTimeout(),
                Connection.NO_TIMEOUT);
        LdapUtils.storeDiff(ldapObj, mods, "dcmRetrieveTimeout",
                a.getRetrieveTimeout(),
                b.getRetrieveTimeout(),
                Connection.NO_TIMEOUT);
        LdapUtils.storeDiff(ldapObj, mods, "dcmRetrieveTimeoutTotal",
                a.isRetrieveTimeoutTotal(),
                b.isRetrieveTimeoutTotal(),
                false);
        LdapUtils.storeDiff(ldapObj, mods, "dcmIdleTimeout",
                a.getIdleTimeout(),
                b.getIdleTimeout(),
                Connection.NO_TIMEOUT);
        LdapUtils.storeDiff(ldapObj, mods, "dcmTCPCloseDelay",
                a.getSocketCloseDelay(),
                b.getSocketCloseDelay(),
                Connection.DEF_SOCKETDELAY);
        LdapUtils.storeDiff(ldapObj, mods, "dcmTCPSendBufferSize",
                a.getSendBufferSize(),
                b.getSendBufferSize(),
                Connection.DEF_BUFFERSIZE);
        LdapUtils.storeDiff(ldapObj, mods, "dcmTCPReceiveBufferSize",
                a.getReceiveBufferSize(),
                b.getReceiveBufferSize(),
                Connection.DEF_BUFFERSIZE);
        LdapUtils.storeDiff(ldapObj, mods, "dcmTCPNoDelay",
                a.isTcpNoDelay(),
                b.isTcpNoDelay(),
                true);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmBindAddress",
                a.getBindAddress(),
                b.getBindAddress(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmClientBindAddress",
                a.getClientBindAddress(),
                b.getClientBindAddress(), null);
        LdapUtils.storeDiff(ldapObj, mods, "dcmTLSProtocol",
                a.getTlsProtocols(),
                b.getTlsProtocols(),
                Connection.DEFAULT_TLS_PROTOCOLS);
        LdapUtils.storeDiff(ldapObj, mods, "dcmTLSNeedClientAuth",
                a.isTlsNeedClientAuth(),
                b.isTlsNeedClientAuth(),
                true);
        LdapUtils.storeDiff(ldapObj, mods, "dcmSendPDULength",
                a.getSendPDULength(),
                b.getSendPDULength(),
                Connection.DEF_MAX_PDU_LENGTH);
        LdapUtils.storeDiff(ldapObj, mods, "dcmReceivePDULength",
                a.getReceivePDULength(),
                b.getReceivePDULength(),
                Connection.DEF_MAX_PDU_LENGTH);
        LdapUtils.storeDiff(ldapObj, mods, "dcmMaxOpsPerformed",
                a.getMaxOpsPerformed(),
                b.getMaxOpsPerformed(),
                Connection.SYNCHRONOUS_MODE);
        LdapUtils.storeDiff(ldapObj, mods, "dcmMaxOpsInvoked",
                a.getMaxOpsInvoked(),
                b.getMaxOpsInvoked(),
                Connection.SYNCHRONOUS_MODE);
        LdapUtils.storeDiff(ldapObj, mods, "dcmPackPDV",
                a.isPackPDV(),
                b.isPackPDV(),
                true);
        return mods;
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, ApplicationEntity a,
                                              ApplicationEntity b, String deviceDN, List<ModificationItem> mods, boolean preserveVendorData) {
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomDescription",
                a.getDescription(),
                b.getDescription(), null);
        if (!preserveVendorData)
            storeDiff(ldapObj, mods, "dicomVendorData",
                    a.getVendorData(),
                    b.getVendorData());
        LdapUtils.storeDiff(ldapObj, mods, "dicomApplicationCluster",
                a.getApplicationClusters(),
                b.getApplicationClusters());
        LdapUtils.storeDiff(ldapObj, mods, "dicomPreferredCallingAETitle",
                a.getPreferredCallingAETitles(),
                b.getPreferredCallingAETitles());
        LdapUtils.storeDiff(ldapObj, mods, "dicomPreferredCalledAETitle",
                a.getPreferredCalledAETitles(),
                b.getPreferredCalledAETitles());
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomAssociationInitiator",
                a.isAssociationInitiator(),
                b.isAssociationInitiator(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomAssociationAcceptor",
                a.isAssociationAcceptor(),
                b.isAssociationAcceptor(), null);
        LdapUtils.storeDiff(ldapObj, mods, "dicomNetworkConnectionReference",
                a.getConnections(),
                b.getConnections(),
                deviceDN);
        LdapUtils.storeDiff(ldapObj, mods, "dicomSupportedCharacterSet",
                a.getSupportedCharacterSets(),
                b.getSupportedCharacterSets());
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled(), null);
        if (!extended)
            return mods;

        LdapUtils.storeDiffObject(ldapObj, mods, "dcmRoleSelectionNegotiationLenient",
                a.isRoleSelectionNegotiationLenient(),
                b.isRoleSelectionNegotiationLenient(), null);
        LdapUtils.storeDiffWithOrdinalPrefix(ldapObj, mods, "dcmPreferredTransferSyntax",
                a.getPreferredTransferSyntaxes(),
                b.getPreferredTransferSyntaxes());
        LdapUtils.storeDiff(ldapObj, mods, "dcmAcceptedCallingAETitle",
                a.getAcceptedCallingAETitles(),
                b.getAcceptedCallingAETitles());
        LdapUtils.storeDiff(ldapObj, mods, "dcmOtherAETitle",
                a.getOtherAETitles(),
                b.getOtherAETitles());
        LdapUtils.storeDiff(ldapObj, mods, "dcmMasqueradeCallingAETitle",
                a.getMasqueradeCallingAETitles(),
                b.getMasqueradeCallingAETitles());
        LdapUtils.storeDiffObject(ldapObj, mods, "hl7ApplicationName",
                a.getHl7ApplicationName(),
                b.getHl7ApplicationName(), null);
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.storeDiffs(ldapObj, a, b, mods);
        return mods;
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, TransferCapability a,
                                              TransferCapability b, List<ModificationItem> mods) {
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomSOPClass",
                a.getSopClass(),
                b.getSopClass(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomTransferRole",
                a.getRole(),
                b.getRole(), null);
        LdapUtils.storeDiff(ldapObj, mods, "dicomTransferSyntax",
                a.getTransferSyntaxes(),
                b.getTransferSyntaxes());
        if (!extended)
            return mods;

        LdapUtils.storeDiffWithOrdinalPrefix(ldapObj, mods, "dcmPreferredTransferSyntax",
                a.getPreferredTransferSyntaxes(),
                b.getPreferredTransferSyntaxes());
        storeDiffs(ldapObj, a.getQueryOptions(), b.getQueryOptions(), mods);
        storeDiffs(ldapObj, a.getStorageOptions(), b.getStorageOptions(), mods);
        return mods;
    }

    private void storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, EnumSet<QueryOption> prev,
                            EnumSet<QueryOption> val, List<ModificationItem> mods) {
        if (prev != null ? prev.equals(val) : val == null)
            return;

        LdapUtils.storeDiff(ldapObj, mods, "dcmRelationalQueries",
                prev != null && prev.contains(QueryOption.RELATIONAL),
                val != null && val.contains(QueryOption.RELATIONAL),
                false);
        LdapUtils.storeDiff(ldapObj, mods, "dcmCombinedDateTimeMatching",
                prev != null && prev.contains(QueryOption.DATETIME),
                val != null && val.contains(QueryOption.DATETIME),
                false);
        LdapUtils.storeDiff(ldapObj, mods, "dcmFuzzySemanticMatching",
                prev != null && prev.contains(QueryOption.FUZZY),
                val != null && val.contains(QueryOption.FUZZY),
                false);
        LdapUtils.storeDiff(ldapObj, mods, "dcmTimezoneQueryAdjustment",
                prev != null && prev.contains(QueryOption.TIMEZONE),
                val != null && val.contains(QueryOption.TIMEZONE),
                false);
    }

    private void storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, StorageOptions prev,
                            StorageOptions val, List<ModificationItem> mods) {
        if (prev != null ? prev.equals(val) : val == null)
            return;

        LdapUtils.storeDiff(ldapObj, mods, "dcmStorageConformance",
                prev != null ? prev.getLevelOfSupport().ordinal() : -1,
                val != null ? val.getLevelOfSupport().ordinal() : -1,
                -1);
        LdapUtils.storeDiff(ldapObj, mods, "dcmDigitalSignatureSupport",
                prev != null ? prev.getDigitalSignatureSupport().ordinal() : -1,
                val != null ? val.getDigitalSignatureSupport().ordinal() : -1,
                -1);
        LdapUtils.storeDiff(ldapObj, mods, "dcmDataElementCoercion",
                prev != null ? prev.getElementCoercion().ordinal() : -1,
                val != null ? val.getElementCoercion().ordinal() : -1,
                -1);
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, WebApplication a,
                                              WebApplication b, String deviceDN, List<ModificationItem> mods) {
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomDescription",
                a.getDescription(),
                b.getDescription(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmWebServicePath",
                a.getServicePath(),
                b.getServicePath(), null);
        LdapUtils.storeDiff(ldapObj, mods, "dcmWebServiceClass",
                a.getServiceClasses(),
                b.getServiceClasses());
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomAETitle",
                a.getAETitle(),
                b.getAETitle(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmKeycloakClientID",
                a.getKeycloakClientID(),
                b.getKeycloakClientID(), null);
        LdapUtils.storeDiff(ldapObj, mods, "dicomApplicationCluster",
                a.getApplicationClusters(),
                b.getApplicationClusters());
        LdapUtils.storeDiff(ldapObj, mods, "dicomNetworkConnectionReference",
                a.getConnections(),
                b.getConnections(),
                deviceDN);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled(), null);
        return mods;
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, KeycloakClient a,
            KeycloakClient b, List<ModificationItem> mods) {
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmURI",
                a.getKeycloakServerURL(), b.getKeycloakServerURL(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmKeycloakRealm",
                a.getKeycloakRealm(), b.getKeycloakRealm(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmKeycloakGrantType",
                a.getKeycloakGrantType(), b.getKeycloakGrantType(), KeycloakClient.GrantType.client_credentials);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmKeycloakClientSecret",
                a.getKeycloakClientSecret(), b.getKeycloakClientSecret(), null);
        LdapUtils.storeDiff(ldapObj, mods, "dcmTLSAllowAnyHostname",
                a.isTLSAllowAnyHostname(), b.isTLSAllowAnyHostname(), false);
        LdapUtils.storeDiff(ldapObj, mods, "dcmTLSDisableTrustManager",
                a.isTLSDisableTrustManager(), b.isTLSDisableTrustManager(), false);
        LdapUtils.storeDiffObject(ldapObj, mods, "uid", a.getUserID(), b.getUserID(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "userPassword", a.getPassword(), b.getPassword(), null);
        return mods;
    }

    private static byte[][] byteArrays(Attribute attr) throws NamingException {
        if (attr == null)
            return new byte[0][];

        byte[][] bb = new byte[attr.size()][];
        for (int i = 0; i < bb.length; i++)
            bb[i] = (byte[]) attr.get(i);

        return bb;
    }

    private static Issuer issuerValue(Attribute attr) throws NamingException {
        return attr != null ? new Issuer((String) attr.get()) : null;
    }

    private void mergeAEs(ConfigurationChanges diffs, Device prevDev, Device dev, String deviceDN, boolean preserveVendorData)
            throws NamingException {
        Collection<String> aets = dev.getApplicationAETitles();
        for (String aet : prevDev.getApplicationAETitles()) {
            if (!aets.contains(aet)) {
                String aetDN = aetDN(aet, deviceDN);
                destroySubcontextWithChilds(aetDN);
                ConfigurationChanges.addModifiedObject(diffs, aetDN, ConfigurationChanges.ChangeType.D);
            }
        }
        Collection<String> prevAETs = prevDev.getApplicationAETitles();
        for (ApplicationEntity ae : dev.getApplicationEntities()) {
            String aet = ae.getAETitle();
            if (!prevAETs.contains(aet)) {
                store(diffs, ae, deviceDN);
            }
            else
                merge(diffs, prevDev.getApplicationEntity(aet), ae, deviceDN, preserveVendorData);
        }
    }

    private void merge(ConfigurationChanges diffs, ApplicationEntity prev, ApplicationEntity ae,
                       String deviceDN, boolean preserveVendorData) throws NamingException {
        String aeDN = aetDN(ae.getAETitle(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, aeDN, ConfigurationChanges.ChangeType.U);
        modifyAttributes(aeDN, storeDiffs(ldapObj, prev, ae, deviceDN, new ArrayList<ModificationItem>(), preserveVendorData));
        ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
        mergeChilds(diffs, prev, ae, aeDN);
    }

    private void mergeChilds(ConfigurationChanges diffs, ApplicationEntity prev, ApplicationEntity ae,
                             String aeDN) throws NamingException {
        merge(diffs, prev.getTransferCapabilities(), ae.getTransferCapabilities(), aeDN);
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.mergeChilds(diffs, prev, ae, aeDN);
    }

    private void mergeWebApps(ConfigurationChanges diffs, Device prevDev, Device dev, String deviceDN)
            throws NamingException {
        Collection<String> names = dev.getWebApplicationNames();
        for (String webAppName : prevDev.getWebApplicationNames()) {
            if (!names.contains(webAppName)) {
                String webAppDN = webAppDN(webAppName, deviceDN);
                destroySubcontextWithChilds(webAppDN);
                ConfigurationChanges.addModifiedObject(diffs, webAppDN, ConfigurationChanges.ChangeType.D);
            }
        }
        Collection<String> prevNames = prevDev.getWebApplicationNames();
        for (WebApplication webapp : dev.getWebApplications()) {
            String name = webapp.getApplicationName();
            if (!prevNames.contains(name)) {
                store(diffs, webapp, deviceDN);
            }
            else
                merge(diffs, prevDev.getWebApplication(name), webapp, deviceDN);
        }
    }

    private void merge(ConfigurationChanges diffs, WebApplication prev, WebApplication webapp, String deviceDN)
            throws NamingException {
        String webappDN = webAppDN(webapp.getApplicationName(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, webappDN, ConfigurationChanges.ChangeType.U);
        modifyAttributes(webappDN, storeDiffs(ldapObj, prev, webapp, deviceDN, new ArrayList<ModificationItem>()));
        ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
    }

    private void mergeKeycloakClients(ConfigurationChanges diffs, Device prevDev, Device dev, String deviceDN)
            throws NamingException {
        Collection<String> clientIDs = dev.getKeycloakClientIDs();
        for (String clientID : prevDev.getKeycloakClientIDs()) {
            if (!clientIDs.contains(clientID)) {
                String keycloakClientDN = keycloakClientDN(clientID, deviceDN);
                destroySubcontextWithChilds(keycloakClientDN);
                ConfigurationChanges.addModifiedObject(diffs, keycloakClientDN, ConfigurationChanges.ChangeType.D);
            }
        }
        Collection<String> prevClientIDs = prevDev.getKeycloakClientIDs();
        for (KeycloakClient client : dev.getKeycloakClients()) {
            String clientID = client.getKeycloakClientID();
            if (!prevClientIDs.contains(clientID)) {
                store(diffs, client, deviceDN);
            }
            else
                merge(diffs, prevDev.getKeycloakClient(clientID), client, deviceDN);
        }
    }

    private void merge(ConfigurationChanges diffs, KeycloakClient prev, KeycloakClient client, String deviceDN)
            throws NamingException {
        String clientDN = keycloakClientDN(client.getKeycloakClientID(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, clientDN, ConfigurationChanges.ChangeType.U);
        modifyAttributes(clientDN, storeDiffs(ldapObj, prev, client, new ArrayList<ModificationItem>()));
        ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
    }

    public void modifyAttributes(String dn, List<ModificationItem> mods)
            throws NamingException {
        if (!mods.isEmpty())
            ctx.modifyAttributes(dn, mods.toArray(new ModificationItem[mods.size()]));
    }

    public void replaceAttributes(String dn, Attributes attrs)
            throws NamingException {
        ctx.modifyAttributes(dn, DirContext.REPLACE_ATTRIBUTE, attrs);
    }

    
    private void merge(ConfigurationChanges diffs, Collection<TransferCapability> prevs,
                       Collection<TransferCapability> tcs, String aeDN) throws NamingException {
        for (TransferCapability tc : prevs) {
            String dn = dnOf(tc, aeDN);
            if (findByDN(aeDN, tcs, dn) == null) {
                destroySubcontext(dn);
                ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
            }
        }
        for (TransferCapability tc : tcs) {
            String dn = dnOf(tc, aeDN);
            TransferCapability prev = findByDN(aeDN, prevs, dn);
            if (prev == null) {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.C);
                createSubcontext(dn,
                        storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                                tc, new BasicAttributes(true)));
            } else {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.U);
                modifyAttributes(dn, storeDiffs(ldapObj, prev, tc, new ArrayList<ModificationItem>()));
                ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
            }
        }
    }

    private void mergeConnections(ConfigurationChanges diffs, Device prevDev, Device device, String deviceDN)
            throws NamingException {
        List<Connection> prevs = prevDev.listConnections();
        List<Connection> conns = device.listConnections();
        for (Connection prev : prevs) {
            String dn = LdapUtils.dnOf(prev, deviceDN);
            if (LdapUtils.findByDN(deviceDN, conns, dn) == null) {
                destroySubcontext(dn);
                ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
            }
        }
        for (Connection conn : conns) {
            String dn = LdapUtils.dnOf(conn, deviceDN);
            Connection prev = LdapUtils.findByDN(deviceDN, prevs, dn);
            if (prev == null) {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.C);
                createSubcontext(dn,
                        storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                                conn, new BasicAttributes(true)));
            } else {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.U);
                modifyAttributes(dn, storeDiffs(ldapObj, prev, conn, new ArrayList<ModificationItem>()));
                ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
            }
        }
    }

    private static TransferCapability findByDN(String aeDN, 
            Collection<TransferCapability> tcs, String dn) {
        for (TransferCapability tc : tcs)
            if (dn.equals(dnOf(tc, aeDN)))
                return tc;
        return null;
    }

    private static void storeDiff(ConfigurationChanges.ModifiedObject ldapObj, List<ModificationItem> mods, String attrId,
                                  byte[][] prevs, byte[][] vals) {
        if (!equals(prevs, vals)) {
            mods.add((vals.length == 0)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                        new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                        attr(attrId, vals)));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrId);
                for (byte[] val : vals)
                    attribute.addValue(val.length + " bytes");
                for (byte[] prev : prevs)
                    attribute.removeValue(prev.length + " bytes");
                ldapObj.add(attribute);
            }
        }
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
        return LdapUtils.dnOf("dicomAETitle" , aet, parentDN);
    }

    private static String webAppDN(String webAppName, String parentDN) {
        return LdapUtils.dnOf("dcmWebAppName" , webAppName, parentDN);
    }

    private static String keycloakClientDN(String keycloakClientID, String parentDN) {
        return LdapUtils.dnOf("dcmKeycloakClientID" , keycloakClientID, parentDN);
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

    private static void storeNotEmpty(ConfigurationChanges.ModifiedObject ldapObj, Attributes attrs, String attrID, byte[]... vals) {
        if (vals != null && vals.length > 0) {
            attrs.put(attr(attrID, vals));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                for (byte[] val : vals)
                    attribute.addValue(val.length + " bytes");
                ldapObj.add(attribute);
            }
        }
    }

    private static Attribute attr(String attrID, byte[]... vals) {
        Attribute attr = new BasicAttribute(attrID);
        for (byte[] val : vals)
            attr.add(val);
        return attr;
    }

    public void store(ConfigurationChanges diffs, Map<String, BasicBulkDataDescriptor> descriptors, String parentDN)
            throws NamingException {
        for (BasicBulkDataDescriptor descriptor : descriptors.values()) {
            String dn = LdapUtils.dnOf("dcmBulkDataDescriptorID",
                    descriptor.getBulkDataDescriptorID(), parentDN);
            ConfigurationChanges.ModifiedObject ldapObj =
                    ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.C);
            createSubcontext(dn, storeTo(ldapObj, descriptor, new BasicAttributes(true)));
        }
    }

    private static Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj,
                                      BasicBulkDataDescriptor descriptor, BasicAttributes attrs) {
        attrs.put("objectclass", "dcmBulkDataDescriptor");
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmBulkDataDescriptorID",
                descriptor.getBulkDataDescriptorID(), null);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmBulkDataExcludeDefaults",
                descriptor.isExcludeDefaults(), false);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmAttributeSelector", descriptor.getAttributeSelectors());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmBulkDataVRLengthThreshold",
                descriptor.getLengthsThresholdsAsStrings());
        return attrs;
    }

    public void load(Map<String, BasicBulkDataDescriptor> descriptors, String parentDN) throws NamingException {
        NamingEnumeration<SearchResult> ne =
                search(parentDN, "(objectclass=dcmBulkDataDescriptor)");
        try {
            while (ne.hasMore()) {
                BasicBulkDataDescriptor descriptor = loadBulkDataDescriptor(ne.next());
                descriptors.put(descriptor.getBulkDataDescriptorID(), descriptor);
            }
        } finally {
            LdapUtils.safeClose(ne);
        }
    }

    private BasicBulkDataDescriptor loadBulkDataDescriptor(SearchResult sr) throws NamingException {
        Attributes attrs = sr.getAttributes();
        BasicBulkDataDescriptor descriptor = new BasicBulkDataDescriptor(
                LdapUtils.stringValue(attrs.get("dcmBulkDataDescriptorID"), null));
        descriptor.excludeDefaults(LdapUtils.booleanValue(attrs.get("dcmBulkDataExcludeDefaults"), false));
        descriptor.setAttributeSelectorsFromStrings(LdapUtils.stringArray(attrs.get("dcmAttributeSelector")));
        descriptor.setLengthsThresholdsFromStrings(LdapUtils.stringArray(attrs.get("dcmBulkDataVRLengthThreshold")));
        return descriptor ;
    }

    public void merge(ConfigurationChanges diffs,
                      Map<String,BasicBulkDataDescriptor> prevs,
                      Map<String,BasicBulkDataDescriptor> descriptors,
                      String parentDN)
            throws NamingException {
        for (String prevBulkDataDescriptorID : prevs.keySet()) {
            if (!descriptors.containsKey(prevBulkDataDescriptorID)) {
                String dn = LdapUtils.dnOf("dcmBulkDataDescriptorID", prevBulkDataDescriptorID, parentDN);
                destroySubcontext(dn);
                ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
            }
        }
        for (Map.Entry<String, BasicBulkDataDescriptor> entry : descriptors.entrySet()) {
            String dn = LdapUtils.dnOf("dcmBulkDataDescriptorID", entry.getKey(), parentDN);
            BasicBulkDataDescriptor prev = prevs.get(entry.getKey());
            if (prev == null) {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.C);
                createSubcontext(dn, storeTo(ldapObj, prev, new BasicAttributes(true)));
            } else {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.U);
                modifyAttributes(dn, storeDiffs(ldapObj, prev, entry.getValue(), new ArrayList<ModificationItem>()));
                ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
            }
        }
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj,
                                              BasicBulkDataDescriptor prev,
                                              BasicBulkDataDescriptor descriptor,
                                              ArrayList<ModificationItem> mods) {
        LdapUtils.storeDiff(ldapObj, mods, "dcmBulkDataExcludeDefaults",
                prev.isExcludeDefaults(), descriptor.isExcludeDefaults(), false);
        LdapUtils.storeDiff(ldapObj, mods, "dcmAttributeSelector",
                prev.getAttributeSelectors(), descriptor.getAttributeSelectors());
        LdapUtils.storeDiff(ldapObj, mods, "dcmBulkDataVRLengthThreshold",
                prev.getLengthsThresholdsAsStrings(), descriptor.getLengthsThresholdsAsStrings());
        return mods;
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
        LdapUtils.storeNotNullOrDef(attrs, "dcmDIMSE", ac.getDIMSE(), null);
        LdapUtils.storeNotNullOrDef(attrs, "dicomTransferRole", ac.getRole(), null);
        LdapUtils.storeNotEmpty(attrs, "dcmAETitle", ac.getAETitles());
        LdapUtils.storeNotEmpty(attrs, "dcmSOPClass", ac.getSOPClasses());
        LdapUtils.storeNotNullOrDef(attrs, "dcmURI", ac.getURI(), null);
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
                        LdapUtils.stringValue(attrs.get("dcmURI"), null)));
            }
        } finally {
           LdapUtils.safeClose(ne);
        }
    }

    public void merge(ConfigurationChanges diffs, AttributeCoercions prevs, AttributeCoercions acs,
                      String parentDN) throws NamingException {
        for (AttributeCoercion prev : prevs) {
            String cn = prev.getCommonName();
            if (acs.findByCommonName(cn) == null) {
                String dn = LdapUtils.dnOf("cn", cn, parentDN);
                destroySubcontext(dn);
                ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
            }
        }
        for (AttributeCoercion ac : acs) {
            String cn = ac.getCommonName();
            String dn = LdapUtils.dnOf("cn", cn, parentDN);
            AttributeCoercion prev = prevs.findByCommonName(cn);
            if (prev == null) {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.C);
                createSubcontext(dn, storeTo(ac, new BasicAttributes(true)));
            } else {
                ConfigurationChanges.ModifiedObject ldapObj =
                    ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.U);
                modifyAttributes(dn, storeDiffs(ldapObj, prev, ac, new ArrayList<ModificationItem>()));
                ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
            }
        }
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, AttributeCoercion prev,
                                              AttributeCoercion ac, ArrayList<ModificationItem> mods) {
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmDIMSE", prev.getDIMSE(), ac.getDIMSE(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomTransferRole",
                prev.getRole(),
                ac.getRole(), null);
        LdapUtils.storeDiff(ldapObj, mods, "dcmAETitle",
                prev.getAETitles(),
                ac.getAETitles());
        LdapUtils.storeDiff(ldapObj, mods, "dcmSOPClass",
                prev.getSOPClasses(),
                ac.getSOPClasses());
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmURI", prev.getURI(), ac.getURI(), null);
        return mods;
    }

    @Override
    public void sync() throws ConfigurationException {
        // NOOP
    }

    @Override
    public synchronized ApplicationEntityInfo[] listAETInfos(ApplicationEntityInfo keys)
            throws ConfigurationException {
        if (!configurationExists())
            return new ApplicationEntityInfo[0];

        ArrayList<ApplicationEntityInfo> results = new ArrayList<ApplicationEntityInfo>();
        NamingEnumeration<SearchResult> ne = null;
        try {
            String deviceName = keys.getDeviceName();
            ne = search(deviceName, AE_ATTRS, toFilter(keys));
            Map<String, Connection> connCache = new HashMap<>();
            while (ne.hasMore()) {
                ApplicationEntityInfo aetInfo = new ApplicationEntityInfo();
                SearchResult ne1 = ne.next();
                loadFrom(aetInfo, ne1.getAttributes(),
                        deviceName != null ? deviceName : LdapUtils.cutDeviceName(ne1.getName()), connCache);
                results.add(aetInfo);
            }
        } catch (NameNotFoundException e) {
            return new ApplicationEntityInfo[0];
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } finally {
            LdapUtils.safeClose(ne);
        }
        return results.toArray(new ApplicationEntityInfo[results.size()]);
    }

    @Override
    public synchronized WebApplicationInfo[] listWebApplicationInfos(WebApplicationInfo keys)
            throws ConfigurationException {
        if (!configurationExists())
            return new WebApplicationInfo[0];

        ArrayList<WebApplicationInfo> results = new ArrayList<>();
        NamingEnumeration<SearchResult> ne = null;
        try {
            String deviceName = keys.getDeviceName();
            ne = search(deviceName, WEBAPP_ATTRS, toFilter(keys));
            Map<String, Connection> connCache = new HashMap<>();
            Map<String, KeycloakClient> keycloakClientCache = new HashMap<>();
            while (ne.hasMore()) {
                WebApplicationInfo webappInfo = new WebApplicationInfo();
                SearchResult ne1 = ne.next();
                loadFrom(webappInfo, ne1.getAttributes(),
                        deviceName != null ? deviceName : LdapUtils.cutDeviceName(ne1.getName()),
                        connCache, keycloakClientCache);
                results.add(webappInfo);
            }
        } catch (NameNotFoundException e) {
            return new WebApplicationInfo[0];
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } finally {
            LdapUtils.safeClose(ne);
        }
        return results.toArray(new WebApplicationInfo[results.size()]);
    }

    public NamingEnumeration<SearchResult> search(String deviceName, String[] attrsArray, String filter)
            throws NamingException {
        return deviceName != null
                ? search(deviceRef(deviceName), filter, attrsArray)
                : ctx.search(devicesDN, filter, searchControlSubtreeScope(0, attrsArray, true));
    }

    private static String toFilter(ApplicationEntityInfo keys) {
        if (keys == null)
            return "(objectclass=dicomNetworkAE)";

        StringBuilder sb = new StringBuilder();
        sb.append("(&(objectclass=dicomNetworkAE)");
        if (keys.getAETitle() != null) {
            sb.append("(|");
            appendFilter("dicomAETitle", keys.getAETitle(), sb);
            appendFilter("dcmOtherAETitle", keys.getAETitle(), sb);
            sb.append(")");
        }
        appendFilter("dicomDescription", keys.getDescription(), sb);
        appendFilter("dicomAssociationInitiator", keys.getAssociationInitiator(), sb);
        appendFilter("dicomAssociationAcceptor", keys.getAssociationAcceptor(), sb);
        appendFilter("dicomApplicationCluster", keys.getApplicationClusters(), sb);
        sb.append(")");
        return sb.toString();
    }

    private static String toFilter(WebApplicationInfo keys) {
        if (keys == null)
            return "(objectclass=dcmWebApp)";

        StringBuilder sb = new StringBuilder();
        sb.append("(&(objectclass=dcmWebApp)");
        appendFilter("dcmWebAppName", keys.getApplicationName(), sb);
        appendFilter("dicomDescription", keys.getDescription(), sb);
        appendFilter("dcmWebServicePath", keys.getServicePath(), sb);
        appendFilter("dcmWebServiceClass", keys.getServiceClasses(), sb);
        appendFilter("dicomAETitle", keys.getAETitle(), sb);
        appendFilter("dicomApplicationCluster", keys.getApplicationClusters(), sb);
        appendFilter("dcmKeycloakClientID", keys.getKeycloakClientID(), sb);
        sb.append(")");
        return sb.toString();
    }
}
