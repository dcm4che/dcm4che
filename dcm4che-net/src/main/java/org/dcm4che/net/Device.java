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

package org.dcm4che.net;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.dcm4che.util.StringUtils;

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
public class Device {

    private static final int DEF_CONN_LIMIT = 100;

    private String deviceName;
    private String description;
    private String manufacturer;
    private String manufacturerModelName;
    private String stationName;
    private String deviceSerialNumber;
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
    private boolean installed = true;
    private boolean activated = false;
    private final LinkedHashMap<String, X509Certificate[]> authorizedNodeCertificates = 
            new LinkedHashMap<String, X509Certificate[]>();
    private final LinkedHashMap<String, X509Certificate[]> thisNodeCertificates = 
            new LinkedHashMap<String, X509Certificate[]>();
    private final List<Connection> conns = new ArrayList<Connection>();
    private final LinkedHashMap<String, ApplicationEntity> aes = 
            new LinkedHashMap<String, ApplicationEntity>();

    private DimseRQHandler dimseRQHandler;
    private HashMap<String,Object> properties = new HashMap<String,Object>();

    private int connLimit = DEF_CONN_LIMIT;
    private int connCount = 0;
    private final Object connCountLock = new Object();

    private Executor executor;
    private ScheduledExecutorService scheduledExecutor;
    private SSLContext sslContext;
    private KeyManager km;
    private TrustManager tm;

    public Device(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Device Name cannot be empty");
        this.deviceName = name;
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
    public final String[] getSoftwareVersion() {
        return softwareVersions;
    }

    /**
     * Set the software versions running on (or implemented by) this device.
     * <p>
     * This should be the same as the values of Software Versions (0018,1020) in
     * SOP instances created by this device.
     * 
     * @param softwareVersion
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
     * @param addr
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
     * @param name
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
    }

    public X509Certificate[] removeAuthorizedNodeCertificates(String ref) {
        return authorizedNodeCertificates.remove(ref);
    }

    public void removeAllAuthorizedNodeCertificates(String ref, X509Certificate... certs) {
        authorizedNodeCertificates.put(ref, certs);
    }

    public X509Certificate[] getAllAuthorizedNodeCertificates() {
        return toArray(authorizedNodeCertificates.values());
    }

    public String[] getAuthorizedNodeCertificateRefs() {
        return authorizedNodeCertificates.keySet().toArray(StringUtils.EMPTY_STRING);
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

    public void removeAllThisNodeCertificates(String ref, X509Certificate... certs) {
        thisNodeCertificates.put(ref, certs);
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
     * @throws KeyManagementException 
     */
    public final void setInstalled(boolean installed) throws IOException {
        if (this.installed == installed)
            return;

        this.installed = installed;
        if (activated)
            if (installed)
                try {
                    activateConnections();
                } catch (IOException e) {
                    this.installed = false;
                    throw e;
                }
            else
                deactivateConnections();
    }

    public final boolean isActivated() {
        return activated;
    }

    public final void setDimseRQHandler(DimseRQHandler dimseRQHandler) {
        this.dimseRQHandler = dimseRQHandler;
    }

    public final DimseRQHandler getDimseRQHandler() {
        return dimseRQHandler;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public Object setProperty(String key, Object value) {
        return properties.put(key, value);
    }

    public Object clearProperty(String key) {
        return properties.remove(key);
    }

    public void activate() throws IOException {
        if (activated)
            throw new IllegalStateException("already activated");

        activateConnections();
        activated = true;
    }

    public void deactivate() {
        if (!activated)
            return;

        deactivateConnections();
        activated = false;
    }

    private void activateConnections() throws IOException {
        try {
            for (Connection con : conns)
                con.activate();
        } catch (IOException e) {
            deactivateConnections();
            throw e;
        }
    }

    private void needRebindTLSConnections()  {
        for (Connection con : conns)
            if (con.isTls())
                con.needRebind();
    }

    private void deactivateConnections() {
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

    public void addConnection(Connection conn) throws IOException {
        conn.setDevice(this);
        conns.add(conn);
        if (activated)
            try {
                conn.activate();
            } catch (IOException e) {
                removeConnection(conn);
                throw e;
            }
    }

    public boolean removeConnection(Connection conn) {
        if (!conns.remove(conn))
            return false;

        conn.setDevice(null);
        conn.unbind();
        return true;
    }

    public List<Connection> listConnections() {
        return Collections.unmodifiableList(conns);
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

    public final int getLimitOfOpenConnections() {
        return connLimit;
    }

    public final void setLimitOfOpenConnections(int limit) {
        if (limit <= 0)
            throw new IllegalArgumentException("limit: " + limit);

        this.connLimit = limit;
    }

    public int getNumberOfOpenConnections() {
        return connCount;
    }

    void incrementNumberOfOpenConnections() {
        synchronized (connCountLock) {
            connCount++;
        }
    }

    void decrementNumberOfOpenConnections() {
        synchronized (connCountLock) {
            if (--connCount <= 0)
                connCountLock.notifyAll();
        }
    }

    public void waitForNoOpenConnections() throws InterruptedException {
        synchronized (connCountLock) {
            while (connCount > 0)
                connCountLock.wait();
        }
    }

    public boolean isLimitOfOpenConnectionsExceeded() {
        return getNumberOfOpenConnections() > connLimit;
    }

    public ApplicationEntity getApplicationEntity(String aet) {
        ApplicationEntity ae = aes.get(aet);
        if (ae == null)
            ae = aes.get("*");
        return ae;
    }

    public Collection<ApplicationEntity> getApplicationEntities() {
        return aes.values();
    }

    public final void setKeyManager(KeyManager km) throws KeyManagementException {
        this.km = km;
        if (tm != null)
            initTLS();
    }

    public final KeyManager getKeyManager() {
        return km;
    }

    public final void setTrustManager(TrustManager tm) throws KeyManagementException {
        this.tm = tm;
        if (tm != null)
            initTLS();
    }

    public final TrustManager getTrustManager() {
        return tm;
    }

    public void initTrustManager() throws KeyStoreException, KeyManagementException {
        setTrustManager(SSLManagerFactory.createTrustManager(
                getAllAuthorizedNodeCertificates()));
    }

    SSLContext sslContext() {
        if (sslContext == null)
            throw new IllegalStateException("TrustManager not initalized");
        return sslContext;
    }

    private void initTLS() throws KeyManagementException {
        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
        KeyManager km = this.km;
        TrustManager tm = this.tm;
        sslContext.init(km != null ? new KeyManager[]{ km } : null, 
                tm != null ? new TrustManager[]{ tm } : null, null);
        needRebindTLSConnections();
    }

    public void execute(Runnable command) {
        if (executor == null)
            throw new IllegalStateException("executer not initalized");

        executor.execute(command);
    }

    public ScheduledFuture<?> schedule(Runnable command, long delay,
            TimeUnit unit) {
        if (scheduledExecutor == null)
            throw new IllegalStateException(
                    "scheduled executor service not initalized");

        return scheduledExecutor.schedule(command, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
            long initialDelay, long period, TimeUnit unit) {
        if (scheduledExecutor == null)
            throw new IllegalStateException(
                    "scheduled executor service not initalized");

        return scheduledExecutor.scheduleAtFixedRate(command,
                initialDelay, period, unit);
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
}
