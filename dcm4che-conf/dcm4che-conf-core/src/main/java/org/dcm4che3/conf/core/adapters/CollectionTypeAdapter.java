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

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Note: As a parent for it's elements, the collection adapter will pass it's own parent, and not itself
 *
 * @author Roman K
 */
public class CollectionTypeAdapter< V extends Collection,T extends Collection> implements ConfigTypeAdapter<V, T> {

    private Class<V> clazz;
    private Class<T> clazzNode;

    public CollectionTypeAdapter(Class<? extends V> clazzVitalized, Class<? extends T> clazzNode) {
        this.clazz = (Class<V>) clazzVitalized;
        this.clazzNode = (Class<T>) clazzNode;
    }

    private V createCollection() throws ConfigurationException {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    private T createCollectionNode() throws ConfigurationException {
        try {
            return clazzNode.newInstance();
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    private V createCollectionVitalized(ConfigProperty property) throws ConfigurationException {
        if (EnumSet.class.isAssignableFrom(property.getRawClass())) {
            Class enumClass = (Class) property.getTypeForGenericsParameter(0);
            return (V) EnumSet.noneOf(enumClass);
        } else
            return createCollection();
    }

    @Override
    public V fromConfigNode(T configNode, ConfigProperty property, LoadingContext ctx, Object parent) throws ConfigurationException {

        ConfigProperty elementPseudoProperty = property.getPseudoPropertyForCollectionElement();

        ConfigTypeAdapter elementAdapter;
        if (property.isCollectionOfReferences())
            elementAdapter = ctx.getVitalizer().getReferenceTypeAdapter();
        else
            elementAdapter = ctx.getVitalizer().lookupTypeAdapter(elementPseudoProperty);

        V collection = createCollectionVitalized(property);

        for (Object o : configNode)
            collection.add(elementAdapter.fromConfigNode(o, elementPseudoProperty, ctx, parent));

        return collection;
    }

    @Override
    public T toConfigNode(V object, ConfigProperty property, SavingContext ctx) throws ConfigurationException {

        ConfigProperty elementPseudoProperty = property.getPseudoPropertyForCollectionElement();

        ConfigTypeAdapter elementAdapter;
        if (property.isCollectionOfReferences())
            elementAdapter = ctx.getVitalizer().getReferenceTypeAdapter();
        else
            elementAdapter = ctx.getVitalizer().lookupTypeAdapter(elementPseudoProperty);

        T node = createCollectionNode();
        for (Object element : object)
            node.add(elementAdapter.toConfigNode(element, elementPseudoProperty, ctx));

        return node;
    }

    @Override
    public Map<String, Object> getSchema(ConfigProperty property, ProcessingContext ctx) throws ConfigurationException {

        Map<String, Object> metadata = new HashMap<String, Object>();
        Map<String, Object> elementMetadata = new HashMap<String, Object>();

        metadata.put("type", "array");

        ConfigProperty elementPseudoProperty = property.getPseudoPropertyForCollectionElement();

        ConfigTypeAdapter elementAdapter;
        if (property.isCollectionOfReferences())
            elementAdapter = ctx.getVitalizer().getReferenceTypeAdapter();
        else
            elementAdapter = ctx.getVitalizer().lookupTypeAdapter(elementPseudoProperty);

        elementMetadata.putAll(elementAdapter.getSchema(elementPseudoProperty, ctx));
        metadata.put("items", elementMetadata);

        return metadata;
    }

    @Override
    public T normalize(Object configNode, ConfigProperty property, ProcessingContext ctx) throws ConfigurationException {
        if (configNode == null)
            return createCollectionNode();
        return (T) configNode;
    }
}
