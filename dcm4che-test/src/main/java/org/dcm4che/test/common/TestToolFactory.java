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

import org.dcm4che.test.annotations.MppsParameters;
import org.dcm4che.test.annotations.QueryParameters;
import org.dcm4che.test.annotations.RemoteConnectionParameters;
import org.dcm4che.test.annotations.StoreParameters;
import org.dcm4che.test.annotations.TestConfig;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;
import org.dcm4che3.net.Device; 
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.tool.findscu.test.QueryTool;
import org.dcm4che3.tool.mppsscu.test.MppsTool;
import org.dcm4che3.tool.storescu.test.StoreTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham elbadawi <bsdreko@gmail.com>
 */

public class TestToolFactory {

    private static final Logger LOG = LoggerFactory.getLogger(TestToolFactory.class);
    
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
        DCMQRSCPTool
    }
    private static DicomConfiguration config;
    
    public static TestTool createToolForTest(TestToolType type, BasicTest test) {
        TestTool tool = null;
        
        //Load default parameters
         Properties defaultParams = BasicTest.getDefaultProperties();
         
        //Load config file if parametrized else load default config
        File file = null;
        if(test.getParams().get("configfile")!=null) {
                file = new File(((TestConfig)test.getParams().get("configfile")).configFile());
        }
        else {
            
            try {
                file = new File("tmp");
                Files.copy(TestToolFactory.class.getClassLoader()
                        .getResourceAsStream(defaultParams.getProperty("configfile"))
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
            //create tool
                    String aeTitle=null,sourceDevice=null, sourceAETitle=null;
                    Device device = null;
                    File baseDir = null;
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
                } catch (ConfigurationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } 
                tool = new StoreTool(host,port,aeTitle,baseDir,
                        device == null ? new Device(sourceDevice):device,sourceAETitle);
            break;

        case FindTool:
                QueryParameters queryParams = (QueryParameters) test.getParams().get("QueryParameters");
                aeTitle = queryParams!=null && queryParams.aeTitle()!=null? queryParams.aeTitle() 
                        : defaultParams.getProperty("query.aetitle");
                sourceDevice = queryParams!=null?queryParams.sourceDevice():"findscu";
                sourceAETitle = queryParams!=null?queryParams.sourceAETitle():"FINDSCU";
                device = null;
                try {
                    device = getDicomConfiguration().findDevice(sourceDevice);    
                } catch (ConfigurationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } 
                tool = new QueryTool(host, port, aeTitle, device, sourceAETitle);
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
            } catch (ConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
            tool = new MppsTool(host, port, aeTitle, baseDir, device, sourceAETitle);
            break;
        default:
            break;
        }
        return tool;
    }
    
    public static DicomConfiguration getDicomConfiguration() {
        return config;
    }
}
