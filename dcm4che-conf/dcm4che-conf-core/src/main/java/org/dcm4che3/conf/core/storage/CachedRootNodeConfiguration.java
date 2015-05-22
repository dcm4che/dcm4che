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

import org.codehaus.jackson.map.ObjectMapper;
import org.dcm4che3.conf.core.DelegatingConfiguration;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.util.ConfigNodeUtil;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Roman K
 */
public class CachedRootNodeConfiguration extends DelegatingConfiguration {

    public static final Logger log = LoggerFactory.getLogger(CachedRootNodeConfiguration.class);


    protected Map<String, Object> configurationRoot = null;

    public CachedRootNodeConfiguration(Configuration delegate) {
        this(delegate, System.getProperties());
    }

    public CachedRootNodeConfiguration(Configuration delegate, Hashtable<?, ?> properties) {
        super(delegate);
        staleTimeout = Integer.valueOf(DicomConfigurationBuilder.getPropertyWithNotice(properties, "org.dcm4che.conf.staleTimeout", "30")) * 1000L;
    }

    long staleTimeout;
    long fetchTime;

    @Override
    public synchronized Map<String, Object> getConfigurationRoot() throws ConfigurationException {
        long now = System.currentTimeMillis();

        if (configurationRoot == null ||
                (staleTimeout != 0 && now > fetchTime + staleTimeout)) {
            fetchTime = now;
            configurationRoot = delegate.getConfigurationRoot();
            log.debug("Configuration cache refreshed");
        }
        return configurationRoot;
    }

    /**
     * Return cached node
     *
     * @param path
     * @param configurableClass
     * @return
     * @throws ConfigurationException
     */
    @Override
    public synchronized Object getConfigurationNode(String path, Class configurableClass) throws ConfigurationException {
        Object node = ConfigNodeUtil.getNode(getConfigurationRoot(), path);

        if (node == null) return null;

        // clone
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.treeToValue(objectMapper.valueToTree(node), Map.class);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public synchronized void persistNode(String path, Map<String, Object> configNode, Class configurableClass) throws ConfigurationException {
        if (!path.equals("/"))
            ConfigNodeUtil.replaceNode(getConfigurationRoot(), path, configNode);
        else
            configurationRoot = configNode;
        delegate.persistNode(path, configNode, configurableClass);
    }

    @Override
    public synchronized void refreshNode(String path) throws ConfigurationException {
        Map<String, Object> newConfigurationNode = (Map<String, Object>) delegate.getConfigurationNode(path, null);
        if (path.equals("/"))
            configurationRoot = newConfigurationNode;
        else
            ConfigNodeUtil.replaceNode(getConfigurationRoot(), path, newConfigurationNode);

    }

    @Override
    public synchronized boolean nodeExists(String path) throws ConfigurationException {
        return ConfigNodeUtil.nodeExists(getConfigurationRoot(), path);
    }

    @Override
    public synchronized void removeNode(String path) throws ConfigurationException {
        delegate.removeNode(path);
        ConfigNodeUtil.removeNodes(getConfigurationRoot(), path);
    }

    @Override
    public synchronized Iterator search(String liteXPathExpression) throws IllegalArgumentException, ConfigurationException {
        return ConfigNodeUtil.search(getConfigurationRoot(), liteXPathExpression);
    }

}
