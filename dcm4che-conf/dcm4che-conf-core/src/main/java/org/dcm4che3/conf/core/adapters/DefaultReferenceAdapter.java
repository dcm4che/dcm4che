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

import org.apache.commons.beanutils.PropertyUtils;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.internal.*;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.util.PathPattern;

import java.util.HashMap;
import java.util.Map;

/**
 * Default de/referencer.
 */
public class DefaultReferenceAdapter implements ConfigTypeAdapter {

    // generic uuid-based reference
    static PathPattern uuidReferencePath = new PathPattern(Configuration.REFERENCE_BY_UUID_PATTERN);

    private final Map metadata = new HashMap<String, String>();

    public DefaultReferenceAdapter(BeanVitalizer vitalizer, Configuration config) {
        metadata.put("type", "string");
        metadata.put("class", "Reference");
    }

    @Override
    public Object fromConfigNode(Object configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer, Object parent) throws ConfigurationException {

        // TODO: remove this around beginning 2016
        // old deprecated style ref, for backwards-compatibility
        if (configNode instanceof String) {

            String refStr = (String) configNode;

            Configuration config = vitalizer.getContext(ConfigurationManager.class).getConfigurationStorage();
            Map<String, Object> referencedNode = (Map<String, Object>) config.getConfigurationNode(refStr, property.getRawClass());

            if (referencedNode == null) {
                if (property.isWeakReference())
                    return null;
                else
                    throw new ConfigurationException("Referenced node '" + refStr + "' not found");
            }

            // there is always uuid
            String uuid;
            try {
                uuid = (String) referencedNode.get(Configuration.UUID_KEY);
            } catch (Exception e) {
                throw new IllegalArgumentException("A referable node MUST have a UUID. A node referenced by " + refStr + " does not have UUID property.");
            }

            return getReferencedConfigurableObject(uuid, vitalizer, property);
        }
        // new style
        else {
            String uuidRefStr = (String) ((Map) configNode).get(Configuration.REFERENCE_KEY);

            String uuid = null;
            try {
                uuid = uuidReferencePath.parse(uuidRefStr).getParam("uuid");
            } catch (RuntimeException e) {
                // in case if it's not a map or is null or has no property
                throw new IllegalArgumentException("Unexpected value for reference property " + property.getAnnotatedName() + ", value" + configNode);
            }

            return getReferencedConfigurableObject(uuid, vitalizer, property);
        }
    }


    @SuppressWarnings("unchecked")
    protected Object getReferencedConfigurableObject(String uuid, BeanVitalizer vitalizer, AnnotatedConfigurableProperty property) {

        Object instanceFromPool = vitalizer.getInstanceFromThreadLocalPoolByUUID(uuid, property.getRawClass());

        if (instanceFromPool != null)
            return instanceFromPool;
        else {
            // fallback to simply loading by reference
            Configuration configuration = vitalizer.getContext(ConfigurationManager.class).getConfigurationStorage();
            Map<String, Object> referencedNode = (Map<String, Object>) configuration.getConfigurationNode(
                    uuidReferencePath.set("uuid", uuid).path(),
                    property.getRawClass()
            );

            return new ReflectiveAdapter().fromConfigNode(referencedNode, new AnnotatedConfigurableProperty(property.getRawClass()), vitalizer, null);
        }
    }

    @Override
    public Object toConfigNode(Object object, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
        Map<String, Object> node = Configuration.NodeFactory.emptyNode();

        AnnotatedConfigurableProperty uuidPropertyForClass = ConfigIterators.getUUIDPropertyForClass(property.getRawClass());
        if (uuidPropertyForClass == null)
            throw new ConfigurationException("Class " + property.getRawClass().getName() + " cannot be referenced, because it lacks a UUID property");

        String uuid;
        try {
            uuid = (String) PropertyUtils.getSimpleProperty(object, uuidPropertyForClass.getName());
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
        node.put(Configuration.REFERENCE_KEY, uuidReferencePath.set("uuid", uuid).path());

        if (property.isWeakReference())
            node.put(Configuration.WEAK_REFERENCE_KEY, true);

        return node;
    }

    @Override
    public Map<String, Object> getSchema(AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
        Map<String, Object> schema = new HashMap<String, Object>();
        schema.putAll(metadata);
        schema.put("referencedClass", property.getRawClass().getSimpleName());
        return schema;
    }

    @Override
    public Object normalize(Object configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
        return configNode;
    }

}
