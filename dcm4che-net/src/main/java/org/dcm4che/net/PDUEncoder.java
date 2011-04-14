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
import java.io.OutputStream;

import org.dcm4che.net.pdu.AAbort;
import org.dcm4che.net.pdu.AAssociateAC;
import org.dcm4che.net.pdu.AAssociateRJ;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.AAssociateRQAC;
import org.dcm4che.net.pdu.CommonExtendedNegotiation;
import org.dcm4che.net.pdu.ExtendedNegotiation;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.net.pdu.RoleSelection;
import org.dcm4che.net.pdu.UserIdentityAC;
import org.dcm4che.net.pdu.UserIdentityRQ;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
class PDUEncoder {

    private Association as;
    private OutputStream out;
    private byte[] buf = new byte[AAssociateRQAC.DEF_MAX_PDU_LENGTH + 6];
    private int pos;
    private int pdvpcid;
    private int pdvcmd;
    private int pdvpos;
    private int maxpdulen;

    public PDUEncoder(Association as, OutputStream out) {
        this.as = as;
        this.out = out;
    }

    public void write(AAssociateRQ rq) throws IOException {
        encode(rq, PDUType.A_ASSOCIATE_RQ, ItemType.RQ_PRES_CONTEXT);
        encode(rq.getUserIdentity());
        writePDU();
    }

    public void write(AAssociateAC ac) throws IOException {
        encode(ac, PDUType.A_ASSOCIATE_AC, ItemType.AC_PRES_CONTEXT);
        encode(ac.getUserIdentity());
        writePDU();
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

    private void write(int pdutype, int result, int source, int reason)
            throws IOException {
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

    private void writePDU() throws IOException {
        out.write(buf, 0, pos);
        out.flush();
        pdvpos = 6;
        pos = 12;
    }

    private void encode(AAssociateRQAC rqac, int pduType, int pcItemType) {
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

}
