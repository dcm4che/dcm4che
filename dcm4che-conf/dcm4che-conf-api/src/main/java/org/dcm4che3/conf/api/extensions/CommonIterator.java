package org.dcm4che3.conf.api.extensions;

import org.apache.commons.beanutils.PropertyUtils;
import org.dcm4che3.conf.core.api.internal.AnnotatedConfigurableProperty;
import org.dcm4che3.conf.core.api.internal.ConfigIterators;

/**
 * @author Roman K
 */
public class CommonIterator {
    public static void reconfigure(Object source, Object target, Class configurableClass) {
        for (AnnotatedConfigurableProperty property : ConfigIterators.getAllConfigurableFields(configurableClass)) {
            try {
                PropertyUtils.setSimpleProperty(target, property.getName(), PropertyUtils.getSimpleProperty(source, property.getName()));
            } catch (Exception e) {
                throw new RuntimeException("Unable to reconfigure instance of class " + property.getRawClass(), e);
            }
        }
    }
}
