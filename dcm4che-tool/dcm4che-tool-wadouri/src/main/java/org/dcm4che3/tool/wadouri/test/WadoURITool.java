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
package org.dcm4che3.tool.wadouri.test;

import java.io.File;

import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.tool.wadouri.WadoURI;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

public class WadoURITool implements TestTool{

    private String url;
    private String studyUID;
    private String seriesUID;
    private String objectUID;
    private String contentType;
    private String charset;
    private boolean anonymize;
    private String annotation;
    private int rows;
    private int columns;
    private String regionCoordinates;
    //windowParams;
    private String windowCenter;
    private String windowWidth;
    private int frameNumber;
    private int imageQuality;
    //presentationUID;
    private String presentationSeriesUID;
    private String presentationUID;
    private String transferSyntax;
    private boolean overlays;
    private File retrieveDir;
    private TestResult result;

    public WadoURITool() {
        
    }

    public WadoURITool(
            String url,
            String studyUID,
            String seriesUID,
            String objectUID,
            String contentType,
            String charset,
            boolean anonymize,
            String annotation,
            int rows,
            int columns,
            String regionCoordinates,
            String windowCenter,
            String windowWidth,
            int frameNumber,
            int imageQuality,
            String presentationSeriesUID,
            String presentationUID,
            String transferSyntax,
            File retrieveDir) {
        super();
        this.url = url;
        this.studyUID = studyUID;
        this.seriesUID = seriesUID;
        this.objectUID = objectUID;
        this.contentType = contentType;
        this.charset = charset;
        this.anonymize = anonymize;
        this.annotation = annotation;
        this.rows = rows;
        this.columns = columns;
        this.regionCoordinates = regionCoordinates;
        this.windowCenter = windowCenter;
        this.windowWidth = windowWidth;
        this.frameNumber = frameNumber;
        this.imageQuality = imageQuality;
        this.presentationSeriesUID = presentationSeriesUID;
        this.presentationUID = presentationUID;
        this.transferSyntax = transferSyntax;
        this.retrieveDir = retrieveDir;
    }

    public void wadoURI(String testDescription) throws Exception {
        wado(testDescription, false);
    }

    public void wadoURI(String testDescription, boolean enableOverlays) throws Exception {
        wado(testDescription, enableOverlays);
    }

    private void wado(String testDescription, boolean enableOverlays) throws Exception {
        long t1, t2;
        WadoURI wadouri = new WadoURI(this.url, this.studyUID,this.seriesUID,this.objectUID,
                this.contentType,this.charset,this.anonymize,this.annotation
                ,this.rows,this.columns,this.regionCoordinates,this.windowCenter,
                this.windowWidth,this.frameNumber,this.imageQuality,this.presentationSeriesUID,
                this.presentationUID,this.transferSyntax);
        wadouri.setOverlays(enableOverlays);
        wadouri.setOutDir(this.retrieveDir);
        wadouri.setOutFileName(this.objectUID);
        t1 = System.currentTimeMillis();
        wadouri.wado(wadouri);
        t2 = System.currentTimeMillis();
        init(new WadoURIResult(testDescription, t2-t1, wadouri.getResponse()));
    }
    @Override
    public void init(TestResult result) {
        this.result = result;
    }

    @Override
    public TestResult getResult() {
        return this.result;
    }

    public File getRetrieveDir() {
        return retrieveDir;
    }
}
