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
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.MissingArgumentException;
import org.dcm4che.test.annotations.TestLocalConfig;
import org.dcm4che.test.annotations.TestParamDefaults;
import org.dcm4che.test.common.TestToolFactory.TestToolType;
import org.dcm4che.test.utils.LoadProperties;
import org.dcm4che.test.utils.RemoteDicomConfigFactory;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.dcmgen.test.DcmGenResult;
import org.dcm4che3.tool.dcmgen.test.DcmGenTool;
import org.dcm4che3.tool.findscu.test.QueryTool;
import org.dcm4che3.tool.movescu.test.MoveResult;
import org.dcm4che3.tool.movescu.test.MoveTool;
import org.dcm4che3.tool.mppsscu.test.MppsTool;
import org.dcm4che3.tool.storescu.test.StoreTool;
import org.junit.Rule;

/**
 * @author Hesham elbadawi <bsdreko@gmail.com>
 * @author Roman K
 */

public abstract class BasicTest {
    @Rule
    public TestParametersRule rule = new TestParametersRule(this);

    private Properties defaultProperties;

    private DicomConfiguration localConfig;

    private DicomConfiguration remoteConfig = null;

    public DicomConfiguration getRemoteConfig() {
        if (remoteConfig == null) {
            String baseURL = getDefaultProperties().getProperty("remoteConn.url")+"/config/data";
            remoteConfig = RemoteDicomConfigFactory.createRemoteDicomConfiguration(baseURL);
        }
        return remoteConfig;
    }

    private static Map<String, Annotation> params = new HashMap<String, Annotation>();

    public Map<String, Annotation> getParams() {
        return params;
    }

    public void setDefaultProperties(Properties props) {
        defaultProperties = props;
    }
    public Properties getDefaultProperties() {
        return defaultProperties;
    }
    protected void addParam( String key, Annotation anno) {
        params.put(key, anno);
    }
    protected void clearParams() {
        params.clear();
    }
    public void init(Class<? extends BasicTest> clazz){
        try {
            if(this.getParams().containsKey("defaultParams") 
                    && this.getParams().get("defaultParams") != null)
                System.setProperty("defaultParams", ((TestParamDefaults)
                        this.getParams().get("defaultParams")).propertiesFile());
            if(this.getParams().containsKey("defaultLocalConfig")
                    && this.getParams().get("defaultLocalConfig") != null)
                System.setProperty("defaultLocalConfig", ((TestLocalConfig)
                        this.getParams().get("defaultLocalConfig")).configFile());
            
            this.setDefaultProperties(LoadProperties.load(clazz.getClass()));

            this.setLocalConfig(System.getProperty("defaultLocalConfig"));
        } catch (IOException e) {
            throw new TestToolException(e);
        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public TestResult store(String description, String fileName) throws MissingArgumentException {
        StoreTool storeTool = (StoreTool) TestToolFactory.createToolForTest(TestToolType.StoreTool, this);
        try {
            storeTool.store(description, fileName);
        } catch (Exception e) {
            throw new TestToolException(e);
        }
        return storeTool.getResult();
    }
    
    public TestResult storeResource(String description, String fileName) throws MissingArgumentException {
        StoreTool storeTool = (StoreTool) TestToolFactory.createToolForTest(TestToolType.StoreTool, this);
        File f = new File(fileName);
        storeTool.setbaseDir(f.getParent()==null?"target/test-classes/":f.getParent());
        try {
            storeTool.store(description, fileName);
        } catch (Exception e) {
            throw new TestToolException(e);
        }
        return storeTool.getResult();
    }   
    public TestResult query(String description, Attributes keys, boolean fuzzy, int expectedMatches) throws MissingArgumentException {
        QueryTool queryTool = (QueryTool) TestToolFactory.createToolForTest(TestToolType.QueryTool, this);
        queryTool.setExpectedMatches(expectedMatches);
        queryTool.addAll(keys);
            try {
                if(fuzzy)
                    queryTool.queryfuzzy(description);
                else
                    queryTool.query(description);
            } catch (Exception e) {
                throw new TestToolException(e);
            }
        return queryTool.getResult();
    }

    public TestResult move(String description, Attributes moveAttrs, int expectedMatches) throws MissingArgumentException {
        MoveTool tool = (MoveTool) TestToolFactory.createToolForTest(TestToolType.MoveTool, this);
        tool.setExpectedMatches(expectedMatches);
        tool.addAll(moveAttrs);
        try {
            tool.move(description);
        } catch (Exception e) {
            throw new TestToolException(e);
        }
        MoveResult result = (MoveResult) tool.getResult();
        return result;
    }
    public TestResult mpps(String description, String fileName) throws MissingArgumentException {
        MppsTool mppsTool = (MppsTool) TestToolFactory.createToolForTest(TestToolType.MppsTool, this);
        try {
            mppsTool.mppsscu(description, fileName);
        } catch (Exception e) {
            throw new TestToolException(e);
        }
        return mppsTool.getResult();
    }

    public TestResult storeGenerated(String description, File file) throws MissingArgumentException {
        StoreTool storeTool = (StoreTool) TestToolFactory.createToolForTest(TestToolType.StoreTool, this);
        
        try {
                //get whole study
                storeTool.store(description, file.getAbsolutePath());
        } catch (Exception e) {
            throw new TestToolException(e);
        }
        return storeTool.getResult();
    }

    public TestResult generateAndSend(String description, Attributes overrideAttributes) throws MissingArgumentException {
        DcmGenTool dcmGenTool = (DcmGenTool) TestToolFactory.createToolForTest(TestToolType.DcmGenTool, this);
        TestResult storeResult;
        dcmGenTool.generateFiles(description, overrideAttributes);
        DcmGenResult result = (DcmGenResult) dcmGenTool.getResult();
        storeResult = storeGenerated(description, dcmGenTool.getOutputDir());
        return storeResult;
    }

    public void setLocalConfig(String defaultLocalConfigSystemProperty) throws ConfigurationException {
        File LocalConfigFile = null;
        if(defaultLocalConfigSystemProperty == null) {
                try {
                    LocalConfigFile = Files.createTempFile("tempdefaultconfig", "json").toFile();
                    
                    Files.copy(TestToolFactory.class.getClassLoader()
                            .getResourceAsStream("defaultConfig.json")
                            , LocalConfigFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    LocalConfigFile.deleteOnExit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        else {
            LocalConfigFile = new File (defaultLocalConfigSystemProperty);
        }
        this.localConfig = DicomConfigurationBuilder.newJsonConfigurationBuilder(LocalConfigFile.getPath()).build();
    }

    public DicomConfiguration getLocalConfig() {
        return localConfig;
    }
}
