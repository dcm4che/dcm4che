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

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.pdu.AAbort;
import org.dcm4che3.net.pdu.AAssociateAC;
import org.dcm4che3.net.pdu.AAssociateRJ;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.AAssociateRQAC;
import org.dcm4che3.net.pdu.CommonExtendedNegotiation;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.RoleSelection;
import org.dcm4che3.net.pdu.UserIdentityAC;
import org.dcm4che3.net.pdu.UserIdentityRQ;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
class PDUEncoder extends PDVOutputStream {

    private Association as;
    private OutputStream out;
    private byte[] buf = new byte[Connection.DEF_MAX_PDU_LENGTH + 6];
    private int pos;
    private int pdvpcid;
    private int pdvcmd;
    private int pdvpos;
    private int maxpdulen;
    private Thread th;
    private Object dimseLock = new Object();

    public PDUEncoder(Association as, OutputStream out) {
        this.as = as;
        this.out = (out instanceof BufferedOutputStream) ? out : new BufferedOutputStream(out);
    }

    public void write(AAssociateRQ rq) throws IOException {
        encode(rq, PDUType.A_ASSOCIATE_RQ, ItemType.RQ_PRES_CONTEXT);
        writePDU(pos - 6);
    }

    public void write(AAssociateAC ac) throws IOException {
        encode(ac, PDUType.A_ASSOCIATE_AC, ItemType.AC_PRES_CONTEXT);
        writePDU(pos - 6);
    }

    public void write(AAssociateRJ rj) throws IOException {
        write(PDUType.A_ASSOCIATE_RJ, rj.getResult(), rj.getSource(),
                rj.getReason());
    }

    public void writeAReleaseRQ() throws IOException {
        write(PDUType.A_RELEASE_RQ, 0, 0, 0);
    }

    public void writeAReleaseRP() throws IOException {
        write(PDUType.A_RELEASE_RP, 0, 0, 0);
    }

    public void write(AAbort aa) throws IOException {
        write(PDUType.A_ABORT, 0, aa.getSource(), aa.getReason());
    }

    private synchronized void write(int pdutype, int result, int source,
            int reason) throws IOException {
        byte[] b = {
                (byte) pdutype,
                0,
                0, 0, 0, 4, // pdulen
                0,
                (byte) result,
                (byte) source,
                (byte) reason
        };
        out.write(b);
        out.flush();
    }

    private synchronized void writePDU(int pdulen) throws IOException {
        try {
            out.write(buf, 0, 6 + pdulen);
            out.flush();
        } catch (IOException e) {
            as.onIOException(e);
            throw e;
        }
        pdvpos = 6;
        pos = 12;
    }

    private void encode(AAssociateRQAC rqac, int pduType, int pcItemType) {
        rqac.checkCallingAET();
        rqac.checkCalledAET();

        int pdulen = rqac.length();
        if (buf.length < 6 + pdulen)
            buf = new byte[6 + pdulen];
        pos = 0;
        put(pduType);
        put(0);
        putInt(pdulen);
        putShort(rqac.getProtocolVersion());
        put(0);
        put(0);
        encodeAET(rqac.getCalledAET());
        encodeAET(rqac.getCallingAET());
        put(rqac.getReservedBytes(), 0, 32);
        encodeStringItem(ItemType.APP_CONTEXT, rqac.getApplicationContext());
        for (PresentationContext pc : rqac.getPresentationContexts())
            encode(pc, pcItemType);
        encodeUserInfo(rqac);
    }

    private void put(int ch) {
        buf[pos++] = (byte) ch;
    }

    private void put(byte[] b) {
        put(b, 0, b.length);
    }

    private void put(byte[] b, int off, int len) {
        System.arraycopy(b, off, buf, pos, len);
        pos += len;
    }

    private void putShort(int v) {
        buf[pos++] = (byte) (v >> 8);
        buf[pos++] = (byte) v;
    }

