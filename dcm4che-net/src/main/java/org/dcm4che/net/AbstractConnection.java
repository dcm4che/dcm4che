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

import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

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
abstract class AbstractConnection {
    
    private static String[] TLS_WO_SSLv2 = { "TLSv1", "SSLv3" };
    private static String[] TLS_AND_SSLv2 = { "TLSv1", "SSLv3", "SSLv2Hello" };
    private static final String[] TLS_NULL = { "SSL_RSA_WITH_NULL_SHA" };
    private static final String[] TLS_3DES_EDE_CBC = {
        "SSL_RSA_WITH_3DES_EDE_CBC_SHA"
    };
    private static final String[] TLS_AES_128_CBC = {
        "TLS_RSA_WITH_AES_128_CBC_SHA",
        "SSL_RSA_WITH_3DES_EDE_CBC_SHA"
    };

    protected Device device;
    protected String commonName;
    protected String hostname;
    protected int releaseTimeout;
    protected int socketCloseDelay = 50;
    protected int sendBufferSize;
    protected int receiveBufferSize;
    protected boolean tcpNoDelay = true;
    protected String[] tlsProtocol = TLS_WO_SSLv2;
    protected String[] tlsCipherSuite = {};
    protected InetAddress addr;

    protected InetAddress addr() throws UnknownHostException {
        if (addr == null && hostname != null)
            addr = InetAddress.getByName(hostname);
        return addr;
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
    public final void setDevice(Device device) {
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
     * The TLS CipherSuites that are supported on this particular connection.
     * TLS CipherSuites shall be described using an RFC-2246 string
     * representation (e.g. 'SSL_RSA_WITH_3DES_EDE_CBC_SHA')
     * 
     * @return A String array containing the supported cipher suites
     */
    public String[] getTlsCipherSuite() {
        return tlsCipherSuite.clone();
    }

    /**
     * The TLS CipherSuites that are supported on this particular connection.
     * TLS CipherSuites shall be described using an RFC-2246 string
     * representation (e.g. 'SSL_RSA_WITH_3DES_EDE_CBC_SHA')
     * 
     * @param tlsCipherSuite
     *            A String array containing the supported cipher suites
     */
    public void setTlsCipherSuite(String[] tlsCipherSuite) {
        checkNotNull("tlsCipherSuite", tlsCipherSuite);
        this.tlsCipherSuite = tlsCipherSuite.clone();
    }

    private static void checkNotNull(String name, Object[] a) {
        if (a == null)
            throw new NullPointerException(name);
        for (int i = 0; i < a.length; i++)
            if (a[i] == null)
                throw new NullPointerException(name + '[' + i + ']');
    }

    public final void setTlsWithoutEncyrption() {
        this.tlsCipherSuite = TLS_NULL;
    }

    public final void setTls3DES_EDE_CBC() {
        this.tlsCipherSuite = TLS_3DES_EDE_CBC;
    }

    public final void setTlsAES_128_CBC() {
        this.tlsCipherSuite = TLS_AES_128_CBC;
    }

    public final boolean isTLS() {
        return tlsCipherSuite.length > 0;
    }

    protected void appendTLS(StringBuilder sb) {
        if (isTLS()) {
            sb.append(", tls=[");
            for (String s : tlsCipherSuite)
                sb.append(s).append(", ");
            sb.setLength(sb.length() - 2);
            sb.append(']');
        }
    }

    protected void appendCN(StringBuilder sb) {
        if (commonName != null)
            sb.append(", cn=").append(commonName);
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

    public String[] getTlsProtocol() {
        return tlsProtocol.clone();
    }

    public void setTlsProtocol(String[] tlsProtocol) {
        this.tlsProtocol = tlsProtocol.clone();
    }

    public void enableSSLv2Hello() {
        this.tlsProtocol = TLS_AND_SSLv2;
    }

    public void disableSSLv2Hello() {
        this.tlsProtocol = TLS_WO_SSLv2;
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
    protected void setSocketOptions(Socket s) throws SocketException {
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

}
