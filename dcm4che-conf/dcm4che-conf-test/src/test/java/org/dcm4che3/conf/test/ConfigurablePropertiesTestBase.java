/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2020
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.conf.test;

import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.fail;

/**
 * @author Zoe Zhang <zoe.zhang@agfa.com>
 */
public abstract class ConfigurablePropertiesTestBase {

    @Test
    public void testForPrimitiveWrapperExistence() throws NoSuchFieldException {
        for (Class clazz : getClassesToTest()) {
            Optional<Field> primitiveWrapperField = checkForPrimitiveWrapperFields(clazz);
            if (primitiveWrapperField.isPresent()) {
                Field theField = primitiveWrapperField.get();
                fail("Field '" + theField.getName() + "' in class '" + clazz.getCanonicalName()
                        + "' is of a primitive wrapper type of '" + theField.getType().getSimpleName() + "'.");
            }
        }
    }

    protected abstract Class[] getClassesToTest();

    protected abstract Set<Field> getPropertiesToSkip() throws NoSuchFieldException;

    private Optional<Field> checkForPrimitiveWrapperFields(Class theClass) throws NoSuchFieldException {
        Set<Field> propertiesToSkip = getPropertiesToSkip();
        Field[] fields = theClass.getDeclaredFields();
        for (Field f : fields) {
            if (propertiesToSkip != null && propertiesToSkip.contains(f)) {
                continue;
            }

            Annotation[] annotations = f.getDeclaredAnnotations();
            for (Annotation ann : annotations) {
                if (ann.annotationType().equals(ConfigurableProperty.class) && isPrimitiveWrapperType(f.getType())) {
                    return Optional.of(f);
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isPrimitiveWrapperType(Class clazz) {
        switch (clazz.getSimpleName()) {
            case "Boolean":
            case "Byte":
            case "Character":
            case "Float":
            case "Integer":
            case "Long":
            case "Short":
            case "Double":
                return true;
            default:
                return false;
        }
    }
}
