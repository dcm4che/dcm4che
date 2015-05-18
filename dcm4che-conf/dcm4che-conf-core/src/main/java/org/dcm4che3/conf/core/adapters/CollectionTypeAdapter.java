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

import org.dcm4che3.conf.core.api.internal.ConfigTypeAdapter;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.internal.AnnotatedConfigurableProperty;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;
import org.dcm4che3.conf.core.api.ConfigurableProperty;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman K
 */
public class CollectionTypeAdapter<T extends Collection> implements ConfigTypeAdapter<T, T> {

    private Class clazz;

    public CollectionTypeAdapter(Class<? extends T> clazz) {
        this.clazz = clazz;
    }

    private T createCollection() throws ConfigurationException {
        try {
            return (T) clazz.newInstance();
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    private Collection createCollectionDeserialized(AnnotatedConfigurableProperty property) throws ConfigurationException {
        if (EnumSet.class.isAssignableFrom(property.getRawClass())) {
            Class enumClass = (Class) property.getTypeForGenericsParameter(0);
            return EnumSet.noneOf(enumClass);
        } else
            return createCollection();
    }

    @Override
    public T fromConfigNode(T configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {

        AnnotatedConfigurableProperty elementPseudoProperty = property.getPseudoPropertyForCollectionElement();

        ConfigTypeAdapter elementAdapter;
        if (property.getAnnotation(ConfigurableProperty.class).collectionOfReferences())
            elementAdapter = vitalizer.getReferenceTypeAdapter();
        else
            elementAdapter = vitalizer.lookupTypeAdapter(elementPseudoProperty);

        T collection = (T) createCollectionDeserialized(property);

        for (Object o : configNode)
            collection.add(elementAdapter.fromConfigNode(o, elementPseudoProperty, vitalizer));

        return collection;
    }

    @Override
    public T toConfigNode(T object, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {

        AnnotatedConfigurableProperty elementPseudoProperty = property.getPseudoPropertyForCollectionElement();

        ConfigTypeAdapter elementAdapter;
        if (property.getAnnotation(ConfigurableProperty.class).collectionOfReferences())
            elementAdapter = vitalizer.getReferenceTypeAdapter();
        else
            elementAdapter = vitalizer.lookupTypeAdapter(elementPseudoProperty);

        T node = createCollection();
        for (Object element : object)
            node.add(elementAdapter.toConfigNode(element, elementPseudoProperty, vitalizer));

        return node;
    }

    @Override
    public Map<String, Object> getSchema(AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {

        Map<String, Object> metadata = new HashMap<String, Object>();
        Map<String, Object> elementMetadata = new HashMap<String, Object>();

        metadata.put("type", "array");

        AnnotatedConfigurableProperty elementPseudoProperty = property.getPseudoPropertyForCollectionElement();

        ConfigTypeAdapter elementAdapter;
        if (property.getAnnotation(ConfigurableProperty.class).collectionOfReferences())
            elementAdapter = vitalizer.getReferenceTypeAdapter();
        else
            elementAdapter = vitalizer.lookupTypeAdapter(elementPseudoProperty);

        elementMetadata.putAll(elementAdapter.getSchema(elementPseudoProperty, vitalizer));
        metadata.put("items", elementMetadata);

        return metadata;
    }

    @Override
    public T normalize(Object configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
        if (configNode == null)
            return createCollection();
        return (T) configNode;
    }
}
