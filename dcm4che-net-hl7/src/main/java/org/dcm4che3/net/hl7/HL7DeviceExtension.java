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

import org.dcm4che3.hl7.HL7Exception;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.DeviceExtension;

import java.net.Socket;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class HL7DeviceExtension extends DeviceExtension {

    private static final long serialVersionUID = -411853996726542266L;

    static {
        Connection.registerTCPProtocolHandler(
                Connection.Protocol.HL7, HL7ProtocolHandler.INSTANCE);
    }

    private final LinkedHashMap<String, HL7Application> hl7apps =
            new LinkedHashMap<String, HL7Application>();

    private transient HL7MessageListener hl7MessageListener;

    @Override
    public void verifyNotUsed(Connection conn) {
        for (HL7Application app : hl7apps.values())
            if (app.getConnections().contains(conn))
                throw new IllegalStateException(conn
                        + " used by HL7 Application: "
                        + app.getApplicationName());
    }

    public void addHL7Application(HL7Application hl7App) {
        hl7App.setDevice(device);
        hl7apps.put(hl7App.getApplicationName(), hl7App);
    }

    public HL7Application removeHL7Application(String name) {
        HL7Application hl7App = hl7apps.remove(name);
        if (hl7App != null)
            hl7App.setDevice(null);

        return hl7App;
    }

    public boolean removeHL7Application(HL7Application hl7App) {
        return removeHL7Application(hl7App.getApplicationName()) != null;
    }

    public HL7Application getHL7Application(String name) {
        HL7Application hl7App = hl7apps.get(name);
        if (hl7App == null)
            hl7App = hl7apps.get("*");
        return hl7App;
    }

    public boolean containsHL7Application(String name) {
        return hl7apps.containsKey(name);
    }

    public Collection<String> getHL7ApplicationNames() {
        return hl7apps.keySet();
    }

    public Collection<HL7Application> getHL7Applications() {
        return hl7apps.values();
    }

    public final HL7MessageListener getHL7MessageListener() {
        return hl7MessageListener;
    }

    public final void setHL7MessageListener(HL7MessageListener listener) {
        this.hl7MessageListener = listener;
    }

    byte[] onMessage(Connection conn, Socket s, UnparsedHL7Message msg) throws HL7Exception {
        HL7Application hl7App = getHL7Application(msg.msh().getReceivingApplicationWithFacility());
        if (hl7App == null)
            throw new HL7Exception(HL7Exception.AR, "Receiving Application not recognized");
        return hl7App.onMessage(conn, s, msg);
    }

    @Override
    public void reconfigure(DeviceExtension from)  {
        reconfigureHL7Applications((HL7DeviceExtension) from);
    }

    private void reconfigureHL7Applications(HL7DeviceExtension from) {
        hl7apps.keySet().retainAll(from.hl7apps.keySet());
        for (HL7Application src : from.hl7apps.values()) {
            HL7Application hl7app = hl7apps.get(src.getApplicationName());
            if (hl7app == null)
                addHL7Application(hl7app = new HL7Application(src.getApplicationName()));
            hl7app.reconfigure(src);
        }
    }
}
