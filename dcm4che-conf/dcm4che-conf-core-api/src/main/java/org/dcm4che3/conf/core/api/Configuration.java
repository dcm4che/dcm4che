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
package org.dcm4che3.conf.core.api;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Denotes a configuration source. Can be used by BeanVitalizer that creates POJOs, or configuration administration app that provides UI to edit configuration.
 * <br/> <br/>
 * The Configuration API operates on a tree data structure, where any subtree is referred to as a configuration node.
 * A configuration node represents a JSON object. A configuration node is either
 * <ul>
 * <li> a primitive wrapper/string (Number, Boolean, String)</li>
 * <li> null</li>
 * <li> a collection of nodes </li>
 * <li> Map&lt;String,Object&gt; where each object is a configuration node (single map can have values of multiple types.</li>
 * </ul>
 * Formally, if such a node object is serialized into JSON and back without any transformations applied, the resulting object should be deep-equal to the original one.
 * <p/>
 * A <i>path</i> is a valid XPath expression evaluated against the configuration tree. The usage of very advanced XPath expressions is not recommended, since it could lead to eager loading of configuration tree.
 * Examples of paths can be found in org.dcm4che3.conf.dicom.DicomPath. A helper class org.dcm4che3.conf.core.util.PathPattern can be used to safely compose parametrized paths.
 */
public interface Configuration extends BatchRunner {

    String CONF_STORAGE_SYSTEM_PROP = "org.dcm4che.conf.storage";

    /**
     * A special property key that indicates that this property is the referable uuid of the containing config node
     */
    String UUID_KEY = "_.uuid";

    /**
     * A special property key that indicates that
     * the containing node is a hash-based optimistic locking root and
     * that this property contains the hash of this node.
     */
    String OLOCK_HASH_KEY = "_.hash";

    /**
     * A special property key that indicates that the container of this property is a reference
     * to a node with uuid that equals to the property's value.
     * Additionally if WEAK_REFERENCE_KEY:true is defined in the container node,
     * this reference is considered to be a weak reference ({@link ConfigurableProperty#weakReference()})
     */
    String REFERENCE_KEY = "_.ref";
    String WEAK_REFERENCE_KEY = "weakReference";
    String REFERENCE_BY_UUID_PATTERN = "//*[_.uuid='{uuid}']";

    enum ConfigStorageType {
        JSON_FILE,
        DB_BLOBS;
    }

    /**
     * Return the root of the configuration tree.
     * The returned node should not be modified directly (only through persistNode/removeNode).
     *
     * @return configuration tree
     * @throws ConfigurationException
     */
    Map<String, Object> getConfigurationRoot() throws ConfigurationException;

    /**
     * Loads a configuration node under the specified path.
     *
     * @param path              A reference to a node
     * @param configurableClass
     * @return configuration node or null, if not found
     * @throws ConfigurationException
     */
    Object getConfigurationNode(String path, Class configurableClass) throws ConfigurationException;

    /**
     * Tests if a node under the specified path exists.
     *
     * @param path
     * @return
     * @throws ConfigurationException
     */
    boolean nodeExists(String path) throws ConfigurationException;

    /**
     * Persists the configuration node to the specified path.
     * The path must exist (or at least all nodes but the last one).
     * The property is created/fully overwritten, i.e. if there were any child nodes in the old root that are not present in the new node root, they will be deleted in the new tree.
     *
     * @param path              path to the node
     * @param configNode        configuration node to persist
     * @param configurableClass class annotated with ConfigurableClass, ConfigurableProperty annotations that corresponds to this node.
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
     * Removes a configuration node under the specified path with all its children permanently
     *
     * @param path
     */
    void removeNode(String path) throws ConfigurationException;

    /**
     * Performs a search on the configuration tree and returns an iterator to configuration nodes that satisfy the search criteria.
     *
     * @param liteXPathExpression Must be absolute path, no double slashes, no @attributes (only [attr=val] or [attr<>val])
     */
    Iterator search(String liteXPathExpression) throws IllegalArgumentException, ConfigurationException;

    /**
     * Aquire a global pessimistic lock (cluster-aware implementations should ensure that only a single node can aquire the lock at a time)
     * Subsequent calls from the same transaction should not block.
     * Should be auto-released on transaction commit/rollback.
     */
    void lock();


    class NodeFactory {
        public static Map<String,Object> emptyNode() {
            return new TreeMap<String, Object>();
        }
    }
}
