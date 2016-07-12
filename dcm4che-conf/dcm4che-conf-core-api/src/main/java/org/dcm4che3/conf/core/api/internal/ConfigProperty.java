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

import org.dcm4che3.conf.core.api.*;
import org.dcm4che3.conf.core.api.ConfigurableProperty.ConfigurablePropertyType;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * This class shall NOT be referenced externally, it will be removed/renamed/refactored without notice.
 *
 * @author Roman K
 */

public class ConfigProperty {

    private static final LDAP dummyLdapAnno = DummyConfigurableClass.class.getAnnotation(LDAP.class);
    private static final Map<Type, Annotation> dummyAnnotations = new HashMap<Type, Annotation>();

    static {
        try {
            ConfigurableProperty configurableProperty = DummyConfigurableClass.class.getField("dummy").getAnnotation(ConfigurableProperty.class);
            dummyAnnotations.put(ConfigurableProperty.class, configurableProperty);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("That was unexpected");
        }
    }

    private final Map<Type, Annotation> annotations;

    private final ConfigurableProperty configurablePropertyAnnotation;
    private LDAP ldapAnnotation;

    private final Type type;
    private final Class rawType;

    private final String name;
    private final String annotatedName;

    private final boolean isReference;
    private final boolean isUuid;
    private final boolean isCollectionOfConfObjects;
    private final boolean isMapOfConfObjects;
    private final boolean isOlockHash;
    private final boolean isExtensionsProperty;
    private final boolean isConfObject;
    private final boolean isCollection;
    private final boolean isCollectionOfReferences;
    private final boolean isArrayOfConfObjects;
    private final boolean isArray;
    private final boolean isMap;
    private final boolean isWeakReference;

    private final ConfigProperty pseudoPropertyForCollectionElement;
    private final ConfigProperty pseudoPropertyForConfigClassCollectionElement;

    private final Method valueOfMethod;
    private final ConfigurableProperty.EnumRepresentation enumRepresentation;
    private final Enum[] enumValues;

    private final Type[] genericsTypes;
    private final String defaultValue;


    public ConfigProperty(Map<Type, Annotation> annotations, String name, Type type) {
        this.annotations = annotations;
        this.name = name;
        this.type = type;

        // Annotations
        configurablePropertyAnnotation = (ConfigurableProperty) this.annotations.get(ConfigurableProperty.class);
        ldapAnnotation = (LDAP) this.annotations.get(LDAP.class);

        if (ldapAnnotation == null)
            ldapAnnotation = dummyLdapAnno;

        // Raw type
        if (this.type instanceof ParameterizedType)
            this.rawType = (Class) ((ParameterizedType) this.type).getRawType();
        else {
            this.rawType = (Class) this.type;
        }

        isOlockHash = configurablePropertyAnnotation.type().equals(ConfigurablePropertyType.OptimisticLockingHash);
        isUuid = configurablePropertyAnnotation.type().equals(ConfigurablePropertyType.UUID);

        //// annotated name ////
        if (isUuid) {
            annotatedName = Configuration.UUID_KEY;
        } else if (isOlockHash) {
            annotatedName = Configuration.OLOCK_HASH_KEY;
        } else if (configurablePropertyAnnotation.name().isEmpty()) {

            annotatedName = this.name;

            if (annotatedName == null)
                throw new ConfigurationException("Property name not specified");
        } else {
            annotatedName = configurablePropertyAnnotation.name();
        }

        defaultValue = configurablePropertyAnnotation.defaultValue();

        if (this.type instanceof ParameterizedType) {
            genericsTypes = ((ParameterizedType) this.type).getActualTypeArguments();
        } else
            genericsTypes = null;

        isReference = configurablePropertyAnnotation.isReference() ||
                configurablePropertyAnnotation.type().equals(ConfigurablePropertyType.Reference);

        isWeakReference = configurablePropertyAnnotation.weakReference();

        isExtensionsProperty = configurablePropertyAnnotation.isExtensionsProperty() ||
                configurablePropertyAnnotation.type().equals(ConfigurablePropertyType.ExtensionsProperty);

        isConfObject = ConfigReflection.isConfigurableClass(getRawClass());

        isCollection = Collection.class.isAssignableFrom(getRawClass());


        isCollectionOfReferences = configurablePropertyAnnotation.collectionOfReferences()
                || configurablePropertyAnnotation.type().equals(ConfigurablePropertyType.CollectionOfReferences);


        isMapOfConfObjects = Map.class.isAssignableFrom(getRawClass())
                && getPseudoPropertyForGenericsParamater(1).isConfObject()
                && !isCollectionOfReferences()
                && !isExtensionsProperty();

        isCollectionOfConfObjects = Collection.class.isAssignableFrom(getRawClass())
                && ConfigReflection.isConfigurableClass(getPseudoPropertyForGenericsParamater(0).getRawClass())
                && !isCollectionOfReferences();

        isArrayOfConfObjects = getRawClass().isArray()
                && ConfigReflection.isConfigurableClass(getRawClass().getComponentType())
                && !isCollectionOfReferences();

        isArray = rawType.isArray();

        isMap = Map.class.isAssignableFrom(getRawClass());


        pseudoPropertyForCollectionElement = calcPseudoPropertyForCollectionElement();
        pseudoPropertyForConfigClassCollectionElement = calcPseudoPropertyForConfigClassCollectionElement();

        if (rawType.isEnum()) {
            try {
                valueOfMethod = rawType.getMethod("valueOf", String.class);

                enumRepresentation = configurablePropertyAnnotation.enumRepresentation();

                Method valuesMethod = ((Class) getType()).getMethod("values");
                enumValues = (Enum[]) valuesMethod.invoke(null);

            } catch (Exception e) {
                throw new RuntimeException("Unexpected error while working with enum's methods", e);
            }

        } else {
            valueOfMethod = null;
            enumRepresentation = null;
            enumValues = null;
        }


    }

