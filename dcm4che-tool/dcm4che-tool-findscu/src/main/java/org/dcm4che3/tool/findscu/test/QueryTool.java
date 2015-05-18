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

package org.dcm4che3.tool.findscu.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.EnumSet;
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
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.tool.findscu.FindSCU;
import org.dcm4che3.tool.findscu.FindSCU.InformationModel;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * @author Hesham elbadawi <bsdreko@gmail.com>
 */
public class QueryTool implements TestTool {

    private String host;
    private int port;
    private String aeTitle;
    private Device device;
    private Connection conn;
    private String sourceAETitle;
    private List<Attributes> response = new ArrayList<Attributes>();
    private TestResult result;
    private String queryLevel;
    private InformationModel queryModel;
    private boolean relational;
    private int numMatches;
    private static String[] IVR_LE_FIRST = { UID.ImplicitVRLittleEndian,
            UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndianRetired };
    private Attributes queryatts = new Attributes();
    private int expectedMatches = Integer.MIN_VALUE;
    
    private long timeFirst=0;

    /**
     * @param host
     * @param port
     * @param aeTitle
     * @param conn 
     */
    public QueryTool(String host, int port, String aeTitle, String queryLevel, String queryModel
            , boolean relational, Device device, String sourceAETitle, Connection conn) {
        super();
        this.host = host;
        this.port = port;
        this.aeTitle = aeTitle;
        this.device = device;
        this.sourceAETitle = sourceAETitle;
        this.conn = conn;
        this.queryLevel = queryLevel;
        this.queryModel = queryModel.equalsIgnoreCase("StudyRoot")
                ?InformationModel.StudyRoot: InformationModel.PatientRoot;
        this.relational = relational;
    }

    public void query(String testDescription, boolean fuzzy, boolean dataTimeCombined) throws IOException,
    InterruptedException, IncompatibleConnectionException,
    GeneralSecurityException {
        doQuery(testDescription, fuzzy, dataTimeCombined);
    }

    
    private void doQuery(String testDescription, boolean fuzzy, boolean combined) throws IOException,
            InterruptedException, IncompatibleConnectionException,
            GeneralSecurityException {
        device.setInstalled(true);
        ApplicationEntity ae = new ApplicationEntity(sourceAETitle);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
        FindSCU main = new FindSCU(ae);
        main.getAAssociateRQ().setCalledAET(aeTitle);
        main.getRemoteConnection().setHostname(host);
        main.getRemoteConnection().setPort(port);
        //ensure secure connection
        main.getRemoteConnection().setTlsCipherSuites(conn.getTlsCipherSuites());
        main.getRemoteConnection().setTlsProtocols(conn.getTlsProtocols());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ScheduledExecutorService scheduledExecutorService = Executors
                .newSingleThreadScheduledExecutor();
        main.getDevice().setExecutor(executorService);
        main.getDevice().setScheduledExecutor(scheduledExecutorService);

        EnumSet<QueryOption> queryOptions = EnumSet.noneOf(QueryOption.class);
        if (fuzzy) queryOptions.add(QueryOption.FUZZY);
        
        if (combined) queryOptions.add(QueryOption.DATETIME);
        
        if(relational)
            queryOptions.add(QueryOption.RELATIONAL);
        
        main.setInformationModel(queryModel, IVR_LE_FIRST,queryOptions);
        main.addLevel(queryLevel);
//        if (relational) {
//            main.getAAssociateRQ()
//            .addExtendedNegotiation(new ExtendedNegotiation(queryModel.getCuid(), new byte[]{1}));
//        }
        main.getKeys().addAll(queryatts);

        long timeStart = System.currentTimeMillis();

        try {

            main.open();
            main.query(getDimseRSPHandler(main.getAssociation().nextMessageID()));

        } finally {
            main.close(); // is waiting for all the responsens to be complete
            executorService.shutdown();
            scheduledExecutorService.shutdown();

        }

        long timeEnd = System.currentTimeMillis();

        validateMatches(testDescription);

        init(new QueryResult(testDescription, expectedMatches, numMatches,
                (timeEnd - timeStart), (timeFirst-timeStart), response ));
    }

    private void validateMatches(String testDescription) {
        if (this.expectedMatches >= 0)
            assertTrue("test[" + testDescription
                    + "] not returned expected result:" + this.expectedMatches
                    + " but:" + numMatches, numMatches == this.expectedMatches);
    }

    public void addQueryTag(int tag, String value) throws Exception {
        VR vr = ElementDictionary.vrOf(tag, null);
        queryatts.setString(tag, vr, value);
    }

    public void clearQueryKeys() {
        this.queryatts = new Attributes();
    }
    public void addAll(Attributes attrs) {
        queryatts.addAll(attrs);
    }

    public void addReturnTag(int tag) throws Exception {
        VR vr = ElementDictionary.vrOf(tag, null);
        queryatts.setNull(tag, vr);
    }

    public void setExpectedMatches(int matches) {
        this.expectedMatches = matches;
    }

    private DimseRSPHandler getDimseRSPHandler(int messageID) {

        DimseRSPHandler rspHandler = new DimseRSPHandler(messageID) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd,
                    Attributes data) {
                super.onDimseRSP(as, cmd, data);
                onCFindResponse(cmd, data);
            }
        };

        return rspHandler;
    }

    protected void onCFindResponse(Attributes cmd, Attributes data) {
        if (numMatches==0) timeFirst = System.currentTimeMillis();
        int status = cmd.getInt(Tag.Status, -1);
        if (Status.isPending(status)) {
            response.add(data);
            ++numMatches;
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

    public void setAeTitle(String aeTitle) {
        this.aeTitle = aeTitle;
    }

    public String getQueryLevel() {
        return queryLevel;
    }
}
