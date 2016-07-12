package org.dcm4che3.conf.core.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Denotes a field where the parent object will be set. Parent is defined as follows:
 * <ul>
 *     <li>For child property (field), parent is the object</li>
 *     <li>For a list element, parent is the object that contains the list  </li>
 *     <li>For a map value, parent is the object that contains the map  </li>
 *     <li>For an extension, parent is the object that contains the extension map  </li>
 * </ul>
 *
 * If there is no parent found (i.e. the object is used independently), will be set to <b>null</b>.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Parent {
}
