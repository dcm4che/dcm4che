package org.dcm4che3.conf.core.api.internal;

import org.dcm4che3.conf.core.api.ConfigurationException;

import java.util.Map;

/**
 * This API shall NOT be considered stable, it will be refactored without notice.
 * @author Roman K
 */
public interface BeanVitalizer {
    void setReferenceTypeAdapter(ConfigTypeAdapter referenceTypeAdapter);

    ConfigTypeAdapter getReferenceTypeAdapter();

    <T> T newConfiguredInstance(Map<String, Object> configNode, Class<T> clazz) throws ConfigurationException;

    <T> T newInstance(Class<T> clazz) throws ConfigurationException;

    <T> void configureInstance(T object, Map<String, Object> configNode, Class configurableClass) throws ConfigurationException;

    <T> Map<String, Object> createConfigNodeFromInstance(T object) throws ConfigurationException;

    <T> Map<String, Object> createConfigNodeFromInstance(T object, Class configurableClass) throws ConfigurationException;

    @SuppressWarnings("unchecked")
    ConfigTypeAdapter lookupTypeAdapter(AnnotatedConfigurableProperty property) throws ConfigurationException;

    @SuppressWarnings("unchecked")
    ConfigTypeAdapter lookupDefaultTypeAdapter(Class clazz) throws ConfigurationException;

    void registerContext(Class clazz, Object context);

    <T> T getContext(Class<T> clazz);

    void registerCustomConfigTypeAdapter(Class clazz, ConfigTypeAdapter typeAdapter);
}
