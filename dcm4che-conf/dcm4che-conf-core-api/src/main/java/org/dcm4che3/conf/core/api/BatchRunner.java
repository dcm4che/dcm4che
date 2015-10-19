package org.dcm4che3.conf.core.api;

/**
 * @author Roman K
 */
public interface BatchRunner {
    /**
     * Provides support for batching configuration changes.
     * </p>
     * The method implementation must ensure that the batch-changes are executed within a transaction.
     * The implementation may decide to run the changes either in
     * <ul>
     * <li>the context of an already existing transaction</li>
     * <li>the context of a new transaction</li>
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
