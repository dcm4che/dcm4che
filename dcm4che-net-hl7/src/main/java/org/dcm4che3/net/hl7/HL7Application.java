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

package org.dcm4che3.net.hl7;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.hl7.HL7Exception;
import org.dcm4che3.hl7.HL7Segment;
import org.dcm4che3.hl7.MLLPConnection;
import org.dcm4che3.net.CompatibleConnection;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.IncompatibleConnectionException;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
@LDAP(objectClasses = "hl7Application")
@ConfigurableClass
public class HL7Application implements Serializable {

    private static final long serialVersionUID = -1765110968524548056L;

    private Device device;

    @ConfigurableProperty(name = "hl7ApplicationName",
            label = "HL7 application name",
            description = "HL7 Application and Facility name (Application^Facility)"
    )
    private String applicationName;

    @ConfigurableProperty(name = "hl7DefaultCharacterSet",
            label = "Default character set",
            description = "Character Set used to decode received messages if not specified by MSH-18, ASCII if absent")
    private String HL7DefaultCharacterSet;

    @ConfigurableProperty(name = "dicomInstalled")
    private Boolean hl7Installed;

    @ConfigurableProperty(name = "hl7AcceptedSendingApplication",
            label = "Accepted applications",
            description = "Application^Facility name of accepted Sending Application(s); any if absent")
    private Set<String> acceptedSendingApplicationsSet =
            new LinkedHashSet<String>();

    @ConfigurableProperty(name = "hl7AcceptedMessageType",
            label = "Accepted message types",
            description = "Message Type(s) (MessageType^TriggerEvent) of accepted messages")
    private Set<String> acceptedMessageTypesSet =
            new LinkedHashSet<String>();

    @ConfigurableProperty(name = "dicomNetworkConnectionReference",
            label = "Connections",
            description = "Which connections are used by this HL7 application",
            collectionOfReferences = true)
    private List<Connection> conns = new ArrayList<Connection>(1);

    private Map<Class<? extends HL7ApplicationExtension>, HL7ApplicationExtension> extensions =
            new HashMap<Class<? extends HL7ApplicationExtension>, HL7ApplicationExtension>();

    private transient HL7MessageListener hl7MessageListener;

    public HL7Application() {
    }

    public HL7Application(String applicationName) {
        setApplicationName(applicationName);
    }

    public final Device getDevice() {
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
        this.applicationName = name;
/*      TODO: inspect - commented out since this is done in HL7DeviceExtension.hl7apps setter
        HL7DeviceExtension ext = device != null
                ? device.getDeviceExtension(HL7DeviceExtension.class)
                : null;
        if (ext != null)
            ext.removeHL7Application(this.name);
        this.name = name;
        if (ext != null)
            ext.addHL7Application(this);*/
    }

    public Set<String> getAcceptedMessageTypesSet() {
        return acceptedMessageTypesSet;
    }

    public void setAcceptedMessageTypesSet(Set<String> acceptedMessageTypesSet) {
        this.acceptedMessageTypesSet = acceptedMessageTypesSet;
    }

    public final String getHL7DefaultCharacterSet() {
        return HL7DefaultCharacterSet;
    }

    public final void setHL7DefaultCharacterSet(String hl7DefaultCharacterSet) {
        this.HL7DefaultCharacterSet = hl7DefaultCharacterSet;
    }

    public Set<String> getAcceptedSendingApplicationsSet() {
        return acceptedSendingApplicationsSet;
    }

    public void setAcceptedSendingApplicationsSet(Set<String> acceptedSendingApplicationsSet) {
        this.acceptedSendingApplicationsSet = acceptedSendingApplicationsSet;
    }

    public String[] getAcceptedSendingApplications() {
        return acceptedSendingApplicationsSet.toArray(
                new String[acceptedSendingApplicationsSet.size()]);
    }

    public void setAcceptedSendingApplications(String... names) {
        acceptedSendingApplicationsSet.clear();
        for (String name : names)
            acceptedSendingApplicationsSet.add(name);
    }

    public String[] getAcceptedMessageTypes() {
        return acceptedMessageTypesSet.toArray(
                new String[acceptedMessageTypesSet.size()]);
    }

    public void setAcceptedMessageTypes(String... types) {
        acceptedMessageTypesSet.clear();
        for (String name : types)
            acceptedMessageTypesSet.add(name);
    }

    public boolean isInstalled() {
        return device != null && device.isInstalled()
                && (hl7Installed == null || hl7Installed.booleanValue());
    }

    public Boolean getHl7Installed() {
        return hl7Installed;
    }

    public void setHl7Installed(Boolean hl7Installed) {
        if (hl7Installed != null && hl7Installed.booleanValue()
                && device != null && !device.isInstalled())
            throw new IllegalStateException("owning device not installed");
        this.hl7Installed = hl7Installed;
    }

    public HL7MessageListener getHL7MessageListener() {
        HL7MessageListener listener = hl7MessageListener;
        if (listener != null)
            return listener;

        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        return hl7Ext != null
                ? hl7Ext.getHL7MessageListener()
                : null;
    }

    public final void setHL7MessageListener(HL7MessageListener listener) {
        this.hl7MessageListener = listener;
    }

    public void addConnection(Connection conn) {
        if (conn.getProtocol() != Connection.Protocol.HL7)
            throw new IllegalArgumentException(
                    "protocol != HL7 - " + conn.getProtocol());

        if (device != null && device != conn.getDevice())
            throw new IllegalStateException(conn + " not contained by " +
                    device.getDeviceName());
        conns.add(conn);
    }

