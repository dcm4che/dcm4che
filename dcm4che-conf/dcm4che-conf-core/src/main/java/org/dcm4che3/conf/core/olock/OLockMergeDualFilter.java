/*
 *
 * ** BEGIN LICENSE BLOCK *****
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
 * Portions created by the Initial Developer are Copyright (C) 2015
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
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4che3.conf.core.olock;

import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.OptimisticLockException;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser.ADualNodeFilter;
import org.dcm4che3.conf.core.Nodes;

import java.util.*;

/**
 * Performs hash-based optimistic locking logic that is
 * <ol>
 *     <li>to detect conflicting changes an throw an expection if there are any</li>
 *     <li>if there are none, make sure that all non-conflicting changes from both old and new nodes are preserved.</li>
 * </ol>
 *
 * Merges old node and new node into a single resulting node.
 * After running this filter, newNode becomes the resulting node.
 * <br/>
 * Changes the state of both nodes!
 *
 * @author Roman K
 */
class OLockMergeDualFilter extends ADualNodeFilter {

    final Deque<String> path = new ArrayDeque<String>();

    /**
     * Is currently merging new (non-conflicting) stuff from backend into the new node (being persisted)
     */
    final Deque<Boolean> isMerging = new ArrayDeque<Boolean>();


    public OLockMergeDualFilter() {
        isMerging.push(false);
    }

    @Override
    public void beforeNode(Map<String, Object> oldNode, Map<String, Object> newNode) {

        // if this node is not olock-enabled, don't apply olock logic, just push current mode to stack once again
        if (!newNode.containsKey(Configuration.OLOCK_HASH_KEY)) {
            isMerging.push(isMerging.peek());
            return;
        }

        if (!isMerging.peek()) {

            // We are NOT merging now

            if (newNode.get("#old_hash").equals(newNode.get(Configuration.OLOCK_HASH_KEY))) {
                // If we met a olocked node where new node did not change, then we swap and turn on merging (from old to new)
                isMerging.push(true);
                swap(oldNode, newNode);
            } else {
                if (newNode.get("#old_hash").equals(oldNode.get(Configuration.OLOCK_HASH_KEY))) {
                    // If we met a olocked node where new node changed, then check the hash in old node and if it's not changed - keep going
                    isMerging.push(false);
                } else {
                    // If we met a olocked node where new node changed, and the hash in old node has also changed - that's an exception
                    throw new OptimisticLockException(Nodes.toSimpleEscapedPath(path.descendingIterator()));
                }
            }
        } else {

            // We ARE merging now
            // have to be careful since oldNode and newNode are swapped!
            Map<String, Object> actualNewNode = oldNode;
            Map<String, Object> actualOldNode = newNode;


            if (actualNewNode.get("#old_hash").equals(actualNewNode.get(Configuration.OLOCK_HASH_KEY))) {
                // If we met a olocked node where new node did not change, then keep on merging (from old to new)
                isMerging.push(true);
            } else {
                if (actualNewNode.get("#old_hash").equals(actualOldNode.get(Configuration.OLOCK_HASH_KEY))) {
                    // If we met a olocked node where new node changed, then check the hash in old node and if it's not changed, swap and switch to non-merging mode
                    isMerging.push(false);
                    swap(actualOldNode, actualNewNode);
                } else {
                    // If we met a olocked node where new node changed, and the hash in old node has also changed - that's an exception
                    throw new OptimisticLockException(Nodes.toSimpleEscapedPath(path.descendingIterator()));
                }
            }
        }
    }

    /**
     * Swaps CONTENTS
     *
     * @param oldNode
     * @param newNode
     */
    private void swap(Map<String, Object> oldNode, Map<String, Object> newNode) {
        Map<String, Object> tmpNode = new LinkedHashMap<String, Object>();
        tmpNode.putAll(oldNode);

        oldNode.clear();
        oldNode.putAll(newNode);

        newNode.clear();
        newNode.putAll(tmpNode);
    }




    @Override
    public void afterNode(Map<String, Object> node1, Map<String, Object> node2) {
        isMerging.pop();
    }

    @Override
    public void beforeNodeProperty(String key) {
        path.push(key);
    }

    @Override
    public void afterNodeProperty(String key) {
        path.pop();
    }

    @Override
    public void beforeListElement(int index1, int index2) {
        if (isMerging.peek())
            path.push(Integer.toString(index1));
        else
            path.push(Integer.toString(index2));
    }

    @Override
    public void afterListElement(int index1, int index2) {
        path.pop();
    }
}
