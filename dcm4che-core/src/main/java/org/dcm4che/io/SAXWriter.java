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

package org.dcm4che.io;

import java.io.IOException;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.BulkDataLocator;
import org.dcm4che.data.ElementDictionary;
import org.dcm4che.data.Fragments;
import org.dcm4che.data.PersonName;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
import org.dcm4che.data.Value;
import org.dcm4che.util.Base64;
import org.dcm4che.util.TagUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class SAXWriter implements DicomInputHandler {

    private static final int BASE64_CHUNK_LENGTH = 256 * 3;
    private static final int BUFFER_LENGTH = 256 * 4;
    
    private static final AttributesImpl NO_ATTS = new AttributesImpl();

    private boolean includeKeyword = true;

    private ContentHandler ch;

    private char[] buffer = new char[BUFFER_LENGTH];

    public SAXWriter(ContentHandler ch) {
        this.ch = ch;
    }

    public final boolean isIncludeKeyword() {
        return includeKeyword;
    }

    public final void setIncludeKeyword(boolean includeKeyword) {
        this.includeKeyword = includeKeyword;
    }

    @Override
    public void startDataset(DicomInputStream dis) throws IOException {
        try {
            ch.startDocument();
            ch.startElement("", "", "NativeDicomModel",
                    atts("xml-space", "preserved"));
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void endDataset(DicomInputStream dis) throws IOException {
        try {
            ch.endElement("", "", "NativeDicomModel");
            ch.endDocument();
        } catch (SAXException e) {
            throw new IOException(e);
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
        } else if (!dis.isIncludeBulkData()
                && !dis.isIncludeBulkDataLocator()
                && dis.isBulkData(attrs)) {
            if (len == -1)
                dis.readValue(dis, attrs);
            else
                dis.skipFully(len);
        } else try {
            String privateCreator = attrs.getPrivateCreator(tag);
            ch.startElement("", "", "DicomAttribute",
                    atts(privateCreator != null ? tag & 0xffff00ff : tag,
                            privateCreator, vr));
            if (vr == VR.SQ || len == -1) {
                dis.readValue(dis, attrs);
            } else if (len > 0) {
                if (dis.isIncludeBulkDataLocator() && dis.isBulkData(attrs)) {
                    writeBulkDataLocator(dis.createBulkDataLocator());
                } else {
                    byte[] b = dis.readValue();
                    if (tag == Tag.TransferSyntaxUID
                            || tag == Tag.SpecificCharacterSet)
                        attrs.setBytes(tag, vr, b);
                    if (dis.bigEndian())
                        vr.toggleEndian(b, false);
                    vr.toXML(b, false, attrs.getSpecificCharacterSet(), this);
                }
            }
            ch.endElement("", "", "DicomAttribute");
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    private AttributesImpl atts(String qName, String value) {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "", qName, "NMTOKEN", value);
        return atts ;
    }

    private AttributesImpl atts(int tag, String privateCreator, VR vr) {
        AttributesImpl atts = new AttributesImpl();
        if (includeKeyword) {
            String keyword = ElementDictionary.keywordOf(tag, privateCreator);
            if (keyword != null && !keyword.isEmpty())
                atts.addAttribute("", "", "keyword", "NMTOKEN", keyword);
        }
        atts.addAttribute("", "", "tag", "NMTOKEN", TagUtils.toHexString(tag));
        if (privateCreator != null)
            atts.addAttribute("", "", "privateCreator", "NMTOKEN", 
                    privateCreator);
        atts.addAttribute("", "", "vr", "NMTOKEN", vr.name());
        return atts ;
    }

    @Override
    public void readValue(DicomInputStream dis, Sequence seq)
            throws IOException {
        try {
            ch.startElement("", "", "Item",
                    atts("number", Integer.toString(seq.size() + 1)));
            dis.readValue(dis, seq);
            ch.endElement("", "", "Item");
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void readValue(DicomInputStream dis, Fragments frags)
            throws IOException {
        int len = dis.length();
        if (!dis.isIncludeBulkData()
                && !dis.isIncludeBulkDataLocator()
                && dis.isBulkDataFragment()) {
            dis.skipFully(len);
        } else try {
            frags.add(Value.EMPTY_BYTES); // increment size
            if (len > 0) {
                ch.startElement("", "", "DataFragment",
                        atts("number", Integer.toString(frags.size())));
                if (dis.isIncludeBulkDataLocator() && dis.isBulkDataFragment()) {
                    writeBulkDataLocator(dis.createBulkDataLocator());
                } else {
                    byte[] b = dis.readValue();
                    if (dis.bigEndian())
                        frags.vr().toggleEndian(b, false);
                    writeValueBase64(b);
                }
                ch.endElement("", "", "DataFragment");
            }
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    public void writeValue(int index, String s) throws SAXException {
        writeElement("Value", atts("number", Integer.toString(index + 1)), s);
    }

    public void writeValueBase64(byte[] b) throws SAXException {
        ch.startElement("", "", "Value",  atts("number", "1"));
        char[] buf = buffer;
        for (int off = 0; off < b.length;) {
            int len = Math.min(b.length - off, BASE64_CHUNK_LENGTH);
            Base64.encode(b, off, len, buf, 0);
            ch.characters(buf, 0, (len * 4 / 3 + 3) & ~3);
            off += len;
        }
        ch.endElement("", "", "Value");
    }

    private void writeBulkDataLocator(BulkDataLocator bdl)
            throws SAXException {
        ch.startElement("", "", "BulkDataLocator", NO_ATTS); 
        writeElement("Length", NO_ATTS, Integer.toString(bdl.length));
        writeElement("Offset", NO_ATTS, Long.toString(bdl.offset));
        writeElement("TransferSyntax", NO_ATTS, bdl.transferSyntax);
        writeElement("URI", NO_ATTS, bdl.uri);
        ch.endElement("", "", "BulkDataLocator");
    }

    private void writeElement(String qname, AttributesImpl atts, String s)
            throws SAXException {
        if (s != null) {
            char[] buf = buffer;
            ch.startElement("", "", qname, atts); 
            for (int off = 0, totlen = s.length(); off < totlen;) {
                int len = Math.min(totlen - off, buf.length);
                s.getChars(off, off += len, buf, 0);
                ch.characters(buf, 0, len);
            }
            ch.endElement("", "", qname);
        }
    }

    public void writePersonName(int index, PersonName pn) throws SAXException {
        if (!pn.isEmpty()) {
            ch.startElement("", "", "PersonName",
                    atts("number", Integer.toString(index + 1)));
            writePNGroup("Alphabetic", pn, PersonName.Group.Alphabetic);
            writePNGroup("Ideographic", pn, PersonName.Group.Ideographic);
            writePNGroup("Phonetic", pn, PersonName.Group.Phonetic);
            ch.endElement("", "", "PersonName");
        }
    }

    private void writePNGroup(String qname, PersonName pn,
            PersonName.Group group) throws SAXException {
        if (!pn.isEmpty(group)) {
            ch.startElement("", "", qname, NO_ATTS); 
            writeElement("FamilyName", NO_ATTS,
                    pn.get(group, PersonName.Component.FamilyName));
            writeElement("GivenName", NO_ATTS,
                    pn.get(group, PersonName.Component.GivenName));
            writeElement("MiddleName", NO_ATTS,
                    pn.get(group, PersonName.Component.MiddleName));
            writeElement("NamePrefix", NO_ATTS,
                    pn.get(group, PersonName.Component.NamePrefix));
            writeElement("NameSuffix", NO_ATTS,
                    pn.get(group, PersonName.Component.NameSuffix));
            ch.endElement("", "", qname);
        }
    }

}
