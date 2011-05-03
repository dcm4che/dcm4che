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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.dcm4che.util.SafeClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DICOM Part 15, Annex H compliant class, <code>NetworkConnection</code>
 * encapsulates the properties associated with a connection to a TCP/IP network.
 * <p>
 * The <i>network connection</i> describes one TCP port on one network device.
 * This can be used for a TCP connection over which a DICOM association can be
 * negotiated with one or more Network AEs. It specifies 8 the hostname and TCP
 * port number. A network connection may support multiple Network AEs. The
 * Network AE selection takes place during association negotiation based on the
 * called and calling AE-titles.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Connection {

    private static Logger LOG = LoggerFactory.getLogger(Connection.class);
    
    private static final String[] DEF_TLS_PROTOCOLS = { "TLSv1", "SSLv3" };

    private Device device;
    private String commonName;
    private String hostname;
    private int port = -1;
    private int backlog = 50;
    private int connectTimeout;
    private int requestTimeout;
    private int acceptTimeout;
    private int releaseTimeout;
    private int dimseRSPTimeout;
    private int retrieveRSPTimeout;
    private int idleTimeout;
    private int checkForStalenessPeriod;
    private int socketCloseDelay = 50;
    private int sendBufferSize;
    private int receiveBufferSize;
    private boolean tcpNoDelay = true;
    private boolean tlsNeedClientAuth = true;
    private String[] tlsCipherSuites = {};
    private String[] tlsProtocols = DEF_TLS_PROTOCOLS;
    private Boolean installed;
    private Collection<InetAddress> blacklist;

    private InetAddress addr;
    private ServerSocket server;

     /**
     * Get the <code>Device</code> object that this Network Connection belongs
     * to.
     * 
     * @return Device
     */
    public final Device getDevice() {
        return device;
    }

    /**
     * Set the <code>Device</code> object that this Network Connection belongs
     * to.
     * 
     * @param device
     *            The owning <code>Device</code> object.
     */
    public final void setDevice(Device device) {
        if (device != null && this.device != null)
            throw new IllegalStateException("already owned by " + device);
        this.device = device;
    }

    /**
     * This is the DNS name for this particular connection. This is used to
     * obtain the current IP address for connections. Hostname must be
     * sufficiently qualified to be unambiguous for any client DNS user.
     * 
     * @return A String containing the host name.
     */
    public final String getHostname() {
        return hostname;
    }

    /**
     * This is the DNS name for this particular connection. This is used to
     * obtain the current IP address for connections. Hostname must be
     * sufficiently qualified to be unambiguous for any client DNS user.
     * 
     * @param hostname
     *            A String containing the host name.
     */
    public final void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * An arbitrary name for the Network Connections object. Can be a meaningful
     * name or any unique sequence of characters.
     * 
     * @return A String containing the name.
     */
    public final String getCommonName() {
        return commonName;
    }

    /**
     * An arbitrary name for the Network Connections object. Can be a meaningful
     * name or any unique sequence of characters.
     * 
     * @param name
     *            A String containing the name.
     */
    public final void setCommonName(String name) {
        this.commonName = name;
    }

    /**
     * The TCP port that the AE is listening on or <code>-1</code> for a
     *          network connection that only initiates associations.
     * 
     * @return An int containing the port number or <code>-1</code>.
     */
    public final int getPort() {
        return port;
    }

    /**
     * The TCP port that the AE is listening on or <code>-1</code> for a
     *          network connection that only initiates associations.
     * 
     * A valid port value is between 1 and 65535.
     * 
     * @param port
     *            The port number or <code>-1</code>.
     */
    public final void setPort(int port) {
        if (port != -1 && port <= 0 || port > 0xFFFF)
            throw new IllegalArgumentException("port out of range:" + port);

        this.port = port;
    }

    public final boolean isServer() {
        return port > 0;
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


    /**
     * Timeout in ms for receiving A-RELEASE-RP, 5000 by default.
     * 
     * @return An int value containing the milliseconds.
     */
    public final int getReleaseTimeout() {
        return releaseTimeout;
    }

    /**
     * Timeout in ms for receiving A-RELEASE-RP, 5000 by default.
     * 
     * @param timeout
     *            An int value containing the milliseconds.
     */
    public final void setReleaseTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.releaseTimeout = timeout;
    }

    /**
     * Delay in ms for Socket close after sending A-ABORT, 50ms by default.
     * 
     * @return An int value containing the milliseconds.
     */
    public final int getSocketCloseDelay() {
        return socketCloseDelay;
    }

    /**
     * Delay in ms for Socket close after sending A-ABORT, 50ms by default.
     * 
     * @param delay
     *            An int value containing the milliseconds.
     */
    public final void setSocketCloseDelay(int delay) {
        if (delay < 0)
            throw new IllegalArgumentException("delay: " + delay);
        this.socketCloseDelay = delay;
    }

    public final int getDimseRSPTimeout() {
        return dimseRSPTimeout;
    }

    public final void setDimseRSPTimeout(int dimseRSPTimeout) {
        this.dimseRSPTimeout = dimseRSPTimeout;
    }

    public final int getRetrieveRSPTimeout() {
        return retrieveRSPTimeout;
    }

    public final void setRetrieveRSPTimeout(int retrieveRSPTimeout) {
        this.retrieveRSPTimeout = retrieveRSPTimeout;
    }

    public final int getIdleTimeout() {
        return idleTimeout;
    }

    public final void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public final int getCheckForStalenessPeriod() {
        return checkForStalenessPeriod;
    }

    public final void setCheckForStalenessPeriod(int checkForStalenessPeriod) {
        this.checkForStalenessPeriod = checkForStalenessPeriod;
    }

    /**
     * The TLS CipherSuites that are supported on this particular connection.
     * TLS CipherSuites shall be described using an RFC-2246 string
     * representation (e.g. 'SSL_RSA_WITH_3DES_EDE_CBC_SHA')
     * 
     * @return A String array containing the supported cipher suites
     */
    public String[] getTLSCipherSuite() {
        return tlsCipherSuites.clone();
    }

    /**
     * The TLS CipherSuites that are supported on this particular connection.
     * TLS CipherSuites shall be described using an RFC-2246 string
     * representation (e.g. 'SSL_RSA_WITH_3DES_EDE_CBC_SHA')
     * 
     * @param tlsCipherSuite
     *            A String array containing the supported cipher suites
     */
    public void setTLSCipherSuite(String... tlsCipherSuite) {
        for (String s : tlsCipherSuite)
            if (s == null)
                throw new NullPointerException();
        this.tlsCipherSuites = tlsCipherSuite.clone();
    }

    public final boolean isTLS() {
        return tlsCipherSuites.length > 0;
    }

    public String[] getTLSProtocols() {
        return tlsProtocols.clone();
    }

    public void setTLSProtocol(String... tlsProtocols) {
        if (tlsProtocols.length == 0)
            throw new IllegalArgumentException("no TLS protocol specified");
        for (String s : tlsProtocols)
            if (s == null)
                throw new NullPointerException();
        this.tlsProtocols = tlsProtocols.clone();
    }

    public final boolean isTLSNeedClientAuth() {
        return tlsNeedClientAuth;
    }

    public final void setTLSNeedClientAuth(boolean tlsNeedClientAuth) {
        this.tlsNeedClientAuth = tlsNeedClientAuth;
    }

    /**
     * Get the SO_RCVBUF socket value in KB.
     * 
     * @return An int value containing the buffer size in KB.
     */
    public final int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    /**
     * Set the SO_RCVBUF socket option to specified value in KB.
     * 
     * @param bufferSize
     *            An int value containing the buffer size in KB.
     */
    public final void setReceiveBufferSize(int size) {
        if (size < 0)
            throw new IllegalArgumentException("size: " + size);
        this.receiveBufferSize = size;
    }

    /**
     * Get the SO_SNDBUF socket option value in KB,
     * 
     * @return An int value containing the buffer size in KB.
     */
    public final int getSendBufferSize() {
        return sendBufferSize;
    }

    /**
     * Set the SO_SNDBUF socket option to specified value in KB,
     * 
     * @param bufferSize
     *            An int value containing the buffer size in KB.
     */
    public final void setSendBufferSize(int size) {
        if (size < 0)
            throw new IllegalArgumentException("size: " + size);
        this.sendBufferSize = size;
    }

    /**
     * Determine if this network connection is using Nagle's algorithm as part
     * of its network communication.
     * 
     * @return boolean True if TCP no delay (disable Nagle's algorithm) is used.
     */
    public final boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    /**
     * Set whether or not this network connection should use Nagle's algorithm
     * as part of its network communication.
     * 
     * @param tcpNoDelay
     *            boolean True if TCP no delay (disable Nagle's algorithm)
     *            should be used.
     */
    public final void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    /**
     * True if the Network Connection is installed on the network. If not
     * present, information about the installed status of the Network Connection
     * is inherited from the device.
     * 
     * @return boolean True if the NetworkConnection is installed on the
     *         network.
     */
    public boolean isInstalled() {
        return device != null && device.isInstalled() 
                && (installed == null || installed.booleanValue());
    }

    /**
     * True if the Network Connection is installed on the network. If not
     * present, information about the installed status of the Network Connection
     * is inherited from the device.
     * 
     * @param installed
     *                True if the NetworkConnection is installed on the network.
     */
    public void setInstalled(Boolean installed) {
        this.installed = installed;
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
        sb.append("Connection[")
            .append(hostname)
            .append(':')
            .append(port);
        if (isTLS()) {
            sb.append(", tls=[");
            for (String s : tlsCipherSuites)
                sb.append(s).append(", ");
            sb.setLength(sb.length() - 2);
            sb.append(']');
        }
        if (commonName != null)
            sb.append(", cn=").append(commonName);
        sb.append(']');
        return sb.toString();
    }

    /**
     * Set options on a socket that was either just accepted (if this network
     * connection is an SCP), or just created (if this network connection is an
     * SCU).
     * 
     * @param s
     *            The <code>Socket</code> object.
     * @throws SocketException
     *             If the options cannot be set on the socket.
     */
    private void setSocketOptions(Socket s) throws SocketException {
        int size;
        size = s.getReceiveBufferSize();
        if (receiveBufferSize == 0) {
            receiveBufferSize = size;
        } else if (receiveBufferSize != size) {
            s.setReceiveBufferSize(receiveBufferSize);
            receiveBufferSize = s.getReceiveBufferSize();
        }
        size = s.getSendBufferSize();
        if (sendBufferSize == 0) {
            sendBufferSize = size;
        } else if (sendBufferSize != size) {
            s.setSendBufferSize(sendBufferSize);
            sendBufferSize = s.getSendBufferSize();
        }
        if (s.getTcpNoDelay() != tcpNoDelay) {
            s.setTcpNoDelay(tcpNoDelay);
        }
    }

    private InetAddress addr() throws UnknownHostException {
        if (addr == null && hostname != null)
            addr = InetAddress.getByName(hostname);
        return addr;
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

    private void checkInstalled() {
        if (!isInstalled())
            throw new IllegalStateException("Not installed");
    }

    private void checkDevice() {
        if (device == null)
            throw new IllegalStateException("Not attached to Device");
    }

    /**
     * Bind this network connection to a TCP port and start a server socket
     * accept loop.
     * 
     * @throws IOException
     *             If there is a problem with the network interaction.
     */
    public synchronized void bind() throws IOException {
        checkDevice();
        checkInstalled();
        if (!isServer())
            throw new IllegalStateException("Does not accept connections");
        if (server != null)
            throw new IllegalStateException("Already listening - " + server);
        server = isTLS() ? createTLSServerSocket() : new ServerSocket();
        server.bind(getEndPoint(), backlog);
        device.execute(new Runnable() {

            public void run() {
                SocketAddress sockAddr = server.getLocalSocketAddress();
                LOG.info("Start listening on {}", sockAddr);
                try {
                    for (;;) {
                        LOG.debug("Wait for connection on {}", sockAddr);
                        Socket s = server.accept();
                        if (isBlackListed(s.getInetAddress())) {
                            LOG.info("Reject connection from {}", s);
                            SafeClose.close(s);
                        } else {
                            LOG.info("Accept connection from {}", s);
                            setSocketOptions(s);
                            Association as =
                                    new Association(null, Connection.this, s);
                            as.activate();
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
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket();
        ss.setEnabledProtocols(tlsProtocols);
        ss.setEnabledCipherSuites(tlsCipherSuites);
        ss.setNeedClientAuth(tlsNeedClientAuth);
        return ss;
    }

    private boolean isBlackListed(InetAddress ia) {
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

    /**
     * Create a socket as an SCU and connect to a peer network connection (the
     * SCP).
     * 
     * @param peerConfig
     *            The peer <code>NetworkConnection</code> object that this
     *            network connection is connecting to.
     * @return Socket The created socket object.
     * @throws IOException
     *             If the connection cannot be made due to network IO reasons.
     */
    public Socket connect(String host, int port) throws IOException {
        checkInstalled();
        Socket s = isTLS() ? createTLSSocket() : new Socket();
        InetSocketAddress bindPoint = getBindPoint();
        InetSocketAddress endpoint = new InetSocketAddress(host, port);
        LOG.info("Initiate connection from {} to {}", bindPoint, endpoint);
        s.bind(bindPoint);
        setSocketOptions(s);
        s.connect(endpoint, connectTimeout);
        return s;
    }

    private Socket createTLSSocket() throws IOException {
        SSLContext sslContext = device.getSSLContext();
        SSLSocketFactory sf = sslContext.getSocketFactory();
        SSLSocket s = (SSLSocket) sf.createSocket();
        s.setEnabledProtocols(tlsProtocols);
        s.setEnabledCipherSuites(tlsCipherSuites);
        return s;
    }

    private InetSocketAddress getBindPoint() throws UnknownHostException {
        // don't use loopback address as bind point to avoid
        // ConnectionException connection to remote endpoint
        return new InetSocketAddress(maskLoopBackAddress(addr()), 0);
    }

    private static InetAddress maskLoopBackAddress(InetAddress addr) {
        return addr != null && addr.isLoopbackAddress() ? null : addr;
    }
}
