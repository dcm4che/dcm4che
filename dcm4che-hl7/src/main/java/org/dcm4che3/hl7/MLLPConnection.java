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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4che3.hl7;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class MLLPConnection implements Closeable {

    private static Logger LOG = LoggerFactory.getLogger(MLLPConnection.class);

    private final Socket sock;
    private final MLLPInputStream mllpIn;
    private final MLLPOutputStream mllpOut;

    public MLLPConnection(Socket sock) throws IOException {
        this.sock = sock;
        mllpIn = new MLLPInputStream(sock.getInputStream());
        mllpOut = new MLLPOutputStream(sock.getOutputStream());
    }

    public MLLPConnection(Socket sock, int bufferSize) throws IOException {
        this.sock = sock;
        mllpIn = new MLLPInputStream(sock.getInputStream());
        mllpOut = new MLLPOutputStream(new BufferedOutputStream(sock.getOutputStream(), bufferSize));
    }

    public final Socket getSocket() {
        return sock;
    }

    public void writeMessage(byte[] b) throws IOException {
       writeMessage(b, 0, b.length);
    }

    public void writeMessage(byte[] b, int off, int len) throws IOException {
        log("{} << {}", b, off, len);
        mllpOut.writeMessage(b, off, len);
    }

    public byte[] readMessage() throws IOException {
        byte[] b = mllpIn.readMessage();
        if (b != null)
            log("{} >> {}", b, 0, b.length);
        return b;
    }

    private void log(String format, byte[] b, int off, int len) {
        if (!LOG.isInfoEnabled())
            return;
        int mshlen = 0;
        while (mshlen < len && b[off + mshlen] != '\r')
            mshlen++;
        LOG.info(format, sock, new String(b, off, mshlen));
        if (LOG.isDebugEnabled())
            LOG.debug(format, sock, new String(b, off, len).replace('\r', '\n'));
    }

    @Override
    public void close() throws IOException {
        sock.close();
    }
}
