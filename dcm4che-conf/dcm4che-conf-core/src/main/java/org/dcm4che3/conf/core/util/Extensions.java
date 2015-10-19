package org.dcm4che3.conf.core.util;

import org.dcm4che3.conf.core.api.ConfigurableClassExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Roman K
 */
public class Extensions {
    public static Map<Class, List<Class>> getAMapOfExtensionsByBaseExtension(List<ConfigurableClassExtension> allExtensions) {

        Map<Class, List<Class>> extensions = new HashMap<Class, List<Class>>();

        for (ConfigurableClassExtension extension : allExtensions) {
            Class baseExtensionClass = extension.getBaseClass();

            List<Class> extensionsForBaseClass = extensions.get(baseExtensionClass);

            if (extensionsForBaseClass == null)
                extensions.put(baseExtensionClass, extensionsForBaseClass = new ArrayList<Class>());

            // don't put duplicates
            if (!extensionsForBaseClass.contains(extension.getClass()))
                extensionsForBaseClass.add(extension.getClass());

        }
        return extensions;
    }

    public static Class<?> getExtensionClassBySimpleName(String extensionSimpleName, List allExtensionClasses) throws ClassNotFoundException {

        for (Class aClass : (List<Class>) allExtensionClasses) {
            if (aClass.getSimpleName().equals(extensionSimpleName)) return aClass;
        }

        throw new ClassNotFoundException();
    }
}
