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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

import java.io.IOException;
import java.net.*;
import java.security.GeneralSecurityException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
class UDPListener implements Listener {

    private static final int MAX_PACKAGE_LEN = 0x10000;

    private final Connection conn;
    private final UDPProtocolHandler handler;
    private final DatagramSocket ds;

    public UDPListener(Connection conn, UDPProtocolHandler handler)
            throws IOException, GeneralSecurityException {
        this.conn = conn;
        this.handler = handler;
        try {
            ds = new DatagramSocket(conn.getBindPoint());
        } catch (BindException e) {
            throw new IOException("Cannot start UDP listener on "+conn.getBindPoint().getHostName()+":"+conn.getBindPoint().getPort(),e);
        }
        conn.setReceiveBufferSize(ds);
        conn.getDevice().execute(new Runnable(){

            @Override
            public void run() { listen(); }
        });
    }


    private void listen() {
        SocketAddress sockAddr = ds.getLocalSocketAddress();
        Connection.LOG.info("Start UDP listener on {}", sockAddr);
        byte[] data = new byte[MAX_PACKAGE_LEN];
        try {
            while (!ds.isClosed()) {
                Connection.LOG.debug("Wait for UDP datagram package on {}", sockAddr);
                DatagramPacket dp = new DatagramPacket(data, MAX_PACKAGE_LEN);
                ds.receive(dp);
                InetAddress senderAddr = dp.getAddress();
                if (conn.isBlackListed(dp.getAddress())) {
                    Connection.LOG.info(
                            "Ignore UDP datagram package received from blacklisted {}", senderAddr);
                } else {
                    Connection.LOG.info(
                            "Received UDP datagram package from {}", senderAddr);
                    try {
                        handler.onReceive(conn, dp);
                    } catch (Throwable e) {
                        Connection.LOG.warn(
                                "Exception processing UDP received from {}:", senderAddr, e);
                    }
                }
            }
        } catch (Throwable e) {
            if (!ds.isClosed()) // ignore exception caused by close()
                Connection.LOG.error("Exception on listing on {}:", sockAddr, e);
        }
        Connection.LOG.info("Stop UDP listener on {}", sockAddr);
    }


    @Override
    public SocketAddress getEndPoint()  {
        return ds.getLocalSocketAddress();
    }

    @Override
    public void close() throws IOException {
         try {
            ds.close();
        } catch (Throwable e) {
            // Ignore errors when closing the datagram socket.
        }
    }
}
