package org.dcm4che3.conf.core.context;

import org.dcm4che3.conf.core.api.TypeSafeConfiguration;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;

/**
 * @author rawmahn
 */
public class ProcessingContext {

    protected final BeanVitalizer vitalizer;
    protected final TypeSafeConfiguration typeSafeConfiguration;

    public ProcessingContext(TypeSafeConfiguration typeSafeConfiguration) {
        this.vitalizer = typeSafeConfiguration.getVitalizer() ;
        this.typeSafeConfiguration = typeSafeConfiguration;
    }

    public ProcessingContext(BeanVitalizer vitalizer) {
        this.vitalizer = vitalizer ;
        this.typeSafeConfiguration = null;
    }


    public TypeSafeConfiguration getTypeSafeConfiguration() {
        return typeSafeConfiguration;
    }

    public BeanVitalizer getVitalizer() {
        return vitalizer;
    }
}
