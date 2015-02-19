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
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dcm4che.test.common.TestToolFactory.TestToolType;
import org.dcm4che.test.utils.LoadProperties;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.dcmgen.test.DcmGenResult;
import org.dcm4che3.tool.dcmgen.test.DcmGenTool;
import org.dcm4che3.tool.findscu.test.QueryTool;
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

    private static Map<String, Annotation> params = new HashMap<String, Annotation>();

    protected Map<String, Annotation> getParams() {
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
    
    public void init(Class<? extends BasicTest> clazz){
        try {
            this.setDefaultProperties(LoadProperties.load(clazz.getClass()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TestResult store(String description, String fileName) {
        StoreTool storeTool = (StoreTool) TestToolFactory.createToolForTest(TestToolType.StoreTool, this);
        try {
            storeTool.store(description, fileName);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IncompatibleConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return storeTool.getResult();
    }
    
    public TestResult storeResource(String description, String fileName) {
        StoreTool storeTool = (StoreTool) TestToolFactory.createToolForTest(TestToolType.StoreTool, this);
        File f = new File(fileName);
        storeTool.setbaseDir(f.getParent()==null?"target/test-classes/":f.getParent());
        try {
            storeTool.store(description, fileName);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IncompatibleConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return storeTool.getResult();
    }   
    public TestResult query(String description, Attributes keys, boolean fuzzy) {
        QueryTool queryTool = (QueryTool) TestToolFactory.createToolForTest(TestToolType.FindTool, this);
        queryTool.addAll(keys);
            try {
                if(fuzzy)
                    queryTool.queryfuzzy(description);
                else
                    queryTool.query(description);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IncompatibleConnectionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        return queryTool.getResult();
    }

    public TestResult mpps(String description, String fileName) {
        MppsTool mppsTool = (MppsTool) TestToolFactory.createToolForTest(TestToolType.MppsTool, this);
        try {
            mppsTool.mppsscu(description, fileName);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IncompatibleConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return mppsTool.getResult();
    }

    public TestResult storeResources(String description, DcmGenResult result) {
        StoreTool storeTool = (StoreTool) TestToolFactory.createToolForTest(TestToolType.StoreTool, this);
        
        try {
            for(String fileName : result.getGeneratedResults()) {
                File f = new File(fileName);
                storeTool.setbaseDir(f.getParent()==null?"target/test-classes/":f.getParent());
                storeTool.store(description, fileName);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IncompatibleConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return storeTool.getResult();
    }

    public TestResult generateAndSend(String description, Attributes overrideAttributes) {
        DcmGenTool dcmGenTool = (DcmGenTool) TestToolFactory.createToolForTest(TestToolType.DcmGenTool, this);
        TestResult storeResult;
        dcmGenTool.generateFiles(description, overrideAttributes);
        DcmGenResult result = (DcmGenResult) dcmGenTool.getResult();
        storeResult = storeResources(description, result);
        return storeResult;
    }
}
