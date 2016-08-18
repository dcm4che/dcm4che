package org.dcm4che3.conf.core.context;

import org.dcm4che3.conf.core.api.TypeSafeConfiguration;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;

/**
 * @author rawmahn
 */
public class SavingContext extends ProcessingContext {

    public SavingContext(TypeSafeConfiguration typeSafeConfiguration) {
        super(typeSafeConfiguration);
    }

    public SavingContext(BeanVitalizer vitalizer) {
        super(vitalizer);
    }

}
