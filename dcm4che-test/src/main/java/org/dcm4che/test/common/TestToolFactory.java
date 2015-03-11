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

package org.dcm4che.test.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import org.apache.commons.cli.MissingArgumentException;
import org.dcm4che.test.annotations.*;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device; 
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.tool.dcmgen.test.DcmGenTool;
import org.dcm4che3.tool.findscu.test.QueryTool;
import org.dcm4che3.tool.getscu.test.RetrieveTool;
import org.dcm4che3.tool.movescu.test.MoveTool;
import org.dcm4che3.tool.mppsscu.test.MppsTool;
import org.dcm4che3.tool.qidors.test.QidoRSTool;
import org.dcm4che3.tool.stgcmtscu.test.StgCmtTool;
import org.dcm4che3.tool.storescu.test.StoreTool;
import org.dcm4che3.tool.stowrs.test.StowRSTool;
import org.dcm4che3.tool.wadors.WadoRS;
import org.dcm4che3.tool.wadors.test.WadoRSTool;
import org.dcm4che3.tool.wadouri.test.WadoURITool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham elbadawi <bsdreko@gmail.com>
 */

public class TestToolFactory {

    public enum TestToolType {
        StoreTool,
        GetTool,
        MoveTool,
        FindTool,
        MppsTool,
        StorageCommitmentTool,
        StowTool,
        QidoTool,
        WadoURITool,
        WadoRSTool,
        DCMQRSCPTool,
        DcmGenTool
    }
    private static DicomConfiguration config;
    
