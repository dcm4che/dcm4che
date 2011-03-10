package org.dcm4che.io;

import java.io.IOException;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.ElementDictionary;
import org.dcm4che.data.Fragments;
import org.dcm4che.data.PersonName;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
import org.dcm4che.data.Value;
import org.dcm4che.util.TagUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class SAXWriter implements DicomInputHandler {

    private static final AttributesImpl NO_ATTS = new AttributesImpl();

    private ContentHandler ch;

    private char[] buffer = new char[1024];

    public SAXWriter(ContentHandler ch) {
        this.ch = ch;
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
        try {
            int tag = dis.tag();
            VR vr = dis.vr();
            int len = dis.length();
            ch.startElement("", "", "DicomAttribute",
                    atts(tag, attrs.getPrivateCreator(tag), vr));
            if (vr == VR.SQ || dis.length() == -1) {
                dis.readValue(dis, attrs);
            } else if (len > 0) {
                byte[] b = dis.readValue();
                vr.toXML(b, dis.bigEndian(), attrs.getSpecificCharacterSet(),
                        this);
                if (tag == Tag.FileMetaInformationGroupLength)
                    dis.setFileMetaInformationGroupLength(b);
                else if (tag == Tag.TransferSyntaxUID
                        || tag == Tag.SpecificCharacterSet
                        || TagUtils.isPrivateCreator(tag))
                    attrs.setBytes(tag, null, vr, b);
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
        String keyword = ElementDictionary.keywordOf(tag, privateCreator);
        if (keyword != null && !keyword.isEmpty())
            atts.addAttribute("", "", "keyword", "NMTOKEN", keyword);
        atts.addAttribute("", "", "tag", "NMTOKEN", TagUtils.toHexString(tag));
        if (privateCreator != null)
            atts.addAttribute("", "", "privateCreator", "NMTOKEN", privateCreator);
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
        try {
            frags.add(Value.EMPTY_BYTES); // increment size
            ch.startElement("", "", "DataFragment",
                    atts("number", Integer.toString(frags.size())));
            writeBase64(dis.readValue(), dis.bigEndian(),
                    frags.vr().numEndianBytes());
            ch.endElement("", "", "DataFragment");
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    public void writeValue(int index, String s) throws SAXException {
        writeElement("Value", atts("number", Integer.toString(index + 1)), s);
    }

    public void writeValueBase64(byte[] b, boolean bigEndian,
            int numEndianBytes) throws SAXException {
        ch.startElement("", "", "Value",  atts("number", "1"));
        writeBase64(b, bigEndian, numEndianBytes);
        ch.endElement("", "", "Value");
    }

    private void writeBase64(byte[] b, boolean bigEndian, int numEndianBytes)
            throws SAXException {
        // TODO
    }

    private void writeElement(String qname, AttributesImpl atts, String s)
            throws SAXException {
        if (s != null) {
            ch.startElement("", "", qname, atts); 
            for (int off = 0, totlen = s.length(); off < totlen;) {
                int len = Math.min(totlen - off, buffer.length);
                s.getChars(off, off += len, buffer, 0);
                ch.characters(buffer, 0, len);
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
