package org.dcm4che3.conf.core.util;

import org.dcm4che3.conf.core.Nodes;

import java.util.ArrayList;
import java.util.List;


public class SplittedPath {
    private final int level;
    private List<String> outerPathItems;
    private List<String> innerPathitems;
    private int i;
    private List<String> pathItems;

    public SplittedPath(String path, int level) {
        this(Nodes.getPathItems(path), level);
    }

    public SplittedPath(List<String> pathItems, int level) {
        this.level = level;
        this.pathItems = pathItems;
        calc();
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
        for (String name : pathItems) {
            if (i++ <= level)
                getOuterPathItems().add(name);
            else
                getInnerPathitems().add(name);
        }
        return this;
    }

    private void setOuterPathItems(List<String> outerPathItems) {
        this.outerPathItems = outerPathItems;
    }

    private void setInnerPathitems(List<String> innerPathitems) {
        this.innerPathitems = innerPathitems;
    }
}
