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
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.net.pdu.AAbort;
import org.dcm4che.net.pdu.AAssociateAC;
import org.dcm4che.net.pdu.AAssociateRJ;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.CommonExtendedNegotiation;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.net.pdu.RoleSelection;
import org.dcm4che.util.IntHashMap;
import org.dcm4che.util.SafeClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Association {

    public static final Logger LOG_ACSE =
            LoggerFactory.getLogger("org.dcm4che.net.Association.acse");
    public static final Logger LOG_DIMSE =
            LoggerFactory.getLogger("org.dcm4che.net.Association.dimse");
    public static final Logger LOG_TIMEOUT =
            LoggerFactory.getLogger("org.dcm4che.net.Association.timeout");

    private static final AtomicInteger prevSerialNo = new AtomicInteger();
    private final AtomicInteger messageID = new AtomicInteger();
    private final int serialNo;
    private final boolean requestor;
    private String name;
    private ApplicationEntity ae;
    private final Device device;
    private final Connection conn;
    private final Socket sock;
    private final InputStream in;
    private final OutputStream out;
    private final PDUEncoder encoder;
    private PDUDecoder decoder;
    private State state;
    private AAssociateRQ rq;
    private AAssociateAC ac;
    private IOException ex;

    private HashMap<String, Object> properties;
    private int maxOpsInvoked;
    private int maxPDULength;
    private int performing;
    private ScheduledFuture<?> timeout;
    private final IntHashMap<DimseRSPHandler> rspHandlerForMsgId =
            new IntHashMap<DimseRSPHandler>();
    private final IntHashMap<DimseRSP> cancelHandlerForMsgId =
            new IntHashMap<DimseRSP>();
    private final HashMap<String,HashMap<String,PresentationContext>> pcMap =
            new HashMap<String,HashMap<String,PresentationContext>>();

     Association(ApplicationEntity ae, Connection local, Socket sock)
            throws IOException {
        this.serialNo = prevSerialNo.incrementAndGet();
        this.ae = ae;
        this.requestor = ae != null;
        this.name = "Association" + delim() + serialNo;
        this.conn = local;
        this.device = local.getDevice();
        this.sock = sock;
        this.in = sock.getInputStream();
        this.out = sock.getOutputStream();
        this.encoder = new PDUEncoder(this, out);
        if (requestor) {
            enterState(State.Sta4);
        } else {
            enterState(State.Sta2);
            startRequestTimeout();
        }
        activate();
    }

    public int nextMessageID() {
        return messageID.incrementAndGet() & 0xFFFF;
    }

    private void adjustNextMessageID(int msgId) {
        if ((messageID.get() & 0xFFFF) < msgId)
            messageID.set(msgId);
    }

    private char delim() {
        return requestor ? '-' : '+';
    }

    @Override
    public String toString() {
        return name;
    }

    public final Socket getSocket() {
        return sock;
    }

    public final AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    public final AAssociateAC getAAssociateAC() {
        return ac;
    }

    public final IOException getException() {
        return ex;
    }

    public final ApplicationEntity getApplicationEntity() {
        return ae;
    }

    public Object getProperty(String key) {
        return properties != null ? properties.get(key) : null;
    }

    public Object setProperty(String key, Object value) {
        if (properties == null)
            properties = new HashMap<String, Object>();
        return properties.put(key, value);
    }

    public Object clearProperty(String key) {
        return properties != null ? properties.remove(key) : null;
    }

    public final boolean isRequestor() {
        return requestor;
    }

    public boolean isReadyForDataTransfer() {
        return state == State.Sta6;
    }

    private void checkIsSCP(String cuid) throws NoRoleSelectionException {
        if (!isSCPFor(cuid))
            throw new NoRoleSelectionException(cuid,
                    TransferCapability.Role.SCP);
    }

    private boolean isSCPFor(String cuid) {
        RoleSelection rolsel = ac.getRoleSelectionFor(cuid);
        if (rolsel == null)
            return !requestor;
        return requestor ? rolsel.isSCP() : rolsel.isSCU();
    }

    private void checkIsSCU(String cuid) throws NoRoleSelectionException {
        if (!isSCUFor(cuid))
            throw new NoRoleSelectionException(cuid,
                    TransferCapability.Role.SCU);
    }

    private boolean isSCUFor(String cuid) {
        RoleSelection rolsel = ac.getRoleSelectionFor(cuid);
        if (rolsel == null)
            return requestor;
        return requestor ? rolsel.isSCU() : rolsel.isSCP();
    }

    public String getCallingAET() {
        return rq != null ? rq.getCallingAET() : null;
    }

    public String getCalledAET() {
        return rq != null ? rq.getCalledAET() : null;
    }

    public String getRemoteAET() {
        return requestor ? getCalledAET() : getCallingAET();
    }

    public String getLocalAET() {
        return requestor ? getCallingAET() : getCalledAET();
    }

    final int getMaxPDULengthSend() {
        return maxPDULength;
    }

    boolean isPackPDV() {
        return ae.isPackPDV();
    }

    public void release() throws IOException {
        state.writeAReleaseRQ(this);
    }

    public void abort() {
        abort(AAbort.UL_SERIVE_USER, 0);
    }

    private void pabort() {
        abort(AAbort.UL_SERIVE_PROVIDER, AAbort.REASON_NOT_SPECIFIED);
    }

    private void abort(int source, int reason) {
        abort(new AAbort(source, reason));
    }

    void abort(AAbort aa) {
        try {
            state.write(this, aa);
        } catch (IOException e) {
            // already handled by onIOException()
            // do not bother user about
        }
    }

    private synchronized void closeSocket() {
        state.closeSocket(this);
    }

    void doCloseSocket() {
        LOG_ACSE.info("{}: close {}", name, sock);
        SafeClose.close(sock);
        enterState(State.Sta1);
    }

    synchronized private void closeSocketDelayed() {
        state.closeSocketDelayed(this);
    }

    void doCloseSocketDelayed() {
        enterState(State.Sta13);
        int delay = conn.getSocketCloseDelay();
        if (delay > 0)
            device.schedule(new Runnable() {
    
                @Override
                public void run() {
                    closeSocket();
                }
            }, delay);
        else
            closeSocket();
    }

    synchronized void onIOException(IOException e) {
        if (ex != null)
            return;

        ex = e;
        LOG_ACSE.info("{}: i/o exception: {} in State: {}",
                new Object[] { name, e, state });
        closeSocket();
    }

    void write(AAbort aa) throws IOException  {
        LOG_ACSE.info("{} << {}", name, aa);
        encoder.write(aa);
        ex = aa;
        closeSocketDelayed();
    }

    void writeAReleaseRQ() throws IOException {
        LOG_ACSE.info("{} << A-RELEASE-RQ", name);
        enterState(State.Sta7);
        stopTimeout();
        encoder.writeAReleaseRQ();
        startReleaseTimeout();
    }

    private void startRequestTimeout() {
        startTimeout("{}: start A-ASSOCIATE-RQ timeout of {}ms",
                "{}: A-ASSOCIATE-RQ timeout expired",
                conn.getRequestTimeout(), State.Sta2);
    }

    private void startAcceptTimeout() {
        startTimeout("{}: start A-ASSOCIATE-AC timeout of {}ms",
                "{}: A-ASSOCIATE-AC timeout expired",
                conn.getAcceptTimeout(), State.Sta5);
    }

    private void startReleaseTimeout() {
        startTimeout("{}: start A-RELEASE-RP timeout of {}ms",
                "{}: A-RELEASE-RP timeout expired",
                conn.getReleaseTimeout(), State.Sta7);
    }

    private void startIdleTimeout() {
        startTimeout("{}: start idle timeout of {}ms",
                "{}: idle timeout expired",
                conn.getIdleTimeout(), State.Sta6);
    }

    private void startTimeout(String start, final String expired,
            int timeout, State state) {
        if (timeout > 0 && performing == 0 && rspHandlerForMsgId.isEmpty()) {
            synchronized (this) {
                if (this.state == state) {
                    stopTimeout();
                    LOG_TIMEOUT.debug(start, name, timeout);
                    this.timeout = device.schedule(new Runnable(){
        
                        @Override
                        public void run() {
                            LOG_TIMEOUT.info(expired, name);
                            pabort();
                        }}, timeout);
                }
            }
        }
    }

    private void startTimeout(final int msgID, int timeout) {
        if (timeout > 0) {
            synchronized (rspHandlerForMsgId) {
                DimseRSPHandler rspHandler = rspHandlerForMsgId.get(msgID);
                if (rspHandler != null) {
                    rspHandler.setTimeout(device.schedule(new Runnable(){

                        @Override
                        public void run() {
                            LOG_TIMEOUT.info(
                                    "{}: {}:DIMSE-RSP timeout expired",
                                    name, msgID);
                            pabort();
                        }}, timeout));
                    LOG_TIMEOUT.debug(
                            "{}: start {}:DIMSE-RSP timeout of {}ms",
                            new Object[] {name, msgID, timeout});
                }
            }
        }
    }

    private synchronized void stopTimeout() {
        if (timeout != null) {
            LOG_TIMEOUT.debug("{}: stop timeout", name);
            timeout.cancel(false);
            timeout = null;
        }
    }

    public void waitForOutstandingRSP() throws InterruptedException {
        synchronized (rspHandlerForMsgId) {
            while (!rspHandlerForMsgId.isEmpty())
                rspHandlerForMsgId.wait();
        }
    }

    void write(AAssociateRQ rq) throws IOException {
        name = rq.getCalledAET() + delim() + serialNo;
        this.rq = rq;
        LOG_ACSE.info("{} << A-ASSOCIATE-RQ", name);
        LOG_ACSE.debug("{}", rq);
        enterState(State.Sta5);
        encoder.write(rq);
        startAcceptTimeout();
    }

    private void write(AAssociateAC ac) throws IOException {
        LOG_ACSE.info("{} << A-ASSOCIATE-AC", name);
        LOG_ACSE.debug("{}", ac);
        enterState(State.Sta6);
        encoder.write(ac);
        startIdleTimeout();
    }

    private void write(AAssociateRJ e) throws IOException {
        LOG_ACSE.info("{} << {}", name, e);
        encoder.write(e);
        closeSocketDelayed();
    }

    private void checkException() throws IOException {
        if (ex != null)
            throw ex;
    }

    private synchronized void enterState(State newState) {
        LOG_ACSE.debug("{}: enter state: {}", name, newState);
        this.state = newState;
        notifyAll();
    }

    synchronized void waitForLeaving(State state)
            throws InterruptedException, IOException {
        while (this.state == state)
            wait();
        checkException();
    }

    private void activate() {
        device.execute(new Runnable() {

            @Override
            public void run() {
                decoder = new PDUDecoder(Association.this, in);
                device.incrementNumberOfOpenConnections();
                try {
                    while (!(state == State.Sta1 || state == State.Sta13))
                        decoder.nextPDU();
                } catch (AAbort aa) {
                    abort(aa);
                } catch (IOException e) {
                    onIOException(e);
                } finally {
                    onClose();
                    device.decrementNumberOfOpenConnections();
                }
            }
        });
    }

    private void onClose() {
        stopTimeout();
        synchronized (rspHandlerForMsgId) {
            IntHashMap.Visitor<DimseRSPHandler> visitor =
                    new IntHashMap.Visitor<DimseRSPHandler>() {

                @Override
                public boolean visit(int key, DimseRSPHandler value) {
                    value.onClose(Association.this);
                    return true;
                }
            };
            rspHandlerForMsgId.accept(visitor);
            rspHandlerForMsgId.clear();
            rspHandlerForMsgId.notifyAll();
        }
        if (ae != null)
            ae.onClose(this);
    }

    void onAAssociateRQ(AAssociateRQ rq) throws IOException {
        LOG_ACSE.info("{} >> A-ASSOCIATE-RQ", name);
        LOG_ACSE.debug("{}", rq);
        stopTimeout();
        state.onAAssociateRQ(this, rq);
    }

    void handle(AAssociateRQ rq) throws IOException {
        this.rq = rq;
        name = rq.getCallingAET() + delim() + serialNo;
        enterState(State.Sta3);
        try {
            if ((rq.getProtocolVersion() & 1) == 0)
                throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                        AAssociateRJ.SOURCE_SERVICE_PROVIDER_ACSE,
                        AAssociateRJ.REASON_PROTOCOL_VERSION_NOT_SUPPORTED);
            if (!rq.getApplicationContext().equals(
                    UID.DICOMApplicationContextName))
                throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                        AAssociateRJ.SOURCE_SERVICE_USER,
                        AAssociateRJ.REASON_APP_CTX_NAME_NOT_SUPPORTED);
            ae = device.getApplicationEntity(rq.getCalledAET());
            if (ae == null)
                throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                        AAssociateRJ.SOURCE_SERVICE_USER,
                        AAssociateRJ.REASON_CALLED_AET_NOT_RECOGNIZED);
            ac = ae.negotiate(this, rq);
            initPCMap();
            maxOpsInvoked = ac.getMaxOpsPerformed();
            maxPDULength = ApplicationEntity.minZeroAsMax(
                    rq.getMaxPDULength(), ae.getMaxPDULengthSend());
            write(ac);
        } catch (AAssociateRJ e) {
            write(e);
        }
    }

    void onAAssociateAC(AAssociateAC ac) throws IOException {
        LOG_ACSE.info("{} >> A-ASSOCIATE-AC", name);
        LOG_ACSE.debug("{}", ac);
        stopTimeout();
        state.onAAssociateAC(this, ac);
    }

    void handle(AAssociateAC ac) throws IOException {
        this.ac = ac;
        initPCMap();
        maxOpsInvoked = ac.getMaxOpsInvoked();
        maxPDULength = ApplicationEntity.minZeroAsMax(
                ac.getMaxPDULength(), ae.getMaxPDULengthSend());
        enterState(State.Sta6);
        startIdleTimeout();
    }

    void onAAssociateRJ(AAssociateRJ rj) throws IOException {
        LOG_ACSE.info("{} >> {}", name, rj);
        state.onAAssociateRJ(this, rj);
    }

    void handle(AAssociateRJ rq) {
        ex = rq;
        closeSocket();
    }

    void onAReleaseRQ() throws IOException {
        LOG_ACSE.info("{} >> A-RELEASE-RQ", name);
        stopTimeout();
        state.onAReleaseRQ(this);
    }

    void handleAReleaseRQ() throws IOException {
        enterState(State.Sta8);
        waitForPerformingOps();
        LOG_ACSE.info("{} << A-RELEASE-RP", name);
        encoder.writeAReleaseRP();
        closeSocketDelayed();
    }

    private synchronized void waitForPerformingOps() {
        while (performing > 0 && state == State.Sta8) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    void handleAReleaseRQCollision() throws IOException {
        if (isRequestor()) {
            enterState(State.Sta9);
            LOG_ACSE.info("{} << A-RELEASE-RP", name);
            encoder.writeAReleaseRP();
            enterState(State.Sta11);
        } else {
            enterState(State.Sta10);
        }
    }

    void onAReleaseRP() throws IOException {
        LOG_ACSE.info("{} >> A-RELEASE-RP", name);
        stopTimeout();
        state.onAReleaseRP(this);
    }

    void handleAReleaseRP() throws IOException {
        closeSocket();
    }

   void handleAReleaseRPCollision() throws IOException {
        enterState(State.Sta12);
        LOG_ACSE.info("{} << A-RELEASE-RP", name);
        encoder.writeAReleaseRP();
        closeSocketDelayed();
   }

    void onAAbort(AAbort aa) {
        LOG_ACSE.info("{} << {}", name, aa);
        stopTimeout();
        ex = aa;
        closeSocket();
    }

    void unexpectedPDU(String pdu) throws AAbort {
        LOG_ACSE.warn("{} >> unexpected {} in state: {}",
                new Object[] { name, pdu, state });
        throw new AAbort(AAbort.UL_SERIVE_PROVIDER, AAbort.UNEXPECTED_PDU);
    }

    void onPDataTF() throws IOException {
        state.onPDataTF(this);
    }

    void handlePDataTF() throws IOException {
        decoder.decodeDIMSE();
    }

    void writePDataTF() throws IOException {
        checkException();
        state.writePDataTF(this);
    }

    void doWritePDataTF() throws IOException {
        encoder.writePDataTF();
    }

    void onDimseRQ(PresentationContext pc, Attributes cmd,
            PDVInputStream data) throws IOException {
        stopTimeout();
        incPerforming();
        adjustNextMessageID(cmd.getInt(Tag.MessageID, 0));
        ae.onDimseRQ(this, pc, cmd, data);
    }

    private synchronized void incPerforming() {
        ++performing;
    }

    private synchronized void decPerforming() {
        --performing;
        notifyAll();
    }

    void onDimseRSP(Attributes cmd, Attributes data) {
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo, -1);
        int status = cmd.getInt(Tag.Status, 0);
        boolean pending = Commands.isPending(status);
        DimseRSPHandler rspHandler = pending ?
                getDimseRSPHandler(msgId) : removeDimseRSPHandler(msgId);
        if (rspHandler == null) {
            LOG_DIMSE.info("{}: unexpected message ID in DIMSE RSP:", name);
            LOG_DIMSE.info("\n{}", cmd);
            pabort();
        }
        rspHandler.onDimseRSP(this, cmd, data);
        if (!pending) {
            removeCancelRQHandler(msgId);
            if (rspHandlerForMsgId.isEmpty() && performing == 0)
                startIdleOrReleaseTimeout();
        } else
            startTimeout(msgId, conn.getDimseRSPTimeout(
                    cmd.getInt(Tag.CommandField, 0)));
    }

    private synchronized void startIdleOrReleaseTimeout() {
        if (state == State.Sta6)
            startIdleTimeout();
        else if (state == State.Sta7)
            startReleaseTimeout();
    }

    private void addDimseRSPHandler(DimseRSPHandler rspHandler)
            throws InterruptedException {
        synchronized (rspHandlerForMsgId) {
            while (maxOpsInvoked > 0
                    && rspHandlerForMsgId.size() >= maxOpsInvoked)
                rspHandlerForMsgId.wait();
            rspHandlerForMsgId.put(rspHandler.getMessageID(), rspHandler);
        }
    }

    private DimseRSPHandler getDimseRSPHandler(int msgId) {
        synchronized (rspHandlerForMsgId ) {
            return rspHandlerForMsgId.get(msgId);
        }
    }

    private DimseRSPHandler removeDimseRSPHandler(int msgId) {
        synchronized (rspHandlerForMsgId ) {
            DimseRSPHandler tmp = rspHandlerForMsgId.remove(msgId);
            rspHandlerForMsgId.notifyAll();
            return tmp;
        }
    }

    void cancel(PresentationContext pc, int msgId) throws IOException {
        Attributes cmd = Commands.mkCCancelRQ(msgId);
        encoder.writeDIMSE(pc, cmd, null);
    }

    public void writeDimseRSP(PresentationContext pc, Attributes cmd)
            throws IOException {
        writeDimseRSP(pc, cmd, null);
    }

    public void writeDimseRSP(PresentationContext pc, Attributes cmd,
            Attributes data) throws IOException {
        DataWriter writer = null;
        int datasetType = Commands.NO_DATASET;
        if (data != null) {
            writer = new DataWriterAdapter(data);
            datasetType = Commands.getWithDatasetType();
        }
        cmd.setInt(Tag.CommandDataSetType, VR.US, datasetType);
        encoder.writeDIMSE(pc, cmd, writer);
        if (!Commands.isPending(cmd.getInt(Tag.Status, 0))) {
            decPerforming();
            startIdleTimeout();
        }
    }

    void onCancelRQ(Attributes cmd) throws IOException {
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo, -1);
        DimseRSP handler = removeCancelRQHandler(msgId);
        if (handler != null)
            handler.cancel(this);
    }

    public void addCancelRQHandler(int msgId, DimseRSP handler) {
        synchronized (cancelHandlerForMsgId) {
            cancelHandlerForMsgId.put(msgId, handler);
        }
    }

    private DimseRSP removeCancelRQHandler(int msgId) {
        synchronized (cancelHandlerForMsgId) {
            return cancelHandlerForMsgId.remove(msgId);
        }
    }

    private void initPCMap() {
        for (PresentationContext pc : ac.getPresentationContexts())
            if (pc.isAccepted())
                initTSMap(rq.getPresentationContext(pc.getPCID())
                            .getAbstractSyntax())
                        .put(pc.getTransferSyntax(), pc);
    }

    private HashMap<String, PresentationContext> initTSMap(String as) {
        HashMap<String, PresentationContext> tsMap = pcMap.get(as);
        if (tsMap == null)
            pcMap.put(as, tsMap = new HashMap<String, PresentationContext>());
        return tsMap;
    }

    private PresentationContext pcFor(String cuid, String tsuid)
            throws NoPresentationContextException {
        HashMap<String, PresentationContext> tsMap = pcMap.get(cuid);
        if (tsMap == null)
            throw new NoPresentationContextException(cuid);
        if (tsuid == null)
            return tsMap.values().iterator().next();
        PresentationContext pc = tsMap.get(tsuid);
        if (pc == null)
            throw new NoPresentationContextException(cuid, tsuid);
        return pc;
    }

    PresentationContext getPresentationContext(int pcid) {
        return ac.getPresentationContext(pcid);
    }

    public CommonExtendedNegotiation getCommonExtendedNegotiationFor(
            String cuid) {
        return ac.getCommonExtendedNegotiationFor(cuid);
    }

    public DimseRSP cecho() throws IOException, InterruptedException {
        return cecho(UID.VerificationSOPClass);
    }

    public DimseRSP cecho(String cuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        PresentationContext pc = pcFor(cuid, null);
        checkIsSCU(cuid);
        Attributes cechorq = Commands.mkCEchoRQ(rsp.getMessageID(), cuid);
        invoke(pc, cechorq, null, rsp, conn.getDimseRSPTimeout());
        return rsp;
    }

    public void cstore(String cuid, String iuid, int priority, DataWriter data,
            String tsuid, DimseRSPHandler rspHandler) throws IOException,
            InterruptedException {
        cstore(cuid, cuid, iuid, priority, data, tsuid, rspHandler);
    }

    public void cstore(String asuid, String cuid, String iuid, int priority,
            DataWriter data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        PresentationContext pc = pcFor(asuid, tsuid);
        checkIsSCU(cuid);
        Attributes cstorerq = Commands.mkCStoreRQ(rspHandler.getMessageID(),
                cuid, iuid, priority);
        invoke(pc, cstorerq, data, rspHandler, conn.getDimseRSPTimeout());
    }

    public DimseRSP cstore(String cuid, String iuid, int priority,
            DataWriter data, String tsuid)
            throws IOException, InterruptedException {
        return cstore(cuid, cuid, iuid, priority, data, tsuid);
    }

    public DimseRSP cstore(String asuid, String cuid, String iuid,
            int priority, DataWriter data, String tsuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        cstore(asuid, cuid, iuid, priority, data, tsuid, rsp);
        return rsp;
    }

    public void cstore(String cuid, String iuid, int priority,
            String moveOriginatorAET, int moveOriginatorMsgId,
            DataWriter data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        cstore(cuid, cuid, iuid, priority, moveOriginatorAET,
                moveOriginatorMsgId, data, tsuid, rspHandler);
    }

    public void cstore(String asuid, String cuid, String iuid, int priority,
            String moveOriginatorAET, int moveOriginatorMsgId,
            DataWriter data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        PresentationContext pc = pcFor(asuid, tsuid);
        Attributes cstorerq = Commands.mkCStoreRQ(rspHandler.getMessageID(),
                cuid, iuid, priority, moveOriginatorAET, moveOriginatorMsgId);
        invoke(pc, cstorerq, data, rspHandler, conn.getDimseRSPTimeout());
    }

    public DimseRSP cstore(String cuid, String iuid, int priority,
            String moveOriginatorAET, int moveOriginatorMsgId,
            DataWriter data, String tsuid) throws IOException,
            InterruptedException {
        return cstore(cuid, cuid, iuid, priority, moveOriginatorAET,
                moveOriginatorMsgId, data, tsuid);
    }

    public DimseRSP cstore(String asuid, String cuid, String iuid,
            int priority, String moveOriginatorAET, int moveOriginatorMsgId,
            DataWriter data, String tsuid) throws IOException,
            InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        cstore(asuid, cuid, iuid, priority, moveOriginatorAET,
                moveOriginatorMsgId, data, tsuid, rsp);
        return rsp;
    }

    public void cfind(String cuid, int priority, Attributes data,
            String tsuid, DimseRSPHandler rspHandler) throws IOException,
            InterruptedException {
        cfind(cuid, cuid, priority, data, tsuid, rspHandler);
    }

    public void cfind(String asuid, String cuid, int priority,
            Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        PresentationContext pc = pcFor(asuid, tsuid);
        checkIsSCU(cuid);
        Attributes cfindrq =
                Commands.mkCFindRQ(rspHandler.getMessageID(), cuid, priority);
        invoke(pc, cfindrq, new DataWriterAdapter(data), rspHandler,
                conn.getDimseRSPTimeout());
    }

    public DimseRSP cfind(String cuid, int priority, Attributes data,
            String tsuid, int autoCancel) throws IOException,
            InterruptedException {
        return cfind(cuid, cuid, priority, data, tsuid, autoCancel);
    }

    public DimseRSP cfind(String asuid, String cuid, int priority,
            Attributes data, String tsuid, int autoCancel) throws IOException,
            InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        rsp.setAutoCancel(autoCancel);
        cfind(asuid, cuid, priority, data, tsuid, rsp);
        return rsp;
    }

    private void invoke(PresentationContext pc, Attributes cmd,
            DataWriter data, DimseRSPHandler rspHandler, int rspTimeout)
            throws IOException, InterruptedException {
        stopTimeout();
        checkException();
        rspHandler.setPC(pc);
        addDimseRSPHandler(rspHandler);
        encoder.writeDIMSE(pc, cmd, data);
        startTimeout(rspHandler.getMessageID(), rspTimeout);
    }

}
