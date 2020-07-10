/*
 * **** BEGIN LICENSE BLOCK *****
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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015-2018
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
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4che3.net;

import java.util.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Apr 2018
 */
public class WebApplicationInfo {

    private String deviceName;
    private String applicationName;
    private String description;
    private String servicePath;
    private String aeTitle;
    private String[] applicationClusters = {};
    private String keycloakClientID;
    private KeycloakClient keycloakClient;
    private Boolean installed;
    private final EnumSet<WebApplication.ServiceClass> serviceClasses = EnumSet.noneOf(WebApplication.ServiceClass.class);
    private final Map<String, String> properties = new HashMap<>();
    private final List<Connection> conns = new ArrayList<>(1);

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String name) {
        this.applicationName = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public String getAETitle() {
        return aeTitle;
    }

    public void setAETitle(String aeTitle) {
        this.aeTitle = aeTitle;
    }

    public String[] getApplicationClusters() {
        return applicationClusters;
    }

    public void setApplicationClusters(String[] applicationClusters) {
        this.applicationClusters = applicationClusters;
    }

    public String getKeycloakClientID() {
        return keycloakClientID;
    }

    public void setKeycloakClientID(String keycloakClientID) {
        this.keycloakClientID = keycloakClientID;
    }

    public KeycloakClient getKeycloakClient() {
        return keycloakClient;
    }

    public void setKeycloakClient(KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    public Boolean getInstalled() {
        return installed;
    }

    public void setInstalled(Boolean installed) {
        this.installed = installed;
    }

    public List<Connection> getConnections() {
        return conns;
    }

    public WebApplication.ServiceClass[] getServiceClasses() {
        return serviceClasses.toArray(new WebApplication.ServiceClass[0]);
    }

    public void setServiceClasses(WebApplication.ServiceClass... serviceClasses) {
        setServiceClasses(Arrays.asList(serviceClasses));
    }

    public void setServiceClasses(List<WebApplication.ServiceClass> serviceClasses) {
        this.serviceClasses.clear();
        this.serviceClasses.addAll(serviceClasses);
    }

    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    public String getProperty(String name, String defValue) {
        String value = properties.get(name);
        return value != null ? value : defValue;
    }

    public Map<String,String> getProperties() {
        return properties;
    }

    public void setProperties(String[] ss) {
        properties.clear();
        for (String s : ss) {
            int index = s.indexOf('=');
            if (index < 0)
                throw new IllegalArgumentException("Property in incorrect format : " + s);
            setProperty(s.substring(0, index), s.substring(index+1));
        }
    }

    @Override
    public String toString() {
        return "WebApplicationInfo[name=" + applicationName
                + ",classes=" + serviceClasses
                + ",path=" + servicePath
                + ",aet=" + aeTitle
                + ",applicationClusters=" + Arrays.toString(applicationClusters)
                + ",keycloakClientID=" + keycloakClientID
                + ",serviceClasses=" + serviceClasses
                + ",properties=" + properties
                + ",installed=" + installed
                + ']';
    }
}
