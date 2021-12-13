package org.dcm4che3.conf.core.refindexing;

import java.util.HashMap;
import java.util.Map;

import org.dcm4che3.conf.core.DelegatingConfiguration;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Path;

/**
 * A  utility decorator that will remove nodes upon refresh. Used to simulate caching behavior.
 * @author Homero Cardoso de Almeida (homero.cardosodealmeida@agfa.com)
 */
public class SampleCachingConfigDecorator extends DelegatingConfiguration {

    private Map<String, Object> cache;

    public SampleCachingConfigDecorator(Configuration delegate) {
        super(delegate);
        cache = new HashMap<>();
    }

    @Override
    public Object getConfigurationNode(Path path, Class configurableClass) throws ConfigurationException {
        Object node = getNodeFromCache(path);
        if (node == null) {
            node = super.getConfigurationNode(path, configurableClass);
            putNodeIntoCache(path, node);
        }
        return node;
    }

    @Override
    public void refreshNode(Path path) throws ConfigurationException {
        cache.remove(path.toSimpleEscapedPath());
    }

    private Object getNodeFromCache(Path nodePath) {
        return cache.get(nodePath.toSimpleEscapedPath());
    }

    private void putNodeIntoCache(Path nodePath, Object node) {
        String key = nodePath.toSimpleEscapedPath();
        if (node != null) {
            cache.put(key, node);
        } else {
            cache.remove(key);
        }
    }
}
