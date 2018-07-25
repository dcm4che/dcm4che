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
 * Portions created by the Initial Developer are Copyright (C) 2011-2014
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

package org.dcm4che3.json;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.json.stream.JsonGenerator;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.PersonName;
import org.dcm4che3.data.Value;
import org.dcm4che3.data.PersonName.Group;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.SpecificCharacterSet;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputHandler;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.Base64;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class JSONWriter implements DicomInputHandler {

    private static final Logger LOG = LoggerFactory.getLogger(JSONWriter.class);

    private final JsonGenerator gen;
    private final Deque<Boolean> hasItems = new ArrayDeque<Boolean>();
    private String replaceBulkDataURI;

    public JSONWriter(JsonGenerator gen) {
        this.gen = gen;
    }

    public String getReplaceBulkDataURI() {
        return replaceBulkDataURI;
    }

    public void setReplaceBulkDataURI(String replaceBulkDataURI) {
        this.replaceBulkDataURI = replaceBulkDataURI;
    }

    public void write(Attributes attrs) {
        final SpecificCharacterSet cs = attrs.getSpecificCharacterSet();
        gen.writeStartObject();
        try {
            attrs.accept(new Attributes.Visitor(){

                @Override
                public boolean visit(Attributes attrs, int tag, VR vr, Object value)
                        throws Exception {
                     writeAttribute(tag, vr, value, cs, attrs);
                     return true;
                }},
                false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        gen.writeEnd();
    }

    private void writeAttribute(int tag, VR vr, Object value,
            SpecificCharacterSet cs, Attributes attrs) {
        if (TagUtils.isGroupLength(tag))
            return;

        gen.writeStartObject(TagUtils.toHexString(tag));
        gen.write("vr", vr.name());
        if (value instanceof Value)
            writeValue((Value) value, attrs.bigEndian());
        else
            writeValue(vr, value, attrs.bigEndian(),
                    attrs.getSpecificCharacterSet(vr), true);
        gen.writeEnd();
    }

    private void writeValue(Value value, boolean bigEndian) {
        if (value.isEmpty())
            return;

        if (value instanceof Sequence) {
            gen.writeStartArray("Value");
            for (Attributes item : (Sequence) value) {
                write(item);
            }
            gen.writeEnd();
        } else if (value instanceof Fragments) {
            gen.writeStartArray("DataFragment");
            Fragments frags = (Fragments) value;
            for (Object frag : frags) {
                gen.writeStartObject();
                if (!(frag instanceof Value && ((Value) frag).isEmpty())) 
                    if (frag instanceof BulkData)
                        writeBulkData((BulkData) frag);
                    else {
                        writeInlineBinary(frags.vr(), (byte[]) frag, bigEndian, true);
                    }
                gen.writeEnd();
            }
            gen.writeEnd();
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
        if (TagUtils.isGroupLength(tag)) {
            dis.readValue(dis, attrs);
        } else if (dis.isExcludeBulkData()) {
            dis.readValue(dis, attrs);
        } else {
            gen.writeStartObject(TagUtils.toHexString(tag));
            gen.write("vr", vr.name());
            if (vr == VR.SQ || len == -1) {
                hasItems.addLast(false);
                dis.readValue(dis, attrs);
                if (hasItems.removeLast())
                    gen.writeEnd();
            } else if (len > 0) {
                if (dis.isIncludeBulkDataURI()) {
                    writeBulkData(dis.createBulkData(dis));
                } else {
                    byte[] b = dis.readValue();
                    if (tag == Tag.TransferSyntaxUID
                            || tag == Tag.SpecificCharacterSet)
                        attrs.setBytes(tag, vr, b);
                    writeValue(vr, b, dis.bigEndian(),
                                attrs.getSpecificCharacterSet(vr), false);
                 }
            }
            gen.writeEnd();
        }
    }

    private void writeValue(VR vr, Object val, boolean bigEndian,
            SpecificCharacterSet cs, boolean preserve) {
        switch (vr) {
        case AE:
        case AS:
        case AT:
        case CS:
        case DA:
        case DS:
        case DT:
        case IS:
        case LO:
        case LT:
        case PN:
        case SH:
        case ST:
        case TM:
        case UC:
        case UI:
        case UR:
        case UT:
            writeStringValues(vr, val, bigEndian, cs);
            break;
        case FL:
        case FD:
            writeDoubleValues(vr, val, bigEndian);
            break;
        case SL:
        case SS:
        case US:
            writeIntValues(vr, val, bigEndian);
            break;
        case UL:
            writeUIntValues(vr, val, bigEndian);
            break;
        case OB:
        case OD:
        case OF:
        case OL:
        case OW:
        case UN:
            writeInlineBinary(vr, (byte[]) val, bigEndian, preserve);
            break;
        case SQ:
            assert true;
        }
    }

    private void writeStringValues(VR vr, Object val, boolean bigEndian,
            SpecificCharacterSet cs) {
        gen.writeStartArray("Value");
        Object o = vr.toStrings(val, bigEndian, cs);
        String[] ss = (o instanceof String[])
                ? (String[]) o
                : new String[]{ (String) o };
        for (String s : ss) {
            if (s == null || s.isEmpty())
                gen.writeNull();
            else switch (vr) {
            case DS:
                try {
                    gen.write(StringUtils.parseDS(s));
                } catch (NumberFormatException e) {
                    LOG.info("illegal DS value: {} - encoded as null", s);
                    gen.writeNull();
                }
                break;
            case IS:
                try {
                    gen.write(StringUtils.parseIS(s));
                } catch (NumberFormatException e) {
                    LOG.info("illegal IS value: {} - encoded as null", s);
                    gen.writeNull();
                }
                break;
            case PN:
                writePersonName(s);
                break;
            default:
                gen.write(s);
            }
        }
        gen.writeEnd();
    }

    private void writeDoubleValues(VR vr, Object val, boolean bigEndian) {
        gen.writeStartArray("Value");
        int vm = vr.vmOf(val);
        for (int i = 0; i < vm; i++) {
            gen.write(vr.toDouble(val, bigEndian, i, 0));
        }
        gen.writeEnd();
    }

    private void writeIntValues(VR vr, Object val, boolean bigEndian) {
        gen.writeStartArray("Value");
        int vm = vr.vmOf(val);
        for (int i = 0; i < vm; i++) {
            gen.write(vr.toInt(val, bigEndian, i, 0));
        }
        gen.writeEnd();
    }

    private void writeUIntValues(VR vr, Object val, boolean bigEndian) {
        gen.writeStartArray("Value");
        int vm = vr.vmOf(val);
        for (int i = 0; i < vm; i++) {
            gen.write(vr.toInt(val, bigEndian, i, 0) & 0xffffffffL);
        }
        gen.writeEnd();
    }

    private void writePersonName(String s) {
        PersonName pn = new PersonName(s, true);
        gen.writeStartObject();
        writePNGroup("Alphabetic", pn, PersonName.Group.Alphabetic);
        writePNGroup("Ideographic", pn, PersonName.Group.Ideographic);
        writePNGroup("Phonetic", pn, PersonName.Group.Phonetic);
        gen.writeEnd();
    }

    private void writePNGroup(String name, PersonName pn, Group group) {
        if (pn.contains(group))
            gen.write(name, pn.toString(group, true));
    }

    private void writeInlineBinary(VR vr, byte[] b, boolean bigEndian,
            boolean preserve) {
        if (bigEndian)
            b = vr.toggleEndian(b, preserve);
        gen.write("InlineBinary", encodeBase64(b));
    }

    private String encodeBase64(byte[] b) {
        int len = (b.length * 4 / 3 + 3) & ~3;
        char[] ch = new char[len];
        Base64.encode(b, 0, b.length, ch, 0);
        return new String(ch);
    }

    private void writeBulkData(BulkData blkdata) {
        gen.write("BulkDataURI", replaceBulkDataURI != null ? replaceBulkDataURI : blkdata.getURI());
    }

    @Override
    public void readValue(DicomInputStream dis, Sequence seq)
            throws IOException {
        if (!hasItems.getLast()) {
            gen.writeStartArray("Value");
            hasItems.removeLast();
            hasItems.addLast(true);
        }
        gen.writeStartObject();
        dis.readValue(dis, seq);
        gen.writeEnd();
    }

    @Override
    public void readValue(DicomInputStream dis, Fragments frags)
            throws IOException {
        int len = dis.length();
        if (dis.isExcludeBulkData()) {
            dis.skipFully(len);
            return;
        }
        if (!hasItems.getLast()) {
            gen.writeStartArray("DataFragment");
            hasItems.removeLast();
            hasItems.add(true);
        }
        
        gen.writeStartObject();
        if (len > 0) {
            if (dis.isIncludeBulkDataURI()) {
                writeBulkData(dis.createBulkData(dis));
            } else {
                writeInlineBinary(frags.vr(), dis.readValue(), 
                        dis.bigEndian(), false);
            }
        }
        gen.writeEnd();
    }

    @Override
    public void startDataset(DicomInputStream dis) throws IOException {
        gen.writeStartObject();
    }

    @Override
    public void endDataset(DicomInputStream dis) throws IOException {
        gen.writeEnd();
    }
}
