package org.dcm4che3.conf.core.util;

import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.api.internal.ConfigReflection;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * @author rawmahn
 */
public class PathFollower {


    /**
     * Walk the given path 'through' the class structures and return the 'trace' of all visited properties
     *
     * @return
     */
    public static Deque<ConfigProperty> traceProperties(Class rootConfigurableClazz, Path path) {


        ArrayDeque<ConfigProperty> trace = new ArrayDeque<ConfigProperty>(path.getPathItems().size());

        ConfigProperty current = ConfigReflection.getDummyPropertyForClass(rootConfigurableClazz);

        for (Object i : path.getPathItems()) {

            trace.addLast(current);

            if ((i instanceof String && !current.isConfObject() && !current.isMap())
                    || (i instanceof Number && !current.isCollection())) {
                throw new IllegalArgumentException(
                        "Unexpected path element " + i + " in path " + path + " for root class " + rootConfigurableClazz + ", corresponding property " + current
                );
            }

            if (current.isConfObject()) {
                List<ConfigProperty> props = ConfigReflection.getAllConfigurableFields(current.getRawClass());

                boolean found = false;
                for (ConfigProperty prop : props) {
                    if (prop.getAnnotatedName().equals(i)) {
                        current = prop;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    throw new IllegalArgumentException("Cannot find the property with name " + i + " in class " + current.getRawClass() + " while tracing path " + path);
                }

            } else if (current.isMap() || current.isCollection()) {
                current = current.getPseudoPropertyForCollectionElement();
            } else
                throw new IllegalArgumentException("Path " + path + " cannot be followed - property " + current + " is not supposed to have no children");


            // TODO: add support for extensions - for now there are no usecases that involve referables *inside* extensions (hl7apps however are a good example)

        }

        trace.addLast(current);

        return trace;
    }


}
