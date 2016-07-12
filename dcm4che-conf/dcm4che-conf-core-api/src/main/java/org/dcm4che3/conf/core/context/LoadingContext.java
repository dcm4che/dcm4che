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
    private final ConcurrentHashMap<String, Future<Object>> referables = new ConcurrentHashMap<String, Future<Object>>();

    public LoadingContext(TypeSafeConfiguration typeSafeConfiguration) {
        super(typeSafeConfiguration);
    }

    public LoadingContext(BeanVitalizer vitalizer) {
        super(vitalizer);
    }

    /**
     * Behaves like {@link ConcurrentMap#putIfAbsent}
     * @param uuid config object uuid
     * @param o the candidate future
     * @return
     */
    public Future<Object> registerConfigObjectFutureIfAbsent(String uuid, Future<Object> o) {
        return referables.putIfAbsent(uuid, o);
    }

    public Future<Object> getConfigObjectFuture(String uuid) {
        return referables.get(uuid);
    }

}
