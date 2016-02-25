package org.dcm4che3.conf.core.util;

import java.util.*;

/**
 * Tracks the current path from the root while tracersing
 */
public class PathTrackingConfigNodeFilter extends ConfigNodeTraverser.AConfigNodeFilter {

    public PathTrackingConfigNodeFilter(List<String> rootPathItems) {

        if (rootPathItems==null)
            throw new IllegalArgumentException("Unexpected error");

        for (String pathItem : rootPathItems) {
            path.push(pathItem);
        }
    }

    final protected Deque<String> path = new ArrayDeque<String>();

    @Override
    public void beforeNode(Map<String, Object> node) {
        super.beforeNode(node);
    }


    @Override
    public void beforeNodeElement(Map<String, Object> containerNode, String key, Object value) {
        path.push(key);
    }

    @Override
    public void afterNodeElement(Map<String, Object> containerNode, String key, Object value) {
        path.pop();
    }

    @Override
    public void beforeListElement(Collection list, int index, Object element) {
        path.push(Integer.toString(index));
    }

    @Override
    public void afterListElement(Collection list, int index, Object element) {
        path.pop();
    }

}
