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
 * Portions created by the Initial Developer are Copyright (C) 2011
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

package org.dcm4che.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class InboundConnection extends AbstractConnection {

    private static Logger LOG =
        LoggerFactory.getLogger(InboundConnection.class);

    private int port;
    private int backlog = 50;
    private int connectTimeout;
    private int requestTimeout;
    private int acceptTimeout;
    private boolean tlsNeedClientAuth = true;
    private Collection<InetAddress> blacklist;

    private ServerSocket server;

    /**
     * The TCP port that the AE is listening on. (This may be missing for a
     * network connection that only initiates associations.)
     * 
     * @return An int containing the port number.
     */
    public final int getPort() {
        return port;
    }

    /**
     * The TCP port that the AE is listening on.
     * 
     * A valid port value is between 0 and 65535.
     * 
     * A port number of <code>zero</code> will let the system pick up an
     * ephemeral port.
     * 
     * @param port
     *            The port number
     */
    public final void setPort(int port) {
        if (port < 0 || port > 0xFFFF) {
            throw new IllegalArgumentException("port out of range:" + port);
        }
        this.port = port;
    }

    public final int getBacklog() {
        return backlog;
    }

    public final void setBacklog(int backlog) {
        if (backlog < 1)
            throw new IllegalArgumentException("backlog: " + backlog);
        this.backlog = backlog;
    }

    public final int getConnectTimeout() {
        return connectTimeout;
    }

    public final void setConnectTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.connectTimeout = timeout;
    }

    /**
     * Timeout in ms for receiving A-ASSOCIATE-RQ, 5000 by default
     * 
     * @param An
     *            int value containing the milliseconds.
     */
    public final int getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Timeout in ms for receiving A-ASSOCIATE-RQ, 5000 by default
     * 
     * @param timeout
     *            An int value containing the milliseconds.
     */
    public final void setRequestTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.requestTimeout = timeout;
    }

    public final int getAcceptTimeout() {
        return acceptTimeout;
    }

    public final void setAcceptTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.acceptTimeout = timeout;
    }

    public final boolean isTlsNeedClientAuth() {
        return tlsNeedClientAuth;
    }

    public final void setTlsNeedClientAuth(boolean tlsNeedClientAuth) {
        this.tlsNeedClientAuth = tlsNeedClientAuth;
    }

    /**
     * Get a list of IP addresses from which we should ignore connections.
     * Useful in an environment that utilizes a load balancer. In the case of a
     * TCP ping from a load balancing switch, we don't want to spin off a new
     * thread and try to negotiate an association.
     * 
     * @return Returns the list of IP addresses which should be ignored.
     */
    public final Collection<InetAddress> getBlacklist() {
        return blacklist;
    }

    /**
     * Set a list of IP addresses from which we should ignore connections.
     * Useful in an environment that utilizes a load balancer. In the case of a
     * TCP ping from a load balancing switch, we don't want to spin off a new
     * thread and try to negotiate an association.
     * 
     * @param blacklist
     *            the list of IP addresses which should be ignored.
     */
    public final void setBlacklist(Collection<InetAddress> blacklist) {
        this.blacklist = blacklist;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InboundConnection[")
            .append(hostname)
            .append(':')
            .append(port);
        appendTLS(sb);
        appendCN(sb);
        sb.append(']');
        return sb.toString();
    }

    InetSocketAddress getEndPoint() throws UnknownHostException {
        return new InetSocketAddress(addr(), port);
    }

    /**
     * Returns server socket associated with this Network Connection, bound to
     * the TCP port, listening for connect requests. Returns <code>null</code>
     * if this network connection only initiates associations or was not yet
     * bound by {@link #bind}.
     * 
     * @return server socket associated with this Network Connection or
     *         <code>null</code>
     */
    public ServerSocket getServer() {
        return server;
    }

    /**
     * Bind this network connection to a TCP port and start a server socket
     * accept loop.
     * 
     * @param executor
     *            The <code>Executor</code> implementation that association
     *            threads should run within. The executor determines the
     *            threading model.
     * @throws IOException
     *             If there is a problem with the network interaction.
     */
    public synchronized void bind(final Executor executor) throws IOException {
        if (device == null)
            throw new IllegalStateException("Device not initalized");
        if (server != null)
            throw new IllegalStateException("Already listening - " + server);
        server = isTLS() ? createTLSServerSocket() : new ServerSocket();
        server.bind(getEndPoint(), backlog);
        executor.execute(new Runnable() {

            public void run() {
                SocketAddress sockAddr = server.getLocalSocketAddress();
                LOG.info("Start listening on {}", sockAddr);
                try {
                    for (;;) {
                        LOG.debug("Wait for connection on {}", sockAddr);
                        Socket s = server.accept();
                        if (isBlocked(s.getInetAddress())) {
                            LOG.info("Reject connection from {}", s);
                            try { s.close(); } catch (IOException ignore) {}
                        } else {
                            LOG.info("Accept connection from {}", s);
                            setSocketOptions(s);
                            Association.accept(InboundConnection.this, s, executor);
                        }
                    }
                } catch (Throwable e) {
                    // assume exception was raised by graceful stop of server
                }
                LOG.info("Stop listening on {}", sockAddr);
            }


        });
    }

    private ServerSocket createTLSServerSocket() throws IOException {
        SSLContext sslContext = device.getSSLContext();
        if (sslContext == null)
            throw new IllegalStateException("TLS Context not initialized!");
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket();
        ss.setEnabledProtocols(tlsProtocol);
        ss.setEnabledCipherSuites(tlsCipherSuite);
        ss.setNeedClientAuth(tlsNeedClientAuth);
        return ss;
    }

    private boolean isBlocked(InetAddress ia) {
        return blacklist != null && blacklist.contains(ia);
    }

    public synchronized void unbind() {
        if (server == null)
            return;
        try {
            server.close();
        } catch (Throwable e) {
            // Ignore errors when closing the server socket.
        }
        server = null;
    }

}
