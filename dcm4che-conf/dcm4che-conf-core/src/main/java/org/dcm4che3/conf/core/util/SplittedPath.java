package org.dcm4che3.conf.core.util;

import java.util.ArrayList;
import java.util.List;


public class SplittedPath {
    private final int level;
    private String path;
    private List<String> outerPathItems;
    private List<String> innerPathitems;
    private int i;

    public SplittedPath(String path, int level) {
        this.path = path;
        this.level = level;
    }

    public List<String> getOuterPathItems() {
        return outerPathItems;
    }

    public List<String> getInnerPathitems() {
        return innerPathitems;
    }

    public int getTotalDepth() {
        return i;
    }

    public SplittedPath calc() {
        setOuterPathItems(new ArrayList<String>());
        setInnerPathitems(new ArrayList<String>());
        i = 1;
        for (String name : SimpleConfigNodeUtil.getPathItems(path)) {
            if (i++ <= level)
                getOuterPathItems().add(name);
            else
                getInnerPathitems().add(name);
        }
        return this;
    }

    public void setOuterPathItems(List<String> outerPathItems) {
        this.outerPathItems = outerPathItems;
    }

    public void setInnerPathitems(List<String> innerPathitems) {
        this.innerPathitems = innerPathitems;
    }
}
