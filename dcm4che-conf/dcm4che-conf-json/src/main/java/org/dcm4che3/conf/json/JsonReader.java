/*
 * *** BEGIN LICENSE BLOCK *****
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
 * J4Care.
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.conf.json;

import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParsingException;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.DatePrecision;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.TimeZone;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Dec 2015
 */
public class JsonReader {
    private static final Logger LOG = LoggerFactory.getLogger(JsonConfiguration.class);
    private static final Code[] EMPTY_CODES = {};
    private static final int CONN_REF_INDEX_START = "/dicomNetworkConnection/".length();

    private final JsonParser parser;
    private JsonParser.Event event;
    private String s;

    public JsonReader(JsonParser parser) {
        this.parser = parser;
    }

    public static int toConnectionIndex(String connRef) {
        return Integer.parseInt(connRef.substring(CONN_REF_INDEX_START));
    }

    public JsonParser.Event next() {
        s = null;
        return event = parser.next();
    }

    public String getString() {
        if (s == null)
            s = parser.getString();
        return s;
    }

    public JsonParser.Event getEvent() {
        return event;
    }

    public JsonLocation getLocation() {
        return parser.getLocation();
    }

    public void expect(JsonParser.Event expected) {
        if (this.event != expected)
            throw new JsonParsingException("Unexpected " + event + ", expected " + expected, parser.getLocation());
    }

    public String stringValue() {
        next();
        expect(JsonParser.Event.VALUE_STRING);
        return getString();
    }


    public String[] stringArray() {
        next();
        expect(JsonParser.Event.START_ARRAY);
        ArrayList<String> a = new ArrayList<>();
        while (next() == JsonParser.Event.VALUE_STRING)
            a.add(getString());
        expect(JsonParser.Event.END_ARRAY);
        return a.toArray(StringUtils.EMPTY_STRING);
    }

    public <T extends Enum<T>> T[] enumArray(Class<T> enumType) {
        next();
        expect(JsonParser.Event.START_ARRAY);
        EnumSet<T> a = EnumSet.noneOf(enumType);
        while (next() == JsonParser.Event.VALUE_STRING)
            a.add(T.valueOf(enumType, getString()));
        expect(JsonParser.Event.END_ARRAY);
        return a.toArray((T[]) Array.newInstance(enumType, a.size()));
   }

    public long longValue() {
        next();
        expect(JsonParser.Event.VALUE_NUMBER);
        return Long.parseLong(getString());
    }

   public int intValue() {
        next();
        expect(JsonParser.Event.VALUE_NUMBER);
        return Integer.parseInt(getString());
    }

    public int[] intArray() {
        next();
        expect(JsonParser.Event.START_ARRAY);
        ArrayList<String> a = new ArrayList<>();
        while (next() == JsonParser.Event.VALUE_NUMBER) {
            a.add(getString());
        }
        expect(JsonParser.Event.END_ARRAY);
        int[] is = new int[a.size()];
        for (int i = 0; i < is.length; i++) {
            is[i] = Integer.parseInt(a.get(i));
        }
        return is;
    }

    public boolean booleanValue() {
        switch (next()) {
            case VALUE_FALSE:
                return false;
            case VALUE_TRUE:
                return true;
        }
        throw new JsonParsingException("Unexpected " + event
                + ", expected VALUE_FALSE or VALUE_TRUE", parser.getLocation());
    }

    public Issuer issuerValue() {
        return new Issuer(stringValue());
    }

    public Code[] codeArray() {
        next();
        expect(JsonParser.Event.START_ARRAY);
        ArrayList<Code> a = new ArrayList<>();
        while (next() == JsonParser.Event.VALUE_STRING)
            a.add(new Code(getString()));
        expect(JsonParser.Event.END_ARRAY);
        return a.toArray(EMPTY_CODES);
    }

    public TimeZone timeZoneValue() {
        return TimeZone.getTimeZone(stringValue());
    }

    public Date dateTimeValue() {
        return DateUtils.parseDT(null, stringValue(), new DatePrecision());
    }

    public void skipUnknownProperty() {
        LOG.warn("Skip unknown property: {}", s);
        skipValue();
    }

    private void skipValue() {
        int level = 0;
        do {
            switch (next()) {
                case START_ARRAY:
                case START_OBJECT:
                    level++;
                    break;
                case END_OBJECT:
                case END_ARRAY:
                    level--;
                    break;
            }
        } while (level > 0);
    }
}
