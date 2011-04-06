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

import java.io.InputStream;

import org.dcm4che.util.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
class PDUDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(PDUDecoder.class);
    private static final int DEF_PDU_LEN = 0x4000; // 16KB
    private static final int MAX_PDU_LEN = 0x1000000; // 16MB

    private final Association as;
    private final InputStream in;
    private final Thread th;
    private byte[] buf = new byte[6 + DEF_PDU_LEN];
    private int pos;
    private int pdutype;
    private int pdulen;

    public PDUDecoder(Association as, InputStream in) {
        this.as = as;
        this.in = in;
        this.th = Thread.currentThread();
    }

    private int remaining() {
        return pdulen + 6 - pos;
    }

    private boolean hasRemaining() {
        return pos < pdulen + 6;
    }
        
    private int get() {
        if (!hasRemaining())
            throw new IndexOutOfBoundsException();
        return buf[pos++] & 0xFF;
    }

    private void get(byte[] b, int off, int len) {
        if (len > remaining())
            throw new IndexOutOfBoundsException();
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
    }

    private void skip(int len) {
        if (len > remaining())
            throw new IndexOutOfBoundsException();
        pos += len;
    }
        
    private int getUnsignedShort() {
        int val = ByteUtils.bytesToUShortBE(buf, pos);
        pos += 2;
        return val;
    }

    private int getInt() {
        int val = ByteUtils.bytesToIntBE(buf, pos);
        pos += 4;
        return val;
    }

    public void nextPDU() {
        LOG.debug("{} waiting for PDU", as);
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        
        
    }

}
