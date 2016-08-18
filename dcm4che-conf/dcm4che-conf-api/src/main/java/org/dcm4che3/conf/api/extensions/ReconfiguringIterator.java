package org.dcm4che3.conf.api.extensions;

import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.api.internal.ConfigReflection;

/**
 *
 * @author Roman K
 */
public class ReconfiguringIterator {
    public static void reconfigure(Object source, Object target, Class configurableClass) {
        for (ConfigProperty property : ConfigReflection.getAllConfigurableFields(configurableClass)) {
            try {
                ConfigReflection.setProperty(target, property, ConfigReflection.getProperty(source, property));
            } catch (Exception e) {
                throw new RuntimeException("Unable to reconfigure instance of class " + property.getRawClass(), e);
            }
        }
    }
}