    private void putInt(int v) {
        buf[pos++] = (byte) (v >> 24);
        buf[pos++] = (byte) (v >> 16);
        buf[pos++] = (byte) (v >> 8);
        buf[pos++] = (byte) v;
    }

    @SuppressWarnings("deprecation")
    private void putString(String s) {
        int len = s.length();
        s.getBytes(0, len, buf, pos);
        pos += len;
    }

    private void encode(byte[] b) {
        putShort(b.length);
        put(b, 0, b.length);
    }

    private void encode(String s) {
        putShort(s.length());
        putString(s);
    }

    private void encodeAET(String aet) {
        int endpos = pos + 16;
        putString(aet);
        while (pos < endpos)
            put(0x20);
    }

    private void encodeItemHeader(int type, int len) {
        put(type);
        put(0);
        putShort(len);
    }

    private void encodeStringItem(int type, String s) {
        if (s == null)
            return;

        encodeItemHeader(type, s.length());
        putString(s);
    }

    private void encode(PresentationContext pc, int pcItemType) {
        encodeItemHeader(pcItemType, pc.length());
        put(pc.getPCID());
        put(0);
        put(pc.getResult());
        put(0);
        encodeStringItem(ItemType.ABSTRACT_SYNTAX, pc.getAbstractSyntax());
        for (String ts : pc.getTransferSyntaxes())
            encodeStringItem(ItemType.TRANSFER_SYNTAX, ts);
    }

    private void encodeUserInfo(AAssociateRQAC rqac) {
        encodeItemHeader(ItemType.USER_INFO, rqac.userInfoLength());
        encodeMaxPDULength(rqac.getMaxPDULength());
        encodeStringItem(ItemType.IMPL_CLASS_UID, rqac.getImplClassUID());
        if (rqac.isAsyncOps())
            encodeAsyncOpsWindow(rqac);
        for (RoleSelection rs : rqac.getRoleSelections())
            encode(rs);
        encodeStringItem(ItemType.IMPL_VERSION_NAME, rqac.getImplVersionName());
        for (ExtendedNegotiation extNeg : rqac.getExtendedNegotiations())
            encode(extNeg);
        for (CommonExtendedNegotiation extNeg :
                rqac.getCommonExtendedNegotiations())
            encode(extNeg);
        encode(rqac.getUserIdentityRQ());
        encode(rqac.getUserIdentityAC());
    }

    private void encodeMaxPDULength(int maxPDULength) {
        encodeItemHeader(ItemType.MAX_PDU_LENGTH, 4);
        putInt(maxPDULength);
    }

    private void encodeAsyncOpsWindow(AAssociateRQAC rqac) {
        encodeItemHeader(ItemType.ASYNC_OPS_WINDOW, 4);
        putShort(rqac.getMaxOpsInvoked());
        putShort(rqac.getMaxOpsPerformed());
    }

    private void encode(RoleSelection rs) {
        encodeItemHeader(ItemType.ROLE_SELECTION, rs.length());
        encode(rs.getSOPClassUID());
        put(rs.isSCU() ? 1 : 0);
        put(rs.isSCP() ? 1 : 0);
    }

    private void encode(ExtendedNegotiation extNeg) {
        encodeItemHeader(ItemType.EXT_NEG, extNeg.length());
        encode(extNeg.getSOPClassUID());
        put(extNeg.getInformation());
    }

    private void encode(CommonExtendedNegotiation extNeg) {
        encodeItemHeader(ItemType.COMMON_EXT_NEG, extNeg.length());
        encode(extNeg.getSOPClassUID());
        encode(extNeg.getServiceClassUID());
        putShort(extNeg.getRelatedGeneralSOPClassUIDsLength());
        for (String cuid : extNeg.getRelatedGeneralSOPClassUIDs())
            encode(cuid);
    }

    private void encode(UserIdentityRQ userIdentity) {
        if (userIdentity == null)
            return;

        encodeItemHeader(ItemType.RQ_USER_IDENTITY, userIdentity.length());
        put(userIdentity.getType());
        put(userIdentity.isPositiveResponseRequested() ? 1 : 0);
        encode(userIdentity.getPrimaryField());
        encode(userIdentity.getSecondaryField());
    }

