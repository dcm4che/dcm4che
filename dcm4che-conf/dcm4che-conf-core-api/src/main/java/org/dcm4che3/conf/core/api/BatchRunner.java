package org.dcm4che3.conf.core.api;

/**
 * @author Roman K
 */
public interface BatchRunner {
    /**
     * Provides support for batching configuration changes.
     * </p>
     * It is guaranteed that
     * <ul>
     * <li>All configuration reads and writes in a batch are performed within a single transaction, i.e. the changes are atomic and performed in READ COMMITTED isolation</li>
     * <li>When this method is called, an ongoing transaction (in case there is one) will be SUSPENDED. The batch will ALWAYS be executed in a SEPARATE transaction.</li>
     * <li>At most ONE batch is executed at the same time in the whole cluster (synchronized with a database lock)</li>
     * </ul>
     *
     * @param batch Configuration batch change to execute
     */
    void runBatch(Batch batch);

    /**
     * Defines a configuration batch that allows to execute configuration changes in a bulk-type manner.
     *
     * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
     */
    interface Batch extends Runnable{
    }
}
