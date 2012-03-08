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

package org.dcm4che.net.hl7;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import org.dcm4che.hl7.HL7Charset;
import org.dcm4che.hl7.HL7Exception;
import org.dcm4che.hl7.HL7Message;
import org.dcm4che.hl7.MLLPInputStream;
import org.dcm4che.hl7.MLLPOutputStream;
import org.dcm4che.hl7.HL7Segment;
import org.dcm4che.net.Connection;
import org.dcm4che.net.ConnectionHandler;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
enum HL7ConnectionHandler implements ConnectionHandler {
    INSTANCE;

    @Override
    public void onAccept(Connection conn, Socket s) throws IOException {
        s.setSoTimeout(conn.getIdleTimeout());
        MLLPInputStream mllpIn = new MLLPInputStream(s.getInputStream());
        MLLPOutputStream mllpOut = new MLLPOutputStream(s.getOutputStream());
        MyByteArrayOutputStream inbuf = new MyByteArrayOutputStream();
        while (mllpIn.hasMoreInput()) {
            mllpIn.copyTo(inbuf);
            byte[] buf = inbuf.buf();
            int size = inbuf.size();
            HL7Segment msh = HL7Segment.parseMSH(buf);
            String charsetName = HL7Charset.toCharsetName(msh.getField(17, null));
            Connection.LOG.info("Received HL7 Message: {}",
                    promptHL7(buf, size, charsetName));
            byte[] rsp;
            try {
                rsp = ((HL7Device) conn.getDevice()).onMessage(msh, buf, 0, size, conn);
            } catch (HL7Exception e) {
                rsp = HL7Message.makeACK(msh, e.getAcknowledgmentCode(), e.getErrorMessage())
                        .getBytes(null);
            }
            inbuf.reset();
            Connection.LOG.info("Send HL7 Message: {}",
                    promptHL7(rsp, rsp.length, charsetName));
            mllpOut.write(rsp);
            mllpOut.finish();
        }
        conn.close(s);
    }

    private class MyByteArrayOutputStream extends ByteArrayOutputStream {

        byte[] buf() {
            return buf;
        }

    }

    private String promptHL7(byte[] buf, int size, String charsetName) {
        try {
            String s = new String(buf, 0, Math.min(size, 1024), charsetName)
                .replace('\r', '\n');
            return size > 1024 ? s +  "..." : s;
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e.getMessage());
        }
    }
}
