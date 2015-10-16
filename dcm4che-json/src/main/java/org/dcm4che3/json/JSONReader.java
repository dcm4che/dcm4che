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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;

import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParsingException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.PersonName;
import org.dcm4che3.data.PersonName.Group;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.Base64;
import org.dcm4che3.util.TagUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class JSONReader {

    public interface Callback {

        void onDataset(Attributes fmi, Attributes dataset);

    }

    private final JsonParser parser;
    private boolean addBulkDataReferences;
    private Attributes fmi;
    private JsonLocation location;
    private Event event;
    private final ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
    private final ArrayList<String> stringValues = new ArrayList<String>();
    private final ArrayList<Number> numberValues = new ArrayList<Number>();
    private final EnumMap<Group, String> personNameGroups =
            new EnumMap<PersonName.Group, String>(PersonName.Group.class);

    public JSONReader(JsonParser parser) {
        this.parser = parser;
    }

    public boolean isAddBulkDataReferences() {
        return addBulkDataReferences;
    }

    public void setAddBulkDataReferences(boolean addBulkDataReferences) {
        this.addBulkDataReferences = addBulkDataReferences;
    }

    public Attributes getFileMetaInformation() {
        return fmi;
    }

    private Event next() {
        location = parser.getLocation();
        return event = parser.next();
    }

    private String getString() {
        location = parser.getLocation();
        return parser.getString();
    }

    public Attributes readDataset(Attributes attrs) {
        if (next() != Event.START_OBJECT) {
            throw new JsonParsingException("Unexpected " + event
                    + ", expected Data Set object", location);
        }
        if (attrs == null) {
            attrs = new Attributes();
        }
        fmi = null;
        doReadDataset(attrs);
        return attrs;
    }

    public void readDatasets(Callback callback) {
        if (next() != Event.START_ARRAY) {
            throw new JsonParsingException("Unexpected " + event
                    + ", expected array of Data Set objects", location);
        }
        Attributes attrs;
        for (;;) {
            switch (next()) {
            case END_ARRAY:
                return;
            case START_OBJECT:
                fmi = null;
                attrs = new Attributes();
                doReadDataset(attrs);
                callback.onDataset(fmi, attrs);
                break;
            default:
                throw new JsonParsingException("Unexpected " + event 
                        + ", expected Data Set object", location);
            }
        }
    }

    private void doReadDataset(Attributes attrs) {
        for (;;) {
            switch (next()) {
            case KEY_NAME:
                readAttribute(attrs);
                break;
            case END_OBJECT:
                attrs.trimToSize();
                return;
            default:
                throw new JsonParsingException("Unexpected " + event 
                        + ", expected Tag value", location);
            }
        }
    }

    private void readAttribute(Attributes attrs) {
        String key = getString();
        int tag = (int) Long.parseLong(key, 16);
        if (TagUtils.isFileMetaInformation(tag)) {
            if (fmi == null)
                fmi = new Attributes();
            attrs = fmi;
        }
        if (next() != Event.START_OBJECT) {
            throw new JsonParsingException("Unexpected " + event
                    + " expected attribute object", location);
        }
        if (next() != Event.KEY_NAME) {
            throw new JsonParsingException("Unexpected " + event
                    + "\", expected \"vr\"", location);
        }
        key = getString();
        if (!"vr".equals(key)) {
            throw new JsonParsingException("Unexpected \"" + key
                    + "\", expected: \"vr\"", location);
        }
        if (next() != Event.VALUE_STRING) {
            throw new JsonParsingException("Unexpected " + event
                    + " expected vr value", location);
        }
        VR vr = VR.valueOf(parser.getString());
        switch (next()) {
        case END_OBJECT:
            attrs.setNull(tag, vr);
            break;
        case KEY_NAME:
            key = getString();
            if ("Value".equals(key)) {
                switch (vr) {
                case AE:
                case AS:
                case AT:
                case CS:
                case DA:
                case DT:
                case LO:
                case LT:
                case SH:
                case ST:
                case TM:
                case UC:
                case UI:
                case UR:
                case UT:
                    readStringValues(attrs, tag, vr);
                    break;
                case DS:
                case FL:
                case FD:
                case IS:
                case SL:
                case SS:
                case UL:
                case US:
                    readNumberValues(attrs, tag, vr);
                    break;
                case PN:
                    readPersonNames(attrs, tag);
                    break;
                case SQ:
                    readSequence(attrs, tag);
                    break;
                case OB:
                case OD:
                case OF:
                case OL:
                case OW:
                case UN:
                    throw new JsonParsingException("Unexpected \"Value\""
                            + "\", expected \"InlineBinary\""
                            + " or \"BulkDataURI\" or  \"DataFragment\"", location);
                }
            } else if ("InlineBinary".equals(key)) {
                attrs.setBytes(tag, vr, readInlineBinary());
            } else if ("BulkDataURI".equals(key)) {
                BulkData bulkData = readBulkData(attrs.bigEndian());
                attrs.setValue(tag, vr,
                        bulkData.hasFragments()
                                ? bulkData.toFragments(attrs.privateCreatorOf(tag), tag, vr)
                                : bulkData);
                if (addBulkDataReferences)
                    attrs.getRoot().addBulkDataReference(
                            attrs.privateCreatorOf(tag), tag, vr, bulkData, attrs.itemPointers());

            } else if ("DataFragment".equals(key)) {
                readDataFragment(attrs, tag, vr);
            } else {
                throw new JsonParsingException("Unexpected \"" + key
                        + "\", expected \"Value\" or \"InlineBinary\""
                        + " or \"BulkDataURI\" or  \"DataFragment\"", location);
            }
            if (next() != Event.END_OBJECT) {
                throw new JsonParsingException("Unexpected " + event
                        + " expected end of attribute object", location);
            }
            break;
        default:
            throw new JsonParsingException("Unexpected " + event
                    + "\", expected \"Value\" or \"InlineBinary\""
                    + " or \"BulkDataURI\"", location);
        }
    }

    private void readStringValues(Attributes attrs, int tag, VR vr) {
        if (next() != Event.START_ARRAY) {
            throw new JsonParsingException("Unexpected " + event
                    + ", expected array of values", location);
        }
        for (;;) {
            switch (next()) {
            case END_ARRAY:
                attrs.setString(tag, vr,
                        stringValues.toArray(new String[stringValues.size()]));
                stringValues.clear();
                return;
            case VALUE_NULL:
                stringValues.add(null);
                break;
            case VALUE_STRING:
                stringValues.add(parser.getString());
                break;
            default:
                throw new JsonParsingException("Unexpected " + event 
                        + ", expected string value", location);
            }
        }
    }

    private void readNumberValues(Attributes attrs, int tag, VR vr) {
        if (next() != Event.START_ARRAY) {
            throw new JsonParsingException("Unexpected " + event
                    + ", expected array of values", location);
        }
        for (;;) {
            switch (next()) {
            case END_ARRAY:
                switch(vr) {
                case DS:
                case FL:
                case FD:
                    attrs.setDouble(tag, vr, toDoubles(numberValues));
                    break;
                case IS:
                case SL:
                case SS:
                case UL:
                case US:
                    attrs.setInt(tag, vr, toInts(numberValues));
                    break;
                default:
                    assert true;
                }
                numberValues.clear();
                return;
            case VALUE_NUMBER:
                numberValues.add(parser.getBigDecimal());
                break;
            default:
                throw new JsonParsingException("Unexpected " + event 
                        + ", expected number value", location);
            }
        }
    }

    private double[] toDoubles(ArrayList<Number> values) {
        double[] ds = new double[values.size()];
        for (int i = 0; i < ds.length; i++) {
            ds[i] = values.get(i).doubleValue();
        }
        return ds;
    }

    private int[] toInts(ArrayList<Number> values) {
        int[] is = new int[values.size()];
        for (int i = 0; i < is.length; i++) {
            is[i] = values.get(i).intValue();
        }
        return is;
    }

    private void readPersonNames(Attributes attrs, int tag) {
        if (next() != Event.START_ARRAY) {
            throw new JsonParsingException("Unexpected " + event
                    + ", expected array of person name objects", location);
        }
        for (;;) {
            switch (next()) {
            case END_ARRAY:
                attrs.setString(tag, VR.PN,
                        stringValues.toArray(new String[stringValues.size()]));
                stringValues.clear();
                return;
            case VALUE_NULL:
                stringValues.add(null);
                break;
            case START_OBJECT:
                stringValues.add(readPersonName());
                break;
            default:
                throw new JsonParsingException("Unexpected " + event 
                        + ", expected person name object", location);
            }
        }
    }

    private String readPersonName() {
        PersonName.Group key;
        for (;;) {
            switch (next()) {
            case END_OBJECT:
                String retval = toString(personNameGroups);
                personNameGroups.clear();
                return retval;
            case KEY_NAME:
                try {
                    key = PersonName.Group.valueOf(getString());
                } catch (IllegalArgumentException e) {
                    throw new JsonParsingException("Unexpected \"" + e.getMessage()
                            + "\", expected \"Alphabetic\" or \"Ideographic\""
                            + " or \"Phonetic\"", location);
                }
                if (next() != Event.VALUE_STRING) {
                    throw new JsonParsingException("Unexpected " + event
                            + "\", expected person name value", location);
                }
                personNameGroups.put(key, parser.getString());
                break;
            default:
                 throw new JsonParsingException("Unexpected " + event
                         + ", expected \"Alphabetic\" or \"Ideographic\""
                         + " or \"Phonetic\"", location);
            }
        }
    }

    private String toString(EnumMap<Group, String> groups) {
        String s = groups.get(PersonName.Group.Alphabetic);
        if (s != null && groups.size() == 1)
            return s;
        
        StringBuilder sb = new StringBuilder(64);
        if (s != null)
            sb.append(s);
        
        sb.append('=');
        s = groups.get(PersonName.Group.Ideographic);
        if (s != null)
            sb.append(s);

        s = groups.get(PersonName.Group.Phonetic);
        if (s != null)
            sb.append('=').append(s);
        
        return sb.toString();
    }

    private void readSequence(Attributes attrs, int tag) {
        final Sequence seq = attrs.newSequence(tag, 10);
        Attributes fmi0 = fmi;
        readDatasets(new Callback(){

            @Override
            public void onDataset(Attributes fmi, Attributes item) {
                seq.add(item);
            }});

        fmi = fmi0;
        seq.trimToSize();
    }

    private byte[] readInlineBinary() {
        if (next() != Event.VALUE_STRING) {
            throw new JsonParsingException("Unexpected " + event
                    + ", expected bulk data URI", location);
        }
        char[] base64 = parser.getString().toCharArray();
        bout.reset();
        try {
            Base64.decode(base64, 0, base64.length, bout);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bout.toByteArray();
    }

    private BulkData readBulkData(boolean bigEndian) {
        if (next() != Event.VALUE_STRING) {
            throw new JsonParsingException("Unexpected " + event
                    + ", expected bulk data URI", location);
        }
        String uri = parser.getString();
        return new BulkData(null, uri, bigEndian);
    }

    private void readDataFragment(Attributes attrs, int tag, VR vr) {
        if (next() != Event.START_ARRAY) {
            throw new JsonParsingException("Unexpected " + event
                    + ", expected array of data fragment objects", location);
        }
        Fragments frags = attrs.newFragments(tag, vr, 10);
        for (;;) {
            switch (next()) {
            case END_ARRAY:
                frags.trimToSize();
                return;
            case VALUE_NULL:
                frags.add(null);
                break;
            case START_OBJECT:
                frags.add(readDataFragment(attrs.bigEndian()));
                break;
            default:
                throw new JsonParsingException("Unexpected " + event 
                        + ", expected data fragment object", location);
            }
        }
    }

    private Object readDataFragment(boolean bigEndian) {
        Event event = next();
        switch (event) {
        case KEY_NAME:
            break;
        case END_OBJECT:
            return null;
            default: throw new JsonParsingException("Unexpected " + event
                    + ", expected \"InlineBinary\""
                    + " or \"BulkDataURI\"", location);
        }
        
        if (event != Event.KEY_NAME) {
            throw new JsonParsingException("Unexpected " + event
                    + ", expected \"InlineBinary\""
                    + " or \"BulkDataURI\"", location);
        }
        String key = getString();
        Object value;
        if ("BulkDataURI".equals(key)) {
            value = readBulkData(bigEndian);
        } else if ("InlineBinary".equals(key)) {
            value = readInlineBinary();
        } else {
            throw new JsonParsingException("Unexpected \"" + key
                    + "\", expected \"InlineBinary\""
                    + " or \"BulkDataURI\"", location);
        }
        if (next() != Event.END_OBJECT) {
            throw new JsonParsingException("Unexpected " + event
                    + " expected end of data fragment object", location);
        }
        return value;
    }

}
