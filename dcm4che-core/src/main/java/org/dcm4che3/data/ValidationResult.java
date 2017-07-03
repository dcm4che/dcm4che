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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

import java.util.ArrayList;
import java.util.List;

import org.dcm4che3.data.IOD.DataElement;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class ValidationResult {

    public enum Invalid {
        VR,
        VM,
        Value,
        Item,
        MultipleItems,
        Code
    }

    public class InvalidAttributeValue {
        public final IOD.DataElement dataElement;
        public final Invalid reason;
        public final ValidationResult[] itemValidationResults;
        public final IOD[] missingItems;
        public InvalidAttributeValue(DataElement dataElement, Invalid reason,
                ValidationResult[] itemValidationResults, IOD[] missingItems) {
            this.dataElement = dataElement;
            this.reason = reason;
            this.itemValidationResults = itemValidationResults;
            this.missingItems = missingItems;
        }
    }

    private ArrayList<IOD.DataElement> missingAttributes;
    private ArrayList<IOD.DataElement> missingAttributeValues;
    private ArrayList<IOD.DataElement> notAllowedAttributes;
    private ArrayList<InvalidAttributeValue> invalidAttributeValues;

    public boolean hasMissingAttributes() {
        return missingAttributes != null;
    }

    public boolean hasMissingAttributeValues() {
        return missingAttributeValues != null;
    }

    public boolean hasInvalidAttributeValues() {
        return invalidAttributeValues != null;
    }

    public boolean hasNotAllowedAttributes() {
        return notAllowedAttributes != null;
    }

    public boolean isValid() {
        return !hasMissingAttributes()
            && !hasMissingAttributeValues()
            && !hasInvalidAttributeValues()
            && !hasNotAllowedAttributes();
    }

    public void addMissingAttribute(IOD.DataElement dataElement) {
        if (missingAttributes == null)
            missingAttributes = new ArrayList<IOD.DataElement>();
        missingAttributes.add(dataElement);
    }

    public void addMissingAttributeValue(IOD.DataElement dataElement) {
        if (missingAttributeValues == null)
            missingAttributeValues = new ArrayList<IOD.DataElement>();
        missingAttributeValues.add(dataElement);
    }

    public void addInvalidAttributeValue(IOD.DataElement dataElement, Invalid reason) {
        addInvalidAttributeValue(dataElement, reason, null, null);
    }

    public void addInvalidAttributeValue(IOD.DataElement dataElement,
            Invalid reason, ValidationResult[] itemValidationResult, IOD[] missingItems) {
        if (invalidAttributeValues == null)
            invalidAttributeValues = new ArrayList<InvalidAttributeValue>();
        invalidAttributeValues.add(
                new InvalidAttributeValue(dataElement, reason, 
                        itemValidationResult, missingItems));
    }

    public void addNotAllowedAttribute(DataElement el) {
        if (notAllowedAttributes == null)
            notAllowedAttributes = new ArrayList<IOD.DataElement>();
        notAllowedAttributes.add(el);
    }

    public int[] tagsOfNotAllowedAttributes() {
        return tagsOf(notAllowedAttributes);
    }

    public int[] tagsOfMissingAttributeValues() {
        return tagsOf(missingAttributeValues);
    }

    public int[] tagsOfMissingAttributes() {
        return tagsOf(missingAttributes);
    }

    public int[] tagsOfInvalidAttributeValues() {
        ArrayList<InvalidAttributeValue> list = invalidAttributeValues;
        if (list == null)
            return ByteUtils.EMPTY_INTS;

        int[] tags = new int[list.size()];
        for (int i = 0; i < tags.length; i++)
            tags[i] = list.get(i).dataElement.tag;
        return tags;
    }

    public int[] getOffendingElements() {
        return cat(tagsOfMissingAttributes(),
                tagsOfMissingAttributeValues(),
                tagsOfInvalidAttributeValues(),
                tagsOfNotAllowedAttributes());
    }

    private int[] cat(int[]... iss) {
        int length = 0;
        for (int[] is : iss)
            length += is.length;
        int[] tags = new int[length];
        int off = 0;
        for (int[] is : iss) {
            System.arraycopy(is, 0, tags, off, is.length);
            off += is.length;
        }
        return tags;
    }

    private int[] tagsOf(List<DataElement> list) {
        if (list == null)
            return ByteUtils.EMPTY_INTS;

        int[] tags = new int[list.size()];
        for (int i = 0; i < tags.length; i++)
            tags[i] = list.get(i).tag;
        return tags;
    }

    public String getErrorComment() {
        StringBuilder sb = new StringBuilder();
        if (notAllowedAttributes != null)
            return errorComment(sb, "Not allowed Attribute",
                    tagsOfNotAllowedAttributes()).toString();
        if (missingAttributes != null)
            return errorComment(sb, "Missing Attribute",
                    tagsOfMissingAttributes()).toString();
        if (missingAttributeValues != null)
            return errorComment(sb, "Missing Value of Attribute",
                    tagsOfMissingAttributeValues()).toString();
        if (invalidAttributeValues != null)
            return errorComment(sb, "Invalid Attribute",
                    tagsOfInvalidAttributeValues()).toString();
        return null;
    }

    private static StringBuilder errorComment(StringBuilder sb, String prompt,
            int[] tags) {
        sb.append(prompt);
        String prefix = tags.length > 1 ? "s: " : ": ";
        for (int tag : tags) {
            sb.append(prefix).append(TagUtils.toString(tag));
            prefix = ", ";
        }
        return sb;
    }

    @Override
    public String toString() {
        if (isValid())
            return "VALID";

        StringBuilder sb = new StringBuilder();
        if (notAllowedAttributes != null)
            errorComment(sb, "Not allowed Attribute",
                    tagsOfNotAllowedAttributes()).append(StringUtils.LINE_SEPARATOR);
        if (missingAttributes != null)
            errorComment(sb, "Missing Attribute",
                    tagsOfMissingAttributes()).append(StringUtils.LINE_SEPARATOR);
        if (missingAttributeValues != null)
            errorComment(sb, "Missing Value of Attribute",
                    tagsOfMissingAttributeValues()).append(StringUtils.LINE_SEPARATOR);
        if (invalidAttributeValues != null)
            errorComment(sb, "Invalid Attribute",
                    tagsOfInvalidAttributeValues()).append(StringUtils.LINE_SEPARATOR);

        return sb.substring(0, sb.length()-1);
    }

    public String asText(Attributes attrs) {
        if (isValid())
            return "VALID";

        StringBuilder sb = new StringBuilder();
        appendTextTo(0, attrs, sb);
        return sb.substring(0, sb.length()-1);
    }

    private void appendTextTo(int level, Attributes attrs, StringBuilder sb) {
        if (notAllowedAttributes != null)
            appendTextTo(level, attrs, "Not allowed Attributes:", notAllowedAttributes, sb);
        if (missingAttributes != null)
            appendTextTo(level, attrs, "Missing Attributes:", missingAttributes, sb);
        if (missingAttributeValues != null)
            appendTextTo(level, attrs, "Missing Attribute Values:", missingAttributeValues, sb);
        if (invalidAttributeValues != null)
            appendInvalidAttributeValues(level, attrs, "Invalid Attribute Values:", sb);
    }

    private void appendTextTo(int level, Attributes attrs, String title, 
            List<DataElement> list, StringBuilder sb) {
        appendPrefixTo(level, sb);
        sb.append(title).append(StringUtils.LINE_SEPARATOR);
        for (DataElement el : list) {
            appendAttribute(level, el.tag, sb);
            appendIODRef(el.getLineNumber(), sb);
            sb.append(StringUtils.LINE_SEPARATOR);
        }
    }

    private void appendIODRef(int lineNumber, StringBuilder sb) {
        if (lineNumber > 0)
            sb.append(" // IOD line #").append(lineNumber);
    }

    private void appendInvalidAttributeValues(int level, Attributes attrs,
            String title, StringBuilder sb) {
        appendPrefixTo(level, sb);
        sb.append(title);
        sb.append(StringUtils.LINE_SEPARATOR);
        for (InvalidAttributeValue iav : invalidAttributeValues) {
            int tag = iav.dataElement.tag;
            appendAttribute(level, tag, sb);
            VR.Holder vr = new VR.Holder();
            Object value = attrs.getValue(tag, vr);
            sb.append(' ').append(vr.vr);
            sb.append(" [");
            vr.vr.prompt(value,
                    attrs.bigEndian(), 
                    attrs.getSpecificCharacterSet(vr.vr), 200, sb);
            sb.append(']');
            if (iav.reason != Invalid.Item) {
                sb.append(" Invalid ").append(iav.reason);
                appendIODRef(iav.dataElement.getLineNumber(), sb);
            }
            sb.append(StringUtils.LINE_SEPARATOR);
            if (iav.missingItems != null) {
                for (IOD iod : iav.missingItems) {
                    appendPrefixTo(level+1, sb);
                    sb.append("Missing Item");
                    appendIODRef(iod.getLineNumber(), sb);
                    sb.append(StringUtils.LINE_SEPARATOR);
                }
            }
            if (iav.itemValidationResults != null) {
                Sequence seq = (Sequence) value;
                for (int i = 0; i < iav.itemValidationResults.length; i++) {
                    ValidationResult itemResult = iav.itemValidationResults[i];
                    if (!itemResult.isValid()) {
                        appendPrefixTo(level+1, sb);
                        sb.append("Invalid Item ").append(i+1).append(':')
                          .append(StringUtils.LINE_SEPARATOR);
                        itemResult.appendTextTo(level+1, seq.get(i), sb);
                    }
                }
            }
        }
    }

    private void appendAttribute(int level, int tag, StringBuilder sb) {
        appendPrefixTo(level, sb);
        sb.append(TagUtils.toString(tag))
          .append(' ')
          .append(ElementDictionary.keywordOf(tag, null));
    }

    private void appendPrefixTo(int level, StringBuilder sb) {
        while (level-- > 0)
            sb.append('>');
    }

}
