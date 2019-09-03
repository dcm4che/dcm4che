/*
 * **** BEGIN LICENSE BLOCK *****
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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015-2018
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

package org.dcm4che3.data;

import org.dcm4che3.util.TagUtils;

import java.io.Serializable;
import java.util.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Dec 2018
 */
public class AttributeSelector implements Serializable {

    private static final long serialVersionUID = 9122372717582373730L;

    private static final int MIN_ITEM_POINTER_STR_LEN = 30;

    private final int tag;
    private final String privateCreator;
    private final List<ItemPointer> itemPointers;
    private String str;

    public AttributeSelector(int tag) {
        this(tag, null, Collections.EMPTY_LIST);
    }

    public AttributeSelector(int tag, String privateCreator) {
        this(tag, privateCreator, Collections.EMPTY_LIST);
    }

    public AttributeSelector(int tag, String privateCreator, ItemPointer... itemPointers) {
        this(tag, privateCreator, Arrays.asList(itemPointers));
    }

    public AttributeSelector(int tag, String privateCreator, List<ItemPointer> itemPointers) {
        this.tag = tag;
        this.privateCreator = privateCreator;
        this.itemPointers = itemPointers;
    }

    public int tag() {
        return tag;
    }

    public String privateCreator() {
        return privateCreator;
    }

    public int level() {
        return itemPointers.size();
    }

    public ItemPointer itemPointer(int index) {
        return itemPointers.get(index);
    }

    public String selectStringValue(Attributes attrs, int valueIndex, String defVal) {
        Attributes item = attrs.getNestedDataset(itemPointers);
        return item != null ? item.getString(privateCreator, tag, valueIndex, defVal) : defVal;
    }

    @Override
    public String toString() {
        if (str == null)
            str = toStringBuilder().toString();
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeSelector that = (AttributeSelector) o;
        return tag == that.tag &&
                Objects.equals(privateCreator, that.privateCreator) &&
                itemPointers.equals(that.itemPointers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, privateCreator, itemPointers);
    }

    StringBuilder toStringBuilder() {
        StringBuilder sb = new StringBuilder(32);
        for (ItemPointer ip : itemPointers) {
            appendTo(ip.sequenceTag, ip.privateCreator, "\"]/Item", sb);
            if (ip.itemIndex >= 0)
                sb.append("[@number=\"").append(ip.itemIndex + 1).append("\"]");
            sb.append('/');
        }
        return appendTo(tag, privateCreator, "\"]", sb);
    }

    private StringBuilder appendTo(int tag, String privateCreator, String suffix, StringBuilder sb) {
        sb.append("DicomAttribute[@tag=\"").append(TagUtils.toHexString(tag));
        if (privateCreator != null)
            sb.append("\" and @privateCreator=\"").append(privateCreator);
        return sb.append(suffix);
    }

    public static AttributeSelector valueOf(String s) {
        int fromIndex = s.lastIndexOf("DicomAttribute");
        try {
            return new AttributeSelector(
                    selectTag(s, fromIndex),
                    selectPrivateCreator(s, fromIndex),
                    itemPointersOf(s, fromIndex));
        } catch (Exception e) {
            throw new IllegalArgumentException(s);
        }
    }

    static int selectTag(String s, int fromIndex) {
        String tagStr = select("@tag=", s, fromIndex);
        return Integer.parseInt(tagStr, 16);
    }

    static String selectPrivateCreator(String s, int fromIndex) {
        return select("@privateCreator=", s, fromIndex);
    }

    static int selectNumber(String s, int fromIndex) {
        String no = select("@number=", s, fromIndex);
        return no != null ? Integer.parseInt(no) : 0;
    }

    private static List<ItemPointer> itemPointersOf(String s, int endIndex) {
        if (endIndex == 0)
            return Collections.emptyList();

        ArrayList<ItemPointer> list = new ArrayList<>();
        int fromIndex = 0;
        while (fromIndex < endIndex) {
            list.add(new ItemPointer(
                    selectTag(s, fromIndex),
                    selectPrivateCreator(s, fromIndex),
                    selectNumber(s, fromIndex) - 1));
            fromIndex = s.indexOf("DicomAttribute",
                    fromIndex + MIN_ITEM_POINTER_STR_LEN);
        }
        list.trimToSize();
        return list;
    }

    private static String select(String key, String s, int fromIndex) {
        int pos = s.indexOf(key, fromIndex);
        if (pos < 0)
            return null;

        int quotePos = pos + key.length();
        int beginIndex = quotePos + 1;
        return s.substring(beginIndex, s.indexOf(s.charAt(quotePos), beginIndex));
    }

    public boolean matches(List<ItemPointer> itemPointers, String privateCreator, int tag) {
        int level;
        if (tag != this.tag || !Objects.equals(privateCreator, this.privateCreator)
                || (itemPointers.size() != (level = level()))) {
            return false;
        }
        for (int i = 0; i < level; i++) {
            ItemPointer itemPointer = itemPointers.get(i);
            ItemPointer other = itemPointer(i);
            if (!(itemPointer.itemIndex < 0 || other.itemIndex < 0
                    ? itemPointer.equalsIgnoreItemIndex(other)
                    : itemPointer.equals(other))) {
                return false;
            }
        }
        return true;
    }
}