    private void encode(UserIdentityAC userIdentity) {
        if (userIdentity == null)
            return;

        encodeItemHeader(ItemType.AC_USER_IDENTITY, userIdentity.length());
        encode(userIdentity.getServerResponse());
    }

    @Override
    public void write(int b) throws IOException {
        checkThread();
        flushPDataTF();
        put(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkThread();
        int pos = off;
        int remaining = len;
        while (remaining > 0) {
            flushPDataTF();
            int write = Math.min(remaining, free());
            put(b, pos, write);
            pos += write;
            remaining -= write;
        }
    }

    @Override
    public void close() {
        checkThread();
        encodePDVHeader(PDVType.LAST);
    }

    @Override
    public void copyFrom(InputStream in, int len) throws IOException {
        checkThread();
        int remaining = len;
        while (remaining > 0) {
            flushPDataTF();
            int copy = in.read(buf, pos, Math.min(remaining, free()));
            if (copy == -1)
                throw new EOFException();
            pos += copy;
            remaining -= copy;
        }
    }

    @Override
    public void copyFrom(InputStream in) throws IOException {
        checkThread();
        for (;;) {
            flushPDataTF();
            int copy = in.read(buf, pos, free());
            if (copy == -1)
                return;
            pos += copy;
        }
    }

    private void checkThread() {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
    }

    private int free() {
        return maxpdulen + 6 - pos;
    }

    private void flushPDataTF() throws IOException {
        if (free() > 0)
            return;
        encodePDVHeader(PDVType.PENDING);
        as.writePDataTF();
    }

    private void encodePDVHeader(int last) {
        final int endpos = pos;
        final int pdvlen = endpos - pdvpos - 4;
        pos = pdvpos;
        putInt(pdvlen);
        put(pdvpcid);
        put(pdvcmd | last);
        pos = endpos;
        Association.LOG.trace("{} << PDV[len={}, pcid={}, mch={}]",
                new Object[] { as, pdvlen, pdvpcid, (pdvcmd | last) });
    }

    public void writePDataTF() throws IOException {
        int pdulen = pos - 6;
        pos = 0;
        put(PDUType.P_DATA_TF);
        put(0);
        putInt(pdulen);
        Association.LOG.trace("{} << P-DATA-TF[len={}]",
                new Object[] { as, pdulen });
        writePDU(pdulen);
    }

    public void writeDIMSE(PresentationContext pc, Attributes cmd,
            DataWriter dataWriter) throws IOException {
        synchronized (dimseLock) {
            int pcid = pc.getPCID();
            String tsuid = pc.getTransferSyntax();
            if (Dimse.LOG.isInfoEnabled()) {
                Dimse dimse = Dimse.valueOf(cmd.getInt(Tag.CommandField, -1));
                Dimse.LOG.info("{} << {}", as, dimse.toString(cmd, pcid, tsuid));
                Dimse.LOG.debug("Command:\n{}", cmd);
                if (dataWriter instanceof DataWriterAdapter)
                    Dimse.LOG.debug("Dataset:\n{}",
                            ((DataWriterAdapter) dataWriter).getDataset());
            }
            this.th = Thread.currentThread();
            maxpdulen = as.getMaxPDULengthSend();
            if (buf.length < maxpdulen + 6)
                buf = new byte[maxpdulen + 6];

            pdvpcid = pcid;
            pdvcmd = PDVType.COMMAND;
            DicomOutputStream cmdout =
                new DicomOutputStream(this, UID.ImplicitVRLittleEndian);
            cmdout.writeCommand(cmd);
            cmdout.close();
            if (dataWriter != null) {
                if (!as.isPackPDV()) {
                    as.writePDataTF();
                } else {
                    pdvpos = pos;
                    pos += 6;
                }
                pdvcmd = PDVType.DATA;
                dataWriter.writeTo(this, tsuid);
                close();
            }
            as.writePDataTF();
            this.th = null;
        }
    }
}
