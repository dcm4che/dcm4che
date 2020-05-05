package org.dcm4che3.conf.test;

import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.fail;

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
