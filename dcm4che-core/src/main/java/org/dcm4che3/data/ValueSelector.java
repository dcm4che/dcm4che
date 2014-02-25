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
import java.util.ArrayList;

import org.dcm4che3.util.TagUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class ValueSelector implements Serializable {

    private static final long serialVersionUID = 8346808223314626639L;

    private static final ItemPointer[] NO_ITEMPOINTERS = {};
    private static final int MIN_ITEM_POINTER_STR_LEN = 43;

    public final int tag;
    public final String privateCreator;
    public final VR vr;
    public final int valueIndex;
    public final ItemPointer[] itemPointers;

    public ValueSelector(int tag, String privateCreator, VR vr, int index,
            ItemPointer... itemPointers) {
        this.tag = tag;
        this.privateCreator = privateCreator;
        this.vr = vr;
        this.valueIndex = index;
        this.itemPointers = itemPointers.clone();
    }

    public String selectStringValue(Attributes attrs, String defVal) {
        Attributes item = attrs.getNestedDataset(itemPointers);
        return item != null ? item.getString(privateCreator, tag, vr, valueIndex, defVal) : defVal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        for (ItemPointer ip : itemPointers)
            appendTo(ip.sequenceTag, ip.privateCreator, ip.itemIndex,
                    "\"]/Item[@number=\"", "\"]/", sb);
        appendTo(tag, privateCreator, valueIndex,
                "\"]/Value[@number=\"", "\"]", sb);
        return sb.toString();
    }

    private void appendTo(int tag, String privateCreator, int index, String valueOrItem,
            String suffix, StringBuilder sb) {
        sb.append("DicomAttribute[@tag=\"").append(TagUtils.toHexString(tag));
        if (privateCreator != null)
            sb.append("\" and @privateCreator=\"").append(privateCreator);
        if (vr != null)
            sb.append("\" and @vr=\"").append(vr);
        sb.append(valueOrItem).append(index + 1).append(suffix);
    }

    public static ValueSelector valueOf(String s) {
        int fromIndex = s.lastIndexOf("DicomAttribute");
        try {
            return new ValueSelector(
                    selectTag(s, fromIndex),
                    selectPrivateCreator(s, fromIndex),
                    selectVR(s, fromIndex),
                    selectNumber(s, fromIndex) - 1,
                    itemPointersOf(s, fromIndex));
        } catch (Exception e) {
            throw new IllegalArgumentException(s);
        }
    }

    private static int selectTag(String s, int fromIndex) {
        String tagStr = select("@tag=", s, fromIndex);
        return Integer.parseInt(tagStr, 16);
    }

    private static String selectPrivateCreator(String s, int fromIndex) {
        return select("@privateCreator=", s, fromIndex);
    }

    private static int selectNumber(String s, int fromIndex) {
        String no = select("@number=", s, fromIndex);
        return Integer.parseInt(no);
    }

    private static VR selectVR(String s, int fromIndex) {
        String vrStr = select("@vr=", s, fromIndex);
        return vrStr != null ? VR.valueOf(vrStr) : null;
    }

    private static ItemPointer[] itemPointersOf(String s, int endIndex) {
        if (endIndex == 0)
            return NO_ITEMPOINTERS;

        ArrayList<ItemPointer> list = new ArrayList<ItemPointer>();
        int fromIndex = 0;
        while (fromIndex < endIndex) {
            list.add(new ItemPointer(
                    selectTag(s, fromIndex),
                    selectPrivateCreator(s, fromIndex),
                    selectNumber(s, fromIndex) - 1));
            fromIndex = s.indexOf("DicomAttribute",
                    fromIndex + MIN_ITEM_POINTER_STR_LEN);
        }
        return list.toArray(new ItemPointer[list.size()]);
    }

    private static String select(String key, String s, int fromIndex) {
        int pos = s.indexOf(key, fromIndex);
        if (pos < 0)
            return null;
        
        int quotePos = pos + key.length();
        int beginIndex = quotePos + 1;
        return s.substring(beginIndex, s.indexOf(s.charAt(quotePos), beginIndex));
    }

}
