package org.dcm4che.io;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.BulkDataLocator;
import org.dcm4che.data.ElementDictionary;
import org.dcm4che.data.Fragments;
import org.dcm4che.data.PersonName;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.VR;
import org.dcm4che.data.Value;
import org.dcm4che.util.Base64;
import org.dcm4che.util.TagUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ContentHandlerAdapter extends DefaultHandler {

    private Attributes fmi;
    private final LinkedList<Attributes> items = new LinkedList<Attributes>();
    private final LinkedList<Sequence> seqs = new LinkedList<Sequence>();

    private final ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
    private final char[] carry = new char[3];
    private int carryLen;
    private final StringBuilder sb = new StringBuilder(64);
    private final ArrayList<String> values = new ArrayList<String>();
    private PersonName pn;
    private PersonName.Group pnGroup;
    private int tag;
    private String privateCreator;
    private VR vr;
    private String ts;
    private String uri;
    private long offset;
    private int length;
    private BulkDataLocator bulkDataLocator;
    private Fragments dataFragments;
    private boolean base64;

    public ContentHandlerAdapter(Attributes attrs) {
        if (attrs == null)
            throw new NullPointerException();
        items.add(attrs);
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
            if (qName.equals("BulkDataLocator"))
                startBulkDataLocator();
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
        case 'I':
            if (qName.equals("Item"))
                startItem(Integer.parseInt(atts.getValue("number")));
            else if (qName.equals("Ideographic"))
                startPNGroup(PersonName.Group.Ideographic);
            break;
        case 'P':
            if (qName.equals("PersonName")) {
                startPersonName(Integer.parseInt(atts.getValue("number")));
            } else if (qName.equals("Phonetic"))
                startPNGroup(PersonName.Group.Phonetic);
            break;
        case 'V':
            if (qName.equals("Value")) {
                startValue(Integer.parseInt(atts.getValue("number")));
            }
            break;
        }
   }

    private void startBulkDataLocator() {
        base64 = false;
    }

    private void startDicomAttribute(int tag, String privateCreator,
            String vr) {
        this.tag = tag;
        this.privateCreator = privateCreator;
        this.vr = vr != null ? VR.valueOf(vr)
                             : ElementDictionary.vrOf(tag, privateCreator);
        if (this.vr == VR.SQ)
            seqs.add(items.getLast().newSequence(tag, privateCreator, 10));
        else
            base64 = this.vr.isXMLBase64();
    }

    private void startDataFragment(int number) {
        if (dataFragments == null)
            dataFragments = new Fragments(vr, false, 10);
        while (dataFragments.size() < number-1)
            dataFragments.add(Value.EMPTY_BYTES);
        base64 = true;
    }

    private void startItem(int number) {
        Sequence seq = seqs.getLast();
        while (seq.size() < number)
            seq.add(new Attributes());
        items.add(seq.get(number-1));
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
    public void characters(char[] ch, int offset, int len)
            throws SAXException {
        if (base64) {
            int newCarryLen = (len + carryLen) & 3;
            Base64.decode(ch, offset, len - newCarryLen, carry, carryLen, bout);
            System.arraycopy(ch, offset + len - newCarryLen, carry, 0, newCarryLen);
            carryLen = newCarryLen;
        } else
            sb.append(ch, offset, len);
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        switch (qName.charAt(0)) {
        case 'B':
            if (qName.equals("BulkDataLocator"))
                endBulkDataLocator();
            break;
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
            break;
        case 'L':
            if (qName.equals("Length"))
                length = Integer.parseInt(getString());
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
        case 'O':
            if (qName.equals("Offset"))
                offset = Long.parseLong(getString());
            break;
        case 'P':
            if (qName.equals("PersonName"))
                endPersonName();
            break;
        case 'T':
            if (qName.equals("TransferSyntax")) {
                ts = getString();
            }
            break;
        case 'U':
            if (qName.equals("URI")) {
                uri = getString();
            }
            break;
        case 'V':
            if (qName.equals("Value")) {
                endValue();
            }
            break;
        }
    }

    private void endDataFragment() {
        if (bulkDataLocator != null) {
            dataFragments.add(bulkDataLocator);
            bulkDataLocator = null;
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
        if (bulkDataLocator != null) {
            attrs.setValue(tag, privateCreator, vr, bulkDataLocator);
            bulkDataLocator = null;
        }
        if (base64) {
            attrs.setBytes(tag, privateCreator, vr, getBytes());
        } else {
            attrs.setString(tag, privateCreator, vr, getStrings());
        }
    }

    private void endBulkDataLocator() {
        bulkDataLocator = new BulkDataLocator(uri, ts, offset, length);
    }

    private void endItem() {
        items.removeLast().trimToSize();
        vr = VR.SQ;
    }

    private void endPersonName() {
        values.add(pn.toString());
        pn = null;
    }

    private void endValue() {
        if (!base64)
            values.add(getString());
    }

    private void endPNComponent(PersonName.Component pnComp) {
        pn.set(pnGroup, pnComp, getString());
    }

    private String getString() {
        try {
            return sb.toString();
        } finally {
            sb.setLength(0);
        }
    }

    private byte[] getBytes() {
        try {
            return bout.toByteArray();
        } finally {
            bout.reset();
            base64 = false;
        }
    }

    private String[] getStrings() {
        try {
            return values.toArray(new String[values.size()]);
        } finally {;
            values.clear();
        }
    }

}