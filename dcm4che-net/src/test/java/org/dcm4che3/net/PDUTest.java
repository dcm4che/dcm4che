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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.AbstractDicomService;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 *
 */
public class PDUTest {

    int status;
    
    @Test
    @Ignore("Test needs a DICOM listener and may therefore fail in some environments. The Test was introduced to debug/fix a problem with deflated TS and must not be performed regulary!")
    public void testEVRLE() throws Exception {
        test(UID.ExplicitVRLittleEndian);
    }

    @Test
    @Ignore("Test needs a DICOM listener and may therefore fail in some environments. The Test was introduced to debug/fix a problem with deflated TS and must not be performed regulary!")
    public void testIVRLE() throws Exception {
        test(UID.ImplicitVRLittleEndian);
    }

    @Test
    @Ignore("Test needs a DICOM listener and may therefore fail in some environments. The Test was introduced to debug/fix a problem with deflated TS and must not be performed regulary!")
    public void testEVRBE_retired() throws Exception {
        test(UID.ExplicitVRBigEndianRetired);
    }
    
    @Test
    @Ignore("Test needs a DICOM listener and may therefore fail in some environments. The Test was introduced to debug/fix a problem with deflated TS and must not be performed regulary!")
    public void testDeflated() throws Exception {
        test(UID.DeflatedExplicitVRLittleEndian);
    }

    private void test(String cfindTS) throws Exception {
        ApplicationEntity aeScp = createAE("FIND_SCP", TransferCapability.Role.SCP);
        aeScp.setAssociationAcceptor(true);
        Connection connScp = new Connection("dicom", "localhost", 11122);
        Device deviceScp = createDevice("findSCP", aeScp, connScp);
        deviceScp.setDimseRQHandler(new AbstractDicomService(UID.PatientRootQueryRetrieveInformationModelFIND) {
            @Override
            protected void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes cmd, Attributes data) throws IOException {
                Attributes rsp;
                if ("STUDY".equals(data.getString(Tag.QueryRetrieveLevel))) {
                    rsp = Commands.mkCFindRSP(cmd, Status.Success);
                } else {
                    rsp = Commands.mkCFindRSP(cmd, Status.UnableToProcess);
                }
                as.tryWriteDimseRSP(pc, rsp);
            }
        });
        deviceScp.bindConnections();

        
        Connection connScu = new Connection("dicom-scu", "localhost", 0);
        ApplicationEntity aeScu = createAE("FIND_SCU", TransferCapability.Role.SCU);
        createDevice("FindSCU", aeScu, connScu);
        
        AAssociateRQ rq = getAssocReq(aeScp);
        
        Association as = aeScu.connect(aeScp, rq);
        
        Attributes keys = new Attributes();
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, "STUDY");
        keys.setString(Tag.PatientName, VR.PN, "TEST");
        DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {
            @Override
            public void onDimseRSP(Association as, Attributes cmd,
                    Attributes data) {
                status = cmd.getInt(Tag.Status, -1);
                super.onDimseRSP(as, cmd, data);
            }
        };

        as.cfind(UID.PatientRootQueryRetrieveInformationModelFIND, 0, keys, cfindTS, rspHandler);
        if (as.isReadyForDataTransfer()) {
            as.waitForOutstandingRSP();
            as.release();
        }
        assertEquals("Status SUCCESS", 0, status);
        deviceScp.unbindConnections();
        Thread.sleep(300);
    }

    public AAssociateRQ getAssocReq(ApplicationEntity aeScp) {
        AAssociateRQ rq = new AAssociateRQ();
        rq.addPresentationContext(new PresentationContext(1, UID.PatientRootQueryRetrieveInformationModelFIND, 
                UID.ImplicitVRLittleEndian));
        rq.addPresentationContext(new PresentationContext(3, UID.PatientRootQueryRetrieveInformationModelFIND, 
                UID.ExplicitVRLittleEndian));
        rq.addPresentationContext(new PresentationContext(5, UID.PatientRootQueryRetrieveInformationModelFIND, 
                UID.DeflatedExplicitVRLittleEndian));
        rq.addPresentationContext(new PresentationContext(7, UID.PatientRootQueryRetrieveInformationModelFIND, 
                 UID.ExplicitVRBigEndianRetired));
        rq.setCalledAET(aeScp.getAETitle());
        return rq;
    }
    
    private ApplicationEntity createAE(String aet, Role role) {
        ApplicationEntity ae = new ApplicationEntity(aet);
        ae.addTransferCapability(new TransferCapability(null, UID.VerificationSOPClass, role,
                UID.ImplicitVRLittleEndian));
        ae.addTransferCapability(new TransferCapability(null, UID.PatientRootQueryRetrieveInformationModelFIND, role,
                UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.DeflatedExplicitVRLittleEndian, UID.ExplicitVRBigEndianRetired));
        return ae;
    }

    private Device createDevice(String name, ApplicationEntity ae, Connection conn) throws IOException, GeneralSecurityException {
        Device device = new Device(name);
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
        
        ExecutorService executorService = Executors.newCachedThreadPool();
        ScheduledExecutorService scheduledExecutorService = Executors
                .newSingleThreadScheduledExecutor();
        device.setScheduledExecutor(scheduledExecutorService);
        device.setExecutor(executorService);
        return device;
    }

}
