package org.dcm4che3.conf.api;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.api.internal.ConfigTypeAdapter;
import org.dcm4che3.conf.core.context.LoadingContext;
import org.dcm4che3.conf.core.context.ProcessingContext;
import org.dcm4che3.conf.core.context.SavingContext;

import java.util.Map;

public class DefaultDelegatingConfigTypeAdapter<T, ST> implements ConfigTypeAdapter<T, ST>{

    @SuppressWarnings("unchecked")
    protected ConfigTypeAdapter<T, ST> getDefaultTypeAdapter(ProcessingContext ctx, ConfigProperty property) {
        return ctx.getVitalizer().lookupDefaultTypeAdapter(property.getRawClass());
    }

    @Override
    public T fromConfigNode(ST configNode, ConfigProperty property, LoadingContext ctx, Object parent) throws ConfigurationException {
        return getDefaultTypeAdapter(ctx, property).fromConfigNode(configNode, property, ctx, parent);
    }

    @Override
    public ST toConfigNode(T object, ConfigProperty property, SavingContext ctx) throws ConfigurationException {
        return getDefaultTypeAdapter(ctx, property).toConfigNode(object, property, ctx);
    }

    @Override
    public Map<String, Object> getSchema(ConfigProperty property, ProcessingContext ctx) throws ConfigurationException {
        return getDefaultTypeAdapter(ctx, property).getSchema(property, ctx);
    }

    @Override
    public ST normalize(Object configNode, ConfigProperty property, ProcessingContext ctx) throws ConfigurationException {
        return getDefaultTypeAdapter(ctx, property).normalize(configNode, property, ctx);
    }
}
