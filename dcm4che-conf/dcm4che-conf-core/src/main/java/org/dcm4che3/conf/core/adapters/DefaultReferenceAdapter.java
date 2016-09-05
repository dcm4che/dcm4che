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

import org.dcm4che3.conf.core.api.*;
import org.dcm4che3.conf.core.context.LoadingContext;
import org.dcm4che3.conf.core.context.ProcessingContext;
import org.dcm4che3.conf.core.context.SavingContext;
import org.dcm4che3.conf.core.api.internal.*;
import org.dcm4che3.conf.core.util.PathPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Default de/referencer.
 */
public class DefaultReferenceAdapter implements ConfigTypeAdapter {

    private static Logger log = LoggerFactory.getLogger(DefaultReferenceAdapter.class);

    // generic uuid-based reference
    private static final PathPattern uuidReferencePath = new PathPattern(Configuration.REFERENCE_BY_UUID_PATTERN);

    private final Map<String, String> metadata = new HashMap<String, String>();

    public DefaultReferenceAdapter() {
        metadata.put("type", "string");
        metadata.put("class", "Reference");
    }

    @Override
    public Object fromConfigNode(Object configNode, ConfigProperty property, LoadingContext ctx, Object parent) throws ConfigurationException {

        // old deprecated style ref, for backwards-compatibility
        if (configNode instanceof String) {

            return resolveDeprecatedReference((String) configNode, property, ctx);
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

            return getReferencedConfigurableObject(uuid, ctx, property);
        }
    }

    private Object resolveDeprecatedReference(String configNode, ConfigProperty property, LoadingContext ctx) {
        String refStr = configNode;

        log.warn("Using deprecated reference format for configuration: " + refStr);

        Configuration config = ctx.getTypeSafeConfiguration().getLowLevelAccess();
        Iterator search = config.search(refStr);

        Map<String, Object> referencedNode = null;
        if (search.hasNext())
            referencedNode = (Map<String, Object>) search.next();

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
            if (uuid == null) throw new RuntimeException();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("A referable node MUST have a UUID. A node referenced by " + refStr + " does not have UUID property.");
        }

        return getReferencedConfigurableObject(uuid, ctx, property);
    }


    @SuppressWarnings("unchecked")
    private Object getReferencedConfigurableObject(String uuid, LoadingContext ctx, ConfigProperty property) {
        Object byUUID = ctx.getTypeSafeConfiguration().findByUUID(uuid, property.getRawClass(), ctx);

        if (byUUID == null)
            throw new ConfigurationException("Referenced node with uuid '" + uuid + "' not found");

        return byUUID;
    }

    @Override
    public Object toConfigNode(Object object, ConfigProperty property, SavingContext ctx) throws ConfigurationException {
        Map<String, Object> node = Configuration.NodeFactory.emptyNode();

        ConfigProperty uuidPropertyForClass = ConfigReflection.getUUIDPropertyForClass(property.getRawClass());
        if (uuidPropertyForClass == null)
            throw new ConfigurationException("Class " + property.getRawClass().getName() + " cannot be referenced, because it lacks a UUID property");

        String uuid;
        uuid = (String) ConfigReflection.getProperty(object, uuidPropertyForClass);
        node.put(Configuration.REFERENCE_KEY, uuidReferencePath.set("uuid", uuid).path());

        if (property.isWeakReference())
            node.put(Configuration.WEAK_REFERENCE_KEY, true);

        return node;
    }

    @Override
    public Map<String, Object> getSchema(ConfigProperty property, ProcessingContext ctx) throws ConfigurationException {
        Map<String, Object> schema = new HashMap<String, Object>();
        schema.putAll(metadata);
        schema.put("referencedClass", property.getRawClass().getSimpleName());
        return schema;
    }

    @Override
    public Object normalize(Object configNode, ConfigProperty property, ProcessingContext ctx) throws ConfigurationException {
        return configNode;
    }

}
