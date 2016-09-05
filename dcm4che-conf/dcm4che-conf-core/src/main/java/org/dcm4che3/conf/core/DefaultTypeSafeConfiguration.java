/*
 * *** BEGIN LICENSE BLOCK *****
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
 *  Portions created by the Initial Developer are Copyright (C) 2015
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

import org.dcm4che3.conf.core.adapters.DefaultConfigTypeAdapters;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.api.TypeSafeConfiguration;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;
import org.dcm4che3.conf.core.api.internal.ConfigReflection;
import org.dcm4che3.conf.core.api.internal.ConfigTypeAdapter;
import org.dcm4che3.conf.core.context.ContextFactory;
import org.dcm4che3.conf.core.context.LoadingContext;
import org.dcm4che3.conf.core.context.Referable;
import org.dcm4che3.conf.core.util.PathPattern;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author Roman K
 */
@SuppressWarnings("unchecked")
public class DefaultTypeSafeConfiguration<R> implements TypeSafeConfiguration<R> {

    private static PathPattern referencePattern = new PathPattern(Configuration.REFERENCE_BY_UUID_PATTERN);
    private final Configuration confStorage;
    private final Class<R> rootClass;
    private final BeanVitalizer vitalizer;
    private ContextFactory contextFactory;
    private Map<Class, ConfigTypeAdapter> customAdapters;

    public DefaultTypeSafeConfiguration(Configuration configurationStorage, Class<R> rootClass, Map<Class, List<Class>> extensionsByClass) {
        this.confStorage = configurationStorage;
        this.rootClass = rootClass;
        contextFactory = new ContextFactory(this);
        this.vitalizer = new DefaultBeanVitalizer(extensionsByClass, contextFactory);

    }

    @Override
    public <T> T load(Path path, Class<T> clazz) {
        return load(path, clazz, contextFactory.newLoadingContext());
    }

    @Override
    public <T> T load(Path path, Class<T> clazz, LoadingContext ctx) {

        Object configurationNode = confStorage.getConfigurationNode(path, clazz);

        if (configurationNode == null) {
            return null;
        }

        return vitalizer.newConfiguredInstance((Map<String, Object>) configurationNode, clazz, ctx);
    }

    public <T> T findByUUID(String uuid, Class<T> clazz) {
        return findByUUID(uuid, clazz, contextFactory.newLoadingContext());
    }

    @Override
    public <T> T findByUUID(String uuid, Class<T> clazz, LoadingContext ctx) {

        // TODO: proper cast checks!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        // either get existing from the context
        Referable referable = ctx.getReferable(uuid);
        if (referable != null) {

            // TODO here
            return (T) referable.getConfObject();
        }

        //// or go ahead and load

        Path pathByUUID = confStorage.getPathByUUID(uuid);

        if (pathByUUID == null) return null;

        Map<String, Object> referencedNode = (Map<String, Object>) confStorage.getConfigurationNode(
                pathByUUID,
                clazz
        );

        // TODO here
        return (T) vitalizer.lookupTypeAdapter(ConfigReflection.getDummyPropertyForClass(clazz))
                .fromConfigNode(referencedNode, ConfigReflection.getDummyPropertyForClass(clazz), ctx, null);

    }

    @Override
    public <T> void save(Path path, T object, Class<T> clazz) {
        Map<String, Object> node = vitalizer.createConfigNodeFromInstance(object, clazz);
        confStorage.persistNode(path, node, clazz);
    }

    @Override
    public Configuration getLowLevelAccess() {
        return confStorage;
    }

    @Override
    public BeanVitalizer getVitalizer() {
        return vitalizer;
    }

    @Override
    public ContextFactory getContextFactory() {
        return contextFactory;
    }

    @Override
    public Class<R> getRootClass() {
        return rootClass;
    }

    public void setContextFactory(ContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }
}
