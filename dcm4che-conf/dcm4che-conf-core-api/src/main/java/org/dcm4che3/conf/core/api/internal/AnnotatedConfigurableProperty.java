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

public class AnnotatedConfigurableProperty {

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

    private Map<Type, Annotation> annotations = new HashMap<Type, Annotation>();

    private ConfigurableProperty configurablePropertyAnnotation;
    private LDAP ldapAnnotation;

    private Type type;
    private Class rawType;

    private String name;
    private String annotatedName;

    private boolean isReference;
    private boolean isUuid;
    private boolean isCollectionOfConfObjects;
    private boolean isMapOfConfObjects;
    private boolean isOlockHash;
    private boolean isExtensionsProperty;
    private boolean isConfObject;
    private boolean isCollection;
    private boolean isCollectionOfReferences;
    private boolean isArrayOfConfObjects;
    private boolean isArray;


    public AnnotatedConfigurableProperty() {
    }

    public AnnotatedConfigurableProperty(Map<Type, Annotation> annotations, String name, Type type) {
        this.annotations = annotations;
        this.name = name;
        this.type = type;
        init();
    }

    public AnnotatedConfigurableProperty(Type type) {
        this.type = type;
        this.annotations = dummyAnnotations;
        this.name = "dummy";
        init();
    }

    public AnnotatedConfigurableProperty(Map<Type, Annotation> annotations, Type type) {
        this.annotations = annotations;
        this.type = type;
        this.name = "dummy";
        init();
    }

    //////// wrapping annotations, otherwise it gets too unDRY ///////////////

    public boolean isExtensionsProperty() {
        return isExtensionsProperty;
    }

    public String getDefaultValue() {
        return configurablePropertyAnnotation.defaultValue();
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

    public List<ConfigurableProperty.Tag> getTags() {
        if (configurablePropertyAnnotation == null)
            return new ArrayList<ConfigurableProperty.Tag>();

        return new ArrayList<ConfigurableProperty.Tag>(Arrays.asList(configurablePropertyAnnotation.tags()));
    }

    public String getAnnotatedName() throws ConfigurationException {
        return annotatedName;
    }

    public boolean isUuid() {
        return isUuid;
    }

    public boolean isWeakReference() {
        return configurablePropertyAnnotation.weakReference();
    }
    ////////////////////////////////////////////

    public AnnotatedConfigurableProperty clone() {
        return new AnnotatedConfigurableProperty(
                getAnnotations(),
                getName(),
                getType()
        );
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

        return new AnnotatedConfigurableProperty(
                annotations,
                typeForGenericsParameter
        );
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

        return new AnnotatedConfigurableProperty(annotations, type);
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


        return new AnnotatedConfigurableProperty(
                annotations,
                type
        );
    }

    public Class getRawClass() {
        return rawType;
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

    // init faster access
    private void init() {

        if (annotatedName != null) return;

        // conf prop
        configurablePropertyAnnotation = (ConfigurableProperty) annotations.get(ConfigurableProperty.class);
        ldapAnnotation = (LDAP) annotations.get(LDAP.class);

        if (ldapAnnotation == null)
            ldapAnnotation = dummyLdapAnno;


        // annotated name
        annotatedName = configurablePropertyAnnotation.name();
        if (annotatedName.isEmpty()) {
            annotatedName = this.name;
            if (annotatedName == null)
                throw new ConfigurationException("Property name not specified");
        }

        // type
        if (this.type instanceof ParameterizedType)
            this.rawType  = (Class) ((ParameterizedType) this.type).getRawType();
        else {
            this.rawType  = (Class) this.type;
        }


        if (configurablePropertyAnnotation != null) {

            isReference = configurablePropertyAnnotation.isReference() ||
                    configurablePropertyAnnotation.type().equals(ConfigurablePropertyType.Reference);

            isExtensionsProperty = configurablePropertyAnnotation.isExtensionsProperty() ||
                    configurablePropertyAnnotation.type().equals(ConfigurablePropertyType.ExtensionsProperty);

            isOlockHash = configurablePropertyAnnotation.type().equals(ConfigurablePropertyType.OptimisticLockingHash);

            isConfObject = ConfigIterators.isConfigurableClass(getRawClass());

            isCollection = Collection.class.isAssignableFrom(getRawClass());


            isCollectionOfReferences = configurablePropertyAnnotation.collectionOfReferences()
                    || configurablePropertyAnnotation.type().equals(ConfigurablePropertyType.CollectionOfReferences);


            isMapOfConfObjects = Map.class.isAssignableFrom(getRawClass())
                    && getPseudoPropertyForGenericsParamater(1).isConfObject()
                    && !isCollectionOfReferences()
                    && !isExtensionsProperty();

            isCollectionOfConfObjects = Collection.class.isAssignableFrom(getRawClass())
                    && ConfigIterators.isConfigurableClass(getPseudoPropertyForGenericsParamater(0).getRawClass())
                    && !isCollectionOfReferences();

            isUuid = configurablePropertyAnnotation.type().equals(ConfigurablePropertyType.UUID);


            isArrayOfConfObjects = getRawClass().isArray()
                    && getRawClass().getComponentType().getAnnotation(ConfigurableClass.class) != null
                    && !isCollectionOfReferences();

            isArray = isArray();

        }

    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAnnotation(Class<T> annotationType) {
        if (annotationType.equals(ConfigurableProperty.class))
            return (T) configurablePropertyAnnotation;

        if (annotationType.equals(LDAP.class))
            return (T) ldapAnnotation;

        return (T) annotations.get(annotationType);
    }

    public void setName(String name) {
        this.name = name;
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
}
