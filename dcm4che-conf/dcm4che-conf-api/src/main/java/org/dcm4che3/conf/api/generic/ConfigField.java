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
package org.dcm4che3.conf.api.generic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field of a configuration class as a configuration field, meaning that it will be persisted. If a field of a
 * config class is not annotated, it will be skipped and NOT persisted.
 * 
 * @author Roman K
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigField {

    /**
     * Name in configuration
     * 
     * @return
     */
    String name();

    /**
     * Warning - not the common "default". The logic is that  
     * if the attribute at hand will have the value specified by this parameter, it will not be persisted.
     * Additionally, if applied to a Object-typed field, will populate it with null if there is no child and not throw exception.
     * 
     * @return
     */
    String def() default "N/A";

    /**
     * What is used as a map key, e.g. which names ldap nodes have like if a key is 'name', then nodes are
     * name=registry, name=repository
     * 
     * @return
     */
    String mapKey() default "cn";

    /**
     * What to use for LDAP collection elements as object class if the value class is not a composite ConfigClass'ed
     * type
     * 
     * @return
     */
    String mapElementObjectClass() default "";

    /**
     * Maps support non-ConfigClass'ed value types as well. As an attribute name in child nodes, name() will be used. If
     * you need a different name for the collection node itself in this case, use this parameter.
     */
    String mapName() default "N/A";
    
    /**
     * Label to show in configuration UIs. If empty empty string (default), the name will be used.
     * @return
     */
    String label() default "";
    
    /**
     * Description to show in configuration UIs.
     * @return
     */
    String description() default "";
    
    /**
     * Indicates that the configuration parameter is optional. By default, all parameters are required.
     * For the time being, only used in configuration UIs, the backend validation will follow.
     * @return
     */
    boolean optional() default false;

    /**
     * For Maps. Which delimeter is used to separate key from value.
     * 
     * @return
     */
    @Deprecated
    String delimeter() default "|";

    /**
     * For Maps. Which key to use if only value is provided.
     * 
     * @return
     */
    @Deprecated
    String defaultKey() default "*";

}
