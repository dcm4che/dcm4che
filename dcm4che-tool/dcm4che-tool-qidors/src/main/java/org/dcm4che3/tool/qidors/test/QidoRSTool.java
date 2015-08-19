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

package org.dcm4che3.tool.qidors.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.tool.qidors.QidoRS;


/**
 * @author Hesham elbadawi <bsdreko@gmail.com>
 */
public class QidoRSTool implements TestTool {


    public enum QidoMetaDataType {
        JSON, XML;
    }
    private TestResult result;
    private String url;
    private Attributes queryAttrs;
    private Attributes returnAttrs;
    private boolean fuzzy;
    private boolean timezoneAdjustment;
    private String limit;
    private int expectedMatches;
    private boolean returnAll;
    private int numMatches;
    private String offset;

    public QidoRSTool(String url, String limit, boolean fuzzy, boolean timezone, boolean returnAll, String offset) {
        super();
        this.offset = offset;
        this.url = url;
        this.limit = limit;
        this.fuzzy = fuzzy;
        this.timezoneAdjustment = timezone;
        this.returnAll = returnAll;
    }

    public void queryJSON(String testDescription) throws IOException,
    InterruptedException, IncompatibleConnectionException,
    GeneralSecurityException {
        QidoRS qidors = new QidoRS(this.isFuzzy(), this.isTimezoneAdjustment()
                , this.isReturnAll(), this.getLimit(), this.getOffset()
                , this.getQueryAttrs(), this.getReturnAttrs(), "JSON",this.getUrl());
        qidors.setRunningModeTest(true);
        long t1 = System.currentTimeMillis();
        QidoRS.qido(qidors, false);
        long t2 = System.currentTimeMillis();
        numMatches = qidors.getNumMatches();
        validateMatches(testDescription);
        init(new QidoRSResult(testDescription, expectedMatches, numMatches
                , t2-t1, qidors.getTimeFirst()-t1, qidors.getResponseAttrs()));
        
    }

    public void queryXML(String testDescription) throws IOException,
    InterruptedException, IncompatibleConnectionException,
    GeneralSecurityException {
        QidoRS qidors = new QidoRS(this.isFuzzy(), this.isTimezoneAdjustment()
                , this.isReturnAll(), this.getLimit(), this.getOffset()
                , this.getQueryAttrs(), this.getReturnAttrs(), "XML",this.getUrl());
        qidors.setRunningModeTest(true);
        long t1 = System.currentTimeMillis();
        QidoRS.qido(qidors, false);
        long t2 = System.currentTimeMillis();
        numMatches = qidors.getNumMatches();
        init(new QidoRSResult(testDescription, expectedMatches, numMatches
                , t2-t1, qidors.getTimeFirst()-t1, qidors.getResponseAttrs()));
    }

    private void validateMatches(String testDescription) {
        if (this.expectedMatches >= 0)
            assertTrue("test[" + testDescription
                    + "] not returned expected result:" + this.expectedMatches
                    + " but:" + numMatches, numMatches == this.expectedMatches);
    }

    public void addQueryTag(int tag, String value) throws Exception {
        VR vr = ElementDictionary.vrOf(tag, null);
        Attributes attr = this.getQueryAttrs()!=null?this.getQueryAttrs():new Attributes();
        attr.setString(tag, vr, value);
        this.setQueryAttrs(attr);
    }

    public void clearQueryKeys() {
        this.queryAttrs = new Attributes();
    }
    public void addAll(Attributes attrs) {
        queryAttrs.addAll(attrs);
    }

    public void addReturnTag(int tag) throws Exception {
        VR vr = ElementDictionary.vrOf(tag, null);
        Attributes attr = this.getReturnAttrs()!=null?this.getReturnAttrs():new Attributes();
        attr.setNull(tag,vr);
        this.setReturnAttrs(attr);
    }

    public void setExpectedMatches(int matches) {
        this.expectedMatches = matches;
    }

    @Override
    public void init(TestResult result) {
        this.result = result;
    }

    @Override
    public TestResult getResult() {
        return this.result;
    }

    public String getUrl() {
        return url;
    }

    public Attributes getQueryAttrs() {
        return queryAttrs;
    }

    public void setQueryAttrs(Attributes queryAttrs) {
        this.queryAttrs = queryAttrs;
    }

    public boolean isFuzzy() {
        return fuzzy;
    }

    public boolean isTimezoneAdjustment() {
        return timezoneAdjustment;
    }

    public String getLimit() {
        return limit;
    }

    public int getExpectedMatches() {
        return expectedMatches;
    }

    public boolean isReturnAll() {
        return returnAll;
    }

    public String getOffset() {
        return offset;
    }

    public Attributes getReturnAttrs() {
        return returnAttrs;
    }

    public void setReturnAttrs(Attributes returnAttrs) {
        this.returnAttrs = returnAttrs;
    }
}
