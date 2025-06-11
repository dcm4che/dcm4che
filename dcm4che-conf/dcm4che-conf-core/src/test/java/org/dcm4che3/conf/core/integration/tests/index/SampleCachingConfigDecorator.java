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

package org.dcm4che3.conf.core.integration.tests.index;

import java.util.HashMap;
import java.util.Map;

import org.dcm4che3.conf.core.DelegatingConfiguration;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Path;

/**
 * A  utility decorator that will remove nodes upon refresh. Used to simulate caching behavior.
 * @author Homero Cardoso de Almeida (homero.cardosodealmeida@agfa.com)
 */
public class SampleCachingConfigDecorator extends DelegatingConfiguration {

    private Map<String, Object> cache;

    public SampleCachingConfigDecorator(Configuration delegate) {
        super(delegate);
        cache = new HashMap<>();
    }

    @Override
    public Object getConfigurationNode(Path path, Class configurableClass) throws ConfigurationException {
        Object node = getNodeFromCache(path);
        if (node == null) {
            node = super.getConfigurationNode(path, configurableClass);
            putNodeIntoCache(path, node);
        }
        return node;
    }

    @Override
    public void refreshNode(Path path) throws ConfigurationException {
        cache.remove(path.toSimpleEscapedPath());
    }

    private Object getNodeFromCache(Path nodePath) {
        return cache.get(nodePath.toSimpleEscapedPath());
    }

    private void putNodeIntoCache(Path nodePath, Object node) {
        String key = nodePath.toSimpleEscapedPath();
        if (node != null) {
            cache.put(key, node);
        } else {
            cache.remove(key);
        }
    }
}
