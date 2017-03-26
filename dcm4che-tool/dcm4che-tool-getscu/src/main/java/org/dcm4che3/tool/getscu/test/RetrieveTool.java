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

package org.dcm4che3.tool.getscu.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.tool.getscu.GetSCU;
import org.dcm4che3.tool.getscu.GetSCU.InformationModel;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
public class RetrieveTool implements TestTool{

    private static final Logger LOG = LoggerFactory.getLogger(RetrieveTool.class);

    private final String host;
    private final int port;
    private final String aeTitle;
    private final Device device;
    private final Connection conn;
    private final File retrieveDir;

    private int numCGetFailed;
    private int numCGetSuccess;
    private int numCGetWarning;

    private int numCStores;
    private int numCStoreSuccess;
    private int numCStoreFailed;

    private int expectedMatches = Integer.MIN_VALUE;
    private final Attributes retrieveatts = new Attributes();
    private final GetSCU retrievescu;
    
    private final List<Attributes> response = new ArrayList<Attributes>();
    private TestResult result;
    private final String retrieveLevel;
    private final InformationModel retrieveInformationModel;

    private boolean rememberResultAttributes = true;

    private static String[] IVR_LE_FIRST = { UID.ImplicitVRLittleEndian,
            UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndianRetired };


    public RetrieveTool(String host, int port, String aeTitle, File retrieveDir, Device device, String sourceAETitle,String retrieveLevel
            , String informationModel, boolean relational, Connection conn) {
        super();
        this.host = host;
        this.port = port;
        this.aeTitle = aeTitle;
        this.device = device;
        this.retrieveDir = retrieveDir;
        this.retrieveLevel = retrieveLevel;
        this.retrieveInformationModel = informationModel.equalsIgnoreCase("StudyRoot")
                ? InformationModel.StudyRoot : InformationModel.PatientRoot;
        this.conn = conn;

        //setup device and connection
        device.setInstalled(true);
        ApplicationEntity ae = new ApplicationEntity(sourceAETitle);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
        retrievescu = new GetSCU(ae);

        retrievescu.setInformationModel(retrieveInformationModel, IVR_LE_FIRST, relational);
        try {
            configureServiceClass(retrievescu);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void retrieve(String testDescription) throws IOException, InterruptedException,
            IncompatibleConnectionException, GeneralSecurityException,
            FileNotFoundException, IOException {

        retrievescu.getAAssociateRQ().setCalledAET(aeTitle);
        retrievescu.getRemoteConnection().setHostname(host);
        retrievescu.getRemoteConnection().setPort(port);

        //ensure secure connection
        retrievescu.getRemoteConnection().setTlsCipherSuites(conn.getTlsCipherSuites());
        retrievescu.getRemoteConnection().setTlsProtocols(conn.tlsProtocols());

        retrievescu.setStorageDirectory(retrieveDir);

        registerSCPservice(retrieveDir);
        
        // add retrieve attrs

        // create executor
        ExecutorService executorService = Executors.newCachedThreadPool();
        ScheduledExecutorService scheduledExecutorService = Executors
                .newSingleThreadScheduledExecutor();
        retrievescu.getDevice().setExecutor(executorService);
        retrievescu.getDevice().setScheduledExecutor(scheduledExecutorService);

        retrievescu.addLevel(retrieveLevel);
        retrievescu.getKeys().addAll(retrieveatts);
        
        long timeStart = System.currentTimeMillis();

        // open, send and wait for response
        try {
            retrievescu.open();
            retrievescu.retrieve(getResponseHandler());
        } catch (Exception exception){
            LOG.error("Error while retrieving", exception);
        } finally {
            retrievescu.close();
            executorService.shutdown();
            scheduledExecutorService.shutdown();
        }

        long timeEnd = System.currentTimeMillis();
        
        if (this.expectedMatches >= 0) {
            assertTrue("test[" + testDescription
                    + "] not returned expected result:" + this.expectedMatches
                    + " but:" + numCStores, numCStores == this.expectedMatches);
        }

        assertTrue("test[" + testDescription + "] had failed c-get responses: " + numCGetFailed, numCGetFailed == 0 );

        assertTrue("test[" + testDescription + "] had failed c-store responses: " + numCStoreFailed, numCStoreFailed == 0);
        
        init(new RetrieveResult(testDescription, expectedMatches, numCStores,
                numCStoreSuccess, numCStoreFailed,
                (timeEnd - timeStart), response));
    }

    private DimseRSPHandler getResponseHandler() {
        return new DimseRSPHandler(retrievescu.getAssociation().nextMessageID()) {
            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);

                onCGetReponse(as, cmd, data);
            }
        };
    }

