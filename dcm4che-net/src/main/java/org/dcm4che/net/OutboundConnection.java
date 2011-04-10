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
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class OutboundConnection extends AbstractConnection {

    private static Logger LOG =
        LoggerFactory.getLogger(OutboundConnection.class);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InboundConnection[")
            .append(hostname);
        appendTLS(sb);
        appendCN(sb);
        sb.append(']');
        return sb.toString();
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
    public Socket connect(InboundConnection peerConfig) throws IOException {
        if (device == null)
            throw new IllegalStateException("Device not initalized");
        Socket s = isTLS() ? createTLSSocket() : new Socket();
        InetSocketAddress bindPoint = getBindPoint();
        InetSocketAddress endpoint = peerConfig.getEndPoint();
        LOG.debug("Initiate connection from {} to {}", bindPoint, endpoint);
        s.bind(bindPoint);
        setSocketOptions(s);
        s.connect(endpoint, peerConfig.getConnectTimeout());
        return s;
    }

    private Socket createTLSSocket() throws IOException {
        SSLContext sslContext = device.getSSLContext();
        if (sslContext == null)
            throw new IllegalStateException("TLS Context not initialized!");
        SSLSocketFactory sf = sslContext.getSocketFactory();
        SSLSocket s = (SSLSocket) sf.createSocket();
        s.setEnabledProtocols(tlsProtocol);
        s.setEnabledCipherSuites(tlsCipherSuite);
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
