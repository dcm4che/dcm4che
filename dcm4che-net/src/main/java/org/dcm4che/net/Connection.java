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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.dcm4che.util.SafeClose;
import org.dcm4che.util.StringUtils;
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

    private static final Logger LOG = LoggerFactory.getLogger(Connection.class);

    public static final int NOT_LISTENING = -1;
    public static final int DEF_BACKLOG = 50;
    public static final int DEF_SOCKETDELAY = 50;
    public static final int DEF_BUFFERSIZE = 0;
    public static final int NO_TIMEOUT = 0;

    private Device device;
    private String commonName;
    private String hostname;
    private int port = NOT_LISTENING;
    private int backlog = DEF_BACKLOG;
    private int connectTimeout;
    private int requestTimeout;
    private int acceptTimeout;
    private int releaseTimeout;
    private int dimseRSPTimeout;
    private int cgetRSPTimeout;
    private int cmoveRSPTimeout;
    private int idleTimeout;
    private int socketCloseDelay = DEF_SOCKETDELAY;
    private int sendBufferSize;
    private int receiveBufferSize;
    private boolean tcpNoDelay = true;
    private boolean tlsNeedClientAuth = true;
    private String[] tlsCipherSuites = {};
    private String[] tlsProtocols =  { "TLSv1", "SSLv3" };
    private String[] blacklist = {};
    private Boolean installed;

    private InetAddress addr;
    private List<InetAddress> blacklistAddrs;
    private ServerSocket server;
    private boolean needRebind;

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
    final void setDevice(Device device) {
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
        if (hostname != null ? hostname.equals(this.hostname) : this.hostname == null)
            return;

        this.hostname = hostname;
        needRebind();
    }

    private void needRebind() {
        if (isListening())
            needRebind = true;
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
     * The TCP port that the AE is listening on or <code>0</code> for a
     *          network connection that only initiates associations.
     * 
     * A valid port value is between 0 and 65535.
     * 
     * @param port
     *            The port number or <code>-1</code>.
     */
    public final void setPort(int port) {
        if (this.port == port)
            return;

        if ((port <= 0 || port > 0xFFFF) && port != NOT_LISTENING)
            throw new IllegalArgumentException("port out of range:" + port);

        this.port = port;
        needRebind();
    }

    public final boolean isServer() {
        return port > 0;
    }

    public final int getBacklog() {
        return backlog;
    }

    public final void setBacklog(int backlog) {
        if (this.backlog == backlog)
            return;

        if (backlog < 1)
            throw new IllegalArgumentException("backlog: " + backlog);

        this.backlog = backlog;
        needRebind();
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


    public int getDimseRSPTimeout(int cmdfield) {
        return (cmdfield == Commands.C_GET_RSP)
                ? cgetRSPTimeout
                : (cmdfield == Commands.C_MOVE_RSP)
                        ? cmoveRSPTimeout
                        : dimseRSPTimeout;
    }

    public final int getDimseRSPTimeout() {
        return dimseRSPTimeout;
    }

    public final void setDimseRSPTimeout(int timeout) {
        this.dimseRSPTimeout = timeout;
        if (cgetRSPTimeout == 0)
            cgetRSPTimeout = timeout;
        if (cmoveRSPTimeout == 0)
            cmoveRSPTimeout = timeout;
    }

    public final int getCGetRSPTimeout() {
        return cgetRSPTimeout;
    }

    public final void setCGetRSPTimeout(int timeout) {
        this.cgetRSPTimeout = timeout;
    }

    public final int getCMoveRSPTimeout() {
        return cmoveRSPTimeout;
    }

    public final void setCMoveRSPTimeout(int timeout) {
        this.cmoveRSPTimeout = timeout;
    }

    public final int getIdleTimeout() {
        return idleTimeout;
    }

    public final void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    /**
     * The TLS CipherSuites that are supported on this particular connection.
     * TLS CipherSuites shall be described using an RFC-2246 string
     * representation (e.g. 'SSL_RSA_WITH_3DES_EDE_CBC_SHA')
     * 
     * @return A String array containing the supported cipher suites
     */
    public String[] getTlsCipherSuites() {
        return tlsCipherSuites;
    }

    /**
     * The TLS CipherSuites that are supported on this particular connection.
     * TLS CipherSuites shall be described using an RFC-2246 string
     * representation (e.g. 'SSL_RSA_WITH_3DES_EDE_CBC_SHA')
     * 
     * @param tlsCipherSuite
     *            A String array containing the supported cipher suites
     */
    public void setTlsCipherSuites(String... tlsCipherSuites) {
        if (Arrays.equals(this.tlsCipherSuites, tlsCipherSuites))
            return;

        this.tlsCipherSuites = tlsCipherSuites;
        needRebind();
    }

    public final boolean isTls() {
        return tlsCipherSuites.length > 0;
    }

    public final String[] getTlsProtocols() {
        return tlsProtocols;
    }

    public final void setTlsProtocols(String... tlsProtocols) {
        if (Arrays.equals(this.tlsProtocols, tlsProtocols))
            return;

        this.tlsProtocols = tlsProtocols;
        needRebind();
    }

    public final boolean isTlsNeedClientAuth() {
        return tlsNeedClientAuth;
    }

    public final void setTlsNeedClientAuth(boolean tlsNeedClientAuth) {
        if (this.tlsNeedClientAuth == tlsNeedClientAuth)
            return;

        this.tlsNeedClientAuth = tlsNeedClientAuth;
        needRebind();
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

    public Boolean getInstalled() {
        return installed;
    }

    /**
     * True if the Network Connection is installed on the network. If not
     * present, information about the installed status of the Network Connection
     * is inherited from the device.
     * 
     * @param installed
     *                True if the NetworkConnection is installed on the network.
     */
    public void setInstalled(Boolean installed) throws IOException {
        if (this.installed == null
                ? installed == null 
                : this.installed.equals(installed))
            return;

        Boolean prev = this.installed;
        this.installed = installed;
        if (device != null && device.isInstalled() && device.isActivated()
                && isServer()) {
            if (isInstalled()) {
                if (server == null)
                    try {
                         bind();
                    } catch (IOException e) {
                        this.installed = prev;
                        throw e;
                    }
            } else
                unbind();
        }
    }

    void activate() throws IOException {
        if (isInstalled() && isServer() && server == null)
            bind();
    }

    /**
     * Get a list of IP addresses from which we should ignore connections.
     * Useful in an environment that utilizes a load balancer. In the case of a
     * TCP ping from a load balancing switch, we don't want to spin off a new
     * thread and try to negotiate an association.
     * 
     * @return Returns the list of IP addresses which should be ignored.
     */
    public final String[] getBlacklist() {
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
    public final void setBlacklist(String[] blacklist) {
        this.blacklist = blacklist;
        this.blacklistAddrs = null;
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder(), "").toString();
    }

    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + "  ";
        StringUtils.appendLine(sb, indent, "Connection[cn: ", commonName);
        StringUtils.appendLine(sb, indent2,"host: ", hostname);
        StringUtils.appendLine(sb, indent2,"port: ", port);
        StringUtils.appendLine(sb, indent2,"ciphers: ", Arrays.toString(tlsCipherSuites));
        StringUtils.appendLine(sb, indent2,"installed: ", getInstalled());
        return sb.append(indent).append(']');
    }

    private void setSocketSendOptions(Socket s) throws SocketException {
        int size = s.getSendBufferSize();
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

    private void setReceiveBufferSize(Socket s) throws SocketException {
        int size = s.getReceiveBufferSize();
        if (receiveBufferSize == 0) {
            receiveBufferSize = size;
        } else if (receiveBufferSize != size) {
            s.setReceiveBufferSize(receiveBufferSize);
            receiveBufferSize = s.getReceiveBufferSize();
        }
    }

    private void setReceiveBufferSize(ServerSocket ss) throws SocketException {
        int size = ss.getReceiveBufferSize();
        if (receiveBufferSize == 0) {
            receiveBufferSize = size;
        } else if (receiveBufferSize != size) {
            ss.setReceiveBufferSize(receiveBufferSize);
            receiveBufferSize = ss.getReceiveBufferSize();
        }
    }

    private InetAddress addr() throws UnknownHostException {
        if (addr == null && hostname != null)
            addr = InetAddress.getByName(hostname);
        return addr;
    }

    private List<InetAddress> blacklistAddrs() {
        if (blacklistAddrs == null) {
            blacklistAddrs = new ArrayList<InetAddress>(blacklist.length);
            for (String hostname : blacklist)
                try {
                    blacklistAddrs.add(InetAddress.getByName(hostname));
                } catch (UnknownHostException e) {
                    LOG.warn("Failed to lookup InetAddress of " + hostname, e);
                }
        }
        return blacklistAddrs;
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

    private void checkCompatible(Connection remoteConn) throws IncompatibleConnectionException {
        if (!isCompatible(remoteConn))
            throw new IncompatibleConnectionException(remoteConn.toString());
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
        if (isListening())
            throw new IllegalStateException("Already listening - " + server);
        server = isTls() ? createTLSServerSocket() : new ServerSocket();
        setReceiveBufferSize(server);
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
                            setSocketSendOptions(s);
                            new Association(null, Connection.this, s);
                        }
                    }
                } catch (Throwable e) {
                    // assume exception was raised by graceful stop of server
                }
                LOG.info("Stop listening on {}", sockAddr);
            }


        });
    }

    public final boolean isListening() {
        return server != null;
    }

    public final boolean isNeedRebind() {
        return needRebind;
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
        return blacklistAddrs().contains(ia);
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
        needRebind = false;
    }

    public Socket connect(Connection remoteConn)
            throws IOException, IncompatibleConnectionException {
        checkInstalled();
        checkCompatible(remoteConn);
        Socket s = isTls() ? createTLSSocket(remoteConn) : new Socket();
        InetSocketAddress bindPoint = getBindPoint();
        InetSocketAddress endpoint = new InetSocketAddress(
                remoteConn.getHostname(), remoteConn.getPort());
        LOG.info("Initiate connection from {} to {}", bindPoint, endpoint);
        s.bind(bindPoint);
        setReceiveBufferSize(s);
        setSocketSendOptions(s);
        s.connect(endpoint, connectTimeout);
        return s;
    }

    private Socket createTLSSocket(Connection remoteConn) throws IOException {
        SSLContext sslContext = device.getSSLContext();
        SSLSocketFactory sf = sslContext.getSocketFactory();
        SSLSocket s = (SSLSocket) sf.createSocket();
        s.setEnabledProtocols(
                intersect(remoteConn.tlsProtocols, tlsProtocols));
        s.setEnabledCipherSuites(
                intersect(remoteConn.tlsCipherSuites, tlsCipherSuites));
        return s;
    }

    public boolean isCompatible(Connection remoteConn) {
        return remoteConn.isTls() 
                ? isTls()
                        && hasCommon(remoteConn.tlsProtocols, tlsProtocols)
                        && hasCommon(remoteConn.tlsCipherSuites, tlsCipherSuites)
                : !isTls();
    }

    private boolean hasCommon(String[] ss1,  String[] ss2) {
        for (String s1 : ss1)
            for (String s2 : ss2)
                if (s1.equals(s2))
                    return true;
        return false;
    }

    private static String[] intersect(String[] ss1, String[] ss2) {
        String[] ss = new String[Math.min(ss1.length, ss2.length)];
        int len = 0;
        for (String s1 : ss1)
            for (String s2 : ss2)
                if (s1.equals(s2)) {
                    ss[len++] = s1;
                    break;
                };
        if (len == ss.length)
            return ss;

        String[] dest = new String[len];
        System.arraycopy(ss, 0, dest, 0, len);
        return dest;
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
