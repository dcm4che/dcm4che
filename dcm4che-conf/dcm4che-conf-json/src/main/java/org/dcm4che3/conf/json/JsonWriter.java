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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
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

import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;

import javax.json.stream.JsonGenerator;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Dec 2015
 */
public class JsonWriter {
    private final JsonGenerator gen;

    public JsonWriter(JsonGenerator gen) {
        this.gen = gen;
    }

    public JsonGenerator writeStartObject() {
        return gen.writeStartObject();
    }

    public JsonGenerator writeStartObject(String name) {
        return gen.writeStartObject(name);
    }

    public JsonGenerator writeStartArray() {
        return gen.writeStartArray();
    }

    public JsonGenerator writeStartArray(String name) {
        return gen.writeStartArray(name);
    }

    public JsonGenerator write(String name, int value) {
        return gen.write(name, value);
    }

    public JsonGenerator write(String name, boolean value) {
        return gen.write(name, value);
    }

    public JsonGenerator writeEnd() {
        return gen.writeEnd();
    }

    public JsonGenerator write(String value) {
        return gen.write(value);
    }

    public void writeNotNull(String name, Object value) {
        if (value != null)
            gen.write(name, value.toString());
    }

    public void writeNotNull(String name, Boolean value) {
        if (value != null)
            gen.write(name, value.booleanValue());
    }

    public void writeNotNull(String name, TimeZone value) {
        if (value != null)
            gen.write(name, value.getID());
    }

    public void writeNotEmpty(String name, Object[] values) {
        if (values.length != 0) {
            gen.writeStartArray(name);
            for (Object value : values)
                gen.write(value.toString());
            gen.writeEnd();
        }
    }

    public void writeNotDef(String name, int value, int defVal) {
        if (value != defVal)
            gen.write(name, value);
    }

    public void writeNotDef(String name, boolean value, boolean defVal) {
        if (value != defVal)
            gen.write(name, value);
    }

    public void writeConnRefs(List<Connection> conns, List<Connection> refs) {
        writeStartArray("dicomNetworkConnectionReference");
        for (Connection ref : refs)
            write("/dicomNetworkConnection/" + conns.indexOf(ref));
        writeEnd();
    }
}
