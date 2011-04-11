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
import java.util.concurrent.Executor;
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

    private static final Logger LOG =
            LoggerFactory.getLogger(Association.class);

    private static final AtomicInteger prevSerialNo = new AtomicInteger();

    private final int serialNo;
    private final Device device;
    private final AbstractConnection conn;
    private Socket sock;
    private final InputStream in;
    private final OutputStream out;
    private final PDUEncoder encoder;
    private final PDUDecoder decoder;
    private String name;
    private State state;
    private AAssociateRQ rq;
    private AAssociateAC ac;
    private IOException ex;

    private Association(AbstractConnection conn, Socket sock, State state)
            throws IOException {
        this.serialNo = prevSerialNo.incrementAndGet();
        this.name = "Association(" + serialNo + ')';
        this.conn = conn;
        this.device = conn.getDevice();
        this.sock = sock;
        this.in = sock.getInputStream();
        this.out = sock.getOutputStream();
        this.encoder = new PDUEncoder(this, out);
        this.decoder = new PDUDecoder(this, in);
        enterState(state);
    }

    private Association(OutboundConnection local, Socket sock)
            throws IOException {
        this(local, sock, State.Sta4);
    }

    private Association(InboundConnection local, Socket sock)
            throws IOException {
        this(local, sock, State.Sta2);
        startARTIM(local.getRequestTimeout());
    }

    public static Association connect(OutboundConnection local,
            InboundConnection remote, AAssociateRQ rq, Executor executer)
            throws IOException, InterruptedException {
        Association as = new Association(local, local.connect(remote));
        as.write(rq, remote.getAcceptTimeout());
        as.activate(executer);
        as.waitForLeaving(State.Sta5);
        return as;
    }

    public static Association accept(InboundConnection local, Socket sock,
            Executor executer) throws IOException, InterruptedException {
        Association as = new Association(local, sock);
        as.activate(executer);
        return as;
    }

    private void write(AAssociateRQ rq, int acceptTimeout) throws IOException {
        this.rq = rq;
        encoder.write(rq);
        startARTIM(acceptTimeout);
        enterState(State.Sta5);
    }

    private void write(AAssociateAC ac) throws IOException {
        this.ac = ac;
        encoder.write(ac);
        enterState(State.Sta6);
    }

    private void startARTIM(int timeout) throws IOException {
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

    private synchronized void waitForLeaving(State sta)
            throws InterruptedException, IOException {
        while (state == sta)
            wait();
        checkException();
    }

    private void activate(Executor executer) {
        if (!(state == State.Sta2 || state == State.Sta5))
                throw new IllegalStateException("state: " + state);

        executer.execute(new Runnable() {

            @Override
            public void run() {
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
        if (state == State.Sta13) {
            try {
                Thread.sleep(conn.getSocketCloseDelay());
            } catch (InterruptedException e) {
                LOG.warn("Interrupted Socket Close Delay", e);
            }
        }
        enterState(State.Sta1);
        try { out.close(); } catch (IOException ignore) {}
        try { in.close(); } catch (IOException ignore) {}
        if (sock != null) {
            LOG.info("{}: close {}", name, sock);
            try { sock.close(); } catch (IOException ignore) {}
            sock = null;
        }
    }

    public void abort(AAbort aa) {
        // TODO Auto-generated method stub
        
    }

    void onAAssociateRQ(AAssociateRQ rq) throws IOException {
        state.onAAssociateRQ(this, rq);
    }

    void handle(AAssociateRQ rq) throws IOException {
        this.rq = rq;
        name = rq.getCallingAET() + '(' + serialNo + ")";
        stopARTIM();
        LOG.info("{} >> A-ASSOCIATE-RQ", name);
        LOG.debug("{}", rq);
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
            write(device.negotiate(this, rq));
        } catch (AAssociateRJ e) {
            encoder.write(e);
            enterState(State.Sta13);
        }
    }

    void onAAssociateAC(AAssociateAC ac) throws IOException {
        state.onAAssociateAC(this, ac);
    }

    void handle(AAssociateAC rq) throws IOException {
        stopARTIM();
    }

    void onAAssociateRJ(AAssociateRJ rj) throws IOException {
        state.onAAssociateRJ(this, rj);
    }

    void handle(AAssociateRJ rq) {
    }

    void onAReleaseRQ() throws IOException {
        state.onAReleaseRQ(this);
    }

    void handleAReleaseRQ() {
    }

    void handleAReleaseRQCollision() {
    }

    void onAReleaseRP() throws IOException {
        state.onAReleaseRP(this);
    }

    void handleAReleaseRP() {
    }

    void onAAbort(AAbort aAbort) {
        // TODO Auto-generated method stub
        
    }

    void handleAReleaseRPCollision() {
        // TODO Auto-generated method stub
        
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
