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

package org.dcm4che3.tool.storescp.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.tool.storescp.StoreSCP;
import org.dcm4che3.util.SafeClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
public class StoreSCPTool implements TestTool {

    private final File storageDirectory;

    String aeTitle;

    Device device;

    StoreSCP storeSCP ;

    private final String sourceAETitle;

    private TestResult result;

    Connection bound;

    long totalTime;

    int status;

    long t1=-1,t2=-1;

    private boolean first=true;

    private final boolean noStore;

    private int fileReceived=0;

    private final List<Attributes> rqCMDs = new ArrayList<Attributes>();

    private final List<String> instanceLocations = new ArrayList<String>();

    private final List<File> instanceFiles = new ArrayList<File>();

    private static final Logger LOG = LoggerFactory.getLogger(StoreSCPTool.class);

    private String testDescription;

    private List<String> sopIUIDs;

    private boolean started = false;

    private final BasicCStoreSCP cstoreSCP = new BasicCStoreSCP("*") {

        @Override
        protected void store(Association as, PresentationContext pc,
                Attributes rq, PDVInputStream data, Attributes rsp)
                throws IOException {
            if(first)
                t1=System.currentTimeMillis();
                first = false;
            rsp.setInt(Tag.Status, VR.US, status);
            if (noStore) {
                fileReceived++;
                return;
            }
            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
            addSopUID(iuid);
            String tsuid = pc.getTransferSyntax();
            File file = new File(storageDirectory, iuid);
            try {
                LOG.info("{}: M-WRITE {}", as, file);
                file.getParentFile().mkdirs();
                DicomOutputStream out = new DicomOutputStream(file);
                try {
                    out.writeFileMetaInformation(as.createFileMetaInformation(iuid, cuid, tsuid));
                    data.copyTo(out);
                } finally {
                    SafeClose.close(out);
                    fileReceived++;
                    rqCMDs.add(rq);
                    instanceFiles.add(file);
                    instanceLocations.add(file.getAbsolutePath());
                }
                
            } catch (Exception e) {
                throw new DicomServiceException(Status.ProcessingFailure, e);
            }
        }

    };

    public StoreSCPTool( File storageDirectory, Device device,
            String sourceAETitle, Connection conn, boolean noStore) {
        this.device = device;
        this.sourceAETitle = sourceAETitle;
        this.storageDirectory = storageDirectory;
        this.bound = conn;
        this.noStore = noStore;
    }

    public void start(String testDescriptionIn) throws InterruptedException {

        started = true;

        this.testDescription = testDescriptionIn;
        ApplicationEntity ae = new ApplicationEntity(sourceAETitle);
        device.setDimseRQHandler(createServiceRegistry());
        device.addApplicationEntity(ae);
        ae.addConnection(bound);

        for(Iterator<Connection> iterator=device.getConnections().iterator(); iterator.hasNext();) {
            Connection next = iterator.next();
            if(!next.getCommonName().equalsIgnoreCase(bound.getCommonName()))
                iterator.remove();
        }
        ae.setAssociationAcceptor(true);
        //accept all
        ae.addTransferCapability(
                new TransferCapability(null, 
                        "*",
                        TransferCapability.Role.SCP,
                        "*"));
        ExecutorService executorService = Executors.newCachedThreadPool();
        ScheduledExecutorService scheduledExecutorService = 
                Executors.newSingleThreadScheduledExecutor();
        device.setScheduledExecutor(scheduledExecutorService);
        device.setExecutor(executorService);
        try {
            device.bindConnections();
        } catch (IOException e) {
            LOG.error("Error binding connection for storescp , {}", e);
        } catch (GeneralSecurityException e) {
            LOG.error("Error binding connection for storescp , {}", e);
        }
    }

    public void stop() {

        if (!started)
            return;
        started = false;

        t2 = System.currentTimeMillis();
        device.unbindConnections();
        ((ExecutorService) device.getExecutor()).shutdown();
        device.getScheduledExecutor().shutdown();

        //very quick fix to block for listening connection
        while (device.getConnections().get(0).isListening())
        {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        init(new StoreSCPResult(this.testDescription, t2 - t1, getfilesReceived(), getCmdRQList(), this.sopIUIDs, this.instanceLocations));
    }
    private List<Attributes> getCmdRQList() {
        return rqCMDs;
    }

    private int getfilesReceived() {
        return fileReceived;
    }

    private DicomServiceRegistry createServiceRegistry() {
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        serviceRegistry.addDicomService(cstoreSCP);
        return serviceRegistry;
    }

    @Override
    public void init(TestResult resultIn) {
        this.result = resultIn;
    }

    @Override
    public TestResult getResult() {
        return this.result;
    }

    private void addSopUID(String uid) {
        if(this.sopIUIDs == null)
            this.sopIUIDs = new ArrayList<String>();
        this.sopIUIDs.add(uid);
    }

    public List<String> getInstanceLocations() {
        return instanceLocations;
    }

    public List<File> getInstanceFiles() {
        return instanceFiles;
    }
    public Path getStorageDirectory() {
        return storageDirectory.toPath();
    }
}
