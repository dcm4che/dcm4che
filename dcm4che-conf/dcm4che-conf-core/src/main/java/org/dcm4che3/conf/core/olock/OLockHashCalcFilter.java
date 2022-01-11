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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser;
import org.dcm4che3.util.Base64;

/**
 * Calculates and sets olock hashes for nodes that have the _.hash property (ignores the previous value)
 *
 * @author Roman K
 * @author Maciek Siemczyk (maciek.siemczyk@agfa.com)
 */
public class OLockHashCalcFilter extends ConfigNodeTraverser.AConfigNodeFilter {
    
    private final Deque<byte[]> hashCalculationNodeStack = new ArrayDeque<>();
    private final List<String> ignoredKeys = new ArrayList<>();
    
    private MessageDigest cript;

    /**
     * Default constructor for no additional ignored keys.
     */
    public OLockHashCalcFilter() {
        
        this(new String[0]);
    }
 
    /**
     * Parameterized constructor for specifying additional keys to ignore (in hash calculation).
     * 
     * @param ignoredKeys One or more config keys to ignore.
     */
    public OLockHashCalcFilter(String... ignoredKeys) {
        
        hashCalculationNodeStack.push(new byte[20]);
        
        if (ignoredKeys != null) {
            this.ignoredKeys.addAll(Arrays.asList(ignoredKeys));    
        }
    }

    public List<String> getIgnoredKeys() {

        return ignoredKeys;
    }

    /**
     * Associative op
     *
     * @param one
     * @param two
     */
    private void addHash(byte[] one, byte[] two) {

        for (int i = 0; i < one.length; i++) {
            one[i] = (byte) (one[i] + two[i]);
        }
    }

    private byte[] getHash(String what) {
        getCript().reset();
        getCript().update(what.getBytes());
        return getCript().digest();
    }

    private MessageDigest getCript() {

        if (cript == null)
            try {
                cript = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

        return cript;
    }

    @Override
    public void onPrimitiveNodeElement(Map<String, Object> containerNode, String key, Object value) {

        // add up to hash
        addHash( hashCalculationNodeStack.peek(), getHash(String.valueOf(value)));

    }

    private String hashToString(byte[] hash) {
        return Base64.toBase64(hash);
    }

    private boolean doIgnore(String key) {
        return Configuration.OLOCK_HASH_KEY.equals(key) || ignoredKeys.contains(key);
    }

    @Override
    public void beforeNode(Map<String, Object> node) {
        hashCalculationNodeStack.push(new byte[20]);
    }

    @Override
    public void afterNode(Map<String, Object> node) {

        byte[] pop = hashCalculationNodeStack.pop();

        // if this node is olock'd, save hash, otherwise addup to what will be consumed by the parent
        if (node.containsKey(Configuration.OLOCK_HASH_KEY))
            node.put(Configuration.OLOCK_HASH_KEY, hashToString(pop));
        else
            addHash( hashCalculationNodeStack.peek(), getHash(hashToString(pop)));
    }

    @Override
    public void beforeNodeElement(Map<String, Object> containerNode, String key, Object value) {
        hashCalculationNodeStack.push(new byte[20]);
    }

    @Override
    public void afterNodeElement(Map<String, Object> containerNode, String key, Object value) {
        byte[] valueHash = hashCalculationNodeStack.pop();

        // don't add for olock hashes and ignored keys
        if (!doIgnore(key)) {
            addHash( hashCalculationNodeStack.peek(), getHash(key + hashToString(valueHash)));
        }
    }

    @Override
    public void beforeListElement(
            @SuppressWarnings("rawtypes") Collection list,
            int index,
            Object element) {
        
        hashCalculationNodeStack.push(new byte[20]);
    }

    @Override
    public void afterListElement(
            @SuppressWarnings("rawtypes") Collection list,
            int index,
            Object element) {
        
        byte[] listElementHash = hashCalculationNodeStack.pop();
        byte[] listHash = hashCalculationNodeStack.pop();
        
        hashCalculationNodeStack.push(getHash(hashToString(listHash) + hashToString(listElementHash)));
    }

    @Override
    public void onPrimitiveListElement(
            @SuppressWarnings("rawtypes") Collection list,
            Object element) {
        
        addHash( hashCalculationNodeStack.peek(), getHash(String.valueOf(element)));
    }
}
