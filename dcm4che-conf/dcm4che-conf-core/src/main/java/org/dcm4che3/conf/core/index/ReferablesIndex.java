package org.dcm4che3.conf.core.index;

import org.dcm4che3.conf.core.api.Path;

/**
 * @author rawmahn
 */
public interface ReferablesIndex {
    Path getPathByUUID(String uuid);
}
