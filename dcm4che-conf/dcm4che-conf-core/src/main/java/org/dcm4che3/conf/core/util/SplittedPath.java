package org.dcm4che3.conf.core.util;

import java.util.ArrayList;
import java.util.List;


public class SplittedPath {
    private final int level;
    private List<Object> outerPathItems;
    private List<Object> innerPathitems;
    private int i;
    private List<Object> pathItems;

    public SplittedPath(List<Object> pathItems, int level) {
        this.level = level;
        this.pathItems = pathItems;
        this.outerPathItems = new ArrayList<Object>();
        this.innerPathitems = new ArrayList<Object>();
        i = 0;
        for (Object name : this.pathItems) {
            if (i++ < this.level)
                getOuterPathItems().add(name);
            else
                getInnerPathitems().add(name);
        }
    }

    public List<Object> getOuterPathItems() {
        return outerPathItems;
    }

    public List<Object> getInnerPathitems() {
        return innerPathitems;
    }

    public int getTotalDepth() {
        return i;
    }

}
