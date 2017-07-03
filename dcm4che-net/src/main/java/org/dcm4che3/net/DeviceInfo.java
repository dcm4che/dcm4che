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

package org.dcm4che3.net;

import java.io.Serializable;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 *
 */
public class DeviceInfo implements Serializable {

    private static final long serialVersionUID = 5456917095404357064L;

    private String deviceName;
    private String description;
    private String manufacturer;
    private String manufacturerModelName;
    private String stationName;
    private String[] softwareVersions = {};
    private String[] primaryDeviceTypes = {};
    private String[] institutionNames = {};
    private String[] institutionalDepartmentNames = {};
    private Boolean installed;
    private Boolean arcDevExt;

    public final String getDeviceName() {
        return deviceName;
    }

    public final void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public final String getDescription() {
        return description;
    }

    public final void setDescription(String description) {
        this.description = description;
    }

    public final String getManufacturer() {
        return manufacturer;
    }

    public final void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public final String getManufacturerModelName() {
        return manufacturerModelName;
    }

    public final void setManufacturerModelName(String manufacturerModelName) {
        this.manufacturerModelName = manufacturerModelName;
    }

    public final String getStationName() {
        return stationName;
    }

    public final void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public final String[] getSoftwareVersions() {
        return softwareVersions;
    }

    public final void setSoftwareVersions(String[] softwareVersions) {
        this.softwareVersions = softwareVersions;
    }

    public final String[] getPrimaryDeviceTypes() {
        return primaryDeviceTypes;
    }

    public final void setPrimaryDeviceTypes(String[] primaryDeviceTypes) {
        this.primaryDeviceTypes = primaryDeviceTypes;
    }

    public final String[] getInstitutionNames() {
        return institutionNames;
    }

    public final void setInstitutionNames(String[] institutionNames) {
        this.institutionNames = institutionNames;
    }

    public final String[] getInstitutionalDepartmentNames() {
        return institutionalDepartmentNames;
    }

    public final void setInstitutionalDepartmentNames(
            String[] institutionalDepartmentNames) {
        this.institutionalDepartmentNames = institutionalDepartmentNames;
    }

    public final Boolean getInstalled() {
        return installed;
    }

    public final void setInstalled(Boolean installed) {
        this.installed = installed;
    }

    public Boolean getArcDevExt() {
        return arcDevExt;
    }

    public void setArcDevExt(Boolean arcDevExt) {
        this.arcDevExt = arcDevExt;
    }

    @Override
    public String toString() {
        return "DeviceInfo[name=" + deviceName
                + ", installed=" + installed
                + "]";
    }

}
