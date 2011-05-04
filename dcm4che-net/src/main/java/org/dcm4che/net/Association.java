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
import java.net.SocketException;
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
import org.dcm4che.net.pdu.AAssociateRQAC;
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

    static final Logger LOG =
            LoggerFactory.getLogger(Association.class);
    static final Logger LOG_AARQAC =
            LoggerFactory.getLogger(AAssociateRQAC.class);

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
    private long idleTimeout;
    private final IntHashMap<DimseRSPHandler> rspHandlerForMsgId =
            new IntHashMap<DimseRSPHandler>();
    private final IntHashMap<DimseRSP> cancelHandlerForMsgId =
            new IntHashMap<DimseRSP>();
    private final HashMap<String,HashMap<String,PresentationContext>> pcMap =
            new HashMap<String,HashMap<String,PresentationContext>>();
    private ScheduledFuture<?> scheduleAtFixedRate;

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
        enterState(requestor ? State.Sta4 : State.Sta2);
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
        abort(new AAbort());
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
        LOG.info("{}: close {}", name, sock);
        SafeClose.close(sock);
        enterState(State.Sta1);
    }

    synchronized private void closeSocketDelayed() {
        state.closeSocketDelayed(this);
    }

    void doCloseSocketDelayed() {
        enterState(State.Sta13);
        device.schedule(new Runnable() {

            @Override
            public void run() {
                closeSocket();
            }
        }, conn.getSocketCloseDelay());
    }

    synchronized void onIOException(IOException e) {
        if (ex != null)
            return;

        ex = e;
        LOG.info("{}: i/o exception: {} in State: {}",
                new Object[] { name, e, state });
        closeSocket();
    }

    void write(AAbort aa) throws IOException  {
        LOG.info("{} << {}", name, aa);
        encoder.write(aa);
        closeSocketDelayed();
    }

    void writeAReleaseRQ() throws IOException {
        LOG.info("{} << A-RELEASE-RQ", name);
        enterState(State.Sta7);
        encoder.writeAReleaseRQ();
        startARTIM(conn.getReleaseTimeout());
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
        LOG.info("{} << A-ASSOCIATE-RQ", name);
        LOG_AARQAC.debug("{}", rq);
        enterState(State.Sta5);
        encoder.write(rq);
    }

    private void write(AAssociateAC ac) throws IOException {
        LOG.info("{} << A-ASSOCIATE-AC", name);
        LOG_AARQAC.debug("{}", ac);
        enterState(State.Sta6);
        encoder.write(ac);
        startCheckForStaleness();
    }

    private void write(AAssociateRJ e) throws IOException {
        Association.LOG.info("{} << {}", name, e);
        encoder.write(e);
        closeSocketDelayed();
    }

    private void startARTIM(int timeout) {
        LOG.debug("{}: start ARTIM {}ms", name, timeout);
        try {
            sock.setSoTimeout(timeout);
        } catch (SocketException e) {
            LOG.warn(name, e);
        }
    }

    private void stopARTIM() {
        LOG.debug("{}: stop ARTIM", name);
        try {
            sock.setSoTimeout(0);
        } catch (SocketException e) {
            LOG.warn(name, e);
        }
    }

    private void checkException() throws IOException {
        if (ex != null)
            throw ex;
    }

    private synchronized void enterState(State newState) {
        LOG.debug("{}: enter state: {}", name, newState);
        this.state = newState;
        notifyAll();
    }

    synchronized void waitForLeaving(State state)
            throws InterruptedException, IOException {
        while (this.state == state)
            wait();
        checkException();
    }

    void activate() {
        device.execute(new Runnable() {

            @Override
            public void run() {
                decoder = new PDUDecoder(Association.this, in);
                device.incrementNumberOfOpenConnections();
                startARTIM(requestor
                        ? conn.getAcceptTimeout()
                        : conn.getRequestTimeout());
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
        if (scheduleAtFixedRate != null) {
            scheduleAtFixedRate.cancel(false);
        }
        if (ae != null)
            ae.onClose(this);
    }

    void onAAssociateRQ(AAssociateRQ rq) throws IOException {
        LOG.info("{} >> A-ASSOCIATE-RQ", name);
        LOG_AARQAC.debug("{}", rq);
        state.onAAssociateRQ(this, rq);
    }

    void handle(AAssociateRQ rq) throws IOException {
        this.rq = rq;
        name = rq.getCallingAET() + delim() + serialNo;
        stopARTIM();
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
        LOG.info("{} >> A-ASSOCIATE-AC", name);
        LOG_AARQAC.debug("{}", ac);
        state.onAAssociateAC(this, ac);
    }

    void handle(AAssociateAC ac) throws IOException {
        this.ac = ac;
        initPCMap();
        maxOpsInvoked = ac.getMaxOpsInvoked();
        maxPDULength = ApplicationEntity.minZeroAsMax(
                ac.getMaxPDULength(), ae.getMaxPDULengthSend());
        stopARTIM();
        enterState(State.Sta6);
        startCheckForStaleness();
    }

    private void startCheckForStaleness() {
        final int period = conn.getCheckForStalenessPeriod();
        if (period > 0) {
            scheduleAtFixedRate = device.scheduleAtFixedRate(new Runnable(){

                @Override
                public void run() {
                    checkForStaleness();
                }}, period);
        }
    }

    private void checkForStaleness() {
        if (rspHandlerForMsgId.isEmpty()) {
            releaseStale("{}: idle timeout expired", idleTimeout);
        } else
            synchronized (rspHandlerForMsgId) {
                IntHashMap.Visitor<DimseRSPHandler> visitor =
                        new IntHashMap.Visitor<DimseRSPHandler>() {
    
                    @Override
                    public boolean visit(int key, DimseRSPHandler value) {
                        return !releaseStale("{}: response timeout expired",
                                value.getTimeout());
                    }
                };
                rspHandlerForMsgId.accept(visitor);
            }
    }

    private boolean releaseStale(String logmsg, long timeout) {
        if (timeout == 0 || timeout > System.currentTimeMillis())
            return false;

        LOG.info(logmsg, this);
        try {
            release();
        } catch (IOException e) {
            // already handled by onIOException()
        }
        return true;
    }

    private void updateIdleTimeout() {
        int timeout = conn.getIdleTimeout();
        if (timeout > 0)
            this.idleTimeout = System.currentTimeMillis() + timeout;
    }

    void onAAssociateRJ(AAssociateRJ rj) throws IOException {
        LOG.info("{} >> {}", name, rj);
        state.onAAssociateRJ(this, rj);
    }

    void handle(AAssociateRJ rq) {
        ex = rq;
        closeSocket();
    }

    void onAReleaseRQ() throws IOException {
        LOG.info("{} >> A-RELEASE-RQ", name);
        state.onAReleaseRQ(this);
    }

    void handleAReleaseRQ() throws IOException {
        enterState(State.Sta8);
        waitForPerformingOps();
        LOG.info("{} << A-RELEASE-RP", name);
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
            LOG.info("{} << A-RELEASE-RP", name);
            encoder.writeAReleaseRP();
            enterState(State.Sta11);
        } else {
            enterState(State.Sta10);
        }
    }

    void onAReleaseRP() throws IOException {
        LOG.info("{} >> A-RELEASE-RP", name);
        state.onAReleaseRP(this);
    }

    void handleAReleaseRP() throws IOException {
        stopARTIM();
        closeSocket();
    }

   void handleAReleaseRPCollision() throws IOException {
        stopARTIM();
        enterState(State.Sta12);
        LOG.info("{} << A-RELEASE-RP", name);
        encoder.writeAReleaseRP();
        closeSocketDelayed();
   }

    void onAAbort(AAbort aa) {
        LOG.info("{} << {}", name, aa);
        ex = aa;
        closeSocket();
    }

    void unexpectedPDU(String pdu) throws AAbort {
        LOG.warn("{} >> unexpected {} in state: {}",
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
        state.writePDataTF(this);
    }

    void doWritePDataTF() throws IOException {
        encoder.writePDataTF();
    }

    void onDimseRQ(PresentationContext pc, Attributes cmd,
            PDVInputStream data) throws IOException {
        updateIdleTimeout();
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
        updateIdleTimeout();
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo, -1);
        DimseRSPHandler rspHandler = getDimseRSPHandler(msgId);
        if (rspHandler == null) {
            LOG.info("{}: unexpected message ID in DIMSE RSP:", name);
            LOG.info("{}", cmd);
            abort();
        }
        try {
            rspHandler.onDimseRSP(this, cmd, data);
        } finally {
            if (!Commands.hasPendingStatus(cmd)) {
                removeDimseRSPHandler(msgId);
                removeCancelRQHandler(msgId);
            } else {
                int timeout = Commands.isRetrieveRSP(cmd)
                        ? conn.getRetrieveRSPTimeout()
                        : conn.getDimseRSPTimeout();
                rspHandler.updateTimeout(timeout);
            }
        }
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

    private void removeDimseRSPHandler(int msgId) {
        synchronized (rspHandlerForMsgId ) {
            rspHandlerForMsgId.remove(msgId);
            rspHandlerForMsgId.notifyAll();
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
        if (!Commands.hasPendingStatus(cmd))
            decPerforming();
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

    private void invoke(PresentationContext pc, Attributes cmd,
            DataWriter data, DimseRSPHandler rspHandler, int rspTimeout)
            throws IOException, InterruptedException {
        checkException();
        updateIdleTimeout();
        rspHandler.setPC(pc);
        addDimseRSPHandler(rspHandler);
        encoder.writeDIMSE(pc, cmd, data);
        rspHandler.updateTimeout(rspTimeout);
    }

}
