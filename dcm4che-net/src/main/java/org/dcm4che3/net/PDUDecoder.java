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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
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
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
class PDUDecoder extends PDVInputStream {

    private static final String UNRECOGNIZED_PDU =
            "{}: unrecognized PDU[type={}, len={}]";
    private static final String INVALID_PDU_LENGTH =
            "{}: invalid length of PDU[type={}, len={}]";
    private static final String INVALID_COMMON_EXTENDED_NEGOTIATION =
            "{}: invalid Common Extended Negotiation sub-item in PDU[type={}, len={}]";
    private static final String INVALID_USER_IDENTITY =
            "{}: invalid User Identity sub-item in PDU[type={}, len={}]";
    private static final String INVALID_PDV =
            "{}: invalid PDV in PDU[type={}, len={}]";
    private static final String UNEXPECTED_PDV_TYPE =
            "{}: unexpected PDV type in PDU[type={}, len={}]";
    private static final String UNEXPECTED_PDV_PCID =
            "{}: unexpected pcid in PDV in PDU[type={}, len={}]";

    private static final int MAX_PDU_LEN = 0x1000000; // 16MiB

    private final Association as;
    private final InputStream in;
    private final Thread th;
    private byte[] buf = new byte[6 + Connection.DEF_MAX_PDU_LENGTH];
    private int pos;
    private int pdutype;
    private int pdulen;
    private int pcid = -1;
    private int pdvmch;
    private int pdvend;

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

    private byte[] getBytes(int len) {
        byte[] bs = new byte[len];
        get(bs, 0, len);
        return bs;
    }

    private byte[] decodeBytes() {
        return getBytes(getUnsignedShort());
    }

    public void nextPDU() throws IOException {
        checkThread();
        Association.LOG.trace("{}: waiting for PDU", as);
        readFully(0, 10);
        pos = 0;
        pdutype = get();
        get();
        pdulen = getInt();
        Association.LOG.trace("{} >> PDU[type={}, len={}]",
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
            abort(AAbort.UNRECOGNIZED_PDU, UNRECOGNIZED_PDU);
        }
    }

    private void checkThread() {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
    }

