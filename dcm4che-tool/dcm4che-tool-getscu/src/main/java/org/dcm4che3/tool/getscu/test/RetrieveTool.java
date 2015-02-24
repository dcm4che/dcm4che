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
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.tool.getscu.GetSCU;
import org.dcm4che3.tool.getscu.GetSCU.InformationModel;
import org.dcm4che3.util.StringUtils;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
public class RetrieveTool implements TestTool{

    private String host;
    private int port;
    private String aeTitle;
    private Device device;
    private String sourceAETitle;
    private File retrieveDir;
    private int numResponses;
    private int numSuccess;
    private int numFailed;
    private int expectedMatches = Integer.MIN_VALUE;
    private long timeFirst=0;
    private Attributes retrieveatts = new Attributes();
    
    private List<Attributes> response = new ArrayList<Attributes>();
    private TestResult result;
    
    private static String[] IVR_LE_FIRST = { UID.ImplicitVRLittleEndian,
            UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndianRetired };


    public RetrieveTool(String host, int port, String aeTitle, File retrieveDir, Device device, String sourceAETitle) {
        super();
        this.host = host;
        this.port = port;
        this.aeTitle = aeTitle;
        this.device = device;
        this.sourceAETitle = sourceAETitle;
        this.retrieveDir = retrieveDir;
    }

    public void retrieve(String testDescription) throws IOException, InterruptedException,
            IncompatibleConnectionException, GeneralSecurityException,
            FileNotFoundException, IOException {
        //setup device and connection
        Connection conn = new Connection();
        device.addConnection(conn);
        device.setInstalled(true);
        ApplicationEntity ae = new ApplicationEntity(sourceAETitle);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
        final GetSCU retrievescu = new GetSCU(ae);
        retrievescu.getAAssociateRQ().setCalledAET(aeTitle);
        retrievescu.getRemoteConnection().setHostname(host);
        retrievescu.getRemoteConnection().setPort(port);
        retrievescu.setStorageDirectory(retrieveDir);

        // add retrieve attrs


        // create executor
        ExecutorService executorService = Executors.newCachedThreadPool();
        ScheduledExecutorService scheduledExecutorService = Executors
                .newSingleThreadScheduledExecutor();
        retrievescu.getDevice().setExecutor(executorService);
        retrievescu.getDevice().setScheduledExecutor(scheduledExecutorService);
        retrievescu.getDevice().bindConnections();

        retrievescu.setInformationModel(InformationModel.StudyRoot,
                IVR_LE_FIRST, false);
        configureServiceClass(retrievescu);

        retrievescu.getKeys().addAll(retrieveatts);
        
        long timeStart = System.currentTimeMillis();

        // open, send and wait for response
        try {
            retrievescu.open();
            retrievescu.retrieve(getDimseRSPHandler(retrievescu.getAssociation().nextMessageID()));
        } finally {
            retrievescu.close();
            executorService.shutdown();
            scheduledExecutorService.shutdown();
        }

        long timeEnd = System.currentTimeMillis();
        
        if (this.expectedMatches >= 0)
            assertTrue("test[" + testDescription 
                    + "] not returned expected result:" + this.expectedMatches
                    + " but:" + numResponses, numResponses == this.expectedMatches);
        
        assertTrue("test[" + testDescription
                    + "] had failed responses:"+ numFailed, numFailed==0);
        
        init(new RetrieveResult(testDescription, expectedMatches, numResponses,
                numSuccess, numFailed,
                (timeEnd - timeStart), (timeFirst-timeStart), response));
    }
    
    public void addTag(int tag, String value) throws Exception {
        VR vr = ElementDictionary.vrOf(tag, null); 
        retrieveatts.setString(tag, vr, value);
    }
    
    public void setExpectedMatches(int expectedResult) {
        this.expectedMatches = expectedResult;
    }
    
    private DimseRSPHandler getDimseRSPHandler(int messageID) {

        DimseRSPHandler rspHandler = new DimseRSPHandler(messageID) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd,
                    Attributes data) {
                super.onDimseRSP(as, cmd, data);
                onCGetResponse(cmd, data);
            }
        };

        return rspHandler;
    }

    protected void onCGetResponse(Attributes cmd, Attributes data) {
        if (numResponses==0) timeFirst = System.currentTimeMillis();
        int status = cmd.getInt(Tag.Status, -1);
        if (status == 0XFF00 || status == 0)
            ++numSuccess;
        else
            ++numFailed;
        response.add(data);
        ++numResponses;
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
    public void init(TestResult result) {
        this.result = result;
    }

    @Override
    public TestResult getResult() {
        return this.result;
    }

}
