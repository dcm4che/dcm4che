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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4che3.net.audit;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.DeviceExtension;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@LDAP(objectClasses = "dcmAuditRecordRepository")
@ConfigurableClass
public class AuditRecordRepository extends DeviceExtension {

    private static final long serialVersionUID = -2279487409324427161L;

    static {
        Connection.registerTCPProtocolHandler(
                Connection.Protocol.SYSLOG_TLS, SyslogProtocolHandler.INSTANCE);
        Connection.registerUDPProtocolHandler(
                Connection.Protocol.SYSLOG_UDP, SyslogProtocolHandler.INSTANCE);
    }

    @ConfigurableProperty(name="dicomInstalled")
    private Boolean arrInstalled;

    @ConfigurableProperty(name="dicomNetworkConnectionReference", collectionOfReferences = true)
    private final List<Connection> connections = new ArrayList<Connection>(1);
    
    @ConfigurableProperty(name="auditRecordCodeCachingEnabled", defaultValue = "false")
    private boolean auditRecordCodeCachingEnabled;

    private transient AuditRecordHandler handler;

    public boolean isInstalled() {
        return device != null && device.isInstalled() 
                && (arrInstalled == null || arrInstalled.booleanValue());
    }

    public final Boolean getArrInstalled() {
        return arrInstalled;
    }

    public void setArrInstalled(Boolean installed) {
        if (installed != null && installed.booleanValue()
                && device != null && !device.isInstalled())
            throw new IllegalStateException("owning device not installed");
        this.arrInstalled = installed;
    }

    public void addConnection(Connection conn) {
        if (!conn.getProtocol().isSyslog())
            throw new IllegalArgumentException(
                    "Audit Record Repository does not support protocol " + conn.getProtocol());
        if (device != null && device != conn.getDevice())
            throw new IllegalStateException(conn + " not contained by " + 
                    device.getDeviceName());
        connections.add(conn);
    }
    
    public boolean removeConnection(Connection conn) {
        return connections.remove(conn);
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections.clear();
        for (Connection connection : connections) addConnection(connection);
    }

    public AuditRecordHandler getAuditRecordHandler() {
        return handler;
    }

    public void setAuditRecordHandler(AuditRecordHandler handler) {
        this.handler = handler;
    }
    
    public boolean isAuditRecordCodeCachingEnabled() {
        return auditRecordCodeCachingEnabled;
    }

    public void setAuditRecordCodeCachingEnabled(boolean auditRecordCodeCachingEnabled) {
        this.auditRecordCodeCachingEnabled = auditRecordCodeCachingEnabled;
    }

    @Override
    public void reconfigure(DeviceExtension from)  {
        reconfigure((AuditRecordRepository) from);
    }

    private void reconfigure(AuditRecordRepository from) {
        setArrInstalled(from.arrInstalled);
        device.reconfigureConnections(connections, from.connections);
    }

    public void onMessage(byte[] data, int xmlOffset, int xmlLength,
            Connection conn, InetAddress from) {
        if (handler == null)
            throw new IllegalStateException("No AuditRecordHandler initialized");

        handler.onMessage(data, xmlOffset, xmlLength, conn, from);
    }
}
