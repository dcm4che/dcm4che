/*
 * *** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
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
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2015
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
 *  ***** END LICENSE BLOCK *****
 */

package org.dcm4che3.tool.mppsscp.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.BasicMPPSSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.common.test.TestTool;


/**
 * @author Roman K
 */
public class MPPSSCPTool implements TestTool {

    Device d;
    ApplicationEntity ae;

    private List<ReceivedMPPS> received = Collections.synchronizedList(new ArrayList<ReceivedMPPS>());

    public class ReceivedMPPS {
        public String iuid;
        public Dimse dimse;
        public Attributes attributes;
        public Attributes request;
    }

    public MPPSSCPTool(Device d) {
        this.d = d;
        ae = d.getApplicationEntities().iterator().next();
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        serviceRegistry.addDicomService(new ToolMPPSSCP());
        ae.setDimseRQHandler(serviceRegistry);
    }

    @Override
    public void init(TestResult resultIn) {


    }

    public void start() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        d.setScheduledExecutor(scheduledExecutorService);
        d.setExecutor(executorService);
        try {
            d.bindConnections();
        } catch (Exception e) {
            throw new RuntimeException("Unable to start MPPSSCP tool", e);
        }
    }

    public void stop() {
        d.unbindConnections();
        ((ExecutorService) d.getExecutor()).shutdown();
        d.getScheduledExecutor().shutdown();

        //very quick fix to block for listening connection
        while (d.getConnections().get(0).isListening()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    @Override
    public MPPSResult getResult() {
        return new MPPSResult();
    }


    public void waitForIncoming(int howMany, int timeoutInSeconds) {
        try {
            long t = System.currentTimeMillis();
            while (timeoutInSeconds <= 0 || System.currentTimeMillis() < t + timeoutInSeconds * 1000.0) {

                synchronized (this) {
                    try {
                        this.wait(100);
                    } catch (InterruptedException ignored) {
                    }
                }

                if (received.size() >= howMany) return;

            }
            throw new RuntimeException("Timeout - did not receive all the expected MPPS messages in "+timeoutInSeconds+" seconds:" +
                    "\n Expected "+howMany+" messages" +
                    "\n Received "+received.size()+" messages");
        } finally {
            stop();
        }
    }

    public class MPPSResult implements TestResult {
        public List<ReceivedMPPS> getReceivedMPPS() {
            return received;
        }
    }

    private class ToolMPPSSCP extends BasicMPPSSCP {
        @Override
        protected Attributes create(Association as, Attributes rq, Attributes rqAttrs, Attributes rsp) throws DicomServiceException {

            ReceivedMPPS receivedMPPS = new ReceivedMPPS();
            receivedMPPS.iuid = rq.getString(Tag.AffectedSOPInstanceUID);
            receivedMPPS.dimse = Dimse.N_CREATE_RQ;
            receivedMPPS.attributes = rqAttrs;
            received.add(receivedMPPS);
            synchronized (MPPSSCPTool.this) {
                MPPSSCPTool.this.notify();
            }
            return null;
        }

        @Override
        protected Attributes set(Association as, Attributes rq, Attributes rqAttrs, Attributes rsp) throws DicomServiceException {

            ReceivedMPPS receivedMPPS = new ReceivedMPPS();
            receivedMPPS.iuid = rq.getString(Tag.AffectedSOPInstanceUID);
            receivedMPPS.dimse = Dimse.N_SET_RQ;
            receivedMPPS.attributes = rqAttrs;
            received.add(receivedMPPS);
            synchronized (MPPSSCPTool.this) {
                MPPSSCPTool.this.notify();
            }
            return null;
        }
    }

}
