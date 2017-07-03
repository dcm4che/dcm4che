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
 * Java(TM), hosted at https://github.com/dcm4che/dcm4che.
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
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.Status;
import org.dcm4che3.tool.findscu.FindSCU;
import org.dcm4che3.tool.findscu.FindSCU.InformationModel;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * 
 */
public class QueryTest {

    private String host;
    private int port;
    String aeTitle;

    private int numMatches;
    private ArrayList<String> returnedValues = new ArrayList<String>();
    private Integer returnTag = null;

    private static String[] IVR_LE_FIRST = { UID.ImplicitVRLittleEndian,
            UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndianRetired };

    private Attributes queryatts = new Attributes();
    private int expectedResult = Integer.MIN_VALUE;
    private List<String> expectedValues = null;
    
    private long timeFirst=0;

    /**
     * @param host
     * @param port
     * @param aeTitle
     */
    public QueryTest(String host, int port, String aeTitle) {
        super();
        this.host = host;
        this.port = port;
        this.aeTitle = aeTitle;
    }

    public QueryResult queryfuzzy(String testDescription) throws IOException,
    InterruptedException, IncompatibleConnectionException,
    GeneralSecurityException {
        return query(testDescription,true);
    }

    public QueryResult query(String testDescription) throws IOException,
    InterruptedException, IncompatibleConnectionException,
    GeneralSecurityException {
        return query(testDescription,false);
    }

    
    private QueryResult query(String testDescription, boolean fuzzy) throws IOException,
            InterruptedException, IncompatibleConnectionException,
            GeneralSecurityException {

        FindSCU main = new FindSCU();
        main.getAAssociateRQ().setCalledAET(aeTitle);
        main.getRemoteConnection().setHostname(host);
        main.getRemoteConnection().setPort(port);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ScheduledExecutorService scheduledExecutorService = Executors
                .newSingleThreadScheduledExecutor();
        main.getDevice().setExecutor(executorService);
        main.getDevice().setScheduledExecutor(scheduledExecutorService);

        EnumSet<QueryOption> queryOptions = EnumSet.noneOf(QueryOption.class);
        if (fuzzy) queryOptions.add(QueryOption.FUZZY);
        
        main.setInformationModel(InformationModel.StudyRoot, IVR_LE_FIRST,queryOptions);

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

        if (this.expectedResult >= 0)
            assertTrue("test[" + testDescription
                    + "] not returned expected result:" + this.expectedResult
                    + " but:" + numMatches, numMatches == this.expectedResult);

        if (this.expectedValues != null)
            for (String expectedValue : expectedValues)
                assertTrue(
                        "tag[" + ElementDictionary.keywordOf(returnTag, null)
                                + "] not returned expected value:"
                                + expectedValue,
                        returnedValues.contains(expectedValue));

        return new QueryResult(testDescription, expectedResult, numMatches,
                (timeEnd - timeStart), (timeFirst-timeStart));
    }

    public void addTag(int tag, String value) throws Exception {
        VR vr = ElementDictionary.vrOf(tag, null);
        queryatts.setString(tag, vr, value);
    }

    public void setReturnTag(int tag) throws Exception {
        VR vr = ElementDictionary.vrOf(tag, null);
        queryatts.setNull(tag, vr);
        returnTag = tag;
    }

    public void setExpectedResultsNumeber(int expectedResult) {
        this.expectedResult = expectedResult;
    }

    public void addExpectedResult(String value) {

        if (this.expectedValues == null)
            this.expectedValues = new ArrayList<String>();

        this.expectedValues.add(value);
    }

    private DimseRSPHandler getDimseRSPHandler(int messageID) {

        DimseRSPHandler rspHandler = new DimseRSPHandler(messageID) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd,
                    Attributes data) {
                super.onDimseRSP(as, cmd, data);
                if (numMatches==0) timeFirst = System.currentTimeMillis();
                int status = cmd.getInt(Tag.Status, -1);
                if (Status.isPending(status)) {
                    if (returnTag != null) {

                        if (returnTag.equals(Tag.OtherPatientIDsSequence))
                        {
                            Sequence seq = data.getSequence(returnTag);
                            for (Attributes element : seq)
                            {
                                String returnedValue = element.getString(Tag.PatientID);
                                if (!returnedValues.contains(returnedValue))
                                    returnedValues.add(returnedValue);
                            }
                        }
                        else
                        {
                            String returnedValue = data.getString(returnTag);
                            if (!returnedValues.contains(returnedValue))
                                returnedValues.add(returnedValue);
                        }

                        // System.out.println(returnedValue);
                    }
                    ++numMatches;
                }
            }
        };

        return rspHandler;
    }
}
