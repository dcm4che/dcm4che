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
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
class TCPListener implements Listener {

    private final Connection conn;
    private final TCPProtocolHandler handler;
	private final boolean proxyProtocolEnabled;
    private final ServerSocket ss;

    public TCPListener(Connection conn, TCPProtocolHandler handler)
            throws IOException, GeneralSecurityException {
        try {
        
            this.conn = conn;
            this.handler = handler;
			this.proxyProtocolEnabled = conn.isProxyProtocolEnabled();
            ss = conn.isTls() ? createTLSServerSocket(conn) : new ServerSocket();
            conn.setReceiveBufferSize(ss);
            ss.bind(conn.getBindPoint(), conn.getBacklog());
            conn.getDevice().execute(new Runnable(){
    
                @Override
                public void run() { listen(); }
            });
        
        } catch (IOException e) {
            throw new IOException("Unable to start TCPListener on "+conn.getHostname()+":"+conn.getPort(), e);
        }
    }

    private ServerSocket createTLSServerSocket(Connection conn)
            throws IOException, GeneralSecurityException {
        SSLContext sslContext = conn.getDevice().sslContext();
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket();
        ss.setEnabledProtocols(conn.getTlsProtocols());
        ss.setEnabledCipherSuites(conn.getTlsCipherSuites());
        ss.setNeedClientAuth(conn.isTlsNeedClientAuth());
        return ss;
    }

    private void listen() {
        SocketAddress sockAddr = ss.getLocalSocketAddress();
        Connection.LOG.info("Start TCP Listener on {}", sockAddr);

        try {
            while (!ss.isClosed()) {
                Connection.LOG.debug("Wait for connection on {}", sockAddr);
                Socket rawSocket = ss.accept();   // original socket from accept()

                Socket socketToUse = rawSocket;
                ProxyProtocol.Info proxyInfo = null;

                if (conn.isProxyProtocolEnabled()) {
                    try {
						Connection.LOG.info("read Proxy protocol ");
                        proxyInfo = ProxyProtocol.parse(rawSocket);
						Connection.LOG.info("Proxy read : {} ",rawSocket.getRemoteSocketAddress() );
                        socketToUse = new ProxySocket(rawSocket,
                                proxyInfo.proxiedRemote,
                                proxyInfo.receivingRemote,proxyInfo.pbInputStream);
                    } catch (Exception e) {
                        Connection.LOG.warn("Failed to parse PROXY protocol from {} - falling back to LB IP", rawSocket.getRemoteSocketAddress(), e);
                    }
                }

                ConnectionMonitor monitor = conn.getDevice() != null
                        ? conn.getDevice().getConnectionMonitor()
                        : null;

                // Blacklist check now always uses the best available client IP
                InetAddress clientAddress = socketToUse.getInetAddress();

                if (conn.isBlackListed(clientAddress)) {
                    if (monitor != null)
                        monitor.onConnectionRejectedBlacklisted(conn, socketToUse);
                    Connection.LOG.info("Reject blacklisted connection from {}", clientAddress);
                    conn.close(rawSocket);   // always close the original socket
                    continue;
                }

                // Normal processing
                try {
                    conn.setSocketSendOptions(socketToUse);
                } catch (Throwable e) {
                    if (monitor != null)
                        monitor.onConnectionRejected(conn, socketToUse, e);
                    Connection.LOG.warn("Reject connection {}:", socketToUse, e);
                    conn.close(rawSocket);
                    continue;
                }

                if (monitor != null)
                    monitor.onConnectionAccepted(conn, socketToUse);

                Connection.LOG.info("Accept connection from {}", socketToUse);

                try {
					if (proxyInfo != null) {
						handler.onAccept(conn, socketToUse, proxyInfo);
					} else {
						handler.onAccept(conn, socketToUse);
					}
                } catch (Throwable e) {
                    Connection.LOG.warn("Exception on accepted connection {}:", socketToUse, e);
                    conn.close(rawSocket);
                }
            }
        } catch (Throwable e) {
            if (!ss.isClosed())
                Connection.LOG.error("Exception on listening on {}:", sockAddr, e);
        }

        Connection.LOG.info("Stop TCP Listener on {}", sockAddr);
    }


    @Override
    public SocketAddress getEndPoint() {
        return ss.getLocalSocketAddress();
    }

    @Override
    public void close() throws IOException {
         try {
            ss.close();
        } catch (Throwable e) {
            // Ignore errors when closing the server socket.
        }
    }
}
