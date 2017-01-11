package org.dcm4che3.conf.api.upgrade;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FixUpTo {

    /**
     * Use major-minor-patch instead!
     */
    @Deprecated
    String value() default "";
    int major() default -1;
    int minor() default -1;
    int patch() default -1;

}
