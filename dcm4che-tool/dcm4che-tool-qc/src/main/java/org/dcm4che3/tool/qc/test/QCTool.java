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

import java.util.ArrayList;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.tool.qc.QC;
import org.dcm4che3.tool.qc.QCOperation;
import org.dcm4che3.tool.qc.QCUpdateScope;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

public class QCTool implements TestTool{
	private QC qc;
    private TestResult result;
	public QCTool(String url , QCOperation operation
			, Code code, String targetStudyUID) {
		qc = new QC(url, code, operation, targetStudyUID);
	}
	
    @Override
    public void init(TestResult result) {
    	this.result = result;
    }

    @Override
    public TestResult getResult() {
    	return this.result;
    }
    
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

    public void updateAttributes(String testDescription, QCUpdateScope updateScope,
            Attributes updateData) {
        qc.setUpdateScope(updateScope);
        qc.setUpdateAttrs(updateData);
        
        QCResult tmpResult = qc.performOperation(testDescription, qc);
        init(tmpResult);
    }
    public void delete(String testDescription, String deleteParams) {
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
}
