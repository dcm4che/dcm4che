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


import org.apache.commons.beanutils.PropertyUtils;
import org.dcm4che3.conf.core.api.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * This class shall NOT be referenced externally, it will be removed/renamed/refactored without notice.
 * Caches to minimize reflection access.
 *
 * @author Roman K
 */
public class ConfigReflection {


    private static final Map<Class, ClassInfo> classInfoCache = Collections.synchronizedMap(new HashMap<Class, ClassInfo>());
    private static final Map<Class, Boolean> isClassConfigurable = Collections.synchronizedMap(new HashMap<Class, Boolean>());

    private static final Map<Class, ConfigProperty> dummyPropsCache = Collections.synchronizedMap(new HashMap<Class, ConfigProperty>());

    public static List<ConfigProperty> getAllConfigurableFields(Class clazz) {
        return getClassInfo(clazz).configurableProperties;
    }

    public static ConfigProperty getDummyPropertyForClass(Class clazz) {
        ConfigProperty found = dummyPropsCache.get(clazz);

        if (found != null) {
            return found;
        } else {
            ConfigProperty property = new ConfigProperty(clazz);
            dummyPropsCache.put(clazz, property);
            return property;
        }
    }

    private static ClassInfo getClassInfo(Class clazz) {
        ClassInfo classInfo = classInfoCache.get(clazz);

        if (classInfo != null) {
            return classInfo;
        } else {
            return processAndCacheClassInfo(clazz);
        }
    }

    public static boolean isConfigurableClass(Class clazz) {

        if (isClassConfigurable.containsKey(clazz))
            return isClassConfigurable.get(clazz);

        boolean isItForReal = clazz.getAnnotation(ConfigurableClass.class) != null;
        isClassConfigurable.put(clazz, isItForReal);

        return isItForReal;
    }

    public static ConfigProperty getUUIDPropertyForClass(Class clazz) {
        return getClassInfo(clazz).uuidProperty;
    }


    private static ClassInfo processAndCacheClassInfo(Class clazz) {

        ConfigurableClass configClassAnno = (ConfigurableClass) clazz.getAnnotation(ConfigurableClass.class);
        if (configClassAnno == null)
            throw new IllegalArgumentException("Class '" + clazz.getName() + "' is not a configurable class. Make sure the a dependency to org.dcm4che.conf.core-api exists.");


        ClassInfo classInfo = scanClass(clazz);

        // some restrictions on extensions
        if (ConfigurableClassExtension.class.isAssignableFrom(clazz)) {

            if (configClassAnno.referable()) {
                throw new IllegalArgumentException("A configurable extension class MUST NOT be referable - violated by class " + clazz.getName());
            }

            if (classInfo.uuidProperty != null) {
                throw new IllegalArgumentException("A configurable extension class MUST NOT have a uuid - violated by class " + clazz.getName());
            }
        }

        classInfoCache.put(clazz, classInfo);

        return classInfo;
    }


    private static ClassInfo scanClass(Class clazz) {

        ClassInfo classInfo = new ClassInfo();
        classInfo.configurableProperties = new ArrayList<ConfigProperty>();

        // scan all fields from this class and superclasses
        for (Field field : getAllFields(clazz)) {
            if (field.getAnnotation(ConfigurableProperty.class) != null) {

                ConfigProperty ap = new ConfigProperty(
                        annotationsArrayToMap(field.getAnnotations()),
                        field.getName(),
                        field.getGenericType()
                );
                classInfo.configurableProperties.add(ap);

                if (ap.isUuid()) {
                    if (classInfo.uuidProperty != null) {
                        throw new IllegalArgumentException("A configurable class MUST NOT have more than one UUID field - violated by class " + clazz.getName());
                    }

                    classInfo.uuidProperty = ap;
                }

                if (ap.isOlockHash()) {
                    if (classInfo.olockHashProperty != null) {
                        throw new IllegalArgumentException("A configurable class MUST NOT have more than one optimistic locking hash field - violated by class " + clazz.getName());
                    }

                    classInfo.olockHashProperty = ap;
                }
            } else if (field.getAnnotation(Parent.class) != null) {
                if (classInfo.parentField != null) {
                    throw new IllegalArgumentException("A configurable class MUST NOT have more than one field annotated with @Parent - violated by class " + clazz.getName());
                }

                classInfo.parentField = field;
            }
        }


        if (!ConfigurableClassExtension.class.isAssignableFrom(clazz) && classInfo.uuidProperty == null && classInfo.parentField != null) {
            throw new IllegalArgumentException("A configurable class that refers to a @Parent must have a uuid property defined (except the extensions). Violated by " + clazz.getName());
        }

        return classInfo;
    }

    public static Map<Type, Annotation> annotationsArrayToMap(Annotation[] annos) {
        HashMap<Type, Annotation> annotations = new HashMap<Type, Annotation>();
        for (Annotation anno : annos)
            annotations.put(anno.annotationType(), anno);
        return annotations;
    }

    public static Field getParentPropertyForClass(Class<?> extensionClass) {
        return getClassInfo(extensionClass).parentField;
    }


    /**
     * Gets all the fields for the class and it's superclass(es)
     */
    private static List<Field> getAllFields(Class clazz) {

        List<Field> fields = new ArrayList<Field>();

        // get all fields of the current class (includes public, protected, default, and private fields)
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

        // go to the parent recursively
        Class<?> parent = clazz.getSuperclass();
        if (parent != null)
            fields.addAll(getAllFields(parent));

        return fields;
    }

    public static void setProperty(Object object, ConfigProperty property, Object value) throws ReflectionAccessException {
        setProperty(object, property.getName(), value);
    }

    public static void setProperty(Object object, String propertyName, Object value) throws ReflectionAccessException {
        try {
            PropertyUtils.setSimpleProperty(object, propertyName, value);
        } catch (IllegalAccessException e) {
            throw new ReflectionAccessException("Could not set property " + propertyName + " in class " + object.getClass().toString(), e);
        } catch (InvocationTargetException e) {
            throw new ReflectionAccessException("Could not set property " + propertyName + " in class " + object.getClass().toString(), e);
        } catch (NoSuchMethodException e) {
            throw new ReflectionAccessException("Could not set property " + propertyName + " in class " + object.getClass().toString(), e);
        }
    }

    public static Object getProperty(Object object, ConfigProperty property) throws ReflectionAccessException {
        try {
            return PropertyUtils.getSimpleProperty(object, property.getName());
        } catch (IllegalAccessException e) {
            throw new ReflectionAccessException("Could not get property " + property + " in class " + object.getClass().toString(), e);
        } catch (InvocationTargetException e) {
            throw new ReflectionAccessException("Could not get property " + property + " in class " + object.getClass().toString(), e);
        } catch (NoSuchMethodException e) {
            throw new ReflectionAccessException("Could not get property " + property + " in class " + object.getClass().toString(), e);
        }
    }

    private static class ClassInfo {

        List<ConfigProperty> configurableProperties;
        ConfigProperty uuidProperty;
        ConfigProperty olockHashProperty;
        Field parentField;

    }
}
