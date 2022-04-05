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

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import org.dcm4che3.hl7.*;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.TCPProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
enum HL7ProtocolHandler implements TCPProtocolHandler {
    INSTANCE;

    private static Logger LOG = LoggerFactory.getLogger(HL7ProtocolHandler.class);

    @Override
    public void onAccept(Connection conn, Socket s) throws IOException {
        conn.getDevice().execute(new HL7Receiver(conn, s));
    }

    private static class HL7Receiver implements Runnable {

        final Connection conn;
        final Socket s;
        final HL7DeviceExtension hl7dev;

        HL7Receiver(Connection conn, Socket s) {
            this.conn = conn;
            this.s = s;
            this.hl7dev = conn.getDevice().getDeviceExtensionNotNull(HL7DeviceExtension.class);
        }

        public void run() {
            int messageCount = 0;
            try {
                s.setSoTimeout(conn.getIdleTimeout());
                MLLPConnection mllp = new MLLPConnection(s,
                        conn.getProtocol() == Connection.Protocol.HL7_MLLP2 ? MLLPRelease.MLLP2 : MLLPRelease.MLLP1);
                byte[] data;
                while ((data = mllp.readMessage()) != null) {
                    messageCount++;
                    HL7ConnectionMonitor monitor = hl7dev.getHL7ConnectionMonitor();
                    UnparsedHL7Message msg = new UnparsedHL7Message(data);
                    if (monitor != null)
                        monitor.onMessageReceived(conn, s, msg);
                    UnparsedHL7Message rsp;
                    try {
                        rsp = hl7dev.onMessage(conn, s, msg);
                        if (monitor != null)
                            monitor.onMessageProcessed(conn, s, msg, rsp, null);
                    } catch (HL7Exception e) {
                        LOG.info("{}: failed to process {}:\n", s, msg, e);
                        rsp = new UnparsedHL7Message(
                                HL7Message.makeACK(msg.msh(), e).getBytes(null));
                        if (monitor != null)
                            monitor.onMessageProcessed(conn, s, msg, rsp, e);
                    }
                    mllp.writeMessage(rsp.data());
                }
            } catch (IOException e) {
                if (e instanceof SocketException && messageCount == 0)
                    LOG.info("Exception on accepted connection {}: {}", s, e.toString());
                else
                    LOG.warn("Exception on accepted connection {}:", s, e);
            } finally {
                conn.close(s);
            }
        }
    }
}
