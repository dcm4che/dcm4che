package org.dcm4che3.conf.core.api;

public class Node<V> {

    V ref;

    V get() {
        return ref;
    }

    public boolean isPrimitive() {
        return ref == null ||
                ref instanceof Number ||
                ref instanceof String ||
                ref instanceof Boolean;
    }

}