    public static TestTool createToolForTest(TestToolType type, BasicTest test) throws MissingArgumentException {
        TestTool tool = null;
        String aeTitle = null
                , sourceDevice = null
                , sourceAETitle = null
                , destAEtitle = null
                , retrieveLevel = null
                , queryLevel = null
                , url = null
                , studyUID = null
                , seriesUID = null
                , objectUID = null
                , contentType = null
                , charset = null
                , annotation = null
                , regionCoordinates = null
                , windowCenter = null
                , windowWidth = null
                , presentationSeriesUID = null
                , presentationUID = null
                , transferSyntax = null;
            boolean anonymize;
            int rows
            , columns
            , frameNumber
            , imageQuality;
            
            Device device = null;
            Connection conn = null;
            File baseDir = null;
            File retrieveDir = null;
            File stgCmtStorageDirectory = null;
        //Load default parameters
         Properties defaultParams = test.getDefaultProperties();
        //Load config file if parametrized else load default config
        File file = null;
        if(test.getParams().get("configfile")!=null) {
                file = new File(((TestConfig)test.getParams().get("configfile")).configFile());
        }
        else {
            try {
                file = new File("tmp");
                
                Files.copy(TestToolFactory.class.getClassLoader()
                        .getResourceAsStream("defaultConfig.json")
                        , file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                file.deleteOnExit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
            try {
                config = DicomConfigurationBuilder.newJsonConfigurationBuilder(file.getPath()).build();
            } catch (ConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //get remote connection parameters
            RemoteConnectionParameters remoteParams = 
                    (RemoteConnectionParameters) test.getParams().get("RemoteConnectionParameters");
            String host = remoteParams==null?
                    defaultParams.getProperty("remoteConn.hostname"):remoteParams.hostName();
            int port = remoteParams==null?
                    Integer.valueOf(defaultParams.getProperty("remoteConn.port"))
                    :Integer.valueOf(remoteParams.port());
            String baseURL =  remoteParams==null?
                    defaultParams.getProperty("remoteConn.url")
                    :remoteParams.baseURL();

        switch (type) {
        case StoreTool:
                StoreParameters storeParams = (StoreParameters) test.getParams().get("StoreParameters");
                aeTitle = storeParams!=null && storeParams.aeTitle()!=null? storeParams.aeTitle() 
                        : defaultParams.getProperty("store.aetitle");
                baseDir = storeParams!=null && storeParams.baseDirectory()!=null? new File(storeParams.baseDirectory())
                :new File(defaultParams.getProperty("store.directory"));
                sourceDevice = storeParams!=null?storeParams.sourceDevice():"storescu";
                sourceAETitle = storeParams!=null?storeParams.sourceAETitle():"STORESCU";

                try {
                    device = getDicomConfiguration().findDevice(sourceDevice);
                    conn = device.connectionWithEqualsRDN(new Connection(
                            (String) (storeParams != null && storeParams.connection() != null?
                                    storeParams.connection():defaultParams.get("store.connection")), ""));
                } catch (ConfigurationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                tool = new StoreTool(host,port,aeTitle,baseDir,
                        device == null ? new Device(sourceDevice):device,sourceAETitle, conn);
            break;

        case FindTool:
                QueryParameters queryParams = (QueryParameters) test.getParams().get("QueryParameters");
                aeTitle = queryParams!=null && queryParams.aeTitle()!=null? queryParams.aeTitle() 
                        : defaultParams.getProperty("query.aetitle");
                sourceDevice = queryParams!=null?queryParams.sourceDevice():"findscu";
                sourceAETitle = queryParams!=null?queryParams.sourceAETitle():"FINDSCU";
                queryLevel = queryParams != null && queryParams.queryLevel()!=null? queryParams.queryLevel()
                        : defaultParams.getProperty("query.level");
                device = null;
                try {
                    device = getDicomConfiguration().findDevice(sourceDevice);
                    conn = device.connectionWithEqualsRDN(new Connection(
                            (String) (queryParams != null && queryParams.connection() != null?
                                    queryParams.connection():defaultParams.get("query.connection")), ""));
                } catch (ConfigurationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                tool = new QueryTool(host, port, aeTitle, queryLevel, device, sourceAETitle, conn);
            break;
        case MppsTool:
            MppsParameters mppsParams = (MppsParameters) test.getParams().get("MppsParameters");
            aeTitle = mppsParams != null && mppsParams.aeTitle()!=null? mppsParams.aeTitle() 
                    : defaultParams.getProperty("mpps.aetitle");
            baseDir = mppsParams != null && mppsParams.baseDirectory()!=null? new File(mppsParams.baseDirectory())
            :new File(defaultParams.getProperty("mpps.directory"));
            sourceDevice = mppsParams != null?mppsParams.sourceDevice():"mppsscu";
            sourceAETitle = mppsParams != null?mppsParams.sourceAETitle():"MPPSSCU";
            device = null;
            try {
                device = getDicomConfiguration().findDevice(sourceDevice);
                conn = device.connectionWithEqualsRDN(new Connection(
                        (String) (mppsParams != null && mppsParams.connection() != null?
                                mppsParams.connection():defaultParams.get("mpps.connection")), ""));
            } catch (ConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            tool = new MppsTool(host, port, aeTitle, baseDir, device, sourceAETitle, conn);
            break;
        case GetTool:
            GetParameters getParams = (GetParameters) test.getParams().get("GetParameters");
            aeTitle = getParams != null && getParams.aeTitle()!=null? getParams.aeTitle() 
                    : defaultParams.getProperty("retrieve.aetitle");
            retrieveLevel = getParams != null && getParams.retrieveLevel()!=null? getParams.retrieveLevel()
                    : defaultParams.getProperty("retrieve.level");
            retrieveDir = getParams != null && getParams.retrieveDir()!=null? new File(getParams.retrieveDir())
            :new File(defaultParams.getProperty("retrieve.directory"));
            sourceDevice = getParams != null?getParams.sourceDevice():"getscu";
            sourceAETitle = getParams != null?getParams.sourceAETitle():"GETSCU";
            device = null;
            try {
                device = getDicomConfiguration().findDevice(sourceDevice);
                conn = device.connectionWithEqualsRDN(new Connection(
                        (String) (getParams != null && getParams.connection() != null?
                                getParams.connection():defaultParams.get("retrieve.connection")), ""));
            } catch (ConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            tool = new RetrieveTool(host, port, aeTitle, retrieveDir, device, sourceAETitle, retrieveLevel, conn);
            break;
        case DcmGenTool:
            DcmGenParameters genParams = (DcmGenParameters) test.getParams().get("DcmGenParameters");
            File seedFile = new File(genParams.seedFile());
            File outputDir = new File(genParams.outputDir());
            int instanceCnt = genParams.instanceCount();
            int seriesCnt = genParams.seriesCount();
            tool = new DcmGenTool(instanceCnt, seriesCnt, outputDir, seedFile);
            break;
        case StorageCommitmentTool:
            StgCmtParameters stgcmtParams = (StgCmtParameters) test.getParams().get("StgCmtParameters");
            aeTitle = stgcmtParams != null && stgcmtParams.aeTitle() != null? stgcmtParams.aeTitle()
                    :defaultParams.getProperty("stgcmt.aetitle");
            baseDir = stgcmtParams != null && stgcmtParams.baseDirectory()!=null? new File(stgcmtParams.baseDirectory())
                    :new File(defaultParams.getProperty("stgcmt.directory"));
            stgCmtStorageDirectory =  stgcmtParams != null && stgcmtParams.storageDirectory()!=null?
                    new File(stgcmtParams.storageDirectory())
                    :new File(defaultParams.getProperty("stgcmt.storedirectory"));
            sourceDevice = stgcmtParams != null? stgcmtParams.sourceDevice():"stgcmtscu";
            sourceAETitle = stgcmtParams != null? stgcmtParams.sourceAETitle():"STGCMTSCU";
            device = null;
            try {
                device = getDicomConfiguration().findDevice(sourceDevice);
                conn = device.connectionWithEqualsRDN(new Connection(
                        (String) (stgcmtParams != null && stgcmtParams.connection() != null?
                                stgcmtParams.connection():defaultParams.get("stgcmt.connection")), ""));
            } catch (ConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            tool = new StgCmtTool(host,port,aeTitle,baseDir,stgCmtStorageDirectory,device,sourceAETitle, conn);
            break;
        case MoveTool:
            MoveParameters moveParams = (MoveParameters) test.getParams().get("MoveParameters");
            aeTitle = moveParams != null && moveParams.aeTitle() != null? moveParams.aeTitle()
                    :defaultParams.getProperty("move.aetitle");
            destAEtitle = moveParams !=null && moveParams.destAEtitle() !=null? moveParams.destAEtitle()
                    :defaultParams.getProperty("move.destaetitle");
            retrieveLevel = moveParams != null && moveParams.retrieveLevel()!=null? moveParams.retrieveLevel()
                    : defaultParams.getProperty("move.level");
            sourceDevice = moveParams != null? moveParams.sourceDevice():"movescu";
            sourceAETitle = moveParams != null? moveParams.sourceAETitle():"MOVESCU";
            device = null;
            try {
                device = getDicomConfiguration().findDevice(sourceDevice);
                conn = device.connectionWithEqualsRDN(new Connection(
                        (String) (moveParams != null && moveParams.connection() != null?
                                moveParams.connection():defaultParams.get("move.connection")), ""));
            } catch (ConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            tool = new MoveTool(host, port, aeTitle, destAEtitle, retrieveLevel, device, sourceAETitle, conn);
            break;
        case StowTool:
            StowRSParameters stowParams = (StowRSParameters) test.getParams().get("StowRSParameters");
            url = stowParams != null && stowParams.url() != null? stowParams.url()
                    :null;
            if(url == null)
                throw new MissingArgumentException("To create a StowRS Tool a url must be specified"
                        + " in the StowParameters annotation");
            tool = new StowRSTool(baseURL + "/dcm4chee-arc"+(url.startsWith("/")? url : "/"+url));
            break;
        case QidoTool:
            QidoRSParameters qidoParams = (QidoRSParameters) test.getParams().get("QidoRSParameters");
            url = qidoParams != null && qidoParams.url() != null? qidoParams.url()
                    :null;
            if(url == null)
                throw new MissingArgumentException("To create a QidoRS Tool a url must be specified"
                        + " in the QidoParameters annotation");
            String limit = qidoParams != null && !qidoParams.limit()
                    .equalsIgnoreCase("-1")?qidoParams.limit() : null;
            boolean fuzzy = qidoParams !=null && qidoParams.fuzzyMatching()
                    ?qidoParams.fuzzyMatching() : false;
            boolean timezone = qidoParams !=null && qidoParams.timezoneAdjustment()
                    ?qidoParams.timezoneAdjustment() : false;
            boolean returnAll = qidoParams !=null && qidoParams.returnAll()
                    ?qidoParams.returnAll() : true;
            String offset = qidoParams !=null && !qidoParams.offset().equalsIgnoreCase("0")
                    ?qidoParams.offset() : "0";
            tool = new QidoRSTool(baseURL + "/dcm4chee-arc"+(url.startsWith("/")? url : "/"+url),
                    limit, fuzzy, timezone, returnAll, offset);
            break;
        case WadoURITool:
            WadoURIParameters wadoUriParams = (WadoURIParameters) test.getParams().get("WadoURIParameters");
            if(wadoUriParams == null)
                throw new MissingArgumentException("WadoURIParameters annotation"
                        + " must be used to create a WadoURI tool");
            url = wadoUriParams != null && wadoUriParams.url() != null? wadoUriParams.url()
                    :null;
            studyUID = wadoUriParams != null && wadoUriParams.studyUID() != null
                    ? wadoUriParams.studyUID():null;
            seriesUID = wadoUriParams != null && wadoUriParams.seriesUID() != null
                    ? wadoUriParams.seriesUID():null;
            objectUID = wadoUriParams != null && wadoUriParams.objectUID() != null
                    ? wadoUriParams.objectUID():null;
            contentType = wadoUriParams != null && wadoUriParams.contentType() != null
                    ? wadoUriParams.contentType():null;
                    //non-mandatory
            charset = wadoUriParams.charset();
            annotation = wadoUriParams.annotation();
            regionCoordinates = wadoUriParams.regionCoordinates();
            windowCenter = wadoUriParams.windowCenter();
            windowWidth = wadoUriParams.windowWidth();
            presentationSeriesUID = wadoUriParams.presentationSeriesUID();
            presentationUID = wadoUriParams.presentationUID();
            transferSyntax = wadoUriParams.transferSyntax();
            anonymize = wadoUriParams.anonymize();
            rows = wadoUriParams.rows();
            columns = wadoUriParams.columns();
            frameNumber = wadoUriParams.frameNumber();
            imageQuality = wadoUriParams.imageQuality();
            retrieveDir = new File(wadoUriParams.retrieveDir());
            tool = new WadoURITool(baseURL + "/dcm4chee-arc"+(url.startsWith("/")? url : "/"+url)
                    ,studyUID, seriesUID, objectUID
                    , contentType, charset, anonymize
                    , annotation, rows, columns
                    , regionCoordinates, windowCenter, windowWidth
                    , frameNumber, imageQuality, presentationSeriesUID
                    , presentationUID, transferSyntax, retrieveDir);
            break;
        case WadoRSTool:
            WadoRSParameters wadoRSParams = (WadoRSParameters) test.getParams().get("WadoRSParameters");
            if(wadoRSParams == null)
                throw new MissingArgumentException("WadoRSParameters annotation"
                        + " must be used to create a WadoRS tool");
            url = wadoRSParams != null && wadoRSParams.url() != null? wadoRSParams.url()
                    :null;
            retrieveDir = new File(wadoRSParams.retrieveDir());
            tool = new WadoRSTool(baseURL + "/dcm4chee-arc"+(url.startsWith("/")? url : "/"+url), retrieveDir);
            break;
        default:
            throw new IllegalArgumentException("Unsupported TestToolType specified"
                    + ", unable to create tool");
        }
        return tool;
    }

    
    
    public static DicomConfiguration getDicomConfiguration() {
        return config;
    }
}
