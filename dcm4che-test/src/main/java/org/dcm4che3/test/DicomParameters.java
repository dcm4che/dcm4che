package org.dcm4che3.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DicomParameters {

    /**
     * Application Entity title of the destination
     *
     * @return
     */
    String targetAeTitle() default "";

    String sourceConnectionCn() default "";

    String sourceDeviceName() default "";

    String targetDeviceName() default "";

    String targetConnectionCn() default "";

    String[] files() default {};
}
