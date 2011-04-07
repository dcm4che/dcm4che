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
import java.io.InputStream;
import java.util.Arrays;

import org.dcm4che.net.pdu.AAbort;
import org.dcm4che.net.pdu.AAssociateAC;
import org.dcm4che.net.pdu.AAssociateRJ;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.AAssociateRQAC;
import org.dcm4che.util.ByteUtils;
import org.dcm4che.util.StreamUtils;
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

    public void nextPDU() throws IOException {
        LOG.debug("{} waiting for PDU", as);
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        StreamUtils.readFully(in, buf, 0, 10);
        pos = 0;
        pdutype = get();
        get();
        pdulen = getInt();
        LOG.debug("{} >> PDU[type={}, len={}]",
                new Object[] { as, pdutype, pdulen & 0xFFFFFFFFL });
        switch (pdutype) {
        case PDUType.A_ASSOCIATE_RQ:
            readPDU();
            as.onAAssociateRQ((AAssociateRQ) decode(new AAssociateRQ()));
            return;
        case PDUType.A_ASSOCIATE_AC:
            readPDU();
            as.onAAssociateAC((AAssociateAC) decode(new AAssociateAC()));
            return;
        case PDUType.P_DATA_TF:
            readPDU();
            as.onPDataTF();
            return;
        case PDUType.A_ASSOCIATE_RJ:
            checkPDULength(4);
            get();
            as.onAAssociateRJ(new AAssociateRJ(get(), get(), get()));
            break;
        case PDUType.A_RELEASE_RQ:
            checkPDULength(4);
            as.onAReleaseRQ();
            break;
        case PDUType.A_RELEASE_RP:
            checkPDULength(4);
            as.onAReleaseRP();
            break;
        case PDUType.A_ABORT:
            checkPDULength(4);
            get();
            get();
            as.onAAbort(new AAbort(get(), get()));
            break;
        default:
            LOG.warn("{} >> unrecognized PDU[type={}, len={}]",
                    new Object[] { as, pdutype, pdulen & 0xFFFFFFFFL });
            throw new AAbort(AAbort.UL_SERIVE_PROVIDER,
                    AAbort.UNRECOGNIZED_PDU);
        }
    }

    private void checkPDULength(int len) throws AAbort {
        if (pdulen != len)
            invalidPDULength();
    }

    private void readPDU() throws IOException {
        if (pdulen < 4 || pdulen > MAX_PDU_LEN)
            invalidPDULength();

        if (6 + pdulen > buf.length)
            buf = Arrays.copyOf(buf, 6 + pdulen);

        StreamUtils.readFully(in, buf, 10, pdulen - 4);
    }

    private void invalidPDULength() throws AAbort {
        LOG.warn("{} >> invalid length of PDU[type={}, len={}]",
                new Object[] { as, pdutype, pdulen & 0xFFFFFFFFL });
        throw new AAbort(AAbort.UL_SERIVE_PROVIDER,
                AAbort.INVALID_PDU_PARAMETER_VALUE);
    }

    private AAssociateRQAC decode(AAssociateRQAC rqac) {
        // TODO Auto-generated method stub
        return rqac;
    }

}
