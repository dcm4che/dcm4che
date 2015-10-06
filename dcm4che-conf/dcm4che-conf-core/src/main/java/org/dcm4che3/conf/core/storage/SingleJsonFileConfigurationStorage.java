/*
 * **** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2014
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */
package org.dcm4che3.conf.core.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.dcm4che3.conf.ConfigurationSettingsLoader;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.util.ConfigNodeUtil;
import org.dcm4che3.util.StringUtils;

/**
 * @author Roman K
 */
public class SingleJsonFileConfigurationStorage implements Configuration {
    private String fileName;
    private ObjectMapper objectMapper = new ObjectMapper();

    public static String resolveConfigFileNameSetting(Hashtable<?, ?> props) {
        return StringUtils.replaceSystemProperties(
                ConfigurationSettingsLoader.getPropertyWithNotice(
                        props,
                        "org.dcm4che.conf.filename",
                        "${jboss.server.config.dir}/dcm4chee-arc/sample-config.json"));
    }

    public SingleJsonFileConfigurationStorage() {
        //NOOP
    }

    public SingleJsonFileConfigurationStorage(String fileName) {
        setFileName(fileName);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean nodeExists(String path) throws ConfigurationException {
        return ConfigNodeUtil.nodeExists(getConfigurationRoot(), path);
    }

    @Override
    public Map<String, Object> getConfigurationRoot() throws ConfigurationException {
        try {
            return objectMapper.readValue(new File(fileName), Map.class);
        } catch (FileNotFoundException e) {
            return new HashMap<String, Object>();
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public Object getConfigurationNode(String path, Class configurableClass) throws ConfigurationException {
        Object node = ConfigNodeUtil.getNode(getConfigurationRoot(), path);
        return node;
    }


    @Override
    public void persistNode(String path, Map<String, Object> configNode, Class configurableClass) throws ConfigurationException {
        Map<String, Object> configurationRoot = getConfigurationRoot();

//        if (configurableClass != null)
//            configNode.put("#class", configurableClass.getName());

        if (!path.equals("/")) {
                ConfigNodeUtil.replaceNode(configurationRoot, path, configNode);
        } else
            configurationRoot = configNode;

        try {
            File resultFile = new File(fileName);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(resultFile, (Object) configurationRoot);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }

    }

    @Override
    public void refreshNode(String path) {

    }

    @Override
    public void removeNode(String path) throws ConfigurationException {

        Map<String, Object> configurationRoot = getConfigurationRoot();
        ConfigNodeUtil.removeNodes(configurationRoot, path);
        persistNode("/", configurationRoot, null);
    }

    @Override
    public Iterator search(String liteXPathExpression) throws IllegalArgumentException, ConfigurationException {
        return ConfigNodeUtil.search(getConfigurationRoot(), liteXPathExpression);
    }

    @Override
    public void lock() {
        // ostrich
    }

    @Override
    public void runBatch(ConfigBatch batch) {
        batch.run();
    }
    
}
