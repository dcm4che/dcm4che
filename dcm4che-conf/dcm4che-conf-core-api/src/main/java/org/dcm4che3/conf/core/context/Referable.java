package org.dcm4che3.conf.core.context;

import java.util.concurrent.Future;

public class Referable {

    private Future<Object> confObjectFuture;
    private Object confObject;

    public Referable(Future<Object> confObjectFuture, Object confObject) {
        this.confObjectFuture = confObjectFuture;
        this.confObject = confObject;
    }

    public Future<Object> getConfObjectFuture() {
        return confObjectFuture;
    }

    /**
     * Might be not fully loaded
     */
    public Object getConfObject() {
        return confObject;
    }

}
