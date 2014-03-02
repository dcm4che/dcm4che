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

package org.dcm4che3.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.dcm4che3.util.Base64;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StringUtils;
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
public class Connection implements Serializable {

    private static final long serialVersionUID = -7814748788035232055L;

    public enum Protocol { DICOM, HL7, SYSLOG_TLS, SYSLOG_UDP;
        public boolean isTCP() { return this != SYSLOG_UDP; }
        public boolean isSyslog() { return this == SYSLOG_TLS || this == SYSLOG_UDP; }
    }

    public static final Logger LOG = LoggerFactory.getLogger(Connection.class);

    public static final int NO_TIMEOUT = 0;
    public static final int SYNCHRONOUS_MODE = 1;
    public static final int NOT_LISTENING = -1;
    public static final int DEF_BACKLOG = 50;
    public static final int DEF_SOCKETDELAY = 50;
    public static final int DEF_BUFFERSIZE = 0;
    public static final int DEF_MAX_PDU_LENGTH = 16378;
    // to fit into SunJSSE TLS Application Data Length 16408

    public static final String TLS_RSA_WITH_NULL_SHA = "SSL_RSA_WITH_NULL_SHA";
    public static final String TLS_RSA_WITH_3DES_EDE_CBC_SHA = "SSL_RSA_WITH_3DES_EDE_CBC_SHA";
    public static final String TLS_RSA_WITH_AES_128_CBC_SHA = "TLS_RSA_WITH_AES_128_CBC_SHA";

    private Device device;
    private String commonName;
    private String hostname;
    private String httpProxy;
    private int port = NOT_LISTENING;
    private int backlog = DEF_BACKLOG;
    private int connectTimeout;
    private int requestTimeout;
    private int acceptTimeout;
    private int releaseTimeout;
    private int responseTimeout;
    private int retrieveTimeout;
    private int idleTimeout;
    private int socketCloseDelay = DEF_SOCKETDELAY;
    private int sendBufferSize;
    private int receiveBufferSize;
    private int sendPDULength = DEF_MAX_PDU_LENGTH;
    private int receivePDULength = DEF_MAX_PDU_LENGTH;
    private int maxOpsPerformed = SYNCHRONOUS_MODE;
    private int maxOpsInvoked = SYNCHRONOUS_MODE;
    private boolean packPDV = true;
    private boolean tcpNoDelay = true;
    private boolean tlsNeedClientAuth = true;
    private String[] tlsCipherSuites = {};
    private String[] tlsProtocols =  { "TLSv1", "SSLv3" };
    private String[] blacklist = {};
    private Boolean installed;
    private Protocol protocol = Protocol.DICOM;
    private static final EnumMap<Protocol, ProtocolHandler> handlers =
            new EnumMap<Protocol, ProtocolHandler>(Protocol.class);

    private transient List<InetAddress> blacklistAddrs;
    private transient volatile ServerSocket server;
    private transient boolean rebindNeeded;

    static {
        registerProtocolHandler(Protocol.DICOM, DicomProtocolHandler.INSTANCE);
    }

    public Connection() {
    }

    public Connection(String commonName, String hostname) {
        this(commonName, hostname, NOT_LISTENING);
    }

    public Connection(String commonName, String hostname, int port) {
        this.commonName = commonName;
        this.hostname = hostname;
        this.port = port;
    }

    public static ProtocolHandler registerProtocolHandler(
            Protocol protocol, ProtocolHandler handler) {
        return handlers.put(protocol, handler);
    }

    public static ProtocolHandler unregisterProtocolHandler(
            Protocol protocol) {
        return handlers.remove(protocol);
    }

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

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        if (protocol == null)
            throw new NullPointerException();

        if (this.protocol == protocol)
            return;

