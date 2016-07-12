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
package org.dcm4che3.conf.core.api.internal;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.context.LoadingContext;
import org.dcm4che3.conf.core.context.ProcessingContext;
import org.dcm4che3.conf.core.context.SavingContext;

import java.util.Map;

/**
 * This API shall NOT be considered public/stable, it will be refactored without notice.
 * @author Roman K
 */
public interface ConfigTypeAdapter<T, ST> {


    /**
     * Converts serialized configuration representation to the type provided by this adaptor.
     * Handles default values
     *
     * @param configNode
     * @param property   the property which is going to be assigned the returned value (Can be null)
     * @param ctx
     * @param parent
     * @return
     * @throws ConfigurationException
     */
    T fromConfigNode(ST configNode, ConfigProperty property, LoadingContext ctx, Object parent) throws ConfigurationException;

    /**
     * <p>Creates a serialized configuration representation for a provided object.
     * Throws ConfigurationUnserializableException when the object allows configuration
     * with setters in which case it is impossible to trace the parameters used in the setters back.</p>
     * <p/>
     *
     * @param object
     * @param property
     * @param ctx @return
     * @throws ConfigurationException
     */
    ST toConfigNode(T object, ConfigProperty property, SavingContext ctx) throws ConfigurationException;

    /**
     * Returns a metadata node in json-schema format (http://json-schema.org/)
     * <p/>
     * Additional proprietary optional schema parameters:<br/><ul>
     * <li>essentialType - e.g., "base64" for type string</li>
     * <li>class - simple name of the ConfigurableClass if this is a corresponding object</li>
     * <li>mapkey - container that denotes schema for the key (type will always be string, but there could be essentialType, etc)</li>
     * </ul>
     *
     * @param property
     * @param ctx
     * @return
     * @throws ConfigurationException
     */
    Map<String, Object> getSchema(ConfigProperty property, ProcessingContext ctx) throws ConfigurationException;

    /**
     * Converts allowed representations (e.g., "123" for Integer) to proper serialized representation
     *
     * @param configNode
     * @param property
     * @param ctx
     * @return
     */
    ST normalize(Object configNode, ConfigProperty property, ProcessingContext ctx) throws ConfigurationException;

}
