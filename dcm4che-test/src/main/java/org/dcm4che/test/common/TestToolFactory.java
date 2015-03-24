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
import org.dcm4che3.tool.storescp.test.StoreSCPTool;
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
        QueryTool,
        MppsTool,
        StorageCommitmentTool,
        StowTool,
        QidoTool,
        WadoURITool,
        WadoRSTool,
        StoreSCPTool,
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
                , queryInformationModel = null
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
            boolean anonymize,relational;
            int rows
            , columns
            , frameNumber
            , imageQuality;
            
            Device device = null;
            Connection conn = null;
            File baseDir = null;
            File retrieveDir = null;
            File stgCmtStorageDirectory = null;
            File storeSCPStorageDirectory = null;
        //Load default parameters
         Properties defaultParams = test.getDefaultProperties();

            config = getDicomConfiguration(test);
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
            String webContext = remoteParams==null?
                    defaultParams.getProperty("remoteConn.webcontext")
                    :remoteParams.webContext();

        switch (type) {

        case StoreTool:

                StoreParameters storeParams = (StoreParameters) test.getParams().get("StoreParameters");
                
                aeTitle = storeParams!=null && !storeParams.aeTitle()
                        .equalsIgnoreCase("NULL")? storeParams.aeTitle() 
                        :(defaultParams.getProperty("store.aetitle")!=null
                        ?defaultParams.getProperty("store.aetitle"):null);
                        
                baseDir = storeParams!=null && !storeParams.baseDirectory()
                        .equalsIgnoreCase("NULL")? new File(storeParams.baseDirectory())
                        :(defaultParams.getProperty("store.directory")!=null
                        ?new File(defaultParams.getProperty("store.directory")):null);
                        
                sourceDevice = storeParams!=null?storeParams.sourceDevice():"storescu";
                
                sourceAETitle = storeParams!=null?storeParams.sourceAETitle():"STORESCU";

                try {
                    device = getDicomConfiguration(test).findDevice(sourceDevice);
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

        case QueryTool:

                QueryParameters queryParams = (QueryParameters) test.getParams().get("QueryParameters");
                
                aeTitle = queryParams!=null && !queryParams.aeTitle()
                        .equalsIgnoreCase("NULL")? queryParams.aeTitle() 
                        :(defaultParams.getProperty("query.aetitle")!=null
                        ?defaultParams.getProperty("query.aetitle"):null);
                        
                queryLevel = queryParams!=null ? queryParams.queryLevel() 
                        :(defaultParams.getProperty("query.level")!=null
                        ?defaultParams.getProperty("query.level"):null);
                        
                queryInformationModel = queryParams!=null ? queryParams.queryInformationModel() 
                        :(defaultParams.getProperty("query.informationmodel")!=null
                        ?defaultParams.getProperty("query.informationmodel"):null);
                relational = queryParams!=null ? queryParams.relational()
                        :(defaultParams.getProperty("query.relational")!=null
                        ?Boolean.valueOf(defaultParams.getProperty("query.relational")):null);
                        
                sourceDevice = queryParams!=null?queryParams.sourceDevice():"findscu";
                
                sourceAETitle = queryParams!=null?queryParams.sourceAETitle():"FINDSCU";
                
                        
                device = null;
                try {
                    device = getDicomConfiguration(test).findDevice(sourceDevice);
                    conn = device.connectionWithEqualsRDN(new Connection(
                            (String) (queryParams != null && queryParams.connection() != null?
                                    queryParams.connection():defaultParams.get("query.connection")), ""));
                } catch (ConfigurationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                tool = new QueryTool(host, port, aeTitle, queryLevel
                        , queryInformationModel, relational, device, sourceAETitle, conn);
            break;

        case MppsTool:

            MppsParameters mppsParams = (MppsParameters) test.getParams().get("MppsParameters");
            
            aeTitle = mppsParams!=null && !mppsParams.aeTitle()
                    .equalsIgnoreCase("NULL")? mppsParams.aeTitle() 
                    :(defaultParams.getProperty("mpps.aetitle")!=null
                    ?defaultParams.getProperty("mpps.aetitle"):null);
            baseDir = mppsParams!=null && !mppsParams.baseDirectory()
                    .equalsIgnoreCase("NULL")? new File(mppsParams.baseDirectory())
                    :(defaultParams.getProperty("mpps.directory")!=null
                    ?new File(defaultParams.getProperty("mpps.directory")):null);
                    
            sourceDevice = mppsParams != null?mppsParams.sourceDevice():"mppsscu";
            sourceAETitle = mppsParams != null?mppsParams.sourceAETitle():"MPPSSCU";
            device = null;
            try {
                device = getDicomConfiguration(test).findDevice(sourceDevice);
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
            
            aeTitle = getParams!=null && !getParams.aeTitle()
                    .equalsIgnoreCase("NULL")? getParams.aeTitle() 
                    :(defaultParams.getProperty("retrieve.aetitle")!=null
                    ?defaultParams.getProperty("retrieve.aetitle"):null);
                    
            retrieveLevel = getParams != null && getParams.retrieveLevel()!=null? getParams.retrieveLevel()
                    : defaultParams.getProperty("retrieve.level");
            
            queryInformationModel = getParams!=null ? getParams.retrieveInformationModel() 
                    :(defaultParams.getProperty("retrieve.informationmodel")!=null
                    ?defaultParams.getProperty("retrieve.informationmodel"):null);
            
            relational = getParams!=null ? getParams.relational()
                    :(defaultParams.getProperty("retrieve.relational")!=null
                    ?Boolean.valueOf(defaultParams.getProperty("retrieve.relational")):null);
            
            retrieveDir = getParams!=null && !getParams.retrieveDir()
                    .equalsIgnoreCase("NULL")? new File(getParams.retrieveDir())
                    :(defaultParams.getProperty("retrieve.directory")!=null
                    ?new File(defaultParams.getProperty("retrieve.directory")):null);

            sourceDevice = getParams != null?getParams.sourceDevice():"getscu";
            sourceAETitle = getParams != null?getParams.sourceAETitle():"GETSCU";
            device = null;
            try {
                device = getDicomConfiguration(test).findDevice(sourceDevice);
                conn = device.connectionWithEqualsRDN(new Connection(
                        (String) (getParams != null && getParams.connection() != null?
                                getParams.connection():defaultParams.get("retrieve.connection")), ""));
            } catch (ConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            tool = new RetrieveTool(host, port, aeTitle, retrieveDir, device
                    , sourceAETitle, retrieveLevel, queryInformationModel, relational, conn);
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
            
            aeTitle = stgcmtParams!=null && !stgcmtParams.aeTitle()
                    .equalsIgnoreCase("NULL")? stgcmtParams.aeTitle() 
                    :(defaultParams.getProperty("stgcmt.aetitle")!=null
                    ?defaultParams.getProperty("stgcmt.aetitle"):null);
                    
            baseDir = stgcmtParams!=null && !stgcmtParams.baseDirectory()
                    .equalsIgnoreCase("NULL")? new File(stgcmtParams.baseDirectory())
                    :(defaultParams.getProperty("stgcmt.directory")!=null
                    ?new File(defaultParams.getProperty("stgcmt.directory")):null);
                    
            stgCmtStorageDirectory =  stgcmtParams!=null && !stgcmtParams.storageDirectory()
                    .equalsIgnoreCase("NULL")? new File(stgcmtParams.storageDirectory())
                    :(defaultParams.getProperty("stgcmt.storedirectory")!=null
                    ?new File(defaultParams.getProperty("stgcmt.storedirectory")):null);
                    
            sourceDevice = stgcmtParams != null? stgcmtParams.sourceDevice():"stgcmtscu";
            sourceAETitle = stgcmtParams != null? stgcmtParams.sourceAETitle():"STGCMTSCU";
            device = null;
            try {
                device = getDicomConfiguration(test).findDevice(sourceDevice);
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
            
            aeTitle = moveParams!=null && !moveParams.aeTitle()
                    .equalsIgnoreCase("NULL")? moveParams.aeTitle() 
                    :(defaultParams.getProperty("move.aetitle")!=null
                    ?defaultParams.getProperty("move.aetitle"):null);
                    
            retrieveLevel = moveParams!=null && !moveParams.retrieveLevel()
                    .equalsIgnoreCase("NULL")? moveParams.retrieveLevel() 
                    :(defaultParams.getProperty("move.level")!=null
                    ?defaultParams.getProperty("move.level"):null);
            
            queryInformationModel = moveParams!=null ? moveParams.retrieveInformationModel() 
                    :(defaultParams.getProperty("move.informationmodel")!=null
                    ?defaultParams.getProperty("move.informationmodel"):null);
            
            relational = moveParams!=null ? moveParams.relational()
                    :(defaultParams.getProperty("move.relational")!=null
                    ?Boolean.valueOf(defaultParams.getProperty("move.relational")):null);
                    
            destAEtitle = moveParams!=null && !moveParams.destAEtitle()
                    .equalsIgnoreCase("NULL")? moveParams.destAEtitle()
                    :(defaultParams.getProperty("move.destaetitle")!=null
                    ?defaultParams.getProperty("move.destaetitle"):null);
            
            sourceDevice = moveParams != null? moveParams.sourceDevice():"movescu";
            sourceAETitle = moveParams != null? moveParams.sourceAETitle():"MOVESCU";
            device = null;
            try {
                device = getDicomConfiguration(test).findDevice(sourceDevice);
                conn = device.connectionWithEqualsRDN(new Connection(
                        (String) (moveParams != null && moveParams.connection() != null?
                                moveParams.connection():defaultParams.get("move.connection")), ""));
            } catch (ConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            tool = new MoveTool(host, port, aeTitle, destAEtitle, retrieveLevel
                    , queryInformationModel, relational, device, sourceAETitle, conn);
            break;

        case StowTool:

            StowRSParameters stowParams = (StowRSParameters) test.getParams().get("StowRSParameters");
            
            url = stowParams != null && stowParams.url() != null? stowParams.url()
                    :null;
            if(url == null)
                throw new MissingArgumentException("To create a StowRS Tool a url must be specified"
                        + " in the StowParameters annotation");
            tool = new StowRSTool(baseURL + "/"+webContext+(url.startsWith("/")? url : "/"+url));
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
            tool = new QidoRSTool(baseURL + "/"+webContext+(url.startsWith("/")? url : "/"+url),
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
            tool = new WadoURITool(baseURL + "/"+webContext+(url.startsWith("/")? url : "/"+url)
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
            tool = new WadoRSTool(baseURL + "/"+webContext+(url.startsWith("/")? url : "/"+url), retrieveDir);
            break;

        case StoreSCPTool:

            StoreSCPParameters storeSCPParams = (StoreSCPParameters) test.getParams()
            .get("StoreSCPParameters");

            // if custom dir is set by annotation, use it, otherwise fallback to property, if even the prop is not there, use constant
            if (storeSCPParams != null &&
                    !storeSCPParams.storageDirectory().equals(StoreSCPParameters.DEFAULT_STORAGE_DIR))
                storeSCPStorageDirectory = new File(storeSCPParams.storageDirectory());
            else
                storeSCPStorageDirectory = new File(defaultParams.getProperty("storescp.storedirectory", StoreSCPParameters.DEFAULT_STORAGE_DIR));

            sourceDevice = storeSCPParams != null
                    ? storeSCPParams.sourceDevice():"storescp";
                    
            sourceAETitle = storeSCPParams != null
                    ? storeSCPParams.sourceAETitle():"STORESCP";
                    
            boolean noStore = storeSCPParams != null
                    ? storeSCPParams.noStore():false;
                    
            device = null;
            try {
                device = getDicomConfiguration(test).findDevice(sourceDevice);
                conn = device.connectionWithEqualsRDN(new Connection(
                        (String) (storeSCPParams != null && storeSCPParams.connection() != null?
                                storeSCPParams.connection():defaultParams.get("storescp.connection")), ""));
            } catch (ConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            tool = new StoreSCPTool(storeSCPStorageDirectory, device, sourceAETitle, conn, noStore);
            break;

        default:
            throw new IllegalArgumentException("Unsupported TestToolType specified"
                    + ", unable to create tool");
        }
        return tool;
    }

    
    
    public static DicomConfiguration getDicomConfiguration(BasicTest test) {
        return config != null? config : test.getLocalConfig();
    }
}
