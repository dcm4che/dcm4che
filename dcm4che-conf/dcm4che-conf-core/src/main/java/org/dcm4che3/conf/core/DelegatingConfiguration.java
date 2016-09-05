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
package org.dcm4che3.conf.core;

import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Path;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Roman K
 */
public class DelegatingConfiguration implements Configuration {

    protected Configuration delegate;

    public DelegatingConfiguration() {
    }

    public DelegatingConfiguration(Configuration delegate) {
        this.delegate = delegate;
    }

    @Override
    public Map<String, Object> getConfigurationRoot() throws ConfigurationException {
        return delegate.getConfigurationRoot();
    }

    @Override
    public Object getConfigurationNode(Path path, Class configurableClass) throws ConfigurationException {
        return delegate.getConfigurationNode(path, configurableClass);
    }

    @Override
    public boolean nodeExists(Path path) throws ConfigurationException {
        return delegate.nodeExists(path);
    }

    @Override
    public void persistNode(Path path, Map<String, Object> configNode, Class configurableClass) throws ConfigurationException {
        delegate.persistNode(path, configNode, configurableClass);
    }

    @Override
    public void refreshNode(Path path) throws ConfigurationException {
        delegate.refreshNode(path);
    }

    @Override
    public void removeNode(Path path) throws ConfigurationException {
        delegate.removeNode(path);
    }

    @Override
    public Path getPathByUUID(String uuid) {
        return delegate.getPathByUUID(uuid);
    }

    @Override
    public Iterator search(String liteXPathExpression) throws IllegalArgumentException, ConfigurationException {
        return delegate.search(liteXPathExpression);
    }

    @Override
    public void lock() {
        delegate.lock();
    }

    @Override
    public void runBatch(Batch batch) {
        delegate.runBatch(batch);
    }

}
