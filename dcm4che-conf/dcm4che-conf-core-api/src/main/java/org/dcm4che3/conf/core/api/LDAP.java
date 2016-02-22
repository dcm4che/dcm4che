package org.dcm4che3.conf.core.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by aprvf on 13/10/2014.
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.FIELD})
public @interface LDAP {

    public static final String DEFAULT_DISTINGUISHING_FIELD = "cn";

    String distinguishingField() default DEFAULT_DISTINGUISHING_FIELD;

    /**
     * Can be used on classes and on fields
     * @return
     */
    boolean noContainerNode() default false;


    String[] objectClasses() default {};



    // needed if map entry value is a primitive
    String mapValueAttribute() default "";

    // needed if map entry value is a primitive
    String mapEntryObjectClass() default "";

    /**
     * Applicable to EnumSet&lt;some enum&gt;. Triggers an alternative representation of EnumSets, instead of
     *  enumField : [0,2]
     * recognizes
     * enumfield:{
     *  OPT1 : TRUE,
     *  OPT2 : FALSE,
     *  OPT3 : TRUE
     * }
     *
     * The array must contain property names that correspond to enum values in the right order and the enum representation MUST be ordinal for this property.
     */
    String[] booleanBasedEnumStorageOptions() default {};

    /**
     * Alternative property name that overrides the ConfigurableProperty's name when this property is stored in LDAP.
     * @return
     */
    String overriddenName() default "";

}
