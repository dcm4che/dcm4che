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

package org.dcm4che3.net;

import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.dcm4che3.data.Code;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.util.StringUtils;

/**
 * DICOM Part 15, Annex H compliant description of a DICOM enabled system or
 * device. This is used to describe a DICOM-enabled network endpoint in terms of
 * its physical attributes (serial number, manufacturer, etc.), its context
 * (issuer of patient ids used by the device, etc.), as well as its capabilities
 * (TLS-enabled, AE titles used, etc.).
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Device implements Serializable {

    private static final long serialVersionUID = -5816872456184522866L;

    private String deviceName;
    private String deviceUID;
    private String description;
    private String manufacturer;
    private String manufacturerModelName;
    private String stationName;
    private String deviceSerialNumber;
    private String trustStoreURL;
    private String trustStoreType;
    private String trustStorePin;
    private String trustStorePinProperty;
    private String keyStoreURL;
    private String keyStoreType;
    private String keyStorePin;
    private String keyStorePinProperty;
    private String keyStoreKeyPin;
    private String keyStoreKeyPinProperty;
    private Issuer issuerOfPatientID;
    private Issuer issuerOfAccessionNumber;
    private Issuer orderPlacerIdentifier;
    private Issuer orderFillerIdentifier;
    private Issuer issuerOfAdmissionID;
    private Issuer issuerOfServiceEpisodeID;
    private Issuer issuerOfContainerIdentifier;
    private Issuer issuerOfSpecimenIdentifier;
    private String[] softwareVersions = {};
    private String[] primaryDeviceTypes = {};
    private String[] institutionNames = {};
    private Code[] institutionCodes = {};
    private String[] institutionAddresses = {};
    private String[] institutionalDepartmentNames = {};
    private String[] relatedDeviceRefs = {};
    private byte[][] vendorData = {};
    private int limitOpenAssociations;
    private boolean installed = true;
    private boolean roleSelectionNegotiationLenient;
    private TimeZone timeZoneOfDevice;

    private final LinkedHashMap<String, Integer> limitAssociationsInitiatedBy = new LinkedHashMap<>();
    private final LinkedHashMap<String, X509Certificate[]> authorizedNodeCertificates =
            new LinkedHashMap<String, X509Certificate[]>();
    private final LinkedHashMap<String, X509Certificate[]> thisNodeCertificates = 
            new LinkedHashMap<String, X509Certificate[]>();
    private final List<Connection> conns = new ArrayList<Connection>();
    private final LinkedHashMap<String, ApplicationEntity> aes = 
            new LinkedHashMap<String, ApplicationEntity>();
    private final LinkedHashMap<String, WebApplication> webapps = new LinkedHashMap<>();
    private final LinkedHashMap<String, KeycloakClient> keycloakClients = new LinkedHashMap<>();
    private final Map<Class<? extends DeviceExtension>,DeviceExtension> extensions =
            new HashMap<Class<? extends DeviceExtension>,DeviceExtension>();

    private transient AssociationHandler associationHandler = new AssociationHandler();
    private transient DimseRQHandler dimseRQHandler;
    private transient ConnectionMonitor connectionMonitor;
    private transient AssociationMonitor associationMonitor;

    private transient final List<Association> associations = new ArrayList<>();

    private transient Executor executor;
    private transient ScheduledExecutorService scheduledExecutor;
    private transient volatile SSLContext sslContext;
    private transient volatile KeyManager km;
    private transient volatile TrustManager tm;

    public Device() {
    }

    public Device(String name) {
        setDeviceName(name);
    }

    private void checkNotEmpty(String name, String val) {
        if (val != null && val.isEmpty())
            throw new IllegalArgumentException(name + " cannot be empty");
    }

    /**
     * Get the name of this device.
     * 
     * @return A String containing the device name.
     */
    public final String getDeviceName() {
        return deviceName;
    }

    /**
     * Set the name of this device.
     * 
     * @param name A String containing the device name.
     */
    public final void setDeviceName(String name) {
        checkNotEmpty("Device Name", name);
        this.deviceName = name;
    }

    /**
     * Get the description of this device.
     * 
     * @return A String containing the device description.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Set the description of this device.
     * 
     * @param description
     *                A String containing the device description.
     */
    public final void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceUID() {
        return deviceUID;
    }

    public void setDeviceUID(String deviceUID) {
        this.deviceUID = deviceUID;
    }

    /**
     * Get the manufacturer of this device.
     * 
     * @return A String containing the device manufacturer.
     */
    public final String getManufacturer() {
        return manufacturer;
    }

    /**
     * Set the manufacturer of this device.
     * <p>
     * This should be the same as the value of Manufacturer (0008,0070) in SOP
     * instances created by this device.
     * 
     * @param manufacturer
     *                A String containing the device manufacturer.
     */
    public final void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * Get the manufacturer model name of this device.
     * 
     * @return A String containing the device manufacturer model name.
     */
    public final String getManufacturerModelName() {
        return manufacturerModelName;
    }

    /**
     * Set the manufacturer model name of this device.
     * <p>
     * This should be the same as the value of Manufacturer Model Name
     * (0008,1090) in SOP instances created by this device.
     * 
     * @param manufacturerModelName
     *                A String containing the device manufacturer model name.
     */
    public final void setManufacturerModelName(String manufacturerModelName) {
        this.manufacturerModelName = manufacturerModelName;
    }

    /**
     * Get the software versions running on (or implemented by) this device.
     * 
     * @return A String array containing the software versions.
     */
    public final String[] getSoftwareVersions() {
        return softwareVersions;
    }

    /**
     * Set the software versions running on (or implemented by) this device.
     * <p>
     * This should be the same as the values of Software Versions (0018,1020) in
     * SOP instances created by this device.
     * 
     * @param softwareVersions
     *                A String array containing the software versions.
     */
    public final void setSoftwareVersions(String... softwareVersions) {
        this.softwareVersions = softwareVersions;
    }

    /**
     * Get the station name belonging to this device.
     * 
     * @return A String containing the station name.
     */
    public final String getStationName() {
        return stationName;
    }

    /**
     * Set the station name belonging to this device.
     * <p>
     * This should be the same as the value of Station Name (0008,1010) in SOP
     * instances created by this device.
     * 
     * @param stationName
     *                A String containing the station name.
     */
    public final void setStationName(String stationName) {
        this.stationName = stationName;
    }

    /**
     * Get the serial number belonging to this device.
     * 
     * @return A String containing the serial number.
     */
    public final String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    /**
     * Set the serial number of this device.
     * <p>
     * This should be the same as the value of Device Serial Number (0018,1000)
     * in SOP instances created by this device.
     * 
     * @param deviceSerialNumber
     *                A String containing the serial number.
     */
    public final void setDeviceSerialNumber(String deviceSerialNumber) {
        this.deviceSerialNumber = deviceSerialNumber;
    }

    /**
     * Get the type codes associated with this device.
     * 
     * @return A String array containing the type codes of this device.
     */
    public final String[] getPrimaryDeviceTypes() {
        return primaryDeviceTypes;
    }

    /**
     * Set the type codes associated with this device.
     * <p>
     * Represents the kind of device and is most applicable for acquisition
     * modalities. Types should be selected from the list of code values
     * (0008,0100) for Context ID 30 in PS3.16 when applicable.
     * 
     * @param primaryDeviceTypes
     */
    public void setPrimaryDeviceTypes(String... primaryDeviceTypes) {
        this.primaryDeviceTypes = primaryDeviceTypes;
    }

    /**
     * Get the institution name associated with this device; may be the site
     * where it resides or is operating on behalf of.
     * 
     * @return A String array containing the institution name values.
     */
    public final String[] getInstitutionNames() {
        return institutionNames;
    }

    /**
     * Set the institution name associated with this device; may be the site
     * where it resides or is operating on behalf of.
     * <p>
     * Should be the same as the value of Institution Name (0008,0080) in SOP
     * Instances created by this device.
     * 
     * @param names
     *                A String array containing the institution name values.
     */
    public void setInstitutionNames(String... names) {
        institutionNames = names;
    }

    public final Code[] getInstitutionCodes() {
        return institutionCodes;
    }

    public void setInstitutionCodes(Code... codes) {
        institutionCodes = codes;
    }

    /**
     * Set the address of the institution which operates this device.
     * 
     * @return A String array containing the institution address values.
     */
    public final String[] getInstitutionAddresses() {
        return institutionAddresses;
    }

    /**
     * Get the address of the institution which operates this device.
     * <p>
     * Should be the same as the value of Institution Address (0008,0081)
     * attribute in SOP Instances created by this device.
     * 
     * @param addresses
     *                A String array containing the institution address values.
     */
    public void setInstitutionAddresses(String... addresses) {
        institutionAddresses = addresses;
    }

    /**
     * Get the department name associated with this device.
     * 
     * @return A String array containing the dept. name values.
     */
    public final String[] getInstitutionalDepartmentNames() {
        return institutionalDepartmentNames;
    }

    /**
     * Set the department name associated with this device.
     * <p>
     * Should be the same as the value of Institutional Department Name
     * (0008,1040) in SOP Instances created by this device.
     * 
     * @param names
     *                A String array containing the dept. name values.
     */
    public void setInstitutionalDepartmentNames(String... names) {
        institutionalDepartmentNames = names;
    }

    public final Issuer getIssuerOfPatientID() {
        return issuerOfPatientID;
    }

    public final void setIssuerOfPatientID(Issuer issuerOfPatientID) {
        this.issuerOfPatientID = issuerOfPatientID;
    }

    public final Issuer getIssuerOfAccessionNumber() {
        return issuerOfAccessionNumber;
    }

    public final void setIssuerOfAccessionNumber(Issuer issuerOfAccessionNumber) {
        this.issuerOfAccessionNumber = issuerOfAccessionNumber;
    }

    public final Issuer getOrderPlacerIdentifier() {
        return orderPlacerIdentifier;
    }

    public final void setOrderPlacerIdentifier(Issuer orderPlacerIdentifier) {
        this.orderPlacerIdentifier = orderPlacerIdentifier;
    }

    public final Issuer getOrderFillerIdentifier() {
        return orderFillerIdentifier;
    }

    public final void setOrderFillerIdentifier(Issuer orderFillerIdentifier) {
        this.orderFillerIdentifier = orderFillerIdentifier;
    }

    public final Issuer getIssuerOfAdmissionID() {
        return issuerOfAdmissionID;
    }

    public final void setIssuerOfAdmissionID(Issuer issuerOfAdmissionID) {
        this.issuerOfAdmissionID = issuerOfAdmissionID;
    }

    public final Issuer getIssuerOfServiceEpisodeID() {
        return issuerOfServiceEpisodeID;
    }

    public final void setIssuerOfServiceEpisodeID(Issuer issuerOfServiceEpisodeID) {
        this.issuerOfServiceEpisodeID = issuerOfServiceEpisodeID;
    }

    public final Issuer getIssuerOfContainerIdentifier() {
        return issuerOfContainerIdentifier;
    }

    public final void setIssuerOfContainerIdentifier(Issuer issuerOfContainerIdentifier) {
        this.issuerOfContainerIdentifier = issuerOfContainerIdentifier;
    }

    public final Issuer getIssuerOfSpecimenIdentifier() {
        return issuerOfSpecimenIdentifier;
    }

    public final void setIssuerOfSpecimenIdentifier(Issuer issuerOfSpecimenIdentifier) {
        this.issuerOfSpecimenIdentifier = issuerOfSpecimenIdentifier;
    }

    public X509Certificate[] getAuthorizedNodeCertificates(String ref) {
        return authorizedNodeCertificates.get(ref);
    }

    public void setAuthorizedNodeCertificates(String ref, X509Certificate... certs) {
        authorizedNodeCertificates.put(ref, certs);
        setTrustManager(null);
    }

    public X509Certificate[] removeAuthorizedNodeCertificates(String ref) {
        X509Certificate[] certs = authorizedNodeCertificates.remove(ref);
        setTrustManager(null);
        return certs;
    }

    public void removeAllAuthorizedNodeCertificates() {
        authorizedNodeCertificates.clear();
        setTrustManager(null);
    }

    public X509Certificate[] getAllAuthorizedNodeCertificates() {
        return toArray(authorizedNodeCertificates.values());
    }

    public String[] getAuthorizedNodeCertificateRefs() {
        return authorizedNodeCertificates.keySet().toArray(StringUtils.EMPTY_STRING);
    }

    public final String getTrustStoreURL() {
        return trustStoreURL;
    }

    public final void setTrustStoreURL(String trustStoreURL) {
        checkNotEmpty("trustStoreURL", trustStoreURL);
        if (trustStoreURL == null
                ? this.trustStoreURL == null
                : trustStoreURL.equals(this.trustStoreURL))
            return;

        this.trustStoreURL = trustStoreURL;
        setTrustManager(null);
    }

    public final String getTrustStoreType() {
        return trustStoreType;
    }

    public final void setTrustStoreType(String trustStoreType) {
        checkNotEmpty("trustStoreType", trustStoreType);
        this.trustStoreType = trustStoreType;
    }

    public final String getTrustStorePin() {
        return trustStorePin;
    }

    public final void setTrustStorePin(String trustStorePin) {
        checkNotEmpty("trustStorePin", trustStorePin);
        this.trustStorePin = trustStorePin;
    }

    public final String getTrustStorePinProperty() {
        return trustStorePinProperty;
    }

    public final void setTrustStorePinProperty(String trustStorePinProperty) {
        checkNotEmpty("trustStorePinProperty", trustStorePinProperty);
        this.trustStorePinProperty = trustStorePinProperty;
    }

    public X509Certificate[] getThisNodeCertificates(String ref) {
        return thisNodeCertificates.get(ref);
    }

    public void setThisNodeCertificates(String ref, X509Certificate... certs) {
        thisNodeCertificates.put(ref, certs);
    }

    public X509Certificate[] removeThisNodeCertificates(String ref) {
        return thisNodeCertificates.remove(ref);
    }

    public final String getKeyStoreURL() {
        return keyStoreURL;
    }

    public final void setKeyStoreURL(String keyStoreURL) {
        checkNotEmpty("keyStoreURL", keyStoreURL);
        if (keyStoreURL == null
                ? this.keyStoreURL == null
                : keyStoreURL.equals(this.keyStoreURL))
            return;

        this.keyStoreURL = keyStoreURL;
        setKeyManager(null);
    }

    public final String getKeyStoreType() {
        return keyStoreType;
    }

    public final void setKeyStoreType(String keyStoreType) {
        checkNotEmpty("keyStoreType", keyStoreType);
        this.keyStoreType = keyStoreType;
    }

    public final String getKeyStorePin() {
        return keyStorePin;
    }

    public final void setKeyStorePin(String keyStorePin) {
        checkNotEmpty("keyStorePin", keyStorePin);
        this.keyStorePin = keyStorePin;
    }

    public final String getKeyStorePinProperty() {
        return keyStorePinProperty;
    }

    public final void setKeyStorePinProperty(String keyStorePinProperty) {
        checkNotEmpty("keyStorePinProperty", keyStorePinProperty);
        this.keyStorePinProperty = keyStorePinProperty;
    }

    public final String getKeyStoreKeyPin() {
        return keyStoreKeyPin;
    }

    public final void setKeyStoreKeyPin(String keyStorePin) {
        checkNotEmpty("keyStoreKeyPin", keyStorePin);
        this.keyStoreKeyPin = keyStorePin;
    }

    public final String getKeyStoreKeyPinProperty() {
        return keyStoreKeyPinProperty;
    }

    public final void setKeyStoreKeyPinProperty(String keyStoreKeyPinProperty) {
        checkNotEmpty("keyStoreKeyPinProperty", keyStoreKeyPinProperty);
        this.keyStoreKeyPinProperty = keyStoreKeyPinProperty;
    }

    public void removeAllThisNodeCertificates() {
        thisNodeCertificates.clear();
    }

    public X509Certificate[] getAllThisNodeCertificates() {
        return toArray(thisNodeCertificates.values());
    }

    public String[] getThisNodeCertificateRefs() {
        return thisNodeCertificates.keySet().toArray(StringUtils.EMPTY_STRING);
    }

    private static X509Certificate[] toArray(Collection<X509Certificate[]> c) {
        int size = 0;
        for (X509Certificate[] certs : c)
            size += certs.length;

        X509Certificate[] dest = new X509Certificate[size];
        int destPos = 0;
        for (X509Certificate[] certs : c) {
            System.arraycopy(certs, 0, dest, destPos, certs.length);
            destPos += certs.length;
        }
        return dest ;
    }


    public final String[] getRelatedDeviceRefs() {
        return relatedDeviceRefs;
    }

    public void setRelatedDeviceRefs(String... refs) {
        relatedDeviceRefs = refs;
    }

    /**
     * Get device specific vendor configuration information
     * 
     * @return An Object of the device data.
     */
    public final byte[][] getVendorData() {
        return vendorData;
    }

    /**
     * Set device specific vendor configuration information
     * 
     * @param vendorData
     *                An Object of the device data.
     */
    public void setVendorData(byte[]... vendorData) {
        this.vendorData = vendorData;
    }

    /**
     * Get a boolean to indicate whether this device is presently installed on
     * the network. (This is useful for pre-configuration, mobile vans, and
     * similar situations.)
     * 
     * @return A boolean which will be true if this device is installed.
     */
    public final boolean isInstalled() {
        return installed;
    }

    /**
     * Get a boolean to indicate whether this device is presently installed on
     * the network. (This is useful for pre-configuration, mobile vans, and
     * similar situations.)
     * 
     * @param installed
     *                A boolean which will be true if this device is installed.
     * @throws IOException 
     * @throws GeneralSecurityException 
     * @throws KeyManagementException 
     */
    public final void setInstalled(boolean installed) {
        if (this.installed == installed)
            return;

        this.installed = installed;
        needRebindConnections();
    }

    public boolean isRoleSelectionNegotiationLenient() {
        return roleSelectionNegotiationLenient;
    }

    public void setRoleSelectionNegotiationLenient(boolean roleSelectionNegotiationLenient) {
        this.roleSelectionNegotiationLenient = roleSelectionNegotiationLenient;
    }

    public void setTimeZoneOfDevice(TimeZone timeZoneOfDevice) {
        this.timeZoneOfDevice = timeZoneOfDevice;
    }

    public TimeZone getTimeZoneOfDevice()
    {
	return timeZoneOfDevice;
    }
   
    public final void setDimseRQHandler(DimseRQHandler dimseRQHandler) {
        this.dimseRQHandler = dimseRQHandler;
    }

    public final DimseRQHandler getDimseRQHandler() {
        return dimseRQHandler;
    }

    public final AssociationHandler getAssociationHandler() {
        return associationHandler;
    }

    public void setAssociationHandler(AssociationHandler associationHandler) {
        if (associationHandler == null)
            throw new NullPointerException();
        this.associationHandler = associationHandler;
    }

    public ConnectionMonitor getConnectionMonitor() {
        return connectionMonitor;
    }

    public void setConnectionMonitor(ConnectionMonitor connectionMonitor) {
        this.connectionMonitor = connectionMonitor;
    }

    public AssociationMonitor getAssociationMonitor() {
        return associationMonitor;
    }

    public void setAssociationMonitor(AssociationMonitor associationMonitor) {
        this.associationMonitor = associationMonitor;
    }

    public void bindConnections() throws IOException, GeneralSecurityException {
        for (Connection con : conns)
            con.bind();
    }

    public void rebindConnections() throws IOException, GeneralSecurityException {
        for (Connection con : conns)
            if (con.isRebindNeeded())
                con.rebind();
    }

    private void needRebindConnections()  {
        for (Connection con : conns)
            con.needRebind();
     }

    private void needReconfigureTLS()  {
        for (Connection con : conns)
            if (con.isTls())
                con.needRebind();
        sslContext = null;
    }

    public void unbindConnections() {
        for (Connection con : conns)
            con.unbind();
    }

    public final Executor getExecutor() {
        return executor;
    }

    public final void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public final ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    public final void setScheduledExecutor(ScheduledExecutorService executor) {
        this.scheduledExecutor = executor;
    }

    public void addConnection(Connection conn) {
        conn.setDevice(this);
        conns.add(conn);
        conn.needRebind();
    }

    public boolean removeConnection(Connection conn) {
        for (ApplicationEntity ae : aes.values())
            if (ae.getConnections().contains(conn))
                throw new IllegalStateException(conn + " used by AE: " + 
                        ae.getAETitle());

        for (DeviceExtension ext : extensions.values())
            ext.verifyNotUsed(conn);

        if (!conns.remove(conn))
            return false;

        conn.setDevice(null);
        conn.unbind();
        return true;
    }

    public List<Connection> listConnections() {
        return Collections.unmodifiableList(conns);
    }

    public Connection connectionWithEqualsRDN(Connection other) {
        for (Connection conn : conns)
            if (conn.equalsRDN(other))
                return conn;

        return null;
    }


    public void addApplicationEntity(ApplicationEntity ae) {
        ae.setDevice(this);
        aes.put(ae.getAETitle(), ae);
    }

    public ApplicationEntity removeApplicationEntity(ApplicationEntity ae) {
        return removeApplicationEntity(ae.getAETitle());
    }

    public ApplicationEntity removeApplicationEntity(String aet) {
        ApplicationEntity ae = aes.remove(aet);
        if (ae != null)
            ae.setDevice(null);
        return ae;
    }

    public Collection<String> getWebApplicationNames() {
        return webapps.keySet();
    }

    public Collection<WebApplication> getWebApplications() {
        return webapps.values();
    }

    public Collection<WebApplication> getWebApplicationsWithServiceClass(WebApplication.ServiceClass serviceClass) {
        Collection<WebApplication> result = new ArrayList<>(webapps.size());
        for (WebApplication webapp : webapps.values()) {
            if (webapp.containsServiceClass(serviceClass))
                result.add(webapp);
        }
        return result;
    }

    public WebApplication getWebApplication(String name) {
        return webapps.get(name);
    }

    public void addWebApplication(WebApplication webapp) {
        webapp.setDevice(this);
        webapps.put(webapp.getApplicationName(), webapp);
    }

    public WebApplication removeWebApplication(WebApplication webapp) {
        return removeWebApplication(webapp.getApplicationName());
    }

    public WebApplication removeWebApplication(String name) {
        WebApplication webapp = webapps.remove(name);
        if (webapp != null)
            webapp.setDevice(null);
        return webapp;
    }

    public Collection<String> getKeycloakClientIDs() {
        return keycloakClients.keySet();
    }

    public Collection<KeycloakClient> getKeycloakClients() {
        return keycloakClients.values();
    }

    public KeycloakClient getKeycloakClient(String clientID) {
        return keycloakClients.get(clientID);
    }

    public void addKeycloakClient(KeycloakClient client) {
        client.setDevice(this);
        keycloakClients.put(client.getKeycloakClientID(), client);
    }

    public KeycloakClient removeKeycloakClient(KeycloakClient client) {
        return removeKeycloakClient(client.getKeycloakClientID());
    }

    public KeycloakClient removeKeycloakClient(String name) {
        KeycloakClient client = keycloakClients.remove(name);
        if (client != null)
            client.setDevice(null);
        return client;
    }

    public void addDeviceExtension(DeviceExtension ext) {
        Class<? extends DeviceExtension> clazz = ext.getClass();
        if (extensions.containsKey(clazz))
            throw new IllegalStateException(
                    "already contains Device Extension:" + clazz);

        ext.setDevice(this);
        extensions.put(clazz, ext);
    }

    public boolean removeDeviceExtension(DeviceExtension ext) {
        if (extensions.remove(ext.getClass()) == null)
            return false;

        ext.setDevice(null);
        return true;
    }

    public final int getLimitOpenAssociations() {
        return limitOpenAssociations;
    }

    public final void setLimitOpenAssociations(int limit) {
        if (limit < 0)
            throw new IllegalArgumentException("limit: " + limit);

        this.limitOpenAssociations = limit;
    }

    /** Returns maximal number of open Associations which can be initiated by the specified remote AE.
     * If the limit is exceeded, further Association requests from that AE will be rejected with
     * Result = 2 - rejected-transient, Source = 1 - DICOM UL service-user, Reason = 2 - local-limit-exceeded.
     *
     * @param callingAET AE Title of remote AE.
     * @return maximal number of open Associations or 0 for no limit.
     * @throws NullPointerException if callingAET is null.
     *
     * @see #setLimitAssociationsInitiatedBy(String, int)
     */
    public int getLimitAssociationsInitiatedBy(String callingAET) {
        Integer value = limitAssociationsInitiatedBy.get(Objects.requireNonNull(callingAET));
        return value != null ? value.intValue() : 0;
    }

    /** Sets maximal number of open Associations which can be initiated by the specified remote AE.
     * If the limit is exceeded, further Association requests from that AE will be rejected with
     * Result = 2 - rejected-transient, Source = 1 - DICOM UL service-user, Reason = 2 - local-limit-exceeded.
     *
     * @param callingAET AE Title of remote AE.
     * @param limit maximal number of open Associations or 0 for no limit.
     * @throws NullPointerException if callingAET is null.
     * @throws IllegalArgumentException if limit is lesser than zero.
     *
     * @see #getLimitAssociationsInitiatedBy(String)
     */
    public void setLimitAssociationsInitiatedBy(String callingAET, int limit) {
        Objects.requireNonNull(callingAET);
        if (limit < 0)
            throw new IllegalArgumentException("limit: " + limit);

        if (limit > 0)
            limitAssociationsInitiatedBy.put(callingAET, limit);
        else
            limitAssociationsInitiatedBy.remove(callingAET);
    }

    public String[] getLimitAssociationsInitiatedBy() {
        String[] ss = new String[limitAssociationsInitiatedBy.size()];
        int i = 0;
        for (Entry<String, Integer> entry : limitAssociationsInitiatedBy.entrySet()) {
            ss[i++] = entry.getKey() + '=' + entry.getValue();
        }
        return ss;
    }

    public void setLimitAssociationsInitiatedBy(String[] values) {
        Map<String, Integer> tmp = new HashMap<>();
        for (String value : values) {
            int endIndex = value.lastIndexOf('=');
            try {
                tmp.put(value.substring(0, endIndex), Integer.valueOf(value.substring(endIndex + 1)));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(value);
            }
        }
        setLimitAssociationsInitiatedBy(tmp);
    }

    private void setLimitAssociationsInitiatedBy(Map<String, Integer> tmp) {
        limitAssociationsInitiatedBy.clear();
        limitAssociationsInitiatedBy.putAll(tmp);
    }

    void addAssociation(Association as) {
        synchronized (associations) {
            associations.add(as);
        }
    }

    void removeAssociation(Association as) {
        synchronized (associations) {
            associations.remove(as);
            associations.notifyAll();
        }
    }

    public Association [] listOpenAssociations() {
        synchronized (associations) {
            return associations.toArray(new Association[associations.size()]);
        }
    }

    public int getNumberOfOpenAssociations() {
        return associations.size();
    }

    public int getNumberOfAssociationsInitiatedBy(String callingAET) {
        synchronized (associations) {
            int count = 0;
            for (Association association : associations) {
                if (callingAET.equals(association.getCallingAET()))
                    count++;
            }
            return count;
        }
    }

    public int getNumberOfAssociationsInitiatedTo(String calledAET) {
        synchronized (associations) {
            int count = 0;
            for (Association association : associations) {
                if (calledAET.equals(association.getCalledAET()))
                    count++;
            }
            return count;
        }
    }

    public void waitForNoOpenConnections() throws InterruptedException {
        synchronized (associations) {
            while (!associations.isEmpty())
                associations.wait();
        }
    }

    public boolean isLimitOfAssociationsExceeded(AAssociateRQ rq) {
        Integer limit;
        return limitOpenAssociations > 0 && associations.size() > limitOpenAssociations
                || (limit = limitAssociationsInitiatedBy.get(rq.getCallingAET())) != null
                && getNumberOfAssociationsInitiatedBy(rq.getCallingAET()) > limit;
    }

    public ApplicationEntity getApplicationEntity(String aet) {
        return aes.get(aet);
    }

    public ApplicationEntity getApplicationEntity(String aet, boolean matchOtherAETs) {
        ApplicationEntity ae = aes.get(aet);
        if (ae == null)
            ae = aes.get("*");
        if (ae == null && matchOtherAETs)
            for (ApplicationEntity ae1 : getApplicationEntities())
                if (ae1.isOtherAETitle(aet))
                    return ae1;
        return ae;
    }

    public Collection<String> getApplicationAETitles() {
        return aes.keySet();
    }

    public Collection<ApplicationEntity> getApplicationEntities() {
        return aes.values();
    }

    public final void setKeyManager(KeyManager km) {
        this.km = km;
        needReconfigureTLS();
    }

    public final KeyManager getKeyManager() {
        return km;
    }

    private KeyManager km() throws GeneralSecurityException, IOException {
        KeyManager ret = km;
        if (ret != null || keyStoreURL == null)
            return ret;
        String keyStorePin = keyStorePin();
        km = ret = SSLManagerFactory.createKeyManager(
                        StringUtils.replaceSystemProperties(keyStoreType()),
                        StringUtils.replaceSystemProperties(keyStoreURL),
                        StringUtils.replaceSystemProperties(keyStorePin()),
                        StringUtils.replaceSystemProperties(keyPin(keyStorePin)));
        return ret;
    }

    private String keyStoreType() {
        if (keyStoreType == null)
            throw new IllegalStateException("keyStoreURL requires keyStoreType");
            
        return keyStoreType;
    }

    private String keyStorePin() {
        if (keyStorePin != null)
            return keyStorePin;

        if (keyStorePinProperty == null)
            throw new IllegalStateException(
                    "keyStoreURL requires keyStorePin or keyStorePinProperty");

        String pin = System.getProperty(keyStorePinProperty);
        if (pin == null)
            throw new IllegalStateException(
                    "No such keyStorePinProperty: " + keyStorePinProperty);

        return pin;
    }

    private String keyPin(String keyStorePin) {
        if (keyStoreKeyPin != null)
            return keyStoreKeyPin;

        if (keyStoreKeyPinProperty == null)
            return keyStorePin;

        String pin = System.getProperty(keyStoreKeyPinProperty);
        if (pin == null)
            throw new IllegalStateException(
                    "No such keyPinProperty: " + keyStoreKeyPinProperty);

        return pin;
    }

    public final void setTrustManager(TrustManager tm) {
        this.tm = tm;
        needReconfigureTLS();
    }

    public final TrustManager getTrustManager() {
        return tm;
    }

    private TrustManager tm() throws GeneralSecurityException, IOException {
        TrustManager ret = tm;
        if (ret != null
                || trustStoreURL == null && authorizedNodeCertificates.isEmpty())
            return ret;

        tm = ret = trustStoreURL != null
                ? SSLManagerFactory.createTrustManager(
                        StringUtils.replaceSystemProperties(trustStoreType()),
                        StringUtils.replaceSystemProperties(trustStoreURL),
                        StringUtils.replaceSystemProperties(trustStorePin()))
                : SSLManagerFactory.createTrustManager(
                        getAllAuthorizedNodeCertificates());
        return ret;
    }

    private String trustStoreType() {
        if (trustStoreType == null)
            throw new IllegalStateException("trustStoreURL requires trustStoreType");
            
        return trustStoreType;
    }

    private String trustStorePin() {
        if (trustStorePin != null)
            return trustStorePin;

        if (trustStorePinProperty == null)
            throw new IllegalStateException(
                    "trustStoreURL requires trustStorePin or trustStorePinProperty");

        String pin = System.getProperty(trustStorePinProperty);
        if (pin == null)
            throw new IllegalStateException(
                    "No such trustStorePinProperty: " + trustStorePinProperty);

        return pin;
    }

    public SSLContext sslContext() throws GeneralSecurityException, IOException {
        SSLContext ctx = sslContext;
        if (ctx != null)
            return ctx;

        ctx = SSLContext.getInstance("TLS");
        ctx.init(keyManagers(), trustManagers(), null);
        sslContext = ctx;
        return ctx;
    }

    public KeyManager[] keyManagers() throws GeneralSecurityException, IOException {
        KeyManager tmp = km();
        return tmp != null ? new KeyManager[] { tmp } : null;
    }

    public TrustManager[] trustManagers() throws GeneralSecurityException, IOException {
        TrustManager tmp = tm();
        return tmp != null ? new TrustManager[] { tmp } : null;
    }

    public void execute(Runnable command) {
        if (executor == null)
            throw new IllegalStateException("executor not initialized");

        executor.execute(command);
    }

    public ScheduledFuture<?> schedule(Runnable command, long delay,
            TimeUnit unit) {
        if (scheduledExecutor == null)
            throw new IllegalStateException(
                    "scheduled executor service not initialized");

        return scheduledExecutor.schedule(command, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
            long initialDelay, long period, TimeUnit unit) {
        if (scheduledExecutor == null)
            throw new IllegalStateException(
                    "scheduled executor service not initialized");

        return scheduledExecutor.scheduleAtFixedRate(command,
                initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
            long initialDelay, long delay, TimeUnit unit) {
        if (scheduledExecutor == null)
            throw new IllegalStateException(
                    "scheduled executor service not initialized");

        return scheduledExecutor.scheduleWithFixedDelay(command,
                initialDelay, delay, unit);
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder(512), "").toString();
    }

    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + "  ";
        StringUtils.appendLine(sb, indent, "Device[name: ", deviceName);
        StringUtils.appendLine(sb, indent2,"desc: ", description);
        StringUtils.appendLine(sb, indent2,"installed: ", installed);
        for (Connection conn : conns)
            conn.promptTo(sb, indent2).append(StringUtils.LINE_SEPARATOR);
        for (ApplicationEntity ae : aes.values())
            ae.promptTo(sb, indent2).append(StringUtils.LINE_SEPARATOR);
        return sb.append(indent).append(']');
    }

    public void reconfigure(Device from) throws IOException, GeneralSecurityException {
        setDeviceAttributes(from);
        reconfigureConnections(from);
        reconfigureApplicationEntities(from);
        reconfigureWebApplications(from);
        reconfigureKeycloakClients(from);
        reconfigureDeviceExtensions(from);
    }

    protected void setDeviceAttributes(Device from) {
        setDescription(from.description);
        setDeviceUID(from.deviceUID);
        setManufacturer(from.manufacturer);
        setManufacturerModelName(from.manufacturerModelName);
        setSoftwareVersions(from.softwareVersions);
        setStationName(from.stationName);
        setDeviceSerialNumber(from.deviceSerialNumber);
        setTrustStoreURL(from.trustStoreURL);
        setTrustStoreType(from.trustStoreType);
        setTrustStorePin(from.trustStorePin);
        setKeyStoreURL(from.keyStoreURL);
        setKeyStoreType(from.keyStoreType);
        setKeyStorePin(from.keyStorePin);
        setKeyStoreKeyPin(from.keyStoreKeyPin);
        setTimeZoneOfDevice(from.timeZoneOfDevice);
        setIssuerOfPatientID(from.issuerOfPatientID);
        setIssuerOfAccessionNumber(from.issuerOfAccessionNumber);
        setOrderPlacerIdentifier(from.orderPlacerIdentifier);
        setOrderFillerIdentifier(from.orderFillerIdentifier);
        setIssuerOfAdmissionID(from.issuerOfAdmissionID);
        setIssuerOfServiceEpisodeID(from.issuerOfServiceEpisodeID);
        setIssuerOfContainerIdentifier(from.issuerOfContainerIdentifier);
        setIssuerOfSpecimenIdentifier(from.issuerOfSpecimenIdentifier);
        setInstitutionNames(from.institutionNames);
        setInstitutionCodes(from.institutionCodes);
        setInstitutionAddresses(from.institutionAddresses);
        setInstitutionalDepartmentNames(from.institutionalDepartmentNames);
        setPrimaryDeviceTypes(from.primaryDeviceTypes);
        setRelatedDeviceRefs(from.relatedDeviceRefs);
        setAuthorizedNodeCertificates(from.authorizedNodeCertificates);
        setThisNodeCertificates(from.thisNodeCertificates);
        setVendorData(from.vendorData);
        setLimitOpenAssociations(from.limitOpenAssociations);
        setInstalled(from.installed);
        setLimitAssociationsInitiatedBy(from.limitAssociationsInitiatedBy);
        setRoleSelectionNegotiationLenient(from.roleSelectionNegotiationLenient);
     }

     private void setAuthorizedNodeCertificates(Map<String, X509Certificate[]> from) {
         if (update(authorizedNodeCertificates, from))
             setTrustManager(null);
     }

     private void setThisNodeCertificates(Map<String, X509Certificate[]> from) {
         update(thisNodeCertificates, from);
     }

     private boolean update(Map<String, X509Certificate[]> target,
            Map<String, X509Certificate[]> from) {
        boolean updated = target.keySet().retainAll(from.keySet());
        for (Entry<String, X509Certificate[]> e : from.entrySet()) {
            String key = e.getKey();
            X509Certificate[] value = e.getValue();
            X509Certificate[] certs = target.get(key);
            if (certs == null || !Arrays.equals(value, certs)) {
                target.put(key, value);
                updated = true;
            }
        }
        return updated;
     }

     private void reconfigureConnections(Device from) {
         Iterator<Connection> connIter = conns.iterator();
         while (connIter.hasNext()) {
             Connection conn = connIter.next();
             if (from.connectionWithEqualsRDN(conn) == null) {
                 connIter.remove();
                 conn.setDevice(null);
                 conn.unbind();
             }
         }
         for (Connection src : from.conns) {
             Connection conn = connectionWithEqualsRDN(src);
             if (conn == null)
                 this.addConnection(conn = new Connection());
             conn.reconfigure(src);
         }
    }

     private void reconfigureApplicationEntities(Device from) {
         aes.keySet().retainAll(from.aes.keySet());
         for (ApplicationEntity src : from.aes.values()) {
             ApplicationEntity ae = aes.get(src.getAETitle());
             if (ae == null)
                 addApplicationEntity(ae = new ApplicationEntity(src.getAETitle()));
             ae.reconfigure(src);
         }
     }

    private void reconfigureWebApplications(Device from) {
        webapps.keySet().retainAll(from.webapps.keySet());
        for (WebApplication src : from.webapps.values()) {
            WebApplication webapp = webapps.get(src.getApplicationName());
            if (webapp == null)
                addWebApplication(webapp = new WebApplication(src.getApplicationName()));
            webapp.reconfigure(src);
        }
    }

    private void reconfigureKeycloakClients(Device from) {
        keycloakClients.keySet().retainAll(from.keycloakClients.keySet());
        for (KeycloakClient src : from.keycloakClients.values()) {
            KeycloakClient client = keycloakClients.get(src.getKeycloakClientID());
            if (client == null)
                addKeycloakClient(client = new KeycloakClient(src.getKeycloakClientID()));
            client.reconfigure(src);
        }
    }

    public void reconfigureConnections(List<Connection> conns,
            List<Connection> src) {
        conns.clear();
        for (Connection conn : src)
            conns.add(connectionWithEqualsRDN(conn));
    }

    private void reconfigureDeviceExtensions(Device from) {
        for (Iterator<Class<? extends DeviceExtension>> it =
                extensions.keySet().iterator(); it.hasNext();) {
            if (!from.extensions.containsKey(it.next()))
                it.remove();
        }
        for (DeviceExtension src : from.extensions.values()) {
            Class<? extends DeviceExtension> clazz = src.getClass();
            DeviceExtension ext = extensions.get(clazz);
            if (ext == null)
                try {
                    addDeviceExtension(ext = clazz.newInstance());
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to instantiate " + clazz.getName(), e);
                }
            ext.reconfigure(src);
        }
    }

    public Collection<DeviceExtension> listDeviceExtensions() {
        return extensions.values();
    }

    @SuppressWarnings("unchecked")
    public <T extends DeviceExtension> T getDeviceExtension(Class<T> clazz) {
        return (T) extensions.get(clazz);
    }

    public <T extends DeviceExtension> T getDeviceExtensionNotNull(Class<T> clazz) {
        T devExt = getDeviceExtension(clazz);
        if (devExt == null)
            throw new IllegalStateException("No " + clazz.getName()
                    + " configured for Device: " + deviceName);
        return devExt;
    }
}
