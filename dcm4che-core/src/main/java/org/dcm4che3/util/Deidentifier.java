package org.dcm4che3.util;

import org.dcm4che3.data.VR;

/**
 * Created by Umberto Cappellini on 11/27/15.
 */
public interface Deidentifier {

    Object deidentify(int tag, VR vr, Object value);
}
