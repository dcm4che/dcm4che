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
import org.dcm4che3.conf.core.util.ConfigNodeTraverser;
import org.dcm4che3.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;

/**
 * Calculates and sets olock hashes for nodes that have the _.hash property (ignores the previous value)
 *
 * @author Roman K
 */
public class OLockHashCalcFilter extends ConfigNodeTraverser.AConfigNodeFilter {

    Logger log = LoggerFactory.getLogger(HashBasedOptimisticLockingConfiguration.class);

    final Deque<byte[]> stack = new ArrayDeque<byte[]>();
    private String ignoredKey;
    private MessageDigest cript;

    public OLockHashCalcFilter() {
        stack.push(new byte[20]);

    }

    public OLockHashCalcFilter(String ignoredKey) {
        this();
        this.ignoredKey = ignoredKey;
    }

    /**
     * Associative op
     *
     * @param one
     * @param two
     */
    public void addHash(byte[] one, byte[] two) {

        for (int i = 0; i < one.length; i++) {
            one[i] = (byte) (one[i] + two[i]);
        }
    }

    public byte[] getHash(String what) {
        getCript().reset();
        getCript().update(what.getBytes());
        return getCript().digest();
    }

    public MessageDigest getCript() {

        if (cript == null)
            try {
                cript = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

        return cript;
    }

    public Deque<byte[]> getStack() {
        return stack;
    }


    @Override
    public void onPrimitiveNodeElement(Map<String, Object> containerNode, String key, Object value) {

        // add up to hash
        addHash(stack.peek(), getHash(String.valueOf(value)));

    }

    private String hashToString(byte[] hash) {
        return Base64.toBase64(hash);
    }

    private boolean doIgnore(String key) {
        return Configuration.OLOCK_HASH_KEY.equals(key) || ignoredKey != null && ignoredKey.equals(key);
    }


    @Override
    public void beforeNode(Map<String, Object> node) {
        stack.push(new byte[20]);
    }

    @Override
    public void afterNode(Map<String, Object> node) {

        byte[] pop = stack.pop();

        // if this node is olock'd, save hash, otherwise addup to what will be consumed by the parent
        if (node.containsKey(Configuration.OLOCK_HASH_KEY))
            node.put(Configuration.OLOCK_HASH_KEY, hashToString(pop));
        else
            addHash(stack.peek(), getHash(hashToString(pop)));
    }

    @Override
    public void beforeNodeElement(Map<String, Object> containerNode, String key, Object value) {
        stack.push(new byte[20]);
    }

    @Override
    public void afterNodeElement(Map<String, Object> containerNode, String key, Object value) {
        byte[] valueHash = stack.pop();

        // don't add for olock hashes and ignored keys
        if (!doIgnore(key)) {
//            String peekStr = hashToString(stack.peek());
            addHash(stack.peek(), getHash(key + hashToString(valueHash)));
//            log.info("Added hash of property '{}' ({} -> {})", key, peekStr, hashToString(stack.peek()));
        }


    }

    @Override
    public void beforeListElement(Collection list, int index, Object element) {
        stack.push(new byte[20]);
    }

    @Override
    public void afterListElement(Collection list, int index, Object element) {
        byte[] listElementHash = stack.pop();
        byte[] listHash = stack.pop();
        stack.push(getHash(hashToString(listHash) + hashToString(listElementHash)));
    }

    @Override
    public void onPrimitiveListElement(Collection list, Object element) {
        addHash(stack.peek(), getHash(String.valueOf(element)));
    }
}
