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
package org.dcm4che3.conf.core.adapters;

import org.dcm4che3.conf.core.context.LoadingContext;
import org.dcm4che3.conf.core.context.ProcessingContext;
import org.dcm4che3.conf.core.context.SavingContext;
import org.dcm4che3.conf.core.api.internal.ConfigTypeAdapter;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.internal.ConfigProperty;

import java.util.Map;

/**
 * @author: Roman K
 */

/**
 * Returns null for from and toConfigNode when argument is null
 * @param <T>
 * @param <ST>
 */
public class NullToNullDecorator<T, ST> implements ConfigTypeAdapter<T, ST> {

    private final ConfigTypeAdapter<T,ST> configTypeAdapter ;

    public NullToNullDecorator(ConfigTypeAdapter configTypeAdapter) {
        this.configTypeAdapter = configTypeAdapter;
    }

    public T fromConfigNode(ST configNode, ConfigProperty property, LoadingContext ctx, Object parent) throws ConfigurationException {
        if (configNode == null) return null;
        return configTypeAdapter.fromConfigNode(configNode, property, ctx, parent);
    }

    public ST toConfigNode(T object, ConfigProperty property, SavingContext ctx) throws ConfigurationException {
        if (object == null) return null;
        return configTypeAdapter.toConfigNode(object, property, ctx);
    }

    public Map<String, Object> getSchema(ConfigProperty property, ProcessingContext ctx) throws ConfigurationException {
        return configTypeAdapter.getSchema(property, ctx);
    }

    public ST normalize(Object configNode, ConfigProperty property, ProcessingContext ctx) throws ConfigurationException {
        return configTypeAdapter.normalize(configNode, property, ctx);
    }


}
