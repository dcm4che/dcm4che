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

package org.dcm4che3.tool.movescu.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
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
import org.dcm4che3.net.Priority;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.tool.movescu.MoveSCU;
import org.dcm4che3.tool.movescu.MoveSCU.InformationModel;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
public class MoveTool implements TestTool{

    private final String host;
    private final int port;
    private final String aeTitle;
    private final String destAEtitle;
    private final String retrieveLevel;
    private final InformationModel retrieveInformationModel;
    private final boolean relational;
    private final Device device;
    private final Connection conn;
    private final String sourceAETitle;

    private int numResponses;
    private int numSuccess;
    private int numFailed;
    private int numWarning;
    private int expectedMatches = Integer.MIN_VALUE;
    
    private final Attributes moveAttrs = new Attributes();
    
    private final List<Attributes> response = new ArrayList<Attributes>();
    private TestResult result;
    
    private static String[] IVR_LE_FIRST = { UID.ImplicitVRLittleEndian,
            UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndianRetired };


    public MoveTool(String host, int port, String aeTitle, String destAEtitle, String retrieveLevel,
            String informationModel, boolean relational, Device device, String sourceAETitle, Connection conn) {
        super();
        this.host = host;
        this.port = port;
        this.aeTitle = aeTitle;
        this.destAEtitle = destAEtitle;
        this.retrieveLevel = retrieveLevel;
        this.retrieveInformationModel = "StudyRoot".equalsIgnoreCase(informationModel) ? InformationModel.StudyRoot : InformationModel.PatientRoot;
        this.relational = relational;
        this.device = device;
        this.sourceAETitle = sourceAETitle;
        this.conn = conn;
    }

    public void move(String testDescription) throws IOException, InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
        device.setInstalled(true);
        ApplicationEntity ae = new ApplicationEntity(sourceAETitle);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);

        MoveSCU main = new MoveSCU(ae);

        main.getAAssociateRQ().setCalledAET(aeTitle);
        main.getRemoteConnection().setHostname(host);
        main.getRemoteConnection().setPort(port);

        // ensure secure connection
        main.getRemoteConnection().setTlsCipherSuites(conn.getTlsCipherSuites());
        main.getRemoteConnection().setTlsProtocols(conn.tlsProtocols());

        main.getKeys().addAll(moveAttrs);
        main.setPriority(Priority.NORMAL);
        main.setDestination(destAEtitle);

        ExecutorService executorService =
                Executors.newSingleThreadExecutor();
        ScheduledExecutorService scheduledExecutorService =
                Executors.newSingleThreadScheduledExecutor();
        main.getDevice().setExecutor(executorService);
        main.getDevice().setScheduledExecutor(scheduledExecutorService);
        main.setInformationModel(retrieveInformationModel, IVR_LE_FIRST, relational);
        main.addLevel(retrieveLevel);

        long timeStart = System.currentTimeMillis();
        try {
            main.open();
            main.retrieve(main.getKeys(), getDimseRSPHandler(main.getAssociation().nextMessageID()));
        } finally {
            main.close();
            executorService.shutdown();
            scheduledExecutorService.shutdown();
        }

        long timeEnd = System.currentTimeMillis();

        if (this.expectedMatches >= 0)
            assertTrue("test[" + testDescription
                    + "] not returned expected result:" + this.expectedMatches
                    + " but:" + numResponses, numResponses == this.expectedMatches);

        // commented since tests could also test failed responses
        //            assertTrue("test[" + testDescription
        //                        + "] had failed responses:"+ numFailed, numFailed==0);

        init(new MoveResult(testDescription, expectedMatches, numResponses,
                numSuccess, numFailed, numWarning,
                (timeEnd - timeStart), response));
    }

    private DimseRSPHandler getDimseRSPHandler(int messageID) {

        DimseRSPHandler rspHandler = new DimseRSPHandler(messageID) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd,
                    Attributes data) {
                super.onDimseRSP(as, cmd, data);
                onCMoveResponse(cmd, data);
            }
        };

        return rspHandler;
    }

    protected void onCMoveResponse(Attributes cmd, Attributes data) {
        int status = cmd.getInt(Tag.Status, -1);
        if(!cmd.contains(Tag. NumberOfRemainingSuboperations)) {
            numSuccess = cmd.getInt(Tag.NumberOfCompletedSuboperations,0);
            numFailed = cmd.getInt(Tag.NumberOfFailedSuboperations,0);
            numWarning = cmd.getInt(Tag.NumberOfWarningSuboperations,0);
            numResponses = numSuccess + numFailed + numWarning;
        }
        response.add(cmd);
    }

    public void addTag(int tag, String value) throws Exception {
        VR vr = ElementDictionary.vrOf(tag, null); 
        moveAttrs.setString(tag, vr, value);
    }

    public void addAll(Attributes keys) {
        moveAttrs.addAll(keys);
    }

    public void setExpectedMatches(int expectedResult) {
        this.expectedMatches = expectedResult;
    }

    @Override
    public void init(TestResult resultIn) {
        this.result = resultIn;
    }

    @Override
    public TestResult getResult() {
        return this.result;
    }

}
