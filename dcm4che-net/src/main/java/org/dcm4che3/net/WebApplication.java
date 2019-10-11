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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Description of a Web Application provided by {@link Device}.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Apr 2018
 */
public class WebApplication {

    public enum ServiceClass {
        WADO_URI,
        WADO_RS,
        STOW_RS,
        QIDO_RS,
        UPS_RS,
        DCM4CHEE_ARC,
        DCM4CHEE_ARC_AET,
        PAM,
        REJECT,
        MOVE,
        MOVE_MATCHING,
        ELASTICSEARCH,
        DCM4CHEE_ARC_AET_DIFF
    }

    private Device device;
    private String applicationName;
    private String description;
    private String servicePath;
    private String aeTitle;
    private String[] applicationClusters = {};
    private String keycloakClientID;
    private Boolean installed;
    private EnumSet<ServiceClass> serviceClasses = EnumSet.noneOf(ServiceClass.class);
    private final List<Connection> conns = new ArrayList<>(1);

    public WebApplication() {}

    public WebApplication(String applicationName) {
        this.applicationName = applicationName;
    }

    public Device getDevice() {
        return device;
    }

    void setDevice(Device device) {
        if (device != null) {
            if (this.device != null)
                throw new IllegalStateException("already owned by " +
                        this.device.getDeviceName());
            for (Connection conn : conns)
                if (conn.getDevice() != device)
                    throw new IllegalStateException(conn + " not owned by " +
                            device.getDeviceName());
        }
        this.device = device;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("name cannot be empty");
        Device device = this.device;
        if (device != null)
            device.removeWebApplication(this.applicationName);
        this.applicationName = name;
        if (device != null)
            device.addWebApplication(this);
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
        this.servicePath = servicePath.startsWith("/") ? servicePath : '/' + servicePath;
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


    public void setApplicationClusters(String... applicationClusters) {
        this.applicationClusters = applicationClusters;
    }

    public String getKeycloakClientID() {
        return keycloakClientID;
    }

    public void setKeycloakClientID(String keycloakClientID) {
        this.keycloakClientID = keycloakClientID;
    }

    public boolean isInstalled() {
        return device != null && device.isInstalled()
                && (installed == null || installed.booleanValue());
    }

    public final Boolean getInstalled() {
        return installed;
    }

    public void setInstalled(Boolean installed) {
        if (installed != null && installed.booleanValue()
                && device != null && !device.isInstalled())
            throw new IllegalStateException("owning device not installed");
        this.installed = installed;
    }

    public KeycloakClient getKeycloakClient() {
        return keycloakClientID != null ? device.getKeycloakClient(keycloakClientID) : null;
    }

    public void addConnection(Connection conn) {
        if (conn.getProtocol() != Connection.Protocol.HTTP)
            throw new IllegalArgumentException(
                    "Web Application does not support protocol " + conn.getProtocol());
        if (device != null && device != conn.getDevice())
            throw new IllegalStateException(conn + " not contained by " +
                    device.getDeviceName());
        conns.add(conn);
    }

    public StringBuilder getServiceURL() {
        return getServiceURL(firstInstalledConnection());
    }

    private Connection firstInstalledConnection() {
        for (Connection conn : conns) {
            if (conn.isInstalled())
                return conn;
        }
        throw new IllegalStateException("No installed Network Connection");
    }

    public StringBuilder getServiceURL(Connection conn) {
        return new StringBuilder(64)
                .append(conn.isTls() ? "https://" : "http://")
                .append(conn.getHostname())
                .append(':')
                .append(conn.getPort())
                .append(servicePath);
    }

    public boolean removeConnection(Connection conn) {
        return conns.remove(conn);
    }

    public List<Connection> getConnections() {
        return conns;
    }

    public ServiceClass[] getServiceClasses() {
        return serviceClasses.toArray(new ServiceClass[0]);
    }

    public void setServiceClasses(ServiceClass... serviceClasses) {
        this.serviceClasses.clear();
        this.serviceClasses.addAll(Arrays.asList(serviceClasses));
    }

    public boolean containsServiceClass(ServiceClass serviceClass) {
        return serviceClasses.contains(serviceClass);
    }

    void reconfigure(WebApplication src) {
        description = src.description;
        servicePath = src.servicePath;
        aeTitle = src.aeTitle;
        applicationClusters = src.applicationClusters;
        keycloakClientID = src.keycloakClientID;
        installed = src.installed;
        serviceClasses.clear();
        serviceClasses.addAll(src.serviceClasses);
        device.reconfigureConnections(conns, src.conns);
    }

    @Override
    public String toString() {
        return "WebApplication[name=" + applicationName
                + ",classes=" + serviceClasses
                + ",path=" + servicePath
                + ",aet=" + aeTitle
                + ']';
    }
}
