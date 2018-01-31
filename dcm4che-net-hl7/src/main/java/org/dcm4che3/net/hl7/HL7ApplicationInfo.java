/*
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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015-2017
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
 */

package org.dcm4che3.net.hl7;

import org.dcm4che3.net.Connection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @since July 2017
 */
public class HL7ApplicationInfo implements Serializable {

    private String deviceName;
    private String hl7ApplicationName;
    private String[] hl7OtherApplicationName;
    private String description;
    private String[] applicationClusters = {};
    private Boolean installed;
    private final List<Connection> conns = new ArrayList<>(1);

    public Boolean getInstalled() {
        return installed;
    }

    public void setInstalled(Boolean installed) {
        this.installed = installed;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHl7ApplicationName() {
        return hl7ApplicationName;
    }

    public void setHl7ApplicationName(String hl7ApplicationName) {
        this.hl7ApplicationName = hl7ApplicationName;
    }

    public String[] getHl7OtherApplicationName() {
        return hl7OtherApplicationName;
    }

    public void setHl7OtherApplicationName(String[] hl7OtherApplicationName) {
        this.hl7OtherApplicationName = hl7OtherApplicationName;
    }

    public String[] getApplicationClusters() {
        return applicationClusters;
    }

    public void setApplicationClusters(String[] applicationClusters) {
        this.applicationClusters = applicationClusters;
    }

    public List<Connection> getConnections() {
        return conns;
    }

    @Override
    public String toString() {
        return "HL7ApplicationInfo[hl7ApplicationName=" + hl7ApplicationName
                + "]";
    }

}
