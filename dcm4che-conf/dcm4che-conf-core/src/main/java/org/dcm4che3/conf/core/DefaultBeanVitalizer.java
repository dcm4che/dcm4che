/*
 * **** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2014
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */
package org.dcm4che3.conf.core;

/**
 * @author Roman K
 */

import org.dcm4che3.conf.core.adapters.*;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;
import org.dcm4che3.conf.core.api.internal.ConfigReflection;
import org.dcm4che3.conf.core.api.internal.ConfigTypeAdapter;
import org.dcm4che3.conf.core.context.ContextFactory;
import org.dcm4che3.conf.core.context.LoadingContext;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Handles the conversion between annotated Java objects and config nodes from a configuration backend.
 */
public class DefaultBeanVitalizer implements BeanVitalizer {

    private final Map<Class, ConfigTypeAdapter> customConfigTypeAdapters = new HashMap<Class, ConfigTypeAdapter>();
    private int loadingTimeoutSec = 5;

    private final Map<Class, List<Class>> extensionsByClass;

    private final ConfigTypeAdapter referenceTypeAdapter = DefaultConfigTypeAdapters.getReferenceAdapter();

    private final ContextFactory contextFactory;

    /**
     * "Standalone" vitalizer. This should only be used for e.g. tests.
     * To be able to handle references, custom context factories, etc, vitalizer must be bound to typeSafeConfiguration
     */
    public DefaultBeanVitalizer() {
        contextFactory = new ContextFactory(this);
        extensionsByClass = new HashMap<Class, List<Class>>();
    }

    public DefaultBeanVitalizer(Map<Class, List<Class>> extensionsByClass, ContextFactory contextFactory) {
        this.extensionsByClass = extensionsByClass;
        this.contextFactory = contextFactory;
    }

    /**
     * Sets the timeout for resolving futures (objs being loaded by other threads) while loading the config.
     * <p/>
     * This is rather a defence-against-ourselves measure, i.e. normally the config futures should always get resolved/fail with an exception at some point.
     *
     * @param loadingTimeoutSec timeout. If <b>0</b> is passed, timeout is disabled.
     */
    public void setLoadingTimeoutSec(int loadingTimeoutSec) {
        this.loadingTimeoutSec = loadingTimeoutSec;
    }


    @Override
    public ConfigTypeAdapter getReferenceTypeAdapter() {
        return referenceTypeAdapter;
    }


    @Override
    public Object resolveFutureOrFail(String uuid, Future<Object> f) {
        try {

            if (loadingTimeoutSec == 0) {
                return f.get();
            } else {
                return f.get(loadingTimeoutSec, TimeUnit.SECONDS);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConfigurationException("Loading of configuration unexpectedly interrupted", e);

        } catch (ExecutionException e) {
            if (e.getCause() instanceof ConfigurationException) {
                throw (ConfigurationException) e.getCause();
            } else {
                throw new ConfigurationException("Error while loading configuration", e.getCause());
            }

        } catch (TimeoutException e) {
            throw new ConfigurationException("Time-out while waiting for the object [uuid=" + uuid + "] to be loaded", e);
        }
    }


    @Override
    public <T> T newConfiguredInstance(Map<String, Object> configNode, Class<T> clazz) throws ConfigurationException {
        return newConfiguredInstance(configNode, clazz, contextFactory.newLoadingContext());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T newConfiguredInstance(Map<String, Object> configurationNode, Class<T> clazz, LoadingContext ctx) {
        ConfigProperty propertyForClass = ConfigReflection.getDummyPropertyForClass(clazz);
        return (T) lookupTypeAdapter(propertyForClass)
                .fromConfigNode(configurationNode, propertyForClass, ctx, null);
    }

    /**
     * Creates a new instance.
     *
     * @param clazz
     * @param <T>
     * @return
     * @throws ConfigurationException
     */
    @Override
    public <T> T newInstance(Class<T> clazz) throws ConfigurationException {
        try {

            return clazz.newInstance();

        } catch (InstantiationException e) {
            throw new ConfigurationException(e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException(e);
        }
    }


    @Override
    public Map<String, Object> createConfigNodeFromInstance(Object object) throws ConfigurationException {
        if (object == null) return null;
        return createConfigNodeFromInstance(object, object.getClass());
    }


    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> createConfigNodeFromInstance(Object object, Class clazz) throws ConfigurationException {
        ConfigProperty propertyForClass = ConfigReflection.getDummyPropertyForClass(clazz);
        return (Map<String, Object>) lookupTypeAdapter(propertyForClass)
                .toConfigNode(object, propertyForClass, contextFactory.newSavingContext());
    }

    @Override
    public List<Class> getExtensionClassesByBaseClass(Class extensionBaseClass) {

        List<Class> classes = extensionsByClass.get(extensionBaseClass);

        if (classes == null)
            return Collections.emptyList();

        return classes;
    }


    @Override
    @SuppressWarnings("unchecked")
    public ConfigTypeAdapter lookupTypeAdapter(ConfigProperty property) throws ConfigurationException {

        Class clazz = property.getRawClass();

        // check if it is a reference
        if (property.isReference())
            return getReferenceTypeAdapter();

        // check for a custom adapter
        ConfigTypeAdapter typeAdapter = customConfigTypeAdapters.get(clazz);
        if (typeAdapter != null) return typeAdapter;

        // check if it is an extensions map
        if (property.isExtensionsProperty())
            return DefaultConfigTypeAdapters.getExtensionTypeAdapter();

        // delegate to default otherwise
        return lookupDefaultTypeAdapter(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConfigTypeAdapter lookupDefaultTypeAdapter(Class clazz) throws ConfigurationException {

        ConfigTypeAdapter adapter;

        // if it is a config class, use reflective adapter
        if (ConfigReflection.isConfigurableClass(clazz))
            adapter = DefaultConfigTypeAdapters.getReflectiveAdapter();
        else if (clazz.isArray())
            adapter = DefaultConfigTypeAdapters.getArrayTypeAdapter();
        else if (clazz.isEnum())
            adapter = DefaultConfigTypeAdapters.get(Enum.class);
        else
            adapter = DefaultConfigTypeAdapters.get(clazz);

        if (adapter == null)
            throw new ConfigurationException("TypeAdapter not found for class " + clazz.getName());

        return adapter;
    }

    /**
     * Registers a custom type adapter for configurable properties for the specified class
     *
     * @param clazz
     * @param typeAdapter
     */
    @Override
    public void registerCustomConfigTypeAdapter(Class clazz, ConfigTypeAdapter typeAdapter) {
        customConfigTypeAdapters.put(clazz, typeAdapter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getSchemaForConfigurableClass(Class<?> clazz) {
        return lookupDefaultTypeAdapter(clazz).getSchema(new ConfigProperty(clazz), contextFactory.newProcessingContext());
    }
}
