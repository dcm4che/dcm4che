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

package org.dcm4che.net.hl7;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.dcm4che.hl7.HL7Exception;
import org.dcm4che.hl7.HL7Segment;
import org.dcm4che.hl7.MLLPConnection;
import org.dcm4che.net.CompatibleConnection;
import org.dcm4che.net.Connection;
import org.dcm4che.net.IncompatibleConnectionException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class HL7Application {

    private HL7Device device;
    private String name;
    private String hl7DefaultCharacterSet;
    private Boolean installed;

    private final LinkedHashSet<String> acceptedSendingApplications =
            new LinkedHashSet<String>();
    private final LinkedHashSet<String> acceptedMessageTypes =
            new LinkedHashSet<String>();
    private final List<Connection> conns = new ArrayList<Connection>(1);
    private HL7MessageListener hl7MessageListener;

    public HL7Application(String name) {
        setApplicationName(name);
    }

    public final HL7Device getDevice() {
        return device;
    }

    void setDevice(HL7Device device) {
        if (device != null  && this.device != null)
            throw new IllegalStateException("already owned by " + this.device);
        this.device = device;
    }

    public String getApplicationName() {
        return name;
    }

    public void setApplicationName(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("name cannot be empty");
        HL7Device device = this.device;
        if (device != null)
            device.removeHL7Application(this.name);
        this.name = name;
        if (device != null)
            device.addHL7Application(this);
    }

    public final String getHL7DefaultCharacterSet() {
        return hl7DefaultCharacterSet;
    }

    public final void setHL7DefaultCharacterSet(String hl7DefaultCharacterSet) {
        this.hl7DefaultCharacterSet = hl7DefaultCharacterSet;
    }

    public String[] getAcceptedSendingApplications() {
        return acceptedSendingApplications.toArray(
                new String[acceptedSendingApplications.size()]);
    }

    public void setAcceptedSendingApplications(String... names) {
        acceptedSendingApplications.clear();
        for (String name : names)
            acceptedSendingApplications.add(name);
    }

    public String[] getAcceptedMessageTypes() {
        return acceptedMessageTypes.toArray(
                new String[acceptedMessageTypes.size()]);
    }

    public void setAcceptedMessageTypes(String... names) {
        acceptedMessageTypes.clear();
        for (String name : names)
            acceptedMessageTypes.add(name);
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

    public HL7MessageListener getHL7MessageListener() {
        HL7MessageListener listener = hl7MessageListener;
        if (listener != null)
            return listener;

        HL7Device device = this.device;
        return device != null
                ? device.getHL7MessageListener()
                : null;
    }

    public final void setHL7MessageListener(HL7MessageListener listener) {
        this.hl7MessageListener = listener;
    }

    public void addConnection(Connection conn) {
        conn.setConnectionHandler(HL7ConnectionHandler.INSTANCE);
        conns.add(conn);
    }

    public boolean removeConnection(Connection conn) {
        return conns.remove(conn);
    }

    public List<Connection> getConnections() {
        return conns;
    }

    byte[] onMessage(HL7Segment msh, byte[] msg, int off, int len, int mshlen, Connection conn)
            throws HL7Exception {
        if (!(isInstalled() && conns.contains(conn)))
            throw new HL7Exception(HL7Exception.AR, "Receiving Application not recognized");
        if (!(acceptedSendingApplications.isEmpty()
                || acceptedSendingApplications.contains(msh.getSendingApplicationWithFacility())))
            throw new HL7Exception(HL7Exception.AR, "Sending Application not recognized");
        if (!(acceptedMessageTypes.contains("*")
                || acceptedMessageTypes.contains(msh.getMessageType())))
            throw new HL7Exception(HL7Exception.AR, "Message Type not supported");

        HL7MessageListener listener = getHL7MessageListener();
        if (listener == null)
            throw new HL7Exception(HL7Exception.AE, "No HL7 Message Listener configured");
        return listener.onMessage(this, msh, msg, off, len, mshlen);
    }

    public MLLPConnection connect(Connection remote)
            throws IOException, IncompatibleConnectionException {
        return connect(findCompatibelConnection(remote), remote);
    }

    public MLLPConnection connect(HL7Application remote)
            throws IOException, IncompatibleConnectionException {
        CompatibleConnection cc = findCompatibelConnection(remote);
        return connect(cc.getLocalConnection(), cc.getRemoteConnection());
    }

    public MLLPConnection connect(Connection local, Connection remote)
            throws IOException, IncompatibleConnectionException {
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
}