        this.protocol = protocol;
        needRebind();
    }

    boolean isRebindNeeded() {
        return rebindNeeded;
    }

    void needRebind() {
        this.rebindNeeded = true;
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

    public final String getHttpProxy() {
        return httpProxy;
    }

    public final void setHttpProxy(String proxy) {
        this.httpProxy = proxy;
    }

    public final boolean useHttpProxy() {
        return httpProxy != null;
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

    public final void setResponseTimeout(int timeout) {
        this.responseTimeout = timeout;
    }

    public final int getResponseTimeout() {
        return responseTimeout;
    }

    public final int getRetrieveTimeout() {
        return retrieveTimeout;
    }

    public final void setRetrieveTimeout(int timeout) {
        this.retrieveTimeout = timeout;
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

    public final int getSendPDULength() {
        return sendPDULength;
    }

    public final void setSendPDULength(int sendPDULength) {
        this.sendPDULength = sendPDULength;
    }

    public final int getReceivePDULength() {
        return receivePDULength;
    }

    public final void setReceivePDULength(int receivePDULength) {
        this.receivePDULength = receivePDULength;
    }

    public final int getMaxOpsPerformed() {
        return maxOpsPerformed;
    }

    public final void setMaxOpsPerformed(int maxOpsPerformed) {
        this.maxOpsPerformed = maxOpsPerformed;
    }

    public final int getMaxOpsInvoked() {
        return maxOpsInvoked;
    }

    public final void setMaxOpsInvoked(int maxOpsInvoked) {
        this.maxOpsInvoked = maxOpsInvoked;
    }

    public final boolean isPackPDV() {
        return packPDV;
    }

    public final void setPackPDV(boolean packPDV) {
        this.packPDV = packPDV;
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
     * @throws GeneralSecurityException 
     */
    public void setInstalled(Boolean installed) {
        if (this.installed == installed)
            return;

        boolean prev = isInstalled();
        this.installed = installed;
        if (isInstalled() != prev)
            needRebind();
    }

    synchronized void rebind() throws IOException, GeneralSecurityException {
        unbind();
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
        return hostname != null ? InetAddress.getByName(hostname) : null;
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


    public InetSocketAddress getEndPoint() throws UnknownHostException {
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

    /**
     * Bind this network connection to a TCP port and start a server socket
     * accept loop.
     * 
     * @throws IOException
     *             If there is a problem with the network interaction.
     * @throws GeneralSecurityException 
     */
    public synchronized boolean bind() throws IOException, GeneralSecurityException {
        if (!(isInstalled() && isServer())) {
            rebindNeeded = false;
            return false;
        }
        if (device == null)
            throw new IllegalStateException("Not attached to Device");
        if (isListening())
            throw new IllegalStateException("Already listening - " + server);
        final ProtocolHandler connectionHandler = handlers.get(protocol);
        if (connectionHandler == null)
            throw new IllegalStateException("No ConnectionHandler for protocol " + protocol);
        server = isTls() ? createTLSServerSocket() : new ServerSocket();
        setReceiveBufferSize(server);
        server.bind(getEndPoint(), backlog);
        device.execute(new Runnable() {

            public void run() {
                ServerSocket tmp = server;
                SocketAddress sockAddr = tmp.getLocalSocketAddress();
                LOG.info("Start listening on {}", sockAddr);
                try {
                    for (;;) {
                        LOG.debug("Wait for connection on {}", sockAddr);
                        Socket s = server.accept();
                        if (isBlackListed(s.getInetAddress())) {
                            LOG.info("Reject connection {}", s);
                            close(s);
                        } else {
                            LOG.info("Accept connection {}", s);
                            try {
                                setSocketSendOptions(s);
                                connectionHandler.onAccept(Connection.this, s);
                            } catch (Throwable e) {
                                LOG.warn("Exception on accepted connection " + s + ":", e);
                                close(s);
                            }
                        }
                    }
                } catch (Throwable e) {
                    if (server == tmp) // ignore exception caused by unbind()
                        LOG.error("Exception on listing on " + sockAddr + ":", e);
                }
                LOG.info("Stop listening on {}", sockAddr);
            }
        });
        rebindNeeded = false;
        return true;
    }

    public final boolean isListening() {
        return server != null;
    }

    private ServerSocket createTLSServerSocket() throws IOException, GeneralSecurityException {
        SSLContext sslContext = device.sslContext();
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
        ServerSocket tmp = server;
        if (tmp == null)
            return;
        server = null;
        try {
            tmp.close();
        } catch (Throwable e) {
            // Ignore errors when closing the server socket.
        }
    }

    public Socket connect(Connection remoteConn)
            throws IOException, IncompatibleConnectionException, GeneralSecurityException {
        checkInstalled();
        if (!protocol.isTCP())
            throw new IllegalStateException("Not a TCP Connection");
        checkCompatible(remoteConn);
        InetSocketAddress bindPoint = getBindPoint();
        String remoteHostname = remoteConn.getHostname();
        int remotePort = remoteConn.getPort();
        LOG.info("Initiate connection from {} to {}:{}",
                new Object[] {bindPoint, remoteHostname, remotePort});
        Socket s = new Socket();
        s.bind(bindPoint);
        setReceiveBufferSize(s);
        setSocketSendOptions(s);
        String remoteProxy = remoteConn.getHttpProxy();
        if (remoteProxy != null) {
            String userauth = null;
            String[] ss = StringUtils.split(remoteProxy, '@');
            if (ss.length > 1) {
                userauth = ss[0];
                remoteProxy = ss[1];
            }
            ss = StringUtils.split(remoteProxy, ':');
            int proxyPort = ss.length > 1 ? Integer.parseInt(ss[1]) : 8080;
            s.connect(new InetSocketAddress(ss[0], proxyPort), connectTimeout);
            try {
                doProxyHandshake(s, remoteHostname, remotePort, userauth,
                        connectTimeout);
            } catch (IOException e) {
                SafeClose.close(s);
                throw e;
            }
        } else {
            s.connect(new InetSocketAddress(remoteHostname, remotePort),
                    connectTimeout);
        }
        if (isTls())
            try {
                s = createTLSSocket(s, remoteConn);
            } catch (GeneralSecurityException e) {
                SafeClose.close(s);
                throw e;
            } catch (IOException e) {
                SafeClose.close(s);
                throw e;
            }
        LOG.info("Established connection {}", s);
        return s;
    }

    public DatagramSocket createDatagramSocket() throws IOException {
        checkInstalled();
        if (protocol.isTCP())
            throw new IllegalStateException("Not a UDP Connection");

        DatagramSocket ds = new DatagramSocket(getBindPoint());
        int size = ds.getSendBufferSize();
        if (sendBufferSize == 0) {
            sendBufferSize = size;
        } else if (sendBufferSize != size) {
            ds.setSendBufferSize(sendBufferSize);
            sendBufferSize = ds.getSendBufferSize();
        }
        return ds;
    }

    private void doProxyHandshake(Socket s, String hostname, int port,
            String userauth, int connectTimeout) throws IOException {

        StringBuilder request = new StringBuilder(128);
        request.append("CONNECT ")
          .append(hostname).append(':').append(port)
          .append(" HTTP/1.1\r\nHost: ")
          .append(hostname).append(':').append(port);
        if (userauth != null) {
           byte[] b = userauth.getBytes("UTF-8");
           char[] base64 = new char[(b.length + 2) / 3 * 4];
           Base64.encode(b, 0, b.length, base64, 0);
           request.append("\r\nProxy-Authorization: basic ")
               .append(base64);
        }
        request.append("\r\n\r\n");
        OutputStream out = s.getOutputStream();
        out.write(request.toString().getBytes("US-ASCII"));
        out.flush();

        s.setSoTimeout(connectTimeout);
        @SuppressWarnings("resource")
        String response = new HTTPResponse(s).toString();
        s.setSoTimeout(0);
        if (!response.startsWith("HTTP/1.1 2"))
            throw new IOException("Unable to tunnel through " + s
                    + ". Proxy returns \"" + response + '\"');
    }

    private static class HTTPResponse extends ByteArrayOutputStream {

        private final String rsp;

        public HTTPResponse(Socket s) throws IOException {
            super(64);
            InputStream in = s.getInputStream();
            boolean eol = false;
            int b;
            while ((b = in.read()) != -1) {
                write(b);
                if (b == '\n') {
                    if (eol) {
                        rsp = new String(super.buf, 0, super.count, "US-ASCII");
                        return;
                    }
                    eol = true;
                } else if (b != '\r') {
                    eol = false;
                }
            }
            throw new IOException("Unexpected EOF from " + s);
        }

        @Override
        public String toString() {
            return rsp;
        }
    }

    private SSLSocket createTLSSocket(Socket s, Connection remoteConn)
            throws GeneralSecurityException, IOException {
        SSLContext sslContext = device.sslContext();
        SSLSocketFactory sf = sslContext.getSocketFactory();
        SSLSocket ssl = (SSLSocket) sf.createSocket(s,
                remoteConn.getHostname(), remoteConn.getPort(), true);
        ssl.setEnabledProtocols(
                intersect(remoteConn.tlsProtocols, tlsProtocols));
        ssl.setEnabledCipherSuites(
                intersect(remoteConn.tlsCipherSuites, tlsCipherSuites));
        ssl.startHandshake();
        return ssl;
    }

    public void close(Socket s) {
        LOG.info("Close connection {}", s);
        SafeClose.close(s);
    }

    public boolean isCompatible(Connection remoteConn) {
        if (remoteConn.protocol != protocol)
            return false;
        
        if (!protocol.isTCP())
            return true;
        
        if (!isTls())
            return !remoteConn.isTls();
        
        return hasCommon(remoteConn.tlsProtocols, tlsProtocols)
            && hasCommon(remoteConn.tlsCipherSuites, tlsCipherSuites);
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

    boolean equalsRDN(Connection other) {
        return commonName != null
                ? commonName.equals(other.commonName)
                : other.commonName == null
                    && hostname.equals(other.hostname)
                    && port == other.port
                    && protocol == other.protocol;
    }

    void reconfigure(Connection from) {
        setCommonName(from.commonName);
        setHostname(from.hostname);
        setPort(from.port);
        setProtocol(from.protocol);
        setHttpProxy(from.httpProxy);
        setBacklog(from.backlog);
        setConnectTimeout(from.connectTimeout);
        setRequestTimeout(from.requestTimeout);
        setAcceptTimeout(from.acceptTimeout);
        setReleaseTimeout(from.releaseTimeout);
        setResponseTimeout(from.responseTimeout);
        setRetrieveTimeout(from.retrieveTimeout);
        setIdleTimeout(from.idleTimeout);
        setSocketCloseDelay(from.socketCloseDelay);
        setSendBufferSize(from.sendBufferSize);
        setReceiveBufferSize(from.receiveBufferSize);
        setSendPDULength(from.sendPDULength);
        setReceivePDULength(from.receivePDULength);
        setMaxOpsPerformed(from.maxOpsPerformed);
        setMaxOpsPerformed(from.maxOpsInvoked);
        setPackPDV(from.packPDV);
        setTcpNoDelay(from.tcpNoDelay);
        setTlsNeedClientAuth(from.tlsNeedClientAuth);
        setTlsCipherSuites(from.tlsCipherSuites);
        setTlsProtocols(from.tlsProtocols);
        setBlacklist(from.blacklist);
        setInstalled(from.installed);
    }

}
