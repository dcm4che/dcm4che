package org.dcm4che.io;

import java.io.IOException;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.ElementDictionary;
import org.dcm4che.data.Fragments;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
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
    public void startDataset() throws IOException {
        try {
            ch.startDocument();
            ch.startElement("", "", "NativeDicomModel",
                    atts("xml-space", "preserved"));
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void endDataset() throws IOException {
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
        if (TagUtils.isFileMetaInformation(tag)) {
            dis.readValue(dis, attrs);
        } else try {
            VR vr = dis.vr();
            ch.startElement("", "", "DicomAttribute",
                    atts(tag, attrs.getPrivateCreator(tag), vr));
            dis.readValue(dis, attrs);
            if (tag != Tag.SpecificCharacterSet
                    && TagUtils.isPrivateCreator(tag)) {
                attrs.remove(tag, null);
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
        dis.readValue(dis, seq);
    }

    @Override
    public void readValue(DicomInputStream dis, Fragments frags)
            throws IOException {
        dis.readValue(dis, frags);
    }

}
