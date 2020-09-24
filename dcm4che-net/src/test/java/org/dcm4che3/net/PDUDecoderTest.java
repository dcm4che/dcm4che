/*
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  J4Care.
 *  Portions created by the Initial Developer are Copyright (C) 2015-2017
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 */

package org.dcm4che3.net;

import org.dcm4che3.data.UID;
import org.dcm4che3.net.pdu.AAbort;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jul 2017
 */
public class PDUDecoderTest {

    private static final TransferCapability ECHO_SCP = new TransferCapability(
            null, UID.Verification, TransferCapability.Role.SCP, UID.ImplicitVRLittleEndian);
    private static byte[] ECHO_RQ_WITH_PENDING_PDV = {
            4, 0, 0, 0, 0, 74, 0, 0, 0, 70, 1, 3, 0, 0, 0, 0,
            4, 0, 0, 0, 56, 0, 0, 0, 0, 0, 2, 0, 18, 0, 0, 0,
            49, 46, 50, 46, 56, 52, 48, 46, 49, 48, 48, 48, 56, 46, 49, 46,
            49, 0, 0, 0, 0, 1, 2, 0, 0, 0, 48, 0, 0, 0, 16, 1,
            2, 0, 0, 0, 1, 0, 0, 0, 0, 8, 2, 0, 0, 0, 0, 0,
            4, 0, 0, 0, 0, 6, 0, 0, 0, 2, 1, 0
    };
    private ExecutorService executor;
    private ScheduledExecutorService scheduledExecutor;
    private Device acceptor;
    private Device requestor;

    @Before
    public void setUp() throws Exception {
        executor = Executors.newCachedThreadPool();
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        requestor = createDevice("REQUESTOR", 0);
        acceptor = createDevice("ACCEPTOR", 55104);
        acceptor.bindConnections();
    }

    private Device createDevice(String aet, int port) {
        Device device = new Device(aet);
        ApplicationEntity ae = new ApplicationEntity(aet);
        Connection conn = new Connection(null, "localhost", port);
        device.addApplicationEntity(ae);
        device.addConnection(conn);
        ae.addConnection(conn);
        if (port > 0) {
            ae.addTransferCapability(ECHO_SCP);
            ae.setDimseRQHandler(new BasicCEchoSCP());
        }
        device.setExecutor(executor);
        device.setScheduledExecutor(scheduledExecutor);
        return device;
    }


    @After
    public void tearDown() throws Exception {
        Thread.sleep(100); // acceptor.waitForNoOpenConnections() may hang;
        acceptor.unbindConnections();
        executor.shutdown();
        scheduledExecutor.shutdown();
    }

    @Test(timeout = 1000, expected = AAbort.class)
    public void releaseAfterPendingPDV() throws Exception {
        Association as = aeOf(requestor).connect(aeOf(acceptor), aarq());
        as.getSocket().getOutputStream().write(ECHO_RQ_WITH_PENDING_PDV);
        as.release();
        as.waitForSocketClose();
    }

    private AAssociateRQ aarq() {
        AAssociateRQ aarq = new AAssociateRQ();
        aarq.addPresentationContextFor(UID.Verification, UID.ImplicitVRLittleEndian);
        return aarq;
    }

    private static ApplicationEntity aeOf(Device device) {
        return device.getApplicationEntities().iterator().next();
    }

}