    private void checkPDULength(int len) throws AAbort {
        if (pdulen != len)
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_PDU_LENGTH);
    }

    private void readPDU() throws IOException {
        if (pdulen < 4 || pdulen > MAX_PDU_LEN)
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_PDU_LENGTH);

        if (6 + pdulen > buf.length)
            buf = Arrays.copyOf(buf, 6 + pdulen);

        readFully(10, pdulen - 4);
    }

    private void readFully(int off, int len) throws IOException {
        try {
            StreamUtils.readFully(in, buf, off, len);
        } catch (IOException e) {
            throw e;
        }
    }

    private void abort(int reason, String logmsg) throws AAbort {
        Association.LOG.warn(logmsg,
                new Object[] { as, pdutype, pdulen & 0xFFFFFFFFL });
        throw new AAbort(AAbort.UL_SERIVE_PROVIDER, reason);
    }

    @SuppressWarnings("deprecation")
    private String getString(int len) {
        if (pos + len > pdulen + 6)
            throw new IndexOutOfBoundsException();
        String s;
        // Skip illegal trailing NULL
        int len0 = len;
        while (len0 > 0 && buf[pos + len0 - 1] == 0) {
            len0--;
        }
        s = new String(buf, 0, pos, len0);
        pos += len;
        return s;
    }

    private String decodeString() {
        return getString(getUnsignedShort());
    }

    private AAssociateRQAC decode(AAssociateRQAC rqac)
            throws AAbort {
        try {
            rqac.setImplVersionName(null);
            rqac.setProtocolVersion(getUnsignedShort());
            get();
            get();
            rqac.setCalledAET(getString(16).trim());
            rqac.setCallingAET(getString(16).trim());
            rqac.setReservedBytes(getBytes(32));
            while (pos < pdulen)
                decodeItem(rqac);
            checkPDULength(pos - 6);
        } catch (IndexOutOfBoundsException e) {
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_PDU_LENGTH);
        }
        return rqac;
    }

    private void decodeItem(AAssociateRQAC rqac) throws AAbort {
        int itemType = get();
        get(); // skip reserved byte
        int itemLen = getUnsignedShort();
        switch (itemType)
        {
        case ItemType.APP_CONTEXT:
            rqac.setApplicationContext(getString(itemLen));
            break;
        case ItemType.RQ_PRES_CONTEXT:
        case ItemType.AC_PRES_CONTEXT:
            rqac.addPresentationContext(decodePC(itemLen));
            break;
        case ItemType.USER_INFO:
            decodeUserInfo(itemLen, rqac);
            break;
        default:
            skip(itemLen);
        }
    }

    private PresentationContext decodePC(int itemLen) {
        int pcid = get();
        get(); // skip reserved byte
        int result = get();
        get(); // skip reserved byte
        String as = null;
        ArrayList<String> tss = new ArrayList<String>(1);
        int endpos = pos + itemLen - 4;
        while (pos < endpos) {
            int subItemType = get() & 0xff;
            get(); // skip reserved byte
            int subItemLen = getUnsignedShort();
            switch (subItemType)
            {
            case ItemType.ABSTRACT_SYNTAX:
                as = getString(subItemLen);
                break;
            case ItemType.TRANSFER_SYNTAX:
                tss.add(getString(subItemLen));
                break;
            default:
                skip(subItemLen);
            }
        }
        return new PresentationContext(pcid, result, as,
                tss.toArray(new String[tss.size()]));
    }

    private void decodeUserInfo(int itemLength, AAssociateRQAC rqac) throws AAbort
    {
        int endpos = pos + itemLength;
        while (pos < endpos)
            decodeUserInfoSubItem(rqac);
    }

    private void decodeUserInfoSubItem(AAssociateRQAC rqac) throws AAbort
    {
        int itemType = get();
        get(); // skip reserved byte
        int itemLen = getUnsignedShort();
        switch (itemType)
        {
        case ItemType.MAX_PDU_LENGTH:
            rqac.setMaxPDULength(getInt());
            break;
        case ItemType.IMPL_CLASS_UID:
            rqac.setImplClassUID(getString(itemLen));
            break;
        case ItemType.ASYNC_OPS_WINDOW:
            rqac.setMaxOpsInvoked(getUnsignedShort());
            rqac.setMaxOpsPerformed(getUnsignedShort());
            break;
        case ItemType.ROLE_SELECTION:
            rqac.addRoleSelection(decodeRoleSelection(itemLen));
            break;
        case ItemType.IMPL_VERSION_NAME:
            rqac.setImplVersionName(getString(itemLen));
            break;
        case ItemType.EXT_NEG:
            rqac.addExtendedNegotiation(decodeExtNeg(itemLen));
            break;
        case ItemType.COMMON_EXT_NEG:
            rqac.addCommonExtendedNegotiation(decodeCommonExtNeg(itemLen));
            break;
        case ItemType.RQ_USER_IDENTITY:
            rqac.setUserIdentityRQ(decodeUserIdentityRQ(itemLen));
            break;
        case ItemType.AC_USER_IDENTITY:
            rqac.setUserIdentityAC(decodeUserIdentityAC(itemLen));
            break;
        default:
            skip(itemLen);
        }
    }

    private RoleSelection decodeRoleSelection(int itemLen) {
        String cuid = decodeString();
        boolean scu = get() != 0;
        boolean scp = get() != 0;
        return new RoleSelection(cuid, scu, scp);
    }

    private ExtendedNegotiation decodeExtNeg(int itemLen) {
        int uidLength = getUnsignedShort();
        String cuid = getString(uidLength);
        byte[] info = getBytes(itemLen - uidLength - 2);
        return new ExtendedNegotiation(cuid, info);
    }

    private CommonExtendedNegotiation decodeCommonExtNeg(int itemLen)
            throws AAbort {
        int endPos = pos + itemLen;
        String sopCUID = getString(getUnsignedShort());
        String serviceCUID = getString(getUnsignedShort());
        ArrayList<String> relSopCUIDs = new ArrayList<String>(1);
        int relSopCUIDsLen = getUnsignedShort();
        int endRelSopCUIDs  = pos + relSopCUIDsLen;
        while (pos < endRelSopCUIDs)
            relSopCUIDs.add(decodeString());
        if (pos != endRelSopCUIDs || pos > endPos)
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE,
                    INVALID_COMMON_EXTENDED_NEGOTIATION);
        skip(endPos - pos);
        return new CommonExtendedNegotiation(sopCUID, serviceCUID,
                relSopCUIDs.toArray(new String[relSopCUIDs.size()]));
    }

    private UserIdentityRQ decodeUserIdentityRQ(int itemLen) throws AAbort {
        int endPos = pos + itemLen;
        int type = get() & 0xff;
        boolean rspReq = get() != 0;
        byte[] primaryField = decodeBytes();
        byte[] secondaryField = decodeBytes();
        if (pos != endPos)
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_USER_IDENTITY);
        return new UserIdentityRQ(type, rspReq, primaryField, secondaryField);
    }

    private UserIdentityAC decodeUserIdentityAC(int itemLen) throws AAbort {
        int endPos = pos + itemLen;
        byte[] serverResponse = decodeBytes();
        if (pos != endPos)
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_USER_IDENTITY);
        return new UserIdentityAC(serverResponse);
    }

    public void decodeDIMSE() throws IOException {
        checkThread();
        if (pcid != - 1)
            return; // already inside decodeDIMSE

        nextPDV(PDVType.COMMAND, -1);

        PresentationContext pc = as.getPresentationContext(pcid);
        if (pc == null) {
            Association.LOG.warn(
                    "{}: No Presentation Context with given ID - {}",
                    as, pcid);
            throw new AAbort();
        }

        if (!pc.isAccepted()) {
            Association.LOG.warn(
                    "{}: No accepted Presentation Context with given ID - {}",
                    as, pcid);
            throw new AAbort();
        }

        Attributes cmd = readCommand();
        Dimse dimse = dimseOf(cmd);
        String tsuid = pc.getTransferSyntax();
        if (Dimse.LOG.isInfoEnabled()) {
            Dimse.LOG.info("{} >> {}", as, dimse.toString(cmd, pcid, tsuid));
            if (Dimse.LOG.isDebugEnabled()) {
                Dimse.LOG.debug("{} >> {} Command:\n{}", as, dimse.toString(cmd), cmd);
            }
        }
        if (dimse == Dimse.C_CANCEL_RQ) {
            as.onCancelRQ(cmd);
        } else if (Commands.hasDataset(cmd)) {
            nextPDV(PDVType.DATA, pcid);
            if (dimse.isRSP()) {
                Attributes data = readDataset(tsuid);
                if (Dimse.LOG.isDebugEnabled()) {
                    Dimse.LOG.debug("{} >> {} Dataset:\n{}", as, dimse.toString(cmd), data);
                }
                as.onDimseRSP(dimse, cmd, data);
            } else {
                if (Dimse.LOG.isDebugEnabled()) {
                    Dimse.LOG.debug("{} >> {} Dataset receiving...", as, dimse.toString(cmd));
                }
                as.onDimseRQ(pc, dimse, cmd, this);
                long skipped = skipAll();
                if (skipped > 0)
                    Association.LOG.debug(
                        "{}: Service User did not consume {} bytes of DIMSE data.",
                        as, skipped);
            }
        } else {
            if (dimse.isRSP()) {
                as.onDimseRSP(dimse, cmd, null);
            } else {
                as.onDimseRQ(pc, dimse, cmd, null);
            }
        }
        pcid = -1;
    }

    private Dimse dimseOf(Attributes cmd) throws AAbort {
        try {
            return Dimse.valueOf(cmd.getInt(Tag.CommandField, 0));
        } catch (IllegalArgumentException e) {
            Dimse.LOG.info("{}: illegal DIMSE:", as);
            Dimse.LOG.info("\n{}", cmd);
            throw new AAbort();
        }
    }

    private Attributes readCommand() throws IOException {
        DicomInputStream in =
                new DicomInputStream(this, UID.ImplicitVRLittleEndian);
        try {
            return in.readCommand();
        } finally {
            SafeClose.close(in);
        }
    }

    @Override
    public Attributes readDataset(String tsuid) throws IOException {
        DicomInputStream in = new DicomInputStream(this, tsuid);
        try {
            return in.readDataset();
        } finally {
            SafeClose.close(in);
        }
    }

    private void nextPDV(int expectedPDVType, int expectedPCID)
            throws IOException {
        if (!hasRemaining()) {
            nextPDU();
            if (pdutype != PDUType.P_DATA_TF) {
                Association.LOG.info(
                        "{}: Expected P-DATA-TF PDU but received PDU[type={}]",
                        as, pdutype);
                throw new EOFException();
            }
        }
        if (remaining() < 6)
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_PDV);
        int pdvlen = getInt();
        this.pdvend = pos + pdvlen;
        if (pdvlen < 2 || pdvlen > remaining())
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_PDV);
        this.pcid = get();
        this.pdvmch = get();
        Association.LOG.trace("{} >> PDV[len={}, pcid={}, mch={}]",
                new Object[] { as, pdvlen, pcid, pdvmch } );
        if ((pdvmch & PDVType.COMMAND) != expectedPDVType)
            abort(AAbort.UNEXPECTED_PDU_PARAMETER, UNEXPECTED_PDV_TYPE);
        if (expectedPCID != -1 && pcid != expectedPCID)
            abort(AAbort.UNEXPECTED_PDU_PARAMETER, UNEXPECTED_PDV_PCID);
    }

    private boolean isLastPDV() throws IOException {
        while (pos == pdvend) {
            if ((pdvmch & PDVType.LAST) != 0)
                return true;
            nextPDV(pdvmch & PDVType.COMMAND, pcid);
        }
        return false;
    }

    boolean isPendingPDV() {
        return pcid != -1 && (pdvmch & PDVType.LAST) == 0;
    }

    @Override
    public int read() throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        if (isLastPDV())
            return -1;

        return get();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        if (isLastPDV())
            return -1;

        int read = Math.min(len, pdvend - pos);
        get(b, off, read);
        return read;
    }

    @Override
    public final int available() {
        return pdvend - pos;
    }

    @Override
    public long skip(long n) throws IOException
    {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        if (n <= 0 || isLastPDV())
            return 0;

        int skipped = (int) Math.min(n, pdvend - pos);
        skip(skipped);
        return skipped;
    }
    
    @Override
    public void close() throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        skipAll();
    }

    @Override
    public long skipAll() throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        long n = 0;
        while (!isLastPDV()) {
            n += pdvend - pos;
            pos = pdvend;
        }
        return n;
    }

    @Override
    public void copyTo(OutputStream out, int length) throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        int remaining = length;
        while (remaining > 0) {
            if (isLastPDV())
                throw new EOFException("remaining: " + remaining);
            int read = Math.min(remaining, pdvend - pos);
            out.write(buf, pos, read);
            remaining -= read;
            pos += read;
        }
    }

    @Override
    public void copyTo(OutputStream out) throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        while (!isLastPDV()) {
            out.write(buf, pos, pdvend - pos);
            pos = pdvend;
        }
    }
}
