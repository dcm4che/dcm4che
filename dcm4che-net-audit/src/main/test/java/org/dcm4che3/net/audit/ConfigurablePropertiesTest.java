package org.dcm4che3.net.audit;

import org.dcm4che3.conf.test.ConfigurablePropertiesTestBase;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ConfigurablePropertiesTest extends ConfigurablePropertiesTestBase {

    @Override
    protected Class[] getClassesToTest() {
        return new Class[]{AuditLogger.class, AuditRecordRepository.class, AuditSuppressCriteria.class};
    }

    @Override
    protected Set<Field> getPropertiesToSkip() {
        return new HashSet<>();
    }
}
