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

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.ConfigurableProperty.ConfigurablePropertyType;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.LDAP;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * This class shall NOT be referenced externally, it will be removed/renamed/refactored without notice.
 *
 * @author Roman K
 */
@LDAP
public class AnnotatedConfigurableProperty {

    private Map<Type, Annotation> annotations = new HashMap<Type, Annotation>();
    private Type type;
    private String name;

    @ConfigurableClass
    public static class DummyConfigurableClass {
        @ConfigurableProperty
        public int dummy;
    }


    public AnnotatedConfigurableProperty() {
    }

    public AnnotatedConfigurableProperty(Type type) {
        setType(type);

        // create dummy annotation
        try {
            annotations.put(
                    ConfigurableProperty.class,
                    DummyConfigurableClass.class.getField("dummy").getAnnotation(ConfigurableProperty.class)
            );
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Unexpected error", e);
        }

    }

    //////// wrapping annotations, otherwise it gets too unDRY ///////////////

    public boolean isExtensionsProperty() {
        return getAnnotation(ConfigurableProperty.class).isExtensionsProperty() ||
                getAnnotation(ConfigurableProperty.class).type().equals(ConfigurablePropertyType.ExtensionsProperty);
    }

    public String getDefaultValue() {
        return getAnnotation(ConfigurableProperty.class).defaultValue();
    }

    public boolean isOlockHash() {
        return getAnnotation(ConfigurableProperty.class).type().equals(ConfigurablePropertyType.OptimisticLockingHash);
    }

    public boolean isCollectionOfReferences() {
        return getAnnotation(ConfigurableProperty.class).collectionOfReferences() ||
                getAnnotation(ConfigurableProperty.class).type().equals(ConfigurablePropertyType.CollectionOfReferences);
    }

    public boolean isReference() {
        return getAnnotation(ConfigurableProperty.class).isReference() ||
                getAnnotation(ConfigurableProperty.class).type().equals(ConfigurablePropertyType.Reference);
    }

    public List<ConfigurableProperty.Tag> getTags() {
        if (getAnnotation(ConfigurableProperty.class) == null)
            return new ArrayList<ConfigurableProperty.Tag>();

        return new ArrayList<ConfigurableProperty.Tag>(Arrays.asList(getAnnotation(ConfigurableProperty.class).tags()));
    }

    public String getAnnotatedName() throws ConfigurationException {

        String name = getAnnotation(ConfigurableProperty.class).name();
        if (!name.equals("")) return name;
        name = this.name;
        if (name != null) return name;
        throw new ConfigurationException("Property name not specified");

    }

    public boolean isUuid() {
        return getAnnotation(ConfigurableProperty.class).type().equals(ConfigurablePropertyType.UUID);
    }

    public boolean isWeakReference() {
        return getAnnotation(ConfigurableProperty.class).weakReference();
    }
    ////////////////////////////////////////////

    public AnnotatedConfigurableProperty clone() {

        AnnotatedConfigurableProperty property = new AnnotatedConfigurableProperty();
        property.setAnnotations(getAnnotations());
        property.setType(getType());
        property.setName(getName());
        return property;
    }

    public Map<Type, Annotation> getAnnotations() {
        return annotations;
    }

    public Type getTypeForGenericsParameter(int genericParameterIndex) {
        Type[] actualTypeArguments = ((ParameterizedType) getType()).getActualTypeArguments();
        return actualTypeArguments[genericParameterIndex];
    }

    public AnnotatedConfigurableProperty getPseudoPropertyForGenericsParamater(int genericParameterIndex) {

        Type typeForGenericsParameter = getTypeForGenericsParameter(genericParameterIndex);
        AnnotatedConfigurableProperty clone = clone();
        clone.setType(typeForGenericsParameter);
        return clone;
    }

    /**
     * get type of generic/component for collection/Array, Value type for map
     *
     * @return
     */
    public AnnotatedConfigurableProperty getPseudoPropertyForConfigClassCollectionElement() {

        Type type;
        if (isMapOfConfObjects())
            type = getTypeForGenericsParameter(1);
        else if (isCollectionOfConfObjects())
            type = getTypeForGenericsParameter(0);
        else if (isArrayOfConfObjects())
            type = getRawClass().getComponentType();
        else
            return null;
        //throw new IllegalArgumentException("This property is not a collection/array/map - "+getType());

        AnnotatedConfigurableProperty clone = clone();
        clone.setType(type);
        return clone;
    }

    /**
     * get type of generic/component for collection/Array, Value type for map
     * Just copies other annotation parameters.
     *
     * @return null if not a collection/map
     */
    public AnnotatedConfigurableProperty getPseudoPropertyForCollectionElement() {

        Type type;
        if (Map.class.isAssignableFrom(getRawClass()))
            type = getTypeForGenericsParameter(1);
        else if (Collection.class.isAssignableFrom(getRawClass()))
            type = getTypeForGenericsParameter(0);
        else if (getRawClass().isArray())
            type = getRawClass().getComponentType();
        else
            return null;
        //throw new IllegalArgumentException("This property is not a collection/array/map - "+getType());

        // TODO: only specific params shold be cloned...
        AnnotatedConfigurableProperty clone = clone();
        clone.setType(type);
        return clone;
    }

    public Class getRawClass() {
        Class clazz;

        if (type instanceof ParameterizedType)
            clazz = (Class) ((ParameterizedType) type).getRawType();
        else {
            clazz = (Class) type;
        }
        return clazz;
    }

    public boolean isMap() {
        return Map.class.isAssignableFrom(getRawClass());
    }

    public boolean isCollection() {
        return Collection.class.isAssignableFrom(getRawClass());
    }

    public boolean isArray() {
        return getRawClass().isArray();
    }

    public void setAnnotations(Map<Type, Annotation> annotations) {
        this.annotations = annotations;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAnnotation(Class<T> annotationType) {
        T t = (T) annotations.get(annotationType);
        if (t == null && annotationType.equals(LDAP.class)) {
            return (T) AnnotatedConfigurableProperty.class.getAnnotation(LDAP.class);
        }
        return t;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isConfObject() {
        return getRawClass().getAnnotation(ConfigurableClass.class) != null;
    }

    public boolean isMapOfConfObjects() {
        return Map.class.isAssignableFrom(getRawClass())
                && getPseudoPropertyForGenericsParamater(1).isConfObject()
                && !isCollectionOfReferences()
                && !isExtensionsProperty();
    }

    public boolean isCollectionOfConfObjects() {
        return Collection.class.isAssignableFrom(getRawClass())
                && getPseudoPropertyForGenericsParamater(0).getRawClass().getAnnotation(ConfigurableClass.class) != null
                && !isCollectionOfReferences();
    }

    public boolean isArrayOfConfObjects() {
        return getRawClass().isArray()
                && getRawClass().getComponentType().getAnnotation(ConfigurableClass.class) != null
                && !isCollectionOfReferences();
    }
}
