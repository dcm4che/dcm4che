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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

import org.dcm4che3.data.*;
import org.dcm4che3.data.PersonName.Group;
import org.dcm4che3.util.Base64;
import org.dcm4che3.util.TagUtils;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParsingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class JSONReader {

    public interface Callback {

        void onDataset(Attributes fmi, Attributes dataset);

    }

    private final JsonParser parser;
    private Attributes fmi;
    private Event event;
    private String s;
    private final ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
    private final EnumMap<Group, String> pnGroups = new EnumMap<Group, String>(PersonName.Group.class);

    public JSONReader(JsonParser parser) {
        this.parser = parser;
    }

    public Attributes getFileMetaInformation() {
        return fmi;
    }

    private Event next() {
        s = null;
        return event = parser.next();
    }

    private String getString() {
        if (s == null)
            s = parser.getString();
        return s;
    }

    private void expect(Event expected) {
        if (this.event != expected)
            throw new JsonParsingException("Unexpected " + event + ", expected " + expected, parser.getLocation());
    }

    private String valueString() {
        next();
        expect(JsonParser.Event.VALUE_STRING);
        return getString();
    }

    public Attributes readDataset(Attributes attrs) {
        next();
        expect(Event.START_OBJECT);
        if (attrs == null) {
            attrs = new Attributes();
        }
        fmi = null;
        next();
        doReadDataset(attrs);
        return attrs;
    }

    public void readDatasets(Callback callback) {
        next();
        expect(Event.START_ARRAY);
        Attributes attrs;
        while (next() == JsonParser.Event.START_OBJECT) {
            fmi = null;
            attrs = new Attributes();
            next();
            doReadDataset(attrs);
            callback.onDataset(fmi, attrs);
        }
        expect(JsonParser.Event.END_ARRAY);
    }

    private Attributes doReadDataset(Attributes attrs) {
        while (event == JsonParser.Event.KEY_NAME) {
            readAttribute(attrs);
            next();
        }
        expect(JsonParser.Event.END_OBJECT);
        attrs.trimToSize();
        return attrs;
    }

    private void readAttribute(Attributes attrs) {
        int tag = (int) Long.parseLong(getString(), 16);
        if (TagUtils.isFileMetaInformation(tag)) {
            if (fmi == null)
                fmi = new Attributes();
            attrs = fmi;
        }
        next();
        expect(Event.START_OBJECT);
        Element el = new Element();
        while (next() == JsonParser.Event.KEY_NAME) {
            String key = getString();
            if (key.equals("vr"))
                try {
                    el.vr = VR.valueOf(valueString());
                } catch (IllegalArgumentException e) {
                    throw new JsonParsingException("Invalid vr: " + key, parser.getLocation());
                }
            else if (key.equals("Value"))
                el.values = readValues();
            else if (key.equals("InlineBinary"))
                el.bytes = readInlineBinary();
            else if (key.equals("BulkDataURI"))
                el.bulkDataURI = valueString();
            else if (key.equals("DataFragment"))
                el.values = readDataFragments();
            else
                throw new JsonParsingException("Unexpected \"" + key
                        + "\", expected \"Value\" or \"InlineBinary\""
                        + " or \"BulkDataURI\" or  \"DataFragment\"", parser.getLocation());
        }
        expect(JsonParser.Event.END_OBJECT);
        if (el.vr == null)
            throw new JsonParsingException("Missing property: vr", parser.getLocation());

        if (el.isEmpty())
            attrs.setNull(tag, el.vr);
        else switch (el.vr) {
            case AE:
            case AS:
            case AT:
            case CS:
            case DA:
            case DT:
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
                attrs.setString(tag, el.vr, el.toStrings());
                break;
            case DS:
            case FL:
            case FD:
                attrs.setDouble(tag, el.vr, el.toDoubles());
                break;
            case IS:
            case SL:
            case SS:
            case UL:
            case US:
                attrs.setInt(tag, el.vr, el.toInts());
                break;
            case SQ:
                el.toItems(attrs.newSequence(tag, el.values.size()));
                break;
            case OB:
            case OD:
            case OF:
            case OL:
            case OW:
            case UN:
                if (el.bytes != null)
                    attrs.setBytes(tag, el.vr, el.bytes);
                else if (el.bulkDataURI != null) {
                    BulkData bulkData = new BulkData(null, el.bulkDataURI, false);
                        attrs.setValue(tag, el.vr, bulkData.hasFragments()
                                ? bulkData.toFragments(null, tag, el.vr)
                                : bulkData);
                } else
                    el.toFragments(attrs.newFragments(tag, el.vr, el.values.size()));
        }
    }

    private List<Object> readValues() {
        ArrayList<Object> list = new ArrayList<Object>();
        next();
        expect(Event.START_ARRAY);
        while (next() != Event.END_ARRAY) {
            switch (event) {
                case START_OBJECT:
                    list.add(readItemOrPersonName());
                    break;
                case VALUE_STRING:
                    list.add(parser.getString());
                    break;
                case VALUE_NUMBER:
                    list.add(parser.getBigDecimal());
                    break;
                case VALUE_NULL:
                    list.add(null);
                    break;
                default:
                    throw new JsonParsingException("Unexpected " + event, parser.getLocation());
            }
        }
        return list;
    }

    private List<Object> readDataFragments() {
        ArrayList<Object> list = new ArrayList<Object>();
        next();
        expect(Event.START_ARRAY);
        while (next() != Event.END_ARRAY) {
            switch (event) {
                case START_OBJECT:
                    list.add(readDataFragment());
                    break;
                case VALUE_NULL:
                    list.add(null);
                    break;
                default:
                    throw new JsonParsingException("Unexpected " + event, parser.getLocation());
            }
        }
        return list;
    }

    private Object readItemOrPersonName() {
        if (next() != JsonParser.Event.KEY_NAME)
            return null;

        return (getString().length() == 8)
                ? doReadDataset(new Attributes())
                : readPersonName();
    }

    private String readPersonName() {
        pnGroups.clear();
        while (event == JsonParser.Event.KEY_NAME) {
            Group key;
            try {
                key = PersonName.Group.valueOf(getString());
            } catch (IllegalArgumentException e) {
                throw new JsonParsingException("Unexpected \"" + getString()
                        + "\", expected \"Alphabetic\" or \"Ideographic\""
                        + " or \"Phonetic\"", parser.getLocation());
            }
            pnGroups.put(key, valueString());
            next();
        }
        expect(JsonParser.Event.END_OBJECT);
        String s = pnGroups.get(PersonName.Group.Alphabetic);
        if (s != null && pnGroups.size() == 1)
            return s;
        
        StringBuilder sb = new StringBuilder(64);
        if (s != null)
            sb.append(s);
        
        sb.append('=');
        s = pnGroups.get(PersonName.Group.Ideographic);
        if (s != null)
            sb.append(s);

        s = pnGroups.get(PersonName.Group.Phonetic);
        if (s != null)
            sb.append('=').append(s);
        
        return sb.toString();
    }

    private byte[] readInlineBinary() {
        char[] base64 = valueString().toCharArray();
        bout.reset();
        try {
            Base64.decode(base64, 0, base64.length, bout);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bout.toByteArray();
    }

    private Object readDataFragment() {
        next();
        byte[] bytes = null;
        String bulkDataURI = null;
        while (next() != Event.KEY_NAME) {
            String key = getString();
            if (key.equals("BulkDataURI"))
                bulkDataURI = valueString();
            else if (key.equals("InlineBinary"))
                bytes = readInlineBinary();
            else
                throw new JsonParsingException("Unexpected \"" + key
                        + "\", expected \"InlineBinary\""
                        + " or \"BulkDataURI\"", parser.getLocation());
        }
        expect(Event.END_OBJECT);
        return bulkDataURI != null
                ? new BulkData(null, bulkDataURI, false)
                : bytes;
    }

    private static class Element {
        VR vr;
        List<Object> values;
        byte[] bytes;
        String bulkDataURI;

        boolean isEmpty() {
            return (values == null || values.isEmpty()) && (bytes == null || bytes.length == 0) && bulkDataURI == null;
        }

        String[] toStrings() {
            String[] ss = new String[values.size()];
            for (int i = 0; i < ss.length; i++) {
                Object value = values.get(i);
                ss[i] = value != null ? value.toString() : null;
            }
            return ss;
        }

        double[] toDoubles() {
            double[] ds = new double[values.size()];
            for (int i = 0; i < ds.length; i++) {
                ds[i] = ((Number) values.get(i)).doubleValue();
            }
            return ds;
        }

        int[] toInts() {
            int[] is = new int[values.size()];
            for (int i = 0; i < is.length; i++) {
                is[i] = ((Number) values.get(i)).intValue();
            }
            return is;
        }

        void toItems(Sequence seq) {
            for (Object value : values) {
                seq.add(value != null ? (Attributes) value : new Attributes(0));
            }
        }

        void toFragments(Fragments fragments) {
            for (Object value : values) {
                fragments.add(value);
            }
        }

    }
}
