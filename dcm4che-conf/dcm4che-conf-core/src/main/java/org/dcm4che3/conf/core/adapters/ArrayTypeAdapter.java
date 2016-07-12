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

import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.context.LoadingContext;
import org.dcm4che3.conf.core.context.ProcessingContext;
import org.dcm4che3.conf.core.context.SavingContext;
import org.dcm4che3.conf.core.api.internal.ConfigTypeAdapter;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.util.Base64;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Arrays.
 * Special case - byte arrays are encoded as base64 strings
 */
public class ArrayTypeAdapter implements ConfigTypeAdapter<Object, Object> {

    private static final Map<Class, Class> PRIMITIVE_TO_WRAPPER;

    static {

        PRIMITIVE_TO_WRAPPER = new HashMap<Class, Class>();
        PRIMITIVE_TO_WRAPPER.put(boolean.class, Boolean.class);
        PRIMITIVE_TO_WRAPPER.put(byte.class, Byte.class);
        PRIMITIVE_TO_WRAPPER.put(char.class, Character.class);
        PRIMITIVE_TO_WRAPPER.put(double.class, Double.class);
        PRIMITIVE_TO_WRAPPER.put(float.class, Float.class);
        PRIMITIVE_TO_WRAPPER.put(int.class, Integer.class);
        PRIMITIVE_TO_WRAPPER.put(long.class, Long.class);
        PRIMITIVE_TO_WRAPPER.put(short.class, Short.class);
        PRIMITIVE_TO_WRAPPER.put(void.class, Void.class);
    }

    @Override
    public Object fromConfigNode(Object configNode, ConfigProperty property, LoadingContext ctx, Object parent) throws ConfigurationException {

        Class<?> componentType = ((Class) property.getType()).getComponentType();

        // handle null (in theory should never be the case since normalization is done)
        if (configNode == null) return Array.newInstance(componentType, 0);

        // handle byte[]. Expect a base64 String.
        if (componentType.equals(byte.class))
            try {
                return Base64.fromBase64((String) configNode);
            } catch (IOException e) {
                throw new ConfigurationException("Cannot read Base64", e);
            }


        // if it is a collection, create an array with proper component type
        if (Collection.class.isAssignableFrom(configNode.getClass())) {
            Collection l = ((Collection) configNode);
            Object arr = Array.newInstance(componentType, l.size());
            ConfigProperty componentPseudoProperty = property.getPseudoPropertyForCollectionElement();
            ConfigTypeAdapter elementTypeAdapter = ctx.getVitalizer().lookupTypeAdapter(componentPseudoProperty);
            int i = 0;
            for (Object el : l) {
                // deserialize element
                el = elementTypeAdapter.fromConfigNode(el, componentPseudoProperty, ctx, arr);

                // push to array
                try {
                    Array.set(arr, i++, el);
                } catch (IllegalArgumentException e) {
                    throw new ConfigurationException("Element type in the supplied collection does not match the target array's component type ( " + el.getClass().getName() + " vs " + componentType.getName() + " )", e);
                }
            }
            return arr;
        } else
            throw new ConfigurationException("Object of unexpected type (" + configNode.getClass().getName() + ") supplied for conversion into an array. Must be a collection.");
    }

    @Override
    public Object toConfigNode(Object object, ConfigProperty property, SavingContext ctx) throws ConfigurationException {

        if (object == null) return null;

        Class<?> componentType = ((Class) property.getType()).getComponentType();

        // handle byte[]. Convert to base64 String.
        if (componentType.equals(byte.class))
            return Base64.toBase64((byte[]) object);

        Class wrapperClass = PRIMITIVE_TO_WRAPPER.get(componentType);
        ConfigProperty componentPseudoProperty = property.getPseudoPropertyForCollectionElement();

        ConfigTypeAdapter elementTypeAdapter = ctx.getVitalizer().lookupTypeAdapter(componentPseudoProperty);

        ArrayList list = new ArrayList();
        for (int i = 0; i < Array.getLength(object); i++) {
            Object el = elementTypeAdapter.toConfigNode(Array.get(object, i), componentPseudoProperty, ctx);
            list.add(wrapperClass != null ? wrapperClass.cast(el) : el);
        }
        return list;
    }

    @Override
    public Map<String, Object> getSchema(ConfigProperty property, ProcessingContext ctx) throws ConfigurationException {

        Map<String, Object> metadata = new HashMap<String, Object>();

        // handle byte[]
        if (((Class) property.getType()).getComponentType().equals(byte.class)) {
            metadata.put("type", "string");
            metadata.put("class", "Base64");
            return metadata;
        }

        metadata.put("type", "array");


        ConfigProperty componentPseudoProperty = property.getPseudoPropertyForCollectionElement();
        metadata.put("items", ctx.getVitalizer().lookupTypeAdapter(componentPseudoProperty).getSchema(componentPseudoProperty, ctx));

        return metadata;
    }

    @Override
    public Object normalize(Object configNode, ConfigProperty property, ProcessingContext ctx) throws ConfigurationException {

        // byte[] is a special case
        if (((Class) property.getType()).getComponentType().equals(byte.class))
            return configNode;

        ConfigTypeAdapter elemAdapter = ctx.getVitalizer().lookupTypeAdapter(property.getPseudoPropertyForCollectionElement());

        // always create an empty collection
        Collection c = new ArrayList();

        if (configNode != null)
        for (Object o : (Collection) configNode) {
            c.add(elemAdapter.normalize(o, property.getPseudoPropertyForCollectionElement(), ctx));
        }

        return c;
    }

}
