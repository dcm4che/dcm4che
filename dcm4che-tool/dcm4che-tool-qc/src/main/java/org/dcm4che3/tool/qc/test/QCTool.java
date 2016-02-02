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
 * Portions created by the Initial Developer are Copyright (C) 2012
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
package org.dcm4che3.tool.qc.test;

import org.apache.commons.cli.MissingArgumentException;
import org.dcm4che3.data.*;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.tool.qc.QC;
import org.dcm4che3.tool.qc.QCOperation;
import org.dcm4che3.tool.qc.QCUpdateScope;

import java.util.ArrayList;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */

public class QCTool implements TestTool {
    private QC qc;
    private TestResult result;

    public QCTool(String url, QCOperation operation
            , Code code, String targetStudyUID) {
        qc = new QC(url, code, operation);
        qc.setTargetStudyUID(targetStudyUID);
    }

    @Override
    public void init(TestResult result) {
    	this.result = result;
    }

    @Override
    public TestResult getResult() {
    	return this.result;
    }
    /**
     * merge.
     * Calls the tool to merge a study into the target patient.
     *
     * @param testDescription
     *            the test description
     * @param mergeUIDs
     *            the uids for the studies to merge
     * @param targetStudyAttrs
     *            the target study attributes to be updated
     * @param targetSeriesAttrs
     *            the target series attributes to be updated
     * @param pid
     *            patient ID
     */
    public void merge(String testDescription,
            ArrayList<String> mergeUIDs, Attributes targetStudyAttrs,
            Attributes targetSeriesAttrs, IDWithIssuer pid) {
        qc.setMergeUIDs(mergeUIDs);
        qc.setTargetSeriesAttrs(targetSeriesAttrs);
        qc.setTargetStudyAttrs(targetStudyAttrs);
        qc.setPid(pid);
        QCResult tmpResult = qc.performOperation(testDescription, qc);
        init(tmpResult);
    }

    /**
     * split.
     * Calls the tool to split some instances specified by moveUIDs.
     *
     * @param testDescription
     *            the test description
     * @param moveUIDs
     *            the uids for the moved instances
     * @param targetStudyAttrs
     *            the target study attributes to be updated
     * @param targetSeriesAttrs
     *            the target series attributes to be updated
     * @param pid
     *            patient ID
     */
    public void split(String testDescription, ArrayList<String> moveUIDs,
            Attributes targetStudyAttrs, Attributes targetSeriesAttrs,
            IDWithIssuer pid) {
        qc.setMoveUIDs(moveUIDs);
        qc.setTargetSeriesAttrs(targetSeriesAttrs);
        qc.setTargetStudyAttrs(targetStudyAttrs);
        qc.setPid(pid);
        QCResult tmpResult = qc.performOperation(testDescription, qc);
        init(tmpResult);
    }
    /**
     * segment.
     * Calls the tool to either move some instances specified by moveUIDs or clone some instances specified by cloneUIDs.
     *
     * @param testDescription
     *            the test description
     * @param moveUIDs
     *            the uids for the moved instances
     * @param cloneUIDs
     *            the uids for the cloned instance
     * @param targetStudyAttrs
     *            the target study attributes to be updated
     * @param targetSeriesAttrs
     *            the target series attributes to be updated
     * @param pid
     *            patient ID
     */
    public void segment(String testDescription, ArrayList<String> moveUIDs,
            ArrayList<String> cloneUIDs,Attributes targetStudyAttrs,
            Attributes targetSeriesAttrs,IDWithIssuer pid) {
        qc.setMoveUIDs(moveUIDs);
        qc.setCloneUIDs(cloneUIDs);
        qc.setTargetSeriesAttrs(targetSeriesAttrs);
        qc.setTargetStudyAttrs(targetStudyAttrs);
        qc.setPid(pid);
        QCResult tmpResult = qc.performOperation(testDescription, qc);
        init(tmpResult);
    }
    /**
     * updateAttributes.
     * Calls the tool to update instances identified by their uids
     * in the updateData with metadata also specified in the updateData.
     * Will not update UIDS.
     *
     * @param testDescription
     *            the test description
     * @param updateScope
     *            the scope can be any scope specified in QCUpdateScope
     * @param updateData
     *            Attributes used to identify and update objects
     */
    public void updateAttributes(String testDescription, QCUpdateScope updateScope,
            Attributes updateData) {
        qc.setUpdateScope(updateScope);
        qc.setUpdateAttrs(updateData);

        QCResult tmpResult = qc.performOperation(testDescription, qc);
        init(tmpResult);
    }

