package org.dcm4che3.net.hl7;

import org.dcm4che3.conf.test.ConfigurablePropertiesTestBase;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ConfigurablePropertiesTest extends ConfigurablePropertiesTestBase {

    @Override
    protected Class[] getClassesToTest() {
        return new Class[]{HL7Application.class, HL7DeviceExtension.class};
    }

    @Override
    protected Set<Field> getPropertiesToSkip() throws NoSuchFieldException {
        Set<Field> set = new HashSet<>();
        set.add(HL7Application.class.getDeclaredField("hl7Installed"));
        return set;
    }
}
