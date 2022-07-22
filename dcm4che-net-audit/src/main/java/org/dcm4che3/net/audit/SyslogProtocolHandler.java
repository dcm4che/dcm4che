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

package org.dcm4che3.net.audit;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executor;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.TCPProtocolHandler;
import org.dcm4che3.net.UDPProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
enum SyslogProtocolHandler implements TCPProtocolHandler, UDPProtocolHandler {
    INSTANCE;

    private static final int INIT_MSG_LEN = 8192;
    private static final int MAX_MSG_LEN = 1024*1024*20; //20mb
    private static final int MAX_MSG_PREFIX = 200;
    private static final int MSG_PROMPT_LEN = 8192;

    private static Logger LOG = LoggerFactory.getLogger(SyslogProtocolHandler.class);

    private static volatile Executor executor;

    public static void setExecutor(Executor executor) {
        SyslogProtocolHandler.executor = executor;
    }

    @Override
    public void onAccept(Connection conn, Socket s) {
        if (executor != null)
            executor.execute(new SyslogReceiverTLS(conn, s));
        else
            conn.getDevice().execute(new SyslogReceiverTLS(conn, s));
    }

    @Override
    public void onReceive(Connection conn, DatagramPacket packet) {
        if (executor != null)
            executor.execute(new SyslogReceiverUDP(conn, packet));
        else
            conn.getDevice().execute(new SyslogReceiverUDP(conn, packet));
    }

    private static int readMessageLength(InputStream in, Socket s) throws IOException {
        int ch;
        try {
            ch = in.read();
        } catch (SocketTimeoutException e) {
            LOG.info("Timeout expired for connection to {}", s);
            return -1;
        }
        if (ch < 0)
            return -1;

        int d, len = 0;
        do {
            d = ch - '0';
            if (d < 0 || d > 9) {
                LOG.warn("Illegal character code: {} in message length received from {}",
                        ch, s);
                return -1;
            }
            len = (len << 3) + (len << 1) + d; // 10 * len + d
            ch = in.read();
        } while (ch > 0 && ch != ' ');
        return len;
   }

    private static int readMessage(InputStream in, byte[] data, int len) throws IOException {
        int count, n = 0;
        while (n < len && (count = in.read(data, n, len - n)) > 0) {
            n += count;
        }
        return n;
    }

    private static void onMessage(AuditRecordRepository arr, byte[] data, int offset, int length,
            Connection conn, InetAddress from) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(prompt(data, MSG_PROMPT_LEN));
        }
        int xmlOffset = indexOfXML(data, offset, Math.min(MAX_MSG_PREFIX, length));
        if (xmlOffset != -1) {
            int xmlLength = length - xmlOffset + offset;
            arr.onMessage(data, xmlOffset, xmlLength, conn, from);
        } else {
            LOG.warn("Ignore unexpected message from {}: {}", from,
                    prompt(data, MAX_MSG_PREFIX));
        }
    }

    private static int indexOfXML(byte[] buf, int offset, int maxIndex) {
        for(int index = offset, xmlDeclIndex = -1; index <= maxIndex; index++) {
            if (buf[index] != '<')
                continue;
            if (isAuditMessage(buf, index) || isIHEYr4(buf, index))
                return xmlDeclIndex == -1 ? index : xmlDeclIndex;
            else if (xmlDeclIndex == -1 && isXMLDecl(buf, index))
                xmlDeclIndex = index;
        }
        return -1;
    }

    private static boolean isXMLDecl(byte[] buf, int index) {
        return index + 4 < buf.length
            && buf[index+1] == '?'
            && buf[index+2] == 'x'
            && buf[index+3] == 'm'
            && buf[index+4] == 'l';
    }

    private static boolean isAuditMessage(byte[] buf, int index) {
        return index + 12 < buf.length
            && buf[index+1] == 'A'
            && buf[index+2] == 'u'
            && buf[index+3] == 'd'
            && buf[index+4] == 'i'
            && buf[index+5] == 't'
            && buf[index+6] == 'M'
            && buf[index+7] == 'e'
            && buf[index+8] == 's'
            && buf[index+9] == 's'
            && buf[index+10] == 'a'
            && buf[index+11] == 'g'
            && buf[index+12] == 'e';
    }

    private static boolean isIHEYr4(byte[] buf, int index) {
        return index + 6 < buf.length
            && buf[index+1] == 'I'
            && buf[index+2] == 'H'
            && buf[index+3] == 'E'
            && buf[index+4] == 'Y'
            && buf[index+5] == 'r'
            && buf[index+6] == '4';
    }

    private static String prompt(byte[] data, int maxLen) {
        try {
            return data.length > maxLen
                    ? (new String(data, 0, maxLen, "UTF-8") + "...") 
                    : new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static class SyslogReceiverTLS implements Runnable {
        private final Connection conn;
        private final Socket s;
        private final AuditRecordRepository arr;

        public SyslogReceiverTLS(Connection conn, Socket s) {
            this.conn = conn;
            this.s = s;
            this.arr = conn.getDevice().getDeviceExtensionNotNull(AuditRecordRepository.class);
        }

        @Override
        public void run() {
            try {
                InputStream in = s.getInputStream();
                byte[] data = new byte[INIT_MSG_LEN];
                int length;
                s.setSoTimeout(conn.getIdleTimeout());
                while ((length = readMessageLength(in, s)) > 0) {
                    if (length > MAX_MSG_LEN) {
                        LOG.warn("Message length: {} received from {} exceeds limit {}",
                                length, s, MAX_MSG_LEN);
                        break;
                    }
                    if (length > data.length)
                        data = new byte[length];

                    if (readMessage(in, data, length) < length) {
                        LOG.warn("Connection closed by remote host {} during receive of message",
                                s);
                        break;
                    }
                    LOG.info("Received Syslog message of {} bytes from {}",
                            length, s);
                    onMessage(arr, data, 0, length, conn, s.getInetAddress());
                }
            } catch (IOException e) {
                LOG.warn("Exception on accepted connection {}:",s, e);
            } finally {
                conn.close(s);
            }
        }
    }

    private static class SyslogReceiverUDP implements Runnable {
        private final Connection conn;
        private final DatagramPacket packet;
        private final AuditRecordRepository arr;

        public SyslogReceiverUDP(Connection conn, DatagramPacket packet) {
            this.conn = conn;
            this.packet = packet;
            this.arr = conn.getDevice().getDeviceExtensionNotNull(AuditRecordRepository.class);
        }

        @Override
        public void run() {
            LOG.info("Received UDP Syslog message of {} bytes from {}",
                    packet.getLength(), packet.getAddress());
            onMessage(arr, packet.getData(), packet.getOffset(), packet.getLength(),
                    conn, packet.getAddress());

        }
    }
}