    public ConfigProperty(Map<Type, Annotation> annotations, Type type) {
        this(annotations, "dummy", type);
    }

    public ConfigProperty(Type type) {
        this(dummyAnnotations, type);
    }

    public ConfigProperty clone() {
        return new ConfigProperty(
                annotations,
                getName(),
                getType()
        );
    }

    public Enum<?> getEnumValueFor(String enumString) {
        try {
            return (Enum<?>) this.valueOfMethod.invoke(null, enumString);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unexpected error while working with enum's methods", e);
        } catch (InvocationTargetException e) {
            throw new ConfigurationException("Error while trying to convert '" + enumString + "' to enum [" + getRawClass() + "]", e.getCause());
        }
    }

    public List<ConfigurableProperty.Tag> getTags() {
        if (configurablePropertyAnnotation == null)
            return new ArrayList<ConfigurableProperty.Tag>();

        return new ArrayList<ConfigurableProperty.Tag>(Arrays.asList(configurablePropertyAnnotation.tags()));
    }

    //TODO: cache
    public ConfigProperty getPseudoPropertyForGenericsParamater(int genericParameterIndex) {

        Type typeForGenericsParameter = getTypeForGenericsParameter(genericParameterIndex);

        return new ConfigProperty(
                annotations,
                typeForGenericsParameter
        );
    }

    @Override
    public String toString() {
        return "ConfigProperty[name='" + name + "', annotatedName='" + annotatedName + "', rawType='" + rawType + "']";
    }


    public boolean isExtensionsProperty() {
        return isExtensionsProperty;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isOlockHash() {
        return isOlockHash;
    }

    public boolean isCollectionOfReferences() {
        return isCollectionOfReferences;
    }

    public boolean isReference() {
        return isReference;
    }


    public String getAnnotatedName() {
        return annotatedName;
    }

    public boolean isUuid() {
        return isUuid;
    }

    public boolean isWeakReference() {
        return isWeakReference;
    }

    public Type getTypeForGenericsParameter(int genericParameterIndex) {
        return genericsTypes[genericParameterIndex];
    }


    /**
     * get type of generic/component for collection/Array, Value type for map
     *
     * @return
     */
    public ConfigProperty getPseudoPropertyForConfigClassCollectionElement() {
        return pseudoPropertyForConfigClassCollectionElement;
    }

    public ConfigProperty calcPseudoPropertyForConfigClassCollectionElement() {

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

        return new ConfigProperty(annotations, type);
    }

    /**
     * get type of generic/component for collection/Array, Value type for map
     * Just copies other annotation parameters.
     *
     * @return null if not a collection/map
     */

    public ConfigProperty getPseudoPropertyForCollectionElement() {
        return pseudoPropertyForCollectionElement;
    }

    private ConfigProperty calcPseudoPropertyForCollectionElement() {

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
        // TODO: to get rid of getReferenceAdapter, we need to create child pseudoProp with reference=true in case of collectionOfReferences

        return new ConfigProperty(
                annotations,
                type
        );
    }

    @SuppressWarnings("unchecked")
    public <T> T getAnnotation(Class<T> annotationType) {
        if (annotationType.equals(ConfigurableProperty.class))
            return (T) configurablePropertyAnnotation;

        if (annotationType.equals(LDAP.class))
            return (T) ldapAnnotation;

        return (T) annotations.get(annotationType);
    }

    public Class getRawClass() {
        return rawType;
    }

    public boolean isMap() {
        return isMap;
    }

    public boolean isCollection() {
        return isCollection;
    }

    public boolean isArray() {
        return isArray;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }


    public boolean isConfObject() {
        return isConfObject;
    }

    public boolean isMapOfConfObjects() {
        return isMapOfConfObjects;
    }

    public boolean isCollectionOfConfObjects() {
        return isCollectionOfConfObjects;
    }

    public boolean isArrayOfConfObjects() {
        return isArrayOfConfObjects;
    }

    public Enum[] getEnumValues() {
        return enumValues;
    }

    public ConfigurableProperty.EnumRepresentation getEnumRepresentation() {
        return enumRepresentation;
    }


}
