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

package org.dcm4che3.tool.stgcmtscu.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.AssociationStateException;
import org.dcm4che3.net.Commands;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.AbstractDicomService;
import org.dcm4che3.net.service.DicomService;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.tool.common.DicomFiles;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.tool.stgcmtscu.StgCmtSCU;
import org.dcm4che3.util.SafeClose;
import org.junit.Assert;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
public class StgCmtTool implements TestTool {

    private final String host;

    private final int port;

    private final File baseDirectory;

    private final File storageDirectory;

    String aeTitle;

    Device device;

    StgCmtSCU stgCmtSCU ;

    private final String sourceAETitle;

    private Attributes nEventReqData = new Attributes();

    private int success;

    private int fails;

    private TestResult result;

    Connection bound;


    public StgCmtTool(String host, int port, String aeTitle,
            File baseDir,File storageDirectory, Device device,
            String sourceAETitle, Connection conn) {
        this.host = host;
        this.port = port;
        this.aeTitle = aeTitle;
        this.baseDirectory = baseDir;
        this.device = device;
        this.sourceAETitle = sourceAETitle;
        this.storageDirectory = storageDirectory;
        this.bound = conn;
    }

    private final DicomService stgcmtResultHandler =
            new AbstractDicomService(UID.StorageCommitmentPushModelSOPClass) {

                @Override
                public void onDimseRQ(Association as, PresentationContext pc,
                                      Dimse dimse, Attributes cmd, Attributes data)
                        throws IOException {
                    if (dimse != Dimse.N_EVENT_REPORT_RQ)
                        throw new DicomServiceException(Status.UnrecognizedOperation);

                    int eventTypeID = cmd.getInt(Tag.EventTypeID, 0);
                    if (eventTypeID != 1 && eventTypeID != 2)
                        throw new DicomServiceException(Status.NoSuchEventType)
                                .setEventTypeID(eventTypeID);
                    String tuid = data.getString(Tag.TransactionUID);
                    try {
                        Attributes rsp = Commands.mkNEventReportRSP(cmd, 0);
                        Attributes rspAttrs =  writeResponse(as, cmd, data);
                        nEventReqData = rspAttrs;
                        as.writeDimseRSP(pc, rsp, null);

                    } catch (AssociationStateException e) {
                        System.out.println(as.toString() + " << N-EVENT-RECORD-RSP failed: " + e.getMessage());
                    } finally {
                        stgCmtSCU.removeOutstandingResult(tuid);
                    }
                }
            };

    private Attributes writeResponse(Association as, Attributes cmd, Attributes data) throws DicomServiceException {
        setFailsandSuccess(data);
        if(storageDirectory == null) {
            return data;
        }
        else {
            String cuid = cmd.getString(Tag.AffectedSOPClassUID);
            String iuid = cmd.getString(Tag.AffectedSOPInstanceUID);
            String tuid = data.getString(Tag.TransactionUID);
            File file = new File(storageDirectory, tuid);
            DicomOutputStream out = null;
//            System.out.println(as + "{}: M-WRITE {}" + file);
            try {
                out = new DicomOutputStream(file);
                out.writeDataset(
                        Attributes.createFileMetaInformation(iuid, cuid,
                                UID.ExplicitVRLittleEndian),
                        data);
            } catch (IOException e) {
                //System.out.println(as + ": Failed to store Storage Commitment Result:" + e);
                if(!(e instanceof FileNotFoundException))
                throw new DicomServiceException(Status.ProcessingFailure, e);
            } finally {
                SafeClose.close(out);
            }
        }
        return data;
    }

    private void setFailsandSuccess(Attributes data) {
        success = data.getSequence(Tag.ReferencedSOPSequence).size();
        if(data.contains(Tag.FailedSOPSequence))
        fails = data.getSequence(Tag.FailedSOPSequence).size();
    }

    public void stgcmt(String description, String fileName) throws InterruptedException, IOException, GeneralSecurityException, IncompatibleConnectionException {

        long t1, t2;

        File file = new File(baseDirectory, fileName);

        Assert.assertTrue(
                "file or directory does not exists: " + file.getAbsolutePath(),
                file.exists());

        ApplicationEntity ae = new ApplicationEntity(sourceAETitle);
        device.addApplicationEntity(ae);
        ae.addConnection(bound);

        for(Iterator<Connection> iterator=device.getConnections().iterator(); iterator.hasNext();) {
            Connection next = iterator.next();
            if(!next.getCommonName().equalsIgnoreCase(bound.getCommonName()))
                iterator.remove();
        }
        
        this.stgCmtSCU = new StgCmtSCU(ae, stgcmtResultHandler);

        // configure
        bound.setMaxOpsInvoked(0);
        bound.setMaxOpsPerformed(0);

        stgCmtSCU.getAAssociateRQ().setCalledAET(aeTitle);
        stgCmtSCU.getRemoteConnection().setHostname(host);
        stgCmtSCU.getRemoteConnection().setPort(port);
        //ensure secure connection
        stgCmtSCU.getRemoteConnection().setTlsCipherSuites(bound.getTlsCipherSuites());
        stgCmtSCU.getRemoteConnection().setTlsProtocols(bound.tlsProtocols());
        stgCmtSCU.setTransferSyntaxes(new String[]{UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndianRetired});
        stgCmtSCU.setAttributes(new Attributes());
        stgCmtSCU.setStorageDirectory(storageDirectory);

        // scan
        t1 = System.currentTimeMillis();
        DicomFiles.scan(Arrays.asList(file.getAbsolutePath()), new DicomFiles.Callback() {

            @Override
            public boolean dicomFile(File f, Attributes fmi, long dsPos,
                                     Attributes ds) {
                return stgCmtSCU.addInstance(ds);
            }
        });
        t2 = System.currentTimeMillis();

        // create executor
        ExecutorService executorService =
                Executors.newCachedThreadPool();
        ScheduledExecutorService scheduledExecutorService =
                Executors.newSingleThreadScheduledExecutor();
        device.setExecutor(executorService);
        device.setScheduledExecutor(scheduledExecutorService);
        device.bindConnections();

        // open, send and wait for response
        try {
            stgCmtSCU.open();
            stgCmtSCU.sendRequests();
        } finally {
            stgCmtSCU.close();
            if (bound.isListening()) {
                device.waitForNoOpenConnections();
                device.unbindConnections();
            }
            executorService.shutdown();
            scheduledExecutorService.shutdown();
        }
        init(new StgCmtResult(description,t2-t1,success, fails, nEventReqData));
    }
    @Override
    public void init(TestResult result) {
        this.result = result;
    }

    @Override
    public TestResult getResult() {
        return this.result;
    }

    public File getStorageDirectory() {
        return storageDirectory;
    }

}
