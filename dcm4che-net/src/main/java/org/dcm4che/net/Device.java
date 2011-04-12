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

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;

import org.dcm4che.net.pdu.AAssociateAC;
import org.dcm4che.net.pdu.AAssociateRJ;
import org.dcm4che.net.pdu.AAssociateRQ;

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
    private String issuerOfPatientID;
    private String[] softwareVersion = {};
    private String[] primaryDeviceType = {};
    private String[] institutionName = {};
    private String[] institutionAddress = {};
    private String[] institutionalDepartmentName = {};
    private X509Certificate[] authorizedNodeCertificate = {};
    private X509Certificate[] thisNodeCertificate = {};
    private Object vendorDeviceData;
    private boolean installed;
    private final List<Connection> conns = new ArrayList<Connection>();
    private final LinkedHashMap<String, ApplicationEntity> aes = 
            new LinkedHashMap<String, ApplicationEntity>();

    private int connLimit = DEF_CONN_LIMIT;

    private final AtomicInteger connCount = new AtomicInteger(0);

    private SSLContext sslContext;

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
     * <p>
     * This should be a unique name for this device. It is restricted to legal
     * LDAP names, and not constrained by DICOM AE Title limitations.
     * 
     * @param deviceName
     *                A String containing the device name.
     */
    public final void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
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
        return softwareVersion;
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
    public final void setSoftwareVersion(String... softwareVersion) {
        this.softwareVersion = softwareVersion;
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
    public final String[] getPrimaryDeviceType() {
        return primaryDeviceType;
    }

    /**
     * Set the type codes associated with this device.
     * <p>
     * Represents the kind of device and is most applicable for acquisition
     * modalities. Types should be selected from the list of code values
     * (0008,0100) for Context ID 30 in PS3.16 when applicable.
     * 
     * @param primaryDeviceType
     */
    public final void setPrimaryDeviceType(String... primaryDeviceType) {
        this.primaryDeviceType = primaryDeviceType;
    }

    /**
     * Get the institution name associated with this device; may be the site
     * where it resides or is operating on behalf of.
     * 
     * @return A String array containing the institution name values.
     */
    public final String[] getInstitutionName() {
        return institutionName;
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
    public final void setInstitutionName(String... name) {
        this.institutionName = name;
    }

    /**
     * Set the address of the institution which operates this device.
     * 
     * @return A String array containing the institution address values.
     */
    public final String[] getInstitutionAddress() {
        return institutionAddress;
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
    public final void setInstitutionAddress(String... addr) {
        this.institutionAddress = addr;
    }

    /**
     * Get the department name associated with this device.
     * 
     * @return A String array containing the dept. name values.
     */
    public final String[] getInstitutionalDepartmentName() {
        return institutionalDepartmentName;
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
    public final void setInstitutionalDepartmentName(String... name) {
        this.institutionalDepartmentName = name;
    }

    /**
     * Get the issuer of patient IDs for this device.
     * 
     * @return A String containing the PID issuer value.
     */
    public final String getIssuerOfPatientID() {
        return issuerOfPatientID;
    }

    /**
     * Set the issuer of patient IDs for this device.
     * <p>
     * Default value for the Issuer of Patient ID (0010,0021) for SOP Instances
     * created by this device. May be overridden by the values received in a
     * worklist or other source.
     * 
     * @param issuerOfPatientID
     *                A String containing the PID issuer value.
     */
    public final void setIssuerOfPatientID(String issuerOfPatientID) {
        this.issuerOfPatientID = issuerOfPatientID;
    }

    /**
     * Get the certificates of nodes that are authorized to connect to this
     * device.
     * 
     * @return An array containing the X509Certificate objects
     */
    public final X509Certificate[] getAuthorizedNodeCertificate() {
        return authorizedNodeCertificate;
    }

    /**
     * Set the certificates of nodes that are authorized to connect to this
     * device.
     * 
     * @param certs
     *                An array containing the X509Certificate objects.
     */
    public final void setAuthorizedNodeCertificate(X509Certificate... certs) {
        this.authorizedNodeCertificate = certs;
    }

    /**
     * Get the public certificate for this device.
     * 
     * @return An array containing the X509Certificate objects
     */
    public final X509Certificate[] getThisNodeCertificate() {
        return thisNodeCertificate;
    }

    /**
     * Set the public certificates for this device.
     * 
     * @param certs
     *                An array containing the X509Certificate objects.
     */
    public final void setThisNodeCertificate(X509Certificate... certs) {
        this.thisNodeCertificate = certs;
    }

    /**
     * Get device specific vendor configuration information
     * 
     * @return An Object of the device data.
     */
    public final Object getVendorDeviceData() {
        return vendorDeviceData;
    }

    /**
     * Set device specific vendor configuration information
     * 
     * @param vendorDeviceData
     *                An Object of the device data.
     */
    public final void setVendorDeviceData(Object vendorDeviceData) {
        this.vendorDeviceData = vendorDeviceData;
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
     */
    public final void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public void addConnection(Connection conn) {
        conn.setDevice(this);
        conns.add(conn);
    }

    public boolean removeConnection(Connection conn) {
        if (!conns.remove(conn))
            return false;

        conn.setDevice(null);
        return true;
    }

    public void addApplicationEntity(ApplicationEntity ae) {
        String aet = ae.getAETitle();
        if (aet == null)
            throw new IllegalArgumentException("ae without AE Title");

        ae.setDevice(this);
        aes.put(aet, ae);
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
        return connCount.intValue();
    }

    void incrementNumberOfOpenConnections() {
        connCount.incrementAndGet();
    }

    void decrementNumberOfOpenConnections() {
        connCount.decrementAndGet();
    }

    public boolean isLimitOfOpenConnectionsExceeded() {
        return getNumberOfOpenConnections() > connLimit;
    }

    public SSLContext getSSLContext() {
        // TODO Auto-generated method stub
        return null;
    }

    public ApplicationEntity getApplicationEntity(String aet) {
        return aes.get(aet);
    }

    public AAssociateAC negotiate(Association as, AAssociateRQ rq)
            throws AAssociateRJ {
        if (isLimitOfOpenConnectionsExceeded())
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_TRANSIENT,
                    AAssociateRJ.SOURCE_SERVICE_PROVIDER_ACSE,
                    AAssociateRJ.REASON_LOCAL_LIMIT_EXCEEDED);
        ApplicationEntity ae = aes.get(rq.getCalledAET());
        if (ae == null || !ae.isInstalled() || !ae.isAssociationAcceptor())
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_CALLED_AET_NOT_RECOGNIZED);
        if (ae.isAcceptOnlyPreferredCallingAETitles()
                && !ae.isPreferredCallingAETitle(rq.getCallingAET()))
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_CALLING_AET_NOT_RECOGNIZED);
        return ae.negotiate(as, rq);
    }

}
