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

package org.dcm4che3.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.PersonName;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.Base64;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.TagUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class ContentHandlerAdapter extends DefaultHandler {

    private Attributes fmi;
    private final boolean bigEndian;
    private final LinkedList<Attributes> items = new LinkedList<Attributes>();
    private final LinkedList<Sequence> seqs = new LinkedList<Sequence>();

    private final ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
    private final char[] carry = new char[4];
    private int carryLen;
    private final StringBuilder sb = new StringBuilder(64);
    private final ArrayList<String> values = new ArrayList<String>();
    private PersonName pn;
    private PersonName.Group pnGroup;
    private int tag;
    private String privateCreator;
    private VR vr;
    private BulkData bulkData;
    private Fragments dataFragments;
    private boolean processCharacters;
    private boolean inlineBinary;

    public ContentHandlerAdapter(Attributes attrs) {
        if (attrs == null)
            throw new NullPointerException();
        items.add(attrs);
        bigEndian = attrs.bigEndian();
    }

    public Attributes getFileMetaInformation() {
        return fmi;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            org.xml.sax.Attributes atts) throws SAXException {
        switch (qName.charAt(0)) {
        case 'A':
            if (qName.equals("Alphabetic"))
                startPNGroup(PersonName.Group.Alphabetic);
            break;
        case 'B':
            if (qName.equals("BulkData"))
                bulkData(atts.getValue("uuid"), atts.getValue("uri"));
            break;
        case 'D':
            if (qName.equals("DicomAttribute"))
                startDicomAttribute(
                        (int) Long.parseLong(atts.getValue("tag"), 16),
                        atts.getValue("privateCreator"),
                        atts.getValue("vr"));
            else if (qName.equals("DataFragment"))
                startDataFragment(Integer.parseInt(atts.getValue("number")));
            break;
        case 'F':
            if (qName.equals("FamilyName"))
                startText();
            break;
        case 'G':
            if (qName.equals("GivenName"))
                startText();
            break;
        case 'I':
            if (qName.equals("Item"))
                startItem(Integer.parseInt(atts.getValue("number")));
            else if (qName.equals("InlineBinary"))
                startInlineBinary();
            else if (qName.equals("Ideographic"))
                startPNGroup(PersonName.Group.Ideographic);
            break;
        case 'L':
            if (qName.equals("Length"))
                startText();
            break;
        case 'M':
            if (qName.equals("MiddleName"))
                startText();
            break;
        case 'N':
            if (qName.equals("NamePrefix") || qName.equals("NameSuffix"))
                startText();
            break;
        case 'O':
            if (qName.equals("Offset"))
                startText();
            break;
        case 'P':
            if (qName.equals("PersonName")) {
                startPersonName(Integer.parseInt(atts.getValue("number")));
            } else if (qName.equals("Phonetic"))
                startPNGroup(PersonName.Group.Phonetic);
            break;
        case 'T':
            if (qName.equals("TransferSyntax"))
                startText();
            break;
        case 'U':
            if (qName.equals("URI"))
                startText();
            break;
        case 'V':
            if (qName.equals("Value")) {
                startValue(Integer.parseInt(atts.getValue("number")));
                startText();
            }
            break;
        }
   }

    private void bulkData(String uuid, String uri) {
        bulkData = new BulkData(uuid, uri, items.getLast().bigEndian());
    }

    private void startInlineBinary() {
        processCharacters = true;
        inlineBinary = true;
        bout.reset();
    }

    private void startText() {
        processCharacters = true;
        inlineBinary = false;
        sb.setLength(0);
    }

    private void startDicomAttribute(int tag, String privateCreator,
            String vr) {
        this.tag = tag;
        this.privateCreator = privateCreator;
        this.vr = vr != null ? VR.valueOf(vr)
                             : ElementDictionary.vrOf(tag, privateCreator);
        if (this.vr == VR.SQ)
            seqs.add(items.getLast().newSequence(privateCreator, tag, 10));
    }

    private void startDataFragment(int number) {
        if (dataFragments == null)
            dataFragments = items.getLast()
                    .newFragments(privateCreator, tag, vr,  10);
        while (dataFragments.size() < number-1)
            dataFragments.add(ByteUtils.EMPTY_BYTES);
    }

    private void startItem(int number) {
        Sequence seq = seqs.getLast();
        while (seq.size() < number-1)
            seq.add(new Attributes(0));
        Attributes item = new Attributes();
        seq.add(item);
        items.add(item);
    }

    private void startValue(int number) {
        while (values.size() < number-1)
            values.add(null);
    }

    private void startPersonName(int number) {
        startValue(number);
        pn = new PersonName();
    }

    private void startPNGroup(PersonName.Group pnGroup) {
        this.pnGroup = pnGroup;
    }

    @Override
    public void characters(char[] ch, int offset, int len) throws SAXException {
        if (processCharacters) {
            if (inlineBinary) {
                processBinary(ch, offset, len);
            } else {
                sb.append(ch, offset, len);
            }
        }
    }

    /**
     * Processes Base64-encoded binary data. (See http://www.w3.org/TR/2004/PER-xmlschema-2-20040318/#base64Binary)
     *
     * Since the XML schema definition for base64 data consists of quartets of characters, this process copies any
     * "overflow" characters into a "carry" array of length 4. That is, if the number of characters being copied is 10,
     * the last 2 characters (since 10 % 4 = 2) will be copied into the carry. When the next set of characters is read,
     * the carry will be filled out to complete the quartet and then decoded and written to the contained OutputStream.
     *
     * Since the carry is only read out on subsequent calls to processBinary, it is possible that some data could be
     * left unwritten in the carry buffer if the inline binary data is malformed (not a multiple of 4). The
     * endInlineBinary method throws a SAXException if the carry contains any unwritten characters when the inline
     * binary element is finished.
     *
     * @param   chars   The characters from the XML document, representing Base64-encoded binary data.
     * @param   offset  The start position in the array
     * @param   length  The number of characters to read from the array
     */
    protected void processBinary(char[] chars, int offset, int length) {
        // Strip out whitespace and adjust the length to the number of (non-whitespace) characters actually read
        char[] scrubbed = new char[length];
        length = scrubWhitespace(chars, offset, scrubbed, 0, length);
        chars = scrubbed;
        offset = 0;

        try {
            if (carryLen != 0) {
                // Figure out how many characters are needed to fill the carry
                int copy = 4 - carryLen;

                if (copy > length) {
                    // Can't fill carry, since we're reading fewer than the number of bytes left
                    // Read all that we can into the carry
                    System.arraycopy(chars, offset, carry, carryLen, length);

                    // Adjust the carry length to include the extra characters
                    carryLen += length;

                    // Return, since we've read everything
                    return;
                }

                // Fill out the carry
                System.arraycopy(chars, offset, carry, carryLen, copy);

                // Copy the carry into the output stream
                Base64.decode(carry, 0, 4, bout);

                // Adjust the offset and the remaining characters to be read
                offset += copy;
                length -= copy;
            }

            // See if the characters to be read are divisible by 4
            if ((carryLen = length & 3) != 0) {
                // Adjust the length by the carry overflow
                length -= carryLen;

                // Copy the overflow (from the end of the read portion) into the carry
                System.arraycopy(chars, offset + length, carry, 0, carryLen);
            }

            // Read the characters into the output stream
            Base64.decode(chars, offset, length, bout);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Copies the source array to the target, removing any XML whitespace characters
     * (see http://www.w3.org/TR/2000/WD-xml-2e-20000814#sec-common-syn).
     *
     * @param   source          The source character array
     * @param   sourceOffset    The position in the source array to start copying from
     * @param   target          The target character array
     * @param   targetOffset    The position in the target array to start copying to
     * @param   length          The number of characters to copy
     * @return                  The total number of characters copied
     */
    protected int scrubWhitespace(char[] source, int sourceOffset, char[] target, int targetOffset, int length) {
        int sourceStart = sourceOffset;
        int sourceEnd = sourceStart + length;
        int copiedCount = 0;

        for (int i = sourceStart; i < sourceEnd; i++) {
            // Check for XML whitespace
            switch (source[i]) {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    // Copy everything up to this point
                    System.arraycopy(source, sourceStart, target, targetOffset + copiedCount, (i - sourceStart));

                    copiedCount += (i - sourceStart);
                    sourceStart = i + 1; // Move to the character after this (skipping the whitespace character)

                    break;
                default:
                    // Do nothing
            }
        }

        // Copy everything remaining
        int finalCopy = sourceEnd - sourceStart;
        System.arraycopy(source, sourceStart, target, targetOffset + copiedCount, finalCopy);

        return (copiedCount + finalCopy);
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        switch (qName.charAt(0)) {
        case 'D':
            if (qName.equals("DicomAttribute"))
                endDicomAttribute();
            else if (qName.equals("DataFragment"))
                endDataFragment();
            break;
        case 'F':
            if (qName.equals("FamilyName"))
                endPNComponent(PersonName.Component.FamilyName);
            break;
        case 'G':
            if (qName.equals("GivenName"))
                endPNComponent(PersonName.Component.GivenName);
            break;
        case 'I':
            if (qName.equals("Item"))
                endItem();
            else if (qName.equals("InlineBinary"))
                endInlineBinary();
            break;
        case 'M':
            if (qName.equals("MiddleName"))
                endPNComponent(PersonName.Component.MiddleName);
            break;
        case 'N':
            if (qName.equals("NamePrefix"))
                endPNComponent(PersonName.Component.NamePrefix);
            else if (qName.equals("NameSuffix"))
                endPNComponent(PersonName.Component.NameSuffix);
            break;
        case 'P':
            if (qName.equals("PersonName"))
                endPersonName();
            break;
        case 'V':
            if (qName.equals("Value")) {
                endValue();
            }
            break;
        }
        processCharacters = false;
    }

    @Override
    public void endDocument() throws SAXException {
        if (fmi != null)
            fmi.trimToSize();
        items.getFirst().trimToSize();
    }

    private void endDataFragment() {
        if (bulkData != null) {
            dataFragments.add(bulkData);
            bulkData = null;
        } else {
            dataFragments.add(getBytes());
        }
    }

    private void endDicomAttribute() {
        if (vr == VR.SQ) {
            seqs.removeLast().trimToSize();
            return;
        }
        if (dataFragments != null) {
            dataFragments.trimToSize();
            dataFragments = null;
            return;
        }
        Attributes attrs = items.getLast();
        if (TagUtils.isFileMetaInformation(tag)) {
            if (fmi == null)
                fmi = new Attributes();
            attrs = fmi;
        }
        if (bulkData != null) {
            attrs.setValue(privateCreator, tag, vr, bulkData);
            bulkData = null;
        } else if (inlineBinary) {
            attrs.setBytes(privateCreator, tag, vr, getBytes());
        } else {
            attrs.setString(privateCreator, tag, vr, getStrings());
        }
    }

    private void endItem() {
        items.removeLast().trimToSize();
        vr = VR.SQ;
    }

    private void endInlineBinary() throws SAXException {
        if (carryLen != 0) {
            // Attempting to end the inline binary section while we still have leftover characters in the carry
            throw new SAXException("Inline binary data contained invalid number of characters");
        }
    }

    private void endPersonName() {
        values.add(pn.toString());
        pn = null;
    }

    private void endValue() {
        values.add(getString());
    }

    private void endPNComponent(PersonName.Component pnComp) {
        pn.set(pnGroup, pnComp, getString());
    }

    private String getString() {
        return sb.toString();
    }

    private byte[] getBytes() {
        byte[] b = bout.toByteArray();
        return bigEndian ? vr.toggleEndian(b, false) : b;
    }

    private String[] getStrings() {
        try {
            return values.toArray(new String[values.size()]);
        } finally {;
            values.clear();
        }
    }

}