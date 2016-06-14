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

package org.dcm4che3.net.hl7.service;

import org.dcm4che3.hl7.HL7Exception;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.hl7.DefaultHL7MessageListener;
import org.dcm4che3.net.hl7.HL7Application;
import org.dcm4che3.net.hl7.HL7MessageListener;
import org.dcm4che3.net.hl7.UnparsedHL7Message;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class HL7ServiceRegistry extends DefaultHL7MessageListener {

    private final ArrayList<HL7Service> services = new ArrayList<HL7Service>();
    private final HashMap<String,HL7MessageListener> listeners =
            new HashMap<String,HL7MessageListener>();

    public synchronized void addHL7Service(HL7Service service) {
        services.add(service);
        for (String messageType : service.getMessageTypes())
            listeners.put(messageType, service);
    }

    public synchronized boolean removeHL7Service(HL7Service service) {
        if (!services.remove(service))
            return false;
        
        for (String messageType : service.getMessageTypes())
            listeners.remove(messageType);

        return true;
    }

    @Override
    public byte[] onMessage(HL7Application hl7App, Connection conn, Socket s, UnparsedHL7Message msg)
            throws HL7Exception {
        HL7MessageListener listener = listeners.get(msg.msh().getMessageType());
        if (listener == null) {
            listener = listeners.get("*");
            if (listener == null)
                return super.onMessage(hl7App, conn, s, msg);
        }
        return  listener.onMessage(hl7App, conn, s, msg);
    }
 
}
