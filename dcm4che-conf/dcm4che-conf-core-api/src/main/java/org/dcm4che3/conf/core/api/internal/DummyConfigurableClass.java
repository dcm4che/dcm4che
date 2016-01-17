package org.dcm4che3.conf.core.api.internal;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;

/**
 * @author Roman K <roman.khazankin@gmail.com>
 */
@LDAP
@ConfigurableClass
public class DummyConfigurableClass {
    @ConfigurableProperty
    public int dummy;
}