    public boolean removeConnection(Connection conn) {
        return conns.remove(conn);
    }

    public List<Connection> getConnections() {
        return conns;
    }

    public List<Connection> getConns() {
        return conns;
    }

    public void setConns(List<Connection> conns) {
        this.conns.clear();
        for (Connection conn : conns) addConnection(conn);
    }

    byte[] onMessage(Connection conn, Socket s, HL7Segment msh, byte[] msg, int off,
                     int len, int mshlen) throws HL7Exception {
        if (!(isInstalled() && conns.contains(conn)))
            throw new HL7Exception(HL7Exception.AR, "Receiving Application not recognized");
        if (!(acceptedSendingApplicationsSet.isEmpty()
                || acceptedSendingApplicationsSet.contains(msh.getSendingApplicationWithFacility())))
            throw new HL7Exception(HL7Exception.AR, "Sending Application not recognized");
        if (!(acceptedMessageTypesSet.contains("*")
                || acceptedMessageTypesSet.contains(msh.getMessageType())))
            throw new HL7Exception(HL7Exception.AR, "Message Type not supported");

        HL7MessageListener listener = getHL7MessageListener();
        if (listener == null)
            throw new HL7Exception(HL7Exception.AE, "No HL7 Message Listener configured");
        return listener.onMessage(this, conn, s, msh, msg, off, len, mshlen);
    }

    public MLLPConnection connect(Connection remote)
            throws IOException, IncompatibleConnectionException, GeneralSecurityException {
        return connect(findCompatibelConnection(remote), remote);
    }

    public MLLPConnection connect(HL7Application remote)
            throws IOException, IncompatibleConnectionException, GeneralSecurityException {
        CompatibleConnection cc = findCompatibelConnection(remote);
        return connect(cc.getLocalConnection(), cc.getRemoteConnection());
    }

    public MLLPConnection connect(Connection local, Connection remote)
            throws IOException, IncompatibleConnectionException, GeneralSecurityException {
        checkDevice();
        checkInstalled();
        Socket sock = local.connect(remote);
        sock.setSoTimeout(local.getResponseTimeout());
        return new MLLPConnection(sock);
    }

    public CompatibleConnection findCompatibelConnection(HL7Application remote)
            throws IncompatibleConnectionException {
        for (Connection remoteConn : remote.conns)
            if (remoteConn.isInstalled() && remoteConn.isServer())
                for (Connection conn : conns)
                    if (conn.isInstalled() && conn.isCompatible(remoteConn))
                        return new CompatibleConnection(conn, remoteConn);
        throw new IncompatibleConnectionException(
                "No compatible connection to " + remote + " available on " + this);
    }

    public Connection findCompatibelConnection(Connection remoteConn)
            throws IncompatibleConnectionException {
        for (Connection conn : conns)
            if (conn.isInstalled() && conn.isCompatible(remoteConn))
                return conn;
        throw new IncompatibleConnectionException(
                "No compatible connection to " + remoteConn + " available on " + this);
    }

    private void checkInstalled() {
        if (!isInstalled())
            throw new IllegalStateException("Not installed");
    }

    private void checkDevice() {
        if (device == null)
            throw new IllegalStateException("Not attached to Device");
    }

    void reconfigure(HL7Application src) {
        setHL7ApplicationAttributes(src);
        device.reconfigureConnections(conns, src.conns);
        reconfigureHL7ApplicationExtensions(src);
    }

    private void reconfigureHL7ApplicationExtensions(HL7Application from) {
        for (Iterator<Class<? extends HL7ApplicationExtension>> it =
                     extensions.keySet().iterator(); it.hasNext(); ) {
            if (!from.extensions.containsKey(it.next()))
                it.remove();
        }
        for (HL7ApplicationExtension src : from.extensions.values()) {
            Class<? extends HL7ApplicationExtension> clazz = src.getClass();
            HL7ApplicationExtension ext = extensions.get(clazz);
            if (ext == null)
                try {
                    addHL7ApplicationExtension(ext = clazz.newInstance());
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to instantiate " + clazz.getName(), e);
                }
            ext.reconfigure(src);
        }
    }

    protected void setHL7ApplicationAttributes(HL7Application src) {
        setHL7DefaultCharacterSet(src.HL7DefaultCharacterSet);
        setAcceptedSendingApplications(src.getAcceptedSendingApplications());
        setAcceptedMessageTypes(src.getAcceptedMessageTypes());
        setHl7Installed(src.hl7Installed);
    }

    public void addHL7ApplicationExtension(HL7ApplicationExtension ext) {
        Class<? extends HL7ApplicationExtension> clazz = ext.getClass();
        if (extensions.containsKey(clazz))
            throw new IllegalStateException(
                    "already contains AE Extension:" + clazz);

        ext.setHL7Application(this);
        extensions.put(clazz, ext);
    }

    public boolean removeHL7ApplicationExtension(HL7ApplicationExtension ext) {
        if (extensions.remove(ext.getClass()) == null)
            return false;

        ext.setHL7Application(null);
        return true;
    }

    public Collection<HL7ApplicationExtension> listHL7ApplicationExtensions() {
        return extensions.values();
    }

    @SuppressWarnings("unchecked")
    public <T extends HL7ApplicationExtension> T getHL7ApplicationExtension(Class<T> clazz) {
        return (T) extensions.get(clazz);
    }
}
