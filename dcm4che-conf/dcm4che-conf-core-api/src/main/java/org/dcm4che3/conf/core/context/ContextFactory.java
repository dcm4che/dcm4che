package org.dcm4che3.conf.core.context;

import org.dcm4che3.conf.core.api.TypeSafeConfiguration;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;

/**
 * @author rawmahn
 */
public class ContextFactory {

    private final TypeSafeConfiguration typeSafeConfiguration;
    private final BeanVitalizer vitalizer;

    public ContextFactory(TypeSafeConfiguration typeSafeConfiguration) {
        this.typeSafeConfiguration = typeSafeConfiguration;
        this.vitalizer = null;
    }
    public ContextFactory(BeanVitalizer vitalizer) {
        this.typeSafeConfiguration = null;
        this.vitalizer = vitalizer;
    }

    public LoadingContext newLoadingContext() {
        if (typeSafeConfiguration == null)
            return new LoadingContext(vitalizer);
        else
            return new LoadingContext(typeSafeConfiguration);
    }

    public SavingContext newSavingContext() {
        if (typeSafeConfiguration == null) {
            return new SavingContext(vitalizer);
        } else
            return new SavingContext(typeSafeConfiguration);
    }

    public ProcessingContext newProcessingContext() {
        if (typeSafeConfiguration==null)
            return new ProcessingContext(vitalizer);
        else
            return new ProcessingContext(typeSafeConfiguration);
    }

}
