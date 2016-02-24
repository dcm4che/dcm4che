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
import org.dcm4che3.conf.core.api.ConfigurableClassExtension;
import org.dcm4che3.conf.core.api.ConfigurableProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * This class shall NOT be referenced externally, it will be removed/renamed/refactored without notice.
 * @author Roman K
 */
public class ConfigIterators {

    /*
     * Caches to overcome slow reflection access
     */


    private static final Map<Class, List<AnnotatedConfigurableProperty>> configurableFieldsAndSettersCache = Collections.synchronizedMap(new HashMap<Class, List<AnnotatedConfigurableProperty>>());
    private static final Map<Class, List<AnnotatedConfigurableProperty>> configurableFieldsCache = Collections.synchronizedMap(new HashMap<Class, List<AnnotatedConfigurableProperty>>());
    private static final Map<Class, List<AnnotatedSetter>> configurableSettersCache = Collections.synchronizedMap(new HashMap<Class, List<AnnotatedSetter>>());
    private static final Map<Class, Boolean> isClassConfigurable = Collections.synchronizedMap(new HashMap<Class, Boolean>());

    public static class AnnotatedSetter {
        private Map<Type, Annotation> annotations;
        private List<AnnotatedConfigurableProperty> parameters;
        private Method method;

        public Map<Type, Annotation> getAnnotations() {
            return annotations;
        }

        public void setAnnotations(Map<Type, Annotation> annotations) {
            this.annotations = annotations;
        }

        public <T> T getAnnotation(Class<T> annotationType) {
            return (T) annotations.get(annotationType);
        }

        public List<AnnotatedConfigurableProperty> getParameters() {
            return parameters;
        }

        public void setParameters(List<AnnotatedConfigurableProperty> parameters) {
            this.parameters = parameters;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }
    }


    public static List<AnnotatedConfigurableProperty> getAllConfigurableFieldsAndSetterParameters(Class clazz) {

        if (configurableFieldsAndSettersCache.containsKey(clazz))
            return configurableFieldsAndSettersCache.get(clazz);

        List<AnnotatedConfigurableProperty> fields = new ArrayList<AnnotatedConfigurableProperty>( getAllConfigurableFields(clazz));
        for (AnnotatedSetter s : getAllConfigurableSetters(clazz)) fields.addAll(s.getParameters());

        configurableFieldsAndSettersCache.put(clazz, fields);

        return fields;
    }

    public static List<AnnotatedSetter> getAllConfigurableSetters(Class clazz) {
        List<AnnotatedSetter> list = configurableSettersCache.get(clazz);
        if (list != null) return list;

        processAndCacheAnnotationsForClass(clazz);

        return configurableSettersCache.get(clazz);
    }

    public static List<AnnotatedConfigurableProperty> getAllConfigurableFields(Class clazz) {

        //check cache
        List<AnnotatedConfigurableProperty> l = configurableFieldsCache.get(clazz);
        if (l != null) return l;
        processAndCacheAnnotationsForClass(clazz);

        return configurableFieldsCache.get(clazz);

    }

    public static boolean isConfigurableClass(Class clazz) {

        if (isClassConfigurable.containsKey(clazz))
            return isClassConfigurable.get(clazz);

        boolean isItForReal = clazz.getAnnotation(ConfigurableClass.class) != null;
        isClassConfigurable.put(clazz, isItForReal);

        return isItForReal;
    }

    public static AnnotatedConfigurableProperty getUUIDPropertyForClass(Class clazz) {
        for (AnnotatedConfigurableProperty annotatedConfigurableProperty : getAllConfigurableFieldsAndSetterParameters(clazz)) {
            if (annotatedConfigurableProperty.isUuid())
                return annotatedConfigurableProperty;
        }
        return null;
    }

