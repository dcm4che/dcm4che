package org.dcm4che3.conf.core.api.internal;

import org.dcm4che3.conf.core.api.ConfigurationException;

import java.util.Map;

/**
 * This API shall NOT be considered stable, it will be refactored without notice.
 * @author Roman K
 */
public interface BeanVitalizer {

    /*******************/
    /**** ADAPTERS *****/
    /*******************/

    ConfigTypeAdapter getReferenceTypeAdapter();

    void registerCustomConfigTypeAdapter(Class clazz, ConfigTypeAdapter typeAdapter);

    ConfigTypeAdapter lookupTypeAdapter(AnnotatedConfigurableProperty property) throws ConfigurationException;

    ConfigTypeAdapter lookupDefaultTypeAdapter(Class clazz) throws ConfigurationException;

    /********************/
     /* DE/SERIALIZE ******/
     /*******************/

    /**
     * This method includes circular reference resolution logic
     * @param configNode
     * @param clazz
     * @param <T>
     * @return
     * @throws ConfigurationException
     */
    <T> T newConfiguredInstance(Map<String, Object> configNode, Class<T> clazz) throws ConfigurationException;

    /**
     * A factory to create an empty instance. Override e.g. to use CDI or whatever
     * @param clazz
     * @param <T>
     * @return
     * @throws ConfigurationException
     */
    <T> T newInstance(Class<T> clazz) throws ConfigurationException;

    <T> Map<String, Object> createConfigNodeFromInstance(T object) throws ConfigurationException;

    <T> Map<String, Object> createConfigNodeFromInstance(T object, Class configurableClass) throws ConfigurationException;


    /*****************/
     /* EXTENSIBILITY */
     /***************/


    void registerContext(Class clazz, Object context);

    <T> T getContext(Class<T> clazz);

    /*****************/
     /* REFERENCES */
    /***************/

    /**
     * Used to resolve circular references while deserializing config.
     * @param uuid uuid of a configurable object
     * @param expectedClazz
     * @return this object, if it was already deserialized or it's deserialization has started (but e.g. is not finished because we are resolving a circular reference)
     * or null if the object with this uuid was not yet loaded in this load context
     */
    <T> T getInstanceFromThreadLocalPoolByUUID(String uuid, Class<T> expectedClazz);

    /**
     *
     * @param uuid uuid of a configurable object
     * @param instance an object that corresponds to the uuid, which deserialization has started
     */
    void registerInstanceInThreadLocalPool(String uuid, Object instance);
}
