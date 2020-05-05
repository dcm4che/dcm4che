package org.dcm4che3.imageio.codec;

import org.dcm4che3.conf.test.ConfigurablePropertiesTestBase;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ConfigurablePropertiesTest extends ConfigurablePropertiesTestBase {

    @Override
    protected Class[] getClassesToTest() {
        return new Class[]{CompressionRule.class, CompressionRule.Condition.class, CompressionRules.class,
                ImageReaderFactory.class, ImageReaderFactory.ImageReaderParam.class, ImageWriterFactory.class,
                ImageWriterFactory.ImageWriterParam.class};
    }

    @Override
    protected Set<Field> getPropertiesToSkip() {
        return new HashSet<>();
    }
}
