package org.dcm4che3.conf.core.context;

import org.dcm4che3.conf.core.api.TypeSafeConfiguration;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

/**
 * @author rawmahn
 */
public class LoadingContext extends ProcessingContext {


    /**
     * A map of UUID to a loaded (being loaded) object.
     * Used to close circular reference loops, and for "optimistic" reference resolution.
     *
     * @return
     */
    private final ConcurrentHashMap<String, Referable> referables = new ConcurrentHashMap<String, Referable>();

    public LoadingContext(TypeSafeConfiguration typeSafeConfiguration) {
        super(typeSafeConfiguration);
    }

    public LoadingContext(BeanVitalizer vitalizer) {
        super(vitalizer);
    }

    /**
     * Behaves like {@link ConcurrentMap#putIfAbsent}
     *
     * @param uuid config object uuid
     * @param r    the candidate future
     * @return
     */
    public Referable registerReferableIfAbsent(String uuid, Referable r) {
        return referables.putIfAbsent(uuid, r);
    }

    public Referable getReferable(String uuid) {
        return referables.get(uuid);
    }

    public Future<Object> getConfigObjectFuture(String uuid) {
        Referable referable = referables.get(uuid);
        return referable == null ? null : referable.getConfObjectFuture();
    }

}
