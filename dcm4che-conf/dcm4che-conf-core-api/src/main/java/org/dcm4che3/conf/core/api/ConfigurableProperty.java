/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2014
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4che3.conf.core.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field of a configuration class to be a persistable configuration property of the bean
 *
 * @author Roman K
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigurableProperty {

    enum EnumRepresentation {
        ORDINAL,
        STRING
    }

    /**
     * Name of the node. If not specified, the field/parameter name is used.
     *
     * @return
     */
    String name() default "";

    /**
     * Label to show in configuration UIs. If empty empty string (default), the name will be used.
     *
     * @return
     */
    String label() default "";

    /**
     * Description to show in configuration UIs.
     *
     * @return
     */
    String description() default "";

    /**
     * Just a random string very unlikely to be equal to any user-specified default string (java does not allow to use null...)
     */
    String NO_DEFAULT_VALUE = " $#!@@#$@!#$%  Default-value-does-not-exist-for-this-property  $#!@@#$@!#$%";


    /**
     * Default for primitives (int, string, boolean, enum). If default is not specified, the property is considered required.
     *
     * @return
     */
    String defaultValue() default NO_DEFAULT_VALUE;

    /**
     * Is the property required to be set, i.e. must be non-null for objects, non-empty for Strings
     *
     * @return
     */
    boolean required() default false;

    EnumRepresentation enumRepresentation() default EnumRepresentation.STRING;

    enum ConfigurablePropertyType {
        /**
         * Referenceable UUID
         */
        UUID,

        /**
         * Specifies that the annotated field/property is not stored as a child node, but
         * as a reference to another node instead
         */
        Reference,

        /**
         * Same as {@link org.dcm4che3.conf.core.api.ConfigurableProperty.ConfigurablePropertyType#Reference}
         * but applicable for Collections and Maps
         */
        CollectionOfReferences,

        /**
         * Enables the extension-by-composition mechanism of the framework for containing class and marks the property as an extension map.
         * Only may be applied on fields with type Map&lt;Class<? extends T>, T&gt;.
         * T will be treated as base extension class
         *
         * @return
         */
        ExtensionsProperty,


        /**
         * Marks the class as a root for the hash-based optimistic locking mechanism.
         * This field will be auto-calculated on loading,
         * and will be used to compare against the current state in the backend on persist to prevent conflicting changes.
         * <p/>
         * <p>There must be only single property of this type for a configurable class.</p>
         */
        OptimisticLockingHash,

        /**
         * Basic property - default.
         */
        Basic
    }

    /**
     * Defines special behavior for the property
     */
    ConfigurablePropertyType type() default ConfigurablePropertyType.Basic;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /// GUI ///////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    enum Tag {
        /**
         * Non-required properties could be put into a "advanced..." tab to simplify the view.
         * This tag indicates that the property should never be hidden from the user with such technique.
         */
        PRIMARY;

    }

    /**
     * Ordering of properties for GUI. The larger the number, the lower in the list the property will be displayed.
     *
     * @return
     */
    int order() default 0;

    /**
     * Name of the group in the GUI this property belongs to
     */
    String group() default "Other";

    /**
     * Additional info for the GUI
     *
     * @return
     */
    Tag[] tags() default {};

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /// DEPRECATED ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Deprecated
    /**
     * Use {@link ConfigurableProperty#type()} instead
     */
    boolean isReference() default false;

    @Deprecated
    /**
     * Use {@link ConfigurableProperty#type()} instead
     */
    boolean collectionOfReferences() default false;


    @Deprecated
    /**
     * Use {@link ConfigurableProperty#type()} instead
     */
    boolean isExtensionsProperty() default false;


}