    /**
     * delete.
     * Calls the tool to delete studies, series or instance
     * specified in the deleteParams string.
     * Can specify patient data instead in the form
     * patientID:localIssuer:UniversalEntityUID:UniversalEntityUIDType.
     * Must specify boolean true in case of patient
     *
     * @param testDescription
     *            the test description
     * @param deleteParams
     *            the string separated by colons for the object to delete
     * @param patient
     *            boolean specifying if the deleteParams are that of a patient
     */
    public void delete(String testDescription, String deleteParams, boolean patient) {
        if(patient)
            try {
                qc.setPid(QC.toIDWithIssuer(deleteParams));
            }catch (MissingArgumentException e) {

            }
        qc.setDeleteParams(deleteParams);
        QCResult tmpResult = qc.performOperation(testDescription, qc);
        init(tmpResult); 
    }

    public void deleteMulti(String testDescription, String[] deleteParams) {

        if (deleteParams == null)
            return;

        MultipleQCResult results = new MultipleQCResult();

        for (String param : deleteParams) {
            qc.setDeleteParams(param);
            QCResult tmpResult = qc.performOperation(testDescription, qc);
            results.getResults().add(tmpResult);
        }

        init(results);
    }

    /**
     * performPatientOperation.
     * Calls the tool to perform one of the patient operations specified
     * Patient operations can be any of QCOperation that start with
     * PATIENT_
     *
     * @param testDescription
     *            the test description
     * @param sourcePatientAttrs
     *            the attributes with id and optionally issuer of the patient
     *            used as source patient in the process
     * @param targetPatientAttrs
     *            the attributes with id and optionally issuer of the patient
     *            used as target patient in the process
     */
    public void performPatientOperation(String testDescription, Attributes sourcePatientAttrs, Attributes targetPatientAttrs) {
        qc.setSourcePatientAttributes(sourcePatientAttrs);
        qc.setTargetPatientAttributes(targetPatientAttrs);
        QCResult tmpResult = qc.performOperation(testDescription, qc);
        init(tmpResult);
    }

    /**
     * reject.
     * Calls the tool to reject studies, series or instance
     *
     * @param testDescription
     *            the test description
     * @param rOrUIDs
     *            the list of SOPUIDs
     */
    public void reject(String testDescription, ArrayList<String> rOrUIDs) {
        qc.setRrUIDs(rOrUIDs);
        QCResult tmpResult = qc.performOperation(testDescription, qc);
        init(tmpResult);
    }

    /**
     * restore.
     * Calls the tool to restore a set of instances from being rejected.
     * Configuration of the archive must allow this code to override previous one.
     *
     * @param testDescription
     *            the test description
     * @param rOrUIDs
     *            the list of SOPUIDs
     */
    public void restore(String testDescription, ArrayList<String> rOrUIDs) {
        qc.setRrUIDs(rOrUIDs);
        QCResult tmpResult = qc.performOperation(testDescription, qc);
        init(tmpResult);
    }
    
    /**
     * updateAttributes.
     * Calls the tool to update instances identified by their uids
     * in the updateData with metadata also specified in the updateData.
     * Will not update UIDS.
     *
     * @param testDescription
     *            the test description
     * @param updateScope
     *            the scope can be any scope specified in QCUpdateScope
     * @param updateData
     *            Attributes used to identify and update objects
     */
    public void quickFixLinkMWL(String testDescription, Attributes mwlAttributes, IDWithIssuer pid) {
        qc.setUpdateAttrs(mwlAttributes);
        qc.setPid(pid);

        QCResult tmpResult = qc.performOperation(testDescription, qc);
        init(tmpResult);
    }

}