    private static void processAndCacheAnnotationsForClass(Class clazz) {
        processAnnotatedSetters(clazz);
        processAnnotatedProperties(clazz);


        ConfigurableClass configClassAnno = (ConfigurableClass) clazz.getAnnotation(ConfigurableClass.class);
        if (configClassAnno == null)
            throw new IllegalArgumentException("Class '"+clazz.getName()+"' is not a configurable class. Make sure the a dependency to org.dcm4che.conf.core-api exists.");

        //// safeguards

        // refs vs extensions
        if (configClassAnno.referable() && ConfigurableClassExtension.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException("A configurable extension class MUST NOT be referable - violated by class " + clazz.getName());

        // make sure there is at most one UUID and olockHash
        int uuidProps = 0;
        int olockProps = 0;
        for (AnnotatedConfigurableProperty annotatedConfigurableProperty : getAllConfigurableFieldsAndSetterParameters(clazz)) {
            if (annotatedConfigurableProperty.isUuid()) uuidProps++;
            if (annotatedConfigurableProperty.isOlockHash()) olockProps++;
        }
        if (uuidProps>1)
            throw new IllegalArgumentException("A configurable class MUST NOT have more than one UUID field - violated by class " + clazz.getName());
        if (olockProps>1)
            throw new IllegalArgumentException("A configurable class MUST NOT have more than one optimistic locking hash field - violated by class " + clazz.getName());


    }


    private static List<AnnotatedSetter> processAnnotatedSetters(Class clazz) {
        List<AnnotatedSetter> list;
        list = new ArrayList<AnnotatedSetter>();

        // scan all methods including superclasses, assume each is a config-setter
        for (Method m : clazz.getMethods()) {
            AnnotatedSetter annotatedSetter = new AnnotatedSetter();
            annotatedSetter.setParameters(new ArrayList<AnnotatedConfigurableProperty>());


            Annotation[][] parameterAnnotations = m.getParameterAnnotations();
            Type[] genericParameterTypes = m.getGenericParameterTypes();

            // if method is no-arg, then it is not a setter
            boolean thisMethodIsNotASetter = true;

            for (int i = 0; i < parameterAnnotations.length; i++) {

                thisMethodIsNotASetter = false;

                AnnotatedConfigurableProperty property = null;
                try {
                    property = new AnnotatedConfigurableProperty(
                            annotationsArrayToMap(parameterAnnotations[i]),
                            genericParameterTypes[i]
                    );
                } catch (Exception e) {
                    // dirty..
                    thisMethodIsNotASetter = true;
                    break;
                }

                annotatedSetter.getParameters().add(property);

                // make sure all the parameters of this setter-wannabe are annotated
                if (property.getAnnotation(ConfigurableProperty.class) == null) {
                    thisMethodIsNotASetter = true;
                    break;
                }
            }

            // filter out non-setters
            if (thisMethodIsNotASetter) continue;

            list.add(annotatedSetter);
            annotatedSetter.setAnnotations(annotationsArrayToMap(m.getAnnotations()));
            annotatedSetter.setMethod(m);
        }

        configurableSettersCache.put(clazz, list);
        return list;
    }

    private static List<AnnotatedConfigurableProperty> processAnnotatedProperties(Class clazz) {
        List<AnnotatedConfigurableProperty> l;
        l = new ArrayList<AnnotatedConfigurableProperty>();

        // scan all fields from this class and superclasses
        for (Field field : getFieldsUpTo(clazz, null)) {
            if (field.getAnnotation(ConfigurableProperty.class) != null) {

                AnnotatedConfigurableProperty ap = new AnnotatedConfigurableProperty(
                        annotationsArrayToMap(field.getAnnotations()),
                        field.getName(),
                        field.getGenericType()
                );
                l.add(ap);
            }
        }

        configurableFieldsCache.put(clazz, l);
        return l;
    }

    public static Map<Type, Annotation> annotationsArrayToMap(Annotation[] annos) {
        HashMap<Type, Annotation> annotations = new HashMap<Type, Annotation>();
        for (Annotation anno : annos)
            annotations.put(anno.annotationType(), anno);
        return annotations;
    }

    /**
     * Iterates over the whole hierarchy of classes starting from the startClass.
     * Taken from http://stackoverflow.com/questions/16966629/what-is-the-difference-between-getfields-getdeclaredfields-in-java-reflection
     */
    public static Iterable<Field> getFieldsUpTo(Class<?> startClass,
                                                Class<?> exclusiveParent) {

        List<Field> currentClassFields = new ArrayList<Field>();
        currentClassFields.addAll(Arrays.asList(startClass.getDeclaredFields()));

        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null &&
                (exclusiveParent == null || !(parentClass.equals(exclusiveParent)))) {
            List<Field> parentClassFields =
                    (List<Field>) getFieldsUpTo(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

}
