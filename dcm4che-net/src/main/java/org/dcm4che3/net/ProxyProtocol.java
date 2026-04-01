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
 * Johannes Pretorius - Shorai Tek
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
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * PROXY Protocol (v1 and v2) parser.
 * Uses PushbackInputStream so that non-PROXY connections are not corrupted.
 */
public final class ProxyProtocol {

    public static final class Info {
        public final InetSocketAddress receivingRemote; // IP:port seen by the server (usually LB)
        public final InetSocketAddress proxiedRemote;   // Original client IP:port from PROXY header
        public final InetSocketAddress receivingLocal;  // Local server address
		public final PushbackInputStream pbInputStream; //Stream used to read from for socket comms after proxy headers stripped

        Info(InetSocketAddress receivingRemote,
             InetSocketAddress proxiedRemote,
             InetSocketAddress receivingLocal,
			 PushbackInputStream pbInputStream) {
            this.receivingRemote = receivingRemote;
            this.proxiedRemote = proxiedRemote;
            this.receivingLocal = receivingLocal;
			this.pbInputStream = pbInputStream;
        }
    }

    /**
     * Parse PROXY header from the socket.
     * If no valid PROXY header is present, the original bytes are pushed back.
     */
    public static Info parse(Socket socket) throws IOException {
		PushbackInputStream pbIn = new PushbackInputStream(socket.getInputStream(), 512);
        //InputStream rawIn = socket.getInputStream();
        //PushbackInputStream pbIn = new PushbackInputStream(rawIn, 512);
		//rawIn.mark(512);

        SocketAddress recvRemoteSA = socket.getRemoteSocketAddress();
        SocketAddress recvLocalSA = socket.getLocalSocketAddress();

        InetSocketAddress receivingRemote = recvRemoteSA instanceof InetSocketAddress
                ? (InetSocketAddress) recvRemoteSA : null;
        InetSocketAddress receivingLocal = recvLocalSA instanceof InetSocketAddress
                ? (InetSocketAddress) recvLocalSA : null;

        byte[] buf = new byte[16];
        int n = pbIn.read(buf, 0, 12);
        if (n < 12) {
            pbIn.unread(buf, 0, n);
            throw new IOException("Short read while detecting PROXY header");
        }

        if (isV2Signature(buf)) {
            // v2 header continues after the 12-byte signature
            int remaining = 4; // version, command, family, protocol, length(2)
            n = pbIn.read(buf, 12, remaining);
            if (n < remaining) {
                pbIn.unread(buf, 0, 12 + n);
                throw new IOException("Short PROXY v2 header");
            }
            return parseV2(pbIn, buf, receivingRemote, receivingLocal);
        } else if (isV1Signature(buf)) {
            return parseV1(pbIn, buf, receivingRemote, receivingLocal);
        } else {
            // No PROXY header → push the 12 bytes back for DICOM layer
            pbIn.unread(buf, 0, 12);
           // in.reset(); 
            return new Info(receivingRemote, receivingRemote, receivingLocal,pbIn);
        }
    }

    private static boolean isV2Signature(byte[] b) {
        return b[0] == 0x0D && b[1] == 0x0A && b[2] == 0x0D && b[3] == 0x0A
                && b[4] == 0x00 && b[5] == 0x0D && b[6] == 0x0A && b[7] == 0x51
                && b[8] == 0x55 && b[9] == 0x49 && b[10] == 0x54 && b[11] == 0x0A;
    }

    private static boolean isV1Signature(byte[] b) {
        return b[0] == 'P' && b[1] == 'R' && b[2] == 'O' && b[3] == 'X' && b[4] == 'Y';
    }

    private static Info parseV1(PushbackInputStream in, byte[] initial,
                                InetSocketAddress receivingRemote,
                                InetSocketAddress receivingLocal) throws IOException {

        StringBuilder sb = new StringBuilder(new String(initial, 0, 12, StandardCharsets.US_ASCII));
        int c;
        while ((c = in.read()) != -1) {
            sb.append((char) c);
            if (c == '\n') break;
        }

        String line = sb.toString().trim();
        String[] parts = line.split("\\s+");
        if (parts.length < 6 || !"PROXY".equals(parts[0])) {
            throw new IOException("Invalid PROXY v1 header");
        }

        String srcIP = parts[2];
        int srcPort = Integer.parseInt(parts[4]);

        InetAddress srcAddr = InetAddress.getByName(srcIP);
        InetSocketAddress proxied = new InetSocketAddress(srcAddr, srcPort);

        return new Info(receivingRemote, proxied, receivingLocal, in);
    }

    private static Info parseV2(PushbackInputStream in, byte[] header,
                                InetSocketAddress receivingRemote,
                                InetSocketAddress receivingLocal) throws IOException {

        int addrLen = ((header[14] & 0xFF) << 8) | (header[15] & 0xFF);

        byte[] addrBuf = new byte[addrLen];
        int read = 0;
        while (read < addrLen) {
            int r = in.read(addrBuf, read, addrLen - read);
            if (r <= 0) {
                throw new IOException("Short address read in PROXY v2");
            }
            read += r;
        }

        InetSocketAddress proxied = null;
        int family = (header[13] >> 4) & 0x0F;

        if (family == 1) { // IPv4
            if (addrLen >= 12) {
                byte[] ip = new byte[4];
                System.arraycopy(addrBuf, 0, ip, 0, 4);
                int port = ((addrBuf[8] & 0xFF) << 8) | (addrBuf[9] & 0xFF);
                proxied = new InetSocketAddress(InetAddress.getByAddress(ip), port);
            }
        } else if (family == 2) { // IPv6
            if (addrLen >= 36) {
                byte[] ip = new byte[16];
                System.arraycopy(addrBuf, 0, ip, 0, 16);
                int port = ((addrBuf[32] & 0xFF) << 8) | (addrBuf[33] & 0xFF);
                proxied = new InetSocketAddress(InetAddress.getByAddress(ip), port);
            }
        }

        if (proxied == null) {
            proxied = receivingRemote;
        }

        return new Info(receivingRemote, proxied, receivingLocal, in);
    }
}