package org.dcm4che3.conf.core.api.internal;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.context.LoadingContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

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

    ConfigTypeAdapter lookupTypeAdapter(ConfigProperty property) throws ConfigurationException;

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
    @Deprecated
    <T> T newConfiguredInstance(Map<String, Object> configNode, Class<T> clazz) throws ConfigurationException;

    <T> T newConfiguredInstance(Map<String, Object> configurationNode, Class<T> clazz, LoadingContext ctx);


    /**
     * A factory to create an empty instance. Override e.g. to use CDI or whatever
     * @param clazz
     * @param <T>
     * @return
     * @throws ConfigurationException
     */
    <T> T newInstance(Class<T> clazz) throws ConfigurationException;

    Map<String, Object> createConfigNodeFromInstance(Object object) throws ConfigurationException;

    Map<String, Object> createConfigNodeFromInstance(Object object, Class configurableClass) throws ConfigurationException;


    /*****************/
     /* EXTENSIBILITY */
     /***************/

    //TODO: add factory registration

    /**
     * Returns a list of registered extensions for a specified base extension class
     */
    List<Class> getExtensionClassesByBaseClass(Class extensionBaseClass);

    /*****************/
     /* REFERENCES */
    /***************/


    Object resolveFutureOrFail(String uuid, Future<Object> existingFuture);

    Map<String, Object> getSchemaForConfigurableClass(Class<?> clazz);
}
