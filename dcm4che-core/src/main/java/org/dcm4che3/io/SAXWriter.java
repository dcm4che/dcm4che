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

import java.io.IOException;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.PersonName;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.SpecificCharacterSet;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Value;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.util.Base64;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.TagUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class SAXWriter implements DicomInputHandler {

    private static final String NAMESPACE = "http://dicom.nema.org/PS3.19/models/NativeDICOM";
    private static final int BASE64_CHUNK_LENGTH = 256 * 3;
    private static final int BUFFER_LENGTH = 256 * 4;
    
    private boolean includeKeyword = true;
    private String namespace = "";

    private final ContentHandler ch;
    private final AttributesImpl atts = new AttributesImpl();
    private final char[] buffer = new char[BUFFER_LENGTH];

    public SAXWriter(ContentHandler ch) {
        this.ch = ch;
    }

    public final boolean isIncludeKeyword() {
        return includeKeyword;
    }

    public final void setIncludeKeyword(boolean includeKeyword) {
        this.includeKeyword = includeKeyword;
    }

    public final boolean isIncludeNamespaceDeclaration() {
        return namespace == NAMESPACE;
    }

    public final void setIncludeNamespaceDeclaration(boolean includeNameSpaceDeclaration) {
        this.namespace = includeNameSpaceDeclaration ? NAMESPACE : "";
    }

    public void write(Attributes attrs) throws SAXException {
        startDocument();
        attrs.writeTo(this);
        endDocument();
    }

    @Override
    public void startDataset(DicomInputStream dis) throws IOException {
        try {
            startDocument();
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void endDataset(DicomInputStream dis) throws IOException {
        try {
            endDocument();
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    private void startDocument() throws SAXException {
        ch.startDocument();
        startElement("NativeDicomModel", "xml-space", "preserved");
    }

    private void endDocument() throws SAXException {
        endElement("NativeDicomModel");
        ch.endDocument();
    }

    private void startElement(String name, String attrName, int attrValue)
            throws SAXException {
        startElement(name, attrName, Integer.toString(attrValue));
    }

    private void startElement(String name, String attrName, String attrValue)
            throws SAXException {
        addAttribute(attrName, attrValue);
        startElement(name);
    }

    private void startElement(String name) throws SAXException {
        ch.startElement(namespace, name, name, atts);
        atts.clear();
    }

    private void endElement(String name) throws SAXException {
        ch.endElement(namespace, name, name);
    }

    private void addAttribute(String name, String value) {
        atts.addAttribute(namespace, name, name, "NMTOKEN", value);
    }

    public void writeAttribute(int tag, VR vr, Object value,
            SpecificCharacterSet cs, Attributes attrs) throws SAXException {
        if (TagUtils.isGroupLength(tag) || TagUtils.isPrivateCreator(tag))
            return;

        String privateCreator = attrs.getPrivateCreator(tag);
        addAttributes(tag, vr, privateCreator);
        startElement("DicomAttribute");
        if (value instanceof Value) {
            writeAttribute((Value) value, attrs.bigEndian());
        } else {
            vr.toXML(value, attrs.bigEndian(), cs, this);
        }
        endElement("DicomAttribute");
    }

    private void writeAttribute(Value value, boolean bigEndian)
            throws SAXException {
        if (value.isEmpty())
            return;

        if (value instanceof Sequence) {
            Sequence seq = (Sequence) value;
            int number = 0;
            for (Attributes item : seq) {
                startElement("Item", "number", ++number);
                item.writeTo(this);
                endElement("Item");
            }
        } else if (value instanceof Fragments) {
            Fragments frags = (Fragments) value;
            int number = 0;
            for (Object frag : frags) {
                ++number;
                if (frag instanceof Value && ((Value) frag).isEmpty())
                    continue;
                startElement("DataFragment", "number", number);
                if (frag instanceof BulkData)
                    writeBulkData((BulkData) frag);
                else {
                    byte[] b = (byte[]) frag;
                    if (bigEndian)
                        frags.vr().toggleEndian(b, true);
                    writeInlineBinary(b);
                }
                endElement("DataFragment");
            }
        } else if (value instanceof BulkData) {
            writeBulkData((BulkData) value);
        }
    }

    @Override
    public void readValue(DicomInputStream dis, Attributes attrs)
            throws IOException {
        int tag = dis.tag();
        VR vr = dis.vr();
        int len = dis.length();
        if (TagUtils.isGroupLength(tag) || TagUtils.isPrivateCreator(tag)) {
            dis.readValue(dis, attrs);
        } else if (dis.getIncludeBulkData() == IncludeBulkData.NO
                && dis.isBulkData(attrs)) {
            if (len == -1)
                dis.readValue(dis, attrs);
            else
                dis.skipFully(len);
        } else try {
            String privateCreator = attrs.getPrivateCreator(tag);
            addAttributes(tag, vr, privateCreator);
            startElement("DicomAttribute");
            if (vr == VR.SQ || len == -1) {
                dis.readValue(dis, attrs);
            } else if (len > 0) {
                if (dis.getIncludeBulkData() ==  IncludeBulkData.URI
                        && dis.isBulkData(attrs)) {
                    writeBulkData(dis.createBulkData());
                } else {
                    byte[] b = dis.readValue();
                    if (tag == Tag.TransferSyntaxUID
                            || tag == Tag.SpecificCharacterSet)
                        attrs.setBytes(tag, vr, b);
                    if (dis.bigEndian())
                        vr.toggleEndian(b, false);
                    vr.toXML(b, false, attrs.getSpecificCharacterSet(vr), this);
                }
            }
            endElement("DicomAttribute");
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    private void addAttributes(int tag, VR vr, String privateCreator) {
        if (privateCreator != null)
            tag &= 0xffff00ff;
        if (includeKeyword) {
            String keyword = ElementDictionary.keywordOf(tag, privateCreator);
            if (keyword != null && !keyword.isEmpty())
                addAttribute("keyword", keyword);
        }
        addAttribute("tag", TagUtils.toHexString(tag));
        if (privateCreator != null)
            addAttribute("privateCreator", privateCreator);
        addAttribute("vr", vr.name());
    }

    @Override
    public void readValue(DicomInputStream dis, Sequence seq)
            throws IOException {
        try {
            startElement("Item", "number", seq.size() + 1);
            dis.readValue(dis, seq);
            endElement("Item");
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void readValue(DicomInputStream dis, Fragments frags)
            throws IOException {
        int len = dis.length();
        if (dis.getIncludeBulkData() == IncludeBulkData.NO
                && dis.isBulkDataFragment(frags)) {
            dis.skipFully(len);
        } else try {
            frags.add(ByteUtils.EMPTY_BYTES); // increment size
            if (len > 0) {
                startElement("DataFragment", "number", frags.size());
                if (dis.getIncludeBulkData() == IncludeBulkData.URI
                        && dis.isBulkDataFragment(frags)) {
                    writeBulkData(dis.createBulkData());
                } else {
                    byte[] b = dis.readValue();
                    if (dis.bigEndian())
                        frags.vr().toggleEndian(b, false);
                    writeInlineBinary(b);
                }
                endElement("DataFragment");
            }
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    public void writeValue(int index, String s) throws SAXException {
        addAttribute("number", Integer.toString(index + 1));
        writeElement("Value", s);
    }

    public void writeInlineBinary(byte[] b) throws SAXException {
        startElement("InlineBinary");
        char[] buf = buffer;
        for (int off = 0; off < b.length;) {
            int len = Math.min(b.length - off, BASE64_CHUNK_LENGTH);
            Base64.encode(b, off, len, buf, 0);
            ch.characters(buf, 0, (len * 4 / 3 + 3) & ~3);
            off += len;
        }
        endElement("InlineBinary");
    }

    private void writeBulkData(BulkData bulkData)
            throws SAXException {
        if (bulkData.uuid != null)
            addAttribute("uuid", bulkData.uuid);
        if (bulkData.uri != null)
            addAttribute("uri", bulkData.uri);
        startElement("BulkData");
        endElement("BulkData");
    }

    private void writeElement(String qname, String s) throws SAXException {
        if (s != null) {
            startElement(qname); 
            char[] buf = buffer;
            for (int off = 0, totlen = s.length(); off < totlen;) {
                int len = Math.min(totlen - off, buf.length);
                s.getChars(off, off += len, buf, 0);
                ch.characters(buf, 0, len);
            }
            endElement(qname);
        }
    }

    public void writePersonName(int index, PersonName pn) throws SAXException {
        if (!pn.isEmpty()) {
            startElement("PersonName", "number", index + 1);
            writePNGroup("Alphabetic", pn, PersonName.Group.Alphabetic);
            writePNGroup("Ideographic", pn, PersonName.Group.Ideographic);
            writePNGroup("Phonetic", pn, PersonName.Group.Phonetic);
            endElement("PersonName");
        }
    }

    private void writePNGroup(String qname, PersonName pn,
            PersonName.Group group) throws SAXException {
        if (pn.contains(group)) {
            startElement(qname); 
            writeElement("FamilyName",
                    pn.get(group, PersonName.Component.FamilyName));
            writeElement("GivenName",
                    pn.get(group, PersonName.Component.GivenName));
            writeElement("MiddleName",
                    pn.get(group, PersonName.Component.MiddleName));
            writeElement("NamePrefix",
                    pn.get(group, PersonName.Component.NamePrefix));
            writeElement("NameSuffix",
                    pn.get(group, PersonName.Component.NameSuffix));
            endElement(qname);
        }
    }

}