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
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
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

package org.dcm4che3.data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class ValueSelector implements Serializable {

    private static final long serialVersionUID = 7043276856762009199L;

    private final AttributeSelector attributeSelector;
    private final int valueIndex;
    private String str;

    public ValueSelector(int tag, String privateCreator, int index, ItemPointer... itemPointers) {
        this(new AttributeSelector(tag, privateCreator, itemPointers), index);
    }

    public ValueSelector(AttributeSelector attributeSelector, int index) {
        this.attributeSelector = Objects.requireNonNull(attributeSelector);
        this.valueIndex = index;
    }

    public int tag() {
        return attributeSelector.tag();
    }

    public String privateCreator() {
        return attributeSelector.privateCreator();
    }

    public int level() {
        return attributeSelector.level();
    }

    public ItemPointer itemPointer(int index) {
        return attributeSelector.itemPointer(index);
    }

    public int valueIndex() {
        return valueIndex;
    }

    public String selectStringValue(Attributes attrs, String defVal) {
        return attributeSelector.selectStringValue(attrs, valueIndex, defVal);
    }

    @Override
    public String toString() {
        if (str == null)
            str = attributeSelector.toStringBuilder()
                    .append("/Value[@number=\"")
                    .append(valueIndex + 1)
                    .append("\"]")
                    .toString();
        return str;
    }

    public static ValueSelector valueOf(String s) {
        int fromIndex = s.lastIndexOf("DicomAttribute");
        try {
            return new ValueSelector(AttributeSelector.valueOf(s),
                    AttributeSelector.selectNumber(s, fromIndex) - 1);
        } catch (Exception e) {
            throw new IllegalArgumentException(s);
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ValueSelector))
            return false;

        return toString().equals(obj.toString());
    }
}
