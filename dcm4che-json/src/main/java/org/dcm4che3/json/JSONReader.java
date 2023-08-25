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

import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;
import jakarta.json.stream.JsonParsingException;
import org.dcm4che3.data.*;
import org.dcm4che3.data.PersonName.Group;
import org.dcm4che3.util.Base64;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.function.ToLongFunction;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class JSONReader {

    private static final Logger LOG = LoggerFactory.getLogger(JSONReader.class);

    public interface Callback {

        void onDataset(Attributes fmi, Attributes dataset);

    }

    private final JsonParser parser;
    private boolean skipBulkDataURI;
    private BulkData.Creator bulkDataCreator = BulkData::new;
    private Attributes fmi;
    private Event event;
    private String s;
    private int level = -1;
    private final ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
    private final EnumMap<Group, String> pnGroups = new EnumMap<>(PersonName.Group.class);

    public JSONReader(JsonParser parser) {
        this.parser = Objects.requireNonNull(parser);
    }

    public boolean isSkipBulkDataURI() {
        return skipBulkDataURI;
    }

    public void setSkipBulkDataURI(boolean skipBulkDataURI) {
        this.skipBulkDataURI = skipBulkDataURI;
    }

    public void setBulkDataCreator(BulkData.Creator bulkDataCreator ) {
        this.bulkDataCreator = Objects.requireNonNull(bulkDataCreator);
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
        boolean wrappedInArray = next() == Event.START_ARRAY;
        if (wrappedInArray) next();
        expect(Event.START_OBJECT);
        if (attrs == null) {
            attrs = new Attributes();
        }
        fmi = null;
        next();
        doReadDataset(attrs);
        if (wrappedInArray) next();
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
        level++;
        while (event == JsonParser.Event.KEY_NAME) {
            readAttribute(attrs);
            next();
        }
        expect(JsonParser.Event.END_OBJECT);
        attrs.trimToSize();
        level--;
        return attrs;
    }

    private void readAttribute(Attributes attrs) {
        int tag = (int) Long.parseLong(getString(), 16);
        if (level == 0 && TagUtils.isFileMetaInformation(tag)) {
            if (fmi == null)
                fmi = new Attributes();
            attrs = fmi;
        }
        next();
        expect(Event.START_OBJECT);
        Element el = new Element();
        while (next() == JsonParser.Event.KEY_NAME) {
            switch (getString()) {
                case "vr":
                    try {
                        el.vr = VR.valueOf(valueString());
                    } catch (IllegalArgumentException e) {
                        el.vr = ElementDictionary.getStandardElementDictionary().vrOf(tag);
                        LOG.info("Invalid vr: '{}' at {} - treat as '{}'",getString(), parser.getLocation(), el.vr);
                    }
                    break;
                case "Value":
                    el.values = readValues();
                    break;
                case "InlineBinary":
                    el.bytes = readInlineBinary();
                    break;
                case "BulkDataURI":
                    el.bulkDataURI = valueString();
                    break;
                case "DataFragment":
                    el.values = readDataFragments();
                    break;
                default:
                    throw new JsonParsingException("Unexpected \"" + getString()
                            + "\", expected \"Value\" or \"InlineBinary\""
                            + " or \"BulkDataURI\" or  \"DataFragment\"", parser.getLocation());
            }
        }
        expect(JsonParser.Event.END_OBJECT);
        if (el.vr == null) {
            el.vr = ElementDictionary.getStandardElementDictionary().vrOf(tag);
            LOG.info("Missing property: vr at {} - treat as '{}'", parser.getLocation(), el.vr);
        }
        if (el.isEmpty())
            attrs.setNull(tag, el.vr);
        else if (el.bulkDataURI != null) {
            if (!skipBulkDataURI)
                attrs.setValue(tag, el.vr, bulkDataCreator.create(null, el.bulkDataURI, false));
        } else switch (el.vr) {
            case AE:
            case AS:
            case AT:
            case CS:
            case DA:
            case DS:
            case DT:
            case LO:
            case LT:
            case PN:
            case IS:
            case SH:
            case ST:
            case TM:
            case UC:
            case UI:
            case UR:
            case UT:
                attrs.setString(tag, el.vr, el.toStrings());
                break;
            case FL:
            case FD:
                attrs.setDouble(tag, el.vr, el.toDoubles());
                break;
            case SL:
            case SS:
            case UL:
            case US:
                attrs.setInt(tag, el.vr, el.toInts());
                break;
            case SV:
                attrs.setLong(tag, el.vr, el.toLongs(Long::parseLong));
                break;
            case UV:
                attrs.setLong(tag, el.vr, el.toLongs(Long::parseUnsignedLong));
                break;
            case SQ:
                el.toItems(attrs.newSequence(tag, el.values.size()));
                break;
            case OB:
            case OD:
            case OF:
            case OL:
            case OV:
            case OW:
            case UN:
                if (el.bytes != null)
                    attrs.setBytes(tag, el.vr, el.bytes);
                else
                    el.toFragments(attrs.newFragments(tag, el.vr, el.values.size()));
        }
    }

    private List<Object> readValues() {
        ArrayList<Object> list = new ArrayList<>();
        next();
        if( this.event == Event.VALUE_STRING ) {
            LOG.info("Missing value array at {} - treat as single value", parser.getLocation());
            list.add(getString());
            return list;
        }
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
        ArrayList<Object> list = new ArrayList<>();
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
        byte[] bytes = null;
        String bulkDataURI = null;
        while (next() == Event.KEY_NAME) {
            switch (getString()) {
                case "BulkDataURI":
                    bulkDataURI = valueString();
                    break;
                case "InlineBinary":
                    bytes = readInlineBinary();
                    break;
                default:
                    throw new JsonParsingException("Unexpected \"" + getString()
                            + "\", expected \"InlineBinary\""
                            + " or \"BulkDataURI\"", parser.getLocation());
            }
        }
        expect(Event.END_OBJECT);
        return bulkDataURI != null && !skipBulkDataURI
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
                Number number = (Number) values.get(i);
                double d;
                if (number == null) {
                    LOG.info("decode {} null as NaN", vr);
                    d = Double.NaN;
                } else {
                    d = number.doubleValue();
                    if (d == -Double.MAX_VALUE) {
                        LOG.info("decode {} {} as -Infinity", vr, d);
                        d = Double.NEGATIVE_INFINITY;
                    } else if (d == Double.MAX_VALUE) {
                        LOG.info("decode {} {} as Infinity", vr, d);
                        d = Double.POSITIVE_INFINITY;
                    }
                }
                ds[i] = d;
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

        long[] toLongs(ToLongFunction<String> parse) {
            long[] ls = new long[values.size()];
            for (int i = 0; i < ls.length; i++) {
                ls[i] = longValueOf(parse, values.get(i));
            }
            return ls;
        }

        private long longValueOf(ToLongFunction<String> string2long, Object o) {
            return o instanceof Number ? ((Number) o).longValue() : string2long.applyAsLong((String) o);
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
