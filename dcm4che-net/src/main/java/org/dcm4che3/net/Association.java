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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.pdu.AAbort;
import org.dcm4che3.net.pdu.AAssociateAC;
import org.dcm4che3.net.pdu.AAssociateRJ;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.CommonExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.RoleSelection;
import org.dcm4che3.util.IntHashMap;
import org.dcm4che3.util.SafeClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Association {

    public static final Logger LOG = LoggerFactory.getLogger(Association.class);

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
    private Timeout timeout;
    private final IntHashMap<DimseRSPHandler> rspHandlerForMsgId =
            new IntHashMap<DimseRSPHandler>();
    private final IntHashMap<CancelRQHandler> cancelHandlerForMsgId =
            new IntHashMap<CancelRQHandler>();
    private final HashMap<String,HashMap<String,PresentationContext>> pcMap =
            new HashMap<String,HashMap<String,PresentationContext>>();

    Association(ApplicationEntity ae, Connection local, Socket sock)
            throws IOException {
        this.serialNo = prevSerialNo.incrementAndGet();
        this.ae = ae;
        this.requestor = ae != null;
        this.name = "" + sock.getLocalSocketAddress()
             + delim() + sock.getRemoteSocketAddress()
             + '(' + serialNo + ')';
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

    public Device getDevice() {
         return device;
    }

    public int nextMessageID() {
        return messageID.incrementAndGet() & 0xFFFF;
    }

    private String delim() {
        return requestor ? "->" : "<-";
    }

    @Override
    public String toString() {
        return name;
    }

    public final Socket getSocket() {
        return sock;
    }

    public final Connection getConnection() {
        return conn;
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

    @SuppressWarnings("unchecked")
    public <T> T getProperty(Class<T> clazz) {
        return (T) getProperty(clazz.getName());
    }

    public void setProperty(Object value) {
        setProperty(value.getClass().getName(), value);
    }

    public boolean containsProperty(String key) {
        return properties != null && properties.containsKey(key);
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

    public boolean isSCPFor(String cuid) {
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

    public boolean isSCUFor(String cuid) {
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

    public String getRemoteImplVersionName() {
        return (requestor ? ac : rq).getImplVersionName();
    }

    public String getRemoteImplClassUID() {
        return (requestor ? ac : rq).getImplClassUID();
    }

    public String getLocalImplVersionName() {
        return (requestor ? rq : ac).getImplVersionName();
    }

    public String getLocalImplClassUID() {
        return (requestor ? rq : ac).getImplClassUID();
    }

    final int getMaxPDULengthSend() {
        return maxPDULength;
    }

    boolean isPackPDV() {
        return conn.isPackPDV();
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
        int delay = conn.getSocketCloseDelay();
        if (delay > 0)
            device.schedule(new Runnable() {

                @Override
                public void run() {
                    closeSocket();
                }
            }, delay, TimeUnit.MILLISECONDS);
        else
            closeSocket();
    }

    synchronized void onIOException(IOException e) {
        if (ex != null)
            return;

        ex = e;
        LOG.warn("{}: i/o exception: {} in State: {}",
                new Object[] { name, e, state });
        closeSocket();
    }

    void write(AAbort aa) throws IOException  {
        LOG.info("{} << {}", name, aa);
        encoder.write(aa);
        ex = aa;
        closeSocketDelayed();
    }

    void writeAReleaseRQ() throws IOException {
        LOG.info("{} << A-RELEASE-RQ", name);
        enterState(State.Sta7);
        stopTimeout();
        encoder.writeAReleaseRQ();
        startReleaseTimeout();
    }

    private void startRequestTimeout() {
        startTimeout("{}: start A-ASSOCIATE-RQ timeout of {}ms",
                "{}: A-ASSOCIATE-RQ timeout expired",
                "{}: stop A-ASSOCIATE-RQ timeout",
                conn.getRequestTimeout(), State.Sta2);
    }

    private void startAcceptTimeout() {
        startTimeout("{}: start A-ASSOCIATE-AC timeout of {}ms",
                "{}: A-ASSOCIATE-AC timeout expired",
                "{}: stop A-ASSOCIATE-AC timeout",
                conn.getAcceptTimeout(), State.Sta5);
    }

    private void startReleaseTimeout() {
        startTimeout("{}: start A-RELEASE-RP timeout of {}ms",
                "{}: A-RELEASE-RP timeout expired",
                "{}: stop A-RELEASE-RP timeout",
                conn.getReleaseTimeout(), State.Sta7);
    }

    private void startIdleTimeout() {
        startTimeout("{}: start idle timeout of {}ms",
                "{}: idle timeout expired",
                "{}: stop idle timeout",
                conn.getIdleTimeout(), State.Sta6);
    }

    private void startTimeout(String startMsg, String expiredMsg,
            String cancelMsg, int timeout, State state) {
        if (timeout > 0 && performing == 0 && rspHandlerForMsgId.isEmpty()) {
            synchronized (this) {
                if (this.state == state) {
                    stopTimeout();
                    this.timeout = Timeout.start(this, startMsg, expiredMsg,
                            cancelMsg, timeout);
                }
            }
        }
    }

    private void startTimeout(final int msgID, int timeout) {
        if (timeout > 0) {
            synchronized (rspHandlerForMsgId) {
                DimseRSPHandler rspHandler = rspHandlerForMsgId.get(msgID);
                if (rspHandler != null) {
                    rspHandler.setTimeout(Timeout.start(this,
                        "{}: start " + msgID + ":DIMSE-RSP timeout of {}ms",
                        "{}: " + msgID + ":DIMSE-RSP timeout expired",
                        "{}: stop " + msgID + ":DIMSE-RSP timeout",
                        timeout));
                }
            }
        }
    }

    private synchronized void stopTimeout() {
        if (timeout != null) {
            timeout.stop();
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
        name = rq.getCallingAET() + delim() + rq.getCalledAET() + '(' + serialNo + ')';
        this.rq = rq;
        LOG.info("{} << A-ASSOCIATE-RQ", name);
        LOG.debug("{}", rq);
        enterState(State.Sta5);
        encoder.write(rq);
        startAcceptTimeout();
    }

    private void write(AAssociateAC ac) throws IOException {
        LOG.info("{} << A-ASSOCIATE-AC", name);
        LOG.debug("{}", ac);
        enterState(State.Sta6);
        encoder.write(ac);
        startIdleTimeout();
    }

    private void write(AAssociateRJ e) throws IOException {
        LOG.info("{} << {}", name, e);
        encoder.write(e);
        closeSocketDelayed();
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

    public final State getState() {
        return state;
    }

    synchronized void waitForLeaving(State state)
            throws InterruptedException, IOException {
        while (this.state == state)
            wait();
        checkException();
    }

    synchronized void waitForEntering(State state)
            throws InterruptedException, IOException {
        while (this.state != state)
            wait();
        checkException();
    }

    public void waitForSocketClose()
            throws InterruptedException, IOException {
        waitForEntering(State.Sta1);
    }

    private void activate() {
        device.execute(new Runnable() {

            @Override
            public void run() {
                decoder = new PDUDecoder(Association.this, in);
                device.incrementNumberOfOpenAssociations();
                try {
                    try {
                        while (!(state == State.Sta1 || state == State.Sta13))
                            decoder.nextPDU();
                    } catch (AAbort aa) {
                        abort(aa);
                    } catch (IOException e) {
                        onIOException(e);
                    } finally {
                        onClose();
                    }
                } finally {
                    device.decrementNumberOfOpenAssociations();
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
            ae.getDevice().getAssociationHandler().onClose(this);
    }

    void onAAssociateRQ(AAssociateRQ rq) throws IOException {
        name = rq.getCalledAET() + delim() + rq.getCallingAET() + '(' + serialNo + ')';
        LOG.info("{} >> A-ASSOCIATE-RQ", name);
        LOG.debug("{}", rq);
        stopTimeout();
        state.onAAssociateRQ(this, rq);
    }

    void handle(AAssociateRQ rq) throws IOException {
        this.rq = rq;
        enterState(State.Sta3);
        try {
            ae = device.getApplicationEntity(rq.getCalledAET());
            ac = device.getAssociationHandler().negotiate(this, rq);
            initPCMap();
            maxOpsInvoked = ac.getMaxOpsPerformed();
            maxPDULength = Association.minZeroAsMax(
                    rq.getMaxPDULength(), conn.getSendPDULength());
            write(ac);
        } catch (AAssociateRJ e) {
            write(e);
        }
    }

    void onAAssociateAC(AAssociateAC ac) throws IOException {
        LOG.info("{} >> A-ASSOCIATE-AC", name);
        LOG.debug("{}", ac);
        stopTimeout();
        state.onAAssociateAC(this, ac);
    }

    void handle(AAssociateAC ac) throws IOException {
        this.ac = ac;
        initPCMap();
        maxOpsInvoked = ac.getMaxOpsInvoked();
        maxPDULength = Association.minZeroAsMax(
                ac.getMaxPDULength(), conn.getSendPDULength());
        enterState(State.Sta6);
        startIdleTimeout();
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
        stopTimeout();
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
        stopTimeout();
        state.onAReleaseRP(this);
    }

    void handleAReleaseRP() throws IOException {
        closeSocket();
    }

   void handleAReleaseRPCollision() throws IOException {
        enterState(State.Sta12);
        LOG.info("{} << A-RELEASE-RP", name);
        encoder.writeAReleaseRP();
        closeSocketDelayed();
   }

    void onAAbort(AAbort aa) {
        LOG.info("{} >> {}", name, aa);
        stopTimeout();
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
        checkException();
        state.writePDataTF(this);
    }

    void doWritePDataTF() throws IOException {
        encoder.writePDataTF();
    }

    void onDimseRQ(PresentationContext pc, Dimse dimse, Attributes cmd,
            PDVInputStream data) throws IOException {
        stopTimeout();
        incPerforming();
        ae.onDimseRQ(this, pc, dimse, cmd, data);
    }

    private synchronized void incPerforming() {
        ++performing;
    }

    private synchronized void decPerforming() {
        --performing;
        notifyAll();
    }

    void onDimseRSP(Dimse dimse, Attributes cmd, Attributes data) throws AAbort {
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo, -1);
        int status = cmd.getInt(Tag.Status, 0);
        boolean pending = Status.isPending(status);
        DimseRSPHandler rspHandler = getDimseRSPHandler(msgId);
        if (rspHandler == null) {
            Dimse.LOG.info("{}: unexpected message ID in DIMSE RSP:", name);
            Dimse.LOG.info("\n{}", cmd);
            throw new AAbort();
        }
        rspHandler.onDimseRSP(this, cmd, data);
        if (pending)
            startTimeout(msgId, dimse.isRetrieveRQ()
                    ? conn.getRetrieveTimeout()
                    : conn.getResponseTimeout());
        else {
            removeDimseRSPHandler(msgId);
            if (rspHandlerForMsgId.isEmpty() && performing == 0)
                startIdleOrReleaseTimeout();
        }
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

    public boolean tryWriteDimseRSP(PresentationContext pc, Attributes cmd) {
        return tryWriteDimseRSP(pc, cmd, null);
    }

    public boolean tryWriteDimseRSP(PresentationContext pc, Attributes cmd,
            Attributes data) {
        try {
            writeDimseRSP(pc, cmd, data);
            return true;
        } catch (IOException e) {
            LOG.warn("{} << {} failed: {}", new Object[] {
                        this,
                        Dimse.valueOf(cmd.getInt(Tag.CommandField, 0)),
                        e.getMessage() });
            return false;
        }
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
        if (!Status.isPending(cmd.getInt(Tag.Status, 0))) {
            decPerforming();
            startIdleTimeout();
        }
    }

    void onCancelRQ(Attributes cmd) throws IOException {
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo, -1);
        CancelRQHandler handler = removeCancelRQHandler(msgId);
        if (handler != null)
            handler.onCancelRQ(this);
    }

    public void addCancelRQHandler(int msgId, CancelRQHandler handler) {
        synchronized (cancelHandlerForMsgId) {
            cancelHandlerForMsgId.put(msgId, handler);
        }
    }

    public CancelRQHandler removeCancelRQHandler(int msgId) {
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

    public Set<String> getTransferSyntaxesFor(String cuid) {
        HashMap<String, PresentationContext> tsMap = pcMap.get(cuid);
        if (tsMap == null)
            return Collections.emptySet();
        return Collections.unmodifiableSet(tsMap.keySet());
    }

    PresentationContext getPresentationContext(int pcid) {
        return ac.getPresentationContext(pcid);
    }

    public CommonExtendedNegotiation getCommonExtendedNegotiationFor(
            String cuid) {
        return ac.getCommonExtendedNegotiationFor(cuid);
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
        invoke(pc, cstorerq, data, rspHandler, conn.getResponseTimeout());
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
        invoke(pc, cstorerq, data, rspHandler, conn.getResponseTimeout());
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
                conn.getResponseTimeout());
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

    public void cget(String cuid, int priority, Attributes data,
            String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        cget(cuid, cuid, priority, data, tsuid, rspHandler);
    }

    public void cget(String asuid, String cuid, int priority,
            Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException,
            InterruptedException {
        PresentationContext pc = pcFor(asuid, tsuid);
        checkIsSCU(cuid);
        Attributes cgetrq = Commands.mkCGetRQ(rspHandler.getMessageID(),
                cuid, priority);
        invoke(pc, cgetrq, new DataWriterAdapter(data), rspHandler,
                conn.getRetrieveTimeout());
    }

    public DimseRSP cget(String cuid, int priority, Attributes data,
            String tsuid) throws IOException,
            InterruptedException {
        return cget(cuid, cuid, priority, data, tsuid);
    }

    public DimseRSP cget(String asuid, String cuid, int priority,
            Attributes data, String tsuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        cget(asuid, cuid, priority, data, tsuid, rsp);
        return rsp;
    }

    public void cmove(String cuid, int priority, Attributes data,
            String tsuid, String destination, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        cmove(cuid, cuid, priority, data, tsuid, destination, rspHandler);
    }

    public void cmove(String asuid, String cuid, int priority,
            Attributes data, String tsuid, String destination,
            DimseRSPHandler rspHandler) throws IOException,
            InterruptedException {
        PresentationContext pc = pcFor(asuid, tsuid);
        checkIsSCU(cuid);
        Attributes cmoverq = Commands.mkCMoveRQ(rspHandler.getMessageID(),
                cuid, priority, destination);
        invoke(pc, cmoverq, new DataWriterAdapter(data), rspHandler,
                conn.getRetrieveTimeout());
    }

    public DimseRSP cmove(String cuid, int priority, Attributes data,
            String tsuid, String destination) throws IOException,
            InterruptedException {
        return cmove(cuid, cuid, priority, data, tsuid, destination);
    }

    public DimseRSP cmove(String asuid, String cuid, int priority,
            Attributes data, String tsuid, String destination)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        cmove(asuid, cuid, priority, data, tsuid, destination, rsp);
        return rsp;
    }

    public DimseRSP cecho() throws IOException, InterruptedException {
        return cecho(UID.VerificationSOPClass);
    }

    public DimseRSP cecho(String cuid) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        PresentationContext pc = pcFor(cuid, null);
        checkIsSCU(cuid);
        Attributes cechorq = Commands.mkCEchoRQ(rsp.getMessageID(), cuid);
        invoke(pc, cechorq, null, rsp, conn.getResponseTimeout());
        return rsp;
    }

    public void neventReport(String cuid, String iuid, int eventTypeId,
            Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        neventReport(cuid, cuid, iuid, eventTypeId, data, tsuid, rspHandler);
    }

    public void neventReport(String asuid, String cuid, String iuid, int eventTypeId,
            Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        PresentationContext pc = pcFor(asuid, tsuid);
        checkIsSCP(cuid);
        Attributes neventrq =
                Commands.mkNEventReportRQ(rspHandler.getMessageID(), cuid, iuid,
                        eventTypeId, data);
        invoke(pc, neventrq, DataWriterAdapter.forAttributes(data), rspHandler,
                conn.getResponseTimeout());
    }

    public DimseRSP neventReport(String cuid, String iuid, int eventTypeId,
            Attributes data, String tsuid) throws IOException,
            InterruptedException {
        return neventReport(cuid, cuid, iuid, eventTypeId, data, tsuid);
    }

    public DimseRSP neventReport(String asuid, String cuid, String iuid,
            int eventTypeId, Attributes data, String tsuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        neventReport(asuid, cuid, iuid, eventTypeId, data, tsuid, rsp);
        return rsp;
    }

    public void nget(String cuid, String iuid, int[] tags,
            DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        nget(cuid, cuid, iuid, tags, rspHandler);
    }

    public void nget(String asuid, String cuid, String iuid, int[] tags,
            DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        PresentationContext pc = pcFor(asuid, null);
        checkIsSCU(cuid);
        Attributes ngetrq =
                Commands.mkNGetRQ(rspHandler.getMessageID(), cuid, iuid, tags);
        invoke(pc, ngetrq, null, rspHandler, conn.getResponseTimeout());
    }

    public DimseRSP nget(String cuid, String iuid, int[] tags)
            throws IOException, InterruptedException {
        return nget(cuid, cuid, iuid, tags);
    }

    public DimseRSP nget(String asuid, String cuid, String iuid, int[] tags)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        nget(asuid, cuid, iuid, tags, rsp);
        return rsp;
    }

    public void nset(String cuid, String iuid, Attributes data,
            String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        nset(cuid, cuid, iuid, new DataWriterAdapter(data), tsuid, rspHandler);
    }

    public void nset(String asuid, String cuid, String iuid,
            Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        nset(asuid, cuid, iuid, new DataWriterAdapter(data), tsuid, rspHandler);
    }

    public DimseRSP nset(String cuid, String iuid, Attributes data,
            String tsuid) throws IOException,
            InterruptedException {
        return nset(cuid, cuid, iuid, new DataWriterAdapter(data), tsuid);
    }

    public DimseRSP nset(String asuid, String cuid, String iuid,
            Attributes data, String tsuid)
            throws IOException, InterruptedException {
        return nset(asuid, cuid, iuid, new DataWriterAdapter(data), tsuid);
    }

    public void nset(String cuid, String iuid, DataWriter data,
            String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        nset(cuid, cuid, iuid, data, tsuid, rspHandler);
    }

    public void nset(String asuid, String cuid, String iuid,
            DataWriter data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        PresentationContext pc = pcFor(asuid, tsuid);
        checkIsSCU(cuid);
        Attributes nsetrq =
                Commands.mkNSetRQ(rspHandler.getMessageID(), cuid, iuid);
        invoke(pc, nsetrq, data, rspHandler, conn.getResponseTimeout());
    }

    public DimseRSP nset(String cuid, String iuid, DataWriter data,
            String tsuid) throws IOException,
            InterruptedException {
        return nset(cuid, cuid, iuid, data, tsuid);
    }

    public DimseRSP nset(String asuid, String cuid, String iuid,
            DataWriter data, String tsuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        nset(asuid, cuid, iuid, data, tsuid, rsp);
        return rsp;
    }

    public void naction(String cuid, String iuid, int actionTypeId,
            Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        naction(cuid, cuid, iuid, actionTypeId, data, tsuid, rspHandler);
    }

    public void naction(String asuid, String cuid, String iuid, int actionTypeId,
            Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        PresentationContext pc = pcFor(asuid, tsuid);
        checkIsSCU(cuid);
        Attributes nactionrq =
                Commands.mkNActionRQ(rspHandler.getMessageID(), cuid, iuid,
                        actionTypeId, data);
        invoke(pc, nactionrq, DataWriterAdapter.forAttributes(data), rspHandler,
                conn.getResponseTimeout());
    }

    public DimseRSP naction(String cuid, String iuid, int actionTypeId,
            Attributes data, String tsuid) throws IOException,
            InterruptedException {
        return naction(cuid, cuid, iuid, actionTypeId, data, tsuid);
    }

    public DimseRSP naction(String asuid, String cuid, String iuid,
            int actionTypeId, Attributes data, String tsuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        naction(asuid, cuid, iuid, actionTypeId, data, tsuid, rsp);
        return rsp;
    }

    public void ncreate(String cuid, String iuid,  Attributes data,
            String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        ncreate(cuid, cuid, iuid, data, tsuid, rspHandler);
    }

    public void ncreate(String asuid, String cuid, String iuid,
            Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        PresentationContext pc = pcFor(asuid, tsuid);
        checkIsSCU(cuid);
        Attributes ncreaterq =
                Commands.mkNCreateRQ(rspHandler.getMessageID(), cuid, iuid);
        invoke(pc, ncreaterq, DataWriterAdapter.forAttributes(data), rspHandler,
                conn.getResponseTimeout());
    }

    public DimseRSP ncreate(String cuid, String iuid, Attributes data,
            String tsuid) throws IOException,
            InterruptedException {
        return ncreate(cuid, cuid, iuid, data, tsuid);
    }

    public DimseRSP ncreate(String asuid, String cuid, String iuid,
            Attributes data, String tsuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        ncreate(asuid, cuid, iuid, data, tsuid, rsp);
        return rsp;
    }

    public void ndelete(String cuid, String iuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        ndelete(cuid, cuid, iuid, rspHandler);
    }

    public void ndelete(String asuid, String cuid, String iuid,
            DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        PresentationContext pc = pcFor(asuid, null);
        checkIsSCU(cuid);
        Attributes ndeleterq =
                Commands.mkNDeleteRQ(rspHandler.getMessageID(), cuid, iuid);
        invoke(pc, ndeleterq, null, rspHandler, conn.getResponseTimeout());
    }

    public DimseRSP ndelete(String cuid, String iuid)
            throws IOException, InterruptedException {
        return ndelete(cuid, cuid, iuid);
    }

    public DimseRSP ndelete(String asuid, String cuid, String iuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        ndelete(asuid, cuid, iuid, rsp);
        return rsp;
    }

    private void invoke(PresentationContext pc, Attributes cmd,
            DataWriter data, DimseRSPHandler rspHandler, int rspTimeout)
            throws IOException, InterruptedException {
        stopTimeout();
        checkException();
        rspHandler.setPC(pc);
        addDimseRSPHandler(rspHandler);
        startTimeout(rspHandler.getMessageID(), rspTimeout);
        encoder.writeDIMSE(pc, cmd, data);
    }

    static int minZeroAsMax(int i1, int i2) {
        return i1 == 0 ? i2 : i2 == 0 ? i1 : Math.min(i1, i2);
    }

    public Attributes createFileMetaInformation(String iuid, String cuid,
            String tsuid) {
        Attributes fmi = new Attributes(7);
        fmi.setBytes(Tag.FileMetaInformationVersion, VR.OB, new byte[] { 0, 1 });
        fmi.setString(Tag.MediaStorageSOPClassUID, VR.UI, cuid);
        fmi.setString(Tag.MediaStorageSOPInstanceUID, VR.UI, iuid);
        fmi.setString(Tag.TransferSyntaxUID, VR.UI, tsuid);
        fmi.setString(Tag.ImplementationClassUID, VR.UI,
                getRemoteImplClassUID());
        String versionName = getRemoteImplVersionName();
        if (versionName != null)
            fmi.setString(Tag.ImplementationVersionName, VR.SH, versionName);
        fmi.setString(Tag.SourceApplicationEntityTitle, VR.SH,
                getRemoteAET());
        return fmi;
    }

    /**
     * Releases the association in a graceful way.
     * <ul>
     *     <li>makes sure the current state is eligible for calling a release() before doing so</li>
     *     <li>waits for outstanding RSP before release</li>
     *     <li>handles exceptions and logs detected issues</li>
     * </ul>
     *
     * This method will not throw exceptions normally.
     */
    public void releaseGracefully() {
        if (isReadyForDataTransfer()) {

            try {
                waitForOutstandingRSP();
            } catch (InterruptedException ignored) {
                // if we get interrupted while trying to close the association, most likely we still want to close it
                Thread.currentThread().interrupt();
                LOG.warn("Interrupted while preparing to close the association, will try to release the association anyway: " + this.toString(), ignored);
            }

            try {
                release();
            } catch (IOException e) {
                LOG.warn("Failed to release association to " + getRemoteAET(), e);
            }

        } else {
            LOG.warn("Attempted to close the association, but it was not ready for data transfer", new IOException("Association not ready for data transfer"));
        }
    }
}