    private void onCGetReponse(Association as, Attributes cmd, Attributes data) {
        int status = cmd.getInt(Tag.Status, -1);
        if(!Status.isPending(status)) {
            numCGetSuccess = cmd.getInt(Tag.NumberOfCompletedSuboperations,0);
            numCGetFailed = cmd.getInt(Tag.NumberOfFailedSuboperations,0);
            numCGetWarning = cmd.getInt(Tag.NumberOfWarningSuboperations,0);
        }
    }

    public void addOfferedStorageSOPClass(String cuid, String... tsuids) {
        retrievescu.addOfferedStorageSOPClass(cuid, tsuids);
    }

    public void addTag(int tag, String... values) {
        VR vr = ElementDictionary.vrOf(tag, null); 
        retrieveatts.setString(tag, vr, values);
    }

    public void addAll(Attributes attrs) {
        retrieveatts.addAll(attrs);
    }

    public void clearTags() {
        retrieveatts.clear();
    }

    public void setExpectedMatches(int expectedResult) {
        this.expectedMatches = expectedResult;
    }

    private void registerSCPservice(final File storeDir) {
            DicomServiceRegistry serviceReg = new DicomServiceRegistry();
            serviceReg.addDicomService( new BasicCStoreSCP("*") {

                @Override
                protected void store(Association as, PresentationContext pc, Attributes rq,
                        PDVInputStream data, Attributes rsp) throws DicomServiceException {

                    if (storeDir == null)
                        return;

                    String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
                    String cuid = rq.getString(Tag.AffectedSOPClassUID);
                    String tsuid = pc.getTransferSyntax();
                    File file = new File(storeDir, iuid );
                    try {
                        GetSCU.storeTo(as, as.createFileMetaInformation(iuid, cuid, tsuid),
                                data, file);
                    } catch (Exception e) {
                        throw new DicomServiceException(Status.ProcessingFailure, e);
                    }
                    //check returned result
                    try{
                    DicomInputStream din = new DicomInputStream(file);
                    Attributes attrs = din.readDataset(-1, -1);
                    din.close();
                    onCStoreReq(rq,attrs);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            device.setDimseRQHandler(serviceReg);
    }

    protected void onCStoreReq(Attributes cmd, Attributes data) {
        int status = cmd.getInt(Tag.Status, -1);
        if (data != null && !data.isEmpty())
            ++numCStoreSuccess;
        else
            ++numCStoreFailed;

        if (rememberResultAttributes)
            response.add(data);

        ++numCStores;
    }

    private static void configureServiceClass(GetSCU main)
            throws FileNotFoundException, IOException {

        URL defaultConfig = main.getClass().getResource(
                "/retrieve/store-tcs.properties");
        Properties props = new Properties();
        props.load(new FileInputStream(new File(defaultConfig.getFile())));

        Set<Entry<Object, Object>> entrySet = props.entrySet();
        for (Entry<Object, Object> entry : entrySet)
            configureStorageSOPClass(main, (String) entry.getKey(),
                    (String) entry.getValue());
    }

    private static void configureStorageSOPClass(GetSCU main, String cuid,
            String tsuids0) {
        String[] tsuids1 = StringUtils.split(tsuids0, ';');
        for (String tsuids2 : tsuids1) {
            main.addOfferedStorageSOPClass(CLIUtils.toUID(cuid),
                    CLIUtils.toUID(tsuids2));
        }
    }

    @Override
    public void init(TestResult resultIn) {
        this.result = resultIn;
    }

    @Override
    public TestResult getResult() {
        return this.result;
    }

    public Path getRetrieveDir() {
        return retrieveDir.toPath();
    }

    /**
     * If set to false, does not keep the attributes retrieved. Useful for large performance tests to avoid VM crashes.
     * @param rememberResultAttributes
     */
    public void setRememberResultAttributes(boolean rememberResultAttributes) {
        this.rememberResultAttributes = rememberResultAttributes;
    }
}
