package org.dcm4che.io;

import java.io.IOException;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.ElementDictionary;
import org.dcm4che.data.Fragments;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.VR;
import org.dcm4che.data.Value;
import org.dcm4che.util.TagUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class SAXWriter implements DicomInputHandler {

     private ContentHandler ch;

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
                // TODO
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
        if (keyword != null)
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
            // TODO
            ch.endElement("", "", "DataFragment");
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

}
