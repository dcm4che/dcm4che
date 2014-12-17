/*
 * **** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2014
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */
package org.dcm4che3.conf.core;

import org.dcm4che3.conf.api.ConfigurationException;

import java.util.Iterator;
import java.util.Map;

/**
 * <strong>This API is UNSTABLE (can and will be likely changed in a non-compatible way in near future), please do not use it without prior consulting with Roman K</strong><br/>
 * Denotes a configuration source. Can be used by BeanVitalizer that creates POJOs, or configuration administration app that provides UI to edit configuration.
 * <br/> <br/>
 * <ul>
 * Configuration node is either
 * <li> a primitive wrapper/string (Integer, Boolean, Float, String)</li>
 * <li> null</li>
 * <li> a collection of nodes </li>
 * <li> Map&lt;String,Object&gt; where each object is a configuration node (single map can have values of multiple types.</li>
 * </ul>
 * <p/>
 */
public interface Configuration {

    /**
     * The returned node must not be modified directly (only through persistNode/removeNode).
     * Thread-safe.
     *
     * @return configuration tree
     * @throws ConfigurationException
     */
    Map<String, Object> getConfigurationRoot() throws ConfigurationException;

    /**
     *
     * @param path A reference to a node
     * @param configurableClass
     * @return configuration node or null, if not found
     * @throws ConfigurationException
     */
    Object getConfigurationNode(String path, Class configurableClass) throws ConfigurationException;

    /**
     * Returns the class that was used to persist the node using persistNode
     *
     * @param path
     * @return
     * @throws ConfigurationException
     */
    Class getConfigurationNodeClass(String path) throws ConfigurationException, ClassNotFoundException;

    boolean nodeExists(String path) throws ConfigurationException;

    /**
     * Persists the configuration node to the specified path.
     * The path must exist (or at least all nodes but the last one).
     * The property is created/fully overwritten, i.e. if there were any child nodes in the old root that are not present in the new node root, they will be deleted in the new tree.
     * <br/>
     * <p><h2>Defaults:</h2>
     * The property values that are equal to default values are be filtered, i.e. not persisted.
     * <p/>
     * </p>
     *
     * @param path              path to the node
     * @param configNode        configuration node to persist
     * @param configurableClass class annotated with ConfigurableClass, ConfigurableProperty annotations that corresponds to this node.
     *                          This parameter is required e.g., by LDAP backend to provide additional metadata like ObjectClasses and LDAP node hierarchy relations.
     */
    void persistNode(String path, Map<String, Object> configNode, Class configurableClass) throws ConfigurationException;

    /**
     * Invalidates any present cached state for the node
     * It is imperative that some other nodes might also be refreshed during the operation.
     * There is no guarantee whether the new version will be re-loaded lazily or eagerly
     *
     * @param path
     */
    void refreshNode(String path) throws ConfigurationException;

    /**
     * Removes a configuration node with all its children permanently
     *
     * @param path
     */
    void removeNode(String path) throws ConfigurationException;

    /**
     * Returns configNodes
     *
     * @param liteXPathExpression Must be absolute path, no double slashes, no @attributes (only [attr=val] or [attr<>val])
     */
    Iterator search(String liteXPathExpression) throws IllegalArgumentException, ConfigurationException;
}
