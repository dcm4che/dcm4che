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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import org.dcm4che.net.pdu.AAbort;
import org.dcm4che.net.pdu.AAssociateAC;
import org.dcm4che.net.pdu.AAssociateRJ;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.AssociationAC;
import org.dcm4che.net.PDUEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Association implements Runnable {

    private static final Logger LOG =
         LoggerFactory.getLogger("org.dcm4che.net.Association");
    private static final Logger LOG_NEGOTIATION =
         LoggerFactory.getLogger("org.dcm4che.net.Association.negotiation");
    private static final AtomicInteger prevSerialNo = new AtomicInteger();

    private final Executor executer;
    private final int serialNo;
    private String name;
    private State state;
    private boolean requestor;
    private Socket sock;
    private InputStream in;
    private OutputStream out;
    private PDUEncoder encoder;
    private PDUDecoder decoder;
    private AssociationAC ac;
    private IOException ex;
    private AAssociateRQ associateRQ;
    private NetworkConnection conn;

    public Association(Executor executer) {
        if (executer == null)
            throw new NullPointerException();
        this.executer = executer;
        this.serialNo = prevSerialNo.incrementAndGet();
        this.name = "Association(" + serialNo + ')';
        this.state = State.Sta1;
    }

    public AssociationAC connect(NetworkConnection local,
            NetworkConnection remote, AAssociateRQ rq)
            throws IOException, InterruptedException {
        return state.connect(this, local, remote, rq);
    }

    AssociationAC doConnect(NetworkConnection local,
            NetworkConnection remote, AAssociateRQ rq)
            throws IOException, InterruptedException {
        enterState(State.Sta4);
        init(local, local.connect(remote), true, remote.getAcceptTimeout());
        enterState(State.Sta5);
        encoder.write(rq);
        synchronized (this) {
            while (state == State.Sta5)
                wait();
        }
        checkException();
        return ac;
    }

    private void init(NetworkConnection local, Socket sock, boolean requestor,
            int timeout) throws IOException {
        this.conn = local;
        this.sock = sock;
        this.requestor = requestor;
        in = sock.getInputStream();
        out = sock.getOutputStream();
        encoder = new PDUEncoder(this, out);
        decoder = new PDUDecoder(this, in);
        startARTIM(timeout);
        executer.execute(this);
    }

    public AssociationAC accept(NetworkConnection local, Socket sock)
            throws IOException, InterruptedException {
        return state.accept(this, local, sock);
    }

    AssociationAC doAccept(NetworkConnection local, Socket sock)
        throws IOException, InterruptedException {
        enterState(State.Sta2);
        init(local, sock, false, local.getRequestTimeout());
        synchronized (this) {
            while (state == State.Sta2)
                wait();
        }
        checkException();
        return ac;
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
        this.state = newState;
        notifyAll();
    }

    @Override
    public void run() {
        if (!(state == State.Sta2 || state == State.Sta4))
            throw new IllegalStateException(state.toString());
        try {
            while (!(state == State.Sta1 || state == State.Sta13))
                decoder.nextPDU();
        } catch (AAbort aa) {
            abort(aa);
        } catch (SocketTimeoutException e) {
            
        } catch (EOFException e) {
            
        } catch (IOException e) {
            
        } finally {
            closeSocket();
        }
    }

    private void closeSocket() {
        // TODO Auto-generated method stub
        
    }

    public void abort(AAbort aa) {
        // TODO Auto-generated method stub
        
    }

    void onAAssociateRQ(AAssociateRQ rq) throws IOException {
        state.onAAssociateRQ(this, rq);
    }

    void handle(AAssociateRQ rq) throws IOException {
        associateRQ = rq;
        name = rq.getCallingAET() + '(' + serialNo + ")";
        stopARTIM();
        LOG.info("{} >> A-ASSOCIATE-RQ", name);
        LOG_NEGOTIATION.debug("{}", rq);
        enterState(State.Sta3);
        
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
