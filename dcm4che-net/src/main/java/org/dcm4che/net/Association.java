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
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.dcm4che.data.UID;
import org.dcm4che.net.pdu.AAbort;
import org.dcm4che.net.pdu.AAssociateAC;
import org.dcm4che.net.pdu.AAssociateRJ;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.PDUEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Association {

    static final Logger LOG =
            LoggerFactory.getLogger(Association.class);

    private static final AtomicInteger prevSerialNo = new AtomicInteger();

    private final int serialNo;
    private String name;
    private ApplicationEntity ae;
    private final Device device;
    private final Connection conn;
    private Connection remote;
    private Socket sock;
    private final InputStream in;
    private final OutputStream out;
    private final PDUEncoder encoder;
    private PDUDecoder decoder;
    private State state;
    private AAssociateRQ rq;
    private AAssociateAC ac;
    private IOException ex;

    Association(Connection local, Socket sock,  State state)
            throws IOException {
        this.serialNo = prevSerialNo.incrementAndGet();
        this.name = "Association(" + serialNo + ')';
        this.conn = local;
        this.device = local.getDevice();
        this.sock = sock;
        this.in = sock.getInputStream();
        this.out = sock.getOutputStream();
        this.encoder = new PDUEncoder(this, out);
        enterState(state);
    }

    @Override
    public String toString() {
        return name;
    }

    final void setRemoteConnection(Connection remote) {
        this.remote = remote;
    }

    public final ApplicationEntity getApplicationEntity() {
        return ae;
    }

    final void setApplicationEntity(ApplicationEntity ae) {
        this.ae = ae;
    }

    public boolean isRequestor() {
        return remote != null;
    }

    public void release() throws IOException {
        if (!isRequestor())
            throw new IllegalStateException("is not association-requestor");
        state.writeAReleaseRQ(this);
    }

    public void abort(AAbort aa) {
        state.write(this, aa);
    }

    void write(AAbort aa) {
        LOG.info("{} << {}", name, aa);
        enterState(State.Sta13);
        try {
            encoder.write(aa);
        } catch (IOException e) {
            LOG.debug("{}: failed to write {}", name, aa);
        }
        ex = aa;
    }

    void writeAReleaseRQ() throws IOException {
        LOG.info("{} << A-RELEASE-RQ", name);
        enterState(State.Sta7);
        encoder.writeAReleaseRQ();
        startARTIM(remote.getReleaseTimeout());
    }

    public void waitForOutstandingRSP() {
        // TODO Auto-generated method stub
        
    }

    void write(AAssociateRQ rq) throws IOException {
        name = rq.getCalledAET() + '(' + serialNo + ")";
        this.rq = rq;
        LOG.info("{} << A-ASSOCIATE-RQ", name);
        LOG.debug("{}", rq);
        enterState(State.Sta5);
        encoder.write(rq);
    }

    private void write(AAssociateAC ac) throws IOException {
        this.ac = ac;
        LOG.info("{} << A-ASSOCIATE-AC", name);
        LOG.debug("{}", ac);
        enterState(State.Sta6);
        encoder.write(ac);
    }

    void startARTIM(int timeout) throws IOException {
        LOG.debug("{}: start ARTIM {}ms", name, timeout);
        sock.setSoTimeout(timeout);
    }

    private void stopARTIM() throws IOException {
        sock.setSoTimeout(0);
        LOG.debug("{}: stop ARTIM", name);
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
        if (!(state == State.Sta2 || state == State.Sta5))
                throw new IllegalStateException("state: " + state);

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
                } catch (SocketTimeoutException e) {
                    ex = e;
                    LOG.warn("{}: ARTIM timer expired in State: {}",
                            name, state);
                } catch (IOException e) {
                    ex = e;
                    LOG.warn("{}: i/o exception: {} in State: {}",
                            new Object[] { name, e, state });
                } finally {
                    device.decrementNumberOfOpenConnections();
                    closeSocket();
                }
            }
        });
    }

    private void closeSocket() {
        if (sock == null)
            return;

        if (state == State.Sta13) {
            try {
                Thread.sleep(conn.getSocketCloseDelay());
            } catch (InterruptedException e) {
                LOG.warn("Interrupted Socket Close Delay", e);
            }
        }
        try { out.close(); } catch (IOException ignore) {}
        try { in.close(); } catch (IOException ignore) {}
        if (sock != null) {
            LOG.info("{}: close {}", name, sock);
            try { sock.close(); } catch (IOException ignore) {}
            sock = null;
        }
        enterState(State.Sta1);
    }

    void onAAssociateRQ(AAssociateRQ rq) throws IOException {
        LOG.info("{} >> A-ASSOCIATE-RQ", name);
        LOG.debug("{}", rq);
        state.onAAssociateRQ(this, rq);
    }

    void handle(AAssociateRQ rq) throws IOException {
        this.rq = rq;
        name = rq.getCallingAET() + '(' + serialNo + ")";
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
            write(ae.negotiate(this, rq));
            enterState(State.Sta6);
        } catch (AAssociateRJ e) {
            Association.LOG.info("{} << {}", name, e);
            enterState(State.Sta13);
            encoder.write(e);
        }
    }

    void onAAssociateAC(AAssociateAC ac) throws IOException {
        LOG.info("{} >> A-ASSOCIATE-AC", name);
        LOG.debug("{}", ac);
        state.onAAssociateAC(this, ac);
    }

    void handle(AAssociateAC ac) throws IOException {
        this.ac = ac;
        stopARTIM();
        enterState(State.Sta6);
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
        LOG.info("{} << A-RELEASE-RP", name);
        enterState(State.Sta13);
        encoder.writeAReleaseRP();
    }

    void handleAReleaseRQCollision() throws IOException {
        if (isRequestor()) {
            enterState(State.Sta9);
            LOG.info("{} << A-RELEASE-RP", name);
            enterState(State.Sta11);
            encoder.writeAReleaseRP();
       } else {
            enterState(State.Sta10);
            try {
                waitForLeaving(State.Sta10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            enterState(State.Sta13);
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

    void handlePDataTF() {
    }
}
