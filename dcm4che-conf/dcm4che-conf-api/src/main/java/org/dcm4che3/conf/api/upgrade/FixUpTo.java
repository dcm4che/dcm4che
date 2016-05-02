package org.dcm4che3.conf.api.upgrade;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FixUpTo {
    String value();
}
